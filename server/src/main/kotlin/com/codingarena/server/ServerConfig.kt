package com.codingarena.server

data class ServerConfig(
    val port: Int = env("PORT", "8080").toInt(),
    val databaseUrl: String = env("ARENA_DATABASE_URL", "jdbc:postgresql://localhost:5432/codingarena"),
    val databaseUser: String = env("ARENA_DATABASE_USER", "codingarena"),
    val databasePassword: String = env("ARENA_DATABASE_PASSWORD", "codingarena"),
    val jwtSecret: String = env("ARENA_JWT_SECRET", "local-development-secret-change-me"),
    val judge0Url: String = env("JUDGE0_URL", "http://127.0.0.1:2358"),
    val judge0Token: String? = System.getenv("JUDGE0_TOKEN"),
)

private fun env(name: String, fallback: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: fallback

