package com.codingarena.domain.engine

import com.codingarena.content.NeetCode150
import com.codingarena.core.common.MILLIS_PER_DAY
import com.codingarena.domain.model.RecallRecord
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Spaced repetition and solved-tracking over the roadmap. */
class BlitzSchedulingTest {

    private val engine = BlitzEngine()
    private val curriculum = NeetCode150.curriculum
    private val random = Random(11)
    private val now = 1_700_000_000_000L

    private val problem = curriculum.problems.first()
    private val card = engine.cardFor(problem, random)
    private val wrongChoice = card.options.first { it != card.correct }

    // -------------------------------------------------------------- intervals

    @Test
    fun `a first correct answer comes back tomorrow`() {
        val record = engine.recordAnswer(null, card, card.correct, 2_000, now)

        assertEquals(now + 3 * MILLIS_PER_DAY, record.dueAt)
    }

    @Test
    fun `intervals climb the ladder with the streak`() {
        var record: RecallRecord? = null
        val intervals = (1..5).map {
            record = engine.recordAnswer(record, card, card.correct, 2_000, now)
            (record!!.dueAt!! - now) / MILLIS_PER_DAY
        }

        assertEquals(listOf(3L, 7L, 14L, 30L, 30L), intervals)
    }

    @Test
    fun `a miss drops the card back to tomorrow`() {
        var record: RecallRecord? = null
        repeat(4) { record = engine.recordAnswer(record, card, card.correct, 2_000, now) }
        assertTrue(record!!.isMastered)

        record = engine.recordAnswer(record, card, wrongChoice, 2_000, now)

        assertEquals(now + MILLIS_PER_DAY, record!!.dueAt)
        assertFalse(record!!.isMastered)
    }

    /**
     * "Locked in" is a status, not a graduation. Mastery flips at a streak of
     * three, but the interval keeps growing after that - so a mastered card
     * returns in two weeks, then a month, rather than vanishing.
     */
    @Test
    fun `a mastered card still comes back - recall decays`() {
        var record: RecallRecord? = null
        repeat(3) { record = engine.recordAnswer(record, card, card.correct, 2_000, now) }

        assertNotNull(record!!.dueAt)
        assertTrue(record!!.isMastered)
        assertFalse(record!!.isDue(now + 13 * MILLIS_PER_DAY))
        assertTrue(record!!.isDue(now + 15 * MILLIS_PER_DAY))

        // One more clean recall pushes it out to the full month.
        record = engine.recordAnswer(record, card, card.correct, 2_000, now)
        assertEquals(now + 30 * MILLIS_PER_DAY, record!!.dueAt)
    }

    @Test
    fun `an unseen card counts as due so it can be introduced`() {
        assertTrue(RecallRecord("never-seen").isDue(now))
    }

    // ------------------------------------------------------------- due queues

    @Test
    fun `due cards exclude ones not yet ready`() {
        val records = mapOf(
            curriculum.problems[0].slug to RecallRecord(
                curriculum.problems[0].slug, dueAt = now - 1, totalSeen = 1,
            ),
            curriculum.problems[1].slug to RecallRecord(
                curriculum.problems[1].slug, dueAt = now + MILLIS_PER_DAY, totalSeen = 1,
            ),
        )

        val due = engine.dueCards(curriculum, records, now)

        assertEquals(1, due.size)
        assertEquals(curriculum.problems[0].slug, due.first().slug)
    }

    @Test
    fun `due cards only counts cards actually started`() {
        // No records at all - nothing is "due for review" because nothing has
        // been learned yet. Due is a review queue, not a to-do list.
        assertTrue(engine.dueCards(curriculum, emptyMap(), now).isEmpty())
    }

    @Test
    fun `the due mode serves exactly the due cards`() {
        val records = curriculum.problems.take(4).mapIndexed { i, p ->
            p.slug to RecallRecord(
                p.slug,
                totalSeen = 1,
                dueAt = if (i < 2) now - 1 else now + MILLIS_PER_DAY,
            )
        }.toMap()

        val queue = engine.buildQueue(curriculum, records, BlitzMode.DueToday, limit = 50, now = now)

        assertEquals(2, queue.size)
    }

    @Test
    fun `progress reports the due count`() {
        val records = curriculum.problems.take(3).associate {
            it.slug to RecallRecord(it.slug, totalSeen = 1, dueAt = now - 1)
        }

        assertEquals(3, engine.progress(curriculum, records, now).due)
    }

    // ---------------------------------------------------------------- solved

    @Test
    fun `marking solved is separate from recall`() {
        val record = engine.markSolved(null, "two-sum", solved = true, now = now)

        assertTrue(record.solved)
        assertEquals(now, record.solvedAt)
        // Solving it does not claim you can recall the pattern on demand.
        assertFalse(record.isMastered)
        assertEquals(0, record.totalSeen)
    }

    @Test
    fun `unmarking clears the timestamp`() {
        val marked = engine.markSolved(null, "two-sum", solved = true, now = now)
        val cleared = engine.markSolved(marked, "two-sum", solved = false, now = now + 1)

        assertFalse(cleared.solved)
        assertNull(cleared.solvedAt)
    }

    @Test
    fun `marking solved preserves recall history`() {
        var record: RecallRecord? = null
        repeat(3) { record = engine.recordAnswer(record, card, card.correct, 2_000, now) }

        val marked = engine.markSolved(record, problem.slug, solved = true, now = now)

        assertTrue(marked.isMastered)
        assertEquals(3, marked.totalSeen)
        assertTrue(marked.solved)
    }

    @Test
    fun `progress counts solved separately from mastered`() {
        val a = curriculum.problems[0]
        val b = curriculum.problems[1]
        val records = mapOf(
            // Recalled but never actually solved.
            a.slug to RecallRecord(a.slug, correctStreak = 3, totalSeen = 3, totalCorrect = 3),
            // Solved but the pattern was never drilled.
            b.slug to RecallRecord(b.slug, solved = true, solvedAt = now),
        )

        val progress = engine.progress(curriculum, records, now)

        assertEquals(1, progress.mastered)
        assertEquals(1, progress.solved)
        assertTrue(progress.solvedFraction > 0f)
    }

    @Test
    fun `section progress tracks solved too`() {
        val section = curriculum.sections.first()
        val records = section.problems.associate {
            it.slug to RecallRecord(it.slug, solved = true, solvedAt = now)
        }

        val progress = engine.progress(curriculum, records, now)

        assertEquals(
            section.problems.size,
            progress.sectionProgress.getValue(section.group).solved,
        )
    }

    @Test
    fun `answering does not silently clear a solved flag`() {
        val solved = engine.markSolved(null, problem.slug, solved = true, now = now)

        val after = engine.recordAnswer(solved, card, card.correct, 2_000, now + 1)

        assertTrue(after.solved, "drilling the card should not unmark it as solved")
    }
}
