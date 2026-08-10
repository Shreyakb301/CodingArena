package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * Activities that count as a real practice day. Opening the app is
 * deliberately absent (spec 5.12).
 */
@Serializable
enum class StreakActivity(val displayName: String) {
    DAILY_PUZZLE("Completed the Daily Puzzle"),
    LESSON("Finished a lesson"),
    CODE_RUSH("Completed five Code Rush questions"),
    SCHEDULED_REVIEW("Completed a scheduled review"),
    ;

    companion object {
        /** Code Rush only counts once this many questions are answered. */
        const val CODE_RUSH_QUESTION_THRESHOLD = 5
    }
}

/**
 * Streak state, stored as epoch days so that "did they practise yesterday?"
 * never turns into a timezone bug.
 */
@Serializable
data class StreakState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    /** Epoch day of the last day that earned credit; null if never. */
    val lastActiveDay: Long? = null,
    val weeklyGoalDays: Int = DEFAULT_WEEKLY_GOAL,
    /** Epoch days inside the current week that earned credit. */
    val activeDaysThisWeek: Set<Long> = emptySet(),
    val totalActiveDays: Int = 0,
) {
    val weeklyProgress: Int get() = activeDaysThisWeek.size

    val metWeeklyGoal: Boolean get() = weeklyProgress >= weeklyGoalDays

    fun isActiveOn(epochDay: Long): Boolean = lastActiveDay != null && lastActiveDay >= epochDay

    companion object {
        const val DEFAULT_WEEKLY_GOAL = 5
    }
}
