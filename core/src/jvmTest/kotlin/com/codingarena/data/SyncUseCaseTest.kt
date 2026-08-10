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
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.model.UserTopicRating
import com.codingarena.domain.sync.ArenaRemoteDataSource
import com.codingarena.domain.sync.OfflineException
import com.codingarena.domain.sync.RatingsResolution
import com.codingarena.domain.sync.SyncFailure
import com.codingarena.domain.sync.SyncUseCase
import com.codingarena.domain.sync.UnauthorisedException
import com.codingarena.domain.usecase.AnswerSubmission
import com.codingarena.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Sync behaviour against a scriptable fake backend.
 *
 * The real Supabase client does not exist yet; these tests pin down the
 * contract it will have to satisfy, particularly around conflict resolution
 * and never losing local work on failure.
 */
class SyncUseCaseTest {

    private val io = Dispatchers.Unconfined
    private val time = FixedTimeProvider(millis = 1_700_000_000_000)

    /** Records what was pushed and serves whatever the test scripts. */
    private class FakeRemote : ArenaRemoteDataSource {
        val pushedAttempts = mutableListOf<PracticeAttempt>()
        var pushedAchievements: Map<String, Long> = emptyMap()
        var pushedRatings: PlayerRatings? = null

        var serverRatings: PlayerRatings? = null
        var serverProblems: List<CodingProblem> = emptyList()
        var serverDailyPuzzle: String? = null
        var lastProblemsSince: Long? = null

        var failOnAttempts: Throwable? = null
        var failOnProblems: Throwable? = null
        var failOnRatings: Throwable? = null

        override suspend fun pushAttempts(userId: String, attempts: List<PracticeAttempt>) {
            failOnAttempts?.let { throw it }
            pushedAttempts += attempts
        }

        override suspend fun pushAchievements(userId: String, unlocked: Map<String, Long>) {
            pushedAchievements = unlocked
        }

        override suspend fun pushRatings(userId: String, ratings: PlayerRatings) {
            failOnRatings?.let { throw it }
            pushedRatings = ratings
        }

        override suspend fun fetchProblems(since: Long): List<CodingProblem> {
            failOnProblems?.let { throw it }
            lastProblemsSince = since
            return serverProblems
        }

        override suspend fun fetchRatings(userId: String): PlayerRatings? {
            failOnRatings?.let { throw it }
            return serverRatings
        }

        override suspend fun fetchDailyPuzzle(epochDay: Long): String? = serverDailyPuzzle
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

    private fun env() = Env(ArenaDatabase(DatabaseDriverFactory(path = null).create()), io)

    private fun sync(env: Env, remote: FakeRemote) = SyncUseCase(
        remote = remote,
        attempts = env.attempts,
        achievements = env.achievements,
        ratings = env.ratings,
        problems = env.problems,
        dailyPuzzles = env.dailyPuzzles,
        settings = env.settings,
        time = time,
    )

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
        ids = SequentialIdGenerator(),
    )

    /** Answers [count] problems locally, as an offline session would. */
    private suspend fun practiseOffline(env: Env, count: Int) {
        env.problems.upsertAll(StarterContent.problems)
        val useCase = submitUseCase(env)
        StarterContent.problems.take(count).forEach { problem ->
            useCase(
                AnswerSubmission(
                    userId = "u1",
                    problem = problem,
                    selectedAnswerIds = listOf(problem.correctAnswerId),
                    startedAt = time.nowMillis() - 20_000,
                )
            )
        }
    }

    @Test
    fun `offline work is uploaded and flagged as synced`() = runTest {
        val env = env()
        val remote = FakeRemote()
        practiseOffline(env, 3)

        assertEquals(3, env.attempts.unsynced().size)

        val report = sync(env, remote)("u1")

        assertTrue(report.succeeded)
        assertEquals(3, report.attemptsUploaded)
        assertEquals(3, remote.pushedAttempts.size)
        assertTrue(env.attempts.unsynced().isEmpty())
    }

    @Test
    fun `a second pass uploads nothing new`() = runTest {
        val env = env()
        val remote = FakeRemote()
        practiseOffline(env, 2)
        val useCase = sync(env, remote)

        useCase("u1")
        val second = useCase("u1")

        assertEquals(0, second.attemptsUploaded)
        assertEquals(2, remote.pushedAttempts.size)
    }

    @Test
    fun `a failed upload leaves attempts pending for the next pass`() = runTest {
        val env = env()
        val remote = FakeRemote().apply { failOnAttempts = OfflineException() }
        practiseOffline(env, 2)

        val report = sync(env, remote)("u1")

        assertFalse(report.succeeded)
        assertTrue(report.failure is SyncFailure.Offline)
        // The critical property: nothing was marked synced, so no work is lost.
        assertEquals(2, env.attempts.unsynced().size)
    }

    @Test
    fun `a failure stops the pass without recording a successful sync time`() = runTest {
        val env = env()
        val remote = FakeRemote().apply { failOnAttempts = OfflineException() }
        practiseOffline(env, 1)
        val useCase = sync(env, remote)

        useCase("u1")

        assertNull(useCase.lastSyncedAt())
    }

    @Test
    fun `a successful pass records when it happened`() = runTest {
        val env = env()
        val useCase = sync(env, FakeRemote())

        useCase("u1")

        assertEquals(time.nowMillis(), useCase.lastSyncedAt())
    }

    @Test
    fun `an expired session is reported as unauthorised`() = runTest {
        val env = env()
        val remote = FakeRemote().apply { failOnAttempts = UnauthorisedException() }
        practiseOffline(env, 1)

        val report = sync(env, remote)("u1")

        assertTrue(report.failure is SyncFailure.Unauthorised)
    }

    @Test
    fun `an unknown error is reported without crashing the pass`() = runTest {
        val env = env()
        val remote = FakeRemote().apply { failOnAttempts = IllegalStateException("boom") }
        practiseOffline(env, 1)

        val report = sync(env, remote)("u1")

        val failure = report.failure
        assertTrue(failure is SyncFailure.Unexpected)
        assertEquals("boom", failure.message)
    }

    @Test
    fun `with no server record the local ratings seed the server`() = runTest {
        val env = env()
        val remote = FakeRemote()
        env.ratings.save("u1", PlayerRatings(overall = 1234, updatedAt = 500))

        val report = sync(env, remote)("u1")

        assertEquals(RatingsResolution.SEEDED_FROM_LOCAL, report.ratingsResolution)
        assertEquals(1234, remote.pushedRatings?.overall)
    }

    @Test
    fun `a newer server record replaces the local one`() = runTest {
        val env = env()
        val remote = FakeRemote().apply {
            serverRatings = PlayerRatings(
                overall = 1500,
                topics = mapOf(
                    CodingTopic.ARRAYS to UserTopicRating(CodingTopic.ARRAYS, 1600, 20, 18),
                ),
                updatedAt = 9_000,
            )
        }
        env.ratings.save("u1", PlayerRatings(overall = 1200, updatedAt = 1_000))

        val report = sync(env, remote)("u1")

        assertEquals(RatingsResolution.SERVER_WON, report.ratingsResolution)
        assertEquals(1500, env.ratings.load("u1").overall)
        assertEquals(1600, env.ratings.load("u1").topicRating(CodingTopic.ARRAYS))
    }

    @Test
    fun `newer local work is not overwritten by a stale server record`() = runTest {
        val env = env()
        val remote = FakeRemote().apply {
            serverRatings = PlayerRatings(overall = 1100, updatedAt = 1_000)
        }
        env.ratings.save("u1", PlayerRatings(overall = 1350, updatedAt = 9_000))

        val report = sync(env, remote)("u1")

        assertEquals(RatingsResolution.LOCAL_WON, report.ratingsResolution)
        assertEquals(1350, env.ratings.load("u1").overall)
        assertEquals(1350, remote.pushedRatings?.overall)
    }

    @Test
    fun `matching timestamps are left alone`() = runTest {
        val env = env()
        val remote = FakeRemote().apply {
            serverRatings = PlayerRatings(overall = 1100, updatedAt = 4_000)
        }
        env.ratings.save("u1", PlayerRatings(overall = 1100, updatedAt = 4_000))

        val report = sync(env, remote)("u1")

        assertEquals(RatingsResolution.ALREADY_IN_SYNC, report.ratingsResolution)
        assertNull(remote.pushedRatings)
    }

    @Test
    fun `downloaded problems are stored and readable`() = runTest {
        val env = env()
        val newProblem = StarterContent.problems.first().copy(
            id = "server-1",
            title = "Server problem",
        )
        val remote = FakeRemote().apply { serverProblems = listOf(newProblem) }

        val report = sync(env, remote)("u1")

        assertEquals(1, report.problemsDownloaded)
        assertEquals("Server problem", env.problems.byId("server-1")?.title)
    }

    @Test
    fun `problem downloads are incremental after the first pass`() = runTest {
        val env = env()
        val remote = FakeRemote().apply {
            serverProblems = listOf(StarterContent.problems.first().copy(id = "server-1"))
        }
        val useCase = sync(env, remote)

        useCase("u1")
        assertEquals(0L, remote.lastProblemsSince)

        time.advanceDays(1)
        useCase("u1")
        assertTrue(remote.lastProblemsSince!! > 0L, "second pass should send a watermark")
    }

    @Test
    fun `the daily puzzle is refreshed from the server`() = runTest {
        val env = env()
        val remote = FakeRemote().apply { serverDailyPuzzle = "curated-1" }

        val report = sync(env, remote)("u1")

        assertTrue(report.dailyPuzzleRefreshed)
        assertEquals("curated-1", env.dailyPuzzles.puzzleFor(time.epochDay())?.problemId)
    }

    @Test
    fun `an unchanged daily puzzle is not rewritten`() = runTest {
        val env = env()
        val remote = FakeRemote().apply { serverDailyPuzzle = "curated-1" }
        val useCase = sync(env, remote)

        useCase("u1")
        val second = useCase("u1")

        assertFalse(second.dailyPuzzleRefreshed)
    }

    @Test
    fun `achievements are uploaded with their unlock times`() = runTest {
        val env = env()
        val remote = FakeRemote()
        practiseOffline(env, 1)

        val report = sync(env, remote)("u1")

        assertTrue(report.achievementsUploaded > 0)
        assertTrue(remote.pushedAchievements.containsKey("first-challenge"))
        assertNotNull(remote.pushedAchievements["first-challenge"])
    }

    @Test
    fun `another user's attempts are not uploaded under this user`() = runTest {
        val env = env()
        val remote = FakeRemote()
        env.attempts.record(
            PracticeAttempt(
                id = "other",
                userId = "someone-else",
                problemId = "p1",
                startedAt = 0,
                completedAt = 1,
            )
        )

        val report = sync(env, remote)("u1")

        assertEquals(0, report.attemptsUploaded)
        assertTrue(remote.pushedAttempts.isEmpty())
    }

    @Test
    fun `a clean sync on a fresh install does nothing and succeeds`() = runTest {
        val report = sync(env(), FakeRemote())("u1")

        assertTrue(report.succeeded)
        assertEquals(0, report.attemptsUploaded)
        assertEquals(0, report.problemsDownloaded)
    }
}
