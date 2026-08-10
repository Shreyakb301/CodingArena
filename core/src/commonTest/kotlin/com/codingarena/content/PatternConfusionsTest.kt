package com.codingarena.content

import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.model.PatternGroup
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PatternConfusionsTest {

    /**
     * The guarantee that makes Blitz a teacher rather than a quiz: no matter
     * which wrong option a user picks, an authored explanation exists.
     */
    @Test
    fun `every wrong answer a card can offer has an explanation`() {
        val engine = BlitzEngine()
        val missing = mutableSetOf<Pair<PatternGroup, PatternGroup>>()

        NeetCode150.problems.forEach { problem ->
            val card = engine.cardFor(problem, Random(1))
            card.options.filter { it != card.correct }.forEach { wrong ->
                if (PatternConfusions.between(card.correct, wrong) == null) {
                    missing += card.correct to wrong
                }
            }
        }

        assertTrue(missing.isEmpty(), "unexplained confusions: $missing")
    }

    @Test
    fun `every confusable edge is authored in both directions`() {
        val missing = mutableSetOf<Pair<PatternGroup, PatternGroup>>()

        PatternGroup.entries.forEach { group ->
            group.confusableWith.forEach { other ->
                if (PatternConfusions.between(group, other) == null) missing += group to other
                if (PatternConfusions.between(other, group) == null) missing += other to group
            }
        }

        assertTrue(missing.isEmpty(), "unexplained pairs: $missing")
    }

    @Test
    fun `lookup works in either order`() {
        val forward = PatternConfusions.between(
            PatternGroup.SLIDING_WINDOW, PatternGroup.TWO_POINTERS,
        )
        val backward = PatternConfusions.between(
            PatternGroup.TWO_POINTERS, PatternGroup.SLIDING_WINDOW,
        )

        assertNotNull(forward)
        assertEquals(forward, backward)
    }

    @Test
    fun `a group is never confusable with itself`() {
        assertNull(PatternConfusions.between(PatternGroup.GREEDY, PatternGroup.GREEDY))
    }

    @Test
    fun `pairs are unique - no duplicates in either direction`() {
        val seen = mutableSetOf<Set<PatternGroup>>()
        PatternConfusions.all.forEach {
            val key = setOf(it.first, it.second)
            assertTrue(seen.add(key), "duplicate pair: ${it.first} / ${it.second}")
        }
    }

    @Test
    fun `no pair names the same group twice`() {
        PatternConfusions.all.forEach {
            assertTrue(it.first != it.second, "self-pair: ${it.first}")
        }
    }

    @Test
    fun `every explanation names both patterns and their tells`() {
        PatternConfusions.all.forEach {
            val text = it.explanationFor(it.first, it.second)
            assertTrue(text.contains(it.first.displayName), "missing ${it.first} in: $text")
            assertTrue(text.contains(it.second.displayName), "missing ${it.second} in: $text")
            assertTrue(text.contains(it.tellFirst), "missing first tell")
            assertTrue(text.contains(it.tellSecond), "missing second tell")
        }
    }

    @Test
    fun `the explanation leads with the pattern the user should have picked`() {
        val confusion = PatternConfusions.between(
            PatternGroup.SLIDING_WINDOW, PatternGroup.TWO_POINTERS,
        )!!

        val text = confusion.explanationFor(
            actual = PatternGroup.SLIDING_WINDOW,
            mistakenFor = PatternGroup.TWO_POINTERS,
        )

        val windowLine = text.indexOf("${PatternGroup.SLIDING_WINDOW.displayName}:")
        val pointerLine = text.indexOf("${PatternGroup.TWO_POINTERS.displayName}:")
        assertTrue(windowLine in 0 until pointerLine, "correct pattern should be listed first")
    }

    @Test
    fun `explanations are substantial enough to teach something`() {
        PatternConfusions.all.forEach {
            assertTrue(
                it.distinction.length >= 80,
                "${it.first}/${it.second} distinction is too thin: ${it.distinction}",
            )
            assertTrue(it.tellFirst.isNotBlank() && it.tellSecond.isNotBlank())
        }
    }

    @Test
    fun `explain falls back rather than failing on an unauthored pair`() {
        // TRIES and INTERVALS are not confusable, so no pair is authored.
        val text = PatternConfusions.explain(PatternGroup.TRIES, PatternGroup.INTERVALS)

        assertTrue(text.contains("Tries"), text)
        assertTrue(text.isNotBlank())
    }

    @Test
    fun `a card gives the distinction on a wrong answer and confirms on a right one`() {
        val engine = BlitzEngine()
        val problem = NeetCode150.problems.first { it.group == PatternGroup.SLIDING_WINDOW }
        val card = engine.cardFor(problem, Random(3))

        val right = card.feedbackFor(card.correct)
        assertTrue(right.contains("Sliding Window"))

        val wrongChoice = card.options.first { it != card.correct }
        val wrong = card.feedbackFor(wrongChoice)
        assertTrue(wrong.contains(wrongChoice.displayName), wrong)
        assertTrue(wrong.length > right.length, "a miss should teach more than a hit")
    }

    @Test
    fun `every group has revision material`() {
        PatternGroup.entries.forEach { group ->
            assertTrue(
                PatternConfusions.involving(group).isNotEmpty(),
                "$group has no distinctions to revise",
            )
        }
    }
}
