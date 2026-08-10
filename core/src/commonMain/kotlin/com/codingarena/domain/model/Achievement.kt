package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * What an achievement measures. Keeping the trigger as data (rather than a
 * lambda per achievement) means the whole set can be evaluated in one pass and
 * partial progress can be shown for locked ones.
 */
@Serializable
sealed interface AchievementTrigger {
    /** Total challenges completed. */
    @Serializable
    data class ChallengesCompleted(val count: Int) : AchievementTrigger

    /** Consecutive practice days. */
    @Serializable
    data class StreakDays(val days: Int) : AchievementTrigger

    /** Correct answers in a row with no hints used. */
    @Serializable
    data class HintFreeStreak(val count: Int) : AchievementTrigger

    /** A topic rating crossing a threshold. */
    @Serializable
    data class TopicRating(val topic: CodingTopic, val rating: Int) : AchievementTrigger

    /** Net overall rating gained since the starting rating. */
    @Serializable
    data class OverallRatingGain(val points: Int) : AchievementTrigger

    /** Best Code Rush score. */
    @Serializable
    data class CodeRushScore(val score: Int) : AchievementTrigger

    /** Patterns taken all the way to mastery. */
    @Serializable
    data class PatternsMastered(val count: Int) : AchievementTrigger

    /** A single review earning the top label. */
    @Serializable
    data object PerfectReview : AchievementTrigger
}

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val trigger: AchievementTrigger,
    val tier: AchievementTier = AchievementTier.BRONZE,
)

@Serializable
enum class AchievementTier(val displayName: String) {
    BRONZE("Bronze"),
    SILVER("Silver"),
    GOLD("Gold"),
}

/** An achievement plus how close the user is to it. */
@Serializable
data class AchievementProgress(
    val achievement: Achievement,
    val current: Int,
    val target: Int,
    val unlockedAt: Long? = null,
) {
    val isUnlocked: Boolean get() = unlockedAt != null

    val fraction: Float
        get() = if (target <= 0) 0f else (current.toFloat() / target).coerceIn(0f, 1f)
}
