package com.codingarena.data.remote

import com.codingarena.domain.model.CodeSubmission
import com.codingarena.domain.model.CreateSubmissionRequest
import com.codingarena.domain.model.SubmissionAccepted
import com.codingarena.domain.submission.SubmissionGateway
import com.codingarena.domain.submission.SubmissionUnavailableException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess

class KtorSubmissionGateway(
    private val client: HttpClient,
    private val baseUrl: String,
    private val token: suspend () -> String?,
) : SubmissionGateway {
    override suspend fun create(request: CreateSubmissionRequest): SubmissionAccepted = call {
        client.post("$baseUrl/v1/submissions") {
            token()?.let(::bearerAuth)
            setBody(request)
        }.let { response ->
            if (!response.status.isSuccess()) error("Submission rejected: ${response.status.value}")
            response.body()
        }
    }

    override suspend fun get(id: String): CodeSubmission = call {
        client.get("$baseUrl/v1/submissions/$id") { token()?.let(::bearerAuth) }.let { response ->
            if (!response.status.isSuccess()) error("Submission unavailable: ${response.status.value}")
            response.body()
        }
    }

    override suspend fun cancel(id: String): CodeSubmission = call {
        client.post("$baseUrl/v1/submissions/$id/cancel") { token()?.let(::bearerAuth) }.let { response ->
            if (!response.status.isSuccess()) error("Cancellation rejected: ${response.status.value}")
            response.body()
        }
    }

    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (error: SubmissionUnavailableException) {
        throw error
    } catch (error: Throwable) {
        throw SubmissionUnavailableException(
            "Code execution needs a connection to the CodingArena server.", error,
        )
    }
}
