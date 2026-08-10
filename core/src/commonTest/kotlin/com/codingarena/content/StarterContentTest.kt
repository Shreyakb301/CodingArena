package com.codingarena.content

import com.codingarena.domain.model.ChallengeType
import com.codingarena.domain.model.CodingTopic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Content integrity. Authored data breaks in ways code does not, so these
 * checks run on every build rather than living in a review checklist.
 */
class StarterContentTest {

    private val problems = StarterContent.problems

    @Test
    fun `the starter set covers at least the twenty promised categories`() {
        assertTrue(problems.size >= 20, "only ${problems.size} problems")
        assertEquals(20, StarterContent.starterCategories.size)
    }

    @Test
    fun `problem ids are unique`() {
        val duplicates = problems.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "duplicate ids: $duplicates")
    }

    @Test
    fun `answer choice ids are unique within a problem`() {
        problems.forEach { p ->
            val ids = p.choices.map { it.id }
            assertEquals(ids.size, ids.distinct().size, "${p.id} has duplicate choice ids")
        }
    }

    @Test
    fun `every problem has at least three choices`() {
        problems.forEach { p ->
            assertTrue(p.choices.size >= 3, "${p.id} has only ${p.choices.size} choices")
        }
    }

    @Test
    fun `every correct answer id exists among the choices`() {
        problems.forEach { p ->
            p.correctAnswerIds.forEach { id ->
                assertNotNull(p.choice(id), "${p.id} references missing choice $id")
            }
        }
    }

    @Test
    fun `ordering problems list every choice exactly once as the answer`() {
        problems.filter { it.challengeType.isOrdering }.forEach { p ->
            assertEquals(
                p.choices.map { it.id }.sorted(),
                p.correctAnswerIds.sorted(),
                "${p.id} ordering answer does not cover every line",
            )
        }
    }

    @Test
    fun `non-ordering problems have exactly one correct answer`() {
        problems.filterNot { it.challengeType.isOrdering }.forEach { p ->
            assertEquals(1, p.correctAnswerIds.size, "${p.id} has multiple correct answers")
        }
    }

    @Test
    fun `every distractor explains why it fails`() {
        problems.filterNot { it.challengeType.isOrdering }.forEach { p ->
            p.choices.filter { it.id !in p.correctAnswerIds }.forEach { choice ->
                assertTrue(
                    !choice.rationale.isNullOrBlank(),
                    "${p.id} choice ${choice.id} has no rationale - the Solution Review would " +
                        "fall back to a generic explanation",
                )
            }
        }
    }

    @Test
    fun `every problem carries the fields the review screen renders`() {
        problems.forEach { p ->
            assertTrue(p.title.isNotBlank(), "${p.id} title")
            assertTrue(p.description.isNotBlank(), "${p.id} description")
            assertTrue(p.explanation.isNotBlank(), "${p.id} explanation")
            assertTrue(p.bestApproach.isNotBlank(), "${p.id} bestApproach")
            assertTrue(p.commonMistakes.isNotEmpty(), "${p.id} commonMistakes")
            assertTrue(p.hints.isNotEmpty(), "${p.id} hints")
            assertTrue(!p.timeComplexity.isNullOrBlank(), "${p.id} timeComplexity")
            assertTrue(!p.spaceComplexity.isNullOrBlank(), "${p.id} spaceComplexity")
        }
    }

    @Test
    fun `at least one distractor per problem credits the instinct behind it`() {
        problems.filterNot { it.challengeType.isOrdering }.forEach { p ->
            val withInsight = p.choices.count { !it.insight.isNullOrBlank() }
            assertTrue(
                withInsight >= 1,
                "${p.id} has no choice carrying an insight, so a wrong answer gets no good-move line",
            )
        }
    }

    @Test
    fun `every referenced pattern exists in the library`() {
        problems.mapNotNull { it.patternId }.distinct().forEach { id ->
            assertNotNull(PatternLibrary.byId(id), "problem references unknown pattern $id")
        }
    }

    /**
     * A pattern with a lesson but no practice is a dead end: the recommender
     * cannot target it and the learning path engine cannot build a step from
     * it. This shipped broken for five patterns once already.
     */
    @Test
    fun `every pattern has at least one practice problem`() {
        val empty = PatternLibrary.patterns.filter { it.allProblemIds.isEmpty() }.map { it.id }

        assertTrue(empty.isEmpty(), "patterns with no practice problems: $empty")
    }

    /**
     * Every topic a user can claim to know during onboarding must be
     * practisable, or their rating on it can never move off the seed.
     */
    @Test
    fun `every onboarding topic has content`() {
        val covered = problems.flatMap { it.allTopics }.toSet()
        val missing = CodingTopic.subjectTopics.filterNot { it in covered }

        assertTrue(missing.isEmpty(), "topics with no problems: $missing")
    }

    @Test
    fun `every pattern topic is practisable`() {
        val covered = problems.flatMap { it.allTopics }.toSet()
        val orphans = PatternLibrary.patterns
            .map { it.topic }
            .distinct()
            .filterNot { it in covered }

        assertTrue(orphans.isEmpty(), "patterns teaching unpractisable topics: $orphans")
    }

    @Test
    fun `every challenge type has more than a single example`() {
        val thin = ChallengeType.entries
            .associateWith { type -> problems.count { it.challengeType == type } }
            .filterValues { it < 2 }

        assertTrue(thin.isEmpty(), "challenge types with fewer than two problems: $thin")
    }

    @Test
    fun `every problem referenced by a pattern exists`() {
        PatternLibrary.patterns.forEach { pattern ->
            pattern.allProblemIds.forEach { id ->
                assertNotNull(
                    StarterContent.byId(id),
                    "pattern ${pattern.id} references unknown problem $id",
                )
            }
        }
    }

    @Test
    fun `difficulty ratings sit in a sane band`() {
        problems.forEach { p ->
            assertTrue(
                p.difficultyRating in 600..2000,
                "${p.id} rating ${p.difficultyRating} is outside the authored range",
            )
        }
    }

    @Test
    fun `every challenge type has content`() {
        val covered = problems.map { it.challengeType }.toSet()
        val missing = ChallengeType.entries - covered
        assertTrue(missing.isEmpty(), "no problems for: $missing")
    }

    @Test
    fun `the core interview topics all have content`() {
        val covered = problems.flatMap { it.allTopics }.toSet()
        val required = listOf(
            CodingTopic.ARRAYS,
            CodingTopic.STRINGS,
            CodingTopic.HASH_MAPS,
            CodingTopic.LINKED_LISTS,
            CodingTopic.STACKS,
            CodingTopic.QUEUES,
            CodingTopic.TREES,
            CodingTopic.GRAPHS,
            CodingTopic.RECURSION,
            CodingTopic.BINARY_SEARCH,
            CodingTopic.SLIDING_WINDOW,
            CodingTopic.TWO_POINTERS,
            CodingTopic.SORTING,
            CodingTopic.COMPLEXITY,
            CodingTopic.DEBUGGING,
            CodingTopic.HEAPS,
            CodingTopic.DYNAMIC_PROGRAMMING,
        )
        val missing = required - covered
        assertTrue(missing.isEmpty(), "no problems for: $missing")
    }

    @Test
    fun `hint counts stay within the three the schema allows`() {
        problems.forEach { p ->
            assertTrue(p.hints.size <= 3, "${p.id} has ${p.hints.size} hints")
        }
    }

    @Test
    fun `the daily puzzle is stable for a day and rotates across days`() {
        assertEquals(
            StarterContent.dailyPuzzleFor(20_000).id,
            StarterContent.dailyPuzzleFor(20_000).id,
        )
        val week = (20_000L..20_006L).map { StarterContent.dailyPuzzleFor(it).id }
        assertEquals(week.size, week.distinct().size, "the same puzzle repeated within a week")
    }

    @Test
    fun `the daily puzzle handles negative epoch days`() {
        assertNotNull(StarterContent.dailyPuzzleFor(-1))
        assertNotNull(StarterContent.dailyPuzzleFor(-9_999))
    }

    @Test
    fun `enough short problems exist for code rush`() {
        val rushable = problems.count { it.estimatedSeconds <= 45 }
        assertTrue(rushable >= 10, "only $rushable problems are short enough for Code Rush")
    }

    @Test
    fun `achievement ids are unique`() {
        val ids = AchievementCatalogue.achievements.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `pattern ids are unique and every pattern is fully authored`() {
        val ids = PatternLibrary.patterns.map { it.id }
        assertEquals(ids.size, ids.distinct().size)

        PatternLibrary.patterns.forEach { p ->
            assertTrue(p.summary.isNotBlank(), "${p.id} summary")
            assertTrue(p.whenToUse.isNotBlank(), "${p.id} whenToUse")
            assertTrue(p.recognitionSignals.isNotEmpty(), "${p.id} recognitionSignals")
            assertTrue(p.visualExample.isNotBlank(), "${p.id} visualExample")
            assertTrue(p.codeTemplate.isNotBlank(), "${p.id} codeTemplate")
            assertTrue(p.commonMistakes.isNotEmpty(), "${p.id} commonMistakes")
        }
    }
}
