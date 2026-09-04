package com.codingarena.core.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.async.coroutines.awaitMigrate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.codingarena.db.ArenaDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Worker

/**
 * Browser driver.
 *
 * SQLite in a browser has no synchronous binding, so the database runs in a Web
 * Worker (sql.js) and every operation is a `postMessage` round trip - which is
 * why the whole database is generated in async mode (`generateAsync` in
 * build.gradle.kts). The worker ([WORKER_URL], a classic worker served next to
 * the bundle) snapshots the database to IndexedDB after every write, so it now
 * persists across reloads.
 *
 * Because the database survives, the schema is only created once. [create]
 * checks `PRAGMA user_version`: 0 means a fresh database (create + stamp the
 * version), a lower version means an upgrade (migrate + restamp), and an equal
 * version means there is nothing to do. The web entry point awaits
 * [schemaReady] before mounting the app.
 */
@OptIn(DelicateCoroutinesApi::class)
actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val driver = WebWorkerDriver(Worker(WORKER_URL))
        GlobalScope.launch {
            try {
                val schema = ArenaDatabase.Schema
                when (val current = driver.userVersion()) {
                    0L -> {
                        schema.awaitCreate(driver)
                        driver.setUserVersion(schema.version)
                    }
                    in 1 until schema.version -> {
                        schema.awaitMigrate(driver, current, schema.version)
                        driver.setUserVersion(schema.version)
                    }
                }
                schemaReady.complete(Unit)
            } catch (t: Throwable) {
                schemaReady.completeExceptionally(t)
            }
        }
        return driver
    }

    companion object {
        private const val WORKER_URL = "sqljs.worker.js"

        /** Completes once [create]'s schema setup has finished. */
        val schemaReady: CompletableDeferred<Unit> = CompletableDeferred()

        /** Suspends until the database schema exists. Safe to call repeatedly. */
        suspend fun awaitSchemaReady() = schemaReady.await()
    }
}

private suspend fun SqlDriver.userVersion(): Long =
    executeQuery(
        identifier = null,
        sql = "PRAGMA user_version",
        mapper = { cursor -> QueryResult.Value(if (cursor.next().value) cursor.getLong(0) ?: 0L else 0L) },
        parameters = 0,
    ).await()

private suspend fun SqlDriver.setUserVersion(version: Long) {
    execute(identifier = null, sql = "PRAGMA user_version = $version", parameters = 0).await()
}
