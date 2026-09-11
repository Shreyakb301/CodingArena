package com.codingarena.content

import com.codingarena.domain.model.SystemDesignCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SystemDesignContentTest {
    private val concepts = SystemDesignContent.concepts

    @Test
    fun `at least one concept is authored`() {
        assertTrue(concepts.isNotEmpty())
    }

    @Test
    fun `concept ids are unique`() {
        val ids = concepts.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "duplicate concept ids found")
    }

    @Test
    fun `every concept has a summary and key points`() {
        concepts.forEach { concept ->
            assertTrue(concept.summary.isNotBlank(), concept.id)
            assertTrue(concept.keyPoints.isNotEmpty(), concept.id)
            assertTrue(concept.questions.isNotEmpty(), "${concept.id} has no questions")
        }
    }

    @Test
    fun `every question has exactly one correct choice with real explanations`() {
        concepts.forEach { concept ->
            concept.questions.forEach { question ->
                val label = "${concept.id}/${question.id}"
                assertEquals(3, question.choices.size, label)
                assertEquals(1, question.choices.count { it.correct }, label)
                question.choices.forEach { choice ->
                    assertTrue(choice.text.isNotBlank(), label)
                    assertTrue(choice.explanation.length >= 40, "$label has a shallow explanation")
                }
            }
        }
    }

    @Test
    fun `question ids are unique across all concepts`() {
        val ids = concepts.flatMap { it.questions.map { q -> q.id } }
        assertEquals(ids.size, ids.distinct().size, "duplicate question ids found")
    }

    @Test
    fun `questionForDay is stable and cycles through every question`() {
        val total = SystemDesignContent.allQuestions.size
        val seen = (0 until total.toLong()).map { SystemDesignContent.questionForDay(it).id }.toSet()
        assertEquals(total, seen.size, "the daily rotation should eventually reach every question")
        // Stable: same day always yields the same question.
        assertEquals(SystemDesignContent.questionForDay(5), SystemDesignContent.questionForDay(5))
    }

    @Test
    fun `byCategory only returns concepts of that category`() {
        SystemDesignCategory.entries.forEach { category ->
            SystemDesignContent.byCategory(category).forEach { concept ->
                assertEquals(category, concept.category, concept.id)
            }
        }
    }
}
