package com.codingarena.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExperienceLevel(val displayName: String, val startingRating: Int) {
    NEW_TO_CODING("New to coding", 700),
    SOME_PRACTICE("Some practice", 950),
    COMFORTABLE("Comfortable with basics", 1150),
    INTERVIEW_READY("Interviewing already", 1350),
}

@Serializable
enum class TargetJobLevel(val displayName: String) {
    INTERNSHIP("Internship"),
    NEW_GRAD("New grad"),
    JUNIOR("Junior"),
    MID_LEVEL("Mid-level"),
    SENIOR("Senior"),
}

@Serializable
enum class ProgrammingLanguage(val displayName: String) {
    PYTHON("Python"),
    JAVA("Java"),
    JAVASCRIPT("JavaScript"),
    KOTLIN("Kotlin"),
    CPP("C++"),
    GO("Go"),
    SWIFT("Swift"),
}

@Serializable
enum class SessionLength(val displayName: String, val minutes: Int) {
    QUICK("5 minutes", 5),
    STANDARD("10 minutes", 10),
    LONG("15 minutes", 15),
}

/** Everything onboarding collects (spec 5.1). */
@Serializable
data class OnboardingAnswers(
    val experienceLevel: ExperienceLevel = ExperienceLevel.SOME_PRACTICE,
    val preferredLanguage: ProgrammingLanguage = ProgrammingLanguage.PYTHON,
    val targetJobLevel: TargetJobLevel = TargetJobLevel.NEW_GRAD,
    val targetCompanies: List<String> = emptyList(),
    val weeklyGoalDays: Int = StreakState.DEFAULT_WEEKLY_GOAL,
    val sessionLength: SessionLength = SessionLength.STANDARD,
    val knownTopics: Set<CodingTopic> = emptySet(),
    val placementRating: Int? = null,
)

@Serializable
data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val isGuest: Boolean = true,
    val onboarding: OnboardingAnswers = OnboardingAnswers(),
    val createdAt: Long = 0L,
    val startingRating: Int = PlayerRatings.DEFAULT_RATING,
)

/** Headline counters for the profile screen. */
@Serializable
data class PlayerStats(
    val totalCompleted: Int = 0,
    val totalCorrect: Int = 0,
    val totalHintsUsed: Int = 0,
    val averageSolveMs: Long = 0L,
    val problemsSeen: Int = 0,
    val patternsMastered: Int = 0,
    val upcomingReviews: Int = 0,
) {
    val accuracy: Double
        get() = if (totalCompleted == 0) 0.0 else totalCorrect.toDouble() / totalCompleted
}

/**
 * A blunt "how ready are you" number.
 *
 * Deliberately conservative: it is capped by breadth, so a user with a strong
 * Arrays rating and nothing else cannot read as interview ready.
 */
@Serializable
data class InterviewReadiness(
    val score: Int,
    val band: ReadinessBand,
    val rationale: String,
    val limitingTopics: List<CodingTopic> = emptyList(),
)

@Serializable
enum class ReadinessBand(val displayName: String, val range: IntRange) {
    STARTING_OUT("Starting out", 0..24),
    BUILDING("Building fundamentals", 25..49),
    PROGRESSING("Progressing well", 50..69),
    NEARLY_READY("Nearly interview ready", 70..84),
    READY("Interview ready", 85..100),
    ;

    companion object {
        fun forScore(score: Int): ReadinessBand =
            entries.first { score.coerceIn(0, 100) in it.range }
    }
}
