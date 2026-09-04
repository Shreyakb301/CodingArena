package com.codingarena.server

import com.auth0.jwt.JWT
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.encodeURLParameter
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

private const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
private const val STATE_COOKIE = "arena_oauth_state"

@Serializable
private data class GoogleTokenResponse(
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("error") val error: String? = null,
)

/**
 * Server-side "Continue with Google" (OAuth 2.0 authorization-code flow).
 *
 *   GET /v1/auth/google/start     -> redirect to Google's consent screen
 *   GET /v1/auth/google/callback  -> exchange the code, create/find the user,
 *                                    redirect back to the app with a session
 *
 * Same flow works for the web app and, later, iOS. Only enabled when the three
 * GOOGLE_* env vars are set; otherwise the routes report 501.
 *
 * The id_token is decoded but not signature-verified: it is fetched directly
 * from Google over TLS in the code exchange, so it is already authentic. (A
 * client-side flow, where the token arrives from the browser, would need JWKS
 * verification.)
 */
fun Route.googleAuthRoutes(
    config: ServerConfig,
    client: HttpClient,
    store: ArenaStore,
    tokens: JwtTokens,
) = route("/google") {
    val google = config.google

    get("/start") {
        if (google == null) {
            return@get call.respond(HttpStatusCode.NotImplemented, ApiError("Google sign-in is not configured"))
        }
        val state = UUID.randomUUID().toString()
        call.response.cookies.append(
            name = STATE_COOKIE,
            value = state,
            maxAge = 600,
            path = "/v1/auth/google",
            secure = config.appUrl.startsWith("https"),
            httpOnly = true,
            extensions = mapOf("SameSite" to "Lax"),
        )
        val url = AUTH_ENDPOINT + "?" + listOf(
            "client_id" to google.clientId,
            "redirect_uri" to google.redirectUri,
            "response_type" to "code",
            "scope" to "openid email profile",
            "state" to state,
            "access_type" to "online",
            "prompt" to "select_account",
        ).joinToString("&") { (k, v) -> "$k=${v.encodeURLParameter()}" }
        call.respondRedirect(url)
    }

    get("/callback") {
        if (google == null) {
            return@get call.respond(HttpStatusCode.NotImplemented, ApiError("Google sign-in is not configured"))
        }

        fun fail(reason: String): Nothing {
            throw CallbackRedirect("${config.appUrl}/#auth_error=${reason.encodeURLParameter()}")
        }

        val error = call.request.queryParameters["error"]
        if (error != null) fail(error)

        val code = call.request.queryParameters["code"] ?: fail("missing_code")
        val state = call.request.queryParameters["state"]
        val expectedState = call.request.cookies[STATE_COOKIE]
        if (expectedState == null || state != expectedState) fail("bad_state")

        val response: GoogleTokenResponse = try {
            client.submitForm(
                url = TOKEN_ENDPOINT,
                formParameters = Parameters.build {
                    append("code", code)
                    append("client_id", google.clientId)
                    append("client_secret", google.clientSecret)
                    append("redirect_uri", google.redirectUri)
                    append("grant_type", "authorization_code")
                },
            ).body()
        } catch (t: Throwable) {
            fail("token_exchange_failed")
        }

        val idToken = response.idToken ?: fail(response.error ?: "no_id_token")
        val claims = runCatching { JWT.decode(idToken) }.getOrNull() ?: fail("bad_id_token")
        val sub = claims.subject ?: fail("no_subject")
        val email = claims.getClaim("email").asString() ?: fail("no_email")
        val emailVerified = claims.getClaim("email_verified").asBoolean() ?: false
        if (!emailVerified) fail("email_unverified")
        val name = claims.getClaim("name").asString().orEmpty()

        val account = store.linkOrCreateGoogleUser(sub, email, name)
        val session = tokens.issue(account)

        call.response.cookies.append(STATE_COOKIE, "", maxAge = 0, path = "/v1/auth/google")
        call.respondRedirect("${config.appUrl}/#token=${session.encodeURLParameter()}")
    }
}

/** Thrown from the callback to redirect the browser back to the app with an error. */
class CallbackRedirect(val location: String) : RuntimeException()
