package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * How well a challenge went, mapped to the Elo "actual score" values in
 * spec 5.6. Anything short of a clean first-try solve scores below 1.0, which
 * is what stops a user from grinding easy problems with hints to farm rating.
 */
@Serializable
enum class AnswerOutcome(val actualScore: Double, val displayName: String) {
    CORRECT_NO_HINTS(1.00, "Solved cleanly"),
    CORRECT_WITH_HINT(0.85, "Solved with a hint"),
    CORRECT_AFTER_RETRIES(0.70, "Solved after retries"),
    PARTIALLY_CORRECT(0.40, "Partially correct"),
    INCORRECT(0.00, "Incorrect"),
    ;

    val wasCorrect: Boolean
        get() = this != INCORRECT && this != PARTIALLY_CORRECT

    companion object {
        /**
         * Classifies a finished attempt. Order matters: a solve that needed
         * both hints and retries is scored by the harsher of the two.
         */
        fun classify(
            correct: Boolean,
            partialScore: Double,
            hintsUsed: Int,
            attemptsCount: Int,
        ): AnswerOutcome = when {
            !correct && partialScore >= PARTIAL_CREDIT_THRESHOLD -> PARTIALLY_CORRECT
            !correct -> INCORRECT
            attemptsCount > 1 -> CORRECT_AFTER_RETRIES
            hintsUsed > 1 -> CORRECT_AFTER_RETRIES
            hintsUsed == 1 -> CORRECT_WITH_HINT
            else -> CORRECT_NO_HINTS
        }

        /** Ordering problems need at least half the sequence right to earn credit. */
        const val PARTIAL_CREDIT_THRESHOLD = 0.5
    }
}

/**
 * One completed pass at a problem.
 *
 * Written locally first and pushed to the backend later; [synced] is what the
 * offline-first sync pass filters on.
 */
@Serializable
data class PracticeAttempt(
    val id: String,
    val userId: String,
    val problemId: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val selectedAnswerIds: List<String> = emptyList(),
    val outcome: AnswerOutcome = AnswerOutcome.INCORRECT,
    val attemptsCount: Int = 1,
    val hintsUsed: Int = 0,
    val ratingBefore: Int = 0,
    val ratingAfter: Int = 0,
    val source: AttemptSource = AttemptSource.PRACTICE,
    val synced: Boolean = false,
) {
    val wasCorrect: Boolean get() = outcome.wasCorrect

    val ratingChange: Int get() = ratingAfter - ratingBefore

    val solveDurationMs: Long? get() = completedAt?.let { it - startedAt }
}

/** Where an attempt came from; Code Rush answers are rated more gently. */
@Serializable
enum class AttemptSource {
    PRACTICE,
    DAILY_PUZZLE,
    CODE_RUSH,
    SCHEDULED_REVIEW,
    LEARNING_PATH,
    PLACEMENT_TEST,
}
