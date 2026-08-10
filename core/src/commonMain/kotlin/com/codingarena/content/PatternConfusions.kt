package com.codingarena.content

import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.PatternGroup.ADVANCED_GRAPHS
import com.codingarena.domain.model.PatternGroup.ARRAYS_HASHING
import com.codingarena.domain.model.PatternGroup.BACKTRACKING
import com.codingarena.domain.model.PatternGroup.BINARY_SEARCH
import com.codingarena.domain.model.PatternGroup.BIT_MANIPULATION
import com.codingarena.domain.model.PatternGroup.DP_1D
import com.codingarena.domain.model.PatternGroup.DP_2D
import com.codingarena.domain.model.PatternGroup.GRAPHS
import com.codingarena.domain.model.PatternGroup.GREEDY
import com.codingarena.domain.model.PatternGroup.HEAP
import com.codingarena.domain.model.PatternGroup.INTERVALS
import com.codingarena.domain.model.PatternGroup.LINKED_LIST
import com.codingarena.domain.model.PatternGroup.MATH_GEOMETRY
import com.codingarena.domain.model.PatternGroup.SLIDING_WINDOW
import com.codingarena.domain.model.PatternGroup.STACK
import com.codingarena.domain.model.PatternGroup.TREES
import com.codingarena.domain.model.PatternGroup.TRIES
import com.codingarena.domain.model.PatternGroup.TWO_POINTERS
import kotlinx.serialization.Serializable

/**
 * Why one pattern is not the other.
 *
 * [tellFirst] and [tellSecond] are the recognition cues - the thing you should
 * be able to spot in a problem statement in two seconds.
 */
@Serializable
data class PatternConfusion(
    val first: PatternGroup,
    val second: PatternGroup,
    val distinction: String,
    val tellFirst: String,
    val tellSecond: String,
) {
    /** The distinction, phrased for someone who answered [mistakenFor]. */
    fun explanationFor(actual: PatternGroup, mistakenFor: PatternGroup): String {
        val actualTell = if (actual == first) tellFirst else tellSecond
        val wrongTell = if (mistakenFor == first) tellFirst else tellSecond
        return "$distinction\n\n" +
            "${actual.displayName}: $actualTell\n" +
            "${mistakenFor.displayName}: $wrongTell"
    }
}

/**
 * The teaching layer behind Blitz.
 *
 * Written per *pair* rather than per problem. Thirty-eight explanations cover
 * every wrong answer a user can give across all 150 problems, and each one is
 * the distinction actually worth memorising - far better value than 150
 * separate paragraphs, and it doubles as a revision sheet.
 */
object PatternConfusions {

    private fun c(
        first: PatternGroup,
        second: PatternGroup,
        distinction: String,
        tellFirst: String,
        tellSecond: String,
    ) = PatternConfusion(first, second, distinction, tellFirst, tellSecond)

    val all: List<PatternConfusion> = listOf(

        c(
            SLIDING_WINDOW, TWO_POINTERS,
            "Both walk two indices, but for different reasons. A window keeps a " +
                "contiguous run and its width changes to satisfy a constraint. Two pointers " +
                "converge from the ends of ordered data, discarding a whole side each step.",
            "Contiguous subarray or substring, with a constraint that tightens and loosens.",
            "Sorted input, converging from both ends, looking for a pair or a partition.",
        ),
        c(
            SLIDING_WINDOW, ARRAYS_HASHING,
            "A frequency map answers \"have I seen this?\" anywhere in the input. A window " +
                "asks the same question but only about the elements currently inside it - the " +
                "map is a tool the window uses, not the pattern itself.",
            "The answer is a contiguous run, and elements leave the picture as it moves.",
            "Position does not matter; you only need counts or membership over the whole input.",
        ),
        c(
            SLIDING_WINDOW, DP_1D,
            "Both build an answer left to right. A window's state depends only on the " +
                "elements inside it; DP's state depends on decisions made about elements " +
                "already passed.",
            "The answer is one contiguous stretch, and old elements are truly discarded.",
            "You need the best answer *ending at* each index, carrying earlier choices forward.",
        ),
        c(
            TWO_POINTERS, ARRAYS_HASHING,
            "Both find pairs. Two pointers needs order and gives O(1) space; a hash map " +
                "needs no order and costs O(n) space. The constraint in the question picks one.",
            "The input is sorted, or constant extra space is demanded.",
            "The input is unsorted and you can afford memory.",
        ),
        c(
            TWO_POINTERS, BINARY_SEARCH,
            "Both exploit sorted input. Two pointers walks inward linearly; binary search " +
                "jumps to the midpoint and halves the range.",
            "You are comparing two elements against each other, or partitioning.",
            "You are locating a single value or boundary against a fixed predicate.",
        ),
        c(
            TWO_POINTERS, LINKED_LIST,
            "Fast and slow pointers are two pointers, but on a list you cannot index or " +
                "walk backwards, so the technique becomes about relative speed rather than " +
                "converging ends.",
            "Random access is available - an array or string.",
            "You can only follow next pointers; cycle detection or finding the middle.",
        ),
        c(
            TWO_POINTERS, MATH_GEOMETRY,
            "Matrix problems often move two indices, but they are traversal bookkeeping " +
                "rather than a converging search over ordered data.",
            "Ordered input where each comparison eliminates a range of candidates.",
            "Grid traversal, rotation or coordinate arithmetic with no ordering to exploit.",
        ),
        c(
            ARRAYS_HASHING, HEAP,
            "A hash map counts; a heap orders. Top-k problems usually need both - the map " +
                "to count, the heap to rank - and the pattern is named for whichever does the " +
                "harder work.",
            "The answer is counting, grouping or membership, with no ranking involved.",
            "You repeatedly need the largest or smallest of a changing collection.",
        ),
        c(
            ARRAYS_HASHING, STACK,
            "Both can scan once. A hash map remembers values regardless of position; a " +
                "stack remembers elements still waiting for something that has not arrived yet.",
            "\"Have I seen this?\" or \"how many times?\"",
            "\"What is the next greater / matching / closing thing?\" - order matters.",
        ),
        c(
            ARRAYS_HASHING, BINARY_SEARCH,
            "A hash map finds a known value in O(1). Binary search finds a *boundary* - " +
                "the first or last position where something becomes true.",
            "You know exactly what you are looking for.",
            "You are looking for a threshold, or the input is sorted and space is constrained.",
        ),
        c(
            ARRAYS_HASHING, LINKED_LIST,
            "Both may use a map, but list problems turn on pointer surgery - the map is " +
                "usually a helper for node lookup, not the shape of the solution.",
            "The data is an array or string with random access.",
            "You are relinking nodes, or the input is explicitly a linked list.",
        ),
        c(
            ARRAYS_HASHING, TRIES,
            "A hash set answers exact membership. A trie answers prefix questions, which a " +
                "hash cannot do at all without scanning everything.",
            "\"Is this exact word present?\"",
            "\"Which words start with this?\" - autocomplete, prefix search, wildcards.",
        ),
        c(
            ARRAYS_HASHING, INTERVALS,
            "Interval problems look like array problems until you notice the elements are " +
                "ranges, and that sorting them by an endpoint is what unlocks the answer.",
            "Elements are independent values.",
            "Elements are [start, end] pairs that can overlap or merge.",
        ),
        c(
            ARRAYS_HASHING, MATH_GEOMETRY,
            "Both operate on arrays. Math and geometry problems turn on the numeric or " +
                "spatial structure - digits, coordinates, matrix layout - rather than on " +
                "lookup.",
            "Counting, grouping or membership.",
            "Rotation, spirals, digit manipulation or coordinate arithmetic.",
        ),
        c(
            ARRAYS_HASHING, BIT_MANIPULATION,
            "Both handle duplicates. A map is general; XOR is the trick that solves the " +
                "specific case of pairs cancelling, in constant space.",
            "Counts matter, or elements appear an arbitrary number of times.",
            "Everything pairs up except one, and constant space is demanded.",
        ),
        c(
            STACK, LINKED_LIST,
            "A stack is a discipline; a linked list is a structure. Reversing a list *with* " +
                "a stack is possible, but the pattern being tested is usually the pointer work.",
            "LIFO matters - matching, nesting, or pending work.",
            "You are traversing or relinking nodes you can only reach in one direction.",
        ),
        c(
            STACK, GREEDY,
            "A monotonic stack often looks greedy because it discards candidates " +
                "permanently. The difference is that the stack keeps the discarded work " +
                "*pending* until it can be resolved.",
            "Elements wait for a later element to answer them.",
            "Each choice is final the moment it is made.",
        ),
        c(
            BINARY_SEARCH, GREEDY,
            "\"Binary search the answer\" pairs a greedy feasibility check with a search " +
                "over candidate answers. If you are only making local choices, it is greedy; " +
                "if you are testing candidate answers, it is binary search.",
            "You can cheaply test \"is X achievable?\" and X is monotonic.",
            "You walk the input once making a locally optimal choice each step.",
        ),
        c(
            BINARY_SEARCH, HEAP,
            "Both find k-th elements. A heap streams and is incremental; binary search " +
                "needs the whole ordered space up front but uses no extra memory.",
            "The search space is ordered and static.",
            "Elements arrive over time, or you need repeated extreme lookups.",
        ),
        c(
            TREES, GRAPHS,
            "A tree is a graph with no cycles and one path between any two nodes. That is " +
                "exactly why trees need no visited set and graphs always do.",
            "Parent-child structure, guaranteed acyclic.",
            "Arbitrary connections, cycles possible - forgetting the visited set loops forever.",
        ),
        c(
            TREES, BACKTRACKING,
            "Tree recursion combines answers coming back up. Backtracking builds a " +
                "candidate going down and must undo it on the way back.",
            "Each node's answer is computed from its children's answers.",
            "You are assembling a path or selection, and must unmake choices to try others.",
        ),
        c(
            TREES, HEAP,
            "A heap is a tree in memory, but heap problems are about priority, not " +
                "structure. Nothing about the shape of the heap is ever the question.",
            "The tree's structure is the problem - depth, paths, traversal, validity.",
            "You need the extreme element repeatedly and do not care how it is stored.",
        ),
        c(
            TREES, TRIES,
            "A trie is a tree keyed by characters. If the question is about words and " +
                "prefixes it is a trie; if it is about node relationships it is a tree.",
            "Depth, balance, traversal order, ancestors.",
            "Strings sharing prefixes - insert, search, autocomplete.",
        ),
        c(
            TRIES, BACKTRACKING,
            "Word Search II is both: a trie prunes the search, backtracking does the " +
                "searching. Name it for the structure that makes it tractable.",
            "The dictionary itself is the hard part - many words, prefix queries.",
            "Exploring a space of candidates with undo, over a small word set.",
        ),
        c(
            HEAP, GREEDY,
            "Greedy with a changing collection usually needs a heap to find the next " +
                "choice. The heap is the tool; greedy is the strategy.",
            "The collection changes and you repeatedly need its extreme.",
            "One sort up front is enough, and choices are final.",
        ),
        c(
            HEAP, INTERVALS,
            "Meeting Rooms II is the crossover: intervals sorted by start, a min-heap of " +
                "end times. Intervals name the input; the heap answers \"how many at once?\".",
            "You need a running count of overlapping things.",
            "You need to merge, insert or remove ranges.",
        ),
        c(
            BACKTRACKING, DP_1D,
            "Both explore choices. DP applies when the same subproblem recurs and can be " +
                "cached; backtracking applies when every branch is distinct and must be listed.",
            "You must *enumerate* every valid configuration.",
            "You need one optimal value, and subproblems repeat heavily.",
        ),
        c(
            BACKTRACKING, GRAPHS,
            "DFS on a graph visits each node once. Backtracking may revisit a node on a " +
                "different path, which is why it unmarks as it returns.",
            "Paths are the answer, and a node can appear on many of them.",
            "Reachability or connectivity - each node needs visiting only once.",
        ),
        c(
            BACKTRACKING, DP_2D,
            "Grid DP fills a table once. Grid backtracking walks paths and undoes them. " +
                "If a cell's answer is fixed regardless of route, it is DP.",
            "The route taken changes what is valid - Word Search, N-Queens.",
            "Each cell has one answer built from its neighbours - Unique Paths, Edit Distance.",
        ),
        c(
            GRAPHS, ADVANCED_GRAPHS,
            "Plain graph problems need only reachability, so BFS or DFS suffices. Advanced " +
                "ones add weights or ordering, which needs Dijkstra, union-find or a topological " +
                "sort.",
            "Unweighted edges - islands, connectivity, shortest hop count.",
            "Weighted edges, minimum spanning trees, or dependency ordering.",
        ),
        c(
            GRAPHS, DP_2D,
            "A grid is both a graph and a DP table. Traversal problems flood outward; DP " +
                "problems build a value per cell from its neighbours.",
            "You are spreading, flooding or counting regions.",
            "Each cell holds a computed optimum derived from adjacent cells.",
        ),
        c(
            ADVANCED_GRAPHS, HEAP,
            "Dijkstra *is* a heap-driven graph traversal. Name it for the graph if the " +
                "structure is the challenge, for the heap if the ordering is.",
            "Weighted shortest paths or minimum spanning trees.",
            "Priority selection with no graph involved.",
        ),
        c(
            ADVANCED_GRAPHS, GREEDY,
            "Prim's and Kruskal's are greedy algorithms on graphs. The greedy choice is " +
                "provably safe; the graph is what makes it non-trivial.",
            "Nodes and edges, with weights or dependencies.",
            "A linear sequence where each local choice is final.",
        ),
        c(
            DP_1D, DP_2D,
            "Count the things that vary. One changing quantity means a 1-D table; two " +
                "independent ones - two strings, or an index plus a budget - means 2-D.",
            "State is a single index or amount.",
            "State is a pair: two sequences, or position plus remaining capacity.",
        ),
        c(
            DP_1D, GREEDY,
            "Greedy commits to a local choice; DP keeps every option until it knows which " +
                "wins. Greedy needs an exchange argument - without one, use DP.",
            "A choice now constrains later choices in ways you cannot see yet.",
            "The locally best choice is provably part of some optimal solution.",
        ),
        c(
            GREEDY, INTERVALS,
            "Most interval problems are solved greedily after a sort. Intervals describe " +
                "the input; greedy describes the method - the exam question is which key to " +
                "sort by.",
            "A sequence of values where each choice is final.",
            "The elements are ranges, and sorting by start or end unlocks it.",
        ),
        c(
            MATH_GEOMETRY, BIT_MANIPULATION,
            "Both are number tricks. Bit problems manipulate the binary representation " +
                "directly; math problems work with the value.",
            "Digits, coordinates, overflow, matrix layout.",
            "Shifts, masks, XOR - the binary representation is the point.",
        ),
        c(
            BIT_MANIPULATION, DP_1D,
            "Counting Bits is both: a bit identity, memoised. If the recurrence is the " +
                "insight it is DP; if the bit trick is, it is bit manipulation.",
            "A single expression like x & (x - 1) solves it.",
            "You build results for 0..n from earlier answers.",
        ),
    )

    private val index: Map<Set<PatternGroup>, PatternConfusion> =
        all.associateBy { setOf(it.first, it.second) }

    /** The distinction between two groups, in either order. */
    fun between(a: PatternGroup, b: PatternGroup): PatternConfusion? =
        if (a == b) null else index[setOf(a, b)]

    /**
     * The explanation to show after a wrong Blitz answer.
     *
     * Falls back to the two groups' own descriptions if no pair is authored,
     * so the UI never has to handle a null.
     */
    fun explain(actual: PatternGroup, mistakenFor: PatternGroup): String =
        between(actual, mistakenFor)?.explanationFor(actual, mistakenFor)
            ?: "This is a ${actual.displayName} problem, not ${mistakenFor.displayName}."

    /** Every distinction involving [group], for the revision sheet. */
    fun involving(group: PatternGroup): List<PatternConfusion> =
        all.filter { it.first == group || it.second == group }
}
