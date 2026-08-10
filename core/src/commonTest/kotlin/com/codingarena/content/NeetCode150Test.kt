package com.codingarena.content

import com.codingarena.domain.model.PatternGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Roadmap data integrity.
 *
 * The list is hand-transcribed, so these checks exist to catch the mistakes
 * transcription actually produces: duplicates, typos in slugs, and sections
 * silently losing entries.
 */
class NeetCode150Test {

    private val problems = NeetCode150.problems

    @Test
    fun `the list holds exactly 150 problems`() {
        assertEquals(150, problems.size)
    }

    @Test
    fun `slugs are unique`() {
        val duplicates = problems.groupBy { it.slug }.filterValues { it.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "duplicate slugs: $duplicates")
    }

    @Test
    fun `titles are unique`() {
        val duplicates = problems.groupBy { it.title }.filterValues { it.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "duplicate titles: $duplicates")
    }

    @Test
    fun `slugs look like real LeetCode slugs`() {
        val bad = problems.filterNot { it.slug.matches(Regex("[a-z0-9]+(-[a-z0-9]+)*")) }
        assertTrue(bad.isEmpty(), "malformed slugs: ${bad.map { it.slug }}")
    }

    @Test
    fun `every problem carries a one-line prompt`() {
        problems.forEach {
            assertTrue(it.ask.isNotBlank(), "${it.slug} has no prompt")
            assertTrue(it.ask.length <= 120, "${it.slug} prompt is too long to blitz: ${it.ask}")
        }
    }

    /**
     * Prompts are paraphrases written for this app. A prompt long enough to be
     * a real problem statement would suggest someone pasted one in.
     */
    @Test
    fun `prompts are paraphrases, not problem statements`() {
        val suspicious = problems.filter { it.ask.count { ch -> ch == '.' } > 2 }
        assertTrue(suspicious.isEmpty(), "overlong prompts: ${suspicious.map { it.slug }}")
    }

    @Test
    fun `every pattern group has problems`() {
        val covered = problems.map { it.group }.toSet()
        val missing = PatternGroup.entries - covered
        assertTrue(missing.isEmpty(), "empty groups: $missing")
    }

    @Test
    fun `section sizes add up to the whole list`() {
        assertEquals(150, NeetCode150.curriculum.sections.sumOf { it.problems.size })
    }

    @Test
    fun `sections come back in pattern-group order`() {
        val order = NeetCode150.curriculum.sections.map { it.group.ordinal }
        assertEquals(order.sorted(), order)
    }

    @Test
    fun `the blind 75 subset is drawn from the 150`() {
        val slugs = problems.map { it.slug }.toSet()
        assertTrue(NeetCode150.blind75.problems.all { it.slug in slugs })
    }

    /**
     * Transcribed by hand from a widely published list, so the count is pinned
     * here to catch a dropped or double-counted flag. If this ever fails, the
     * flags are what changed - check them against the canonical list rather
     * than editing the expectation.
     */
    @Test
    fun `the blind 75 subset is the size it claims`() {
        assertEquals(76, NeetCode150.blind75.problems.size)
    }

    @Test
    fun `the blind 75 subset spans most pattern groups`() {
        val groups = NeetCode150.blind75.problems.map { it.group }.distinct()
        assertTrue(groups.size >= 12, "Blind 75 only covers ${groups.size} groups")
    }

    @Test
    fun `urls are built from slugs`() {
        val twoSum = NeetCode150.curriculum.bySlug("two-sum")
        assertNotNull(twoSum)
        assertEquals("https://leetcode.com/problems/two-sum/", twoSum.url)
    }

    @Test
    fun `lookup by slug works and misses cleanly`() {
        assertNotNull(NeetCode150.curriculum.bySlug("valid-anagram"))
        assertEquals(null, NeetCode150.curriculum.bySlug("not-a-real-problem"))
    }

    @Test
    fun `every group maps to a pattern the library teaches, or to none deliberately`() {
        PatternGroup.entries.forEach { group ->
            val patternId = group.patternId ?: return@forEach
            assertNotNull(
                PatternLibrary.byId(patternId),
                "${group.name} points at unknown pattern $patternId",
            )
        }
    }

    @Test
    fun `difficulty spread is plausible for a curated list`() {
        val counts = problems.groupingBy { it.difficulty }.eachCount()
        counts.forEach { (difficulty, count) ->
            assertTrue(count >= 10, "only $count $difficulty problems")
        }
    }
}
