package com.codingarena.content

import com.codingarena.domain.model.BehavioralCategory
import com.codingarena.domain.model.ChoiceRating
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.RatedChoice
import com.codingarena.domain.model.StarStage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterviewContentTest {

    private fun assertWellFormedChoices(label: String, choices: List<RatedChoice>) {
        assertEquals(3, choices.size, "$label should have exactly 3 choices")
        assertEquals(1, choices.count { it.rating == ChoiceRating.STRONG }, "$label should have exactly one STRONG choice")
        assertEquals(1, choices.count { it.rating == ChoiceRating.REASONABLE }, "$label should have exactly one REASONABLE choice")
        assertEquals(1, choices.count { it.rating == ChoiceRating.WEAK }, "$label should have exactly one WEAK choice")
        choices.forEach { choice ->
            assertTrue(choice.text.isNotBlank(), "$label has a blank choice")
            assertTrue(choice.feedback.length >= 40, "$label/${choice.id} has shallow feedback (${choice.feedback.length} chars)")
        }
    }

    @Test
    fun `at least one STAR workout is authored`() {
        assertTrue(InterviewContent.starWorkouts.isNotEmpty())
    }

    @Test
    fun `every STAR workout has exactly five steps in the fixed stage order`() {
        InterviewContent.starWorkouts.forEach { workout ->
            assertEquals(StarStage.ORDERED, workout.steps.map { it.stage }, workout.id)
        }
    }

    @Test
    fun `every STAR step is well formed`() {
        InterviewContent.starWorkouts.forEach { workout ->
            workout.steps.forEach { step ->
                assertWellFormedChoices("${workout.id}/${step.stage}", step.choices)
                assertTrue(step.prompt.isNotBlank(), "${workout.id}/${step.stage} has a blank prompt")
            }
        }
    }

    @Test
    fun `every STAR workout has a non-blank common question`() {
        InterviewContent.starWorkouts.forEach { workout ->
            assertTrue(workout.commonQuestion.isNotBlank(), "${workout.id} has a blank commonQuestion")
        }
    }

    @Test
    fun `a category authored at more than one difficulty tier uses a different common question per tier`() {
        InterviewContent.starWorkouts.groupBy { it.category }.forEach { (category, workouts) ->
            val questionsByTier = workouts.associate { it.difficulty to it.commonQuestion }
            if (questionsByTier.size > 1) {
                assertEquals(questionsByTier.size, questionsByTier.values.distinct().size, "$category repeats the same commonQuestion across tiers")
            }
        }
    }

    @Test
    fun `every behavioral exercise is well formed`() {
        InterviewContent.behavioralExercises.forEach { exercise ->
            assertWellFormedChoices(exercise.id, exercise.choices)
            assertTrue(exercise.scenario.isNotBlank(), "${exercise.id} has a blank scenario")
            assertTrue(exercise.prompt.isNotBlank(), "${exercise.id} has a blank prompt")
            assertTrue(exercise.commonQuestion.isNotBlank(), "${exercise.id} has a blank commonQuestion")
        }
    }

    @Test
    fun `every technical communication item is well formed`() {
        InterviewContent.techCommItems.forEach { item ->
            assertWellFormedChoices(item.id, item.choices)
            assertTrue(item.prompt.isNotBlank(), "${item.id} has a blank prompt")
        }
    }

    @Test
    fun `every authored mock interview has exactly five connected decisions in order`() {
        assertTrue(InterviewContent.mockInterviews.isNotEmpty())
        InterviewContent.mockInterviews.forEach { mock ->
            assertEquals(5, mock.decisions.size, mock.id)
            assertEquals((1..5).toList(), mock.decisions.map { it.order }, mock.id)
            mock.decisions.forEach { decision -> assertWellFormedChoices("${mock.id}/${decision.id}", decision.choices) }
        }
    }

    @Test
    fun `mock interview ids are unique`() {
        val ids = InterviewContent.mockInterviews.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "duplicate mock interview ids")
    }

    @Test
    fun `every id across all Interview content is globally unique`() {
        val ids = buildList {
            InterviewContent.starWorkouts.forEach { workout ->
                add(workout.id)
                workout.steps.forEach { step -> step.choices.forEach { add(it.id) } }
            }
            InterviewContent.behavioralExercises.forEach { exercise ->
                add(exercise.id)
                exercise.choices.forEach { add(it.id) }
            }
            InterviewContent.techCommItems.forEach { item ->
                add(item.id)
                item.choices.forEach { add(it.id) }
            }
            InterviewContent.mockInterviews.forEach { mock ->
                add(mock.id)
                mock.decisions.forEach { decision ->
                    add(decision.id)
                    decision.choices.forEach { add(it.id) }
                }
            }
        }
        assertEquals(ids.size, ids.distinct().size, "duplicate ids found across Interview content")
    }

    @Test
    fun `technical communication items only reference problems authored elsewhere`() {
        val knownSlugs = setOf("two-sum", "contains-duplicate", "valid-anagram", "group-anagrams")
        InterviewContent.techCommItems.forEach { item ->
            assertTrue(item.problemSlug in knownSlugs, "${item.id} references an unexpected problem slug ${item.problemSlug}")
        }
    }

    @Test
    fun `lookup helpers resolve authored content at the exact tier`() {
        BehavioralCategory.entries.forEach { category ->
            val workout = InterviewContent.starWorkoutFor(category, PracticeDifficulty.BEGINNER)
            if (workout != null) assertEquals(category, workout.category)
        }
        assertTrue(InterviewContent.techCommItemsFor("two-sum", PracticeDifficulty.BEGINNER).isNotEmpty())
    }

    @Test
    fun `starWorkoutFor falls back through searchOrder when the exact tier isn't authored`() {
        // every category is authored at BEGINNER; INTERMEDIATE/ADVANCED aren't authored yet for any category,
        // so asking for them must still resolve via nearest-tier fallback rather than returning null.
        BehavioralCategory.entries.forEach { category ->
            val fallback = InterviewContent.starWorkoutFor(category, PracticeDifficulty.ADVANCED)
            assertTrue(fallback != null, "$category should resolve an ADVANCED request via searchOrder fallback")
        }
    }

    @Test
    fun `techCommItemsFor falls back through searchOrder when the exact tier isn't authored`() {
        val fallback = InterviewContent.techCommItemsFor("two-sum", PracticeDifficulty.ADVANCED)
        assertTrue(fallback.isNotEmpty(), "two-sum should resolve an ADVANCED request via searchOrder fallback")
    }

    @Test
    fun `at least one category is authored beyond BEGINNER`() {
        val tiersAuthored = InterviewContent.starWorkouts.map { it.difficulty }.distinct()
        assertTrue(tiersAuthored.size > 1, "expected more than one difficulty tier of STAR workouts to be authored")
    }
}
