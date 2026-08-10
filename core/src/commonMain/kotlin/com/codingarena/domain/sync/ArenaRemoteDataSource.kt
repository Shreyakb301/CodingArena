package com.codingarena.domain.sync

import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PracticeAttempt

/**
 * The backend seam (spec 7, phase 2).
 *
 * Defined in the domain layer with no Supabase or Ktor types in sight, so the
 * sync logic can be tested against a fake and the real client can be dropped in
 * later without touching anything above it.
 */
interface ArenaRemoteDataSource {

    /** Uploads locally recorded attempts. Implementations must be idempotent. */
    suspend fun pushAttempts(userId: String, attempts: List<PracticeAttempt>)

    /** Uploads unlocked achievement ids with their local unlock timestamps. */
    suspend fun pushAchievements(userId: String, unlocked: Map<String, Long>)

    /** Uploads the local rating snapshot. */
    suspend fun pushRatings(userId: String, ratings: PlayerRatings)

    /** Problems added or changed since [since] (epoch millis; 0 fetches all). */
    suspend fun fetchProblems(since: Long): List<CodingProblem>

    /** The server's rating record, or null if the server has never seen this user. */
    suspend fun fetchRatings(userId: String): PlayerRatings?

    /** The curated Daily Puzzle problem id for a day, or null if not published. */
    suspend fun fetchDailyPuzzle(epochDay: Long): String?
}

/** What one sync pass did. */
data class SyncReport(
    val attemptsUploaded: Int = 0,
    val achievementsUploaded: Int = 0,
    val problemsDownloaded: Int = 0,
    val ratingsResolution: RatingsResolution = RatingsResolution.NOT_ATTEMPTED,
    val dailyPuzzleRefreshed: Boolean = false,
    val failure: SyncFailure? = null,
) {
    val succeeded: Boolean get() = failure == null
}

/**
 * How the local and server rating records were reconciled.
 *
 * The spec calls ratings "server-authoritative", but that cannot mean
 * "server always wins": a user who practises on a plane would lose the session
 * on reconnect. The rule implemented is server-authoritative *for the record
 * the server has already seen*, with newer local work still winning on
 * timestamp.
 */
enum class RatingsResolution {
    NOT_ATTEMPTED,

    /** No server record existed; the local snapshot was uploaded as the seed. */
    SEEDED_FROM_LOCAL,

    /** The server record was newer and replaced the local one. */
    SERVER_WON,

    /** Local work was newer than the server record and was uploaded. */
    LOCAL_WON,

    /** Both sides already agreed. */
    ALREADY_IN_SYNC,
}

/** Why a sync pass stopped. Sync is best-effort: failures never lose local data. */
sealed interface SyncFailure {
    val message: String

    /** No usable connection. Normal, and not worth surfacing to the user. */
    data class Offline(override val message: String = "No connection") : SyncFailure

    /** The server rejected the credentials; the user needs to sign in again. */
    data class Unauthorised(override val message: String = "Sign in again to sync") : SyncFailure

    data class Unexpected(override val message: String) : SyncFailure
}
