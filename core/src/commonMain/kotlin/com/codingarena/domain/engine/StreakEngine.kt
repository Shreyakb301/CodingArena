package com.codingarena.domain.engine

import com.codingarena.domain.model.StreakActivity
import com.codingarena.domain.model.StreakState

/**
 * Maintains daily streaks (spec 5.12).
 *
 * Works in epoch days, and only ever advances a streak in response to a
 * qualifying [StreakActivity] - there is deliberately no "app opened" entry
 * point into this engine.
 */
class StreakEngine {

    /**
     * Credits [activity] on [epochDay] and returns the new state.
     *
     * Idempotent within a day: a second qualifying activity on the same day
     * does not extend the streak twice.
     */
    fun recordActivity(
        state: StreakState,
        activity: StreakActivity,
        epochDay: Long,
    ): StreakResult {
        val last = state.lastActiveDay

        if (last != null && last >= epochDay) {
            // Already credited today (or the clock moved backwards).
            return StreakResult(state, extended = false, activity = activity)
        }

        val continues = last != null && epochDay - last == 1L
        val newCurrent = if (continues) state.currentStreak + 1 else 1
        val weekStart = weekStartDay(epochDay)
        val carriedWeek = state.activeDaysThisWeek.filter { it >= weekStart }.toSet()

        val updated = state.copy(
            currentStreak = newCurrent,
            longestStreak = maxOf(state.longestStreak, newCurrent),
            lastActiveDay = epochDay,
            activeDaysThisWeek = carriedWeek + epochDay,
            totalActiveDays = state.totalActiveDays + 1,
        )
        return StreakResult(
            state = updated,
            extended = true,
            activity = activity,
            streakBroken = last != null && !continues,
        )
    }

    /**
     * Recomputes state for a day on which nothing has been recorded yet.
     *
     * Called when the app opens so the UI can show "streak at risk" or a
     * lapsed streak without waiting for the user to answer something.
     */
    fun refresh(state: StreakState, today: Long): StreakState {
        val last = state.lastActiveDay ?: return state
        val weekStart = weekStartDay(today)
        val trimmedWeek = state.activeDaysThisWeek.filter { it >= weekStart }.toSet()
        val gap = today - last
        return when {
            gap <= 1L -> state.copy(activeDaysThisWeek = trimmedWeek)
            else -> state.copy(currentStreak = 0, activeDaysThisWeek = trimmedWeek)
        }
    }

    /** True when the user has a live streak but has not practised today yet. */
    fun isStreakAtRisk(state: StreakState, today: Long): Boolean {
        val last = state.lastActiveDay ?: return false
        return state.currentStreak > 0 && today - last == 1L
    }

    /**
     * Whether a Code Rush run is long enough to count as a practice day
     * (five questions, per spec 5.12).
     */
    fun codeRushQualifies(questionsAnswered: Int): Boolean =
        questionsAnswered >= StreakActivity.CODE_RUSH_QUESTION_THRESHOLD

    /** Epoch day 0 is a Thursday; this normalises weeks to start on Monday. */
    private fun weekStartDay(epochDay: Long): Long {
        val dayOfWeek = ((epochDay + 3) % 7 + 7) % 7 // 0 = Monday
        return epochDay - dayOfWeek
    }
}

data class StreakResult(
    val state: StreakState,
    val extended: Boolean,
    val activity: StreakActivity,
    val streakBroken: Boolean = false,
)
