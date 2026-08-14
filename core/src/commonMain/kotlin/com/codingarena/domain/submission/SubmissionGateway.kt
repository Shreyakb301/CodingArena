package com.codingarena.domain.submission

import com.codingarena.domain.model.CodeSubmission
import com.codingarena.domain.model.CreateSubmissionRequest
import com.codingarena.domain.model.SubmissionAccepted

interface SubmissionGateway {
    suspend fun create(request: CreateSubmissionRequest): SubmissionAccepted
    suspend fun get(id: String): CodeSubmission
    suspend fun cancel(id: String): CodeSubmission
}

class SubmissionUnavailableException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

