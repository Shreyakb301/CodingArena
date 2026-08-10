package com.codingarena.domain.engine

import com.codingarena.content.AchievementCatalogue
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PlayerStats
import com.codingarena.domain.model.ReviewLabel
import com.codingarena.domain.model.StreakState
import com.codingarena.domain.model.UserTopicRating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AchievementEngineTest {

    private val engine = AchievementEngine(AchievementCatalogue.achievements)

    private fun context(
        completed: Int = 0,
        streak: Int = 0,
        hintFree: Int = 0,
        rushBest: Int = 0,
        patterns: Int = 0,
        overall: Int = 1000,
        startingRating: Int = 1000,
        topicRatings: Map<CodingTopic, Int> = emptyMap(),
        bestLabel: ReviewLabel? = null,
    ) = AchievementContext(
        stats = PlayerStats(totalCompleted = completed, totalCorrect = completed),
        ratings = PlayerRatings(
            overall = overall,
            topics = topicRatings.mapValues { (topic, rating) ->
                UserTopicRating(topic, rating, attempts = 10, correctAnswers = 8)
            },
        ),
        streak = StreakState(currentStreak = streak, longestStreak = streak),
        startingRating = startingRating,
        hintFreeStreak = hintFree,
        bestCodeRushScore = rushBest,
        patternsMastered = patterns,
        bestReviewLabel = bestLabel,
    )

    @Test
    fun `a brand new user has unlocked nothing`() {
        val progress = engine.evaluate(context())

        assertTrue(progress.none { it.isUnlocked }, "something unlocked with no activity")
        assertEquals(AchievementCatalogue.achievements.size, progress.size)
    }

    @Test
    fun `the first challenge unlocks the first badge`() {
        val unlocked = engine.evaluate(context(completed = 1), now = 500L)
            .filter { it.isUnlocked }
            .map { it.achievement.id }

        assertEquals(listOf("first-challenge"), unlocked)
    }

    @Test
    fun `streak badges unlock at their thresholds`() {
        assertFalse(unlockedIds(context(streak = 6)).contains("streak-7"))
        assertTrue(unlockedIds(context(streak = 7)).contains("streak-7"))
        assertFalse(unlockedIds(context(streak = 7)).contains("streak-30"))
        assertTrue(unlockedIds(context(streak = 30)).contains("streak-30"))
    }

    @Test
    fun `a longest streak counts even after the current one lapses`() {
        val lapsed = AchievementContext(
            stats = PlayerStats(),
            ratings = PlayerRatings(),
            streak = StreakState(currentStreak = 0, longestStreak = 9),
            startingRating = 1000,
        )

        assertTrue(engine.evaluate(lapsed).first { it.achievement.id == "streak-7" }.isUnlocked)
    }

    @Test
    fun `topic badges measure progress from the default rating`() {
        val halfway = engine.evaluate(context(topicRatings = mapOf(CodingTopic.ARRAYS to 1100)))
            .first { it.achievement.id == "arrays-apprentice" }

        assertFalse(halfway.isUnlocked)
        assertEquals(0.5f, halfway.fraction)

        assertTrue(unlockedIds(context(topicRatings = mapOf(CodingTopic.ARRAYS to 1200)))
            .contains("arrays-apprentice"))
    }

    @Test
    fun `rating gain is measured against where the user started`() {
        assertFalse(
            unlockedIds(context(overall = 1250, startingRating = 1200))
                .contains("rating-gain-100")
        )
        assertTrue(
            unlockedIds(context(overall = 1300, startingRating = 1200))
                .contains("rating-gain-100")
        )
    }

    @Test
    fun `a rating loss shows as zero progress rather than negative`() {
        val progress = engine.evaluate(context(overall = 900, startingRating = 1200))
            .first { it.achievement.id == "rating-gain-100" }

        assertEquals(0, progress.current)
        assertEquals(0f, progress.fraction)
    }

    @Test
    fun `code rush badges unlock in order`() {
        val ids = unlockedIds(context(rushBest = 25))

        assertTrue(ids.contains("code-rush-10"))
        assertTrue(ids.contains("code-rush-25"))
        assertFalse(ids.contains("code-rush-50"))
    }

    @Test
    fun `only a brilliant counts as a perfect review`() {
        assertFalse(unlockedIds(context(bestLabel = ReviewLabel.BEST_MOVE)).contains("first-perfect-review"))
        assertTrue(unlockedIds(context(bestLabel = ReviewLabel.BRILLIANT)).contains("first-perfect-review"))
    }

    @Test
    fun `newly unlocked excludes badges the user already holds`() {
        val ctx = context(completed = 1, streak = 7)
        val existing = mapOf("first-challenge" to 100L)

        val fresh = engine.newlyUnlocked(ctx, existing, now = 200L).map { it.id }

        assertEquals(listOf("streak-7"), fresh)
    }

    @Test
    fun `an unlock timestamp is never overwritten by a later evaluation`() {
        val ctx = context(completed = 5)
        val existing = mapOf("first-challenge" to 100L)

        val progress = engine.evaluate(ctx, existing, now = 900L)
            .first { it.achievement.id == "first-challenge" }

        assertEquals(100L, progress.unlockedAt)
    }

    @Test
    fun `progress never exceeds its target`() {
        engine.evaluate(context(completed = 999, streak = 999, rushBest = 999)).forEach {
            assertTrue(it.current <= it.target, "${it.achievement.id} overshot its target")
            assertTrue(it.fraction <= 1f)
        }
    }

    private fun unlockedIds(context: AchievementContext): List<String> =
        engine.evaluate(context, now = 1L).filter { it.isUnlocked }.map { it.achievement.id }
}
