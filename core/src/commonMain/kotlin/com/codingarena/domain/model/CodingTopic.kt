package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * The skill dimensions CodingArena rates independently.
 *
 * Every problem names one [CodingProblem.primaryTopic] and any number of
 * secondary topics; the rating engine moves the primary topic further than the
 * secondaries so that a graph problem tagged "recursion" does not quietly
 * inflate a user's recursion rating.
 */
@Serializable
enum class CodingTopic(val displayName: String, val shortName: String) {
    ARRAYS("Arrays", "Arr"),
    STRINGS("Strings", "Str"),
    HASH_MAPS("Hash Maps", "Hash"),
    LINKED_LISTS("Linked Lists", "List"),
    STACKS("Stacks", "Stack"),
    QUEUES("Queues", "Queue"),
    TREES("Trees", "Tree"),
    GRAPHS("Graphs", "Graph"),
    RECURSION("Recursion", "Rec"),
    BACKTRACKING("Backtracking", "Back"),
    BINARY_SEARCH("Binary Search", "BSrch"),
    SLIDING_WINDOW("Sliding Window", "Win"),
    TWO_POINTERS("Two Pointers", "2Ptr"),
    GREEDY("Greedy", "Greedy"),
    DYNAMIC_PROGRAMMING("Dynamic Programming", "DP"),
    HEAPS("Heaps", "Heap"),
    TRIES("Tries", "Trie"),
    BIT_MANIPULATION("Bit Manipulation", "Bits"),
    SORTING("Sorting", "Sort"),
    COMPLEXITY("Complexity Analysis", "O(n)"),
    DEBUGGING("Debugging", "Debug"),
    ;

    companion object {
        /**
         * Topics offered during onboarding. [COMPLEXITY] and [DEBUGGING] are
         * rated like any other topic but read as skills rather than subjects,
         * so they are surfaced separately in the UI.
         */
        val subjectTopics: List<CodingTopic> = entries - setOf(COMPLEXITY, DEBUGGING)

        fun fromId(id: String): CodingTopic? = entries.firstOrNull { it.name == id }
    }
}
