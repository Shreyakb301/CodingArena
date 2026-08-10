package com.codingarena.data

import com.codingarena.content.StarterContent
import com.codingarena.core.common.FixedTimeProvider
import com.codingarena.core.common.SequentialIdGenerator
import com.codingarena.core.database.DatabaseDriverFactory
import com.codingarena.data.repository.LocalLearningPathRepository
import com.codingarena.data.repository.LocalProblemRepository
import com.codingarena.data.repository.LocalProfileRepository
import com.codingarena.data.repository.LocalRatingRepository
import com.codingarena.data.repository.LocalStreakRepository
import com.codingarena.db.ArenaDatabase
import com.codingarena.domain.engine.PlacementAnswer
import com.codingarena.domain.engine.PlacementTestEngine
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.ExperienceLevel
import com.codingarena.domain.model.OnboardingAnswers
import com.codingarena.domain.usecase.CompleteOnboardingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The placement test end to end: answers in, seeded profile and path out. */
class PlacementOnboardingTest {

    private val io = Dispatchers.Unconfined
    private val time = FixedTimeProvider(millis = 1_700_000_000_000)
    private val engine = PlacementTestEngine()

    private class Env(db: ArenaDatabase, io: kotlinx.coroutines.CoroutineDispatcher) {
        val problems = LocalProblemRepository(db, io)
        val profiles = LocalProfileRepository(db, io)
        val ratings = LocalRatingRepository(db, io)
        val learningPaths = LocalLearningPathRepository(db, io)
        val streaks = LocalStreakRepository(db, io)
    }

    private suspend fun env(): Env {
        val env = Env(ArenaDatabase(DatabaseDriverFactory(path = null).create()), io)
        env.problems.upsertAll(StarterContent.problems)
        return env
    }

    private fun onboarding(env: Env) = CompleteOnboardingUseCase(
        profiles = env.profiles,
        ratings = env.ratings,
        problems = env.problems,
        learningPaths = env.learningPaths,
        streaks = env.streaks,
        time = time,
        ids = SequentialIdGenerator(),
    )

    @Test
    fun `a strong placement beats the self-reported experience level`() = runTest {
        val env = env()
        val test = engine.buildTest(StarterContent.problems)
        val placement = engine.estimate(
            test.map { PlacementAnswer(it, it.correctAnswerIds) },
            baseline = ExperienceLevel.NEW_TO_CODING.startingRating,
        )

        val profile = onboarding(env)(
            displayName = "Sam",
            answers = OnboardingAnswers(experienceLevel = ExperienceLevel.NEW_TO_CODING),
            placement = placement,
        )

        // Self-reported "new to coding" would have started them at 700.
        assertTrue(profile.startingRating > ExperienceLevel.NEW_TO_CODING.startingRating)
        assertEquals(placement.estimatedRating, env.ratings.load(profile.id).overall)
        assertEquals(placement.estimatedRating, profile.onboarding.placementRating)
    }

    @Test
    fun `topics proved in the placement test seed higher than untested ones`() = runTest {
        val env = env()
        val test = engine.buildTest(StarterContent.problems)
        val placement = engine.estimate(test.map { PlacementAnswer(it, it.correctAnswerIds) })

        val profile = onboarding(env)(
            displayName = "Sam",
            answers = OnboardingAnswers(),
            placement = placement,
        )

        val ratings = env.ratings.load(profile.id)
        val proved = placement.strongTopics.first()
        val untested = ratings.topics.keys.first { it !in placement.strongTopics }

        assertTrue(
            ratings.topicRating(proved) > ratings.topicRating(untested),
            "proved=${ratings.topicRating(proved)} untested=${ratings.topicRating(untested)}",
        )
    }

    @Test
    fun `the first learning path targets a topic the placement test exposed`() = runTest {
        val env = env()
        val test = engine.buildTest(StarterContent.problems)
        // Miss everything, so every sampled topic reads as weak.
        val placement = engine.estimate(test.map { wrong(it) })

        val profile = onboarding(env)(
            displayName = "Sam",
            answers = OnboardingAnswers(),
            placement = placement,
        )

        val path = env.learningPaths.active(profile.id)
        assertNotNull(path)
        assertTrue(
            path.targetTopic in placement.weakTopics,
            "path targeted ${path.targetTopic}, weak were ${placement.weakTopics}",
        )
    }

    @Test
    fun `skipping the placement test still produces a usable profile`() = runTest {
        val env = env()

        val profile = onboarding(env)(
            displayName = "Sam",
            answers = OnboardingAnswers(experienceLevel = ExperienceLevel.COMFORTABLE),
            placement = null,
        )

        assertEquals(ExperienceLevel.COMFORTABLE.startingRating, profile.startingRating)
        assertNotNull(env.learningPaths.active(profile.id))
    }

    @Test
    fun `a weak placement lands below the claimed experience level`() = runTest {
        val env = env()
        val test = engine.buildTest(StarterContent.problems)
        val placement = engine.estimate(
            test.map { wrong(it) },
            baseline = ExperienceLevel.INTERVIEW_READY.startingRating,
        )

        val profile = onboarding(env)(
            displayName = "Sam",
            answers = OnboardingAnswers(experienceLevel = ExperienceLevel.INTERVIEW_READY),
            placement = placement,
        )

        assertTrue(profile.startingRating < ExperienceLevel.INTERVIEW_READY.startingRating)
    }

    private fun wrong(problem: CodingProblem) = PlacementAnswer(
        problem,
        listOf(problem.choices.first { it.id !in problem.correctAnswerIds }.id),
    )
}
