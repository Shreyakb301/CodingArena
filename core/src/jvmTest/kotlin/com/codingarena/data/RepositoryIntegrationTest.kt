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
import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.ExperienceLevel
import com.codingarena.domain.model.OnboardingAnswers
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.ReviewStage
import com.codingarena.domain.model.StreakState
import com.codingarena.domain.model.UserProfile
import com.codingarena.domain.model.UserTopicRating
import com.codingarena.domain.usecase.AnswerSubmission
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Repository and use-case tests against a real in-memory SQLite database.
 *
 * These live in jvmTest rather than commonTest because constructing a driver
 * needs a platform: the code under test is all shared, only the driver is not.
 */
class RepositoryIntegrationTest {

    private val io = Dispatchers.Unconfined
    private val time = FixedTimeProvider(millis = 1_700_000_000_000)
    private val ids = SequentialIdGenerator()

    private fun db(): ArenaDatabase {
        val driver = DatabaseDriverFactory(path = null).create()
        return ArenaDatabase(driver)
    }

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

    private fun env() = Env(db(), io)

    private fun submitUseCase(env: Env) = SubmitAnswerUseCase(
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

    @Test
    fun `problems round trip through the database intact`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)

        val loaded = env.problems.all()
        assertEquals(StarterContent.problems.size, loaded.size)

        val original = StarterContent.problems.first { it.codeSnippet != null }
        val fetched = env.problems.byId(original.id)
        assertEquals(original, fetched)
    }

    @Test
    fun `seeding is skipped when problems already exist`() = runTest {
        val env = env()
        env.problems.upsertAll(listOf(StarterContent.problems.first()))
        env.problems.seedIfEmpty(StarterContent.problems)

        assertEquals(1, env.problems.all().size)
    }

    @Test
    fun `topic lookup includes secondary topics`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)

        val debugging = env.problems.byTopic(CodingTopic.DEBUGGING)

        assertTrue(debugging.any { it.primaryTopic != CodingTopic.DEBUGGING })
    }

    @Test
    fun `a profile round trips with its onboarding answers`() = runTest {
        val env = env()
        val profile = UserProfile(
            id = "u1",
            displayName = "Sam",
            email = "sam@example.com",
            isGuest = false,
            onboarding = OnboardingAnswers(
                experienceLevel = ExperienceLevel.COMFORTABLE,
                knownTopics = setOf(CodingTopic.ARRAYS, CodingTopic.TREES),
                targetCompanies = listOf("Acme"),
            ),
            createdAt = 42L,
            startingRating = 1150,
        )

        env.profiles.save(profile)

        assertEquals(profile, env.profiles.current())
    }

    @Test
    fun `ratings round trip with their counters`() = runTest {
        val env = env()
        val ratings = PlayerRatings(
            overall = 1234,
            topics = mapOf(
                CodingTopic.ARRAYS to UserTopicRating(CodingTopic.ARRAYS, 1300, 12, 9, 99L),
            ),
            updatedAt = 555L,
        )

        env.ratings.save("u1", ratings)
        val loaded = env.ratings.load("u1")

        assertEquals(1234, loaded.overall)
        assertEquals(1300, loaded.topicRating(CodingTopic.ARRAYS))
        assertEquals(12, loaded.topics.getValue(CodingTopic.ARRAYS).attempts)
        assertEquals(99L, loaded.topics.getValue(CodingTopic.ARRAYS).lastPracticedAt)
    }

    @Test
    fun `an unknown user reads as default ratings rather than failing`() = runTest {
        assertEquals(PlayerRatings.DEFAULT_RATING, env().ratings.load("nobody").overall)
    }

    @Test
    fun `attempts are written unsynced and can be marked synced`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val problem = StarterContent.problems.first()

        val result = submitUseCase(env)(
            AnswerSubmission(
                userId = "u1",
                problem = problem,
                selectedAnswerIds = listOf(problem.correctAnswerId),
                startedAt = time.nowMillis() - 20_000,
            )
        )

        assertEquals(1, env.attempts.unsynced().size)
        env.attempts.markSynced(listOf(result.attempt.id))
        assertTrue(env.attempts.unsynced().isEmpty())
    }

    @Test
    fun `submitting a correct answer moves ratings, schedules a review and logs history`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val problem = StarterContent.problems.first { it.primaryTopic == CodingTopic.ARRAYS }

        val result = submitUseCase(env)(
            AnswerSubmission(
                userId = "u1",
                problem = problem,
                selectedAnswerIds = listOf(problem.correctAnswerId),
                startedAt = time.nowMillis() - 20_000,
            )
        )

        assertTrue(result.attempt.wasCorrect)
        assertTrue(result.ratingUpdate.overallChange > 0)

        val stored = env.ratings.load("u1")
        assertEquals(result.ratingUpdate.overallAfter, stored.overall)

        val review = env.reviews.forProblem("u1", problem.id)
        assertNotNull(review)
        assertEquals(ReviewStage.SHAKY, review.stage)

        assertEquals(1, env.ratings.overallHistory("u1").size)
        assertTrue(env.ratings.topicHistory("u1", problem.primaryTopic).isNotEmpty())
    }

    @Test
    fun `a wrong answer schedules the problem for tomorrow and produces a review`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val problem = StarterContent.problems.first { !it.challengeType.isOrdering }
        val wrongId = problem.choices.first { it.id !in problem.correctAnswerIds }.id

        val result = submitUseCase(env)(
            AnswerSubmission(
                userId = "u1",
                problem = problem,
                selectedAnswerIds = listOf(wrongId),
                startedAt = time.nowMillis() - 20_000,
            )
        )

        assertEquals(AnswerOutcome.INCORRECT, result.attempt.outcome)
        assertTrue(result.ratingUpdate.overallChange <= 0)
        assertEquals(ReviewStage.RELEARN, env.reviews.forProblem("u1", problem.id)!!.stage)
        assertTrue(result.review.whyItMatters.isNotBlank())
        assertTrue(result.review.recommendedPractice.isNotEmpty())
    }

    @Test
    fun `the daily puzzle credits the streak and records a result`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val problem = StarterContent.problems.first()
        val today = time.epochDay()

        val result = submitUseCase(env)(
            AnswerSubmission(
                userId = "u1",
                problem = problem,
                selectedAnswerIds = listOf(problem.correctAnswerId),
                startedAt = time.nowMillis() - 15_000,
                source = AttemptSource.DAILY_PUZZLE,
            )
        )

        assertTrue(result.streakExtended)
        assertEquals(1, env.streaks.load("u1").currentStreak)
        assertNotNull(env.dailyPuzzles.result("u1", today))
    }

    @Test
    fun `code rush answers do not extend the streak on their own`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val problem = StarterContent.problems.first()

        val result = submitUseCase(env)(
            AnswerSubmission(
                userId = "u1",
                problem = problem,
                selectedAnswerIds = listOf(problem.correctAnswerId),
                startedAt = time.nowMillis() - 5_000,
                source = AttemptSource.CODE_RUSH,
            )
        )

        assertTrue(!result.streakExtended)
        assertEquals(0, env.streaks.load("u1").currentStreak)
    }

    @Test
    fun `the first challenge unlocks its achievement exactly once`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val useCase = submitUseCase(env)
        val first = StarterContent.problems[0]
        val second = StarterContent.problems[1]

        val one = useCase(
            AnswerSubmission("u1", first, listOf(first.correctAnswerId), time.nowMillis() - 10_000)
        )
        assertTrue(one.newAchievements.any { it.id == "first-challenge" })

        val two = useCase(
            AnswerSubmission("u1", second, listOf(second.correctAnswerId), time.nowMillis() - 10_000)
        )
        assertTrue(two.newAchievements.none { it.id == "first-challenge" })
        assertEquals(1, env.achievements.unlocked("u1").count { it.key == "first-challenge" })
    }

    @Test
    fun `repeated practice climbs the review ladder`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val useCase = submitUseCase(env)
        val problem = StarterContent.problems.first { it.estimatedSeconds >= 40 }

        repeat(4) {
            useCase(
                AnswerSubmission(
                    userId = "u1",
                    problem = problem,
                    selectedAnswerIds = listOf(problem.correctAnswerId),
                    startedAt = time.nowMillis() - 5_000,
                )
            )
        }

        assertEquals(ReviewStage.MASTERED, env.reviews.forProblem("u1", problem.id)!!.stage)
    }

    @Test
    fun `streak state round trips including the current week`() = runTest {
        val env = env()
        val state = StreakState(
            currentStreak = 4,
            longestStreak = 9,
            lastActiveDay = 20_000,
            weeklyGoalDays = 3,
            activeDaysThisWeek = setOf(19_998L, 19_999L, 20_000L),
            totalActiveDays = 30,
        )

        env.streaks.save("u1", state)

        assertEquals(state, env.streaks.load("u1"))
    }

    @Test
    fun `code rush best score reads back from stored sessions`() = runTest {
        val env = env()
        val engine = com.codingarena.domain.engine.CodeRushEngine()
        var session = engine.start("s1", "u1", com.codingarena.domain.model.CodeRushMode.ThreeMinute, 0L)
        val problem = StarterContent.problems.first()
        repeat(3) { i ->
            session = engine.submit(session, problem.copy(id = "p$i"), true, 1_000, 1_000L * i)
        }
        session = engine.finish(session, com.codingarena.domain.model.RushEndReason.TIME_UP, 180_000)

        env.codeRush.save(session)

        assertEquals(3, env.codeRush.bestScore("u1"))
        assertEquals(1, env.codeRush.sessions("u1").size)
        assertEquals(session, env.codeRush.sessions("u1").first())
    }

    @Test
    fun `an unfinished session does not count toward the best score`() = runTest {
        val env = env()
        val engine = com.codingarena.domain.engine.CodeRushEngine()
        val open = engine.start("s1", "u1", com.codingarena.domain.model.CodeRushMode.Survival, 0L)

        env.codeRush.save(open)

        assertEquals(0, env.codeRush.bestScore("u1"))
    }

    @Test
    fun `the daily puzzle is stable across calls within a day`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        val useCase = EnsureDailyPuzzleUseCase(env.dailyPuzzles, env.problems, time)

        val first = useCase(20_000)
        val again = useCase(20_000)
        val tomorrow = useCase(20_001)

        assertEquals(first?.problemId, again?.problemId)
        assertTrue(first?.problemId != tomorrow?.problemId)
    }

    @Test
    fun `the daily puzzle is null when no content has been seeded`() = runTest {
        val env = env()
        assertNull(EnsureDailyPuzzleUseCase(env.dailyPuzzles, env.problems, time)(20_000))
    }

    @Test
    fun `a first run lands on onboarding and seeds content`() = runTest {
        val env = env()
        val useCase = StartAppUseCase(env.problems, env.profiles, env.settings)

        val destination = useCase()

        assertTrue(destination is StartDestination.Onboarding)
        assertEquals(StarterContent.problems.size, env.problems.all().size)
        assertEquals("1", env.settings.get(StartAppUseCase.KEY_CONTENT_VERSION))
    }

    @Test
    fun `a returning user lands on home`() = runTest {
        val env = env()
        env.profiles.save(UserProfile(id = "u1", displayName = "Sam"))

        val destination = StartAppUseCase(env.problems, env.profiles, env.settings)()

        assertTrue(destination is StartDestination.Home)
    }

    @Test
    fun `onboarding seeds ratings, a streak goal and a first learning path`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)

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
            answers = OnboardingAnswers(
                experienceLevel = ExperienceLevel.COMFORTABLE,
                knownTopics = setOf(CodingTopic.ARRAYS),
                weeklyGoalDays = 4,
            ),
        )

        val ratings = env.ratings.load(profile.id)
        assertEquals(ExperienceLevel.COMFORTABLE.startingRating, ratings.overall)
        assertTrue(ratings.topicRating(CodingTopic.ARRAYS) > ratings.topicRating(CodingTopic.GRAPHS))
        assertEquals(4, env.streaks.load(profile.id).weeklyGoalDays)

        val path = env.learningPaths.active(profile.id)
        assertNotNull(path)
        assertTrue(path.steps.isNotEmpty())
        assertTrue(path.rationale.isNotBlank())
    }

    @Test
    fun `a placement rating overrides the experience-level estimate`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)

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
            answers = OnboardingAnswers(
                experienceLevel = ExperienceLevel.NEW_TO_CODING,
                placementRating = 1420,
            ),
        )

        assertEquals(1420, env.ratings.load(profile.id).overall)
        assertEquals(1420, profile.startingRating)
    }

    @Test
    fun `the home snapshot assembles everything the screen needs`() = runTest {
        val env = env()
        env.problems.upsertAll(StarterContent.problems)
        env.profiles.save(UserProfile(id = "u1", displayName = "Sam", startingRating = 1000))
        env.ratings.save("u1", PlayerRatings(overall = 1100))

        val snapshot = GetHomeSnapshotUseCase(
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
        )("u1")

        assertNotNull(snapshot.dailyPuzzle)
        assertNull(snapshot.dailyPuzzleResult)
        assertEquals(1100, snapshot.ratings.overall)
        assertTrue(snapshot.recommendations.isNotEmpty())
        assertTrue(snapshot.recommendations.none { it.problemId == snapshot.dailyPuzzle!!.id })
        assertNotNull(snapshot.readiness)
    }

    @Test
    fun `settings round trip`() = runTest {
        val env = env()
        assertNull(env.settings.get("theme"))
        env.settings.put("theme", "dark")
        assertEquals("dark", env.settings.get("theme"))
        env.settings.put("theme", "light")
        assertEquals("light", env.settings.get("theme"))
    }
}
