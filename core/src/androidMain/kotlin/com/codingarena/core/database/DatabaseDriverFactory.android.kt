package com.codingarena.core.database

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.codingarena.db.ArenaDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    // See the iOS factory: the async-mode schema is adapted for the synchronous
    // Android driver with synchronous().
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(ArenaDatabase.Schema.synchronous(), context, DATABASE_NAME)
}
