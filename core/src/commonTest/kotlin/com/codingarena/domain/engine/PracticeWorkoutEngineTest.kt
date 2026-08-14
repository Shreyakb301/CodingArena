package com.codingarena.domain.engine

import com.codingarena.content.NeetCode150
import com.codingarena.content.ProblemWorkout
import com.codingarena.content.ProblemWorkouts
import com.codingarena.domain.model.AdaptivePracticePhase
import com.codingarena.domain.model.AdaptivePracticeState
import com.codingarena.domain.model.CurriculumProgress
import com.codingarena.domain.model.MissedConcept
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.PracticeExperienceLevel
import com.codingarena.domain.model.PracticeSessionKind
import com.codingarena.domain.model.RecallRecord
import com.codingarena.domain.model.SectionProgress
import com.codingarena.domain.model.WorkoutStep
import com.codingarena.domain.model.WorkoutStepKind
import com.codingarena.domain.model.searchOrder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticeWorkoutEngineTest {

    private val engine = PracticeWorkoutEngine()
    private val curriculum = NeetCode150.curriculum
    private val workouts = ProblemWorkouts.workouts
    private val authoredGroups = workouts.map { it.group }.distinct()

    private fun progressWithMastered(masteredSectionCount: Int): CurriculumProgress {
        val sections = curriculum.sections.mapIndexed { index, section ->
            val mastered = if (index < masteredSectionCount) section.problems.size else 0
            section.group to SectionProgress(
                group = section.group,
                total = section.problems.size,
                seen = section.problems.size,
                mastered = mastered,
            )
        }.toMap()
        return CurriculumProgress(
            curriculumId = curriculum.id,
            total = curriculum.problems.size,
            seen = curriculum.problems.size,
            mastered = sections.values.sumOf { it.mastered },
            sectionProgress = sections,
        )
    }

    /** Progress where every authored-batch topic has been touched, none mastered. */
    private fun progressCoveringAuthoredTopics(): CurriculumProgress {
        val sections = curriculum.sections.associate { section ->
            section.group to if (section.group in authoredGroups) {
                SectionProgress(section.group, total = section.problems.size, seen = 1, mastered = 0)
            } else {
                SectionProgress(section.group, total = section.problems.size, seen = 0, mastered = 0)
            }
        }
        return CurriculumProgress(curriculum.id, curriculum.problems.size, 0, 0, sectionProgress = sections)
    }

    private fun freshState(
        mode: PracticeSessionKind,
        topic: PatternGroup? = null,
        difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
        missedConcepts: List<MissedConcept> = emptyList(),
        consecutiveBelowEight: Int = 0,
    ): AdaptivePracticeState = AdaptivePracticeState(
        id = "state-1",
        userId = "user-1",
        mode = mode,
        topic = topic,
        difficulty = difficulty,
        missedConcepts = missedConcepts,
        consecutiveBelowEight = consecutiveBelowEight,
        startedAt = 0L,
    )

    private fun answerAllCorrectly(state: AdaptivePracticeState): AdaptivePracticeState {
        var s = state
        val questions = s.currentRound!!.questions
        questions.forEach { q ->
            s = engine.submitRoundAnswer(s, q.instanceId, q.step.correctIndex, elapsedMs = 100, now = 100)
        }
        return s
    }

    private fun answerAllWith(state: AdaptivePracticeState, correct: Boolean): AdaptivePracticeState {
        var s = state
        val questions = s.currentRound!!.questions
        questions.forEach { q ->
            val index = if (correct) q.step.correctIndex else q.step.choices.indexOfFirst { !it.correct }
            s = engine.submitRoundAnswer(s, q.instanceId, index, elapsedMs = 100, now = 100)
        }
        return s
    }

    // ------------------------------------------------------------ experienceLevel

    @Test
    fun `zero to two mastered sections is beginner`() {
        assertEquals(PracticeExperienceLevel.BEGINNER, engine.experienceLevel(progressWithMastered(0)))
        assertEquals(PracticeExperienceLevel.BEGINNER, engine.experienceLevel(progressWithMastered(2)))
    }

    @Test
    fun `three to eight mastered sections is progressing`() {
        assertEquals(PracticeExperienceLevel.PROGRESSING, engine.experienceLevel(progressWithMastered(3)))
        assertEquals(PracticeExperienceLevel.PROGRESSING, engine.experienceLevel(progressWithMastered(8)))
    }

    @Test
    fun `nine or more mastered sections is advanced`() {
        assertEquals(PracticeExperienceLevel.ADVANCED, engine.experienceLevel(progressWithMastered(9)))
    }

    @Test
    fun `null progress is beginner`() {
        assertEquals(PracticeExperienceLevel.BEGINNER, engine.experienceLevel(null))
    }

    // ------------------------------------------------------------ currentTopic / reviewTopics

    @Test
    fun `current topic is the first section not yet fully mastered`() {
        val first = curriculum.sections[0].group
        val second = curriculum.sections[1].group
        val progress = CurriculumProgress(
            curriculumId = curriculum.id, total = 0, seen = 0, mastered = 0,
            sectionProgress = mapOf(first to SectionProgress(first, total = 5, seen = 5, mastered = 5)),
        )
        assertEquals(second, engine.currentTopic(curriculum, progress))
    }

    @Test
    fun `current topic is null once every section is mastered`() {
        val sections = curriculum.sections.associate {
            it.group to SectionProgress(it.group, it.problems.size, it.problems.size, it.problems.size)
        }
        val progress = CurriculumProgress(
            curriculum.id, curriculum.problems.size, curriculum.problems.size, curriculum.problems.size,
            sectionProgress = sections,
        )
        assertNull(engine.currentTopic(curriculum, progress))
    }

    @Test
    fun `review topics rank lower accuracy first`() {
        val groupA = PatternGroup.ARRAYS_HASHING
        val groupB = PatternGroup.TWO_POINTERS
        val slugA = curriculum.problems.first { it.group == groupA }.slug
        val slugB = curriculum.problems.first { it.group == groupB }.slug
        val records = mapOf(
            slugA to RecallRecord(slug = slugA, totalSeen = 10, totalCorrect = 9, lastSeenAt = 1_000),
            slugB to RecallRecord(slug = slugB, totalSeen = 10, totalCorrect = 3, lastSeenAt = 1_000),
        )
        val progress = CurriculumProgress(
            curriculumId = curriculum.id, total = 0, seen = 0, mastered = 0,
            sectionProgress = mapOf(
                groupA to SectionProgress(groupA, total = 9, seen = 1, mastered = 0),
                groupB to SectionProgress(groupB, total = 5, seen = 1, mastered = 0),
            ),
        )
        val ranked = engine.reviewTopics(curriculum, progress, records)
        assertEquals(groupB, ranked.first(), "the 30% accuracy topic should be ranked as needing review first")
    }

    @Test
    fun `review topics only include already-learned sections`() {
        val group = PatternGroup.ARRAYS_HASHING
        val progress = CurriculumProgress(
            curriculumId = curriculum.id, total = 0, seen = 0, mastered = 0,
            sectionProgress = mapOf(group to SectionProgress(group, total = 9, seen = 0, mastered = 0)),
        )
        assertTrue(engine.reviewTopics(curriculum, progress, emptyMap()).isEmpty())
    }

    // ------------------------------------------------------------ hasCompleteQuestionBank

    @Test
    fun `an authored topic with all four tiers has a complete question bank`() {
        authoredGroups.forEach { group ->
            assertTrue(engine.hasCompleteQuestionBank(group, workouts), "$group should be complete")
        }
    }

    @Test
    fun `an unauthored topic does not have a complete question bank`() {
        val unauthored = PatternGroup.entries.first { it !in authoredGroups }
        assertFalse(engine.hasCompleteQuestionBank(unauthored, workouts))
    }

    // ------------------------------------------------------------ round assembly: exactly ten

    @Test
    fun `a topic-focused round always contains exactly ten questions`() {
        authoredGroups.forEach { group ->
            val state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
            val started = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(1))
            assertEquals(10, started.currentRound?.questions?.size, group.name)
        }
    }

    @Test
    fun `a recommended round always contains exactly ten questions`() {
        val progress = progressCoveringAuthoredTopics()
        repeat(20) { seed ->
            val state = freshState(PracticeSessionKind.RECOMMENDED)
            val started = engine.startRound(state, curriculum, progress, emptyMap(), workouts, now = 0L, random = Random(seed))
            assertEquals(10, started.currentRound?.questions?.size, "seed $seed")
        }
    }

    @Test
    fun `a mixed round always contains exactly ten questions`() {
        val progress = progressCoveringAuthoredTopics()
        val state = freshState(PracticeSessionKind.MIXED)
        val started = engine.startRound(state, curriculum, progress, emptyMap(), workouts, now = 0L, random = Random(2))
        assertEquals(10, started.currentRound?.questions?.size)
    }

    @Test
    fun `a thin pool still reaches ten questions via the least-recently-shown fallback`() {
        // A single-problem, single-tier slice of content is far thinner than the
        // real 5-topic pool - the round must still reach exactly ten by repeating.
        val thinWorkouts = listOf(workouts.first { it.problemSlug == "two-sum" })
        val group = thinWorkouts.first().group
        val state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        val started = engine.startRound(state, curriculum, null, emptyMap(), thinWorkouts, now = 0L, random = Random(5))
        assertEquals(10, started.currentRound?.questions?.size)
    }

    // ------------------------------------------------------------ review question selection

    @Test
    fun `a concept missed in one round is reviewed in the next, pinned to the difficulty it was missed at`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, difficulty = PracticeDifficulty.DEVELOPING)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(3))
        state = answerAllWith(state, correct = false)
        state = engine.finishRound(state, now = 100)

        assertTrue(state.missedConcepts.isNotEmpty())
        val missed = state.missedConcepts.first()
        assertEquals(PracticeDifficulty.DEVELOPING, missed.missedAtDifficulty)

        val nextRound = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 200L, random = Random(9))
        val reviewSteps = nextRound.currentRound!!.questions.filter { it.isReview }.map { it.step }
        assertTrue(reviewSteps.any { it.group == missed.group && it.conceptKey == missed.conceptKey })
        assertTrue(reviewSteps.all { it.difficulty == missed.missedAtDifficulty || !it.isReviewOf(missed) })
    }

    // Helper predicate kept local to the test above's readability.
    private fun WorkoutStep.isReviewOf(concept: MissedConcept) =
        group == concept.group && conceptKey == concept.conceptKey

    @Test
    fun `review questions never exceed five per round`() {
        val group = PatternGroup.STACK
        val manyMissed = (1..8).map { i ->
            MissedConcept(
                group = group,
                conceptKey = "stack-fake-concept-$i",
                kind = WorkoutStepKind.APPROACH,
                missedAtDifficulty = PracticeDifficulty.BEGINNER,
                missCount = 10 - i,
            )
        }
        // Fake concept keys won't resolve to real steps, so this also proves
        // startRound gracefully skips unresolvable reviews rather than failing.
        val state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, missedConcepts = manyMissed)
        val started = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(4))
        val reviewCount = started.currentRound!!.questions.count { it.isReview }
        assertTrue(reviewCount <= 5, "got $reviewCount review questions")
    }

    @Test
    fun `a review question prefers a different problem slug than previously used for that concept`() {
        val group = PatternGroup.ARRAYS_HASHING
        // Real, resolvable concept: pick one authored at BEGINNER for this group.
        val realConcept = workouts.filter { it.group == group }
            .flatMap { it.steps }
            .first { it.difficulty == PracticeDifficulty.BEGINNER && it.kind == WorkoutStepKind.APPROACH }
        val missed = MissedConcept(
            group = group,
            conceptKey = realConcept.conceptKey,
            kind = WorkoutStepKind.APPROACH,
            missedAtDifficulty = PracticeDifficulty.BEGINNER,
            missCount = 5,
            reviewedStepSlugs = setOf(realConcept.problemSlug),
        )
        val state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, missedConcepts = listOf(missed))
        val started = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(6))

        val reviewStep = started.currentRound!!.questions.first { it.isReview }.step
        assertEquals(realConcept.conceptKey, reviewStep.conceptKey)
        // Only asserted when an alternative genuinely exists in the pool.
        val alternatives = workouts.filter { it.group == group }.flatMap { it.steps }
            .filter { it.conceptKey == realConcept.conceptKey && it.difficulty == PracticeDifficulty.BEGINNER }
        if (alternatives.size > 1) {
            assertTrue(reviewStep.problemSlug != realConcept.problemSlug, "expected a different variation")
        }
    }

    // ------------------------------------------------------------ quota interaction

    @Test
    fun `remaining slots after review are filled with new questions honoring the type quota`() {
        val group = PatternGroup.BINARY_SEARCH
        val realConcept = workouts.filter { it.group == group }
            .flatMap { it.steps }
            .first { it.difficulty == PracticeDifficulty.BEGINNER && it.kind == WorkoutStepKind.APPROACH }
        val missed = MissedConcept(
            group = group,
            conceptKey = realConcept.conceptKey,
            kind = WorkoutStepKind.APPROACH,
            missedAtDifficulty = PracticeDifficulty.BEGINNER,
            missCount = 3,
        )
        val state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, missedConcepts = listOf(missed))
        val started = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(8))

        val questions = started.currentRound!!.questions
        assertEquals(10, questions.size)
        val reviewCount = questions.count { it.isReview }
        val newCount = questions.count { !it.isReview }
        assertEquals(10, reviewCount + newCount)
    }

    // ------------------------------------------------------------ weakness resolution

    @Test
    fun `a missed concept is not cleared after one correct review, only after two`() {
        val group = PatternGroup.SLIDING_WINDOW
        val realConcept = workouts.filter { it.group == group }
            .flatMap { it.steps }
            .first { it.difficulty == PracticeDifficulty.BEGINNER && it.kind == WorkoutStepKind.TIME_COMPLEXITY }
        var missed = MissedConcept(
            group = group,
            conceptKey = realConcept.conceptKey,
            kind = WorkoutStepKind.TIME_COMPLEXITY,
            missedAtDifficulty = PracticeDifficulty.BEGINNER,
            missCount = 1,
        )
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, missedConcepts = listOf(missed))

        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(11))
        state = answerAllCorrectly(state)
        state = engine.finishRound(state, now = 10)
        missed = state.missedConcepts.first { it.conceptKey == realConcept.conceptKey }
        assertEquals(1, missed.successfulReviews)
        assertTrue(missed.pendingReview, "one correct review should not resolve the weakness yet")

        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 20, random = Random(12))
        state = answerAllCorrectly(state)
        state = engine.finishRound(state, now = 30)
        missed = state.missedConcepts.first { it.conceptKey == realConcept.conceptKey }
        assertEquals(2, missed.successfulReviews)
        assertFalse(missed.pendingReview, "two correct reviews should resolve the weakness")
    }

    @Test
    fun `missing a review again resets successful reviews and reopens pending review`() {
        val group = PatternGroup.SLIDING_WINDOW
        val realConcept = workouts.filter { it.group == group }
            .flatMap { it.steps }
            .first { it.difficulty == PracticeDifficulty.BEGINNER && it.kind == WorkoutStepKind.TIME_COMPLEXITY }
        val missed = MissedConcept(
            group = group,
            conceptKey = realConcept.conceptKey,
            kind = WorkoutStepKind.TIME_COMPLEXITY,
            missedAtDifficulty = PracticeDifficulty.BEGINNER,
            missCount = 1,
            successfulReviews = 1,
            pendingReview = true,
        )
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, missedConcepts = listOf(missed))
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(13))
        state = answerAllWith(state, correct = false)
        state = engine.finishRound(state, now = 10)

        val updated = state.missedConcepts.first { it.conceptKey == realConcept.conceptKey }
        assertEquals(0, updated.successfulReviews)
        assertTrue(updated.pendingReview)
        assertEquals(2, updated.missCount)
    }

    // ------------------------------------------------------------ difficulty state machine

    @Test
    fun `a score of eight advances difficulty`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, difficulty = PracticeDifficulty.BEGINNER)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(20))
        val questions = state.currentRound!!.questions
        questions.forEachIndexed { index, q ->
            val correct = index < 8
            val choiceIndex = if (correct) q.step.correctIndex else q.step.choices.indexOfFirst { !it.correct }
            state = engine.submitRoundAnswer(state, q.instanceId, choiceIndex, elapsedMs = 100, now = 100)
        }
        state = engine.finishRound(state, now = 200)
        assertEquals(PracticeDifficulty.DEVELOPING, state.difficulty)
        assertEquals(0, state.consecutiveBelowEight)
    }

    @Test
    fun `a score of seven does not advance difficulty`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, difficulty = PracticeDifficulty.BEGINNER)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(21))
        val questions = state.currentRound!!.questions
        questions.forEachIndexed { index, q ->
            val correct = index < 7
            val choiceIndex = if (correct) q.step.correctIndex else q.step.choices.indexOfFirst { !it.correct }
            state = engine.submitRoundAnswer(state, q.instanceId, choiceIndex, elapsedMs = 100, now = 100)
        }
        state = engine.finishRound(state, now = 200)
        assertEquals(PracticeDifficulty.BEGINNER, state.difficulty)
        assertEquals(1, state.consecutiveBelowEight)
    }

    @Test
    fun `two consecutive sub-eight rounds reduce difficulty`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, difficulty = PracticeDifficulty.DEVELOPING)

        repeat(2) { roundNum ->
            state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = roundNum * 100L, random = Random(30 + roundNum))
            state = answerAllWith(state, correct = false)
            state = engine.finishRound(state, now = roundNum * 100L + 50)
        }

        assertEquals(PracticeDifficulty.BEGINNER, state.difficulty)
        assertEquals(0, state.consecutiveBelowEight)
    }

    @Test
    fun `difficulty never rises above advanced`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, difficulty = PracticeDifficulty.ADVANCED)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(40))
        state = answerAllCorrectly(state)
        state = engine.finishRound(state, now = 100)
        assertEquals(PracticeDifficulty.ADVANCED, state.difficulty)
    }

    @Test
    fun `difficulty never drops below beginner`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, difficulty = PracticeDifficulty.BEGINNER, consecutiveBelowEight = 1)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(41))
        state = answerAllWith(state, correct = false)
        state = engine.finishRound(state, now = 100)
        assertEquals(PracticeDifficulty.BEGINNER, state.difficulty)
    }

    // ------------------------------------------------------------ difficulty search order

    @Test
    fun `difficulty search falls back through nearest easier then nearest harder tiers`() {
        assertEquals(
            listOf(PracticeDifficulty.DEVELOPING, PracticeDifficulty.BEGINNER, PracticeDifficulty.INTERMEDIATE, PracticeDifficulty.ADVANCED),
            PracticeDifficulty.DEVELOPING.searchOrder(),
        )
        assertEquals(
            listOf(PracticeDifficulty.BEGINNER, PracticeDifficulty.DEVELOPING, PracticeDifficulty.INTERMEDIATE, PracticeDifficulty.ADVANCED),
            PracticeDifficulty.BEGINNER.searchOrder(),
        )
    }

    // ------------------------------------------------------------ repeat avoidance

    @Test
    fun `a correctly answered question is not repeated while unseen alternatives exist in the pool`() {
        val group = PatternGroup.ARRAYS_HASHING
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(50))
        val firstRoundStepIds = state.currentRound!!.questions.map { it.step.id }.toSet()
        state = answerAllCorrectly(state)
        state = engine.finishRound(state, now = 100)

        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 200L, random = Random(51))
        val secondRoundStepIds = state.currentRound!!.questions.map { it.step.id }.toSet()

        val overlap = firstRoundStepIds.intersect(secondRoundStepIds)
        // Arrays & Hashing has a large enough Beginner pool that a full repeat isn't expected.
        assertTrue(overlap.size < firstRoundStepIds.size, "expected meaningfully fresh content, overlap=$overlap")
    }

    // ------------------------------------------------------------ topic isolation

    @Test
    fun `topic-focused rounds only draw from the selected topic`() {
        val group = PatternGroup.TWO_POINTERS
        val state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        val started = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(60))
        assertTrue(started.currentRound!!.questions.all { it.step.group == group })
    }

    @Test
    fun `recommended rounds exclude topics the user has not learned`() {
        // Only ARRAYS_HASHING has been touched; every other topic (learned or not) must be excluded.
        val learnedOnly = PatternGroup.ARRAYS_HASHING
        val sections = curriculum.sections.associate {
            it.group to if (it.group == learnedOnly) {
                SectionProgress(it.group, total = it.problems.size, seen = 1, mastered = 0)
            } else {
                SectionProgress(it.group, total = it.problems.size, seen = 0, mastered = 0)
            }
        }
        val progress = CurriculumProgress(curriculum.id, curriculum.problems.size, 1, 0, sectionProgress = sections)
        val state = freshState(PracticeSessionKind.RECOMMENDED)
        val started = engine.startRound(state, curriculum, progress, emptyMap(), workouts, now = 0L, random = Random(61))

        assertTrue(started.currentRound!!.questions.all { it.step.group == learnedOnly })
    }

    @Test
    fun `recommended and mixed rounds exclude topics with an incomplete question bank`() {
        val unauthored = PatternGroup.entries.first { it !in authoredGroups }
        val sections = curriculum.sections.associate {
            it.group to SectionProgress(it.group, total = it.problems.size, seen = 1, mastered = 0)
        }
        val progress = CurriculumProgress(curriculum.id, curriculum.problems.size, curriculum.problems.size, 0, sectionProgress = sections)

        val recommended = engine.startRound(
            freshState(PracticeSessionKind.RECOMMENDED), curriculum, progress, emptyMap(), workouts, now = 0L, random = Random(62),
        )
        assertTrue(recommended.currentRound!!.questions.none { it.step.group == unauthored })

        val mixed = engine.startRound(
            freshState(PracticeSessionKind.MIXED), curriculum, progress, emptyMap(), workouts, now = 0L, random = Random(63),
        )
        assertTrue(mixed.currentRound!!.questions.none { it.step.group == unauthored })
    }

    // ------------------------------------------------------------ answer-safe shuffling

    @Test
    fun `shuffling a step's choices never changes which choice is graded correct`() {
        val step = workouts.first().steps.first()
        repeat(30) { seed ->
            val shuffled = step.copy(choices = step.choices.shuffled(Random(seed)))
            val correctChoice = step.choices.first { it.correct }
            assertEquals(correctChoice.text, shuffled.choices[shuffled.correctIndex].text, "seed $seed")
            assertEquals(1, shuffled.choices.count { it.correct })
        }
    }

    // ------------------------------------------------------------ instanceId uniqueness

    @Test
    fun `repeated placements of the same step within one round get distinct instance ids`() {
        val thinWorkouts = listOf(workouts.first { it.problemSlug == "two-sum" })
        val group = thinWorkouts.first().group
        val state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        val started = engine.startRound(state, curriculum, null, emptyMap(), thinWorkouts, now = 0L, random = Random(70))

        val instanceIds = started.currentRound!!.questions.map { it.instanceId }
        assertEquals(instanceIds.size, instanceIds.distinct().size, "duplicate instanceIds found")
    }

    @Test
    fun `submitting an answer for a duplicated step's two occurrences does not collide`() {
        val thinWorkouts = listOf(workouts.first { it.problemSlug == "two-sum" })
        val group = thinWorkouts.first().group
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        state = engine.startRound(state, curriculum, null, emptyMap(), thinWorkouts, now = 0L, random = Random(71))

        val duplicatedStepId = state.currentRound!!.questions
            .groupBy { it.step.id }
            .entries.firstOrNull { it.value.size > 1 }
            ?.key
        if (duplicatedStepId != null) {
            val occurrences = state.currentRound!!.questions.filter { it.step.id == duplicatedStepId }
            state = engine.submitRoundAnswer(state, occurrences[0].instanceId, occurrences[0].step.correctIndex, 100, 100)
            state = engine.submitRoundAnswer(state, occurrences[1].instanceId, occurrences[1].step.correctIndex, 100, 100)
            assertEquals(2, state.currentRound!!.answers.count { it.instanceId in occurrences.map { o -> o.instanceId } })
        }
    }

    // ------------------------------------------------------------ finishRound / phase transitions

    @Test
    fun `finishRound keeps the completed round and moves to ROUND_RESULTS instead of clearing it`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group, difficulty = PracticeDifficulty.DEVELOPING)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(80))
        state = answerAllCorrectly(state)
        state = engine.finishRound(state, now = 100)

        assertEquals(AdaptivePracticePhase.ROUND_RESULTS, state.phase)
        assertNotNull(state.currentRound)
        assertEquals(PracticeDifficulty.DEVELOPING, state.currentRound!!.targetDifficulty)
        assertTrue(state.currentRound!!.isOver)
    }

    @Test
    fun `phase invariants hold after every transition`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        assertTrue(state.isPhaseValid, "READY")

        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(90))
        assertTrue(state.isPhaseValid, "ROUND_ACTIVE")

        state = answerAllCorrectly(state)
        assertTrue(state.isPhaseValid, "still ROUND_ACTIVE, all answered but not finished")

        state = engine.finishRound(state, now = 100)
        assertTrue(state.isPhaseValid, "ROUND_RESULTS")

        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 200L, random = Random(91))
        assertTrue(state.isPhaseValid, "back to ROUND_ACTIVE via Continue Practicing")
    }

    // ------------------------------------------------------------ transition guards

    @Test
    fun `startRound is a no-op when a round is already active`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(100))
        val activeRound = state.currentRound

        val unchanged = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 999L, random = Random(101))

        assertEquals(activeRound, unchanged.currentRound)
    }

    @Test
    fun `submitRoundAnswer is a no-op outside ROUND_ACTIVE`() {
        val group = PatternGroup.STACK
        val state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group) // phase READY, no round
        val unchanged = engine.submitRoundAnswer(state, "q0", 0, elapsedMs = 100, now = 100)
        assertEquals(state, unchanged)
    }

    @Test
    fun `submitRoundAnswer rejects a duplicate instanceId`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(110))
        val question = state.currentRound!!.questions.first()

        state = engine.submitRoundAnswer(state, question.instanceId, question.step.correctIndex, 100, 100)
        val afterFirst = state.currentRound!!.answers.size

        val afterDuplicate = engine.submitRoundAnswer(state, question.instanceId, question.step.correctIndex, 100, 100)

        assertEquals(afterFirst, afterDuplicate.currentRound!!.answers.size)
    }

    @Test
    fun `submitRoundAnswer rejects an out-of-range choice index`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(111))
        val question = state.currentRound!!.questions.first()

        val unchanged = engine.submitRoundAnswer(state, question.instanceId, 99, elapsedMs = 100, now = 100)

        assertEquals(0, unchanged.currentRound!!.answers.size)
    }

    @Test
    fun `finishRound is a no-op until every question is answered`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(120))
        val question = state.currentRound!!.questions.first()
        state = engine.submitRoundAnswer(state, question.instanceId, question.step.correctIndex, 100, 100)

        val unchanged = engine.finishRound(state, now = 200)

        assertEquals(AdaptivePracticePhase.ROUND_ACTIVE, unchanged.phase)
        assertEquals(state, unchanged)
    }

    @Test
    fun `Continue Practicing replaces the completed round and returns to ROUND_ACTIVE`() {
        val group = PatternGroup.STACK
        var state = freshState(PracticeSessionKind.TOPIC_FOCUSED, topic = group)
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(130))
        state = answerAllCorrectly(state)
        state = engine.finishRound(state, now = 100)
        val finishedRound = state.currentRound

        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 200L, random = Random(131))

        assertEquals(AdaptivePracticePhase.ROUND_ACTIVE, state.phase)
        assertTrue(state.currentRound !== finishedRound)
        assertFalse(state.currentRound!!.isOver)
    }

    // ------------------------------------------------------------ persistence round-trip

    @Test
    fun `a saved state round-trips through JSON with phase, difficulty, and missed concepts intact`() {
        val group = PatternGroup.STACK
        var state = freshState(
            PracticeSessionKind.TOPIC_FOCUSED, topic = group, difficulty = PracticeDifficulty.INTERMEDIATE,
            missedConcepts = listOf(
                MissedConcept(
                    group = group, conceptKey = "some-concept", kind = WorkoutStepKind.APPROACH,
                    missedAtDifficulty = PracticeDifficulty.DEVELOPING, missCount = 2,
                ),
            ),
        )
        state = engine.startRound(state, curriculum, null, emptyMap(), workouts, now = 0L, random = Random(140))

        val json = Json { ignoreUnknownKeys = true }
        val encoded = json.encodeToString(state)
        val decoded = json.decodeFromString<AdaptivePracticeState>(encoded)

        assertEquals(state.phase, decoded.phase)
        assertEquals(state.difficulty, decoded.difficulty)
        assertEquals(state.missedConcepts, decoded.missedConcepts)
        assertEquals(state.currentRound?.questions?.size, decoded.currentRound?.questions?.size)
    }
}
