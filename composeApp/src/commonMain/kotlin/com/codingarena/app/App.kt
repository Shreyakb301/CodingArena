package com.codingarena.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codingarena.core.design.ArenaTheme
import org.koin.compose.KoinContext

/**
 * Root composable, shared by the iOS and Android entry points.
 *
 * The bottom bar is hidden for the full-screen flows - splash, onboarding, a
 * live challenge and a Code Rush run - because those are focus states where a
 * tab tap would silently discard progress.
 */
@Composable
fun App() {
    KoinContext {
        ArenaTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AppContent()
            }
        }
    }
}

@Composable
private fun AppContent() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute != null && currentRoute !in fullScreenRoutes &&
        !currentRoute.startsWith("challenge/") &&
        !currentRoute.startsWith("review/") &&
        !currentRoute.startsWith("codeRush/session/") &&
        !currentRoute.startsWith("blitz/")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    // Keep a single copy of each tab on the
                                    // stack and preserve its scroll state.
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            AppNavigation(navController)
        }
    }
}

private val fullScreenRoutes = setOf(Routes.SPLASH, Routes.ONBOARDING)

// Restricted to the icons bundled with material-icons-core, so the app does
// not pull in the whole extended icon set for five glyphs.
private val TopLevelDestination.icon
    get() = when (this) {
        TopLevelDestination.HOME -> Icons.Filled.Home
        TopLevelDestination.ROADMAP -> Icons.Filled.PlayArrow
        TopLevelDestination.PATTERNS -> Icons.Filled.Menu
        TopLevelDestination.RATINGS -> Icons.Filled.Star
        TopLevelDestination.PROFILE -> Icons.Filled.Person
    }
