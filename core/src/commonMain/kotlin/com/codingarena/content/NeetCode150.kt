package com.codingarena.content

import com.codingarena.domain.model.Curriculum
import com.codingarena.domain.model.CurriculumDifficulty
import com.codingarena.domain.model.CurriculumDifficulty.EASY
import com.codingarena.domain.model.CurriculumDifficulty.HARD
import com.codingarena.domain.model.CurriculumDifficulty.MEDIUM
import com.codingarena.domain.model.CurriculumProblem
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

/**
 * The NeetCode 150 roadmap, with Blind 75 membership flagged.
 *
 * **What is and is not here.** Problem titles, slugs, difficulties and pattern
 * groupings are factual metadata about a widely published curated list. The
 * original problem *statements* are not reproduced - they are copyrighted, and
 * they are not what this app drills anyway. Each entry carries [ask], a
 * one-line paraphrase written for this app that is just enough to trigger
 * pattern recall, plus a link out to solve the real thing.
 *
 * The grouping is the point: the skill being trained is looking at a problem
 * and knowing *which pattern it wants* before writing a line.
 */
object NeetCode150 {

    private fun p(
        slug: String,
        title: String,
        difficulty: CurriculumDifficulty,
        group: PatternGroup,
        ask: String,
        blind75: Boolean = false,
    ) = CurriculumProblem(slug, title, difficulty, group, ask, blind75)

    val problems: List<CurriculumProblem> = listOf(

        // ------------------------------------------------ Arrays & Hashing (9)
        p("contains-duplicate", "Contains Duplicate", EASY, ARRAYS_HASHING,
            "Does any value appear more than once?", blind75 = true),
        p("valid-anagram", "Valid Anagram", EASY, ARRAYS_HASHING,
            "Do two strings use exactly the same letters?", blind75 = true),
        p("two-sum", "Two Sum", EASY, ARRAYS_HASHING,
            "Find two indices whose values sum to a target.", blind75 = true),
        p("group-anagrams", "Group Anagrams", MEDIUM, ARRAYS_HASHING,
            "Bucket words that are anagrams of each other.", blind75 = true),
        p("top-k-frequent-elements", "Top K Frequent Elements", MEDIUM, ARRAYS_HASHING,
            "Return the k most common values.", blind75 = true),
        p("encode-and-decode-strings", "Encode and Decode Strings", MEDIUM, ARRAYS_HASHING,
            "Serialise a list of strings so it can be split back apart unambiguously.",
            blind75 = true),
        p("product-of-array-except-self", "Product of Array Except Self", MEDIUM, ARRAYS_HASHING,
            "Product of everything but each element, without division.", blind75 = true),
        p("valid-sudoku", "Valid Sudoku", MEDIUM, ARRAYS_HASHING,
            "Check rows, columns and boxes for repeats."),
        p("longest-consecutive-sequence", "Longest Consecutive Sequence", MEDIUM, ARRAYS_HASHING,
            "Longest run of consecutive integers, in any order.", blind75 = true),

        // --------------------------------------------------- Two Pointers (5)
        p("valid-palindrome", "Valid Palindrome", EASY, TWO_POINTERS,
            "Reads the same both ways, ignoring non-letters.", blind75 = true),
        p("two-sum-ii-input-array-is-sorted", "Two Sum II", MEDIUM, TWO_POINTERS,
            "Pair summing to a target, in a sorted array, constant space."),
        p("3sum", "3Sum", MEDIUM, TWO_POINTERS,
            "All unique triples summing to zero.", blind75 = true),
        p("container-with-most-water", "Container With Most Water", MEDIUM, TWO_POINTERS,
            "Two lines holding the most water between them.", blind75 = true),
        p("trapping-rain-water", "Trapping Rain Water", HARD, TWO_POINTERS,
            "Water held by an elevation map."),

        // ------------------------------------------------- Sliding Window (6)
        p("best-time-to-buy-and-sell-stock", "Best Time to Buy and Sell Stock", EASY,
            SLIDING_WINDOW, "Largest profit from one buy and one later sell.", blind75 = true),
        p("longest-substring-without-repeating-characters",
            "Longest Substring Without Repeating Characters", MEDIUM, SLIDING_WINDOW,
            "Longest stretch with no repeated character.", blind75 = true),
        p("longest-repeating-character-replacement", "Longest Repeating Character Replacement",
            MEDIUM, SLIDING_WINDOW, "Longest same-letter run allowing k replacements.",
            blind75 = true),
        p("permutation-in-string", "Permutation in String", MEDIUM, SLIDING_WINDOW,
            "Does one string contain a permutation of another?"),
        p("minimum-window-substring", "Minimum Window Substring", HARD, SLIDING_WINDOW,
            "Shortest window containing every required character.", blind75 = true),
        p("sliding-window-maximum", "Sliding Window Maximum", HARD, SLIDING_WINDOW,
            "Maximum of every k-wide window."),

        // ---------------------------------------------------------- Stack (7)
        p("valid-parentheses", "Valid Parentheses", EASY, STACK,
            "Are the brackets balanced and correctly nested?", blind75 = true),
        p("min-stack", "Min Stack", MEDIUM, STACK,
            "Stack that also reports its minimum in constant time."),
        p("evaluate-reverse-polish-notation", "Evaluate Reverse Polish Notation", MEDIUM, STACK,
            "Evaluate postfix arithmetic."),
        p("generate-parentheses", "Generate Parentheses", MEDIUM, STACK,
            "All valid bracket arrangements of n pairs."),
        p("daily-temperatures", "Daily Temperatures", MEDIUM, STACK,
            "Days until a warmer temperature, for each day."),
        p("car-fleet", "Car Fleet", MEDIUM, STACK,
            "How many groups of cars arrive together."),
        p("largest-rectangle-in-histogram", "Largest Rectangle in Histogram", HARD, STACK,
            "Biggest rectangle fitting under a bar chart."),

        // -------------------------------------------------- Binary Search (7)
        p("binary-search", "Binary Search", EASY, BINARY_SEARCH,
            "Locate a value in a sorted array."),
        p("search-a-2d-matrix", "Search a 2D Matrix", MEDIUM, BINARY_SEARCH,
            "Find a value in a row-and-column sorted grid."),
        p("koko-eating-bananas", "Koko Eating Bananas", MEDIUM, BINARY_SEARCH,
            "Smallest rate that finishes in time - search the answer, not the array."),
        p("find-minimum-in-rotated-sorted-array", "Find Minimum in Rotated Sorted Array", MEDIUM,
            BINARY_SEARCH, "Smallest value in a rotated sorted array.", blind75 = true),
        p("search-in-rotated-sorted-array", "Search in Rotated Sorted Array", MEDIUM,
            BINARY_SEARCH, "Find a value in a rotated sorted array.", blind75 = true),
        p("time-based-key-value-store", "Time Based Key-Value Store", MEDIUM, BINARY_SEARCH,
            "Retrieve the value stored at or before a timestamp."),
        p("median-of-two-sorted-arrays", "Median of Two Sorted Arrays", HARD, BINARY_SEARCH,
            "Median across two sorted arrays in log time."),

        // --------------------------------------------------- Linked List (11)
        p("reverse-linked-list", "Reverse Linked List", EASY, LINKED_LIST,
            "Reverse the pointers of a list.", blind75 = true),
        p("merge-two-sorted-lists", "Merge Two Sorted Lists", EASY, LINKED_LIST,
            "Interleave two sorted lists into one.", blind75 = true),
        p("reorder-list", "Reorder List", MEDIUM, LINKED_LIST,
            "Fold the list so it alternates front and back.", blind75 = true),
        p("remove-nth-node-from-end-of-list", "Remove Nth Node From End of List", MEDIUM,
            LINKED_LIST, "Delete the nth node counting backwards, in one pass.", blind75 = true),
        p("copy-list-with-random-pointer", "Copy List with Random Pointer", MEDIUM, LINKED_LIST,
            "Deep copy a list whose nodes also point anywhere."),
        p("add-two-numbers", "Add Two Numbers", MEDIUM, LINKED_LIST,
            "Add two numbers stored as digit lists."),
        p("linked-list-cycle", "Linked List Cycle", EASY, LINKED_LIST,
            "Does the list loop back on itself?", blind75 = true),
        p("find-the-duplicate-number", "Find the Duplicate Number", MEDIUM, LINKED_LIST,
            "One repeated value, read-only input, constant space."),
        p("lru-cache", "LRU Cache", MEDIUM, LINKED_LIST,
            "Fixed-size cache evicting the least recently used entry.", blind75 = true),
        p("merge-k-sorted-lists", "Merge K Sorted Lists", HARD, LINKED_LIST,
            "Merge many sorted lists at once.", blind75 = true),
        p("reverse-nodes-in-k-group", "Reverse Nodes in K-Group", HARD, LINKED_LIST,
            "Reverse the list in fixed-size blocks."),

        // --------------------------------------------------------- Trees (15)
        p("invert-binary-tree", "Invert Binary Tree", EASY, TREES,
            "Mirror a tree left to right.", blind75 = true),
        p("maximum-depth-of-binary-tree", "Maximum Depth of Binary Tree", EASY, TREES,
            "How deep does the tree go?", blind75 = true),
        p("diameter-of-binary-tree", "Diameter of Binary Tree", EASY, TREES,
            "Longest path between any two nodes."),
        p("balanced-binary-tree", "Balanced Binary Tree", EASY, TREES,
            "Do sibling subtree heights ever differ by more than one?"),
        p("same-tree", "Same Tree", EASY, TREES,
            "Are two trees structurally identical?", blind75 = true),
        p("subtree-of-another-tree", "Subtree of Another Tree", EASY, TREES,
            "Does one tree appear inside another?", blind75 = true),
        p("lowest-common-ancestor-of-a-binary-search-tree",
            "Lowest Common Ancestor of a BST", MEDIUM, TREES,
            "Deepest node that is an ancestor of both.", blind75 = true),
        p("binary-tree-level-order-traversal", "Binary Tree Level Order Traversal", MEDIUM, TREES,
            "Print the tree one level at a time.", blind75 = true),
        p("binary-tree-right-side-view", "Binary Tree Right Side View", MEDIUM, TREES,
            "What you would see standing to the right of the tree."),
        p("count-good-nodes-in-binary-tree", "Count Good Nodes in Binary Tree", MEDIUM, TREES,
            "Nodes with no larger ancestor on their path."),
        p("validate-binary-search-tree", "Validate Binary Search Tree", MEDIUM, TREES,
            "Is every node within its allowed range?", blind75 = true),
        p("kth-smallest-element-in-a-bst", "Kth Smallest Element in a BST", MEDIUM, TREES,
            "The kth value in sorted order.", blind75 = true),
        p("construct-binary-tree-from-preorder-and-inorder-traversal",
            "Construct Binary Tree from Preorder and Inorder", MEDIUM, TREES,
            "Rebuild a tree from two traversals.", blind75 = true),
        p("binary-tree-maximum-path-sum", "Binary Tree Maximum Path Sum", HARD, TREES,
            "Best-value path bending through any node.", blind75 = true),
        p("serialize-and-deserialize-binary-tree", "Serialize and Deserialize Binary Tree", HARD,
            TREES, "Turn a tree into a string and back.", blind75 = true),

        // ---------------------------------------------------------- Tries (3)
        p("implement-trie-prefix-tree", "Implement Trie", MEDIUM, TRIES,
            "Insert, search and prefix-search words.", blind75 = true),
        p("design-add-and-search-words-data-structure", "Design Add and Search Words", MEDIUM,
            TRIES, "Word search where a dot matches any letter.", blind75 = true),
        p("word-search-ii", "Word Search II", HARD, TRIES,
            "Find many words in a grid at once.", blind75 = true),

        // ---------------------------------------------- Heap / Priority Queue (7)
        p("kth-largest-element-in-a-stream", "Kth Largest Element in a Stream", EASY, HEAP,
            "Report the kth largest as values keep arriving."),
        p("last-stone-weight", "Last Stone Weight", EASY, HEAP,
            "Repeatedly smash the two heaviest stones."),
        p("k-closest-points-to-origin", "K Closest Points to Origin", MEDIUM, HEAP,
            "The k nearest points."),
        p("kth-largest-element-in-an-array", "Kth Largest Element in an Array", MEDIUM, HEAP,
            "The kth largest without fully sorting."),
        p("task-scheduler", "Task Scheduler", MEDIUM, HEAP,
            "Shortest schedule with a cooldown between repeats."),
        p("design-twitter", "Design Twitter", MEDIUM, HEAP,
            "Merge recent posts from everyone you follow."),
        p("find-median-from-data-stream", "Find Median from Data Stream", HARD, HEAP,
            "Running median of a stream.", blind75 = true),

        // --------------------------------------------------- Backtracking (9)
        p("subsets", "Subsets", MEDIUM, BACKTRACKING,
            "Every possible subset."),
        p("combination-sum", "Combination Sum", MEDIUM, BACKTRACKING,
            "All combinations summing to a target, reuse allowed.", blind75 = true),
        p("permutations", "Permutations", MEDIUM, BACKTRACKING,
            "Every ordering of the elements."),
        p("subsets-ii", "Subsets II", MEDIUM, BACKTRACKING,
            "Every subset, with duplicates in the input."),
        p("combination-sum-ii", "Combination Sum II", MEDIUM, BACKTRACKING,
            "Combinations summing to a target, each element used once."),
        p("word-search", "Word Search", MEDIUM, BACKTRACKING,
            "Trace a word through adjacent grid cells.", blind75 = true),
        p("palindrome-partitioning", "Palindrome Partitioning", MEDIUM, BACKTRACKING,
            "Every way to cut a string into palindromes."),
        p("letter-combinations-of-a-phone-number", "Letter Combinations of a Phone Number", MEDIUM,
            BACKTRACKING, "All strings a keypad number could spell."),
        p("n-queens", "N-Queens", HARD, BACKTRACKING,
            "Place n queens so none attack each other."),

        // -------------------------------------------------------- Graphs (13)
        p("number-of-islands", "Number of Islands", MEDIUM, GRAPHS,
            "Count connected regions of land.", blind75 = true),
        p("clone-graph", "Clone Graph", MEDIUM, GRAPHS,
            "Deep copy an arbitrary graph.", blind75 = true),
        p("max-area-of-island", "Max Area of Island", MEDIUM, GRAPHS,
            "Largest connected region of land."),
        p("pacific-atlantic-water-flow", "Pacific Atlantic Water Flow", MEDIUM, GRAPHS,
            "Cells draining to both oceans.", blind75 = true),
        p("surrounded-regions", "Surrounded Regions", MEDIUM, GRAPHS,
            "Capture regions not touching the border."),
        p("rotting-oranges", "Rotting Oranges", MEDIUM, GRAPHS,
            "Minutes until spoilage spreads everywhere."),
        p("walls-and-gates", "Walls and Gates", MEDIUM, GRAPHS,
            "Distance from every room to its nearest gate."),
        p("course-schedule", "Course Schedule", MEDIUM, GRAPHS,
            "Can all courses be finished given prerequisites?", blind75 = true),
        p("course-schedule-ii", "Course Schedule II", MEDIUM, GRAPHS,
            "An order in which all courses can be taken."),
        p("redundant-connection", "Redundant Connection", MEDIUM, GRAPHS,
            "Which edge creates the cycle."),
        p("number-of-connected-components-in-an-undirected-graph",
            "Number of Connected Components", MEDIUM, GRAPHS,
            "How many separate groups exist.", blind75 = true),
        p("graph-valid-tree", "Graph Valid Tree", MEDIUM, GRAPHS,
            "Connected with no cycles?", blind75 = true),
        p("word-ladder", "Word Ladder", HARD, GRAPHS,
            "Fewest one-letter steps between two words."),

        // ----------------------------------------------- Advanced Graphs (6)
        p("reconstruct-itinerary", "Reconstruct Itinerary", HARD, ADVANCED_GRAPHS,
            "Use every ticket exactly once."),
        p("min-cost-to-connect-all-points", "Min Cost to Connect All Points", MEDIUM,
            ADVANCED_GRAPHS, "Cheapest way to wire every point together."),
        p("network-delay-time", "Network Delay Time", MEDIUM, ADVANCED_GRAPHS,
            "Time for a signal to reach every node."),
        p("swim-in-rising-water", "Swim in Rising Water", HARD, ADVANCED_GRAPHS,
            "Lowest water level that lets you cross."),
        p("alien-dictionary", "Alien Dictionary", HARD, ADVANCED_GRAPHS,
            "Deduce letter order from sorted words.", blind75 = true),
        p("cheapest-flights-within-k-stops", "Cheapest Flights Within K Stops", MEDIUM,
            ADVANCED_GRAPHS, "Cheapest route with a hop limit."),

        // ---------------------------------------------------------- 1-D DP (12)
        p("climbing-stairs", "Climbing Stairs", EASY, DP_1D,
            "Ways to climb taking one or two steps.", blind75 = true),
        p("min-cost-climbing-stairs", "Min Cost Climbing Stairs", EASY, DP_1D,
            "Cheapest way to the top."),
        p("house-robber", "House Robber", MEDIUM, DP_1D,
            "Most loot without hitting adjacent houses.", blind75 = true),
        p("house-robber-ii", "House Robber II", MEDIUM, DP_1D,
            "Same, but the street is a circle.", blind75 = true),
        p("longest-palindromic-substring", "Longest Palindromic Substring", MEDIUM, DP_1D,
            "Longest stretch reading the same both ways.", blind75 = true),
        p("palindromic-substrings", "Palindromic Substrings", MEDIUM, DP_1D,
            "How many palindromic substrings exist.", blind75 = true),
        p("decode-ways", "Decode Ways", MEDIUM, DP_1D,
            "Ways to read digits back as letters.", blind75 = true),
        p("coin-change", "Coin Change", MEDIUM, DP_1D,
            "Fewest coins making an amount.", blind75 = true),
        p("maximum-product-subarray", "Maximum Product Subarray", MEDIUM, DP_1D,
            "Largest product of a contiguous run.", blind75 = true),
        p("word-break", "Word Break", MEDIUM, DP_1D,
            "Can the string be cut into dictionary words?", blind75 = true),
        p("longest-increasing-subsequence", "Longest Increasing Subsequence", MEDIUM, DP_1D,
            "Longest rising subsequence, not necessarily contiguous.", blind75 = true),
        p("partition-equal-subset-sum", "Partition Equal Subset Sum", MEDIUM, DP_1D,
            "Split into two halves of equal sum."),

        // ---------------------------------------------------------- 2-D DP (11)
        p("unique-paths", "Unique Paths", MEDIUM, DP_2D,
            "Routes across a grid moving only right and down.", blind75 = true),
        p("longest-common-subsequence", "Longest Common Subsequence", MEDIUM, DP_2D,
            "Longest sequence shared by two strings.", blind75 = true),
        p("best-time-to-buy-and-sell-stock-with-cooldown",
            "Best Time to Buy and Sell Stock with Cooldown", MEDIUM, DP_2D,
            "Trade repeatedly with a rest day after selling."),
        p("coin-change-ii", "Coin Change II", MEDIUM, DP_2D,
            "How many ways to make an amount."),
        p("target-sum", "Target Sum", MEDIUM, DP_2D,
            "Ways to sign the numbers to hit a target."),
        p("interleaving-string", "Interleaving String", MEDIUM, DP_2D,
            "Can two strings shuffle into a third?"),
        p("longest-increasing-path-in-a-matrix", "Longest Increasing Path in a Matrix", HARD,
            DP_2D, "Longest strictly rising walk through a grid."),
        p("distinct-subsequences", "Distinct Subsequences", HARD, DP_2D,
            "How many ways one string appears inside another."),
        p("edit-distance", "Edit Distance", HARD, DP_2D,
            "Fewest edits turning one string into another."),
        p("burst-balloons", "Burst Balloons", HARD, DP_2D,
            "Best order to pop for maximum coins."),
        p("regular-expression-matching", "Regular Expression Matching", HARD, DP_2D,
            "Match a string against dot and star."),

        // --------------------------------------------------------- Greedy (8)
        p("maximum-subarray", "Maximum Subarray", MEDIUM, GREEDY,
            "Largest sum of a contiguous run.", blind75 = true),
        p("jump-game", "Jump Game", MEDIUM, GREEDY,
            "Can you reach the last index?", blind75 = true),
        p("jump-game-ii", "Jump Game II", MEDIUM, GREEDY,
            "Fewest jumps to the end."),
        p("gas-station", "Gas Station", MEDIUM, GREEDY,
            "Where to start a circuit you can complete."),
        p("hand-of-straights", "Hand of Straights", MEDIUM, GREEDY,
            "Can the cards be split into consecutive runs?"),
        p("merge-triplets-to-form-target-triplet", "Merge Triplets to Form Target", MEDIUM, GREEDY,
            "Can chosen triplets combine into the target?"),
        p("partition-labels", "Partition Labels", MEDIUM, GREEDY,
            "Cut the string so no letter spans two parts."),
        p("valid-parenthesis-string", "Valid Parenthesis String", MEDIUM, GREEDY,
            "Brackets with wildcards - is it balanceable?"),

        // ------------------------------------------------------ Intervals (6)
        p("insert-interval", "Insert Interval", MEDIUM, INTERVALS,
            "Slot a new interval into a sorted set.", blind75 = true),
        p("merge-intervals", "Merge Intervals", MEDIUM, INTERVALS,
            "Combine every overlapping interval.", blind75 = true),
        p("non-overlapping-intervals", "Non-overlapping Intervals", MEDIUM, INTERVALS,
            "Fewest removals leaving no overlaps.", blind75 = true),
        p("meeting-rooms", "Meeting Rooms", EASY, INTERVALS,
            "Can one person attend every meeting?", blind75 = true),
        p("meeting-rooms-ii", "Meeting Rooms II", MEDIUM, INTERVALS,
            "How many rooms are needed at once.", blind75 = true),
        p("minimum-interval-to-include-each-query", "Minimum Interval to Include Each Query", HARD,
            INTERVALS, "Smallest interval covering each query point."),

        // ------------------------------------------------- Math & Geometry (8)
        p("rotate-image", "Rotate Image", MEDIUM, MATH_GEOMETRY,
            "Turn a matrix 90 degrees in place.", blind75 = true),
        p("spiral-matrix", "Spiral Matrix", MEDIUM, MATH_GEOMETRY,
            "Read a matrix in a spiral.", blind75 = true),
        p("set-matrix-zeroes", "Set Matrix Zeroes", MEDIUM, MATH_GEOMETRY,
            "Zero out the row and column of every zero.", blind75 = true),
        p("happy-number", "Happy Number", EASY, MATH_GEOMETRY,
            "Does repeatedly squaring digits reach one?"),
        p("plus-one", "Plus One", EASY, MATH_GEOMETRY,
            "Increment a number stored as digits."),
        p("powx-n", "Pow(x, n)", MEDIUM, MATH_GEOMETRY,
            "Exponentiation faster than repeated multiplication."),
        p("multiply-strings", "Multiply Strings", MEDIUM, MATH_GEOMETRY,
            "Multiply two numbers given as strings."),
        p("detect-squares", "Detect Squares", MEDIUM, MATH_GEOMETRY,
            "Count axis-aligned squares through a point."),

        // ------------------------------------------------ Bit Manipulation (7)
        p("single-number", "Single Number", EASY, BIT_MANIPULATION,
            "Everything appears twice except one value."),
        p("number-of-1-bits", "Number of 1 Bits", EASY, BIT_MANIPULATION,
            "Count the set bits.", blind75 = true),
        p("counting-bits", "Counting Bits", EASY, BIT_MANIPULATION,
            "Set-bit count for every number up to n.", blind75 = true),
        p("reverse-bits", "Reverse Bits", EASY, BIT_MANIPULATION,
            "Reverse the bit order of a 32-bit integer.", blind75 = true),
        p("missing-number", "Missing Number", EASY, BIT_MANIPULATION,
            "One value missing from the range 0 to n.", blind75 = true),
        p("sum-of-two-integers", "Sum of Two Integers", MEDIUM, BIT_MANIPULATION,
            "Add without using plus or minus.", blind75 = true),
        p("reverse-integer", "Reverse Integer", MEDIUM, BIT_MANIPULATION,
            "Reverse the digits, handling overflow."),
    )

    val curriculum: Curriculum = Curriculum(
        id = "neetcode-150",
        name = "NeetCode 150",
        description = "150 problems grouped by the pattern each one wants. Work the groups, " +
            "not the list - the goal is recognising the shape before you write anything.",
        problems = problems,
    )

    /**
     * The Blind 75 subset.
     *
     * Kept as a flag on the 150 rather than a separate list, so progress on one
     * automatically counts toward the other.
     */
    val blind75: Curriculum = Curriculum(
        id = "blind-75",
        name = "Blind 75",
        description = "The classic shortlist. Every problem here is also in the NeetCode 150, " +
            "so drilling one advances both.",
        problems = problems.filter { it.inBlind75 },
    )
}
