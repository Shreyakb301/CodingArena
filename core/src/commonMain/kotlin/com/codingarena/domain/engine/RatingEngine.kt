package com.codingarena.domain.engine

import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.ModeRating
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.RatingUpdate
import com.codingarena.domain.model.UserTopicRating
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Tunables for the Elo variant described in spec 5.6.
 *
 * @param provisionalK larger K while a rating is still provisional, so a new
 *   user converges on a believable rating within a handful of problems instead
 *   of crawling there.
 * @param maxChangePerAttempt hard cap that stops a single lucky (or unlucky)
 *   answer from moving a rating dramatically.
 * @param secondaryTopicWeight fraction of the primary topic's delta applied to
 *   secondary topics (spec 5.7).
 * @param easyProblemDamping applied when a correct answer comes from a problem
 *   far below the user's rating. This is the anti-farming rule: clearing easy
 *   problems is worth very little, while *failing* one still costs full price.
 */
data class RatingConfig(
    val kFactor: Double = 24.0,
    val provisionalK: Double = 48.0,
    val maxChangePerAttempt: Int = 32,
    val secondaryTopicWeight: Double = 0.4,
    val codeRushWeight: Double = 0.5,
    val easyProblemDamping: Double = 0.25,
    val easyProblemThreshold: Int = 300,
    val floorRating: Int = 400,
    val ceilingRating: Int = 3000,
)

/**
 * Turns one finished attempt into rating movements.
 *
 * Pure and synchronous: no clock, no storage, no coroutines. Everything about
 * the outcome is passed in, which keeps the maths straightforward to test.
 */
class RatingEngine(private val config: RatingConfig = RatingConfig()) {

    /** Probability a user of [userRating] answers a [problemRating] problem correctly. */
    fun expectedScore(userRating: Int, problemRating: Int): Double =
        1.0 / (1.0 + 10.0.pow((problemRating - userRating) / 400.0))

    /**
     * Applies [outcome] on [problem] to [ratings] and returns the new state
     * alongside a description of what moved.
     */
    fun rate(
        ratings: PlayerRatings,
        problem: CodingProblem,
        outcome: AnswerOutcome,
        source: AttemptSource = AttemptSource.PRACTICE,
        now: Long = 0L,
    ): Pair<PlayerRatings, RatingUpdate> {
        val sourceWeight = if (source == AttemptSource.CODE_RUSH) config.codeRushWeight else 1.0

        val overallAttempts = ratings.topics.values.sumOf { it.attempts }
        val rawOverallDelta = delta(
            userRating = ratings.overall,
            problemRating = problem.difficultyRating,
            outcome = outcome,
            attempts = overallAttempts,
        ) * sourceWeight
        val cappedOverall = cap(rawOverallDelta)
        val newOverall = clamp(ratings.overall + cappedOverall)

        val topicChanges = mutableMapOf<CodingTopic, Int>()
        val newTopics = ratings.topics.toMutableMap()

        problem.allTopics.forEach { topic ->
            val existing = newTopics[topic]
            val currentRating = existing?.rating ?: PlayerRatings.DEFAULT_RATING
            val topicWeight =
                if (topic == problem.primaryTopic) 1.0 else config.secondaryTopicWeight
            val rawDelta = delta(
                userRating = currentRating,
                problemRating = problem.difficultyRating,
                outcome = outcome,
                attempts = existing?.attempts ?: 0,
            ) * topicWeight * sourceWeight
            val change = cap(rawDelta)

            newTopics[topic] = UserTopicRating(
                topic = topic,
                rating = clamp(currentRating + change),
                attempts = (existing?.attempts ?: 0) + 1,
                correctAnswers = (existing?.correctAnswers ?: 0) + if (outcome.wasCorrect) 1 else 0,
                lastPracticedAt = now,
            )
            topicChanges[topic] = change
        }

        val mode = problem.practiceMode
        val existingMode = ratings.modes[mode]
        val modeRating = existingMode?.rating ?: PlayerRatings.DEFAULT_RATING
        val modeChange = cap(
            delta(
                userRating = modeRating,
                problemRating = problem.difficultyRating,
                outcome = outcome,
                attempts = existingMode?.attempts ?: 0,
            ) * sourceWeight
        )
        val newModes = ratings.modes.toMutableMap().apply {
            put(
                mode,
                ModeRating(
                    mode = mode,
                    rating = clamp(modeRating + modeChange),
                    attempts = (existingMode?.attempts ?: 0) + 1,
                    correctAnswers = (existingMode?.correctAnswers ?: 0) +
                        if (outcome.wasCorrect) 1 else 0,
                ),
            )
        }

        val updated = ratings.copy(
            overall = newOverall,
            topics = newTopics,
            modes = newModes,
            updatedAt = now,
        )
        val update = RatingUpdate(
            overallBefore = ratings.overall,
            overallAfter = newOverall,
            topicChanges = topicChanges,
            modeChange = mode to modeChange,
            wasCapped = abs(rawOverallDelta) > config.maxChangePerAttempt,
        )
        return updated to update
    }

    /**
     * Raw Elo delta before capping.
     *
     * Two departures from textbook Elo, both deliberate:
     *  - K is inflated while the rating is provisional, for faster convergence.
     *  - Gains from problems well below the user's level are damped, so that
     *    replaying introductory problems cannot be used to farm rating. Losses
     *    are never damped.
     */
    private fun delta(
        userRating: Int,
        problemRating: Int,
        outcome: AnswerOutcome,
        attempts: Int,
    ): Double {
        val expected = expectedScore(userRating, problemRating)
        val k = if (attempts < UserTopicRating.PROVISIONAL_ATTEMPTS) config.provisionalK else config.kFactor
        val raw = k * (outcome.actualScore - expected)
        val isFarBelow = userRating - problemRating > config.easyProblemThreshold
        return if (raw > 0 && isFarBelow) raw * config.easyProblemDamping else raw
    }

    private fun cap(raw: Double): Int =
        raw.roundToInt().coerceIn(-config.maxChangePerAttempt, config.maxChangePerAttempt)

    private fun clamp(rating: Int): Int = rating.coerceIn(config.floorRating, config.ceilingRating)

    /**
     * Seeds ratings from the optional placement test. Topics the user said
     * they already know start slightly above the estimate, everything else
     * slightly below - a starting guess the engine corrects within a few
     * problems rather than a claim of measurement.
     */
    fun seedRatings(
        estimatedRating: Int,
        knownTopics: Set<CodingTopic>,
        now: Long = 0L,
    ): PlayerRatings {
        val base = estimatedRating.coerceIn(config.floorRating, config.ceilingRating)
        val topics = CodingTopic.entries.associateWith { topic ->
            val seed = if (topic in knownTopics) base + KNOWN_TOPIC_BONUS else base - UNKNOWN_TOPIC_PENALTY
            UserTopicRating(topic = topic, rating = clamp(seed), attempts = 0, correctAnswers = 0)
        }
        return PlayerRatings(overall = base, topics = topics, modes = emptyMap(), updatedAt = now)
    }

    private companion object {
        const val KNOWN_TOPIC_BONUS = 75
        const val UNKNOWN_TOPIC_PENALTY = 75
    }
}
