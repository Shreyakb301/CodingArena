package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/** Code Rush formats (spec 5.8). */
@Serializable
sealed interface CodeRushMode {
    val displayName: String
    val durationSeconds: Int?

    @Serializable
    data object ThreeMinute : CodeRushMode {
        override val displayName get() = "3 Minute Rush"
        override val durationSeconds get() = 180
    }

    @Serializable
    data object FiveMinute : CodeRushMode {
        override val displayName get() = "5 Minute Rush"
        override val durationSeconds get() = 300
    }

    /** No clock - the run ends when lives run out. */
    @Serializable
    data object Survival : CodeRushMode {
        override val displayName get() = "Survival"
        override val durationSeconds: Int? get() = null
    }

    @Serializable
    data class TopicRush(val topic: CodingTopic) : CodeRushMode {
        override val displayName get() = "${topic.displayName} Rush"
        override val durationSeconds get() = 300
    }

    companion object {
        /** Only 5 Minute Rush is offered from the Code Rush menu today. */
        val standardModes: List<CodeRushMode> = listOf(FiveMinute)
    }
}

/** Why a run ended - drives the wording on the summary screen. */
@Serializable
enum class RushEndReason { TIME_UP, OUT_OF_LIVES, NO_MORE_PROBLEMS, ABANDONED }

@Serializable
data class CodeRushAnswer(
    val problemId: String,
    val topic: CodingTopic,
    val challengeType: ChallengeType,
    val wasCorrect: Boolean,
    val elapsedMs: Long,
    val difficultyRating: Int,
)

/** A live or finished Code Rush run. */
@Serializable
data class CodeRushSession(
    val id: String,
    val userId: String,
    val mode: CodeRushMode,
    val startedAt: Long,
    val endedAt: Long? = null,
    val livesRemaining: Int = STARTING_LIVES,
    val answers: List<CodeRushAnswer> = emptyList(),
    val endReason: RushEndReason? = null,
) {
    val score: Int get() = answers.count { it.wasCorrect }

    val isOver: Boolean get() = endedAt != null

    val accuracy: Double
        get() = if (answers.isEmpty()) 0.0 else score.toDouble() / answers.size

    val averageAnswerMs: Long
        get() = if (answers.isEmpty()) 0L else answers.sumOf { it.elapsedMs } / answers.size

    companion object {
        const val STARTING_LIVES = 3
    }
}

/** Aggregate Code Rush record shown on the profile. */
@Serializable
data class CodeRushStats(
    val bestScore: Int = 0,
    val sessionsPlayed: Int = 0,
    val averageScore: Double = 0.0,
    val accuracy: Double = 0.0,
    val strongestCategory: ChallengeType? = null,
    val weakestCategory: ChallengeType? = null,
)
