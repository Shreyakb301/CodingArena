package com.codingarena.core.database

import app.cash.sqldelight.db.SqlDriver
import com.codingarena.db.ArenaDatabase

/**
 * Platform-specific SQLite driver creation.
 *
 * Declared as an `expect class` rather than an interface because each platform
 * needs different construction arguments (an Android `Context`, a filename on
 * iOS and the JVM), and those differences should not leak into shared code.
 */
expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}

/** Builds the generated database from a platform driver. */
fun createArenaDatabase(factory: DatabaseDriverFactory): ArenaDatabase =
    ArenaDatabase(factory.create())

internal const val DATABASE_NAME = "coding_arena.db"
