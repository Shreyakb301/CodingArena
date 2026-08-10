package com.codingarena.domain.engine

import com.codingarena.content.NeetCode150
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.RecallRecord
import com.codingarena.domain.model.RecallStrength
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BlitzEngineTest {

    private val engine = BlitzEngine()
    private val curriculum = NeetCode150.curriculum
    private val random = Random(42)

    // ------------------------------------------------------------ card design

    @Test
    fun `a card always contains its own answer`() {
        curriculum.problems.forEach { problem ->
            val card = engine.cardFor(problem, random)
            assertTrue(card.correct in card.options, "${problem.slug} has no correct option")
            assertEquals(problem.group, card.correct)
        }
    }

    @Test
    fun `every card offers exactly four distinct options`() {
        curriculum.problems.forEach { problem ->
            val card = engine.cardFor(problem, random)
            assertEquals(4, card.options.size, "${problem.slug} option count")
            assertEquals(4, card.options.distinct().size, "${problem.slug} has duplicate options")
        }
    }

    @Test
    fun `distractors are patterns you would plausibly confuse, not random ones`() {
        val problem = curriculum.problems.first { it.group == PatternGroup.SLIDING_WINDOW }
        val card = engine.cardFor(problem, random)

        val distractors = card.options - card.correct
        assertTrue(
            distractors.all { it in PatternGroup.SLIDING_WINDOW.confusableWith },
            "got implausible distractors: $distractors",
        )
    }

    @Test
    fun `the answer is not always in the same position`() {
        val problem = curriculum.problems.first()
        val positions = (1..40).map { seed ->
            val card = engine.cardFor(problem, Random(seed))
            card.options.indexOf(card.correct)
        }.toSet()

        assertTrue(positions.size > 1, "the correct answer never moved")
    }

    @Test
    fun `no pattern group lists itself as a confusable`() {
        PatternGroup.entries.forEach { group ->
            assertFalse(group in group.confusableWith, "$group confuses with itself")
        }
    }

    // ------------------------------------------------------------ recall state

    @Test
    fun `a correct answer extends the streak and records the time`() {
        val card = engine.cardFor(curriculum.problems.first(), random)

        val record = engine.recordAnswer(null, card, card.correct, elapsedMs = 4_000, now = 100)

        assertEquals(1, record.correctStreak)
        assertEquals(1, record.totalSeen)
        assertEquals(1, record.totalCorrect)
        assertEquals(4_000L, record.fastestMs)
        assertEquals(100L, record.lastSeenAt)
    }

    @Test
    fun `a miss resets the streak entirely`() {
        val card = engine.cardFor(curriculum.problems.first(), random)
        val wrong = card.options.first { it != card.correct }

        var record = engine.recordAnswer(null, card, card.correct, 3_000, 1)
        record = engine.recordAnswer(record, card, card.correct, 3_000, 2)
        assertEquals(2, record.correctStreak)

        record = engine.recordAnswer(record, card, wrong, 3_000, 3)

        assertEquals(0, record.correctStreak)
        assertEquals(3, record.totalSeen)
        assertEquals(2, record.totalCorrect)
    }

    @Test
    fun `mastery needs three correct in a row`() {
        val card = engine.cardFor(curriculum.problems.first(), random)
        var record: RecallRecord? = null

        repeat(2) { record = engine.recordAnswer(record, card, card.correct, 2_000, 1) }
        assertFalse(record!!.isMastered)

        record = engine.recordAnswer(record, card, card.correct, 2_000, 1)
        assertTrue(record!!.isMastered)
        assertEquals(RecallStrength.MASTERED, record!!.strength)
    }

    @Test
    fun `a mastered card can be lost again`() {
        val card = engine.cardFor(curriculum.problems.first(), random)
        var record: RecallRecord? = null
        repeat(3) { record = engine.recordAnswer(record, card, card.correct, 2_000, 1) }
        assertTrue(record!!.isMastered)

        record = engine.recordAnswer(record, card, card.options.first { it != card.correct }, 2_000, 2)

        assertFalse(record!!.isMastered)
        assertEquals(RecallStrength.SHAKY, record!!.strength)
    }

    @Test
    fun `fastest time only improves on correct answers`() {
        val card = engine.cardFor(curriculum.problems.first(), random)
        val wrong = card.options.first { it != card.correct }

        var record = engine.recordAnswer(null, card, card.correct, 5_000, 1)
        record = engine.recordAnswer(record, card, wrong, 100, 2)

        assertEquals(5_000L, record.fastestMs, "a wrong answer should not set a personal best")

        record = engine.recordAnswer(record, card, card.correct, 2_000, 3)
        assertEquals(2_000L, record.fastestMs)
    }

    // ------------------------------------------------------------------ queue

    @Test
    fun `the queue leads with cards never seen`() {
        val seen = curriculum.problems.take(10)
            .associate { it.slug to RecallRecord(it.slug, correctStreak = 3, totalSeen = 3, totalCorrect = 3) }

        val queue = engine.buildQueue(curriculum, seen, BlitzMode.FullList, limit = 5)

        assertTrue(queue.none { it.slug in seen.keys }, "mastered cards led the queue")
    }

    @Test
    fun `shaky cards outrank learning cards`() {
        val shaky = curriculum.problems[0]
        val learning = curriculum.problems[1]
        val records = mapOf(
            shaky.slug to RecallRecord(shaky.slug, correctStreak = 0, totalSeen = 4, totalCorrect = 1),
            learning.slug to RecallRecord(learning.slug, correctStreak = 1, totalSeen = 2, totalCorrect = 2),
        )

        val queue = engine.buildQueue(curriculum, records, BlitzMode.WeakestFirst, limit = 150)

        assertTrue(
            queue.indexOfFirst { it.slug == shaky.slug } <
                queue.indexOfFirst { it.slug == learning.slug },
        )
    }

    @Test
    fun `weak spots mode excludes mastered cards`() {
        val records = curriculum.problems.associate {
            it.slug to RecallRecord(it.slug, correctStreak = 3, totalSeen = 3, totalCorrect = 3)
        }

        assertTrue(engine.buildQueue(curriculum, records, BlitzMode.WeakestFirst).isEmpty())
    }

    @Test
    fun `section mode only serves that section`() {
        val queue = engine.buildQueue(
            curriculum,
            emptyMap(),
            BlitzMode.Section(PatternGroup.TRIES),
            limit = 100,
        )

        assertTrue(queue.isNotEmpty())
        assertTrue(queue.all { it.group == PatternGroup.TRIES })
    }

    @Test
    fun `blind 75 mode only serves the shortlist`() {
        val queue = engine.buildQueue(curriculum, emptyMap(), BlitzMode.Blind75, limit = 200)

        assertTrue(queue.isNotEmpty())
        assertTrue(queue.all { it.inBlind75 })
    }

    @Test
    fun `the queue honours its limit`() {
        assertEquals(7, engine.buildQueue(curriculum, emptyMap(), BlitzMode.FullList, limit = 7).size)
    }

    // ---------------------------------------------------------------- session

    @Test
    fun `a session tracks score, streak and accuracy`() {
        var session = engine.start(curriculum.id, BlitzMode.FullList, now = 0)
        val cards = curriculum.problems.take(5).map { engine.cardFor(it, random) }

        session = engine.submit(session, cards[0], cards[0].correct, 3_000)
        session = engine.submit(session, cards[1], cards[1].correct, 3_000)
        session = engine.submit(
            session, cards[2], cards[2].options.first { it != cards[2].correct }, 3_000,
        )
        session = engine.submit(session, cards[3], cards[3].correct, 3_000)

        assertEquals(3, session.score)
        assertEquals(1, session.streak, "streak resets after the miss")
        assertEquals(2, session.bestStreak)
        assertEquals(0.75, session.accuracy)
        assertEquals(3_000L, session.averageMs)
    }

    @Test
    fun `answers after the session ends are ignored`() {
        var session = engine.start(curriculum.id, BlitzMode.FullList, now = 0)
        val card = engine.cardFor(curriculum.problems.first(), random)
        session = engine.finish(session, now = 60_000)

        val after = engine.submit(session, card, card.correct, 1_000)

        assertEquals(session, after)
    }

    @Test
    fun `blitz never ends in failure - there are no lives`() {
        var session = engine.start(curriculum.id, BlitzMode.FullList, now = 0)
        curriculum.problems.take(10).forEach {
            val card = engine.cardFor(it, random)
            session = engine.submit(session, card, card.options.first { o -> o != card.correct }, 1_000)
        }

        assertFalse(session.isOver, "ten wrong answers should not end a Blitz run")
        assertEquals(0, session.score)
    }

    // --------------------------------------------------------------- progress

    @Test
    fun `a fresh user has zero progress`() {
        val progress = engine.progress(curriculum, emptyMap())

        assertEquals(150, progress.total)
        assertEquals(0, progress.seen)
        assertEquals(0, progress.mastered)
        assertEquals(0f, progress.fraction)
    }

    @Test
    fun `progress counts seen and mastered separately`() {
        val records = mapOf(
            curriculum.problems[0].slug to RecallRecord(
                curriculum.problems[0].slug, correctStreak = 3, totalSeen = 3, totalCorrect = 3,
            ),
            curriculum.problems[1].slug to RecallRecord(
                curriculum.problems[1].slug, correctStreak = 1, totalSeen = 2, totalCorrect = 1,
            ),
        )

        val progress = engine.progress(curriculum, records)

        assertEquals(2, progress.seen)
        assertEquals(1, progress.mastered)
        assertTrue(progress.coverageFraction > progress.fraction)
    }

    @Test
    fun `section progress adds up to the whole`() {
        val records = curriculum.problems.take(30).associate {
            it.slug to RecallRecord(it.slug, correctStreak = 3, totalSeen = 3, totalCorrect = 3)
        }

        val progress = engine.progress(curriculum, records)

        assertEquals(progress.total, progress.sectionProgress.values.sumOf { it.total })
        assertEquals(progress.mastered, progress.sectionProgress.values.sumOf { it.mastered })
    }

    @Test
    fun `weakest sections surface the ones with least mastery`() {
        // Master every Trie card, leave everything else untouched.
        val records = curriculum.problems
            .filter { it.group == PatternGroup.TRIES }
            .associate { it.slug to RecallRecord(it.slug, correctStreak = 3, totalSeen = 3, totalCorrect = 3) }

        val weakest = engine.progress(curriculum, records).weakestSections(limit = 3)

        assertTrue(weakest.none { it.group == PatternGroup.TRIES })
        assertTrue(weakest.all { it.fraction == 0f })
    }

    // -------------------------------------------------------------- diagnosis

    @Test
    fun `confusions report which pattern gets mistaken for which`() {
        var session = engine.start(curriculum.id, BlitzMode.FullList, now = 0)
        val windowProblems = curriculum.problems.filter { it.group == PatternGroup.SLIDING_WINDOW }

        windowProblems.take(3).forEach {
            val card = engine.cardFor(it, random)
            session = engine.submit(session, card, PatternGroup.TWO_POINTERS, 2_000)
        }

        val confusions = engine.confusions(listOf(session))

        assertTrue(confusions.isNotEmpty())
        val top = confusions.first()
        assertEquals(PatternGroup.SLIDING_WINDOW, top.actual)
        assertEquals(PatternGroup.TWO_POINTERS, top.mistakenFor)
        assertEquals(3, top.count)
        assertTrue(top.description.contains("Sliding Window"), top.description)
    }

    @Test
    fun `correct answers never appear as confusions`() {
        var session = engine.start(curriculum.id, BlitzMode.FullList, now = 0)
        curriculum.problems.take(5).forEach {
            val card = engine.cardFor(it, random)
            session = engine.submit(session, card, card.correct, 2_000)
        }

        assertTrue(engine.confusions(listOf(session)).isEmpty())
    }

    @Test
    fun `confusions aggregate across sessions`() {
        fun sessionMistaking(count: Int): BlitzSession {
            var s = engine.start(curriculum.id, BlitzMode.FullList, now = 0)
            curriculum.problems.filter { it.group == PatternGroup.DP_1D }.take(count).forEach {
                s = engine.submit(s, engine.cardFor(it, random), PatternGroup.GREEDY, 2_000)
            }
            return s
        }

        val confusions = engine.confusions(listOf(sessionMistaking(2), sessionMistaking(3)))

        assertEquals(5, confusions.first { it.actual == PatternGroup.DP_1D }.count)
    }
}
