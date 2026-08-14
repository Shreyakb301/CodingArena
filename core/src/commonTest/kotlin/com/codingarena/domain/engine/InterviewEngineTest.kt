package com.codingarena.domain.engine

import com.codingarena.domain.model.AdaptivePracticePhase
import com.codingarena.domain.model.BehavioralCategory
import com.codingarena.domain.model.BehavioralExercise
import com.codingarena.domain.model.ChoiceRating
import com.codingarena.domain.model.InterviewExerciseMode
import com.codingarena.domain.model.InterviewProgress
import com.codingarena.domain.model.InterviewRound
import com.codingarena.domain.model.InterviewRoundAnswer
import com.codingarena.domain.model.InterviewRoundQuestion
import com.codingarena.domain.model.InterviewRoundState
import com.codingarena.domain.model.InterviewRoundSurface
import com.codingarena.domain.model.MockInterview
import com.codingarena.domain.model.MockInterviewDecision
import com.codingarena.domain.model.MockInterviewResult
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.RatedChoice
import com.codingarena.domain.model.StarStage
import com.codingarena.domain.model.StarStep
import com.codingarena.domain.model.StarWorkout
import com.codingarena.domain.model.TechCommExerciseKind
import com.codingarena.domain.model.TechCommItem
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InterviewEngineTest {

    private val engine = InterviewEngine()

    // ------------------------------------------------------------ fixtures

    private fun choice(id: String, rating: ChoiceRating) = RatedChoice(id, "text-$id", rating, "feedback-$id")

    private fun starWorkout(id: String, category: BehavioralCategory, difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER) =
        StarWorkout(
            id = id,
            category = category,
            difficulty = difficulty,
            commonQuestion = "Tell me about a time for $id",
            scenario = "scenario-$id",
            steps = StarStage.ORDERED.map { stage ->
                StarStep(
                    stage = stage,
                    prompt = "prompt-$id-${stage.name}",
                    choices = listOf(
                        choice("$id-${stage.name}-strong", ChoiceRating.STRONG),
                        choice("$id-${stage.name}-reasonable", ChoiceRating.REASONABLE),
                        choice("$id-${stage.name}-weak", ChoiceRating.WEAK),
                    ),
                )
            },
        )

    private fun exercise(
        id: String,
        category: BehavioralCategory,
        mode: InterviewExerciseMode,
        difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    ) = BehavioralExercise(
        id = id,
        category = category,
        mode = mode,
        difficulty = difficulty,
        commonQuestion = "Common question for $id",
        scenario = "scenario-$id",
        prompt = "prompt-$id",
        choices = listOf(
            choice("$id-strong", ChoiceRating.STRONG),
            choice("$id-reasonable", ChoiceRating.REASONABLE),
            choice("$id-weak", ChoiceRating.WEAK),
        ),
    )

    private fun techCommItem(
        id: String,
        problemSlug: String,
        kind: TechCommExerciseKind,
        difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    ) = TechCommItem(
        id = id,
        problemSlug = problemSlug,
        kind = kind,
        difficulty = difficulty,
        prompt = "prompt-$id",
        choices = listOf(
            choice("$id-strong", ChoiceRating.STRONG),
            choice("$id-reasonable", ChoiceRating.REASONABLE),
            choice("$id-weak", ChoiceRating.WEAK),
        ),
    )

    private val exerciseModes = listOf(
        InterviewExerciseMode.IMPROVE_AN_ANSWER,
        InterviewExerciseMode.SPOT_RED_FLAG,
        InterviewExerciseMode.HANDLE_FOLLOWUPS,
    )

    // one full pool (workout + 3 exercises = 8 items) per category
    private fun workoutFor(category: BehavioralCategory, difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER) =
        starWorkout("${category.name}-${difficulty.name}-star", category, difficulty)

    private fun exercisesFor(category: BehavioralCategory, difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER) =
        exerciseModes.map { mode -> exercise("${category.name}-${difficulty.name}-${mode.name}", category, mode, difficulty) }

    private val leadershipWorkout = workoutFor(BehavioralCategory.LEADERSHIP)
    private val leadershipExercises = exercisesFor(BehavioralCategory.LEADERSHIP)
    private val conflictWorkout = workoutFor(BehavioralCategory.CONFLICT)
    private val conflictExercises = exercisesFor(BehavioralCategory.CONFLICT)
    private val teamworkWorkout = workoutFor(BehavioralCategory.TEAMWORK)
    private val teamworkExercises = exercisesFor(BehavioralCategory.TEAMWORK)
    private val leadershipDevWorkout = workoutFor(BehavioralCategory.LEADERSHIP, PracticeDifficulty.DEVELOPING)
    private val leadershipDevExercises = exercisesFor(BehavioralCategory.LEADERSHIP, PracticeDifficulty.DEVELOPING)

    private val allWorkouts = listOf(leadershipWorkout, conflictWorkout, teamworkWorkout, leadershipDevWorkout)
    private val allExercises = leadershipExercises + conflictExercises + teamworkExercises + leadershipDevExercises

    private fun freshBehavioralState(category: BehavioralCategory?, difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER) =
        InterviewRoundState(
            id = "round-1",
            userId = "u1",
            surface = InterviewRoundSurface.BEHAVIORAL,
            category = category,
            difficulty = difficulty,
            startedAt = 0L,
        )

    private fun freshTechCommState(problemSlug: String, difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER) =
        InterviewRoundState(
            id = "round-1",
            userId = "u1",
            surface = InterviewRoundSurface.TECH_COMM,
            problemSlug = problemSlug,
            difficulty = difficulty,
            startedAt = 0L,
        )

    // a hand-built, fully deterministic 10-question round for splice/finish tests
    private fun manualQuestion(index: Int, category: BehavioralCategory = BehavioralCategory.LEADERSHIP, stage: StarStage? = null) =
        InterviewRoundQuestion(
            instanceId = index.toString(),
            contentId = "q$index",
            category = category,
            commonQuestion = null,
            scenario = null,
            referenceAnswer = null,
            prompt = "prompt-$index",
            choices = listOf(choice("q$index-strong", ChoiceRating.STRONG), choice("q$index-weak", ChoiceRating.WEAK)),
            stage = stage,
        )

    private fun manualRoundState(surface: InterviewRoundSurface, difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER): InterviewRoundState {
        val questions = (0 until 10).map { manualQuestion(it) }
        return InterviewRoundState(
            id = "round-1",
            userId = "u1",
            surface = surface,
            category = if (surface == InterviewRoundSurface.BEHAVIORAL) BehavioralCategory.LEADERSHIP else null,
            problemSlug = if (surface == InterviewRoundSurface.TECH_COMM) "two-sum" else null,
            difficulty = difficulty,
            phase = AdaptivePracticePhase.ROUND_ACTIVE,
            currentRound = InterviewRound(targetDifficulty = difficulty, questions = questions, startedAt = 0L),
            startedAt = 0L,
        )
    }

    // ------------------------------------------------------------ startBehavioralRound

    @Test
    fun `startBehavioralRound assembles exactly ten questions from a single category's pool with repeats`() {
        val state = engine.startBehavioralRound(freshBehavioralState(BehavioralCategory.LEADERSHIP), allWorkouts, allExercises, now = 0L, random = Random(1))
        val round = state.currentRound
        assertNotNull(round)
        assertEquals(10, round.questions.size)
        assertEquals(AdaptivePracticePhase.ROUND_ACTIVE, state.phase)
    }

    @Test
    fun `startBehavioralRound single category includes every STAR stage and every exercise mode when the pool covers them`() {
        val state = engine.startBehavioralRound(freshBehavioralState(BehavioralCategory.LEADERSHIP), allWorkouts, allExercises, now = 0L, random = Random(2))
        val round = state.currentRound!!
        assertEquals(StarStage.ORDERED.toSet(), round.questions.mapNotNull { it.stage }.toSet())
        val exerciseContentIds = leadershipExercises.map { it.id }.toSet()
        assertTrue(round.questions.map { it.contentId }.toSet().containsAll(exerciseContentIds))
    }

    @Test
    fun `startBehavioralRound combined round draws from every category`() {
        val state = engine.startBehavioralRound(freshBehavioralState(category = null), allWorkouts, allExercises, now = 0L, random = Random(3))
        val round = state.currentRound!!
        assertEquals(10, round.questions.size)
        assertEquals(
            setOf(BehavioralCategory.LEADERSHIP, BehavioralCategory.CONFLICT, BehavioralCategory.TEAMWORK),
            round.questions.mapNotNull { it.category }.toSet(),
        )
    }

    @Test
    fun `startBehavioralRound is a safe no-op when the catalog has no content at all`() {
        val fresh = freshBehavioralState(BehavioralCategory.LEADERSHIP)
        val state = engine.startBehavioralRound(fresh, emptyList(), emptyList(), now = 0L)
        assertEquals(fresh, state)
    }

    @Test
    fun `startBehavioralRound is a no-op when already round active`() {
        val active = engine.startBehavioralRound(freshBehavioralState(BehavioralCategory.LEADERSHIP), allWorkouts, allExercises, now = 0L, random = Random(4))
        val again = engine.startBehavioralRound(active, allWorkouts, allExercises, now = 100L, random = Random(5))
        assertEquals(active, again)
    }

    @Test
    fun `startBehavioralRound is a no-op for a tech comm surfaced state`() {
        val techState = freshTechCommState("two-sum")
        val state = engine.startBehavioralRound(techState, allWorkouts, allExercises, now = 0L)
        assertEquals(techState, state)
    }

    @Test
    fun `startBehavioralRound honors difficulty via searchOrder fallback`() {
        val state = engine.startBehavioralRound(
            freshBehavioralState(BehavioralCategory.LEADERSHIP, PracticeDifficulty.DEVELOPING),
            allWorkouts,
            allExercises,
            now = 0L,
            random = Random(6),
        )
        val round = state.currentRound!!
        assertTrue(round.questions.any { it.contentId == leadershipDevWorkout.id + ":" + StarStage.SITUATION.name })
    }

    // ------------------------------------------------------------ submitBehavioralRoundAnswer guards

    @Test
    fun `submitBehavioralRoundAnswer is a no-op outside ROUND_ACTIVE`() {
        val ready = freshBehavioralState(BehavioralCategory.LEADERSHIP)
        val result = engine.submitBehavioralRoundAnswer(ready, "0", "q0-strong", now = 0L)
        assertEquals(ready, result)
    }

    @Test
    fun `submitBehavioralRoundAnswer rejects a duplicate instanceId`() {
        val state = manualRoundState(InterviewRoundSurface.BEHAVIORAL)
        val once = engine.submitBehavioralRoundAnswer(state, "0", "q0-strong", now = 0L)
        val twice = engine.submitBehavioralRoundAnswer(once, "0", "q0-strong", now = 1L)
        assertEquals(once, twice)
    }

    @Test
    fun `submitBehavioralRoundAnswer rejects an unresolvable choiceId`() {
        val state = manualRoundState(InterviewRoundSurface.BEHAVIORAL)
        val result = engine.submitBehavioralRoundAnswer(state, "0", "not-a-real-choice", now = 0L)
        assertEquals(state, result)
    }

    // ------------------------------------------------------------ replay-splice mechanic

    @Test
    fun `a WEAK answer with no prior solves is a safe no-op splice`() {
        val state = manualRoundState(InterviewRoundSurface.BEHAVIORAL)
        val afterWeak = engine.submitBehavioralRoundAnswer(state, "0", "q0-weak", now = 0L)
        assertEquals(state.currentRound!!.questions.drop(1), afterWeak.currentRound!!.questions.drop(1))
        assertTrue(afterWeak.recentlySolved.isEmpty())
        assertEquals(1, afterWeak.currentRound!!.answers.size)
    }

    @Test
    fun `a WEAK answer splices up to three most-recently-solved questions into the next slots`() {
        var state = manualRoundState(InterviewRoundSurface.BEHAVIORAL)
        state = engine.submitBehavioralRoundAnswer(state, "0", "q0-strong", now = 0L)
        state = engine.submitBehavioralRoundAnswer(state, "1", "q1-strong", now = 1L)
        state = engine.submitBehavioralRoundAnswer(state, "2", "q2-strong", now = 2L)
        state = engine.submitBehavioralRoundAnswer(state, "3", "q3-strong", now = 3L)
        assertEquals(listOf("q0", "q1", "q2", "q3"), state.recentlySolved.map { it.contentId })

        state = engine.submitBehavioralRoundAnswer(state, "4", "q4-weak", now = 4L)

        // most recently solved (q1, q2, q3) spliced into the next three slots (positions 5, 6, 7)
        assertEquals(listOf("q0"), state.recentlySolved.map { it.contentId })
        val round = state.currentRound!!
        assertEquals("q1", round.questions[5].contentId)
        assertEquals("q2", round.questions[6].contentId)
        assertEquals("q3", round.questions[7].contentId)
        assertTrue(round.questions[5].isReplay)
        assertTrue(round.questions[6].isReplay)
        assertTrue(round.questions[7].isReplay)
        assertEquals("5", round.questions[5].instanceId)
        assertEquals("6", round.questions[6].instanceId)
        assertEquals("7", round.questions[7].instanceId)
        // untouched, still-planned slots are unaffected
        assertEquals("q8", round.questions[8].contentId)
        assertEquals("q9", round.questions[9].contentId)
        assertFalse(round.questions[8].isReplay)
        assertEquals(5, round.answers.size)
    }

    @Test
    fun `the replay splice caps at the number of remaining unanswered slots`() {
        var state = manualRoundState(InterviewRoundSurface.BEHAVIORAL)
        // answer 0..6 as STRONG, building up seven recently-solved entries
        for (i in 0..6) {
            state = engine.submitBehavioralRoundAnswer(state, i.toString(), "q$i-strong", now = i.toLong())
        }
        assertEquals(7, state.recentlySolved.size)
        // only two slots (8, 9) remain after answering slot 7
        state = engine.submitBehavioralRoundAnswer(state, "7", "q7-weak", now = 7L)
        val round = state.currentRound!!
        // spliceCount = min(3, 7, 2) = 2
        assertEquals(5, state.recentlySolved.size)
        assertTrue(round.questions[8].isReplay)
        assertTrue(round.questions[9].isReplay)
    }

    @Test
    fun `a spliced replay question can itself be answered WEAK and trigger a further splice`() {
        var state = manualRoundState(InterviewRoundSurface.BEHAVIORAL)
        for (i in 0..3) {
            state = engine.submitBehavioralRoundAnswer(state, i.toString(), "q$i-strong", now = i.toLong())
        }
        state = engine.submitBehavioralRoundAnswer(state, "4", "q4-weak", now = 4L) // splices q1,q2,q3 into 5,6,7
        assertEquals(listOf("q0"), state.recentlySolved.map { it.contentId })

        // answer the replayed slot 5 (now contentId q1) as WEAK too - only one solved entry (q0) remains to splice
        val replayedChoiceId = state.currentRound!!.questions[5].choices.first { it.rating == ChoiceRating.WEAK }.id
        state = engine.submitBehavioralRoundAnswer(state, "5", replayedChoiceId, now = 5L)
        assertTrue(state.recentlySolved.isEmpty())
        assertTrue(state.currentRound!!.questions[6].isReplay || state.currentRound!!.questions[6].contentId == "q0")
    }

    @Test
    fun `submitTechCommRoundAnswer never triggers a replay splice`() {
        var state = manualRoundState(InterviewRoundSurface.TECH_COMM)
        for (i in 0..2) {
            state = engine.submitTechCommRoundAnswer(state, i.toString(), "q$i-strong", now = i.toLong())
        }
        val before = state.currentRound!!.questions.toList()
        state = engine.submitTechCommRoundAnswer(state, "3", "q3-weak", now = 3L)
        assertEquals(before, state.currentRound!!.questions)
        assertTrue(state.recentlySolved.isEmpty())
    }

    // ------------------------------------------------------------ finishBehavioralRound / finishTechCommRound

    @Test
    fun `finishBehavioralRound is a no-op until every question is answered`() {
        var state = manualRoundState(InterviewRoundSurface.BEHAVIORAL)
        state = engine.submitBehavioralRoundAnswer(state, "0", "q0-strong", now = 0L)
        val result = engine.finishBehavioralRound(state, now = 100L)
        assertEquals(state, result)
    }

    @Test
    fun `finishBehavioralRound is a no-op outside ROUND_ACTIVE`() {
        val ready = freshBehavioralState(BehavioralCategory.LEADERSHIP)
        val result = engine.finishBehavioralRound(ready, now = 0L)
        assertEquals(ready, result)
    }

    @Test
    fun `a perfect round advances difficulty and resets the sub-eight streak`() {
        var state = manualRoundState(InterviewRoundSurface.BEHAVIORAL, PracticeDifficulty.BEGINNER)
        for (i in 0..9) {
            state = engine.submitBehavioralRoundAnswer(state, i.toString(), "q$i-strong", now = i.toLong())
        }
        val finished = engine.finishBehavioralRound(state, now = 50L)
        assertEquals(AdaptivePracticePhase.ROUND_RESULTS, finished.phase)
        assertEquals(PracticeDifficulty.DEVELOPING, finished.difficulty)
        assertEquals(0, finished.consecutiveBelowEight)
        assertEquals(1, finished.completedRounds)
        assertNotNull(finished.currentRound)
        assertEquals(50L, finished.currentRound!!.completedAt)
    }

    @Test
    fun `two consecutive sub-eight rounds regress difficulty`() {
        fun weakRoundState(difficulty: PracticeDifficulty, consecutiveBelowEight: Int): InterviewRoundState {
            val questions = (0 until 10).map { manualQuestion(it) }
            val answers = (0 until 10).map { i ->
                // 5 non-weak, 5 weak -> score 5, below the 8-10 advance range
                InterviewRoundAnswer(i.toString(), "q$i-" + (if (i < 5) "strong" else "weak"), if (i < 5) ChoiceRating.STRONG else ChoiceRating.WEAK)
            }
            return InterviewRoundState(
                id = "r", userId = "u1", surface = InterviewRoundSurface.BEHAVIORAL, category = BehavioralCategory.LEADERSHIP,
                difficulty = difficulty, phase = AdaptivePracticePhase.ROUND_ACTIVE, consecutiveBelowEight = consecutiveBelowEight,
                currentRound = InterviewRound(difficulty, questions, answers, startedAt = 0L), startedAt = 0L,
            )
        }

        val firstSubEight = engine.finishBehavioralRound(weakRoundState(PracticeDifficulty.DEVELOPING, consecutiveBelowEight = 0), now = 0L)
        assertEquals(PracticeDifficulty.DEVELOPING, firstSubEight.difficulty)
        assertEquals(1, firstSubEight.consecutiveBelowEight)

        val secondSubEight = engine.finishBehavioralRound(weakRoundState(PracticeDifficulty.DEVELOPING, consecutiveBelowEight = 1), now = 0L)
        assertEquals(PracticeDifficulty.BEGINNER, secondSubEight.difficulty)
        assertEquals(0, secondSubEight.consecutiveBelowEight)
    }

    @Test
    fun `finishTechCommRound uses the same difficulty state machine`() {
        var state = manualRoundState(InterviewRoundSurface.TECH_COMM)
        for (i in 0..9) {
            state = engine.submitTechCommRoundAnswer(state, i.toString(), "q$i-strong", now = i.toLong())
        }
        val finished = engine.finishTechCommRound(state, now = 10L)
        assertEquals(PracticeDifficulty.DEVELOPING, finished.difficulty)
        assertEquals(AdaptivePracticePhase.ROUND_RESULTS, finished.phase)
    }

    // ------------------------------------------------------------ progress bookkeeping

    @Test
    fun `applyBehavioralRoundToProgress tallies every touched category and the dimension breakdown`() {
        val questions = listOf(
            manualQuestion(0, BehavioralCategory.LEADERSHIP, StarStage.SITUATION),
            manualQuestion(1, BehavioralCategory.CONFLICT, StarStage.TASK),
        )
        val answers = listOf(
            InterviewRoundAnswer("0", "q0-strong", ChoiceRating.STRONG),
            InterviewRoundAnswer("1", "q1-weak", ChoiceRating.WEAK),
        )
        val state = InterviewRoundState(
            id = "r", userId = "u1", surface = InterviewRoundSurface.BEHAVIORAL, category = null,
            difficulty = PracticeDifficulty.DEVELOPING, phase = AdaptivePracticePhase.ROUND_RESULTS, consecutiveBelowEight = 1,
            currentRound = InterviewRound(PracticeDifficulty.BEGINNER, questions, answers, startedAt = 0L, completedAt = 10L), startedAt = 0L,
        )
        val progress = engine.applyBehavioralRoundToProgress(null, "u1", state, now = 10L)

        assertEquals(1, progress.categoryProgress.getValue(BehavioralCategory.LEADERSHIP).completedCount)
        assertEquals(1, progress.categoryProgress.getValue(BehavioralCategory.LEADERSHIP).strongCount)
        assertEquals(1, progress.categoryProgress.getValue(BehavioralCategory.CONFLICT).weakCount)
        assertEquals(1, progress.starWorkoutsCompleted)
        assertEquals(1, progress.dimensionStrongCounts[StarStage.SITUATION.dimension])
        assertEquals(1, progress.dimensionWeakCounts[StarStage.TASK.dimension])
        assertEquals(PracticeDifficulty.DEVELOPING, progress.behavioralDifficulty)
        assertEquals(1, progress.behavioralConsecutiveBelowTarget)
    }

    @Test
    fun `applyTechCommRoundToProgress increments its own completion counter and difficulty ladder`() {
        val state = manualRoundState(InterviewRoundSurface.TECH_COMM, PracticeDifficulty.DEVELOPING).copy(consecutiveBelowEight = 1)
        val progress = engine.applyTechCommRoundToProgress(null, "u1", state, now = 5L)
        assertEquals(1, progress.techCommCompleted)
        assertEquals(0, progress.starWorkoutsCompleted)
        assertEquals(PracticeDifficulty.DEVELOPING, progress.techCommDifficulty)
        assertEquals(1, progress.techCommConsecutiveBelowTarget)
    }

    // ------------------------------------------------------------ mock interview (unchanged flat flow)

    private val mock = MockInterview(
        id = "mock-fixture",
        title = "Fixture Mock",
        setup = "setup",
        decisions = (1..5).map { n ->
            MockInterviewDecision(
                id = "mock-d$n",
                order = n,
                prompt = "prompt-$n",
                choices = listOf(choice("mock-d$n-strong", ChoiceRating.STRONG), choice("mock-d$n-weak", ChoiceRating.WEAK)),
            )
        },
    )

    @Test
    fun `a mock interview is complete after five decisions`() {
        var session = engine.startMockInterview(mock, Random(7))
        assertFalse(session.isOver)
        while (!session.isOver) {
            val decision = session.currentDecision!!
            session = engine.submitMockAnswer(session, decision.choices.first().id)
        }
        assertTrue(session.isOver)
        val result = engine.finishMockInterview(session, now = 500L)
        assertNotNull(result)
        assertEquals(5, result.answers.size)
    }

    @Test
    fun `submitMockAnswer rejects an unknown choice id`() {
        val session = engine.startMockInterview(mock, Random(8))
        val updated = engine.submitMockAnswer(session, "not-a-real-choice")
        assertEquals(session, updated)
    }

    @Test
    fun `finishMockInterview is a no-op until every decision is answered`() {
        val session = engine.startMockInterview(mock, Random(9))
        assertNull(engine.finishMockInterview(session, now = 0L))
    }

    // ------------------------------------------------------------ pickMockInterview

    private fun mockAt(difficulty: PracticeDifficulty) = mock.copy(id = "mock-${difficulty.name}", difficulty = difficulty)

    @Test
    fun `pickMockInterview returns the exact tier match when available`() {
        val catalog = listOf(mockAt(PracticeDifficulty.BEGINNER), mockAt(PracticeDifficulty.DEVELOPING))
        val progress = InterviewProgress(userId = "u1", mockDifficulty = PracticeDifficulty.DEVELOPING)
        val picked = engine.pickMockInterview(progress, catalog)
        assertEquals("mock-DEVELOPING", picked?.id)
    }

    @Test
    fun `pickMockInterview falls back to the nearest tier when the exact one isn't authored`() {
        val catalog = listOf(mockAt(PracticeDifficulty.BEGINNER), mockAt(PracticeDifficulty.ADVANCED))
        val progress = InterviewProgress(userId = "u1", mockDifficulty = PracticeDifficulty.INTERMEDIATE)
        val picked = engine.pickMockInterview(progress, catalog)
        assertNotNull(picked)
        assertTrue(picked.difficulty == PracticeDifficulty.BEGINNER || picked.difficulty == PracticeDifficulty.ADVANCED)
    }

    @Test
    fun `pickMockInterview returns null for an empty catalog`() {
        assertNull(engine.pickMockInterview(null, emptyList()))
    }

    @Test
    fun `pickMockInterview defaults to BEGINNER when progress is null`() {
        val catalog = listOf(mockAt(PracticeDifficulty.BEGINNER), mockAt(PracticeDifficulty.ADVANCED))
        assertEquals("mock-BEGINNER", engine.pickMockInterview(null, catalog)?.id)
    }

    // ------------------------------------------------------------ recordMockResult

    private fun mockResult(strong: Int, weak: Int): MockInterviewResult {
        val reasonable = 5 - strong - weak
        val ratings = List(strong) { ChoiceRating.STRONG } + List(weak) { ChoiceRating.WEAK } + List(reasonable) { ChoiceRating.REASONABLE }
        return MockInterviewResult(
            mockId = mock.id,
            answers = ratings.mapIndexed { i, rating -> com.codingarena.domain.model.MockInterviewAnswer("mock-d${i + 1}", "choice-$i", rating) },
            completedAt = 0L,
        )
    }

    @Test
    fun `a STRONG mock result advances difficulty and resets the weak streak`() {
        val progress = InterviewProgress(userId = "u1", mockDifficulty = PracticeDifficulty.BEGINNER, mockConsecutiveWeak = 1)
        val updated = engine.recordMockResult(progress, "u1", mockResult(strong = 4, weak = 0), now = 0L)
        assertEquals(PracticeDifficulty.DEVELOPING, updated.mockDifficulty)
        assertEquals(0, updated.mockConsecutiveWeak)
        assertEquals(1, updated.mockInterviewsCompleted)
    }

    @Test
    fun `two consecutive WEAK mock results regress difficulty`() {
        val afterFirst = engine.recordMockResult(
            InterviewProgress(userId = "u1", mockDifficulty = PracticeDifficulty.DEVELOPING),
            "u1", mockResult(strong = 0, weak = 3), now = 0L,
        )
        assertEquals(PracticeDifficulty.DEVELOPING, afterFirst.mockDifficulty)
        assertEquals(1, afterFirst.mockConsecutiveWeak)

        val afterSecond = engine.recordMockResult(afterFirst, "u1", mockResult(strong = 0, weak = 3), now = 0L)
        assertEquals(PracticeDifficulty.BEGINNER, afterSecond.mockDifficulty)
        assertEquals(0, afterSecond.mockConsecutiveWeak)
    }

    @Test
    fun `a REASONABLE mock result resets the weak streak without changing difficulty`() {
        val progress = InterviewProgress(userId = "u1", mockDifficulty = PracticeDifficulty.DEVELOPING, mockConsecutiveWeak = 1)
        val updated = engine.recordMockResult(progress, "u1", mockResult(strong = 1, weak = 1), now = 0L)
        assertEquals(PracticeDifficulty.DEVELOPING, updated.mockDifficulty)
        assertEquals(0, updated.mockConsecutiveWeak)
    }

    // ------------------------------------------------------------ recommended today / recordBehavioralAnswer

    @Test
    fun `recommended today picks the least-practiced category`() {
        val progress = InterviewProgress(
            userId = "u1",
            categoryProgress = BehavioralCategory.entries.associateWith {
                com.codingarena.domain.model.CategoryProgress(it, completedCount = 5)
            } + (BehavioralCategory.AMBIGUITY to com.codingarena.domain.model.CategoryProgress(BehavioralCategory.AMBIGUITY, completedCount = 0)),
        )
        val recommendation = engine.recommendedToday(progress, epochDay = 42L)
        assertEquals(BehavioralCategory.AMBIGUITY, recommendation.category)
    }

    @Test
    fun `recommended today is stable for the same epoch day`() {
        val first = engine.recommendedToday(null, epochDay = 100L)
        val second = engine.recommendedToday(null, epochDay = 100L)
        assertEquals(first, second)
    }

    @Test
    fun `recording a behavioral answer tallies the category and rating`() {
        val progress = engine.recordBehavioralAnswer(null, "u1", BehavioralCategory.TEAMWORK, ChoiceRating.STRONG, now = 10L)
        val category = progress.categoryProgress.getValue(BehavioralCategory.TEAMWORK)
        assertEquals(1, category.completedCount)
        assertEquals(1, category.strongCount)
        assertEquals(0, category.weakCount)
    }
}
