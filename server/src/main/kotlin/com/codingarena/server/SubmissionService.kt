package com.codingarena.server

import com.codingarena.domain.model.CodeSubmission
import com.codingarena.domain.model.CreateSubmissionRequest
import com.codingarena.domain.model.SubmissionAccepted
import com.codingarena.domain.model.SubmissionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.ArrayDeque

class SubmissionService(
    private val store: ArenaStore,
    private val runner: CodeRunner,
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val recentByUser = ConcurrentHashMap<String, ArrayDeque<Long>>()

    fun create(userId: String, request: CreateSubmissionRequest): SubmissionAccepted {
        enforceUserRateLimit(userId)
        require(request.sourceCode.toByteArray().size <= MAX_SOURCE_BYTES) { "Source code exceeds 64 KB" }
        val tests = ExecutionTests.forRequest(request)
        val now = clock()
        val submission = CodeSubmission(
            id = UUID.randomUUID().toString(),
            userId = userId,
            problemId = request.problemId,
            language = request.language,
            sourceCode = request.sourceCode,
            status = SubmissionStatus.QUEUED,
            createdAt = now,
            updatedAt = now,
        )
        store.saveSubmission(submission)
        store.auditSubmission(submission.id, userId, "created", now)
        scope.launch {
            store.saveSubmission(submission.copy(status = SubmissionStatus.RUNNING, updatedAt = clock()))
            store.auditSubmission(submission.id, userId, "running", clock())
            val completed = runCatching { runner.execute(request.language, request.sourceCode, tests) }
                .fold(
                    onSuccess = { result -> submission.copy(
                        status = result.status,
                        publicTests = result.publicTests,
                        hiddenPassed = result.hiddenPassed,
                        hiddenTotal = result.hiddenTotal,
                        compilerOutput = result.compilerOutput,
                        runtimeOutput = result.runtimeOutput,
                        updatedAt = clock(),
                    ) },
                    onFailure = { error -> submission.copy(
                        status = SubmissionStatus.SYSTEM_ERROR,
                        runtimeOutput = error.message?.take(MAX_OUTPUT_CHARS),
                        updatedAt = clock(),
                    ) },
                )
            store.saveSubmission(completed)
            store.auditSubmission(submission.id, userId, "completed:${completed.status}", clock())
        }
        return SubmissionAccepted(submission.id, submission.status)
    }

    fun get(userId: String, id: String): CodeSubmission = store.submission(id)
        ?.takeIf { it.userId == userId }
        ?: throw NoSuchElementException("Submission not found")

    fun cancel(userId: String, id: String): CodeSubmission {
        val current = get(userId, id)
        if (current.status !in setOf(SubmissionStatus.QUEUED, SubmissionStatus.COMPILING, SubmissionStatus.RUNNING)) {
            return current
        }
        return current.copy(status = SubmissionStatus.CANCELLED, updatedAt = clock())
            .also {
                store.saveSubmission(it)
                store.auditSubmission(id, userId, "cancelled", clock())
            }
    }

    private fun enforceUserRateLimit(userId: String) {
        val now = clock()
        val queue = recentByUser.getOrPut(userId) { ArrayDeque() }
        synchronized(queue) {
            while (queue.isNotEmpty() && queue.first() <= now - RATE_WINDOW_MS) queue.removeFirst()
            require(queue.size < USER_RATE_LIMIT) { "Too many submissions; wait one minute" }
            queue.addLast(now)
        }
    }

    companion object {
        const val MAX_SOURCE_BYTES = 64 * 1024
        const val USER_RATE_LIMIT = 10
        private const val RATE_WINDOW_MS = 60_000L
        private const val MAX_OUTPUT_CHARS = 8_192
    }
}
