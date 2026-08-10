package com.codingarena.content

import com.codingarena.domain.model.CodingPattern
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PatternCategory

/**
 * The Pattern Library (spec 5.9).
 *
 * Each entry leads with recognition signals rather than implementation,
 * because the skill being trained is deciding *which* pattern a problem wants -
 * writing the loop afterwards is the easy half.
 */
object PatternLibrary {

    val patterns: List<CodingPattern> = listOf(

        CodingPattern(
            id = "array-traversal",
            name = "Array Traversal",
            category = PatternCategory.ARRAYS_AND_STRINGS,
            topic = CodingTopic.ARRAYS,
            summary = "Visit each element once, carrying whatever state the answer needs.",
            whenToUse = "The answer depends on every element and can be accumulated in a single " +
                "left-to-right pass.",
            recognitionSignals = listOf(
                "\"Find the sum / maximum / count of ...\"",
                "The result can be updated element by element",
                "No relationship between distant elements is needed",
            ),
            visualExample = """
                nums   = [3, 1, 4, 1, 5]
                         ^
                running = 3 -> 3 -> 4 -> 4 -> 5   (tracking the maximum)
            """.trimIndent(),
            codeTemplate = """
                best = nums[0]          # seed from the data, not from 0
                for n in nums[1:]:
                    best = max(best, n)
                return best
            """.trimIndent(),
            commonMistakes = listOf(
                "Seeding an accumulator with 0 when negative values are possible",
                "Looping to len(nums) - 1 when the last element must be included",
                "Reading nums[i + 1] without shortening the loop bound",
            ),
            timeComplexity = "O(n)",
            spaceComplexity = "O(1)",
            beginnerProblemIds = listOf("arrays-traversal-01", "output-predict-01"),
            intermediateProblemIds = listOf("debug-loops-01", "edge-case-01"),
            masteryProblemIds = listOf("debug-index-01", "linked-list-01"),
        ),

        CodingPattern(
            id = "frequency-map",
            name = "Frequency Map",
            category = PatternCategory.ARRAYS_AND_STRINGS,
            topic = CodingTopic.HASH_MAPS,
            summary = "Trade memory for time by remembering what you have already seen.",
            whenToUse = "You need to answer \"have I seen this?\" or \"how many times?\" while " +
                "scanning.",
            recognitionSignals = listOf(
                "The words duplicate, anagram, count, or \"seen before\"",
                "A brute force that rescans earlier elements",
                "Pairs that must sum or match, with no ordering requirement",
            ),
            visualExample = """
                nums = [2, 7, 11, 15], target = 9
                seen = {}         -> need 7,  not seen, store 2
                seen = {2}        -> need 2,  found - answer is (2, 7)
            """.trimIndent(),
            codeTemplate = """
                seen = set()
                for n in nums:
                    if target - n in seen:
                        return True
                    seen.add(n)     # add after checking, never before
                return False
            """.trimIndent(),
            commonMistakes = listOf(
                "Adding the current element before checking, letting it pair with itself",
                "Using a set when counts matter, which loses multiplicity",
                "Sorting first and losing the original indices",
            ),
            timeComplexity = "O(n)",
            spaceComplexity = "O(n)",
            beginnerProblemIds = listOf("duplicate-detect-01"),
            intermediateProblemIds = listOf("hashmap-lookup-01", "string-anagram-01"),
            masteryProblemIds = listOf("sliding-window-var-01"),
        ),

        CodingPattern(
            id = "two-pointers",
            name = "Two Pointers",
            category = PatternCategory.ARRAYS_AND_STRINGS,
            topic = CodingTopic.TWO_POINTERS,
            summary = "Walk two indices toward each other, discarding a whole range at each step.",
            whenToUse = "The input is sorted (or can be) and each comparison rules out an entire " +
                "row of candidates.",
            recognitionSignals = listOf(
                "The input is described as sorted",
                "Constant extra space is required",
                "You are looking for a pair, a palindrome, or a partition",
            ),
            visualExample = """
                [1, 3, 5, 8, 11]   target = 13
                 L           R     1 + 11 = 12  too small -> move L
                    L        R     3 + 11 = 14  too large -> move R
                    L     R        3 +  8 = 11  too small -> move L
                       L  R        5 +  8 = 13  found
            """.trimIndent(),
            codeTemplate = """
                lo, hi = 0, len(nums) - 1
                while lo < hi:
                    total = nums[lo] + nums[hi]
                    if total == target: return (lo, hi)
                    if total < target:  lo += 1
                    else:               hi -= 1
                return None
            """.trimIndent(),
            commonMistakes = listOf(
                "Applying the movement rule to unsorted input, where it is not valid",
                "Moving both pointers in one step and skipping the answer",
                "Using <= as the loop condition and comparing an element with itself",
            ),
            timeComplexity = "O(n)",
            spaceComplexity = "O(1)",
            beginnerProblemIds = listOf("two-pointers-01"),
            intermediateProblemIds = listOf("hashmap-lookup-01"),
            masteryProblemIds = listOf("sliding-window-var-01"),
        ),

        CodingPattern(
            id = "sliding-window",
            name = "Sliding Window",
            category = PatternCategory.ARRAYS_AND_STRINGS,
            topic = CodingTopic.SLIDING_WINDOW,
            summary = "Maintain a contiguous range and update it incrementally as it moves.",
            whenToUse = "The answer is a contiguous subarray or substring, and recomputing it " +
                "from scratch each step would be wasteful.",
            recognitionSignals = listOf(
                "The words contiguous, consecutive, subarray, or substring",
                "A fixed size k, or a constraint that grows and shrinks",
                "A brute force that re-sums overlapping ranges",
            ),
            visualExample = """
                fixed k = 3
                [2, 1, 5, 1, 3, 2]
                 [-----]              sum 8
                    [-----]           sum 7   = 8 + 1 - 2
                       [-----]        sum 9   = 7 + 3 - 1
            """.trimIndent(),
            codeTemplate = """
                # variable window
                left = 0
                for right, value in enumerate(nums):
                    add(value)
                    while window_is_invalid():
                        remove(nums[left]); left += 1
                    best = max(best, right - left + 1)
            """.trimIndent(),
            commonMistakes = listOf(
                "Recomputing the window from scratch, which returns the solution to O(n*k)",
                "Getting the departing index wrong by one",
                "Measuring the window before restoring its validity",
            ),
            timeComplexity = "O(n)",
            spaceComplexity = "O(1) fixed, O(k) with a frequency map",
            beginnerProblemIds = listOf("sliding-window-fixed-01"),
            intermediateProblemIds = listOf("sliding-window-var-01"),
            masteryProblemIds = listOf("prefix-sum-01"),
        ),

        CodingPattern(
            id = "prefix-sum",
            name = "Prefix Sum",
            category = PatternCategory.ARRAYS_AND_STRINGS,
            topic = CodingTopic.ARRAYS,
            summary = "Precompute cumulative totals so any range sum becomes one subtraction.",
            whenToUse = "The array is static and many range queries are asked of it.",
            recognitionSignals = listOf(
                "Many queries over an unchanging array",
                "\"Sum between i and j\" or \"count of subarrays summing to k\"",
            ),
            visualExample = """
                nums   = [ 2,  4,  1,  3]
                prefix = [0, 2,  6,  7, 10]
                sum(1..2) = prefix[3] - prefix[1] = 7 - 2 = 5
            """.trimIndent(),
            codeTemplate = """
                prefix = [0]
                for n in nums:
                    prefix.append(prefix[-1] + n)
                # inclusive range i..j
                range_sum = prefix[j + 1] - prefix[i]
            """.trimIndent(),
            commonMistakes = listOf(
                "Mixing inclusive and exclusive bounds between build and query",
                "Rebuilding the prefix array per query",
            ),
            timeComplexity = "O(n) build, O(1) per query",
            spaceComplexity = "O(n)",
            beginnerProblemIds = listOf("prefix-sum-01"),
            intermediateProblemIds = listOf("sliding-window-fixed-01"),
        ),

        CodingPattern(
            id = "dfs",
            name = "Depth-First Search",
            category = PatternCategory.TREES,
            topic = CodingTopic.TREES,
            summary = "Follow one branch to its end before backtracking to the next.",
            whenToUse = "You need paths, depth, or any answer that combines results from " +
                "children.",
            recognitionSignals = listOf(
                "\"Path from root to leaf\", depth, or height",
                "The answer for a node is built from its children's answers",
            ),
            visualExample = """
                        4
                       / \
                      2   6      pre-order:  4 2 1 3 6
                     / \         in-order:   1 2 3 4 6
                    1   3        post-order: 1 3 2 6 4
            """.trimIndent(),
            codeTemplate = """
                def dfs(node):
                    if node is None: return 0
                    left  = dfs(node.left)
                    right = dfs(node.right)
                    return 1 + max(left, right)
            """.trimIndent(),
            commonMistakes = listOf(
                "Misplacing the visit and producing the wrong traversal order",
                "Forgetting the null base case",
                "Ignoring the O(h) stack cost when the tree may be a chain",
            ),
            timeComplexity = "O(n)",
            spaceComplexity = "O(h)",
            beginnerProblemIds = listOf("tree-dfs-01"),
            intermediateProblemIds = listOf("recursion-01"),
            masteryProblemIds = listOf("graph-traversal-01"),
        ),

        CodingPattern(
            id = "bfs",
            name = "Breadth-First Search",
            category = PatternCategory.TREES,
            topic = CodingTopic.QUEUES,
            summary = "Expand outward one layer at a time using a queue.",
            whenToUse = "You need level order, or the shortest path in an unweighted graph.",
            recognitionSignals = listOf(
                "\"Level by level\", \"minimum number of steps\", \"shortest path\"",
                "All edges cost the same",
            ),
            visualExample = """
                queue: [4]        -> visit 4, enqueue 2, 6
                queue: [2, 6]     -> visit 2, enqueue 1, 3
                queue: [6, 1, 3]  -> visit 6
                order: 4 2 6 1 3
            """.trimIndent(),
            codeTemplate = """
                queue, seen = deque([start]), {start}
                while queue:
                    node = queue.popleft()
                    for nxt in graph[node]:
                        if nxt not in seen:
                            seen.add(nxt)       # mark on enqueue, not on dequeue
                            queue.append(nxt)
            """.trimIndent(),
            commonMistakes = listOf(
                "Marking nodes seen when dequeuing, which lets duplicates queue up",
                "Using a stack and silently getting depth-first order",
                "Not snapshotting the queue length when levels must stay separate",
            ),
            timeComplexity = "O(V + E)",
            spaceComplexity = "O(V)",
            beginnerProblemIds = listOf("queue-behaviour-01", "tree-bfs-01"),
            intermediateProblemIds = listOf("graph-rearrange-01"),
            masteryProblemIds = listOf("graph-traversal-01"),
        ),

        CodingPattern(
            id = "graph-traversal",
            name = "Graph Traversal",
            category = PatternCategory.GRAPHS,
            topic = CodingTopic.GRAPHS,
            summary = "Explore connected structure, visiting each node exactly once.",
            whenToUse = "Connectivity, components, reachability, or flood fill over a grid.",
            recognitionSignals = listOf(
                "\"Connected\", \"region\", \"island\", \"reachable\"",
                "A grid where neighbouring cells relate to each other",
            ),
            visualExample = """
                1 1 0 0        two regions of 1s:
                1 0 0 1        - top-left block
                0 0 1 1        - bottom-right block
            """.trimIndent(),
            codeTemplate = """
                count = 0
                for cell in grid_cells:
                    if is_land(cell) and cell not in seen:
                        flood(cell)    # DFS or BFS marks the whole region
                        count += 1
                return count
            """.trimIndent(),
            commonMistakes = listOf(
                "Not marking cells visited, so a region is counted repeatedly",
                "Including diagonals when the problem says four-directional",
                "Recursing on a large grid and overflowing the stack",
            ),
            timeComplexity = "O(V + E)",
            spaceComplexity = "O(V)",
            beginnerProblemIds = listOf("graph-traversal-01"),
            intermediateProblemIds = listOf("graph-rearrange-01"),
        ),

        CodingPattern(
            id = "union-find",
            name = "Union Find",
            category = PatternCategory.GRAPHS,
            topic = CodingTopic.GRAPHS,
            summary = "Track disjoint sets with near-constant merge and lookup.",
            whenToUse = "Groups merge over time, or you must detect a cycle while adding edges.",
            recognitionSignals = listOf(
                "Edges arrive one at a time and groups combine",
                "\"Are these two in the same group?\"",
                "Minimum spanning tree (Kruskal)",
            ),
            visualExample = """
                union(1,2) -> {1,2}
                union(3,4) -> {1,2} {3,4}
                union(2,3) -> {1,2,3,4}
                find(4) == find(1)  ->  true
            """.trimIndent(),
            codeTemplate = """
                def find(x):
                    while parent[x] != x:
                        parent[x] = parent[parent[x]]   # path compression
                        x = parent[x]
                    return x

                def union(a, b):
                    ra, rb = find(a), find(b)
                    if ra == rb: return False           # already connected
                    parent[ra] = rb
                    return True
            """.trimIndent(),
            commonMistakes = listOf(
                "Skipping path compression or union by rank and degrading to O(n) per query",
                "Comparing elements rather than their roots",
            ),
            timeComplexity = "near O(1) amortised per operation",
            spaceComplexity = "O(n)",
            beginnerProblemIds = listOf("unionfind-01"),
            intermediateProblemIds = listOf("unionfind-02"),
        ),

        CodingPattern(
            id = "topological-sort",
            name = "Topological Sort",
            category = PatternCategory.GRAPHS,
            topic = CodingTopic.GRAPHS,
            summary = "Order the nodes of a directed acyclic graph so every edge points forward.",
            whenToUse = "Tasks have prerequisites, or you must detect a cycle in a dependency " +
                "graph.",
            recognitionSignals = listOf(
                "\"Prerequisites\", \"build order\", \"schedule\"",
                "\"Is it possible to finish all ...?\"",
            ),
            visualExample = """
                A -> B -> D
                 \-> C -/
                valid order: A, B, C, D  (or A, C, B, D)
            """.trimIndent(),
            codeTemplate = """
                queue = [n for n in nodes if indegree[n] == 0]
                order = []
                while queue:
                    n = queue.pop()
                    order.append(n)
                    for m in graph[n]:
                        indegree[m] -= 1
                        if indegree[m] == 0: queue.append(m)
                return order if len(order) == len(nodes) else []   # short = cycle
            """.trimIndent(),
            commonMistakes = listOf(
                "Not checking that every node was emitted, missing a cycle",
                "Building the indegree map from the wrong edge direction",
            ),
            timeComplexity = "O(V + E)",
            spaceComplexity = "O(V + E)",
            beginnerProblemIds = listOf("toposort-01"),
            intermediateProblemIds = listOf("toposort-02"),
        ),

        CodingPattern(
            id = "binary-search",
            name = "Binary Search",
            category = PatternCategory.SEARCHING_AND_SORTING,
            topic = CodingTopic.BINARY_SEARCH,
            summary = "Halve the search space on every comparison.",
            whenToUse = "The data is sorted, or some predicate over the answer is monotonic.",
            recognitionSignals = listOf(
                "Sorted input",
                "\"Minimum value such that ...\" or \"maximum value such that ...\"",
                "An O(n) check exists and the answer range is large",
            ),
            visualExample = """
                [1, 3, 5, 7, 9, 11]   target 9
                lo=0 hi=5 mid=2 -> 5  < 9   lo = 3
                lo=3 hi=5 mid=4 -> 9 == 9   found
            """.trimIndent(),
            codeTemplate = """
                lo, hi = 0, len(nums) - 1
                while lo <= hi:
                    mid = lo + (hi - lo) // 2      # avoids overflow in fixed-width ints
                    if nums[mid] == target: return mid
                    if nums[mid] <  target: lo = mid + 1   # always exclude mid
                    else:                   hi = mid - 1
                return -1
            """.trimIndent(),
            commonMistakes = listOf(
                "Leaving mid inside the range, which loops forever",
                "Mismatching the loop condition with the update rule",
                "Forgetting that the input must be sorted",
            ),
            timeComplexity = "O(log n)",
            spaceComplexity = "O(1)",
            beginnerProblemIds = listOf("binary-search-complexity-01"),
            intermediateProblemIds = listOf("binary-search-01"),
        ),

        CodingPattern(
            id = "sorting",
            name = "Sorting and Comparators",
            category = PatternCategory.SEARCHING_AND_SORTING,
            topic = CodingTopic.SORTING,
            summary = "Reorder data so the answer becomes obvious - and encode tie-breaks in the " +
                "comparator.",
            whenToUse = "Order matters, duplicates should become adjacent, or a greedy choice " +
                "needs a sorted sequence.",
            recognitionSignals = listOf(
                "\"Sort by X, then by Y\"",
                "Merging or grouping intervals",
                "A greedy strategy that needs the largest or smallest first",
            ),
            visualExample = """
                sort by (-salary, name):
                  (90, "ana")  (90, "bo")  (70, "cy")
            """.trimIndent(),
            codeTemplate = """
                people.sort(key=lambda p: (-p.salary, p.name))
            """.trimIndent(),
            commonMistakes = listOf(
                "Chaining separate sorts in the wrong order",
                "Assuming stability in a language that does not guarantee it",
                "Sorting when an O(n) hash-based pass would do",
            ),
            timeComplexity = "O(n log n)",
            spaceComplexity = "O(1) to O(n)",
            beginnerProblemIds = listOf("sorting-01"),
            intermediateProblemIds = listOf("sorting-02"),
        ),

        CodingPattern(
            id = "monotonic-stack",
            name = "Monotonic Stack",
            category = PatternCategory.OTHER,
            topic = CodingTopic.STACKS,
            summary = "Hold elements still waiting for an answer in sorted order on a stack.",
            whenToUse = "Each element needs the next (or previous) larger or smaller element.",
            recognitionSignals = listOf(
                "\"Next greater\", \"previous smaller\", \"days until warmer\"",
                "Largest rectangle in a histogram",
                "A brute force that rescans to the right from each element",
            ),
            visualExample = """
                nums  = [2, 1, 3]
                stack = [2]      -> 1 < 2, push
                stack = [2, 1]   -> 3 pops 1 (answer 3), pops 2 (answer 3)
                result = [3, 3, -1]
            """.trimIndent(),
            codeTemplate = """
                stack, result = [], [-1] * len(nums)
                for i, n in enumerate(nums):
                    while stack and nums[stack[-1]] < n:
                        result[stack.pop()] = n
                    stack.append(i)      # store indices, not values
                return result
            """.trimIndent(),
            commonMistakes = listOf(
                "Storing values when the position is what the answer needs",
                "Forgetting that leftovers on the stack have no answer",
            ),
            timeComplexity = "O(n)",
            spaceComplexity = "O(n)",
            beginnerProblemIds = listOf("stack-behaviour-01"),
            intermediateProblemIds = listOf("monotonic-stack-01"),
        ),

        CodingPattern(
            id = "backtracking",
            name = "Backtracking",
            category = PatternCategory.OTHER,
            topic = CodingTopic.BACKTRACKING,
            summary = "Build a candidate step by step and undo the step when it cannot work.",
            whenToUse = "You must enumerate permutations, combinations, or all valid " +
                "configurations.",
            recognitionSignals = listOf(
                "\"All possible\", \"every combination\", \"generate\"",
                "Constraints that can be checked partway through",
                "N-queens, sudoku, word search",
            ),
            visualExample = """
                []           choose 1 -> [1]  choose 2 -> [1,2]  record
                             undo      <- [1]  choose 3 -> [1,3]  record
            """.trimIndent(),
            codeTemplate = """
                def backtrack(path, choices):
                    if is_complete(path):
                        results.append(path[:])     # copy, not the live list
                        return
                    for c in choices:
                        if not is_valid(path, c): continue
                        path.append(c)
                        backtrack(path, remaining(choices, c))
                        path.pop()                  # undo
            """.trimIndent(),
            commonMistakes = listOf(
                "Appending the live list instead of a copy, so every result mutates together",
                "Forgetting to undo the choice after recursing",
                "Pruning too late and exploring branches that cannot succeed",
            ),
            timeComplexity = "Exponential, reduced by pruning",
            spaceComplexity = "O(depth)",
            beginnerProblemIds = listOf("backtracking-01"),
            intermediateProblemIds = listOf("backtracking-03"),
            masteryProblemIds = listOf("backtracking-02"),
        ),

        CodingPattern(
            id = "greedy",
            name = "Greedy",
            category = PatternCategory.OTHER,
            topic = CodingTopic.GREEDY,
            summary = "Take the locally best option and never reconsider it.",
            whenToUse = "A local choice is provably part of some optimal solution.",
            recognitionSignals = listOf(
                "Interval scheduling, or merging intervals after a sort",
                "\"Minimum number of ...\" with an obvious ordering",
                "Fractional (divisible) resources",
            ),
            visualExample = """
                meetings sorted by end time:
                  [1,3] [2,5] [4,7]
                take [1,3], skip [2,5] (overlaps), take [4,7]  ->  2 meetings
            """.trimIndent(),
            codeTemplate = """
                items.sort(key=chooser)
                for item in items:
                    if fits(item):
                        take(item)
            """.trimIndent(),
            commonMistakes = listOf(
                "Applying greedy where items are indivisible and DP is required",
                "Sorting by the wrong key - end time versus start time changes the answer",
                "Not being able to justify why the local choice is safe",
            ),
            timeComplexity = "O(n log n) with the sort",
            spaceComplexity = "O(1)",
            beginnerProblemIds = listOf("greedy-01"),
            intermediateProblemIds = listOf("greedy-02"),
            masteryProblemIds = listOf("greedy-03"),
        ),

        CodingPattern(
            id = "dynamic-programming",
            name = "Dynamic Programming",
            category = PatternCategory.OTHER,
            topic = CodingTopic.DYNAMIC_PROGRAMMING,
            summary = "Solve overlapping subproblems once and reuse the answers.",
            whenToUse = "The optimal answer is built from optimal answers to smaller versions of " +
                "the same problem, and those smaller versions repeat.",
            recognitionSignals = listOf(
                "\"Maximum / minimum / number of ways\" under a constraint",
                "A recursive brute force that recomputes the same arguments",
                "Take-it-or-leave-it choices under a budget",
            ),
            visualExample = """
                0/1 knapsack, capacity 5
                items (w,v): (2,3) (3,4) (4,5)
                dp = [0,0,3,4,7,7]   ->  best value 7
            """.trimIndent(),
            codeTemplate = """
                dp = [0] * (capacity + 1)
                for w, v in items:
                    for c in range(capacity, w - 1, -1):   # downward: each item used once
                        dp[c] = max(dp[c], dp[c - w] + v)
                return dp[capacity]
            """.trimIndent(),
            commonMistakes = listOf(
                "Iterating capacity upward in the 1-D version, silently reusing an item",
                "Using greedy on the 0/1 variant",
                "Getting the base cases wrong and shifting every later answer",
            ),
            timeComplexity = "O(states * transitions)",
            spaceComplexity = "O(states), often reducible by one dimension",
            beginnerProblemIds = listOf("dp-02"),
            intermediateProblemIds = listOf("dp-knapsack-01"),
        ),

        CodingPattern(
            id = "heap",
            name = "Heap",
            category = PatternCategory.OTHER,
            topic = CodingTopic.HEAPS,
            summary = "Keep the smallest (or largest) element instantly available.",
            whenToUse = "You repeatedly need the extreme element of a changing collection.",
            recognitionSignals = listOf(
                "\"Top k\", \"k-th largest\", \"median of a stream\"",
                "Merging sorted lists",
                "Scheduling by the earliest finishing task",
            ),
            visualExample = """
                top 2 of [5, 1, 9, 3] using a min-heap capped at 2:
                [5] -> [1,5] -> push 9, pop 1 -> [5,9] -> 3 < 5, discard
                result: 5, 9
            """.trimIndent(),
            codeTemplate = """
                heap = []
                for n in nums:
                    heapq.heappush(heap, n)
                    if len(heap) > k:
                        heapq.heappop(heap)   # drop the weakest of the current best k
                return heap
            """.trimIndent(),
            commonMistakes = listOf(
                "Using a max-heap for top-k, whose root is not the element to evict",
                "Letting the heap grow to n when it only needs k",
                "Forgetting to negate values to simulate a max-heap where only min-heaps exist",
            ),
            timeComplexity = "O(n log k)",
            spaceComplexity = "O(k)",
            beginnerProblemIds = listOf("heap-02"),
            intermediateProblemIds = listOf("heap-topk-01"),
        ),

        CodingPattern(
            id = "trie",
            name = "Trie",
            category = PatternCategory.OTHER,
            topic = CodingTopic.TRIES,
            summary = "Store strings by shared prefix so prefix queries cost only their length.",
            whenToUse = "Many words are queried by prefix, or autocomplete is involved.",
            recognitionSignals = listOf(
                "\"Starts with\", prefix, autocomplete, dictionary",
                "Repeated searches over a fixed word list",
            ),
            visualExample = """
                insert: cat, car, dog
                        (root)
                        /    \
                       c      d
                       |      |
                       a      o
                      / \     |
                     t   r    g
            """.trimIndent(),
            codeTemplate = """
                node = root
                for ch in word:
                    node = node.children.setdefault(ch, {})
                node["*"] = True        # mark a complete word
            """.trimIndent(),
            commonMistakes = listOf(
                "Not marking word ends, so a prefix reads as a stored word",
                "Building a trie for a single lookup, where a hash set is simpler",
            ),
            timeComplexity = "O(length) per insert or query",
            spaceComplexity = "O(total characters)",
            beginnerProblemIds = listOf("trie-01"),
            intermediateProblemIds = listOf("trie-02"),
        ),

        CodingPattern(
            id = "bit-manipulation",
            name = "Bit Manipulation",
            category = PatternCategory.OTHER,
            topic = CodingTopic.BIT_MANIPULATION,
            summary = "Treat a number as a row of flags and operate on them directly.",
            whenToUse = "Constant extra space is demanded, values pair up and cancel, or you are " +
                "tracking a small set of on/off states.",
            recognitionSignals = listOf(
                "\"Without extra space\" on a problem about duplicates or pairs",
                "Counting set bits, powers of two, or subsets encoded as masks",
                "Every element appears twice except one",
            ),
            visualExample = """
                x     = 12  ->  1100
                x - 1 = 11  ->  1011
                x & (x-1)   ->  1000   (lowest set bit cleared)

                XOR cancels pairs:  4 ^ 7 ^ 4  ->  7
            """.trimIndent(),
            codeTemplate = """
                x & 1          # lowest bit
                x >> 1         # drop the lowest bit
                x & (x - 1)    # clear the lowest set bit
                a ^ b ^ a == b # equal values cancel
            """.trimIndent(),
            commonMistakes = listOf(
                "Confusing the number of bit positions with the number of set bits",
                "Assuming XOR needs the input sorted or grouped - it does not",
                "Forgetting that shifts on signed types can behave differently per language",
            ),
            timeComplexity = "O(1) per operation, O(bits) per scan",
            spaceComplexity = "O(1)",
            beginnerProblemIds = listOf("bits-01"),
            intermediateProblemIds = listOf("bits-02"),
            masteryProblemIds = listOf("bits-03"),
        ),

        CodingPattern(
            id = "complexity",
            name = "Complexity Analysis",
            category = PatternCategory.OTHER,
            topic = CodingTopic.COMPLEXITY,
            summary = "Count how the work grows with the input, and remember the call stack.",
            whenToUse = "Always - it is the question every interviewer asks after the code works.",
            recognitionSignals = listOf(
                "Nested loops: multiply their counts",
                "Sequential loops: add them, then keep the dominant term",
                "A counter that multiplies rather than increments: logarithmic",
            ),
            visualExample = """
                for i in range(n):        n
                    j = 1
                    while j < n:          log n
                        j *= 2
                total: O(n log n)
            """.trimIndent(),
            codeTemplate = """
                # time  : how many operations as n grows
                # space : data structures + maximum recursion depth
            """.trimIndent(),
            commonMistakes = listOf(
                "Assuming every nested loop is O(n^2)",
                "Ignoring recursion depth when reporting space complexity",
                "Quoting the search cost while forgetting a sort that preceded it",
            ),
            timeComplexity = "n/a",
            spaceComplexity = "n/a",
            beginnerProblemIds = listOf("time-complexity-01"),
            intermediateProblemIds = listOf("space-complexity-01", "binary-search-complexity-01"),
        ),
    )

    private val byId: Map<String, CodingPattern> = patterns.associateBy { it.id }

    fun byId(id: String): CodingPattern? = byId[id]

    fun byCategory(category: PatternCategory): List<CodingPattern> =
        patterns.filter { it.category == category }

    val categories: List<PatternCategory> =
        PatternCategory.entries.filter { category -> patterns.any { it.category == category } }
}
