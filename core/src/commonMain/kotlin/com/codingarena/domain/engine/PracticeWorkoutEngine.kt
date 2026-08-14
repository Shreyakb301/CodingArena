package com.codingarena.domain.engine

import com.codingarena.content.ProblemWorkout
import com.codingarena.domain.model.AdaptivePracticeAnswer
import com.codingarena.domain.model.AdaptivePracticePhase
import com.codingarena.domain.model.AdaptivePracticeRound
import com.codingarena.domain.model.AdaptivePracticeState
import com.codingarena.domain.model.Curriculum
import com.codingarena.domain.model.CurriculumProgress
import com.codingarena.domain.model.MissedConcept
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.PracticeExperienceLevel
import com.codingarena.domain.model.PracticeSessionKind
import com.codingarena.domain.model.RecallRecord
import com.codingarena.domain.model.RoundQuestion
import com.codingarena.domain.model.WorkoutStep
import com.codingarena.domain.model.WorkoutStepKind
import com.codingarena.domain.model.searchOrder
import kotlin.random.Random

/**
 * Recommended / topic-focused / mixed adaptive Practice rounds.
 *
 * Deliberately independent of [BlitzEngine] - Blitz drills pattern-name
 * recall (four options, no code); this drills reasoning through a solution
 * one decision at a time. The only thing shared is the [PatternGroup]
 * taxonomy and the "current topic" rule, which mirrors what the Roadmap
 * screen already computes inline, so the two screens never disagree about
 * what "current" means.
 *
 * Pure: the caller owns the clock, the round ids, and the [Random] source,
 * so a run is fully reproducible in tests. Every transition method
 * (`startRound`, `submitRoundAnswer`, `finishRound`) is a safe no-op - it
 * returns the input state unchanged - when called from the wrong phase or
 * with invalid input.
 */
class PracticeWorkoutEngine {

    /**
     * How practiced a user is, from Roadmap coverage alone.
     *
     * No rating tier exists anywhere else in the app - [PlayerRatings] is a
     * continuous Elo, not a named level - so this is a new, deliberately
     * simple rule: how many [PatternGroup] sections are fully mastered.
     */
    fun experienceLevel(progress: CurriculumProgress?): PracticeExperienceLevel {
        val mastered = progress?.sectionProgress?.values?.count { it.fraction >= 1f } ?: 0
        return when {
            mastered <= BEGINNER_MAX_MASTERED -> PracticeExperienceLevel.BEGINNER
            mastered <= PROGRESSING_MAX_MASTERED -> PracticeExperienceLevel.PROGRESSING
            else -> PracticeExperienceLevel.ADVANCED
        }
    }

    /** The same rule `RoadmapScreen` computes inline: the first section not yet fully mastered. */
    fun currentTopic(curriculum: Curriculum, progress: CurriculumProgress?): PatternGroup? =
        curriculum.sections.firstOrNull { section ->
            (progress?.sectionProgress?.get(section.group)?.fraction ?: 0f) < 1f
        }?.group

    /**
     * Already-learned topics (excluding [excluding]), weakest first.
     *
     * Ranked by recall accuracy, then how long it has been since the topic
     * was last touched, then independent-solve rate - the three per-topic
     * signals that actually exist in the data today. Hints used and repeated
     * mistakes are not tracked per topic anywhere in the codebase yet
     * ([com.codingarena.domain.model.PracticeAttempt] has no topic field), so
     * they are not silently faked here; this ranking is structured so a
     * fourth weighted term can be folded in later without a redesign.
     */
    fun reviewTopics(
        curriculum: Curriculum,
        progress: CurriculumProgress?,
        records: Map<String, RecallRecord>,
        excluding: PatternGroup? = null,
    ): List<PatternGroup> {
        val learned = curriculum.sections
            .filter { section -> (progress?.sectionProgress?.get(section.group)?.seen ?: 0) > 0 }
            .map { it.group }
            .filter { it != excluding }

        fun accuracyFor(group: PatternGroup): Double {
            val slugs = curriculum.problems.filter { it.group == group }.map { it.slug }
            val accuracies = slugs.mapNotNull { records[it]?.takeIf { r -> r.totalSeen > 0 }?.accuracy }
            return if (accuracies.isEmpty()) 0.0 else accuracies.average()
        }

        fun lastSeenFor(group: PatternGroup): Long {
            val slugs = curriculum.problems.filter { it.group == group }.map { it.slug }
            return slugs.mapNotNull { records[it]?.lastSeenAt }.maxOrNull() ?: 0L
        }

        fun solvedFractionFor(group: PatternGroup): Float =
            progress?.sectionProgress?.get(group)?.let { section ->
                if (section.total == 0) 0f else section.solved.toFloat() / section.total
            } ?: 0f

        return learned.sortedWith(
            compareBy(
                { group -> accuracyFor(group) },
                { group -> lastSeenFor(group) },
                { group -> solvedFractionFor(group) },
            )
        )
    }

    /**
     * True only if every [PracticeDifficulty] tier has enough eligible steps
     * for [group] to cover the round quota (2/2/2/2/1/1 across the six round
     * categories). Used to gate topic selection everywhere a topic is
     * chosen for adaptive practice - an incomplete topic is never selected
     * at all, rather than selected and quietly served a lower tier than the
     * session's actual difficulty (which would make difficulty progression
     * dishonest).
     */
    fun hasCompleteQuestionBank(group: PatternGroup, workouts: List<ProblemWorkout>): Boolean {
        val stepsForGroup = workouts.filter { it.group == group }.flatMap { it.steps }
        return PracticeDifficulty.entries.all { difficulty ->
            val atTier = stepsForGroup.filter { it.difficulty == difficulty }
            RoundCategory.entries.all { category ->
                atTier.count { it.kind in category.kinds } >= category.target
            }
        }
    }

    // ---------------------------------------------------------- adaptive rounds

    /**
     * Assembles a new ten-question round onto [state] and moves it to
     * [AdaptivePracticePhase.ROUND_ACTIVE]. Guard: only valid from
     * [AdaptivePracticePhase.READY] or [AdaptivePracticePhase.ROUND_RESULTS] -
     * called from [AdaptivePracticePhase.ROUND_ACTIVE] it is a no-op, since a
     * round is already in progress.
     *
     * Review questions (up to 5, highest [MissedConcept.missCount] first) are
     * pinned to the exact `(group, conceptKey, missedAtDifficulty)` they were
     * missed at - never widened to a neighboring tier - preferring a
     * different [ProblemWorkout] than [MissedConcept.reviewedStepSlugs]
     * already used, falling back to repeating the exact missed step
     * (reshuffled) only once every variation is exhausted. The remaining
     * slots are filled with new questions honoring the round's type quota,
     * with reviews counted against their own category's target first (a
     * review-heavy round is accepted rather than forced back to the nominal
     * 2/2/2/2/1/1 split).
     *
     * For [PracticeSessionKind.RECOMMENDED], each new-question slot
     * independently rolls its topic via [PracticeExperienceLevel]'s mix
     * (Beginner 80/20 current/review, Progressing 50/30/20
     * current/weak-due/mixed, Advanced 20/30/50) - a single round can span
     * multiple topics. [PracticeSessionKind.TOPIC_FOCUSED] stays on
     * [AdaptivePracticeState.topic] for every slot. [PracticeSessionKind.MIXED]
     * rolls uniformly across every learned, complete topic per slot. Every
     * topic considered is filtered through [hasCompleteQuestionBank] first.
     */
    fun startRound(
        state: AdaptivePracticeState,
        curriculum: Curriculum,
        progress: CurriculumProgress?,
        records: Map<String, RecallRecord>,
        workouts: List<ProblemWorkout>,
        now: Long,
        random: Random = Random.Default,
    ): AdaptivePracticeState {
        if (state.phase == AdaptivePracticePhase.ROUND_ACTIVE) return state

        val level = experienceLevel(progress)
        val rawCurrent = currentTopic(curriculum, progress)
        val current = rawCurrent?.takeIf { hasCompleteQuestionBank(it, workouts) }
        val weakDue = reviewTopics(curriculum, progress, records, excluding = rawCurrent)
            .filter { hasCompleteQuestionBank(it, workouts) }
        val learnedComplete = curriculum.sections
            .filter { (progress?.sectionProgress?.get(it.group)?.seen ?: 0) > 0 }
            .map { it.group }
            .filter { hasCompleteQuestionBank(it, workouts) }

        val roundNumber = state.completedRounds + 1
        var shown = state.stepLastShownAtRound
        val usedStepIdsThisRound = mutableSetOf<String>()
        val placed = mutableListOf<RoundQuestion>()

        // 1. Review questions.
        val topMissed = state.missedConcepts
            .filter { it.pendingReview }
            .sortedByDescending { it.missCount }
            .take(MAX_REVIEW_QUESTIONS)
        for (concept in topMissed) {
            val step = pickReviewQuestion(concept, workouts, shown, random) ?: continue
            val shuffled = step.copy(choices = step.choices.shuffled(random))
            placed += RoundQuestion(instanceId = "", step = shuffled, isReview = true)
            usedStepIdsThisRound += step.id
            shown = shown + (step.id to roundNumber)
        }

        // 2. New questions, quota-first: reviews count against their own category before new ones are drawn for it.
        val remaining = RoundCategory.entries.associateWithTo(LinkedHashMap()) { it.target }
        for (rq in placed) {
            val category = RoundCategory.of(rq.step.kind)
            remaining[category] = maxOf(0, (remaining[category] ?: 0) - 1)
        }
        val newQuestionsNeeded = (TOTAL_ROUND_SIZE - placed.size).coerceAtLeast(0)
        val categoryPlan = mutableListOf<RoundCategory>()
        repeat(newQuestionsNeeded) {
            val next = remaining.entries.filter { it.value > 0 }.maxByOrNull { it.value }?.key
                ?: RoundCategory.entries.first()
            categoryPlan += next
            remaining[next] = (remaining[next] ?: 0) - 1
        }

        val usedKindsByCategory = mutableMapOf<RoundCategory, MutableSet<WorkoutStepKind>>()
        for (category in categoryPlan) {
            val usedKinds = usedKindsByCategory.getOrPut(category) { mutableSetOf() }
            val kind = category.kinds.firstOrNull { it !in usedKinds } ?: category.kinds.random(random)
            usedKinds += kind

            val chosenTopic = when (state.mode) {
                PracticeSessionKind.TOPIC_FOCUSED -> state.topic
                PracticeSessionKind.MIXED -> learnedComplete.randomOrNull(random)
                PracticeSessionKind.RECOMMENDED -> rollRecommendedTopic(level, current, weakDue, learnedComplete, random)
            } ?: continue

            val topicWorkouts = workouts.filter { it.group == chosenTopic }
            val step = pickNewQuestion(
                kind = kind,
                topicWorkouts = topicWorkouts,
                difficulty = state.difficulty,
                correctlyAnsweredStepIds = state.correctlyAnsweredStepIds,
                stepLastShownAtRound = shown,
                excludeStepIds = usedStepIdsThisRound,
                random = random,
            ) ?: continue
            val shuffled = step.copy(choices = step.choices.shuffled(random))
            placed += RoundQuestion(instanceId = "", step = shuffled, isReview = false)
            usedStepIdsThisRound += step.id
            shown = shown + (step.id to roundNumber)
        }

        // 3. Guarantee exactly ten whenever any content exists anywhere, even before every topic has full coverage.
        if (placed.size < TOTAL_ROUND_SIZE && workouts.isNotEmpty()) {
            val everything = workouts.flatMap { it.steps }
            while (placed.size < TOTAL_ROUND_SIZE) {
                val candidates = everything.filterNot { it.id in usedStepIdsThisRound }.ifEmpty { everything }
                val step = candidates.minByOrNull { shown[it.id] ?: -1 } ?: break
                val shuffled = step.copy(choices = step.choices.shuffled(random))
                placed += RoundQuestion(instanceId = "", step = shuffled, isReview = false)
                usedStepIdsThisRound += step.id
                shown = shown + (step.id to roundNumber)
            }
        }

        if (placed.isEmpty()) return state

        val finalQuestions = placed.shuffled(random).mapIndexed { index, rq -> rq.copy(instanceId = index.toString()) }
        val newRound = AdaptivePracticeRound(
            targetDifficulty = state.difficulty,
            questions = finalQuestions,
            startedAt = now,
        )

        return state.copy(
            phase = AdaptivePracticePhase.ROUND_ACTIVE,
            currentRound = newRound,
            stepLastShownAtRound = shown,
        )
    }

    /**
     * Records an answer against the current round. Guards: only valid during
     * [AdaptivePracticePhase.ROUND_ACTIVE]; rejects an [instanceId] that
     * already has an answer, and rejects a [chosenIndex] outside the
     * question's own choice range - both are no-ops rather than throwing.
     */
    fun submitRoundAnswer(
        state: AdaptivePracticeState,
        instanceId: String,
        chosenIndex: Int,
        elapsedMs: Long,
        now: Long,
    ): AdaptivePracticeState {
        if (state.phase != AdaptivePracticePhase.ROUND_ACTIVE) return state
        val round = state.currentRound ?: return state
        if (round.answers.any { it.instanceId == instanceId }) return state
        val question = round.questions.firstOrNull { it.instanceId == instanceId } ?: return state
        if (chosenIndex !in question.step.choices.indices) return state

        val answer = AdaptivePracticeAnswer(
            instanceId = instanceId,
            chosenIndex = chosenIndex,
            wasCorrect = question.step.choices[chosenIndex].correct,
            elapsedMs = elapsedMs,
        )
        val updatedRound = round.copy(answers = round.answers + answer)
        return state.copy(currentRound = updatedRound)
    }

    /**
     * Closes out the current round: applies the difficulty state machine,
     * updates [AdaptivePracticeState.missedConcepts] and
     * [AdaptivePracticeState.correctlyAnsweredStepIds], and moves to
     * [AdaptivePracticePhase.ROUND_RESULTS] - the completed round stays on
     * [AdaptivePracticeState.currentRound] rather than being cleared, so a
     * restart can restore the results page. Guard: only valid during
     * [AdaptivePracticePhase.ROUND_ACTIVE] with every question answered.
     */
    fun finishRound(state: AdaptivePracticeState, now: Long): AdaptivePracticeState {
        if (state.phase != AdaptivePracticePhase.ROUND_ACTIVE) return state
        val round = state.currentRound ?: return state
        if (!round.isFullyAnswered) return state

        var difficulty = state.difficulty
        var consecutiveBelowEight = state.consecutiveBelowEight
        val score = round.score
        if (score in ADVANCE_SCORE_RANGE) {
            difficulty = difficulty.next()
            consecutiveBelowEight = 0
        } else {
            consecutiveBelowEight += 1
            if (consecutiveBelowEight >= CONSECUTIVE_BELOW_EIGHT_TO_REGRESS) {
                difficulty = difficulty.previous()
                consecutiveBelowEight = 0
            }
        }

        var missedConcepts = state.missedConcepts
        var correctlyAnswered = state.correctlyAnsweredStepIds

        fun conceptIndex(step: WorkoutStep): Int = missedConcepts.indexOfFirst {
            it.group == step.group && it.conceptKey == step.conceptKey && it.missedAtDifficulty == step.difficulty
        }

        for (rq in round.questions) {
            val answer = round.answers.firstOrNull { it.instanceId == rq.instanceId } ?: continue
            val step = rq.step
            if (answer.wasCorrect) {
                correctlyAnswered = correctlyAnswered + step.id
                if (rq.isReview) {
                    val idx = conceptIndex(step)
                    if (idx >= 0) {
                        val existing = missedConcepts[idx]
                        val reviews = existing.successfulReviews + 1
                        missedConcepts = missedConcepts.toMutableList().also {
                            it[idx] = existing.copy(successfulReviews = reviews, pendingReview = reviews < MASTERY_REVIEWS)
                        }
                    }
                }
            } else {
                val idx = conceptIndex(step)
                missedConcepts = if (idx >= 0) {
                    val existing = missedConcepts[idx]
                    missedConcepts.toMutableList().also {
                        it[idx] = existing.copy(
                            missCount = existing.missCount + 1,
                            successfulReviews = 0,
                            pendingReview = true,
                            reviewedStepSlugs = existing.reviewedStepSlugs + step.problemSlug,
                        )
                    }
                } else {
                    missedConcepts + MissedConcept(
                        group = step.group,
                        conceptKey = step.conceptKey,
                        kind = step.kind,
                        missedAtDifficulty = step.difficulty,
                        missCount = 1,
                        reviewedStepSlugs = setOf(step.problemSlug),
                    )
                }
            }
        }

        return state.copy(
            phase = AdaptivePracticePhase.ROUND_RESULTS,
            difficulty = difficulty,
            consecutiveBelowEight = consecutiveBelowEight,
            completedRounds = state.completedRounds + 1,
            missedConcepts = missedConcepts,
            correctlyAnsweredStepIds = correctlyAnswered,
            currentRound = round.copy(completedAt = now),
        )
    }

    /** A concept's review pool, pinned to the exact difficulty it was missed at - never widened. */
    private fun pickReviewQuestion(
        concept: MissedConcept,
        workouts: List<ProblemWorkout>,
        stepLastShownAtRound: Map<String, Int>,
        random: Random,
    ): WorkoutStep? {
        val candidates = workouts
            .filter { it.group == concept.group }
            .flatMap { it.steps }
            .filter { it.conceptKey == concept.conceptKey && it.difficulty == concept.missedAtDifficulty }
        if (candidates.isEmpty()) return null
        val fresh = candidates.filter { it.problemSlug !in concept.reviewedStepSlugs }
        val pool = fresh.ifEmpty { candidates }
        return pool.minByOrNull { stepLastShownAtRound[it.id] ?: -1 } ?: pool.randomOrNull(random)
    }

    /** New-question candidate search: exact difficulty first, then [searchOrder]'s easier-then-harder fallback. */
    private fun pickNewQuestion(
        kind: WorkoutStepKind,
        topicWorkouts: List<ProblemWorkout>,
        difficulty: PracticeDifficulty,
        correctlyAnsweredStepIds: Set<String>,
        stepLastShownAtRound: Map<String, Int>,
        excludeStepIds: Set<String>,
        random: Random,
    ): WorkoutStep? {
        val allSteps = topicWorkouts.flatMap { it.steps }.filter { it.kind == kind }
        for (tier in difficulty.searchOrder()) {
            val atTier = allSteps.filter { it.difficulty == tier }
            if (atTier.isEmpty()) continue
            val notPlacedThisRound = atTier.filterNot { it.id in excludeStepIds }
            val pool1 = notPlacedThisRound.ifEmpty { atTier }
            val unseen = pool1.filterNot { it.id in correctlyAnsweredStepIds }
            val pool2 = unseen.ifEmpty { pool1 }
            return pool2.minByOrNull { stepLastShownAtRound[it.id] ?: -1 } ?: pool2.randomOrNull(random)
        }
        return null
    }

    /** Recommended Practice's per-slot topic mix, by [PracticeExperienceLevel]. */
    private fun rollRecommendedTopic(
        level: PracticeExperienceLevel,
        current: PatternGroup?,
        weakDue: List<PatternGroup>,
        mixedPool: List<PatternGroup>,
        random: Random,
    ): PatternGroup? {
        val roll = random.nextDouble()
        val chosen = when (level) {
            PracticeExperienceLevel.BEGINNER -> if (roll < 0.8) current else weakDue.firstOrNull()
            PracticeExperienceLevel.PROGRESSING -> when {
                roll < 0.5 -> current
                roll < 0.8 -> weakDue.firstOrNull()
                else -> mixedPool.randomOrNull(random)
            }
            PracticeExperienceLevel.ADVANCED -> when {
                roll < 0.2 -> current
                roll < 0.5 -> weakDue.firstOrNull()
                else -> mixedPool.randomOrNull(random)
            }
        }
        return chosen ?: current ?: weakDue.firstOrNull() ?: mixedPool.randomOrNull(random)
    }

    /** The round's six question categories - [WorkoutStepKind.EDGE_CASE] is Problem-Workout-only and never appears here. */
    private enum class RoundCategory(val kinds: List<WorkoutStepKind>, val target: Int) {
        PATTERN_RECOGNITION(listOf(WorkoutStepKind.PATTERN_RECOGNITION), 2),
        APPROACH(listOf(WorkoutStepKind.APPROACH), 2),
        TRACE_DEBUG(listOf(WorkoutStepKind.STATE_SELECTION, WorkoutStepKind.BOUNDARY_UPDATE), 2),
        BUILD(listOf(WorkoutStepKind.CODE_BLOCK), 2),
        TIME(listOf(WorkoutStepKind.TIME_COMPLEXITY), 1),
        SPACE_OR_TRANSFER(listOf(WorkoutStepKind.SPACE_COMPLEXITY, WorkoutStepKind.TRANSFER), 1),
        ;

        companion object {
            fun of(kind: WorkoutStepKind): RoundCategory = entries.first { kind in it.kinds }
        }
    }

    companion object {
        private const val BEGINNER_MAX_MASTERED = 2
        private const val PROGRESSING_MAX_MASTERED = 8
        private const val MAX_REVIEW_QUESTIONS = 5
        private const val TOTAL_ROUND_SIZE = 10
        private val ADVANCE_SCORE_RANGE = 8..10
        private const val CONSECUTIVE_BELOW_EIGHT_TO_REGRESS = 2
        private const val MASTERY_REVIEWS = 2
    }
}
