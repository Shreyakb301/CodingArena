package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/** The five-beat shape a learning path follows (spec 5.10). */
@Serializable
enum class StepKind(val displayName: String) {
    LESSON("Short lesson"),
    EASY_PRACTICE("Easy practice"),
    MEDIUM_PRACTICE("Medium practice"),
    REVIEW("Review challenge"),
    MASTERY("Mastery assessment"),
}

@Serializable
data class LearningPathStep(
    val id: String,
    val kind: StepKind,
    val title: String,
    /** Pattern to read for [StepKind.LESSON] steps. */
    val patternId: String? = null,
    /** Problems to solve for practice steps. */
    val problemIds: List<String> = emptyList(),
    val completedProblemIds: Set<String> = emptySet(),
    val completedAt: Long? = null,
) {
    val isComplete: Boolean
        get() = completedAt != null ||
            (problemIds.isNotEmpty() && completedProblemIds.containsAll(problemIds))

    val fraction: Float
        get() = when {
            isComplete -> 1f
            problemIds.isEmpty() -> 0f
            else -> (completedProblemIds.size.toFloat() / problemIds.size).coerceIn(0f, 1f)
        }
}

/**
 * A short, finishable sequence aimed at one weakness.
 *
 * [rationale] is shown verbatim to the user - the spec's example is
 * "You understand basic Sliding Window problems but struggle when the window
 * requires a frequency map", and the recommender is expected to produce that
 * kind of specific sentence rather than "practise more".
 */
@Serializable
data class LearningPath(
    val id: String,
    val userId: String,
    val title: String,
    val rationale: String,
    val targetTopic: CodingTopic,
    val steps: List<LearningPathStep>,
    val createdAt: Long,
    val completedAt: Long? = null,
) {
    val currentStep: LearningPathStep? get() = steps.firstOrNull { !it.isComplete }

    val completedStepCount: Int get() = steps.count { it.isComplete }

    val fraction: Float
        get() = if (steps.isEmpty()) 0f else completedStepCount.toFloat() / steps.size

    val isComplete: Boolean get() = steps.isNotEmpty() && steps.all { it.isComplete }
}
