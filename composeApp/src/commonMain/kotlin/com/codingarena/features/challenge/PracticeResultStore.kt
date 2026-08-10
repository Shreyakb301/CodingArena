package com.codingarena.features.challenge

import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.usecase.PracticeResult

/**
 * Hands a finished attempt from the challenge screen to the review screen.
 *
 * The review is fully derived from the submission, so re-deriving it after a
 * navigation would mean re-running the rating engine and double-counting the
 * attempt. Passing the computed result across in memory avoids that entirely;
 * nothing here needs to survive process death, because a lost review just
 * returns the user to a home screen that already reflects the answer.
 */
class PracticeResultStore {

    private val results = mutableMapOf<String, Entry>()

    data class Entry(val result: PracticeResult, val problem: CodingProblem)

    fun put(result: PracticeResult, problem: CodingProblem) {
        // Only the most recent handful matter; this is a hand-off, not a cache.
        if (results.size >= MAX_ENTRIES) {
            results.remove(results.keys.first())
        }
        results[result.attempt.id] = Entry(result, problem)
    }

    fun get(attemptId: String): Entry? = results[attemptId]

    fun clear() = results.clear()

    private companion object {
        const val MAX_ENTRIES = 8
    }
}
