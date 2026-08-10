package com.codingarena.domain.engine

import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.problem
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RatingEngineTest {

    private val engine = RatingEngine()

    @Test
    fun `equal ratings give a coin flip expected score`() {
        assertEquals(0.5, engine.expectedScore(1200, 1200), absoluteTolerance = 1e-9)
    }

    @Test
    fun `a 400 point gap matches the Elo ten to one rule`() {
        assertEquals(10.0 / 11.0, engine.expectedScore(1400, 1000), absoluteTolerance = 1e-9)
        assertEquals(1.0 / 11.0, engine.expectedScore(1000, 1400), absoluteTolerance = 1e-9)
    }

    @Test
    fun `beating a stronger problem gains more than beating a weaker one`() {
        val ratings = seasonedRatings(1200)

        val hardGain = engine.rate(ratings, problem(rating = 1400), AnswerOutcome.CORRECT_NO_HINTS)
            .second.overallChange
        val easyGain = engine.rate(ratings, problem(rating = 1100), AnswerOutcome.CORRECT_NO_HINTS)
            .second.overallChange

        assertTrue(hardGain > easyGain, "hard=$hardGain should beat easy=$easyGain")
    }

    @Test
    fun `losing to an easy problem costs more than losing to a hard one`() {
        val ratings = seasonedRatings(1200)

        val easyLoss = engine.rate(ratings, problem(rating = 900), AnswerOutcome.INCORRECT)
            .second.overallChange
        val hardLoss = engine.rate(ratings, problem(rating = 1500), AnswerOutcome.INCORRECT)
            .second.overallChange

        assertTrue(easyLoss < hardLoss, "easyLoss=$easyLoss should be worse than hardLoss=$hardLoss")
        assertTrue(easyLoss < 0 && hardLoss < 0)
    }

    @Test
    fun `farming far easier problems yields almost nothing but failing them still hurts`() {
        val ratings = seasonedRatings(1500)
        val trivial = problem(rating = 900) // 600 below the user

        val gain = engine.rate(ratings, trivial, AnswerOutcome.CORRECT_NO_HINTS).second.overallChange
        val loss = engine.rate(ratings, trivial, AnswerOutcome.INCORRECT).second.overallChange

        assertTrue(gain in 0..2, "expected a negligible gain, got $gain")
        assertTrue(abs(loss) > gain * 5, "expected the loss ($loss) to dwarf the gain ($gain)")
    }

    @Test
    fun `rating changes are capped`() {
        val config = RatingConfig(maxChangePerAttempt = 10)
        val capped = RatingEngine(config)
        val ratings = seasonedRatings(2400)

        val (_, update) = capped.rate(ratings, problem(rating = 400), AnswerOutcome.INCORRECT)

        assertEquals(-10, update.overallChange)
        assertTrue(update.wasCapped)
    }

    @Test
    fun `hints and retries reduce the gain`() {
        val ratings = seasonedRatings(1200)
        val p = problem(rating = 1300)

        val clean = engine.rate(ratings, p, AnswerOutcome.CORRECT_NO_HINTS).second.overallChange
        val hinted = engine.rate(ratings, p, AnswerOutcome.CORRECT_WITH_HINT).second.overallChange
        val retried = engine.rate(ratings, p, AnswerOutcome.CORRECT_AFTER_RETRIES).second.overallChange

        assertTrue(clean > hinted, "clean=$clean hinted=$hinted")
        assertTrue(hinted > retried, "hinted=$hinted retried=$retried")
    }

    @Test
    fun `the primary topic moves further than secondary topics`() {
        val ratings = seasonedRatings(1200)
        val p = problem(
            rating = 1400,
            topic = CodingTopic.GRAPHS,
            secondary = listOf(CodingTopic.RECURSION),
        )

        val (_, update) = engine.rate(ratings, p, AnswerOutcome.CORRECT_NO_HINTS)

        val primary = update.topicChanges.getValue(CodingTopic.GRAPHS)
        val secondary = update.topicChanges.getValue(CodingTopic.RECURSION)
        assertTrue(primary > secondary, "primary=$primary secondary=$secondary")
        assertTrue(secondary > 0)
    }

    @Test
    fun `provisional ratings move faster than settled ones`() {
        val fresh = PlayerRatings(overall = 1200)
        val settled = seasonedRatings(1200)
        val p = problem(rating = 1400)

        val freshChange = engine.rate(fresh, p, AnswerOutcome.CORRECT_NO_HINTS).second.overallChange
        val settledChange = engine.rate(settled, p, AnswerOutcome.CORRECT_NO_HINTS).second.overallChange

        assertTrue(freshChange > settledChange, "fresh=$freshChange settled=$settledChange")
    }

    @Test
    fun `code rush answers move ratings less than deliberate practice`() {
        val ratings = seasonedRatings(1200)
        val p = problem(rating = 1400)

        val practice = engine.rate(ratings, p, AnswerOutcome.CORRECT_NO_HINTS, AttemptSource.PRACTICE)
            .second.overallChange
        val rush = engine.rate(ratings, p, AnswerOutcome.CORRECT_NO_HINTS, AttemptSource.CODE_RUSH)
            .second.overallChange

        assertTrue(rush < practice, "rush=$rush practice=$practice")
        assertTrue(rush > 0)
    }

    @Test
    fun `attempt counters and accuracy are tracked per topic`() {
        var ratings = seasonedRatings(1200)
        val p = problem(rating = 1200, topic = CodingTopic.TREES)

        ratings = engine.rate(ratings, p, AnswerOutcome.CORRECT_NO_HINTS, now = 5L).first
        ratings = engine.rate(ratings, p, AnswerOutcome.INCORRECT, now = 9L).first

        val trees = ratings.topics.getValue(CodingTopic.TREES)
        assertEquals(12, trees.attempts) // 10 seeded + 2
        assertEquals(11, trees.correctAnswers) // 10 seeded + 1
        assertEquals(9L, trees.lastPracticedAt)
    }

    @Test
    fun `ratings never fall below the floor or exceed the ceiling`() {
        val config = RatingConfig(floorRating = 400, ceilingRating = 3000)
        val bounded = RatingEngine(config)

        var low = seasonedRatings(410)
        repeat(20) {
            low = bounded.rate(low, problem(rating = 400), AnswerOutcome.INCORRECT).first
        }
        assertEquals(400, low.overall)

        var high = seasonedRatings(2990)
        repeat(20) {
            high = bounded.rate(high, problem(rating = 3000), AnswerOutcome.CORRECT_NO_HINTS).first
        }
        assertEquals(3000, high.overall)
    }

    @Test
    fun `seeded ratings favour topics the user already knows`() {
        val seeded = engine.seedRatings(1150, setOf(CodingTopic.ARRAYS, CodingTopic.STRINGS))

        assertEquals(1150, seeded.overall)
        assertTrue(seeded.topicRating(CodingTopic.ARRAYS) > 1150)
        assertTrue(seeded.topicRating(CodingTopic.GRAPHS) < 1150)
        assertTrue(seeded.topics.values.all { it.isProvisional })
    }

    @Test
    fun `weakest and strongest topics ignore untouched ones`() {
        var ratings = engine.seedRatings(1200, emptySet())
        ratings = engine.rate(ratings, problem(rating = 1600, topic = CodingTopic.GRAPHS), AnswerOutcome.INCORRECT).first
        ratings = engine.rate(ratings, problem(rating = 1000, topic = CodingTopic.ARRAYS), AnswerOutcome.CORRECT_NO_HINTS).first

        assertEquals(listOf(CodingTopic.GRAPHS), ratings.weakestTopics(1).map { it.topic })
        assertEquals(listOf(CodingTopic.ARRAYS), ratings.strongestTopics(1).map { it.topic })
    }

    /** Ratings with enough attempts behind them to be out of the provisional window. */
    private fun seasonedRatings(overall: Int): PlayerRatings = PlayerRatings(
        overall = overall,
        topics = CodingTopic.entries.associateWith {
            com.codingarena.domain.model.UserTopicRating(
                topic = it,
                rating = overall,
                attempts = 10,
                correctAnswers = 10,
            )
        },
        modes = emptyMap(),
    )
}
