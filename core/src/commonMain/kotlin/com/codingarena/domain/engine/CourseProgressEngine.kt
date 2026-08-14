package com.codingarena.domain.engine

import com.codingarena.core.common.MILLIS_PER_DAY
import com.codingarena.domain.model.Chapter
import com.codingarena.domain.model.ChapterProgress
import com.codingarena.domain.model.Course
import com.codingarena.domain.model.EvidenceKind
import com.codingarena.domain.model.MasteryState
import com.codingarena.domain.model.SkillEvidence

/** Pure unlock and mastery rules shared by every platform and the server. */
class CourseProgressEngine {

    fun state(
        chapter: Chapter,
        progress: ChapterProgress?,
        allProgress: Map<String, ChapterProgress>,
        now: Long,
    ): MasteryState {
        if (!chapter.available || !prerequisitesPassed(chapter, allProgress)) return MasteryState.LOCKED
        val value = progress ?: return MasteryState.LEARNING
        if (value.reviewDueAt?.let { it <= now } == true && hasMastery(value)) {
            return MasteryState.REVIEW_DUE
        }
        if (hasMastery(value)) return MasteryState.MASTERED
        if (value.checkpointScore >= CHECKPOINT_PASS && value.passedIndependentExerciseIds.isNotEmpty()) {
            return MasteryState.PRACTICE
        }
        if (value.completedBlockIds.containsAll(chapter.lessonBlockIds)) {
            return if (value.checkpointScore >= CHECKPOINT_PASS) MasteryState.PRACTICE
            else MasteryState.CHECKPOINT
        }
        return MasteryState.LEARNING
    }

    fun states(
        course: Course,
        progress: Map<String, ChapterProgress>,
        now: Long,
    ): Map<String, MasteryState> = course.chapters.associate { chapter ->
        chapter.id to state(chapter, progress[chapter.id], progress, now)
    }

    fun recordLessonBlock(
        progress: ChapterProgress,
        blockId: String,
        evidenceId: String,
        now: Long,
    ): ChapterProgress = progress.copy(
        completedBlockIds = progress.completedBlockIds + blockId,
        evidence = progress.evidence + SkillEvidence(
            evidenceId, progress.chapterId, EvidenceKind.LESSON_BLOCK, occurredAt = now,
        ),
        lastPractisedAt = now,
    )

    fun recordCheckpoint(
        progress: ChapterProgress,
        exerciseId: String,
        score: Double,
        evidenceId: String,
        now: Long,
    ): ChapterProgress = progress.copy(
        checkpointScore = maxOf(progress.checkpointScore, score.coerceIn(0.0, 1.0)),
        evidence = progress.evidence + SkillEvidence(
            evidenceId, progress.chapterId, EvidenceKind.CHECKPOINT, exerciseId, score, now,
        ),
        lastPractisedAt = now,
    )

    fun recordIndependentPass(
        progress: ChapterProgress,
        exerciseId: String,
        evidenceId: String,
        now: Long,
    ): ChapterProgress = progress.copy(
        passedIndependentExerciseIds = progress.passedIndependentExerciseIds + exerciseId,
        evidence = progress.evidence + SkillEvidence(
            evidenceId, progress.chapterId, EvidenceKind.INDEPENDENT_PASS, exerciseId, 1.0, now,
        ),
        reviewDueAt = progress.reviewDueAt ?: now + MILLIS_PER_DAY,
        lastPractisedAt = now,
    )

    fun recordReview(
        progress: ChapterProgress,
        exerciseId: String,
        passed: Boolean,
        evidenceId: String,
        now: Long,
    ): ChapterProgress {
        val successes = distinctReviewDays(progress).size + if (passed) 1 else 0
        val days = when {
            !passed -> 1
            successes >= MASTERY_REVIEW_COUNT -> 30
            successes == 2 -> 7
            else -> 3
        }
        return progress.copy(
            evidence = progress.evidence + SkillEvidence(
                evidenceId,
                progress.chapterId,
                if (passed) EvidenceKind.REVIEW_PASS else EvidenceKind.REVIEW_LAPSE,
                exerciseId,
                if (passed) 1.0 else 0.0,
                now,
            ),
            reviewDueAt = now + days * MILLIS_PER_DAY,
            lastPractisedAt = now,
        )
    }

    fun placementChapter(course: Course, estimatedRating: Int): Chapter = course.chapters
        .filter { it.available }
        .lastOrNull { chapter -> estimatedRating >= 700 + (chapter.rank - 1) * 75 }
        ?: course.chapters.first { it.available }

    private fun prerequisitesPassed(
        chapter: Chapter,
        progress: Map<String, ChapterProgress>,
    ): Boolean = chapter.prerequisiteIds.all { id ->
        progress[id]?.let {
            it.checkpointScore >= CHECKPOINT_PASS && it.passedIndependentExerciseIds.isNotEmpty()
        } == true
    }

    private fun hasMastery(progress: ChapterProgress): Boolean =
        distinctReviewDays(progress).size >= MASTERY_REVIEW_COUNT

    private fun distinctReviewDays(progress: ChapterProgress): Set<Long> = progress.evidence
        .filter { it.kind == EvidenceKind.REVIEW_PASS }
        .map { it.occurredAt / MILLIS_PER_DAY }
        .toSet()

    companion object {
        const val CHECKPOINT_PASS = 0.70
        const val MASTERY_REVIEW_COUNT = 3
    }
}
