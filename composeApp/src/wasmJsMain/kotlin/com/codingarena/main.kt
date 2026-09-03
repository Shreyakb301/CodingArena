package com.codingarena

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import com.codingarena.app.App
import com.codingarena.core.database.DatabaseDriverFactory
import com.codingarena.core.design.ArenaTheme
import com.codingarena.db.ArenaDatabase
import com.codingarena.di.appModule
import com.codingarena.di.coreModule
import kotlinx.browser.document
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * Browser entry point.
 *
 * Mirrors [setupKoin] + [MainViewController] on iOS, with one extra step: the
 * browser SQLite database starts empty and its schema is created asynchronously
 * in a Web Worker, so the app waits for [DatabaseDriverFactory.awaitSchemaReady]
 * before mounting - otherwise the first screen queries a table that does not
 * exist yet.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // The static "Loading…" placeholder in index.html has done its job once the
    // Wasm bundle is running; ComposeViewport draws over the body but does not
    // clear it.
    document.getElementById("loading")?.remove()

    startKoin {
        modules(
            module { single { DatabaseDriverFactory() } },
            coreModule,
            appModule,
        )
    }
    ComposeViewport(document.body!!) {
        var ready by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            // Touching the singleton builds the driver and starts schema creation.
            KoinPlatform.getKoin().get<ArenaDatabase>()
            DatabaseDriverFactory.awaitSchemaReady()
            ready = true
        }
        if (ready) App() else BootSplash()
    }
}

@Composable
private fun BootSplash() {
    ArenaTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
