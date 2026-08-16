package com.codingarena.content

import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.PatternGroup.ARRAYS_HASHING
import com.codingarena.domain.model.PatternGroup.BINARY_SEARCH
import com.codingarena.domain.model.PatternGroup.SLIDING_WINDOW
import com.codingarena.domain.model.PatternGroup.STACK
import com.codingarena.domain.model.PatternGroup.TWO_POINTERS
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.ProgrammingLanguage.CPP
import com.codingarena.domain.model.ProgrammingLanguage.GO
import com.codingarena.domain.model.ProgrammingLanguage.JAVA
import com.codingarena.domain.model.ProgrammingLanguage.JAVASCRIPT
import com.codingarena.domain.model.ProgrammingLanguage.PYTHON
import com.codingarena.domain.model.ProgrammingLanguage.SWIFT
import com.codingarena.domain.model.WorkoutChoice
import com.codingarena.domain.model.WorkoutCodeVariant
import com.codingarena.domain.model.WorkoutStep
import com.codingarena.domain.model.WorkoutStepKind
import com.codingarena.domain.model.WorkoutStepKind.APPROACH
import com.codingarena.domain.model.WorkoutStepKind.BOUNDARY_UPDATE
import com.codingarena.domain.model.WorkoutStepKind.CODE_BLOCK
import com.codingarena.domain.model.WorkoutStepKind.EDGE_CASE
import com.codingarena.domain.model.WorkoutStepKind.PATTERN_RECOGNITION
import com.codingarena.domain.model.WorkoutStepKind.SPACE_COMPLEXITY
import com.codingarena.domain.model.WorkoutStepKind.STATE_SELECTION
import com.codingarena.domain.model.WorkoutStepKind.TIME_COMPLEXITY
import com.codingarena.domain.model.WorkoutStepKind.TRANSFER

/**
 * One problem's Build the Solution decision chain - the per-problem question
 * bank the adaptive round system draws from.
 *
 * [problemSlug] does not have to resolve against [NeetCode150] - this is a
 * standalone reasoning-practice pool, not a roadmap completion tracker, so a
 * problem can be authored here even if it is not (yet) one of the 150.
 */
data class ProblemWorkout(
    val problemSlug: String,
    val group: PatternGroup,
    /** Ordered: PATTERN_RECOGNITION, APPROACH, STATE_SELECTION, BOUNDARY_UPDATE, CODE_BLOCK,
     *  TIME_COMPLEXITY, SPACE_COMPLEXITY, TRANSFER, EDGE_CASE. */
    val steps: List<WorkoutStep>,
)

internal fun step(
    slug: String,
    group: PatternGroup,
    kind: WorkoutStepKind,
    prompt: String,
    conceptKey: String,
    difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    code: String? = null,
    choices: List<WorkoutChoice>,
    languageVariants: List<WorkoutCodeVariant> = emptyList(),
): WorkoutStep = WorkoutStep(
    id = "$slug-${kind.name.lowercase()}-${difficulty.name.lowercase()}",
    problemSlug = slug,
    group = group,
    kind = kind,
    conceptKey = conceptKey,
    difficulty = difficulty,
    prompt = prompt,
    code = code,
    choices = choices,
    languageVariants = languageVariants,
)

internal fun choice(text: String, correct: Boolean, feedback: String, code: String? = null) =
    WorkoutChoice(text, correct, feedback, code)

/**
 * Authored first, per the user's own example, to lock the step format before
 * batch-authoring the rest.
 */
private val minimumSizeSubarraySum = ProblemWorkout(
    problemSlug = "minimum-size-subarray-sum",
    group = SLIDING_WINDOW,
    steps = listOf(
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, PATTERN_RECOGNITION,
            "Given a target sum and an array of positive integers, find the length of the shortest contiguous subarray whose sum is at least target. Which pattern fits?",
            conceptKey = "minimum-size-subarray-sum-pattern-recognition",
            choices = listOf(
                choice(
                    "Sliding window: grow a window from the right, shrink it from the left once it qualifies.",
                    true,
                    "Every value is positive, so the running sum only ever grows as the window grows and only ever shrinks as the window shrinks - that monotonic behavior is exactly what a sliding window needs.",
                ),
                choice(
                    "Two pointers starting at both ends of the array, moving inward.",
                    false,
                    "Starting from both ends assumes some relationship between the first and last elements, but the target subarray could be anywhere in the middle - there's no reason to anchor a pointer at the very end.",
                ),
                choice(
                    "Check every possible subarray's sum with nested loops and keep the shortest that qualifies.",
                    false,
                    "This finds the right answer but recomputes overlapping sums from scratch for every pair of start and end indices, which is far more work than a window that only adds or removes one value at a time.",
                )),
        ),
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, APPROACH,
            "Which approach correctly finds the minimum-length qualifying window?",
            conceptKey = "minimum-size-subarray-sum-approach",
            choices = listOf(
                choice(
                    "Expand the window right by one each step; whenever the running sum is at least target, record the length and shrink from the left until it no longer qualifies.",
                    true,
                    "Shrinking as far as possible while still qualifying, before expanding again, guarantees every recorded length is the shortest window ending at that right edge.",
                ),
                choice(
                    "Expand the window right by one each step; whenever the running sum is at least target, record the length immediately without shrinking.",
                    false,
                    "Without shrinking, the window only ever grows, so it records the length of the first qualifying window rather than searching for a shorter one that also qualifies.",
                ),
                choice(
                    "Fix the window length first, slide it across the array, and increase the length only if no fixed-length window qualifies.",
                    false,
                    "Trying every fixed length from smallest to largest works but re-slides the whole array for each length tried, which is much more work than one pass with a window that resizes itself.",
                )),
        ),
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, STATE_SELECTION,
            "What state does the window need to track as it moves?",
            conceptKey = "minimum-size-subarray-sum-state-selection",
            choices = listOf(
                choice(
                    "A left pointer, the running sum of the current window, and the shortest qualifying length seen so far.",
                    true,
                    "The left pointer marks the window's start, the running sum lets each step update in O(1) instead of re-summing, and the shortest length is the answer being built up.",
                ),
                choice(
                    "A sorted copy of the array, the current index, and a recursion depth counter.",
                    false,
                    "Sorting destroys the array's original order, but the answer specifically depends on contiguous positions in the original sequence - a sorted copy loses that information.",
                ),
                choice(
                    "A set containing every subarray considered so far.",
                    false,
                    "Storing every subarray considered costs far more memory than necessary - the window only ever needs to know its own current boundaries and sum, not a history of every past window.",
                )),
        ),
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, BOUNDARY_UPDATE,
            "Which rule correctly grows and shrinks the window's boundaries?",
            conceptKey = "minimum-size-subarray-sum-boundary-update",
            choices = listOf(
                choice(
                    "Add nums[right] to the sum as the window grows; once the sum is at least target, subtract nums[left] exactly once and move on.",
                    false,
                    "Shrinking only once misses the chance to shrink further when the window still qualifies after removing a single value, so it can report a longer window than the true minimum.",
                ),
                choice(
                    "Add nums[right] to the sum as the window grows; while the sum is still at least target, record the length, subtract nums[left], and move left forward.",
                    true,
                    "Shrinking happens in a while loop, not a single step, because after removing one value the window might still qualify and could shrink further - stopping too early would miss a shorter answer.",
                ),
                choice(
                    "Reset the window's sum to zero and restart from the current right pointer whenever the sum reaches target.",
                    false,
                    "Restarting from scratch throws away the work already done tracking the window and would need to re-scan forward for every qualifying window instead of just shrinking from the left.",
                )),
        ),
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, CODE_BLOCK,
            "Which snippet correctly implements the shrink-while-qualifying loop?",
            conceptKey = "minimum-size-subarray-sum-code-block",
            code = "var left = 0\nvar sum = 0\nvar minLen = Int.MAX_VALUE\nfor (right in nums.indices) {\n    sum += nums[right]\n    // ??? \n}",
            choices = listOf(
                choice(
                    "while (sum >= target) {\n    minLen = minOf(minLen, right - left + 1)\n    sum -= nums[left]\n    left++\n}",
                    true,
                    "The while loop keeps shrinking and recording as long as the window still qualifies, and right - left + 1 correctly counts the window's current length before it shrinks further.",
                    code = "while (sum >= target) {\n    minLen = minOf(minLen, right - left + 1)\n    sum -= nums[left]\n    left++\n}",
                ),
                choice(
                    "if (sum >= target) {\n    minLen = minOf(minLen, right - left + 1)\n    sum -= nums[left]\n    left++\n}",
                    false,
                    "Using if instead of while shrinks by at most one position per outer iteration, which can leave a still-qualifying window unshrunk and report a length longer than the true minimum.",
                    code = "if (sum >= target) {\n    minLen = minOf(minLen, right - left + 1)\n    sum -= nums[left]\n    left++\n}",
                ),
                choice(
                    "while (sum >= target) {\n    sum -= nums[left]\n    left++\n    minLen = minOf(minLen, right - left + 1)\n}",
                    false,
                    "Recording the length after left has already advanced measures the window one position too small, undercounting the true length of the window that actually qualified.",
                    code = "while (sum >= target) {\n    sum -= nums[left]\n    left++\n    minLen = minOf(minLen, right - left + 1)\n}",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "left = 0\nsum_ = 0\nmin_len = float(\"inf\")\nfor right in range(len(nums)):\n    sum_ += nums[right]\n    # ??? \n",
                    choices = listOf(
                        choice(
                            "while sum_ >= target:\n    min_len = min(min_len, right - left + 1)\n    sum_ -= nums[left]\n    left += 1",
                            true,
                            "The while loop keeps shrinking and recording as long as the window still qualifies, and right - left + 1 correctly counts the window's current length before it shrinks further.",
                            code = "while sum_ >= target:\n    min_len = min(min_len, right - left + 1)\n    sum_ -= nums[left]\n    left += 1",
                        ),
                        choice(
                            "if sum_ >= target:\n    min_len = min(min_len, right - left + 1)\n    sum_ -= nums[left]\n    left += 1",
                            false,
                            "Using if instead of while shrinks by at most one position per outer iteration, which can leave a still-qualifying window unshrunk and report a length longer than the true minimum.",
                            code = "if sum_ >= target:\n    min_len = min(min_len, right - left + 1)\n    sum_ -= nums[left]\n    left += 1",
                        ),
                        choice(
                            "while sum_ >= target:\n    sum_ -= nums[left]\n    left += 1\n    min_len = min(min_len, right - left + 1)",
                            false,
                            "Recording the length after left has already advanced measures the window one position too small, undercounting the true length of the window that actually qualified.",
                            code = "while sum_ >= target:\n    sum_ -= nums[left]\n    left += 1\n    min_len = min(min_len, right - left + 1)",
                        ),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "int left = 0;\nint sum = 0;\nint minLen = Integer.MAX_VALUE;\nfor (int right = 0; right < nums.length; right++) {\n    sum += nums[right];\n    // ??? \n}",
                    choices = listOf(
                        choice(
                            "while (sum >= target) {\n    minLen = Math.min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                            true,
                            "The while loop keeps shrinking and recording as long as the window still qualifies, and right - left + 1 correctly counts the window's current length before it shrinks further.",
                            code = "while (sum >= target) {\n    minLen = Math.min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                        ),
                        choice(
                            "if (sum >= target) {\n    minLen = Math.min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                            false,
                            "Using if instead of while shrinks by at most one position per outer iteration, which can leave a still-qualifying window unshrunk and report a length longer than the true minimum.",
                            code = "if (sum >= target) {\n    minLen = Math.min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                        ),
                        choice(
                            "while (sum >= target) {\n    sum -= nums[left];\n    left++;\n    minLen = Math.min(minLen, right - left + 1);\n}",
                            false,
                            "Recording the length after left has already advanced measures the window one position too small, undercounting the true length of the window that actually qualified.",
                            code = "while (sum >= target) {\n    sum -= nums[left];\n    left++;\n    minLen = Math.min(minLen, right - left + 1);\n}",
                        ),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "let left = 0;\nlet sum = 0;\nlet minLen = Infinity;\nfor (let right = 0; right < nums.length; right++) {\n    sum += nums[right];\n    // ??? \n}",
                    choices = listOf(
                        choice(
                            "while (sum >= target) {\n    minLen = Math.min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                            true,
                            "The while loop keeps shrinking and recording as long as the window still qualifies, and right - left + 1 correctly counts the window's current length before it shrinks further.",
                            code = "while (sum >= target) {\n    minLen = Math.min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                        ),
                        choice(
                            "if (sum >= target) {\n    minLen = Math.min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                            false,
                            "Using if instead of while shrinks by at most one position per outer iteration, which can leave a still-qualifying window unshrunk and report a length longer than the true minimum.",
                            code = "if (sum >= target) {\n    minLen = Math.min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                        ),
                        choice(
                            "while (sum >= target) {\n    sum -= nums[left];\n    left++;\n    minLen = Math.min(minLen, right - left + 1);\n}",
                            false,
                            "Recording the length after left has already advanced measures the window one position too small, undercounting the true length of the window that actually qualified.",
                            code = "while (sum >= target) {\n    sum -= nums[left];\n    left++;\n    minLen = Math.min(minLen, right - left + 1);\n}",
                        ),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "int left = 0;\nint sum = 0;\nint minLen = INT_MAX;\nfor (int right = 0; right < (int)nums.size(); right++) {\n    sum += nums[right];\n    // ??? \n}",
                    choices = listOf(
                        choice(
                            "while (sum >= target) {\n    minLen = min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                            true,
                            "The while loop keeps shrinking and recording as long as the window still qualifies, and right - left + 1 correctly counts the window's current length before it shrinks further.",
                            code = "while (sum >= target) {\n    minLen = min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                        ),
                        choice(
                            "if (sum >= target) {\n    minLen = min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                            false,
                            "Using if instead of while shrinks by at most one position per outer iteration, which can leave a still-qualifying window unshrunk and report a length longer than the true minimum.",
                            code = "if (sum >= target) {\n    minLen = min(minLen, right - left + 1);\n    sum -= nums[left];\n    left++;\n}",
                        ),
                        choice(
                            "while (sum >= target) {\n    sum -= nums[left];\n    left++;\n    minLen = min(minLen, right - left + 1);\n}",
                            false,
                            "Recording the length after left has already advanced measures the window one position too small, undercounting the true length of the window that actually qualified.",
                            code = "while (sum >= target) {\n    sum -= nums[left];\n    left++;\n    minLen = min(minLen, right - left + 1);\n}",
                        ),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "left := 0\nsum := 0\nminLen := math.MaxInt32\nfor right := 0; right < len(nums); right++ {\n    sum += nums[right]\n    // ??? \n}",
                    choices = listOf(
                        choice(
                            "for sum >= target {\n    if right-left+1 < minLen {\n        minLen = right - left + 1\n    }\n    sum -= nums[left]\n    left++\n}",
                            true,
                            "The while loop keeps shrinking and recording as long as the window still qualifies, and right - left + 1 correctly counts the window's current length before it shrinks further.",
                            code = "for sum >= target {\n    if right-left+1 < minLen {\n        minLen = right - left + 1\n    }\n    sum -= nums[left]\n    left++\n}",
                        ),
                        choice(
                            "if sum >= target {\n    if right-left+1 < minLen {\n        minLen = right - left + 1\n    }\n    sum -= nums[left]\n    left++\n}",
                            false,
                            "Using if instead of a loop shrinks by at most one position per outer iteration, which can leave a still-qualifying window unshrunk and report a length longer than the true minimum.",
                            code = "if sum >= target {\n    if right-left+1 < minLen {\n        minLen = right - left + 1\n    }\n    sum -= nums[left]\n    left++\n}",
                        ),
                        choice(
                            "for sum >= target {\n    sum -= nums[left]\n    left++\n    if right-left+1 < minLen {\n        minLen = right - left + 1\n    }\n}",
                            false,
                            "Recording the length after left has already advanced measures the window one position too small, undercounting the true length of the window that actually qualified.",
                            code = "for sum >= target {\n    sum -= nums[left]\n    left++\n    if right-left+1 < minLen {\n        minLen = right - left + 1\n    }\n}",
                        ),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var left = 0\nvar sum = 0\nvar minLen = Int.max\nfor right in 0..<nums.count {\n    sum += nums[right]\n    // ??? \n}",
                    choices = listOf(
                        choice(
                            "while sum >= target {\n    minLen = min(minLen, right - left + 1)\n    sum -= nums[left]\n    left += 1\n}",
                            true,
                            "The while loop keeps shrinking and recording as long as the window still qualifies, and right - left + 1 correctly counts the window's current length before it shrinks further.",
                            code = "while sum >= target {\n    minLen = min(minLen, right - left + 1)\n    sum -= nums[left]\n    left += 1\n}",
                        ),
                        choice(
                            "if sum >= target {\n    minLen = min(minLen, right - left + 1)\n    sum -= nums[left]\n    left += 1\n}",
                            false,
                            "Using if instead of while shrinks by at most one position per outer iteration, which can leave a still-qualifying window unshrunk and report a length longer than the true minimum.",
                            code = "if sum >= target {\n    minLen = min(minLen, right - left + 1)\n    sum -= nums[left]\n    left += 1\n}",
                        ),
                        choice(
                            "while sum >= target {\n    sum -= nums[left]\n    left += 1\n    minLen = min(minLen, right - left + 1)\n}",
                            false,
                            "Recording the length after left has already advanced measures the window one position too small, undercounting the true length of the window that actually qualified.",
                            code = "while sum >= target {\n    sum -= nums[left]\n    left += 1\n    minLen = min(minLen, right - left + 1)\n}",
                        ),
                    ),
                ),
            ),
        ),
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, TIME_COMPLEXITY,
            "With n as the array length, what is the time complexity?",
            conceptKey = "minimum-size-subarray-sum-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the window's sum must be checked against a sorted set of targets.",
                    false,
                    "There's no sorting or searching involved - the sum is tracked incrementally and compared directly to a single fixed target value.",
                ),
                choice(
                    "O(n squared), because every right position can trigger a full shrink back to the start.",
                    false,
                    "Left only ever moves forward and never resets, so the total number of times it advances across the entire run is at most n, not n per right position.",
                ),
                choice(
                    "O(n), because left and right each move forward at most n times in total across the whole run.",
                    true,
                    "Even though shrinking happens inside a nested loop, left never moves backward, so the combined movement of both pointers is bounded by n, not n squared.",
                )),
        ),
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, SPACE_COMPLEXITY,
            "How much extra space does this approach use?",
            conceptKey = "minimum-size-subarray-sum-space-complexity",
            choices = listOf(
                choice(
                    "O(1), because only a left pointer, a running sum, and a running minimum are tracked.",
                    true,
                    "Nothing here scales with the size of the input - the same three variables are updated in place as the window slides, regardless of how large the array is.",
                ),
                choice(
                    "O(n), because a prefix-sum array is built before scanning.",
                    false,
                    "No prefix-sum array is needed - the running sum is updated by adding and subtracting single values as the window's boundaries move, not by precomputing sums for every position.",
                ),
                choice(
                    "O(n), because every subarray considered is stored to compare their lengths.",
                    false,
                    "Only the single best length found so far needs to be remembered - there's no need to keep every subarray that was ever considered.",
                )),
        ),
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, TRANSFER,
            "Would this same grow-then-shrink window approach also work for finding the longest substring without repeating characters?",
            conceptKey = "minimum-size-subarray-sum-transfer",
            choices = listOf(
                choice(
                    "Yes - it also grows a window from the right and shrinks from the left whenever a constraint is violated, just tracking distinct characters instead of a sum.",
                    true,
                    "Both problems share the same shape: a window that only ever needs to grow or shrink from its two ends based on a running condition, which is the hallmark of the sliding window pattern.",
                ),
                choice(
                    "No - that problem requires backtracking over every possible substring instead of a moving window.",
                    false,
                    "That problem is actually a textbook sliding window case too - tracking which characters are currently in the window is enough to know when to shrink, no backtracking required.",
                ),
                choice(
                    "No - sliding window only works when every value in the array is a positive number.",
                    false,
                    "The positive-values requirement is specific to using a running sum as the qualifying condition - other sliding window problems use different conditions, like distinct-character counts, that don't need positivity at all.",
                )),
        ),
        step(
            "minimum-size-subarray-sum", SLIDING_WINDOW, EDGE_CASE,
            "Which input would break a version of this solution that assumes a qualifying window always exists?",
            conceptKey = "minimum-size-subarray-sum-edge-case",
            choices = listOf(
                choice(
                    "An array whose total sum is smaller than target, so no subarray - not even the whole array - can ever qualify.",
                    true,
                    "If minLen is never updated because no window ever reaches target, a solution that forgets to check for the untouched Int.MAX_VALUE sentinel would incorrectly return it instead of 0.",
                ),
                choice(
                    "An array containing exactly one element equal to target.",
                    false,
                    "A single element equal to target immediately qualifies as a window of length 1, which is exactly the kind of case the shrink loop handles correctly without any special casing.",
                ),
                choice(
                    "An array where every element is much larger than target.",
                    false,
                    "Oversized elements just mean the shortest qualifying window is length 1, found on the very first expansion - this doesn't expose any bug in the shrink logic.",
                )),
        ),
    ),
)

private val validAnagramWorkout = ProblemWorkout(
    problemSlug = "valid-anagram",
    group = ARRAYS_HASHING,
    steps = listOf(
        step(
            "valid-anagram", ARRAYS_HASHING, PATTERN_RECOGNITION,
            "Given two strings, decide whether the second is an anagram of the first. Which pattern fits?",
            conceptKey = "valid-anagram-pattern-recognition",
            choices = listOf(
                choice(
                    "Frequency map: count how many times each character appears in each string and compare the counts.",
                    true,
                    "An anagram is just a rearrangement, so what matters is not the order of characters but how many of each one appears - a frequency map captures exactly that and nothing more.",
                ),
                choice(
                    "Two pointers walking both strings from opposite ends inward.",
                    false,
                    "Rearranged characters don't line up at mirrored positions the way a palindrome check would need - two pointers from opposite ends has no reason to find matching characters there.",
                ),
                choice(
                    "Binary search each character of one string against the other.",
                    false,
                    "Binary search needs sorted data to search over, and nothing here is sorted going in - counting occurrences directly is simpler and needs no preprocessing.",
                )),
        ),
        step(
            "valid-anagram", ARRAYS_HASHING, APPROACH,
            "Which approach correctly compares the two strings?",
            conceptKey = "valid-anagram-approach",
            choices = listOf(
                choice(
                    "Build one count map by incrementing for each character in the first string and decrementing for each character in the second, then check every count is zero.",
                    true,
                    "Incrementing and decrementing the same map means any character used unevenly between the two strings leaves a nonzero count, which is exactly what marks them as not anagrams.",
                ),
                choice(
                    "Sort both strings alphabetically and compare them for equality.",
                    false,
                    "This also works and is a fine alternative, but it costs O(n log n) for the sort where counting characters only costs O(n) - not the best approach when a linear one is available.",
                ),
                choice(
                    "Compare the strings character by character at each matching index.",
                    false,
                    "Anagrams are rearrangements, so the same characters can appear at completely different indices in each string - comparing index by index would reject valid anagrams.",
                )),
        ),
        step(
            "valid-anagram", ARRAYS_HASHING, STATE_SELECTION,
            "What state is needed to compare the two strings?",
            conceptKey = "valid-anagram-state-selection",
            choices = listOf(
                choice(
                    "A single map from character to a running count, shared across both strings.",
                    true,
                    "Sharing one map and incrementing for one string while decrementing for the other means a perfect anagram always nets back to entirely zero counts.",
                ),
                choice(
                    "Two separate sorted copies of the strings.",
                    false,
                    "Sorting works but needs two full copies and a comparison step afterward - a single shared count map reaches the same answer with less bookkeeping.",
                ),
                choice(
                    "A stack of characters from the first string.",
                    false,
                    "A stack tracks order and last-in-first-out access, neither of which matters here - only how many of each character exist matters, not any ordering.",
                )),
        ),
        step(
            "valid-anagram", ARRAYS_HASHING, BOUNDARY_UPDATE,
            "Which rule correctly updates the shared count map?",
            conceptKey = "valid-anagram-boundary-update",
            choices = listOf(
                choice(
                    "Increment the count for characters in both strings, then check whether every count is even.",
                    false,
                    "Checking for even counts doesn't verify the strings use the *same* characters, only that each one repeats an even number of times combined - two completely different strings could still pass.",
                ),
                choice(
                    "Increment the count for each character seen in the first string, decrement for each character seen in the second string.",
                    true,
                    "This nets every character that appears in both strings back toward zero, leaving only characters that are unevenly used between the two strings with a nonzero count.",
                ),
                choice(
                    "Increment the count for the first string, then reset the whole map before processing the second string.",
                    false,
                    "Resetting the map throws away the first string's counts entirely, leaving nothing to compare the second string's counts against.",
                )),
        ),
        step(
            "valid-anagram", ARRAYS_HASHING, CODE_BLOCK,
            "Which snippet correctly finishes the anagram check?",
            conceptKey = "valid-anagram-code-block",
            code = "if (s.length != t.length) return false\nval counts = IntArray(26)\nfor (c in s) counts[c - 'a']++\n// ???\nreturn counts.all { it == 0 }",
            choices = listOf(
                choice(
                    "for (c in t) counts[c - 'a']--",
                    true,
                    "Decrementing for every character in t undoes exactly the increments from s when the two strings use the same characters the same number of times, leaving every count at zero.",
                    code = "for (c in t) counts[c - 'a']--",
                ),
                choice(
                    "for (c in t) counts[c - 'a']++",
                    false,
                    "Incrementing for both strings only ever adds up total occurrences - it can never distinguish an anagram from two strings that merely share a length and character set.",
                    code = "for (c in t) counts[c - 'a']++",
                ),
                choice(
                    "for (c in s) counts[c - 'a']--",
                    false,
                    "This decrements using s again instead of t, so it only ever resets s's own counts back to zero and never actually reads anything from t at all.",
                    code = "for (c in s) counts[c - 'a']--",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "if len(s) != len(t): return False\ncounts = [0] * 26\nfor c in s: counts[ord(c) - ord(\"a\")] += 1\n# ???\nreturn all(x == 0 for x in counts)",
                    choices = listOf(
                        choice("for c in t: counts[ord(c) - ord(\"a\")] -= 1", true, "Decrementing for every character in t undoes exactly the increments from s when the two strings use the same characters the same number of times, leaving every count at zero.", code = "for c in t: counts[ord(c) - ord(\"a\")] -= 1"),
                        choice("for c in t: counts[ord(c) - ord(\"a\")] += 1", false, "Incrementing for both strings only ever adds up total occurrences - it can never distinguish an anagram from two strings that merely share a length and character set.", code = "for c in t: counts[ord(c) - ord(\"a\")] += 1"),
                        choice("for c in s: counts[ord(c) - ord(\"a\")] -= 1", false, "This decrements using s again instead of t, so it only ever resets s's own counts back to zero and never actually reads anything from t at all.", code = "for c in s: counts[ord(c) - ord(\"a\")] -= 1"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "if (s.length() != t.length()) return false;\nint[] counts = new int[26];\nfor (char c : s.toCharArray()) counts[c - 'a']++;\n// ???\nfor (int x : counts) if (x != 0) return false;\nreturn true;",
                    choices = listOf(
                        choice("for (char c : t.toCharArray()) counts[c - 'a']--;", true, "Decrementing for every character in t undoes exactly the increments from s when the two strings use the same characters the same number of times, leaving every count at zero.", code = "for (char c : t.toCharArray()) counts[c - 'a']--;"),
                        choice("for (char c : t.toCharArray()) counts[c - 'a']++;", false, "Incrementing for both strings only ever adds up total occurrences - it can never distinguish an anagram from two strings that merely share a length and character set.", code = "for (char c : t.toCharArray()) counts[c - 'a']++;"),
                        choice("for (char c : s.toCharArray()) counts[c - 'a']--;", false, "This decrements using s again instead of t, so it only ever resets s's own counts back to zero and never actually reads anything from t at all.", code = "for (char c : s.toCharArray()) counts[c - 'a']--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "if (s.length !== t.length) return false;\nconst counts = new Array(26).fill(0);\nfor (const c of s) counts[c.charCodeAt(0) - 97]++;\n// ???\nreturn counts.every(x => x === 0);",
                    choices = listOf(
                        choice("for (const c of t) counts[c.charCodeAt(0) - 97]--;", true, "Decrementing for every character in t undoes exactly the increments from s when the two strings use the same characters the same number of times, leaving every count at zero.", code = "for (const c of t) counts[c.charCodeAt(0) - 97]--;"),
                        choice("for (const c of t) counts[c.charCodeAt(0) - 97]++;", false, "Incrementing for both strings only ever adds up total occurrences - it can never distinguish an anagram from two strings that merely share a length and character set.", code = "for (const c of t) counts[c.charCodeAt(0) - 97]++;"),
                        choice("for (const c of s) counts[c.charCodeAt(0) - 97]--;", false, "This decrements using s again instead of t, so it only ever resets s's own counts back to zero and never actually reads anything from t at all.", code = "for (const c of s) counts[c.charCodeAt(0) - 97]--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "if (s.size() != t.size()) return false;\nint counts[26] = {0};\nfor (char c : s) counts[c - 'a']++;\n// ???\nfor (int x : counts) if (x != 0) return false;\nreturn true;",
                    choices = listOf(
                        choice("for (char c : t) counts[c - 'a']--;", true, "Decrementing for every character in t undoes exactly the increments from s when the two strings use the same characters the same number of times, leaving every count at zero.", code = "for (char c : t) counts[c - 'a']--;"),
                        choice("for (char c : t) counts[c - 'a']++;", false, "Incrementing for both strings only ever adds up total occurrences - it can never distinguish an anagram from two strings that merely share a length and character set.", code = "for (char c : t) counts[c - 'a']++;"),
                        choice("for (char c : s) counts[c - 'a']--;", false, "This decrements using s again instead of t, so it only ever resets s's own counts back to zero and never actually reads anything from t at all.", code = "for (char c : s) counts[c - 'a']--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "if len(s) != len(t) {\n    return false\n}\nvar counts [26]int\nfor _, c := range s {\n    counts[c-'a']++\n}\n// ???\nfor _, x := range counts {\n    if x != 0 {\n        return false\n    }\n}\nreturn true",
                    choices = listOf(
                        choice("for _, c := range t {\n    counts[c-'a']--\n}", true, "Decrementing for every character in t undoes exactly the increments from s when the two strings use the same characters the same number of times, leaving every count at zero.", code = "for _, c := range t {\n    counts[c-'a']--\n}"),
                        choice("for _, c := range t {\n    counts[c-'a']++\n}", false, "Incrementing for both strings only ever adds up total occurrences - it can never distinguish an anagram from two strings that merely share a length and character set.", code = "for _, c := range t {\n    counts[c-'a']++\n}"),
                        choice("for _, c := range s {\n    counts[c-'a']--\n}", false, "This decrements using s again instead of t, so it only ever resets s's own counts back to zero and never actually reads anything from t at all.", code = "for _, c := range s {\n    counts[c-'a']--\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "if s.count != t.count { return false }\nvar counts = [Int](repeating: 0, count: 26)\nfor c in s { counts[Int(c.asciiValue! - 97)] += 1 }\n// ???\nreturn counts.allSatisfy { \$0 == 0 }",
                    choices = listOf(
                        choice("for c in t { counts[Int(c.asciiValue! - 97)] -= 1 }", true, "Decrementing for every character in t undoes exactly the increments from s when the two strings use the same characters the same number of times, leaving every count at zero.", code = "for c in t { counts[Int(c.asciiValue! - 97)] -= 1 }"),
                        choice("for c in t { counts[Int(c.asciiValue! - 97)] += 1 }", false, "Incrementing for both strings only ever adds up total occurrences - it can never distinguish an anagram from two strings that merely share a length and character set.", code = "for c in t { counts[Int(c.asciiValue! - 97)] += 1 }"),
                        choice("for c in s { counts[Int(c.asciiValue! - 97)] -= 1 }", false, "This decrements using s again instead of t, so it only ever resets s's own counts back to zero and never actually reads anything from t at all.", code = "for c in s { counts[Int(c.asciiValue! - 97)] -= 1 }"),
                    ),
                ),
            ),
        ),
        step(
            "valid-anagram", ARRAYS_HASHING, TIME_COMPLEXITY,
            "With n as the length of the strings, what is the time complexity?",
            conceptKey = "valid-anagram-time-complexity",
            choices = listOf(
                choice(
                    "O(n squared), because every character in one string is compared against every character in the other.",
                    false,
                    "Nothing here does a pairwise comparison - each character only ever updates its own slot in the count map in constant time.",
                ),
                choice(
                    "O(n log n), because the strings must be sorted before comparing.",
                    false,
                    "Counting characters never sorts anything - it's strictly faster than the sort-and-compare alternative, not equal to it.",
                ),
                choice(
                    "O(n), because each string is scanned once to update the shared count map.",
                    true,
                    "Both the increment pass and the decrement pass touch each character exactly once, and checking the final counts is a fixed 26 entries, so the whole thing scales linearly with n.",
                )),
        ),
        step(
            "valid-anagram", ARRAYS_HASHING, SPACE_COMPLEXITY,
            "How much extra space does this approach use?",
            conceptKey = "valid-anagram-space-complexity",
            choices = listOf(
                choice(
                    "O(1), because the count map has a fixed size of 26 regardless of how long the strings are.",
                    true,
                    "With only lowercase English letters possible, the map never grows past 26 entries no matter how long s and t get, so it counts as constant extra space.",
                ),
                choice(
                    "O(n), because a copy of each string is stored to sort them.",
                    false,
                    "No copies or sorted versions of the strings are stored - only a small fixed-size array of counts is kept.",
                ),
                choice(
                    "O(n), because the count map grows by one entry per character processed.",
                    false,
                    "The map's size is bounded by the alphabet, 26 possible letters, not by how many characters are processed - repeated letters update existing slots rather than adding new ones.",
                )),
        ),
        step(
            "valid-anagram", ARRAYS_HASHING, TRANSFER,
            "Would this same shared count-map approach also work for grouping a list of strings into anagram groups?",
            conceptKey = "valid-anagram-transfer",
            choices = listOf(
                choice(
                    "Yes - the same character counts (or a sorted string) can serve as a key to group every anagram of each other under one bucket.",
                    true,
                    "Instead of comparing two strings directly, using each string's character counts as a map key lets every anagram of a word land in the same group automatically.",
                ),
                choice(
                    "No - grouping many strings requires comparing every pair directly, which this approach can't do.",
                    false,
                    "Comparing every pair directly would be far more work than necessary - using counts as a group key avoids pairwise comparisons entirely, the same efficiency win as the two-string case.",
                ),
                choice(
                    "No - frequency counting only works for exactly two strings at a time.",
                    false,
                    "There's nothing two-string-specific about counting characters - the same counts can be computed for any number of strings and used to group them.",
                )),
        ),
        step(
            "valid-anagram", ARRAYS_HASHING, EDGE_CASE,
            "Which input would break a version of this solution that skips the length check?",
            conceptKey = "valid-anagram-edge-case",
            choices = listOf(
                choice(
                    "Strings of different lengths where the shorter one's characters are a subset of the longer one's.",
                    true,
                    "Without checking lengths first, a shorter string that only uses characters also present in a longer string could still net every count to zero for its own characters, wrongly passing as an anagram of a longer string.",
                ),
                choice(
                    "Two empty strings.",
                    false,
                    "Two empty strings trivially net every count to zero and correctly count as anagrams of each other - this doesn't expose any bug.",
                ),
                choice(
                    "Two identical strings.",
                    false,
                    "Identical strings are the simplest possible anagram case and net every count to zero exactly as expected - nothing breaks here.",
                )),
        ),
    ),
)

private val threeSumWorkout = ProblemWorkout(
    problemSlug = "3sum",
    group = TWO_POINTERS,
    steps = listOf(
        step(
            "3sum", TWO_POINTERS, PATTERN_RECOGNITION,
            "Given an integer array, find all unique triplets that sum to zero. Which pattern fits, after an initial sort?",
            conceptKey = "3sum-pattern-recognition",
            choices = listOf(
                choice(
                    "Two pointers: fix one number, then use two pointers from both ends of the remaining sorted range to find pairs that complete the sum.",
                    true,
                    "Once the array is sorted, fixing one value turns the problem into 'find two numbers that sum to a target' in a sorted range - exactly what two pointers closing inward solves in linear time.",
                ),
                choice(
                    "Sliding window across the sorted array looking for three consecutive values that sum to zero.",
                    false,
                    "The three numbers in a valid triplet don't need to be consecutive in the array - a window that only ever looks at adjacent elements would miss almost every valid triplet.",
                ),
                choice(
                    "Try every possible triplet with three nested loops.",
                    false,
                    "This finds every triplet correctly but costs O(n cubed), far more than the O(n squared) that fixing one value and using two pointers on the rest achieves.",
                )),
        ),
        step(
            "3sum", TWO_POINTERS, APPROACH,
            "Which approach correctly finds every unique triplet?",
            conceptKey = "3sum-approach",
            choices = listOf(
                choice(
                    "Sort the array; for each index i, use two pointers starting just after i and at the end to find pairs summing to -nums[i], skipping duplicate values to avoid repeated triplets.",
                    true,
                    "Sorting groups equal values together, which makes both 'find a pair summing to a target' solvable with two pointers and 'skip values equal to the one just tried' straightforward for avoiding duplicates.",
                ),
                choice(
                    "Sort the array; for each index i, use two pointers but do not skip any duplicate values.",
                    false,
                    "Without skipping duplicates, the same triplet found in a different position gets recorded again, producing repeated triplets when the problem asks for unique ones only.",
                ),
                choice(
                    "Use a hash set of all values, then for each pair check whether the negative of their sum exists in the set.",
                    false,
                    "This can work, but avoiding duplicate triplets is considerably trickier with a hash-set approach than with the sorted two-pointer approach, where equal values sit next to each other and are easy to skip.",
                )),
        ),
        step(
            "3sum", TWO_POINTERS, STATE_SELECTION,
            "What state does the two-pointer sweep for each fixed value need?",
            conceptKey = "3sum-state-selection",
            choices = listOf(
                choice(
                    "A left pointer just after the fixed index and a right pointer at the array's end, moving toward each other.",
                    true,
                    "In a sorted array, moving left forward increases the pair's sum and moving right backward decreases it, so these two pointers can search the entire remaining range in one linear pass.",
                ),
                choice(
                    "A hash map from value to how many times it has been used so far.",
                    false,
                    "A usage-count map is unnecessary once the array is sorted - the two-pointer sweep naturally handles finding pairs without needing to track how many times each value has appeared.",
                ),
                choice(
                    "A separate sorted copy of the array for every fixed index.",
                    false,
                    "The array only needs to be sorted once at the very start - re-sorting a fresh copy for every fixed index would repeat the same work n times over for no benefit.",
                )),
        ),
        step(
            "3sum", TWO_POINTERS, BOUNDARY_UPDATE,
            "Which rule correctly moves the two pointers based on the current sum?",
            conceptKey = "3sum-boundary-update",
            choices = listOf(
                choice(
                    "If the sum is too small, move right backward; if too large, move left forward.",
                    false,
                    "This reverses the two pointers' effects - moving right backward actually decreases the sum further, moving it away from a too-small target instead of toward it.",
                ),
                choice(
                    "If the sum is too small, move left forward; if too large, move right backward; if it matches, record the triplet and move both pointers inward, skipping duplicates.",
                    true,
                    "Because the array is sorted, moving left forward strictly increases the sum and moving right backward strictly decreases it, so this rule always moves toward the target sum without ever missing a pair.",
                ),
                choice(
                    "Always move both pointers inward by one on every step, regardless of the current sum.",
                    false,
                    "Moving both pointers regardless of the sum can step right past the pair that would have summed correctly, since neither pointer is responding to whether the sum needs to grow or shrink.",
                )),
        ),
        step(
            "3sum", TWO_POINTERS, CODE_BLOCK,
            "Which snippet correctly implements the inner two-pointer sweep for a fixed index i?",
            conceptKey = "3sum-code-block",
            code = "var left = i + 1\nvar right = nums.size - 1\nwhile (left < right) {\n    val sum = nums[i] + nums[left] + nums[right]\n    // ???\n}",
            choices = listOf(
                choice(
                    "if (sum < 0) left++ else if (sum > 0) right-- else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right-- }",
                    true,
                    "This correctly grows the sum by moving left forward when it's too small, shrinks it by moving right backward when it's too large, and advances both pointers past the match once one is recorded.",
                    code = "if (sum < 0) left++ else if (sum > 0) right-- else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right-- }",
                ),
                choice(
                    "if (sum < 0) right-- else if (sum > 0) left++ else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right-- }",
                    false,
                    "This swaps the two directions - moving right backward when the sum is already too small pushes it even lower instead of toward zero.",
                    code = "if (sum < 0) right-- else if (sum > 0) left++ else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right-- }",
                ),
                choice(
                    "if (sum == 0) { result.add(listOf(nums[i], nums[left], nums[right])) }; left++; right--",
                    false,
                    "Moving both pointers on every iteration regardless of the sum, rather than only after a match, can skip past pairs that would have summed correctly before they're ever checked.",
                    code = "if (sum == 0) { result.add(listOf(nums[i], nums[left], nums[right])) }; left++; right--",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "left = i + 1\nright = len(nums) - 1\nwhile left < right:\n    total = nums[i] + nums[left] + nums[right]\n    # ???",
                    choices = listOf(
                        choice("if total < 0:\n    left += 1\nelif total > 0:\n    right -= 1\nelse:\n    result.append([nums[i], nums[left], nums[right]])\n    left += 1\n    right -= 1", true, "This correctly grows the sum by moving left forward when it's too small, shrinks it by moving right backward when it's too large, and advances both pointers past the match once one is recorded.", code = "if total < 0:\n    left += 1\nelif total > 0:\n    right -= 1\nelse:\n    result.append([nums[i], nums[left], nums[right]])\n    left += 1\n    right -= 1"),
                        choice("if total < 0:\n    right -= 1\nelif total > 0:\n    left += 1\nelse:\n    result.append([nums[i], nums[left], nums[right]])\n    left += 1\n    right -= 1", false, "This swaps the two directions - moving right backward when the sum is already too small pushes it even lower instead of toward zero.", code = "if total < 0:\n    right -= 1\nelif total > 0:\n    left += 1\nelse:\n    result.append([nums[i], nums[left], nums[right]])\n    left += 1\n    right -= 1"),
                        choice("if total == 0:\n    result.append([nums[i], nums[left], nums[right]])\nleft += 1\nright -= 1", false, "Moving both pointers on every iteration regardless of the sum, rather than only after a match, can skip past pairs that would have summed correctly before they're ever checked.", code = "if total == 0:\n    result.append([nums[i], nums[left], nums[right]])\nleft += 1\nright -= 1"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "int left = i + 1;\nint right = nums.length - 1;\nwhile (left < right) {\n    int sum = nums[i] + nums[left] + nums[right];\n    // ???\n}",
                    choices = listOf(
                        choice("if (sum < 0) left++; else if (sum > 0) right--; else { result.add(List.of(nums[i], nums[left], nums[right])); left++; right--; }", true, "This correctly grows the sum by moving left forward when it's too small, shrinks it by moving right backward when it's too large, and advances both pointers past the match once one is recorded.", code = "if (sum < 0) left++; else if (sum > 0) right--; else { result.add(List.of(nums[i], nums[left], nums[right])); left++; right--; }"),
                        choice("if (sum < 0) right--; else if (sum > 0) left++; else { result.add(List.of(nums[i], nums[left], nums[right])); left++; right--; }", false, "This swaps the two directions - moving right backward when the sum is already too small pushes it even lower instead of toward zero.", code = "if (sum < 0) right--; else if (sum > 0) left++; else { result.add(List.of(nums[i], nums[left], nums[right])); left++; right--; }"),
                        choice("if (sum == 0) { result.add(List.of(nums[i], nums[left], nums[right])); } left++; right--;", false, "Moving both pointers on every iteration regardless of the sum, rather than only after a match, can skip past pairs that would have summed correctly before they're ever checked.", code = "if (sum == 0) { result.add(List.of(nums[i], nums[left], nums[right])); } left++; right--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "let left = i + 1;\nlet right = nums.length - 1;\nwhile (left < right) {\n    const sum = nums[i] + nums[left] + nums[right];\n    // ???\n}",
                    choices = listOf(
                        choice("if (sum < 0) left++; else if (sum > 0) right--; else { result.push([nums[i], nums[left], nums[right]]); left++; right--; }", true, "This correctly grows the sum by moving left forward when it's too small, shrinks it by moving right backward when it's too large, and advances both pointers past the match once one is recorded.", code = "if (sum < 0) left++; else if (sum > 0) right--; else { result.push([nums[i], nums[left], nums[right]]); left++; right--; }"),
                        choice("if (sum < 0) right--; else if (sum > 0) left++; else { result.push([nums[i], nums[left], nums[right]]); left++; right--; }", false, "This swaps the two directions - moving right backward when the sum is already too small pushes it even lower instead of toward zero.", code = "if (sum < 0) right--; else if (sum > 0) left++; else { result.push([nums[i], nums[left], nums[right]]); left++; right--; }"),
                        choice("if (sum === 0) { result.push([nums[i], nums[left], nums[right]]); } left++; right--;", false, "Moving both pointers on every iteration regardless of the sum, rather than only after a match, can skip past pairs that would have summed correctly before they're ever checked.", code = "if (sum === 0) { result.push([nums[i], nums[left], nums[right]]); } left++; right--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "int left = i + 1;\nint right = nums.size() - 1;\nwhile (left < right) {\n    int sum = nums[i] + nums[left] + nums[right];\n    // ???\n}",
                    choices = listOf(
                        choice("if (sum < 0) left++; else if (sum > 0) right--; else { result.push_back({nums[i], nums[left], nums[right]}); left++; right--; }", true, "This correctly grows the sum by moving left forward when it's too small, shrinks it by moving right backward when it's too large, and advances both pointers past the match once one is recorded.", code = "if (sum < 0) left++; else if (sum > 0) right--; else { result.push_back({nums[i], nums[left], nums[right]}); left++; right--; }"),
                        choice("if (sum < 0) right--; else if (sum > 0) left++; else { result.push_back({nums[i], nums[left], nums[right]}); left++; right--; }", false, "This swaps the two directions - moving right backward when the sum is already too small pushes it even lower instead of toward zero.", code = "if (sum < 0) right--; else if (sum > 0) left++; else { result.push_back({nums[i], nums[left], nums[right]}); left++; right--; }"),
                        choice("if (sum == 0) { result.push_back({nums[i], nums[left], nums[right]}); } left++; right--;", false, "Moving both pointers on every iteration regardless of the sum, rather than only after a match, can skip past pairs that would have summed correctly before they're ever checked.", code = "if (sum == 0) { result.push_back({nums[i], nums[left], nums[right]}); } left++; right--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "left := i + 1\nright := len(nums) - 1\nfor left < right {\n    sum := nums[i] + nums[left] + nums[right]\n    // ???\n}",
                    choices = listOf(
                        choice("if sum < 0 {\n    left++\n} else if sum > 0 {\n    right--\n} else {\n    result = append(result, []int{nums[i], nums[left], nums[right]})\n    left++\n    right--\n}", true, "This correctly grows the sum by moving left forward when it's too small, shrinks it by moving right backward when it's too large, and advances both pointers past the match once one is recorded.", code = "if sum < 0 {\n    left++\n} else if sum > 0 {\n    right--\n} else {\n    result = append(result, []int{nums[i], nums[left], nums[right]})\n    left++\n    right--\n}"),
                        choice("if sum < 0 {\n    right--\n} else if sum > 0 {\n    left++\n} else {\n    result = append(result, []int{nums[i], nums[left], nums[right]})\n    left++\n    right--\n}", false, "This swaps the two directions - moving right backward when the sum is already too small pushes it even lower instead of toward zero.", code = "if sum < 0 {\n    right--\n} else if sum > 0 {\n    left++\n} else {\n    result = append(result, []int{nums[i], nums[left], nums[right]})\n    left++\n    right--\n}"),
                        choice("if sum == 0 {\n    result = append(result, []int{nums[i], nums[left], nums[right]})\n}\nleft++\nright--", false, "Moving both pointers on every iteration regardless of the sum, rather than only after a match, can skip past pairs that would have summed correctly before they're ever checked.", code = "if sum == 0 {\n    result = append(result, []int{nums[i], nums[left], nums[right]})\n}\nleft++\nright--"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var left = i + 1\nvar right = nums.count - 1\nwhile left < right {\n    let sum = nums[i] + nums[left] + nums[right]\n    // ???\n}",
                    choices = listOf(
                        choice("if sum < 0 { left += 1 } else if sum > 0 { right -= 1 } else { result.append([nums[i], nums[left], nums[right]]); left += 1; right -= 1 }", true, "This correctly grows the sum by moving left forward when it's too small, shrinks it by moving right backward when it's too large, and advances both pointers past the match once one is recorded.", code = "if sum < 0 { left += 1 } else if sum > 0 { right -= 1 } else { result.append([nums[i], nums[left], nums[right]]); left += 1; right -= 1 }"),
                        choice("if sum < 0 { right -= 1 } else if sum > 0 { left += 1 } else { result.append([nums[i], nums[left], nums[right]]); left += 1; right -= 1 }", false, "This swaps the two directions - moving right backward when the sum is already too small pushes it even lower instead of toward zero.", code = "if sum < 0 { right -= 1 } else if sum > 0 { left += 1 } else { result.append([nums[i], nums[left], nums[right]]); left += 1; right -= 1 }"),
                        choice("if sum == 0 { result.append([nums[i], nums[left], nums[right]]) }\nleft += 1\nright -= 1", false, "Moving both pointers on every iteration regardless of the sum, rather than only after a match, can skip past pairs that would have summed correctly before they're ever checked.", code = "if sum == 0 { result.append([nums[i], nums[left], nums[right]]) }\nleft += 1\nright -= 1"),
                    ),
                ),
            ),
        ),
        step(
            "3sum", TWO_POINTERS, TIME_COMPLEXITY,
            "With n as the array length, what is the time complexity?",
            conceptKey = "3sum-time-complexity",
            choices = listOf(
                choice(
                    "O(n), because sorting takes constant time relative to the sweep.",
                    false,
                    "Sorting alone already costs O(n log n), and the outer loop over fixed indices combined with each inner sweep brings the total to O(n squared), well above linear.",
                ),
                choice(
                    "O(n cubed), because three nested loops are needed to check every triplet.",
                    false,
                    "The two-pointer sweep replaces the innermost loop entirely - only one index is fixed with a loop, and the pair search happens in one linear pass, not a nested loop.",
                ),
                choice(
                    "O(n squared), because for each of the n fixed indices, the two-pointer sweep does at most O(n) work.",
                    true,
                    "Sorting costs O(n log n) up front, and then n fixed indices, each running a two-pointer sweep that's linear in the remaining range, multiply out to O(n squared) total.",
                )),
        ),
        step(
            "3sum", TWO_POINTERS, SPACE_COMPLEXITY,
            "How much extra space does this approach use, not counting the output list?",
            conceptKey = "3sum-space-complexity",
            choices = listOf(
                choice(
                    "O(log n) to O(n), for the sort's own internal space, plus O(1) for the two pointers themselves.",
                    true,
                    "The pointers and running sum use only a handful of variables, so the dominant cost is whatever the sorting algorithm itself needs internally, which varies by implementation but is never more than linear.",
                ),
                choice(
                    "O(n) for a hash set tracking every triplet seen so far.",
                    false,
                    "No hash set of triplets is needed - duplicates are avoided by skipping over equal adjacent values in the already-sorted array, not by tracking previously seen combinations.",
                ),
                choice(
                    "O(n squared) for a table of every pair's sum.",
                    false,
                    "No table of pair sums is precomputed or stored - each pair's sum is computed on the fly from the current left and right pointers and immediately discarded.",
                )),
        ),
        step(
            "3sum", TWO_POINTERS, TRANSFER,
            "Would this same fix-one-value-then-two-pointer approach also work for finding the pair in a sorted array closest to a given target?",
            conceptKey = "3sum-transfer",
            choices = listOf(
                choice(
                    "Yes - two pointers from both ends of a sorted array, moving based on whether the sum is too high or too low, applies directly without needing to fix an extra value.",
                    true,
                    "That's actually a simpler version of the same core idea - two pointers narrowing toward a target sum in a sorted array - just without the outer loop that fixes one of three values.",
                ),
                choice(
                    "No - finding a closest pair requires checking every pair directly since 'closest' isn't a fixed target.",
                    false,
                    "Two pointers can still narrow in on the closest sum by tracking the best difference seen so far as they move, the same way they narrow toward an exact target - no need to check every pair.",
                ),
                choice(
                    "No - two pointers only work when searching for triplets, not pairs.",
                    false,
                    "Two pointers converging from both ends of a sorted array is fundamentally a pair-finding technique - the triplet version here just wraps it in an outer loop that fixes one extra value.",
                )),
        ),
        step(
            "3sum", TWO_POINTERS, EDGE_CASE,
            "Which input would break a version of this solution that forgets to skip duplicate values for the fixed index?",
            conceptKey = "3sum-edge-case",
            choices = listOf(
                choice(
                    "An array with repeated values, like [-1, -1, 0, 1, 1], where the same triplet can be found starting from two different but equal fixed indices.",
                    true,
                    "Without skipping to the next distinct value after processing a fixed index, the identical triplet gets rediscovered from a duplicate index and added to the result twice, violating the uniqueness requirement.",
                ),
                choice(
                    "An array where every value is distinct.",
                    false,
                    "With no repeated values at all, there's no duplicate index to skip in the first place, so this case can't expose a missing duplicate-skip bug.",
                ),
                choice(
                    "An array with fewer than three elements.",
                    false,
                    "Too few elements to form any triplet just means the result is empty - this is a simple size check, not something that depends on duplicate handling.",
                )),
        ),
    ),
)

private val validParenthesesWorkout = ProblemWorkout(
    problemSlug = "valid-parentheses",
    group = STACK,
    steps = listOf(
        step(
            "valid-parentheses", STACK, PATTERN_RECOGNITION,
            "Given a string of brackets, decide whether every opening bracket is closed by the same type in the correct order. Which pattern fits?",
            conceptKey = "valid-parentheses-pattern-recognition",
            choices = listOf(
                choice(
                    "Stack: push every opening bracket, and pop to check a match whenever a closing bracket appears.",
                    true,
                    "A closing bracket must always match the most recently opened, still-unclosed bracket - that 'most recent first' requirement is exactly what a stack provides.",
                ),
                choice(
                    "Two pointers scanning from both ends of the string inward.",
                    false,
                    "Brackets don't need to mirror around the string's center the way a palindrome would - the correct match for a closing bracket depends on nesting order, not position from either end.",
                ),
                choice(
                    "Count the total number of opening and closing brackets and compare the totals.",
                    false,
                    "Equal totals don't guarantee correct nesting - '())(' has equal counts of each bracket but is not validly nested, so counts alone can't catch this.",
                )),
        ),
        step(
            "valid-parentheses", STACK, APPROACH,
            "Which approach correctly validates the bracket string?",
            conceptKey = "valid-parentheses-approach",
            choices = listOf(
                choice(
                    "Push every opening bracket onto a stack; for each closing bracket, pop the stack and check it matches, failing immediately if the stack is empty or the types don't match; succeed only if the stack is empty at the end.",
                    true,
                    "Checking both that a pop is possible and that its type matches, plus requiring an empty stack at the end, together rule out unmatched closes, wrong nesting order, and leftover unclosed opens.",
                ),
                choice(
                    "Push every opening bracket onto a stack; for each closing bracket, pop the stack without checking the type, and succeed if the string's length is even.",
                    false,
                    "Skipping the type check would accept mismatched pairs like '(]' as valid, and checking length alone can't detect nesting problems at all.",
                ),
                choice(
                    "Remove matching adjacent pairs like '()' or '[]' repeatedly until no more can be removed, then check if the string is empty.",
                    false,
                    "This can work in principle, but repeatedly re-scanning the shrinking string for adjacent pairs is far less efficient than a single pass with a stack.",
                )),
        ),
        step(
            "valid-parentheses", STACK, STATE_SELECTION,
            "What state does this approach need?",
            conceptKey = "valid-parentheses-state-selection",
            choices = listOf(
                choice(
                    "A stack holding the opening brackets seen so far, in the order they appeared.",
                    true,
                    "The stack's top is always the most recently opened bracket, which is exactly the one any new closing bracket needs to match against first.",
                ),
                choice(
                    "A count of each bracket type seen so far.",
                    false,
                    "Counts alone can't detect ordering problems - a string could have equal counts of every bracket type and still be invalidly nested.",
                ),
                choice(
                    "The index of the first opening bracket in the string.",
                    false,
                    "Only tracking the very first opening bracket's position loses track of every other bracket that's currently open and unmatched.",
                )),
        ),
        step(
            "valid-parentheses", STACK, BOUNDARY_UPDATE,
            "Which rule correctly handles each character while scanning left to right?",
            conceptKey = "valid-parentheses-boundary-update",
            choices = listOf(
                choice(
                    "If it's an opening bracket, push it; if it's a closing bracket, push it too, and check the whole stack for matches at the end.",
                    false,
                    "Pushing closing brackets as well loses the specific 'most recent open must match next' order the stack is meant to enforce, since nothing distinguishes opens from closes on the stack anymore.",
                ),
                choice(
                    "If it's an opening bracket, push it; if it's a closing bracket, fail if the stack is empty or its top doesn't match, otherwise pop.",
                    true,
                    "Failing fast on an empty stack catches a closing bracket with nothing open to match, and checking the popped type catches mismatched pairs like an opening '(' closed by ']'.",
                ),
                choice(
                    "If it's a closing bracket, push it; if it's an opening bracket, pop and check a match.",
                    false,
                    "This reverses which bracket type gets pushed versus popped, so the stack's top would need to already contain a closing bracket before any opening bracket has even been seen.",
                )),
        ),
        step(
            "valid-parentheses", STACK, CODE_BLOCK,
            "Which snippet correctly implements the scan?",
            conceptKey = "valid-parentheses-code-block",
            code = "val stack = ArrayDeque<Char>()\nval pairs = mapOf(')' to '(', ']' to '[', '}' to '{')\nfor (c in s) {\n    // ???\n}\nreturn stack.isEmpty()",
            choices = listOf(
                choice(
                    "if (c in \"([{\") stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
                    true,
                    "This pushes every opening bracket, and for a closing bracket, fails immediately on an empty stack or a mismatched popped value - exactly the two failure conditions that must both be checked.",
                    code = "if (c in \"([{\") stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
                ),
                choice(
                    "if (c in \"([{\") stack.addLast(c) else stack.removeLast()",
                    false,
                    "Popping without checking the stack for emptiness first would crash on a closing bracket with nothing open, and without checking the popped value's type, mismatched pairs slip through unnoticed.",
                    code = "if (c in \"([{\") stack.addLast(c) else stack.removeLast()",
                ),
                choice(
                    "if (c in \")]}\") stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
                    false,
                    "This pushes closing brackets instead of opening ones, so pairs[c] would be looked up against the wrong side of the stack and the logic never matches correctly.",
                    code = "if (c in \")]}\") stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "stack = []\npairs = {\")\": \"(\", \"]\": \"[\", \"}\": \"{\"}\nfor c in s:\n    # ???\nreturn len(stack) == 0",
                    choices = listOf(
                        choice("if c in \"([{\":\n    stack.append(c)\nelse:\n    if not stack or stack.pop() != pairs[c]:\n        return False", true, "This pushes every opening bracket, and for a closing bracket, fails immediately on an empty stack or a mismatched popped value - exactly the two failure conditions that must both be checked.", code = "if c in \"([{\":\n    stack.append(c)\nelse:\n    if not stack or stack.pop() != pairs[c]:\n        return False"),
                        choice("if c in \"([{\":\n    stack.append(c)\nelse:\n    stack.pop()", false, "Popping without checking the stack for emptiness first would crash on a closing bracket with nothing open, and without checking the popped value's type, mismatched pairs slip through unnoticed.", code = "if c in \"([{\":\n    stack.append(c)\nelse:\n    stack.pop()"),
                        choice("if c in \")]}\":\n    stack.append(c)\nelse:\n    if not stack or stack.pop() != pairs[c]:\n        return False", false, "This pushes closing brackets instead of opening ones, so pairs[c] would be looked up against the wrong side of the stack and the logic never matches correctly.", code = "if c in \")]}\":\n    stack.append(c)\nelse:\n    if not stack or stack.pop() != pairs[c]:\n        return False"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "Deque<Character> stack = new ArrayDeque<>();\nMap<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');\nfor (char c : s.toCharArray()) {\n    // ???\n}\nreturn stack.isEmpty();",
                    choices = listOf(
                        choice("if (\"([{\".indexOf(c) >= 0) stack.push(c); else { if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false; }", true, "This pushes every opening bracket, and for a closing bracket, fails immediately on an empty stack or a mismatched popped value - exactly the two failure conditions that must both be checked.", code = "if (\"([{\".indexOf(c) >= 0) stack.push(c); else { if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false; }"),
                        choice("if (\"([{\".indexOf(c) >= 0) stack.push(c); else stack.pop();", false, "Popping without checking the stack for emptiness first would crash on a closing bracket with nothing open, and without checking the popped value's type, mismatched pairs slip through unnoticed.", code = "if (\"([{\".indexOf(c) >= 0) stack.push(c); else stack.pop();"),
                        choice("if (\")]}\".indexOf(c) >= 0) stack.push(c); else { if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false; }", false, "This pushes closing brackets instead of opening ones, so pairs[c] would be looked up against the wrong side of the stack and the logic never matches correctly.", code = "if (\")]}\".indexOf(c) >= 0) stack.push(c); else { if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false; }"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "const stack = [];\nconst pairs = { ')': '(', ']': '[', '}': '{' };\nfor (const c of s) {\n    // ???\n}\nreturn stack.length === 0;",
                    choices = listOf(
                        choice("if (\"([{\".includes(c)) stack.push(c); else { if (stack.length === 0 || stack.pop() !== pairs[c]) return false; }", true, "This pushes every opening bracket, and for a closing bracket, fails immediately on an empty stack or a mismatched popped value - exactly the two failure conditions that must both be checked.", code = "if (\"([{\".includes(c)) stack.push(c); else { if (stack.length === 0 || stack.pop() !== pairs[c]) return false; }"),
                        choice("if (\"([{\".includes(c)) stack.push(c); else stack.pop();", false, "Popping without checking the stack for emptiness first would crash on a closing bracket with nothing open, and without checking the popped value's type, mismatched pairs slip through unnoticed.", code = "if (\"([{\".includes(c)) stack.push(c); else stack.pop();"),
                        choice("if (\")]}\".includes(c)) stack.push(c); else { if (stack.length === 0 || stack.pop() !== pairs[c]) return false; }", false, "This pushes closing brackets instead of opening ones, so pairs[c] would be looked up against the wrong side of the stack and the logic never matches correctly.", code = "if (\")]}\".includes(c)) stack.push(c); else { if (stack.length === 0 || stack.pop() !== pairs[c]) return false; }"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "vector<char> stack;\nunordered_map<char, char> pairs = {{')', '('}, {']', '['}, {'}', '{'}};\nfor (char c : s) {\n    // ???\n}\nreturn stack.empty();",
                    choices = listOf(
                        choice("if (string(\"([{\").find(c) != string::npos) { stack.push_back(c); } else { if (stack.empty()) return false; char top = stack.back(); stack.pop_back(); if (top != pairs[c]) return false; }", true, "This pushes every opening bracket, and for a closing bracket, fails immediately on an empty stack or a mismatched popped value - exactly the two failure conditions that must both be checked.", code = "if (string(\"([{\").find(c) != string::npos) { stack.push_back(c); } else { if (stack.empty()) return false; char top = stack.back(); stack.pop_back(); if (top != pairs[c]) return false; }"),
                        choice("if (string(\"([{\").find(c) != string::npos) stack.push_back(c); else stack.pop_back();", false, "Popping without checking the stack for emptiness first would crash on a closing bracket with nothing open, and without checking the popped value's type, mismatched pairs slip through unnoticed.", code = "if (string(\"([{\").find(c) != string::npos) stack.push_back(c); else stack.pop_back();"),
                        choice("if (string(\")]}\").find(c) != string::npos) { stack.push_back(c); } else { if (stack.empty()) return false; char top = stack.back(); stack.pop_back(); if (top != pairs[c]) return false; }", false, "This pushes closing brackets instead of opening ones, so pairs[c] would be looked up against the wrong side of the stack and the logic never matches correctly.", code = "if (string(\")]}\").find(c) != string::npos) { stack.push_back(c); } else { if (stack.empty()) return false; char top = stack.back(); stack.pop_back(); if (top != pairs[c]) return false; }"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "stack := []byte{}\npairs := map[byte]byte{')': '(', ']': '[', '}': '{'}\nfor i := 0; i < len(s); i++ {\n    c := s[i]\n    // ???\n}\nreturn len(stack) == 0",
                    choices = listOf(
                        choice("if strings.ContainsRune(\"([{\", rune(c)) {\n    stack = append(stack, c)\n} else {\n    if len(stack) == 0 || stack[len(stack)-1] != pairs[c] {\n        return false\n    }\n    stack = stack[:len(stack)-1]\n}", true, "This pushes every opening bracket, and for a closing bracket, fails immediately on an empty stack or a mismatched popped value - exactly the two failure conditions that must both be checked.", code = "if strings.ContainsRune(\"([{\", rune(c)) {\n    stack = append(stack, c)\n} else {\n    if len(stack) == 0 || stack[len(stack)-1] != pairs[c] {\n        return false\n    }\n    stack = stack[:len(stack)-1]\n}"),
                        choice("if strings.ContainsRune(\"([{\", rune(c)) {\n    stack = append(stack, c)\n} else {\n    stack = stack[:len(stack)-1]\n}", false, "Popping without checking the stack for emptiness first would crash on a closing bracket with nothing open, and without checking the popped value's type, mismatched pairs slip through unnoticed.", code = "if strings.ContainsRune(\"([{\", rune(c)) {\n    stack = append(stack, c)\n} else {\n    stack = stack[:len(stack)-1]\n}"),
                        choice("if strings.ContainsRune(\")]}\", rune(c)) {\n    stack = append(stack, c)\n} else {\n    if len(stack) == 0 || stack[len(stack)-1] != pairs[c] {\n        return false\n    }\n    stack = stack[:len(stack)-1]\n}", false, "This pushes closing brackets instead of opening ones, so pairs[c] would be looked up against the wrong side of the stack and the logic never matches correctly.", code = "if strings.ContainsRune(\")]}\", rune(c)) {\n    stack = append(stack, c)\n} else {\n    if len(stack) == 0 || stack[len(stack)-1] != pairs[c] {\n        return false\n    }\n    stack = stack[:len(stack)-1]\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var stack: [Character] = []\nlet pairs: [Character: Character] = [\")\": \"(\", \"]\": \"[\", \"}\": \"{\"]\nfor c in s {\n    // ???\n}\nreturn stack.isEmpty",
                    choices = listOf(
                        choice("if \"([{\".contains(c) { stack.append(c) } else { if stack.isEmpty || stack.removeLast() != pairs[c] { return false } }", true, "This pushes every opening bracket, and for a closing bracket, fails immediately on an empty stack or a mismatched popped value - exactly the two failure conditions that must both be checked.", code = "if \"([{\".contains(c) { stack.append(c) } else { if stack.isEmpty || stack.removeLast() != pairs[c] { return false } }"),
                        choice("if \"([{\".contains(c) { stack.append(c) } else { stack.removeLast() }", false, "Popping without checking the stack for emptiness first would crash on a closing bracket with nothing open, and without checking the popped value's type, mismatched pairs slip through unnoticed.", code = "if \"([{\".contains(c) { stack.append(c) } else { stack.removeLast() }"),
                        choice("if \")]}\".contains(c) { stack.append(c) } else { if stack.isEmpty || stack.removeLast() != pairs[c] { return false } }", false, "This pushes closing brackets instead of opening ones, so pairs[c] would be looked up against the wrong side of the stack and the logic never matches correctly.", code = "if \")]}\".contains(c) { stack.append(c) } else { if stack.isEmpty || stack.removeLast() != pairs[c] { return false } }"),
                    ),
                ),
            ),
        ),
        step(
            "valid-parentheses", STACK, TIME_COMPLEXITY,
            "With n as the string's length, what is the time complexity?",
            conceptKey = "valid-parentheses-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the brackets need to be sorted before checking.",
                    false,
                    "Nothing here is sorted - brackets are pushed and popped in the exact order they appear in the string.",
                ),
                choice(
                    "O(n squared), because each closing bracket searches the whole stack for its match.",
                    false,
                    "The stack is only ever accessed at its very top, using constant-time pops - it's never searched through for a matching bracket.",
                ),
                choice(
                    "O(n), because each character triggers exactly one constant-time push or pop.",
                    true,
                    "One pass through the string, with a fixed, small amount of stack work per character, gives time proportional to n.",
                )),
        ),
        step(
            "valid-parentheses", STACK, SPACE_COMPLEXITY,
            "How much extra space does the stack use in the worst case?",
            conceptKey = "valid-parentheses-space-complexity",
            choices = listOf(
                choice(
                    "O(n), because a string made entirely of opening brackets pushes every character onto the stack.",
                    true,
                    "In the worst case, such as \"((((((\", every character is an opening bracket with nothing to pop it, so the stack can grow to hold all n characters.",
                ),
                choice(
                    "O(1), because brackets are immediately matched and removed.",
                    false,
                    "Matching only happens when a closing bracket appears - a long run of opening brackets with no closes yet can leave many entries sitting on the stack at once.",
                ),
                choice(
                    "O(log n), because the stack only grows for deeply nested brackets.",
                    false,
                    "There's no logarithmic bound here - the stack's size is determined directly by how many opening brackets appear before being closed, which can be close to n.",
                )),
        ),
        step(
            "valid-parentheses", STACK, TRANSFER,
            "Would this same stack-based matching approach also work for removing the minimum number of parentheses to make a string valid?",
            conceptKey = "valid-parentheses-transfer",
            choices = listOf(
                choice(
                    "Yes - a stack (or an equivalent counter) still identifies exactly which brackets are unmatched; those are the ones that need removing.",
                    true,
                    "The same core mechanism - tracking unmatched opens and detecting closes with nothing to match - directly identifies which specific brackets are the problem, which is one step further than just a yes-or-no validity check.",
                ),
                choice(
                    "No - deciding which brackets to remove requires trying every possible subset of removals.",
                    false,
                    "Trying every subset would be far more work than necessary - the same single-pass stack tracking that validates a string also directly identifies which specific brackets are unmatched.",
                ),
                choice(
                    "No - a stack can only answer valid-or-invalid questions, not which characters caused the problem.",
                    false,
                    "A stack (or matching counters) can absolutely pinpoint the offending brackets - an unmatched closing bracket is caught the moment it appears, and unmatched opens are whatever's left on the stack at the end.",
                )),
        ),
        step(
            "valid-parentheses", STACK, EDGE_CASE,
            "Which input would break a version of this solution that only checks whether the stack is empty at the end, without checking it during the scan?",
            conceptKey = "valid-parentheses-edge-case",
            choices = listOf(
                choice(
                    "A string like \"())(\" that has equal bracket counts but closes a bracket before its matching open exists.",
                    true,
                    "Without checking for an empty stack (or a type mismatch) at the moment a closing bracket appears, this string could crash or silently mismatch, even though the stack does end up empty overall.",
                ),
                choice(
                    "An empty string.",
                    false,
                    "An empty string trivially leaves the stack empty from the start, correctly returning true without ever exercising the per-character matching logic.",
                ),
                choice(
                    "A string with only one type of bracket, like \"(()())\".",
                    false,
                    "This is validly nested and every closing bracket has a proper matching open available in the correct order - it doesn't expose a missing per-character check.",
                )),
        ),
    ),
)

private val binarySearchWorkout = ProblemWorkout(
    problemSlug = "binary-search",
    group = BINARY_SEARCH,
    steps = listOf(
        step(
            "binary-search", BINARY_SEARCH, PATTERN_RECOGNITION,
            "Given a sorted array and a target, find the target's index or return -1. Which pattern fits?",
            conceptKey = "binary-search-pattern-recognition",
            choices = listOf(
                choice(
                    "Binary search: repeatedly check the middle of the remaining range and discard the half that can't contain the target.",
                    true,
                    "Because the array is sorted, comparing the middle value to the target always reveals which entire half can be safely thrown away, cutting the search space roughly in half every step.",
                ),
                choice(
                    "Scan the array from the start until the target is found.",
                    false,
                    "This works on any array, sorted or not, but it ignores the sorted order entirely, costing linear time where a search that uses the order can do far better.",
                ),
                choice(
                    "Two pointers starting at both ends, moving toward each other.",
                    false,
                    "Two pointers closing inward is built for finding a pair that sums to something, not for locating one specific value - it doesn't use the sorted order to eliminate large chunks the way comparing to a middle value does.",
                )),
        ),
        step(
            "binary-search", BINARY_SEARCH, APPROACH,
            "Which approach correctly finds the target in logarithmic time?",
            conceptKey = "binary-search-approach",
            choices = listOf(
                choice(
                    "Maintain low and high bounds; repeatedly compare the middle element to the target and narrow the range to whichever half could still contain it.",
                    true,
                    "Discarding an entire half on every comparison, rather than one element at a time, is what gives binary search its logarithmic time instead of linear time.",
                ),
                choice(
                    "Maintain low and high bounds; compare the middle element to the target and, if it doesn't match, move low forward by exactly one.",
                    false,
                    "Moving the bound by only one element per comparison, rather than discarding a whole half, degrades this back into a linear scan in the worst case.",
                ),
                choice(
                    "Split the array into fixed-size blocks and check only the first element of each block.",
                    false,
                    "Checking block boundaries still leaves a number of blocks proportional to the array's size to examine, rather than repeatedly halving the remaining range the way true binary search does.",
                )),
        ),
        step(
            "binary-search", BINARY_SEARCH, STATE_SELECTION,
            "What state does binary search need?",
            conceptKey = "binary-search-state-selection",
            choices = listOf(
                choice(
                    "A low index and a high index marking the current search range.",
                    true,
                    "Everything binary search needs to know - how much of the array is still in play - is captured entirely by these two boundary indices.",
                ),
                choice(
                    "A hash set of every value already checked.",
                    false,
                    "Tracking checked values isn't necessary - each comparison either finds the target or eliminates a whole half, so nothing needs to be remembered between steps beyond the current range.",
                ),
                choice(
                    "A sorted copy of the array rebuilt at every step.",
                    false,
                    "The array is already sorted once at the start - there's no reason to rebuild or re-sort anything as the search narrows its range.",
                )),
        ),
        step(
            "binary-search", BINARY_SEARCH, BOUNDARY_UPDATE,
            "Which rule correctly updates the search range after checking the middle element?",
            conceptKey = "binary-search-boundary-update",
            choices = listOf(
                choice(
                    "If the middle value is too small, set low to mid; if too large, set high to mid.",
                    false,
                    "Leaving mid itself inside the next range means the bounds can stop moving once the range narrows to two elements, causing the search to loop forever without terminating.",
                ),
                choice(
                    "If the middle value is too small, set low to mid + 1; if too large, set high to mid - 1; otherwise return mid.",
                    true,
                    "Moving the bound strictly past the just-checked middle index guarantees that index is excluded from the next range, so the search always makes forward progress and eventually terminates.",
                ),
                choice(
                    "If the middle value is too small, set high to mid - 1; if too large, set low to mid + 1.",
                    false,
                    "This swaps which direction each comparison moves the bounds, so the search space shrinks toward the wrong end of the array and never actually converges on the target.",
                )),
        ),
        step(
            "binary-search", BINARY_SEARCH, CODE_BLOCK,
            "Which snippet correctly implements the search loop?",
            conceptKey = "binary-search-code-block",
            code = "var low = 0\nvar high = nums.size - 1\nwhile (low <= high) {\n    val mid = low + (high - low) / 2\n    // ???\n}\nreturn -1",
            choices = listOf(
                choice(
                    "when {\n    nums[mid] < target -> low = mid + 1\n    nums[mid] > target -> high = mid - 1\n    else -> return mid\n}",
                    true,
                    "Each branch moves the correct bound strictly past mid, and an exact match returns immediately - together these guarantee both correctness and termination.",
                    code = "when {\n    nums[mid] < target -> low = mid + 1\n    nums[mid] > target -> high = mid - 1\n    else -> return mid\n}",
                ),
                choice(
                    "when {\n    nums[mid] < target -> low = mid\n    nums[mid] > target -> high = mid\n    else -> return mid\n}",
                    false,
                    "Leaving mid inside the next range on both branches can stall the loop once low and high are adjacent, since neither bound is guaranteed to move past mid.",
                    code = "when {\n    nums[mid] < target -> low = mid\n    nums[mid] > target -> high = mid\n    else -> return mid\n}",
                ),
                choice(
                    "when {\n    nums[mid] < target -> high = mid - 1\n    nums[mid] > target -> low = mid + 1\n    else -> return mid\n}",
                    false,
                    "This swaps the two branches, so a too-small middle value shrinks the range from the top instead of searching further right where the target actually is.",
                    code = "when {\n    nums[mid] < target -> high = mid - 1\n    nums[mid] > target -> low = mid + 1\n    else -> return mid\n}",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "low = 0\nhigh = len(nums) - 1\nwhile low <= high:\n    mid = low + (high - low) // 2\n    # ???\nreturn -1",
                    choices = listOf(
                        choice("if nums[mid] < target:\n    low = mid + 1\nelif nums[mid] > target:\n    high = mid - 1\nelse:\n    return mid", true, "Each branch moves the correct bound strictly past mid, and an exact match returns immediately - together these guarantee both correctness and termination.", code = "if nums[mid] < target:\n    low = mid + 1\nelif nums[mid] > target:\n    high = mid - 1\nelse:\n    return mid"),
                        choice("if nums[mid] < target:\n    low = mid\nelif nums[mid] > target:\n    high = mid\nelse:\n    return mid", false, "Leaving mid inside the next range on both branches can stall the loop once low and high are adjacent, since neither bound is guaranteed to move past mid.", code = "if nums[mid] < target:\n    low = mid\nelif nums[mid] > target:\n    high = mid\nelse:\n    return mid"),
                        choice("if nums[mid] < target:\n    high = mid - 1\nelif nums[mid] > target:\n    low = mid + 1\nelse:\n    return mid", false, "This swaps the two branches, so a too-small middle value shrinks the range from the top instead of searching further right where the target actually is.", code = "if nums[mid] < target:\n    high = mid - 1\nelif nums[mid] > target:\n    low = mid + 1\nelse:\n    return mid"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "int low = 0;\nint high = nums.length - 1;\nwhile (low <= high) {\n    int mid = low + (high - low) / 2;\n    // ???\n}\nreturn -1;",
                    choices = listOf(
                        choice("if (nums[mid] < target) low = mid + 1; else if (nums[mid] > target) high = mid - 1; else return mid;", true, "Each branch moves the correct bound strictly past mid, and an exact match returns immediately - together these guarantee both correctness and termination.", code = "if (nums[mid] < target) low = mid + 1; else if (nums[mid] > target) high = mid - 1; else return mid;"),
                        choice("if (nums[mid] < target) low = mid; else if (nums[mid] > target) high = mid; else return mid;", false, "Leaving mid inside the next range on both branches can stall the loop once low and high are adjacent, since neither bound is guaranteed to move past mid.", code = "if (nums[mid] < target) low = mid; else if (nums[mid] > target) high = mid; else return mid;"),
                        choice("if (nums[mid] < target) high = mid - 1; else if (nums[mid] > target) low = mid + 1; else return mid;", false, "This swaps the two branches, so a too-small middle value shrinks the range from the top instead of searching further right where the target actually is.", code = "if (nums[mid] < target) high = mid - 1; else if (nums[mid] > target) low = mid + 1; else return mid;"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "let low = 0;\nlet high = nums.length - 1;\nwhile (low <= high) {\n    const mid = low + Math.floor((high - low) / 2);\n    // ???\n}\nreturn -1;",
                    choices = listOf(
                        choice("if (nums[mid] < target) low = mid + 1; else if (nums[mid] > target) high = mid - 1; else return mid;", true, "Each branch moves the correct bound strictly past mid, and an exact match returns immediately - together these guarantee both correctness and termination.", code = "if (nums[mid] < target) low = mid + 1; else if (nums[mid] > target) high = mid - 1; else return mid;"),
                        choice("if (nums[mid] < target) low = mid; else if (nums[mid] > target) high = mid; else return mid;", false, "Leaving mid inside the next range on both branches can stall the loop once low and high are adjacent, since neither bound is guaranteed to move past mid.", code = "if (nums[mid] < target) low = mid; else if (nums[mid] > target) high = mid; else return mid;"),
                        choice("if (nums[mid] < target) high = mid - 1; else if (nums[mid] > target) low = mid + 1; else return mid;", false, "This swaps the two branches, so a too-small middle value shrinks the range from the top instead of searching further right where the target actually is.", code = "if (nums[mid] < target) high = mid - 1; else if (nums[mid] > target) low = mid + 1; else return mid;"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "int low = 0;\nint high = nums.size() - 1;\nwhile (low <= high) {\n    int mid = low + (high - low) / 2;\n    // ???\n}\nreturn -1;",
                    choices = listOf(
                        choice("if (nums[mid] < target) low = mid + 1; else if (nums[mid] > target) high = mid - 1; else return mid;", true, "Each branch moves the correct bound strictly past mid, and an exact match returns immediately - together these guarantee both correctness and termination.", code = "if (nums[mid] < target) low = mid + 1; else if (nums[mid] > target) high = mid - 1; else return mid;"),
                        choice("if (nums[mid] < target) low = mid; else if (nums[mid] > target) high = mid; else return mid;", false, "Leaving mid inside the next range on both branches can stall the loop once low and high are adjacent, since neither bound is guaranteed to move past mid.", code = "if (nums[mid] < target) low = mid; else if (nums[mid] > target) high = mid; else return mid;"),
                        choice("if (nums[mid] < target) high = mid - 1; else if (nums[mid] > target) low = mid + 1; else return mid;", false, "This swaps the two branches, so a too-small middle value shrinks the range from the top instead of searching further right where the target actually is.", code = "if (nums[mid] < target) high = mid - 1; else if (nums[mid] > target) low = mid + 1; else return mid;"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "low := 0\nhigh := len(nums) - 1\nfor low <= high {\n    mid := low + (high-low)/2\n    // ???\n}\nreturn -1",
                    choices = listOf(
                        choice("if nums[mid] < target {\n    low = mid + 1\n} else if nums[mid] > target {\n    high = mid - 1\n} else {\n    return mid\n}", true, "Each branch moves the correct bound strictly past mid, and an exact match returns immediately - together these guarantee both correctness and termination.", code = "if nums[mid] < target {\n    low = mid + 1\n} else if nums[mid] > target {\n    high = mid - 1\n} else {\n    return mid\n}"),
                        choice("if nums[mid] < target {\n    low = mid\n} else if nums[mid] > target {\n    high = mid\n} else {\n    return mid\n}", false, "Leaving mid inside the next range on both branches can stall the loop once low and high are adjacent, since neither bound is guaranteed to move past mid.", code = "if nums[mid] < target {\n    low = mid\n} else if nums[mid] > target {\n    high = mid\n} else {\n    return mid\n}"),
                        choice("if nums[mid] < target {\n    high = mid - 1\n} else if nums[mid] > target {\n    low = mid + 1\n} else {\n    return mid\n}", false, "This swaps the two branches, so a too-small middle value shrinks the range from the top instead of searching further right where the target actually is.", code = "if nums[mid] < target {\n    high = mid - 1\n} else if nums[mid] > target {\n    low = mid + 1\n} else {\n    return mid\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var low = 0\nvar high = nums.count - 1\nwhile low <= high {\n    let mid = low + (high - low) / 2\n    // ???\n}\nreturn -1",
                    choices = listOf(
                        choice("if nums[mid] < target { low = mid + 1 } else if nums[mid] > target { high = mid - 1 } else { return mid }", true, "Each branch moves the correct bound strictly past mid, and an exact match returns immediately - together these guarantee both correctness and termination.", code = "if nums[mid] < target { low = mid + 1 } else if nums[mid] > target { high = mid - 1 } else { return mid }"),
                        choice("if nums[mid] < target { low = mid } else if nums[mid] > target { high = mid } else { return mid }", false, "Leaving mid inside the next range on both branches can stall the loop once low and high are adjacent, since neither bound is guaranteed to move past mid.", code = "if nums[mid] < target { low = mid } else if nums[mid] > target { high = mid } else { return mid }"),
                        choice("if nums[mid] < target { high = mid - 1 } else if nums[mid] > target { low = mid + 1 } else { return mid }", false, "This swaps the two branches, so a too-small middle value shrinks the range from the top instead of searching further right where the target actually is.", code = "if nums[mid] < target { high = mid - 1 } else if nums[mid] > target { low = mid + 1 } else { return mid }"),
                    ),
                ),
            ),
        ),
        step(
            "binary-search", BINARY_SEARCH, TIME_COMPLEXITY,
            "With n as the number of elements, what is the time complexity?",
            conceptKey = "binary-search-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the array must be sorted before searching begins.",
                    false,
                    "The array is already given sorted, so no sorting work happens inside the search itself - only the search's own halving behavior counts toward its complexity.",
                ),
                choice(
                    "O(n), because in the worst case every element might need to be checked.",
                    false,
                    "Binary search never checks every element - each comparison discards roughly half of the remaining range, so far fewer than n comparisons are ever needed.",
                ),
                choice(
                    "O(log n), because each comparison discards half of the remaining search range.",
                    true,
                    "Cutting the remaining range in half on every comparison means the range shrinks to size one after roughly log base two of n steps.",
                )),
        ),
        step(
            "binary-search", BINARY_SEARCH, SPACE_COMPLEXITY,
            "How much extra space does this iterative version use?",
            conceptKey = "binary-search-space-complexity",
            choices = listOf(
                choice(
                    "O(1), because only a fixed handful of variables - low, high, and mid - are tracked regardless of array size.",
                    true,
                    "The iterative version updates the same few integer variables on every loop iteration rather than allocating new space proportional to the array or the range being searched.",
                ),
                choice(
                    "O(log n), because the search range is halved on every step.",
                    false,
                    "Halving the range affects how many iterations the loop runs, not how much memory each iteration uses - the loop itself allocates nothing extra as it progresses.",
                ),
                choice(
                    "O(n), because the array itself counts as extra space.",
                    false,
                    "The input array is typically not counted as extra space since it already exists before the function runs - what matters is space the algorithm itself allocates beyond the input.",
                )),
        ),
        step(
            "binary-search", BINARY_SEARCH, TRANSFER,
            "Would this same halve-the-range approach also work for finding the minimum eating speed that lets Koko finish all the banana piles in time?",
            conceptKey = "binary-search-transfer",
            choices = listOf(
                choice(
                    "Yes - binary search over the range of possible speeds, checking at each candidate whether it's fast enough, narrows to the minimum working speed the same way it narrows to a target value.",
                    true,
                    "As speed increases, the time needed to finish only ever decreases, never increases - that same kind of one-directional relationship is what makes binary searching over a range of possible answers valid here too.",
                ),
                choice(
                    "No - that problem has no target value to compare against, so binary search doesn't apply.",
                    false,
                    "There doesn't need to be a fixed target value already in an array - binary search works equally well over a *range of possible answers*, checking feasibility at each candidate.",
                ),
                choice(
                    "No - binary search only works when searching through an already-sorted array of stored values.",
                    false,
                    "Binary search's real requirement is a range where the answer to 'is this candidate good enough' only flips one way as the candidate increases - an actual stored, sorted array isn't required.",
                )),
        ),
        step(
            "binary-search", BINARY_SEARCH, EDGE_CASE,
            "Which input would break a version of this solution that computes mid as (low + high) / 2 instead of low + (high - low) / 2?",
            conceptKey = "binary-search-edge-case",
            choices = listOf(
                choice(
                    "An array large enough that low + high overflows a fixed-width integer, even though the true midpoint would fit comfortably.",
                    true,
                    "Adding low and high directly can overflow in languages with fixed-width integers when both are large, producing a wrong, out-of-range mid even though low + (high - low) / 2 would compute the same correct value safely.",
                ),
                choice(
                    "An array with only one element.",
                    false,
                    "With low and high both tiny for a single-element array, there's no overflow risk at all - this case doesn't exercise the bug.",
                ),
                choice(
                    "An array where the target isn't present.",
                    false,
                    "Whether the target is present or not doesn't affect how mid is computed - this is about the search returning -1 correctly, not about the overflow-prone arithmetic.",
                )),
        ),
    ),
)

private val twoSumWorkout = ProblemWorkout(
    problemSlug = "two-sum",
    group = ARRAYS_HASHING,
    steps = listOf(
        step(
            "two-sum", ARRAYS_HASHING, PATTERN_RECOGNITION,
            "Given an array and a target, find the indices of two numbers that add up to target. Which pattern fits?",
            conceptKey = "two-sum-pattern-recognition",
            choices = listOf(
                choice(
                    "Hash map from value to index: for each number, check whether target minus that number was already seen.",
                    true,
                    "Looking up whether the complement was already seen is a constant-time operation with a map, turning what would be a nested search into a single pass.",
                ),
                choice(
                    "Sort the array first, then use two pointers from both ends.",
                    false,
                    "Sorting scrambles the original indices, but the answer needs the original positions of the two numbers - a hash map avoids that problem entirely by keeping value-to-index lookups intact.",
                ),
                choice(
                    "Check every pair of numbers with two nested loops.",
                    false,
                    "This finds the right answer but costs O(n squared), far more than the O(n) a hash map achieves by looking up complements instead of comparing every pair.",
                )),
        ),
        step(
            "two-sum", ARRAYS_HASHING, APPROACH,
            "Which approach correctly finds the pair in one pass?",
            conceptKey = "two-sum-approach",
            choices = listOf(
                choice(
                    "For each number, check whether its complement (target minus the number) is already in the map; if not, add the current number and its index to the map.",
                    true,
                    "Checking for the complement before inserting the current number ensures a single element is never paired with itself, while still finding pairs formed by any two distinct positions.",
                ),
                choice(
                    "Add every number and its index to the map first, then make a second pass checking for each number's complement.",
                    false,
                    "This also works correctly, but it needs two full passes over the array where checking for the complement before inserting achieves the same result in just one.",
                ),
                choice(
                    "For each number, check whether the number itself (not its complement) is already in the map.",
                    false,
                    "Checking for the number itself instead of target minus the number would only ever find exact duplicates, not the two different values that actually sum to target in the general case.",
                )),
        ),
        step(
            "two-sum", ARRAYS_HASHING, STATE_SELECTION,
            "What state does this approach need?",
            conceptKey = "two-sum-state-selection",
            choices = listOf(
                choice(
                    "A hash map from each number's value to its index.",
                    true,
                    "Storing the index alongside the value is what lets the final answer report which two positions were used, not just which two values.",
                ),
                choice(
                    "A hash set of every number seen so far, without indices.",
                    false,
                    "A set alone can confirm a matching value exists but can't report which position it came from, and the problem asks for indices, not just values.",
                ),
                choice(
                    "A sorted copy of the array alongside the original.",
                    false,
                    "A sorted copy isn't needed - the hash map approach never requires the array to be in any particular order to find complements in constant time.",
                )),
        ),
        step(
            "two-sum", ARRAYS_HASHING, BOUNDARY_UPDATE,
            "Which rule correctly checks and updates the map at each number?",
            conceptKey = "two-sum-boundary-update",
            choices = listOf(
                choice(
                    "Insert nums[i] with index i into the map first, then check whether target - nums[i] exists in the map.",
                    false,
                    "Inserting before checking means a number can find itself as its own complement whenever target is exactly double that number, incorrectly pairing an index with itself.",
                ),
                choice(
                    "Check whether target - nums[i] exists in the map first; if it does, return the pair; if not, insert nums[i] with index i into the map, then continue.",
                    true,
                    "Checking before inserting guarantees the complement found, if any, was placed there by an earlier, different index, so the same element is never used twice.",
                ),
                choice(
                    "Check whether nums[i] exists in the map, and if so, insert target - nums[i] instead.",
                    false,
                    "This checks for the wrong value entirely - it should be looking up the complement that would pair with the current number, not the current number itself.",
                )),
        ),
        step(
            "two-sum", ARRAYS_HASHING, CODE_BLOCK,
            "Which snippet correctly implements the single-pass lookup?",
            conceptKey = "two-sum-code-block",
            code = "val seen = HashMap<Int, Int>()\nfor (i in nums.indices) {\n    val complement = target - nums[i]\n    // ???\n}",
            choices = listOf(
                choice(
                    "if (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
                    true,
                    "This checks for the complement before touching the map for the current index, then records the current number afterward - exactly the check-then-insert order that avoids pairing an index with itself.",
                    code = "if (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
                ),
                choice(
                    "seen[nums[i]] = i\nif (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)",
                    false,
                    "Inserting the current number before checking for its complement means, when target is exactly double the current value, the number can be found as its own complement.",
                    code = "seen[nums[i]] = i\nif (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)",
                ),
                choice(
                    "if (seen.containsKey(nums[i])) return intArrayOf(seen[nums[i]]!!, i)\nseen[complement] = i",
                    false,
                    "This checks for the current number itself rather than its complement, and stores the complement instead of the number, which looks up and inserts the wrong values entirely.",
                    code = "if (seen.containsKey(nums[i])) return intArrayOf(seen[nums[i]]!!, i)\nseen[complement] = i",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "seen = {}\nfor i in range(len(nums)):\n    complement = target - nums[i]\n    # ???",
                    choices = listOf(
                        choice("if complement in seen:\n    return [seen[complement], i]\nseen[nums[i]] = i", true, "This checks for the complement before touching the map for the current index, then records the current number afterward - exactly the check-then-insert order that avoids pairing an index with itself.", code = "if complement in seen:\n    return [seen[complement], i]\nseen[nums[i]] = i"),
                        choice("seen[nums[i]] = i\nif complement in seen:\n    return [seen[complement], i]", false, "Inserting the current number before checking for its complement means, when target is exactly double the current value, the number can be found as its own complement.", code = "seen[nums[i]] = i\nif complement in seen:\n    return [seen[complement], i]"),
                        choice("if nums[i] in seen:\n    return [seen[nums[i]], i]\nseen[complement] = i", false, "This checks for the current number itself rather than its complement, and stores the complement instead of the number, which looks up and inserts the wrong values entirely.", code = "if nums[i] in seen:\n    return [seen[nums[i]], i]\nseen[complement] = i"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "Map<Integer, Integer> seen = new HashMap<>();\nfor (int i = 0; i < nums.length; i++) {\n    int complement = target - nums[i];\n    // ???\n}",
                    choices = listOf(
                        choice("if (seen.containsKey(complement)) return new int[]{seen.get(complement), i};\nseen.put(nums[i], i);", true, "This checks for the complement before touching the map for the current index, then records the current number afterward - exactly the check-then-insert order that avoids pairing an index with itself.", code = "if (seen.containsKey(complement)) return new int[]{seen.get(complement), i};\nseen.put(nums[i], i);"),
                        choice("seen.put(nums[i], i);\nif (seen.containsKey(complement)) return new int[]{seen.get(complement), i};", false, "Inserting the current number before checking for its complement means, when target is exactly double the current value, the number can be found as its own complement.", code = "seen.put(nums[i], i);\nif (seen.containsKey(complement)) return new int[]{seen.get(complement), i};"),
                        choice("if (seen.containsKey(nums[i])) return new int[]{seen.get(nums[i]), i};\nseen.put(complement, i);", false, "This checks for the current number itself rather than its complement, and stores the complement instead of the number, which looks up and inserts the wrong values entirely.", code = "if (seen.containsKey(nums[i])) return new int[]{seen.get(nums[i]), i};\nseen.put(complement, i);"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "const seen = new Map();\nfor (let i = 0; i < nums.length; i++) {\n    const complement = target - nums[i];\n    // ???\n}",
                    choices = listOf(
                        choice("if (seen.has(complement)) return [seen.get(complement), i];\nseen.set(nums[i], i);", true, "This checks for the complement before touching the map for the current index, then records the current number afterward - exactly the check-then-insert order that avoids pairing an index with itself.", code = "if (seen.has(complement)) return [seen.get(complement), i];\nseen.set(nums[i], i);"),
                        choice("seen.set(nums[i], i);\nif (seen.has(complement)) return [seen.get(complement), i];", false, "Inserting the current number before checking for its complement means, when target is exactly double the current value, the number can be found as its own complement.", code = "seen.set(nums[i], i);\nif (seen.has(complement)) return [seen.get(complement), i];"),
                        choice("if (seen.has(nums[i])) return [seen.get(nums[i]), i];\nseen.set(complement, i);", false, "This checks for the current number itself rather than its complement, and stores the complement instead of the number, which looks up and inserts the wrong values entirely.", code = "if (seen.has(nums[i])) return [seen.get(nums[i]), i];\nseen.set(complement, i);"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "unordered_map<int, int> seen;\nfor (int i = 0; i < (int)nums.size(); i++) {\n    int complement = target - nums[i];\n    // ???\n}",
                    choices = listOf(
                        choice("if (seen.count(complement)) return {seen[complement], i};\nseen[nums[i]] = i;", true, "This checks for the complement before touching the map for the current index, then records the current number afterward - exactly the check-then-insert order that avoids pairing an index with itself.", code = "if (seen.count(complement)) return {seen[complement], i};\nseen[nums[i]] = i;"),
                        choice("seen[nums[i]] = i;\nif (seen.count(complement)) return {seen[complement], i};", false, "Inserting the current number before checking for its complement means, when target is exactly double the current value, the number can be found as its own complement.", code = "seen[nums[i]] = i;\nif (seen.count(complement)) return {seen[complement], i};"),
                        choice("if (seen.count(nums[i])) return {seen[nums[i]], i};\nseen[complement] = i;", false, "This checks for the current number itself rather than its complement, and stores the complement instead of the number, which looks up and inserts the wrong values entirely.", code = "if (seen.count(nums[i])) return {seen[nums[i]], i};\nseen[complement] = i;"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "seen := map[int]int{}\nfor i := 0; i < len(nums); i++ {\n    complement := target - nums[i]\n    // ???\n}",
                    choices = listOf(
                        choice("if j, ok := seen[complement]; ok {\n    return []int{j, i}\n}\nseen[nums[i]] = i", true, "This checks for the complement before touching the map for the current index, then records the current number afterward - exactly the check-then-insert order that avoids pairing an index with itself.", code = "if j, ok := seen[complement]; ok {\n    return []int{j, i}\n}\nseen[nums[i]] = i"),
                        choice("seen[nums[i]] = i\nif j, ok := seen[complement]; ok {\n    return []int{j, i}\n}", false, "Inserting the current number before checking for its complement means, when target is exactly double the current value, the number can be found as its own complement.", code = "seen[nums[i]] = i\nif j, ok := seen[complement]; ok {\n    return []int{j, i}\n}"),
                        choice("if j, ok := seen[nums[i]]; ok {\n    return []int{j, i}\n}\nseen[complement] = i", false, "This checks for the current number itself rather than its complement, and stores the complement instead of the number, which looks up and inserts the wrong values entirely.", code = "if j, ok := seen[nums[i]]; ok {\n    return []int{j, i}\n}\nseen[complement] = i"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var seen: [Int: Int] = [:]\nfor i in 0..<nums.count {\n    let complement = target - nums[i]\n    // ???\n}",
                    choices = listOf(
                        choice("if let j = seen[complement] { return [j, i] }\nseen[nums[i]] = i", true, "This checks for the complement before touching the map for the current index, then records the current number afterward - exactly the check-then-insert order that avoids pairing an index with itself.", code = "if let j = seen[complement] { return [j, i] }\nseen[nums[i]] = i"),
                        choice("seen[nums[i]] = i\nif let j = seen[complement] { return [j, i] }", false, "Inserting the current number before checking for its complement means, when target is exactly double the current value, the number can be found as its own complement.", code = "seen[nums[i]] = i\nif let j = seen[complement] { return [j, i] }"),
                        choice("if let j = seen[nums[i]] { return [j, i] }\nseen[complement] = i", false, "This checks for the current number itself rather than its complement, and stores the complement instead of the number, which looks up and inserts the wrong values entirely.", code = "if let j = seen[nums[i]] { return [j, i] }\nseen[complement] = i"),
                    ),
                ),
            ),
        ),
        step(
            "two-sum", ARRAYS_HASHING, TIME_COMPLEXITY,
            "With n as the array length, what is the time complexity?",
            conceptKey = "two-sum-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the array must be sorted first.",
                    false,
                    "No sorting happens in this approach - the hash map finds complements without needing the array in any particular order.",
                ),
                choice(
                    "O(n squared), because every pair of numbers must be checked.",
                    false,
                    "The hash map replaces the need to check every pair directly - each number only ever looks up its own complement, not every other number in the array.",
                ),
                choice(
                    "O(n), because each element triggers one constant-time map lookup and one constant-time insert.",
                    true,
                    "A single pass through the array, with constant work per element thanks to the hash map, gives time proportional to n.",
                )),
        ),
        step(
            "two-sum", ARRAYS_HASHING, SPACE_COMPLEXITY,
            "How much extra space does this approach use?",
            conceptKey = "two-sum-space-complexity",
            choices = listOf(
                choice(
                    "O(n), because the map can grow to hold every number in the array before a match is found.",
                    true,
                    "In the worst case, no pair is found until nearly the whole array has been scanned, so the map can hold close to n entries.",
                ),
                choice(
                    "O(1), because the map only ever holds two entries at once.",
                    false,
                    "The map keeps every number seen so far, not just the two that eventually match, so it can grow well beyond two entries before the answer is found.",
                ),
                choice(
                    "O(n squared), because a lookup table of every pair's sum is built.",
                    false,
                    "No table of pair sums is ever built - only a single map from value to index is maintained, updated one entry at a time.",
                )),
        ),
        step(
            "two-sum", ARRAYS_HASHING, TRANSFER,
            "Would this same value-to-index map approach also work for detecting whether an array contains any duplicate value?",
            conceptKey = "two-sum-transfer",
            choices = listOf(
                choice(
                    "Yes, and it simplifies further - a plain set of seen values is enough, since only presence matters, not which index paired with which.",
                    true,
                    "Contains Duplicate only needs to know whether a value has been seen before, dropping the need to store an index at all - a simpler version of the same seen-so-far lookup idea.",
                ),
                choice(
                    "No - detecting duplicates requires sorting the array first to bring equal values next to each other.",
                    false,
                    "Sorting works but isn't necessary - a hash set answers 'have I seen this value before' in constant time per element without needing the array in any order.",
                ),
                choice(
                    "No - hash-based lookups only work when searching for a specific target sum.",
                    false,
                    "The core technique here is just 'has this value been seen before,' which applies directly to duplicate detection with no target sum involved at all.",
                )),
        ),
        step(
            "two-sum", ARRAYS_HASHING, EDGE_CASE,
            "Which input would break a version of this solution that checks for the number itself instead of its complement?",
            conceptKey = "two-sum-edge-case",
            choices = listOf(
                choice(
                    "nums = [3, 3], target = 6, where the correct answer pairs the two different indices holding the value 3.",
                    true,
                    "Checking for the number itself rather than target minus the number would look up whether 3 was already seen, which only coincidentally works when the complement equals the number - the check-then-insert order matters even more clearly in cases like this.",
                ),
                choice(
                    "nums = [1, 5, 3], target = 8, with all distinct values.",
                    false,
                    "With entirely distinct values, checking for the complement correctly finds 5 and 3 - this case doesn't stress the same-index or duplicate-value edge behavior.",
                ),
                choice(
                    "An array with only two elements.",
                    false,
                    "Two elements is simply the smallest valid input and doesn't involve any duplicate or self-pairing complication on its own.",
                )),
        ),
    ),
)

private val containsDuplicateWorkout = ProblemWorkout(
    problemSlug = "contains-duplicate",
    group = ARRAYS_HASHING,
    steps = listOf(
        step(
            "contains-duplicate", ARRAYS_HASHING, PATTERN_RECOGNITION,
            "Given an array, decide whether any value appears more than once. Which pattern fits?",
            conceptKey = "contains-duplicate-pattern-recognition",
            choices = listOf(
                choice(
                    "Hash set: track every value seen so far and check membership before adding each new one.",
                    true,
                    "A set answers 'have I seen this value before' in constant time, which is exactly the question that needs answering for every element.",
                ),
                choice(
                    "Two pointers scanning from both ends of the array inward.",
                    false,
                    "Duplicate values could be anywhere in the array, not necessarily positioned symmetrically from the two ends, so there's no reason to anchor pointers there.",
                ),
                choice(
                    "Binary search each value against the rest of the array.",
                    false,
                    "Binary search needs sorted data, and the array isn't sorted going in - a hash set answers the membership question without needing any preprocessing at all.",
                )),
        ),
        step(
            "contains-duplicate", ARRAYS_HASHING, APPROACH,
            "Which approach correctly detects a duplicate in one pass?",
            conceptKey = "contains-duplicate-approach",
            choices = listOf(
                choice(
                    "For each value, check whether it's already in the set; if so, return true immediately; otherwise add it and continue.",
                    true,
                    "Checking before adding is what actually detects the second occurrence - the very moment a repeated value shows up, its first occurrence is already sitting in the set.",
                ),
                choice(
                    "Add every value to the set first, then compare the set's size to the array's length.",
                    false,
                    "This also works, since a smaller set size than the array means something repeated, but it needs to build the whole set before it can answer anything, rather than returning as soon as a duplicate is found.",
                ),
                choice(
                    "Sort the array and return true if it's not already sorted.",
                    false,
                    "Checking sortedness has nothing to do with detecting duplicates - a strictly increasing array has no duplicates regardless of sortedness, and a sorted array can still contain repeats.",
                )),
        ),
        step(
            "contains-duplicate", ARRAYS_HASHING, STATE_SELECTION,
            "What state does this approach need?",
            conceptKey = "contains-duplicate-state-selection",
            choices = listOf(
                choice(
                    "A hash set of every value seen so far.",
                    true,
                    "Membership in a set is all that's needed to answer 'has this exact value shown up before' for each new element.",
                ),
                choice(
                    "A count of how many times each value has appeared, even after the answer is already known.",
                    false,
                    "Continuing to count occurrences after a duplicate is already found does unnecessary extra work - the question only needs a single true or false answer, not full counts.",
                ),
                choice(
                    "The index of the first and last elements only.",
                    false,
                    "A duplicate could involve any two positions in the array, not specifically the first and last elements, so tracking just those two indices misses most cases.",
                )),
        ),
        step(
            "contains-duplicate", ARRAYS_HASHING, BOUNDARY_UPDATE,
            "Which rule correctly checks and updates the set at each value?",
            conceptKey = "contains-duplicate-boundary-update",
            choices = listOf(
                choice(
                    "Add the current value to the set first, then check whether the set contains it.",
                    false,
                    "Adding first means the check afterward will always find the value present, since it was just inserted - this can never correctly detect a duplicate.",
                ),
                choice(
                    "Check whether the current value is already in the set; if yes, return true; if no, add it, then move to the next value.",
                    true,
                    "Checking membership before adding is what makes the check meaningful - if the value is already there, this exact value must have appeared earlier.",
                ),
                choice(
                    "Add the current value to the set only if it's not already the array's first element.",
                    false,
                    "Whether a value is the array's first element has nothing to do with whether it's a duplicate - duplicates can start anywhere in the array.",
                )),
        ),
        step(
            "contains-duplicate", ARRAYS_HASHING, CODE_BLOCK,
            "Which snippet correctly implements the single-pass check?",
            conceptKey = "contains-duplicate-code-block",
            code = "val seen = HashSet<Int>()\nfor (num in nums) {\n    // ???\n}\nreturn false",
            choices = listOf(
                choice(
                    "if (!seen.add(num)) return true",
                    true,
                    "HashSet.add returns false when the value was already present, so this correctly returns true the moment a repeat is found, and keeps adding new values otherwise.",
                    code = "if (!seen.add(num)) return true",
                ),
                choice(
                    "seen.add(num)\nif (seen.contains(num)) return true",
                    false,
                    "Adding before checking means the value is always found present immediately after being added, so this would incorrectly return true on the very first element.",
                    code = "seen.add(num)\nif (seen.contains(num)) return true",
                ),
                choice(
                    "if (seen.size > 0) return true\nseen.add(num)",
                    false,
                    "Checking whether the set is merely non-empty has nothing to do with whether the *current* value specifically is a repeat - this would return true after the very first element is added.",
                    code = "if (seen.size > 0) return true\nseen.add(num)",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "seen = set()\nfor num in nums:\n    # ???\nreturn False",
                    choices = listOf(
                        choice("if num in seen:\n    return True\nseen.add(num)", true, "This checks for the value before touching the set, so it correctly returns True the moment a repeat is found, and keeps adding new values otherwise.", code = "if num in seen:\n    return True\nseen.add(num)"),
                        choice("seen.add(num)\nif num in seen:\n    return True", false, "Adding before checking means the value is always found present immediately after being added, so this would incorrectly return True on the very first element.", code = "seen.add(num)\nif num in seen:\n    return True"),
                        choice("if len(seen) > 0:\n    return True\nseen.add(num)", false, "Checking whether the set is merely non-empty has nothing to do with whether the current value specifically is a repeat - this would return True after the very first element is added.", code = "if len(seen) > 0:\n    return True\nseen.add(num)"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "Set<Integer> seen = new HashSet<>();\nfor (int num : nums) {\n    // ???\n}\nreturn false;",
                    choices = listOf(
                        choice("if (!seen.add(num)) return true;", true, "Set.add returns false when the value was already present, so this correctly returns true the moment a repeat is found, and keeps adding new values otherwise.", code = "if (!seen.add(num)) return true;"),
                        choice("seen.add(num);\nif (seen.contains(num)) return true;", false, "Adding before checking means the value is always found present immediately after being added, so this would incorrectly return true on the very first element.", code = "seen.add(num);\nif (seen.contains(num)) return true;"),
                        choice("if (seen.size() > 0) return true;\nseen.add(num);", false, "Checking whether the set is merely non-empty has nothing to do with whether the current value specifically is a repeat - this would return true after the very first element is added.", code = "if (seen.size() > 0) return true;\nseen.add(num);"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "const seen = new Set();\nfor (const num of nums) {\n    // ???\n}\nreturn false;",
                    choices = listOf(
                        choice("if (seen.has(num)) return true;\nseen.add(num);", true, "This checks for the value before touching the set, so it correctly returns true the moment a repeat is found, and keeps adding new values otherwise.", code = "if (seen.has(num)) return true;\nseen.add(num);"),
                        choice("seen.add(num);\nif (seen.has(num)) return true;", false, "Adding before checking means the value is always found present immediately after being added, so this would incorrectly return true on the very first element.", code = "seen.add(num);\nif (seen.has(num)) return true;"),
                        choice("if (seen.size > 0) return true;\nseen.add(num);", false, "Checking whether the set is merely non-empty has nothing to do with whether the current value specifically is a repeat - this would return true after the very first element is added.", code = "if (seen.size > 0) return true;\nseen.add(num);"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "unordered_set<int> seen;\nfor (int num : nums) {\n    // ???\n}\nreturn false;",
                    choices = listOf(
                        choice("if (!seen.insert(num).second) return true;", true, "insert's returned bool is false when the value was already present, so this correctly returns true the moment a repeat is found, and keeps adding new values otherwise.", code = "if (!seen.insert(num).second) return true;"),
                        choice("seen.insert(num);\nif (seen.count(num)) return true;", false, "Adding before checking means the value is always found present immediately after being added, so this would incorrectly return true on the very first element.", code = "seen.insert(num);\nif (seen.count(num)) return true;"),
                        choice("if (seen.size() > 0) return true;\nseen.insert(num);", false, "Checking whether the set is merely non-empty has nothing to do with whether the current value specifically is a repeat - this would return true after the very first element is added.", code = "if (seen.size() > 0) return true;\nseen.insert(num);"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "seen := map[int]bool{}\nfor _, num := range nums {\n    // ???\n}\nreturn false",
                    choices = listOf(
                        choice("if seen[num] {\n    return true\n}\nseen[num] = true", true, "This checks for the value before touching the map, so it correctly returns true the moment a repeat is found, and keeps adding new values otherwise.", code = "if seen[num] {\n    return true\n}\nseen[num] = true"),
                        choice("seen[num] = true\nif seen[num] {\n    return true\n}", false, "Adding before checking means the value is always found present immediately after being added, so this would incorrectly return true on the very first element.", code = "seen[num] = true\nif seen[num] {\n    return true\n}"),
                        choice("if len(seen) > 0 {\n    return true\n}\nseen[num] = true", false, "Checking whether the map is merely non-empty has nothing to do with whether the current value specifically is a repeat - this would return true after the very first element is added.", code = "if len(seen) > 0 {\n    return true\n}\nseen[num] = true"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var seen = Set<Int>()\nfor num in nums {\n    // ???\n}\nreturn false",
                    choices = listOf(
                        choice("if seen.contains(num) { return true }\nseen.insert(num)", true, "This checks for the value before touching the set, so it correctly returns true the moment a repeat is found, and keeps adding new values otherwise.", code = "if seen.contains(num) { return true }\nseen.insert(num)"),
                        choice("seen.insert(num)\nif seen.contains(num) { return true }", false, "Adding before checking means the value is always found present immediately after being added, so this would incorrectly return true on the very first element.", code = "seen.insert(num)\nif seen.contains(num) { return true }"),
                        choice("if seen.count > 0 { return true }\nseen.insert(num)", false, "Checking whether the set is merely non-empty has nothing to do with whether the current value specifically is a repeat - this would return true after the very first element is added.", code = "if seen.count > 0 { return true }\nseen.insert(num)"),
                    ),
                ),
            ),
        ),
        step(
            "contains-duplicate", ARRAYS_HASHING, TIME_COMPLEXITY,
            "With n as the array length, what is the time complexity?",
            conceptKey = "contains-duplicate-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the array is sorted first to bring duplicates together.",
                    false,
                    "No sorting happens in the set-based approach - membership is checked directly without needing any particular order.",
                ),
                choice(
                    "O(n squared), because every value is compared against every other value.",
                    false,
                    "The hash set answers each membership question directly, without needing to compare the current value against every other value individually.",
                ),
                choice(
                    "O(n), because each element triggers one constant-time set lookup and insert.",
                    true,
                    "A single pass through the array, with constant-time set operations per element, gives time proportional to n.",
                )),
        ),
        step(
            "contains-duplicate", ARRAYS_HASHING, SPACE_COMPLEXITY,
            "How much extra space does this approach use?",
            conceptKey = "contains-duplicate-space-complexity",
            choices = listOf(
                choice(
                    "O(n), because in the worst case (no duplicates at all) every value ends up in the set.",
                    true,
                    "If the array truly has no duplicates, the loop never returns early, and the set grows to hold all n distinct values.",
                ),
                choice(
                    "O(1), because the set never holds more than one value at a time.",
                    false,
                    "The set keeps every distinct value seen so far, not just the most recent one, so it can grow well beyond a single entry.",
                ),
                choice(
                    "O(log n), because the set is internally sorted.",
                    false,
                    "A hash set has no internal ordering to maintain - its size is determined purely by how many distinct values have been inserted.",
                )),
        ),
        step(
            "contains-duplicate", ARRAYS_HASHING, TRANSFER,
            "Would this same seen-so-far set approach also work for Two Sum?",
            conceptKey = "contains-duplicate-transfer",
            choices = listOf(
                choice(
                    "Yes, extended slightly - a map from value to index instead of a plain set, since Two Sum needs to report which two positions matched.",
                    true,
                    "The core 'have I seen something relevant before' lookup carries over directly - it just needs to store index alongside value, and check for a computed complement instead of the value itself.",
                ),
                choice(
                    "No - Two Sum requires sorting the array and using two pointers instead.",
                    false,
                    "Two Sum can be solved with the exact same hash-based seen-so-far idea, just checking for a complement value instead of an exact repeat - sorting isn't required.",
                ),
                choice(
                    "No - a set can only answer true-or-false questions, not return specific values.",
                    false,
                    "Switching from a set to a map (value to index) keeps the same constant-time lookup idea while additionally reporting *which* index matched, which is all Two Sum needs on top of Contains Duplicate.",
                )),
        ),
        step(
            "contains-duplicate", ARRAYS_HASHING, EDGE_CASE,
            "Which input would break a version of this solution that adds every value to the set before checking anything?",
            conceptKey = "contains-duplicate-edge-case",
            choices = listOf(
                choice(
                    "Any array with a duplicate - adding everything first before checking means the function would need to compare set size to array length afterward, and never returns early or points to which value repeated.",
                    true,
                    "Building the entire set first still works for a final true/false answer via size comparison, but it does unnecessary extra work for a large duplicate-free array and can't report early - the check-before-add order is what makes each step meaningful on its own.",
                ),
                choice(
                    "An empty array.",
                    false,
                    "An empty array trivially has no duplicates and returns false either way - it doesn't stress the ordering of checking versus adding.",
                ),
                choice(
                    "An array with all distinct values.",
                    false,
                    "With no duplicates present at all, both orderings of checking and adding end up scanning the whole array and correctly returning false.",
                )),
        ),
    ),
)

private val containerWithMostWaterWorkout = ProblemWorkout(
    problemSlug = "container-with-most-water",
    group = TWO_POINTERS,
    steps = listOf(
        step(
            "container-with-most-water", TWO_POINTERS, PATTERN_RECOGNITION,
            "Given an array of heights, find the two lines that, together with the x-axis, form the container holding the most water. Which pattern fits?",
            conceptKey = "container-with-most-water-pattern-recognition",
            choices = listOf(
                choice(
                    "Two pointers starting at both ends, moving the pointer at the shorter line inward.",
                    true,
                    "The width is largest when the pointers start at the two ends, so the only way to potentially find a taller container is to move the pointer at the shorter, limiting line - the taller one can never help until the shorter one improves.",
                ),
                choice(
                    "Sliding window that only ever grows from the left.",
                    false,
                    "A window that only grows can never test the widest possible pairs first and narrow down, which is exactly the strength of two pointers starting at both ends.",
                ),
                choice(
                    "Check every pair of lines directly.",
                    false,
                    "This finds the correct answer but costs O(n squared), far more than the O(n) that two pointers converging from both ends achieves.",
                )),
        ),
        step(
            "container-with-most-water", TWO_POINTERS, APPROACH,
            "Which approach correctly finds the maximum area?",
            conceptKey = "container-with-most-water-approach",
            choices = listOf(
                choice(
                    "Start pointers at both ends; compute the area at each step; move the pointer at the shorter of the two lines inward, since it's the height that limits the current area.",
                    true,
                    "Moving the shorter line's pointer is the only move that could possibly increase the area - moving the taller one can only ever decrease both the width and the limiting height.",
                ),
                choice(
                    "Start pointers at both ends; compute the area at each step; move the pointer at the taller of the two lines inward.",
                    false,
                    "Moving the taller line inward keeps the shorter, limiting height the same while shrinking the width, which can only ever produce a smaller or equal area, never a larger one.",
                ),
                choice(
                    "Start pointers at both ends; always move both pointers inward together on every step.",
                    false,
                    "Moving both pointers together shrinks the width every time regardless of which height was actually limiting the area, skipping over potentially better combinations.",
                )),
        ),
        step(
            "container-with-most-water", TWO_POINTERS, STATE_SELECTION,
            "What state does this approach need?",
            conceptKey = "container-with-most-water-state-selection",
            choices = listOf(
                choice(
                    "A left pointer, a right pointer, and the maximum area found so far.",
                    true,
                    "Everything needed to compute the current area - the two heights and the width between them - comes directly from these two pointers, and the running maximum is the answer being built.",
                ),
                choice(
                    "A sorted copy of the heights array.",
                    false,
                    "Sorting the heights would scramble their original positions, but the width between two lines depends entirely on their original indices in the array.",
                ),
                choice(
                    "A running sum of every height seen so far.",
                    false,
                    "The area only ever depends on the shorter of exactly two specific heights and the distance between them, not on a cumulative sum of every height passed so far.",
                )),
        ),
        step(
            "container-with-most-water", TWO_POINTERS, BOUNDARY_UPDATE,
            "Which rule correctly moves the pointers after computing the current area?",
            conceptKey = "container-with-most-water-boundary-update",
            choices = listOf(
                choice(
                    "If height[left] < height[right], move right backward; otherwise move left forward.",
                    false,
                    "This moves the pointer at the *taller* line instead, which can only shrink the width while keeping the same limiting height - never an improvement.",
                ),
                choice(
                    "If height[left] < height[right], move left forward; otherwise move right backward.",
                    true,
                    "This always advances whichever pointer points at the currently shorter (or equal) line, giving that side a chance to find a taller line while the limiting height can only improve or stay the same.",
                ),
                choice(
                    "Move left forward if the area increased, otherwise move right backward.",
                    false,
                    "Which pointer to move should depend on which height is currently limiting, not on whether the last area computed happened to be larger - that comparison doesn't track the right thing.",
                )),
        ),
        step(
            "container-with-most-water", TWO_POINTERS, CODE_BLOCK,
            "Which snippet correctly implements the pointer loop?",
            conceptKey = "container-with-most-water-code-block",
            code = "var left = 0\nvar right = height.size - 1\nvar maxArea = 0\nwhile (left < right) {\n    val area = minOf(height[left], height[right]) * (right - left)\n    maxArea = maxOf(maxArea, area)\n    // ???\n}",
            choices = listOf(
                choice(
                    "if (height[left] < height[right]) left++ else right--",
                    true,
                    "This moves the pointer at the shorter line inward, the only move that gives the area a chance to improve, matching the current heights correctly.",
                    code = "if (height[left] < height[right]) left++ else right--",
                ),
                choice(
                    "if (height[left] < height[right]) right-- else left++",
                    false,
                    "This moves the pointer at the *taller* line, which only shrinks the width without any chance of raising the limiting height.",
                    code = "if (height[left] < height[right]) right-- else left++",
                ),
                choice(
                    "left++\nright--",
                    false,
                    "Moving both pointers on every step regardless of which height is limiting can skip past the actual best combination of heights and width.",
                    code = "left++\nright--",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "left = 0\nright = len(height) - 1\nmax_area = 0\nwhile left < right:\n    area = min(height[left], height[right]) * (right - left)\n    max_area = max(max_area, area)\n    # ???",
                    choices = listOf(
                        choice("if height[left] < height[right]:\n    left += 1\nelse:\n    right -= 1", true, "This moves the pointer at the shorter line inward, the only move that gives the area a chance to improve, matching the current heights correctly.", code = "if height[left] < height[right]:\n    left += 1\nelse:\n    right -= 1"),
                        choice("if height[left] < height[right]:\n    right -= 1\nelse:\n    left += 1", false, "This moves the pointer at the taller line, which only shrinks the width without any chance of raising the limiting height.", code = "if height[left] < height[right]:\n    right -= 1\nelse:\n    left += 1"),
                        choice("left += 1\nright -= 1", false, "Moving both pointers on every step regardless of which height is limiting can skip past the actual best combination of heights and width.", code = "left += 1\nright -= 1"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "int left = 0;\nint right = height.length - 1;\nint maxArea = 0;\nwhile (left < right) {\n    int area = Math.min(height[left], height[right]) * (right - left);\n    maxArea = Math.max(maxArea, area);\n    // ???\n}",
                    choices = listOf(
                        choice("if (height[left] < height[right]) left++; else right--;", true, "This moves the pointer at the shorter line inward, the only move that gives the area a chance to improve, matching the current heights correctly.", code = "if (height[left] < height[right]) left++; else right--;"),
                        choice("if (height[left] < height[right]) right--; else left++;", false, "This moves the pointer at the taller line, which only shrinks the width without any chance of raising the limiting height.", code = "if (height[left] < height[right]) right--; else left++;"),
                        choice("left++;\nright--;", false, "Moving both pointers on every step regardless of which height is limiting can skip past the actual best combination of heights and width.", code = "left++;\nright--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "let left = 0;\nlet right = height.length - 1;\nlet maxArea = 0;\nwhile (left < right) {\n    const area = Math.min(height[left], height[right]) * (right - left);\n    maxArea = Math.max(maxArea, area);\n    // ???\n}",
                    choices = listOf(
                        choice("if (height[left] < height[right]) left++; else right--;", true, "This moves the pointer at the shorter line inward, the only move that gives the area a chance to improve, matching the current heights correctly.", code = "if (height[left] < height[right]) left++; else right--;"),
                        choice("if (height[left] < height[right]) right--; else left++;", false, "This moves the pointer at the taller line, which only shrinks the width without any chance of raising the limiting height.", code = "if (height[left] < height[right]) right--; else left++;"),
                        choice("left++;\nright--;", false, "Moving both pointers on every step regardless of which height is limiting can skip past the actual best combination of heights and width.", code = "left++;\nright--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "int left = 0;\nint right = height.size() - 1;\nint maxArea = 0;\nwhile (left < right) {\n    int area = min(height[left], height[right]) * (right - left);\n    maxArea = max(maxArea, area);\n    // ???\n}",
                    choices = listOf(
                        choice("if (height[left] < height[right]) left++; else right--;", true, "This moves the pointer at the shorter line inward, the only move that gives the area a chance to improve, matching the current heights correctly.", code = "if (height[left] < height[right]) left++; else right--;"),
                        choice("if (height[left] < height[right]) right--; else left++;", false, "This moves the pointer at the taller line, which only shrinks the width without any chance of raising the limiting height.", code = "if (height[left] < height[right]) right--; else left++;"),
                        choice("left++;\nright--;", false, "Moving both pointers on every step regardless of which height is limiting can skip past the actual best combination of heights and width.", code = "left++;\nright--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "left := 0\nright := len(height) - 1\nmaxArea := 0\nfor left < right {\n    area := min(height[left], height[right]) * (right - left)\n    if area > maxArea {\n        maxArea = area\n    }\n    // ???\n}",
                    choices = listOf(
                        choice("if height[left] < height[right] {\n    left++\n} else {\n    right--\n}", true, "This moves the pointer at the shorter line inward, the only move that gives the area a chance to improve, matching the current heights correctly.", code = "if height[left] < height[right] {\n    left++\n} else {\n    right--\n}"),
                        choice("if height[left] < height[right] {\n    right--\n} else {\n    left++\n}", false, "This moves the pointer at the taller line, which only shrinks the width without any chance of raising the limiting height.", code = "if height[left] < height[right] {\n    right--\n} else {\n    left++\n}"),
                        choice("left++\nright--", false, "Moving both pointers on every step regardless of which height is limiting can skip past the actual best combination of heights and width.", code = "left++\nright--"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var left = 0\nvar right = height.count - 1\nvar maxArea = 0\nwhile left < right {\n    let area = min(height[left], height[right]) * (right - left)\n    maxArea = max(maxArea, area)\n    // ???\n}",
                    choices = listOf(
                        choice("if height[left] < height[right] { left += 1 } else { right -= 1 }", true, "This moves the pointer at the shorter line inward, the only move that gives the area a chance to improve, matching the current heights correctly.", code = "if height[left] < height[right] { left += 1 } else { right -= 1 }"),
                        choice("if height[left] < height[right] { right -= 1 } else { left += 1 }", false, "This moves the pointer at the taller line, which only shrinks the width without any chance of raising the limiting height.", code = "if height[left] < height[right] { right -= 1 } else { left += 1 }"),
                        choice("left += 1\nright -= 1", false, "Moving both pointers on every step regardless of which height is limiting can skip past the actual best combination of heights and width.", code = "left += 1\nright -= 1"),
                    ),
                ),
            ),
        ),
        step(
            "container-with-most-water", TWO_POINTERS, TIME_COMPLEXITY,
            "With n as the number of heights, what is the time complexity?",
            conceptKey = "container-with-most-water-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the heights are sorted before scanning.",
                    false,
                    "Sorting the heights would break the original positions needed to compute width, so no sorting happens in this approach.",
                ),
                choice(
                    "O(n squared), because every pair of lines is checked.",
                    false,
                    "The two-pointer approach never checks every pair directly - it discards entire ranges of pairs each time a pointer moves, unlike a full pairwise check.",
                ),
                choice(
                    "O(n), because the two pointers together move at most n steps total before meeting.",
                    true,
                    "Each step moves exactly one pointer one position closer to the other, so the total number of steps across the whole run is bounded by n.",
                )),
        ),
        step(
            "container-with-most-water", TWO_POINTERS, SPACE_COMPLEXITY,
            "How much extra space does this approach use?",
            conceptKey = "container-with-most-water-space-complexity",
            choices = listOf(
                choice(
                    "O(1), because only two pointers and a running maximum are tracked.",
                    true,
                    "Nothing here scales with the size of the input - the same few variables are updated in place as the pointers move toward each other.",
                ),
                choice(
                    "O(n), because every area computed is stored to find the maximum.",
                    false,
                    "Only the single best area found so far needs to be remembered - there's no need to store every area that was ever computed.",
                ),
                choice(
                    "O(n), because a sorted copy of the heights is kept alongside the original.",
                    false,
                    "No sorted copy is made - the original array is read directly using the two pointers.",
                )),
        ),
        step(
            "container-with-most-water", TWO_POINTERS, TRANSFER,
            "Would this same shorter-side-moves-inward approach also solve Trapping Rain Water?",
            conceptKey = "container-with-most-water-transfer",
            choices = listOf(
                choice(
                    "Partially - two pointers help there too, but the state tracked differs: it needs the running maximum height seen from each side, not just the two current heights.",
                    true,
                    "Trapping Rain Water asks how much water is trapped *between* every pair of bars, not just the single best pair, so it needs to track the tallest wall seen so far from each side as the pointers move, an extra piece of state this problem doesn't need.",
                ),
                choice(
                    "Yes - the exact same code works unchanged for both problems.",
                    false,
                    "The two problems share the two-pointers-from-both-ends idea, but Trapping Rain Water needs additional state (the running max height from each side) that this problem's code doesn't track at all.",
                ),
                choice(
                    "No - Trapping Rain Water can only be solved by checking every bar against every other bar.",
                    false,
                    "Trapping Rain Water also has an efficient two-pointer solution - it just needs to track a bit more state (the tallest wall seen so far from each side) than this problem does.",
                )),
        ),
        step(
            "container-with-most-water", TWO_POINTERS, EDGE_CASE,
            "Which input would break a version of this solution that always moves the left pointer regardless of height?",
            conceptKey = "container-with-most-water-edge-case",
            choices = listOf(
                choice(
                    "Heights strictly decreasing from left to right, like [9, 7, 5, 3, 1], where the tallest line is at the very start.",
                    true,
                    "Always moving left regardless of height would abandon the tallest line immediately instead of keeping it and moving right inward, missing the actual best combination that starts from the tallest bar.",
                ),
                choice(
                    "Heights that are all equal.",
                    false,
                    "With every height identical, moving either pointer produces the same limiting height each time, so this case doesn't distinguish a correct rule from an always-move-left one.",
                ),
                choice(
                    "An array with only two heights.",
                    false,
                    "With only two lines, there's exactly one possible container and no choice of which pointer to move - this doesn't exercise the movement rule at all.",
                )),
        ),
    ),
)

private val validPalindromeWorkout = ProblemWorkout(
    problemSlug = "valid-palindrome",
    group = TWO_POINTERS,
    steps = listOf(
        step(
            "valid-palindrome", TWO_POINTERS, PATTERN_RECOGNITION,
            "Given a string, ignoring non-alphanumeric characters and case, decide whether it reads the same forward and backward. Which pattern fits?",
            conceptKey = "valid-palindrome-pattern-recognition",
            choices = listOf(
                choice(
                    "Two pointers starting at both ends, moving inward while skipping non-alphanumeric characters.",
                    true,
                    "A palindrome is defined by symmetry around the center, and two pointers closing in from both ends is the direct way to check that symmetry one matching pair at a time.",
                ),
                choice(
                    "Sliding window scanning for the longest palindromic substring.",
                    false,
                    "This problem asks whether the *whole* filtered string is a palindrome, not to find the longest palindromic piece of it - a sliding window solves a different question.",
                ),
                choice(
                    "Build a cleaned copy of the string, reverse it, and compare to the original with a hash of each half.",
                    false,
                    "Reversing and comparing works but needs extra passes and a full copy - two pointers check the same symmetry in one pass without building anything extra.",
                )),
        ),
        step(
            "valid-palindrome", TWO_POINTERS, APPROACH,
            "Which approach correctly checks the palindrome?",
            conceptKey = "valid-palindrome-approach",
            choices = listOf(
                choice(
                    "Move two pointers inward from both ends, skipping any non-alphanumeric character on either side before comparing, and fail on the first case-insensitive mismatch.",
                    true,
                    "Skipping non-alphanumeric characters directly at each pointer, rather than building a separate cleaned string first, checks symmetry in a single pass with no extra storage.",
                ),
                choice(
                    "Build a new string containing only lowercase alphanumeric characters, then check whether it equals its own reverse.",
                    false,
                    "This is also correct, but building a full cleaned copy and its reverse costs extra space that skipping characters in place while comparing avoids entirely.",
                ),
                choice(
                    "Move two pointers inward from both ends, but stop as soon as either pointer hits a non-alphanumeric character.",
                    false,
                    "Stopping instead of skipping past non-alphanumeric characters would end the check far too early on strings with punctuation or spaces mixed throughout.",
                )),
        ),
        step(
            "valid-palindrome", TWO_POINTERS, STATE_SELECTION,
            "What state does this approach need?",
            conceptKey = "valid-palindrome-state-selection",
            choices = listOf(
                choice(
                    "A left pointer and a right pointer walking the original string inward.",
                    true,
                    "Everything needed - which characters to compare next - is captured by these two positions moving toward each other through the original string.",
                ),
                choice(
                    "A fully cleaned, lowercase copy of the string built up front.",
                    false,
                    "Building a whole separate copy first works but isn't necessary - the two pointers can skip irrelevant characters directly in the original string as they move.",
                ),
                choice(
                    "A count of how many alphanumeric characters exist in the string.",
                    false,
                    "A count of alphanumeric characters doesn't tell you anything about whether they read the same forward and backward - the actual characters and their order matter, not just how many there are.",
                )),
        ),
        step(
            "valid-palindrome", TWO_POINTERS, BOUNDARY_UPDATE,
            "Which rule correctly advances the pointers?",
            conceptKey = "valid-palindrome-boundary-update",
            choices = listOf(
                choice(
                    "If left points at a non-alphanumeric character, move it forward once; then compare regardless of what right currently points at.",
                    false,
                    "Skipping only the left pointer, and only once, leaves the right pointer's own non-alphanumeric characters uncompared-against correctly, and a single skip misses runs of multiple such characters.",
                ),
                choice(
                    "While left points at a non-alphanumeric character, move it forward; while right points at one, move it backward; then compare the two characters case-insensitively and move both inward.",
                    true,
                    "Skipping with a while loop (not just an if) on both sides handles runs of multiple punctuation or space characters in a row before ever comparing anything.",
                ),
                choice(
                    "Compare the characters at left and right first, and only skip non-alphanumeric characters if the comparison fails.",
                    false,
                    "Comparing before skipping means punctuation or spaces get compared directly against letters, which would incorrectly fail on strings that are actually valid palindromes once filtered.",
                )),
        ),
        step(
            "valid-palindrome", TWO_POINTERS, CODE_BLOCK,
            "Which snippet correctly implements the pointer loop?",
            conceptKey = "valid-palindrome-code-block",
            code = "var left = 0\nvar right = s.length - 1\nwhile (left < right) {\n    // ???\n    if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false\n    left++\n    right--\n}\nreturn true",
            choices = listOf(
                choice(
                    "while (left < right && !s[left].isLetterOrDigit()) left++\nwhile (left < right && !s[right].isLetterOrDigit()) right--",
                    true,
                    "Both while loops guard with left < right and keep skipping past every non-alphanumeric character on each side before the comparison runs, correctly handling runs of punctuation or spaces.",
                    code = "while (left < right && !s[left].isLetterOrDigit()) left++\nwhile (left < right && !s[right].isLetterOrDigit()) right--",
                ),
                choice(
                    "if (!s[left].isLetterOrDigit()) left++\nif (!s[right].isLetterOrDigit()) right--",
                    false,
                    "Using if instead of while only skips a single character at a time, so a run of two or more non-alphanumeric characters in a row would still leave one unskipped before comparing.",
                    code = "if (!s[left].isLetterOrDigit()) left++\nif (!s[right].isLetterOrDigit()) right--",
                ),
                choice(
                    "while (!s[left].isLetterOrDigit()) left++\nwhile (!s[right].isLetterOrDigit()) right--",
                    false,
                    "Without the left < right guard, a string that's entirely punctuation could run a pointer straight past the other end of the string, reading out of bounds.",
                    code = "while (!s[left].isLetterOrDigit()) left++\nwhile (!s[right].isLetterOrDigit()) right--",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "left = 0\nright = len(s) - 1\nwhile left < right:\n    # ???\n    if s[left].lower() != s[right].lower():\n        return False\n    left += 1\n    right -= 1\nreturn True",
                    choices = listOf(
                        choice("while left < right and not s[left].isalnum():\n    left += 1\nwhile left < right and not s[right].isalnum():\n    right -= 1", true, "Both while loops guard with left < right and keep skipping past every non-alphanumeric character on each side before the comparison runs, correctly handling runs of punctuation or spaces.", code = "while left < right and not s[left].isalnum():\n    left += 1\nwhile left < right and not s[right].isalnum():\n    right -= 1"),
                        choice("if not s[left].isalnum():\n    left += 1\nif not s[right].isalnum():\n    right -= 1", false, "Using if instead of while only skips a single character at a time, so a run of two or more non-alphanumeric characters in a row would still leave one unskipped before comparing.", code = "if not s[left].isalnum():\n    left += 1\nif not s[right].isalnum():\n    right -= 1"),
                        choice("while not s[left].isalnum():\n    left += 1\nwhile not s[right].isalnum():\n    right -= 1", false, "Without the left < right guard, a string that's entirely punctuation could run a pointer straight past the other end of the string, reading out of bounds.", code = "while not s[left].isalnum():\n    left += 1\nwhile not s[right].isalnum():\n    right -= 1"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "int left = 0;\nint right = s.length() - 1;\nwhile (left < right) {\n    // ???\n    if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) return false;\n    left++;\n    right--;\n}\nreturn true;",
                    choices = listOf(
                        choice("while (left < right && !Character.isLetterOrDigit(s.charAt(left))) left++;\nwhile (left < right && !Character.isLetterOrDigit(s.charAt(right))) right--;", true, "Both while loops guard with left < right and keep skipping past every non-alphanumeric character on each side before the comparison runs, correctly handling runs of punctuation or spaces.", code = "while (left < right && !Character.isLetterOrDigit(s.charAt(left))) left++;\nwhile (left < right && !Character.isLetterOrDigit(s.charAt(right))) right--;"),
                        choice("if (!Character.isLetterOrDigit(s.charAt(left))) left++;\nif (!Character.isLetterOrDigit(s.charAt(right))) right--;", false, "Using if instead of while only skips a single character at a time, so a run of two or more non-alphanumeric characters in a row would still leave one unskipped before comparing.", code = "if (!Character.isLetterOrDigit(s.charAt(left))) left++;\nif (!Character.isLetterOrDigit(s.charAt(right))) right--;"),
                        choice("while (!Character.isLetterOrDigit(s.charAt(left))) left++;\nwhile (!Character.isLetterOrDigit(s.charAt(right))) right--;", false, "Without the left < right guard, a string that's entirely punctuation could run a pointer straight past the other end of the string, reading out of bounds.", code = "while (!Character.isLetterOrDigit(s.charAt(left))) left++;\nwhile (!Character.isLetterOrDigit(s.charAt(right))) right--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "let left = 0;\nlet right = s.length - 1;\nwhile (left < right) {\n    // ???\n    if (s[left].toLowerCase() !== s[right].toLowerCase()) return false;\n    left++;\n    right--;\n}\nreturn true;",
                    choices = listOf(
                        choice("while (left < right && !/[a-z0-9]/i.test(s[left])) left++;\nwhile (left < right && !/[a-z0-9]/i.test(s[right])) right--;", true, "Both while loops guard with left < right and keep skipping past every non-alphanumeric character on each side before the comparison runs, correctly handling runs of punctuation or spaces.", code = "while (left < right && !/[a-z0-9]/i.test(s[left])) left++;\nwhile (left < right && !/[a-z0-9]/i.test(s[right])) right--;"),
                        choice("if (!/[a-z0-9]/i.test(s[left])) left++;\nif (!/[a-z0-9]/i.test(s[right])) right--;", false, "Using if instead of while only skips a single character at a time, so a run of two or more non-alphanumeric characters in a row would still leave one unskipped before comparing.", code = "if (!/[a-z0-9]/i.test(s[left])) left++;\nif (!/[a-z0-9]/i.test(s[right])) right--;"),
                        choice("while (!/[a-z0-9]/i.test(s[left])) left++;\nwhile (!/[a-z0-9]/i.test(s[right])) right--;", false, "Without the left < right guard, a string that's entirely punctuation could run a pointer straight past the other end of the string, reading out of bounds.", code = "while (!/[a-z0-9]/i.test(s[left])) left++;\nwhile (!/[a-z0-9]/i.test(s[right])) right--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "int left = 0;\nint right = s.size() - 1;\nwhile (left < right) {\n    // ???\n    if (tolower(s[left]) != tolower(s[right])) return false;\n    left++;\n    right--;\n}\nreturn true;",
                    choices = listOf(
                        choice("while (left < right && !isalnum(s[left])) left++;\nwhile (left < right && !isalnum(s[right])) right--;", true, "Both while loops guard with left < right and keep skipping past every non-alphanumeric character on each side before the comparison runs, correctly handling runs of punctuation or spaces.", code = "while (left < right && !isalnum(s[left])) left++;\nwhile (left < right && !isalnum(s[right])) right--;"),
                        choice("if (!isalnum(s[left])) left++;\nif (!isalnum(s[right])) right--;", false, "Using if instead of while only skips a single character at a time, so a run of two or more non-alphanumeric characters in a row would still leave one unskipped before comparing.", code = "if (!isalnum(s[left])) left++;\nif (!isalnum(s[right])) right--;"),
                        choice("while (!isalnum(s[left])) left++;\nwhile (!isalnum(s[right])) right--;", false, "Without the left < right guard, a string that's entirely punctuation could run a pointer straight past the other end of the string, reading out of bounds.", code = "while (!isalnum(s[left])) left++;\nwhile (!isalnum(s[right])) right--;"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "left := 0\nright := len(s) - 1\nfor left < right {\n    // ???\n    if unicode.ToLower(rune(s[left])) != unicode.ToLower(rune(s[right])) {\n        return false\n    }\n    left++\n    right--\n}\nreturn true",
                    choices = listOf(
                        choice("for left < right && !isAlnum(s[left]) {\n    left++\n}\nfor left < right && !isAlnum(s[right]) {\n    right--\n}", true, "Both loops guard with left < right and keep skipping past every non-alphanumeric character on each side before the comparison runs, correctly handling runs of punctuation or spaces.", code = "for left < right && !isAlnum(s[left]) {\n    left++\n}\nfor left < right && !isAlnum(s[right]) {\n    right--\n}"),
                        choice("if !isAlnum(s[left]) {\n    left++\n}\nif !isAlnum(s[right]) {\n    right--\n}", false, "Using a single if instead of a loop only skips a single character at a time, so a run of two or more non-alphanumeric characters in a row would still leave one unskipped before comparing.", code = "if !isAlnum(s[left]) {\n    left++\n}\nif !isAlnum(s[right]) {\n    right--\n}"),
                        choice("for !isAlnum(s[left]) {\n    left++\n}\nfor !isAlnum(s[right]) {\n    right--\n}", false, "Without the left < right guard, a string that's entirely punctuation could run a pointer straight past the other end of the string, reading out of bounds.", code = "for !isAlnum(s[left]) {\n    left++\n}\nfor !isAlnum(s[right]) {\n    right--\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "let chars = Array(s)\nvar left = 0\nvar right = chars.count - 1\nwhile left < right {\n    // ???\n    if Character(chars[left].lowercased()) != Character(chars[right].lowercased()) { return false }\n    left += 1\n    right -= 1\n}\nreturn true",
                    choices = listOf(
                        choice("while left < right && !(chars[left].isLetter || chars[left].isNumber) { left += 1 }\nwhile left < right && !(chars[right].isLetter || chars[right].isNumber) { right -= 1 }", true, "Both while loops guard with left < right and keep skipping past every non-alphanumeric character on each side before the comparison runs, correctly handling runs of punctuation or spaces.", code = "while left < right && !(chars[left].isLetter || chars[left].isNumber) { left += 1 }\nwhile left < right && !(chars[right].isLetter || chars[right].isNumber) { right -= 1 }"),
                        choice("if !(chars[left].isLetter || chars[left].isNumber) { left += 1 }\nif !(chars[right].isLetter || chars[right].isNumber) { right -= 1 }", false, "Using if instead of while only skips a single character at a time, so a run of two or more non-alphanumeric characters in a row would still leave one unskipped before comparing.", code = "if !(chars[left].isLetter || chars[left].isNumber) { left += 1 }\nif !(chars[right].isLetter || chars[right].isNumber) { right -= 1 }"),
                        choice("while !(chars[left].isLetter || chars[left].isNumber) { left += 1 }\nwhile !(chars[right].isLetter || chars[right].isNumber) { right -= 1 }", false, "Without the left < right guard, a string that's entirely punctuation could run a pointer straight past the other end of the string, reading out of bounds.", code = "while !(chars[left].isLetter || chars[left].isNumber) { left += 1 }\nwhile !(chars[right].isLetter || chars[right].isNumber) { right -= 1 }"),
                    ),
                ),
            ),
        ),
        step(
            "valid-palindrome", TWO_POINTERS, TIME_COMPLEXITY,
            "With n as the string's length, what is the time complexity?",
            conceptKey = "valid-palindrome-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the string must be sorted before comparing.",
                    false,
                    "Checking a palindrome has nothing to do with sorting - it's about comparing characters in their original mirrored positions, not their sorted order.",
                ),
                choice(
                    "O(n squared), because each character is compared against every other character.",
                    false,
                    "Only characters at mirrored positions are ever compared to each other, not every character against every other one.",
                ),
                choice(
                    "O(n), because the two pointers together visit each character at most once as they move inward.",
                    true,
                    "Every character, whether skipped or compared, is only ever looked at once by one of the two pointers moving toward the center.",
                )),
        ),
        step(
            "valid-palindrome", TWO_POINTERS, SPACE_COMPLEXITY,
            "How much extra space does the in-place, skip-as-you-go version use?",
            conceptKey = "valid-palindrome-space-complexity",
            choices = listOf(
                choice(
                    "O(1), because only two pointers are tracked and the original string is read in place.",
                    true,
                    "No new string is built - non-alphanumeric characters are simply skipped over as the two pointers move through the existing string.",
                ),
                choice(
                    "O(n), because a cleaned, lowercase copy of the string is built first.",
                    false,
                    "That describes the alternative build-a-copy-then-reverse approach - the in-place two-pointer version skips characters directly without ever building a new string.",
                ),
                choice(
                    "O(n), because the reversed string is stored to compare against the original.",
                    false,
                    "No reversed string is created - the two pointers compare mirrored characters directly as they move toward each other.",
                )),
        ),
        step(
            "valid-palindrome", TWO_POINTERS, TRANSFER,
            "Would this same two-pointer approach also work for Valid Palindrome II, which allows deleting at most one character?",
            conceptKey = "valid-palindrome-transfer",
            choices = listOf(
                choice(
                    "Yes, with one addition - on the first mismatch, try skipping either the left or the right character and check if what remains is a palindrome.",
                    true,
                    "The same inward-closing pointers still do the comparing; the only new piece is a fallback branch that tries both possible single deletions the moment a mismatch is found.",
                ),
                choice(
                    "No - allowing one deletion requires checking every possible way to remove a character from the string.",
                    false,
                    "Trying every possible deletion would be far more work than necessary - only the exact mismatch position needs a fallback check, not every position in the string.",
                ),
                choice(
                    "No - two pointers can only verify exact palindromes, never approximate ones.",
                    false,
                    "Two pointers can absolutely handle the 'allow one skip' variant - it just needs a small branch at the first mismatch to try skipping one side or the other.",
                )),
        ),
        step(
            "valid-palindrome", TWO_POINTERS, EDGE_CASE,
            "Which input would break a version of this solution that doesn't guard the skip loops with left < right?",
            conceptKey = "valid-palindrome-edge-case",
            choices = listOf(
                choice(
                    "A string made entirely of punctuation and spaces, like \"...,,,...\", with no alphanumeric characters at all.",
                    true,
                    "Without the left < right guard, the skip loop for left would run straight past right and off the end of the string looking for a letter or digit that never appears.",
                ),
                choice(
                    "A single-character alphanumeric string.",
                    false,
                    "A single character trivially satisfies left >= right immediately and returns true without ever needing to skip anything.",
                ),
                choice(
                    "A string that is already a palindrome with no punctuation at all.",
                    false,
                    "With nothing to skip, the guard on the skip loops never comes into play - this case doesn't exercise the out-of-bounds risk at all.",
                )),
        ),
    ),
)

private val bestTimeToBuyAndSellStockWorkout = ProblemWorkout(
    problemSlug = "best-time-to-buy-and-sell-stock",
    group = SLIDING_WINDOW,
    steps = listOf(
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, PATTERN_RECOGNITION,
            "Given daily prices, find the maximum profit from buying on one day and selling on a later day. Which pattern fits?",
            conceptKey = "best-time-to-buy-and-sell-stock-pattern-recognition",
            choices = listOf(
                choice(
                    "A window with an implicit left edge: track the minimum price seen so far as the buy day, and the best profit against it, in one pass.",
                    true,
                    "The buy day only ever needs to be the lowest price seen *before* the current day - tracking that running minimum as an implicit window edge finds the answer in a single linear scan.",
                ),
                choice(
                    "Two pointers starting at both ends of the price array, moving inward.",
                    false,
                    "The best buy and sell days aren't necessarily positioned symmetrically from the two ends of the array - there's no reason to anchor a pointer at the very last day.",
                ),
                choice(
                    "Check every pair of buy and sell days directly.",
                    false,
                    "This finds the correct answer but costs O(n squared), while tracking a running minimum price achieves the same result in O(n).",
                )),
        ),
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, APPROACH,
            "Which approach correctly finds the maximum profit?",
            conceptKey = "best-time-to-buy-and-sell-stock-approach",
            choices = listOf(
                choice(
                    "Scan once, tracking the minimum price seen so far; at each day, compute the profit from selling today against that minimum, and keep the best profit found.",
                    true,
                    "Because the buy day must come before the sell day, comparing today's price only against the lowest price seen *so far* (not the whole array) is exactly what a valid buy-then-sell pair requires.",
                ),
                choice(
                    "Scan once, tracking the minimum price seen so far; at each day, compute the profit from selling today against the minimum of the *entire* array.",
                    false,
                    "Using the overall minimum, even if it occurs after the current day, could produce a profit from selling before buying, which isn't a valid transaction.",
                ),
                choice(
                    "Sort the prices and take the difference between the largest and smallest values.",
                    false,
                    "Sorting destroys the original day order, but a valid sale must happen on a day *after* the purchase - a sorted array loses that ordering entirely.",
                )),
        ),
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, STATE_SELECTION,
            "What state does this approach need?",
            conceptKey = "best-time-to-buy-and-sell-stock-state-selection",
            choices = listOf(
                choice(
                    "The minimum price seen so far, and the maximum profit found so far.",
                    true,
                    "These two running values are exactly enough to evaluate every day's potential sale against the best possible earlier buy day, without storing anything about earlier days individually.",
                ),
                choice(
                    "A sorted copy of all the prices.",
                    false,
                    "Sorting would scramble which day each price belongs to, but the sell day must come after the buy day - that ordering has to be preserved.",
                ),
                choice(
                    "A list of every profit computed between every pair of days.",
                    false,
                    "Only the single best profit found so far needs to be remembered - there's no need to store every possible pair's profit.",
                )),
        ),
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, BOUNDARY_UPDATE,
            "Which rule correctly updates the running minimum and best profit at each day?",
            conceptKey = "best-time-to-buy-and-sell-stock-boundary-update",
            choices = listOf(
                choice(
                    "Update the minimum with today's price first, then compute the best profit using today's price minus the (possibly just-updated) minimum.",
                    false,
                    "Updating the minimum first can let today's own price be used as the buy price for today's own sale, which always produces zero profit and can mask a better answer from an earlier day.",
                ),
                choice(
                    "Update the best profit using today's price minus the minimum seen so far, then update the minimum with today's price if it's lower.",
                    true,
                    "Computing the profit *before* updating the minimum with today's price ensures today is never treated as its own buy day for a same-day sale.",
                ),
                choice(
                    "Only update the minimum price; recompute the best profit once at the very end by re-scanning all days.",
                    false,
                    "This works but requires a second pass over the array, where tracking the running best profit alongside the running minimum finds the same answer in a single pass.",
                )),
        ),
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, CODE_BLOCK,
            "Which snippet correctly implements the single-pass scan?",
            conceptKey = "best-time-to-buy-and-sell-stock-code-block",
            code = "var minPrice = Int.MAX_VALUE\nvar maxProfit = 0\nfor (price in prices) {\n    // ???\n}\nreturn maxProfit",
            choices = listOf(
                choice(
                    "maxProfit = maxOf(maxProfit, price - minPrice)\nminPrice = minOf(minPrice, price)",
                    true,
                    "Computing the profit against the minimum from *before* this day, then updating the minimum afterward, guarantees the buy day is always strictly earlier than the sell day being evaluated.",
                    code = "maxProfit = maxOf(maxProfit, price - minPrice)\nminPrice = minOf(minPrice, price)",
                ),
                choice(
                    "minPrice = minOf(minPrice, price)\nmaxProfit = maxOf(maxProfit, price - minPrice)",
                    false,
                    "Updating the minimum first lets today's own price become the buy price used against today's own sale price, which always yields zero and can hide the true best answer.",
                    code = "minPrice = minOf(minPrice, price)\nmaxProfit = maxOf(maxProfit, price - minPrice)",
                ),
                choice(
                    "maxProfit = maxOf(maxProfit, price)\nminPrice = minOf(minPrice, price)",
                    false,
                    "This tracks the maximum price seen, not the maximum *profit* - it never actually subtracts a buy price, so it doesn't compute profit at all.",
                    code = "maxProfit = maxOf(maxProfit, price)\nminPrice = minOf(minPrice, price)",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "min_price = float(\"inf\")\nmax_profit = 0\nfor price in prices:\n    # ???\nreturn max_profit",
                    choices = listOf(
                        choice("max_profit = max(max_profit, price - min_price)\nmin_price = min(min_price, price)", true, "Computing the profit against the minimum from before this day, then updating the minimum afterward, guarantees the buy day is always strictly earlier than the sell day being evaluated.", code = "max_profit = max(max_profit, price - min_price)\nmin_price = min(min_price, price)"),
                        choice("min_price = min(min_price, price)\nmax_profit = max(max_profit, price - min_price)", false, "Updating the minimum first lets today's own price become the buy price used against today's own sale price, which always yields zero and can hide the true best answer.", code = "min_price = min(min_price, price)\nmax_profit = max(max_profit, price - min_price)"),
                        choice("max_profit = max(max_profit, price)\nmin_price = min(min_price, price)", false, "This tracks the maximum price seen, not the maximum profit - it never actually subtracts a buy price, so it doesn't compute profit at all.", code = "max_profit = max(max_profit, price)\nmin_price = min(min_price, price)"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "int minPrice = Integer.MAX_VALUE;\nint maxProfit = 0;\nfor (int price : prices) {\n    // ???\n}\nreturn maxProfit;",
                    choices = listOf(
                        choice("maxProfit = Math.max(maxProfit, price - minPrice);\nminPrice = Math.min(minPrice, price);", true, "Computing the profit against the minimum from before this day, then updating the minimum afterward, guarantees the buy day is always strictly earlier than the sell day being evaluated.", code = "maxProfit = Math.max(maxProfit, price - minPrice);\nminPrice = Math.min(minPrice, price);"),
                        choice("minPrice = Math.min(minPrice, price);\nmaxProfit = Math.max(maxProfit, price - minPrice);", false, "Updating the minimum first lets today's own price become the buy price used against today's own sale price, which always yields zero and can hide the true best answer.", code = "minPrice = Math.min(minPrice, price);\nmaxProfit = Math.max(maxProfit, price - minPrice);"),
                        choice("maxProfit = Math.max(maxProfit, price);\nminPrice = Math.min(minPrice, price);", false, "This tracks the maximum price seen, not the maximum profit - it never actually subtracts a buy price, so it doesn't compute profit at all.", code = "maxProfit = Math.max(maxProfit, price);\nminPrice = Math.min(minPrice, price);"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "let minPrice = Infinity;\nlet maxProfit = 0;\nfor (const price of prices) {\n    // ???\n}\nreturn maxProfit;",
                    choices = listOf(
                        choice("maxProfit = Math.max(maxProfit, price - minPrice);\nminPrice = Math.min(minPrice, price);", true, "Computing the profit against the minimum from before this day, then updating the minimum afterward, guarantees the buy day is always strictly earlier than the sell day being evaluated.", code = "maxProfit = Math.max(maxProfit, price - minPrice);\nminPrice = Math.min(minPrice, price);"),
                        choice("minPrice = Math.min(minPrice, price);\nmaxProfit = Math.max(maxProfit, price - minPrice);", false, "Updating the minimum first lets today's own price become the buy price used against today's own sale price, which always yields zero and can hide the true best answer.", code = "minPrice = Math.min(minPrice, price);\nmaxProfit = Math.max(maxProfit, price - minPrice);"),
                        choice("maxProfit = Math.max(maxProfit, price);\nminPrice = Math.min(minPrice, price);", false, "This tracks the maximum price seen, not the maximum profit - it never actually subtracts a buy price, so it doesn't compute profit at all.", code = "maxProfit = Math.max(maxProfit, price);\nminPrice = Math.min(minPrice, price);"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "int minPrice = INT_MAX;\nint maxProfit = 0;\nfor (int price : prices) {\n    // ???\n}\nreturn maxProfit;",
                    choices = listOf(
                        choice("maxProfit = max(maxProfit, price - minPrice);\nminPrice = min(minPrice, price);", true, "Computing the profit against the minimum from before this day, then updating the minimum afterward, guarantees the buy day is always strictly earlier than the sell day being evaluated.", code = "maxProfit = max(maxProfit, price - minPrice);\nminPrice = min(minPrice, price);"),
                        choice("minPrice = min(minPrice, price);\nmaxProfit = max(maxProfit, price - minPrice);", false, "Updating the minimum first lets today's own price become the buy price used against today's own sale price, which always yields zero and can hide the true best answer.", code = "minPrice = min(minPrice, price);\nmaxProfit = max(maxProfit, price - minPrice);"),
                        choice("maxProfit = max(maxProfit, price);\nminPrice = min(minPrice, price);", false, "This tracks the maximum price seen, not the maximum profit - it never actually subtracts a buy price, so it doesn't compute profit at all.", code = "maxProfit = max(maxProfit, price);\nminPrice = min(minPrice, price);"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "minPrice := math.MaxInt32\nmaxProfit := 0\nfor _, price := range prices {\n    // ???\n}\nreturn maxProfit",
                    choices = listOf(
                        choice("if price-minPrice > maxProfit {\n    maxProfit = price - minPrice\n}\nif price < minPrice {\n    minPrice = price\n}", true, "Computing the profit against the minimum from before this day, then updating the minimum afterward, guarantees the buy day is always strictly earlier than the sell day being evaluated.", code = "if price-minPrice > maxProfit {\n    maxProfit = price - minPrice\n}\nif price < minPrice {\n    minPrice = price\n}"),
                        choice("if price < minPrice {\n    minPrice = price\n}\nif price-minPrice > maxProfit {\n    maxProfit = price - minPrice\n}", false, "Updating the minimum first lets today's own price become the buy price used against today's own sale price, which always yields zero and can hide the true best answer.", code = "if price < minPrice {\n    minPrice = price\n}\nif price-minPrice > maxProfit {\n    maxProfit = price - minPrice\n}"),
                        choice("if price > maxProfit {\n    maxProfit = price\n}\nif price < minPrice {\n    minPrice = price\n}", false, "This tracks the maximum price seen, not the maximum profit - it never actually subtracts a buy price, so it doesn't compute profit at all.", code = "if price > maxProfit {\n    maxProfit = price\n}\nif price < minPrice {\n    minPrice = price\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var minPrice = Int.max\nvar maxProfit = 0\nfor price in prices {\n    // ???\n}\nreturn maxProfit",
                    choices = listOf(
                        choice("maxProfit = max(maxProfit, price - minPrice)\nminPrice = min(minPrice, price)", true, "Computing the profit against the minimum from before this day, then updating the minimum afterward, guarantees the buy day is always strictly earlier than the sell day being evaluated.", code = "maxProfit = max(maxProfit, price - minPrice)\nminPrice = min(minPrice, price)"),
                        choice("minPrice = min(minPrice, price)\nmaxProfit = max(maxProfit, price - minPrice)", false, "Updating the minimum first lets today's own price become the buy price used against today's own sale price, which always yields zero and can hide the true best answer.", code = "minPrice = min(minPrice, price)\nmaxProfit = max(maxProfit, price - minPrice)"),
                        choice("maxProfit = max(maxProfit, price)\nminPrice = min(minPrice, price)", false, "This tracks the maximum price seen, not the maximum profit - it never actually subtracts a buy price, so it doesn't compute profit at all.", code = "maxProfit = max(maxProfit, price)\nminPrice = min(minPrice, price)"),
                    ),
                ),
            ),
        ),
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, TIME_COMPLEXITY,
            "With n as the number of days, what is the time complexity?",
            conceptKey = "best-time-to-buy-and-sell-stock-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the prices are sorted first.",
                    false,
                    "No sorting happens here - the day order must be preserved since a sale can only happen after its matching purchase.",
                ),
                choice(
                    "O(n squared), because every pair of buy and sell days is checked.",
                    false,
                    "Tracking a running minimum avoids ever comparing every pair of days directly - each day is only ever compared against the single running minimum.",
                ),
                choice(
                    "O(n), because each day updates the running minimum and best profit in constant time.",
                    true,
                    "One pass through the prices, with a fixed, small amount of work per day, gives time proportional to n.",
                )),
        ),
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, SPACE_COMPLEXITY,
            "How much extra space does this approach use?",
            conceptKey = "best-time-to-buy-and-sell-stock-space-complexity",
            choices = listOf(
                choice(
                    "O(1), because only the running minimum price and running best profit are tracked.",
                    true,
                    "Nothing here scales with the number of days - the same two variables are updated in place as the scan proceeds.",
                ),
                choice(
                    "O(n), because every day's profit against the current minimum is stored.",
                    false,
                    "Only the single best profit found so far needs to be kept - there's no need to store every day's individual profit calculation.",
                ),
                choice(
                    "O(n), because a sorted copy of the prices is kept.",
                    false,
                    "No sorted copy is made - prices are read once in their original order as the scan proceeds.",
                )),
        ),
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, TRANSFER,
            "Would this same running-best-so-far idea also work for the classic Maximum Subarray problem (largest sum of any contiguous subarray)?",
            conceptKey = "best-time-to-buy-and-sell-stock-transfer",
            choices = listOf(
                choice(
                    "Yes - tracking a running best (there, the best sum ending at the current position) instead of restarting from scratch at every position is the same underlying idea.",
                    true,
                    "Both problems share the same shape: rather than recomputing from every possible starting point, carry forward one running value (minimum price, or best running sum) and update a single best-so-far answer alongside it.",
                ),
                choice(
                    "No - Maximum Subarray requires checking every possible contiguous subarray directly.",
                    false,
                    "Maximum Subarray also has an O(n) running-best solution (Kadane's algorithm) - it doesn't need to check every subarray individually, following the same one-pass, running-value idea.",
                ),
                choice(
                    "No - this technique only applies to buy-and-sell problems specifically.",
                    false,
                    "The 'track one running value, update a best-so-far answer in a single pass' idea generalizes well beyond stock prices to many problems that ask for a best value ending at each position.",
                )),
        ),
        step(
            "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, EDGE_CASE,
            "Which input would break a version of this solution that doesn't guard against a negative result?",
            conceptKey = "best-time-to-buy-and-sell-stock-edge-case",
            choices = listOf(
                choice(
                    "Prices strictly decreasing every day, like [7, 6, 4, 3, 1], where no profitable sale is ever possible.",
                    true,
                    "With prices only ever falling, every possible sale price minus the minimum-so-far is zero or negative - initializing maxProfit to 0 and only ever raising it (never lowering it) is what correctly returns 0 instead of a negative number.",
                ),
                choice(
                    "Prices strictly increasing every day.",
                    false,
                    "Strictly increasing prices produce a straightforward best profit of the last price minus the first - this doesn't expose any risk of a negative result.",
                ),
                choice(
                    "An array with only one price.",
                    false,
                    "With a single day, no sale is possible at all, but this doesn't specifically test the negative-profit guard - it tests that a loop over one element still behaves correctly.",
                )),
        ),
    ),
)

private val longestSubstringWorkout = ProblemWorkout(
    problemSlug = "longest-substring-without-repeating-characters",
    group = SLIDING_WINDOW,
    steps = listOf(
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, PATTERN_RECOGNITION,
            "Given a string, find the length of the longest substring with no repeating characters. Which pattern fits?",
            conceptKey = "longest-substring-without-repeating-characters-pattern-recognition",
            choices = listOf(
                choice(
                    "Sliding window: grow a window from the right, and shrink it from the left whenever a repeat appears inside it.",
                    true,
                    "The window only ever needs to shrink when its own character set is violated by a new duplicate, which is exactly the grow-and-shrink-on-violation shape a sliding window handles.",
                ),
                choice(
                    "Two pointers starting at both ends of the string, moving inward.",
                    false,
                    "The longest repeat-free substring isn't necessarily anchored at the two ends of the string - it could be entirely in the middle, so there's no reason to start pointers there.",
                ),
                choice(
                    "Check every possible substring for repeated characters.",
                    false,
                    "This finds the correct answer but costs far more than necessary, recomputing overlapping substrings from scratch instead of adjusting one window incrementally.",
                )),
        ),
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, APPROACH,
            "Which approach correctly finds the longest window?",
            conceptKey = "longest-substring-without-repeating-characters-approach",
            choices = listOf(
                choice(
                    "Expand the window right by one each step, tracking characters currently inside it; whenever the new character is already in the window, shrink from the left until it's no longer a duplicate.",
                    true,
                    "Shrinking only exactly as far as needed to remove the specific duplicate keeps the window as large as possible at every step while always staying repeat-free.",
                ),
                choice(
                    "Expand the window right by one each step; whenever any repeat is found anywhere in the string so far, reset the window entirely and start over from the current position.",
                    false,
                    "Resetting the whole window throws away characters that are still valid and repeat-free - only the portion up through and including the earlier occurrence of the duplicate needs to be dropped.",
                ),
                choice(
                    "Try every possible starting index, expanding right until a repeat is found, and keep the longest run found across all starting points.",
                    false,
                    "Restarting the scan from every possible starting index recomputes overlapping work that a single sliding window already tracks incrementally.",
                )),
        ),
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, STATE_SELECTION,
            "What state does the window need?",
            conceptKey = "longest-substring-without-repeating-characters-state-selection",
            choices = listOf(
                choice(
                    "A left pointer and a hash set (or map from character to its most recent index) of characters currently in the window.",
                    true,
                    "The left pointer marks the window's start, and knowing which characters are currently inside is exactly what's needed to detect a duplicate the moment it appears.",
                ),
                choice(
                    "A sorted copy of the string.",
                    false,
                    "Sorting would destroy the original character order, but the answer depends on a contiguous run of characters in their original sequence.",
                ),
                choice(
                    "A count of the total number of distinct characters in the whole string.",
                    false,
                    "The overall count of distinct characters in the string doesn't tell you anything about how long a repeat-free *contiguous* run can be.",
                )),
        ),
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, BOUNDARY_UPDATE,
            "Which rule correctly grows and shrinks the window's boundaries?",
            conceptKey = "longest-substring-without-repeating-characters-boundary-update",
            choices = listOf(
                choice(
                    "Add the new right character to the window's set; if it already exists in the set, remove only the character currently at the left pointer once, without checking again.",
                    false,
                    "A single removal might not be enough - if the duplicate character isn't the very first one in the window, one removal from the left won't yet make the window valid again.",
                ),
                choice(
                    "Add the new right character to the window's set; while it now appears more than once in the window, remove the character at the left pointer from the set and move left forward.",
                    true,
                    "Shrinking in a while loop, checking the set after each removal, correctly handles the case where a single removal isn't yet enough to resolve the duplicate.",
                ),
                choice(
                    "Add the new right character to the window's set; if it exists elsewhere in the window, clear the entire set and start the window over from the right pointer's position.",
                    false,
                    "Clearing the whole window discards characters between the old duplicate and the current position that are still perfectly valid to keep in a shrunk window.",
                )),
        ),
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, CODE_BLOCK,
            "Which snippet correctly implements the grow-and-shrink loop?",
            conceptKey = "longest-substring-without-repeating-characters-code-block",
            code = "var left = 0\nvar maxLen = 0\nval window = HashSet<Char>()\nfor (right in s.indices) {\n    // ???\n    window.add(s[right])\n    maxLen = maxOf(maxLen, right - left + 1)\n}",
            choices = listOf(
                choice(
                    "while (s[right] in window) {\n    window.remove(s[left])\n    left++\n}",
                    true,
                    "This shrinks the window from the left, one character at a time, exactly until the specific duplicate character is removed, before the new character is added and the length is recorded.",
                    code = "while (s[right] in window) {\n    window.remove(s[left])\n    left++\n}",
                ),
                choice(
                    "if (s[right] in window) {\n    window.remove(s[left])\n    left++\n}",
                    false,
                    "A single if only removes one character, which might not actually remove the specific duplicate if it isn't the leftmost character in the window.",
                    code = "if (s[right] in window) {\n    window.remove(s[left])\n    left++\n}",
                ),
                choice(
                    "if (s[right] in window) {\n    window.clear()\n    left = right\n}",
                    false,
                    "Clearing the entire window and jumping left all the way to right throws away every character between the old duplicate and the current position that could still be part of a valid window.",
                    code = "if (s[right] in window) {\n    window.clear()\n    left = right\n}",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "left = 0\nmax_len = 0\nwindow = set()\nfor right in range(len(s)):\n    # ???\n    window.add(s[right])\n    max_len = max(max_len, right - left + 1)",
                    choices = listOf(
                        choice("while s[right] in window:\n    window.remove(s[left])\n    left += 1", true, "This shrinks the window from the left, one character at a time, exactly until the specific duplicate character is removed, before the new character is added and the length is recorded.", code = "while s[right] in window:\n    window.remove(s[left])\n    left += 1"),
                        choice("if s[right] in window:\n    window.remove(s[left])\n    left += 1", false, "A single if only removes one character, which might not actually remove the specific duplicate if it isn't the leftmost character in the window.", code = "if s[right] in window:\n    window.remove(s[left])\n    left += 1"),
                        choice("if s[right] in window:\n    window.clear()\n    left = right", false, "Clearing the entire window and jumping left all the way to right throws away every character between the old duplicate and the current position that could still be part of a valid window.", code = "if s[right] in window:\n    window.clear()\n    left = right"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "int left = 0;\nint maxLen = 0;\nSet<Character> window = new HashSet<>();\nfor (int right = 0; right < s.length(); right++) {\n    // ???\n    window.add(s.charAt(right));\n    maxLen = Math.max(maxLen, right - left + 1);\n}",
                    choices = listOf(
                        choice("while (window.contains(s.charAt(right))) {\n    window.remove(s.charAt(left));\n    left++;\n}", true, "This shrinks the window from the left, one character at a time, exactly until the specific duplicate character is removed, before the new character is added and the length is recorded.", code = "while (window.contains(s.charAt(right))) {\n    window.remove(s.charAt(left));\n    left++;\n}"),
                        choice("if (window.contains(s.charAt(right))) {\n    window.remove(s.charAt(left));\n    left++;\n}", false, "A single if only removes one character, which might not actually remove the specific duplicate if it isn't the leftmost character in the window.", code = "if (window.contains(s.charAt(right))) {\n    window.remove(s.charAt(left));\n    left++;\n}"),
                        choice("if (window.contains(s.charAt(right))) {\n    window.clear();\n    left = right;\n}", false, "Clearing the entire window and jumping left all the way to right throws away every character between the old duplicate and the current position that could still be part of a valid window.", code = "if (window.contains(s.charAt(right))) {\n    window.clear();\n    left = right;\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "let left = 0;\nlet maxLen = 0;\nconst window = new Set();\nfor (let right = 0; right < s.length; right++) {\n    // ???\n    window.add(s[right]);\n    maxLen = Math.max(maxLen, right - left + 1);\n}",
                    choices = listOf(
                        choice("while (window.has(s[right])) {\n    window.delete(s[left]);\n    left++;\n}", true, "This shrinks the window from the left, one character at a time, exactly until the specific duplicate character is removed, before the new character is added and the length is recorded.", code = "while (window.has(s[right])) {\n    window.delete(s[left]);\n    left++;\n}"),
                        choice("if (window.has(s[right])) {\n    window.delete(s[left]);\n    left++;\n}", false, "A single if only removes one character, which might not actually remove the specific duplicate if it isn't the leftmost character in the window.", code = "if (window.has(s[right])) {\n    window.delete(s[left]);\n    left++;\n}"),
                        choice("if (window.has(s[right])) {\n    window.clear();\n    left = right;\n}", false, "Clearing the entire window and jumping left all the way to right throws away every character between the old duplicate and the current position that could still be part of a valid window.", code = "if (window.has(s[right])) {\n    window.clear();\n    left = right;\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "int left = 0;\nint maxLen = 0;\nunordered_set<char> window;\nfor (int right = 0; right < (int)s.size(); right++) {\n    // ???\n    window.insert(s[right]);\n    maxLen = max(maxLen, right - left + 1);\n}",
                    choices = listOf(
                        choice("while (window.count(s[right])) {\n    window.erase(s[left]);\n    left++;\n}", true, "This shrinks the window from the left, one character at a time, exactly until the specific duplicate character is removed, before the new character is added and the length is recorded.", code = "while (window.count(s[right])) {\n    window.erase(s[left]);\n    left++;\n}"),
                        choice("if (window.count(s[right])) {\n    window.erase(s[left]);\n    left++;\n}", false, "A single if only removes one character, which might not actually remove the specific duplicate if it isn't the leftmost character in the window.", code = "if (window.count(s[right])) {\n    window.erase(s[left]);\n    left++;\n}"),
                        choice("if (window.count(s[right])) {\n    window.clear();\n    left = right;\n}", false, "Clearing the entire window and jumping left all the way to right throws away every character between the old duplicate and the current position that could still be part of a valid window.", code = "if (window.count(s[right])) {\n    window.clear();\n    left = right;\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "left := 0\nmaxLen := 0\nwindow := map[byte]bool{}\nfor right := 0; right < len(s); right++ {\n    // ???\n    window[s[right]] = true\n    if right-left+1 > maxLen {\n        maxLen = right - left + 1\n    }\n}",
                    choices = listOf(
                        choice("for window[s[right]] {\n    delete(window, s[left])\n    left++\n}", true, "This shrinks the window from the left, one character at a time, exactly until the specific duplicate character is removed, before the new character is added and the length is recorded.", code = "for window[s[right]] {\n    delete(window, s[left])\n    left++\n}"),
                        choice("if window[s[right]] {\n    delete(window, s[left])\n    left++\n}", false, "A single if only removes one character, which might not actually remove the specific duplicate if it isn't the leftmost character in the window.", code = "if window[s[right]] {\n    delete(window, s[left])\n    left++\n}"),
                        choice("if window[s[right]] {\n    window = map[byte]bool{}\n    left = right\n}", false, "Clearing the entire window and jumping left all the way to right throws away every character between the old duplicate and the current position that could still be part of a valid window.", code = "if window[s[right]] {\n    window = map[byte]bool{}\n    left = right\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "let chars = Array(s)\nvar left = 0\nvar maxLen = 0\nvar window = Set<Character>()\nfor right in 0..<chars.count {\n    // ???\n    window.insert(chars[right])\n    maxLen = max(maxLen, right - left + 1)\n}",
                    choices = listOf(
                        choice("while window.contains(chars[right]) {\n    window.remove(chars[left])\n    left += 1\n}", true, "This shrinks the window from the left, one character at a time, exactly until the specific duplicate character is removed, before the new character is added and the length is recorded.", code = "while window.contains(chars[right]) {\n    window.remove(chars[left])\n    left += 1\n}"),
                        choice("if window.contains(chars[right]) {\n    window.remove(chars[left])\n    left += 1\n}", false, "A single if only removes one character, which might not actually remove the specific duplicate if it isn't the leftmost character in the window.", code = "if window.contains(chars[right]) {\n    window.remove(chars[left])\n    left += 1\n}"),
                        choice("if window.contains(chars[right]) {\n    window.removeAll()\n    left = right\n}", false, "Clearing the entire window and jumping left all the way to right throws away every character between the old duplicate and the current position that could still be part of a valid window.", code = "if window.contains(chars[right]) {\n    window.removeAll()\n    left = right\n}"),
                    ),
                ),
            ),
        ),
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, TIME_COMPLEXITY,
            "With n as the string's length, what is the time complexity?",
            conceptKey = "longest-substring-without-repeating-characters-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the window's characters are kept in sorted order.",
                    false,
                    "The window's characters don't need to be sorted - only membership (is this character currently in the window) needs to be checked, which a hash set does in constant time.",
                ),
                choice(
                    "O(n squared), because every right position can trigger a full shrink back to the start.",
                    false,
                    "Left only ever moves forward and never resets to the very start after the first shrink, so its total movement across the whole run stays bounded by n.",
                ),
                choice(
                    "O(n), because left and right each move forward at most n times combined across the whole run.",
                    true,
                    "Even though shrinking happens inside a nested loop, left never moves backward, so the total movement of both pointers together is bounded by n, not n squared.",
                )),
        ),
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, SPACE_COMPLEXITY,
            "How much extra space does this approach use?",
            conceptKey = "longest-substring-without-repeating-characters-space-complexity",
            choices = listOf(
                choice(
                    "O(min(n, k)), where k is the size of the character set, because the window can hold at most one of each possible character.",
                    true,
                    "Since the window never contains a repeat, its size is capped by however many distinct characters are possible, not by the string's total length.",
                ),
                choice(
                    "O(1), because the window set never grows beyond a single character.",
                    false,
                    "The window can hold many distinct characters at once, up to the size of the character set - it's not limited to just one.",
                ),
                choice(
                    "O(n squared), because every substring considered is stored.",
                    false,
                    "Only the current window's characters are stored at any one time - previous windows are discarded as the window slides, not accumulated.",
                )),
        ),
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, TRANSFER,
            "Would this same grow-then-shrink-on-violation window also work for finding the longest substring with at most two distinct characters?",
            conceptKey = "longest-substring-without-repeating-characters-transfer",
            choices = listOf(
                choice(
                    "Yes - swap the violation condition from 'a character repeats' to 'more than two distinct characters are in the window', shrinking whenever that new condition is broken.",
                    true,
                    "The window mechanics stay identical - grow from the right, shrink from the left while a condition is violated - only the specific condition being checked changes.",
                ),
                choice(
                    "No - allowing some repeats requires abandoning the window approach entirely for a full substring comparison.",
                    false,
                    "Allowing a bounded number of distinct characters is still a condition that can be checked incrementally as the window grows, so the same window mechanics still apply.",
                ),
                choice(
                    "No - sliding window can only track whether characters repeat, not how many distinct ones are present.",
                    false,
                    "A window can track a map from character to count just as easily as a plain set - counting distinct characters is a natural extension of the same idea.",
                )),
        ),
        step(
            "longest-substring-without-repeating-characters", SLIDING_WINDOW, EDGE_CASE,
            "Which input would break a version of this solution that shrinks by exactly one character on any duplicate, without a while loop?",
            conceptKey = "longest-substring-without-repeating-characters-edge-case",
            choices = listOf(
                choice(
                    "A string like \"abba\", where the second 'a' duplicates a character that isn't the current leftmost one, needing more than one removal to resolve.",
                    true,
                    "When the right pointer reaches the second 'a', the window is \"bb a\"-ish and a single removal from the left (removing the first 'b') doesn't actually remove the duplicate 'a' - a while loop keeps removing until the duplicate itself is gone.",
                ),
                choice(
                    "A string with all identical characters, like \"aaaa\".",
                    false,
                    "Every step here duplicates the immediately preceding character, so a single shrink each time happens to be enough - this case wouldn't expose the missing while-loop bug.",
                ),
                choice(
                    "A string with all unique characters.",
                    false,
                    "With no duplicates at all, the shrink logic never runs in the first place, so this case can't reveal a bug in how shrinking handles a duplicate.",
                )),
        ),
    ),
)

private val minStackWorkout = ProblemWorkout(
    problemSlug = "min-stack",
    group = STACK,
    steps = listOf(
        step(
            "min-stack", STACK, PATTERN_RECOGNITION,
            "Design a stack that supports push, pop, top, and getMin, all in O(1). Which pattern fits?",
            conceptKey = "min-stack-pattern-recognition",
            choices = listOf(
                choice(
                    "A stack paired with a second, parallel min-stack that tracks the minimum at each level.",
                    true,
                    "Recording the minimum-so-far alongside every push, on a stack of its own, means the current minimum is always sitting right at the top, ready in constant time.",
                ),
                choice(
                    "A sorted list rebuilt after every push and pop.",
                    false,
                    "Re-sorting after every operation costs far more than constant time, and it also throws away the last-in-first-out order a stack is supposed to preserve.",
                ),
                choice(
                    "A hash map from value to how many times it currently appears on the stack.",
                    false,
                    "A count map can tell you which values exist, but finding the *minimum* among them still requires scanning all the keys, which isn't constant time.",
                )),
        ),
        step(
            "min-stack", STACK, APPROACH,
            "Which approach correctly supports O(1) getMin?",
            conceptKey = "min-stack-approach",
            choices = listOf(
                choice(
                    "Maintain a second stack where, on every push, the minimum of the new value and the current minimum is pushed alongside it; pop both stacks together.",
                    true,
                    "Because the min-stack's top always reflects the minimum of everything currently on the main stack, getMin is a simple top-of-stack read, and popping both together keeps them in sync automatically.",
                ),
                choice(
                    "Track a single running minimum variable, updated on every push, and recompute it by scanning the whole stack after every pop.",
                    false,
                    "Rescanning the whole stack after every pop is not constant time - it costs O(n) exactly when the current minimum happens to be popped.",
                ),
                choice(
                    "Sort the stack's contents whenever getMin is called.",
                    false,
                    "Sorting on demand costs more than constant time per call and also disturbs the stack's actual push order, which needs to stay intact for pop and top to work correctly.",
                )),
        ),
        step(
            "min-stack", STACK, STATE_SELECTION,
            "What state does this design need?",
            conceptKey = "min-stack-state-selection",
            choices = listOf(
                choice(
                    "A main stack holding the pushed values, and a parallel min-stack holding the running minimum at each level.",
                    true,
                    "Keeping the two stacks the same size, growing and shrinking together, is what lets getMin always read the min-stack's top directly.",
                ),
                choice(
                    "A single stack, plus a separate variable holding only the current overall minimum.",
                    false,
                    "A single variable can't recover the *previous* minimum once the current one is popped off - the min-stack is what remembers what the minimum was at every earlier point.",
                ),
                choice(
                    "A binary search tree of all values currently on the stack.",
                    false,
                    "A tree would need rebalancing on every push and pop, and finding the minimum in a tree still takes work proportional to its height, not truly constant time.",
                )),
        ),
        step(
            "min-stack", STACK, BOUNDARY_UPDATE,
            "Which rule correctly keeps the two stacks synchronized?",
            conceptKey = "min-stack-boundary-update",
            choices = listOf(
                choice(
                    "On push, push the new value onto the main stack, and only push onto the min-stack when the new value is smaller than the current minimum.",
                    false,
                    "Pushing onto the min-stack only sometimes means the two stacks fall out of sync in size, so a later pop wouldn't know whether to also pop from the min-stack.",
                ),
                choice(
                    "On push, push the new value onto the main stack and push minOf(newValue, currentMin) onto the min-stack; on pop, pop both stacks together.",
                    true,
                    "Pushing a value onto the min-stack for every single push, not just when a new minimum appears, keeps the two stacks exactly the same size so they always pop back into sync.",
                ),
                choice(
                    "On pop, pop from the main stack, but only pop from the min-stack if the popped value equals the current minimum.",
                    false,
                    "Popping from the min-stack conditionally, rather than every time, breaks the size-matching between the two stacks that getMin depends on.",
                )),
        ),
        step(
            "min-stack", STACK, CODE_BLOCK,
            "Which snippet correctly implements push and pop?",
            conceptKey = "min-stack-code-block",
            code = "val stack = ArrayDeque<Int>()\nval minStack = ArrayDeque<Int>()\nfun push(value: Int) {\n    // ???\n}\nfun pop() {\n    stack.removeLast()\n    minStack.removeLast()\n}",
            choices = listOf(
                choice(
                    "stack.addLast(value)\nval newMin = if (minStack.isEmpty()) value else minOf(value, minStack.last())\nminStack.addLast(newMin)",
                    true,
                    "Every push adds exactly one entry to both stacks, with the min-stack entry always reflecting the minimum so far - this keeps both stacks the same size and getMin correct at every step.",
                    code = "stack.addLast(value)\nval newMin = if (minStack.isEmpty()) value else minOf(value, minStack.last())\nminStack.addLast(newMin)",
                ),
                choice(
                    "stack.addLast(value)\nif (minStack.isEmpty() || value < minStack.last()) minStack.addLast(value)",
                    false,
                    "Only sometimes pushing onto the min-stack leaves it a different size than the main stack, so popping both together (as pop() does here) would desynchronize them.",
                    code = "stack.addLast(value)\nif (minStack.isEmpty() || value < minStack.last()) minStack.addLast(value)",
                ),
                choice(
                    "stack.addLast(value)\nminStack.addLast(value)",
                    false,
                    "This pushes the raw value instead of the running minimum onto the min-stack, so its top would just be the most recent push, not the smallest value seen so far.",
                    code = "stack.addLast(value)\nminStack.addLast(value)",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "stack = []\nmin_stack = []\ndef push(value):\n    # ???\ndef pop():\n    stack.pop()\n    min_stack.pop()",
                    choices = listOf(
                        choice("stack.append(value)\nnew_min = value if not min_stack else min(value, min_stack[-1])\nmin_stack.append(new_min)", true, "Every push adds exactly one entry to both stacks, with the min-stack entry always reflecting the minimum so far - this keeps both stacks the same size and getMin correct at every step.", code = "stack.append(value)\nnew_min = value if not min_stack else min(value, min_stack[-1])\nmin_stack.append(new_min)"),
                        choice("stack.append(value)\nif not min_stack or value < min_stack[-1]:\n    min_stack.append(value)", false, "Only sometimes pushing onto the min-stack leaves it a different size than the main stack, so popping both together (as pop() does here) would desynchronize them.", code = "stack.append(value)\nif not min_stack or value < min_stack[-1]:\n    min_stack.append(value)"),
                        choice("stack.append(value)\nmin_stack.append(value)", false, "This pushes the raw value instead of the running minimum onto the min-stack, so its top would just be the most recent push, not the smallest value seen so far.", code = "stack.append(value)\nmin_stack.append(value)"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "Deque<Integer> stack = new ArrayDeque<>();\nDeque<Integer> minStack = new ArrayDeque<>();\nvoid push(int value) {\n    // ???\n}\nvoid pop() {\n    stack.removeLast();\n    minStack.removeLast();\n}",
                    choices = listOf(
                        choice("stack.addLast(value);\nint newMin = minStack.isEmpty() ? value : Math.min(value, minStack.peekLast());\nminStack.addLast(newMin);", true, "Every push adds exactly one entry to both stacks, with the min-stack entry always reflecting the minimum so far - this keeps both stacks the same size and getMin correct at every step.", code = "stack.addLast(value);\nint newMin = minStack.isEmpty() ? value : Math.min(value, minStack.peekLast());\nminStack.addLast(newMin);"),
                        choice("stack.addLast(value);\nif (minStack.isEmpty() || value < minStack.peekLast()) minStack.addLast(value);", false, "Only sometimes pushing onto the min-stack leaves it a different size than the main stack, so popping both together (as pop() does here) would desynchronize them.", code = "stack.addLast(value);\nif (minStack.isEmpty() || value < minStack.peekLast()) minStack.addLast(value);"),
                        choice("stack.addLast(value);\nminStack.addLast(value);", false, "This pushes the raw value instead of the running minimum onto the min-stack, so its top would just be the most recent push, not the smallest value seen so far.", code = "stack.addLast(value);\nminStack.addLast(value);"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "const stack = [];\nconst minStack = [];\nfunction push(value) {\n    // ???\n}\nfunction pop() {\n    stack.pop();\n    minStack.pop();\n}",
                    choices = listOf(
                        choice("stack.push(value);\nconst newMin = minStack.length === 0 ? value : Math.min(value, minStack[minStack.length - 1]);\nminStack.push(newMin);", true, "Every push adds exactly one entry to both stacks, with the min-stack entry always reflecting the minimum so far - this keeps both stacks the same size and getMin correct at every step.", code = "stack.push(value);\nconst newMin = minStack.length === 0 ? value : Math.min(value, minStack[minStack.length - 1]);\nminStack.push(newMin);"),
                        choice("stack.push(value);\nif (minStack.length === 0 || value < minStack[minStack.length - 1]) minStack.push(value);", false, "Only sometimes pushing onto the min-stack leaves it a different size than the main stack, so popping both together (as pop() does here) would desynchronize them.", code = "stack.push(value);\nif (minStack.length === 0 || value < minStack[minStack.length - 1]) minStack.push(value);"),
                        choice("stack.push(value);\nminStack.push(value);", false, "This pushes the raw value instead of the running minimum onto the min-stack, so its top would just be the most recent push, not the smallest value seen so far.", code = "stack.push(value);\nminStack.push(value);"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "vector<int> stack;\nvector<int> minStack;\nvoid push(int value) {\n    // ???\n}\nvoid pop() {\n    stack.pop_back();\n    minStack.pop_back();\n}",
                    choices = listOf(
                        choice("stack.push_back(value);\nint newMin = minStack.empty() ? value : min(value, minStack.back());\nminStack.push_back(newMin);", true, "Every push adds exactly one entry to both stacks, with the min-stack entry always reflecting the minimum so far - this keeps both stacks the same size and getMin correct at every step.", code = "stack.push_back(value);\nint newMin = minStack.empty() ? value : min(value, minStack.back());\nminStack.push_back(newMin);"),
                        choice("stack.push_back(value);\nif (minStack.empty() || value < minStack.back()) minStack.push_back(value);", false, "Only sometimes pushing onto the min-stack leaves it a different size than the main stack, so popping both together (as pop() does here) would desynchronize them.", code = "stack.push_back(value);\nif (minStack.empty() || value < minStack.back()) minStack.push_back(value);"),
                        choice("stack.push_back(value);\nminStack.push_back(value);", false, "This pushes the raw value instead of the running minimum onto the min-stack, so its top would just be the most recent push, not the smallest value seen so far.", code = "stack.push_back(value);\nminStack.push_back(value);"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "var stack []int\nvar minStack []int\nfunc push(value int) {\n    // ???\n}\nfunc pop() {\n    stack = stack[:len(stack)-1]\n    minStack = minStack[:len(minStack)-1]\n}",
                    choices = listOf(
                        choice("stack = append(stack, value)\nnewMin := value\nif len(minStack) > 0 && minStack[len(minStack)-1] < newMin {\n    newMin = minStack[len(minStack)-1]\n}\nminStack = append(minStack, newMin)", true, "Every push adds exactly one entry to both stacks, with the min-stack entry always reflecting the minimum so far - this keeps both stacks the same size and getMin correct at every step.", code = "stack = append(stack, value)\nnewMin := value\nif len(minStack) > 0 && minStack[len(minStack)-1] < newMin {\n    newMin = minStack[len(minStack)-1]\n}\nminStack = append(minStack, newMin)"),
                        choice("stack = append(stack, value)\nif len(minStack) == 0 || value < minStack[len(minStack)-1] {\n    minStack = append(minStack, value)\n}", false, "Only sometimes pushing onto the min-stack leaves it a different size than the main stack, so popping both together (as pop() does here) would desynchronize them.", code = "stack = append(stack, value)\nif len(minStack) == 0 || value < minStack[len(minStack)-1] {\n    minStack = append(minStack, value)\n}"),
                        choice("stack = append(stack, value)\nminStack = append(minStack, value)", false, "This pushes the raw value instead of the running minimum onto the min-stack, so its top would just be the most recent push, not the smallest value seen so far.", code = "stack = append(stack, value)\nminStack = append(minStack, value)"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var stack: [Int] = []\nvar minStack: [Int] = []\nfunc push(_ value: Int) {\n    // ???\n}\nfunc pop() {\n    stack.removeLast()\n    minStack.removeLast()\n}",
                    choices = listOf(
                        choice("stack.append(value)\nlet newMin = minStack.isEmpty ? value : min(value, minStack.last!)\nminStack.append(newMin)", true, "Every push adds exactly one entry to both stacks, with the min-stack entry always reflecting the minimum so far - this keeps both stacks the same size and getMin correct at every step.", code = "stack.append(value)\nlet newMin = minStack.isEmpty ? value : min(value, minStack.last!)\nminStack.append(newMin)"),
                        choice("stack.append(value)\nif minStack.isEmpty || value < minStack.last! { minStack.append(value) }", false, "Only sometimes pushing onto the min-stack leaves it a different size than the main stack, so popping both together (as pop() does here) would desynchronize them.", code = "stack.append(value)\nif minStack.isEmpty || value < minStack.last! { minStack.append(value) }"),
                        choice("stack.append(value)\nminStack.append(value)", false, "This pushes the raw value instead of the running minimum onto the min-stack, so its top would just be the most recent push, not the smallest value seen so far.", code = "stack.append(value)\nminStack.append(value)"),
                    ),
                ),
            ),
        ),
        step(
            "min-stack", STACK, TIME_COMPLEXITY,
            "What is the time complexity of push, pop, top, and getMin?",
            conceptKey = "min-stack-time-complexity",
            choices = listOf(
                choice(
                    "O(log n) for all operations, because the min-stack keeps its entries sorted.",
                    false,
                    "The min-stack isn't sorted - it's just a plain stack, aligned entry-by-entry with the main stack, so no logarithmic search is ever needed.",
                ),
                choice(
                    "O(1) for push and pop, but O(n) for getMin since it scans the whole stack.",
                    false,
                    "That would be true for a single running-minimum variable without a min-stack, but the parallel min-stack specifically avoids ever needing to scan anything for getMin.",
                ),
                choice(
                    "O(1) for every operation, because each only ever touches the top of one or both stacks.",
                    true,
                    "Reading or removing from the top of a stack is constant time, and the min-stack's design means getMin is just another top-of-stack read.",
                )),
        ),
        step(
            "min-stack", STACK, SPACE_COMPLEXITY,
            "How much extra space does the min-stack use?",
            conceptKey = "min-stack-space-complexity",
            choices = listOf(
                choice(
                    "O(n), because it holds one entry for every entry on the main stack.",
                    true,
                    "Since every push adds exactly one value to both stacks, the min-stack always ends up exactly as large as the main stack.",
                ),
                choice(
                    "O(1), because it only ever stores the single current minimum.",
                    false,
                    "It stores a minimum for *every* level of the stack, not just the current one, so that popping can recover what the minimum used to be at each earlier point.",
                ),
                choice(
                    "O(log n), because only new minimums are recorded.",
                    false,
                    "Every single push records an entry on the min-stack, not just the pushes that happen to set a new minimum, which is exactly what keeps the two stacks synchronized.",
                )),
        ),
        step(
            "min-stack", STACK, TRANSFER,
            "Would this same parallel-stack trick also work for a getMax stack instead of getMin?",
            conceptKey = "min-stack-transfer",
            choices = listOf(
                choice(
                    "Yes - the same idea works symmetrically, pushing the running maximum instead of the running minimum onto the second stack.",
                    true,
                    "Nothing about the trick is specific to minimums - tracking the running maximum at each level the same way gives O(1) getMax with the identical push/pop synchronization.",
                ),
                choice(
                    "No - tracking a maximum requires a completely different data structure, like a heap.",
                    false,
                    "A heap could track a maximum too, but it's unnecessary here - the same parallel-stack idea used for the minimum applies directly and symmetrically to the maximum.",
                ),
                choice(
                    "No - a stack can only ever track one of minimum or maximum, never either.",
                    false,
                    "There's nothing that limits this trick to one direction - a min-stack and a max-stack could even both be maintained alongside the main stack at the same time if both were needed.",
                )),
        ),
        step(
            "min-stack", STACK, EDGE_CASE,
            "Which sequence of operations would break a version of this solution that only pushes onto the min-stack when a new minimum appears?",
            conceptKey = "min-stack-edge-case",
            choices = listOf(
                choice(
                    "push(5), push(3), pop(), getMin() - after popping the 3, a version that never recorded 5 on the min-stack (since 5 wasn't a new minimum before 3 was pushed) would return the wrong minimum.",
                    true,
                    "If the min-stack only stores new minimums, it would hold just [5, 3]... but actually never even store 5 if 3 comes right after and is smaller only relative to itself - the mismatched sizes between the two stacks make popping unreliable about which minimum applies after each pop.",
                ),
                choice(
                    "push(5), getMin().",
                    false,
                    "A single push followed by getMin is the simplest possible case and works correctly regardless of whether the min-stack pushes conditionally or every time.",
                ),
                choice(
                    "push(5), push(10), push(15).",
                    false,
                    "With values only ever increasing, the minimum never changes after the first push, so this sequence doesn't expose a desynchronization bug between the two stacks.",
                )),
        ),
    ),
)

private val dailyTemperaturesWorkout = ProblemWorkout(
    problemSlug = "daily-temperatures",
    group = STACK,
    steps = listOf(
        step(
            "daily-temperatures", STACK, PATTERN_RECOGNITION,
            "Given daily temperatures, return for each day how many days until a warmer day, or 0 if none exists. Which pattern fits?",
            conceptKey = "daily-temperatures-pattern-recognition",
            choices = listOf(
                choice(
                    "A monotonic stack of day indices still waiting for a warmer day.",
                    true,
                    "Each day waits until a strictly warmer day shows up later, and the days still waiting are exactly the ones a stack, popped whenever a warmer day appears, naturally tracks.",
                ),
                choice(
                    "For each day, scan forward until a warmer day is found.",
                    false,
                    "This finds the correct answer but can cost O(n squared) in the worst case, such as strictly decreasing temperatures, where a monotonic stack still finishes in linear time.",
                ),
                choice(
                    "Sort the days by temperature and answer each day using its new sorted position.",
                    false,
                    "Sorting destroys the original day-to-day order, but the answer specifically depends on how many days must pass in the *original* sequence - a sorted rearrangement loses that.",
                )),
        ),
        step(
            "daily-temperatures", STACK, APPROACH,
            "Which approach correctly finds every day's answer?",
            conceptKey = "daily-temperatures-approach",
            choices = listOf(
                choice(
                    "Walk through the temperatures once, maintaining a stack of day indices waiting for a warmer day; whenever the current temperature is warmer than the one at the top of the stack, pop it and record the day difference.",
                    true,
                    "The stack holds exactly the days that haven't yet found their answer - each day is pushed once and popped once, when its answer is finally found, keeping the total work linear.",
                ),
                choice(
                    "Walk through the temperatures once, maintaining a stack, but pop only a single day whenever the current temperature is warmer, even if several days could be resolved at once.",
                    false,
                    "Popping just one day at a time can leave other days on the stack unresolved even though the current, warmer temperature would have answered them too.",
                ),
                choice(
                    "For each day, binary search the remaining days for the first one that is warmer.",
                    false,
                    "The remaining days aren't sorted by temperature, so binary search doesn't apply - the stack-based approach handles this without needing any sorted structure.",
                )),
        ),
        step(
            "daily-temperatures", STACK, STATE_SELECTION,
            "What state does this approach need?",
            conceptKey = "daily-temperatures-state-selection",
            choices = listOf(
                choice(
                    "A stack of day indices, and an answer array initialized to zero for every day.",
                    true,
                    "The stack tracks which earlier days are still unresolved, and the answer array gets filled in for each of those days the moment a warmer day pops it off the stack.",
                ),
                choice(
                    "A sorted copy of the temperatures alongside their original indices.",
                    false,
                    "Sorting breaks the original day order, but the answer for each day depends on how many days pass in the original sequence, not on relative temperature rank.",
                ),
                choice(
                    "A running maximum temperature seen so far.",
                    false,
                    "A single running maximum can't tell you *when* each earlier day's answer was found - a stack of specific waiting days is needed to record the correct wait time for each one individually.",
                )),
        ),
        step(
            "daily-temperatures", STACK, BOUNDARY_UPDATE,
            "Which rule correctly resolves waiting days as the scan proceeds?",
            conceptKey = "daily-temperatures-boundary-update",
            choices = listOf(
                choice(
                    "If today's temperature is warmer than the temperature at the day on top of the stack, pop only that one day and move on, even if the next day down would also qualify.",
                    false,
                    "Using if instead of while can leave a day on the stack that also has a warmer day today, unresolved until some later day happens to be warm enough on its own.",
                ),
                choice(
                    "While the stack isn't empty and today's temperature is warmer than the temperature at the day on top of the stack, pop that day and record today's index minus the popped day's index as its answer.",
                    true,
                    "Popping every day whose day has now found a warmer temperature, using a while loop rather than a single pop, correctly resolves every waiting day as soon as its answer becomes known.",
                ),
                choice(
                    "Push today's day onto the stack only after popping every day currently on it, regardless of whether today is actually warmer than each one.",
                    false,
                    "Popping unconditionally, without checking whether today is actually warmer, would resolve days with an incorrect wait time or discard days that haven't found their answer yet.",
                )),
        ),
        step(
            "daily-temperatures", STACK, CODE_BLOCK,
            "Which snippet correctly implements the scan?",
            conceptKey = "daily-temperatures-code-block",
            code = "val answer = IntArray(temperatures.size)\nval stack = ArrayDeque<Int>()\nfor (i in temperatures.indices) {\n    // ???\n    stack.addLast(i)\n}\nreturn answer",
            choices = listOf(
                choice(
                    "while (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n    val prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}",
                    true,
                    "The while loop resolves every waiting day whose temperature today beats, recording the correct day difference for each one before the current day is pushed.",
                    code = "while (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n    val prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}",
                ),
                choice(
                    "if (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n    val prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}",
                    false,
                    "Using if instead of while pops only a single waiting day at most, leaving other days that today's warmer temperature could also resolve still stuck on the stack.",
                    code = "if (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n    val prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}",
                ),
                choice(
                    "while (stack.isNotEmpty()) {\n    val prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}",
                    false,
                    "Popping every day on the stack regardless of temperature would incorrectly resolve days even when today isn't actually warmer than they were.",
                    code = "while (stack.isNotEmpty()) {\n    val prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "answer = [0] * len(temperatures)\nstack = []\nfor i in range(len(temperatures)):\n    # ???\n    stack.append(i)\nreturn answer",
                    choices = listOf(
                        choice("while stack and temperatures[i] > temperatures[stack[-1]]:\n    prev_day = stack.pop()\n    answer[prev_day] = i - prev_day", true, "The while loop resolves every waiting day whose temperature today beats, recording the correct day difference for each one before the current day is pushed.", code = "while stack and temperatures[i] > temperatures[stack[-1]]:\n    prev_day = stack.pop()\n    answer[prev_day] = i - prev_day"),
                        choice("if stack and temperatures[i] > temperatures[stack[-1]]:\n    prev_day = stack.pop()\n    answer[prev_day] = i - prev_day", false, "Using if instead of while pops only a single waiting day at most, leaving other days that today's warmer temperature could also resolve still stuck on the stack.", code = "if stack and temperatures[i] > temperatures[stack[-1]]:\n    prev_day = stack.pop()\n    answer[prev_day] = i - prev_day"),
                        choice("while stack:\n    prev_day = stack.pop()\n    answer[prev_day] = i - prev_day", false, "Popping every day on the stack regardless of temperature would incorrectly resolve days even when today isn't actually warmer than they were.", code = "while stack:\n    prev_day = stack.pop()\n    answer[prev_day] = i - prev_day"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "int[] answer = new int[temperatures.length];\nDeque<Integer> stack = new ArrayDeque<>();\nfor (int i = 0; i < temperatures.length; i++) {\n    // ???\n    stack.addLast(i);\n}\nreturn answer;",
                    choices = listOf(
                        choice("while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peekLast()]) {\n    int prevDay = stack.removeLast();\n    answer[prevDay] = i - prevDay;\n}", true, "The while loop resolves every waiting day whose temperature today beats, recording the correct day difference for each one before the current day is pushed.", code = "while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peekLast()]) {\n    int prevDay = stack.removeLast();\n    answer[prevDay] = i - prevDay;\n}"),
                        choice("if (!stack.isEmpty() && temperatures[i] > temperatures[stack.peekLast()]) {\n    int prevDay = stack.removeLast();\n    answer[prevDay] = i - prevDay;\n}", false, "Using if instead of while pops only a single waiting day at most, leaving other days that today's warmer temperature could also resolve still stuck on the stack.", code = "if (!stack.isEmpty() && temperatures[i] > temperatures[stack.peekLast()]) {\n    int prevDay = stack.removeLast();\n    answer[prevDay] = i - prevDay;\n}"),
                        choice("while (!stack.isEmpty()) {\n    int prevDay = stack.removeLast();\n    answer[prevDay] = i - prevDay;\n}", false, "Popping every day on the stack regardless of temperature would incorrectly resolve days even when today isn't actually warmer than they were.", code = "while (!stack.isEmpty()) {\n    int prevDay = stack.removeLast();\n    answer[prevDay] = i - prevDay;\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "const answer = new Array(temperatures.length).fill(0);\nconst stack = [];\nfor (let i = 0; i < temperatures.length; i++) {\n    // ???\n    stack.push(i);\n}\nreturn answer;",
                    choices = listOf(
                        choice("while (stack.length && temperatures[i] > temperatures[stack[stack.length - 1]]) {\n    const prevDay = stack.pop();\n    answer[prevDay] = i - prevDay;\n}", true, "The while loop resolves every waiting day whose temperature today beats, recording the correct day difference for each one before the current day is pushed.", code = "while (stack.length && temperatures[i] > temperatures[stack[stack.length - 1]]) {\n    const prevDay = stack.pop();\n    answer[prevDay] = i - prevDay;\n}"),
                        choice("if (stack.length && temperatures[i] > temperatures[stack[stack.length - 1]]) {\n    const prevDay = stack.pop();\n    answer[prevDay] = i - prevDay;\n}", false, "Using if instead of while pops only a single waiting day at most, leaving other days that today's warmer temperature could also resolve still stuck on the stack.", code = "if (stack.length && temperatures[i] > temperatures[stack[stack.length - 1]]) {\n    const prevDay = stack.pop();\n    answer[prevDay] = i - prevDay;\n}"),
                        choice("while (stack.length) {\n    const prevDay = stack.pop();\n    answer[prevDay] = i - prevDay;\n}", false, "Popping every day on the stack regardless of temperature would incorrectly resolve days even when today isn't actually warmer than they were.", code = "while (stack.length) {\n    const prevDay = stack.pop();\n    answer[prevDay] = i - prevDay;\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "vector<int> answer(temperatures.size(), 0);\nvector<int> stack;\nfor (int i = 0; i < (int)temperatures.size(); i++) {\n    // ???\n    stack.push_back(i);\n}\nreturn answer;",
                    choices = listOf(
                        choice("while (!stack.empty() && temperatures[i] > temperatures[stack.back()]) {\n    int prevDay = stack.back();\n    stack.pop_back();\n    answer[prevDay] = i - prevDay;\n}", true, "The while loop resolves every waiting day whose temperature today beats, recording the correct day difference for each one before the current day is pushed.", code = "while (!stack.empty() && temperatures[i] > temperatures[stack.back()]) {\n    int prevDay = stack.back();\n    stack.pop_back();\n    answer[prevDay] = i - prevDay;\n}"),
                        choice("if (!stack.empty() && temperatures[i] > temperatures[stack.back()]) {\n    int prevDay = stack.back();\n    stack.pop_back();\n    answer[prevDay] = i - prevDay;\n}", false, "Using if instead of while pops only a single waiting day at most, leaving other days that today's warmer temperature could also resolve still stuck on the stack.", code = "if (!stack.empty() && temperatures[i] > temperatures[stack.back()]) {\n    int prevDay = stack.back();\n    stack.pop_back();\n    answer[prevDay] = i - prevDay;\n}"),
                        choice("while (!stack.empty()) {\n    int prevDay = stack.back();\n    stack.pop_back();\n    answer[prevDay] = i - prevDay;\n}", false, "Popping every day on the stack regardless of temperature would incorrectly resolve days even when today isn't actually warmer than they were.", code = "while (!stack.empty()) {\n    int prevDay = stack.back();\n    stack.pop_back();\n    answer[prevDay] = i - prevDay;\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "answer := make([]int, len(temperatures))\nstack := []int{}\nfor i := 0; i < len(temperatures); i++ {\n    // ???\n    stack = append(stack, i)\n}\nreturn answer",
                    choices = listOf(
                        choice("for len(stack) > 0 && temperatures[i] > temperatures[stack[len(stack)-1]] {\n    prevDay := stack[len(stack)-1]\n    stack = stack[:len(stack)-1]\n    answer[prevDay] = i - prevDay\n}", true, "The loop resolves every waiting day whose temperature today beats, recording the correct day difference for each one before the current day is pushed.", code = "for len(stack) > 0 && temperatures[i] > temperatures[stack[len(stack)-1]] {\n    prevDay := stack[len(stack)-1]\n    stack = stack[:len(stack)-1]\n    answer[prevDay] = i - prevDay\n}"),
                        choice("if len(stack) > 0 && temperatures[i] > temperatures[stack[len(stack)-1]] {\n    prevDay := stack[len(stack)-1]\n    stack = stack[:len(stack)-1]\n    answer[prevDay] = i - prevDay\n}", false, "Using a single if instead of a loop pops only a single waiting day at most, leaving other days that today's warmer temperature could also resolve still stuck on the stack.", code = "if len(stack) > 0 && temperatures[i] > temperatures[stack[len(stack)-1]] {\n    prevDay := stack[len(stack)-1]\n    stack = stack[:len(stack)-1]\n    answer[prevDay] = i - prevDay\n}"),
                        choice("for len(stack) > 0 {\n    prevDay := stack[len(stack)-1]\n    stack = stack[:len(stack)-1]\n    answer[prevDay] = i - prevDay\n}", false, "Popping every day on the stack regardless of temperature would incorrectly resolve days even when today isn't actually warmer than they were.", code = "for len(stack) > 0 {\n    prevDay := stack[len(stack)-1]\n    stack = stack[:len(stack)-1]\n    answer[prevDay] = i - prevDay\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "var answer = [Int](repeating: 0, count: temperatures.count)\nvar stack: [Int] = []\nfor i in 0..<temperatures.count {\n    // ???\n    stack.append(i)\n}\nreturn answer",
                    choices = listOf(
                        choice("while let last = stack.last, temperatures[i] > temperatures[last] {\n    let prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}", true, "The while loop resolves every waiting day whose temperature today beats, recording the correct day difference for each one before the current day is pushed.", code = "while let last = stack.last, temperatures[i] > temperatures[last] {\n    let prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}"),
                        choice("if let last = stack.last, temperatures[i] > temperatures[last] {\n    let prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}", false, "Using if instead of while pops only a single waiting day at most, leaving other days that today's warmer temperature could also resolve still stuck on the stack.", code = "if let last = stack.last, temperatures[i] > temperatures[last] {\n    let prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}"),
                        choice("while !stack.isEmpty {\n    let prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}", false, "Popping every day on the stack regardless of temperature would incorrectly resolve days even when today isn't actually warmer than they were.", code = "while !stack.isEmpty {\n    let prevDay = stack.removeLast()\n    answer[prevDay] = i - prevDay\n}"),
                    ),
                ),
            ),
        ),
        step(
            "daily-temperatures", STACK, TIME_COMPLEXITY,
            "With n as the number of days, what is the time complexity?",
            conceptKey = "daily-temperatures-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the stack must stay sorted by temperature.",
                    false,
                    "The stack isn't kept in any particular sorted order - it's popped based on direct comparisons to today's temperature, using plain constant-time stack operations.",
                ),
                choice(
                    "O(n squared), because each day might pop many earlier days off the stack.",
                    false,
                    "Even though one day can pop several earlier days, each day is only ever pushed once and popped from the stack at most once overall - the total number of stack operations stays bounded by n.",
                ),
                choice(
                    "O(n), because each day is pushed onto the stack exactly once and popped at most once, across the whole run.",
                    true,
                    "Even though a single day can trigger several pops at once, the combined total of every push and pop across the entire algorithm never exceeds roughly 2n.",
                )),
        ),
        step(
            "daily-temperatures", STACK, SPACE_COMPLEXITY,
            "How much extra space does the stack use in the worst case?",
            conceptKey = "daily-temperatures-space-complexity",
            choices = listOf(
                choice(
                    "O(n), because strictly decreasing temperatures would push every day onto the stack before any of them get popped.",
                    true,
                    "Days are only popped when a strictly warmer day appears later - a temperature sequence that keeps decreasing never triggers this, so the stack can grow to hold every single day.",
                ),
                choice(
                    "O(1), because the stack only ever holds a small, fixed number of waiting days.",
                    false,
                    "The stack can grow much larger than a small fixed number - if temperatures keep dropping, every one of those days stays on the stack, unresolved, until a warmer day finally appears.",
                ),
                choice(
                    "O(log n), because the stack only grows for a small portion of typical temperature sequences.",
                    false,
                    "How often temperatures happen to increase doesn't bound the stack logarithmically - in the worst case, the stack grows linearly, holding every single day at once.",
                )),
        ),
        step(
            "daily-temperatures", STACK, TRANSFER,
            "Would this same waiting-stack approach also work for finding the next greater element in a circular array?",
            conceptKey = "daily-temperatures-transfer",
            choices = listOf(
                choice(
                    "Yes, with a small twist - scanning the array twice (or the indices mod the array length twice) lets the stack also account for a next-greater element that wraps around to the front.",
                    true,
                    "The core mechanism - a stack of unresolved positions, popped when a greater value appears - carries over directly; only the scan range needs to extend to simulate wrapping around the array once.",
                ),
                choice(
                    "No - a circular array has no clear starting point, so a stack-based scan can't be applied at all.",
                    false,
                    "A circular array can still be scanned in a fixed order (twice through, using index mod length) - the stack mechanism itself doesn't require the array to have a fixed logical start.",
                ),
                choice(
                    "No - 'next greater element' is a completely different problem from 'days until warmer'.",
                    false,
                    "These are actually the same problem in different clothing - both ask 'how far until something bigger appears next,' which is exactly what a monotonic stack of unresolved positions answers.",
                )),
        ),
        step(
            "daily-temperatures", STACK, EDGE_CASE,
            "Which input would expose a bug in a version of this solution that initializes the answer array with -1 instead of 0?",
            conceptKey = "daily-temperatures-edge-case",
            choices = listOf(
                choice(
                    "Strictly decreasing temperatures, like [80, 70, 60], where the last day never finds a warmer day and the problem expects its answer to stay 0.",
                    true,
                    "Any day that's never popped off the stack keeps whatever its answer array slot was initialized to - the problem specifically expects that untouched value to be 0, not -1 or any other placeholder.",
                ),
                choice(
                    "Strictly increasing temperatures, like [60, 70, 80].",
                    false,
                    "Every day except the last one is resolved almost immediately by the very next day, so the untouched-initial-value case barely comes up here.",
                ),
                choice(
                    "A single-day array.",
                    false,
                    "A single day trivially has no future day to compare against, but this is really just a size-one version of the general 'no warmer day exists' case, not specific to the initialization bug.",
                )),
        ),
    ),
)

private val kokoEatingBananasWorkout = ProblemWorkout(
    problemSlug = "koko-eating-bananas",
    group = BINARY_SEARCH,
    steps = listOf(
        step(
            "koko-eating-bananas", BINARY_SEARCH, PATTERN_RECOGNITION,
            "Find the minimum integer eating speed that lets Koko finish all banana piles within h hours. Which pattern fits?",
            conceptKey = "koko-eating-bananas-pattern-recognition",
            choices = listOf(
                choice(
                    "Binary search over the range of possible eating speeds, checking feasibility at each candidate.",
                    true,
                    "As speed increases, the hours needed to finish never increases - that consistent, one-directional relationship between speed and total hours is exactly what makes binary searching over candidate speeds valid.",
                ),
                choice(
                    "Start at speed 1 and increase it by 1 each time, checking after every increase.",
                    false,
                    "This eventually finds the right answer, but checking every single speed one at a time from 1 upward can take as many steps as the largest pile size, far more work than necessary.",
                ),
                choice(
                    "Sort the piles from largest to smallest and eat the largest pile first every hour.",
                    false,
                    "The order in which piles are eaten doesn't change how many hours a given fixed speed takes overall, since each pile's hours depend only on its own size and the chosen speed.",
                )),
        ),
        step(
            "koko-eating-bananas", BINARY_SEARCH, APPROACH,
            "Which approach correctly finds the minimum working speed?",
            conceptKey = "koko-eating-bananas-approach",
            choices = listOf(
                choice(
                    "Binary search speeds from 1 to the largest pile size; at each candidate speed, check whether all piles finish within h hours, and narrow toward the smallest speed that works.",
                    true,
                    "Because feasibility only ever flips from no to yes as speed increases, never back, binary search can reliably home in on that single flip point.",
                ),
                choice(
                    "Binary search speeds from 1 to the largest pile size, but always move toward slower speeds regardless of whether the current speed is feasible.",
                    false,
                    "Ignoring whether a candidate speed is actually feasible and always moving one direction would search essentially at random rather than narrowing toward the true minimum.",
                ),
                choice(
                    "Compute the total number of bananas and divide by h to get the speed directly.",
                    false,
                    "A single division ignores that each pile must be finished within its own hour boundaries - a pile can't share leftover time with another pile, so this doesn't account for how eating time is actually spent.",
                )),
        ),
        step(
            "koko-eating-bananas", BINARY_SEARCH, STATE_SELECTION,
            "What state does this binary search need?",
            conceptKey = "koko-eating-bananas-state-selection",
            choices = listOf(
                choice(
                    "Low and high bounds on the candidate speed, initialized to 1 and the largest pile size.",
                    true,
                    "The largest pile size is always a feasible speed (finishing that pile in one hour), and 1 is the slowest possible speed, so these bounds safely contain the true answer.",
                ),
                choice(
                    "A sorted copy of the piles.",
                    false,
                    "Sorting the piles doesn't help compute the hours needed at a given speed - that only requires summing up each pile's required hours, regardless of order.",
                ),
                choice(
                    "A hash map from pile size to how many hours it has taken historically.",
                    false,
                    "There's no history to track - hours needed for a pile at a given speed is a direct calculation, not something learned from prior attempts.",
                )),
        ),
        step(
            "koko-eating-bananas", BINARY_SEARCH, BOUNDARY_UPDATE,
            "Which rule correctly narrows the speed range after checking a candidate?",
            conceptKey = "koko-eating-bananas-boundary-update",
            choices = listOf(
                choice(
                    "If the candidate speed finishes within h hours, discard it and search for something faster by moving low up past it.",
                    false,
                    "Discarding a speed that already works and searching faster moves away from the minimum, converging on a needlessly fast speed instead of the smallest one that still works.",
                ),
                choice(
                    "If the candidate speed finishes within h hours, it's feasible - keep it as a candidate and search for something slower by moving high down to it; otherwise move low up past it.",
                    true,
                    "Keeping a feasible speed in the range while searching for an even slower one, rather than discarding it, is what lets the search converge on the true minimum feasible speed.",
                ),
                choice(
                    "If the candidate speed does not finish within h hours, keep it as a candidate and search slower.",
                    false,
                    "A speed that doesn't finish in time can never be the answer, so it should never be kept as a candidate - the search should move toward faster speeds instead.",
                )),
        ),
        step(
            "koko-eating-bananas", BINARY_SEARCH, CODE_BLOCK,
            "Which snippet correctly implements the feasibility check and search?",
            conceptKey = "koko-eating-bananas-code-block",
            code = "fun hoursNeeded(speed: Int): Long = piles.sumOf { (it + speed - 1) / speed }\nvar low = 1\nvar high = piles.max()\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    // ???\n}\nreturn low",
            choices = listOf(
                choice(
                    "if (hoursNeeded(mid) <= h) high = mid else low = mid + 1",
                    true,
                    "When mid is fast enough, keeping it in range and searching slower (high = mid) converges toward the smallest feasible speed; when it isn't fast enough, low must move past it entirely.",
                    code = "if (hoursNeeded(mid) <= h) high = mid else low = mid + 1",
                ),
                choice(
                    "if (hoursNeeded(mid) <= h) low = mid + 1 else high = mid",
                    false,
                    "This moves past a speed that already works instead of keeping it as a candidate, converging on a needlessly fast speed rather than the true minimum.",
                    code = "if (hoursNeeded(mid) <= h) low = mid + 1 else high = mid",
                ),
                choice(
                    "if (hoursNeeded(mid) < h) high = mid else low = mid + 1",
                    false,
                    "Using a strict less-than instead of less-than-or-equal would treat a speed that finishes in *exactly* h hours as not feasible, incorrectly ruling it out.",
                    code = "if (hoursNeeded(mid) < h) high = mid else low = mid + 1",
                )),
            languageVariants = listOf(
                WorkoutCodeVariant(
                    PYTHON,
                    "def hours_needed(speed):\n    return sum((p + speed - 1) // speed for p in piles)\nlow = 1\nhigh = max(piles)\nwhile low < high:\n    mid = low + (high - low) // 2\n    # ???\nreturn low",
                    choices = listOf(
                        choice("if hours_needed(mid) <= h:\n    high = mid\nelse:\n    low = mid + 1", true, "When mid is fast enough, keeping it in range and searching slower (high = mid) converges toward the smallest feasible speed; when it isn't fast enough, low must move past it entirely.", code = "if hours_needed(mid) <= h:\n    high = mid\nelse:\n    low = mid + 1"),
                        choice("if hours_needed(mid) <= h:\n    low = mid + 1\nelse:\n    high = mid", false, "This moves past a speed that already works instead of keeping it as a candidate, converging on a needlessly fast speed rather than the true minimum.", code = "if hours_needed(mid) <= h:\n    low = mid + 1\nelse:\n    high = mid"),
                        choice("if hours_needed(mid) < h:\n    high = mid\nelse:\n    low = mid + 1", false, "Using a strict less-than instead of less-than-or-equal would treat a speed that finishes in exactly h hours as not feasible, incorrectly ruling it out.", code = "if hours_needed(mid) < h:\n    high = mid\nelse:\n    low = mid + 1"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVA,
                    "long hoursNeeded(int speed) {\n    long hours = 0;\n    for (int p : piles) hours += (p + speed - 1) / speed;\n    return hours;\n}\nint low = 1;\nint high = Arrays.stream(piles).max().getAsInt();\nwhile (low < high) {\n    int mid = low + (high - low) / 2;\n    // ???\n}\nreturn low;",
                    choices = listOf(
                        choice("if (hoursNeeded(mid) <= h) high = mid; else low = mid + 1;", true, "When mid is fast enough, keeping it in range and searching slower (high = mid) converges toward the smallest feasible speed; when it isn't fast enough, low must move past it entirely.", code = "if (hoursNeeded(mid) <= h) high = mid; else low = mid + 1;"),
                        choice("if (hoursNeeded(mid) <= h) low = mid + 1; else high = mid;", false, "This moves past a speed that already works instead of keeping it as a candidate, converging on a needlessly fast speed rather than the true minimum.", code = "if (hoursNeeded(mid) <= h) low = mid + 1; else high = mid;"),
                        choice("if (hoursNeeded(mid) < h) high = mid; else low = mid + 1;", false, "Using a strict less-than instead of less-than-or-equal would treat a speed that finishes in exactly h hours as not feasible, incorrectly ruling it out.", code = "if (hoursNeeded(mid) < h) high = mid; else low = mid + 1;"),
                    ),
                ),
                WorkoutCodeVariant(
                    JAVASCRIPT,
                    "function hoursNeeded(speed) {\n    return piles.reduce((sum, p) => sum + Math.ceil(p / speed), 0);\n}\nlet low = 1;\nlet high = Math.max(...piles);\nwhile (low < high) {\n    const mid = low + Math.floor((high - low) / 2);\n    // ???\n}\nreturn low;",
                    choices = listOf(
                        choice("if (hoursNeeded(mid) <= h) high = mid; else low = mid + 1;", true, "When mid is fast enough, keeping it in range and searching slower (high = mid) converges toward the smallest feasible speed; when it isn't fast enough, low must move past it entirely.", code = "if (hoursNeeded(mid) <= h) high = mid; else low = mid + 1;"),
                        choice("if (hoursNeeded(mid) <= h) low = mid + 1; else high = mid;", false, "This moves past a speed that already works instead of keeping it as a candidate, converging on a needlessly fast speed rather than the true minimum.", code = "if (hoursNeeded(mid) <= h) low = mid + 1; else high = mid;"),
                        choice("if (hoursNeeded(mid) < h) high = mid; else low = mid + 1;", false, "Using a strict less-than instead of less-than-or-equal would treat a speed that finishes in exactly h hours as not feasible, incorrectly ruling it out.", code = "if (hoursNeeded(mid) < h) high = mid; else low = mid + 1;"),
                    ),
                ),
                WorkoutCodeVariant(
                    CPP,
                    "long long hoursNeeded(int speed) {\n    long long hours = 0;\n    for (int p : piles) hours += (p + speed - 1) / speed;\n    return hours;\n}\nint low = 1;\nint high = *max_element(piles.begin(), piles.end());\nwhile (low < high) {\n    int mid = low + (high - low) / 2;\n    // ???\n}\nreturn low;",
                    choices = listOf(
                        choice("if (hoursNeeded(mid) <= h) high = mid; else low = mid + 1;", true, "When mid is fast enough, keeping it in range and searching slower (high = mid) converges toward the smallest feasible speed; when it isn't fast enough, low must move past it entirely.", code = "if (hoursNeeded(mid) <= h) high = mid; else low = mid + 1;"),
                        choice("if (hoursNeeded(mid) <= h) low = mid + 1; else high = mid;", false, "This moves past a speed that already works instead of keeping it as a candidate, converging on a needlessly fast speed rather than the true minimum.", code = "if (hoursNeeded(mid) <= h) low = mid + 1; else high = mid;"),
                        choice("if (hoursNeeded(mid) < h) high = mid; else low = mid + 1;", false, "Using a strict less-than instead of less-than-or-equal would treat a speed that finishes in exactly h hours as not feasible, incorrectly ruling it out.", code = "if (hoursNeeded(mid) < h) high = mid; else low = mid + 1;"),
                    ),
                ),
                WorkoutCodeVariant(
                    GO,
                    "hoursNeeded := func(speed int) int {\n    hours := 0\n    for _, p := range piles {\n        hours += (p + speed - 1) / speed\n    }\n    return hours\n}\nlow := 1\nhigh := 0\nfor _, p := range piles {\n    if p > high {\n        high = p\n    }\n}\nfor low < high {\n    mid := low + (high-low)/2\n    // ???\n}\nreturn low",
                    choices = listOf(
                        choice("if hoursNeeded(mid) <= h {\n    high = mid\n} else {\n    low = mid + 1\n}", true, "When mid is fast enough, keeping it in range and searching slower (high = mid) converges toward the smallest feasible speed; when it isn't fast enough, low must move past it entirely.", code = "if hoursNeeded(mid) <= h {\n    high = mid\n} else {\n    low = mid + 1\n}"),
                        choice("if hoursNeeded(mid) <= h {\n    low = mid + 1\n} else {\n    high = mid\n}", false, "This moves past a speed that already works instead of keeping it as a candidate, converging on a needlessly fast speed rather than the true minimum.", code = "if hoursNeeded(mid) <= h {\n    low = mid + 1\n} else {\n    high = mid\n}"),
                        choice("if hoursNeeded(mid) < h {\n    high = mid\n} else {\n    low = mid + 1\n}", false, "Using a strict less-than instead of less-than-or-equal would treat a speed that finishes in exactly h hours as not feasible, incorrectly ruling it out.", code = "if hoursNeeded(mid) < h {\n    high = mid\n} else {\n    low = mid + 1\n}"),
                    ),
                ),
                WorkoutCodeVariant(
                    SWIFT,
                    "func hoursNeeded(_ speed: Int) -> Int {\n    piles.reduce(0) { \$0 + (\$1 + speed - 1) / speed }\n}\nvar low = 1\nvar high = piles.max()!\nwhile low < high {\n    let mid = low + (high - low) / 2\n    // ???\n}\nreturn low",
                    choices = listOf(
                        choice("if hoursNeeded(mid) <= h { high = mid } else { low = mid + 1 }", true, "When mid is fast enough, keeping it in range and searching slower (high = mid) converges toward the smallest feasible speed; when it isn't fast enough, low must move past it entirely.", code = "if hoursNeeded(mid) <= h { high = mid } else { low = mid + 1 }"),
                        choice("if hoursNeeded(mid) <= h { low = mid + 1 } else { high = mid }", false, "This moves past a speed that already works instead of keeping it as a candidate, converging on a needlessly fast speed rather than the true minimum.", code = "if hoursNeeded(mid) <= h { low = mid + 1 } else { high = mid }"),
                        choice("if hoursNeeded(mid) < h { high = mid } else { low = mid + 1 }", false, "Using a strict less-than instead of less-than-or-equal would treat a speed that finishes in exactly h hours as not feasible, incorrectly ruling it out.", code = "if hoursNeeded(mid) < h { high = mid } else { low = mid + 1 }"),
                    ),
                ),
            ),
        ),
        step(
            "koko-eating-bananas", BINARY_SEARCH, TIME_COMPLEXITY,
            "With n piles and m as the largest pile size, what is the time complexity?",
            conceptKey = "koko-eating-bananas-time-complexity",
            choices = listOf(
                choice(
                    "O(log n * log m), because both the piles and the speed range are binary searched.",
                    false,
                    "Only the speed range is binary searched - computing the hours needed for a given speed still requires a plain linear pass over all n piles, not a search through them.",
                ),
                choice(
                    "O(n * m), because every possible speed from 1 to m is checked against every pile.",
                    false,
                    "Binary searching over the range of speeds means only about log m candidate speeds are ever checked, not all m of them individually.",
                ),
                choice(
                    "O(n log m), because binary search over speeds takes O(log m) iterations, and each checks feasibility across all n piles.",
                    true,
                    "The speed range from 1 to m shrinks logarithmically through binary search, and computing the hours needed for a candidate speed requires looking at every one of the n piles once.",
                )),
        ),
        step(
            "koko-eating-bananas", BINARY_SEARCH, SPACE_COMPLEXITY,
            "How much extra space does this approach use?",
            conceptKey = "koko-eating-bananas-space-complexity",
            choices = listOf(
                choice(
                    "O(1), because only a fixed handful of variables - the speed bounds and a running hour total - are tracked at any point.",
                    true,
                    "Checking feasibility for a candidate speed only needs a running total updated pile by pile, and the binary search over speeds only needs its own low and high bounds.",
                ),
                choice(
                    "O(n), because the hours needed for each pile must be stored to sum them.",
                    false,
                    "Each pile's hours are added directly into a running total as the loop goes, so nothing proportional to the number of piles needs to be stored at once.",
                ),
                choice(
                    "O(log m), matching the number of binary search iterations performed.",
                    false,
                    "The number of iterations the binary search performs affects how long it runs, not how much memory is used at any single point.",
                )),
        ),
        step(
            "koko-eating-bananas", BINARY_SEARCH, TRANSFER,
            "Would this same binary-search-on-the-answer idea also work for finding the minimum ship capacity needed to deliver all packages within D days?",
            conceptKey = "koko-eating-bananas-transfer",
            choices = listOf(
                choice(
                    "Yes - binary search over possible capacities, checking at each candidate how many days it would take, the same way speed is checked against hours here.",
                    true,
                    "Both problems share the same shape: a monotonic relationship between a candidate value and whether it's 'good enough,' which is exactly what makes binary searching over the range of possible answers valid.",
                ),
                choice(
                    "No - shipping capacity depends on package weights, which can't be searched over the same way a speed can.",
                    false,
                    "The specific quantity being searched doesn't matter - what matters is that increasing capacity only ever makes the days-needed go down or stay the same, the same one-directional relationship speed has with hours here.",
                ),
                choice(
                    "No - binary search on the answer only works for eating-speed-style problems specifically.",
                    false,
                    "Binary search on the answer is a general technique for any problem asking for the minimum (or maximum) value satisfying a feasibility check that only flips one way as the value increases.",
                )),
        ),
        step(
            "koko-eating-bananas", BINARY_SEARCH, EDGE_CASE,
            "Which input would break a version of this solution that uses plain integer division instead of ceiling division for hours needed?",
            conceptKey = "koko-eating-bananas-edge-case",
            choices = listOf(
                choice(
                    "A pile of size 7 with a candidate speed of 3, where 7 doesn't divide evenly by 3.",
                    true,
                    "Plain integer division would compute 7 / 3 = 2 hours, silently dropping the extra hour needed to finish the remaining 1 banana - the correct value is ceiling(7/3) = 3 hours.",
                ),
                choice(
                    "A pile of size 6 with a candidate speed of 3, where 6 divides evenly by 3.",
                    false,
                    "When the pile size divides evenly by the speed, plain division and ceiling division give the identical answer, so this case wouldn't expose the missing rounding-up bug.",
                ),
                choice(
                    "h exactly equal to the number of piles.",
                    false,
                    "This tests a different edge - it forces the minimum speed up to the largest single pile - but it doesn't specifically depend on whether division rounds up or not for every pile.",
                )),
        ),
    ),
)

private val findMinimumInRotatedSortedArrayWorkout = ProblemWorkout(
    problemSlug = "find-minimum-in-rotated-sorted-array",
    group = BINARY_SEARCH,
    steps = listOf(
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, PATTERN_RECOGNITION,
            "A sorted array has been rotated at an unknown pivot. Find its minimum element. Which pattern fits?",
            conceptKey = "find-minimum-in-rotated-sorted-array-pattern-recognition",
            choices = listOf(
                choice(
                    "Binary search, comparing the middle element to the rightmost element to decide which half contains the rotation point.",
                    true,
                    "Comparing the middle to the right edge reveals which side is the untouched, still-sorted portion and which side contains the rotation point where the minimum hides, letting half the array be discarded each step.",
                ),
                choice(
                    "Scan the array once, tracking the smallest value seen so far.",
                    false,
                    "This correctly finds the minimum, but scanning every element takes linear time and ignores the fact that most of the array is still sorted in two pieces.",
                ),
                choice(
                    "Two pointers starting at both ends, moving inward.",
                    false,
                    "The minimum isn't necessarily positioned symmetrically from the two ends - it sits specifically at the rotation point, which two pointers closing inward has no way to target directly.",
                )),
        ),
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, APPROACH,
            "Which approach correctly finds the minimum?",
            conceptKey = "find-minimum-in-rotated-sorted-array-approach",
            choices = listOf(
                choice(
                    "Binary search by comparing the middle element to the rightmost element; if the middle is larger, the minimum lies to its right, and if smaller, the minimum lies at or to its left.",
                    true,
                    "This comparison always identifies which half currently holds the rotation point, so half the array can always be safely discarded on every step.",
                ),
                choice(
                    "Binary search by comparing the middle element to the leftmost element; if the middle is larger, the minimum lies at or to its left.",
                    false,
                    "Comparing against the left edge instead of the right edge doesn't reliably reveal which half currently holds the rotation point, since the left portion can look consistent either way.",
                ),
                choice(
                    "Compare the first and last elements of the array; if the first is smaller, return it, otherwise scan linearly for the minimum.",
                    false,
                    "This only handles the case where the array wasn't rotated at all - for a genuinely rotated array, it falls back to a linear scan instead of continuing to use binary search.",
                )),
        ),
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, STATE_SELECTION,
            "What state does this binary search need?",
            conceptKey = "find-minimum-in-rotated-sorted-array-state-selection",
            choices = listOf(
                choice(
                    "A low index and a high index marking the current search range.",
                    true,
                    "Everything needed to decide which half still contains the rotation point comes from comparing the values at these two boundary indices to the middle.",
                ),
                choice(
                    "A separate sorted copy of the array to compare against.",
                    false,
                    "Rebuilding a sorted copy would need to know the answer already - the whole point is finding the minimum directly from the rotated array using comparisons alone.",
                ),
                choice(
                    "A count of how many rotations the array has undergone.",
                    false,
                    "The number of rotations isn't needed and isn't given - the comparison-based search finds the minimum directly without ever needing to know how the array got rotated.",
                )),
        ),
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, BOUNDARY_UPDATE,
            "Which rule correctly narrows the search range?",
            conceptKey = "find-minimum-in-rotated-sorted-array-boundary-update",
            choices = listOf(
                choice(
                    "If nums[mid] > nums[high], set high = mid - 1; otherwise set low = mid + 1.",
                    false,
                    "Moving high to mid - 1 when the minimum could be at or before mid can eliminate mid even when it might actually be the answer, since the minimum was never confirmed to be strictly before mid.",
                ),
                choice(
                    "If nums[mid] > nums[high], the rotation point (and minimum) is strictly to the right, so set low = mid + 1; otherwise the minimum is at mid or to its left, so set high = mid.",
                    true,
                    "Keeping mid in range on the second branch matters because mid itself could already be the minimum - excluding it entirely would risk skipping right past the correct answer.",
                ),
                choice(
                    "If nums[mid] > nums[low], the rotation point is to the right, so set low = mid + 1.",
                    false,
                    "Comparing against the left edge instead of the right edge doesn't reliably indicate which half currently holds the rotation point.",
                )),
        ),
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, CODE_BLOCK,
            "Which snippet correctly implements the search?",
            conceptKey = "find-minimum-in-rotated-sorted-array-code-block",
            code = "var low = 0\nvar high = nums.size - 1\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    // ???\n}\nreturn nums[low]",
            choices = listOf(
                choice(
                    "if (nums[mid] > nums[high]) low = mid + 1 else high = mid",
                    true,
                    "This keeps mid in the range whenever it could still be the minimum, and only excludes it entirely when it's confirmed to be part of the larger, still-sorted left portion.",
                    code = "if (nums[mid] > nums[high]) low = mid + 1 else high = mid",
                ),
                choice(
                    "if (nums[mid] > nums[high]) high = mid - 1 else low = mid + 1",
                    false,
                    "This reverses which direction each comparison moves the bounds, so the search space shrinks toward the wrong end of the array and never actually converges on the minimum.",
                    code = "if (nums[mid] > nums[high]) high = mid - 1 else low = mid + 1",
                ),
                choice(
                    "if (nums[mid] > nums[low]) low = mid + 1 else high = mid",
                    false,
                    "Comparing mid against low instead of high doesn't reliably reveal which side currently holds the rotation point, since the left portion can look consistent with either case.",
                    code = "if (nums[mid] > nums[low]) low = mid + 1 else high = mid",
                )),
        ),
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, TIME_COMPLEXITY,
            "With n as the number of elements, what is the time complexity?",
            conceptKey = "find-minimum-in-rotated-sorted-array-time-complexity",
            choices = listOf(
                choice(
                    "O(n log n), because the array must first be checked for whether it's actually rotated at all.",
                    false,
                    "No separate check for rotation happens before the search begins - the same comparison logic naturally handles an unrotated array as a special case where the minimum sits at index 0.",
                ),
                choice(
                    "O(n), because in the worst case the rotation point could be anywhere in the array.",
                    false,
                    "Where the rotation point happens to sit doesn't matter - every comparison still discards roughly half of the remaining range, regardless of which half turns out to hold the minimum.",
                ),
                choice(
                    "O(log n), because each comparison between the middle and an edge value discards half of the remaining search range.",
                    true,
                    "Comparing the middle to the rightmost value reliably identifies which half can be safely thrown away on every step, so the range shrinks logarithmically just like standard binary search.",
                )),
        ),
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, SPACE_COMPLEXITY,
            "How much extra space does this binary search use?",
            conceptKey = "find-minimum-in-rotated-sorted-array-space-complexity",
            choices = listOf(
                choice(
                    "O(1), because only a fixed handful of variables - low, high, and mid - are tracked regardless of array size.",
                    true,
                    "The array is read in place using index comparisons, and the search only ever updates the same few integer variables as it narrows the range.",
                ),
                choice(
                    "O(log n), matching the number of comparisons the search performs before converging.",
                    false,
                    "The number of comparisons performed affects the running time, not the memory used at any single moment - each comparison reuses the same few tracked variables.",
                ),
                choice(
                    "O(n), because the original unrotated array must be reconstructed to compare against.",
                    false,
                    "Nothing about this approach reconstructs the original unrotated array - the minimum is located directly within the rotated array using comparisons alone.",
                )),
        ),
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, TRANSFER,
            "Would this same rotation-aware comparison also help with searching for a specific target value in a rotated sorted array?",
            conceptKey = "find-minimum-in-rotated-sorted-array-transfer",
            choices = listOf(
                choice(
                    "Yes - the same idea of checking which half is currently sorted extends naturally to deciding which half a target value could be hiding in.",
                    true,
                    "Once you know how to tell which half of a rotated range is the sorted one, checking whether the target falls within that sorted half's value range is a small, direct extension of the same reasoning.",
                ),
                choice(
                    "No - searching for a specific value in a rotated array requires un-rotating it back to sorted order first.",
                    false,
                    "Un-rotating the array would take linear time and defeats the purpose of a logarithmic search - the same binary search idea, extended slightly, works directly on the rotated array.",
                ),
                choice(
                    "No - finding the minimum and finding an arbitrary target are unrelated problems.",
                    false,
                    "Both problems rely on the exact same structural insight: exactly one half of any given range in a rotated sorted array is always fully sorted, which both variants exploit.",
                )),
        ),
        step(
            "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, EDGE_CASE,
            "Which input would break a version of this solution that assumes the array is always genuinely rotated?",
            conceptKey = "find-minimum-in-rotated-sorted-array-edge-case",
            choices = listOf(
                choice(
                    "An array rotated by its full length, leaving it in its original, unrotated sorted order, like [1,2,3,4] rotated by 4.",
                    true,
                    "A version that assumes a 'break point' always exists somewhere in the middle could mishandle the case where nums[mid] never exceeds nums[high] anywhere, since the array is actually still fully sorted from the start.",
                ),
                choice(
                    "An array rotated somewhere in the middle, like [4,5,6,1,2,3].",
                    false,
                    "This is the typical rotated case the algorithm is built to handle directly - it doesn't expose a bug specific to the assume-always-rotated version.",
                ),
                choice(
                    "An array with only one element.",
                    false,
                    "A single element trivially has no meaningful rotation and the loop condition low < high never even runs - this doesn't stress the rotation-detection logic.",
                )),
        ),
    ),
)

/** Central registry, following the same shape as [RoadmapLessons]. */
object ProblemWorkouts {
    /** Beginner-tier workouts, one full step list (including EDGE_CASE, currently unused by any mode) per problem. */
    private val beginnerWorkouts: List<ProblemWorkout> = listOf(
        minimumSizeSubarraySum,
        validAnagramWorkout,
        twoSumWorkout,
        containsDuplicateWorkout,
        threeSumWorkout,
        containerWithMostWaterWorkout,
        validPalindromeWorkout,
        validParenthesesWorkout,
        minStackWorkout,
        dailyTemperaturesWorkout,
        binarySearchWorkout,
        kokoEatingBananasWorkout,
        findMinimumInRotatedSortedArrayWorkout,
        bestTimeToBuyAndSellStockWorkout,
        longestSubstringWorkout,
    )

    /**
     * Every step, across every difficulty tier, merged back into one
     * [ProblemWorkout] per problem slug - the round engine filters by
     * [WorkoutStep.difficulty] itself, so it doesn't matter that a problem's
     * steps here span more than one tier.
     */
    private val all: List<ProblemWorkout> = run {
        val extraSteps = developingWorkoutSteps + intermediateWorkoutSteps + advancedWorkoutSteps
        val extraBySlug = extraSteps.groupBy { it.problemSlug }
        beginnerWorkouts.map { workout ->
            val extra = extraBySlug[workout.problemSlug].orEmpty()
            if (extra.isEmpty()) workout else workout.copy(steps = workout.steps + extra)
        }
    }

    fun bySlug(slug: String): ProblemWorkout? = all.firstOrNull { it.problemSlug == slug }
    fun byGroup(group: PatternGroup): List<ProblemWorkout> = all.filter { it.group == group }
    val workouts: List<ProblemWorkout> get() = all
}
