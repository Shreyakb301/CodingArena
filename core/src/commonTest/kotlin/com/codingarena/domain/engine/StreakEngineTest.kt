package com.codingarena.domain.engine

import com.codingarena.domain.model.StreakActivity
import com.codingarena.domain.model.StreakState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreakEngineTest {

    private val engine = StreakEngine()

    @Test
    fun `the first qualifying activity starts a streak`() {
        val result = engine.recordActivity(StreakState(), StreakActivity.DAILY_PUZZLE, epochDay = 100)

        assertTrue(result.extended)
        assertEquals(1, result.state.currentStreak)
        assertEquals(1, result.state.longestStreak)
        assertEquals(100L, result.state.lastActiveDay)
    }

    @Test
    fun `consecutive days extend the streak`() {
        var state = StreakState()
        (100L..104L).forEach { day ->
            state = engine.recordActivity(state, StreakActivity.LESSON, day).state
        }

        assertEquals(5, state.currentStreak)
        assertEquals(5, state.longestStreak)
    }

    @Test
    fun `a skipped day restarts the streak but keeps the record`() {
        var state = StreakState()
        (100L..104L).forEach { state = engine.recordActivity(state, StreakActivity.LESSON, it).state }

        val result = engine.recordActivity(state, StreakActivity.LESSON, epochDay = 106)

        assertTrue(result.streakBroken)
        assertEquals(1, result.state.currentStreak)
        assertEquals(5, result.state.longestStreak)
    }

    @Test
    fun `a second activity on the same day does not double count`() {
        var state = engine.recordActivity(StreakState(), StreakActivity.DAILY_PUZZLE, 100).state
        val second = engine.recordActivity(state, StreakActivity.CODE_RUSH, 100)

        assertFalse(second.extended)
        assertEquals(1, second.state.currentStreak)
        assertEquals(1, second.state.totalActiveDays)
        state = second.state
        assertEquals(setOf(100L), state.activeDaysThisWeek)
    }

    @Test
    fun `refresh clears a streak once a day has been missed`() {
        val state = engine.recordActivity(StreakState(), StreakActivity.LESSON, 100).state

        assertEquals(1, engine.refresh(state, today = 101).currentStreak)
        assertEquals(0, engine.refresh(state, today = 102).currentStreak)
    }

    @Test
    fun `a streak is at risk on the day after the last practice`() {
        val state = engine.recordActivity(StreakState(), StreakActivity.LESSON, 100).state

        assertFalse(engine.isStreakAtRisk(state, today = 100))
        assertTrue(engine.isStreakAtRisk(state, today = 101))
        assertFalse(engine.isStreakAtRisk(state, today = 102)) // already broken
    }

    @Test
    fun `weekly progress resets when the week rolls over`() {
        // Epoch day 0 is a Thursday, so day 4 is the following Monday.
        var state = StreakState()
        state = engine.recordActivity(state, StreakActivity.LESSON, 4).state // Monday
        state = engine.recordActivity(state, StreakActivity.LESSON, 5).state // Tuesday
        assertEquals(2, state.weeklyProgress)

        state = engine.recordActivity(state, StreakActivity.LESSON, 11).state // next Monday
        assertEquals(1, state.weeklyProgress)
    }

    @Test
    fun `the weekly goal is met once enough days are logged`() {
        var state = StreakState(weeklyGoalDays = 3)
        assertFalse(state.metWeeklyGoal)

        (4L..6L).forEach { state = engine.recordActivity(state, StreakActivity.LESSON, it).state }

        assertTrue(state.metWeeklyGoal)
    }

    @Test
    fun `code rush only counts after five questions`() {
        assertFalse(engine.codeRushQualifies(4))
        assertTrue(engine.codeRushQualifies(5))
        assertTrue(engine.codeRushQualifies(12))
    }
}
