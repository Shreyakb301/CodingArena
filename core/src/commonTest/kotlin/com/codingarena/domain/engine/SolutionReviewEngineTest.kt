package com.codingarena.domain.engine

import com.codingarena.attempt
import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.ReviewLabel
import com.codingarena.problem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SolutionReviewEngineTest {

    private val engine = SolutionReviewEngine()

    @Test
    fun `a fast clean solve well above your level is brilliant`() {
        assertEquals(
            ReviewLabel.BRILLIANT,
            engine.labelFor(AnswerOutcome.CORRECT_NO_HINTS, expected = 0.20, solvedQuickly = true),
        )
    }

    @Test
    fun `the same solve taken slowly is only a best move`() {
        assertEquals(
            ReviewLabel.BEST_MOVE,
            engine.labelFor(AnswerOutcome.CORRECT_NO_HINTS, expected = 0.20, solvedQuickly = false),
        )
    }

    @Test
    fun `missing something you should have got is a blunder`() {
        assertEquals(
            ReviewLabel.BLUNDER,
            engine.labelFor(AnswerOutcome.INCORRECT, expected = 0.90, solvedQuickly = false),
        )
    }

    @Test
    fun `missing a problem far above your level is only an inaccuracy`() {
        assertEquals(
            ReviewLabel.INACCURACY,
            engine.labelFor(AnswerOutcome.INCORRECT, expected = 0.15, solvedQuickly = false),
        )
    }

    @Test
    fun `an even-money miss is a mistake`() {
        assertEquals(
            ReviewLabel.MISTAKE,
            engine.labelFor(AnswerOutcome.INCORRECT, expected = 0.55, solvedQuickly = false),
        )
    }

    @Test
    fun `solving with a hint is good, never a mistake`() {
        assertEquals(
            ReviewLabel.GOOD,
            engine.labelFor(AnswerOutcome.CORRECT_WITH_HINT, expected = 0.9, solvedQuickly = true),
        )
    }

    @Test
    fun `a wrong answer explains why that specific choice failed`() {
        val p = problem(rating = 1000)
        val review = engine.build(
            problem = p,
            attempt = attempt(outcome = AnswerOutcome.INCORRECT, selected = listOf("b")),
            userRatingBefore = 1000,
        )

        assertEquals("This is O(n^2).", review.whyItMatters)
        assertTrue(review.mistake!!.contains("Wrong choice"))
        assertTrue(review.bestMove.contains("Correct choice"))
    }

    @Test
    fun `a wrong answer still credits what the user got right`() {
        val review = engine.build(
            problem = problem(),
            attempt = attempt(outcome = AnswerOutcome.INCORRECT, selected = listOf("b")),
            userRatingBefore = 1000,
        )

        assertEquals("The instinct to scan twice is natural.", review.goodMove)
    }

    @Test
    fun `a clean solve has a good move and no mistake`() {
        val review = engine.build(
            problem = problem(),
            attempt = attempt(outcome = AnswerOutcome.CORRECT_NO_HINTS),
            userRatingBefore = 1000,
        )

        assertNotNull(review.goodMove)
        assertNull(review.mistake)
        assertEquals(ReviewLabel.BEST_MOVE, review.label)
    }

    @Test
    fun `hint usage is named in the mistake line`() {
        val review = engine.build(
            problem = problem(),
            attempt = attempt(outcome = AnswerOutcome.CORRECT_WITH_HINT, hintsUsed = 1),
            userRatingBefore = 1000,
        )

        assertTrue(review.mistake!!.contains("one hint"), review.mistake!!)
    }

    @Test
    fun `every review carries complexity and common mistakes from the content`() {
        val review = engine.build(
            problem = problem(),
            attempt = attempt(),
            userRatingBefore = 1000,
        )

        assertEquals("O(n)", review.timeComplexity)
        assertEquals("O(n)", review.spaceComplexity)
        assertEquals(listOf("Forgetting the empty input"), review.commonMistakes)
    }

    @Test
    fun `recommendations after a miss are easier than the problem just failed`() {
        val failed = problem(id = "hard", rating = 1400)
        val candidates = listOf(
            failed,
            problem(id = "easier", rating = 1000),
            problem(id = "harder", rating = 1600),
        )

        val review = engine.build(
            problem = failed,
            attempt = attempt(problemId = "hard", outcome = AnswerOutcome.INCORRECT, selected = listOf("b")),
            userRatingBefore = 1300,
            candidates = candidates,
        )

        assertEquals("easier", review.recommendedPractice.first().problemId)
        assertTrue(review.recommendedPractice.none { it.problemId == "hard" })
    }

    @Test
    fun `recommendations after a solve step up`() {
        val solved = problem(id = "solved", rating = 1200)
        val candidates = listOf(
            solved,
            problem(id = "easier", rating = 900),
            problem(id = "next", rating = 1260),
        )

        val review = engine.build(
            problem = solved,
            attempt = attempt(problemId = "solved"),
            userRatingBefore = 1200,
            candidates = candidates,
        )

        assertEquals("next", review.recommendedPractice.first().problemId)
    }

    @Test
    fun `a review always explains its label`() {
        AnswerOutcome.entries.forEach { outcome ->
            val review = engine.build(
                problem = problem(),
                attempt = attempt(outcome = outcome, selected = listOf("b")),
                userRatingBefore = 1000,
            )
            assertTrue(review.whyItMatters.isNotBlank(), "no explanation for $outcome")
            assertTrue(review.bestMove.isNotBlank(), "no best move for $outcome")
            assertTrue(review.headline.isNotBlank(), "no headline for $outcome")
        }
    }
}
