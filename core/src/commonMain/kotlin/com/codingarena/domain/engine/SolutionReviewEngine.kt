package com.codingarena.domain.engine

import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.CodingPattern
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.model.RatingUpdate
import com.codingarena.domain.model.RecommendedProblem
import com.codingarena.domain.model.ReviewLabel
import com.codingarena.domain.model.SolutionReview

/**
 * Builds the post-challenge review (spec 5.5).
 *
 * Entirely rule-based over curated content - no model calls, so it works
 * offline and costs nothing to run. The rules exist to make the review
 * *specific*: which distractor was picked, how surprising the result was given
 * the user's rating, and what to do next.
 */
class SolutionReviewEngine(
    private val ratingEngine: RatingEngine = RatingEngine(),
    private val recommender: ProblemRecommender = ProblemRecommender(),
) {

    fun build(
        problem: CodingProblem,
        attempt: PracticeAttempt,
        userRatingBefore: Int,
        ratingUpdate: RatingUpdate? = null,
        pattern: CodingPattern? = null,
        candidates: List<CodingProblem> = emptyList(),
        nextReviewAt: Long? = null,
    ): SolutionReview {
        val expected = ratingEngine.expectedScore(userRatingBefore, problem.difficultyRating)
        val solvedQuickly = attempt.solveDurationMs?.let {
            it <= problem.estimatedSeconds * 1000L
        } ?: false
        val label = labelFor(attempt.outcome, expected, solvedQuickly)
        val selected = attempt.selectedAnswerIds.firstOrNull()?.let { problem.choice(it) }
        val correctChoice = problem.choice(problem.correctAnswerId)

        val goodMove = when {
            attempt.outcome.wasCorrect && attempt.hintsUsed == 0 ->
                "You went straight to the right idea: ${lowerFirst(problem.bestApproach)}"

            attempt.outcome.wasCorrect ->
                "You got there. Once you had the hint you applied it correctly, which means the " +
                    "gap was recall, not reasoning."

            selected?.insight != null -> selected.insight

            attempt.outcome == AnswerOutcome.PARTIALLY_CORRECT ->
                "Part of your ordering was right, so the shape of the algorithm is there."

            else -> null
        }

        val mistake = when {
            attempt.outcome.wasCorrect && attempt.attemptsCount > 1 ->
                "It took ${attempt.attemptsCount} attempts. Worth re-reading why the first one failed."

            attempt.outcome.wasCorrect && attempt.hintsUsed > 0 ->
                "You needed ${hintCount(attempt.hintsUsed)} to get there."

            attempt.outcome.wasCorrect -> null

            selected != null -> "You chose \"${selected.text}\"."

            else -> "No answer was submitted before the timer ran out."
        }

        val whyItMatters = when {
            !attempt.outcome.wasCorrect && selected?.rationale != null -> selected.rationale
            !attempt.outcome.wasCorrect -> problem.explanation
            label == ReviewLabel.BRILLIANT ->
                "This problem sits ${problem.difficultyRating - userRatingBefore} points above your " +
                    "current rating, and you solved it cleanly and fast."
            attempt.hintsUsed > 0 || attempt.attemptsCount > 1 ->
                "In an interview there are no hints, so the goal is to reach this " +
                    "unaided. That is what the follow-up practice below is for."
            else -> problem.explanation
        }

        val bestMove = buildString {
            append(problem.bestApproach)
            if (correctChoice != null && !attempt.outcome.wasCorrect) {
                append(" The correct choice was \"")
                append(correctChoice.text)
                append("\".")
            }
        }

        return SolutionReview(
            problemId = problem.id,
            label = label,
            headline = headlineFor(label, attempt.outcome),
            goodMove = goodMove,
            mistake = mistake,
            whyItMatters = whyItMatters,
            bestMove = bestMove,
            explanation = problem.explanation,
            pattern = pattern?.name ?: problem.patternId,
            timeComplexity = problem.timeComplexity,
            spaceComplexity = problem.spaceComplexity,
            commonMistakes = problem.commonMistakes,
            recommendedPractice = recommendations(problem, attempt, userRatingBefore, candidates),
            ratingUpdate = ratingUpdate,
            nextReviewAt = nextReviewAt,
        )
    }

    /**
     * Maps an outcome onto a label, weighted by how surprising it was.
     *
     * [expected] is the Elo probability that this user solves this problem, so
     * missing something you had a 90% shot at reads as a Blunder while missing
     * a problem far above your rating is only an Inaccuracy.
     */
    fun labelFor(
        outcome: AnswerOutcome,
        expected: Double,
        solvedQuickly: Boolean,
    ): ReviewLabel = when (outcome) {
        AnswerOutcome.CORRECT_NO_HINTS ->
            if (expected <= BRILLIANT_EXPECTED_CEILING && solvedQuickly) ReviewLabel.BRILLIANT
            else ReviewLabel.BEST_MOVE

        AnswerOutcome.CORRECT_WITH_HINT -> ReviewLabel.GOOD

        AnswerOutcome.CORRECT_AFTER_RETRIES ->
            if (expected >= ROUTINE_EXPECTED_FLOOR) ReviewLabel.INACCURACY else ReviewLabel.GOOD

        AnswerOutcome.PARTIALLY_CORRECT ->
            if (expected >= ROUTINE_EXPECTED_FLOOR) ReviewLabel.MISTAKE else ReviewLabel.INACCURACY

        AnswerOutcome.INCORRECT -> when {
            expected >= BLUNDER_EXPECTED_FLOOR -> ReviewLabel.BLUNDER
            expected >= MISTAKE_EXPECTED_FLOOR -> ReviewLabel.MISTAKE
            else -> ReviewLabel.INACCURACY
        }
    }

    private fun headlineFor(label: ReviewLabel, outcome: AnswerOutcome): String = when (label) {
        ReviewLabel.BRILLIANT -> "Brilliant - that was above your level"
        ReviewLabel.BEST_MOVE -> "Best move - clean solve"
        ReviewLabel.GOOD -> if (outcome.wasCorrect) "Good - you got there" else "Good instincts"
        ReviewLabel.INACCURACY -> "Inaccuracy - close, but not the strongest choice"
        ReviewLabel.MISTAKE -> "Mistake - this one was within reach"
        ReviewLabel.BLUNDER -> "Blunder - you know this pattern better than this"
    }

    private fun recommendations(
        problem: CodingProblem,
        attempt: PracticeAttempt,
        userRating: Int,
        candidates: List<CodingProblem>,
    ): List<RecommendedProblem> {
        if (candidates.isEmpty()) return emptyList()
        return recommender.nextAfter(
            justAttempted = problem,
            outcome = attempt.outcome,
            userRating = userRating,
            candidates = candidates,
        )
    }

    private fun hintCount(hints: Int): String =
        if (hints == 1) "one hint" else "$hints hints"

    private fun lowerFirst(text: String): String =
        if (text.isEmpty()) text else text.replaceFirstChar { it.lowercase() }

    private companion object {
        const val BRILLIANT_EXPECTED_CEILING = 0.35
        const val ROUTINE_EXPECTED_FLOOR = 0.60
        const val MISTAKE_EXPECTED_FLOOR = 0.45
        const val BLUNDER_EXPECTED_FLOOR = 0.75
    }
}
