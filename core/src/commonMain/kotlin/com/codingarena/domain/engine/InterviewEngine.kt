package com.codingarena.domain.engine

import com.codingarena.domain.model.AdaptivePracticePhase
import com.codingarena.domain.model.BehavioralCategory
import com.codingarena.domain.model.BehavioralExercise
import com.codingarena.domain.model.CategoryProgress
import com.codingarena.domain.model.ChoiceRating
import com.codingarena.domain.model.InterviewExerciseMode
import com.codingarena.domain.model.InterviewProgress
import com.codingarena.domain.model.InterviewRound
import com.codingarena.domain.model.InterviewRoundAnswer
import com.codingarena.domain.model.InterviewRoundQuestion
import com.codingarena.domain.model.InterviewRoundState
import com.codingarena.domain.model.InterviewRoundSurface
import com.codingarena.domain.model.MockInterview
import com.codingarena.domain.model.MockInterviewAnswer
import com.codingarena.domain.model.MockInterviewResult
import com.codingarena.domain.model.MockInterviewSession
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.RatedChoice
import com.codingarena.domain.model.StarStage
import com.codingarena.domain.model.StarStep
import com.codingarena.domain.model.StarWorkout
import com.codingarena.domain.model.TechCommExerciseKind
import com.codingarena.domain.model.TechCommItem
import com.codingarena.domain.model.searchOrder
import kotlin.random.Random

/** What "Recommended Today" points at - always a STAR-anchored round, the mode guaranteed to exist for every category. */
data class RecommendedInterviewToday(
    val category: BehavioralCategory,
    val mode: InterviewExerciseMode = InterviewExerciseMode.BUILD_STAR_ANSWER,
)

/**
 * Adaptive rounds for Behavioral Workouts and Technical Communication, a
 * lighter difficulty ladder for Mock Interview, and Interview progress
 * bookkeeping.
 *
 * Behavioral and Tech Comm both round-trip through [InterviewRoundState] -
 * one state shape, two entry points (`category` vs `problemSlug`), mirroring
 * how [AdaptivePracticeState][com.codingarena.domain.model.AdaptivePracticeState]
 * already spans several Practice modes via `mode`+`topic`. Mock Interview
 * stays a single continuous narrative (forcing a ten-question round onto one
 * five-decision story doesn't fit) - it gets [pickMockInterview] and a
 * [recordMockResult] state machine instead.
 *
 * Pure: the caller owns the clock, ids, and [Random], so every run is
 * reproducible in tests. Transition methods are safe no-ops - returning the
 * input unchanged - when called with invalid input or from the wrong phase,
 * matching [PracticeWorkoutEngine]'s convention.
 */
class InterviewEngine {

    /** The chosen [RatedChoice], or null if [choiceId] doesn't match any of [choices]. */
    fun grade(choices: List<RatedChoice>, choiceId: String): RatedChoice? =
        choices.firstOrNull { it.id == choiceId }

    /**
     * Content is authored in a consistent Strong/Reasonable/Weak order for
     * readability; shuffling at presentation time - rather than hand-varying
     * every authored list - is what keeps the strongest choice from always
     * landing in the same slot. Answer-safe by construction: [RatedChoice.rating]
     * travels on the choice itself, exactly like [WorkoutChoice.correct][com.codingarena.domain.model.WorkoutChoice.correct]
     * in the Practice engine, so shuffling can never desync grading.
     */
    fun shuffled(choices: List<RatedChoice>, random: Random): List<RatedChoice> = choices.shuffled(random)

    /**
     * Today's dominant recommendation: the least-practiced category (ties
     * broken by [epochDay] so the pick is stable within a day but rotates
     * day to day).
     */
    fun recommendedToday(
        progress: InterviewProgress?,
        epochDay: Long,
        categories: List<BehavioralCategory> = BehavioralCategory.entries,
    ): RecommendedInterviewToday {
        val counts = categories.associateWith { progress?.categoryProgress?.get(it)?.completedCount ?: 0 }
        val minCount = counts.values.minOrNull() ?: 0
        val leastPracticed = categories.filter { counts[it] == minCount }
        val index = ((epochDay % leastPracticed.size) + leastPracticed.size).toInt() % leastPracticed.size
        return RecommendedInterviewToday(leastPracticed[index])
    }

    // ---------------------------------------------------------- Behavioral Workouts rounds

    /**
     * Assembles a new ten-question round onto [state] and moves it to
     * [AdaptivePracticePhase.ROUND_ACTIVE]. Guard: only valid from
     * [AdaptivePracticePhase.READY] or [AdaptivePracticePhase.ROUND_RESULTS].
     *
     * One slot per turn, round-robining across [state.category]'s categories
     * (just one if set, all nine if combined): each turn tries, in order, the
     * next not-yet-used-this-round STAR stage, then the next not-yet-used
     * exercise mode, then a least-recently-shown repeat from that category's
     * entire pool at any tier - exactly Practice's own thin-pool guarantee,
     * narrowed because Behavioral content is one-per-slot rather than
     * many-per-slot, so a numeric quota has nothing to balance.
     */
    fun startBehavioralRound(
        state: InterviewRoundState,
        starWorkouts: List<StarWorkout>,
        exercises: List<BehavioralExercise>,
        now: Long,
        random: Random = Random.Default,
    ): InterviewRoundState {
        if (state.surface != InterviewRoundSurface.BEHAVIORAL) return state
        if (state.phase == AdaptivePracticePhase.ROUND_ACTIVE) return state

        val categories = state.category?.let { listOf(it) } ?: BehavioralCategory.entries.toList()
        val roundNumber = state.completedRounds + 1
        var shown = state.contentLastShownAtRound
        val usedThisRound = mutableSetOf<String>()
        val placed = mutableListOf<InterviewRoundQuestion>()

        var turn = 0
        while (placed.size < TOTAL_ROUND_SIZE && turn < categories.size * TOTAL_ROUND_SIZE * 2) {
            val category = categories[turn % categories.size]
            turn++
            val question = pickBehavioralQuestion(category, state.difficulty, starWorkouts, exercises, usedThisRound, shown, random)
                ?: continue
            placed += question
            usedThisRound += question.contentId
            shown = shown + (question.contentId to roundNumber)
        }

        if (placed.isEmpty()) return state
        val finalQuestions = placed.shuffled(random).mapIndexed { index, q -> q.copy(instanceId = index.toString()) }
        return state.copy(
            phase = AdaptivePracticePhase.ROUND_ACTIVE,
            currentRound = InterviewRound(targetDifficulty = state.difficulty, questions = finalQuestions, startedAt = now),
            contentLastShownAtRound = shown,
        )
    }

    /**
     * Records an answer against the current round. Guards: only during
     * [AdaptivePracticePhase.ROUND_ACTIVE]; rejects an already-answered
     * [instanceId] or an unresolvable [choiceId].
     *
     * A non-[ChoiceRating.WEAK] answer is remembered in [InterviewRoundState.recentlySolved]
     * (most recent last, capped). A [ChoiceRating.WEAK] answer splices up to
     * three of the most recently solved questions into the round's very next
     * not-yet-answered slots - replacing them, not growing the round - before
     * the originally planned questions resume; the displaced originals are
     * dropped, and the splice sources are removed from [InterviewRoundState.recentlySolved]
     * so they aren't reused again until re-solved. Zero available replays is
     * a safe no-op. Every re-stamped `instanceId` is the slot's own position,
     * so collisions are structurally impossible.
     */
    fun submitBehavioralRoundAnswer(
        state: InterviewRoundState,
        instanceId: String,
        choiceId: String,
        now: Long,
    ): InterviewRoundState {
        if (state.phase != AdaptivePracticePhase.ROUND_ACTIVE) return state
        val round = state.currentRound ?: return state
        if (round.answers.any { it.instanceId == instanceId }) return state
        val question = round.questions.firstOrNull { it.instanceId == instanceId } ?: return state
        val choice = grade(question.choices, choiceId) ?: return state

        val answer = InterviewRoundAnswer(instanceId, choice.id, choice.rating)
        var questions = round.questions
        var recentlySolved = state.recentlySolved

        if (choice.rating != ChoiceRating.WEAK) {
            recentlySolved = (recentlySolved + question).takeLast(RECENTLY_SOLVED_CAP)
        } else {
            val spliceStart = round.answers.size + 1
            val remainingSlots = (questions.size - spliceStart).coerceAtLeast(0)
            val spliceCount = minOf(MAX_REPLAY_SPLICE, recentlySolved.size, remainingSlots)
            if (spliceCount > 0) {
                val toSplice = recentlySolved.takeLast(spliceCount)
                recentlySolved = recentlySolved.dropLast(spliceCount)
                val mutableQuestions = questions.toMutableList()
                for (i in 0 until spliceCount) {
                    val pos = spliceStart + i
                    mutableQuestions[pos] = toSplice[i].copy(isReplay = true, instanceId = pos.toString())
                }
                questions = mutableQuestions
            }
        }

        return state.copy(
            currentRound = round.copy(questions = questions, answers = round.answers + answer),
            recentlySolved = recentlySolved,
        )
    }

    /** Guard: only once every question has an answer. Difficulty state machine identical to [PracticeWorkoutEngine.finishRound]. */
    fun finishBehavioralRound(state: InterviewRoundState, now: Long): InterviewRoundState = finishRound(state, now)

    // ---------------------------------------------------------- Technical Communication rounds

    /** Same shape as [startBehavioralRound], sourced from one problem's [TechCommItem]s instead of a category's content. */
    fun startTechCommRound(
        state: InterviewRoundState,
        problemSlug: String,
        items: List<TechCommItem>,
        now: Long,
        random: Random = Random.Default,
    ): InterviewRoundState {
        if (state.surface != InterviewRoundSurface.TECH_COMM) return state
        if (state.phase == AdaptivePracticePhase.ROUND_ACTIVE) return state

        val roundNumber = state.completedRounds + 1
        var shown = state.contentLastShownAtRound
        val usedThisRound = mutableSetOf<String>()
        val placed = mutableListOf<InterviewRoundQuestion>()

        while (placed.size < TOTAL_ROUND_SIZE) {
            val question = pickTechCommQuestion(problemSlug, state.difficulty, items, usedThisRound, shown, random) ?: break
            placed += question
            usedThisRound += question.contentId
            shown = shown + (question.contentId to roundNumber)
        }

        if (placed.isEmpty()) return state
        val finalQuestions = placed.shuffled(random).mapIndexed { index, q -> q.copy(instanceId = index.toString()) }
        return state.copy(
            phase = AdaptivePracticePhase.ROUND_ACTIVE,
            problemSlug = problemSlug,
            currentRound = InterviewRound(targetDifficulty = state.difficulty, questions = finalQuestions, startedAt = now),
            contentLastShownAtRound = shown,
        )
    }

    /** Same guards as [submitBehavioralRoundAnswer] - never splices; Tech Comm's pool is too thin for the replay beat to add anything. */
    fun submitTechCommRoundAnswer(state: InterviewRoundState, instanceId: String, choiceId: String, now: Long): InterviewRoundState {
        if (state.phase != AdaptivePracticePhase.ROUND_ACTIVE) return state
        val round = state.currentRound ?: return state
        if (round.answers.any { it.instanceId == instanceId }) return state
        val question = round.questions.firstOrNull { it.instanceId == instanceId } ?: return state
        val choice = grade(question.choices, choiceId) ?: return state
        val answer = InterviewRoundAnswer(instanceId, choice.id, choice.rating)
        return state.copy(currentRound = round.copy(answers = round.answers + answer))
    }

    fun finishTechCommRound(state: InterviewRoundState, now: Long): InterviewRoundState = finishRound(state, now)

    private fun finishRound(state: InterviewRoundState, now: Long): InterviewRoundState {
        if (state.phase != AdaptivePracticePhase.ROUND_ACTIVE) return state
        val round = state.currentRound ?: return state
        if (!round.isFullyAnswered) return state

        var difficulty = state.difficulty
        var consecutiveBelowEight = state.consecutiveBelowEight
        if (round.score in ADVANCE_SCORE_RANGE) {
            difficulty = difficulty.next()
            consecutiveBelowEight = 0
        } else {
            consecutiveBelowEight += 1
            if (consecutiveBelowEight >= CONSECUTIVE_BELOW_TARGET_TO_REGRESS) {
                difficulty = difficulty.previous()
                consecutiveBelowEight = 0
            }
        }

        return state.copy(
            phase = AdaptivePracticePhase.ROUND_RESULTS,
            difficulty = difficulty,
            consecutiveBelowEight = consecutiveBelowEight,
            completedRounds = state.completedRounds + 1,
            currentRound = round.copy(completedAt = now),
        )
    }

    // ---------------------------------------------------------- round assembly helpers

    private fun pickBehavioralQuestion(
        category: BehavioralCategory,
        difficulty: PracticeDifficulty,
        starWorkouts: List<StarWorkout>,
        exercises: List<BehavioralExercise>,
        usedThisRound: Set<String>,
        shown: Map<String, Int>,
        random: Random,
    ): InterviewRoundQuestion? {
        val workout = starWorkoutFor(category, difficulty, starWorkouts)
        if (workout != null) {
            val nextStage = StarStage.ORDERED.firstOrNull { stage ->
                workout.steps.any { it.stage == stage } && "${workout.id}:${stage.name}" !in usedThisRound
            }
            if (nextStage != null) {
                val step = workout.steps.first { it.stage == nextStage }
                return starRoundQuestion(category, workout, step, random)
            }
        }
        for (mode in EXERCISE_MODE_ORDER) {
            val candidates = behavioralExercisesFor(category, mode, difficulty, exercises).filter { it.id !in usedThisRound }
            val exercise = candidates.firstOrNull() ?: continue
            return exerciseRoundQuestion(exercise, random)
        }
        val everything = allBehavioralQuestions(category, starWorkouts, exercises, random)
        if (everything.isEmpty()) return null
        val pool = everything.filterNot { it.contentId in usedThisRound }.ifEmpty { everything }
        return pool.minByOrNull { shown[it.contentId] ?: -1 }
    }

    private fun pickTechCommQuestion(
        problemSlug: String,
        difficulty: PracticeDifficulty,
        items: List<TechCommItem>,
        usedThisRound: Set<String>,
        shown: Map<String, Int>,
        random: Random,
    ): InterviewRoundQuestion? {
        for (kind in TechCommExerciseKind.entries) {
            val candidates = techCommItemsFor(problemSlug, difficulty, items, kind).filter { it.id !in usedThisRound }
            val item = candidates.firstOrNull() ?: continue
            return techCommRoundQuestion(item, random)
        }
        val everything = items.filter { it.problemSlug == problemSlug }.map { techCommRoundQuestion(it, random) }
        if (everything.isEmpty()) return null
        val pool = everything.filterNot { it.contentId in usedThisRound }.ifEmpty { everything }
        return pool.minByOrNull { shown[it.contentId] ?: -1 }
    }

    private fun starWorkoutFor(category: BehavioralCategory, difficulty: PracticeDifficulty, starWorkouts: List<StarWorkout>): StarWorkout? =
        difficulty.searchOrder().firstNotNullOfOrNull { tier -> starWorkouts.firstOrNull { it.category == category && it.difficulty == tier } }

    private fun behavioralExercisesFor(
        category: BehavioralCategory,
        mode: InterviewExerciseMode,
        difficulty: PracticeDifficulty,
        exercises: List<BehavioralExercise>,
    ): List<BehavioralExercise> =
        difficulty.searchOrder().firstNotNullOfOrNull { tier ->
            exercises.filter { it.category == category && it.mode == mode && it.difficulty == tier }.ifEmpty { null }
        }.orEmpty()

    private fun techCommItemsFor(
        problemSlug: String,
        difficulty: PracticeDifficulty,
        items: List<TechCommItem>,
        kind: TechCommExerciseKind,
    ): List<TechCommItem> =
        difficulty.searchOrder().firstNotNullOfOrNull { tier ->
            items.filter { it.problemSlug == problemSlug && it.kind == kind && it.difficulty == tier }.ifEmpty { null }
        }.orEmpty()

    private fun allBehavioralQuestions(
        category: BehavioralCategory,
        starWorkouts: List<StarWorkout>,
        exercises: List<BehavioralExercise>,
        random: Random,
    ): List<InterviewRoundQuestion> =
        starWorkouts.filter { it.category == category }.flatMap { workout ->
            workout.steps.map { step -> starRoundQuestion(category, workout, step, random) }
        } + exercises.filter { it.category == category }.map { exerciseRoundQuestion(it, random) }

    private fun starRoundQuestion(category: BehavioralCategory, workout: StarWorkout, step: StarStep, random: Random) =
        InterviewRoundQuestion(
            instanceId = "",
            contentId = "${workout.id}:${step.stage.name}",
            category = category,
            commonQuestion = workout.commonQuestion,
            scenario = workout.scenario,
            referenceAnswer = null,
            prompt = step.prompt,
            choices = shuffled(step.choices, random),
            stage = step.stage,
        )

    private fun exerciseRoundQuestion(exercise: BehavioralExercise, random: Random) =
        InterviewRoundQuestion(
            instanceId = "",
            contentId = exercise.id,
            category = exercise.category,
            commonQuestion = exercise.commonQuestion,
            scenario = exercise.scenario,
            referenceAnswer = exercise.referenceAnswer,
            prompt = exercise.prompt,
            choices = shuffled(exercise.choices, random),
            stage = null,
        )

    private fun techCommRoundQuestion(item: TechCommItem, random: Random) =
        InterviewRoundQuestion(
            instanceId = "",
            contentId = item.id,
            problemSlug = item.problemSlug,
            techCommKind = item.kind,
            commonQuestion = null,
            scenario = null,
            referenceAnswer = null,
            prompt = item.prompt,
            choices = shuffled(item.choices, random),
            stage = null,
        )

    // ---------------------------------------------------------- mock interview

    /** Every decision's choices are shuffled once, up front, for this run - see [shuffled]. */
    fun startMockInterview(mock: MockInterview, random: Random = Random.Default): MockInterviewSession {
        val shuffledDecisions = mock.decisions.map { it.copy(choices = shuffled(it.choices, random)) }
        return MockInterviewSession(mock.copy(decisions = shuffledDecisions))
    }

    /** Guard: only valid while the session has an unanswered decision and [choiceId] resolves. */
    fun submitMockAnswer(session: MockInterviewSession, choiceId: String): MockInterviewSession {
        val decision = session.currentDecision ?: return session
        val choice = decision.choices.firstOrNull { it.id == choiceId } ?: return session
        return session.copy(
            answers = session.answers + MockInterviewAnswer(decision.id, choice.id, choice.rating),
        )
    }

    /** Guard: only once every decision has an answer. */
    fun finishMockInterview(session: MockInterviewSession, now: Long): MockInterviewResult? {
        if (!session.isOver) return null
        return MockInterviewResult(session.mock.id, session.answers, now)
    }

    /** The catalog's tier match for [InterviewProgress.mockDifficulty], falling back through [PracticeDifficulty.searchOrder]. Null only if [catalog] is empty. */
    fun pickMockInterview(progress: InterviewProgress?, catalog: List<MockInterview>): MockInterview? {
        val target = progress?.mockDifficulty ?: PracticeDifficulty.BEGINNER
        for (tier in target.searchOrder()) {
            val atTier = catalog.filter { it.difficulty == tier }
            if (atTier.isNotEmpty()) return atTier.first()
        }
        return null
    }

    // ---------------------------------------------------------- progress bookkeeping

    /** Folds one single-decision behavioral answer into [progress]. */
    fun recordBehavioralAnswer(
        progress: InterviewProgress?,
        userId: String,
        category: BehavioralCategory,
        rating: ChoiceRating,
        now: Long,
    ): InterviewProgress {
        val current = progress ?: InterviewProgress(userId = userId)
        val existing = current.categoryProgress[category] ?: CategoryProgress(category)
        val updated = existing.copy(
            completedCount = existing.completedCount + 1,
            strongCount = existing.strongCount + if (rating == ChoiceRating.STRONG) 1 else 0,
            weakCount = existing.weakCount + if (rating == ChoiceRating.WEAK) 1 else 0,
            lastCompletedAt = now,
        )
        return current.copy(
            categoryProgress = current.categoryProgress + (category to updated),
            lastCompletedAt = now,
        )
    }

    /**
     * Folds a finished Behavioral round into [progress]: one [recordBehavioralAnswer]
     * per answer (so each touched category's tally stays accurate even in a
     * combined round spanning several categories), a per-dimension tally from
     * the round's STAR-derived answers, and the round's own difficulty ladder.
     */
    fun applyBehavioralRoundToProgress(
        progress: InterviewProgress?,
        userId: String,
        state: InterviewRoundState,
        now: Long,
    ): InterviewProgress {
        val round = state.currentRound ?: return progress ?: InterviewProgress(userId = userId)
        var current = progress ?: InterviewProgress(userId = userId)

        for (answer in round.answers) {
            val question = round.questions.firstOrNull { it.instanceId == answer.instanceId } ?: continue
            val category = question.category ?: continue
            current = recordBehavioralAnswer(current, userId, category, answer.rating, now)
        }

        var strongCounts = current.dimensionStrongCounts
        var weakCounts = current.dimensionWeakCounts
        for (score in round.dimensionBreakdown) {
            when (score.rating) {
                ChoiceRating.STRONG -> strongCounts = strongCounts + (score.dimension to (strongCounts[score.dimension] ?: 0) + 1)
                ChoiceRating.WEAK -> weakCounts = weakCounts + (score.dimension to (weakCounts[score.dimension] ?: 0) + 1)
                ChoiceRating.REASONABLE -> Unit
            }
        }

        return current.copy(
            starWorkoutsCompleted = current.starWorkoutsCompleted + 1,
            dimensionStrongCounts = strongCounts,
            dimensionWeakCounts = weakCounts,
            behavioralDifficulty = state.difficulty,
            behavioralConsecutiveBelowTarget = state.consecutiveBelowEight,
        )
    }

    /** Folds a finished Tech Comm round into [progress]: a completion tally plus the round's own difficulty ladder. */
    fun applyTechCommRoundToProgress(
        progress: InterviewProgress?,
        userId: String,
        state: InterviewRoundState,
        now: Long,
    ): InterviewProgress {
        val current = progress ?: InterviewProgress(userId = userId)
        return current.copy(
            techCommCompleted = current.techCommCompleted + 1,
            lastCompletedAt = now,
            techCommDifficulty = state.difficulty,
            techCommConsecutiveBelowTarget = state.consecutiveBelowEight,
        )
    }

    fun recordMockResult(
        progress: InterviewProgress?,
        userId: String,
        result: MockInterviewResult,
        now: Long,
    ): InterviewProgress {
        val current = progress ?: InterviewProgress(userId = userId)
        var difficulty = current.mockDifficulty
        var consecutiveWeak = current.mockConsecutiveWeak
        when (result.overallRating) {
            ChoiceRating.STRONG -> {
                difficulty = difficulty.next()
                consecutiveWeak = 0
            }
            ChoiceRating.WEAK -> {
                consecutiveWeak += 1
                if (consecutiveWeak >= CONSECUTIVE_BELOW_TARGET_TO_REGRESS) {
                    difficulty = difficulty.previous()
                    consecutiveWeak = 0
                }
            }
            ChoiceRating.REASONABLE -> consecutiveWeak = 0
        }
        return current.copy(
            mockInterviewsCompleted = current.mockInterviewsCompleted + 1,
            mockDifficulty = difficulty,
            mockConsecutiveWeak = consecutiveWeak,
            lastCompletedAt = now,
        )
    }

    private companion object {
        const val TOTAL_ROUND_SIZE = 10
        const val RECENTLY_SOLVED_CAP = 30
        const val MAX_REPLAY_SPLICE = 3
        val ADVANCE_SCORE_RANGE = 8..10
        const val CONSECUTIVE_BELOW_TARGET_TO_REGRESS = 2
        val EXERCISE_MODE_ORDER = listOf(
            InterviewExerciseMode.IMPROVE_AN_ANSWER,
            InterviewExerciseMode.SPOT_RED_FLAG,
            InterviewExerciseMode.HANDLE_FOLLOWUPS,
        )
    }
}
