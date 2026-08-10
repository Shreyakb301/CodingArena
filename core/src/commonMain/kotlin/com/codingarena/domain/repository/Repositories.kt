package com.codingarena.domain.repository

import com.codingarena.data.repository.BlitzSessionSummary
import com.codingarena.domain.engine.BlitzSession
import com.codingarena.domain.model.Achievement
import com.codingarena.domain.model.CodeRushSession
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.DailyPuzzle
import com.codingarena.domain.model.DailyPuzzleResult
import com.codingarena.domain.model.LearningPath
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.model.RatingHistoryEntry
import com.codingarena.domain.model.RecallRecord
import com.codingarena.domain.model.ScheduledReview
import com.codingarena.domain.model.StreakState
import com.codingarena.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing storage contracts.
 *
 * Kept in the domain layer so use cases depend on these rather than on
 * SQLDelight, which is what allows the engines above them to stay pure.
 */

interface ProblemRepository {
    suspend fun all(): List<CodingProblem>
    suspend fun byId(id: String): CodingProblem?
    suspend fun byTopic(topic: CodingTopic): List<CodingProblem>
    fun observeAll(): Flow<List<CodingProblem>>

    /** Writes the bundled starter set if the local table is empty. */
    suspend fun seedIfEmpty(problems: List<CodingProblem>)
    suspend fun upsertAll(problems: List<CodingProblem>)
}

interface ProfileRepository {
    fun observe(): Flow<UserProfile?>
    suspend fun current(): UserProfile?
    suspend fun save(profile: UserProfile)
    suspend fun clear(userId: String)
}

interface RatingRepository {
    fun observe(userId: String): Flow<PlayerRatings>
    suspend fun load(userId: String): PlayerRatings
    suspend fun save(userId: String, ratings: PlayerRatings)
    suspend fun appendHistory(entry: RatingHistoryEntry)
    suspend fun overallHistory(userId: String): List<RatingHistoryEntry>
    suspend fun topicHistory(userId: String, topic: CodingTopic): List<RatingHistoryEntry>
}

interface AttemptRepository {
    suspend fun record(attempt: PracticeAttempt)
    suspend fun recent(userId: String, limit: Int = 50): List<PracticeAttempt>
    suspend fun forProblem(userId: String, problemId: String): List<PracticeAttempt>
    fun observeRecent(userId: String, limit: Int = 50): Flow<List<PracticeAttempt>>
    suspend fun unsynced(): List<PracticeAttempt>
    suspend fun markSynced(ids: List<String>)
}

interface ReviewRepository {
    fun observe(userId: String): Flow<List<ScheduledReview>>
    suspend fun all(userId: String): List<ScheduledReview>
    suspend fun due(userId: String, now: Long): List<ScheduledReview>
    suspend fun forProblem(userId: String, problemId: String): ScheduledReview?
    suspend fun save(review: ScheduledReview)
}

interface StreakRepository {
    fun observe(userId: String): Flow<StreakState>
    suspend fun load(userId: String): StreakState
    suspend fun save(userId: String, state: StreakState)
}

interface AchievementRepository {
    fun observeUnlocked(userId: String): Flow<Map<String, Long>>
    suspend fun unlocked(userId: String): Map<String, Long>
    suspend fun unlock(userId: String, achievements: List<Achievement>, now: Long)
}

interface DailyPuzzleRepository {
    suspend fun puzzleFor(epochDay: Long): DailyPuzzle?
    suspend fun save(puzzle: DailyPuzzle)
    suspend fun result(userId: String, epochDay: Long): DailyPuzzleResult?
    suspend fun saveResult(userId: String, result: DailyPuzzleResult)
    fun observeResult(userId: String, epochDay: Long): Flow<DailyPuzzleResult?>
}

interface LearningPathRepository {
    fun observeActive(userId: String): Flow<LearningPath?>
    suspend fun active(userId: String): LearningPath?
    suspend fun save(path: LearningPath)
}

interface CodeRushRepository {
    suspend fun save(session: CodeRushSession)
    suspend fun sessions(userId: String): List<CodeRushSession>
    fun observeSessions(userId: String): Flow<List<CodeRushSession>>
    suspend fun bestScore(userId: String): Int
}

/**
 * Blitz recall state over a curated roadmap.
 *
 * Keyed by curriculum slug rather than local problem id - the roadmap points
 * at problems that have no bundled counterpart.
 */
interface CurriculumRepository {
    suspend fun records(userId: String): Map<String, RecallRecord>
    fun observeRecords(userId: String): Flow<Map<String, RecallRecord>>
    suspend fun record(userId: String, slug: String): RecallRecord?
    suspend fun save(userId: String, record: RecallRecord)
    suspend fun saveAll(userId: String, records: List<RecallRecord>)
    suspend fun saveSession(userId: String, id: String, session: BlitzSession)
    suspend fun recentSessions(userId: String, limit: Int = 20): List<BlitzSessionSummary>
    suspend fun bestScore(userId: String): Int
}

interface SettingsRepository {
    fun observe(key: String): Flow<String?>
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
}
