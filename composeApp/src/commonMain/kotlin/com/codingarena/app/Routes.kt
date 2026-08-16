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
    const val PRACTICE = "practice"
    const val RATINGS = "ratings"
    const val PATTERNS = "patterns"
    const val LEARNING_PATH = "learningPath"
    const val ACHIEVEMENTS = "achievements"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val ROADMAP = "roadmap"
    const val BLITZ_HOME = "blitzHome"
    const val STRATEGY_BLITZ = "strategyBlitz"

    const val CHALLENGE_ROUTE = "challenge/{problemId}/{source}"
    const val REVIEW_ROUTE = "review/{attemptId}"
    const val PATTERN_DETAIL_ROUTE = "pattern/{patternId}"
    const val CODE_RUSH_SESSION_ROUTE = "codeRush/session/{mode}"
    const val BLITZ_ROUTE = "blitz/{mode}"
    const val CHAPTER_ROUTE = "course/chapter/{chapterId}"
    const val EDITOR_ROUTE = "course/editor/{exerciseId}"
    const val ROADMAP_LESSON_ROUTE = "roadmap/lesson/{slug}"
    const val ROADMAP_EXPLANATION_ROUTE = "roadmap/lesson/{slug}/explanation"

    const val PRACTICE_TOPIC_FOCUS = "practice/topic"
    const val PRACTICE_TOPIC_ACTIVITY_ROUTE = "practice/topic/{group}"
    const val WORKOUT_RECOMMENDED = "practice/workout/recommended"
    const val WORKOUT_MIXED = "practice/workout/mixed"
    const val WORKOUT_TOPIC_ROUTE = "practice/workout/topic/{group}"

    const val INTERVIEW_HOME = "interview"
    const val INTERVIEW_ALL_WORKOUT = "interview/workout/all"
    const val INTERVIEW_CATEGORY_WORKOUT_ROUTE = "interview/categories/{category}/workout"
    const val INTERVIEW_TECH_COMM = "interview/techcomm"
    const val INTERVIEW_TECH_COMM_PROBLEM_ROUTE = "interview/techcomm/{problemSlug}"
    const val INTERVIEW_MOCK = "interview/mock"

    const val ARG_PROBLEM_ID = "problemId"
    const val ARG_SOURCE = "source"
    const val ARG_ATTEMPT_ID = "attemptId"
    const val ARG_PATTERN_ID = "patternId"
    const val ARG_MODE = "mode"
    const val ARG_CHAPTER_ID = "chapterId"
    const val ARG_EXERCISE_ID = "exerciseId"
    const val ARG_SLUG = "slug"
    const val ARG_GROUP = "group"
    const val ARG_CATEGORY = "category"
    const val ARG_PROBLEM_SLUG = "problemSlug"

    fun challenge(problemId: String, source: AttemptSource = AttemptSource.PRACTICE): String =
        "challenge/$problemId/${source.name}"

    fun review(attemptId: String): String = "review/$attemptId"

    fun patternDetail(patternId: String): String = "pattern/$patternId"

    fun codeRushSession(mode: String): String = "codeRush/session/$mode"

    fun blitz(mode: String): String = "blitz/$mode"
    fun chapter(id: String): String = "course/chapter/$id"
    fun editor(id: String): String = "course/editor/$id"
    fun roadmapLesson(slug: String): String = "roadmap/lesson/$slug"
    fun roadmapExplanation(slug: String): String = "roadmap/lesson/$slug/explanation"

    fun topicActivity(group: String): String = "practice/topic/$group"
    fun workoutTopic(group: String): String = "practice/workout/topic/$group"

    fun interviewCategoryWorkout(category: String): String = "interview/categories/$category/workout"
    fun interviewTechCommProblem(problemSlug: String): String = "interview/techcomm/$problemSlug"
}

/** The five primary jobs: orient, progress, practise, interview-prep, and manage. */
enum class TopLevelDestination(val route: String, val label: String) {
    HOME(Routes.HOME, "Home"),
    PATH(Routes.ROADMAP, "Roadmap"),
    PRACTICE(Routes.PRACTICE, "Practice"),
    INTERVIEW(Routes.INTERVIEW_HOME, "Interview"),
    MORE(Routes.PROFILE, "More"),
}
