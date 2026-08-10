package com.codingarena.domain.engine

import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PlayerStats
import com.codingarena.domain.model.ReadinessBand
import com.codingarena.domain.model.TargetJobLevel
import com.codingarena.domain.model.UserTopicRating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadinessEngineTest {

    private val engine = ReadinessEngine()
    private val core = ReadinessConfig().coreTopics

    @Test
    fun `too little practice gives no estimate`() {
        val readiness = engine.estimate(
            ratings = ratings(CodingTopic.ARRAYS to 1400),
            stats = PlayerStats(totalCompleted = 4, totalCorrect = 4),
            targetLevel = TargetJobLevel.NEW_GRAD,
        )

        assertEquals(0, readiness.score)
        assertEquals(ReadinessBand.STARTING_OUT, readiness.band)
        assertTrue(readiness.rationale.contains("at least"), readiness.rationale)
    }

    @Test
    fun `one strong topic cannot carry a weak profile`() {
        val lopsided = engine.estimate(
            ratings = ratings(
                CodingTopic.ARRAYS to 1800,
                CodingTopic.STRINGS to 1750,
                CodingTopic.HASH_MAPS to 1700,
                CodingTopic.GRAPHS to 600,
            ),
            stats = PlayerStats(totalCompleted = 40, totalCorrect = 32),
            targetLevel = TargetJobLevel.NEW_GRAD,
        )

        assertTrue(lopsided.score < 60, "expected a modest score, got ${lopsided.score}")
        assertEquals(CodingTopic.GRAPHS, lopsided.limitingTopics.first())
    }

    @Test
    fun `broad and even practice reads as ready`() {
        val even = engine.estimate(
            ratings = ratings(*core.map { it to 1450 }.toTypedArray()),
            stats = PlayerStats(totalCompleted = 120, totalCorrect = 102),
            targetLevel = TargetJobLevel.NEW_GRAD,
        )

        assertTrue(even.score >= 85, "expected a ready score, got ${even.score}")
        assertEquals(ReadinessBand.READY, even.band)
    }

    @Test
    fun `the same profile reads lower against a senior target`() {
        val topics = core.map { it to 1400 }.toTypedArray()
        val stats = PlayerStats(totalCompleted = 100, totalCorrect = 85)

        val newGrad = engine.estimate(ratings(*topics), stats, TargetJobLevel.NEW_GRAD)
        val senior = engine.estimate(ratings(*topics), stats, TargetJobLevel.SENIOR)

        assertTrue(senior.score < newGrad.score, "senior=${senior.score} newGrad=${newGrad.score}")
    }

    @Test
    fun `narrow practice is called out as a breadth problem`() {
        val narrow = engine.estimate(
            ratings = ratings(
                CodingTopic.ARRAYS to 1400,
                CodingTopic.STRINGS to 1400,
                CodingTopic.HASH_MAPS to 1400,
            ),
            stats = PlayerStats(totalCompleted = 30, totalCorrect = 27),
            targetLevel = TargetJobLevel.NEW_GRAD,
        )

        assertTrue(narrow.rationale.contains("core topics"), narrow.rationale)
        assertTrue(narrow.score < 70)
    }

    @Test
    fun `the weakest core topic is named in the rationale`() {
        val readiness = engine.estimate(
            ratings = ratings(*core.map { it to 1400 }.toTypedArray(), CodingTopic.GRAPHS to 900),
            stats = PlayerStats(totalCompleted = 80, totalCorrect = 64),
            targetLevel = TargetJobLevel.NEW_GRAD,
        )

        assertTrue(readiness.rationale.contains("Graphs"), readiness.rationale)
    }

    @Test
    fun `scores stay inside zero to one hundred`() {
        val maxed = engine.estimate(
            ratings = ratings(*core.map { it to 3000 }.toTypedArray()),
            stats = PlayerStats(totalCompleted = 500, totalCorrect = 500),
            targetLevel = TargetJobLevel.INTERNSHIP,
        )

        assertEquals(100, maxed.score)
        assertEquals(ReadinessBand.READY, maxed.band)
    }

    private fun ratings(vararg entries: Pair<CodingTopic, Int>): PlayerRatings {
        // Later entries win, so callers can override one topic after a bulk set.
        val topics = linkedMapOf<CodingTopic, UserTopicRating>()
        entries.forEach { (topic, rating) ->
            topics[topic] = UserTopicRating(topic, rating, attempts = 10, correctAnswers = 8)
        }
        return PlayerRatings(
            overall = topics.values.map { it.rating }.average().toInt(),
            topics = topics,
        )
    }
}
