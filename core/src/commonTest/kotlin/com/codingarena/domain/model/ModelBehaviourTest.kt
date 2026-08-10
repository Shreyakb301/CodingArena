package com.codingarena.domain.model

import com.codingarena.problem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelBehaviourTest {

    @Test
    fun `outcome classification follows the spec's score table`() {
        assertEquals(
            AnswerOutcome.CORRECT_NO_HINTS,
            AnswerOutcome.classify(correct = true, partialScore = 1.0, hintsUsed = 0, attemptsCount = 1),
        )
        assertEquals(
            AnswerOutcome.CORRECT_WITH_HINT,
            AnswerOutcome.classify(correct = true, partialScore = 1.0, hintsUsed = 1, attemptsCount = 1),
        )
        assertEquals(
            AnswerOutcome.CORRECT_AFTER_RETRIES,
            AnswerOutcome.classify(correct = true, partialScore = 1.0, hintsUsed = 0, attemptsCount = 2),
        )
        assertEquals(
            AnswerOutcome.INCORRECT,
            AnswerOutcome.classify(correct = false, partialScore = 0.0, hintsUsed = 0, attemptsCount = 1),
        )
    }

    @Test
    fun `two hints score the same as a retry, not as a single hint`() {
        assertEquals(
            AnswerOutcome.CORRECT_AFTER_RETRIES,
            AnswerOutcome.classify(correct = true, partialScore = 1.0, hintsUsed = 2, attemptsCount = 1),
        )
    }

    @Test
    fun `a half-correct ordering earns partial credit`() {
        assertEquals(
            AnswerOutcome.PARTIALLY_CORRECT,
            AnswerOutcome.classify(correct = false, partialScore = 0.5, hintsUsed = 0, attemptsCount = 1),
        )
        assertEquals(
            AnswerOutcome.INCORRECT,
            AnswerOutcome.classify(correct = false, partialScore = 0.4, hintsUsed = 0, attemptsCount = 1),
        )
    }

    @Test
    fun `actual scores match the values the spec lists`() {
        assertEquals(1.00, AnswerOutcome.CORRECT_NO_HINTS.actualScore)
        assertEquals(0.85, AnswerOutcome.CORRECT_WITH_HINT.actualScore)
        assertEquals(0.70, AnswerOutcome.CORRECT_AFTER_RETRIES.actualScore)
        assertEquals(0.40, AnswerOutcome.PARTIALLY_CORRECT.actualScore)
        assertEquals(0.00, AnswerOutcome.INCORRECT.actualScore)
    }

    @Test
    fun `ordering problems score by position`() {
        val ordering = problem(
            type = ChallengeType.REARRANGE_CODE,
            choices = listOf(
                AnswerChoice("a", "one"),
                AnswerChoice("b", "two"),
                AnswerChoice("c", "three"),
                AnswerChoice("d", "four"),
            ),
            correctIds = listOf("a", "b", "c", "d"),
        )

        assertTrue(ordering.isCorrect(listOf("a", "b", "c", "d")))
        assertFalse(ordering.isCorrect(listOf("a", "b", "d", "c")))
        assertEquals(1.0, ordering.partialScore(listOf("a", "b", "c", "d")))
        assertEquals(0.5, ordering.partialScore(listOf("a", "b", "d", "c")))
        assertEquals(0.0, ordering.partialScore(listOf("d", "c", "b", "a")))
    }

    @Test
    fun `single-answer problems are all or nothing`() {
        val p = problem()

        assertTrue(p.isCorrect(listOf("a")))
        assertFalse(p.isCorrect(listOf("b")))
        assertFalse(p.isCorrect(listOf("a", "b")))
        assertEquals(0.0, p.partialScore(listOf("b")))
    }

    @Test
    fun `a problem rejects an answer id it does not contain`() {
        assertFailsWith<IllegalArgumentException> {
            problem(correctIds = listOf("does-not-exist"))
        }
    }

    @Test
    fun `a problem rejects more than three hints`() {
        assertFailsWith<IllegalArgumentException> {
            problem(hints = listOf("1", "2", "3", "4"))
        }
    }

    @Test
    fun `difficulty bands cover every rating`() {
        assertEquals(Difficulty.INTRODUCTORY, Difficulty.forRating(0))
        assertEquals(Difficulty.EASY, Difficulty.forRating(1000))
        assertEquals(Difficulty.MEDIUM, Difficulty.forRating(1300))
        assertEquals(Difficulty.HARD, Difficulty.forRating(1600))
        assertEquals(Difficulty.EXPERT, Difficulty.forRating(99_999))
    }

    @Test
    fun `topics are listed primary first with no duplicates`() {
        val p = problem(
            topic = CodingTopic.ARRAYS,
            secondary = listOf(CodingTopic.HASH_MAPS, CodingTopic.ARRAYS),
        )

        assertEquals(listOf(CodingTopic.ARRAYS, CodingTopic.HASH_MAPS), p.allTopics)
    }

    @Test
    fun `challenge types map onto the practice-mode ratings`() {
        assertEquals(PracticeMode.DEBUGGING, ChallengeType.FIND_THE_BUG.practiceMode)
        assertEquals(PracticeMode.COMPLEXITY, ChallengeType.TIME_COMPLEXITY.practiceMode)
        assertEquals(PracticeMode.PATTERN_RECOGNITION, ChallengeType.PATTERN_RECOGNITION.practiceMode)
        assertEquals(PracticeMode.PUZZLE, ChallengeType.REARRANGE_CODE.practiceMode)
    }

    @Test
    fun `daily puzzle scoring rewards speed and penalises hints`() {
        fun score(hints: Int = 0, attempts: Int = 1, durationMs: Long = 30_000, outcome: AnswerOutcome = AnswerOutcome.CORRECT_NO_HINTS) =
            DailyPuzzleResult(
                epochDay = 1,
                problemId = "p1",
                attemptId = "a1",
                outcome = outcome,
                solveDurationMs = durationMs,
                hintsUsed = hints,
                attemptsCount = attempts,
                ratingChange = 0,
                completedAt = 0,
            ).score(difficultyRating = 1000, estimatedSeconds = 60)

        val clean = score()
        assertTrue(score(durationMs = 20_000) >= clean, "faster should not score lower")
        assertTrue(score(hints = 2) < clean, "hints should cost")
        assertTrue(score(attempts = 3) < clean, "retries should cost")
        assertEquals(0, score(outcome = AnswerOutcome.INCORRECT))
    }

    @Test
    fun `daily puzzle scores stay within zero and one hundred`() {
        val perfect = DailyPuzzleResult(
            epochDay = 1, problemId = "p", attemptId = "a",
            outcome = AnswerOutcome.CORRECT_NO_HINTS,
            solveDurationMs = 1, hintsUsed = 0, attemptsCount = 1,
            ratingChange = 0, completedAt = 0,
        ).score(difficultyRating = 3000, estimatedSeconds = 300)

        assertTrue(perfect in 0..100, "score was $perfect")
    }

    @Test
    fun `provisional ratings need five attempts to settle`() {
        assertTrue(UserTopicRating(CodingTopic.ARRAYS, 1200, attempts = 4).isProvisional)
        assertFalse(UserTopicRating(CodingTopic.ARRAYS, 1200, attempts = 5).isProvisional)
    }

    @Test
    fun `review stages step up and clamp at the ends`() {
        assertEquals(ReviewStage.SHAKY, ReviewStage.RELEARN.next())
        assertEquals(ReviewStage.MASTERED, ReviewStage.MASTERED.next())
        assertEquals(ReviewStage.RELEARN, ReviewStage.RELEARN.previous())
        assertEquals(listOf(1, 3, 7, 14, 30), ReviewStage.entries.map { it.intervalDays })
    }
}
