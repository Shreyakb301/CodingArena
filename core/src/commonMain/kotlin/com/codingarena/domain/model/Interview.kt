package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * The nine behavioral areas Interview content is organized around. Every
 * category is guaranteed a [InterviewExerciseMode.BUILD_STAR_ANSWER] workout; the other
 * three modes are authored for a growing subset, not necessarily all nine.
 */
@Serializable
enum class BehavioralCategory(val displayName: String) {
    LEADERSHIP("Leadership"),
    CONFLICT("Conflict & Disagreement"),
    FAILURE_LEARNING("Failure & Learning"),
    OWNERSHIP("Ownership"),
    TEAMWORK("Teamwork"),
    AMBIGUITY("Ambiguity"),
    PRIORITIZATION("Prioritization"),
    COMMUNICATION("Communication"),
    PROJECT_IMPACT("Project Impact"),
}

/** The four ways a [BehavioralCategory] can be drilled. */
@Serializable
enum class InterviewExerciseMode(val displayName: String) {
    BUILD_STAR_ANSWER("Build a STAR Answer"),
    IMPROVE_AN_ANSWER("Improve an Answer"),
    SPOT_RED_FLAG("Spot the Red Flag"),
    HANDLE_FOLLOWUPS("Handle Follow-ups"),
}

/**
 * How a behavioral choice is graded. Deliberately not correct/incorrect - a
 * behavioral answer is rarely simply wrong, so the label captures how strong
 * the choice is rather than whether it "passes."
 */
@Serializable
enum class ChoiceRating(val label: String) {
    STRONG("Strong choice"),
    REASONABLE("Reasonable but incomplete"),
    WEAK("Weak or risky"),
}

/**
 * One option in any Interview decision point - a behavioral exercise, a STAR
 * stage, a technical-communication item, or a mock-interview decision. The
 * shape is identical everywhere on purpose: one rating vocabulary, one
 * feedback shape, reused across the whole tab.
 */
@Serializable
data class RatedChoice(
    val id: String,
    val text: String,
    val rating: ChoiceRating,
    /** Always shown for behavioral/tech-comm exercises; withheld until the end for a mock interview. */
    val feedback: String,
)

/**
 * A single-decision-point behavioral drill - what [InterviewExerciseMode.IMPROVE_AN_ANSWER],
 * [InterviewExerciseMode.SPOT_RED_FLAG], and [InterviewExerciseMode.HANDLE_FOLLOWUPS] are built
 * from. [referenceAnswer] is set only for Improve an Answer, where the choices
 * are ways to strengthen it rather than options for a fresh answer.
 */
@Serializable
data class BehavioralExercise(
    val id: String,
    val category: BehavioralCategory,
    val mode: InterviewExerciseMode,
    val difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    /** The actual, commonly-asked interview question this drill trains - verbatim, not paraphrased. */
    val commonQuestion: String,
    /** A fictional, realistic workplace scenario - never a real company or person. */
    val scenario: String,
    val prompt: String,
    val referenceAnswer: String? = null,
    val choices: List<RatedChoice>,
)

/**
 * The five stages of a guided STAR answer, in the fixed order a workout is
 * always presented. Each stage maps onto exactly one grading dimension, so
 * five answered stages produce a complete five-dimension breakdown with no
 * extra bookkeeping.
 */
@Serializable
enum class StarStage {
    SITUATION,
    TASK,
    ACTION,
    RESULT,
    REFLECTION,
    ;

    val dimension: StarDimension
        get() = when (this) {
            SITUATION -> StarDimension.RELEVANCE
            TASK -> StarDimension.OWNERSHIP
            ACTION -> StarDimension.SPECIFICITY
            RESULT -> StarDimension.IMPACT
            REFLECTION -> StarDimension.REFLECTION
        }

    companion object {
        /** The fixed presentation order every [StarWorkout] follows. */
        val ORDERED: List<StarStage> = listOf(SITUATION, TASK, ACTION, RESULT, REFLECTION)
    }
}

/** The five axes a behavioral answer is graded on, one per [StarStage]. */
@Serializable
enum class StarDimension(val displayName: String) {
    RELEVANCE("Relevance"),
    OWNERSHIP("Ownership"),
    SPECIFICITY("Specificity"),
    IMPACT("Impact"),
    REFLECTION("Reflection"),
}

@Serializable
data class StarStep(
    val stage: StarStage,
    val prompt: String,
    val choices: List<RatedChoice>,
)

/**
 * One guided STAR workout: a fictional scenario prompt, then exactly five
 * [StarStep]s in [StarStage.ORDERED] order.
 *
 * A [category] can have up to four of these, one per [PracticeDifficulty]
 * tier - same reasoning and same reused type as
 * [ProblemWorkout][com.codingarena.content.ProblemWorkout]'s difficulty
 * tiers in Practice: same scenario shape, harder judgment calls at higher
 * tiers, not new mechanics.
 */
@Serializable
data class StarWorkout(
    val id: String,
    val category: BehavioralCategory,
    val difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    /** The actual, commonly-asked interview question this workout trains - verbatim, not paraphrased. */
    val commonQuestion: String,
    val scenario: String,
    val steps: List<StarStep>,
)

@Serializable
data class StarAnswer(
    val stage: StarStage,
    val choiceId: String,
    val rating: ChoiceRating,
)

@Serializable
data class StarDimensionScore(
    val dimension: StarDimension,
    val rating: ChoiceRating,
)

/** A completed STAR workout - kept, not cleared, so the results screen can render the breakdown after a restart. */
@Serializable
data class StarWorkoutResult(
    val workoutId: String,
    val answers: List<StarAnswer>,
    val completedAt: Long,
) {
    /** One score per dimension, in [StarStage.ORDERED] order. */
    val dimensionBreakdown: List<StarDimensionScore>
        get() = answers.map { StarDimensionScore(it.stage.dimension, it.rating) }

    val overallRating: ChoiceRating
        get() {
            val strong = answers.count { it.rating == ChoiceRating.STRONG }
            val weak = answers.count { it.rating == ChoiceRating.WEAK }
            return when {
                strong >= 4 -> ChoiceRating.STRONG
                weak >= 3 -> ChoiceRating.WEAK
                else -> ChoiceRating.REASONABLE
            }
        }
}

/** A live or finished guided STAR workout. Pure - the caller owns the clock and ids. */
@Serializable
data class StarWorkoutSession(
    val workout: StarWorkout,
    val answers: List<StarAnswer> = emptyList(),
) {
    val currentStepIndex: Int get() = answers.size
    val currentStep: StarStep? get() = workout.steps.getOrNull(currentStepIndex)
    val isOver: Boolean get() = currentStepIndex >= workout.steps.size
}

/** The four options-based ways Technical Communication drills an already-learned problem. */
@Serializable
enum class TechCommExerciseKind(val displayName: String) {
    CLARIFY_PROBLEM("Clarify the Problem"),
    EXPLAIN_APPROACH("Explain an Approach"),
    DISCUSS_TRADEOFFS("Discuss Tradeoffs"),
    EXPLAIN_EDGE_CASES("Explain Edge Cases"),
}

/**
 * One Technical Communication item, tied to a problem the learner has already
 * covered on the Roadmap - never introduces new algorithmic content, only
 * drills how to talk through it out loud.
 */
@Serializable
data class TechCommItem(
    val id: String,
    val problemSlug: String,
    val kind: TechCommExerciseKind,
    val difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    val prompt: String,
    val choices: List<RatedChoice>,
)

@Serializable
data class MockInterviewDecision(
    val id: String,
    /** 1-based position in the fixed five-decision sequence. */
    val order: Int,
    val prompt: String,
    val choices: List<RatedChoice>,
)

/**
 * One behavioral mock interview: a short framing, then exactly five connected
 * decisions. Feedback for every decision is withheld until
 * [MockInterviewResult] - unlike every other Interview exercise, a mock does
 * not reveal how a choice graded until the run is over.
 */
@Serializable
data class MockInterview(
    val id: String,
    val title: String,
    val difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    val setup: String,
    val decisions: List<MockInterviewDecision>,
)

@Serializable
data class MockInterviewAnswer(
    val decisionId: String,
    val choiceId: String,
    val rating: ChoiceRating,
)

@Serializable
data class MockInterviewResult(
    val mockId: String,
    val answers: List<MockInterviewAnswer>,
    val completedAt: Long,
) {
    val overallRating: ChoiceRating
        get() {
            val strong = answers.count { it.rating == ChoiceRating.STRONG }
            val weak = answers.count { it.rating == ChoiceRating.WEAK }
            return when {
                strong >= 4 -> ChoiceRating.STRONG
                weak >= 3 -> ChoiceRating.WEAK
                else -> ChoiceRating.REASONABLE
            }
        }
}

/** A live or finished mock interview. Pure - the caller owns the clock and ids. */
@Serializable
data class MockInterviewSession(
    val mock: MockInterview,
    val answers: List<MockInterviewAnswer> = emptyList(),
) {
    val currentDecisionIndex: Int get() = answers.size
    val currentDecision: MockInterviewDecision? get() = mock.decisions.getOrNull(currentDecisionIndex)
    val isOver: Boolean get() = currentDecisionIndex >= mock.decisions.size
}

// ---------------------------------------------------------------- adaptive behavioral rounds
//
// The Behavioral Workouts round system - same mechanism as Practice's
// adaptive rounds (10 questions, [PracticeDifficulty] advances on a score of
// 8-10, regresses after two consecutive sub-8 rounds), plus one addition
// Practice doesn't have: getting a question WEAK mid-round immediately
// splices 2-3 previously-solved questions from this session into the round's
// very next slots before the originally planned questions resume - a
// confidence-rebuilding beat, not a drill-the-mistake one. See
// [InterviewEngine.submitBehavioralRoundAnswer].

/**
 * One question placed in a round - reused by both Behavioral Workouts
 * (STAR stage or single-decision exercise, [category]/[stage] set,
 * [problemSlug] null) and Technical Communication ([problemSlug]/[techCommKind]
 * set, [category]/[stage] null) - one round shape, one engine, two content
 * sources.
 */
@Serializable
data class InterviewRoundQuestion(
    val instanceId: String,
    /**
     * Stable identity independent of [instanceId] - which is re-stamped to a
     * round-positional index every round and at every replay splice, so it
     * can never be used for durable tracking. This is what
     * [InterviewRoundState.recentlySolved] and
     * [InterviewRoundState.contentLastShownAtRound] key against.
     */
    val contentId: String,
    val category: BehavioralCategory? = null,
    val problemSlug: String? = null,
    val techCommKind: TechCommExerciseKind? = null,
    val commonQuestion: String?,
    val scenario: String?,
    val referenceAnswer: String?,
    val prompt: String,
    val choices: List<RatedChoice>,
    /** Non-null only for STAR-derived questions - drives the results screen's dimension breakdown. */
    val stage: StarStage? = null,
    /** True when this slot is a replay of an earlier solve, inserted after a WEAK answer. */
    val isReplay: Boolean = false,
)

@Serializable
data class InterviewRoundAnswer(
    val instanceId: String,
    val choiceId: String,
    val rating: ChoiceRating,
)

/** One ten-question adaptive behavioral round. Pure - the caller owns the clock, ids, and [kotlin.random.Random]. */
@Serializable
data class InterviewRound(
    val targetDifficulty: PracticeDifficulty,
    val questions: List<InterviewRoundQuestion>,
    val answers: List<InterviewRoundAnswer> = emptyList(),
    val startedAt: Long,
    val completedAt: Long? = null,
) {
    val currentIndex: Int get() = answers.size
    val currentQuestion: InterviewRoundQuestion? get() = questions.getOrNull(currentIndex)
    val isFullyAnswered: Boolean get() = currentIndex >= questions.size
    val isOver: Boolean get() = completedAt != null

    /** Out of [questions].size - a WEAK answer is the only one that doesn't count toward it. */
    val score: Int get() = answers.count { it.rating != ChoiceRating.WEAK }

    val dimensionBreakdown: List<StarDimensionScore>
        get() = answers.mapNotNull { answer ->
            val question = questions.firstOrNull { it.instanceId == answer.instanceId } ?: return@mapNotNull null
            question.stage?.let { StarDimensionScore(it.dimension, answer.rating) }
        }
}

/** Which content source a round draws from - see [InterviewRoundState]. */
@Serializable
enum class InterviewRoundSurface { BEHAVIORAL, TECH_COMM }

/**
 * Cross-round state for one Behavioral Workouts or Technical Communication
 * run - one shape for both, discriminated by [surface], mirroring how
 * [AdaptivePracticeState] already spans RECOMMENDED/TOPIC_FOCUSED/MIXED via
 * `mode`+`topic`. Deliberately not persisted across app restarts - see
 * [InterviewProgress]'s doc comment for why in-progress Interview state stays
 * ephemeral; only the completed-round tallies folded into [InterviewProgress]
 * survive a restart.
 */
@Serializable
data class InterviewRoundState(
    val id: String,
    val userId: String,
    val surface: InterviewRoundSurface,
    /** BEHAVIORAL only. Null means every category, combined into one pool - set means one category only. */
    val category: BehavioralCategory? = null,
    /** TECH_COMM only. */
    val problemSlug: String? = null,
    val difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    val phase: AdaptivePracticePhase = AdaptivePracticePhase.READY,
    val completedRounds: Int = 0,
    val consecutiveBelowEight: Int = 0,
    /** Correctly-answered questions this session, most recent last - the replay pool for the wrong-answer recovery beat. BEHAVIORAL only; always empty for TECH_COMM. */
    val recentlySolved: List<InterviewRoundQuestion> = emptyList(),
    /** [InterviewRoundQuestion.contentId] to the round number it was last shown in - drives least-recently-shown fallback and repeat ordering. */
    val contentLastShownAtRound: Map<String, Int> = emptyMap(),
    val currentRound: InterviewRound? = null,
    val startedAt: Long,
) {
    val isPhaseValid: Boolean
        get() = when (phase) {
            AdaptivePracticePhase.READY -> currentRound == null
            AdaptivePracticePhase.ROUND_ACTIVE -> currentRound != null && !currentRound.isOver
            AdaptivePracticePhase.ROUND_RESULTS -> currentRound != null && currentRound.isOver
        }
}

/** Per-category tally, kept small - just enough to drive "Recommended Today" and the progress card. */
@Serializable
data class CategoryProgress(
    val category: BehavioralCategory,
    val completedCount: Int = 0,
    val strongCount: Int = 0,
    val weakCount: Int = 0,
    val lastCompletedAt: Long? = null,
)

/**
 * Cross-session Interview stats - one row per user, updated after every
 * finished exercise/workout/mock. Deliberately much lighter than
 * [AdaptivePracticeState]: nothing here needs to resume mid-exercise, so only
 * completed-result tallies are kept, not in-progress session state.
 */
@Serializable
data class InterviewProgress(
    val userId: String,
    val categoryProgress: Map<BehavioralCategory, CategoryProgress> = emptyMap(),
    val starWorkoutsCompleted: Int = 0,
    val techCommCompleted: Int = 0,
    val mockInterviewsCompleted: Int = 0,
    val dimensionStrongCounts: Map<StarDimension, Int> = emptyMap(),
    val dimensionWeakCounts: Map<StarDimension, Int> = emptyMap(),
    val lastCompletedAt: Long? = null,
    /**
     * One difficulty ladder per surface, carried across sessions so "start
     * easy, get harder" is a real journey rather than resetting to
     * [PracticeDifficulty.BEGINNER] every time the tab is reopened - the
     * in-progress round itself stays ephemeral (see [InterviewRoundState]'s
     * doc comment), but the level it left off at is exactly what's saved here.
     */
    val behavioralDifficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    val behavioralConsecutiveBelowTarget: Int = 0,
    val techCommDifficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    val techCommConsecutiveBelowTarget: Int = 0,
    val mockDifficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    val mockConsecutiveWeak: Int = 0,
) {
    val totalCompleted: Int get() = categoryProgress.values.sumOf { it.completedCount } + techCommCompleted + mockInterviewsCompleted

    val strongestDimension: StarDimension?
        get() = StarDimension.entries.maxByOrNull { dimensionStrongCounts[it] ?: 0 }
            ?.takeIf { (dimensionStrongCounts[it] ?: 0) > 0 }

    val weakestDimension: StarDimension?
        get() = StarDimension.entries.maxByOrNull { dimensionWeakCounts[it] ?: 0 }
            ?.takeIf { (dimensionWeakCounts[it] ?: 0) > 0 }
}
