package com.codingarena.domain.engine

import com.codingarena.content.StarterContent
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.ExperienceLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlacementTestEngineTest {

    private val engine = PlacementTestEngine()
    private val catalogue = StarterContent.problems

    @Test
    fun `the test is the requested length`() {
        assertEquals(8, engine.buildTest(catalogue).size)
        assertEquals(5, engine.buildTest(catalogue, questionCount = 5).size)
    }

    @Test
    fun `the length is clamped to the five to ten the spec allows`() {
        assertEquals(5, engine.buildTest(catalogue, questionCount = 1).size)
        assertEquals(10, engine.buildTest(catalogue, questionCount = 50).size)
    }

    @Test
    fun `every placement area is represented`() {
        val test = engine.buildTest(catalogue)

        PlacementArea.entries.forEach { area ->
            assertTrue(
                test.any { area.matches(it) },
                "no question covering ${area.displayName}",
            )
        }
    }

    @Test
    fun `questions are ordered easiest first`() {
        val ratings = engine.buildTest(catalogue).map { it.difficultyRating }

        assertEquals(ratings.sorted(), ratings)
    }

    @Test
    fun `the same question never appears twice`() {
        val ids = engine.buildTest(catalogue, questionCount = 10).map { it.id }

        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `ordering problems are excluded - they are too slow for a placement test`() {
        assertTrue(engine.buildTest(catalogue, 10).none { it.challengeType.isOrdering })
    }

    @Test
    fun `an empty catalogue yields no test rather than failing`() {
        assertTrue(engine.buildTest(emptyList()).isEmpty())
    }

    @Test
    fun `skipping the test falls back to the experience-level estimate`() {
        val result = engine.estimate(emptyList(), baseline = 1150)

        assertEquals(1150, result.estimatedRating)
        assertEquals(0, result.totalCount)
        assertTrue(result.summary.contains("experience level"))
    }

    @Test
    fun `answering everything correctly raises the estimate`() {
        val test = engine.buildTest(catalogue)
        val result = engine.estimate(test.map { correct(it) }, baseline = 1000)

        assertTrue(result.estimatedRating > 1000, "got ${result.estimatedRating}")
        assertEquals(test.size, result.correctCount)
        assertTrue(result.weakTopics.isEmpty())
    }

    @Test
    fun `answering everything wrongly lowers the estimate`() {
        val test = engine.buildTest(catalogue)
        val result = engine.estimate(test.map { wrong(it) }, baseline = 1000)

        assertTrue(result.estimatedRating < 1000, "got ${result.estimatedRating}")
        assertEquals(0, result.correctCount)
        assertTrue(result.strongTopics.isEmpty())
    }

    @Test
    fun `a stronger performance always estimates higher than a weaker one`() {
        val test = engine.buildTest(catalogue)

        val allWrong = engine.estimate(test.map { wrong(it) }).estimatedRating
        val half = engine.estimate(
            test.mapIndexed { i, p -> if (i % 2 == 0) correct(p) else wrong(p) }
        ).estimatedRating
        val allRight = engine.estimate(test.map { correct(it) }).estimatedRating

        assertTrue(allWrong < half, "allWrong=$allWrong half=$half")
        assertTrue(half < allRight, "half=$half allRight=$allRight")
    }

    @Test
    fun `getting a hard question right is worth more than an easy one`() {
        val easy = catalogue.minBy { it.difficultyRating }
        val hard = catalogue.maxBy { it.difficultyRating }

        val fromEasy = engine.estimate(listOf(correct(easy)), baseline = 1000).estimatedRating
        val fromHard = engine.estimate(listOf(correct(hard)), baseline = 1000).estimatedRating

        assertTrue(fromHard > fromEasy, "hard=$fromHard easy=$fromEasy")
    }

    @Test
    fun `estimates stay inside the configured band`() {
        val test = engine.buildTest(catalogue, 10)

        val floor = engine.estimate(test.map { wrong(it) }, baseline = 600).estimatedRating
        val ceiling = engine.estimate(test.map { correct(it) }, baseline = 1800).estimatedRating

        assertTrue(floor >= 600, "floor was $floor")
        assertTrue(ceiling <= 1800, "ceiling was $ceiling")
    }

    @Test
    fun `missed topics are reported as weak and named in the summary`() {
        val test = engine.buildTest(catalogue)
        // Pick a question whose topic is not also covered by another question,
        // otherwise answering that other one correctly clears the weak flag -
        // which is the behaviour the next test pins down.
        val soleIndex = test.indices.first { i ->
            test.count { it.primaryTopic == test[i].primaryTopic } == 1
        }

        val result = engine.estimate(
            test.mapIndexed { i, p -> if (i == soleIndex) wrong(p) else correct(p) }
        )

        val missed = test[soleIndex].primaryTopic
        assertTrue(missed in result.weakTopics, "weak=${result.weakTopics}")
        assertTrue(result.summary.contains(missed.displayName), result.summary)
    }

    @Test
    fun `a topic answered both right and wrong does not count as weak`() {
        val topic = catalogue.first().primaryTopic
        val onTopic = catalogue.filter { it.primaryTopic == topic }.take(2)
        if (onTopic.size < 2) return

        val result = engine.estimate(listOf(correct(onTopic[0]), wrong(onTopic[1])))

        assertTrue(topic in result.strongTopics)
        assertTrue(topic !in result.weakTopics)
    }

    @Test
    fun `the summary always reports the score`() {
        val test = engine.buildTest(catalogue)
        val result = engine.estimate(test.map { correct(it) })

        assertTrue(result.summary.contains("${test.size}"), result.summary)
        assertEquals(1.0, result.accuracy)
    }

    @Test
    fun `experience level baselines land in different bands`() {
        val test = engine.buildTest(catalogue)
        val answers = test.mapIndexed { i, p -> if (i % 2 == 0) correct(p) else wrong(p) }

        val novice = engine.estimate(answers, ExperienceLevel.NEW_TO_CODING.startingRating)
        val ready = engine.estimate(answers, ExperienceLevel.INTERVIEW_READY.startingRating)

        assertTrue(novice.estimatedRating < ready.estimatedRating)
    }

    private fun correct(problem: CodingProblem) =
        PlacementAnswer(problem, problem.correctAnswerIds)

    private fun wrong(problem: CodingProblem) = PlacementAnswer(
        problem,
        listOf(problem.choices.first { it.id !in problem.correctAnswerIds }.id),
    )
}
