package com.codingarena.domain.engine

import com.codingarena.domain.model.Achievement
import com.codingarena.domain.model.AchievementProgress
import com.codingarena.domain.model.AchievementTrigger
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PlayerStats
import com.codingarena.domain.model.ReviewLabel
import com.codingarena.domain.model.StreakState

/**
 * Everything the achievement rules read. Passing one snapshot keeps evaluation
 * a single pure function over the whole catalogue.
 */
data class AchievementContext(
    val stats: PlayerStats,
    val ratings: PlayerRatings,
    val streak: StreakState,
    val startingRating: Int,
    val hintFreeStreak: Int = 0,
    val bestCodeRushScore: Int = 0,
    val patternsMastered: Int = 0,
    val bestReviewLabel: ReviewLabel? = null,
)

/**
 * Evaluates the achievement catalogue.
 *
 * Achievements are meant to reward learning behaviour rather than grinding, so
 * the triggers key off ratings, streaks and hint-free solves - not raw volume
 * beyond a single "you started" milestone.
 */
class AchievementEngine(private val catalogue: List<Achievement>) {

    /** Current progress for every achievement, unlocked or not. */
    fun evaluate(
        context: AchievementContext,
        alreadyUnlocked: Map<String, Long> = emptyMap(),
        now: Long = 0L,
    ): List<AchievementProgress> = catalogue.map { achievement ->
        val (current, target) = measure(achievement.trigger, context)
        val previouslyUnlockedAt = alreadyUnlocked[achievement.id]
        val unlockedAt = previouslyUnlockedAt ?: if (current >= target) now else null
        AchievementProgress(
            achievement = achievement,
            current = current.coerceAtMost(target),
            target = target,
            unlockedAt = unlockedAt,
        )
    }

    /** Achievements that just crossed their threshold, for the unlock toast. */
    fun newlyUnlocked(
        context: AchievementContext,
        alreadyUnlocked: Map<String, Long>,
        now: Long,
    ): List<Achievement> = evaluate(context, alreadyUnlocked, now)
        .filter { it.isUnlocked && it.achievement.id !in alreadyUnlocked }
        .map { it.achievement }

    private fun measure(
        trigger: AchievementTrigger,
        context: AchievementContext,
    ): Pair<Int, Int> = when (trigger) {
        is AchievementTrigger.ChallengesCompleted ->
            context.stats.totalCompleted to trigger.count

        is AchievementTrigger.StreakDays ->
            maxOf(context.streak.currentStreak, context.streak.longestStreak) to trigger.days

        is AchievementTrigger.HintFreeStreak ->
            context.hintFreeStreak to trigger.count

        // Measured relative to the default starting rating, otherwise a brand
        // new user's untouched 1000 would render as most of the way there.
        is AchievementTrigger.TopicRating ->
            (context.ratings.topicRating(trigger.topic) - PlayerRatings.DEFAULT_RATING)
                .coerceAtLeast(0) to
                (trigger.rating - PlayerRatings.DEFAULT_RATING).coerceAtLeast(1)

        is AchievementTrigger.OverallRatingGain ->
            (context.ratings.overall - context.startingRating).coerceAtLeast(0) to trigger.points

        is AchievementTrigger.CodeRushScore ->
            context.bestCodeRushScore to trigger.score

        is AchievementTrigger.PatternsMastered ->
            context.patternsMastered to trigger.count

        AchievementTrigger.PerfectReview ->
            (if (context.bestReviewLabel == ReviewLabel.BRILLIANT) 1 else 0) to 1
    }
}
