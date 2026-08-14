package com.codingarena.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SubmissionStatus {
    QUEUED,
    COMPILING,
    RUNNING,
    PASSED,
    FAILED,
    SYSTEM_ERROR,
    CANCELLED,
}

@Serializable
enum class SubmissionPurpose { RUN, SUBMIT }

@Serializable
data class CreateSubmissionRequest(
    val problemId: String,
    val language: ProgrammingLanguage,
    val sourceCode: String,
    val purpose: SubmissionPurpose = SubmissionPurpose.SUBMIT,
)

@Serializable
data class SubmissionAccepted(val id: String, val status: SubmissionStatus)

@Serializable
data class TestResult(
    val name: String,
    val passed: Boolean,
    val actualOutput: String? = null,
    val expectedOutput: String? = null,
    val durationMs: Long? = null,
)

@Serializable
data class CodeSubmission(
    val id: String,
    val userId: String,
    val problemId: String,
    val language: ProgrammingLanguage,
    val sourceCode: String,
    val status: SubmissionStatus,
    val publicTests: List<TestResult> = emptyList(),
    val hiddenPassed: Int = 0,
    val hiddenTotal: Int = 0,
    val compilerOutput: String? = null,
    val runtimeOutput: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class CodeDraft(
    val userId: String,
    val problemId: String,
    val language: ProgrammingLanguage,
    val sourceCode: String,
    val updatedAt: Long,
    val synced: Boolean = false,
)
