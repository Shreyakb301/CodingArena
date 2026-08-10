package com.codingarena.domain.usecase

import com.codingarena.content.PatternLibrary
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.engine.LearningPathEngine
import com.codingarena.domain.engine.LearningSignals
import com.codingarena.domain.engine.PlacementResult
import com.codingarena.domain.engine.ProblemRecommender
import com.codingarena.domain.engine.RatingEngine
import com.codingarena.domain.engine.ReadinessEngine
import com.codingarena.domain.engine.SpacedRepetitionEngine
import com.codingarena.domain.engine.StreakEngine
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.DailyPuzzle
import com.codingarena.domain.model.DailyPuzzleResult
import com.codingarena.domain.model.InterviewReadiness
import com.codingarena.domain.model.LearningPath
import com.codingarena.domain.model.OnboardingAnswers
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PlayerStats
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.model.RecommendedProblem
import com.codingarena.domain.model.StreakState
import com.codingarena.domain.model.UserProfile
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.repository.DailyPuzzleRepository
import com.codingarena.domain.repository.LearningPathRepository
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.repository.ProfileRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.ReviewRepository
import com.codingarena.domain.repository.StreakRepository

/** Everything the home screen renders, assembled in one call. */
data class HomeSnapshot(
    val profile: UserProfile?,
    val ratings: PlayerRatings,
    val streak: StreakState,
    val streakAtRisk: Boolean,
    val dailyPuzzle: CodingProblem?,
    val dailyPuzzleResult: DailyPuzzleResult?,
    val dueReviewCount: Int,
    val upcomingReviewCount: Int,
    val learningPath: LearningPath?,
    val recommendations: List<RecommendedProblem>,
    val stats: PlayerStats,
    val readiness: InterviewReadiness?,
    val recentRatingChange: Int,
)

class GetHomeSnapshotUseCase(
    private val profiles: ProfileRepository,
    private val problems: ProblemRepository,
    private val ratings: RatingRepository,
    private val streaks: StreakRepository,
    private val reviews: ReviewRepository,
    private val attempts: AttemptRepository,
    private val learningPaths: LearningPathRepository,
    private val dailyPuzzles: DailyPuzzleRepository,
    private val ensureDailyPuzzle: EnsureDailyPuzzleUseCase,
    private val recommender: ProblemRecommender = ProblemRecommender(),
    private val srsEngine: SpacedRepetitionEngine = SpacedRepetitionEngine(),
    private val streakEngine: StreakEngine = StreakEngine(),
    private val readinessEngine: ReadinessEngine = ReadinessEngine(),
    private val time: TimeProvider,
) {

    suspend operator fun invoke(userId: String): HomeSnapshot {
        val now = time.nowMillis()
        val today = time.epochDay(now)

        val profile = profiles.current()
        val playerRatings = ratings.load(userId)
        val streak = streakEngine.refresh(streaks.load(userId), today)
        val allProblems = problems.all()
        val schedule = reviews.all(userId)
        val recent = attempts.recent(userId, limit = RECENT_WINDOW)

        val puzzle = ensureDailyPuzzle(today)
        val puzzleProblem = puzzle?.let { p -> allProblems.firstOrNull { it.id == p.problemId } }

        val stats = statsFrom(recent)
        val readiness = profile?.let {
            readinessEngine.estimate(playerRatings, stats, it.onboarding.targetJobLevel)
        }

        return HomeSnapshot(
            profile = profile,
            ratings = playerRatings,
            streak = streak,
            streakAtRisk = streakEngine.isStreakAtRisk(streak, today),
            dailyPuzzle = puzzleProblem,
            dailyPuzzleResult = dailyPuzzles.result(userId, today),
            dueReviewCount = srsEngine.dueReviews(schedule, now).size,
            upcomingReviewCount = srsEngine.upcomingCount(schedule, now),
            learningPath = learningPaths.active(userId),
            recommendations = recommender.recommendForUser(
                ratings = playerRatings,
                candidates = allProblems,
                excludeProblemIds = setOfNotNull(puzzle?.problemId),
            ),
            stats = stats,
            readiness = readiness,
            recentRatingChange = recent.firstOrNull()?.ratingChange ?: 0,
        )
    }

    private fun statsFrom(recent: List<PracticeAttempt>): PlayerStats {
        val completed = recent.filter { it.completedAt != null }
        val durations = completed.mapNotNull { it.solveDurationMs }
        return PlayerStats(
            totalCompleted = completed.size,
            totalCorrect = completed.count { it.wasCorrect },
            totalHintsUsed = completed.sumOf { it.hintsUsed },
            averageSolveMs = if (durations.isEmpty()) 0L else durations.sum() / durations.size,
            problemsSeen = completed.map { it.problemId }.distinct().size,
        )
    }

    private companion object {
        const val RECENT_WINDOW = 200
    }
}

/**
 * Resolves the Daily Puzzle for a day, choosing and storing one if none has
 * been recorded yet.
 *
 * The selection is deterministic in the epoch day, so a device that has never
 * been online still gets a stable puzzle rather than no puzzle at all.
 */
class EnsureDailyPuzzleUseCase(
    private val dailyPuzzles: DailyPuzzleRepository,
    private val problems: ProblemRepository,
    private val time: TimeProvider,
) {
    suspend operator fun invoke(epochDay: Long): DailyPuzzle? {
        dailyPuzzles.puzzleFor(epochDay)?.let { return it }

        val available = problems.all().filter { it.isPublished }
        if (available.isEmpty()) return null

        val ordered = available.sortedBy { it.id }
        val index = ((epochDay % ordered.size) + ordered.size) % ordered.size
        val puzzle = DailyPuzzle(
            epochDay = epochDay,
            problemId = ordered[index.toInt()].id,
            downloadedAt = time.nowMillis(),
        )
        dailyPuzzles.save(puzzle)
        return puzzle
    }
}

/**
 * Turns onboarding answers into a starting profile, seeded ratings and a first
 * learning path.
 */
class CompleteOnboardingUseCase(
    private val profiles: ProfileRepository,
    private val ratings: RatingRepository,
    private val problems: ProblemRepository,
    private val learningPaths: LearningPathRepository,
    private val streaks: StreakRepository,
    private val ratingEngine: RatingEngine = RatingEngine(),
    private val pathEngine: LearningPathEngine = LearningPathEngine(),
    private val time: TimeProvider,
    private val ids: IdGenerator,
) {

    /**
     * @param placement result of the optional placement test. When present its
     *   estimate wins over the self-reported experience level, and the topics
     *   it found solid are folded into the seeded topic ratings.
     */
    suspend operator fun invoke(
        displayName: String,
        answers: OnboardingAnswers,
        placement: PlacementResult? = null,
    ): UserProfile {
        val now = time.nowMillis()
        val startingRating = placement?.estimatedRating
            ?: answers.placementRating
            ?: answers.experienceLevel.startingRating

        val profile = UserProfile(
            id = ids.newId(),
            displayName = displayName.ifBlank { "Guest" },
            isGuest = true,
            onboarding = answers.copy(placementRating = placement?.estimatedRating ?: answers.placementRating),
            createdAt = now,
            startingRating = startingRating,
        )
        profiles.save(profile)

        // Topics the placement test actually demonstrated count as known, on
        // top of whatever the user claimed during onboarding.
        val knownTopics = answers.knownTopics + placement?.strongTopics.orEmpty()
        val seeded = ratingEngine.seedRatings(startingRating, knownTopics, now)
        ratings.save(profile.id, seeded)

        streaks.save(profile.id, StreakState(weeklyGoalDays = answers.weeklyGoalDays))

        val available = problems.all()
        // A brand new user has no attempt history, so the recommender would
        // otherwise fall back to the first topic with content. If the placement
        // test found a real weakness, start there instead.
        val target = placement?.weakTopics
            ?.firstOrNull { topic -> available.any { topic in it.allTopics } }
            ?: pathEngine.selectTargetTopic(seeded, LearningSignals(), available)
        if (target != null) {
            pathEngine.buildPath(
                pathId = ids.newId(),
                userId = profile.id,
                topic = target,
                ratings = seeded,
                signals = LearningSignals(),
                available = available,
                patterns = PatternLibrary.patterns,
                now = now,
            )?.let { learningPaths.save(it) }
        }

        return profile
    }
}

/**
 * Rebuilds the learning path once the current one is finished, or when the
 * user's weakest topic has moved on.
 */
class RefreshLearningPathUseCase(
    private val problems: ProblemRepository,
    private val ratings: RatingRepository,
    private val attempts: AttemptRepository,
    private val learningPaths: LearningPathRepository,
    private val pathEngine: LearningPathEngine = LearningPathEngine(),
    private val time: TimeProvider,
    private val ids: IdGenerator,
) {

    suspend operator fun invoke(userId: String, force: Boolean = false): LearningPath? {
        val existing = learningPaths.active(userId)
        if (existing != null && !existing.isComplete && !force) return existing

        val playerRatings = ratings.load(userId)
        val available = problems.all()
        val recent = attempts.recent(userId, limit = 100)

        val failureCounts = recent
            .filter { !it.wasCorrect }
            .groupingBy { it.problemId }
            .eachCount()

        val signals = LearningSignals(
            recentAttempts = recent,
            repeatedFailures = failureCounts.filterValues { it >= 2 }.keys,
            hintsUsedRecently = recent.sumOf { it.hintsUsed },
        )

        val target = pathEngine.selectTargetTopic(playerRatings, signals, available) ?: return null
        val path = pathEngine.buildPath(
            pathId = ids.newId(),
            userId = userId,
            topic = target,
            ratings = playerRatings,
            signals = signals,
            available = available,
            patterns = PatternLibrary.patterns,
            now = time.nowMillis(),
        ) ?: return null

        learningPaths.save(path)
        return path
    }
}
