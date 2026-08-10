package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * Spaced repetition stages (spec 5.11). Each stage carries the interval used
 * when a review lands on it, so the schedule is data rather than a switch
 * statement scattered across the codebase.
 */
@Serializable
enum class ReviewStage(val intervalDays: Int, val displayName: String) {
    RELEARN(1, "Tomorrow"),
    SHAKY(3, "In three days"),
    SLOW(7, "In a week"),
    CONFIDENT(14, "In two weeks"),
    MASTERED(30, "In a month"),
    ;

    fun next(): ReviewStage = entries.getOrElse(ordinal + 1) { MASTERED }

    fun previous(): ReviewStage = entries.getOrElse(ordinal - 1) { RELEARN }
}

/** A problem queued to come back around, and why. */
@Serializable
data class ScheduledReview(
    val problemId: String,
    val userId: String,
    val stage: ReviewStage,
    val dueAt: Long,
    val lastReviewedAt: Long,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val topic: CodingTopic,
) {
    fun isDue(now: Long): Boolean = dueAt <= now

    val isMastered: Boolean get() = stage == ReviewStage.MASTERED && lapses == 0
}
