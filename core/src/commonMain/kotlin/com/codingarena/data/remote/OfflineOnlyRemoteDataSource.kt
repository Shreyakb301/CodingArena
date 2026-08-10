package com.codingarena.data.remote

import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.sync.ArenaRemoteDataSource
import com.codingarena.domain.sync.OfflineException

/**
 * Stand-in backend for phase 1.
 *
 * There is no server yet, and the app is offline-first by design, so the
 * honest behaviour is to report "no connection" rather than to silently
 * succeed. Every sync pass therefore leaves local work marked unsynced and
 * ready for the real client, which drops in by replacing this binding.
 */
class OfflineOnlyRemoteDataSource : ArenaRemoteDataSource {

    override suspend fun pushAttempts(userId: String, attempts: List<PracticeAttempt>): Unit =
        throw OfflineException(MESSAGE)

    override suspend fun pushAchievements(userId: String, unlocked: Map<String, Long>): Unit =
        throw OfflineException(MESSAGE)

    override suspend fun pushRatings(userId: String, ratings: PlayerRatings): Unit =
        throw OfflineException(MESSAGE)

    override suspend fun fetchProblems(since: Long): List<CodingProblem> =
        throw OfflineException(MESSAGE)

    override suspend fun fetchRatings(userId: String): PlayerRatings? =
        throw OfflineException(MESSAGE)

    override suspend fun fetchDailyPuzzle(epochDay: Long): String? =
        throw OfflineException(MESSAGE)

    private companion object {
        const val MESSAGE = "Cloud sync is not enabled yet - your progress is saved on this device"
    }
}
