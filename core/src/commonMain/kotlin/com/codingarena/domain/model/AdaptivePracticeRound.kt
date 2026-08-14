package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * One occurrence of a [WorkoutStep] within a round.
 *
 * Deliberately wrapped rather than referencing [WorkoutStep.id] directly -
 * the rare least-recently-shown repeat fallback can place the same step
 * twice within one round when a topic's content pool is thin, and answers
 * need a way to unambiguously identify *which* occurrence they belong to.
 */
@Serializable
data class RoundQuestion(
    val instanceId: String,
    /** Already choice-shuffled for this specific round - see [PracticeWorkoutEngine.startRound]. */
    val step: WorkoutStep,
    val isReview: Boolean,
)

@Serializable
data class AdaptivePracticeAnswer(
    val instanceId: String,
    val chosenIndex: Int,
    val wasCorrect: Boolean,
    val elapsedMs: Long,
)

/**
 * One ten-question adaptive practice round.
 *
 * [targetDifficulty] is frozen at round start specifically so the results
 * screen can report "Completed round: X" even though
 * [AdaptivePracticeState.difficulty] has already moved on to "Next round: Y"
 * by the time results are shown - review questions inside this round may
 * individually be pinned to an *earlier* difficulty than this one.
 */
@Serializable
data class AdaptivePracticeRound(
    val targetDifficulty: PracticeDifficulty,
    val questions: List<RoundQuestion>,
    val answers: List<AdaptivePracticeAnswer> = emptyList(),
    val startedAt: Long,
    val completedAt: Long? = null,
) {
    val currentIndex: Int get() = answers.size

    val currentQuestion: RoundQuestion? get() = questions.getOrNull(currentIndex)

    /** Every question has an answer, but [finishRound][com.codingarena.domain.engine.PracticeWorkoutEngine.finishRound] hasn't run yet. */
    val isFullyAnswered: Boolean get() = currentIndex >= questions.size

    /**
     * True only once [finishRound][com.codingarena.domain.engine.PracticeWorkoutEngine.finishRound]
     * has actually run - deliberately not inferred from [isFullyAnswered],
     * since finishRound's own guard ("only once every question is answered")
     * needs a fully-answered-but-not-yet-finished state to be a distinct,
     * valid ROUND_ACTIVE moment, not one already indistinguishable from over.
     */
    val isOver: Boolean get() = completedAt != null

    val score: Int get() = answers.count { it.wasCorrect }
}
