package com.codingarena.domain.engine

import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.InterviewReadiness
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PlayerStats
import com.codingarena.domain.model.ReadinessBand
import com.codingarena.domain.model.TargetJobLevel

/**
 * Estimates interview readiness (spec 5.2, 5.14).
 *
 * Deliberately hard to game. Readiness is the *floor* of a user's core topics
 * rather than their average, because an interview asks whichever question it
 * likes: a 1600 Arrays rating does not compensate for a 700 Graphs rating.
 * Breadth and accuracy then adjust that floor.
 */
class ReadinessEngine(private val config: ReadinessConfig = ReadinessConfig()) {

    fun estimate(
        ratings: PlayerRatings,
        stats: PlayerStats,
        targetLevel: TargetJobLevel,
    ): InterviewReadiness {
        val target = config.targetRatingFor(targetLevel)
        val core = config.coreTopics

        val practised = core.mapNotNull { topic ->
            ratings.topics[topic]?.takeIf { it.attempts > 0 }
        }

        if (practised.size < config.minimumTopicsForEstimate) {
            return InterviewReadiness(
                score = 0,
                band = ReadinessBand.STARTING_OUT,
                rationale = "Practise at least ${config.minimumTopicsForEstimate} core topics for " +
                    "a readiness estimate. You have covered ${practised.size} so far.",
                limitingTopics = core.filter { topic ->
                    ratings.topics[topic]?.attempts.let { it == null || it == 0 }
                }.take(3),
            )
        }

        // The weakest practised core topic sets the ceiling.
        val floorTopic = practised.minBy { it.rating }
        val ratingComponent = (floorTopic.rating.toDouble() / target).coerceIn(0.0, 1.0)

        // Breadth: how much of the core curriculum has been touched at all.
        val breadth = practised.size.toDouble() / core.size

        val accuracy = stats.accuracy.coerceIn(0.0, 1.0)

        val weighted = ratingComponent * config.ratingWeight +
            breadth * config.breadthWeight +
            accuracy * config.accuracyWeight

        // Breadth is a ceiling, not just a term. Without this, someone who has
        // practised three core topics to a high rating and never touched the
        // other nine would read as nearly interview ready - which is exactly
        // the false confidence this estimate exists to avoid.
        val breadthCeiling = config.breadthCeilingBase + breadth * config.breadthCeilingRange

        val score = minOf(weighted * 100, breadthCeiling).toInt().coerceIn(0, 100)

        val limiting = practised
            .sortedBy { it.rating }
            .take(3)
            .map { it.topic }

        return InterviewReadiness(
            score = score,
            band = ReadinessBand.forScore(score),
            rationale = rationale(floorTopic.topic, floorTopic.rating, target, breadth, core.size, practised.size),
            limitingTopics = limiting,
        )
    }

    private fun rationale(
        weakest: CodingTopic,
        weakestRating: Int,
        target: Int,
        breadth: Double,
        coreCount: Int,
        practisedCount: Int,
    ): String = when {
        breadth < config.breadthThreshold ->
            "You have practised $practisedCount of $coreCount core topics. Breadth is the " +
                "limiting factor right now, not depth."

        weakestRating < target - config.significantShortfall ->
            "${weakest.displayName} is your weakest core topic at $weakestRating, against a " +
                "target of $target for this level. Interviews sample topics at random, so this " +
                "is the one that would cost you."

        else ->
            "Your core topics are close to the $target target for this level. Keep the weakest " +
                "(${weakest.displayName}, $weakestRating) moving and stay consistent."
    }
}

data class ReadinessConfig(
    val ratingWeight: Double = 0.6,
    val breadthWeight: Double = 0.25,
    val accuracyWeight: Double = 0.15,
    val minimumTopicsForEstimate: Int = 3,
    val breadthThreshold: Double = 0.5,
    /** Score ceiling when no core topic has been practised at all. */
    val breadthCeilingBase: Double = 40.0,
    /** How much of the ceiling full coverage of the core topics unlocks. */
    val breadthCeilingRange: Double = 60.0,
    val significantShortfall: Int = 150,
    /** Topics an entry-level interview realistically samples from. */
    val coreTopics: List<CodingTopic> = listOf(
        CodingTopic.ARRAYS,
        CodingTopic.STRINGS,
        CodingTopic.HASH_MAPS,
        CodingTopic.TWO_POINTERS,
        CodingTopic.SLIDING_WINDOW,
        CodingTopic.BINARY_SEARCH,
        CodingTopic.STACKS,
        CodingTopic.TREES,
        CodingTopic.GRAPHS,
        CodingTopic.RECURSION,
        CodingTopic.SORTING,
        CodingTopic.COMPLEXITY,
    ),
) {
    fun targetRatingFor(level: TargetJobLevel): Int = when (level) {
        TargetJobLevel.INTERNSHIP -> 1150
        TargetJobLevel.NEW_GRAD -> 1300
        TargetJobLevel.JUNIOR -> 1350
        TargetJobLevel.MID_LEVEL -> 1500
        TargetJobLevel.SENIOR -> 1650
    }
}
