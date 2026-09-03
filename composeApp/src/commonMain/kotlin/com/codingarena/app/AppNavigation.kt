package com.codingarena.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.BehavioralCategory
import com.codingarena.domain.model.PatternGroup
import com.codingarena.content.NeetCode150
import com.codingarena.content.RoadmapLessons
import com.codingarena.features.achievements.AchievementsScreen
import com.codingarena.features.blitz.BlitzScreen
import com.codingarena.features.blitz.BlitzHubScreen
import com.codingarena.features.blitz.StrategyBlitzScreen
import com.codingarena.features.course.ChapterScreen
import com.codingarena.features.course.CourseRoadmapScreen
import com.codingarena.features.editor.CodeEditorScreen
import com.codingarena.features.challenge.ChallengeScreen
import com.codingarena.features.coderush.CodeRushModes
import com.codingarena.features.coderush.CodeRushSessionScreen
import com.codingarena.features.home.HomeScreen
import com.codingarena.features.interview.BehavioralRoundScreen
import com.codingarena.features.interview.InterviewHomeScreen
import com.codingarena.features.interview.MockInterviewScreen
import com.codingarena.features.interview.TechCommProblemsScreen
import com.codingarena.features.interview.TechCommRoundScreen
import com.codingarena.features.learningpath.LearningPathScreen
import com.codingarena.features.onboarding.OnboardingScreen
import com.codingarena.features.patternlibrary.PatternDetailScreen
import com.codingarena.features.patternlibrary.PatternLibraryScreen
import com.codingarena.features.profile.ProfileScreen
import com.codingarena.features.practice.PracticeScreen
import com.codingarena.features.practice.TopicActivityScreen
import com.codingarena.features.practice.TopicFocusScreen
import com.codingarena.features.practice.WorkoutRunnerScreen
import com.codingarena.features.practice.WorkoutSource
import com.codingarena.features.ratings.RatingsScreen
import com.codingarena.features.roadmap.RoadmapScreen
import com.codingarena.features.roadmap.RoadmapLessonScreen
import com.codingarena.features.roadmap.RoadmapExplanationScreen
import com.codingarena.features.settings.SettingsScreen
import com.codingarena.features.solutionreview.SolutionReviewScreen
import com.codingarena.features.splash.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = navEnter,
        exitTransition = navExit,
        popEnterTransition = navPopEnter,
        popExitTransition = navPopExit,
    ) {

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
                onOpenPractice = { navController.navigate(Routes.PRACTICE) },
                onOpenLearningPath = { navController.navigate(Routes.LEARNING_PATH) },
                onOpenRatings = { navController.navigate(Routes.RATINGS) },
            )
        }

        composable(Routes.PRACTICE) {
            PracticeScreen(
                onStartRecommended = { navController.navigate(Routes.WORKOUT_RECOMMENDED) },
                onOpenTopicFocus = { navController.navigate(Routes.PRACTICE_TOPIC_FOCUS) },
                onStartBlitz = { mode -> navController.navigate(Routes.blitz(mode)) },
                onStartMixed = { navController.navigate(Routes.WORKOUT_MIXED) },
                onOpenCodeRush = { navController.navigate(Routes.codeRushSession(CodeRushModes.FIVE)) },
                onOpenQuickRecall = { navController.navigate(Routes.BLITZ_HOME) },
            )
        }

        composable(Routes.PRACTICE_TOPIC_FOCUS) {
            TopicFocusScreen(
                onBack = { navController.popBackStack() },
                onSelectTopic = { group -> navController.navigate(Routes.topicActivity(group.name)) },
            )
        }

        composable(
            route = Routes.PRACTICE_TOPIC_ACTIVITY_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_GROUP) { type = NavType.StringType }),
        ) { entry ->
            val group = entry.stringArg(Routes.ARG_GROUP)?.let { name ->
                PatternGroup.entries.firstOrNull { it.name == name }
            }
            if (group == null) {
                navController.popBackStack()
            } else {
                TopicActivityScreen(
                    group = group,
                    onBack = { navController.popBackStack() },
                    onCompleteWorkout = { navController.navigate(Routes.workoutTopic(it.name)) },
                    onLearnTheConcept = { slug, patternId ->
                        when {
                            slug != null -> navController.navigate(Routes.roadmapLesson(slug))
                            patternId != null -> navController.navigate(Routes.patternDetail(patternId))
                            else -> navController.navigate(Routes.PATTERNS)
                        }
                    },
                    onTimedPractice = { navController.navigate(Routes.codeRushSession(CodeRushModes.FIVE)) },
                )
            }
        }

        composable(Routes.WORKOUT_RECOMMENDED) {
            WorkoutRunnerScreen(
                source = WorkoutSource.Recommended,
                onExit = { navController.popBackStack() },
            )
        }

        composable(Routes.WORKOUT_MIXED) {
            WorkoutRunnerScreen(
                source = WorkoutSource.Mixed,
                onExit = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.WORKOUT_TOPIC_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_GROUP) { type = NavType.StringType }),
        ) { entry ->
            val group = entry.stringArg(Routes.ARG_GROUP)?.let { name ->
                PatternGroup.entries.firstOrNull { it.name == name }
            }
            if (group == null) {
                navController.popBackStack()
            } else {
                WorkoutRunnerScreen(
                    source = WorkoutSource.TopicFocused(group),
                    onExit = { navController.popBackStack() },
                )
            }
        }

        composable(Routes.INTERVIEW_HOME) {
            InterviewHomeScreen(
                onStartCategoryWorkout = { category -> navController.navigate(Routes.interviewCategoryWorkout(category.name)) },
                onOpenBehavioralWorkouts = { navController.navigate(Routes.INTERVIEW_ALL_WORKOUT) },
                onOpenTechComm = { navController.navigate(Routes.INTERVIEW_TECH_COMM) },
                onOpenMockInterview = { navController.navigate(Routes.INTERVIEW_MOCK) },
            )
        }

        composable(Routes.INTERVIEW_ALL_WORKOUT) {
            BehavioralRoundScreen(category = null, onExit = { navController.popBackStack() })
        }

        composable(
            route = Routes.INTERVIEW_CATEGORY_WORKOUT_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_CATEGORY) { type = NavType.StringType }),
        ) { entry ->
            val category = entry.stringArg(Routes.ARG_CATEGORY)?.let { name ->
                BehavioralCategory.entries.firstOrNull { it.name == name }
            }
            if (category == null) {
                navController.popBackStack()
            } else {
                BehavioralRoundScreen(category = category, onExit = { navController.popBackStack() })
            }
        }

        composable(Routes.INTERVIEW_TECH_COMM) {
            TechCommProblemsScreen(
                onBack = { navController.popBackStack() },
                onSelectProblem = { slug -> navController.navigate(Routes.interviewTechCommProblem(slug)) },
            )
        }

        composable(
            route = Routes.INTERVIEW_TECH_COMM_PROBLEM_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_PROBLEM_SLUG) { type = NavType.StringType }),
        ) { entry ->
            TechCommRoundScreen(
                problemSlug = entry.stringArg(Routes.ARG_PROBLEM_SLUG).orEmpty(),
                onDone = { navController.popBackStack() },
            )
        }

        composable(Routes.INTERVIEW_MOCK) {
            MockInterviewScreen(onExit = { navController.popBackStack() })
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

        composable(
            route = Routes.CODE_RUSH_SESSION_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_MODE) { type = NavType.StringType }),
        ) { entry ->
            CodeRushSessionScreen(
                modeKey = entry.stringArg(Routes.ARG_MODE).orEmpty(),
                onFinished = { navController.popBackStack() },
            )
        }

        composable(Routes.ROADMAP) {
            val uriHandler = LocalUriHandler.current
            RoadmapScreen(
                onStartBlitz = { mode -> navController.navigate(Routes.blitz(mode)) },
                onOpenProblem = { slug ->
                    if (RoadmapLessons.hasLesson(slug)) {
                        navController.navigate(Routes.roadmapLesson(slug))
                    } else {
                        NeetCode150.curriculum.bySlug(slug)?.url?.let(uriHandler::openUri)
                    }
                },
                onBrowsePatterns = { navController.navigate(Routes.PATTERNS) },
            )
        }

        composable(
            route = Routes.ROADMAP_LESSON_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_SLUG) { type = NavType.StringType }),
        ) { entry ->
            RoadmapLessonScreen(
                slug = entry.stringArg(Routes.ARG_SLUG).orEmpty(),
                onBack = { navController.popBackStack() },
                onReadExplanation = { slug ->
                    navController.navigate(Routes.roadmapExplanation(slug))
                },
                onNextProblem = { nextSlug ->
                    if (nextSlug != null) {
                        navController.navigate(Routes.roadmapLesson(nextSlug)) {
                            popUpTo(Routes.ROADMAP_LESSON_ROUTE) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack(Routes.ROADMAP, inclusive = false)
                    }
                },
            )
        }

        composable(
            route = Routes.ROADMAP_EXPLANATION_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_SLUG) { type = NavType.StringType }),
        ) { entry ->
            RoadmapExplanationScreen(
                slug = entry.stringArg(Routes.ARG_SLUG).orEmpty(),
                onBack = { navController.popBackStack() },
                onNextProblem = { nextSlug ->
                    if (nextSlug != null) {
                        navController.navigate(Routes.roadmapLesson(nextSlug)) {
                            popUpTo(Routes.ROADMAP_EXPLANATION_ROUTE) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack(Routes.ROADMAP, inclusive = false)
                    }
                },
            )
        }

        composable(
            route = Routes.CHAPTER_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_CHAPTER_ID) { type = NavType.StringType }),
        ) { entry ->
            ChapterScreen(
                chapterId = entry.stringArg(Routes.ARG_CHAPTER_ID).orEmpty(),
                onOpenEditor = { navController.navigate(Routes.editor(it)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EDITOR_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_EXERCISE_ID) { type = NavType.StringType }),
        ) { entry ->
            CodeEditorScreen(
                exerciseId = entry.stringArg(Routes.ARG_EXERCISE_ID).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.BLITZ_HOME) {
            BlitzHubScreen(
                onStart = { navController.navigate(Routes.blitz(it)) },
                onStartStrategy = { navController.navigate(Routes.STRATEGY_BLITZ) },
            )
        }

        composable(Routes.STRATEGY_BLITZ) {
            StrategyBlitzScreen(onDone = { navController.popBackStack() })
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
                onOpenPatterns = { navController.navigate(Routes.PATTERNS) },
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
