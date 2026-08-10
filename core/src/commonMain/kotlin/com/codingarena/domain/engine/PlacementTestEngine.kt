package com.codingarena.domain.engine

import com.codingarena.domain.model.ChallengeType
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.ExperienceLevel
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The areas the placement test samples (spec 5.1): arrays, strings,
 * complexity, debugging and algorithm recognition.
 */
enum class PlacementArea(val displayName: String) {
    ARRAYS("Arrays"),
    STRINGS("Strings"),
    COMPLEXITY("Complexity"),
    DEBUGGING("Debugging"),
    ALGORITHM_RECOGNITION("Algorithm recognition"),
    ;

    /** Whether [problem] can stand in for this area. */
    fun matches(problem: CodingProblem): Boolean = when (this) {
        ARRAYS -> CodingTopic.ARRAYS in problem.allTopics ||
            CodingTopic.HASH_MAPS in problem.allTopics

        STRINGS -> CodingTopic.STRINGS in problem.allTopics

        COMPLEXITY -> problem.challengeType == ChallengeType.TIME_COMPLEXITY ||
            problem.challengeType == ChallengeType.SPACE_COMPLEXITY

        DEBUGGING -> problem.challengeType == ChallengeType.FIND_THE_BUG ||
            problem.challengeType == ChallengeType.VARIABLE_TRACE ||
            problem.challengeType == ChallengeType.OUTPUT_PREDICTION

        ALGORITHM_RECOGNITION -> problem.challengeType == ChallengeType.PATTERN_RECOGNITION ||
            problem.challengeType == ChallengeType.MULTIPLE_CHOICE ||
            problem.challengeType == ChallengeType.DATA_STRUCTURE_CHOICE
    }
}

/** One answered placement question. */
data class PlacementAnswer(
    val problem: CodingProblem,
    val selectedAnswerIds: List<String>,
) {
    val wasCorrect: Boolean get() = problem.isCorrect(selectedAnswerIds)
}

/** What the placement test concluded. */
data class PlacementResult(
    val estimatedRating: Int,
    val correctCount: Int,
    val totalCount: Int,
    /** Topics the user got right, used to seed their starting topic ratings. */
    val strongTopics: Set<CodingTopic>,
    val weakTopics: Set<CodingTopic>,
    val summary: String,
) {
    val accuracy: Double get() = if (totalCount == 0) 0.0 else correctCount.toDouble() / totalCount
}

/**
 * The optional placement test (spec 5.1).
 *
 * Produces a starting rating estimate rather than a measurement - the whole
 * point of the rating system is that it converges over the first few real
 * problems, so this only needs to land the user in roughly the right band.
 *
 * Scoring reuses Elo with a deliberately large K: eight questions have to move
 * the estimate a long way, which is exactly the behaviour a normal K factor is
 * designed to prevent.
 */
class PlacementTestEngine(private val config: PlacementConfig = PlacementConfig()) {

    /**
     * Chooses the questions.
     *
     * One pass per area first so the test never skips a whole skill, then fills
     * any remaining slots with the closest-to-median problems left over.
     * Questions come back easiest-first so an unsure user is not scared off by
     * question one.
     */
    fun buildTest(
        candidates: List<CodingProblem>,
        questionCount: Int = config.defaultQuestionCount,
    ): List<CodingProblem> {
        val available = candidates.filter { it.isPublished && !it.challengeType.isOrdering }
        if (available.isEmpty()) return emptyList()

        val target = questionCount.coerceIn(config.minQuestions, config.maxQuestions)
        val chosen = linkedSetOf<CodingProblem>()

        PlacementArea.entries.forEach { area ->
            if (chosen.size >= target) return@forEach
            available
                .filter { it !in chosen && area.matches(it) }
                .minByOrNull { kotlin.math.abs(it.difficultyRating - config.anchorRating) }
                ?.let { chosen += it }
        }

        available
            .filter { it !in chosen }
            .sortedBy { kotlin.math.abs(it.difficultyRating - config.anchorRating) }
            .take((target - chosen.size).coerceAtLeast(0))
            .forEach { chosen += it }

        return chosen.sortedBy { it.difficultyRating }
    }

    /**
     * Turns answers into a starting rating.
     *
     * @param baseline where to start from - the experience level the user
     *   selected, so someone who skips questions still lands somewhere sane.
     */
    fun estimate(
        answers: List<PlacementAnswer>,
        baseline: Int = ExperienceLevel.SOME_PRACTICE.startingRating,
    ): PlacementResult {
        if (answers.isEmpty()) {
            return PlacementResult(
                estimatedRating = baseline,
                correctCount = 0,
                totalCount = 0,
                strongTopics = emptySet(),
                weakTopics = emptySet(),
                summary = "No placement test taken - starting you at $baseline based on your " +
                    "experience level. Your rating will settle within a few problems.",
            )
        }

        var rating = baseline.toDouble()
        answers.forEach { answer ->
            val expected = 1.0 / (1.0 + 10.0.pow((answer.problem.difficultyRating - rating) / 400.0))
            val actual = if (answer.wasCorrect) 1.0 else 0.0
            rating += config.kFactor * (actual - expected)
        }

        val estimated = rating.roundToInt().coerceIn(config.floorRating, config.ceilingRating)
        val correct = answers.count { it.wasCorrect }

        val strong = answers.filter { it.wasCorrect }.map { it.problem.primaryTopic }.toSet()
        val weak = answers.filterNot { it.wasCorrect }.map { it.problem.primaryTopic }.toSet() - strong

        return PlacementResult(
            estimatedRating = estimated,
            correctCount = correct,
            totalCount = answers.size,
            strongTopics = strong,
            weakTopics = weak,
            summary = summarise(estimated, correct, answers.size, weak),
        )
    }

    private fun summarise(
        rating: Int,
        correct: Int,
        total: Int,
        weak: Set<CodingTopic>,
    ): String {
        val opener = "You answered $correct of $total correctly, so we are starting you at $rating."
        val closer = when {
            weak.isEmpty() ->
                " Nothing stood out as a weak spot yet - your first few problems will find one."

            weak.size == 1 ->
                " ${weak.first().displayName} looked shakiest, so that is where your first " +
                    "learning path begins."

            else ->
                " ${weak.take(2).joinToString(" and ") { it.displayName }} looked shakiest, so " +
                    "that is where your first learning path begins."
        }
        return opener + closer
    }
}

data class PlacementConfig(
    /**
     * Much larger than the normal K factor. The placement test has only a
     * handful of questions to move the estimate across the whole range.
     */
    val kFactor: Double = 90.0,
    val defaultQuestionCount: Int = 8,
    val minQuestions: Int = 5,
    val maxQuestions: Int = 10,
    /** Difficulty the test is centred on before any answers are known. */
    val anchorRating: Int = 1000,
    val floorRating: Int = 600,
    val ceilingRating: Int = 1800,
)
