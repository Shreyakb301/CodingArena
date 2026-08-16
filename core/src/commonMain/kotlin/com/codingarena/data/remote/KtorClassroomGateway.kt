package com.codingarena.data.remote

import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.model.Assignment
import com.codingarena.domain.model.AuthResponse
import com.codingarena.domain.model.ClassMembership
import com.codingarena.domain.model.Classroom
import com.codingarena.domain.model.CreateAssignmentRequest
import com.codingarena.domain.model.CreateClassroomRequest
import com.codingarena.domain.model.JoinClassroomRequest
import com.codingarena.domain.model.LoginRequest
import com.codingarena.domain.model.RegisterRequest
import com.codingarena.domain.model.ProgressSyncPayload
import com.codingarena.domain.model.ClassroomDashboard
import com.codingarena.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class KtorClassroomGateway(
    private val client: HttpClient,
    private val config: ArenaServerConfig,
    private val settings: SettingsRepository,
) : ClassroomGateway {
    override suspend fun register(request: RegisterRequest): AuthResponse {
        val response = client.post("${config.baseUrl}/v1/auth/register") {
            contentType(ContentType.Application.Json); setBody(request)
        }.authBody()
        saveAuth(response)
        return response
    }

    override suspend fun login(request: LoginRequest): AuthResponse {
        val response = client.post("${config.baseUrl}/v1/auth/login") {
            contentType(ContentType.Application.Json); setBody(request)
        }.authBody()
        saveAuth(response)
        return response
    }

    override suspend fun classrooms(): List<Classroom> =
        client.get("${config.baseUrl}/v1/classes") { authenticate() }.checkedBody()

    override suspend fun createClassroom(name: String): Classroom =
        client.post("${config.baseUrl}/v1/classes") {
            authenticate(); contentType(ContentType.Application.Json); setBody(CreateClassroomRequest(name))
        }.checkedBody()

    override suspend fun join(inviteCode: String, displayName: String): Classroom =
        client.post("${config.baseUrl}/v1/classes/join") {
            authenticate(); contentType(ContentType.Application.Json)
            setBody(JoinClassroomRequest(inviteCode, displayName))
        }.checkedBody()

    override suspend fun createAssignment(
        classroomId: String,
        request: CreateAssignmentRequest,
    ): Assignment = client.post("${config.baseUrl}/v1/classes/$classroomId/assignments") {
        authenticate(); contentType(ContentType.Application.Json); setBody(request)
    }.checkedBody()

    override suspend fun assignments(classroomId: String): List<Assignment> =
        client.get("${config.baseUrl}/v1/classes/$classroomId/assignments") { authenticate() }.checkedBody()

    override suspend fun members(classroomId: String): List<ClassMembership> =
        client.get("${config.baseUrl}/v1/classes/$classroomId/members") { authenticate() }.checkedBody()

    override suspend fun pushProgress(payload: ProgressSyncPayload) {
        val response = client.post("${config.baseUrl}/v1/progress") {
            authenticate(); contentType(ContentType.Application.Json); setBody(payload)
        }
        if (!response.status.isSuccess()) error("Progress sync failed: ${response.status.value}")
    }

    override suspend fun fetchProgress(): ProgressSyncPayload? {
        val response = client.get("${config.baseUrl}/v1/progress") { authenticate() }
        if (response.status == io.ktor.http.HttpStatusCode.NotFound) return null
        if (!response.status.isSuccess()) error("Fetching progress failed: ${response.status.value}")
        return response.body()
    }

    override suspend fun dashboard(classroomId: String): ClassroomDashboard =
        client.get("${config.baseUrl}/v1/classes/$classroomId/dashboard") { authenticate() }.checkedBody()

    override suspend fun signOut() {
        settings.put(AUTH_TOKEN, "")
        settings.put(AUTH_ROLE, "")
        settings.put(AUTH_NAME, "")
    }

    private suspend fun io.ktor.client.request.HttpRequestBuilder.authenticate() {
        val token = settings.get(AUTH_TOKEN)?.takeIf { it.isNotBlank() } ?: error("Sign in first")
        bearerAuth(token)
    }

    private suspend fun saveAuth(response: AuthResponse) {
        settings.put(AUTH_TOKEN, response.token)
        settings.put(AUTH_ROLE, response.role.name)
        settings.put(AUTH_NAME, response.displayName)
    }

    private suspend inline fun <reified T> io.ktor.client.statement.HttpResponse.checkedBody(): T {
        if (!status.isSuccess()) error("Server returned ${status.value}")
        return body()
    }

    private suspend fun io.ktor.client.statement.HttpResponse.authBody(): AuthResponse = checkedBody()

    companion object {
        const val AUTH_TOKEN = "auth.token"
        const val AUTH_ROLE = "auth.role"
        const val AUTH_NAME = "auth.name"
    }
}
