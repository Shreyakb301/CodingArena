package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * Where an [AdaptivePracticeState] currently is.
 *
 * Enforced as an invariant everywhere the state transitions (see
 * [PracticeWorkoutEngine][com.codingarena.domain.engine.PracticeWorkoutEngine]):
 * READY has no current round; ROUND_ACTIVE's round exists and is incomplete;
 * ROUND_RESULTS's round exists and is complete. This is what lets resume
 * logic branch on phase alone rather than inferring intent from nullability.
 */
@Serializable
enum class AdaptivePracticePhase {
    READY,
    ROUND_ACTIVE,
    ROUND_RESULTS,
}

/**
 * One tracked weakness: a specific misunderstanding, at a specific
 * difficulty, that the user has gotten wrong at least once this session.
 *
 * Keyed by (group, conceptKey, missedAtDifficulty) - not just (group,
 * conceptKey) - because a mistake made at Beginner and a later mistake on
 * the same underlying idea at Advanced need correcting at different levels
 * and are tracked as separate entries.
 */
@Serializable
data class MissedConcept(
    val group: PatternGroup,
    val conceptKey: String,
    val kind: WorkoutStepKind,
    val missedAtDifficulty: PracticeDifficulty,
    val missCount: Int,
    val successfulReviews: Int = 0,
    /** Cleared only after [successfulReviews] reaches 2 - see [PracticeWorkoutEngine.finishRound]. */
    val pendingReview: Boolean = true,
    /** Problem slugs already used as "a different variation" for this concept, at this difficulty. */
    val reviewedStepSlugs: Set<String> = emptySet(),
) {
    val key: Triple<PatternGroup, String, PracticeDifficulty> get() = Triple(group, conceptKey, missedAtDifficulty)
}

/**
 * Cross-round state for Recommended, Topic-Focused, and Mixed adaptive
 * practice.
 *
 * Pure and fully [Serializable] end to end (every field type already is),
 * so it round-trips through a single persisted JSON blob with no snapshot
 * or DTO conversion needed.
 */
@Serializable
data class AdaptivePracticeState(
    val id: String,
    val userId: String,
    val mode: PracticeSessionKind,
    /** Set for TOPIC_FOCUSED, null for RECOMMENDED and MIXED. */
    val topic: PatternGroup?,
    val difficulty: PracticeDifficulty,
    val phase: AdaptivePracticePhase = AdaptivePracticePhase.READY,
    val completedRounds: Int = 0,
    val consecutiveBelowEight: Int = 0,
    val missedConcepts: List<MissedConcept> = emptyList(),
    val correctlyAnsweredStepIds: Set<String> = emptySet(),
    /** Step id to the round number it was last shown in - drives least-recently-shown repeat ordering. */
    val stepLastShownAtRound: Map<String, Int> = emptyMap(),
    val currentRound: AdaptivePracticeRound? = null,
    val startedAt: Long,
) {
    val isPhaseValid: Boolean
        get() = when (phase) {
            AdaptivePracticePhase.READY -> currentRound == null
            AdaptivePracticePhase.ROUND_ACTIVE -> currentRound != null && !currentRound.isOver
            AdaptivePracticePhase.ROUND_RESULTS -> currentRound != null && currentRound.isOver
        }
}
