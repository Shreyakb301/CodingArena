package com.codingarena.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.codingarena.domain.model.AttemptSource
import com.codingarena.features.achievements.AchievementsScreen
import com.codingarena.features.blitz.BlitzScreen
import com.codingarena.features.challenge.ChallengeScreen
import com.codingarena.features.coderush.CodeRushScreen
import com.codingarena.features.coderush.CodeRushSessionScreen
import com.codingarena.features.home.HomeScreen
import com.codingarena.features.learningpath.LearningPathScreen
import com.codingarena.features.onboarding.OnboardingScreen
import com.codingarena.features.patternlibrary.PatternDetailScreen
import com.codingarena.features.patternlibrary.PatternLibraryScreen
import com.codingarena.features.profile.ProfileScreen
import com.codingarena.features.ratings.RatingsScreen
import com.codingarena.features.roadmap.RoadmapScreen
import com.codingarena.features.settings.SettingsScreen
import com.codingarena.features.solutionreview.SolutionReviewScreen
import com.codingarena.features.splash.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onOnboardingNeeded = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onReady = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenProblem = { id, source -> navController.navigate(Routes.challenge(id, source)) },
                onOpenCodeRush = { navController.navigate(Routes.CODE_RUSH) },
                onOpenLearningPath = { navController.navigate(Routes.LEARNING_PATH) },
                onOpenRatings = { navController.navigate(Routes.RATINGS) },
            )
        }

        composable(
            route = Routes.CHALLENGE_ROUTE,
            arguments = listOf(
                navArgument(Routes.ARG_PROBLEM_ID) { type = NavType.StringType },
                navArgument(Routes.ARG_SOURCE) { type = NavType.StringType },
            ),
        ) { entry ->
            val problemId = entry.stringArg(Routes.ARG_PROBLEM_ID).orEmpty()
            val source = entry.stringArg(Routes.ARG_SOURCE)
                ?.let { name -> AttemptSource.entries.firstOrNull { it.name == name } }
                ?: AttemptSource.PRACTICE

            ChallengeScreen(
                problemId = problemId,
                source = source,
                onSubmitted = { attemptId ->
                    navController.navigate(Routes.review(attemptId)) {
                        // The challenge is finished - going back should return
                        // to where the user came from, not re-open the question.
                        popUpTo(Routes.CHALLENGE_ROUTE) { inclusive = true }
                    }
                },
                onExit = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.REVIEW_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_ATTEMPT_ID) { type = NavType.StringType }),
        ) { entry ->
            SolutionReviewScreen(
                attemptId = entry.stringArg(Routes.ARG_ATTEMPT_ID).orEmpty(),
                onPractiseNext = { id ->
                    navController.navigate(Routes.challenge(id)) {
                        popUpTo(Routes.REVIEW_ROUTE) { inclusive = true }
                    }
                },
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.RATINGS) {
            RatingsScreen(onOpenTopicPractice = { id -> navController.navigate(Routes.challenge(id)) })
        }

        composable(Routes.PATTERNS) {
            PatternLibraryScreen(
                onOpenPattern = { id -> navController.navigate(Routes.patternDetail(id)) },
            )
        }

        composable(
            route = Routes.PATTERN_DETAIL_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_PATTERN_ID) { type = NavType.StringType }),
        ) { entry ->
            PatternDetailScreen(
                patternId = entry.stringArg(Routes.ARG_PATTERN_ID).orEmpty(),
                onOpenProblem = { id -> navController.navigate(Routes.challenge(id)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.LEARNING_PATH) {
            LearningPathScreen(
                onOpenProblem = { id ->
                    navController.navigate(Routes.challenge(id, AttemptSource.LEARNING_PATH))
                },
                onOpenPattern = { id -> navController.navigate(Routes.patternDetail(id)) },
            )
        }

        composable(Routes.CODE_RUSH) {
            CodeRushScreen(
                onStart = { mode -> navController.navigate(Routes.codeRushSession(mode)) },
            )
        }

        composable(
            route = Routes.CODE_RUSH_SESSION_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_MODE) { type = NavType.StringType }),
        ) { entry ->
            CodeRushSessionScreen(
                modeKey = entry.stringArg(Routes.ARG_MODE).orEmpty(),
                onFinished = { navController.popBackStack(Routes.CODE_RUSH, inclusive = false) },
            )
        }

        composable(Routes.ROADMAP) {
            RoadmapScreen(
                onStartBlitz = { mode -> navController.navigate(Routes.blitz(mode)) },
                // Roadmap entries are LeetCode problems, not bundled ones, so
                // tapping through opens the pattern lesson rather than a local
                // challenge that does not exist.
                onOpenProblem = { navController.navigate(Routes.PATTERNS) },
            )
        }

        composable(
            route = Routes.BLITZ_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_MODE) { type = NavType.StringType }),
        ) { entry ->
            BlitzScreen(
                modeKey = entry.stringArg(Routes.ARG_MODE).orEmpty(),
                onDone = { navController.popBackStack(Routes.ROADMAP, inclusive = false) },
            )
        }

        composable(Routes.ACHIEVEMENTS) { AchievementsScreen() }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onOpenAchievements = { navController.navigate(Routes.ACHIEVEMENTS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenRatings = { navController.navigate(Routes.RATINGS) },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Navigation 2.9 exposes route arguments as a multiplatform [androidx.savedstate.SavedState]
 * rather than an Android `Bundle`, so reads go through a `read { }` scope. Wrapped
 * here so the call sites above stay one line each.
 */
private fun NavBackStackEntry.stringArg(key: String): String? =
    arguments?.read { getStringOrNull(key) }
