package com.codingarena.core.database

import app.cash.sqldelight.async.coroutines.awaitCreate
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
 * Worker (sql.js - SQLite compiled to Wasm) and every operation is a
 * `postMessage` round trip. This is why the whole database is generated in
 * async mode (see `generateAsync` in build.gradle.kts).
 *
 * The worker script is [WORKER_URL] - a plain classic worker served next to the
 * bundle (composeApp/src/wasmJsMain/resources/sqljs.worker.js), not the bundled
 * `@cashapp/sqldelight-sqljs-worker`, which needs a bundling step Kotlin/Wasm's
 * webpack output does not give it.
 *
 * The sql.js database starts empty on every load, so the schema must be created
 * before the first query. [create] kicks that off; the web entry point awaits
 * [schemaReady] before mounting the app. (In memory for now - progress is lost
 * on a hard reload until an OPFS-backed worker is wired in; accounts + server
 * sync are the durable path.)
 */
@OptIn(DelicateCoroutinesApi::class)
actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val driver = WebWorkerDriver(Worker(WORKER_URL))
        GlobalScope.launch {
            try {
                ArenaDatabase.Schema.awaitCreate(driver)
                schemaReady.complete(Unit)
            } catch (t: Throwable) {
                schemaReady.completeExceptionally(t)
            }
        }
        return driver
    }

    companion object {
        private const val WORKER_URL = "sqljs.worker.js"

        /** Completes once [create]'s schema creation has finished. */
        val schemaReady: CompletableDeferred<Unit> = CompletableDeferred()

        /** Suspends until the database schema exists. Safe to call repeatedly. */
        suspend fun awaitSchemaReady() = schemaReady.await()
    }
}
