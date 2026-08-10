package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * One answer option.
 *
 * [rationale] is what makes the Solution Review specific rather than generic:
 * it explains why *this particular* choice is wrong, so a user who picked the
 * nested-loop option is told about O(n^2) rather than being handed the same
 * paragraph everyone else sees.
 */
@Serializable
data class AnswerChoice(
    val id: String,
    val text: String,
    val rationale: String? = null,
    /**
     * What is *right* about the instinct behind this choice. Authored for
     * plausible distractors so that the review can open with a genuine "good
     * move" line even when the answer was wrong.
     */
    val insight: String? = null,
    /** Optional short label, e.g. a line number for find-the-bug problems. */
    val tag: String? = null,
)

/**
 * A single curated challenge.
 *
 * Problems are content, not user data: they ship inside the app (see
 * `com.codingarena.content.StarterProblems`) and are refreshed from the
 * backend when one is reachable.
 */
@Serializable
data class CodingProblem(
    val id: String,
    val title: String,
    val description: String,
    val difficultyRating: Int,
    val primaryTopic: CodingTopic,
    val secondaryTopics: List<CodingTopic> = emptyList(),
    val challengeType: ChallengeType,
    val choices: List<AnswerChoice>,
    /** Ordered ids for [ChallengeType.isOrdering] problems, single id otherwise. */
    val correctAnswerIds: List<String>,
    val explanation: String,
    val bestApproach: String,
    val codeSnippet: String? = null,
    val timeComplexity: String? = null,
    val spaceComplexity: String? = null,
    val commonMistakes: List<String> = emptyList(),
    val hints: List<String> = emptyList(),
    val patternId: String? = null,
    val estimatedSeconds: Int = 60,
    val isPublished: Boolean = true,
) {
    val difficulty: Difficulty get() = Difficulty.forRating(difficultyRating)

    val practiceMode: PracticeMode get() = challengeType.practiceMode

    /** Primary first, then secondaries, de-duplicated and order-stable. */
    val allTopics: List<CodingTopic>
        get() = (listOf(primaryTopic) + secondaryTopics).distinct()

    val correctAnswerId: String get() = correctAnswerIds.first()

    fun choice(id: String): AnswerChoice? = choices.firstOrNull { it.id == id }

    fun isCorrect(answer: List<String>): Boolean =
        if (challengeType.isOrdering) answer == correctAnswerIds
        else answer.size == 1 && answer.first() == correctAnswerId

    /**
     * Ordering problems can be partially right. Returns the fraction of
     * positions placed correctly; non-ordering problems are all-or-nothing.
     */
    fun partialScore(answer: List<String>): Double {
        if (!challengeType.isOrdering) return if (isCorrect(answer)) 1.0 else 0.0
        if (correctAnswerIds.isEmpty()) return 0.0
        val matched = correctAnswerIds.indices.count { i ->
            answer.getOrNull(i) == correctAnswerIds[i]
        }
        return matched.toDouble() / correctAnswerIds.size
    }

    init {
        require(choices.isNotEmpty()) { "Problem $id has no choices" }
        require(correctAnswerIds.isNotEmpty()) { "Problem $id has no correct answer" }
        val choiceIds = choices.map { it.id }.toSet()
        require(choiceIds.containsAll(correctAnswerIds)) {
            "Problem $id references unknown answer ids"
        }
        require(hints.size <= 3) { "Problem $id declares more than three hints" }
    }
}
