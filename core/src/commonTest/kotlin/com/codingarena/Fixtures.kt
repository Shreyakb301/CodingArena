package com.codingarena

import com.codingarena.domain.model.AnswerChoice
import com.codingarena.domain.model.AnswerOutcome
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.ChallengeType
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PracticeAttempt

fun problem(
    id: String = "p1",
    rating: Int = 1000,
    topic: CodingTopic = CodingTopic.ARRAYS,
    secondary: List<CodingTopic> = emptyList(),
    type: ChallengeType = ChallengeType.MULTIPLE_CHOICE,
    estimatedSeconds: Int = 60,
    correctIds: List<String> = listOf("a"),
    choices: List<AnswerChoice> = listOf(
        AnswerChoice("a", "Correct choice"),
        AnswerChoice("b", "Wrong choice", rationale = "This is O(n^2).", insight = "The instinct to scan twice is natural."),
    ),
    hints: List<String> = listOf("Think about lookups."),
    patternId: String? = null,
) = CodingProblem(
    id = id,
    title = "Problem $id",
    description = "Description for $id",
    difficultyRating = rating,
    primaryTopic = topic,
    secondaryTopics = secondary,
    challengeType = type,
    choices = choices,
    correctAnswerIds = correctIds,
    explanation = "Explanation for $id.",
    bestApproach = "Use a hash map for O(1) lookups.",
    timeComplexity = "O(n)",
    spaceComplexity = "O(n)",
    commonMistakes = listOf("Forgetting the empty input"),
    hints = hints,
    patternId = patternId,
    estimatedSeconds = estimatedSeconds,
)

fun attempt(
    problemId: String = "p1",
    outcome: AnswerOutcome = AnswerOutcome.CORRECT_NO_HINTS,
    hintsUsed: Int = 0,
    attemptsCount: Int = 1,
    selected: List<String> = listOf("a"),
    startedAt: Long = 0L,
    completedAt: Long? = 30_000L,
    source: AttemptSource = AttemptSource.PRACTICE,
) = PracticeAttempt(
    id = "attempt-$problemId",
    userId = "user-1",
    problemId = problemId,
    startedAt = startedAt,
    completedAt = completedAt,
    selectedAnswerIds = selected,
    outcome = outcome,
    attemptsCount = attemptsCount,
    hintsUsed = hintsUsed,
    source = source,
)
