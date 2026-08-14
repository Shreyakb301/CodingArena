package com.codingarena.content

import com.codingarena.domain.model.ExerciseMode
import com.codingarena.domain.model.ProgrammingLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArenaCourseTest {
    @Test
    fun `available chapters form a valid closed prerequisite graph`() {
        val chapters = ArenaCourse.availableChapters
        val ids = chapters.map { it.id }.toSet()
        chapters.forEach { chapter ->
            assertTrue(chapter.prerequisiteIds.all { it in ids })
            assertTrue(chapter.lessons.isNotEmpty(), chapter.id)
            assertTrue(chapter.exercises.any { it.mode == ExerciseMode.CHECKPOINT }, chapter.id)
            assertTrue(chapter.exercises.any { it.mode == ExerciseMode.INDEPENDENT_CODE }, chapter.id)
            assertTrue(chapter.exercises.any { it.mode == ExerciseMode.BLITZ }, chapter.id)
        }
    }

    @Test
    fun `every independent problem supports all launch languages and public tests`() {
        ArenaCourse.availableChapters.flatMap { it.independentExercises }.forEach { exercise ->
            assertEquals(ProgrammingLanguage.entries.toSet(), exercise.templates.map { it.language }.toSet())
            assertTrue(exercise.examples.isNotEmpty(), exercise.id)
            assertTrue(exercise.hiddenTestCount > 0, exercise.id)
        }
    }

    @Test
    fun `guided course covers all mobile puzzle formats`() {
        val modes = ArenaCourse.availableChapters.flatMap { it.exercises }.map { it.mode }.toSet()
        assertTrue(ExerciseMode.TRACE in modes)
        assertTrue(ExerciseMode.REARRANGE in modes)
        assertTrue(ExerciseMode.DEBUG in modes)
        assertTrue(ExerciseMode.FILL_CODE in modes)
    }
}
