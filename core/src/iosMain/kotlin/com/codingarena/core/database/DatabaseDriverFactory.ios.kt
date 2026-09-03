package com.codingarena.core.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.codingarena.db.ArenaDatabase

actual class DatabaseDriverFactory {
    // The database is generated in async mode for the web target; the native
    // driver runs synchronously, so the schema is adapted back with synchronous().
    actual fun create(): SqlDriver =
        NativeSqliteDriver(ArenaDatabase.Schema.synchronous(), DATABASE_NAME)
}
