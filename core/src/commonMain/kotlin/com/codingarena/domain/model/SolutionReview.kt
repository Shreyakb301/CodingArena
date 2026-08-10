package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * Chess.com-style move quality labels.
 *
 * The spec is explicit that these must always come with a real explanation, so
 * a [SolutionReview] can never be built from a label alone - every label is
 * paired with [SolutionReview.whyItMatters].
 */
@Serializable
enum class ReviewLabel(val displayName: String, val isPositive: Boolean) {
    BRILLIANT("Brilliant", true),
    BEST_MOVE("Best Move", true),
    GOOD("Good", true),
    INACCURACY("Inaccuracy", false),
    MISTAKE("Mistake", false),
    BLUNDER("Blunder", false),
}

/**
 * The flagship post-challenge screen (spec 5.5).
 *
 * Every field is filled from curated problem content and the user's actual
 * answer - there is no language model in the loop, which is what keeps the
 * running cost at zero.
 */
@Serializable
data class SolutionReview(
    val problemId: String,
    val label: ReviewLabel,
    val headline: String,
    /** What the user got right - present even when the answer was wrong. */
    val goodMove: String?,
    /** What went wrong; null on a clean solve. */
    val mistake: String?,
    val whyItMatters: String,
    val bestMove: String,
    val explanation: String,
    val pattern: String?,
    val timeComplexity: String?,
    val spaceComplexity: String?,
    val commonMistakes: List<String> = emptyList(),
    val recommendedPractice: List<RecommendedProblem> = emptyList(),
    val ratingUpdate: RatingUpdate? = null,
    val nextReviewAt: Long? = null,
)

/** A follow-up problem, with the reason it was chosen. */
@Serializable
data class RecommendedProblem(
    val problemId: String,
    val title: String,
    val reason: String,
    val topic: CodingTopic,
    val difficultyRating: Int,
)
