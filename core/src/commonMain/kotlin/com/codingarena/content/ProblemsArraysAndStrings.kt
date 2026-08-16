package com.codingarena.content

import com.codingarena.domain.model.AnswerChoice
import com.codingarena.domain.model.ChallengeType
import com.codingarena.domain.model.CodeVariant
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.ProgrammingLanguage.CPP
import com.codingarena.domain.model.ProgrammingLanguage.GO
import com.codingarena.domain.model.ProgrammingLanguage.JAVA
import com.codingarena.domain.model.ProgrammingLanguage.JAVASCRIPT
import com.codingarena.domain.model.ProgrammingLanguage.KOTLIN
import com.codingarena.domain.model.ProgrammingLanguage.SWIFT

/**
 * Starter content, part one: array and string fundamentals.
 *
 * Every distractor carries a [AnswerChoice.rationale] (why it fails) and,
 * where the instinct behind it is reasonable, an [AnswerChoice.insight] (what
 * was right about it). The Solution Review screen is only as specific as this
 * content is, so both fields are treated as required authoring, not optional
 * polish.
 */
internal val arraysAndStringsProblems: List<CodingProblem> = listOf(

    CodingProblem(
        id = "arrays-traversal-01",
        title = "Summing every other element",
        description = "What does this function return for nums = [1, 2, 3, 4, 5]?",
        difficultyRating = 850,
        primaryTopic = CodingTopic.ARRAYS,
        challengeType = ChallengeType.OUTPUT_PREDICTION,
        codeSnippet = """
            def total(nums):
                result = 0
                for i in range(0, len(nums), 2):
                    result += nums[i]
                return result
        """.trimIndent(),
        choices = listOf(
            AnswerChoice("a", "9", rationale = "Correct: indices 0, 2 and 4 hold 1, 3 and 5."),
            AnswerChoice(
                "b", "6",
                rationale = "That sums indices 1 and 3 (2 + 4). The range starts at 0, not 1.",
                insight = "You correctly spotted that the loop skips elements.",
            ),
            AnswerChoice(
                "c", "15",
                rationale = "That is the sum of every element. The step of 2 means half are skipped.",
                insight = "You read the accumulation correctly - the step argument is the detail.",
            ),
            AnswerChoice("d", "5", rationale = "That is just the last element, not a running total."),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "range(0, 5, 2) yields 0, 2, 4, so the function adds nums[0] + nums[2] + " +
            "nums[4] = 1 + 3 + 5 = 9.",
        bestApproach = "Read the three arguments to range as start, stop and step, then list the " +
            "indices it actually produces before touching the values.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Assuming range's third argument is an end index rather than a step",
            "Forgetting that range's stop value is exclusive",
        ),
        hints = listOf(
            "Write out the indices the loop visits before the values.",
            "range(start, stop, step) - the third argument is the step.",
        ),
        patternId = "array-traversal",
        estimatedSeconds = 40,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                static int total(int[] nums) {
                    int result = 0;
                    for (int i = 0; i < nums.length; i += 2) {
                        result += nums[i];
                    }
                    return result;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function total(nums) {
                    let result = 0;
                    for (let i = 0; i < nums.length; i += 2) {
                        result += nums[i];
                    }
                    return result;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun total(nums: IntArray): Int {
                    var result = 0
                    var i = 0
                    while (i < nums.size) {
                        result += nums[i]
                        i += 2
                    }
                    return result
                }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                int total(vector<int>& nums) {
                    int result = 0;
                    for (int i = 0; i < (int)nums.size(); i += 2) {
                        result += nums[i];
                    }
                    return result;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                func total(nums []int) int {
                    result := 0
                    for i := 0; i < len(nums); i += 2 {
                        result += nums[i]
                    }
                    return result
                }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                func total(_ nums: [Int]) -> Int {
                    var result = 0
                    var i = 0
                    while i < nums.count {
                        result += nums[i]
                        i += 2
                    }
                    return result
                }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "hashmap-lookup-01",
        title = "Two numbers that add to a target",
        description = "Given an unsorted array, find whether two numbers sum to a target. " +
            "Which approach is best?",
        difficultyRating = 950,
        primaryTopic = CodingTopic.HASH_MAPS,
        secondaryTopics = listOf(CodingTopic.ARRAYS),
        challengeType = ChallengeType.MULTIPLE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "Store each value seen so far in a hash set and look for target - value",
                rationale = "Correct: one pass, O(1) average lookups.",
            ),
            AnswerChoice(
                "b", "Check every pair with two nested loops",
                rationale = "This works but takes O(n^2). Repeated lookups are the signal to " +
                    "reach for a hash map instead.",
                insight = "You identified that we need to relate pairs of elements - that part is right.",
            ),
            AnswerChoice(
                "c", "Sort the array, then use two pointers from both ends",
                rationale = "This is correct and O(n log n), but sorting is unnecessary work when " +
                    "a hash set gets you to O(n).",
                insight = "Strong instinct - this is exactly the right approach when the array is " +
                    "already sorted or you need the pair's original ordering.",
            ),
            AnswerChoice(
                "d", "Binary search for target - value for each element",
                rationale = "Binary search needs a sorted array, so this still costs a sort first, " +
                    "and it lands at O(n log n).",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "As you scan, you already know every value behind you. Storing them in a " +
            "hash set turns \"have I seen target - value?\" into an O(1) question, so the whole " +
            "problem collapses to a single pass.",
        bestApproach = "Keep a hash set of values already seen and, for each new value, check " +
            "whether target - value is in it.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Sorting when the original indices are needed",
            "Adding the current value to the set before checking for its complement, which lets " +
                "one element pair with itself",
        ),
        hints = listOf(
            "What do you already know about every element behind your current position?",
            "Which data structure answers \"have I seen this?\" in constant time?",
        ),
        patternId = "frequency-map",
        estimatedSeconds = 45,
    ),

    CodingProblem(
        id = "duplicate-detect-01",
        title = "Detecting a duplicate",
        description = "You must report whether an array of a million integers contains any " +
            "duplicate. Which data structure fits best?",
        difficultyRating = 900,
        primaryTopic = CodingTopic.HASH_MAPS,
        secondaryTopics = listOf(CodingTopic.ARRAYS),
        challengeType = ChallengeType.DATA_STRUCTURE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "A hash set",
                rationale = "Correct: insert as you scan and stop the moment an insert reports the " +
                    "value was already present.",
            ),
            AnswerChoice(
                "b", "A sorted list",
                rationale = "Sorting first costs O(n log n) when a single O(n) pass is available.",
                insight = "Sorting does make duplicates adjacent, which is a genuinely useful " +
                    "property - it is just more work than this question needs.",
            ),
            AnswerChoice(
                "c", "A stack",
                rationale = "A stack only exposes its top element, so it cannot answer \"have I " +
                    "seen this anywhere?\".",
            ),
            AnswerChoice(
                "d", "A queue",
                rationale = "A queue preserves order but offers no membership test.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "\"Have I seen this value before?\" is a membership question, and a hash " +
            "set answers membership in O(1) average time.",
        bestApproach = "Scan once, inserting into a hash set and returning true as soon as an " +
            "insert finds the value already there.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Sorting purely to make duplicates adjacent when memory is not constrained",
            "Building the whole set before checking, instead of stopping at the first repeat",
        ),
        hints = listOf("What kind of question is \"have I seen this before?\""),
        patternId = "frequency-map",
        estimatedSeconds = 35,
    ),

    CodingProblem(
        id = "two-pointers-01",
        title = "Recognising the two-pointer setup",
        description = "\"Given a sorted array, return the two values that sum to a target, using " +
            "constant extra space.\" Which pattern does this call for?",
        difficultyRating = 1050,
        primaryTopic = CodingTopic.TWO_POINTERS,
        secondaryTopics = listOf(CodingTopic.ARRAYS),
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "Two pointers from both ends",
                rationale = "Correct: sorted input plus a constant-space requirement is the " +
                    "signature of this pattern.",
            ),
            AnswerChoice(
                "b", "Frequency map",
                rationale = "A hash map would solve it, but it needs O(n) extra space, and the " +
                    "question rules that out.",
                insight = "On an unsorted array with no space constraint this would be the right call.",
            ),
            AnswerChoice(
                "c", "Sliding window",
                rationale = "A window suits contiguous subarrays. Here the two answers can sit " +
                    "anywhere in the array.",
            ),
            AnswerChoice(
                "d", "Binary search on the answer",
                rationale = "There is no monotonic predicate to search over here.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "With the array sorted, a sum that is too small can only be fixed by moving " +
            "the left pointer right, and a sum that is too large by moving the right pointer " +
            "left. Each step discards a whole row of possibilities, so one pass suffices.",
        bestApproach = "Start a pointer at each end. Move the left pointer inward when the sum is " +
            "too small and the right pointer inward when it is too large.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Using two pointers on unsorted input, where the movement rule no longer holds",
            "Moving both pointers on the same step and skipping the answer",
        ),
        hints = listOf(
            "Two constraints are doing the work here: the array is sorted, and space must be constant.",
            "If the current sum is too small, which pointer can possibly help?",
        ),
        patternId = "two-pointers",
        estimatedSeconds = 45,
    ),

    CodingProblem(
        id = "sliding-window-fixed-01",
        title = "Completing a fixed-size window",
        description = "This function finds the largest sum of any k consecutive elements. " +
            "Which line belongs in the blank?",
        difficultyRating = 1100,
        primaryTopic = CodingTopic.SLIDING_WINDOW,
        secondaryTopics = listOf(CodingTopic.ARRAYS),
        challengeType = ChallengeType.FILL_IN_THE_BLANK,
        codeSnippet = """
            def max_window_sum(nums, k):
                window = sum(nums[:k])
                best = window
                for i in range(k, len(nums)):
                    # ___ blank ___
                    best = max(best, window)
                return best
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "window += nums[i] - nums[i - k]",
                rationale = "Correct: add the element entering the window, remove the one leaving it.",
            ),
            AnswerChoice(
                "b", "window += nums[i]",
                rationale = "The window would keep growing - nothing ever leaves it, so this " +
                    "computes a prefix sum instead.",
                insight = "Adding the incoming element is half of the update. The other half is " +
                    "removing the one that fell out.",
            ),
            AnswerChoice(
                "c", "window = sum(nums[i - k:i])",
                rationale = "Correct results, but re-summing the window each step makes it O(n*k) " +
                    "and throws away the whole point of the pattern.",
                insight = "The window boundaries are right - it is the cost that is wrong.",
            ),
            AnswerChoice(
                "d", "window += nums[i] - nums[i - k + 1]",
                rationale = "Off by one: the element leaving a k-wide window ending at i is at " +
                    "index i - k, not i - k + 1.",
                insight = "You have the right shape for the update - only the departing index is off.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "A fixed window moving one step right gains exactly one element and loses " +
            "exactly one. Updating in O(1) per step is what turns an O(n*k) scan into O(n).",
        bestApproach = "Maintain the running window sum and adjust it by the entering and leaving " +
            "elements on each step.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Recomputing the window sum from scratch on every step",
            "Getting the departing index wrong by one",
        ),
        hints = listOf(
            "When the window slides one place right, how many elements change?",
            "Which index falls out of a k-wide window that now ends at i?",
        ),
        patternId = "sliding-window",
        estimatedSeconds = 55,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                static int maxWindowSum(int[] nums, int k) {
                    int window = 0;
                    for (int i = 0; i < k; i++) window += nums[i];
                    int best = window;
                    for (int i = k; i < nums.length; i++) {
                        // ___ blank ___
                        best = Math.max(best, window);
                    }
                    return best;
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "window += nums[i] - nums[i - k];", rationale = "Correct: add the element entering the window, remove the one leaving it."),
                    AnswerChoice("b", "window += nums[i];", rationale = "The window would keep growing - nothing ever leaves it, so this computes a prefix sum instead.", insight = "Adding the incoming element is half of the update. The other half is removing the one that fell out."),
                    AnswerChoice("c", "window = Arrays.stream(nums, i - k, i).sum();", rationale = "Correct results, but re-summing the window each step makes it O(n*k) and throws away the whole point of the pattern.", insight = "The window boundaries are right - it is the cost that is wrong."),
                    AnswerChoice("d", "window += nums[i] - nums[i - k + 1];", rationale = "Off by one: the element leaving a k-wide window ending at i is at index i - k, not i - k + 1.", insight = "You have the right shape for the update - only the departing index is off."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function maxWindowSum(nums, k) {
                    let window = nums.slice(0, k).reduce((a, b) => a + b, 0);
                    let best = window;
                    for (let i = k; i < nums.length; i++) {
                        // ___ blank ___
                        best = Math.max(best, window);
                    }
                    return best;
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "window += nums[i] - nums[i - k];", rationale = "Correct: add the element entering the window, remove the one leaving it."),
                    AnswerChoice("b", "window += nums[i];", rationale = "The window would keep growing - nothing ever leaves it, so this computes a prefix sum instead.", insight = "Adding the incoming element is half of the update. The other half is removing the one that fell out."),
                    AnswerChoice("c", "window = nums.slice(i - k, i).reduce((a, b) => a + b, 0);", rationale = "Correct results, but re-summing the window each step makes it O(n*k) and throws away the whole point of the pattern.", insight = "The window boundaries are right - it is the cost that is wrong."),
                    AnswerChoice("d", "window += nums[i] - nums[i - k + 1];", rationale = "Off by one: the element leaving a k-wide window ending at i is at index i - k, not i - k + 1.", insight = "You have the right shape for the update - only the departing index is off."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun maxWindowSum(nums: IntArray, k: Int): Int {
                    var window = nums.take(k).sum()
                    var best = window
                    for (i in k until nums.size) {
                        // ___ blank ___
                        best = maxOf(best, window)
                    }
                    return best
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "window += nums[i] - nums[i - k]", rationale = "Correct: add the element entering the window, remove the one leaving it."),
                    AnswerChoice("b", "window += nums[i]", rationale = "The window would keep growing - nothing ever leaves it, so this computes a prefix sum instead.", insight = "Adding the incoming element is half of the update. The other half is removing the one that fell out."),
                    AnswerChoice("c", "window = nums.slice(i - k until i).sum()", rationale = "Correct results, but re-summing the window each step makes it O(n*k) and throws away the whole point of the pattern.", insight = "The window boundaries are right - it is the cost that is wrong."),
                    AnswerChoice("d", "window += nums[i] - nums[i - k + 1]", rationale = "Off by one: the element leaving a k-wide window ending at i is at index i - k, not i - k + 1.", insight = "You have the right shape for the update - only the departing index is off."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                CPP,
                """
                int maxWindowSum(vector<int>& nums, int k) {
                    int window = 0;
                    for (int i = 0; i < k; i++) window += nums[i];
                    int best = window;
                    for (int i = k; i < (int)nums.size(); i++) {
                        // ___ blank ___
                        best = max(best, window);
                    }
                    return best;
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "window += nums[i] - nums[i - k];", rationale = "Correct: add the element entering the window, remove the one leaving it."),
                    AnswerChoice("b", "window += nums[i];", rationale = "The window would keep growing - nothing ever leaves it, so this computes a prefix sum instead.", insight = "Adding the incoming element is half of the update. The other half is removing the one that fell out."),
                    AnswerChoice("c", "window = accumulate(nums.begin() + i - k, nums.begin() + i, 0);", rationale = "Correct results, but re-summing the window each step makes it O(n*k) and throws away the whole point of the pattern.", insight = "The window boundaries are right - it is the cost that is wrong."),
                    AnswerChoice("d", "window += nums[i] - nums[i - k + 1];", rationale = "Off by one: the element leaving a k-wide window ending at i is at index i - k, not i - k + 1.", insight = "You have the right shape for the update - only the departing index is off."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                GO,
                """
                func maxWindowSum(nums []int, k int) int {
                    window := 0
                    for i := 0; i < k; i++ {
                        window += nums[i]
                    }
                    best := window
                    for i := k; i < len(nums); i++ {
                        // ___ blank ___
                        if window > best {
                            best = window
                        }
                    }
                    return best
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "window += nums[i] - nums[i-k]", rationale = "Correct: add the element entering the window, remove the one leaving it."),
                    AnswerChoice("b", "window += nums[i]", rationale = "The window would keep growing - nothing ever leaves it, so this computes a prefix sum instead.", insight = "Adding the incoming element is half of the update. The other half is removing the one that fell out."),
                    AnswerChoice("c", "window = sumRange(nums, i-k, i)", rationale = "Correct results, but re-summing the window each step makes it O(n*k) and throws away the whole point of the pattern.", insight = "The window boundaries are right - it is the cost that is wrong."),
                    AnswerChoice("d", "window += nums[i] - nums[i-k+1]", rationale = "Off by one: the element leaving a k-wide window ending at i is at index i - k, not i - k + 1.", insight = "You have the right shape for the update - only the departing index is off."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                SWIFT,
                """
                func maxWindowSum(_ nums: [Int], _ k: Int) -> Int {
                    var window = nums[0..<k].reduce(0, +)
                    var best = window
                    for i in k..<nums.count {
                        // ___ blank ___
                        best = max(best, window)
                    }
                    return best
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "window += nums[i] - nums[i - k]", rationale = "Correct: add the element entering the window, remove the one leaving it."),
                    AnswerChoice("b", "window += nums[i]", rationale = "The window would keep growing - nothing ever leaves it, so this computes a prefix sum instead.", insight = "Adding the incoming element is half of the update. The other half is removing the one that fell out."),
                    AnswerChoice("c", "window = nums[(i - k)..<i].reduce(0, +)", rationale = "Correct results, but re-summing the window each step makes it O(n*k) and throws away the whole point of the pattern.", insight = "The window boundaries are right - it is the cost that is wrong."),
                    AnswerChoice("d", "window += nums[i] - nums[i - k + 1]", rationale = "Off by one: the element leaving a k-wide window ending at i is at index i - k, not i - k + 1.", insight = "You have the right shape for the update - only the departing index is off."),
                ),
                correctAnswerIds = listOf("a"),
            ),
        ),
    ),

    CodingProblem(
        id = "sliding-window-var-01",
        title = "Longest substring without repeats",
        description = "Find the length of the longest substring with no repeated characters. " +
            "Which approach is best?",
        difficultyRating = 1300,
        primaryTopic = CodingTopic.SLIDING_WINDOW,
        secondaryTopics = listOf(CodingTopic.STRINGS, CodingTopic.HASH_MAPS),
        challengeType = ChallengeType.MULTIPLE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "A variable-size window, shrinking from the left whenever a repeat appears",
                rationale = "Correct: each character enters and leaves the window at most once, " +
                    "so the whole scan is O(n).",
            ),
            AnswerChoice(
                "b", "Check every substring for uniqueness",
                rationale = "There are O(n^2) substrings and checking each costs O(n), so this is " +
                    "O(n^3).",
                insight = "It is the correct definition of the problem - it just enumerates far " +
                    "more than it needs to.",
            ),
            AnswerChoice(
                "c", "A fixed-size window of the alphabet's size",
                rationale = "The answer's length is unknown in advance, so no fixed width works.",
                insight = "You spotted that this is a window problem, which is the hard half. The " +
                    "width is what has to vary.",
            ),
            AnswerChoice(
                "d", "Sort the string, then scan for adjacent duplicates",
                rationale = "Sorting destroys the ordering that makes a substring contiguous.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Grow the window on the right; when the incoming character is already " +
            "inside, move the left edge past its previous occurrence. Both edges only ever move " +
            "forward, which is why the scan stays linear despite the nested-looking logic.",
        bestApproach = "Keep a map from character to its last index and a left edge that jumps " +
            "past any repeat, tracking the widest window seen.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(min(n, alphabet))",
        commonMistakes = listOf(
            "Moving the left edge back when a stale index is found in the map",
            "Measuring the window before removing the duplicate",
        ),
        hints = listOf(
            "The answer's length is not known ahead of time - what does that rule out?",
            "When a repeat arrives, where exactly must the left edge move to?",
            "Store each character's last index so the left edge can jump instead of crawl.",
        ),
        patternId = "sliding-window",
        estimatedSeconds = 60,
    ),

    CodingProblem(
        id = "prefix-sum-01",
        title = "Many range-sum queries",
        description = "You are given a fixed array and must answer 100,000 queries of the form " +
            "\"what is the sum between indices i and j?\". Which pattern fits?",
        difficultyRating = 1150,
        primaryTopic = CodingTopic.ARRAYS,
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "Prefix sums computed once up front",
                rationale = "Correct: O(n) preprocessing turns every query into one subtraction.",
            ),
            AnswerChoice(
                "b", "Sum the range on each query",
                rationale = "Each query costs O(n), so 100,000 queries on a large array is far too " +
                    "slow.",
                insight = "It is the correct answer per query - the issue is doing that work " +
                    "repeatedly rather than once.",
            ),
            AnswerChoice(
                "c", "Sliding window",
                rationale = "Windows suit ranges of a known or monotonically moving width. These " +
                    "queries are arbitrary.",
            ),
            AnswerChoice(
                "d", "Binary search",
                rationale = "There is nothing ordered to search over here.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "When the array never changes and queries are many, pay once to build " +
            "prefix[i] = sum of the first i elements. Then any range sum is prefix[j+1] - prefix[i].",
        bestApproach = "Build a prefix-sum array in one pass and answer each query with a single " +
            "subtraction.",
        timeComplexity = "O(n) preprocessing, O(1) per query",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Off-by-one errors at the range boundaries - decide whether prefix[i] is inclusive and stay consistent",
            "Rebuilding the prefix array after each query",
        ),
        hints = listOf(
            "The array never changes, and there are far more queries than elements.",
            "What could you compute once that makes each query constant time?",
        ),
        patternId = "prefix-sum",
        estimatedSeconds = 45,
    ),

    CodingProblem(
        id = "string-anagram-01",
        title = "Valid anagram",
        description = "Decide whether two strings are anagrams of each other. Which approach is " +
            "strongest?",
        difficultyRating = 1000,
        primaryTopic = CodingTopic.STRINGS,
        secondaryTopics = listOf(CodingTopic.HASH_MAPS),
        challengeType = ChallengeType.MULTIPLE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "Count characters in one string and decrement with the other",
                rationale = "Correct: O(n) time, and the counts must land back at zero everywhere.",
            ),
            AnswerChoice(
                "b", "Sort both strings and compare",
                rationale = "Correct but O(n log n) - fine in an interview as a first answer, " +
                    "though you should then offer the counting version.",
                insight = "This is a perfectly valid solution and worth saying out loud before " +
                    "optimising.",
            ),
            AnswerChoice(
                "c", "Compare the sum of the character codes",
                rationale = "Different multisets can share a sum - \"ad\" and \"bc\" both total " +
                    "the same. This gives false positives.",
                insight = "Reducing to a single comparable value is a good instinct; the reduction " +
                    "just has to be collision-free.",
            ),
            AnswerChoice(
                "d", "Check that both strings contain the same set of characters",
                rationale = "A set ignores multiplicity, so \"aab\" and \"abb\" would compare equal.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Anagram means identical character multisets. Counting captures multiplicity " +
            "exactly, which is what sets and sums both throw away.",
        bestApproach = "Build a frequency map from the first string, decrement it with the second, " +
            "and check that every count ends at zero.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1) for a fixed alphabet",
        commonMistakes = listOf(
            "Forgetting to compare lengths first",
            "Using a set and losing character counts",
        ),
        hints = listOf("What exactly does \"anagram\" mean in terms of character counts?"),
        patternId = "frequency-map",
        estimatedSeconds = 45,
    ),

    CodingProblem(
        id = "edge-case-01",
        title = "Which input breaks this maximum?",
        description = "Which input makes this function behave incorrectly?",
        difficultyRating = 1000,
        primaryTopic = CodingTopic.ARRAYS,
        secondaryTopics = listOf(CodingTopic.DEBUGGING),
        challengeType = ChallengeType.EDGE_CASE,
        codeSnippet = """
            def largest(nums):
                best = 0
                for n in nums:
                    if n > best:
                        best = n
                return best
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "[-5, -2, -9]",
                rationale = "Correct: every value is below the seed of 0, so it returns 0 - a " +
                    "number not even present in the input.",
            ),
            AnswerChoice(
                "b", "[3, 1, 2]",
                rationale = "Returns 3, which is right.",
            ),
            AnswerChoice(
                "c", "[7]",
                rationale = "A single positive element works fine.",
            ),
            AnswerChoice(
                "d", "[0, 0, 0]",
                rationale = "Returns 0, which is genuinely the maximum here.",
                insight = "Reasonable suspicion - the seed value of 0 is exactly the problem, it " +
                    "just happens to be the right answer for this input.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Seeding an accumulator with 0 quietly assumes the answer is non-negative. " +
            "With all-negative input the function returns its seed rather than any real element.",
        bestApproach = "Seed with the first element (and reject or specify behaviour for empty " +
            "input) rather than with a magic constant.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Seeding a maximum with 0 instead of the first element",
            "Not deciding what an empty input should do",
        ),
        hints = listOf(
            "What does the function return if no element ever beats the initial value?",
            "What assumption does starting at 0 quietly make?",
        ),
        patternId = "array-traversal",
        estimatedSeconds = 45,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                static int largest(int[] nums) {
                    int best = 0;
                    for (int n : nums) {
                        if (n > best) best = n;
                    }
                    return best;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function largest(nums) {
                    let best = 0;
                    for (const n of nums) {
                        if (n > best) best = n;
                    }
                    return best;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun largest(nums: IntArray): Int {
                    var best = 0
                    for (n in nums) {
                        if (n > best) best = n
                    }
                    return best
                }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                int largest(vector<int>& nums) {
                    int best = 0;
                    for (int n : nums) {
                        if (n > best) best = n;
                    }
                    return best;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                func largest(nums []int) int {
                    best := 0
                    for _, n := range nums {
                        if n > best {
                            best = n
                        }
                    }
                    return best
                }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                func largest(_ nums: [Int]) -> Int {
                    var best = 0
                    for n in nums {
                        if n > best { best = n }
                    }
                    return best
                }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "output-predict-01",
        title = "Slicing a string",
        description = "What does this print?",
        difficultyRating = 900,
        primaryTopic = CodingTopic.STRINGS,
        challengeType = ChallengeType.OUTPUT_PREDICTION,
        codeSnippet = """
            s = "interview"
            print(s[2:5] + s[-2:])
        """.trimIndent(),
        choices = listOf(
            AnswerChoice("a", "terew", rationale = "Correct: s[2:5] is \"ter\" and s[-2:] is \"ew\"."),
            AnswerChoice(
                "b", "tervew",
                rationale = "That takes s[2:6]. The stop index is exclusive, so index 5 is not included.",
                insight = "The negative slice was read correctly - only the exclusive stop tripped it.",
            ),
            AnswerChoice(
                "c", "terw",
                rationale = "s[-2:] runs to the end of the string, so it is two characters, not one.",
                insight = "The first slice is right; the open-ended second slice is the detail.",
            ),
            AnswerChoice("d", "nteew", rationale = "That would be s[1:4] plus s[-2:]."),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "\"interview\" indexes as i(0) n(1) t(2) e(3) r(4) v(5) i(6) e(7) w(8). " +
            "s[2:5] takes indices 2, 3, 4 = \"ter\"; s[-2:] takes the last two = \"ew\".",
        bestApproach = "Write the string out with its indices before evaluating any slice, and " +
            "remember that the stop bound is exclusive.",
        timeComplexity = "O(k)",
        spaceComplexity = "O(k)",
        commonMistakes = listOf(
            "Treating the slice stop index as inclusive",
            "Reading a negative index as counting from zero rather than from the end",
        ),
        hints = listOf("Index the string on paper first, including the negative positions."),
        patternId = "array-traversal",
        estimatedSeconds = 40,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                String s = "interview";
                System.out.println(s.substring(2, 5) + s.substring(s.length() - 2));
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                const s = "interview";
                console.log(s.slice(2, 5) + s.slice(-2));
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                val s = "interview"
                println(s.substring(2, 5) + s.substring(s.length - 2))
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                string s = "interview";
                cout << s.substr(2, 3) + s.substr(s.size() - 2);
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                s := "interview"
                fmt.Println(s[2:5] + s[len(s)-2:])
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                let s = Array("interview")
                print(String(s[2..<5]) + String(s[(s.count - 2)...]))
                """.trimIndent(),
            ),
        ),
    ),
)
