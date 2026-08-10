package com.codingarena.domain.engine

import com.codingarena.attempt
import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.CodingPattern
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PatternCategory
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.StepKind
import com.codingarena.domain.model.UserTopicRating
import com.codingarena.problem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LearningPathEngineTest {

    private val engine = LearningPathEngine()

    private val catalogue = buildList {
        listOf(700, 850, 1000, 1150, 1300, 1450).forEachIndexed { i, rating ->
            add(problem(id = "graph-$i", rating = rating, topic = CodingTopic.GRAPHS))
            add(problem(id = "array-$i", rating = rating, topic = CodingTopic.ARRAYS))
        }
    }

    private val patterns = listOf(
        CodingPattern(
            id = "graph-traversal",
            name = "Graph Traversal",
            category = PatternCategory.GRAPHS,
            topic = CodingTopic.GRAPHS,
            summary = "Visit every reachable node once.",
            whenToUse = "Connected components, reachability.",
            recognitionSignals = listOf("grid", "neighbours"),
            visualExample = "A - B - C",
            codeTemplate = "fun dfs(node: Int) { }",
            commonMistakes = listOf("Forgetting the visited set"),
            timeComplexity = "O(V + E)",
            spaceComplexity = "O(V)",
        ),
    )

    @Test
    fun `the weakest practised topic is targeted`() {
        val ratings = ratings(CodingTopic.GRAPHS to 800, CodingTopic.ARRAYS to 1400)

        assertEquals(CodingTopic.GRAPHS, engine.selectTargetTopic(ratings, LearningSignals(), catalogue))
    }

    @Test
    fun `repeated recent failures outweigh a low rating elsewhere`() {
        val ratings = ratings(CodingTopic.GRAPHS to 800, CodingTopic.ARRAYS to 1400)
        val signals = LearningSignals(
            recentAttempts = listOf(
                attempt(problemId = "array-0", outcome = AnswerOutcome.INCORRECT),
                attempt(problemId = "array-1", outcome = AnswerOutcome.INCORRECT),
            ),
        )

        assertEquals(CodingTopic.ARRAYS, engine.selectTargetTopic(ratings, signals, catalogue))
    }

    @Test
    fun `a path follows the lesson to mastery shape`() {
        val ratings = ratings(CodingTopic.GRAPHS to 1000)
        val path = engine.buildPath(
            pathId = "path-1",
            userId = "user-1",
            topic = CodingTopic.GRAPHS,
            ratings = ratings,
            signals = LearningSignals(repeatedFailures = setOf("graph-1")),
            available = catalogue,
            patterns = patterns,
            now = 100L,
        )

        assertNotNull(path)
        assertEquals(
            listOf(
                StepKind.LESSON,
                StepKind.EASY_PRACTICE,
                StepKind.MEDIUM_PRACTICE,
                StepKind.REVIEW,
                StepKind.MASTERY,
            ),
            path.steps.map { it.kind },
        )
        assertEquals(CodingTopic.GRAPHS, path.targetTopic)
    }

    @Test
    fun `a path never repeats the same problem across steps`() {
        val path = engine.buildPath(
            "path-1", "user-1", CodingTopic.GRAPHS,
            ratings(CodingTopic.GRAPHS to 1000), LearningSignals(),
            catalogue, patterns, now = 0L,
        )

        assertNotNull(path)
        val practiceIds = path.steps
            .filter { it.kind != StepKind.REVIEW }
            .flatMap { it.problemIds }
        assertEquals(practiceIds.size, practiceIds.distinct().size)
    }

    @Test
    fun `practice steps escalate in difficulty`() {
        val path = engine.buildPath(
            "path-1", "user-1", CodingTopic.GRAPHS,
            ratings(CodingTopic.GRAPHS to 1000), LearningSignals(),
            catalogue, patterns, now = 0L,
        )!!

        fun averageRating(kind: StepKind): Double = path.steps
            .first { it.kind == kind }
            .problemIds
            .mapNotNull { id -> catalogue.firstOrNull { it.id == id }?.difficultyRating }
            .average()

        assertTrue(averageRating(StepKind.EASY_PRACTICE) < averageRating(StepKind.MEDIUM_PRACTICE))
        assertTrue(averageRating(StepKind.MEDIUM_PRACTICE) < averageRating(StepKind.MASTERY))
    }

    @Test
    fun `no path is built for a topic with no content`() {
        assertNull(
            engine.buildPath(
                "path-1", "user-1", CodingTopic.TRIES,
                PlayerRatings(), LearningSignals(), catalogue, patterns, now = 0L,
            )
        )
    }

    @Test
    fun `completing problems advances the path`() {
        var path = engine.buildPath(
            "path-1", "user-1", CodingTopic.GRAPHS,
            ratings(CodingTopic.GRAPHS to 1000), LearningSignals(),
            catalogue, patterns, now = 0L,
        )!!

        assertEquals(StepKind.LESSON, path.currentStep?.kind)
        path = engine.completeLesson(path, path.steps.first().id, now = 10L)
        assertEquals(StepKind.EASY_PRACTICE, path.currentStep?.kind)

        val easy = path.steps.first { it.kind == StepKind.EASY_PRACTICE }
        easy.problemIds.forEach { path = engine.recordCompletion(path, it, now = 20L) }

        assertEquals(StepKind.MEDIUM_PRACTICE, path.currentStep?.kind)
        assertTrue(path.fraction > 0f && path.fraction < 1f)
    }

    @Test
    fun `a path completes once every step is done`() {
        var path = engine.buildPath(
            "path-1", "user-1", CodingTopic.GRAPHS,
            ratings(CodingTopic.GRAPHS to 1000), LearningSignals(),
            catalogue, patterns, now = 0L,
        )!!

        path = engine.completeLesson(path, path.steps.first().id, now = 1L)
        path.steps.flatMap { it.problemIds }.distinct().forEach {
            path = engine.recordCompletion(path, it, now = 2L)
        }

        assertTrue(path.isComplete)
        assertEquals(2L, path.completedAt)
        assertNull(path.currentStep)
    }

    @Test
    fun `an untouched topic gets an unknown-territory rationale`() {
        val text = engine.rationale(CodingTopic.TRIES, PlayerRatings(), LearningSignals())

        assertTrue(text.contains("not practised"), text)
    }

    @Test
    fun `repeated failures produce a pattern-not-clicked rationale`() {
        val text = engine.rationale(
            CodingTopic.SLIDING_WINDOW,
            ratings(CodingTopic.SLIDING_WINDOW to 900),
            LearningSignals(repeatedFailures = setOf("a", "b")),
        )

        assertTrue(text.contains("more than once"), text)
    }

    @Test
    fun `a big gap to the overall rating is quantified in the rationale`() {
        val text = engine.rationale(
            CodingTopic.GRAPHS,
            ratings(CodingTopic.GRAPHS to 800).copy(overall = 1200),
            LearningSignals(),
        )

        assertTrue(text.contains("400 points below"), text)
    }

    private fun ratings(vararg entries: Pair<CodingTopic, Int>): PlayerRatings = PlayerRatings(
        overall = 1000,
        topics = entries.associate { (topic, rating) ->
            topic to UserTopicRating(topic, rating, attempts = 8, correctAnswers = 5)
        },
    )
}
