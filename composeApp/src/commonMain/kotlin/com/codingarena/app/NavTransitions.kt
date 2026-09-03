package com.codingarena.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/**
 * Screen transitions for [AppNavigation]'s `NavHost`.
 *
 * Two motions:
 *  - **Push / pop** (drilling into a problem, a lesson, settings): the incoming
 *    screen slides in from the edge while the outgoing one parallax-slides a
 *    fifth of the way in the same direction, mirrored on back. Reads as depth.
 *  - **Tab to tab** (the five bottom-bar destinations): a plain cross-fade -
 *    a horizontal slide there would imply an order the tabs do not have.
 */
private const val DURATION = 300

private fun AnimatedContentTransitionScope<NavBackStackEntry>.betweenTabs(): Boolean =
    initialState.destination.route in TAB_ROUTES && targetState.destination.route in TAB_ROUTES

private val TAB_ROUTES: Set<String> = setOf(
    Routes.HOME,
    Routes.ROADMAP,
    Routes.LEARNING_PATH,
    Routes.PRACTICE,
    Routes.INTERVIEW_HOME,
    Routes.PROFILE,
    Routes.PATTERNS,
    Routes.RATINGS,
    Routes.ACHIEVEMENTS,
    Routes.SETTINGS,
)

private fun fade() = fadeIn(tween(DURATION))
private fun unfade() = fadeOut(tween(DURATION))
private fun slideSpec() = tween<IntOffset>(DURATION, easing = FastOutSlowInEasing)

val navEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (betweenTabs()) fade()
    else slideInHorizontally(slideSpec()) { it } + fade()
}

val navExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (betweenTabs()) unfade()
    else slideOutHorizontally(slideSpec()) { -it / 5 } + unfade()
}

val navPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (betweenTabs()) fade()
    else slideInHorizontally(slideSpec()) { -it / 5 } + fade()
}

val navPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (betweenTabs()) unfade()
    else slideOutHorizontally(slideSpec()) { it } + unfade()
}
