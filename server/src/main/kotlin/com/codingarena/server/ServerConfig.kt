package com.codingarena.server

data class ServerConfig(
    val port: Int = env("PORT", "8080").toInt(),
    val databaseUrl: String = toJdbcUrl(env("ARENA_DATABASE_URL", "jdbc:postgresql://localhost:5432/codingarena")),
    val databaseUser: String = env("ARENA_DATABASE_USER", "codingarena"),
    val databasePassword: String = env("ARENA_DATABASE_PASSWORD", "codingarena"),
    val jwtSecret: String = env("ARENA_JWT_SECRET", "local-development-secret-change-me"),
    val judge0Url: String = env("JUDGE0_URL", "http://127.0.0.1:2358"),
    val judge0Token: String? = System.getenv("JUDGE0_TOKEN"),
)

private fun env(name: String, fallback: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: fallback

// Hosts like Render provide postgres://user:pass@host:port/db; the JDBC driver needs jdbc:postgresql://host:port/db.
private fun toJdbcUrl(raw: String): String {
    if (raw.startsWith("jdbc:")) return raw
    val afterScheme = raw.substringAfter("://", raw)
    val hostAndPath = afterScheme.substringAfter("@", afterScheme)
    return "jdbc:postgresql://$hostAndPath"
}

