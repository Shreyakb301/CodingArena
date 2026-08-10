package com.codingarena.domain.engine

import com.codingarena.domain.model.CodingPattern
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.LearningPath
import com.codingarena.domain.model.LearningPathStep
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PracticeAttempt
import com.codingarena.domain.model.StepKind
import com.codingarena.domain.model.UserTopicRating

/** Recent behaviour the recommender reads, beyond raw ratings. */
data class LearningSignals(
    val recentAttempts: List<PracticeAttempt> = emptyList(),
    /** Problem ids the user has failed more than once. */
    val repeatedFailures: Set<String> = emptySet(),
    val hintsUsedRecently: Int = 0,
    val topicLastPractised: Map<CodingTopic, Long> = emptyMap(),
)

/**
 * Builds the personalised learning path (spec 5.10).
 *
 * The path is short and finishable on purpose - five steps aimed at one
 * weakness - because the product's claim is measurable improvement on a
 * specific gap, not a curriculum the user abandons in week two.
 */
class LearningPathEngine(
    private val recommender: ProblemRecommender = ProblemRecommender(),
) {

    /**
     * Chooses the topic to work on next.
     *
     * Ordered by need: a topic the user keeps failing outranks one that is
     * merely low-rated, and a topic never practised outranks nothing at all.
     */
    fun selectTargetTopic(
        ratings: PlayerRatings,
        signals: LearningSignals,
        available: List<CodingProblem>,
    ): CodingTopic? {
        val topicsWithContent = available.map { it.primaryTopic }.toSet()
        if (topicsWithContent.isEmpty()) return null

        val failureTopics = signals.recentAttempts
            .filter { !it.wasCorrect }
            .mapNotNull { attempt -> available.firstOrNull { it.id == attempt.problemId }?.primaryTopic }
            .groupingBy { it }
            .eachCount()

        val practised = ratings.topics.values.filter { it.attempts > 0 && it.topic in topicsWithContent }

        return practised
            .sortedWith(
                compareByDescending<UserTopicRating> { failureTopics[it.topic] ?: 0 }
                    .thenBy { it.rating }
                    .thenBy { it.topic.ordinal }
            )
            .firstOrNull()
            ?.topic
            ?: topicsWithContent.minByOrNull { it.ordinal }
    }

    /**
     * Builds a lesson -> easy -> medium -> review -> mastery path for [topic].
     *
     * Steps whose problems do not exist in the content set are dropped rather
     * than left empty, so a path never contains a step the user cannot finish.
     */
    fun buildPath(
        pathId: String,
        userId: String,
        topic: CodingTopic,
        ratings: PlayerRatings,
        signals: LearningSignals,
        available: List<CodingProblem>,
        patterns: List<CodingPattern>,
        now: Long,
    ): LearningPath? {
        val topicProblems = available.filter { it.isPublished && topic in it.allTopics }
        if (topicProblems.isEmpty()) return null

        val base = ratings.topicRating(topic)
        val pattern = patterns.firstOrNull { it.topic == topic }

        val used = mutableSetOf<String>()
        fun pick(targetRating: Int, count: Int = 1): List<String> =
            topicProblems
                .filter { it.id !in used }
                .sortedBy { kotlin.math.abs(it.difficultyRating - targetRating) }
                .take(count)
                .onEach { used += it.id }
                .map { it.id }

        val steps = buildList {
            if (pattern != null) {
                add(
                    LearningPathStep(
                        id = "$pathId-lesson",
                        kind = StepKind.LESSON,
                        title = pattern.name,
                        patternId = pattern.id,
                    )
                )
            }
            pick(base - EASY_OFFSET, 2).takeIf { it.isNotEmpty() }?.let {
                add(
                    LearningPathStep(
                        id = "$pathId-easy",
                        kind = StepKind.EASY_PRACTICE,
                        title = "Warm up: ${topic.displayName}",
                        problemIds = it,
                    )
                )
            }
            pick(base + MEDIUM_OFFSET, 2).takeIf { it.isNotEmpty() }?.let {
                add(
                    LearningPathStep(
                        id = "$pathId-medium",
                        kind = StepKind.MEDIUM_PRACTICE,
                        title = "Stretch: ${topic.displayName}",
                        problemIds = it,
                    )
                )
            }
            val reviewIds = signals.repeatedFailures
                .filter { id -> topicProblems.any { it.id == id } }
                .take(2)
            if (reviewIds.isNotEmpty()) {
                add(
                    LearningPathStep(
                        id = "$pathId-review",
                        kind = StepKind.REVIEW,
                        title = "Revisit what you missed",
                        problemIds = reviewIds,
                    )
                )
            }
            pick(base + MASTERY_OFFSET, 1).takeIf { it.isNotEmpty() }?.let {
                add(
                    LearningPathStep(
                        id = "$pathId-mastery",
                        kind = StepKind.MASTERY,
                        title = "${topic.displayName} mastery check",
                        problemIds = it,
                    )
                )
            }
        }

        if (steps.none { it.problemIds.isNotEmpty() }) return null

        return LearningPath(
            id = pathId,
            userId = userId,
            title = "Improve ${topic.displayName}",
            rationale = rationale(topic, ratings, signals),
            targetTopic = topic,
            steps = steps,
            createdAt = now,
        )
    }

    /**
     * The sentence shown to the user.
     *
     * The spec's benchmark is "You understand basic Sliding Window problems but
     * struggle when the window requires a frequency map" - specific about the
     * boundary, not just "you are weak at X". These templates are built from
     * the concrete numbers behind the recommendation.
     */
    fun rationale(
        topic: CodingTopic,
        ratings: PlayerRatings,
        signals: LearningSignals,
    ): String {
        val topicRating = ratings.topics[topic]
        val gap = ratings.overall - (topicRating?.rating ?: PlayerRatings.DEFAULT_RATING)
        val failures = signals.repeatedFailures.size

        return when {
            topicRating == null || topicRating.attempts == 0 ->
                "You have not practised ${topic.displayName} yet, so it is the biggest unknown " +
                    "in your profile. Five short exercises will give you a real rating here."

            failures >= REPEATED_FAILURE_THRESHOLD ->
                "You have missed the same ${topic.displayName} problems more than once. That " +
                    "usually means the pattern has not clicked yet rather than that you were " +
                    "careless - so this path starts with the lesson, not more practice."

            topicRating.accuracy < LOW_ACCURACY && signals.hintsUsedRecently > 0 ->
                "You are reaching the right answers on ${topic.displayName}, but with hints and " +
                    "at ${(topicRating.accuracy * 100).toInt()}% accuracy. Work these until you " +
                    "can do them unaided."

            gap >= SIGNIFICANT_GAP ->
                "${topic.displayName} sits $gap points below your overall rating of " +
                    "${ratings.overall}. It is the weakness most likely to show up in an " +
                    "interview before the rest of your profile does."

            else ->
                "${topic.displayName} is your lowest rated topic at ${topicRating.rating}. " +
                    "Complete these steps to bring it in line with the rest of your profile."
        }
    }

    /** Marks a problem complete across whichever step contains it. */
    fun recordCompletion(path: LearningPath, problemId: String, now: Long): LearningPath {
        val steps = path.steps.map { step ->
            if (problemId in step.problemIds) {
                val completed = step.completedProblemIds + problemId
                step.copy(
                    completedProblemIds = completed,
                    completedAt = if (completed.containsAll(step.problemIds)) now else step.completedAt,
                )
            } else {
                step
            }
        }
        val updated = path.copy(steps = steps)
        return if (updated.isComplete && path.completedAt == null) {
            updated.copy(completedAt = now)
        } else {
            updated
        }
    }

    /** Marks a lesson step read. */
    fun completeLesson(path: LearningPath, stepId: String, now: Long): LearningPath =
        path.copy(
            steps = path.steps.map {
                if (it.id == stepId && it.kind == StepKind.LESSON) it.copy(completedAt = now) else it
            }
        )

    private companion object {
        const val EASY_OFFSET = 120
        const val MEDIUM_OFFSET = 80
        const val MASTERY_OFFSET = 180
        const val SIGNIFICANT_GAP = 100
        const val LOW_ACCURACY = 0.7
        const val REPEATED_FAILURE_THRESHOLD = 2
    }
}
