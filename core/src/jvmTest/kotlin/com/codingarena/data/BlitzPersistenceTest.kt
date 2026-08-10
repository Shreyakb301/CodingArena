package com.codingarena.data

import com.codingarena.content.NeetCode150
import com.codingarena.core.database.DatabaseDriverFactory
import com.codingarena.data.repository.LocalCurriculumRepository
import com.codingarena.db.ArenaDatabase
import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.engine.BlitzMode
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.RecallRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Roadmap progress has to survive a restart, or the 150 resets every launch.
 * These run against real SQLite.
 */
class BlitzPersistenceTest {

    private val io = Dispatchers.Unconfined
    private val engine = BlitzEngine()
    private val curriculum = NeetCode150.curriculum
    private val random = Random(7)

    private fun repo() =
        LocalCurriculumRepository(ArenaDatabase(DatabaseDriverFactory(path = null).create()), io)

    @Test
    fun `a recall record round trips`() = runTest {
        val repo = repo()
        val record = RecallRecord(
            slug = "two-sum",
            correctStreak = 2,
            totalSeen = 5,
            totalCorrect = 4,
            lastSeenAt = 1_234L,
            fastestMs = 2_100L,
        )

        repo.save("u1", record)

        assertEquals(record, repo.record("u1", "two-sum"))
    }

    @Test
    fun `an unseen slug reads back as null`() = runTest {
        assertEquals(null, repo().record("u1", "two-sum"))
    }

    @Test
    fun `saving the same slug twice updates rather than duplicating`() = runTest {
        val repo = repo()
        repo.save("u1", RecallRecord("two-sum", correctStreak = 1, totalSeen = 1, totalCorrect = 1))
        repo.save("u1", RecallRecord("two-sum", correctStreak = 2, totalSeen = 2, totalCorrect = 2))

        val all = repo.records("u1")
        assertEquals(1, all.size)
        assertEquals(2, all.getValue("two-sum").correctStreak)
    }

    @Test
    fun `records are scoped per user`() = runTest {
        val repo = repo()
        repo.save("u1", RecallRecord("two-sum", correctStreak = 3))
        repo.save("u2", RecallRecord("two-sum", correctStreak = 0))

        assertEquals(3, repo.records("u1").getValue("two-sum").correctStreak)
        assertEquals(0, repo.records("u2").getValue("two-sum").correctStreak)
    }

    @Test
    fun `a batch save writes every record`() = runTest {
        val repo = repo()
        val records = curriculum.problems.take(25).map { RecallRecord(it.slug, totalSeen = 1) }

        repo.saveAll("u1", records)

        assertEquals(25, repo.records("u1").size)
    }

    @Test
    fun `drilling a card three times marks it mastered and that survives a reload`() = runTest {
        val repo = repo()
        val problem = curriculum.problems.first()
        val card = engine.cardFor(problem, random)

        var record = repo.record("u1", problem.slug)
        repeat(3) {
            record = engine.recordAnswer(record, card, card.correct, elapsedMs = 2_000, now = 100)
            repo.save("u1", record!!)
        }

        // Re-read from storage as a fresh launch would.
        val reloaded = repo.records("u1")
        assertTrue(reloaded.getValue(problem.slug).isMastered)

        val progress = engine.progress(curriculum, reloaded)
        assertEquals(1, progress.mastered)
        assertEquals(150, progress.total)
    }

    @Test
    fun `progress across a whole section persists`() = runTest {
        val repo = repo()
        val tries = curriculum.problems.filter { it.group == PatternGroup.TRIES }

        tries.forEach { problem ->
            val card = engine.cardFor(problem, random)
            var record: RecallRecord? = null
            repeat(3) { record = engine.recordAnswer(record, card, card.correct, 1_500, 1) }
            repo.save("u1", record!!)
        }

        val progress = engine.progress(curriculum, repo.records("u1"))
        val section = progress.sectionProgress.getValue(PatternGroup.TRIES)

        assertEquals(tries.size, section.mastered)
        assertEquals(1f, section.fraction)
    }

    @Test
    fun `a finished blitz session is stored with its score`() = runTest {
        val repo = repo()
        var session = engine.start(curriculum.id, BlitzMode.FullList, now = 0)
        curriculum.problems.take(6).forEachIndexed { i, problem ->
            val card = engine.cardFor(problem, random)
            val choice = if (i < 4) card.correct else card.options.first { it != card.correct }
            session = engine.submit(session, card, choice, 2_000)
        }
        session = engine.finish(session, now = 60_000)

        repo.saveSession("u1", "blitz-1", session)

        val stored = repo.recentSessions("u1")
        assertEquals(1, stored.size)
        assertEquals(4, stored.first().score)
        assertEquals(6, stored.first().answered)
        assertNotNull(stored.first().endedAt)
        assertEquals(4, repo.bestScore("u1"))
    }

    @Test
    fun `an unfinished session does not count toward the best score`() = runTest {
        val repo = repo()
        var session = engine.start(curriculum.id, BlitzMode.FullList, now = 0)
        val card = engine.cardFor(curriculum.problems.first(), random)
        session = engine.submit(session, card, card.correct, 1_000)

        repo.saveSession("u1", "open", session)

        assertEquals(0, repo.bestScore("u1"))
        assertEquals(1, repo.recentSessions("u1").size)
    }

    @Test
    fun `recent sessions come back newest first and honour the limit`() = runTest {
        val repo = repo()
        repeat(5) { i ->
            var s = engine.start(curriculum.id, BlitzMode.FullList, now = i * 1_000L)
            s = engine.finish(s, now = i * 1_000L + 500)
            repo.saveSession("u1", "blitz-$i", s)
        }

        val recent = repo.recentSessions("u1", limit = 3)

        assertEquals(3, recent.size)
        assertEquals(recent.map { it.startedAt }.sortedDescending(), recent.map { it.startedAt })
    }

    @Test
    fun `the queue skips cards already mastered on a later run`() = runTest {
        val repo = repo()
        val first = curriculum.problems.first()
        repo.save("u1", RecallRecord(first.slug, correctStreak = 3, totalSeen = 3, totalCorrect = 3))

        val queue = engine.buildQueue(
            curriculum, repo.records("u1"), BlitzMode.WeakestFirst, limit = 150,
        )

        assertFalse(queue.any { it.slug == first.slug })
    }

    @Test
    fun `blind 75 progress advances alongside the 150`() = runTest {
        val repo = repo()
        val blindProblem = curriculum.problems.first { it.inBlind75 }
        repo.save(
            "u1",
            RecallRecord(blindProblem.slug, correctStreak = 3, totalSeen = 3, totalCorrect = 3),
        )

        val records = repo.records("u1")

        assertEquals(1, engine.progress(curriculum, records).mastered)
        assertEquals(1, engine.progress(NeetCode150.blind75, records).mastered)
    }
}
