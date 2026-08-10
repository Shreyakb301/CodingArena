package com.codingarena.domain.engine

import com.codingarena.domain.model.ChallengeType
import com.codingarena.domain.model.CodeRushMode
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.RushEndReason
import com.codingarena.problem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CodeRushEngineTest {

    private val engine = CodeRushEngine()

    private fun newSession(mode: CodeRushMode = CodeRushMode.ThreeMinute) =
        engine.start("rush-1", "user-1", mode, now = 0L)

    @Test
    fun `a run starts with three lives and no score`() {
        val session = newSession()

        assertEquals(3, session.livesRemaining)
        assertEquals(0, session.score)
        assertFalse(session.isOver)
    }

    @Test
    fun `difficulty ramps with each correct answer`() {
        var session = newSession()
        val opening = engine.targetDifficulty(session)

        session = engine.submit(session, problem(rating = 800), wasCorrect = true, elapsedMs = 5_000, now = 5_000)
        val second = engine.targetDifficulty(session)

        session = engine.submit(session, problem(id = "p2", rating = 850), wasCorrect = true, elapsedMs = 5_000, now = 10_000)
        val third = engine.targetDifficulty(session)

        assertTrue(opening < second && second < third, "$opening $second $third")
    }

    @Test
    fun `a wrong answer does not raise difficulty`() {
        var session = newSession()
        val before = engine.targetDifficulty(session)

        session = engine.submit(session, problem(), wasCorrect = false, elapsedMs = 5_000, now = 5_000)

        assertEquals(before, engine.targetDifficulty(session))
    }

    @Test
    fun `three wrong answers end the run`() {
        var session = newSession()

        repeat(3) { i ->
            session = engine.submit(
                session,
                problem(id = "p$i"),
                wasCorrect = false,
                elapsedMs = 4_000,
                now = (i + 1) * 4_000L,
            )
        }

        assertTrue(session.isOver)
        assertEquals(RushEndReason.OUT_OF_LIVES, session.endReason)
        assertEquals(0, session.livesRemaining)
    }

    @Test
    fun `answers after the run is over are ignored`() {
        var session = newSession()
        repeat(3) { i ->
            session = engine.submit(session, problem(id = "p$i"), wasCorrect = false, elapsedMs = 1_000, now = 1_000)
        }
        val ended = session

        session = engine.submit(session, problem(id = "late"), wasCorrect = true, elapsedMs = 1_000, now = 2_000)

        assertEquals(ended, session)
    }

    @Test
    fun `the next problem is never one already served`() {
        val candidates = listOf(
            problem(id = "a", rating = 800, estimatedSeconds = 20),
            problem(id = "b", rating = 810, estimatedSeconds = 20),
        )
        var session = newSession()

        val first = engine.nextProblem(session, candidates)
        assertNotNull(first)
        session = engine.submit(session, first, wasCorrect = true, elapsedMs = 3_000, now = 3_000)

        val second = engine.nextProblem(session, candidates)
        assertNotNull(second)
        assertTrue(second.id != first.id)

        session = engine.submit(session, second, wasCorrect = true, elapsedMs = 3_000, now = 6_000)
        assertNull(engine.nextProblem(session, candidates))
    }

    @Test
    fun `long problems are excluded from rush rounds`() {
        val candidates = listOf(problem(id = "slow", rating = 800, estimatedSeconds = 180))

        assertNull(engine.nextProblem(newSession(), candidates))
    }

    @Test
    fun `topic rush only serves problems on that topic`() {
        val candidates = listOf(
            problem(id = "arrays", rating = 800, topic = CodingTopic.ARRAYS, estimatedSeconds = 20),
            problem(id = "trees", rating = 800, topic = CodingTopic.TREES, estimatedSeconds = 20),
        )
        val session = engine.start("r", "u", CodeRushMode.TopicRush(CodingTopic.TREES), now = 0L)

        assertEquals("trees", engine.nextProblem(session, candidates)?.id)
    }

    @Test
    fun `timed modes run out and survival does not`() {
        val timed = newSession(CodeRushMode.ThreeMinute)
        assertFalse(engine.isTimeUp(timed, now = 179_000))
        assertTrue(engine.isTimeUp(timed, now = 180_000))
        assertEquals(1, engine.secondsRemaining(timed, now = 179_000))

        val survival = newSession(CodeRushMode.Survival)
        assertFalse(engine.isTimeUp(survival, now = 10_000_000))
        assertNull(engine.secondsRemaining(survival, now = 10_000_000))
    }

    @Test
    fun `summary reports best score, averages and category extremes`() {
        val bugs = problem(id = "bug", type = ChallengeType.FIND_THE_BUG, estimatedSeconds = 20)
        val complexity = problem(id = "cx", type = ChallengeType.TIME_COMPLEXITY, estimatedSeconds = 20)

        // Session one: all four bug questions right, three complexity wrong.
        var one = newSession()
        repeat(4) { i ->
            one = engine.submit(one, bugs.copy(id = "bug$i"), wasCorrect = true, elapsedMs = 2_000, now = 2_000L * i)
        }
        repeat(3) { i ->
            one = engine.submit(one, complexity.copy(id = "cx$i"), wasCorrect = false, elapsedMs = 2_000, now = 20_000L + i)
        }

        // Session two: two more bug questions right, ended on time.
        var two = engine.start("rush-2", "user-1", CodeRushMode.ThreeMinute, now = 0L)
        repeat(2) { i ->
            two = engine.submit(two, bugs.copy(id = "b2$i"), wasCorrect = true, elapsedMs = 2_000, now = 2_000L * i)
        }
        two = engine.finish(two, RushEndReason.TIME_UP, now = 180_000)

        val stats = engine.summarise(listOf(one, two))

        assertEquals(4, stats.bestScore)
        assertEquals(2, stats.sessionsPlayed)
        assertEquals(3.0, stats.averageScore)
        assertEquals(ChallengeType.FIND_THE_BUG, stats.strongestCategory)
        assertEquals(ChallengeType.TIME_COMPLEXITY, stats.weakestCategory)
    }

    @Test
    fun `categories with too few samples are not reported`() {
        var session = newSession()
        session = engine.submit(session, problem(type = ChallengeType.EDGE_CASE), wasCorrect = true, elapsedMs = 1_000, now = 1_000)
        session = engine.finish(session, RushEndReason.TIME_UP, now = 180_000)

        val stats = engine.summarise(listOf(session))

        assertNull(stats.strongestCategory)
        assertEquals(1, stats.bestScore)
    }

    @Test
    fun `unfinished sessions are left out of the summary`() {
        assertEquals(0, engine.summarise(listOf(newSession())).sessionsPlayed)
    }
}
