package com.codingarena.content

import com.codingarena.domain.model.PatternGroup.ARRAYS_HASHING
import com.codingarena.domain.model.PatternGroup.BINARY_SEARCH
import com.codingarena.domain.model.PatternGroup.SLIDING_WINDOW
import com.codingarena.domain.model.PatternGroup.STACK
import com.codingarena.domain.model.PatternGroup.TWO_POINTERS
import com.codingarena.domain.model.PracticeDifficulty.INTERMEDIATE
import com.codingarena.domain.model.WorkoutStep
import com.codingarena.domain.model.WorkoutStepKind.APPROACH
import com.codingarena.domain.model.WorkoutStepKind.BOUNDARY_UPDATE
import com.codingarena.domain.model.WorkoutStepKind.CODE_BLOCK
import com.codingarena.domain.model.WorkoutStepKind.PATTERN_RECOGNITION
import com.codingarena.domain.model.WorkoutStepKind.SPACE_COMPLEXITY
import com.codingarena.domain.model.WorkoutStepKind.STATE_SELECTION
import com.codingarena.domain.model.WorkoutStepKind.TIME_COMPLEXITY

/**
 * Intermediate tier: mixed pattern signals, multi-step tracing, subtler bugs,
 * less hand-holding in the prompt than Developing. Same 15 problems, ten
 * steps per topic matching the round quota, same doubled-kind conceptKey
 * sharing pattern as [developingWorkoutSteps].
 */
internal val intermediateWorkoutSteps: List<WorkoutStep> = listOf(
    // ------------------------------------------------------------ Arrays & Hashing
    step(
        "two-sum", ARRAYS_HASHING, PATTERN_RECOGNITION,
        "nums = [3, 2, 4], target = 6, but nums also contains a repeated value elsewhere in a larger version of this array. Which structure keeps the lookup correct as duplicates pile up?",
        conceptKey = "arrays-hashing-index-vs-value-keying", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "A map from value to index, since only the most recent index for a repeated value is ever the one that matters for finding a still-valid complement.",
                true,
                "Even with duplicate values, only the most recently seen index for a given value can pair with a future complement - overwriting the map entry for a repeated value is exactly the right behavior, not a bug.",
            ),
            choice(
                "A map from value to a list of every index that value appeared at.",
                false,
                "Tracking every past index for a value adds complexity that isn't needed here - the earliest occurrence of a duplicate can never be the correct answer once a later occurrence exists at the same value.",
            ),
            choice(
                "A set of values seen so far, checked for membership only.",
                false,
                "A plain set can confirm a complement exists but can't report which index it came from, and the problem specifically asks for the pair of indices.",
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, PATTERN_RECOGNITION,
        "nums = [1, 2, 3, 1] versus nums = [1, 2, 3, 4] - both need a single boolean answer. What's the most defensible reason to reach for a set here rather than tracking counts?",
        conceptKey = "arrays-hashing-index-vs-value-keying", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "The question only ever needs a yes/no answer about existence, and a set answers 'has this value appeared before' with no need to know how many times.",
                true,
                "Counting how many times each value appears is more information than the question requires - membership alone is enough to answer whether any value repeats.",
            ),
            choice(
                "A set is required because counting could overflow for large arrays.",
                false,
                "Overflow isn't a realistic concern for counting occurrences within a single array - the actual reason a set suffices is that the question only needs existence, not frequency.",
            ),
            choice(
                "Counts are needed instead, since the problem might later ask which value repeats most.",
                false,
                "This specific problem only asks whether any duplicate exists at all, not which value repeats most - a set is sufficient for the question as stated.",
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, APPROACH,
        "s = \"rat\", t = \"car\" - same length, overlapping letters, but not anagrams. Which single check, added to the count-map comparison, most directly explains why?",
        conceptKey = "arrays-hashing-partial-overlap-detection", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "That every final count is exactly zero, not just that some counts are - a nonzero count for even one letter (like 'r' or 't' here) means the letters used don't fully match.",
                true,
                "Partial letter overlap between two strings can still leave some counts nonzero after the increment/decrement pass - checking every single count, not just some of them, is what catches this.",
            ),
            choice(
                "That the total character count matches - which it already does here since both strings have length 3.",
                false,
                "Matching total length is necessary but not sufficient - \"rat\" and \"car\" are the same length yet clearly aren't anagrams, so length alone can't be the deciding check.",
            ),
            choice(
                "That the first character of each string matches.",
                false,
                "The first characters don't need to match for two strings to be anagrams - anagrams can rearrange characters in any order, including the very first one.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, APPROACH,
        "nums = [0, 4, 3, 0], target = 0 - the correct pair uses two zeros at different positions. Walk through why the check-then-insert order still gets this right.",
        conceptKey = "arrays-hashing-zero-and-negative-values", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "At the second 0 (index 3), the complement (0 - 0 = 0) is looked up in the map, which by then already holds the first 0's index from index 0 - the check happens before the second 0 overwrites anything.",
                true,
                "The order matters precisely in cases like this: index 3's complement lookup finds index 0's earlier entry before index 3 ever inserts itself, correctly pairing the two distinct zero positions.",
            ),
            choice(
                "It fails here specifically, since zero as a complement is a special case the map can't represent.",
                false,
                "Zero is stored and looked up in the map exactly like any other integer value - there's nothing special about it that breaks the map-based approach.",
            ),
            choice(
                "It only works because target itself happens to be zero.",
                false,
                "The target's value doesn't change how the algorithm behaves - the same check-then-insert logic handles any target correctly, zero included.",
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, STATE_SELECTION,
        "nums has 200,000 elements, mostly distinct, with one pair of duplicates near the very end. Which detail of the set-based approach makes this a near-best-case run rather than a worst case?",
        conceptKey = "arrays-hashing-best-vs-worst-case-shape", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Nothing about the duplicate's late position matters much - the set has to grow to hold nearly the entire array regardless, since no early exit is possible until the duplicate is finally reached.",
                true,
                "A duplicate appearing near the end means the set still grows close to its maximum size before the answer is found - this is actually closer to the worst case than a best case for how much state gets built up.",
            ),
            choice(
                "It's the best case because the set only needs to hold a handful of elements before returning true.",
                false,
                "With the duplicate near the end, the set has already grown to hold nearly every distinct value in the array by the time it's found - it's not a small set at that point.",
            ),
            choice(
                "It's the best case because sets resize more efficiently when nearly full.",
                false,
                "Set resizing behavior isn't what determines best or worst case here - what matters is how much of the array gets scanned (and how large the set grows) before a duplicate is found.",
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, BOUNDARY_UPDATE,
        "This version handles Unicode strings but still allocates a fixed-size IntArray(26). Which input silently produces a wrong result rather than crashing?",
        conceptKey = "arrays-hashing-fixed-alphabet-assumption", difficulty = INTERMEDIATE,
        code = "val counts = IntArray(26)\nfor (c in s) counts[c - 'a']++\nfor (c in t) counts[c - 'a']--\nreturn counts.all { it == 0 }",
        choices = listOf(
            choice(
                "s and t containing accented or non-English letters - c - 'a' produces an index outside 0..25, either crashing or silently corrupting unrelated slots depending on the language's array bounds behavior.",
                true,
                "The fixed 26-slot array assumes only lowercase English letters - characters outside that range compute an out-of-range index, which is a correctness bug baked into the assumption itself, not just an edge case.",
            ),
            choice(
                "s and t both being empty strings.",
                false,
                "Two empty strings never enter either loop at all, so the fixed-alphabet assumption is never exercised - this doesn't expose the bug.",
            ),
            choice(
                "s and t of different lengths but using only lowercase English letters.",
                false,
                "Different lengths using only the assumed alphabet range would still correctly leave a nonzero count somewhere - this doesn't touch the out-of-range indexing problem.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, CODE_BLOCK,
        "nums = [-3, 4, 3, 90], target = 0. Trace which snippet correctly returns the indices of -3 and 3.",
        conceptKey = "arrays-hashing-negative-target-trace", difficulty = INTERMEDIATE,
        code = "val seen = HashMap<Int, Int>()\nfor (i in nums.indices) {\n    val complement = target - nums[i]\n    // ???\n}",
        choices = listOf(
            choice(
                "if (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
                true,
                "At i=0, complement is 0-(-3)=3, not yet seen, so -3 is stored; at i=2, complement is 0-3=-3, which is now in the map from i=0, correctly returning [0, 2].",
                code = "if (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
            ),
            choice(
                "if (seen.containsValue(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
                false,
                "Checking containsValue instead of containsKey searches the map's indices for a match against the complement, rather than its stored values - complement should be looked up as a value that was stored, i.e. a key.",
                code = "if (seen.containsValue(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
            ),
            choice(
                "seen[complement] = i\nif (seen.containsKey(nums[i])) return intArrayOf(seen[nums[i]]!!, i)",
                false,
                "Storing the complement instead of the current value, and checking for the current value instead of the complement, inverts the whole lookup and won't reliably find matching pairs.",
                code = "seen[complement] = i\nif (seen.containsKey(nums[i])) return intArrayOf(seen[nums[i]]!!, i)",
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, CODE_BLOCK,
        "A teammate wants the function to also report *which* value duplicated, not just true/false, without a second pass. Which snippet achieves this cleanly?",
        conceptKey = "arrays-hashing-return-value-vs-boolean", difficulty = INTERMEDIATE,
        code = "val seen = HashSet<Int>()\nfor (num in nums) {\n    // ??? return the duplicate value, or null if none\n}\nreturn null",
        choices = listOf(
            choice(
                "if (!seen.add(num)) return num",
                true,
                "HashSet.add returns false exactly when the value already existed, so returning num at that moment reports which specific value was the duplicate, in the same single pass.",
                code = "if (!seen.add(num)) return num",
            ),
            choice(
                "if (seen.contains(num)) return num\nseen.add(num)",
                false,
                "This actually works too, but doing two separate operations (a lookup then an insert) instead of the single add() call is less direct for the same result - not wrong, but worth recognizing as equivalent rather than uniquely correct.",
                code = "if (seen.contains(num)) return num\nseen.add(num)",
            ),
            choice(
                "seen.add(num)\nif (seen.size < nums.indexOf(num) + 1) return num",
                false,
                "Comparing set size against indexOf(num) + 1 is both slow (indexOf is itself a linear scan) and doesn't reliably identify the duplicate value in all cases.",
                code = "seen.add(num)\nif (seen.size < nums.indexOf(num) + 1) return num",
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, TIME_COMPLEXITY,
        "Two candidate implementations both claim O(n): one uses a HashMap<Char, Int>, the other a fixed IntArray(26). Do they actually have the same real-world performance despite the same complexity class?",
        conceptKey = "arrays-hashing-hashmap-vs-array-constant-factor", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Not exactly - both are O(n) asymptotically, but the array avoids hashing overhead and boxing that a HashMap<Char, Int> incurs per operation, making its constant factor smaller in practice.",
                true,
                "Big-O complexity describes how work scales with input size, not the exact runtime - two O(n) approaches can still differ meaningfully in practice due to per-operation overhead like hashing and object boxing.",
            ),
            choice(
                "Yes - same complexity class always means identical real-world speed.",
                false,
                "Complexity class only describes the growth rate as input size increases - it deliberately ignores constant-factor differences like hashing overhead, which can still matter in practice.",
            ),
            choice(
                "No - the array version is actually a different, worse complexity class, O(n squared).",
                false,
                "The array-based version is still O(n) - a single pass with constant-time array indexing per character doesn't introduce any quadratic behavior.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, SPACE_COMPLEXITY,
        "Compare the space used when the answer pair is found at the very first two elements versus when it's found at the very last two elements. Does this change the worst-case space complexity?",
        conceptKey = "arrays-hashing-early-vs-late-match-space", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "No - worst-case space complexity considers the scenario where the map grows largest before a match, which is still O(n) regardless of typical or average placement of the answer.",
                true,
                "An early match happens to use less space in that particular run, but worst-case analysis is specifically about the input that makes the algorithm do the most work - here, that's still bounded by O(n).",
            ),
            choice(
                "Yes - an early match makes the space complexity O(1) instead of O(n).",
                false,
                "A single lucky run finding the answer early doesn't change the complexity class - worst-case analysis considers what happens across all possible inputs, including ones where the match is found late or not until the very end.",
            ),
            choice(
                "Yes - a late match makes the space complexity O(n squared).",
                false,
                "Even in the worst case, the map holds at most n entries - one per array element - which is O(n), not O(n squared).",
            ),
        ),
    ),

    // ------------------------------------------------------------ Two Pointers
    step(
        "valid-palindrome", TWO_POINTERS, PATTERN_RECOGNITION,
        "\"0P\" should be treated as \"0p\" - a valid two-character palindrome once case is normalized, and both characters are alphanumeric. What subtlety about mixed digits and letters does this test?",
        conceptKey = "two-pointers-mixed-alphanumeric-comparison", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "That case-insensitivity only applies to letters - digits have no case, so lowercasing '0' leaves it unchanged, and the comparison still correctly treats '0' and 'p'/'P' as matching only when the letters themselves match after normalization.",
                true,
                "Digits pass through case normalization unaffected, which is exactly why lowercasing everything before comparing works uniformly for a mix of letters and digits without needing separate handling.",
            ),
            choice(
                "That digits should be excluded from the palindrome check entirely.",
                false,
                "Digits are explicitly alphanumeric and must be included in the comparison, not skipped - only truly non-alphanumeric characters like punctuation and spaces get filtered out.",
            ),
            choice(
                "That '0' and 'O' (the letter) should be treated as equivalent.",
                false,
                "The digit zero and the letter O are different characters entirely - case-insensitivity only unifies a letter with its own different-case version, not visually similar but distinct characters.",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, PATTERN_RECOGNITION,
        "nums = [0, 0, 0, 0] - every element is the same value. Walk through why the sorted two-pointer approach still returns exactly one triplet, not four or more.",
        conceptKey = "two-pointers-mixed-alphanumeric-comparison", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Because the duplicate-skipping rule advances past repeated values both for the fixed outer index and for the inner pointers after a match, so the same combination of values is never recorded more than once.",
                true,
                "With every value identical, the duplicate-skip logic collapses what would otherwise be many redundant discoveries of [0,0,0] down to exactly one, by refusing to re-examine already-tried duplicate positions.",
            ),
            choice(
                "Because zero is treated as a special sentinel value that automatically deduplicates.",
                false,
                "Zero isn't handled any differently from any other value here - the deduplication comes entirely from the general duplicate-skipping rule, not from anything specific to the value 0.",
            ),
            choice(
                "Because the array has fewer than three distinct values, so the algorithm exits early.",
                false,
                "The algorithm doesn't exit early based on how many distinct values exist - it still runs its normal fixed-index-plus-two-pointers process, which happens to naturally produce just one triplet here.",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, APPROACH,
        "heights = [4, 3, 2, 1, 4] - the true maximum uses the two 4s at the very ends. Trace how the shorter-side-moves rule still reaches this answer despite everything in between being smaller.",
        conceptKey = "two-pointers-endpoints-survive-shrinking", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Since both ends start tied at height 4, moving either side first still keeps the other 4 in place as a pointer boundary, and the algorithm naturally re-examines the full width against it before either 4 is ever moved away from.",
                true,
                "The area between the two matching 4s, using the full array width, is checked right at the very first comparison - the shorter-side-moves rule doesn't discard either endpoint until it's actually been outclassed by a taller candidate, which never happens here.",
            ),
            choice(
                "It doesn't reach this answer without a special case for tied heights at the two ends.",
                false,
                "No special case is needed - the very first comparison, before either pointer has moved, already captures the maximum-width container using both 4s, since that's exactly the starting position.",
            ),
            choice(
                "It only works because the smaller values in the middle are irrelevant to the answer.",
                false,
                "The middle values being smaller is true here, but the algorithm doesn't know that in advance - it still checks intermediate combinations as the pointers move, it just doesn't end up finding anything larger than the initial full-width area.",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, APPROACH,
        "nums = [-2, 0, 1, 1, 2] - after fixing i at the -2, walk through why nums[low] <= target && target < nums[mid]-style two-pointer movement correctly finds both valid pairs without needing the array re-sorted per outer iteration.",
        conceptKey = "two-pointers-fixed-index-reuse-sorted-order", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Because the whole array is sorted exactly once up front, and fixing a different outer index just changes where the inner two pointers start - the sortedness itself never needs to be redone.",
                true,
                "Sorting is a one-time O(n log n) cost paid before the outer loop begins - every subsequent fixed index reuses that same sorted array, with only the two inner pointers' starting positions changing.",
            ),
            choice(
                "Because the array happens to already be a valid max-heap after sorting.",
                false,
                "A sorted array and a heap are different structures with different guarantees - what matters here is simply that ascending order lets two pointers reliably narrow toward a target sum, not any heap property.",
            ),
            choice(
                "Because only the first fixed index requires the array to be sorted.",
                false,
                "The sorted order is relied upon for every fixed index's two-pointer search, not just the first one - each one depends on the same global ascending order to know which direction to move.",
            ),
        ),
    ),
    step(
        "valid-palindrome", TWO_POINTERS, STATE_SELECTION,
        "\"A man, a plan, a canal: Panama\" - trace what left and right point to right after the very first skip-and-compare step.",
        conceptKey = "two-pointers-skip-then-compare-trace", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "left moves from index 0 ('A') to itself (already alphanumeric, no skip needed) and right skips backward from the final index (the letter 'a') similarly needing no skip - both compare 'a' to 'a' (lowercased) and match, then both pointers advance inward.",
                true,
                "Since the string both starts and ends with a letter already, no skipping happens on the very first step - the first real comparison is 'A' vs 'a', which match after lowercasing, and both pointers move one step inward.",
            ),
            choice(
                "left skips past the space after 'A' before any comparison happens.",
                false,
                "left starts at index 0, which is the letter 'A' itself, not the space after it - no skipping is needed before the very first comparison in this string.",
            ),
            choice(
                "right skips past the colon before ever reaching a letter.",
                false,
                "right starts at the very last character of the string, which is the letter 'a' in \"Panama\" - the colon is in the middle of the string, not at the position right initially points to.",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, BOUNDARY_UPDATE,
        "This version uses `<=` instead of `<` when comparing heights, moving right when heights are equal instead of left. Does this change correctness?",
        conceptKey = "two-pointers-tie-break-direction", difficulty = INTERMEDIATE,
        code = "if (height[left] <= height[right]) left++ else right--",
        choices = listOf(
            choice(
                "No - when heights are equal, moving either pointer is equally valid, since neither one is uniquely the limiting height at that moment; the area found is the same either way.",
                true,
                "The correctness of the shorter-side-moves rule only depends on never moving the strictly taller side - when both sides are tied, moving either one still respects that rule, so the algorithm still finds the true maximum.",
            ),
            choice(
                "Yes - it will now overwrite maxArea with a smaller, incorrect value on ties.",
                false,
                "Which pointer moves on a tie doesn't affect the area computed at the current step, and maxArea is always updated with maxOf against the running best - a tie doesn't cause it to shrink.",
            ),
            choice(
                "Yes - it causes the loop to terminate one iteration early on ties.",
                false,
                "The loop's termination condition (left < right) isn't affected by which specific pointer moves on any given iteration - it still runs until the two pointers meet.",
            ),
        ),
    ),
    step(
        "valid-palindrome", TWO_POINTERS, CODE_BLOCK,
        "s = \"race a car\" should return false. Which snippet correctly identifies the mismatch, and at which comparison does it first occur?",
        conceptKey = "two-pointers-mismatch-detection-trace", difficulty = INTERMEDIATE,
        code = "var left = 0\nvar right = s.length - 1\nwhile (left < right) {\n    while (left < right && !s[left].isLetterOrDigit()) left++\n    while (left < right && !s[right].isLetterOrDigit()) right--\n    // ???\n    left++\n    right--\n}\nreturn true",
        choices = listOf(
            choice(
                "if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false",
                true,
                "Filtered down to \"raceacar\", the very first comparison is 'r' (left) against 'r' (right) - matching - but the mismatch surfaces two steps in, comparing 'a' against 'a' still matches, then 'c' against 'a' fails, correctly returning false.",
                code = "if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false",
            ),
            choice(
                "if (s[left] != s[right]) return false",
                false,
                "Without lowercasing, this would incorrectly treat differently-cased matching letters as mismatches, though it happens to still work on this particular all-lowercase example - it's not reliable in general.",
                code = "if (s[left] != s[right]) return false",
            ),
            choice(
                "if (s.substring(left, right).contains(' ')) return false",
                false,
                "Checking for a literal space in the remaining substring has nothing to do with comparing the filtered, case-normalized characters at the two pointer positions.",
                code = "if (s.substring(left, right).contains(' ')) return false",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, CODE_BLOCK,
        "nums = [-4, -1, -1, 0, 1, 2]. Trace which snippet correctly finds the triplet [-1, -1, 2] without also finding [-1, 0, 1] a second time under a different fixed index.",
        conceptKey = "two-pointers-fixed-index-duplicate-trace", difficulty = INTERMEDIATE,
        code = "for (i in nums.indices) {\n    if (i > 0 && nums[i] == nums[i - 1]) continue\n    var left = i + 1\n    var right = nums.size - 1\n    while (left < right) {\n        val sum = nums[i] + nums[left] + nums[right]\n        // ???\n    }\n}",
        choices = listOf(
            choice(
                "if (sum < 0) left++ else if (sum > 0) right-- else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right--; while (left < right && nums[left] == nums[left - 1]) left++ }",
                true,
                "Skipping the duplicate fixed index (i=2, the second -1) avoids re-deriving [-1,-1,2] from scratch, and skipping duplicate left values after a match keeps [-1,0,1] from being recorded twice within the same fixed index either.",
                code = "if (sum < 0) left++ else if (sum > 0) right-- else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right--; while (left < right && nums[left] == nums[left - 1]) left++ }",
            ),
            choice(
                "if (sum < 0) left++ else if (sum > 0) right-- else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right-- }",
                false,
                "Without the inner while-skip after a match, the fixed index at the second -1 (i=2) still gets skipped by the outer check, but a duplicate value adjacent to left or right within the same fixed index could still produce a repeated triplet.",
                code = "if (sum < 0) left++ else if (sum > 0) right-- else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right-- }",
            ),
            choice(
                "if (sum < 0) left++ else if (sum > 0) right-- else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right--; i++ }",
                false,
                "Manually incrementing i from inside the inner while loop conflicts with the outer for loop's own iteration over i, producing unpredictable skipping rather than the intended duplicate-avoidance behavior.",
                code = "if (sum < 0) left++ else if (sum > 0) right-- else { result.add(listOf(nums[i], nums[left], nums[right])); left++; right--; i++ }",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, TIME_COMPLEXITY,
        "A teammate argues that since the duplicate-skipping while loops are nested inside the main two-pointer loop, the true complexity is worse than O(n squared). Walk through why this isn't the case.",
        conceptKey = "two-pointers-nested-loop-amortized-analysis", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "The duplicate-skip loops still only ever move left forward and right backward, the same pointers already bounded by the outer two-pointer sweep - the total movement across all skip-loop iterations is still bounded by n, not an extra multiplicative factor.",
                true,
                "Even though the skip loops are textually nested, they don't add new, independent iterations - they consume the same bounded pointer movement the two-pointer sweep already accounts for, so the overall bound stays O(n squared).",
            ),
            choice(
                "It's correct - nested while loops inside a two-pointer sweep always multiply the complexity to O(n cubed).",
                false,
                "Nesting alone doesn't multiply complexity - what matters is whether the inner loop's total work across all outer iterations is bounded independently, and here it shares the same pointer movement budget as the outer sweep.",
            ),
            choice(
                "It's correct only when the array contains many duplicate values.",
                false,
                "More duplicates mean more skipping happens, but that skipping is still just forward pointer movement bounded by the array's length - it doesn't push the complexity into a higher class regardless of how many duplicates exist.",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, SPACE_COMPLEXITY,
        "A teammate refactors the loop into a recursive function, one call per pointer step, arguing it's \"more elegant.\" What's the actual space cost of this refactor?",
        conceptKey = "two-pointers-recursive-refactor-space-cost", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "It changes the space complexity from O(1) to O(n), since each recursive call adds a stack frame that isn't freed until the whole chain of calls returns.",
                true,
                "The iterative version reuses the same few variables across every step, while a recursive version builds up one stack frame per step - up to n of them before the recursion bottoms out and starts returning.",
            ),
            choice(
                "It has no effect on space complexity, since recursion and iteration always use the same memory.",
                false,
                "Recursion and iteration are not equivalent in space usage - each recursive call that hasn't yet returned holds its own stack frame, unlike a loop's constant, reused set of variables.",
            ),
            choice(
                "It reduces space complexity, since recursion avoids needing explicit loop counters.",
                false,
                "Recursion doesn't eliminate the need to track loop state - it just moves that state into stack frames, one per call, which uses more memory than a loop's reused variables, not less.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Sliding Window
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, PATTERN_RECOGNITION,
        "target = 15, nums = [1,2,3,4,5]. The whole array sums to exactly 15. Trace through why the window still correctly identifies the whole array as the (only) qualifying window here.",
        conceptKey = "sliding-window-whole-array-boundary-case", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "The running sum only reaches 15 once right has advanced through every element, so the window can't shrink until that point - and shrinking even one step would drop the sum below target, so length 5 is correctly recorded as the minimum.",
                true,
                "Since every element is needed just to reach the target sum, the window never has room to shrink while still qualifying - the algorithm correctly recognizes that the full array is both the only and the minimum qualifying window.",
            ),
            choice(
                "The algorithm fails here since minLen never gets updated from its initial sentinel value.",
                false,
                "minLen does get updated - once the running sum reaches exactly target on the last element, the qualifying-window check inside the while loop fires and records the window's true length.",
            ),
            choice(
                "The algorithm returns 0 here, since no subarray shorter than the full array qualifies.",
                false,
                "Returning 0 would mean no qualifying window was ever found, but a window (the full array) does qualify and gets recorded - the correct answer is 5, not 0.",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, PATTERN_RECOGNITION,
        "s = \"dvdf\" - trace through why the answer is 3 (\"vdf\"), not 2, even though 'd' repeats early on.",
        conceptKey = "sliding-window-whole-array-boundary-case", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "When the second 'd' is reached, the window shrinks past the *first* 'd' (not further), leaving \"vd\" - then 'f' extends it to \"vdf\", a valid 3-character window, since the shrink only needs to remove the specific duplicate, not everything before it.",
                true,
                "The window doesn't reset to empty on a duplicate - it shrinks just enough to eliminate that one specific repeated character, which can leave a substantial window intact to keep extending from.",
            ),
            choice(
                "The algorithm is wrong here and should return 2, since 'd' appears twice in the string overall.",
                false,
                "The question asks for the longest substring, a contiguous run, without a repeat - \"vdf\" is a genuinely valid 3-character contiguous run with no internal repeats, even though 'd' does appear elsewhere in the full string.",
            ),
            choice(
                "The window resets completely to just the second 'd' the moment a repeat is found.",
                false,
                "A full reset would throw away the 'v' between the two 'd's, which is actually still usable in a valid window starting right after the first 'd' - only the minimal necessary shrink happens, not a full reset.",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, APPROACH,
        "prices = [2, 1, 2, 0, 1]. Trace how the running-minimum approach still finds the best profit of 1, even though the minimum price (0) appears after a higher price that could tempt a naive check.",
        conceptKey = "sliding-window-running-min-mid-sequence-trace", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "At day index 2 (price 2), the running minimum is still 1 (from day 1), giving a candidate profit of 1; later at index 3 (price 0), the minimum updates to 0, and at index 4 (price 1), that gives a matching profit of 1 - so the best profit found across the whole scan is 1.",
                true,
                "The running minimum only ever reflects prices seen so far, so a profit candidate computed at any day always respects that day-must-come-after-buy-day constraint, correctly finding 1 as the best achievable profit here.",
            ),
            choice(
                "It fails here, since the true minimum price (0) comes after a higher price (2), confusing the running minimum.",
                false,
                "The running minimum simply updates whenever a new lower price appears, in this case going 2 -> 1 -> 1 -> 0 -> 0 across the days - there's no confusion, it just reflects the lowest price seen up to each point.",
            ),
            choice(
                "It returns 0 here, since the array contains a 0 and profits can't be negative.",
                false,
                "The presence of a 0 in the prices doesn't force the answer to be 0 - the algorithm still finds the best profit achievable by buying low and selling on a later, higher day, which is 1 here.",
            ),
        ),
    ),
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, APPROACH,
        "A teammate's window shrinks correctly but recomputes the sum from scratch after every shrink instead of subtracting incrementally, arguing \"it's clearer.\" What's the complexity cost?",
        conceptKey = "sliding-window-incremental-vs-recompute-sum", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "It turns the algorithm from O(n) into O(n squared) in the worst case, since recomputing a window's sum from scratch costs time proportional to the window's size, done potentially once per shrink step.",
                true,
                "Incrementally subtracting one value at a time keeps each shrink step at O(1); recomputing the sum from scratch turns each shrink into its own O(window size) operation, which adds up across the whole run.",
            ),
            choice(
                "It has no effect on complexity, since the total amount of summing work done is the same either way.",
                false,
                "Recomputing repeats work that incremental subtraction avoids entirely - the total work is not the same, since recomputation re-adds values that were already accounted for in the running sum.",
            ),
            choice(
                "It only matters for very short arrays, where the constant-factor overhead dominates.",
                false,
                "The opposite is true - the cost of recomputing grows with the window size and how often shrinking happens, making it matter *more*, not less, as the array grows larger.",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, STATE_SELECTION,
        "A teammate proposes tracking the maximum price seen so far instead of the minimum, then computing profit as maxSoFar - currentPrice at each step. Trace why this doesn't compute the right thing.",
        conceptKey = "sliding-window-wrong-direction-state", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "This computes what you'd lose by selling today after having bought at the historical maximum - a profit calculation needs to buy low and sell high, with the high day coming *after* the low day, which this formula gets backwards.",
                true,
                "Tracking the running maximum and subtracting today's price effectively assumes buying at the peak and selling later at a lower price, which is a loss, not a profit - the roles of minimum and maximum are swapped from what the problem needs.",
            ),
            choice(
                "It's equivalent to the running-minimum approach, just computed from the opposite direction.",
                false,
                "It is not equivalent - buying at a historical maximum and selling later can only ever produce zero or negative results, while buying at a historical minimum and selling later is what actually captures real profit opportunities.",
            ),
            choice(
                "It works correctly as long as prices are generally increasing.",
                false,
                "Even with generally increasing prices, buying at the historical maximum-so-far and selling later at today's price (likely still lower than that maximum) doesn't produce a meaningful profit figure.",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, BOUNDARY_UPDATE,
        "This version correctly shrinks with a while loop but removes from the window set using `s[right]` instead of `s[left]`. Which input exposes the bug clearly?",
        conceptKey = "sliding-window-wrong-index-removed", difficulty = INTERMEDIATE,
        code = "var left = 0\nvar maxLen = 0\nval window = HashSet<Char>()\nfor (right in s.indices) {\n    while (s[right] in window) {\n        window.remove(s[right])\n        left++\n    }\n    window.add(s[right])\n    maxLen = maxOf(maxLen, right - left + 1)\n}",
        choices = listOf(
            choice(
                "\"abba\" - removing s[right] (the duplicate character itself) instead of s[left] never actually shrinks the window from its left edge, so left drifts out of sync with what's truly still in the window.",
                true,
                "The window is supposed to shrink from its left edge until the specific duplicate is gone - removing s[right] instead removes the wrong entry from the set entirely and just increments left without actually removing what left points to.",
            ),
            choice(
                "\"abcdef\" - all unique characters.",
                false,
                "With no duplicates at all, the inner while loop's body never executes, so this input can't reveal a bug in what gets removed during shrinking.",
            ),
            choice(
                "\"aaaa\" - every character identical.",
                false,
                "Here removing s[right] happens to remove the same character value that's also at s[left] (since they're all 'a'), coincidentally masking the bug rather than exposing it.",
            ),
        ),
    ),
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, CODE_BLOCK,
        "target = 7, nums = [2, 3, 1, 2, 4, 3]. Trace which snippet correctly identifies the minimum length 2 (the subarray [4, 3]).",
        conceptKey = "sliding-window-trace-to-correct-answer", difficulty = INTERMEDIATE,
        code = "var left = 0\nvar sum = 0\nvar minLen = Int.MAX_VALUE\nfor (right in nums.indices) {\n    sum += nums[right]\n    // ???\n}\nreturn if (minLen == Int.MAX_VALUE) 0 else minLen",
        choices = listOf(
            choice(
                "while (sum >= target) { minLen = minOf(minLen, right - left + 1); sum -= nums[left]; left++ }",
                true,
                "Tracing through: the window grows to [2,3,1,2] (sum 8, length 4) then shrinks to [3,1,2] (sum 6, stop) - later it grows to include 4 and 3, eventually isolating [4,3] (sum 7, length 2), correctly becoming the new minimum.",
                code = "while (sum >= target) { minLen = minOf(minLen, right - left + 1); sum -= nums[left]; left++ }",
            ),
            choice(
                "if (sum >= target) { minLen = minOf(minLen, right - left + 1) }",
                false,
                "Without ever shrinking the window, sum keeps growing and the window only ever expands - this would record the first qualifying (and increasingly long) window's length rather than searching for the true minimum.",
                code = "if (sum >= target) { minLen = minOf(minLen, right - left + 1) }",
            ),
            choice(
                "while (sum >= target) { sum -= nums[left]; left++; minLen = minOf(minLen, right - left + 1) }",
                false,
                "Recording the length after left has already advanced measures the window one position too small at each check, which would report 1 instead of the true minimum of 2 here.",
                code = "while (sum >= target) { sum -= nums[left]; left++; minLen = minOf(minLen, right - left + 1) }",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, CODE_BLOCK,
        "prices = [7, 1, 5, 3, 6, 4]. Trace which snippet correctly identifies the maximum profit of 5 (buy at 1, sell at 6).",
        conceptKey = "sliding-window-running-min-trace-code", difficulty = INTERMEDIATE,
        code = "var minPrice = Int.MAX_VALUE\nvar maxProfit = 0\nfor (price in prices) {\n    // ???\n}\nreturn maxProfit",
        choices = listOf(
            choice(
                "maxProfit = maxOf(maxProfit, price - minPrice)\nminPrice = minOf(minPrice, price)",
                true,
                "Tracing through: minPrice drops to 1 at the second day; by the time price reaches 6, maxProfit is updated to 6-1=5 before minPrice could ever be overwritten by anything higher, correctly capturing the true best profit.",
                code = "maxProfit = maxOf(maxProfit, price - minPrice)\nminPrice = minOf(minPrice, price)",
            ),
            choice(
                "minPrice = minOf(minPrice, price)\nmaxProfit = maxOf(maxProfit, price - minPrice)",
                false,
                "Updating minPrice before computing the profit means the day price=1 itself gets compared against its own price as the minimum, and more subtly, every day's profit is computed against a minimum that may include that same day - masking the true historical-only minimum.",
                code = "minPrice = minOf(minPrice, price)\nmaxProfit = maxOf(maxProfit, price - minPrice)",
            ),
            choice(
                "maxProfit = maxOf(maxProfit, minPrice - price)\nminPrice = minOf(minPrice, price)",
                false,
                "Computing minPrice - price instead of price - minPrice inverts the profit calculation, producing a positive number exactly when price is below the running minimum, which is backwards from what a profit represents.",
                code = "maxProfit = maxOf(maxProfit, minPrice - price)\nminPrice = minOf(minPrice, price)",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, TIME_COMPLEXITY,
        "A teammate claims that since the algorithm both tracks a minimum and computes a profit at every step, it's actually O(2n), which they insist is \"technically not O(n).\" Evaluate this claim precisely.",
        conceptKey = "sliding-window-constant-multiplier-notation", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "O(2n) and O(n) describe the same growth rate - Big-O notation drops constant multipliers because they don't affect how the algorithm scales as n grows, so O(2n) is properly written and understood as O(n).",
                true,
                "The whole point of Big-O is to describe the *shape* of growth, not the exact operation count - a constant number of operations per element, whether it's one or two, still scales linearly, which is what O(n) captures.",
            ),
            choice(
                "The teammate is right - two operations per element genuinely makes this a different, slower complexity class than a single operation per element.",
                false,
                "Both a one-operation-per-element and a two-operations-per-element algorithm belong to the same O(n) class - complexity classes group by growth shape, not by the exact constant number of operations.",
            ),
            choice(
                "The teammate is right, but only because comparisons cost more than additions.",
                false,
                "The relative cost of different operation types isn't what Big-O notation tracks - it describes how total work grows with input size, treating basic operations as taking roughly constant time each.",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, SPACE_COMPLEXITY,
        "A teammate says the space complexity should be described purely as O(n) since the window could theoretically hold the entire string. Is this the tightest correct bound?",
        conceptKey = "sliding-window-charset-bound-tightness", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "No - the window can never contain a repeat, so its size is capped by the number of distinct characters possible, not by the string's length; O(min(n, charset size)) is the tighter, more accurate bound.",
                true,
                "Even for a very long string, the window's own invariant (no repeats) bounds it by how many distinct characters exist - describing it as simply O(n) overstates the space needed once the string is longer than the character set.",
            ),
            choice(
                "Yes - O(n) is always the correct bound for any structure that processes a string of length n.",
                false,
                "A structure's size is bounded by what it's actually allowed to contain, not by the length of the input being processed - here, the no-repeat invariant caps it well below n for long strings.",
            ),
            choice(
                "No - the tighter bound is O(1), since the character set is fixed regardless of string length.",
                false,
                "The character set being fixed does bound the space, but calling it O(1) ignores that the bound is that fixed alphabet size (e.g. 26 or 128), which is a meaningful, non-trivial constant, more precisely written as O(charset size).",
            ),
        ),
    ),

    // ------------------------------------------------------------ Stack
    step(
        "valid-parentheses", STACK, PATTERN_RECOGNITION,
        "s = \"(])\" - not valid, but a naive count of opens vs. closes (1 open, 2 closes... actually 2 opens if miscounted) can mislead. Walk through exactly why counting brackets by type alone isn't enough here.",
        conceptKey = "stack-count-vs-order-sensitivity", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Counting only tracks how many of each bracket type exist, but \"(])\" has correctly balanced counts of each individual type in some pairings - it's the *order and nesting*, not the counts, that make it invalid, which only a stack captures.",
                true,
                "A stack doesn't just tally brackets, it tracks *which* bracket is waiting to be closed next - that ordering information is exactly what distinguishes \"(])\" (invalid nesting) from a genuinely valid string with the same bracket counts.",
            ),
            choice(
                "Because \"(])\" has an odd total number of characters.",
                false,
                "\"(])\" has three characters, which is indeed odd, but that's incidental - a string could have an even count and still be invalid due to nesting order, which is the actual issue here.",
            ),
            choice(
                "Because square brackets are inherently invalid inside parentheses.",
                false,
                "Different bracket types can validly nest inside each other, like \"([])\" - the issue with \"(])\" is specifically that the closing order doesn't match the opening order, not that mixing bracket types is disallowed.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, PATTERN_RECOGNITION,
        "temperatures = [73, 74, 75, 71, 69, 72, 76, 73]. Trace through why day index 3 (71) resolves against day index 6 (76), skipping past days 4 and 5 in the answer computation.",
        conceptKey = "stack-count-vs-order-sensitivity", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "By the time 76 is reached, days 4 (69) and 5 (72) have already been resolved against day 5's 72 and popped off the stack - only day 3's 71 (and day 6's own 76 push) remain relevant, so 71's answer is computed directly against 76's position.",
                true,
                "Each day is only ever compared against days that are *still waiting* on the stack when a warmer temperature arrives - days already resolved by an earlier warmer day are gone from consideration by the time 76 shows up.",
            ),
            choice(
                "Because 71, 69, and 72 are all skipped over as irrelevant to the final answer array.",
                false,
                "None of those days are skipped - day 4 (69) resolves against day 5 (72) with an answer of 1, and day 5 itself later resolves against day 6 (76) with its own answer - every day gets an answer, just via different comparisons.",
            ),
            choice(
                "Because the stack only ever holds the three most recent days at any time.",
                false,
                "The stack can grow to hold many more than three days, depending on the temperature pattern - its size isn't fixed at three, it grows and shrinks based on which days are still unresolved.",
            ),
        ),
    ),
    step(
        "min-stack", STACK, APPROACH,
        "A teammate proposes only pushing onto the min-stack when a *new* minimum is set, storing a count of consecutive pushes at that minimum to know when to pop it. Is this a sound alternative to pushing every time?",
        conceptKey = "stack-conditional-push-with-count-approach", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "It can work, but it's meaningfully more complex to get right - the count has to track exactly how many *main-stack* pushes happened while that minimum was current, which push-every-time avoids needing entirely.",
                true,
                "Push-every-time keeps the two stacks trivially in sync by size alone; a count-based scheme has to carefully reconstruct that same synchronization, which is extra bookkeeping for the same eventual correctness.",
            ),
            choice(
                "It's incorrect in principle - a min-stack can never track anything besides a 1:1 pushed value per entry.",
                false,
                "There's no fundamental rule against a smarter, more compact min-stack representation - it's a legitimate design space, just a more error-prone one than always pushing.",
            ),
            choice(
                "It's strictly better, since it always uses less memory than pushing every time.",
                false,
                "It can save memory in some cases (long runs at the same minimum), but it isn't strictly better in the worst case (a strictly decreasing sequence still needs one entry per push) and it adds real complexity risk.",
            ),
        ),
    ),
    step(
        "valid-parentheses", STACK, APPROACH,
        "s = \"()[]{}\"  followed by a stress-tested version with 10,000 alternating bracket pairs. Which property of the stack approach specifically guarantees it stays correct and fast at that scale?",
        conceptKey = "stack-scales-linearly-with-input", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Each character triggers exactly one constant-time push or pop, so the total work and the stack's own size both scale directly and predictably with the string's length, with no hidden per-character cost that grows.",
                true,
                "Nothing about the stack-based check does more work as the string gets longer beyond the same fixed per-character operations - that's exactly what keeps it both correct and O(n) even at large scale.",
            ),
            choice(
                "The specific alternating pattern of the brackets makes this case unusually easy for the stack.",
                false,
                "The stack's correctness and performance don't depend on any particular pattern in the bracket sequence - the same guarantees hold for any valid or invalid arrangement, alternating or not.",
            ),
            choice(
                "Because 10,000 is still small enough that any approach, including brute-force removal, would perform similarly.",
                false,
                "At that scale, a brute-force repeated-removal approach would already show real slowdown compared to the stack's single linear pass - the difference in approach becomes very much noticeable well before 10,000 characters.",
            ),
        ),
    ),
    step(
        "min-stack", STACK, STATE_SELECTION,
        "A teammate wants to eliminate the min-stack entirely and instead re-scan the main stack for the minimum only when getMin is called, arguing getMin is rarely called. Trace the actual cost tradeoff.",
        conceptKey = "stack-lazy-scan-vs-maintained-state", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "This trades O(1) getMin (with O(n) extra space) for O(n) getMin (with O(1) extra space) - if getMin truly is rare relative to push/pop, this can be a reasonable tradeoff, but it changes getMin's own complexity class, not just its constant factor.",
                true,
                "Removing the min-stack does save the O(n) space it uses, but it fundamentally changes what getMin costs - from a guaranteed O(1) read to a full O(n) scan every time it's called, which matters if getMin is called often even if push/pop dominate in count.",
            ),
            choice(
                "It has no real cost, since getMin isn't part of the stack's core push/pop operations.",
                false,
                "getMin is one of the four operations this data structure is specifically required to support in O(1) - changing its cost to O(n) is a real, meaningful tradeoff, not something that can be dismissed as out of scope.",
            ),
            choice(
                "It's strictly better, since it saves space without affecting time complexity at all.",
                false,
                "It does save space, but it does affect time complexity - getMin degrades from O(1) to O(n), which is a real cost, not something achieved for free.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, BOUNDARY_UPDATE,
        "This version compares `temperatures[i] >= temperatures[stack.last()]` instead of strictly `>`. Which input produces a subtly wrong answer?",
        conceptKey = "stack-strict-vs-inclusive-comparison", difficulty = INTERMEDIATE,
        code = "val answer = IntArray(temperatures.size)\nval stack = ArrayDeque<Int>()\nfor (i in temperatures.indices) {\n    while (stack.isNotEmpty() && temperatures[i] >= temperatures[stack.last()]) {\n        val prevDay = stack.removeLast()\n        answer[prevDay] = i - prevDay\n    }\n    stack.addLast(i)\n}",
        choices = listOf(
            choice(
                "[70, 70, 75] - the first 70 gets resolved against the second 70 (an *equal*, not warmer, temperature), incorrectly reporting a 1-day wait instead of correctly waiting for the actual warmer day at index 2.",
                true,
                "The problem specifically asks for a *warmer* day, not an equal or warmer one - using >= incorrectly resolves a day against an equally-cool day instead of waiting for a genuinely higher temperature.",
            ),
            choice(
                "[70, 72, 75], strictly increasing.",
                false,
                "With every day strictly warmer than the last, the >= comparison behaves identically to a strict > comparison here, since no two adjacent values are ever equal - this case doesn't expose the bug.",
            ),
            choice(
                "[80], a single day.",
                false,
                "With only one day and nothing to compare it against, the while loop's condition is never even evaluated - this doesn't exercise the strict-versus-inclusive distinction at all.",
            ),
        ),
    ),
    step(
        "valid-parentheses", STACK, CODE_BLOCK,
        "A teammate wants to extend the checker to also support angle brackets `<>` alongside the existing three types. Which snippet correctly generalizes without hardcoding a fixed set of pairs inline?",
        conceptKey = "stack-generalized-pair-mapping-code", difficulty = INTERMEDIATE,
        code = "val pairs = mapOf(')' to '(', ']' to '[', '}' to '{', '>' to '<')\nval opens = pairs.values.toSet()\nval stack = ArrayDeque<Char>()\nfor (c in s) {\n    // ???\n}\nreturn stack.isEmpty()",
        choices = listOf(
            choice(
                "if (c in opens) stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
                true,
                "Checking membership in opens (derived from the map's own values) rather than hardcoding a fixed string like \"([{\" means adding a new bracket pair only requires updating the pairs map, nothing else in the logic.",
                code = "if (c in opens) stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
            ),
            choice(
                "if (c in \"([{\") stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
                false,
                "Hardcoding \"([{\" misses the newly added '<' character entirely - it would never get pushed onto the stack, so a matching '>' would incorrectly fail to find it.",
                code = "if (c in \"([{\") stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
            ),
            choice(
                "if (c in pairs) stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
                false,
                "Checking membership in pairs (the map's keys, which are the *closing* brackets) instead of its values would push closing brackets onto the stack and try to pop on opening brackets, inverting the intended logic.",
                code = "if (c in pairs) stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }",
            ),
        ),
    ),
    step(
        "min-stack", STACK, CODE_BLOCK,
        "Trace which snippet correctly returns 1 after this exact sequence: push(3), push(1), push(2), pop(), getMin().",
        conceptKey = "stack-push-min-tracking-trace-code", difficulty = INTERMEDIATE,
        code = "val stack = ArrayDeque<Int>()\nval minStack = ArrayDeque<Int>()\nfun push(v: Int) {\n    stack.addLast(v)\n    // ???\n}",
        choices = listOf(
            choice(
                "minStack.addLast(minOf(v, if (minStack.isEmpty()) v else minStack.last()))",
                true,
                "Tracing through: push(3) -> minStack=[3], push(1) -> minStack=[3,1], push(2) -> minStack=[3,1,1] (2 isn't smaller than 1, so 1 repeats); after pop(), minStack=[3,1], so getMin() correctly returns 1.",
                code = "minStack.addLast(minOf(v, if (minStack.isEmpty()) v else minStack.last()))",
            ),
            choice(
                "if (minStack.isEmpty() || v < minStack.last()) minStack.addLast(v)",
                false,
                "Only conditionally pushing onto minStack desynchronizes its size from the main stack - after pop(), it's unclear how many entries to remove from minStack to correctly undo the push(2) that never added anything to it.",
                code = "if (minStack.isEmpty() || v < minStack.last()) minStack.addLast(v)",
            ),
            choice(
                "minStack.addLast(v)",
                false,
                "Pushing the raw value instead of the running minimum onto minStack means its top after this sequence would be 2 (the most recent push), not 1 (the true minimum of what remains after the pop).",
                code = "minStack.addLast(v)",
            ),
        ),
    ),
    step(
        "min-stack", STACK, TIME_COMPLEXITY,
        "A teammate claims that since two stacks are maintained instead of one, every operation must be O(2), which they argue is different from O(1). Address this precisely.",
        conceptKey = "stack-dual-structure-still-constant-time", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Operating on two stacks per call is still a fixed, input-size-independent amount of work - O(2) and O(1) describe the same complexity class, since Big-O discards constant factors regardless of how many fixed-size structures are touched.",
                true,
                "Whether one stack or two fixed-size structures are touched per operation, neither grows with n - the defining feature of O(1) is that the work doesn't scale with input size, which holds true here regardless of the constant multiplier.",
            ),
            choice(
                "The teammate is right - maintaining two stacks makes every operation twice as expensive in a way that changes the complexity class.",
                false,
                "Doubling a *constant* amount of work per operation doesn't change its complexity class - O(2) collapses to O(1) precisely because Big-O notation is about scaling behavior, not exact operation counts.",
            ),
            choice(
                "The teammate is right, but only for pop, since it touches both stacks; push and getMin remain O(1).",
                false,
                "All three operations (push, pop, and getMin) touch a fixed, small number of structures regardless of stack size - none of them scale with n, so all three are genuinely O(1), pop included.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, SPACE_COMPLEXITY,
        "A teammate argues the stack's worst-case space usage should really be described as depending on the *number of local temperature peaks*, not just \"O(n) worst case.\" Evaluate this framing.",
        conceptKey = "stack-worst-case-characterization", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "That framing describes a more refined, input-dependent behavior, but the standard worst-case bound still needs to account for the single input pattern (strictly decreasing temperatures) that maximizes stack size, which is O(n) regardless of how peaks are counted.",
                true,
                "A more nuanced, pattern-dependent analysis can be interesting, but complexity analysis is ultimately about the bound across *all* possible inputs - and strictly decreasing temperatures, with no peaks resolving anything, is exactly the input that reaches the full O(n) bound.",
            ),
            choice(
                "It's a more accurate replacement for O(n) and should be used instead.",
                false,
                "Peak-counting could describe average or typical behavior for certain input distributions, but it doesn't replace the need for a true worst-case bound, which strictly decreasing temperatures still reach at O(n).",
            ),
            choice(
                "It's irrelevant, since the stack's size never actually depends on the input pattern at all.",
                false,
                "The stack's size very much depends on the temperature pattern - it's specifically the worst-case pattern (strictly decreasing, with nothing ever resolving) that pushes it up to O(n).",
            ),
        ),
    ),

    // ------------------------------------------------------------ Binary Search
    step(
        "binary-search", BINARY_SEARCH, PATTERN_RECOGNITION,
        "nums = [-1, 0, 3, 5, 9, 12], target = 2. Trace through the search to confirm the algorithm correctly reports -1 for a target that falls in a genuine gap between two present values.",
        conceptKey = "binary-search-target-not-present-trace", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "mid lands on 3, then 0, then 3 is retried in a narrowed way—more precisely, low and high converge around index 1-2 without ever landing on 2, since it isn't in the array, and the loop exits with low > high, correctly returning -1.",
                true,
                "The search doesn't need to \"know\" 2 is absent in advance - it simply keeps narrowing the range based on comparisons, and once low exceeds high, that itself is the signal that every possible position has been ruled out.",
            ),
            choice(
                "The algorithm incorrectly returns the index of 3, the closest value, instead of -1.",
                false,
                "Binary search as specified returns -1 for any target it doesn't find an exact match for - it doesn't fall back to returning the index of the nearest value.",
            ),
            choice(
                "The algorithm enters an infinite loop since 2 is never found.",
                false,
                "As long as low and high are updated to move strictly past mid on every non-match, the range keeps shrinking and the loop terminates in a bounded number of steps, found or not.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, PATTERN_RECOGNITION,
        "nums = [3, 4, 5, 1, 2]. Trace through the first two iterations of the min-finding search to confirm it correctly narrows toward index 3 (value 1).",
        conceptKey = "binary-search-target-not-present-trace", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "mid starts at index 2 (value 5); since 5 > nums[high]=2, the rotation point is to the right, so low moves to 3; next mid is index 3 or 4 depending on rounding, and the comparison against the new high continues narrowing until low==high==3.",
                true,
                "Each comparison of nums[mid] against nums[high] correctly identifies which side still contains the rotation point, so the range keeps shrinking toward index 3 without ever needing to check every element directly.",
            ),
            choice(
                "mid starts at index 0 (value 3), and since it's the first element, the search immediately returns it as the minimum.",
                false,
                "Binary search starts by computing mid from the low and high bounds, not by assuming index 0 is special - here mid begins at roughly the middle of the range, not at the very start.",
            ),
            choice(
                "The search fails to converge since the array isn't fully sorted.",
                false,
                "The comparison-based approach specifically handles the two-sorted-runs structure of a rotated array - it doesn't require full sortedness to converge correctly.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, APPROACH,
        "piles = [30, 11, 23, 4, 20], h = 6. The true answer is speed 23. Trace through why the binary search doesn't get stuck considering only feasible-but-too-fast speeds.",
        conceptKey = "binary-search-converge-to-minimum-feasible-trace", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Whenever a candidate speed is feasible, the search keeps it as a possible answer and narrows toward slower speeds (high = mid); it only moves toward faster speeds when the current candidate fails - this asymmetry is exactly what converges on the *minimum* feasible speed rather than just any feasible one.",
                true,
                "The search doesn't just find *a* speed that works - by always preferring to explore slower once a candidate succeeds, it specifically converges on the boundary between infeasible and feasible, which is the minimum working speed.",
            ),
            choice(
                "It works because the piles happen to be given in a favorable order.",
                false,
                "The order the piles are listed in doesn't affect the search - hoursNeeded sums over all piles regardless of their order, so the algorithm's convergence doesn't depend on pile ordering.",
            ),
            choice(
                "It works only because h=6 happens to be a small number.",
                false,
                "The convergence behavior doesn't depend on h being small or large - the same feasible-then-search-slower logic applies and correctly converges regardless of the specific value of h.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, APPROACH,
        "A teammate's version compares nums[mid] to nums[low] instead of nums[high], and it happens to pass several test cases. On which category of rotated array does it actually fail?",
        conceptKey = "binary-search-wrong-comparison-when-fails", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Arrays where the rotation point falls such that the left half (relative to mid) still looks internally increasing regardless of whether the minimum is actually in that half - comparing to nums[low] can't distinguish those two situations the way comparing to nums[high] reliably can.",
                true,
                "The right-edge comparison specifically distinguishes which half currently contains the rotation point in every case; the left-edge comparison can be ambiguous precisely when the left half's own internal order doesn't reveal where the true minimum sits.",
            ),
            choice(
                "It fails only on arrays that were never actually rotated at all.",
                false,
                "An unrotated array is actually one of the easier cases for either comparison to handle correctly - the failure specifically shows up on certain rotated configurations, not the trivial unrotated one.",
            ),
            choice(
                "It fails only on arrays with exactly two elements.",
                false,
                "The comparison's unreliability isn't tied to array size - it's about the specific relationship between the values at low, mid, and high for a given rotation, which can occur at any size beyond the trivial single-element case.",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, STATE_SELECTION,
        "A teammate wants to add a `foundIndex` variable that's updated speculatively before the final comparison confirms a match, to \"save a step.\" Trace why this risks an incorrect early return.",
        conceptKey = "binary-search-speculative-state-risk", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "Setting foundIndex before confirming nums[mid] actually equals target means a subsequent comparison could show it was too small or too large, but the speculative value would already suggest a match was found, risking an incorrect return if not carefully re-checked.",
                true,
                "The whole point of comparing before acting is that mid is only a *candidate* until the comparison confirms it - assigning meaning to it prematurely (as if it were already the answer) invites a bug where a non-match gets treated as one.",
            ),
            choice(
                "It has no risk, since foundIndex would simply be overwritten by later iterations if wrong.",
                false,
                "If the loop returns using the speculative value before ever correcting it, being overwritten later doesn't help - the incorrect return has already happened by that point.",
            ),
            choice(
                "It only matters when the target is the very last element checked.",
                false,
                "The risk exists at any point where a speculative assignment happens before a proper comparison confirms it - it isn't limited to just the final element checked.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, BOUNDARY_UPDATE,
        "This version sets `high = piles.max() - 1` instead of `piles.max()`. Which input reveals this is wrong?",
        conceptKey = "binary-search-upper-bound-off-by-one", difficulty = INTERMEDIATE,
        code = "var low = 1\nvar high = piles.max() - 1\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    if (hoursNeeded(mid) <= h) high = mid else low = mid + 1\n}",
        choices = listOf(
            choice(
                "piles = [10], h = 1, where the only feasible speed is exactly piles.max()=10, but the search space now excludes that value entirely, making the true answer unreachable.",
                true,
                "The largest single pile size is always a genuinely feasible speed (it finishes that pile in exactly one hour), and excluding it from the very start of the range means the search can never converge on it when it's actually needed.",
            ),
            choice(
                "piles = [3, 6, 7, 11], h = 8, where the answer, speed 4, is comfortably below piles.max().",
                false,
                "When the true answer sits well below the maximum pile size, trimming one value off the top of an already-generous range doesn't remove the answer from consideration - this case doesn't expose the bug.",
            ),
            choice(
                "piles = [1, 1, 1], h = 3, uniformly small piles.",
                false,
                "With every pile already tiny and h generous, the true answer is far below the upper bound either way - trimming the top of the range by one doesn't affect whether the answer is reachable here.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, CODE_BLOCK,
        "Trace which snippet correctly narrows toward the minimum for nums = [11, 13, 15, 17] (an array that was rotated its full length, leaving it unrotated).",
        conceptKey = "binary-search-full-rotation-trace-code", difficulty = INTERMEDIATE,
        code = "var low = 0\nvar high = nums.size - 1\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    // ???\n}\nreturn nums[low]",
        choices = listOf(
            choice(
                "if (nums[mid] > nums[high]) low = mid + 1 else high = mid",
                true,
                "Since the array is effectively sorted, nums[mid] is never greater than nums[high] at any step, so high keeps narrowing down to index 0 every time - correctly converging on 11 without needing any special unrotated-case handling.",
                code = "if (nums[mid] > nums[high]) low = mid + 1 else high = mid",
            ),
            choice(
                "if (nums[low] < nums[high]) return nums[low]\nif (nums[mid] > nums[high]) low = mid + 1 else high = mid",
                false,
                "Adding an early-return special case for the already-sorted-looking scenario is unnecessary - the core comparison already converges correctly on its own without it, and the special case adds complexity for no benefit.",
                code = "if (nums[low] < nums[high]) return nums[low]\nif (nums[mid] > nums[high]) low = mid + 1 else high = mid",
            ),
            choice(
                "if (nums[mid] < nums[high]) low = mid + 1 else high = mid",
                false,
                "This flips the comparison direction entirely - it would move low forward when the middle is smaller, which is exactly backward from what's needed to narrow toward the true minimum.",
                code = "if (nums[mid] < nums[high]) low = mid + 1 else high = mid",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, CODE_BLOCK,
        "nums = [1, 3, 5, 7, 9], target = 1 (the very first element). Trace which snippet correctly finds it without an off-by-one error on the initial bounds.",
        conceptKey = "binary-search-first-element-boundary-trace", difficulty = INTERMEDIATE,
        code = "var low = 0\nvar high = nums.size - 1\nwhile (low <= high) {\n    val mid = low + (high - low) / 2\n    // ???\n}\nreturn -1",
        choices = listOf(
            choice(
                "when { nums[mid] < target -> low = mid + 1; nums[mid] > target -> high = mid - 1; else -> return mid }",
                true,
                "Tracing through: low=0, high=4, mid=2 (value 5, too big, high=1); mid=0 (value 1, exact match) - the inclusive low <= high condition correctly allows the search to still check index 0 even after high has shrunk down to it.",
                code = "when { nums[mid] < target -> low = mid + 1; nums[mid] > target -> high = mid - 1; else -> return mid }",
            ),
            choice(
                "when { nums[mid] < target -> low = mid + 1; nums[mid] > target -> high = mid - 1; else -> return mid }  // with while (low < high) instead of low <= high",
                false,
                "With a strict less-than loop condition, once low and high both equal 0, the loop exits before ever comparing nums[0] to target, incorrectly missing a target that happens to be the very first element.",
                code = "// while (low < high) instead of low <= high\nwhen { nums[mid] < target -> low = mid + 1; nums[mid] > target -> high = mid - 1; else -> return mid }",
            ),
            choice(
                "when { nums[mid] <= target -> low = mid + 1; nums[mid] > target -> high = mid - 1 }",
                false,
                "Removing the exact-match branch and folding it into the 'too small' case means the loop never explicitly returns mid on a match - it would keep narrowing right past the target instead of stopping there.",
                code = "when { nums[mid] <= target -> low = mid + 1; nums[mid] > target -> high = mid - 1 }",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, TIME_COMPLEXITY,
        "A teammate argues that since low + (high - low) / 2 involves more arithmetic than (low + high) / 2, the safer version must be asymptotically slower. Address this.",
        conceptKey = "binary-search-safe-arithmetic-same-complexity", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "No - both compute mid with a small, fixed number of arithmetic operations per iteration, and the number of iterations (which is what actually drives O(log n)) is identical either way; the overflow-safe version just does a couple of extra constant-time operations, not more iterations.",
                true,
                "Adding one or two extra arithmetic operations per iteration doesn't change how many iterations the search takes overall - complexity is about how the number of steps scales with n, not the exact operation count within each step.",
            ),
            choice(
                "Yes - extra arithmetic operations always push an algorithm to the next complexity class.",
                false,
                "A fixed number of extra constant-time operations per iteration doesn't change the complexity class - what matters is whether the number of iterations itself scales differently, which it doesn't here.",
            ),
            choice(
                "Yes, but only for very large arrays where the overflow-safe arithmetic matters most.",
                false,
                "The overflow-safe formula does the same small number of extra operations regardless of array size - larger arrays don't make this particular arithmetic difference any more significant relative to the number of iterations.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, SPACE_COMPLEXITY,
        "A teammate suggests memoizing hoursNeeded results in a map, arguing repeated candidate speeds during the search could otherwise be recomputed. Is this optimization worth its space cost here?",
        conceptKey = "binary-search-memoization-tradeoff-analysis", difficulty = INTERMEDIATE,
        choices = listOf(
            choice(
                "No - binary search never re-evaluates the same candidate speed twice (each iteration strictly narrows the range to new values), so a memoization map would add O(log m) space for a lookup that would never actually hit a cached result.",
                true,
                "Binary search's own structure guarantees each mid value is only ever computed once across the whole search - there's no repeated work to cache in the first place, so memoization here adds space without any corresponding benefit.",
            ),
            choice(
                "Yes - it correctly reduces the time complexity from O(n log m) to O(n) by avoiding repeated calls.",
                false,
                "Since no candidate speed is ever actually recomputed in a standard binary search, there's no repeated work to eliminate - the time complexity wouldn't improve, and the memoization map would just be pure overhead.",
            ),
            choice(
                "Yes, but only because hoursNeeded itself is an expensive O(n) operation each call.",
                false,
                "Even though each individual call is O(n), memoization only pays off when the *same* input would otherwise be computed more than once - and binary search never revisits the same mid value, so there's nothing to cache productively.",
            ),
        ),
    ),
)
