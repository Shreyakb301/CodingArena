package com.codingarena.domain.engine

import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.RecommendedProblem
import kotlin.math.abs

/**
 * Picks what to practise next.
 *
 * The product rule is "optimise for measurable improvement, not solved count",
 * so selection is driven by the gap between a user's topic rating and a
 * problem's difficulty rather than by novelty. The target is a problem the
 * user has roughly a 60-70% chance of getting right: hard enough to teach,
 * easy enough to finish.
 */
class ProblemRecommender(
    private val config: RecommenderConfig = RecommenderConfig(),
) {

    /**
     * Follow-ups for the Solution Review screen.
     *
     * After a miss the next problems stay on the same topic and drop below the
     * failed problem's rating - the point is to rebuild the idea, not to hand
     * the user the same wall again.
     */
    fun nextAfter(
        justAttempted: CodingProblem,
        outcome: AnswerOutcome,
        userRating: Int,
        candidates: List<CodingProblem>,
        limit: Int = 3,
    ): List<RecommendedProblem> {
        val targetRating = if (outcome.wasCorrect) {
            userRating + config.stepUp
        } else {
            minOf(userRating, justAttempted.difficultyRating) - config.stepDown
        }
        val reason = if (outcome.wasCorrect) {
            "Builds on ${justAttempted.primaryTopic.displayName} at a slightly higher level"
        } else {
            "Rebuilds the ${justAttempted.primaryTopic.displayName} idea you just missed"
        }

        val sameTopic = candidates
            .filter { it.id != justAttempted.id && it.isPublished }
            .filter { justAttempted.primaryTopic in it.allTopics }

        val pool = sameTopic.ifEmpty {
            candidates.filter { it.id != justAttempted.id && it.isPublished }
        }

        return pool
            .sortedWith(compareBy({ abs(it.difficultyRating - targetRating) }, { it.id }))
            .take(limit)
            .map { it.toRecommendation(reason) }
    }

    /**
     * The home screen's "practise this next" list.
     *
     * Weakest practised topics first, then topics never touched, so that a
     * user's blind spots surface instead of being quietly skipped.
     */
    fun recommendForUser(
        ratings: PlayerRatings,
        candidates: List<CodingProblem>,
        excludeProblemIds: Set<String> = emptySet(),
        limit: Int = 5,
    ): List<RecommendedProblem> {
        val available = candidates.filter { it.isPublished && it.id !in excludeProblemIds }
        if (available.isEmpty()) return emptyList()

        val weakTopics = ratings.weakestTopics(limit = config.weakTopicCount)
            .map { it.topic }
            .ifEmpty { available.map { it.primaryTopic }.distinct().take(config.weakTopicCount) }

        return weakTopics.mapNotNull { topic ->
            val target = ratings.topicRating(topic) + config.stepUp
            available
                .filter { it.primaryTopic == topic }
                .minByOrNull { abs(it.difficultyRating - target) }
                ?.toRecommendation(reasonFor(topic, ratings))
        }.take(limit)
    }

    /** Chooses the single problem that best fits a target rating on a topic. */
    fun bestFit(
        topic: CodingTopic,
        targetRating: Int,
        candidates: List<CodingProblem>,
        excludeProblemIds: Set<String> = emptySet(),
    ): CodingProblem? = candidates
        .filter { it.isPublished && it.id !in excludeProblemIds && topic in it.allTopics }
        .minByOrNull { abs(it.difficultyRating - targetRating) }

    private fun reasonFor(topic: CodingTopic, ratings: PlayerRatings): String {
        val rating = ratings.topics[topic]
        return when {
            rating == null || rating.attempts == 0 ->
                "You have not practised ${topic.displayName} yet"

            rating.rating < ratings.overall - config.weaknessGap ->
                "${topic.displayName} is ${ratings.overall - rating.rating} points below your " +
                    "overall rating"

            rating.accuracy < config.lowAccuracy ->
                "Your ${topic.displayName} accuracy is " +
                    "${(rating.accuracy * 100).toInt()}%"

            else -> "Keeps ${topic.displayName} moving"
        }
    }

    private fun CodingProblem.toRecommendation(reason: String) = RecommendedProblem(
        problemId = id,
        title = title,
        reason = reason,
        topic = primaryTopic,
        difficultyRating = difficultyRating,
    )
}

data class RecommenderConfig(
    /** How far above the user's rating a "next" problem should sit. */
    val stepUp: Int = 60,
    /** How far below to drop after a miss. */
    val stepDown: Int = 120,
    val weakTopicCount: Int = 3,
    val weaknessGap: Int = 100,
    val lowAccuracy: Double = 0.6,
)
