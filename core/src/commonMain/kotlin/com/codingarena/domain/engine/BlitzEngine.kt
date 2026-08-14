package com.codingarena.domain.engine

import com.codingarena.content.PatternConfusions
import com.codingarena.core.common.MILLIS_PER_DAY
import com.codingarena.domain.model.Curriculum
import com.codingarena.domain.model.CurriculumProblem
import com.codingarena.domain.model.CurriculumProgress
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.RecallRecord
import com.codingarena.domain.model.SectionProgress
import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * One Blitz card: a problem, and four patterns to choose between.
 *
 * The whole question is "which pattern does this want?" - there is no code to
 * read and no answer to compute, because the skill being drilled is
 * recognition speed, not solving.
 */
data class BlitzCard(
    val problem: CurriculumProblem,
    val options: List<PatternGroup>,
    val correct: PatternGroup,
) {
    fun isCorrect(choice: PatternGroup): Boolean = choice == correct

    /**
     * What to show after [choice].
     *
     * A drill that only says "wrong" teaches nothing, so a miss returns the
     * distinction between the two patterns rather than just the answer.
     */
    fun feedbackFor(choice: PatternGroup): String =
        if (isCorrect(choice)) {
            "${correct.displayName} - ${problem.ask}"
        } else {
            PatternConfusions.explain(correct, choice)
        }
}

/** A finished or in-progress Blitz run. */
data class BlitzSession(
    val curriculumId: String,
    val mode: BlitzMode,
    val startedAt: Long,
    val answered: List<BlitzAnswer> = emptyList(),
    val endedAt: Long? = null,
) {
    val score: Int get() = answered.count { it.wasCorrect }
    val streak: Int get() = answered.takeLastWhile { it.wasCorrect }.size
    val bestStreak: Int
        get() {
            var best = 0
            var run = 0
            answered.forEach { if (it.wasCorrect) { run++; best = maxOf(best, run) } else run = 0 }
            return best
        }
    val accuracy: Double
        get() = if (answered.isEmpty()) 0.0 else score.toDouble() / answered.size
    val isOver: Boolean get() = endedAt != null

    val averageMs: Long
        get() = if (answered.isEmpty()) 0L else answered.sumOf { it.elapsedMs } / answered.size
}

@Serializable
data class BlitzAnswer(
    val slug: String,
    val group: PatternGroup,
    val chosen: PatternGroup,
    val wasCorrect: Boolean,
    val elapsedMs: Long,
)

/**
 * Storable form of a finished run.
 *
 * [BlitzSession] itself holds a sealed [BlitzMode], which would need
 * polymorphic serialisation for no benefit - the stored form only ever needs
 * to name the mode, so it keeps a plain key.
 */
@Serializable
data class BlitzSessionSnapshot(
    val curriculumId: String,
    val modeKey: String,
    val startedAt: Long,
    val endedAt: Long?,
    val answers: List<BlitzAnswer>,
)

fun BlitzSession.toSnapshot(): BlitzSessionSnapshot = BlitzSessionSnapshot(
    curriculumId = curriculumId,
    modeKey = mode.storageKey,
    startedAt = startedAt,
    endedAt = endedAt,
    answers = answered,
)

/** How a Blitz run picks its cards. */
sealed interface BlitzMode {
    val displayName: String

    /** Everything in the curriculum, weakest recall first. */
    data object FullList : BlitzMode {
        override val displayName get() = "Full list"
    }

    /** Only one pattern group - for drilling a section you keep missing. */
    data class Section(val group: PatternGroup) : BlitzMode {
        override val displayName get() = "${group.displayName} only"
    }

    /** Only cards that are shaky or unseen. */
    data object WeakestFirst : BlitzMode {
        override val displayName get() = "Weak spots"
    }

    /** Only the Blind 75 subset. */
    data object Blind75 : BlitzMode {
        override val displayName get() = "Essential Shortlist"
    }

    /** Only cards whose spaced-repetition review has come round. */
    data object DueToday : BlitzMode {
        override val displayName get() = "Due today"
    }

    companion object {
        val standard: List<BlitzMode> = listOf(DueToday, WeakestFirst, FullList, Blind75)

        /** Parses a [storageKey] back into a mode. */
        fun fromKey(key: String): BlitzMode = when {
            key == "full" -> FullList
            key == "weak" -> WeakestFirst
            key == "blind75" -> Blind75
            key == "due" -> DueToday
            key.startsWith("section:") -> PatternGroup.entries
                .firstOrNull { it.name == key.removePrefix("section:") }
                ?.let { Section(it) }
                ?: FullList
            else -> FullList
        }
    }
}

/** Stable identifier used in storage and navigation routes. */
val BlitzMode.storageKey: String
    get() = when (this) {
        BlitzMode.FullList -> "full"
        BlitzMode.WeakestFirst -> "weak"
        BlitzMode.Blind75 -> "blind75"
        BlitzMode.DueToday -> "due"
        is BlitzMode.Section -> "section:${group.name}"
    }

/**
 * Blitz: rapid pattern recall over a curated list.
 *
 * This is the memorisation drill, and it is deliberately separate from Code
 * Rush. Code Rush is mixed challenge types under a clock with lives; Blitz is
 * flashcards over a specific roadmap, and it never ends in failure - a wrong
 * answer just pushes that card back to the front of the queue.
 *
 * Pure: the caller owns the clock and the stored [RecallRecord]s.
 */
class BlitzEngine(private val config: BlitzConfig = BlitzConfig()) {

    /**
     * Builds a card for [problem].
     *
     * Distractors come from [PatternGroup.confusableWith] rather than at
     * random. Offering "Trie" against a Sliding Window problem teaches nothing;
     * offering "Two Pointers" is the mistake actually worth drilling.
     */
    fun cardFor(problem: CurriculumProblem, random: Random = Random.Default): BlitzCard {
        val distractors = problem.group.confusableWith
            .filter { it != problem.group }
            .distinct()
            .take(config.optionCount - 1)

        val padded = if (distractors.size < config.optionCount - 1) {
            // Small safety net if a group ever declares too few confusables.
            (distractors + PatternGroup.entries.filter {
                it != problem.group && it !in distractors
            }).take(config.optionCount - 1)
        } else {
            distractors
        }

        return BlitzCard(
            problem = problem,
            options = (padded + problem.group).shuffled(random),
            correct = problem.group,
        )
    }

    /**
     * Orders the whole curriculum into a drill queue.
     *
     * Weakest recall first, and within equal strength the least recently seen,
     * so a run always opens on the cards most in need of work rather than on
     * whatever happens to be first alphabetically.
     */
    fun buildQueue(
        curriculum: Curriculum,
        records: Map<String, RecallRecord>,
        mode: BlitzMode,
        limit: Int = config.defaultQueueLength,
        now: Long = 0L,
    ): List<CurriculumProblem> {
        val pool = when (mode) {
            BlitzMode.FullList -> curriculum.problems
            BlitzMode.Blind75 -> curriculum.problems.filter { it.inBlind75 }
            is BlitzMode.Section -> curriculum.problems.filter { it.group == mode.group }
            BlitzMode.WeakestFirst -> curriculum.problems.filter {
                records[it.slug]?.isMastered != true
            }
            BlitzMode.DueToday -> dueCards(curriculum, records, now)
        }

        return pool
            .sortedWith(
                compareBy(
                    { records[it.slug]?.strength?.ordinal ?: 0 },
                    { records[it.slug]?.lastSeenAt ?: 0L },
                    { it.slug },
                )
            )
            .take(limit)
    }

    /**
     * Applies an answer to the stored recall record for that card.
     *
     * Also schedules the next review on the 1/3/7/14/30-day ladder, so cards
     * come back over weeks rather than only within a session. A mastered card
     * still returns in a month - recall decays, and "locked in" that is never
     * re-tested is just a claim.
     */
    fun recordAnswer(
        existing: RecallRecord?,
        card: BlitzCard,
        chosen: PatternGroup,
        elapsedMs: Long,
        now: Long,
    ): RecallRecord {
        val correct = card.isCorrect(chosen)
        val record = existing ?: RecallRecord(slug = card.problem.slug)

        val updated = record.copy(
            // A miss resets the streak completely. Memorisation means getting
            // it right repeatedly, not on average.
            correctStreak = if (correct) record.correctStreak + 1 else 0,
            totalSeen = record.totalSeen + 1,
            totalCorrect = record.totalCorrect + if (correct) 1 else 0,
            lastSeenAt = now,
            fastestMs = if (correct) {
                minOf(record.fastestMs ?: Long.MAX_VALUE, elapsedMs)
            } else {
                record.fastestMs
            },
        )

        return updated.copy(
            dueAt = now + updated.nextInterval.intervalDays * MILLIS_PER_DAY,
        )
    }

    /**
     * Records that the user actually solved the problem on LeetCode.
     *
     * Kept separate from recall: knowing a problem is Sliding Window and having
     * written the solution are different achievements, and for someone working
     * the CodingArena list the second is the one that counts.
     */
    fun markSolved(
        existing: RecallRecord?,
        slug: String,
        solved: Boolean,
        now: Long,
    ): RecallRecord {
        val record = existing ?: RecallRecord(slug = slug)
        return record.copy(
            solved = solved,
            solvedAt = if (solved) now else null,
        )
    }

    /** Cards whose review has come round again. */
    fun dueCards(
        curriculum: Curriculum,
        records: Map<String, RecallRecord>,
        now: Long,
    ): List<CurriculumProblem> = curriculum.problems.filter { problem ->
        val record = records[problem.slug]
        record != null && record.isDue(now)
    }

    fun start(curriculumId: String, mode: BlitzMode, now: Long): BlitzSession =
        BlitzSession(curriculumId = curriculumId, mode = mode, startedAt = now)

    fun submit(
        session: BlitzSession,
        card: BlitzCard,
        chosen: PatternGroup,
        elapsedMs: Long,
    ): BlitzSession {
        if (session.isOver) return session
        return session.copy(
            answered = session.answered + BlitzAnswer(
                slug = card.problem.slug,
                group = card.correct,
                chosen = chosen,
                wasCorrect = card.isCorrect(chosen),
                elapsedMs = elapsedMs,
            )
        )
    }

    fun finish(session: BlitzSession, now: Long): BlitzSession =
        if (session.isOver) session else session.copy(endedAt = now)

    /** Roadmap progress across a curriculum. */
    fun progress(
        curriculum: Curriculum,
        records: Map<String, RecallRecord>,
        now: Long = 0L,
    ): CurriculumProgress {
        val sections = curriculum.sections.associate { section ->
            section.group to SectionProgress(
                group = section.group,
                total = section.problems.size,
                seen = section.problems.count { (records[it.slug]?.totalSeen ?: 0) > 0 },
                mastered = section.problems.count { records[it.slug]?.isMastered == true },
                solved = section.problems.count { records[it.slug]?.solved == true },
            )
        }

        return CurriculumProgress(
            curriculumId = curriculum.id,
            total = curriculum.problems.size,
            seen = curriculum.problems.count { (records[it.slug]?.totalSeen ?: 0) > 0 },
            mastered = curriculum.problems.count { records[it.slug]?.isMastered == true },
            due = dueCards(curriculum, records, now).size,
            solved = curriculum.problems.count { records[it.slug]?.solved == true },
            sectionProgress = sections,
        )
    }

    /**
     * The groups a user confuses most often, as ordered pairs.
     *
     * This is the genuinely diagnostic output: "you call Sliding Window
     * problems Two Pointers" is far more actionable than an accuracy figure.
     */
    fun confusions(sessions: List<BlitzSession>, limit: Int = 5): List<Confusion> =
        sessions
            .flatMap { it.answered }
            .filterNot { it.wasCorrect }
            .groupingBy { it.group to it.chosen }
            .eachCount()
            .map { (pair, count) -> Confusion(pair.first, pair.second, count) }
            .sortedWith(compareByDescending<Confusion> { it.count }.thenBy { it.actual.ordinal })
            .take(limit)
}

/** "[actual] problems get called [mistakenFor]", [count] times. */
data class Confusion(
    val actual: PatternGroup,
    val mistakenFor: PatternGroup,
    val count: Int,
) {
    val description: String
        get() = "You called ${actual.displayName} problems ${mistakenFor.displayName} $count " +
            if (count == 1) "time" else "times"
}

data class BlitzConfig(
    val optionCount: Int = 4,
    val defaultQueueLength: Int = 20,
)
