package com.codingarena.content

import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.WorkoutStepKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProblemWorkoutsTest {
    private val workouts = ProblemWorkouts.workouts

    @Test
    fun `at least one workout is authored`() {
        assertTrue(workouts.isNotEmpty())
    }

    @Test
    fun `every step has exactly three choices, one correct`() {
        workouts.forEach { workout ->
            workout.steps.forEach { step ->
                val label = "${workout.problemSlug}/${step.kind}"
                assertEquals(3, step.choices.size, label)
                assertEquals(1, step.choices.count { it.correct }, label)
                step.choices.forEach { choice ->
                    assertTrue(choice.text.isNotBlank(), label)
                    assertTrue(choice.feedback.length >= 40, "$label has shallow feedback")
                }
            }
        }
    }

    @Test
    fun `steps carry their workout's problem and group`() {
        workouts.forEach { workout ->
            workout.steps.forEach { step ->
                assertEquals(workout.problemSlug, step.problemSlug, step.id)
                assertEquals(workout.group, step.group, step.id)
            }
        }
    }

    @Test
    fun `the beginner tier of a full workout follows the fixed step order`() {
        // A problem's flat step list now spans every authored difficulty
        // tier (the round engine filters by difficulty itself); the fixed,
        // single-tier ordering invariant only applies to the original
        // Beginner tier the content was originally authored against.
        val fullOrder = listOf(
            WorkoutStepKind.PATTERN_RECOGNITION,
            WorkoutStepKind.APPROACH,
            WorkoutStepKind.STATE_SELECTION,
            WorkoutStepKind.BOUNDARY_UPDATE,
            WorkoutStepKind.CODE_BLOCK,
            WorkoutStepKind.TIME_COMPLEXITY,
            WorkoutStepKind.SPACE_COMPLEXITY,
            WorkoutStepKind.TRANSFER,
            WorkoutStepKind.EDGE_CASE,
        )
        workouts.forEach { workout ->
            val beginnerSteps = workout.steps.filter { it.difficulty == PracticeDifficulty.BEGINNER }
            assertEquals(fullOrder, beginnerSteps.map { it.kind }, workout.problemSlug)
        }
    }

    @Test
    fun `build the solution kinds are a prefix of the full step order`() {
        // Build the Solution slices through TRANSFER; EDGE_CASE is Problem-Workout-only.
        val buildKinds = WorkoutStepKind.buildTheSolutionKinds
        assertEquals(WorkoutStepKind.EDGE_CASE, WorkoutStepKind.entries.last())
        assertTrue(WorkoutStepKind.entries.dropLast(1) == buildKinds, "buildTheSolutionKinds $buildKinds")
    }

    @Test
    fun `correct answer position does not reveal a fixed pattern`() {
        workouts.forEach { workout ->
            val positions = workout.steps.map { step -> step.correctIndex }
            assertTrue(positions.distinct().size >= 3, "${workout.problemSlug} correct choices only use positions $positions")
        }
    }

    @Test
    fun `step ids are unique across every workout`() {
        val allIds = workouts.flatMap { it.steps.map { s -> s.id } }
        assertEquals(allIds.size, allIds.distinct().size, "duplicate step ids found")
    }

    @Test
    fun `problem slugs are unique across the pool`() {
        val slugs = workouts.map { it.problemSlug }
        assertEquals(slugs.size, slugs.distinct().size, "duplicate problem slugs found")
    }
}
