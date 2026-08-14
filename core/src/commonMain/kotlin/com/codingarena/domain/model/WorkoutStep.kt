package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * One step in a [ProblemWorkout][com.codingarena.content.ProblemWorkout]'s decision chain.
 *
 * Deliberately separate from Roadmap's `LessonQuestion` - same three-choice
 * shape, but this is a standalone reasoning drill, not a teaching lesson, and
 * it should not be coupled to that flow.
 */
@Serializable
enum class WorkoutStepKind {
    /** "Identify the pattern." */
    PATTERN_RECOGNITION,

    /** "Choose the best approach." */
    APPROACH,

    /** "Select the required state." */
    STATE_SELECTION,

    /** "Choose boundary and update logic." */
    BOUNDARY_UPDATE,

    /** "Identify the correct code block." */
    CODE_BLOCK,

    TIME_COMPLEXITY,
    SPACE_COMPLEXITY,

    /** "Would this pattern also solve a different problem?" */
    TRANSFER,

    /** Authored but not currently drawn into any round - no mode exercises it today. */
    EDGE_CASE,
    ;

    companion object {
        /** Build the Solution: everything up to and including the transfer question. */
        val buildTheSolutionKinds: List<WorkoutStepKind> = listOf(
            PATTERN_RECOGNITION, APPROACH, STATE_SELECTION, BOUNDARY_UPDATE,
            CODE_BLOCK, TIME_COMPLEXITY, SPACE_COMPLEXITY, TRANSFER,
        )
    }
}

@Serializable
data class WorkoutChoice(
    val text: String,
    val correct: Boolean,
    val feedback: String,
    val code: String? = null,
)

@Serializable
data class WorkoutStep(
    val id: String,
    /** [CurriculumProblem.slug] - provenance, since Mixed Practice draws steps from different problems. */
    val problemSlug: String,
    val group: PatternGroup,
    val kind: WorkoutStepKind,
    /**
     * Names one specific misunderstanding, e.g. "window-shrink-condition" -
     * shared across every step (any problem, any difficulty) that tests that
     * same misunderstanding. `group + kind` selects question *format*;
     * `group + conceptKey` is the review-selection join key that identifies
     * *which* misunderstanding a wrong answer revealed.
     */
    val conceptKey: String,
    val difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    val prompt: String,
    val code: String? = null,
    val choices: List<WorkoutChoice>,
) {
    val correctIndex: Int get() = choices.indexOfFirst { it.correct }
}
