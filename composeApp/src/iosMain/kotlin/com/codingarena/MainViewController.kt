package com.codingarena

import androidx.compose.ui.window.ComposeUIViewController
import com.codingarena.app.App
import com.codingarena.core.database.DatabaseDriverFactory
import com.codingarena.di.appModule
import com.codingarena.di.coreModule
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.UIKit.UIViewController

/**
 * Entry points called from Swift.
 *
 * Deliberately *not* named `initKoin`: Kotlin/Native treats exported functions
 * beginning with "init" as Objective-C initialisers and renames them (it would
 * arrive in Swift as `doInitKoin`). Naming it `setupKoin` keeps the Swift call
 * site identical to the Kotlin declaration.
 *
 * Separate from [MainViewController] so the graph is built once in the app
 * initialiser rather than every time SwiftUI recreates the view.
 */
fun setupKoin() {
    startKoin {
        modules(
            // The only platform-specific binding in the whole graph.
            module { single { DatabaseDriverFactory() } },
            coreModule,
            appModule,
        )
    }
}

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
