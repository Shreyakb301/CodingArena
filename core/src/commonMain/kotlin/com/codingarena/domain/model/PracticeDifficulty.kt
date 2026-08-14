package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * How complex a practice round's questions are, adapted round by round based
 * on score. Distinct from [PracticeExperienceLevel], which is a longer-lived
 * signal derived from Roadmap mastery and decides which *topic* a
 * Recommended round draws from, not how hard its questions are.
 */
@Serializable
enum class PracticeDifficulty {
    BEGINNER,
    DEVELOPING,
    INTERMEDIATE,
    ADVANCED,
    ;

    /** No-op at [ADVANCED] - the state machine never pushes past the top of the ladder. */
    fun next(): PracticeDifficulty = entries.getOrElse(ordinal + 1) { this }

    /** No-op at [BEGINNER] - the state machine never drops below the bottom of the ladder. */
    fun previous(): PracticeDifficulty = entries.getOrElse(ordinal - 1) { this }
}

/**
 * This tier first, then the nearest easier tier, then the nearest harder one,
 * alternating outward - always returns all four. Used only to resolve
 * within-topic content exhaustion (a specific kind/tier combination running
 * out mid-round); it is never used to select a topic that lacks a whole tier
 * of content - see [PracticeWorkoutEngine.hasCompleteQuestionBank] for that.
 */
fun PracticeDifficulty.searchOrder(): List<PracticeDifficulty> =
    PracticeDifficulty.entries.sortedBy { candidate ->
        val distance = kotlin.math.abs(candidate.ordinal - ordinal)
        // Ties broken toward easier: the same distance in both directions
        // ranks the easier candidate first, biasing gracefully toward more
        // guided content when a choice has to be made.
        distance * 2 - if (candidate.ordinal < ordinal) 1 else 0
    }
