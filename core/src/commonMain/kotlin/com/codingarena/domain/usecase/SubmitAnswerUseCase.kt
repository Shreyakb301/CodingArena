package com.codingarena.domain.usecase

import com.codingarena.content.PatternLibrary
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.engine.AchievementContext
import com.codingarena.domain.engine.AchievementEngine
import com.codingarena.domain.engine.LearningPathEngine
import com.codingarena.domain.engine.RatingEngine
import com.codingarena.domain.engine.SolutionReviewEngine
import com.codingarena.domain.engine.SpacedRepetitionEngine
import com.codingarena.domain.engine.StreakEngine
import com.codingarena.domain.model.Achievement
import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.DailyPuzzleResult
import com.codingarena.domain.model.PlayerStats
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.model.RatingHistoryEntry
import com.codingarena.domain.model.RatingUpdate
import com.codingarena.domain.model.SolutionReview
import com.codingarena.domain.model.StreakActivity
import com.codingarena.domain.repository.AchievementRepository
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.repository.CodeRushRepository
import com.codingarena.domain.repository.DailyPuzzleRepository
import com.codingarena.domain.repository.LearningPathRepository
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.repository.ProfileRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.ReviewRepository
import com.codingarena.domain.repository.StreakRepository

/** What the user submitted. */
data class AnswerSubmission(
    val userId: String,
    val problem: CodingProblem,
    val selectedAnswerIds: List<String>,
    val startedAt: Long,
    val hintsUsed: Int = 0,
    val attemptsCount: Int = 1,
    val source: AttemptSource = AttemptSource.PRACTICE,
)

/** Everything the Solution Review screen needs, produced in one pass. */
data class PracticeResult(
    val attempt: PracticeAttempt,
    val review: SolutionReview,
    val ratingUpdate: RatingUpdate,
    val streakExtended: Boolean,
    val newAchievements: List<Achievement>,
    val nextReviewAt: Long,
)

/**
 * The core learning loop: solve -> review -> rate -> reschedule.
 *
 * Deliberately one use case rather than several. Every one of these effects
 * must happen for a single answer, and splitting them across call sites is how
 * a rating gets recorded without its matching review entry (or vice versa)
 * when a screen forgets a step.
 */
class SubmitAnswerUseCase(
    private val problems: ProblemRepository,
    private val profiles: ProfileRepository,
    private val attempts: AttemptRepository,
    private val ratings: RatingRepository,
    private val reviews: ReviewRepository,
    private val streaks: StreakRepository,
    private val achievements: AchievementRepository,
    private val dailyPuzzles: DailyPuzzleRepository,
    private val learningPaths: LearningPathRepository,
    private val codeRush: CodeRushRepository,
    private val ratingEngine: RatingEngine = RatingEngine(),
    private val reviewEngine: SolutionReviewEngine = SolutionReviewEngine(),
    private val srsEngine: SpacedRepetitionEngine = SpacedRepetitionEngine(),
    private val streakEngine: StreakEngine = StreakEngine(),
    private val pathEngine: LearningPathEngine = LearningPathEngine(),
    private val achievementEngine: AchievementEngine,
    private val time: TimeProvider,
    private val ids: IdGenerator,
) {

    suspend operator fun invoke(submission: AnswerSubmission): PracticeResult {
        val now = time.nowMillis()
        val problem = submission.problem

        val correct = problem.isCorrect(submission.selectedAnswerIds)
        val outcome = AnswerOutcome.classify(
            correct = correct,
            partialScore = problem.partialScore(submission.selectedAnswerIds),
            hintsUsed = submission.hintsUsed,
            attemptsCount = submission.attemptsCount,
        )

        val ratingsBefore = ratings.load(submission.userId)
        val (ratingsAfter, ratingUpdate) = ratingEngine.rate(
            ratings = ratingsBefore,
            problem = problem,
            outcome = outcome,
            source = submission.source,
            now = now,
        )

        val attempt = PracticeAttempt(
            id = ids.newId(),
            userId = submission.userId,
            problemId = problem.id,
            startedAt = submission.startedAt,
            completedAt = now,
            selectedAnswerIds = submission.selectedAnswerIds,
            outcome = outcome,
            attemptsCount = submission.attemptsCount,
            hintsUsed = submission.hintsUsed,
            ratingBefore = ratingUpdate.overallBefore,
            ratingAfter = ratingUpdate.overallAfter,
            source = submission.source,
            synced = false,
        )

        attempts.record(attempt)
        ratings.save(submission.userId, ratingsAfter)
        ratings.appendHistory(
            RatingHistoryEntry(
                id = ids.newId(),
                userId = submission.userId,
                topic = null,
                rating = ratingUpdate.overallAfter,
                change = ratingUpdate.overallChange,
                recordedAt = now,
                problemId = problem.id,
            )
        )
        ratingUpdate.topicChanges.forEach { (topic, change) ->
            ratings.appendHistory(
                RatingHistoryEntry(
                    id = ids.newId(),
                    userId = submission.userId,
                    topic = topic,
                    rating = ratingsAfter.topicRating(topic),
                    change = change,
                    recordedAt = now,
                    problemId = problem.id,
                )
            )
        }

        val scheduled = srsEngine.schedule(
            problem = problem,
            outcome = outcome,
            hintsUsed = submission.hintsUsed,
            solveDurationMs = attempt.solveDurationMs,
            now = now,
            existing = reviews.forProblem(submission.userId, problem.id),
            userId = submission.userId,
        )
        reviews.save(scheduled)

        // Code Rush credits the streak per session, not per question, so it is
        // excluded here - the session controller records it once at the end.
        val streakResult = when (submission.source) {
            AttemptSource.CODE_RUSH -> null
            AttemptSource.DAILY_PUZZLE -> creditStreak(submission.userId, StreakActivity.DAILY_PUZZLE, now)
            AttemptSource.SCHEDULED_REVIEW -> creditStreak(submission.userId, StreakActivity.SCHEDULED_REVIEW, now)
            AttemptSource.LEARNING_PATH -> creditStreak(submission.userId, StreakActivity.LESSON, now)
            else -> null
        }

        if (submission.source == AttemptSource.DAILY_PUZZLE) {
            dailyPuzzles.saveResult(
                userId = submission.userId,
                result = DailyPuzzleResult(
                    epochDay = time.epochDay(now),
                    problemId = problem.id,
                    attemptId = attempt.id,
                    outcome = outcome,
                    solveDurationMs = attempt.solveDurationMs ?: 0L,
                    hintsUsed = submission.hintsUsed,
                    attemptsCount = submission.attemptsCount,
                    ratingChange = ratingUpdate.overallChange,
                    completedAt = now,
                ),
            )
        }

        if (correct) {
            learningPaths.active(submission.userId)?.let { path ->
                if (path.steps.any { problem.id in it.problemIds }) {
                    learningPaths.save(pathEngine.recordCompletion(path, problem.id, now))
                }
            }
        }

        val candidates = problems.all()
        val review = reviewEngine.build(
            problem = problem,
            attempt = attempt,
            userRatingBefore = ratingUpdate.overallBefore,
            ratingUpdate = ratingUpdate,
            pattern = problem.patternId?.let(PatternLibrary::byId),
            candidates = candidates,
            nextReviewAt = scheduled.dueAt,
        )

        val recentAttempts = attempts.recent(submission.userId, limit = RECENT_WINDOW)
        val alreadyUnlocked = achievements.unlocked(submission.userId)
        val context = AchievementContext(
            stats = statsFrom(recentAttempts),
            ratings = ratingsAfter,
            streak = streakResult ?: streaks.load(submission.userId),
            startingRating = profiles.current()?.startingRating
                ?: com.codingarena.domain.model.PlayerRatings.DEFAULT_RATING,
            hintFreeStreak = hintFreeStreak(recentAttempts),
            bestCodeRushScore = codeRush.bestScore(submission.userId),
            patternsMastered = patternsMastered(recentAttempts),
            bestReviewLabel = review.label,
        )
        val unlocked = achievementEngine.newlyUnlocked(context, alreadyUnlocked, now)
        if (unlocked.isNotEmpty()) {
            achievements.unlock(submission.userId, unlocked, now)
        }

        return PracticeResult(
            attempt = attempt,
            review = review,
            ratingUpdate = ratingUpdate,
            streakExtended = streakResult != null,
            newAchievements = unlocked,
            nextReviewAt = scheduled.dueAt,
        )
    }

    private suspend fun creditStreak(
        userId: String,
        activity: StreakActivity,
        now: Long,
    ): com.codingarena.domain.model.StreakState {
        val current = streakEngine.refresh(streaks.load(userId), time.epochDay(now))
        val result = streakEngine.recordActivity(current, activity, time.epochDay(now))
        streaks.save(userId, result.state)
        return result.state
    }

    /**
     * Counts consecutive hint-free correct answers from the most recent
     * attempt backwards. [recent] must be newest first.
     */
    private fun hintFreeStreak(recent: List<PracticeAttempt>): Int =
        recent.takeWhile { it.wasCorrect && it.hintsUsed == 0 }.size

    /**
     * A pattern counts as mastered once every problem the library lists for it
     * has been solved correctly at least once.
     */
    private fun patternsMastered(recent: List<PracticeAttempt>): Int {
        val solved = recent.filter { it.wasCorrect }.map { it.problemId }.toSet()
        return PatternLibrary.patterns.count { pattern ->
            val required = pattern.allProblemIds
            required.isNotEmpty() && solved.containsAll(required)
        }
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
        /** How far back the achievement and stat calculations look. */
        const val RECENT_WINDOW = 200
    }
}
