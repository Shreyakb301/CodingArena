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
import com.codingarena.data.remote.ArenaServerConfig
import com.codingarena.data.remote.KtorClassroomGateway
import com.codingarena.db.ArenaDatabase
import com.codingarena.di.appModule
import com.codingarena.di.coreModule
import com.codingarena.domain.repository.SettingsRepository
import kotlinx.browser.document
import kotlinx.browser.window
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
            // The API is served from the same origin as the app (Cloudflare
            // Pages functions), so point the client there. Overrides the
            // default in coreModule.
            module { single { ArenaServerConfig(baseUrl = window.location.origin) } },
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
                            captureAuthRedirect()
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

/**
 * The Google sign-in flow redirects back to `…/#token=<jwt>` (or
 * `…/#auth_error=<reason>`). Store the session, then strip the fragment so a
 * reload or a shared link does not carry the token around.
 */
private suspend fun captureAuthRedirect() {
    val hash = window.location.hash.removePrefix("#")
    if (hash.isEmpty()) return
    val params = hash.split('&').mapNotNull {
        val (k, v) = it.split('=', limit = 2).let { p -> p[0] to p.getOrElse(1) { "" } }
        if (k.isEmpty()) null else k to v
    }.toMap()

    val token = params["token"]?.let { decodeURIComponent(it) }
    if (!token.isNullOrBlank()) {
        val settings = KoinPlatform.getKoin().get<SettingsRepository>()
        settings.put(KtorClassroomGateway.AUTH_TOKEN, token)
    }
    // Always clear the fragment (covers the error case too).
    window.history.replaceState(null, "", window.location.pathname + window.location.search)
}

private fun decodeURIComponent(value: String): String =
    js("decodeURIComponent(value)")
