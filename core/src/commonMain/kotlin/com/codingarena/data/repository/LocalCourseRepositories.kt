package com.codingarena.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.codingarena.data.local.arenaJson
import com.codingarena.db.ArenaDatabase
import com.codingarena.domain.model.ChapterProgress
import com.codingarena.domain.model.CodeDraft
import com.codingarena.domain.model.ProgrammingLanguage
import com.codingarena.domain.repository.CodeDraftRepository
import com.codingarena.domain.repository.CourseProgressRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalCourseProgressRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : CourseProgressRepository {
    override fun observe(userId: String): Flow<Map<String, ChapterProgress>> =
        db.arenaQueries.selectChapterProgress(userId).asFlow().mapToList(io).map { rows ->
            rows.mapNotNull { decode(it.payloadJson) }.associateBy { it.chapterId }
        }

    override suspend fun all(userId: String): Map<String, ChapterProgress> = withContext(io) {
        db.arenaQueries.selectChapterProgress(userId).awaitAsList()
            .mapNotNull { decode(it.payloadJson) }.associateBy { it.chapterId }
    }

    override suspend fun chapter(userId: String, chapterId: String): ChapterProgress? = withContext(io) {
        db.arenaQueries.selectChapterProgressById(userId, chapterId).awaitAsOneOrNull()
            ?.payloadJson?.let(::decode)
    }

    override suspend fun save(progress: ChapterProgress, synced: Boolean) = withContext(io) {
        db.arenaQueries.upsertChapterProgress(
            userId = progress.userId,
            chapterId = progress.chapterId,
            payloadJson = arenaJson.encodeToString(progress),
            updatedAt = progress.lastPractisedAt ?: 0L,
            synced = if (synced) 1L else 0L,
        )
    }

    override suspend fun unsynced(): List<ChapterProgress> = withContext(io) {
        db.arenaQueries.selectUnsyncedChapterProgress().awaitAsList()
            .mapNotNull { decode(it.payloadJson) }
    }

    override suspend fun markSynced(userId: String, chapterId: String) = withContext(io) {
        db.arenaQueries.markChapterProgressSynced(userId, chapterId)
    }

    private fun decode(payload: String): ChapterProgress? = runCatching {
        arenaJson.decodeFromString<ChapterProgress>(payload)
    }.getOrNull()
}

class LocalCodeDraftRepository(
    private val db: ArenaDatabase,
    private val io: CoroutineDispatcher,
) : CodeDraftRepository {
    override suspend fun draft(
        userId: String,
        problemId: String,
        language: ProgrammingLanguage,
    ): CodeDraft? = withContext(io) {
        db.arenaQueries.selectCodeDraft(userId, problemId, language.name)
            .awaitAsOneOrNull()?.toDomain()
    }

    override suspend fun drafts(userId: String): List<CodeDraft> = withContext(io) {
        db.arenaQueries.selectCodeDrafts(userId).awaitAsList().mapNotNull { it.toDomain() }
    }

    override suspend fun save(draft: CodeDraft) = withContext(io) {
        db.arenaQueries.upsertCodeDraft(
            draft.userId,
            draft.problemId,
            draft.language.name,
            draft.sourceCode,
            draft.updatedAt,
            if (draft.synced) 1L else 0L,
        )
    }

    override suspend fun delete(userId: String, problemId: String, language: ProgrammingLanguage) =
        withContext(io) { db.arenaQueries.deleteCodeDraft(userId, problemId, language.name) }

    private fun com.codingarena.db.CodeDraft.toDomain(): CodeDraft? =
        ProgrammingLanguage.entries.firstOrNull { it.name == language }?.let { parsed ->
            CodeDraft(userId, problemId, parsed, sourceCode, updatedAt, synced == 1L)
        }
}
