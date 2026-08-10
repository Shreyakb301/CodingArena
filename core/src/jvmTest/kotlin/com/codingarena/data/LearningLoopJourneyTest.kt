package com.codingarena.data

import com.codingarena.content.AchievementCatalogue
import com.codingarena.content.StarterContent
import com.codingarena.core.common.FixedTimeProvider
import com.codingarena.core.common.SequentialIdGenerator
import com.codingarena.core.database.DatabaseDriverFactory
import com.codingarena.data.repository.LocalAchievementRepository
import com.codingarena.data.repository.LocalAttemptRepository
import com.codingarena.data.repository.LocalCodeRushRepository
import com.codingarena.data.repository.LocalDailyPuzzleRepository
import com.codingarena.data.repository.LocalLearningPathRepository
import com.codingarena.data.repository.LocalProblemRepository
import com.codingarena.data.repository.LocalProfileRepository
import com.codingarena.data.repository.LocalRatingRepository
import com.codingarena.data.repository.LocalReviewRepository
import com.codingarena.data.repository.LocalSettingsRepository
import com.codingarena.data.repository.LocalStreakRepository
import com.codingarena.db.ArenaDatabase
import com.codingarena.domain.engine.AchievementEngine
import com.codingarena.domain.engine.SpacedRepetitionEngine
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.ExperienceLevel
import com.codingarena.domain.model.OnboardingAnswers
import com.codingarena.domain.session.ChallengeSession
import com.codingarena.domain.usecase.CompleteOnboardingUseCase
import com.codingarena.domain.usecase.EnsureDailyPuzzleUseCase
import com.codingarena.domain.usecase.GetHomeSnapshotUseCase
import com.codingarena.domain.usecase.StartAppUseCase
import com.codingarena.domain.usecase.StartDestination
import com.codingarena.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The product's flagship loop, walked end to end:
 *
 *   solve -> review -> understand -> practise weakness -> improve rating
 *
 * Everything below the Compose layer participates, so this is the test that
 * fails loudest if the pieces stop fitting together.
 */
class LearningLoopJourneyTest {

    private val io = Dispatchers.Unconfined
    private val time = FixedTimeProvider(millis = 1_700_000_000_000)
    private val ids = SequentialIdGenerator()

    private class Env(db: ArenaDatabase, io: kotlinx.coroutines.CoroutineDispatcher) {
        val problems = LocalProblemRepository(db, io)
        val profiles = LocalProfileRepository(db, io)
        val ratings = LocalRatingRepository(db, io)
        val attempts = LocalAttemptRepository(db, io)
        val reviews = LocalReviewRepository(db, io)
        val streaks = LocalStreakRepository(db, io)
        val achievements = LocalAchievementRepository(db, io)
        val dailyPuzzles = LocalDailyPuzzleRepository(db, io)
        val learningPaths = LocalLearningPathRepository(db, io)
        val codeRush = LocalCodeRushRepository(db, io)
        val settings = LocalSettingsRepository(db, io)
    }

    private fun env() = Env(ArenaDatabase(DatabaseDriverFactory(path = null).create()), io)

    private fun submit(env: Env) = SubmitAnswerUseCase(
        problems = env.problems,
        profiles = env.profiles,
        attempts = env.attempts,
        ratings = env.ratings,
        reviews = env.reviews,
        streaks = env.streaks,
        achievements = env.achievements,
        dailyPuzzles = env.dailyPuzzles,
        learningPaths = env.learningPaths,
        codeRush = env.codeRush,
        achievementEngine = AchievementEngine(AchievementCatalogue.achievements),
        time = time,
        ids = ids,
    )

    private fun home(env: Env) = GetHomeSnapshotUseCase(
        profiles = env.profiles,
        problems = env.problems,
        ratings = env.ratings,
        streaks = env.streaks,
        reviews = env.reviews,
        attempts = env.attempts,
        learningPaths = env.learningPaths,
        dailyPuzzles = env.dailyPuzzles,
        ensureDailyPuzzle = EnsureDailyPuzzleUseCase(env.dailyPuzzles, env.problems, time),
        time = time,
    )

    @Test
    fun `a new user can go from install to a measurably improved rating`() = runTest {
        val env = env()
        val session = ChallengeSession()

        // --- Day 0: install -----------------------------------------------
        val destination = StartAppUseCase(env.problems, env.profiles, env.settings)()
        assertTrue(destination is StartDestination.Onboarding)
        assertEquals(StarterContent.problems.size, env.problems.all().size)

        val profile = CompleteOnboardingUseCase(
            profiles = env.profiles,
            ratings = env.ratings,
            problems = env.problems,
            learningPaths = env.learningPaths,
            streaks = env.streaks,
            time = time,
            ids = ids,
        )(
            displayName = "Sam",
            answers = OnboardingAnswers(experienceLevel = ExperienceLevel.SOME_PRACTICE),
        )
        val userId = profile.id
        val startingRating = env.ratings.load(userId).overall

        // The home screen has something to show before any problem is solved.
        val firstVisit = home(env)(userId)
        assertNotNull(firstVisit.dailyPuzzle)
        assertNotNull(firstVisit.learningPath)
        assertEquals(0, firstVisit.streak.currentStreak)

        // --- Day 0: solve the Daily Puzzle through the challenge session ---
        val puzzle = firstVisit.dailyPuzzle!!
        var challenge = session.start(puzzle, time.nowMillis())
        challenge = if (puzzle.challengeType.isOrdering) {
            challenge.copy(selected = puzzle.correctAnswerIds)
        } else {
            session.select(challenge, puzzle.correctAnswerId)
        }
        val submission = session.toSubmission(challenge, userId, AttemptSource.DAILY_PUZZLE)
        assertNotNull(submission)

        time.advanceMinutes(1)
        val result = submit(env)(submission)

        // Solve -> review: the review is specific, not boilerplate.
        assertTrue(result.review.label.isPositive)
        assertTrue(result.review.whyItMatters.isNotBlank())
        assertTrue(result.review.bestMove.isNotBlank())
        assertTrue(result.ratingUpdate.overallChange > 0)
        assertTrue(result.streakExtended)
        assertTrue(result.newAchievements.any { it.id == "first-challenge" })

        // ... and it schedules the problem to come back.
        val scheduled = env.reviews.forProblem(userId, puzzle.id)
        assertNotNull(scheduled)
        assertTrue(scheduled.dueAt > time.nowMillis())

        // --- The home screen reflects all of it ----------------------------
        val afterSolving = home(env)(userId)
        assertEquals(1, afterSolving.streak.currentStreak)
        assertNotNull(afterSolving.dailyPuzzleResult)
        assertTrue(afterSolving.ratings.overall > startingRating)
        assertTrue(afterSolving.stats.totalCompleted == 1)

        // --- Days 1-4: keep practising -------------------------------------
        val extras = StarterContent.problems.filter { it.id != puzzle.id }.take(4)
        extras.forEach { problem ->
            time.advanceDays(1)
            var next = session.start(problem, time.nowMillis())
            next = if (problem.challengeType.isOrdering) {
                next.copy(selected = problem.correctAnswerIds)
            } else {
                session.select(next, problem.correctAnswerId)
            }
            time.advanceMinutes(1)
            submit(env)(session.toSubmission(next, userId, AttemptSource.DAILY_PUZZLE)!!)
        }

        // --- Improvement is measurable -------------------------------------
        val finalState = home(env)(userId)
        assertEquals(5, finalState.streak.currentStreak)
        assertEquals(5, finalState.stats.totalCompleted)
        assertEquals(1.0, finalState.stats.accuracy)
        assertTrue(
            finalState.ratings.overall > startingRating,
            "rating went from $startingRating to ${finalState.ratings.overall}",
        )
        assertTrue(env.ratings.overallHistory(userId).size == 5)
        assertTrue(finalState.ratings.topics.values.any { it.attempts > 0 })

        // Everything is queued for upload, because nothing has synced.
        assertEquals(5, env.attempts.unsynced().size)
    }

    @Test
    fun `a struggling user is steered toward the topic they keep missing`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val userId = "u1"
        env.profiles.save(
            com.codingarena.domain.model.UserProfile(id = userId, displayName = "Sam")
        )

        // Fail the same topic repeatedly. Ordering problems are excluded because
        // every choice belongs to their answer, so there is no "wrong" option
        // to pick.
        val target = StarterContent.problems
            .filterNot { it.challengeType.isOrdering }
            .groupBy { it.primaryTopic }
            .maxBy { it.value.size }
        val topic = target.key

        target.value.take(3).forEach { problem ->
            time.advanceMinutes(5)
            submit(env)(
                com.codingarena.domain.usecase.AnswerSubmission(
                    userId = userId,
                    problem = problem,
                    selectedAnswerIds = listOf(
                        problem.choices.first { it.id !in problem.correctAnswerIds }.id
                    ),
                    startedAt = time.nowMillis() - 30_000,
                )
            )
        }

        val ratings = env.ratings.load(userId)
        assertTrue(
            ratings.topicRating(topic) < ratings.overall + 1,
            "the failed topic should not sit above the overall rating",
        )

        // Every failure is queued to come back tomorrow, weakest first.
        val due = SpacedRepetitionEngine().dueReviews(
            env.reviews.all(userId),
            time.nowMillis() + 2 * com.codingarena.core.common.MILLIS_PER_DAY,
        )
        assertEquals(3, due.size)
        assertTrue(due.all { it.lapses == 1 })

        // The review screen points at easier work on the same topic.
        val lastReview = submit(env)(
            com.codingarena.domain.usecase.AnswerSubmission(
                userId = userId,
                problem = target.value.first(),
                selectedAnswerIds = listOf(
                    target.value.first().choices
                        .first { it.id !in target.value.first().correctAnswerIds }.id
                ),
                startedAt = time.nowMillis() - 30_000,
            )
        ).review

        assertTrue(lastReview.recommendedPractice.isNotEmpty())
        assertTrue(lastReview.recommendedPractice.all { it.problemId != target.value.first().id })
    }

    @Test
    fun `hints and retries hold the rating back compared with a clean solve`() = runTest {
        val cleanRating = ratingAfter(hints = 0, attempts = 1)
        val hintedRating = ratingAfter(hints = 1, attempts = 1)
        val retriedRating = ratingAfter(hints = 0, attempts = 3)

        assertTrue(cleanRating > hintedRating, "clean=$cleanRating hinted=$hintedRating")
        assertTrue(hintedRating > retriedRating, "hinted=$hintedRating retried=$retriedRating")
    }

    private suspend fun ratingAfter(hints: Int, attempts: Int): Int {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val problem: CodingProblem = StarterContent.problems.first { !it.challengeType.isOrdering }

        submit(env)(
            com.codingarena.domain.usecase.AnswerSubmission(
                userId = "u1",
                problem = problem,
                selectedAnswerIds = listOf(problem.correctAnswerId),
                startedAt = time.nowMillis() - 20_000,
                hintsUsed = hints,
                attemptsCount = attempts,
            )
        )
        return env.ratings.load("u1").overall
    }
}
