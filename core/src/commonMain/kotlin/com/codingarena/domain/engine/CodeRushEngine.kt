package com.codingarena.domain.engine

import com.codingarena.domain.model.ChallengeType
import com.codingarena.domain.model.CodeRushAnswer
import com.codingarena.domain.model.CodeRushMode
import com.codingarena.domain.model.CodeRushSession
import com.codingarena.domain.model.CodeRushStats
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.RushEndReason
import kotlin.math.abs

/**
 * Runs a Code Rush session (spec 5.8).
 *
 * Holds no timers of its own - the caller owns the clock and passes `now` in.
 * That keeps a three-minute run fully testable in microseconds.
 */
class CodeRushEngine(
    private val config: CodeRushConfig = CodeRushConfig(),
) {

    fun start(
        sessionId: String,
        userId: String,
        mode: CodeRushMode,
        now: Long,
    ): CodeRushSession = CodeRushSession(
        id = sessionId,
        userId = userId,
        mode = mode,
        startedAt = now,
        livesRemaining = CodeRushSession.STARTING_LIVES,
    )

    /**
     * Difficulty for the next question.
     *
     * Starts easy and ramps with each correct answer, so the opening questions
     * build momentum and the ceiling is found rather than imposed.
     */
    fun targetDifficulty(session: CodeRushSession): Int {
        val correct = session.score
        return config.startingDifficulty + correct * config.difficultyStep
    }

    /** Picks the next problem, avoiding anything already served this run. */
    fun nextProblem(
        session: CodeRushSession,
        candidates: List<CodingProblem>,
    ): CodingProblem? {
        val seen = session.answers.map { it.problemId }.toSet()
        val target = targetDifficulty(session)
        val topicFilter = (session.mode as? CodeRushMode.TopicRush)?.topic

        return candidates
            .asSequence()
            .filter { it.isPublished && it.id !in seen }
            .filter { it.estimatedSeconds <= config.maxQuestionSeconds }
            .filter { topicFilter == null || topicFilter in it.allTopics }
            .sortedWith(compareBy({ abs(it.difficultyRating - target) }, { it.id }))
            .firstOrNull()
    }

    /** Records an answer and applies the life cost of getting it wrong. */
    fun submit(
        session: CodeRushSession,
        problem: CodingProblem,
        wasCorrect: Boolean,
        elapsedMs: Long,
        now: Long,
    ): CodeRushSession {
        if (session.isOver) return session

        val answer = CodeRushAnswer(
            problemId = problem.id,
            topic = problem.primaryTopic,
            challengeType = problem.challengeType,
            wasCorrect = wasCorrect,
            elapsedMs = elapsedMs,
            difficultyRating = problem.difficultyRating,
        )
        val lives = if (wasCorrect) session.livesRemaining else session.livesRemaining - 1
        val updated = session.copy(
            answers = session.answers + answer,
            livesRemaining = lives.coerceAtLeast(0),
        )
        return if (lives <= 0) finish(updated, RushEndReason.OUT_OF_LIVES, now) else updated
    }

    /** True once the clock has run out for timed modes. */
    fun isTimeUp(session: CodeRushSession, now: Long): Boolean {
        val duration = session.mode.durationSeconds ?: return false
        return now - session.startedAt >= duration * 1000L
    }

    fun secondsRemaining(session: CodeRushSession, now: Long): Int? {
        val duration = session.mode.durationSeconds ?: return null
        val elapsed = ((now - session.startedAt) / 1000L).toInt()
        return (duration - elapsed).coerceAtLeast(0)
    }

    fun finish(
        session: CodeRushSession,
        reason: RushEndReason,
        now: Long,
    ): CodeRushSession =
        if (session.isOver) session else session.copy(endedAt = now, endReason = reason)

    /**
     * Rolls finished sessions into the profile record.
     *
     * Strongest and weakest categories need a minimum sample before they are
     * reported, so one unlucky bug-hunt question does not brand debugging as a
     * user's weakness.
     */
    fun summarise(sessions: List<CodeRushSession>): CodeRushStats {
        val finished = sessions.filter { it.isOver }
        if (finished.isEmpty()) return CodeRushStats()

        val answers = finished.flatMap { it.answers }
        val byType = answers.groupBy { it.challengeType }
            .filterValues { it.size >= config.minSamplesForCategory }
            .mapValues { (_, list) -> list.count { it.wasCorrect }.toDouble() / list.size }

        return CodeRushStats(
            bestScore = finished.maxOf { it.score },
            sessionsPlayed = finished.size,
            averageScore = finished.sumOf { it.score }.toDouble() / finished.size,
            accuracy = if (answers.isEmpty()) 0.0
            else answers.count { it.wasCorrect }.toDouble() / answers.size,
            strongestCategory = byType.maxByOrNull { it.value }?.key,
            weakestCategory = byType.minByOrNull { it.value }?.key,
        )
    }
}

data class CodeRushConfig(
    val startingDifficulty: Int = 800,
    val difficultyStep: Int = 45,
    /** Long-form problems are excluded from rush rounds. */
    val maxQuestionSeconds: Int = 45,
    val minSamplesForCategory: Int = 3,
)

/** Categories reported on the rush summary, for the UI to label. */
val CodeRushStats.hasCategoryInsight: Boolean
    get() = strongestCategory != null && weakestCategory != null &&
        strongestCategory != weakestCategory

/** Challenge types that suit rapid-fire answering. */
val rushFriendlyTypes: Set<ChallengeType> = setOf(
    ChallengeType.FIND_THE_BUG,
    ChallengeType.OUTPUT_PREDICTION,
    ChallengeType.MULTIPLE_CHOICE,
    ChallengeType.TIME_COMPLEXITY,
    ChallengeType.SPACE_COMPLEXITY,
    ChallengeType.FILL_IN_THE_BLANK,
    ChallengeType.EDGE_CASE,
    ChallengeType.DATA_STRUCTURE_CHOICE,
)
