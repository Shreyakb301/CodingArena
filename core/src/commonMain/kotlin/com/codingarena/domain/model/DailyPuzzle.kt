package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * The one curated challenge for a given day.
 *
 * Identified by epoch day rather than a timestamp so that the puzzle is stable
 * for a user's local day and can be pre-downloaded for offline use.
 */
@Serializable
data class DailyPuzzle(
    val epochDay: Long,
    val problemId: String,
    val downloadedAt: Long,
)

/** A user's run at the Daily Puzzle. */
@Serializable
data class DailyPuzzleResult(
    val epochDay: Long,
    val problemId: String,
    val attemptId: String,
    val outcome: AnswerOutcome,
    val solveDurationMs: Long,
    val hintsUsed: Int,
    val attemptsCount: Int,
    val ratingChange: Int,
    val completedAt: Long,
) {
    /**
     * A 0-100 score combining correctness, speed, restraint with hints, and the
     * problem's difficulty (spec 5.3). Correctness dominates - speed can only
     * ever add a modest bonus on top of a correct answer.
     */
    fun score(difficultyRating: Int, estimatedSeconds: Int): Int {
        val base = outcome.actualScore * 70
        val expectedMs = estimatedSeconds * 1000L
        val speedBonus = when {
            !outcome.wasCorrect -> 0.0
            solveDurationMs <= expectedMs / 2 -> 15.0
            solveDurationMs <= expectedMs -> 10.0
            solveDurationMs <= expectedMs * 2 -> 5.0
            else -> 0.0
        }
        val hintPenalty = hintsUsed * 3.0
        val retryPenalty = (attemptsCount - 1).coerceAtLeast(0) * 4.0
        val difficultyBonus = if (outcome.wasCorrect) {
            (difficultyRating - 1000).coerceIn(0, 600) / 40.0
        } else {
            0.0
        }
        return (base + speedBonus + difficultyBonus - hintPenalty - retryPenalty)
            .coerceIn(0.0, 100.0)
            .toInt()
    }
}
