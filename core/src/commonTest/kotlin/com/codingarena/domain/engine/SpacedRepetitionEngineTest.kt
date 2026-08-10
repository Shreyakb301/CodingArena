package com.codingarena.domain.engine

import com.codingarena.core.common.MILLIS_PER_DAY
import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.ReviewStage
import com.codingarena.domain.model.ScheduledReview
import com.codingarena.problem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpacedRepetitionEngineTest {

    private val engine = SpacedRepetitionEngine()
    private val userId = "user-1"
    private val now = 1_000_000L

    @Test
    fun `an incorrect answer comes back tomorrow`() {
        val scheduled = engine.schedule(
            problem = problem(),
            outcome = AnswerOutcome.INCORRECT,
            hintsUsed = 0,
            solveDurationMs = 20_000,
            now = now,
            userId = userId,
        )

        assertEquals(ReviewStage.RELEARN, scheduled.stage)
        assertEquals(now + MILLIS_PER_DAY, scheduled.dueAt)
        assertEquals(1, scheduled.lapses)
    }

    @Test
    fun `a correct answer with a hint stays close`() {
        val existing = review(stage = ReviewStage.CONFIDENT)

        val scheduled = engine.schedule(
            problem = problem(),
            outcome = AnswerOutcome.CORRECT_WITH_HINT,
            hintsUsed = 1,
            solveDurationMs = 20_000,
            now = now,
            existing = existing,
            userId = userId,
        )

        assertEquals(ReviewStage.SLOW, scheduled.stage)
        assertEquals(now + 7 * MILLIS_PER_DAY, scheduled.dueAt)
    }

    @Test
    fun `a correct but slow answer is capped at a week`() {
        val existing = review(stage = ReviewStage.CONFIDENT)

        val scheduled = engine.schedule(
            problem = problem(estimatedSeconds = 30),
            outcome = AnswerOutcome.CORRECT_NO_HINTS,
            hintsUsed = 0,
            solveDurationMs = 120_000, // four times the estimate
            now = now,
            existing = existing,
            userId = userId,
        )

        assertEquals(ReviewStage.SLOW, scheduled.stage)
    }

    @Test
    fun `clean confident answers climb the ladder to thirty days`() {
        var scheduled: ScheduledReview? = null
        val stages = mutableListOf<ReviewStage>()

        repeat(6) {
            scheduled = engine.schedule(
                problem = problem(estimatedSeconds = 60),
                outcome = AnswerOutcome.CORRECT_NO_HINTS,
                hintsUsed = 0,
                solveDurationMs = 20_000,
                now = now,
                existing = scheduled,
                userId = userId,
            )
            stages += scheduled!!.stage
        }

        assertEquals(
            listOf(
                ReviewStage.SHAKY,
                ReviewStage.SLOW,
                ReviewStage.CONFIDENT,
                ReviewStage.MASTERED,
                ReviewStage.MASTERED,
                ReviewStage.MASTERED,
            ),
            stages,
        )
        assertEquals(now + 30 * MILLIS_PER_DAY, scheduled!!.dueAt)
    }

    @Test
    fun `a lapse resets a mastered problem to the bottom`() {
        val mastered = review(stage = ReviewStage.MASTERED, repetitions = 5)

        val scheduled = engine.schedule(
            problem = problem(),
            outcome = AnswerOutcome.INCORRECT,
            hintsUsed = 0,
            solveDurationMs = 90_000,
            now = now,
            existing = mastered,
            userId = userId,
        )

        assertEquals(ReviewStage.RELEARN, scheduled.stage)
        assertEquals(6, scheduled.repetitions)
    }

    @Test
    fun `due reviews put repeatedly failed problems first`() {
        val schedule = listOf(
            review(problemId = "steady", dueAt = now - 1000, lapses = 0),
            review(problemId = "troublesome", dueAt = now - 500, lapses = 3),
            review(problemId = "future", dueAt = now + MILLIS_PER_DAY),
        )

        val due = engine.dueReviews(schedule, now)

        assertEquals(listOf("troublesome", "steady"), due.map { it.problemId })
    }

    @Test
    fun `upcoming count covers the next week only`() {
        val schedule = listOf(
            review(problemId = "a", dueAt = now + 2 * MILLIS_PER_DAY),
            review(problemId = "b", dueAt = now + 6 * MILLIS_PER_DAY),
            review(problemId = "c", dueAt = now + 20 * MILLIS_PER_DAY),
            review(problemId = "overdue", dueAt = now - MILLIS_PER_DAY),
        )

        assertEquals(2, engine.upcomingCount(schedule, now, days = 7))
    }

    @Test
    fun `a review is only due once its time has come`() {
        val entry = review(dueAt = now + 1)
        assertTrue(!entry.isDue(now))
        assertTrue(entry.isDue(now + 1))
    }

    private fun review(
        problemId: String = "p1",
        stage: ReviewStage = ReviewStage.RELEARN,
        dueAt: Long = now,
        lapses: Int = 0,
        repetitions: Int = 1,
    ) = ScheduledReview(
        problemId = problemId,
        userId = userId,
        stage = stage,
        dueAt = dueAt,
        lastReviewedAt = now - MILLIS_PER_DAY,
        repetitions = repetitions,
        lapses = lapses,
        topic = CodingTopic.ARRAYS,
    )
}
