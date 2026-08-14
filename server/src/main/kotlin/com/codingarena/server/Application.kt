package com.codingarena.server

import com.auth0.jwt.interfaces.Payload
import com.codingarena.domain.model.Assignment
import com.codingarena.domain.model.AuthResponse
import com.codingarena.domain.model.CreateAssignmentRequest
import com.codingarena.domain.model.CreateClassroomRequest
import com.codingarena.domain.model.JoinClassroomRequest
import com.codingarena.domain.model.LoginRequest
import com.codingarena.domain.model.RegisterRequest
import com.codingarena.domain.model.ProgressSyncPayload
import com.codingarena.domain.model.ClassroomDashboard
import com.codingarena.domain.model.StudentProgressView
import com.codingarena.domain.model.UserRole
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

data class ServerDependencies(
    val config: ServerConfig,
    val store: ArenaStore,
    val runner: CodeRunner,
    val scope: CoroutineScope,
)

fun main() {
    val config = ServerConfig()
    embeddedServer(Netty, port = config.port) { module() }.start(wait = true)
}

fun Application.module(overrides: ServerDependencies? = null) {
    val config = overrides?.config ?: ServerConfig()
    val jsonConfig = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val client = HttpClient(OkHttp) {
        install(ClientContentNegotiation) { json(jsonConfig) }
    }
    val dependencies = overrides ?: ServerDependencies(
        config = config,
        store = PostgresArenaStore(config),
        runner = Judge0CodeRunner(client, config),
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )
    val tokens = JwtTokens(config.jwtSecret)
    val service = SubmissionService(dependencies.store, dependencies.runner, dependencies.scope)

    install(ContentNegotiation) { json(jsonConfig) }
    install(RateLimit) {
        global { rateLimiter(limit = 60, refillPeriod = 1.minutes) }
    }
    install(Authentication) {
        jwt("arena") {
            realm = "CodingArena"
            verifier(tokens.verifier)
            validate { credential -> credential.payload.toPrincipal() }
        }
    }
    install(StatusPages) {
        exception<IllegalArgumentException> { call, error ->
            call.respond(HttpStatusCode.BadRequest, ApiError(error.message ?: "Invalid request"))
        }
        exception<IllegalStateException> { call, error ->
            call.respond(HttpStatusCode.Forbidden, ApiError(error.message ?: "Forbidden"))
        }
        exception<NoSuchElementException> { call, error ->
            call.respond(HttpStatusCode.NotFound, ApiError(error.message ?: "Not found"))
        }
        exception<Throwable> { call, error ->
            call.respond(HttpStatusCode.InternalServerError, ApiError(error.message ?: "Unexpected server error"))
        }
    }

    routing {
        get("/health") { call.respond(HealthResponse("ok")) }

        route("/v1/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                val account = dependencies.store.createUser(request)
                call.respond(HttpStatusCode.Created, account.authResponse(tokens))
            }
            post("/login") {
                val request = call.receive<LoginRequest>()
                val account = dependencies.store.userByEmail(request.email)
                    ?.takeIf { Passwords.verify(request.password, it.passwordHash) }
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiError("Invalid credentials"))
                call.respond(account.authResponse(tokens))
            }
        }

        authenticate("arena") {
            post("/v1/progress") {
                val principal = call.requireRole(UserRole.STUDENT)
                dependencies.store.saveProgress(principal.userId, call.receive<ProgressSyncPayload>())
                call.respond(HttpStatusCode.NoContent)
            }
            route("/v1/submissions") {
                post {
                    val principal = call.principal<ArenaPrincipal>()!!
                    call.respond(HttpStatusCode.Accepted, service.create(principal.userId, call.receive()))
                }
                get("/{id}") {
                    val principal = call.principal<ArenaPrincipal>()!!
                    call.respond(service.get(principal.userId, call.parameters["id"].orEmpty()))
                }
                post("/{id}/cancel") {
                    val principal = call.principal<ArenaPrincipal>()!!
                    call.respond(service.cancel(principal.userId, call.parameters["id"].orEmpty()))
                }
            }

            route("/v1/classes") {
                get {
                    val principal = call.principal<ArenaPrincipal>()!!
                    call.respond(dependencies.store.classrooms(principal.userId))
                }
                post {
                    val principal = call.requireRole(UserRole.TEACHER)
                    val request = call.receive<CreateClassroomRequest>()
                    call.respond(HttpStatusCode.Created,
                        dependencies.store.createClassroom(principal.userId, request.name, System.currentTimeMillis()))
                }
                post("/join") {
                    val principal = call.requireRole(UserRole.STUDENT)
                    val request = call.receive<JoinClassroomRequest>()
                    val classroom = dependencies.store.joinClassroom(
                        principal.userId, request.displayName, request.inviteCode, System.currentTimeMillis(),
                    ) ?: return@post call.respond(HttpStatusCode.NotFound, ApiError("Invite code not found"))
                    call.respond(classroom)
                }
                get("/{id}/assignments") {
                    val principal = call.principal<ArenaPrincipal>()!!
                    call.respond(dependencies.store.assignments(call.parameters["id"].orEmpty(), principal.userId))
                }
                post("/{id}/assignments") {
                    val principal = call.requireRole(UserRole.TEACHER)
                    val classroomId = call.parameters["id"].orEmpty()
                    val request = call.receive<CreateAssignmentRequest>()
                    val assignment = Assignment(
                        id = UUID.randomUUID().toString(),
                        classroomId = classroomId,
                        title = request.title,
                        chapterIds = request.chapterIds,
                        exerciseIds = request.exerciseIds,
                        dueAt = request.dueAt,
                        createdAt = System.currentTimeMillis(),
                    )
                    call.respond(HttpStatusCode.Created,
                        dependencies.store.createAssignment(principal.userId, assignment))
                }
                get("/{id}/members") {
                    val principal = call.requireRole(UserRole.TEACHER)
                    call.respond(dependencies.store.members(call.parameters["id"].orEmpty(), principal.userId))
                }
                get("/{id}/dashboard") {
                    val principal = call.requireRole(UserRole.TEACHER)
                    val classroomId = call.parameters["id"].orEmpty()
                    val classroom = dependencies.store.classrooms(principal.userId)
                        .firstOrNull { it.id == classroomId }
                        ?: throw NoSuchElementException("Classroom not found")
                    val memberships = dependencies.store.members(classroomId, principal.userId)
                        .filter { it.role == UserRole.STUDENT }
                    val progress = dependencies.store.progress(memberships.map { it.userId })
                    call.respond(
                        ClassroomDashboard(
                            classroom = classroom,
                            students = memberships.map { member ->
                                val payload = progress[member.userId]
                                StudentProgressView(
                                    member.userId,
                                    member.displayName,
                                    payload?.chapters.orEmpty(),
                                    payload?.confusions.orEmpty(),
                                )
                            },
                            assignments = dependencies.store.assignments(classroomId, principal.userId),
                        )
                    )
                }
            }
        }
    }
}

private fun Payload.toPrincipal(): ArenaPrincipal? {
    val userId = subject?.takeIf { it.isNotBlank() } ?: return null
    val role = runCatching { UserRole.valueOf(getClaim("role").asString()) }.getOrNull() ?: return null
    return ArenaPrincipal(userId, role)
}

private fun UserAccount.authResponse(tokens: JwtTokens) =
    AuthResponse(tokens.issue(this), id, role, displayName)

private fun io.ktor.server.application.ApplicationCall.requireRole(role: UserRole): ArenaPrincipal {
    val principal = principal<ArenaPrincipal>() ?: error("Authentication required")
    check(principal.role == role) { "$role access required" }
    return principal
}

@Serializable data class ApiError(val message: String)
@Serializable data class HealthResponse(val status: String)
