package com.codingarena.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: String,
    val title: String,
    val description: String,
    val chapters: List<Chapter>,
) {
    fun chapter(id: String): Chapter? = chapters.firstOrNull { it.id == id }
    fun exercise(id: String): Exercise? = chapters.asSequence()
        .flatMap { it.exercises.asSequence() }
        .firstOrNull { it.id == id }
}

@Serializable
data class Chapter(
    val id: String,
    val title: String,
    val rank: Int,
    val summary: String,
    val prerequisiteIds: List<String> = emptyList(),
    val lessons: List<Lesson>,
    val exercises: List<Exercise>,
    val patternId: String? = null,
    val available: Boolean = true,
) {
    val lessonBlockIds: Set<String>
        get() = lessons.flatMap { lesson -> lesson.blocks.map { it.id } }.toSet()

    val independentExercises: List<Exercise>
        get() = exercises.filter { it.mode == ExerciseMode.INDEPENDENT_CODE }

    val checkpointExercises: List<Exercise>
        get() = exercises.filter { it.mode == ExerciseMode.CHECKPOINT }
}

@Serializable
data class Lesson(
    val id: String,
    val title: String,
    val blocks: List<ConceptBlock>,
)

@Serializable
data class ConceptBlock(
    val id: String,
    val title: String,
    val explanation: String,
    val visualExample: String,
    val recognitionSignals: List<String> = emptyList(),
)

@Serializable
enum class ExerciseMode(val displayName: String) {
    TRACE("Trace the position"),
    REARRANGE("Arrange the moves"),
    DEBUG("Find the blunder"),
    FILL_CODE("Complete the line"),
    CHECKPOINT("Chapter checkpoint"),
    INDEPENDENT_CODE("Independent solve"),
    BLITZ("Choose the strategy"),
}

@Serializable
data class Exercise(
    val id: String,
    val chapterId: String,
    val title: String,
    val prompt: String,
    val mode: ExerciseMode,
    val conceptIds: List<String>,
    val recognitionSignals: List<String> = emptyList(),
    val difficultyRating: Int,
    val patternId: String? = null,
    val supportedLanguages: Set<ProgrammingLanguage> = ProgrammingLanguage.entries.toSet(),
    val templates: List<LanguageTemplate> = emptyList(),
    val examples: List<ExampleTest> = emptyList(),
    val hiddenTestCount: Int = 0,
    val choices: List<String> = emptyList(),
    val correctChoiceIndex: Int? = null,
    val explanation: String,
)

@Serializable
data class LanguageTemplate(
    val language: ProgrammingLanguage,
    val starterCode: String,
)

@Serializable
data class ExampleTest(
    val input: String,
    val expectedOutput: String,
    val explanation: String? = null,
)

@Serializable
enum class MasteryState(val displayName: String) {
    LOCKED("Locked"),
    LEARNING("Learning"),
    CHECKPOINT("Checkpoint"),
    PRACTICE("Practice"),
    MASTERED("Mastered"),
    REVIEW_DUE("Review due"),
}

@Serializable
enum class EvidenceKind {
    LESSON_BLOCK,
    CHECKPOINT,
    INDEPENDENT_PASS,
    REVIEW_PASS,
    REVIEW_LAPSE,
}

@Serializable
data class SkillEvidence(
    val id: String,
    val chapterId: String,
    val kind: EvidenceKind,
    val exerciseId: String? = null,
    val score: Double? = null,
    val occurredAt: Long,
)

@Serializable
data class ChapterProgress(
    val userId: String,
    val chapterId: String,
    val completedBlockIds: Set<String> = emptySet(),
    val checkpointScore: Double = 0.0,
    val passedIndependentExerciseIds: Set<String> = emptySet(),
    val evidence: List<SkillEvidence> = emptyList(),
    val reviewDueAt: Long? = null,
    val lastPractisedAt: Long? = null,
)

@Serializable
enum class ReviewDueReason {
    SCHEDULED_RECALL,
    RECENT_LAPSE,
    SLOW_RECALL,
    REPEATED_CONFUSION,
}

@Serializable
data class ReviewCard(
    val id: String,
    val userId: String,
    val chapterId: String,
    val exerciseId: String,
    val dueAt: Long,
    val reason: ReviewDueReason,
)

