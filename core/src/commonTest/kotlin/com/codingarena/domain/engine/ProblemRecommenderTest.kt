package com.codingarena.domain.engine

import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.UserTopicRating
import com.codingarena.problem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProblemRecommenderTest {

    private val recommender = ProblemRecommender()

    private val catalogue = listOf(
        problem(id = "arr-700", rating = 700, topic = CodingTopic.ARRAYS),
        problem(id = "arr-1000", rating = 1000, topic = CodingTopic.ARRAYS),
        problem(id = "arr-1300", rating = 1300, topic = CodingTopic.ARRAYS),
        problem(id = "graph-800", rating = 800, topic = CodingTopic.GRAPHS),
        problem(id = "graph-1200", rating = 1200, topic = CodingTopic.GRAPHS),
        problem(id = "tree-1100", rating = 1100, topic = CodingTopic.TREES),
    )

    @Test
    fun `after a solve the next problem steps up`() {
        val next = recommender.nextAfter(
            justAttempted = problem(id = "arr-1000", rating = 1000, topic = CodingTopic.ARRAYS),
            outcome = AnswerOutcome.CORRECT_NO_HINTS,
            userRating = 1000,
            candidates = catalogue,
        )

        assertEquals("arr-1300", next.first().problemId)
        assertTrue(next.first().reason.contains("higher level"))
    }

    @Test
    fun `after a miss the next problem steps down`() {
        val next = recommender.nextAfter(
            justAttempted = problem(id = "arr-1300", rating = 1300, topic = CodingTopic.ARRAYS),
            outcome = AnswerOutcome.INCORRECT,
            userRating = 1300,
            candidates = catalogue,
        )

        assertEquals("arr-1000", next.first().problemId)
        assertTrue(next.first().reason.contains("Rebuilds"))
    }

    @Test
    fun `recommendations never include the problem just attempted`() {
        val attempted = catalogue.first { it.id == "arr-1000" }

        val next = recommender.nextAfter(
            justAttempted = attempted,
            outcome = AnswerOutcome.CORRECT_NO_HINTS,
            userRating = 1000,
            candidates = catalogue,
        )

        assertTrue(next.none { it.problemId == "arr-1000" })
    }

    @Test
    fun `recommendations stay on topic when content exists`() {
        val next = recommender.nextAfter(
            justAttempted = problem(id = "graph-800", rating = 800, topic = CodingTopic.GRAPHS),
            outcome = AnswerOutcome.CORRECT_NO_HINTS,
            userRating = 800,
            candidates = catalogue,
        )

        assertTrue(next.all { it.topic == CodingTopic.GRAPHS }, "got ${next.map { it.topic }}")
    }

    @Test
    fun `an unrepresented topic falls back to the wider catalogue`() {
        val orphan = problem(id = "trie-1", rating = 1000, topic = CodingTopic.TRIES)

        val next = recommender.nextAfter(
            justAttempted = orphan,
            outcome = AnswerOutcome.CORRECT_NO_HINTS,
            userRating = 1000,
            candidates = catalogue,
        )

        assertTrue(next.isNotEmpty())
    }

    @Test
    fun `an empty catalogue recommends nothing rather than failing`() {
        assertTrue(
            recommender.nextAfter(problem(), AnswerOutcome.INCORRECT, 1000, emptyList()).isEmpty()
        )
    }

    @Test
    fun `the home list targets the weakest practised topics`() {
        val ratings = PlayerRatings(
            overall = 1100,
            topics = mapOf(
                CodingTopic.ARRAYS to UserTopicRating(CodingTopic.ARRAYS, 1400, 10, 9),
                CodingTopic.GRAPHS to UserTopicRating(CodingTopic.GRAPHS, 800, 10, 3),
            ),
        )

        val recommendations = recommender.recommendForUser(ratings, catalogue)

        assertEquals(CodingTopic.GRAPHS, recommendations.first().topic)
    }

    @Test
    fun `the reason names the size of the gap`() {
        val ratings = PlayerRatings(
            overall = 1200,
            topics = mapOf(
                CodingTopic.GRAPHS to UserTopicRating(CodingTopic.GRAPHS, 800, 10, 8),
            ),
        )

        val reason = recommender.recommendForUser(ratings, catalogue).first().reason

        assertTrue(reason.contains("400 points below"), reason)
    }

    @Test
    fun `an untouched topic is described as unpractised`() {
        val recommendations = recommender.recommendForUser(PlayerRatings(), catalogue)

        assertTrue(recommendations.isNotEmpty())
        assertTrue(
            recommendations.any { it.reason.contains("not practised") },
            recommendations.map { it.reason }.toString(),
        )
    }

    @Test
    fun `excluded problems are never recommended`() {
        val ratings = PlayerRatings(
            overall = 1000,
            topics = mapOf(CodingTopic.GRAPHS to UserTopicRating(CodingTopic.GRAPHS, 800, 5, 2)),
        )

        val recommendations = recommender.recommendForUser(
            ratings = ratings,
            candidates = catalogue,
            excludeProblemIds = setOf("graph-800", "graph-1200"),
        )

        assertTrue(recommendations.none { it.problemId.startsWith("graph") })
    }

    @Test
    fun `unpublished problems are never recommended`() {
        val hidden = catalogue.map { it.copy(isPublished = false) }

        assertTrue(recommender.recommendForUser(PlayerRatings(), hidden).isEmpty())
        assertNull(recommender.bestFit(CodingTopic.ARRAYS, 1000, hidden))
    }

    @Test
    fun `best fit picks the closest rating on the topic`() {
        val fit = recommender.bestFit(CodingTopic.ARRAYS, targetRating = 1250, candidates = catalogue)

        assertNotNull(fit)
        assertEquals("arr-1300", fit.id)
    }

    @Test
    fun `best fit honours exclusions`() {
        val fit = recommender.bestFit(
            topic = CodingTopic.ARRAYS,
            targetRating = 1250,
            candidates = catalogue,
            excludeProblemIds = setOf("arr-1300"),
        )

        assertEquals("arr-1000", fit?.id)
    }

    @Test
    fun `recommendations are deterministic for the same inputs`() {
        val ratings = PlayerRatings(overall = 1000)

        assertEquals(
            recommender.recommendForUser(ratings, catalogue),
            recommender.recommendForUser(ratings, catalogue),
        )
    }
}
