package com.codingarena.domain.engine

import com.codingarena.content.ArenaCourse
import com.codingarena.core.common.MILLIS_PER_DAY
import com.codingarena.domain.model.ChapterProgress
import com.codingarena.domain.model.MasteryState
import kotlin.test.Test
import kotlin.test.assertEquals

class CourseProgressEngineTest {
    private val engine = CourseProgressEngine()
    private val course = ArenaCourse.course

    @Test
    fun `prerequisite keeps next rank locked until checkpoint and independent pass`() {
        val variables = course.chapter("variables")!!
        val conditionals = course.chapter("conditionals")!!
        val initial = ChapterProgress("u", variables.id)
        assertEquals(MasteryState.LOCKED, engine.state(conditionals, null, mapOf(variables.id to initial), 0))

        val passed = engine.recordIndependentPass(
            engine.recordCheckpoint(initial, "variables-checkpoint", .7, "e1", 1),
            "variables-independent", "e2", 2,
        )
        assertEquals(MasteryState.LEARNING, engine.state(conditionals, null, mapOf(variables.id to passed), 2))
    }

    @Test
    fun `lesson completion and seventy percent checkpoint unlock practice`() {
        val chapter = course.chapter("variables")!!
        var progress = ChapterProgress("u", chapter.id)
        progress = engine.recordLessonBlock(progress, chapter.lessonBlockIds.first(), "e1", 1)
        assertEquals(MasteryState.CHECKPOINT, engine.state(chapter, progress, mapOf(chapter.id to progress), 1))
        progress = engine.recordCheckpoint(progress, "variables-checkpoint", .70, "e2", 2)
        assertEquals(MasteryState.PRACTICE, engine.state(chapter, progress, mapOf(chapter.id to progress), 2))
    }

    @Test
    fun `three successful review days master chapter and lapse never relocks it`() {
        val chapter = course.chapter("variables")!!
        var progress = ChapterProgress(
            userId = "u",
            chapterId = chapter.id,
            completedBlockIds = chapter.lessonBlockIds,
            checkpointScore = .9,
            passedIndependentExerciseIds = setOf("variables-independent"),
        )
        repeat(3) { day ->
            progress = engine.recordReview(
                progress, "variables-blitz", true, "review-$day", day * MILLIS_PER_DAY,
            )
        }
        assertEquals(MasteryState.MASTERED, engine.state(chapter, progress, mapOf(chapter.id to progress), 3 * MILLIS_PER_DAY))

        progress = engine.recordReview(progress, "variables-blitz", false, "lapse", 31 * MILLIS_PER_DAY)
        assertEquals(MasteryState.REVIEW_DUE, engine.state(chapter, progress, mapOf(chapter.id to progress), 32 * MILLIS_PER_DAY))
    }

    @Test
    fun `placement chooses a rank but keeps earlier course available`() {
        assertEquals("variables", engine.placementChapter(course, 700).id)
        assertEquals("sets-maps", engine.placementChapter(course, 1200).id)
    }
}
