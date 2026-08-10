package com.codingarena.domain.session

import com.codingarena.domain.model.AnswerChoice
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.ChallengeType
import com.codingarena.problem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChallengeSessionTest {

    private val session = ChallengeSession()

    private val ordering = problem(
        id = "order",
        type = ChallengeType.REARRANGE_CODE,
        choices = listOf(
            AnswerChoice("l1", "one"),
            AnswerChoice("l2", "two"),
            AnswerChoice("l3", "three"),
        ),
        correctIds = listOf("l1", "l2", "l3"),
        hints = listOf("first", "second"),
    )

    @Test
    fun `a single-answer problem starts with nothing selected`() {
        val state = session.start(problem(), now = 100)

        assertTrue(state.selected.isEmpty())
        assertFalse(state.canSubmit)
        assertEquals(100L, state.startedAt)
    }

    @Test
    fun `an ordering problem starts in the authored order`() {
        val state = session.start(ordering, now = 0)

        assertEquals(listOf("l1", "l2", "l3"), state.selected)
        // Every line is placed, so it is submittable immediately - the user is
        // asserting the given order is already right.
        assertTrue(state.canSubmit)
    }

    @Test
    fun `selecting replaces the previous choice rather than accumulating`() {
        var state = session.start(problem(), now = 0)
        state = session.select(state, "a")
        state = session.select(state, "b")

        assertEquals(listOf("b"), state.selected)
        assertTrue(state.canSubmit)
    }

    @Test
    fun `selecting an unknown choice is ignored`() {
        val state = session.select(session.start(problem(), 0), "does-not-exist")

        assertTrue(state.selected.isEmpty())
    }

    @Test
    fun `selecting does nothing on an ordering problem`() {
        val started = session.start(ordering, 0)
        val state = session.select(started, "l3")

        assertEquals(started.selected, state.selected)
    }

    @Test
    fun `moving a line reorders it`() {
        var state = session.start(ordering, 0)
        state = session.move(state, "l3", -1)

        assertEquals(listOf("l1", "l3", "l2"), state.selected)
    }

    @Test
    fun `moving past either end is a no-op`() {
        val state = session.start(ordering, 0)

        assertEquals(state.selected, session.move(state, "l1", -1).selected)
        assertEquals(state.selected, session.move(state, "l3", 1).selected)
    }

    @Test
    fun `moving never loses or duplicates a line`() {
        var state = session.start(ordering, 0)
        repeat(10) {
            state = session.move(state, "l2", 1)
            state = session.move(state, "l1", 1)
            state = session.move(state, "l3", -1)
        }

        assertEquals(listOf("l1", "l2", "l3").sorted(), state.selected.sorted())
    }

    @Test
    fun `hints reveal one at a time and stop at the last one`() {
        var state = session.start(ordering, 0)
        assertEquals(2, state.hintsRemaining)

        state = session.revealHint(state)
        assertEquals(1, state.hintsRevealed)
        assertEquals(listOf("first"), state.visibleHints)

        state = session.revealHint(state)
        state = session.revealHint(state)
        assertEquals(2, state.hintsRevealed)
        assertFalse(state.canRevealHint)
    }

    @Test
    fun `a problem with no hints never offers one`() {
        val state = session.start(problem(hints = emptyList()), 0)

        assertFalse(state.canRevealHint)
        assertEquals(state, session.revealHint(state))
    }

    @Test
    fun `retrying clears the selection and counts the attempt`() {
        var state = session.select(session.start(problem(), 0), "b")
        state = session.retry(state)

        assertTrue(state.selected.isEmpty())
        assertEquals(2, state.attemptsCount)
        assertFalse(state.canSubmit)
    }

    @Test
    fun `nothing changes after submission`() {
        var state = session.markSubmitted(session.select(session.start(problem(), 0), "a"))

        assertFalse(state.canSubmit)
        assertEquals(state, session.select(state, "b"))
        assertEquals(state, session.revealHint(state))
        assertEquals(state, session.retry(state))
    }

    @Test
    fun `a submission carries the hint and retry counts through`() {
        var state = session.start(problem(), now = 1_000)
        state = session.select(state, "a")
        state = session.revealHint(state)

        val submission = session.toSubmission(state, "u1", AttemptSource.DAILY_PUZZLE)

        assertNotNull(submission)
        assertEquals("u1", submission.userId)
        assertEquals(listOf("a"), submission.selectedAnswerIds)
        assertEquals(1, submission.hintsUsed)
        assertEquals(1_000L, submission.startedAt)
        assertEquals(AttemptSource.DAILY_PUZZLE, submission.source)
    }

    @Test
    fun `an incomplete answer cannot be submitted`() {
        val state = session.start(problem(), 0)

        assertNull(session.toSubmission(state, "u1", AttemptSource.PRACTICE))
    }

    @Test
    fun `elapsed time is measured from the start and never negative`() {
        val state = session.start(problem(), now = 10_000)

        assertEquals(0, state.elapsedSeconds(10_000))
        assertEquals(30, state.elapsedSeconds(40_000))
        assertEquals(0, state.elapsedSeconds(5_000))
    }
}
