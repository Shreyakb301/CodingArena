package com.codingarena

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.codingarena.app.App
import com.codingarena.core.database.DatabaseDriverFactory
import com.codingarena.di.appModule
import com.codingarena.di.coreModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Desktop preview.
 *
 * This is not a shipping target - the product is a phone app. It exists so the
 * real UI can be looked at in seconds without Xcode, code signing or a device,
 * which makes layout and navigation problems cheap to find.
 *
 *     ./gradlew :composeApp:run
 */
fun main() {
    startKoin {
        modules(
            // Desktop keeps its database in ~/.codingarena, so experimenting
            // here never touches anything on a phone.
            module { single { DatabaseDriverFactory() } },
            coreModule,
            appModule,
        )
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "CodingArena (desktop preview)",
            state = rememberWindowState(width = 1000.dp, height = 900.dp),
        ) {
            PhoneFrame { App() }
        }
    }
}

/**
 * Constrains the app to phone width.
 *
 * Without this the UI stretches across a desktop window and every layout
 * judgement you make from it is wrong. 390dp is an iPhone 15 logical width.
 */
@Composable
private fun PhoneFrame(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        // App() paints its own Surface, so no background is needed here.
        Box(modifier = Modifier.width(PHONE_WIDTH).fillMaxHeight()) {
            content()
        }
    }
}

private val PHONE_WIDTH = 390.dp
