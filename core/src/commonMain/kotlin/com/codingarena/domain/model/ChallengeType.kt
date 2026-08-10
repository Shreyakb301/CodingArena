package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * Mobile-friendly challenge formats. No free-form code editor is involved:
 * every format resolves to picking or ordering a small set of choices, which
 * keeps a full solve inside a 30-90 second window on a phone.
 */
@Serializable
enum class ChallengeType(
    val displayName: String,
    val prompt: String,
    /** Answering means putting choices in order rather than picking one. */
    val isOrdering: Boolean = false,
) {
    MULTIPLE_CHOICE("Algorithm Selection", "Which approach solves this best?"),
    OUTPUT_PREDICTION("Predict the Output", "What does this code print?"),
    FIND_THE_BUG("Find the Bug", "Which line breaks this code?"),
    TIME_COMPLEXITY("Time Complexity", "What is the time complexity?"),
    SPACE_COMPLEXITY("Space Complexity", "What is the space complexity?"),
    FILL_IN_THE_BLANK("Complete the Code", "Which line belongs in the blank?"),
    REARRANGE_CODE("Rearrange the Code", "Put these lines in the right order.", isOrdering = true),
    EDGE_CASE("Edge Case", "Which input breaks this solution?"),
    VARIABLE_TRACE("Trace the Variables", "What is the value at the marked line?"),
    PATTERN_RECOGNITION("Pattern Match", "Which pattern does this problem call for?"),
    DATA_STRUCTURE_CHOICE("Data Structure", "Which data structure fits best?"),
    ;

    /** Which practice-mode rating a challenge of this type moves. */
    val practiceMode: PracticeMode
        get() = when (this) {
            FIND_THE_BUG, VARIABLE_TRACE, OUTPUT_PREDICTION -> PracticeMode.DEBUGGING
            TIME_COMPLEXITY, SPACE_COMPLEXITY -> PracticeMode.COMPLEXITY
            PATTERN_RECOGNITION, MULTIPLE_CHOICE, DATA_STRUCTURE_CHOICE -> PracticeMode.PATTERN_RECOGNITION
            FILL_IN_THE_BLANK, REARRANGE_CODE, EDGE_CASE -> PracticeMode.PUZZLE
        }
}

/** Rating tracks kept separately from topic ratings (spec 5.6). */
@Serializable
enum class PracticeMode(val displayName: String) {
    PUZZLE("Puzzle"),
    DEBUGGING("Debugging"),
    COMPLEXITY("Complexity"),
    PATTERN_RECOGNITION("Pattern Recognition"),
    CODE_RUSH("Code Rush"),
}

/**
 * A readable band over a problem's numeric difficulty rating. Ratings, not
 * labels, drive the engine; bands exist only so the UI can say something
 * friendlier than "1340".
 */
@Serializable
enum class Difficulty(val displayName: String, val range: IntRange) {
    INTRODUCTORY("Introductory", 0..899),
    EASY("Easy", 900..1199),
    MEDIUM("Medium", 1200..1499),
    HARD("Hard", 1500..1799),
    EXPERT("Expert", 1800..Int.MAX_VALUE),
    ;

    companion object {
        fun forRating(rating: Int): Difficulty =
            entries.first { rating in it.range }
    }
}
