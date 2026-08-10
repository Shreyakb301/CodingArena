package com.codingarena.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PatternCategory(val displayName: String) {
    ARRAYS_AND_STRINGS("Arrays & Strings"),
    TREES("Trees"),
    GRAPHS("Graphs"),
    SEARCHING_AND_SORTING("Searching & Sorting"),
    OTHER("Other"),
}

/**
 * A reusable approach, taught rather than merely tagged.
 *
 * [recognitionSignals] is the field that does the real work: the product's
 * claim is that users learn to *recognise* which pattern a problem wants, so
 * every pattern page leads with the cues that give it away.
 */
@Serializable
data class CodingPattern(
    val id: String,
    val name: String,
    val category: PatternCategory,
    val topic: CodingTopic,
    val summary: String,
    val whenToUse: String,
    val recognitionSignals: List<String>,
    /** Small worked example rendered as monospace text. */
    val visualExample: String,
    val codeTemplate: String,
    val commonMistakes: List<String>,
    val timeComplexity: String,
    val spaceComplexity: String,
    /** Problem ids in escalating order: beginner, intermediate, mastery. */
    val beginnerProblemIds: List<String> = emptyList(),
    val intermediateProblemIds: List<String> = emptyList(),
    val masteryProblemIds: List<String> = emptyList(),
) {
    val allProblemIds: List<String>
        get() = beginnerProblemIds + intermediateProblemIds + masteryProblemIds
}

@Serializable
enum class MasteryLevel(val displayName: String) {
    NOT_STARTED("Not started"),
    LEARNING("Learning"),
    PRACTISED("Practised"),
    MASTERED("Mastered"),
}

/** How far a user has got with one pattern. */
@Serializable
data class PatternProgress(
    val patternId: String,
    val solvedProblemIds: Set<String> = emptySet(),
    val totalProblems: Int = 0,
    val level: MasteryLevel = MasteryLevel.NOT_STARTED,
    val lastPractisedAt: Long? = null,
) {
    val fraction: Float
        get() = if (totalProblems == 0) 0f else
            (solvedProblemIds.size.toFloat() / totalProblems).coerceIn(0f, 1f)
}
