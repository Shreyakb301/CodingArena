package com.codingarena.server

import com.codingarena.domain.model.Assignment
import com.codingarena.domain.model.AuthResponse
import com.codingarena.domain.model.ClassMembership
import com.codingarena.domain.model.Classroom
import com.codingarena.domain.model.CodeSubmission
import com.codingarena.domain.model.CreateSubmissionRequest
import com.codingarena.domain.model.ProgrammingLanguage
import com.codingarena.domain.model.ProgressSyncPayload
import com.codingarena.domain.model.RegisterRequest
import com.codingarena.domain.model.SubmissionAccepted
import com.codingarena.domain.model.SubmissionStatus
import com.codingarena.domain.model.TestResult
import com.codingarena.domain.model.UserRole
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ServerApiTest {
    @Test
    fun `authenticated submission contract enforces source limit and hides hidden cases`() = testApplication {
        val store = MemoryStore()
        val runner = object : CodeRunner {
            override suspend fun execute(
                language: ProgrammingLanguage,
                source: String,
                tests: List<ExecutionTest>,
            ): ExecutionResult = ExecutionResult(
                status = SubmissionStatus.PASSED,
                publicTests = tests.filterNot { it.hidden }.map {
                    TestResult(it.name, true, it.expected, it.expected)
                },
                hiddenPassed = tests.count { it.hidden },
                hiddenTotal = tests.count { it.hidden },
            )
        }
        application {
            module(
                ServerDependencies(
                    ServerConfig(jwtSecret = "test-secret"), store, runner,
                    CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
                )
            )
        }
        val api = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val auth = api.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("Student", "student@example.com", "long-password", UserRole.STUDENT))
        }.body<AuthResponse>()

        val tooLarge = api.post("/v1/submissions") {
            bearerAuth(auth.token); contentType(ContentType.Application.Json)
            setBody(CreateSubmissionRequest("variables-independent", ProgrammingLanguage.PYTHON, "x".repeat(65_537)))
        }
        assertEquals(HttpStatusCode.BadRequest, tooLarge.status)

        val accepted = api.post("/v1/submissions") {
            bearerAuth(auth.token); contentType(ContentType.Application.Json)
            setBody(CreateSubmissionRequest("variables-independent", ProgrammingLanguage.PYTHON, "print(4)"))
        }.body<SubmissionAccepted>()
        val saved = store.submission(accepted.id)!!
        assertTrue(saved.status == SubmissionStatus.PASSED || saved.status == SubmissionStatus.RUNNING)
        if (saved.status == SubmissionStatus.PASSED) {
            assertEquals(1, saved.publicTests.size)
            assertEquals(4, saved.hiddenTotal)
            assertTrue(saved.publicTests.none { it.name.startsWith("Hidden") })
        }
    }

    @Test
    fun `submission ownership prevents cross student access`() {
        val store = MemoryStore()
        val runner = object : CodeRunner {
            override suspend fun execute(language: ProgrammingLanguage, source: String, tests: List<ExecutionTest>) =
                ExecutionResult(SubmissionStatus.PASSED, emptyList(), 0, 0)
        }
        val service = SubmissionService(store, runner, CoroutineScope(Dispatchers.Unconfined)) { 1L }
        val accepted = service.create(
            "student-a",
            CreateSubmissionRequest("variables-independent", ProgrammingLanguage.PYTHON, "print(4)"),
        )
        assertFailsWith<NoSuchElementException> { service.get("student-b", accepted.id) }
    }

    @Test
    fun `password hashing is salted and verifiable`() {
        val first = Passwords.hash("safe-password")
        val second = Passwords.hash("safe-password")
        assertTrue(first != second)
        assertTrue(Passwords.verify("safe-password", first))
        assertTrue(!Passwords.verify("wrong-password", first))
    }

    @Test
    fun `submission service enforces per student rate limit`() {
        val store = MemoryStore()
        val runner = object : CodeRunner {
            override suspend fun execute(language: ProgrammingLanguage, source: String, tests: List<ExecutionTest>) =
                ExecutionResult(SubmissionStatus.PASSED, emptyList(), 0, 0)
        }
        val service = SubmissionService(store, runner, CoroutineScope(Dispatchers.Unconfined)) { 1_000L }
        repeat(SubmissionService.USER_RATE_LIMIT) {
            service.create(
                "student",
                CreateSubmissionRequest("variables-independent", ProgrammingLanguage.PYTHON, "print(4)"),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.create(
                "student",
                CreateSubmissionRequest("variables-independent", ProgrammingLanguage.PYTHON, "print(4)"),
            )
        }
    }
}

private class MemoryStore : ArenaStore {
    val users = mutableMapOf<String, UserAccount>()
    val submissions = mutableMapOf<String, CodeSubmission>()
    val classes = mutableMapOf<String, Classroom>()
    val memberships = mutableListOf<ClassMembership>()
    val assigned = mutableListOf<Assignment>()
    val progress = mutableMapOf<String, ProgressSyncPayload>()

    override fun createUser(request: RegisterRequest): UserAccount = UserAccount(
        "u-${users.size}", request.displayName, request.email, request.role, Passwords.hash(request.password),
    ).also { users[it.email!!] = it }
    override fun userByEmail(email: String) = users[email]
    override fun saveSubmission(submission: CodeSubmission) { submissions[submission.id] = submission }
    override fun submission(id: String) = submissions[id]
    override fun createClassroom(teacherId: String, name: String, now: Long) =
        Classroom("c-${classes.size}", teacherId, name, "CODE", now).also {
            classes[it.id] = it
            memberships += ClassMembership(it.id, teacherId, UserRole.TEACHER, "Teacher", now)
        }
    override fun classrooms(userId: String) = memberships.filter { it.userId == userId }.mapNotNull { classes[it.classroomId] }
    override fun joinClassroom(userId: String, displayName: String, inviteCode: String, now: Long) =
        classes.values.firstOrNull { it.inviteCode == inviteCode }?.also {
            memberships += ClassMembership(it.id, userId, UserRole.STUDENT, displayName, now)
        }
    override fun membership(classroomId: String, userId: String) =
        memberships.firstOrNull { it.classroomId == classroomId && it.userId == userId }
    override fun createAssignment(teacherId: String, assignment: Assignment) = assignment.also { assigned += it }
    override fun assignments(classroomId: String, userId: String) = assigned.filter { it.classroomId == classroomId }
    override fun members(classroomId: String, teacherId: String) = memberships.filter { it.classroomId == classroomId }
    override fun saveProgress(userId: String, payload: ProgressSyncPayload) { progress[userId] = payload }
    override fun progress(userIds: List<String>) = progress.filterKeys { it in userIds }
    override fun auditSubmission(submissionId: String, userId: String, event: String, occurredAt: Long) = Unit
}
