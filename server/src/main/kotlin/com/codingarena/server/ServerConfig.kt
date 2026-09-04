package com.codingarena.server

data class ServerConfig(
    val port: Int = env("PORT", "8080").toInt(),
    val databaseUrl: String = toJdbcUrl(env("ARENA_DATABASE_URL", "jdbc:postgresql://localhost:5432/codingarena")),
    val databaseUser: String = env("ARENA_DATABASE_USER", "codingarena"),
    val databasePassword: String = env("ARENA_DATABASE_PASSWORD", "codingarena"),
    val jwtSecret: String = env("ARENA_JWT_SECRET", "local-development-secret-change-me"),
    val judge0Url: String = env("JUDGE0_URL", "http://127.0.0.1:2358"),
    val judge0Token: String? = System.getenv("JUDGE0_TOKEN"),
    /** Front-end origins allowed to call the API from a browser. */
    val allowedOrigins: List<String> = env("ARENA_ALLOWED_ORIGINS", "http://localhost:8080")
        .split(',').map(String::trim).filter(String::isNotEmpty),
    /** Where the browser app lives; OAuth redirects the finished session back here. */
    val appUrl: String = env("ARENA_APP_URL", "http://localhost:8080").trimEnd('/'),
    val google: GoogleOAuthConfig? = GoogleOAuthConfig.fromEnv(),
)

/** Present only when all three Google OAuth env vars are set. */
data class GoogleOAuthConfig(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
) {
    companion object {
        fun fromEnv(): GoogleOAuthConfig? {
            val id = System.getenv("GOOGLE_CLIENT_ID")?.takeIf { it.isNotBlank() } ?: return null
            val secret = System.getenv("GOOGLE_CLIENT_SECRET")?.takeIf { it.isNotBlank() } ?: return null
            val redirect = System.getenv("GOOGLE_REDIRECT_URI")?.takeIf { it.isNotBlank() } ?: return null
            return GoogleOAuthConfig(id, secret, redirect)
        }
    }
}

private fun env(name: String, fallback: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: fallback

// Hosts like Render provide postgres://user:pass@host:port/db; the JDBC driver needs jdbc:postgresql://host:port/db.
private fun toJdbcUrl(raw: String): String {
    if (raw.startsWith("jdbc:")) return raw
    val afterScheme = raw.substringAfter("://", raw)
    val hostAndPath = afterScheme.substringAfter("@", afterScheme)
    return "jdbc:postgresql://$hostAndPath"
}
