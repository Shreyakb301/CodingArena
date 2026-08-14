package com.codingarena.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.codingarena.db.ArenaDatabase
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

/**
 * JVM driver, used by desktop builds and by the shared test suite.
 *
 * Passing a null [path] gives an in-memory database, which is what the
 * repository tests run against.
 */
actual class DatabaseDriverFactory(private val path: String? = defaultPath()) {

    actual fun create(): SqlDriver {
        val url = if (path == null) JdbcSqliteDriver.IN_MEMORY else "jdbc:sqlite:$path"
        val driver = JdbcSqliteDriver(url, Properties())
        // Unlike the Android and Native drivers, JdbcSqliteDriver does not run
        // SQLDelight migrations automatically. Older desktop installs also did
        // not persist PRAGMA user_version, so infer their version from the
        // tables they contain before applying the migration.
        val currentVersion = if (path != null && File(path).length() > 0) {
            DriverManager.getConnection(url, Properties()).use(::existingSchemaVersion)
        } else null
        if (currentVersion != null) {
            if (currentVersion < ArenaDatabase.Schema.version) {
                ArenaDatabase.Schema.migrate(driver, currentVersion, ArenaDatabase.Schema.version)
            }
        } else {
            ArenaDatabase.Schema.create(driver)
        }
        driver.execute(
            identifier = null,
            sql = "PRAGMA user_version = ${ArenaDatabase.Schema.version}",
            parameters = 0,
        )
        return driver
    }

    private fun existingSchemaVersion(connection: Connection): Long? {
        fun hasTable(name: String): Boolean = connection.prepareStatement(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1"
        ).use { statement ->
            statement.setString(1, name)
            statement.executeQuery().use { it.next() }
        }
        if (!hasTable("profile")) return null
        val recordedVersion = connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA user_version").use { result ->
                if (result.next()) result.getLong(1) else 0L
            }
        }
        return when {
            recordedVersion > 0L -> recordedVersion
            // chapterProgress was introduced by 1.sqm, which took the schema from
            // version 1 to 2 - this must stay pinned to that historical version,
            // not ArenaDatabase.Schema.version, or a pre-migration-tracking
            // install would be misdiagnosed as already being on the *current*
            // (now higher) schema version and silently skip newer migrations.
            hasTable("chapterProgress") -> 2L
            else -> 1L
        }
    }

    private companion object {
        fun defaultPath(): String {
            val dir = File(System.getProperty("user.home"), ".codingarena").apply { mkdirs() }
            return File(dir, DATABASE_NAME).absolutePath
        }
    }
}
