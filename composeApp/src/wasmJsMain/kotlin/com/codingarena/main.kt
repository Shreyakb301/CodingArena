package com.codingarena

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.dp
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
 * Mirrors [setupKoin] + [MainViewController] on iOS, with two extras:
 *  - the browser SQLite database starts empty and its schema is created
 *    asynchronously in a Web Worker, so the app waits for
 *    [DatabaseDriverFactory.awaitSchemaReady] before mounting;
 *  - CodingArena is a phone app, so on a wide desktop window the UI is held to
 *    a phone-width column centred on the arena background rather than stretched.
 */
private val PHONE_MAX_WIDTH = 460.dp

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
        ArenaTheme(darkTheme = true) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.widthIn(max = PHONE_MAX_WIDTH).fillMaxHeight()) {
                        var ready by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            // Touching the singleton builds the driver and starts
                            // schema creation.
                            KoinPlatform.getKoin().get<ArenaDatabase>()
                            DatabaseDriverFactory.awaitSchemaReady()
                            ready = true
                        }
                        if (ready) App() else BootSplash()
                    }
                }
            }
        }
    }
}

@Composable
private fun BootSplash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
