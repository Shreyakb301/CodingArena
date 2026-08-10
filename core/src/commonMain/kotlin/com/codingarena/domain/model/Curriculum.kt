package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * The pattern groups a curated interview list is organised by.
 *
 * These are the NeetCode sections rather than [CodingTopic] because the whole
 * point of the roadmap is the grouping: "this is a Sliding Window problem" is
 * the thing being memorised.
 */
@Serializable
enum class PatternGroup(val displayName: String, val patternId: String?) {
    ARRAYS_HASHING("Arrays & Hashing", "frequency-map"),
    TWO_POINTERS("Two Pointers", "two-pointers"),
    SLIDING_WINDOW("Sliding Window", "sliding-window"),
    STACK("Stack", "monotonic-stack"),
    BINARY_SEARCH("Binary Search", "binary-search"),
    LINKED_LIST("Linked List", null),
    TREES("Trees", "dfs"),
    TRIES("Tries", "trie"),
    HEAP("Heap / Priority Queue", "heap"),
    BACKTRACKING("Backtracking", "backtracking"),
    GRAPHS("Graphs", "graph-traversal"),
    ADVANCED_GRAPHS("Advanced Graphs", "topological-sort"),
    DP_1D("1-D Dynamic Programming", "dynamic-programming"),
    DP_2D("2-D Dynamic Programming", "dynamic-programming"),
    GREEDY("Greedy", "greedy"),
    INTERVALS("Intervals", "greedy"),
    MATH_GEOMETRY("Math & Geometry", null),
    BIT_MANIPULATION("Bit Manipulation", "bit-manipulation"),
    ;

    /**
     * Groups that are genuinely easy to confuse with this one.
     *
     * Blitz draws its wrong answers from here. Offering "Trie" as a distractor
     * for a Sliding Window problem teaches nothing - the mistakes worth
     * drilling are the ones you would actually make.
     */
    val confusableWith: List<PatternGroup>
        get() = when (this) {
            ARRAYS_HASHING -> listOf(TWO_POINTERS, SLIDING_WINDOW, HEAP)
            TWO_POINTERS -> listOf(SLIDING_WINDOW, ARRAYS_HASHING, BINARY_SEARCH)
            SLIDING_WINDOW -> listOf(TWO_POINTERS, ARRAYS_HASHING, DP_1D)
            STACK -> listOf(ARRAYS_HASHING, LINKED_LIST, GREEDY)
            BINARY_SEARCH -> listOf(TWO_POINTERS, ARRAYS_HASHING, GREEDY)
            LINKED_LIST -> listOf(TWO_POINTERS, STACK, ARRAYS_HASHING)
            TREES -> listOf(GRAPHS, BACKTRACKING, HEAP)
            TRIES -> listOf(TREES, ARRAYS_HASHING, BACKTRACKING)
            HEAP -> listOf(ARRAYS_HASHING, GREEDY, BINARY_SEARCH)
            BACKTRACKING -> listOf(DP_1D, GRAPHS, TREES)
            GRAPHS -> listOf(TREES, ADVANCED_GRAPHS, BACKTRACKING)
            ADVANCED_GRAPHS -> listOf(GRAPHS, HEAP, GREEDY)
            DP_1D -> listOf(DP_2D, GREEDY, BACKTRACKING)
            DP_2D -> listOf(DP_1D, BACKTRACKING, GRAPHS)
            GREEDY -> listOf(DP_1D, INTERVALS, HEAP)
            INTERVALS -> listOf(GREEDY, HEAP, ARRAYS_HASHING)
            MATH_GEOMETRY -> listOf(ARRAYS_HASHING, BIT_MANIPULATION, TWO_POINTERS)
            BIT_MANIPULATION -> listOf(MATH_GEOMETRY, ARRAYS_HASHING, DP_1D)
        }
}

@Serializable
enum class CurriculumDifficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
}

/**
 * One entry on a curated list.
 *
 * Deliberately does *not* carry the original problem statement - those are
 * copyrighted. [ask] is a one-line paraphrase written for this app, enough to
 * trigger pattern recall, and [slug] links out to solve the real thing.
 */
@Serializable
data class CurriculumProblem(
    val slug: String,
    val title: String,
    val difficulty: CurriculumDifficulty,
    val group: PatternGroup,
    /** One-line paraphrase, written for this app. */
    val ask: String,
    val inBlind75: Boolean = false,
) {
    val url: String get() = "https://leetcode.com/problems/$slug/"
}

/** A named curated list. */
@Serializable
data class Curriculum(
    val id: String,
    val name: String,
    val description: String,
    val problems: List<CurriculumProblem>,
) {
    val sections: List<CurriculumSection>
        get() = PatternGroup.entries
            .mapNotNull { group ->
                problems.filter { it.group == group }
                    .takeIf { it.isNotEmpty() }
                    ?.let { CurriculumSection(group, it) }
            }

    fun bySlug(slug: String): CurriculumProblem? = problems.firstOrNull { it.slug == slug }
}

@Serializable
data class CurriculumSection(
    val group: PatternGroup,
    val problems: List<CurriculumProblem>,
)

/**
 * How well one curriculum problem's pattern has been recalled.
 *
 * Separate from [UserTopicRating] on purpose: rating measures skill, this
 * measures coverage of a specific list. Drilling a card you already know does
 * almost nothing to your rating, but it should still visibly advance the
 * roadmap.
 */
@Serializable
data class RecallRecord(
    val slug: String,
    val correctStreak: Int = 0,
    val totalSeen: Int = 0,
    val totalCorrect: Int = 0,
    val lastSeenAt: Long? = null,
    val fastestMs: Long? = null,
    /**
     * When this card should come back.
     *
     * Recall decays, so "locked in" is not permanent - a mastered card still
     * returns a month later to prove it stuck. Null means never drilled.
     */
    val dueAt: Long? = null,
    /** True once the user reports having actually solved it on LeetCode. */
    val solved: Boolean = false,
    val solvedAt: Long? = null,
) {
    fun isDue(now: Long): Boolean = dueAt == null || dueAt <= now

    /**
     * The interval this card earns next, on the same ladder the rest of the app
     * uses. Streak length drives it, so a lapse genuinely costs you time.
     */
    val nextInterval: ReviewStage
        get() = when (correctStreak) {
            0 -> ReviewStage.RELEARN
            1 -> ReviewStage.SHAKY
            2 -> ReviewStage.SLOW
            3 -> ReviewStage.CONFIDENT
            else -> ReviewStage.MASTERED
        }

    /**
     * Recall is "locked in" after three consecutive correct answers.
     *
     * Three rather than one because the whole point is memorisation - getting
     * it right once may just mean the distractors were weak that round.
     */
    val isMastered: Boolean get() = correctStreak >= MASTERY_STREAK

    val accuracy: Double get() = if (totalSeen == 0) 0.0 else totalCorrect.toDouble() / totalSeen

    val strength: RecallStrength
        get() = when {
            totalSeen == 0 -> RecallStrength.UNSEEN
            correctStreak >= MASTERY_STREAK -> RecallStrength.MASTERED
            correctStreak >= 1 -> RecallStrength.LEARNING
            else -> RecallStrength.SHAKY
        }

    companion object {
        const val MASTERY_STREAK = 3
    }
}

@Serializable
enum class RecallStrength(val displayName: String) {
    UNSEEN("Not seen"),
    SHAKY("Shaky"),
    LEARNING("Learning"),
    MASTERED("Locked in"),
}

/** Roadmap progress for one curriculum. */
@Serializable
data class CurriculumProgress(
    val curriculumId: String,
    val total: Int,
    val seen: Int,
    val mastered: Int,
    /** Cards whose review has come round again. */
    val due: Int = 0,
    /** Problems the user reports actually solving on LeetCode. */
    val solved: Int = 0,
    val sectionProgress: Map<PatternGroup, SectionProgress> = emptyMap(),
) {
    val fraction: Float = if (total == 0) 0f else (mastered.toFloat() / total).coerceIn(0f, 1f)

    val coverageFraction: Float = if (total == 0) 0f else (seen.toFloat() / total).coerceIn(0f, 1f)

    val solvedFraction: Float = if (total == 0) 0f else (solved.toFloat() / total).coerceIn(0f, 1f)

    /** Sections with the most ground left, for "what to drill next". */
    fun weakestSections(limit: Int = 3): List<SectionProgress> =
        sectionProgress.values
            .filter { it.total > 0 }
            .sortedWith(compareBy({ it.fraction }, { it.group.ordinal }))
            .take(limit)
}

@Serializable
data class SectionProgress(
    val group: PatternGroup,
    val total: Int,
    val seen: Int,
    val mastered: Int,
    val solved: Int = 0,
) {
    val fraction: Float = if (total == 0) 0f else (mastered.toFloat() / total).coerceIn(0f, 1f)
}
