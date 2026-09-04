package com.codingarena.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.codingarena.domain.model.UserRole
import com.codingarena.domain.model.RegisterRequest
import io.ktor.server.auth.Principal
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class ArenaPrincipal(val userId: String, val role: UserRole) : Principal

data class UserAccount(
    val id: String,
    val displayName: String,
    val email: String?,
    val role: UserRole,
    /** Null for accounts created through an identity provider (e.g. Google). */
    val passwordHash: String?,
)

class JwtTokens(secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).withIssuer(ISSUER).build()

    fun issue(account: UserAccount): String = JWT.create()
        .withIssuer(ISSUER)
        .withSubject(account.id)
        .withClaim("role", account.role.name)
        .withExpiresAt(Instant.now().plusSeconds(TOKEN_SECONDS))
        .sign(algorithm)

    companion object {
        const val ISSUER = "codingarena"
        private const val TOKEN_SECONDS = 60L * 60 * 24 * 7
    }
}

object Passwords {
    private val random = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(16).also(random::nextBytes)
        val derived = derive(password, salt)
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derived)
    }

    fun verify(password: String, stored: String?): Boolean {
        if (stored == null) return false
        val parts = stored.split(':')
        if (parts.size != 2) return false
        val salt = runCatching { Base64.getDecoder().decode(parts[0]) }.getOrNull() ?: return false
        val expected = runCatching { Base64.getDecoder().decode(parts[1]) }.getOrNull() ?: return false
        return java.security.MessageDigest.isEqual(expected, derive(password, salt))
    }

    private fun derive(password: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(password.toCharArray(), salt, 120_000, 256)).encoded
}
