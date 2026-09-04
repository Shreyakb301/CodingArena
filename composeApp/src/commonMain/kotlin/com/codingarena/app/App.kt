package com.codingarena.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        ArenaTheme(darkTheme = true) {
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = destination.owns(currentRoute)
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
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
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

private val fullScreenRoutes = setOf(Routes.SPLASH, Routes.ONBOARDING, Routes.LOGIN)

// Restricted to material-icons-core so the shared binary stays small.
private val TopLevelDestination.icon
    get() = when (this) {
        TopLevelDestination.HOME -> Icons.Filled.Home
        TopLevelDestination.PATH -> Icons.AutoMirrored.Filled.List
        TopLevelDestination.PRACTICE -> Icons.Filled.PlayArrow
        TopLevelDestination.INTERVIEW -> Icons.Filled.Person
        TopLevelDestination.MORE -> Icons.Filled.Menu
    }

private fun TopLevelDestination.owns(route: String?): Boolean = when (this) {
    TopLevelDestination.HOME -> route == Routes.HOME
    TopLevelDestination.PATH -> route == Routes.ROADMAP || route == Routes.LEARNING_PATH
    TopLevelDestination.PRACTICE -> route == Routes.PRACTICE || route?.startsWith("codeRush/") == true ||
        route == Routes.BLITZ_HOME || route == Routes.STRATEGY_BLITZ || route?.startsWith("blitz/") == true
    TopLevelDestination.INTERVIEW -> route?.startsWith("interview") == true
    TopLevelDestination.MORE -> route in setOf(
        Routes.PROFILE,
        Routes.PATTERNS,
        Routes.RATINGS,
        Routes.ACHIEVEMENTS,
        Routes.SETTINGS,
    )
}
