package com.codingarena.content

import com.codingarena.domain.model.Achievement
import com.codingarena.domain.model.AchievementTier
import com.codingarena.domain.model.AchievementTrigger
import com.codingarena.domain.model.CodingTopic

/**
 * The initial achievement set (spec 5.13).
 *
 * Note what is absent: there is no "solve 100 problems" badge. Every trigger
 * beyond the first-challenge milestone keys off rating movement, hint-free
 * solves, streaks or mastery - things a user cannot reach by replaying easy
 * content.
 */
object AchievementCatalogue {

    val achievements: List<Achievement> = listOf(
        Achievement(
            id = "first-challenge",
            title = "First Challenge",
            description = "Complete your first challenge.",
            emoji = "🌱",
            trigger = AchievementTrigger.ChallengesCompleted(1),
        ),
        Achievement(
            id = "first-perfect-review",
            title = "First Perfect Review",
            description = "Earn a Brilliant on a problem above your rating.",
            emoji = "✨",
            trigger = AchievementTrigger.PerfectReview,
            tier = AchievementTier.SILVER,
        ),
        Achievement(
            id = "streak-7",
            title = "Seven-Day Streak",
            description = "Practise on seven consecutive days.",
            emoji = "🔥",
            trigger = AchievementTrigger.StreakDays(7),
            tier = AchievementTier.SILVER,
        ),
        Achievement(
            id = "streak-30",
            title = "Thirty-Day Streak",
            description = "Practise on thirty consecutive days.",
            emoji = "🏆",
            trigger = AchievementTrigger.StreakDays(30),
            tier = AchievementTier.GOLD,
        ),
        Achievement(
            id = "hint-free-10",
            title = "Ten Without Hints",
            description = "Solve ten challenges in a row without using a hint.",
            emoji = "🎯",
            trigger = AchievementTrigger.HintFreeStreak(10),
            tier = AchievementTier.SILVER,
        ),
        Achievement(
            id = "arrays-apprentice",
            title = "Arrays Apprentice",
            description = "Reach a 1200 Arrays rating.",
            emoji = "📐",
            trigger = AchievementTrigger.TopicRating(CodingTopic.ARRAYS, 1200),
        ),
        Achievement(
            id = "sliding-window-specialist",
            title = "Sliding Window Specialist",
            description = "Reach a 1300 Sliding Window rating.",
            emoji = "🪟",
            trigger = AchievementTrigger.TopicRating(CodingTopic.SLIDING_WINDOW, 1300),
            tier = AchievementTier.SILVER,
        ),
        Achievement(
            id = "tree-explorer",
            title = "Tree Explorer",
            description = "Reach a 1250 Trees rating.",
            emoji = "🌳",
            trigger = AchievementTrigger.TopicRating(CodingTopic.TREES, 1250),
            tier = AchievementTier.SILVER,
        ),
        Achievement(
            id = "debugging-expert",
            title = "Debugging Expert",
            description = "Reach a 1350 Debugging rating.",
            emoji = "🔍",
            trigger = AchievementTrigger.TopicRating(CodingTopic.DEBUGGING, 1350),
            tier = AchievementTier.GOLD,
        ),
        Achievement(
            id = "rating-gain-100",
            title = "First 100 Rating Gain",
            description = "Gain 100 rating points since you started.",
            emoji = "📈",
            trigger = AchievementTrigger.OverallRatingGain(100),
        ),
        Achievement(
            id = "first-pattern-mastered",
            title = "First Mastered Pattern",
            description = "Take a pattern all the way to mastery.",
            emoji = "🧩",
            trigger = AchievementTrigger.PatternsMastered(1),
            tier = AchievementTier.SILVER,
        ),
        Achievement(
            id = "code-rush-10",
            title = "Code Rush 10",
            description = "Score 10 in a single Code Rush run.",
            emoji = "⚡",
            trigger = AchievementTrigger.CodeRushScore(10),
        ),
        Achievement(
            id = "code-rush-25",
            title = "Code Rush 25",
            description = "Score 25 in a single Code Rush run.",
            emoji = "🚀",
            trigger = AchievementTrigger.CodeRushScore(25),
            tier = AchievementTier.SILVER,
        ),
        Achievement(
            id = "code-rush-50",
            title = "Code Rush 50",
            description = "Score 50 in a single Code Rush run.",
            emoji = "👑",
            trigger = AchievementTrigger.CodeRushScore(50),
            tier = AchievementTier.GOLD,
        ),
    )
}
