package com.codingarena.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.codingarena.data.local.arenaJson
import com.codingarena.data.local.joinAnswers
import com.codingarena.data.local.joinDays
import com.codingarena.data.local.toDomain
import com.codingarena.data.local.toOutcome
import com.codingarena.data.local.toTopicOrNull
import com.codingarena.db.ArenaDatabase
import com.codingarena.domain.model.Achievement
import com.codingarena.domain.model.AdaptivePracticeState
import com.codingarena.domain.model.CodeRushSession
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.DailyPuzzle
import com.codingarena.domain.model.DailyPuzzleResult
import com.codingarena.domain.model.InterviewProgress
import com.codingarena.domain.model.LearningPath
import com.codingarena.domain.model.OnboardingAnswers
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.model.RatingHistoryEntry
import com.codingarena.domain.model.ScheduledReview
import com.codingarena.domain.model.StreakState
import com.codingarena.domain.model.UserProfile
import com.codingarena.domain.repository.AchievementRepository
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.repository.CodeRushRepository
import com.codingarena.domain.repository.DailyPuzzleRepository
import com.codingarena.domain.repository.InterviewProgressRepository
import com.codingarena.domain.repository.LearningPathRepository
import com.codingarena.domain.repository.PracticeStateRepository
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.repository.ProfileRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.ReviewRepository
import com.codingarena.domain.repository.SettingsRepository
import com.codingarena.domain.repository.StreakRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * SQLDelight-backed repositories.
 *
 * Read queries that the UI observes are exposed as Flows via SQLDelight's
 * coroutine extensions, so a write anywhere in the app pushes a fresh value to
 * every screen watching it without any manual invalidation.
 */

class LocalProblemRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : ProblemRepository {

    override suspend fun all(): List<CodingProblem> = withContext(io) {
        db.arenaQueries.selectAllProblems().executeAsList().mapNotNull { it.payloadJson.decode() }
    }

    override suspend fun byId(id: String): CodingProblem? = withContext(io) {
        db.arenaQueries.selectProblemById(id).executeAsOneOrNull()?.payloadJson?.decode()
    }

    override suspend fun byTopic(topic: CodingTopic): List<CodingProblem> = withContext(io) {
        // The indexed column only holds the primary topic, so secondary matches
        // are filtered from the full payload after the query.
        db.arenaQueries.selectAllProblems().executeAsList()
            .mapNotNull { it.payloadJson.decode() }
            .filter { topic in it.allTopics }
    }

    override fun observeAll(): Flow<List<CodingProblem>> =
        db.arenaQueries.selectAllProblems().asFlow().mapToList(io)
            .map { rows -> rows.mapNotNull { it.payloadJson.decode() } }

    override suspend fun seedIfEmpty(problems: List<CodingProblem>) = withContext(io) {
        if (db.arenaQueries.countProblems().executeAsOne() == 0L) {
            upsertAll(problems)
        }
    }

    override suspend fun upsertAll(problems: List<CodingProblem>) = withContext(io) {
        db.transaction {
            problems.forEach { p ->
                db.arenaQueries.upsertProblem(
                    id = p.id,
                    payloadJson = arenaJson.encodeToString(p),
                    difficultyRating = p.difficultyRating.toLong(),
                    primaryTopic = p.primaryTopic.name,
                    challengeType = p.challengeType.name,
                    isPublished = if (p.isPublished) 1L else 0L,
                    updatedAt = 0L,
                )
            }
        }
    }

    private fun String.decode(): CodingProblem? =
        runCatching { arenaJson.decodeFromString<CodingProblem>(this) }.getOrNull()
}

class LocalProfileRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : ProfileRepository {

    override fun observe(): Flow<UserProfile?> =
        db.arenaQueries.selectAnyProfile().asFlow().mapToOneOrNull(io).map { it?.toDomain() }

    override suspend fun current(): UserProfile? = withContext(io) {
        db.arenaQueries.selectAnyProfile().executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(profile: UserProfile) = withContext(io) {
        db.arenaQueries.upsertProfile(
            id = profile.id,
            displayName = profile.displayName,
            email = profile.email,
            isGuest = if (profile.isGuest) 1L else 0L,
            onboardingJson = arenaJson.encodeToString(profile.onboarding),
            startingRating = profile.startingRating.toLong(),
            createdAt = profile.createdAt,
        )
    }

    override suspend fun clear(userId: String) = withContext(io) {
        db.arenaQueries.deleteProfile(userId)
    }

    private fun com.codingarena.db.Profile.toDomain() = UserProfile(
        id = id,
        displayName = displayName,
        email = email,
        isGuest = isGuest == 1L,
        onboarding = runCatching {
            arenaJson.decodeFromString<OnboardingAnswers>(onboardingJson)
        }.getOrElse { OnboardingAnswers() },
        createdAt = createdAt,
        startingRating = startingRating.toInt(),
    )
}

class LocalRatingRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : RatingRepository {

    override fun observe(userId: String): Flow<PlayerRatings> = combine(
        db.arenaQueries.selectOverallRating(userId).asFlow().mapToOneOrNull(io),
        db.arenaQueries.selectTopicRatings(userId).asFlow().mapToList(io),
        db.arenaQueries.selectModeRatings(userId).asFlow().mapToList(io),
    ) { overall, topics, modes ->
        PlayerRatings(
            overall = overall?.rating?.toInt() ?: PlayerRatings.DEFAULT_RATING,
            topics = topics.mapNotNull { it.toDomain() }.associateBy { it.topic },
            modes = modes.mapNotNull { it.toDomain() }.associateBy { it.mode },
            updatedAt = overall?.updatedAt ?: 0L,
        )
    }

    override suspend fun load(userId: String): PlayerRatings = withContext(io) {
        val overall = db.arenaQueries.selectOverallRating(userId).executeAsOneOrNull()
        PlayerRatings(
            overall = overall?.rating?.toInt() ?: PlayerRatings.DEFAULT_RATING,
            topics = db.arenaQueries.selectTopicRatings(userId).executeAsList()
                .mapNotNull { it.toDomain() }.associateBy { it.topic },
            modes = db.arenaQueries.selectModeRatings(userId).executeAsList()
                .mapNotNull { it.toDomain() }.associateBy { it.mode },
            updatedAt = overall?.updatedAt ?: 0L,
        )
    }

    override suspend fun save(userId: String, ratings: PlayerRatings) = withContext(io) {
        db.transaction {
            db.arenaQueries.upsertOverallRating(userId, ratings.overall.toLong(), ratings.updatedAt)
            ratings.topics.values.forEach {
                db.arenaQueries.upsertTopicRating(
                    userId = userId,
                    topic = it.topic.name,
                    rating = it.rating.toLong(),
                    attempts = it.attempts.toLong(),
                    correctAnswers = it.correctAnswers.toLong(),
                    lastPracticedAt = it.lastPracticedAt,
                )
            }
            ratings.modes.values.forEach {
                db.arenaQueries.upsertModeRating(
                    userId = userId,
                    mode = it.mode.name,
                    rating = it.rating.toLong(),
                    attempts = it.attempts.toLong(),
                    correctAnswers = it.correctAnswers.toLong(),
                )
            }
        }
    }

    override suspend fun appendHistory(entry: RatingHistoryEntry) = withContext(io) {
        db.arenaQueries.insertRatingHistory(
            id = entry.id,
            userId = entry.userId,
            topic = entry.topic?.name,
            rating = entry.rating.toLong(),
            change = entry.change.toLong(),
            recordedAt = entry.recordedAt,
            problemId = entry.problemId,
        )
    }

    override suspend fun overallHistory(userId: String): List<RatingHistoryEntry> = withContext(io) {
        db.arenaQueries.selectOverallHistory(userId).executeAsList().map { it.toDomain() }
    }

    override suspend fun topicHistory(userId: String, topic: CodingTopic): List<RatingHistoryEntry> =
        withContext(io) {
            db.arenaQueries.selectTopicHistory(userId, topic.name).executeAsList().map { it.toDomain() }
        }

    private fun com.codingarena.db.RatingHistory.toDomain() = RatingHistoryEntry(
        id = id,
        userId = userId,
        topic = topic?.toTopicOrNull(),
        rating = rating.toInt(),
        change = change.toInt(),
        recordedAt = recordedAt,
        problemId = problemId,
    )
}

class LocalAttemptRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : AttemptRepository {

    override suspend fun record(attempt: PracticeAttempt) = withContext(io) {
        db.arenaQueries.insertAttempt(
            id = attempt.id,
            userId = attempt.userId,
            problemId = attempt.problemId,
            startedAt = attempt.startedAt,
            completedAt = attempt.completedAt,
            selectedAnswers = attempt.selectedAnswerIds.joinAnswers(),
            outcome = attempt.outcome.name,
            attemptsCount = attempt.attemptsCount.toLong(),
            hintsUsed = attempt.hintsUsed.toLong(),
            ratingBefore = attempt.ratingBefore.toLong(),
            ratingAfter = attempt.ratingAfter.toLong(),
            source = attempt.source.name,
            synced = if (attempt.synced) 1L else 0L,
        )
    }

    override suspend fun recent(userId: String, limit: Int): List<PracticeAttempt> = withContext(io) {
        db.arenaQueries.selectRecentAttempts(userId, limit.toLong()).executeAsList().map { it.toDomain() }
    }

    override suspend fun forProblem(userId: String, problemId: String): List<PracticeAttempt> =
        withContext(io) {
            db.arenaQueries.selectAttemptsForProblem(userId, problemId).executeAsList().map { it.toDomain() }
        }

    override fun observeRecent(userId: String, limit: Int): Flow<List<PracticeAttempt>> =
        db.arenaQueries.selectRecentAttempts(userId, limit.toLong()).asFlow().mapToList(io)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun unsynced(): List<PracticeAttempt> = withContext(io) {
        db.arenaQueries.selectUnsyncedAttempts().executeAsList().map { it.toDomain() }
    }

    override suspend fun markSynced(ids: List<String>) = withContext(io) {
        if (ids.isNotEmpty()) db.arenaQueries.markAttemptsSynced(ids)
    }
}

class LocalReviewRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : ReviewRepository {

    override fun observe(userId: String): Flow<List<ScheduledReview>> =
        db.arenaQueries.selectReviews(userId).asFlow().mapToList(io)
            .map { rows -> rows.mapNotNull { it.toDomain() } }

    override suspend fun all(userId: String): List<ScheduledReview> = withContext(io) {
        db.arenaQueries.selectReviews(userId).executeAsList().mapNotNull { it.toDomain() }
    }

    override suspend fun due(userId: String, now: Long): List<ScheduledReview> = withContext(io) {
        db.arenaQueries.selectDueReviews(userId, now).executeAsList().mapNotNull { it.toDomain() }
    }

    override suspend fun forProblem(userId: String, problemId: String): ScheduledReview? =
        withContext(io) {
            db.arenaQueries.selectReviewForProblem(userId, problemId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun save(review: ScheduledReview) = withContext(io) {
        db.arenaQueries.upsertReview(
            userId = review.userId,
            problemId = review.problemId,
            stage = review.stage.name,
            dueAt = review.dueAt,
            lastReviewedAt = review.lastReviewedAt,
            repetitions = review.repetitions.toLong(),
            lapses = review.lapses.toLong(),
            topic = review.topic.name,
        )
    }
}

class LocalStreakRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : StreakRepository {

    override fun observe(userId: String): Flow<StreakState> =
        db.arenaQueries.selectStreak(userId).asFlow().mapToOneOrNull(io)
            .map { it?.toDomain() ?: StreakState() }

    override suspend fun load(userId: String): StreakState = withContext(io) {
        db.arenaQueries.selectStreak(userId).executeAsOneOrNull()?.toDomain() ?: StreakState()
    }

    override suspend fun save(userId: String, state: StreakState) = withContext(io) {
        db.arenaQueries.upsertStreak(
            userId = userId,
            currentStreak = state.currentStreak.toLong(),
            longestStreak = state.longestStreak.toLong(),
            lastActiveDay = state.lastActiveDay,
            weeklyGoalDays = state.weeklyGoalDays.toLong(),
            activeDaysThisWeek = state.activeDaysThisWeek.joinDays(),
            totalActiveDays = state.totalActiveDays.toLong(),
        )
    }
}

class LocalAchievementRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : AchievementRepository {

    override fun observeUnlocked(userId: String): Flow<Map<String, Long>> =
        db.arenaQueries.selectUserAchievements(userId).asFlow().mapToList(io)
            .map { rows -> rows.associate { it.achievementId to it.unlockedAt } }

    override suspend fun unlocked(userId: String): Map<String, Long> = withContext(io) {
        db.arenaQueries.selectUserAchievements(userId).executeAsList()
            .associate { it.achievementId to it.unlockedAt }
    }

    override suspend fun unlock(userId: String, achievements: List<Achievement>, now: Long) =
        withContext(io) {
            db.transaction {
                achievements.forEach {
                    db.arenaQueries.upsertUserAchievement(userId, it.id, now, 0L)
                }
            }
        }
}

class LocalDailyPuzzleRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : DailyPuzzleRepository {

    override suspend fun puzzleFor(epochDay: Long): DailyPuzzle? = withContext(io) {
        db.arenaQueries.selectDailyPuzzle(epochDay).executeAsOneOrNull()?.let {
            DailyPuzzle(it.epochDay, it.problemId, it.downloadedAt)
        }
    }

    override suspend fun save(puzzle: DailyPuzzle) = withContext(io) {
        db.arenaQueries.upsertDailyPuzzle(puzzle.epochDay, puzzle.problemId, puzzle.downloadedAt)
    }

    override suspend fun result(userId: String, epochDay: Long): DailyPuzzleResult? = withContext(io) {
        db.arenaQueries.selectDailyPuzzleResult(epochDay, userId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun saveResult(userId: String, result: DailyPuzzleResult) = withContext(io) {
        db.arenaQueries.upsertDailyPuzzleResult(
            epochDay = result.epochDay,
            userId = userId,
            problemId = result.problemId,
            attemptId = result.attemptId,
            outcome = result.outcome.name,
            solveDurationMs = result.solveDurationMs,
            hintsUsed = result.hintsUsed.toLong(),
            attemptsCount = result.attemptsCount.toLong(),
            ratingChange = result.ratingChange.toLong(),
            completedAt = result.completedAt,
        )
    }

    override fun observeResult(userId: String, epochDay: Long): Flow<DailyPuzzleResult?> =
        db.arenaQueries.selectDailyPuzzleResult(epochDay, userId).asFlow().mapToOneOrNull(io)
            .map { it?.toDomain() }

    private fun com.codingarena.db.DailyPuzzleResult.toDomain() = DailyPuzzleResult(
        epochDay = epochDay,
        problemId = problemId,
        attemptId = attemptId,
        outcome = outcome.toOutcome(),
        solveDurationMs = solveDurationMs,
        hintsUsed = hintsUsed.toInt(),
        attemptsCount = attemptsCount.toInt(),
        ratingChange = ratingChange.toInt(),
        completedAt = completedAt,
    )
}

class LocalLearningPathRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : LearningPathRepository {

    override fun observeActive(userId: String): Flow<LearningPath?> =
        db.arenaQueries.selectActiveLearningPath(userId).asFlow().mapToOneOrNull(io)
            .map { it?.payloadJson?.decode() }

    override suspend fun active(userId: String): LearningPath? = withContext(io) {
        db.arenaQueries.selectActiveLearningPath(userId).executeAsOneOrNull()?.payloadJson?.decode()
    }

    override suspend fun save(path: LearningPath) = withContext(io) {
        db.arenaQueries.upsertLearningPath(
            id = path.id,
            userId = path.userId,
            payloadJson = arenaJson.encodeToString(path),
            targetTopic = path.targetTopic.name,
            createdAt = path.createdAt,
            completedAt = path.completedAt,
        )
    }

    private fun String.decode(): LearningPath? =
        runCatching { arenaJson.decodeFromString<LearningPath>(this) }.getOrNull()
}

class LocalCodeRushRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : CodeRushRepository {

    override suspend fun save(session: CodeRushSession) = withContext(io) {
        db.arenaQueries.upsertCodeRushSession(
            id = session.id,
            userId = session.userId,
            payloadJson = arenaJson.encodeToString(session),
            score = session.score.toLong(),
            startedAt = session.startedAt,
            endedAt = session.endedAt,
            synced = 0L,
        )
    }

    override suspend fun sessions(userId: String): List<CodeRushSession> = withContext(io) {
        db.arenaQueries.selectCodeRushSessions(userId).executeAsList().mapNotNull { it.payloadJson.decode() }
    }

    override fun observeSessions(userId: String): Flow<List<CodeRushSession>> =
        db.arenaQueries.selectCodeRushSessions(userId).asFlow().mapToList(io)
            .map { rows -> rows.mapNotNull { it.payloadJson.decode() } }

    override suspend fun bestScore(userId: String): Int = withContext(io) {
        db.arenaQueries.selectBestCodeRushScore(userId).executeAsOneOrNull()?.max?.toInt() ?: 0
    }

    private fun String.decode(): CodeRushSession? =
        runCatching { arenaJson.decodeFromString<CodeRushSession>(this) }.getOrNull()
}

class LocalPracticeStateRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : PracticeStateRepository {

    override suspend fun load(userId: String): AdaptivePracticeState? = withContext(io) {
        db.arenaQueries.selectPracticeAdaptiveState(userId).executeAsOneOrNull()?.payloadJson?.decode()
    }

    override suspend fun save(state: AdaptivePracticeState, now: Long) = withContext(io) {
        db.arenaQueries.upsertPracticeAdaptiveState(
            userId = state.userId,
            payloadJson = arenaJson.encodeToString(state),
            updatedAt = now,
            synced = 0L,
        )
    }

    override suspend fun clear(userId: String) = withContext(io) {
        db.arenaQueries.clearPracticeAdaptiveState(userId)
    }

    private fun String.decode(): AdaptivePracticeState? =
        runCatching { arenaJson.decodeFromString<AdaptivePracticeState>(this) }.getOrNull()
}

class LocalInterviewProgressRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : InterviewProgressRepository {

    override suspend fun load(userId: String): InterviewProgress? = withContext(io) {
        db.arenaQueries.selectInterviewProgress(userId).executeAsOneOrNull()?.payloadJson?.decode()
    }

    override suspend fun save(progress: InterviewProgress, now: Long) = withContext(io) {
        db.arenaQueries.upsertInterviewProgress(
            userId = progress.userId,
            payloadJson = arenaJson.encodeToString(progress),
            updatedAt = now,
            synced = 0L,
        )
    }

    private fun String.decode(): InterviewProgress? =
        runCatching { arenaJson.decodeFromString<InterviewProgress>(this) }.getOrNull()
}

class LocalSettingsRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : SettingsRepository {

    override fun observe(key: String): Flow<String?> =
        db.arenaQueries.selectSetting(key).asFlow().mapToOneOrNull(io)

    override suspend fun get(key: String): String? = withContext(io) {
        db.arenaQueries.selectSetting(key).executeAsOneOrNull()
    }

    override suspend fun put(key: String, value: String) = withContext(io) {
        db.arenaQueries.upsertSetting(key, value)
    }
}
