package com.codingarena.content

import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic

/**
 * The content that ships inside the app.
 *
 * Bundling the starter set is what lets the first session work with no network
 * and no backend cost at all (spec 13). Downloaded problems are merged on top
 * of this set once a backend exists.
 */
object StarterContent {

    val problems: List<CodingProblem> =
        arraysAndStringsProblems +
            dataStructureProblems +
            algorithmAndDebuggingProblems +
            advancedPatternProblems

    private val byId: Map<String, CodingProblem> = problems.associateBy { it.id }

    fun byId(id: String): CodingProblem? = byId[id]

    fun byTopic(topic: CodingTopic): List<CodingProblem> =
        problems.filter { topic in it.allTopics }

    /**
     * A stable Daily Puzzle for a given day.
     *
     * Deterministic in [epochDay] so that the same day always yields the same
     * problem, on any device, offline, with no server round trip.
     */
    fun dailyPuzzleFor(epochDay: Long): CodingProblem {
        val ordered = problems.sortedBy { it.id }
        val index = ((epochDay % ordered.size) + ordered.size) % ordered.size
        return ordered[index.toInt()]
    }

    /** The 20 starter categories from spec 12, for onboarding and coverage checks. */
    val starterCategories: List<String> = listOf(
        "Array traversal",
        "Hash map lookup",
        "Duplicate detection",
        "Two pointers",
        "Fixed sliding window",
        "Variable sliding window",
        "Stack behaviour",
        "Queue behaviour",
        "Binary search",
        "Tree DFS",
        "Tree BFS",
        "Linked list traversal",
        "Recursion",
        "Sorting",
        "Time complexity",
        "Space complexity",
        "Debugging loops",
        "Debugging indexes",
        "Edge cases",
        "Output prediction",
    )
}
