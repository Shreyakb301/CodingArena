package com.codingarena.domain.sync

import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.model.DailyPuzzle
import com.codingarena.domain.repository.AchievementRepository
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.repository.DailyPuzzleRepository
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException

/**
 * One offline-first sync pass, in the order spec 7 lays out: upload local work
 * first, then pull down content, ratings and the Daily Puzzle.
 *
 * Upload comes first on purpose. If the pass dies halfway, the user's own work
 * is already safe on the server; the worst case is stale content, which the
 * next pass fixes.
 *
 * Every step is individually guarded, so one failing endpoint does not abandon
 * the rest of the pass, and nothing local is ever deleted on failure.
 */
class SyncUseCase(
    private val remote: ArenaRemoteDataSource,
    private val attempts: AttemptRepository,
    private val achievements: AchievementRepository,
    private val ratings: RatingRepository,
    private val problems: ProblemRepository,
    private val dailyPuzzles: DailyPuzzleRepository,
    private val settings: SettingsRepository,
    private val time: TimeProvider,
) {

    suspend operator fun invoke(userId: String): SyncReport {
        var report = SyncReport()

        // 1. Upload unsynchronised attempts.
        report = runStep(report) {
            val pending = attempts.unsynced().filter { it.userId == userId }
            if (pending.isEmpty()) {
                it
            } else {
                remote.pushAttempts(userId, pending)
                // Only flagged as synced once the push returns, so a failure
                // means they are retried rather than silently dropped.
                attempts.markSynced(pending.map { attempt -> attempt.id })
                it.copy(attemptsUploaded = pending.size)
            }
        }

        // 2. Upload achievements.
        report = runStep(report) {
            val unlocked = achievements.unlocked(userId)
            if (unlocked.isEmpty()) {
                it
            } else {
                remote.pushAchievements(userId, unlocked)
                it.copy(achievementsUploaded = unlocked.size)
            }
        }

        // 3. Reconcile ratings.
        report = runStep(report) { it.copy(ratingsResolution = reconcileRatings(userId)) }

        // 4. Download new or changed problems.
        report = runStep(report) {
            val since = settings.get(KEY_PROBLEMS_SYNCED_AT)?.toLongOrNull() ?: 0L
            val downloaded = remote.fetchProblems(since)
            if (downloaded.isEmpty()) {
                it
            } else {
                problems.upsertAll(downloaded)
                settings.put(KEY_PROBLEMS_SYNCED_AT, time.nowMillis().toString())
                it.copy(problemsDownloaded = downloaded.size)
            }
        }

        // 5. Refresh today's Daily Puzzle.
        report = runStep(report) {
            val today = time.epochDay()
            val problemId = remote.fetchDailyPuzzle(today)
            if (problemId == null || dailyPuzzles.puzzleFor(today)?.problemId == problemId) {
                it
            } else {
                dailyPuzzles.save(DailyPuzzle(today, problemId, time.nowMillis()))
                it.copy(dailyPuzzleRefreshed = true)
            }
        }

        if (report.succeeded) {
            settings.put(KEY_LAST_SYNC_AT, time.nowMillis().toString())
        }
        return report
    }

    /**
     * Resolves the local and server rating snapshots.
     *
     * Timestamps decide, per spec 7. The server record wins ties, which is
     * what makes it "authoritative" in the only sense that is safe: it settles
     * disagreements it can already see, without discarding newer local work.
     */
    private suspend fun reconcileRatings(userId: String): RatingsResolution {
        val local = ratings.load(userId)
        val server = remote.fetchRatings(userId)

        return when {
            server == null -> {
                remote.pushRatings(userId, local)
                RatingsResolution.SEEDED_FROM_LOCAL
            }

            server.updatedAt == local.updatedAt -> RatingsResolution.ALREADY_IN_SYNC

            server.updatedAt > local.updatedAt -> {
                ratings.save(userId, server)
                RatingsResolution.SERVER_WON
            }

            else -> {
                remote.pushRatings(userId, local)
                RatingsResolution.LOCAL_WON
            }
        }
    }

    /** Runs one step, converting a thrown failure into a report rather than a crash. */
    private suspend fun runStep(
        report: SyncReport,
        block: suspend (SyncReport) -> SyncReport,
    ): SyncReport {
        if (!report.succeeded) return report
        return try {
            block(report)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            report.copy(failure = error.toSyncFailure())
        }
    }

    /** Time of the last fully successful pass, for the settings screen. */
    suspend fun lastSyncedAt(): Long? = settings.get(KEY_LAST_SYNC_AT)?.toLongOrNull()

    companion object {
        const val KEY_LAST_SYNC_AT = "last_sync_at"
        const val KEY_PROBLEMS_SYNCED_AT = "problems_synced_at"
    }
}

/** Marker an implementation can throw so sync reports offline rather than an error. */
class OfflineException(message: String = "No connection") : Exception(message)

/** Marker for an expired or rejected session. */
class UnauthorisedException(message: String = "Not authorised") : Exception(message)

private fun Throwable.toSyncFailure(): SyncFailure = when (this) {
    is OfflineException -> SyncFailure.Offline(message ?: "No connection")
    is UnauthorisedException -> SyncFailure.Unauthorised(message ?: "Not authorised")
    else -> SyncFailure.Unexpected(message ?: this::class.simpleName ?: "Sync failed")
}
