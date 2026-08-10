package com.codingarena.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.codingarena.db.ArenaDatabase
import java.io.File
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
        // JdbcSqliteDriver does not track schema version for us, so create the
        // tables when this is a fresh database.
        val alreadyCreated = path != null && File(path).length() > 0
        if (!alreadyCreated) {
            ArenaDatabase.Schema.create(driver)
        }
        return driver
    }

    private companion object {
        fun defaultPath(): String {
            val dir = File(System.getProperty("user.home"), ".codingarena").apply { mkdirs() }
            return File(dir, DATABASE_NAME).absolutePath
        }
    }
}
