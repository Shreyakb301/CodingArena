package com.codingarena.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.codingarena.data.local.arenaJson
import com.codingarena.db.ArenaDatabase
import com.codingarena.domain.engine.BlitzSession
import com.codingarena.domain.engine.toSnapshot
import com.codingarena.domain.model.RecallRecord
import com.codingarena.domain.repository.CurriculumRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Blitz recall state, keyed by curriculum slug.
 *
 * Deliberately not keyed by local problem id: the roadmap points at LeetCode
 * problems that have no bundled counterpart, and a user's progress through the
 * 150 has to survive any change to the bundled content set.
 */
class LocalCurriculumRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : CurriculumRepository {

    override suspend fun records(userId: String): Map<String, RecallRecord> = withContext(io) {
        db.arenaQueries.selectRecallRecords(userId).executeAsList()
            .associate { it.slug to it.toDomain() }
    }

    override fun observeRecords(userId: String): Flow<Map<String, RecallRecord>> =
        db.arenaQueries.selectRecallRecords(userId).asFlow().mapToList(io)
            .map { rows -> rows.associate { it.slug to it.toDomain() } }

    override suspend fun record(userId: String, slug: String): RecallRecord? = withContext(io) {
        db.arenaQueries.selectRecallRecord(userId, slug).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(userId: String, record: RecallRecord) = withContext(io) {
        db.arenaQueries.upsertRecallRecord(
            userId = userId,
            slug = record.slug,
            correctStreak = record.correctStreak.toLong(),
            totalSeen = record.totalSeen.toLong(),
            totalCorrect = record.totalCorrect.toLong(),
            lastSeenAt = record.lastSeenAt,
            fastestMs = record.fastestMs,
            dueAt = record.dueAt,
            solved = if (record.solved) 1L else 0L,
            solvedAt = record.solvedAt,
        )
    }

    override suspend fun saveAll(userId: String, records: List<RecallRecord>) = withContext(io) {
        db.transaction {
            records.forEach { record ->
                db.arenaQueries.upsertRecallRecord(
                    userId = userId,
                    slug = record.slug,
                    correctStreak = record.correctStreak.toLong(),
                    totalSeen = record.totalSeen.toLong(),
                    totalCorrect = record.totalCorrect.toLong(),
                    lastSeenAt = record.lastSeenAt,
                    fastestMs = record.fastestMs,
                    dueAt = record.dueAt,
                    solved = if (record.solved) 1L else 0L,
                    solvedAt = record.solvedAt,
                )
            }
        }
    }

    override suspend fun saveSession(userId: String, id: String, session: BlitzSession) =
        withContext(io) {
            db.arenaQueries.upsertBlitzSession(
                id = id,
                userId = userId,
                curriculumId = session.curriculumId,
                payloadJson = arenaJson.encodeToString(session.toSnapshot()),
                score = session.score.toLong(),
                answered = session.answered.size.toLong(),
                startedAt = session.startedAt,
                endedAt = session.endedAt,
                synced = 0L,
            )
        }

    override suspend fun recentSessions(userId: String, limit: Int): List<BlitzSessionSummary> =
        withContext(io) {
            db.arenaQueries.selectBlitzSessions(userId, limit.toLong()).executeAsList().map {
                BlitzSessionSummary(
                    id = it.id,
                    curriculumId = it.curriculumId,
                    score = it.score.toInt(),
                    answered = it.answered.toInt(),
                    startedAt = it.startedAt,
                    endedAt = it.endedAt,
                )
            }
        }

    override suspend fun bestScore(userId: String): Int = withContext(io) {
        db.arenaQueries.selectBestBlitzStreak(userId).executeAsOneOrNull()?.max?.toInt() ?: 0
    }

    private fun com.codingarena.db.RecallRecord.toDomain() = RecallRecord(
        slug = slug,
        correctStreak = correctStreak.toInt(),
        totalSeen = totalSeen.toInt(),
        totalCorrect = totalCorrect.toInt(),
        lastSeenAt = lastSeenAt,
        fastestMs = fastestMs,
        dueAt = dueAt,
        solved = solved == 1L,
        solvedAt = solvedAt,
    )
}

/** Row-level view of a stored Blitz run, without rehydrating every answer. */
data class BlitzSessionSummary(
    val id: String,
    val curriculumId: String,
    val score: Int,
    val answered: Int,
    val startedAt: Long,
    val endedAt: Long?,
) {
    val accuracy: Double get() = if (answered == 0) 0.0 else score.toDouble() / answered
}
