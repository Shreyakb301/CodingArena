package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/** A rating on one topic, plus the counters the UI needs to explain it. */
@Serializable
data class UserTopicRating(
    val topic: CodingTopic,
    val rating: Int,
    val attempts: Int = 0,
    val correctAnswers: Int = 0,
    val lastPracticedAt: Long? = null,
) {
    val accuracy: Double get() = if (attempts == 0) 0.0 else correctAnswers.toDouble() / attempts

    /**
     * A rating built on three attempts is a guess, not a measurement. The UI
     * marks these as "provisional" rather than showing a confident number.
     */
    val isProvisional: Boolean get() = attempts < PROVISIONAL_ATTEMPTS

    companion object {
        const val PROVISIONAL_ATTEMPTS = 5
    }
}

/** A rating on one practice mode (Puzzle, Debugging, Complexity, ...). */
@Serializable
data class ModeRating(
    val mode: PracticeMode,
    val rating: Int,
    val attempts: Int = 0,
    val correctAnswers: Int = 0,
)

/** The complete rating picture for a user at one moment. */
@Serializable
data class PlayerRatings(
    val overall: Int = DEFAULT_RATING,
    val topics: Map<CodingTopic, UserTopicRating> = emptyMap(),
    val modes: Map<PracticeMode, ModeRating> = emptyMap(),
    val updatedAt: Long = 0L,
) {
    fun topicRating(topic: CodingTopic): Int = topics[topic]?.rating ?: DEFAULT_RATING

    fun modeRating(mode: PracticeMode): Int = modes[mode]?.rating ?: DEFAULT_RATING

    /**
     * Weakest topics the user has actually practised. Untouched topics are
     * excluded - recommending "your weakest topic is Tries" to someone who has
     * never seen a trie problem is noise, not insight.
     */
    fun weakestTopics(limit: Int = 3): List<UserTopicRating> =
        topics.values
            .filter { it.attempts > 0 }
            .sortedWith(compareBy({ it.rating }, { it.topic.ordinal }))
            .take(limit)

    fun strongestTopics(limit: Int = 3): List<UserTopicRating> =
        topics.values
            .filter { it.attempts > 0 }
            .sortedWith(compareByDescending<UserTopicRating> { it.rating }.thenBy { it.topic.ordinal })
            .take(limit)

    companion object {
        const val DEFAULT_RATING = 1000
    }
}

/** The result of rating one attempt: what moved, and by how much. */
@Serializable
data class RatingUpdate(
    val overallBefore: Int,
    val overallAfter: Int,
    val topicChanges: Map<CodingTopic, Int> = emptyMap(),
    val modeChange: Pair<PracticeMode, Int>? = null,
    /** True when the raw Elo delta was clipped by the per-attempt cap. */
    val wasCapped: Boolean = false,
) {
    val overallChange: Int get() = overallAfter - overallBefore
}

/** One point on a rating chart. [topic] is null for the overall rating. */
@Serializable
data class RatingHistoryEntry(
    val id: String,
    val userId: String,
    val topic: CodingTopic? = null,
    val rating: Int,
    val change: Int,
    val recordedAt: Long,
    val problemId: String? = null,
)
