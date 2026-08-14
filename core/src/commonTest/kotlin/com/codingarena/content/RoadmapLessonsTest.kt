package com.codingarena.content

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoadmapLessonsTest {
    private val lessons = RoadmapLessons.all

    @Test
    fun `at least one lesson is authored`() {
        assertTrue(lessons.isNotEmpty())
    }

    @Test
    fun `every question has one correct and two instructive distractors`() {
        lessons.forEach { lesson ->
            lesson.questions.forEach { question ->
                val label = "${lesson.slug}/${question.id}"
                assertEquals(3, question.choices.size, label)
                assertEquals(1, question.choices.count { it.correct }, label)
                question.choices.forEach { choice ->
                    assertTrue(choice.text.isNotBlank(), label)
                    assertTrue(choice.feedback.length >= 40, "$label has shallow feedback")
                }
            }
        }
    }

    /**
     * Two authored sequences coexist - see [LessonQuestionKind]'s doc comment.
     * Every lesson must match one of them exactly, not just contain the right
     * kinds in some order.
     */
    @Test
    fun `every lesson follows one of the two authored question sequences`() {
        val legacySequence = listOf(
            LessonQuestionKind.APPROACH,
            LessonQuestionKind.CODE_BLOCK,
            LessonQuestionKind.DEBUG,
            LessonQuestionKind.TIME_COMPLEXITY,
            LessonQuestionKind.SPACE_COMPLEXITY,
        )
        val stepByStepSequence = listOf(
            LessonQuestionKind.PATTERN_RECOGNITION,
            LessonQuestionKind.APPROACH,
            LessonQuestionKind.STATE_SELECTION,
            LessonQuestionKind.BOUNDARY_UPDATE,
            LessonQuestionKind.CODE_BLOCK,
            LessonQuestionKind.TIME_COMPLEXITY,
        )
        lessons.forEach { lesson ->
            val kinds = lesson.questions.map { it.kind }
            assertTrue(kinds == legacySequence || kinds == stepByStepSequence, "${lesson.slug}: $kinds")
        }
    }

    @Test
    fun `correct answer position does not reveal a fixed pattern`() {
        lessons.forEach { lesson ->
            val positions = lesson.questions.map { question -> question.choices.indexOfFirst { it.correct } }
            assertTrue(positions.distinct().size >= 3, "${lesson.slug} correct choices only use positions $positions")
        }
    }

    @Test
    fun `lesson has a complete authored explanation`() {
        lessons.forEach { lesson ->
            val explanation = lesson.explanation
            val label = lesson.slug
            assertTrue(explanation.intuition.isNotBlank(), label)
            assertTrue(explanation.walkthrough.size >= 3, label)
            assertTrue(explanation.pseudocode.isNotBlank(), label)
            assertTrue(explanation.referenceCode.isNotBlank(), label)
            assertTrue(explanation.timeComplexity.contains("O("), label)
            assertTrue(explanation.spaceComplexity.contains("O("), label)
            assertTrue(explanation.alternatives.isNotEmpty(), label)
            assertTrue(explanation.commonMistakes.isNotEmpty(), label)
            assertTrue(explanation.recognitionSignals.isNotEmpty(), label)
        }
    }

    @Test
    fun `lesson ids are unique across the roadmap`() {
        val allQuestionIds = lessons.flatMap { it.questions.map { q -> q.id } }
        assertEquals(allQuestionIds.size, allQuestionIds.distinct().size, "duplicate question ids found")
    }
}
