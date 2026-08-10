package com.codingarena.domain.session

import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.usecase.AnswerSubmission

/**
 * The state of one in-progress challenge.
 *
 * Deliberately a plain data class with no Compose or coroutine types: the
 * screen renders this and forwards events, but every rule about what a user
 * may do next lives in [ChallengeSession] where it can be tested.
 */
data class ChallengeState(
    val problem: CodingProblem,
    /** Chosen id for single-answer problems; the working order for rearrange problems. */
    val selected: List<String> = emptyList(),
    val hintsRevealed: Int = 0,
    val attemptsCount: Int = 1,
    val startedAt: Long = 0L,
    val submitted: Boolean = false,
) {
    val hintsRemaining: Int get() = problem.hints.size - hintsRevealed

    val canRevealHint: Boolean get() = !submitted && hintsRemaining > 0

    val canSubmit: Boolean
        get() = !submitted && when {
            problem.challengeType.isOrdering -> selected.size == problem.choices.size
            else -> selected.size == 1
        }

    /** Hints revealed so far, for rendering. */
    val visibleHints: List<String> get() = problem.hints.take(hintsRevealed)

    fun elapsedSeconds(now: Long): Int =
        if (startedAt == 0L) 0 else ((now - startedAt) / 1000L).toInt().coerceAtLeast(0)
}

/**
 * Pure state machine for the challenge screen.
 *
 * Every transition returns a new [ChallengeState]; nothing here touches
 * storage or the clock beyond the `now` values it is handed.
 */
class ChallengeSession {

    /**
     * Opens [problem].
     *
     * Rearrange problems start in the order the content lists them, which is
     * deliberately not the answer - the content author writes the lines in a
     * scrambled order and the correct sequence lives in `correctAnswerIds`.
     */
    fun start(problem: CodingProblem, now: Long): ChallengeState = ChallengeState(
        problem = problem,
        selected = if (problem.challengeType.isOrdering) problem.choices.map { it.id } else emptyList(),
        startedAt = now,
    )

    /** Picks a single answer. Ignored for ordering problems and after submit. */
    fun select(state: ChallengeState, choiceId: String): ChallengeState = when {
        state.submitted -> state
        state.problem.challengeType.isOrdering -> state
        state.problem.choice(choiceId) == null -> state
        else -> state.copy(selected = listOf(choiceId))
    }

    /**
     * Moves a line by [delta] positions in an ordering problem.
     *
     * Out-of-range moves are no-ops rather than errors, so the screen can wire
     * the buttons up unconditionally.
     */
    fun move(state: ChallengeState, choiceId: String, delta: Int): ChallengeState {
        if (state.submitted || !state.problem.challengeType.isOrdering) return state
        val order = state.selected.toMutableList()
        val from = order.indexOf(choiceId)
        val to = from + delta
        if (from == -1 || to !in order.indices) return state
        order[from] = order[to].also { order[to] = order[from] }
        return state.copy(selected = order)
    }

    /** Reveals the next hint, capped at the number the problem actually has. */
    fun revealHint(state: ChallengeState): ChallengeState =
        if (!state.canRevealHint) state else state.copy(hintsRevealed = state.hintsRevealed + 1)

    /**
     * Lets the user try again without leaving the screen.
     *
     * The retry counter feeds [com.codingarena.domain.model.AnswerOutcome], so
     * a second attempt can never score as a clean solve.
     */
    fun retry(state: ChallengeState): ChallengeState =
        if (state.submitted) state
        else state.copy(attemptsCount = state.attemptsCount + 1, selected = emptyList())

    fun markSubmitted(state: ChallengeState): ChallengeState = state.copy(submitted = true)

    /**
     * Builds the submission for [SubmitAnswerUseCase].
     *
     * Returns null when the answer is incomplete, so a screen cannot submit a
     * half-ordered rearrange problem by wiring the button up wrongly.
     */
    fun toSubmission(
        state: ChallengeState,
        userId: String,
        source: AttemptSource,
    ): AnswerSubmission? {
        if (!state.canSubmit) return null
        return AnswerSubmission(
            userId = userId,
            problem = state.problem,
            selectedAnswerIds = state.selected,
            startedAt = state.startedAt,
            hintsUsed = state.hintsRevealed,
            attemptsCount = state.attemptsCount,
            source = source,
        )
    }
}
