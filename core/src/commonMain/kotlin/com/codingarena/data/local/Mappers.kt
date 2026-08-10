package com.codingarena.data.local

import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.ModeRating
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.model.PracticeMode
import com.codingarena.domain.model.ReviewStage
import com.codingarena.domain.model.ScheduledReview
import com.codingarena.domain.model.StreakState
import com.codingarena.domain.model.UserTopicRating
import kotlinx.serialization.json.Json

/**
 * Row <-> model conversions.
 *
 * Enums are stored by name and read back defensively: a row written by a newer
 * build that names an enum this build does not know must not crash the app, so
 * unknown values fall back rather than throwing.
 */
internal val arenaJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal fun String.toTopicOrNull(): CodingTopic? =
    CodingTopic.entries.firstOrNull { it.name == this }

internal fun String.toOutcome(): AnswerOutcome =
    AnswerOutcome.entries.firstOrNull { it.name == this } ?: AnswerOutcome.INCORRECT

internal fun String.toAttemptSource(): AttemptSource =
    AttemptSource.entries.firstOrNull { it.name == this } ?: AttemptSource.PRACTICE

internal fun String.toReviewStage(): ReviewStage =
    ReviewStage.entries.firstOrNull { it.name == this } ?: ReviewStage.RELEARN

internal fun String.toPracticeMode(): PracticeMode? =
    PracticeMode.entries.firstOrNull { it.name == this }

/** Answer id lists are stored as a single delimited column. */
internal fun List<String>.joinAnswers(): String = joinToString(ANSWER_DELIMITER)

internal fun String.splitAnswers(): List<String> =
    if (isEmpty()) emptyList() else split(ANSWER_DELIMITER)

internal fun Set<Long>.joinDays(): String = joinToString(ANSWER_DELIMITER)

internal fun String.splitDays(): Set<Long> =
    if (isEmpty()) emptySet() else split(ANSWER_DELIMITER).mapNotNull { it.toLongOrNull() }.toSet()

private const val ANSWER_DELIMITER = ","

internal fun com.codingarena.db.Attempt.toDomain() = PracticeAttempt(
    id = id,
    userId = userId,
    problemId = problemId,
    startedAt = startedAt,
    completedAt = completedAt,
    selectedAnswerIds = selectedAnswers.splitAnswers(),
    outcome = outcome.toOutcome(),
    attemptsCount = attemptsCount.toInt(),
    hintsUsed = hintsUsed.toInt(),
    ratingBefore = ratingBefore.toInt(),
    ratingAfter = ratingAfter.toInt(),
    source = source.toAttemptSource(),
    synced = synced == 1L,
)

internal fun com.codingarena.db.TopicRating.toDomain(): UserTopicRating? {
    val parsed = topic.toTopicOrNull() ?: return null
    return UserTopicRating(
        topic = parsed,
        rating = rating.toInt(),
        attempts = attempts.toInt(),
        correctAnswers = correctAnswers.toInt(),
        lastPracticedAt = lastPracticedAt,
    )
}

internal fun com.codingarena.db.ModeRating.toDomain(): ModeRating? {
    val parsed = mode.toPracticeMode() ?: return null
    return ModeRating(
        mode = parsed,
        rating = rating.toInt(),
        attempts = attempts.toInt(),
        correctAnswers = correctAnswers.toInt(),
    )
}

internal fun com.codingarena.db.ReviewSchedule.toDomain(): ScheduledReview? {
    val parsed = topic.toTopicOrNull() ?: return null
    return ScheduledReview(
        problemId = problemId,
        userId = userId,
        stage = stage.toReviewStage(),
        dueAt = dueAt,
        lastReviewedAt = lastReviewedAt,
        repetitions = repetitions.toInt(),
        lapses = lapses.toInt(),
        topic = parsed,
    )
}

internal fun com.codingarena.db.Streak.toDomain() = StreakState(
    currentStreak = currentStreak.toInt(),
    longestStreak = longestStreak.toInt(),
    lastActiveDay = lastActiveDay,
    weeklyGoalDays = weeklyGoalDays.toInt(),
    activeDaysThisWeek = activeDaysThisWeek.splitDays(),
    totalActiveDays = totalActiveDays.toInt(),
)
