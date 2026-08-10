package com.codingarena.domain.engine

import com.codingarena.core.common.MILLIS_PER_DAY
import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.ReviewStage
import com.codingarena.domain.model.ScheduledReview

/**
 * Decides when a problem should come back (spec 5.11).
 *
 * A stage ladder rather than SM-2: the intervals are the ones the spec names
 * (1 / 3 / 7 / 14 / 30 days) and movement between them is driven by how the
 * attempt went, including whether it was *slow*, which is the signal that
 * separates "knows it" from "worked it out".
 */
class SpacedRepetitionEngine(
    private val config: SpacedRepetitionConfig = SpacedRepetitionConfig(),
) {

    /**
     * Schedules [problem] after an attempt.
     *
     * @param existing the current schedule entry, or null the first time.
     * @param solveDurationMs null when the user never answered.
     */
    fun schedule(
        problem: CodingProblem,
        outcome: AnswerOutcome,
        hintsUsed: Int,
        solveDurationMs: Long?,
        now: Long,
        existing: ScheduledReview? = null,
        userId: String,
    ): ScheduledReview {
        val currentStage = existing?.stage ?: ReviewStage.RELEARN
        val wasSlow = solveDurationMs != null &&
            solveDurationMs > problem.estimatedSeconds * 1000L * config.slowMultiplier

        val nextStage = when {
            // Missed it: back to the bottom of the ladder regardless of history.
            !outcome.wasCorrect -> ReviewStage.RELEARN

            // Needed help: it is recall-shaky, so hold it close.
            hintsUsed > 0 -> maxOf(ReviewStage.SHAKY, currentStage.previous())

            // Got there, but slowly: cap the interval at a week.
            wasSlow -> minOf(currentStage.next(), ReviewStage.SLOW)

            // Clean and quick: promote.
            else -> currentStage.next()
        }

        val lapsed = !outcome.wasCorrect
        return ScheduledReview(
            problemId = problem.id,
            userId = userId,
            stage = nextStage,
            dueAt = now + nextStage.intervalDays * MILLIS_PER_DAY,
            lastReviewedAt = now,
            repetitions = (existing?.repetitions ?: 0) + 1,
            lapses = (existing?.lapses ?: 0) + if (lapsed) 1 else 0,
            topic = problem.primaryTopic,
        )
    }

    /**
     * Reviews to serve now, weakest first.
     *
     * Overdue items sort ahead of merely due ones, and within the same
     * overdue-ness the ones the user keeps getting wrong come first - the spec
     * asks for weak patterns rather than random repetition.
     */
    fun dueReviews(
        schedule: List<ScheduledReview>,
        now: Long,
        limit: Int = Int.MAX_VALUE,
    ): List<ScheduledReview> = schedule
        .filter { it.isDue(now) }
        .sortedWith(
            compareByDescending<ScheduledReview> { it.lapses }
                .thenBy { it.dueAt }
                .thenBy { it.problemId }
        )
        .take(limit)

    /** Count of reviews falling due within the next [days] days. */
    fun upcomingCount(schedule: List<ScheduledReview>, now: Long, days: Int = 7): Int {
        val horizon = now + days * MILLIS_PER_DAY
        return schedule.count { it.dueAt in (now + 1)..horizon }
    }
}

data class SpacedRepetitionConfig(
    /** Multiple of a problem's estimated time past which a solve counts as slow. */
    val slowMultiplier: Double = 1.5,
)
