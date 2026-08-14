package com.codingarena.server

import com.codingarena.content.ArenaCourse
import com.codingarena.domain.model.CreateSubmissionRequest
import com.codingarena.domain.model.ProgrammingLanguage
import com.codingarena.domain.model.SubmissionPurpose
import com.codingarena.domain.model.SubmissionStatus
import com.codingarena.domain.model.TestResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ExecutionTest(val name: String, val input: String, val expected: String, val hidden: Boolean)

data class ExecutionResult(
    val status: SubmissionStatus,
    val publicTests: List<TestResult>,
    val hiddenPassed: Int,
    val hiddenTotal: Int,
    val compilerOutput: String? = null,
    val runtimeOutput: String? = null,
)

interface CodeRunner {
    suspend fun execute(language: ProgrammingLanguage, source: String, tests: List<ExecutionTest>): ExecutionResult
}

class Judge0CodeRunner(
    private val client: HttpClient,
    private val config: ServerConfig,
) : CodeRunner {
    override suspend fun execute(
        language: ProgrammingLanguage,
        source: String,
        tests: List<ExecutionTest>,
    ): ExecutionResult {
        var compilerOutput: String? = null
        var runtimeOutput: String? = null
        var infrastructureFailure = false
        val results = tests.map { test ->
            val response = client.post(
                "${config.judge0Url}/submissions?base64_encoded=false&wait=true&fields=stdout,stderr,compile_output,message,status,time"
            ) {
                contentType(ContentType.Application.Json)
                config.judge0Token?.let { header("X-Auth-Token", it) }
                setBody(
                    Judge0Request(
                        sourceCode = source,
                        languageId = languageIds.getValue(language),
                        stdin = test.input,
                        expectedOutput = test.expected,
                    )
                )
            }.body<Judge0Response>()
            compilerOutput = compilerOutput ?: response.compileOutput?.truncated()
            runtimeOutput = runtimeOutput ?: response.stderr?.truncated() ?: response.message?.truncated()
            infrastructureFailure = infrastructureFailure || response.status.id in INFRASTRUCTURE_STATUSES
            val passed = response.status.id == ACCEPTED_STATUS
            TestResult(
                name = test.name,
                passed = passed,
                actualOutput = if (test.hidden) null else response.stdout?.trim()?.truncated(),
                expectedOutput = if (test.hidden) null else test.expected,
                durationMs = response.time?.toDoubleOrNull()?.let { (it * 1000).toLong() },
            )
        }
        return ExecutionResult(
            status = when {
                infrastructureFailure -> SubmissionStatus.SYSTEM_ERROR
                results.all { it.passed } -> SubmissionStatus.PASSED
                else -> SubmissionStatus.FAILED
            },
            publicTests = results.filterIndexed { index, _ -> !tests[index].hidden },
            hiddenPassed = results.filterIndexed { index, _ -> tests[index].hidden }.count { it.passed },
            hiddenTotal = tests.count { it.hidden },
            compilerOutput = compilerOutput,
            runtimeOutput = runtimeOutput,
        )
    }

    private fun String.truncated(): String = take(MAX_OUTPUT_CHARS)

    companion object {
        private const val ACCEPTED_STATUS = 3
        private val INFRASTRUCTURE_STATUSES = setOf(13, 14)
        private const val MAX_OUTPUT_CHARS = 8_192
        private val languageIds = mapOf(
            ProgrammingLanguage.PYTHON to 71,
            ProgrammingLanguage.JAVA to 62,
            ProgrammingLanguage.JAVASCRIPT to 63,
            ProgrammingLanguage.KOTLIN to 78,
            ProgrammingLanguage.CPP to 54,
            ProgrammingLanguage.GO to 60,
            ProgrammingLanguage.SWIFT to 83,
        )
    }
}

@Serializable
private data class Judge0Request(
    @SerialName("source_code") val sourceCode: String,
    @SerialName("language_id") val languageId: Int,
    val stdin: String,
    @SerialName("expected_output") val expectedOutput: String,
    @SerialName("cpu_time_limit") val cpuTimeLimit: Double = 5.0,
    @SerialName("wall_time_limit") val wallTimeLimit: Double = 10.0,
    @SerialName("memory_limit") val memoryLimit: Int = 262_144,
    @SerialName("max_file_size") val maxFileSize: Int = 1_024,
    @SerialName("enable_network") val enableNetwork: Boolean = false,
)

@Serializable
private data class Judge0Status(val id: Int, val description: String)

@Serializable
private data class Judge0Response(
    val stdout: String? = null,
    val stderr: String? = null,
    @SerialName("compile_output") val compileOutput: String? = null,
    val message: String? = null,
    val status: Judge0Status,
    val time: String? = null,
)

object ExecutionTests {
    fun forRequest(request: CreateSubmissionRequest): List<ExecutionTest> {
        val exercise = ArenaCourse.course.exercise(request.problemId)
            ?: throw IllegalArgumentException("Unknown problem")
        require(request.language in exercise.supportedLanguages) { "Language not supported" }
        val public = exercise.examples.mapIndexed { index, test ->
            ExecutionTest("Example ${index + 1}", test.input, test.expectedOutput, hidden = false)
        }
        if (request.purpose == SubmissionPurpose.RUN) return public
        return public + hidden.getValue(request.problemId).mapIndexed { index, (input, expected) ->
            ExecutionTest("Hidden ${index + 1}", input, expected, hidden = true)
        }
    }

    private val hidden: Map<String, List<Pair<String, String>>> = mapOf(
        "variables-independent" to listOf("[-9,-2,-4]" to "-2", "[5]" to "5", "[0,0]" to "0", "[1,9,3]" to "9"),
        "conditionals-independent" to listOf("12" to "1", "-1" to "-1", "0" to "0", "999" to "1"),
        "loops-independent" to listOf("[]" to "0", "[-1,1]" to "0", "[5]" to "5", "[1,2,3,4]" to "10"),
        "functions-independent" to listOf("0" to "true", "-2" to "true", "9" to "false", "101" to "false"),
        "debugging-independent" to listOf("[]" to "0", "[-8]" to "-8", "[0,-1]" to "0", "[2,9,1]" to "9"),
        "arrays-independent" to listOf("[]; 1" to "-1", "[1]; 1" to "0", "[1,1]; 1" to "0", "[2,3]; 4" to "-1"),
        "sets-maps-independent" to listOf("[]" to "false", "[0,0]" to "true", "[-1,1]" to "false", "[3,2,3]" to "true"),
        "hashing-independent" to listOf("[3,2,4]; 6" to "[1,2]", "[-1,-2,-3]; -5" to "[1,2]", "[0,4,3,0]; 0" to "[0,3]", "[2,5]; 7" to "[0,1]"),
    )
}
