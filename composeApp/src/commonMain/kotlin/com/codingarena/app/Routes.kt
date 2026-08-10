package com.codingarena.app

import com.codingarena.domain.model.AttemptSource

/**
 * Every destination in the app.
 *
 * Routes are plain strings because Compose Multiplatform navigation is still
 * string-based on iOS; the helpers below keep argument construction in one
 * place rather than scattering string templates across screens.
 */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val RATINGS = "ratings"
    const val PATTERNS = "patterns"
    const val LEARNING_PATH = "learningPath"
    const val ACHIEVEMENTS = "achievements"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val CODE_RUSH = "codeRush"
    const val ROADMAP = "roadmap"

    const val CHALLENGE_ROUTE = "challenge/{problemId}/{source}"
    const val REVIEW_ROUTE = "review/{attemptId}"
    const val PATTERN_DETAIL_ROUTE = "pattern/{patternId}"
    const val CODE_RUSH_SESSION_ROUTE = "codeRush/session/{mode}"
    const val BLITZ_ROUTE = "blitz/{mode}"

    const val ARG_PROBLEM_ID = "problemId"
    const val ARG_SOURCE = "source"
    const val ARG_ATTEMPT_ID = "attemptId"
    const val ARG_PATTERN_ID = "patternId"
    const val ARG_MODE = "mode"

    fun challenge(problemId: String, source: AttemptSource = AttemptSource.PRACTICE): String =
        "challenge/$problemId/${source.name}"

    fun review(attemptId: String): String = "review/$attemptId"

    fun patternDetail(patternId: String): String = "pattern/$patternId"

    fun codeRushSession(mode: String): String = "codeRush/session/$mode"

    fun blitz(mode: String): String = "blitz/$mode"
}

/** The five tabs in the bottom bar. */
enum class TopLevelDestination(val route: String, val label: String) {
    HOME(Routes.HOME, "Home"),
    ROADMAP(Routes.ROADMAP, "Roadmap"),
    PATTERNS(Routes.PATTERNS, "Patterns"),
    RATINGS(Routes.RATINGS, "Ratings"),
    PROFILE(Routes.PROFILE, "Profile"),
}
