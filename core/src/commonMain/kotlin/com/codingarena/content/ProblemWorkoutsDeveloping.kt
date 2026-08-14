package com.codingarena.content

import com.codingarena.domain.model.PatternGroup.ARRAYS_HASHING
import com.codingarena.domain.model.PatternGroup.BINARY_SEARCH
import com.codingarena.domain.model.PatternGroup.SLIDING_WINDOW
import com.codingarena.domain.model.PatternGroup.STACK
import com.codingarena.domain.model.PatternGroup.TWO_POINTERS
import com.codingarena.domain.model.PracticeDifficulty.DEVELOPING
import com.codingarena.domain.model.WorkoutChoice
import com.codingarena.domain.model.WorkoutStep
import com.codingarena.domain.model.WorkoutStepKind.APPROACH
import com.codingarena.domain.model.WorkoutStepKind.BOUNDARY_UPDATE
import com.codingarena.domain.model.WorkoutStepKind.CODE_BLOCK
import com.codingarena.domain.model.WorkoutStepKind.PATTERN_RECOGNITION
import com.codingarena.domain.model.WorkoutStepKind.SPACE_COMPLEXITY
import com.codingarena.domain.model.WorkoutStepKind.STATE_SELECTION
import com.codingarena.domain.model.WorkoutStepKind.TIME_COMPLEXITY

/**
 * Developing tier: plausible competing approaches, boundary-condition
 * mistakes, short code blocks with a realistic bug. Same 15 problems as
 * [ProblemWorkouts]'s Beginner tier - what changes is what each question
 * tests, not the underlying problem set.
 *
 * Ten steps per topic, matching the round quota exactly: 2 PATTERN_RECOGNITION,
 * 2 APPROACH, 1 STATE_SELECTION, 1 BOUNDARY_UPDATE, 2 CODE_BLOCK, 1
 * TIME_COMPLEXITY, 1 SPACE_COMPLEXITY. The doubled kinds deliberately reuse the
 * same [WorkoutStep.conceptKey] across two different problems - a genuine
 * same-tier variation for review purposes, not just a same-tier duplicate.
 */
internal val developingWorkoutSteps: List<WorkoutStep> = listOf(
    // ------------------------------------------------------------ Arrays & Hashing
    step(
        "two-sum", ARRAYS_HASHING, PATTERN_RECOGNITION,
        "A teammate suggests sorting the array first, then using two pointers, to find the pair that sums to target. Why might a hash map still be the better call here?",
        conceptKey = "arrays-hashing-lookup-vs-sort-tradeoff", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Sorting loses the original indices, and the problem asks for index positions, not just the values.",
                true,
                "A hash map preserves the value-to-index mapping throughout, while sorting would require carrying original indices alongside the values just to recover what the problem actually asks for.",
            ),
            choice(
                "Two pointers on a sorted array is always slower than a hash map, regardless of what the problem asks for.",
                false,
                "Both approaches are linear-ish (O(n) hash map vs O(n log n) sort-then-scan) - the deciding factor here is specifically the lost index information, not raw speed in general.",
            ),
            choice(
                "Two pointers cannot be used unless the array is already sorted.",
                false,
                "That's true but incomplete - the array could be sorted first specifically to enable two pointers; the real issue is that doing so destroys the index information the answer requires.",
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, PATTERN_RECOGNITION,
        "A teammate suggests sorting the array and scanning for adjacent equal values to detect a duplicate. Why might a hash set still be the better call here?",
        conceptKey = "arrays-hashing-lookup-vs-sort-tradeoff", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Sorting costs O(n log n), strictly more than the O(n) a single pass with a hash set achieves for this specific question.",
                true,
                "Since the question only asks whether a duplicate exists, not where or in what order, a hash set answers it in one linear pass - sorting does extra work that isn't needed here.",
            ),
            choice(
                "Sorting cannot detect duplicates at all, only a hash set can.",
                false,
                "Sorting absolutely can detect duplicates by placing equal values adjacent to each other - it's a valid approach, just not the fastest one for this particular question.",
            ),
            choice(
                "A hash set uses less memory than sorting the array in place.",
                false,
                "In-place sorting can use O(1) extra space, while a hash set costs O(n) - memory isn't the advantage here, time complexity is.",
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, APPROACH,
        "A teammate proposes checking anagrams by comparing the sorted forms of both strings instead of counting characters. Is this a reasonable alternative?",
        conceptKey = "arrays-hashing-sort-vs-count-approach", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Yes - it's correct, just O(n log n) instead of O(n), since two strings are anagrams exactly when their sorted forms are identical.",
                true,
                "Sorting both strings and comparing them for equality is a completely valid anagram check - it's simply not the fastest one, trading some speed for a very simple implementation.",
            ),
            choice(
                "No - sorting can produce different results depending on the language's sort stability.",
                false,
                "Sort stability affects the relative order of equal elements, not which elements end up adjacent - two anagrams will always produce identical sorted output regardless of stability.",
            ),
            choice(
                "No - sorting only works if both strings are already the same length.",
                false,
                "Checking lengths first is good practice, but sorting-and-comparing would still correctly reject differently-sized strings anyway, since their sorted forms couldn't match.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, APPROACH,
        "A teammate proposes checking every pair with two nested loops, arguing the array is always small in practice. When does this reasoning actually hold up?",
        conceptKey = "arrays-hashing-brute-force-tradeoff", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Only when n is small enough that O(n squared) genuinely doesn't matter - it degrades badly as the array grows, unlike the hash map approach.",
                true,
                "Brute force isn't wrong, it's just fragile - it works fine on a small, fixed-size input but the hash map approach stays fast regardless of how large the array grows.",
            ),
                choice(
                "Never - nested loops are always incorrect for this problem.",
                false,
                "Nested loops correctly find the answer, they're just slower - correctness and efficiency are separate questions, and this approach is correct, just not scalable.",
            ),
            choice(
                "Only when the array contains negative numbers.",
                false,
                "Whether values are negative has no bearing on nested loops' correctness or performance - the concern is purely about how the work scales with array size.",
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, STATE_SELECTION,
        "A teammate wants to track duplicates using a list instead of a set, checking `value in list` before adding. What's the practical cost of that choice?",
        conceptKey = "arrays-hashing-list-vs-set-membership", difficulty = DEVELOPING,
        code = "val seen = mutableListOf<Int>()\nfor (num in nums) {\n    if (num in seen) return true\n    seen.add(num)\n}\nreturn false",
        choices = listOf(
            choice(
                "It's still correct, but `num in seen` on a list scans it linearly, turning the whole algorithm into O(n squared) instead of O(n).",
                true,
                "A list's membership check has to look through every element one at a time, while a set's is close to constant time - the algorithm's shape stays the same, but the cost of each check changes dramatically.",
            ),
            choice(
                "It's incorrect - lists cannot store duplicate-checking state at all.",
                false,
                "A list can absolutely hold the values seen so far and be checked for membership - it produces the right answer, it's just slower than a set at doing it.",
            ),
            choice(
                "It's equivalent to using a set, since both check membership before adding.",
                false,
                "The check-then-add sequence is the same, but a list's membership check costs O(n) per call versus a set's near-O(1), which changes the total running time substantially.",
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, BOUNDARY_UPDATE,
        "This version decrements the count map using `t`'s characters but never checks the string lengths first. Which input slips through incorrectly?",
        conceptKey = "arrays-hashing-length-check-boundary", difficulty = DEVELOPING,
        code = "val counts = IntArray(26)\nfor (c in s) counts[c - 'a']++\nfor (c in t) counts[c - 'a']--\nreturn counts.all { it == 0 }",
        choices = listOf(
            choice(
                "s = \"ab\", t = \"aabb\" - t has extra characters that cancel out other counts, producing an all-zero array despite unequal lengths.",
                true,
                "Without a length check, t could add and subtract extra matching pairs of characters that happen to net back to zero, wrongly passing as an anagram of a shorter s.",
            ),
            choice(
                "s = \"ab\", t = \"ba\" - same characters, different order.",
                false,
                "This is exactly the case the count map is designed to handle correctly - order doesn't matter for anagrams, and the counts would net to zero as expected.",
            ),
            choice(
                "s = \"\", t = \"\" - both empty strings.",
                false,
                "Two empty strings trivially net every count to zero and are correctly anagrams of each other - this doesn't expose the missing length check.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, CODE_BLOCK,
        "Which snippet correctly finds the pair while also handling the case where the same value appears more than once in a way that could accidentally match itself?",
        conceptKey = "arrays-hashing-self-match-guard", difficulty = DEVELOPING,
        code = "val seen = HashMap<Int, Int>()\nfor (i in nums.indices) {\n    val complement = target - nums[i]\n    // ???\n}",
        choices = listOf(
            choice(
                "if (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
                true,
                "Checking for the complement before inserting the current index means an index can never be paired with itself, even when the target is exactly double the current value.",
                code = "if (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
            ),
            choice(
                "seen[nums[i]] = i\nif (seen.containsKey(complement) && seen[complement] != i) return intArrayOf(seen[complement]!!, i)",
                false,
                "Inserting before checking, then trying to filter out self-matches with an index comparison, is fragile - it happens to work here but adds an unnecessary special case that the check-before-insert order avoids entirely.",
                code = "seen[nums[i]] = i\nif (seen.containsKey(complement) && seen[complement] != i) return intArrayOf(seen[complement]!!, i)",
            ),
            choice(
                "if (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nif (!seen.containsKey(nums[i])) seen[nums[i]] = i",
                false,
                "Only inserting a value the first time it's seen can cause a later, valid complement to be missed if an earlier duplicate's index was the one actually needed.",
                code = "if (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nif (!seen.containsKey(nums[i])) seen[nums[i]] = i",
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, CODE_BLOCK,
        "A teammate rewrites the check using `nums.toSet().size == nums.size`. Is this equivalent to the early-exit set-scan version?",
        conceptKey = "arrays-hashing-eager-vs-lazy-set-build", difficulty = DEVELOPING,
        code = "return nums.toSet().size == nums.size",
        choices = listOf(
            choice(
                "Correct, but it always builds the full set first - the early-exit version can stop the instant a duplicate is found instead of processing the rest of the array.",
                true,
                "Both reach the same true-or-false answer, but comparing sizes requires building the entire set before comparing, while the check-then-add loop can return the moment a duplicate appears.",
            ),
            choice(
                "Incorrect - toSet() does not remove duplicate values.",
                false,
                "toSet() specifically removes duplicates by definition - that's exactly why comparing its size to the original length works as a duplicate check.",
            ),
            choice(
                "Incorrect - this only works if the array is already sorted.",
                false,
                "Set construction doesn't depend on order at all - values are deduplicated regardless of how the input array is arranged.",
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, TIME_COMPLEXITY,
        "A teammate claims the sort-and-compare approach and the count-map approach have the same time complexity since both \"just look at every character.\" Is that right?",
        conceptKey = "arrays-hashing-sort-vs-count-complexity", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "No - sorting costs O(n log n) while the count map costs O(n); looking at every character isn't the same as the total work done overall.",
                true,
                "The count-map approach visits each character a constant number of times, while sorting does extra comparison work proportional to log n per element - both touch every character, but not with the same total cost.",
            ),
            choice(
                "Yes - both approaches are O(n) since they process the same number of characters.",
                false,
                "Sorting isn't a single pass - the sorting algorithm itself costs O(n log n), on top of just visiting each character once.",
            ),
            choice(
                "Yes - both are O(n log n) since count maps also need to be sorted internally.",
                false,
                "A hash-based count map, keyed by a fixed alphabet (26 letters), needs no sorting at all - accessing any slot is close to constant time.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, SPACE_COMPLEXITY,
        "A teammate argues the hash-map approach doesn't really save space over sort-then-two-pointers, since both need to store the array somehow. Evaluate that claim.",
        conceptKey = "arrays-hashing-space-tradeoff-claim", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "The claim conflates the input array (which exists either way) with the extra O(n) map the hash approach specifically allocates - sort-then-two-pointers can often reuse the array in place.",
                true,
                "Both approaches need the input array itself, but the hash-map approach specifically adds its own O(n) structure on top - an in-place sort avoids that additional allocation.",
            ),
            choice(
                "The claim is entirely correct - both approaches use exactly the same amount of extra space.",
                false,
                "A hash map storing up to n entries is genuinely extra space beyond the array itself, while an in-place sort can avoid allocating anything beyond a few index variables.",
            ),
            choice(
                "The claim is backwards - sort-then-two-pointers always uses more space than a hash map.",
                false,
                "An in-place sort can use O(1) extra space (or O(log n) depending on the algorithm), which is typically less than the hash map's O(n), not more.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Two Pointers
    step(
        "valid-palindrome", TWO_POINTERS, PATTERN_RECOGNITION,
        "A teammate proposes building a cleaned, reversed copy of the string and comparing it to a cleaned forward copy. When would two pointers clearly win over this?",
        conceptKey = "two-pointers-in-place-vs-copy", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "When avoiding the extra O(n) space for the copies matters - two pointers check the same symmetry in place, using no extra string storage.",
                true,
                "Both approaches correctly detect a palindrome, but building reversed and cleaned copies costs extra space that two pointers moving through the original string never needs.",
            ),
            choice(
                "Two pointers is the only approach that can skip non-alphanumeric characters.",
                false,
                "A cleaned copy can just as easily filter out non-alphanumeric characters while building it - both approaches can handle that requirement.",
            ),
            choice(
                "Building a reversed copy is always incorrect for palindrome checks.",
                false,
                "Reversing and comparing is a completely valid way to check a palindrome - it's just less space-efficient than checking in place with two pointers.",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, PATTERN_RECOGNITION,
        "A teammate suggests using a hash set of all values and, for each pair, checking whether the negative of their sum exists in it, entirely skipping the sort. What's the tradeoff?",
        conceptKey = "two-pointers-in-place-vs-copy", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It avoids sorting but makes avoiding duplicate triplets significantly harder to get right than the sorted two-pointer approach, where equal values sit next to each other.",
                true,
                "Both approaches can find the same triplets, but without the sorted order, detecting and skipping duplicate combinations requires much more careful bookkeeping.",
            ),
            choice(
                "A hash set cannot represent negative numbers, so this approach fails on arrays with negative values.",
                false,
                "Hash sets store any integer value just fine, negative or positive - there's no representational limitation here.",
            ),
            choice(
                "This approach is strictly faster than the sorted two-pointer approach in every case.",
                false,
                "Both approaches are roughly O(n squared) - the hash-set version isn't inherently faster, and it trades sort time for extra space and duplicate-handling complexity.",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, APPROACH,
        "A teammate argues that since the container's area depends on both heights, the pointer with the *taller* line should move, to \"give the shorter one a chance to catch up.\" Evaluate this reasoning.",
        conceptKey = "two-pointers-shorter-side-moves", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It's backwards - moving the taller line's pointer can only shrink the width while keeping the same limiting height, which can never increase the area.",
                true,
                "The shorter line is what actually limits the current area, so only moving its pointer has any chance of finding a taller line and a larger area; moving the taller one wastes width for no possible gain.",
            ),
            choice(
                "It's correct - moving the taller pointer explores more of the array's range.",
                false,
                "Exploring more range doesn't help if the height that limits the area can never improve - moving the taller pointer guarantees the area gets no better, only possibly worse.",
            ),
            choice(
                "It doesn't matter which pointer moves, as long as both eventually meet.",
                false,
                "Which pointer moves determines whether the algorithm can ever discover a better area - moving the wrong one systematically misses the actual maximum.",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, APPROACH,
        "A teammate's solution finds all triplets correctly but occasionally returns the same triplet twice. What's the most likely cause, given the array is sorted first?",
        conceptKey = "two-pointers-duplicate-skip-approach", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "The loop over the fixed index (or the inner two pointers) isn't skipping past duplicate values after finding a match, so the same combination gets rediscovered from an adjacent equal index.",
                true,
                "In a sorted array, equal values sit next to each other - without explicitly advancing past repeats after a fixed index or a found match, the same triplet's values get revisited from a different, duplicate index.",
            ),
            choice(
                "The array wasn't sorted in descending order instead of ascending.",
                false,
                "Ascending versus descending order doesn't affect duplicate triplets - the two-pointer logic works the same either way, just moving in the opposite direction; the missing piece is skipping repeated values.",
            ),
            choice(
                "The two pointers started at the wrong initial positions.",
                false,
                "Starting positions being off would produce missed or incorrect triplets, not correctly-found ones appearing twice - that specific symptom points to a missing duplicate-skip step.",
            ),
        ),
    ),
    step(
        "valid-palindrome", TWO_POINTERS, STATE_SELECTION,
        "A teammate wants to precompute a fully cleaned, lowercase string before running the two pointers, rather than skipping characters in place. What does this trade away?",
        conceptKey = "two-pointers-precompute-vs-inline-filter", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It trades away O(1) extra space for O(n) extra space, since a whole new cleaned string has to be built and stored before the comparison even starts.",
                true,
                "Filtering characters as the pointers move needs no extra storage beyond the pointers themselves, while precomputing a cleaned string requires allocating and holding a full second copy.",
            ),
            choice(
                "It trades away correctness, since precomputing the cleaned string can miss some non-alphanumeric characters.",
                false,
                "A carefully built cleaned string can correctly remove every non-alphanumeric character - this approach is just as correct, only less space-efficient.",
            ),
            choice(
                "It trades away the ability to check case-insensitively.",
                false,
                "Precomputing a lowercase cleaned string handles case-insensitivity just fine - that's actually one of its more natural strengths, not a weakness.",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, BOUNDARY_UPDATE,
        "This version moves both pointers inward on every step, regardless of which height is shorter. Which input reveals it's wrong?",
        conceptKey = "two-pointers-both-move-boundary-bug", difficulty = DEVELOPING,
        code = "var left = 0\nvar right = height.size - 1\nvar maxArea = 0\nwhile (left < right) {\n    val area = minOf(height[left], height[right]) * (right - left)\n    maxArea = maxOf(maxArea, area)\n    left++\n    right--\n}",
        choices = listOf(
            choice(
                "heights = [1, 8, 1, 8], where the true maximum requires keeping one tall line in place while the pointer at the short line advances past the middle.",
                true,
                "Moving both pointers on every step shrinks the width twice as fast as necessary and can skip right past the combination of the two tall lines that would have produced the maximum area.",
            ),
            choice(
                "heights = [1, 1, 1, 1], all equal.",
                false,
                "With every height identical, moving either or both pointers produces the same limiting height each time - this case doesn't distinguish the buggy version from a correct one.",
            ),
            choice(
                "heights = [5, 4], only two values.",
                false,
                "With only two lines, there's exactly one possible container regardless of how the pointers move - this doesn't exercise the movement-rule bug at all.",
            ),
        ),
    ),
    step(
        "valid-palindrome", TWO_POINTERS, CODE_BLOCK,
        "Which snippet correctly compares characters while also being resilient to a string that is entirely non-alphanumeric?",
        conceptKey = "two-pointers-all-punctuation-guard", difficulty = DEVELOPING,
        code = "var left = 0\nvar right = s.length - 1\nwhile (left < right) {\n    // ???\n}\nreturn true",
        choices = listOf(
            choice(
                "while (left < right && !s[left].isLetterOrDigit()) left++\nwhile (left < right && !s[right].isLetterOrDigit()) right--\nif (left < right) { if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false; left++; right-- }",
                true,
                "Guarding every skip loop with left < right prevents either pointer from running past the other on a string with no alphanumeric characters at all, avoiding an out-of-bounds read.",
                code = "while (left < right && !s[left].isLetterOrDigit()) left++\nwhile (left < right && !s[right].isLetterOrDigit()) right--\nif (left < right) { if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false; left++; right-- }",
            ),
            choice(
                "while (!s[left].isLetterOrDigit()) left++\nwhile (!s[right].isLetterOrDigit()) right--\nif (s[left].lowercaseChar() != s[right].lowercaseChar()) return false\nleft++\nright--",
                false,
                "Without the left < right guard, a string with no alphanumeric characters at all lets one of the skip loops run straight past the other pointer and off the end of the string.",
                code = "while (!s[left].isLetterOrDigit()) left++\nwhile (!s[right].isLetterOrDigit()) right--\nif (s[left].lowercaseChar() != s[right].lowercaseChar()) return false\nleft++\nright--",
            ),
            choice(
                "if (!s[left].isLetterOrDigit()) { left++; return true }\nif (!s[right].isLetterOrDigit()) { right--; return true }",
                false,
                "Returning true the moment a non-alphanumeric character is seen abandons the check entirely instead of skipping past it and continuing to compare the rest of the string.",
                code = "if (!s[left].isLetterOrDigit()) { left++; return true }\nif (!s[right].isLetterOrDigit()) { right--; return true }",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, CODE_BLOCK,
        "Which snippet correctly avoids duplicate triplets when advancing both pointers after a match is found?",
        conceptKey = "two-pointers-duplicate-skip-code", difficulty = DEVELOPING,
        code = "// after recording a match at nums[i], nums[left], nums[right]\n// ???",
        choices = listOf(
            choice(
                "left++; right--\nwhile (left < right && nums[left] == nums[left - 1]) left++\nwhile (left < right && nums[right] == nums[right + 1]) right--",
                true,
                "Advancing past every run of equal values on both sides after a match, not just moving one step, ensures the exact same triplet's values are never rediscovered from an adjacent duplicate.",
                code = "left++; right--\nwhile (left < right && nums[left] == nums[left - 1]) left++\nwhile (left < right && nums[right] == nums[right + 1]) right--",
            ),
            choice(
                "left++; right--",
                false,
                "Moving just one step past the match leaves duplicate values right next to the new positions, so the identical triplet can be found again from those adjacent equal values.",
                code = "left++; right--",
            ),
            choice(
                "left++\nwhile (left < right && nums[left] == nums[left - 1]) left++",
                false,
                "Only skipping duplicates on the left side leaves the right pointer free to rediscover the same triplet from an adjacent equal value on its own side.",
                code = "left++\nwhile (left < right && nums[left] == nums[left - 1]) left++",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, TIME_COMPLEXITY,
        "A teammate claims that because the area formula involves a multiplication, the algorithm must be more than O(n). What's the flaw in this reasoning?",
        conceptKey = "two-pointers-per-step-work-vs-total-time", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "The multiplication is a single constant-time operation done once per step, not something that grows with input size - the total time is still governed by how many steps the pointers take, not by what happens inside each step.",
                true,
                "A single multiplication per iteration adds a fixed, tiny amount of work to each step - what actually determines the overall time complexity is how many total steps occur, which is bounded by n here.",
            ),
            choice(
                "The claim is correct - any arithmetic inside a loop always pushes the complexity above linear.",
                false,
                "Constant-time work inside a loop, like one multiplication, doesn't change the loop's own complexity - it only matters if that work itself scales with input size, which a single multiplication doesn't.",
            ),
            choice(
                "The claim is correct, but only because minOf also adds extra time.",
                false,
                "minOf between two values is also a constant-time operation - neither it nor the multiplication scales with array size, so neither pushes the algorithm above O(n).",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, SPACE_COMPLEXITY,
        "A teammate says the space complexity should be counted as O(n) because the result list can hold many triplets. Is including the output in this count standard practice here?",
        conceptKey = "two-pointers-output-space-convention", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Typically the required output isn't counted as \"extra\" space - the meaningful figure is the auxiliary space the algorithm itself uses beyond producing the answer, which here is just the sort's own overhead.",
                true,
                "Space complexity discussions usually separate the space needed to store the *answer itself*, which the problem requires regardless of approach, from the *extra* space the algorithm allocates while computing it.",
            ),
            choice(
                "Yes - the output list should always be included, making this approach O(n) extra space no matter what.",
                false,
                "Convention typically excludes the required output from the auxiliary space figure, since the answer has to be stored somewhere regardless of which algorithm produces it.",
            ),
            choice(
                "No - the output list uses no memory since it's returned directly to the caller.",
                false,
                "The output list is real memory being allocated and filled, it's simply not usually the number being asked about when discussing an algorithm's *extra* space usage.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Sliding Window
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, PATTERN_RECOGNITION,
        "A teammate proposes precomputing prefix sums and then binary searching for each starting point's shortest qualifying end point. How does this compare to a sliding window here?",
        conceptKey = "sliding-window-vs-prefix-binary-search", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It also works (values are positive, so prefix sums are monotonic) but costs O(n log n) total, more than the sliding window's O(n).",
                true,
                "Because all values are positive, prefix sums increase monotonically, making binary search valid - it just does more total work than the window, which never needs to search at all.",
            ),
            choice(
                "It cannot work at all, since prefix sums don't apply to subarray-sum problems.",
                false,
                "Prefix sums are exactly the tool for reasoning about subarray sums - the binary search idea is valid here, just not as efficient as the window.",
            ),
            choice(
                "It's strictly faster than the sliding window since binary search is involved.",
                false,
                "Binary search inside a loop over every starting point adds a log factor on top of the linear scan, making it slower overall than the window's single linear pass.",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, PATTERN_RECOGNITION,
        "A teammate proposes checking every substring directly, arguing that strings are usually short in real use. When does this reasoning hold up?",
        conceptKey = "sliding-window-vs-prefix-binary-search", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Only when n is small enough that the O(n squared) or worse cost of checking every substring genuinely doesn't matter - it degrades badly as the string grows, unlike the window's O(n).",
                true,
                "Brute-force substring checking isn't wrong, it's just fragile - fine for a short, fixed string, but the sliding window stays fast regardless of how long the string grows.",
            ),
            choice(
                "Never - checking every substring directly is always incorrect.",
                false,
                "Checking every substring directly is a correct, if slow, approach - the concern is scalability, not correctness.",
            ),
            choice(
                "Only when the string contains only lowercase letters.",
                false,
                "The character set involved doesn't affect the brute-force approach's scaling - the concern is purely about how the work grows with string length.",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, APPROACH,
        "A teammate proposes checking every pair of buy and sell days directly with two nested loops. What specifically breaks down as the input grows, compared to the running-minimum approach?",
        conceptKey = "sliding-window-brute-force-tradeoff", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "The nested-loop approach costs O(n squared), while tracking a running minimum reaches the same answer in a single O(n) pass.",
                true,
                "Checking every pair directly repeats work that a single pass with a running minimum avoids entirely - both are correct, but only one stays fast as the number of days grows.",
            ),
            choice(
                "Nested loops give the wrong answer once the array is large enough.",
                false,
                "Nested loops remain correct at any size, checking every possible buy-sell pair exhaustively - the issue is purely how much slower that gets as the array grows, not correctness.",
            ),
            choice(
                "Nested loops only work if the prices are all positive.",
                false,
                "Checking every pair of days works regardless of whether prices are positive or not - the profit calculation and comparison don't depend on price sign.",
            ),
        ),
    ),
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, APPROACH,
        "A teammate's window only ever shrinks by exactly one position per outer loop iteration, using `if` instead of `while`. What specifically goes wrong?",
        conceptKey = "sliding-window-shrink-if-vs-while", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It can report a longer minimum length than the true answer, since the window might still qualify after one shrink and could have shrunk further to find a shorter valid window.",
                true,
                "A single shrink might not be enough - if the window still qualifies afterward, a while loop would keep shrinking to find the truly shortest window, while an if stops one step too early.",
            ),
            choice(
                "It crashes with an index-out-of-bounds error on certain inputs.",
                false,
                "Shrinking too little doesn't cause an out-of-bounds error - the left pointer still only ever moves forward within valid bounds, it just doesn't move as far as it should.",
            ),
            choice(
                "It has no effect on the result, since the window would have shrunk to the same size eventually anyway.",
                false,
                "Using if instead of while genuinely limits how far the window shrinks within a single step, and since the loop moves on to expand right afterward, that shrinking opportunity is lost for good.",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, STATE_SELECTION,
        "A teammate wants to track the minimum price using the built-in `minOf(prices)` recomputed at every day instead of a running variable. What's the practical cost?",
        conceptKey = "sliding-window-recompute-vs-running-state", difficulty = DEVELOPING,
        code = "var maxProfit = 0\nfor (i in prices.indices) {\n    val minPrice = minOf(prices.subList(0, i + 1))\n    maxProfit = maxOf(maxProfit, prices[i] - minPrice)\n}",
        choices = listOf(
            choice(
                "It's still correct, but recomputing the minimum over a growing prefix at every day turns the whole algorithm into O(n squared) instead of O(n).",
                true,
                "A running minimum updates in constant time per day, while recomputing it from scratch over an ever-larger prefix repeats work that a single running variable avoids entirely.",
            ),
            choice(
                "It's incorrect, since minOf over a sublist can include today's own price as the minimum.",
                false,
                "Including today's own price in the minimum calculation is actually fine here, since buying and selling on the same day would just yield zero profit, not an incorrect one.",
            ),
            choice(
                "It uses less memory than a running variable, since nothing is stored between iterations.",
                false,
                "A single running variable is already about as little memory as possible - recomputing from a sublist doesn't save memory and instead costs significant extra time.",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, BOUNDARY_UPDATE,
        "This version shrinks the window with an `if` instead of a `while` when a duplicate is found. Which input exposes the bug?",
        conceptKey = "sliding-window-shrink-if-vs-while", difficulty = DEVELOPING,
        code = "var left = 0\nvar maxLen = 0\nval window = HashSet<Char>()\nfor (right in s.indices) {\n    if (s[right] in window) {\n        window.remove(s[left])\n        left++\n    }\n    window.add(s[right])\n    maxLen = maxOf(maxLen, right - left + 1)\n}",
        choices = listOf(
            choice(
                "\"abba\" - the second 'a' duplicates a character that isn't the current leftmost one, so a single shrink doesn't actually remove the duplicate.",
                true,
                "When right reaches the second 'a', removing only the leftmost character ('a' itself, ironically at that exact moment) may not be enough in general - a while loop is needed to keep shrinking until the specific duplicate is actually gone.",
            ),
            choice(
                "\"aaaa\" - every character is identical.",
                false,
                "With every character being an immediate repeat of the one before it, a single shrink per step happens to be enough here - this case doesn't distinguish the buggy version from a correct one.",
            ),
            choice(
                "\"abcdef\" - all unique characters.",
                false,
                "With no duplicates at all, the shrink logic never runs in the first place, so this input can't reveal a bug in how shrinking handles a duplicate.",
            ),
        ),
    ),
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, CODE_BLOCK,
        "A teammate's version adds nums[right] to the sum but forgets to update `right` in the length calculation, using a stale variable instead. Which snippet is correct?",
        conceptKey = "sliding-window-length-calc-code", difficulty = DEVELOPING,
        code = "var left = 0\nvar sum = 0\nvar minLen = Int.MAX_VALUE\nfor (right in nums.indices) {\n    sum += nums[right]\n    while (sum >= target) {\n        // ???\n        sum -= nums[left]\n        left++\n    }\n}",
        choices = listOf(
            choice(
                "minLen = minOf(minLen, right - left + 1)",
                true,
                "Using the current loop variable right (not a stale copy) alongside the current left correctly measures the window's true current length before it shrinks further.",
                code = "minLen = minOf(minLen, right - left + 1)",
            ),
            choice(
                "minLen = minOf(minLen, nums.size - left)",
                false,
                "Using the array's total size instead of the current right position measures the wrong span - it doesn't reflect where the window's right edge actually is at this point in the loop.",
                code = "minLen = minOf(minLen, nums.size - left)",
            ),
            choice(
                "minLen = minOf(minLen, right - left)",
                false,
                "Omitting the + 1 undercounts the window's length by one - a window spanning indices left through right inclusive has right - left + 1 elements, not right - left.",
                code = "minLen = minOf(minLen, right - left)",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, CODE_BLOCK,
        "A teammate uses a `Map<Char, Int>` of last-seen index instead of a `Set<Char>`, jumping `left` directly instead of shrinking one step at a time. Which snippet correctly implements this variant?",
        conceptKey = "sliding-window-map-jump-variant", difficulty = DEVELOPING,
        code = "var left = 0\nvar maxLen = 0\nval lastSeen = HashMap<Char, Int>()\nfor (right in s.indices) {\n    // ???\n    lastSeen[s[right]] = right\n    maxLen = maxOf(maxLen, right - left + 1)\n}",
        choices = listOf(
            choice(
                "if (lastSeen.containsKey(s[right]) && lastSeen[s[right]]!! >= left) left = lastSeen[s[right]]!! + 1",
                true,
                "Checking that the duplicate's last seen index is still inside the current window (>= left) before jumping avoids jumping backward on a duplicate that's already outside the window.",
                code = "if (lastSeen.containsKey(s[right]) && lastSeen[s[right]]!! >= left) left = lastSeen[s[right]]!! + 1",
            ),
            choice(
                "if (lastSeen.containsKey(s[right])) left = lastSeen[s[right]]!! + 1",
                false,
                "Without checking that the duplicate's last position is still within the current window, this can jump left *backward* on a character that was already excluded, silently growing the window incorrectly.",
                code = "if (lastSeen.containsKey(s[right])) left = lastSeen[s[right]]!! + 1",
            ),
            choice(
                "if (lastSeen.containsKey(s[right]) && lastSeen[s[right]]!! >= left) left = lastSeen[s[right]]!!",
                false,
                "Jumping to exactly the duplicate's old position instead of one past it would leave the duplicate character itself still inside the window, not actually resolving the repeat.",
                code = "if (lastSeen.containsKey(s[right]) && lastSeen[s[right]]!! >= left) left = lastSeen[s[right]]!!",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, TIME_COMPLEXITY,
        "A teammate claims the running-minimum approach is O(n) only in the best case, and worse otherwise, since \"it depends on the price pattern.\" Evaluate this.",
        conceptKey = "sliding-window-worst-case-vs-pattern", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Incorrect - the loop always visits each day exactly once regardless of the price pattern, doing the same constant amount of work per day either way.",
                true,
                "Unlike some algorithms whose work depends on the specific input pattern, this one does the same fixed amount of work - one comparison, one possible update - on every single day, no matter what the prices look like.",
            ),
            choice(
                "Correct - a mostly-decreasing price sequence forces extra work compared to a mostly-increasing one.",
                false,
                "Whether prices trend up or down doesn't change how much work each day's iteration does - updating a running minimum and a running maximum profit are both constant-time regardless of the trend.",
            ),
            choice(
                "Correct - strictly increasing prices are the worst case, costing O(n squared).",
                false,
                "Strictly increasing prices are actually one of the simplest cases for this algorithm, not a worst case - every day's iteration still does the same constant amount of work.",
            ),
        ),
    ),
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, SPACE_COMPLEXITY,
        "A teammate argues the prefix-sum-and-binary-search alternative is worse not just in time but also in space. Is that accurate?",
        conceptKey = "sliding-window-vs-prefix-space-tradeoff", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Yes - the prefix-sum array itself costs O(n) extra space, while the sliding window only ever needs a fixed handful of variables.",
                true,
                "Building a prefix-sum array requires storing one cumulative value per element, while the window's left pointer and running sum are just a few variables regardless of array size.",
            ),
            choice(
                "No - both approaches use the same O(n) space since both read the entire array.",
                false,
                "Reading the array doesn't count as extra space since it already exists as input - the real difference is that the prefix-sum array is a whole new O(n) structure, while the window allocates nothing beyond a few variables.",
            ),
            choice(
                "No - the sliding window actually uses more space because it tracks both a sum and a minimum length.",
                false,
                "Tracking two or three simple variables is still O(1) regardless of how many of them there are - what matters for the complexity class is that none of them scale with the array's size.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Stack
    step(
        "valid-parentheses", STACK, PATTERN_RECOGNITION,
        "A teammate proposes repeatedly finding and removing any adjacent matching pair like \"()\" from the string until nothing more can be removed, then checking if it's empty. How does this compare to the stack approach?",
        conceptKey = "stack-repeated-removal-vs-single-pass", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It's also correct, but each removal pass can cost O(n) and might need to run many times, while the stack solves it in a single O(n) pass.",
                true,
                "Repeatedly re-scanning the shrinking string for adjacent pairs does eventually reach the right answer, but a stack tracks the same information incrementally in one linear pass instead.",
            ),
            choice(
                "It cannot detect mismatched bracket types, only unmatched counts.",
                false,
                "Removing specifically matching adjacent pairs, like \"()\" but not \"(]\", does correctly catch type mismatches - the approach is valid, just less efficient.",
            ),
            choice(
                "It's actually faster than a stack since it doesn't need any extra data structure.",
                false,
                "Avoiding an extra data structure doesn't make repeated string scanning faster - multiple linear passes over a shrinking string is still more total work than one linear pass with a stack.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, PATTERN_RECOGNITION,
        "A teammate proposes, for each day, scanning forward until a warmer day is found. When is this actually acceptable, and when does it clearly lose to the monotonic-stack approach?",
        conceptKey = "stack-repeated-removal-vs-single-pass", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It's fine for a small or mostly-increasing sequence, but on strictly decreasing temperatures it degrades to O(n squared), while the stack stays O(n) regardless of the input pattern.",
                true,
                "Forward-scanning per day works correctly everywhere, but its cost depends heavily on how far away each day's answer is - the stack sidesteps that entirely by resolving each day only when it's actually needed.",
            ),
            choice(
                "It's never acceptable since it produces wrong answers on decreasing sequences.",
                false,
                "Forward-scanning always produces the correct answer eventually - the concern is purely how much slower it can get, not correctness.",
            ),
            choice(
                "It's always at least as fast as the stack approach since it needs no extra data structure.",
                false,
                "Needing no extra data structure doesn't help if the total number of comparisons made across all days grows much larger, which is exactly what happens on unfavorable temperature patterns.",
            ),
        ),
    ),
    step(
        "min-stack", STACK, APPROACH,
        "A teammate proposes tracking only a single running-minimum variable instead of a parallel min-stack. Why does this fall apart specifically on pop?",
        conceptKey = "stack-single-var-vs-parallel-stack", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "A single variable has no way to recover what the minimum used to be once the value that set it is popped off - the parallel stack remembers the minimum at every level specifically to solve that.",
                true,
                "Once the current minimum is popped, a single variable is simply gone with no memory of the prior minimum, while the min-stack's own top always reflects exactly what the minimum was one level down.",
            ),
            choice(
                "A single variable can't be updated in O(1) time on push.",
                false,
                "Updating a single running minimum on push is trivially O(1) - comparing and possibly replacing one value is fast; the real problem shows up specifically when popping.",
            ),
            choice(
                "A single variable uses more memory than a parallel stack.",
                false,
                "A single variable is about as little memory as possible - it's actually the parallel stack that uses more memory (O(n) vs O(1)), in exchange for correctness on pop.",
            ),
        ),
    ),
    step(
        "valid-parentheses", STACK, APPROACH,
        "A teammate's version pushes every character, opening or closing, and checks matching pairs only at the very end by scanning the stack. What's the practical downside versus checking closes immediately?",
        conceptKey = "stack-immediate-vs-deferred-check", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It can't correctly detect ordering problems like \"())(\", since a final scan doesn't capture *when* each character appeared relative to the others.",
                true,
                "Checking matches only at the end loses the information about the order characters arrived in, which is exactly what determines whether the nesting is actually valid, not just whether counts happen to balance.",
            ),
            choice(
                "It uses less memory since only one stack is needed.",
                false,
                "Both the immediate-check and deferred-check versions use a single stack - memory usage isn't what differs between them.",
            ),
            choice(
                "It's equally correct, just slower, since it still processes every character once.",
                false,
                "This isn't just a speed difference - deferring the check to the end can genuinely produce wrong true/false answers on strings that immediate checking would correctly catch as invalid.",
            ),
        ),
    ),
    step(
        "min-stack", STACK, STATE_SELECTION,
        "A teammate wants to store `(value, minSoFar)` pairs in a single stack instead of two parallel stacks. Is this a reasonable alternative?",
        conceptKey = "stack-paired-tuples-vs-two-stacks", difficulty = DEVELOPING,
        code = "val stack = ArrayDeque<Pair<Int, Int>>()  // (value, minSoFar)",
        choices = listOf(
            choice(
                "Yes - it's functionally equivalent to two parallel stacks, just bundled into one stack of pairs instead of two separate structures.",
                true,
                "Whether the value and its corresponding minimum are stored as two aligned stacks or as one stack of pairs, the same information is available at the same time - it's a packaging choice, not a different algorithm.",
            ),
            choice(
                "No - a single stack cannot support both push and pop in O(1) time.",
                false,
                "A stack of pairs supports push and pop in O(1) exactly like a stack of plain values - bundling two values into a pair doesn't change the stack's own time complexity.",
            ),
            choice(
                "No - storing pairs loses the ability to look up just the current minimum.",
                false,
                "The current minimum is still sitting right there as the second element of whatever pair is on top - it's just as directly accessible as it would be from a separate min-stack.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, BOUNDARY_UPDATE,
        "This version pops only a single day off the stack per outer iteration, using `if` instead of `while`. Which input reveals the bug?",
        conceptKey = "stack-single-vs-multi-pop-boundary", difficulty = DEVELOPING,
        code = "val answer = IntArray(temperatures.size)\nval stack = ArrayDeque<Int>()\nfor (i in temperatures.indices) {\n    if (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n        val prevDay = stack.removeLast()\n        answer[prevDay] = i - prevDay\n    }\n    stack.addLast(i)\n}",
        choices = listOf(
            choice(
                "[70, 65, 60, 75] - the day with 75 should resolve all three earlier waiting days at once, but a single if only resolves the most recent one.",
                true,
                "When the warm day 75 arrives, every one of the three earlier, cooler waiting days should be resolved in the same step - popping only once leaves the other two stuck on the stack, unresolved by that day.",
            ),
            choice(
                "[70, 75], a simple two-day case.",
                false,
                "With only one waiting day to resolve, a single pop happens to be exactly enough here - this case doesn't distinguish the buggy version from a correct one.",
            ),
            choice(
                "[80, 70, 60], strictly decreasing temperatures.",
                false,
                "With temperatures only ever decreasing, no day ever triggers a pop at all, so this input can't expose a bug in how many days get popped per resolution.",
            ),
        ),
    ),
    step(
        "valid-parentheses", STACK, CODE_BLOCK,
        "A teammate's version checks the popped value's type but forgets to check whether the stack was empty first. Which snippet correctly guards both failure conditions?",
        conceptKey = "stack-both-guards-code", difficulty = DEVELOPING,
        code = "val stack = ArrayDeque<Char>()\nval pairs = mapOf(')' to '(', ']' to '[', '}' to '{')\nfor (c in s) {\n    if (c in \"([{\") {\n        stack.addLast(c)\n    } else {\n        // ???\n    }\n}\nreturn stack.isEmpty()",
        choices = listOf(
            choice(
                "if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false",
                true,
                "Checking isEmpty() before popping, combined with checking the popped value's type, correctly rejects both an unmatched closing bracket and a mismatched pair.",
                code = "if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false",
            ),
            choice(
                "if (stack.removeLast() != pairs[c]) return false",
                false,
                "Without checking isEmpty() first, calling removeLast() on an empty stack throws an exception instead of correctly identifying the string as invalid.",
                code = "if (stack.removeLast() != pairs[c]) return false",
            ),
            choice(
                "if (stack.isEmpty()) return false\nstack.removeLast()",
                false,
                "Popping without comparing the result to pairs[c] means a mismatched bracket type, like an opening '(' closed by ']', would be silently accepted as valid.",
                code = "if (stack.isEmpty()) return false\nstack.removeLast()",
            ),
        ),
    ),
    step(
        "min-stack", STACK, CODE_BLOCK,
        "A teammate's pop() only removes from the main stack and forgets the min-stack entirely. Which snippet correctly keeps both in sync?",
        conceptKey = "stack-pop-both-together-code", difficulty = DEVELOPING,
        code = "val stack = ArrayDeque<Int>()\nval minStack = ArrayDeque<Int>()\nfun pop() {\n    // ???\n}",
        choices = listOf(
            choice(
                "stack.removeLast()\nminStack.removeLast()",
                true,
                "Popping from both stacks together on every pop() call keeps them the exact same size, which is what lets minStack's top always correctly reflect the minimum of whatever remains on the main stack.",
                code = "stack.removeLast()\nminStack.removeLast()",
            ),
            choice(
                "stack.removeLast()",
                false,
                "Popping only the main stack leaves minStack one entry too large, so its top would still reflect a minimum that includes the value that was just removed - getMin would then be wrong.",
                code = "stack.removeLast()",
            ),
            choice(
                "if (stack.last() == minStack.last()) { stack.removeLast(); minStack.removeLast() } else { stack.removeLast() }",
                false,
                "Only popping the min-stack when the popped value happens to equal the current minimum breaks the assumption that both stacks stay the same size - since every push added to both, every pop needs to remove from both too.",
                code = "if (stack.last() == minStack.last()) { stack.removeLast(); minStack.removeLast() } else { stack.removeLast() }",
            ),
        ),
    ),
    step(
        "min-stack", STACK, TIME_COMPLEXITY,
        "A teammate claims getMin is O(log n) since \"stacks are like trees.\" What's wrong with this reasoning?",
        conceptKey = "stack-flat-structure-not-tree", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "A stack is a flat, linear structure, not a tree - reading its top element, which is exactly what getMin does on the min-stack, is a single O(1) operation, not a search through any hierarchy.",
                true,
                "There's no branching or hierarchy in a stack - the min-stack's top is a specific, single, directly accessible position, making getMin exactly as fast as any other top-of-stack read.",
            ),
            choice(
                "The claim is correct - accessing any element in a stack requires traversing from the bottom up.",
                false,
                "A stack only ever needs access to its top element for push, pop, and peek - there's no need to traverse through the rest of the stack to reach it.",
            ),
            choice(
                "The claim is correct only when the stack contains more than log n elements.",
                false,
                "The number of elements doesn't change how top-of-stack access works - it's O(1) regardless of whether the stack holds 3 elements or 3 million.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, SPACE_COMPLEXITY,
        "A teammate argues the forward-scanning brute-force approach uses less space than the stack approach, since it needs no extra structure. Is that the right way to compare them?",
        conceptKey = "stack-time-space-tradeoff-comparison", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "The space comparison is technically true, but it misses that the brute-force approach trades away time (O(n squared) worst case) for that space savings - the stack's O(n) space buys back a large time improvement.",
                true,
                "It's accurate that forward-scanning avoids the stack's extra memory, but comparing approaches on space alone ignores the far more significant time cost that comes with it.",
            ),
            choice(
                "It's inaccurate - the stack approach actually uses less space than forward-scanning.",
                false,
                "Forward-scanning genuinely needs no extra structure beyond the answer array itself, while the stack can grow to hold up to n day indices - the space comparison itself is correct.",
            ),
            choice(
                "It's irrelevant - both approaches use exactly the same amount of space.",
                false,
                "The two approaches don't use the same space - forward-scanning needs none beyond the output, while the stack can grow to O(n) in the worst case.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Binary Search
    step(
        "binary-search", BINARY_SEARCH, PATTERN_RECOGNITION,
        "A teammate proposes jumping through the array in fixed-size blocks (checking every k-th element) before doing a linear scan within the identified block. How does this compare to standard binary search?",
        conceptKey = "binary-search-vs-block-jump", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It's a valid idea (this is essentially jump search) but with an optimally chosen block size it's O(sqrt n), still worse than binary search's O(log n).",
                true,
                "Jump search is a real, correct technique, but even tuned well it doesn't repeatedly halve the search space the way true binary search does, leaving it slower for large arrays.",
            ),
            choice(
                "It's incorrect since it doesn't use every element of the array.",
                false,
                "Not touching every element is fine when the array is sorted, since large chunks can be safely ruled out - the concern here is speed relative to binary search, not correctness.",
            ),
            choice(
                "It's exactly as fast as binary search regardless of block size.",
                false,
                "Block size directly affects how much work jump search does - it doesn't achieve the same logarithmic time as repeatedly halving the search range.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, PATTERN_RECOGNITION,
        "A teammate proposes scanning the array once and tracking the smallest value seen so far, arguing it's \"simpler to reason about\" than comparing to an edge. What's the real tradeoff?",
        conceptKey = "binary-search-vs-block-jump", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It's correct and simpler, but O(n) instead of O(log n) - it ignores that most of the array is still sorted in two pieces, which binary search exploits to eliminate half the array each step.",
                true,
                "A full linear scan always works, correctly or not, but it throws away the structural information (two sorted runs) that lets binary search find the same answer while touching far fewer elements.",
            ),
            choice(
                "It's incorrect on arrays that have been rotated by their full length.",
                false,
                "A full linear scan correctly handles any rotation amount, including a full-length rotation that leaves the array effectively unrotated - simplicity isn't the issue, speed is.",
            ),
            choice(
                "It's exactly as fast as the comparison-based binary search in every case.",
                false,
                "A linear scan always costs O(n), while binary search costs O(log n) - for any reasonably large array, the two are not close to the same speed.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, APPROACH,
        "A teammate proposes starting the binary search bounds at low=1, high=sum(piles) instead of low=1, high=max(piles). Is the wider range a problem?",
        conceptKey = "binary-search-tight-vs-loose-bounds", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "It's still correct, just slightly slower - the extra iterations needed to search the unnecessarily wide range only add a small constant number of steps thanks to the logarithmic nature of binary search.",
                true,
                "A looser upper bound still finds the right answer, since binary search just needs *some* value guaranteed to be feasible above it - it costs a few extra halving steps, not a change in complexity class.",
            ),
            choice(
                "It's incorrect, since sum(piles) might not actually be a feasible eating speed.",
                false,
                "Any speed at least as large as the largest single pile is always feasible (finishing every pile within an hour each), and sum(piles) is always at least that large, so it remains a valid, if loose, upper bound.",
            ),
            choice(
                "It changes the algorithm's time complexity from O(n log m) to O(n log(sum)).",
                false,
                "Technically the log factor now depends on the sum instead of the max, but since the sum is bounded by n times the max, this only adds a small additive term, not a different complexity class in practice.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, APPROACH,
        "A teammate's version compares the middle element to the *leftmost* element instead of the rightmost. Under what condition does this reasoning fail to reliably identify the rotation point?",
        conceptKey = "binary-search-wrong-comparison-edge", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "When the rotation point is in the right half, the left portion still looks internally consistent with the left edge, so comparing to it doesn't reliably reveal which side actually holds the rotation point.",
                true,
                "The right edge specifically tells you whether the middle element belongs to the larger, unrotated-looking run or not - the left edge doesn't carry that same discriminating information in every case.",
            ),
            choice(
                "It fails only when the array has exactly one element.",
                false,
                "A single-element array doesn't even trigger a meaningful comparison in the first place - the reliability problem shows up specifically in larger, genuinely rotated arrays.",
            ),
            choice(
                "It never fails - comparing to either edge works identically.",
                false,
                "The two comparisons are not interchangeable - comparing against the right edge specifically identifies which side holds the rotation point in a way the left edge comparison doesn't reliably do.",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, STATE_SELECTION,
        "A teammate wants to track just a single `mid` variable across iterations, reusing it as both the current guess and a memory of the last guess. What's the issue?",
        conceptKey = "binary-search-insufficient-state", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Without separately tracked low and high bounds, there's no way to know what range still needs to be searched - a single mid value alone can't reconstruct the current search boundaries.",
                true,
                "The whole point of low and high is to remember which portion of the array is still in play - collapsing that down to just the most recent guess loses the information needed to compute the next one correctly.",
            ),
            choice(
                "It's fine, as long as mid is updated on every iteration.",
                false,
                "Updating mid alone doesn't recover the actual range still being searched - low and high specifically encode that boundary information, which a single reused variable can't hold.",
            ),
            choice(
                "It only fails when the target isn't present in the array.",
                false,
                "The missing boundary information causes problems regardless of whether the target exists - there's no way to correctly narrow the search range without tracking both ends.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, BOUNDARY_UPDATE,
        "This version uses `hoursNeeded(mid) < h` instead of `<= h` when deciding feasibility. Which input reveals the bug?",
        conceptKey = "binary-search-strict-vs-inclusive-boundary", difficulty = DEVELOPING,
        code = "var low = 1\nvar high = piles.max()\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    if (hoursNeeded(mid) < h) high = mid else low = mid + 1\n}",
        choices = listOf(
            choice(
                "piles = [3, 6, 7, 11], h = 8, where the true answer, speed 4, needs exactly 8 hours - a strict less-than would wrongly treat that exact match as infeasible.",
                true,
                "A speed that finishes in exactly h hours should count as feasible, but the strict less-than comparison rejects it, potentially converging on a faster speed than the true minimum.",
            ),
            choice(
                "piles = [10], h = 100, with hours to spare.",
                false,
                "With plenty of extra time available, many speeds finish well under h hours, so the exact-match boundary this bug affects never actually comes into play here.",
            ),
            choice(
                "piles = [5, 5, 5], h = 15, exactly enough time at the slowest reasonable speed.",
                false,
                "This doesn't land exactly on the boundary the bug affects in a way that changes the outcome - the strict-vs-inclusive distinction specifically matters when a candidate speed's hours exactly equal h.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, CODE_BLOCK,
        "A teammate's version handles the case where the array isn't rotated at all as a completely separate special case checked before the loop. Which snippet avoids needing that special case entirely?",
        conceptKey = "binary-search-no-special-case-code", difficulty = DEVELOPING,
        code = "var low = 0\nvar high = nums.size - 1\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    // ???\n}\nreturn nums[low]",
        choices = listOf(
            choice(
                "if (nums[mid] > nums[high]) low = mid + 1 else high = mid",
                true,
                "This same comparison naturally handles an unrotated array too - nums[mid] is never greater than nums[high] when the whole array is sorted, so the loop just keeps narrowing high down to index 0 without needing separate logic.",
                code = "if (nums[mid] > nums[high]) low = mid + 1 else high = mid",
            ),
            choice(
                "if (nums[0] < nums[nums.size - 1]) return nums[0]\nif (nums[mid] > nums[high]) low = mid + 1 else high = mid",
                false,
                "Checking upfront whether the array looks unrotated adds an unnecessary special case - the core comparison already handles that situation correctly on its own.",
                code = "if (nums[0] < nums[nums.size - 1]) return nums[0]\nif (nums[mid] > nums[high]) low = mid + 1 else high = mid",
            ),
            choice(
                "if (nums[mid] > nums[0]) low = mid + 1 else high = mid",
                false,
                "Comparing against index 0 instead of the current high doesn't reliably identify which half currently holds the rotation point, especially as the search range narrows away from the original bounds.",
                code = "if (nums[mid] > nums[0]) low = mid + 1 else high = mid",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, CODE_BLOCK,
        "A teammate's version updates low and high but forgets to return -1 when the loop finishes without finding the target. Which snippet correctly completes the function?",
        conceptKey = "binary-search-not-found-return-code", difficulty = DEVELOPING,
        code = "var low = 0\nvar high = nums.size - 1\nwhile (low <= high) {\n    val mid = low + (high - low) / 2\n    when {\n        nums[mid] < target -> low = mid + 1\n        nums[mid] > target -> high = mid - 1\n        else -> return mid\n    }\n}\n// ???",
        choices = listOf(
            choice(
                "return -1",
                true,
                "Once low exceeds high, every possible position has been ruled out and the target genuinely isn't present - returning -1 here, outside the loop, is exactly the signal the problem asks for.",
                code = "return -1",
            ),
            choice(
                "return low",
                false,
                "Returning low instead of -1 would report a real array index even when the target was never found, since low still points somewhere valid in (or just past) the array - the caller couldn't distinguish a real match from a non-match.",
                code = "return low",
            ),
            choice(
                "return nums[low]",
                false,
                "Returning the value at index low, rather than a sentinel like -1, doesn't signal 'not found' at all and can even throw if low has moved past the array's last valid index.",
                code = "return nums[low]",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, TIME_COMPLEXITY,
        "A teammate claims that because binary search sometimes gets lucky and finds the target on the first try, its *average* case is much better than O(log n). Is this the right way to think about it?",
        conceptKey = "binary-search-average-vs-worst-case", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "Complexity analysis describes how the algorithm scales in the worst (and typical) case across all inputs, not the luckiest possible run - O(log n) already accounts for the fact that most runs don't need the full log n steps.",
                true,
                "A single lucky run finding the target immediately doesn't change what happens across all possible target positions - the O(log n) bound already reflects the typical number of steps needed, not just the worst single instance.",
            ),
            choice(
                "Yes - the correct complexity to report is whatever the best possible run achieves, which is O(1).",
                false,
                "Reporting only the best possible single run (finding the target on the first guess) would be misleading - complexity analysis is about how the algorithm behaves in general, not its luckiest possible outcome.",
            ),
            choice(
                "Yes - average case should always be reported instead of worst case for search algorithms.",
                false,
                "Both average and worst case are meaningful to report - for binary search, they happen to be the same order of growth, O(log n), so the distinction doesn't actually change the answer here.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, SPACE_COMPLEXITY,
        "A teammate argues that since `hoursNeeded` is called from inside the binary search loop, the space complexity should include the call stack depth of that nested call. Evaluate this.",
        conceptKey = "binary-search-nested-call-space", difficulty = DEVELOPING,
        choices = listOf(
            choice(
                "hoursNeeded is a single, non-recursive function call that returns before the next binary search iteration begins, so it contributes only O(1) space at any one time, not something that accumulates across iterations.",
                true,
                "Each call to hoursNeeded uses a small, fixed amount of stack space and then returns completely before the next one starts - nothing from it lingers or builds up across the outer loop's iterations.",
            ),
            choice(
                "The claim is correct - nested function calls always add to the overall space complexity proportional to how many times they're called.",
                false,
                "A non-recursive function call's space is freed the moment it returns - calling it many times sequentially doesn't accumulate space, since each call's stack frame is gone before the next begins.",
            ),
            choice(
                "The claim is correct, and it changes the overall space complexity from O(1) to O(log m).",
                false,
                "The number of times a constant-space function is called doesn't change the peak space used at any one moment, since each call's space is released before the next call happens.",
            ),
        ),
    ),
)
