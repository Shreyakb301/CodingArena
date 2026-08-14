package com.codingarena.content

import com.codingarena.domain.model.CurriculumDifficulty

/**
 * Two authored sequences coexist: the original five-question
 * (APPROACH, CODE_BLOCK, DEBUG, TIME_COMPLEXITY, SPACE_COMPLEXITY) form, and the
 * newer six-question, Problem-Workout-aligned form (PATTERN_RECOGNITION,
 * APPROACH, STATE_SELECTION, BOUNDARY_UPDATE, CODE_BLOCK, TIME_COMPLEXITY) -
 * see [RoadmapLessonsTest] for the exact sequences a lesson must follow.
 */
enum class LessonQuestionKind {
    PATTERN_RECOGNITION,
    APPROACH,
    STATE_SELECTION,
    BOUNDARY_UPDATE,
    CODE_BLOCK,
    DEBUG,
    TIME_COMPLEXITY,
    SPACE_COMPLEXITY,
}

/**
 * [code] is set for CODE_BLOCK questions, where each choice is a complete
 * snippet rather than a text description - the wrong snippets stay structurally
 * close to the right one so spotting the difference is the exercise.
 */
data class LessonChoice(
    val text: String,
    val correct: Boolean,
    val feedback: String,
    val code: String? = null,
)

/** A technical term used in the question, defined plainly once the answer is checked. */
data class GlossaryTerm(
    val term: String,
    val definition: String,
)

data class LessonQuestion(
    val id: String,
    val kind: LessonQuestionKind,
    val prompt: String,
    val code: String? = null,
    val choices: List<LessonChoice>,
    val glossary: List<GlossaryTerm> = emptyList(),
)

data class LessonExample(
    val input: String,
    val output: String,
    val explanation: String,
)

data class CompleteExplanation(
    val intuition: String,
    val walkthrough: List<String>,
    val pseudocode: String,
    val referenceCode: String,
    val timeComplexity: String,
    val spaceComplexity: String,
    val alternatives: List<String>,
    val commonMistakes: List<String>,
    val recognitionSignals: List<String>,
    val takeaway: String,
)

data class RoadmapLesson(
    val slug: String,
    val title: String,
    val difficulty: CurriculumDifficulty,
    val description: String,
    val constraints: List<String>,
    val examples: List<LessonExample>,
    val questions: List<LessonQuestion>,
    val explanation: CompleteExplanation,
    val nextSlug: String?,
)

/**
 * Reviewed, offline lesson content. The choices are deliberately plausible:
 * wrong answers model a real alternative or misconception rather than acting
 * as visual filler that gives the answer away.
 */
object RoadmapLessons {
    private val containsDuplicate = RoadmapLesson(
        slug = "contains-duplicate",
        title = "Contains Duplicate",
        difficulty = CurriculumDifficulty.EASY,
        description = "Given an integer array, decide whether at least one value occurs more than once. Return true when a duplicate exists and false when every value is unique.",
        constraints = listOf(
            "The array may be empty.",
            "Values may be positive, negative, or zero.",
            "The array may contain up to 100,000 values.",
            "Do not change the input order; target average linear time.",
        ),
        examples = listOf(
            LessonExample("[1, 2, 3, 1]", "true", "The value 1 appears at two different positions."),
            LessonExample("[1, 2, 3, 4]", "false", "Every value appears exactly once."),
            LessonExample("[]", "false", "An empty array cannot contain a repeated value."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "contains-duplicate-pattern-recognition",
                kind = LessonQuestionKind.PATTERN_RECOGNITION,
                prompt = "Given an array, decide whether any value appears more than once. Which pattern fits?",
                choices = listOf(
                    LessonChoice(
                        "Hash set: track every value seen so far and check membership before adding each new one.",
                        true,
                        "A set answers 'have I seen this value before' in constant time, which is exactly the question that needs answering for every element.",
                    ),
                    LessonChoice(
                        "Two pointers scanning from both ends of the array inward.",
                        false,
                        "Duplicate values could be anywhere in the array, not necessarily positioned symmetrically from the two ends, so there's no reason to anchor pointers there.",
                    ),
                    LessonChoice(
                        "Binary search each value against the rest of the array.",
                        false,
                        "Binary search needs sorted data, and the array isn't sorted going in - a hash set answers the membership question without needing any preprocessing at all.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "contains-duplicate-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach correctly detects a duplicate in one pass?",
                choices = listOf(
                    LessonChoice(
                        "For each value, check whether it's already in the set; if so, return true immediately; otherwise add it and continue.",
                        true,
                        "Checking before adding is what actually detects the second occurrence - the very moment a repeated value shows up, its first occurrence is already sitting in the set.",
                    ),
                    LessonChoice(
                        "Add every value to the set first, then compare the set's size to the array's length.",
                        false,
                        "This also works, since a smaller set size than the array means something repeated, but it needs to build the whole set before it can answer anything, rather than returning as soon as a duplicate is found.",
                    ),
                    LessonChoice(
                        "Sort the array and return true if it's not already sorted.",
                        false,
                        "Checking sortedness has nothing to do with detecting duplicates - a strictly increasing array has no duplicates regardless of sortedness, and a sorted array can still contain repeats.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "contains-duplicate-state-selection",
                kind = LessonQuestionKind.STATE_SELECTION,
                prompt = "What state does this approach need?",
                choices = listOf(
                    LessonChoice(
                        "A hash set of every value seen so far.",
                        true,
                        "Membership in a set is all that's needed to answer 'has this exact value shown up before' for each new element.",
                    ),
                    LessonChoice(
                        "A count of how many times each value has appeared, even after the answer is already known.",
                        false,
                        "Continuing to count occurrences after a duplicate is already found does unnecessary extra work - the question only needs a single true or false answer, not full counts.",
                    ),
                    LessonChoice(
                        "The index of the first and last elements only.",
                        false,
                        "A duplicate could involve any two positions in the array, not specifically the first and last elements, so tracking just those two indices misses most cases.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "contains-duplicate-boundary-update",
                kind = LessonQuestionKind.BOUNDARY_UPDATE,
                prompt = "Which rule correctly checks and updates the set at each value?",
                choices = listOf(
                    LessonChoice(
                        "Add the current value to the set first, then check whether the set contains it.",
                        false,
                        "Adding first means the check afterward will always find the value present, since it was just inserted - this can never correctly detect a duplicate.",
                    ),
                    LessonChoice(
                        "Check whether the current value is already in the set; if yes, return true; if no, add it, then move to the next value.",
                        true,
                        "Checking membership before adding is what makes the check meaningful - if the value is already there, this exact value must have appeared earlier.",
                    ),
                    LessonChoice(
                        "Add the current value to the set only if it's not already the array's first element.",
                        false,
                        "Whether a value is the array's first element has nothing to do with whether it's a duplicate - duplicates can start anywhere in the array.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "contains-duplicate-code-block",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three read almost identically. Which one actually returns the right answer?",
                choices = listOf(
                    LessonChoice(
                        text = "Checks whether the value was already there, using add()'s return value.",
                        correct = true,
                        feedback = "add() returns false when the value was already present, so !seen.add(n) fires exactly on the duplicate and nothing else.",
                        code = "fun containsDuplicate(nums: IntArray): Boolean {\n    val seen = mutableSetOf<Int>()\n    for (n in nums) {\n        if (!seen.add(n)) return true\n    }\n    return false\n}",
                    ),
                    LessonChoice(
                        text = "Checks the same add() result, but reads it as-is instead of flipping true and false like the correct version does.",
                        correct = false,
                        feedback = "add() returns true for a brand-new value, so this returns true on the very first element of almost any input - the true/false flip is missing.",
                        code = "fun containsDuplicate(nums: IntArray): Boolean {\n    val seen = mutableSetOf<Int>()\n    for (n in nums) {\n        if (seen.add(n)) return true\n    }\n    return false\n}",
                    ),
                    LessonChoice(
                        text = "Compares the set size to the array length afterward.",
                        correct = false,
                        feedback = "A duplicate makes the set smaller than the array, so the sizes are equal exactly when there is no duplicate - this returns the opposite answer.",
                        code = "fun containsDuplicate(nums: IntArray): Boolean {\n    val seen = mutableSetOf<Int>()\n    for (n in nums) {\n        seen.add(n)\n    }\n    return seen.size == nums.size\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "contains-duplicate-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "Assuming average O(1) set membership and insertion, what is the time complexity?",
                choices = listOf(
                    LessonChoice(
                        "O(n log n), because the stored values must remain ordered.",
                        false,
                        "A hash set does not keep its values sorted. There is no sorting step contributing log n work.",
                    ),
                    LessonChoice(
                        "O(n squared), because membership scans every stored value.",
                        false,
                        "That describes a list-based lookup. Average hash-set membership does not scan every stored value.",
                    ),
                    LessonChoice(
                        "Average O(n), because each of n values performs average O(1) work.",
                        true,
                        "Exactly: one traversal multiplied by average constant-time membership and insertion gives average O(n).",
                    ),
                ),
                glossary = listOf(
                    GlossaryTerm("O(1)", "Constant time - the same small amount of work every time, no matter how large the set gets."),
                    GlossaryTerm("membership", "Whether a value is already present in the set."),
                    GlossaryTerm("O(n)", "Linear time - work grows in direct proportion to the input size."),
                    GlossaryTerm("O(n log n)", "Grows a bit faster than linear - what sorting typically costs."),
                    GlossaryTerm("O(n squared)", "Grows much faster than linear - what comparing every value against every other value costs."),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Think of checking people in at the door of a party.\n\nYou keep a guest list, empty at first.\n\nEach time someone new arrives, you check the list before writing their name down. If the name is already there, they've checked in before.\n\nA 'set' in code is exactly that guest list. Its only job is to remember what it has already seen, and answer 'have I seen this?' instantly.",
            walkthrough = listOf(
                "seen starts as an empty set: { }",
                "Look at the first number, 1. It is not in seen, so add it. seen is now { 1 }.",
                "Look at 2. Not in seen, so add it. seen is now { 1, 2 }.",
                "Look at 3. Not in seen, so add it. seen is now { 1, 2, 3 }.",
                "Look at the second 1. It IS already in seen - that means this value showed up earlier, so return true immediately without looking at the rest of the array.",
            ),
            pseudocode = "seen = empty set\nfor each value in nums:\n    if value is already in seen:\n        return true\n    add value to seen\nreturn false",
            referenceCode = "fun containsDuplicate(nums: IntArray): Boolean {\n    val seen = mutableSetOf<Int>()\n    for (value in nums) {\n        if (!seen.add(value)) return true\n    }\n    return false\n}",
            timeComplexity = "O(n) on average, where n is how many numbers are in the array.\n\nIn plain terms: the work grows in a straight line with the input size. Twice as many numbers means roughly twice as much work, not four times as much.\n\nThat's because two things stay fast no matter how big the set gets: checking whether a value is already in the set, and adding a new value.",
            spaceComplexity = "O(n) in the worst case, where n is the size of the array.\n\nThe worst case happens when there are no duplicates at all.\n\nThe set can end up holding a copy of every number in the array, one entry per number. So the memory it uses grows at the same rate as the input.",
            alternatives = listOf(
                "Sort a copy of the array, then walk through checking each number against its neighbor.\nThis works and needs only a small amount of extra memory.\nBut sorting takes O(n log n) time, which is slower than the set approach once the array gets large.",
                "Compare every pair of numbers directly, with no extra memory at all.\nThis is the easiest to picture.\nBut it takes O(n squared) time. For 10,000 numbers, that's roughly 100 million comparisons - far too slow.",
            ),
            commonMistakes = listOf(
                "Adding the value to the set before checking whether it's already there.\nThen the check always finds it, because it was just added.\nSo the function reports a duplicate on almost any input.",
                "Clearing or resetting the set partway through the loop.\nThat erases the memory of what has already been seen.\nSo a real duplicate can be missed.",
                "Assuming this uses no memory just because each individual check is fast.\nThe check is fast, but the set still has to store every value it has seen.\nThat storage is where the O(n) memory comes from.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: has this exact value shown up before?",
                "You're looking for a repeated item, ID, or event somewhere in a list.",
                "You'd happily use a bit more memory in exchange for never having to search through everything again.",
            ),
            takeaway = "Whenever the core question is 'have I seen this before?', reach for a set.\nCheck whether the value is already there first, and only add it afterward if it wasn't.",
        ),
        nextSlug = "valid-anagram",
    )

    private val validAnagram = RoadmapLesson(
        slug = "valid-anagram",
        title = "Valid Anagram",
        difficulty = CurriculumDifficulty.EASY,
        description = "Given two strings, decide whether the second is an anagram of the first - built from exactly the same letters, used the same number of times, in any order. Return true if so, false otherwise.",
        constraints = listOf(
            "Both strings may be empty.",
            "Strings contain lowercase English letters only.",
            "If the strings have different lengths, the answer is always false.",
            "Aim for a single pass over each string rather than repeated searching.",
        ),
        examples = listOf(
            LessonExample("s = \"anagram\", t = \"nagaram\"", "true", "Both strings use exactly the same letters the same number of times."),
            LessonExample("s = \"rat\", t = \"car\"", "false", "The letters don't match: rat has no c, and car has no t."),
            LessonExample("s = \"a\", t = \"ab\"", "false", "Different lengths can never be anagrams of each other."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "valid-anagram-pattern-recognition",
                kind = LessonQuestionKind.PATTERN_RECOGNITION,
                prompt = "Given two strings, decide whether the second is an anagram of the first. Which pattern fits?",
                choices = listOf(
                    LessonChoice(
                        "Two pointers walking both strings from opposite ends inward.",
                        false,
                        "Rearranged characters don't line up at mirrored positions the way a palindrome check would need - two pointers from opposite ends has no reason to find matching characters there.",
                    ),
                    LessonChoice(
                        "Binary search each character of one string against the other.",
                        false,
                        "Binary search needs sorted data to search over, and nothing here is sorted going in - counting occurrences directly is simpler and needs no preprocessing.",
                    ),
                    LessonChoice(
                        "Frequency map: count how many times each character appears in each string and compare the counts.",
                        true,
                        "An anagram is just a rearrangement, so what matters is not the order of characters but how many of each one appears - a frequency map captures exactly that and nothing more.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-anagram-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach correctly compares the two strings?",
                choices = listOf(
                    LessonChoice(
                        "Build one count map by incrementing for each character in the first string and decrementing for each character in the second, then check every count is zero.",
                        true,
                        "Incrementing and decrementing the same map means any character used unevenly between the two strings leaves a nonzero count, which is exactly what marks them as not anagrams.",
                    ),
                    LessonChoice(
                        "Sort both strings alphabetically and compare them for equality.",
                        false,
                        "This also works and is a fine alternative, but it costs O(n log n) for the sort where counting characters only costs O(n) - not the best approach when a linear one is available.",
                    ),
                    LessonChoice(
                        "Compare the strings character by character at each matching index.",
                        false,
                        "Anagrams are rearrangements, so the same characters can appear at completely different indices in each string - comparing index by index would reject valid anagrams.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-anagram-state-selection",
                kind = LessonQuestionKind.STATE_SELECTION,
                prompt = "What state is needed to compare the two strings?",
                choices = listOf(
                    LessonChoice(
                        "A single map from character to a running count, shared across both strings.",
                        true,
                        "Sharing one map and incrementing for one string while decrementing for the other means a perfect anagram always nets back to entirely zero counts.",
                    ),
                    LessonChoice(
                        "Two separate sorted copies of the strings.",
                        false,
                        "Sorting works but needs two full copies and a comparison step afterward - a single shared count map reaches the same answer with less bookkeeping.",
                    ),
                    LessonChoice(
                        "A stack of characters from the first string.",
                        false,
                        "A stack tracks order and last-in-first-out access, neither of which matters here - only how many of each character exist matters, not any ordering.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-anagram-boundary-update",
                kind = LessonQuestionKind.BOUNDARY_UPDATE,
                prompt = "Which rule correctly updates the shared count map?",
                choices = listOf(
                    LessonChoice(
                        "Increment the count for characters in both strings, then check whether every count is even.",
                        false,
                        "Checking for even counts doesn't verify the strings use the *same* characters, only that each one repeats an even number of times combined - two completely different strings could still pass.",
                    ),
                    LessonChoice(
                        "Increment the count for each character seen in the first string, decrement for each character seen in the second string.",
                        true,
                        "This nets every character that appears in both strings back toward zero, leaving only characters that are unevenly used between the two strings with a nonzero count.",
                    ),
                    LessonChoice(
                        "Increment the count for the first string, then reset the whole map before processing the second string.",
                        false,
                        "Resetting the map throws away the first string's counts entirely, leaving nothing to compare the second string's counts against.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-anagram-code-block",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three build a 26-letter count array. Which one actually returns the right answer?",
                choices = listOf(
                    LessonChoice(
                        text = "Increments the count for each letter of s, decrements for each letter of t, then checks every count is zero.",
                        correct = true,
                        feedback = "If s and t use exactly the same letters the same number of times, every increment from s is cancelled by a matching decrement from t, leaving all zeros.",
                        code = "fun isAnagram(s: String, t: String): Boolean {\n    if (s.length != t.length) return false\n    val counts = IntArray(26)\n    for (c in s) counts[c - 'a']++\n    for (c in t) counts[c - 'a']--\n    return counts.all { it == 0 }\n}",
                    ),
                    LessonChoice(
                        text = "Increments the count for each letter of s and, separately, for each letter of t, then checks every count is zero.",
                        correct = false,
                        feedback = "Incrementing for both strings instead of decrementing for one means the counts only reach zero when both strings are empty - this rejects almost every real anagram.",
                        code = "fun isAnagram(s: String, t: String): Boolean {\n    if (s.length != t.length) return false\n    val counts = IntArray(26)\n    for (c in s) counts[c - 'a']++\n    for (c in t) counts[c - 'a']++\n    return counts.all { it == 0 }\n}",
                    ),
                    LessonChoice(
                        text = "Increments the count for each letter of s, decrements for each letter of t, then checks that at least one count is zero.",
                        correct = false,
                        feedback = "Requiring only one zero count instead of all of them accepts strings that share just a single letter's frequency - far too weak a check.",
                        code = "fun isAnagram(s: String, t: String): Boolean {\n    if (s.length != t.length) return false\n    val counts = IntArray(26)\n    for (c in s) counts[c - 'a']++\n    for (c in t) counts[c - 'a']--\n    return counts.any { it == 0 }\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-anagram-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the length of s (equal to t after the length check), what is the time complexity?",
                choices = listOf(
                    LessonChoice(
                        "O(n log n), because the counts must be sorted before comparing.",
                        false,
                        "Nothing here gets sorted - the counts are just 26 fixed slots read and written directly by letter.",
                    ),
                    LessonChoice(
                        "O(n), because each string is scanned once and each array update is O(1).",
                        true,
                        "One pass over s, one pass over t, and one pass over the fixed 26-slot array to check for zeros - all linear in n.",
                    ),
                    LessonChoice(
                        "O(26), because the array only ever has 26 slots.",
                        false,
                        "The array size is fixed, but the work to fill it still depends on how long s and t are - that dependency on n is exactly what O(n) captures.",
                    ),
                ),
                glossary = listOf(
                    GlossaryTerm("O(n)", "Linear time - work grows in direct proportion to the input size."),
                    GlossaryTerm("O(n log n)", "A growth rate a bit worse than linear, typical of sorting."),
                    GlossaryTerm("O(1)", "Constant time - the same small amount of work regardless of size."),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Imagine two jars of Scrabble tiles.\n\nTo know if they contain exactly the same tiles, you don't need to line the tiles up in order - you just need to count how many of each letter is in each jar and compare the counts.\n\nThat's exactly what an anagram check does: it ignores the order of the letters entirely and only cares about how many of each letter shows up.",
            walkthrough = listOf(
                "Check lengths first: \"anagram\" and \"nagaram\" both have 7 letters, so continue.",
                "Scan s = \"anagram\", incrementing a count for each letter: a appears 3 times, n 1, g 1, r 1, m 1.",
                "Scan t = \"nagaram\", decrementing the same counts: n, a, g, a, r, a, m each cancel out one increment from s.",
                "After both scans, every letter's count is back to zero, so the two strings used exactly the same letters the same number of times.",
                "Return true.",
            ),
            pseudocode = "if length of s != length of t: return false\ncounts = array of 26 zeros\nfor each letter in s: counts[letter] += 1\nfor each letter in t: counts[letter] -= 1\nreturn true if every count is 0, else false",
            referenceCode = "fun isAnagram(s: String, t: String): Boolean {\n    if (s.length != t.length) return false\n    val counts = IntArray(26)\n    for (c in s) counts[c - 'a']++\n    for (c in t) counts[c - 'a']--\n    return counts.all { it == 0 }\n}",
            timeComplexity = "O(n), where n is the length of the strings.\n\nEach string is scanned exactly once, and updating a count in the 26-slot array is a constant-time operation no matter how big n gets.\n\nSo doubling the length of the strings roughly doubles the work - a straight line, not a curve.",
            spaceComplexity = "O(1) extra space, because the count array always has exactly 26 slots - one per lowercase letter.\n\nIt does not matter whether the strings are 5 letters long or 5 million letters long: the array never grows past 26 entries.",
            alternatives = listOf(
                "Sort both strings and compare them for equality.\nThis is simple to reason about and definitely correct.\nBut sorting costs O(n log n), which is slower than counting once you don't need the letters in order.",
                "Use a general hash map from character to count instead of a fixed 26-slot array.\nThis generalizes to any alphabet, including uppercase letters or Unicode.\nBut for a lowercase-only problem, it adds hash map overhead for no real benefit over a plain array.",
            ),
            commonMistakes = listOf(
                "Forgetting the length check.\nWithout it, the counting logic alone isn't a fully reliable anagram test.\nA missing length check can let subtly wrong answers slip through in unusual edge cases.",
                "Only incrementing counts for one string and never decrementing for the other.\nThat just tells you how many of each letter s has - it never actually compares s against t.\nThe subtraction is what turns two separate counts into one shared answer.",
                "Assuming case matters when it doesn't, or vice versa, without checking the problem's exact rules.\nThis problem guarantees lowercase-only input, so 'a' - 'a' safely maps to index 0.\nA different problem with mixed case would need an explicit decision about whether case counts as a match.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: do these two collections contain the same items, the same number of times, regardless of order?",
                "The alphabet or set of possible values is small and fixed, which hints that a fixed-size count array can replace a general hash map.",
                "Order doesn't matter to the answer, only frequency does.",
            ),
            takeaway = "When two things only need to match by frequency, not by order, count instead of sort.\nA fixed-size count array is faster than sorting and needs no extra memory that grows with the input.",
        ),
        nextSlug = "two-sum",
    )

    private val twoSum = RoadmapLesson(
        slug = "two-sum",
        title = "Two Sum",
        difficulty = CurriculumDifficulty.EASY,
        description = "Given an array of integers and a target value, return the indices of the two numbers that add up to the target. Assume exactly one valid pair exists, and the same element cannot be used twice.",
        constraints = listOf(
            "The array has at least two elements.",
            "Exactly one pair of indices adds up to the target.",
            "The same index cannot be used twice, though the same value can appear at two different indices.",
            "Aim for a single pass through the array rather than checking every pair.",
        ),
        examples = listOf(
            LessonExample("nums = [2, 7, 11, 15], target = 9", "[0, 1]", "nums[0] + nums[1] = 2 + 7 = 9."),
            LessonExample("nums = [3, 2, 4], target = 6", "[1, 2]", "nums[1] + nums[2] = 2 + 4 = 6."),
            LessonExample("nums = [3, 3], target = 6", "[0, 1]", "The same value at two different positions can still be the answer."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "two-sum-pattern-recognition",
                kind = LessonQuestionKind.PATTERN_RECOGNITION,
                prompt = "Given an array and a target, find the indices of two numbers that add up to target. Which pattern fits?",
                choices = listOf(
                    LessonChoice(
                        "Sort the array first, then use two pointers from both ends.",
                        false,
                        "Sorting scrambles the original indices, but the answer needs the original positions of the two numbers - a hash map avoids that problem entirely by keeping value-to-index lookups intact.",
                    ),
                    LessonChoice(
                        "Check every pair of numbers with two nested loops.",
                        false,
                        "This finds the right answer but costs O(n squared), far more than the O(n) a hash map achieves by looking up complements instead of comparing every pair.",
                    ),
                    LessonChoice(
                        "Hash map from value to index: for each number, check whether target minus that number was already seen.",
                        true,
                        "Looking up whether the complement was already seen is a constant-time operation with a map, turning what would be a nested search into a single pass.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach correctly finds the pair in one pass?",
                choices = listOf(
                    LessonChoice(
                        "For each number, check whether its complement (target minus the number) is already in the map; if not, add the current number and its index to the map.",
                        true,
                        "Checking for the complement before inserting the current number ensures a single element is never paired with itself, while still finding pairs formed by any two distinct positions.",
                    ),
                    LessonChoice(
                        "Add every number and its index to the map first, then make a second pass checking for each number's complement.",
                        false,
                        "This also works correctly, but it needs two full passes over the array where checking for the complement before inserting achieves the same result in just one.",
                    ),
                    LessonChoice(
                        "For each number, check whether the number itself (not its complement) is already in the map.",
                        false,
                        "Checking for the number itself instead of target minus the number would only ever find exact duplicates, not the two different values that actually sum to target in the general case.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-state-selection",
                kind = LessonQuestionKind.STATE_SELECTION,
                prompt = "What state does this approach need?",
                choices = listOf(
                    LessonChoice(
                        "A hash map from each number's value to its index.",
                        true,
                        "Storing the index alongside the value is what lets the final answer report which two positions were used, not just which two values.",
                    ),
                    LessonChoice(
                        "A hash set of every number seen so far, without indices.",
                        false,
                        "A set alone can confirm a matching value exists but can't report which position it came from, and the problem asks for indices, not just values.",
                    ),
                    LessonChoice(
                        "A sorted copy of the array alongside the original.",
                        false,
                        "A sorted copy isn't needed - the hash map approach never requires the array to be in any particular order to find complements in constant time.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-boundary-update",
                kind = LessonQuestionKind.BOUNDARY_UPDATE,
                prompt = "Which rule correctly checks and updates the map at each number?",
                choices = listOf(
                    LessonChoice(
                        "Insert nums[i] with index i into the map first, then check whether target - nums[i] exists in the map.",
                        false,
                        "Inserting before checking means a number can find itself as its own complement whenever target is exactly double that number, incorrectly pairing an index with itself.",
                    ),
                    LessonChoice(
                        "Check whether target - nums[i] exists in the map first; if it does, return the pair; if not, insert nums[i] with index i into the map, then continue.",
                        true,
                        "Checking before inserting guarantees the complement found, if any, was placed there by an earlier, different index, so the same element is never used twice.",
                    ),
                    LessonChoice(
                        "Check whether nums[i] exists in the map, and if so, insert target - nums[i] instead.",
                        false,
                        "This checks for the wrong value entirely - it should be looking up the complement that would pair with the current number, not the current number itself.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-code-block",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use a map from value to index. Which one returns the right pair?",
                choices = listOf(
                    LessonChoice(
                        text = "Checks whether target minus the current value is already in the map before adding the current value.",
                        correct = true,
                        feedback = "Checking for the complement before inserting the current value guarantees the two indices found are always distinct positions.",
                        code = "fun twoSum(nums: IntArray, target: Int): IntArray {\n    val seen = mutableMapOf<Int, Int>()\n    for (i in nums.indices) {\n        val need = target - nums[i]\n        seen[need]?.let { return intArrayOf(it, i) }\n        seen[nums[i]] = i\n    }\n    return intArrayOf()\n}",
                    ),
                    LessonChoice(
                        text = "Adds the current value to the map before checking for its complement.",
                        correct = false,
                        feedback = "Inserting first means a value exactly half of target can match itself, returning the same index twice instead of two distinct positions.",
                        code = "fun twoSum(nums: IntArray, target: Int): IntArray {\n    val seen = mutableMapOf<Int, Int>()\n    for (i in nums.indices) {\n        seen[nums[i]] = i\n        val need = target - nums[i]\n        seen[need]?.let { return intArrayOf(it, i) }\n    }\n    return intArrayOf()\n}",
                    ),
                    LessonChoice(
                        text = "Checks whether the current value itself is already in the map, instead of its complement.",
                        correct = false,
                        feedback = "Looking up nums[i] instead of target - nums[i] only finds a repeated value, not a pair that actually sums to the target.",
                        code = "fun twoSum(nums: IntArray, target: Int): IntArray {\n    val seen = mutableMapOf<Int, Int>()\n    for (i in nums.indices) {\n        seen[nums[i]]?.let { return intArrayOf(it, i) }\n        seen[nums[i]] = i\n    }\n    return intArrayOf()\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of values in the array, what is the time complexity of the single-pass, complement-lookup approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n log n), because the values must be sorted before searching for a pair.",
                        false,
                        "Nothing here gets sorted - each value is looked up and inserted directly by its own value as a key.",
                    ),
                    LessonChoice(
                        "O(n) on average, because each value does one average constant-time map lookup and one average constant-time insert.",
                        true,
                        "One pass through the array, with average O(1) work per value for both the lookup and the insert, gives average O(n) overall.",
                    ),
                    LessonChoice(
                        "O(n squared), because every value must be compared against every other value.",
                        false,
                        "That describes the brute-force pair-checking approach - the map lookup replaces those repeated comparisons with a single average-constant-time check.",
                    ),
                ),
                glossary = listOf(
                    GlossaryTerm("O(n)", "Linear time - work grows in direct proportion to the input size."),
                    GlossaryTerm("O(n log n)", "A growth rate a bit worse than linear, typical of sorting."),
                    GlossaryTerm("O(n squared)", "A growth rate proportional to the square of the input size."),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Imagine going through a guest list looking for two people whose ages add up to exactly 30.\n\nInstead of comparing every guest to every other guest, you could keep a notebook: for each guest, write down 'I need someone who is X years old' where X completes the pair.\n\nThe next time you see a guest whose age matches something already written down, you've found your pair - without ever comparing everyone to everyone.",
            walkthrough = listOf(
                "seen starts as an empty map.",
                "i = 0, nums[0] = 2. Need 9 - 2 = 7. seen has no 7, so remember seen = {2: 0}.",
                "i = 1, nums[1] = 7. Need 9 - 7 = 2. seen has 2 at index 0!",
                "Return [0, 1] - the value at index 0 (2) and the value at index 1 (7) sum to 9.",
            ),
            pseudocode = "seen = empty map from value to index\nfor i in indices of nums:\n    need = target - nums[i]\n    if need is in seen:\n        return [seen[need], i]\n    seen[nums[i]] = i\nreturn no answer found",
            referenceCode = "fun twoSum(nums: IntArray, target: Int): IntArray {\n    val seen = mutableMapOf<Int, Int>()\n    for (i in nums.indices) {\n        val need = target - nums[i]\n        seen[need]?.let { return intArrayOf(it, i) }\n        seen[nums[i]] = i\n    }\n    return intArrayOf()\n}",
            timeComplexity = "O(n) on average, where n is the number of values in the array.\n\nEach value is visited exactly once, and checking or inserting into the map takes roughly constant time each time, no matter how many values have been stored so far.\n\nOne pass, constant work per step - that's a straight line, not a curve.",
            spaceComplexity = "O(n) in the worst case, where n is the number of values in the array.\n\nThe map can end up holding almost every value from the array if the two matching values happen to be near the end.\n\nSo the extra memory used grows at roughly the same rate as how far through the array the search has to go.",
            alternatives = listOf(
                "Check every pair of values directly, with no extra memory at all.\nThis is the simplest to picture and needs no map.\nBut it costs O(n squared) time - for 10,000 values that's roughly 50 million comparisons, far too slow for large inputs.",
                "Sort the array first, then use two pointers moving in from both ends toward the middle.\nThis avoids the extra memory a map needs.\nBut sorting takes O(n log n) time and also destroys the original index order, which this problem specifically asks for - the indices would need to be tracked separately before sorting.",
            ),
            commonMistakes = listOf(
                "Inserting the current value into the map before checking for its complement.\nThis lets a value that is exactly half the target match against itself.\nThe result is a single index returned twice instead of two distinct positions.",
                "Looking up the current value itself instead of its complement (target minus the value).\nThat only detects a repeated value, not a pair that actually sums to the target.\nMost inputs would then be reported as having no answer, or the wrong pair would be returned.",
                "Treating values as if they must be indexed by uniqueness, discarding repeats.\nThe same value can legitimately appear at two different indices and still be the answer.\nThe map should always be keyed so a repeated value simply overwrites its stored index, never blocked from being stored at all.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: which two items, added or combined together, hit an exact target?",
                "You need the positions or identities of the matching items, not just whether a match exists.",
                "You'd rather remember what you've seen than compare everything against everything.",
            ),
            takeaway = "When searching for a pair that satisfies some target condition, check for the complement first and record the current value second - that ordering is what keeps a single pass correct.",
        ),
        nextSlug = "group-anagrams",
    )

    private val groupAnagrams = RoadmapLesson(
        slug = "group-anagrams",
        title = "Group Anagrams",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an array of strings, group the strings that are anagrams of each other into the same group. Return the groups in any order, with the strings inside each group in any order.",
        constraints = listOf(
            "The array may contain up to 10,000 strings.",
            "Each string contains only lowercase English letters.",
            "A string with no anagrams among the others still forms its own group of one.",
            "Aim for roughly linear work per string rather than comparing every pair of strings.",
        ),
        examples = listOf(
            LessonExample("[\"eat\",\"tea\",\"tan\",\"ate\",\"nat\",\"bat\"]", "[[\"bat\"],[\"nat\",\"tan\"],[\"ate\",\"eat\",\"tea\"]]", "\"eat\", \"tea\", and \"ate\" all use the same letters; so do \"tan\" and \"nat\"; \"bat\" matches nothing else."),
            LessonExample("[\"\"]", "[[\"\"]]", "A single empty string forms its own group."),
            LessonExample("[\"a\"]", "[[\"a\"]]", "A single string with no match still forms a group of one."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "group-anagrams-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach groups the strings without comparing every string to every other string?",
                choices = listOf(
                    LessonChoice(
                        "For each string, compare it against every other ungrouped string to check if they're anagrams.",
                        false,
                        "Checking each string against every other string is correct but costs O(n squared) comparisons for n strings - slow once there are many strings.",
                    ),
                    LessonChoice(
                        "For each string, compute a signature that's identical for anagrams (like its sorted letters), and group strings that share a signature.",
                        true,
                        "Anagrams always produce the same sorted signature, so grouping by that signature sorts strings into the right buckets in roughly one pass.",
                    ),
                    LessonChoice(
                        "Sort the entire array of strings alphabetically, then group adjacent identical strings.",
                        false,
                        "Sorting the strings themselves only groups strings that are already identical - anagrams like 'eat' and 'tea' are different strings and won't end up adjacent just from an alphabetical sort.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "group-anagrams-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three group strings by a signature. Which one uses a signature that actually identifies anagrams?",
                choices = listOf(
                    LessonChoice(
                        text = "Groups by the string's own first character.",
                        correct = false,
                        feedback = "The first character alone doesn't determine which letters follow, so 'ate' and 'ant' would be grouped together despite not being anagrams.",
                        code = "fun groupAnagrams(strs: Array<String>): List<List<String>> {\n    val groups = mutableMapOf<Char, MutableList<String>>()\n    for (s in strs) {\n        val key = s.firstOrNull() ?: ' '\n        groups.getOrPut(key) { mutableListOf() }.add(s)\n    }\n    return groups.values.toList()\n}",
                    ),
                    LessonChoice(
                        text = "Groups by the string's length.",
                        correct = false,
                        feedback = "Same-length strings are not necessarily anagrams - 'eat' and 'ant' are both 3 letters but use different letters entirely.",
                        code = "fun groupAnagrams(strs: Array<String>): List<List<String>> {\n    val groups = mutableMapOf<Int, MutableList<String>>()\n    for (s in strs) {\n        groups.getOrPut(s.length) { mutableListOf() }.add(s)\n    }\n    return groups.values.toList()\n}",
                    ),
                    LessonChoice(
                        text = "Groups by the string's letters sorted into order.",
                        correct = true,
                        feedback = "Sorting a string's own letters produces the same result for every anagram of it - 'eat', 'tea', and 'ate' all sort to 'aet'.",
                        code = "fun groupAnagrams(strs: Array<String>): List<List<String>> {\n    val groups = mutableMapOf<String, MutableList<String>>()\n    for (s in strs) {\n        val key = s.toCharArray().sorted().joinToString(\"\")\n        groups.getOrPut(key) { mutableListOf() }.add(s)\n    }\n    return groups.values.toList()\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "group-anagrams-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version puts every string into its own separate group, even true anagrams. What's the bug?",
                code = "for (s in strs) {\n    val key = s\n    groups.getOrPut(key) { mutableListOf() }.add(s)\n}",
                choices = listOf(
                    LessonChoice(
                        "Change val key = s to val key = s.toCharArray().sorted().joinToString(\"\").",
                        true,
                        "Using the string itself as the key means only identical strings share a group - the key needs to be something anagrams have in common, like their sorted letters.",
                    ),
                    LessonChoice(
                        "Change groups.getOrPut(key) to groups.get(key).",
                        false,
                        "get() would return null for a key that hasn't been seen yet, crashing on the very first string of any new group - getOrPut is needed to create the list on first use.",
                    ),
                    LessonChoice(
                        "Change .add(s) to .add(key).",
                        false,
                        "Adding the key instead of the original string s would store sorted, mangled versions of the words instead of the actual input strings.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "group-anagrams-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n strings and k as the length of the longest string, what is the time complexity of the sorted-signature approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because each string only needs to be looked at once.",
                        false,
                        "Each string is looked at once, but computing its sorted signature isn't free - sorting a string of length k costs O(k log k), and that has to be accounted for.",
                    ),
                    LessonChoice(
                        "O(n * k log k), because each of the n strings has its letters sorted, and sorting k letters costs O(k log k).",
                        true,
                        "Building the signature for every string means paying the sorting cost k log k, once per string, n times total.",
                    ),
                    LessonChoice(
                        "O(n squared), because every string is compared against every other string.",
                        false,
                        "The signature approach never directly compares strings to each other - it only compares each string's signature to a map key, replacing all those comparisons with map lookups.",
                    ),
                ),
                glossary = listOf(
                    GlossaryTerm("O(n * k log k)", "n times the cost of sorting one string's k letters - grows faster than n alone, but far slower-growing than comparing every string to every other string."),
                ),
            ),
            LessonQuestion(
                id = "group-anagrams-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the map of signatures to groups use in the worst case?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only one map is created.",
                        false,
                        "Creating one map doesn't limit how much it can hold - in the worst case it stores a copy of essentially every input string.",
                    ),
                    LessonChoice(
                        "O(k), because each signature is only k letters long.",
                        false,
                        "A single signature is about k letters, but the map holds a signature and a full string for every one of the n input strings, not just one.",
                    ),
                    LessonChoice(
                        "O(n * k), because in the worst case, every string ends up stored somewhere in the map, and each string can be up to length k.",
                        true,
                        "If no two strings are anagrams, every one of the n strings gets its own group, and all of them together take roughly n times k space to store.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Think of sorting a pile of mismatched socks into matching pairs, except instead of matching by color you match by which letters are inside each word.\n\nTwo words are anagrams exactly when rearranging their letters into alphabetical order produces the same result.\n\nSo instead of comparing every word to every other word, give each word a 'label' - its letters sorted alphabetically - and words with the same label belong in the same pile.",
            walkthrough = listOf(
                "Take \"eat\". Sort its letters: a, e, t -> \"aet\". Create a new group under key \"aet\" containing [\"eat\"].",
                "Take \"tea\". Sort its letters: a, e, t -> \"aet\". This key already exists, so add \"tea\" to that group: [\"eat\", \"tea\"].",
                "Take \"tan\". Sort its letters: a, n, t -> \"ant\". Create a new group under key \"ant\" containing [\"tan\"].",
                "Take \"ate\". Sort its letters: a, e, t -> \"aet\". Add to the existing group: [\"eat\", \"tea\", \"ate\"].",
                "Continue for \"nat\" (key \"ant\", joins [\"tan\"]) and \"bat\" (key \"abt\", its own new group).",
                "Return every group's values: [\"bat\"], [\"tan\", \"nat\"], and [\"eat\", \"tea\", \"ate\"].",
            ),
            pseudocode = "groups = empty map from signature to list of strings\nfor each string s in strs:\n    key = letters of s sorted alphabetically, joined back into a string\n    add s to groups[key] (creating an empty list first if needed)\nreturn all the lists stored in groups",
            referenceCode = "fun groupAnagrams(strs: Array<String>): List<List<String>> {\n    val groups = mutableMapOf<String, MutableList<String>>()\n    for (s in strs) {\n        val key = s.toCharArray().sorted().joinToString(\"\")\n        groups.getOrPut(key) { mutableListOf() }.add(s)\n    }\n    return groups.values.toList()\n}",
            timeComplexity = "O(n * k log k), where n is the number of strings and k is the length of the longest one.\n\nEvery one of the n strings gets its letters sorted to build its signature, and sorting k letters costs O(k log k).\n\nDo that n times, once per string, and the total work is n times k log k.",
            spaceComplexity = "O(n * k) in the worst case, where n is the number of strings and k is the length of the longest one.\n\nIf none of the strings are anagrams of each other, every single one ends up stored in its own group.\n\nStoring roughly n strings of up to length k each takes space proportional to n times k.",
            alternatives = listOf(
                "Compare every string against every other string directly to check if they're anagrams.\nThis needs no extra signature or map at all.\nBut comparing n strings pairwise costs O(n squared) comparisons, which becomes very slow once there are thousands of strings.",
                "Use a 26-number letter-count array as the signature instead of a sorted string.\nThis avoids the sorting cost entirely, computing each signature in O(k) instead of O(k log k).\nBut turning that count array into something usable as a map key (like joining it into a string) adds its own overhead, so the benefit only shows up for longer strings.",
            ),
            commonMistakes = listOf(
                "Using the original string itself as the map key instead of a shared signature.\nThat only groups strings that are already character-for-character identical.\nTrue anagrams like 'eat' and 'tea' would end up in completely separate groups.",
                "Building the signature from something that doesn't uniquely represent letter frequency, like just the string's length or first letter.\nDifferent words can easily share a length or a first letter without being anagrams.\nOnly something that fully captures which letters appear, and how many times, reliably identifies anagrams.",
                "Forgetting that a string with no anagrams among the others still needs its own group.\nSkipping strings that don't match anything would silently drop input from the result.\nEvery string belongs in exactly one group, even if that group only ever has one member.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: which items are 'the same' under some looser rule than exact equality?",
                "You need to bucket many items into groups based on a shared property, not just find one match.",
                "A cheap-to-compute 'fingerprint' of each item would tell you which bucket it belongs in.",
            ),
            takeaway = "When you need to group items by an underlying property rather than exact equality, compute a signature that's identical for everything in the same group, and use it as a map key instead of comparing items pairwise.",
        ),
        nextSlug = "top-k-frequent-elements",
    )

    private val topKFrequentElements = RoadmapLesson(
        slug = "top-k-frequent-elements",
        title = "Top K Frequent Elements",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an array of integers and an integer k, return the k most frequently occurring values, in any order.",
        constraints = listOf(
            "1 <= k <= number of distinct values in the array.",
            "The answer - the set of k most frequent values - is always unique.",
            "The array can contain up to 100,000 values.",
            "Aim for better than sorting every distinct value by frequency.",
        ),
        examples = listOf(
            LessonExample("nums = [1,1,1,2,2,3], k = 2", "[1, 2]", "1 appears 3 times and 2 appears 2 times - the two most frequent values."),
            LessonExample("nums = [1], k = 1", "[1]", "With only one distinct value, it's automatically the most frequent."),
            LessonExample("nums = [4,4,4,6,6,7], k = 1", "[4]", "4 appears more often (3 times) than 6 (2 times) or 7 (1 time)."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "top-k-frequent-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the k most frequent values without fully sorting every distinct value by frequency?",
                choices = listOf(
                    LessonChoice(
                        "Count how often each value appears, then bucket values by their count, and read off values starting from the highest-count bucket.",
                        true,
                        "Because a value can appear at most n times, there are at most n buckets - reading the top buckets first finds the k most frequent values without comparing every value's count to every other's.",
                    ),
                    LessonChoice(
                        "Count how often each value appears, then sort all distinct values by count from highest to lowest and take the first k.",
                        false,
                        "This works, but sorting every distinct value by count costs O(m log m) for m distinct values - more work than a fixed-size bucket approach needs.",
                    ),
                    LessonChoice(
                        "For each distinct value, scan the entire array again to count how many times it appears.",
                        false,
                        "Rescanning the whole array once per distinct value can cost O(n squared) in the worst case, when there are many distinct values.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "top-k-frequent-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three build count buckets indexed by frequency. Which one collects the top k values correctly?",
                choices = listOf(
                    LessonChoice(
                        text = "Reads buckets starting from index 0 upward, stopping once k values are collected.",
                        correct = false,
                        feedback = "Bucket 0 (or the lowest indices) holds the least frequent values - reading from the bottom up collects the least frequent values first, the opposite of what's needed.",
                        code = "fun topKFrequent(nums: IntArray, k: Int): IntArray {\n    val counts = nums.groupingBy { it }.eachCount()\n    val buckets = Array(nums.size + 1) { mutableListOf<Int>() }\n    for ((value, count) in counts) buckets[count].add(value)\n    val result = mutableListOf<Int>()\n    for (bucket in buckets) {\n        for (value in bucket) {\n            if (result.size == k) return result.toIntArray()\n            result.add(value)\n        }\n    }\n    return result.toIntArray()\n}",
                    ),
                    LessonChoice(
                        text = "Reads buckets starting from the highest index (most frequent) downward, stopping once k values are collected.",
                        correct = true,
                        feedback = "The highest-index buckets hold the most frequent values, so reading from the top down naturally collects the most frequent values first.",
                        code = "fun topKFrequent(nums: IntArray, k: Int): IntArray {\n    val counts = nums.groupingBy { it }.eachCount()\n    val buckets = Array(nums.size + 1) { mutableListOf<Int>() }\n    for ((value, count) in counts) buckets[count].add(value)\n    val result = mutableListOf<Int>()\n    for (bucket in buckets.reversed()) {\n        for (value in bucket) {\n            if (result.size == k) return result.toIntArray()\n            result.add(value)\n        }\n    }\n    return result.toIntArray()\n}",
                    ),
                    LessonChoice(
                        text = "Places each value into bucket[value] instead of bucket[count].",
                        correct = false,
                        feedback = "Indexing buckets by the value itself instead of by how often it occurs loses the frequency information entirely - the buckets would no longer represent 'how many times seen'.",
                        code = "fun topKFrequent(nums: IntArray, k: Int): IntArray {\n    val counts = nums.groupingBy { it }.eachCount()\n    val buckets = Array(nums.size + 1) { mutableListOf<Int>() }\n    for ((value, count) in counts) buckets[value % buckets.size].add(value)\n    val result = mutableListOf<Int>()\n    for (bucket in buckets.reversed()) {\n        for (value in bucket) {\n            if (result.size == k) return result.toIntArray()\n            result.add(value)\n        }\n    }\n    return result.toIntArray()\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "top-k-frequent-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version throws an index-out-of-bounds error on some inputs. What's the bug?",
                code = "val counts = nums.groupingBy { it }.eachCount()\nval buckets = Array(nums.size) { mutableListOf<Int>() }\nfor ((value, count) in counts) buckets[count].add(value)",
                choices = listOf(
                    LessonChoice(
                        "Change nums.groupingBy { it }.eachCount() to nums.groupingBy { it }.eachCount().toMap().",
                        false,
                        "eachCount() already returns a Map, so this change doesn't affect the indexing bug at all.",
                    ),
                    LessonChoice(
                        "Change buckets[count] to buckets[count - 1].",
                        false,
                        "Shifting every index down by one doesn't fix the root problem - a value that appears nums.size times still needs a slot that this array is one short of providing.",
                    ),
                    LessonChoice(
                        "Change Array(nums.size) to Array(nums.size + 1).",
                        true,
                        "A value can appear as many as nums.size times, which needs bucket index nums.size to exist - an array sized nums.size only has valid indices up to nums.size - 1.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "top-k-frequent-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of values in the array, what is the time complexity of the bucket approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because counting is one pass, bucketing is one pass, and reading off the top k values visits at most n bucket entries total.",
                        true,
                        "Every step - counting, placing into buckets, and reading buckets from the top - touches each value or each bucket slot a constant number of times, so the total work stays proportional to n.",
                    ),
                    LessonChoice(
                        "O(n log n), because the distinct values must be sorted by frequency.",
                        false,
                        "Bucketing by frequency avoids sorting entirely - values with the same count just land in the same bucket without ever being compared to each other.",
                    ),
                    LessonChoice(
                        "O(k), because only the top k values are returned.",
                        false,
                        "The output has k values, but building the buckets in the first place still requires looking at every one of the n input values first.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "top-k-frequent-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space do the count map and the bucket array use together, in terms of n?",
                choices = listOf(
                    LessonChoice(
                        "O(k), because only k values are ultimately returned.",
                        false,
                        "The final answer has k values, but the buckets array has one slot for every possible count from 0 to n, and the count map can hold up to n distinct values - both scale with n, not k.",
                    ),
                    LessonChoice(
                        "O(n), because the bucket array has n + 1 slots and the count map holds at most n distinct values.",
                        true,
                        "Both structures are sized relative to how many values are in the input, not how many are returned, so they scale with n.",
                    ),
                    LessonChoice(
                        "O(1), because there are always exactly two data structures, a map and an array.",
                        false,
                        "Having a fixed number of data structures doesn't mean they stay a fixed size - both the map and the array can grow as large as n.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture a row of empty bins on a shelf, one bin for 'appeared once', one for 'appeared twice', all the way up to 'appeared n times'.\n\nFirst, count how many times each value shows up in the array.\n\nThen drop each value into the bin matching its count.\n\nThe most frequent values end up in the bins furthest along the shelf - just walk backward from the end, collecting values, until k of them have been gathered.",
            walkthrough = listOf(
                "Count occurrences: 1 appears 3 times, 2 appears 2 times, 3 appears 1 time.",
                "Create buckets indexed 0 through 6 (array length is 6, so bucket indices run 0..6).",
                "Place 1 into bucket[3], place 2 into bucket[2], place 3 into bucket[1].",
                "Walk backward from bucket[6] toward bucket[0], collecting values: buckets 6, 5, and 4 are empty; bucket[3] has [1] - collect it.",
                "Continue to bucket[2], which has [2] - collect it. Now 2 values have been collected and k = 2, so stop.",
                "Return [1, 2].",
            ),
            pseudocode = "counts = map of value to how many times it appears in nums\nbuckets = array of nums.size + 1 empty lists\nfor each (value, count) in counts:\n    add value to buckets[count]\nresult = empty list\nfor bucket index from highest down to 0:\n    for value in buckets[bucket index]:\n        add value to result\n        if result has k values: return result",
            referenceCode = "fun topKFrequent(nums: IntArray, k: Int): IntArray {\n    val counts = nums.groupingBy { it }.eachCount()\n    val buckets = Array(nums.size + 1) { mutableListOf<Int>() }\n    for ((value, count) in counts) buckets[count].add(value)\n    val result = mutableListOf<Int>()\n    for (bucket in buckets.reversed()) {\n        for (value in bucket) {\n            if (result.size == k) return result.toIntArray()\n            result.add(value)\n        }\n    }\n    return result.toIntArray()\n}",
            timeComplexity = "O(n), where n is the number of values in the array.\n\nCounting occurrences is one pass over the array, placing values into buckets is one pass over the distinct values, and reading off the top buckets visits at most n bucket slots in total.\n\nEach of those steps does a fixed, small amount of work per value, so the total stays proportional to n.",
            spaceComplexity = "O(n), where n is the number of values in the array.\n\nThe count map can hold up to n distinct values, and the bucket array always has n + 1 slots, one for every possible frequency from 0 up to n.\n\nBoth of these grow with the size of the input, not with k.",
            alternatives = listOf(
                "Sort all distinct values by how often they occur, then take the first k.\nThis is simple to write and easy to reason about.\nBut sorting m distinct values costs O(m log m), which is slower than bucket counting once there are many distinct values.",
                "Use a heap that always keeps the k most frequent values seen so far.\nThis can be more memory-efficient when k is much smaller than the number of distinct values, since the heap never grows past size k.\nBut it costs O(n log k) for the heap operations, which is slower than the O(n) bucket approach for most inputs.",
            ),
            commonMistakes = listOf(
                "Sizing the bucket array to exactly the number of distinct values instead of the maximum possible count.\nA single value can appear as many as n times if the whole array is that one value.\nThe bucket array needs n + 1 slots (0 through n) to safely hold every possible frequency.",
                "Reading the buckets from lowest frequency to highest instead of highest to lowest.\nThat collects the least frequent values first, which is the opposite of what 'top k' asks for.\nThe buckets must be walked from the highest index down to correctly prioritize the most frequent values.",
                "Forgetting to stop once k values have been collected.\nWithout that check, every distinct value would end up in the result instead of just the top k.\nThe loop needs an explicit check after adding each value to stop as soon as the result reaches size k.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: which items occur most (or least) often, without needing every item ranked?",
                "The possible counts are naturally bounded (here, by the array's own length), which hints that buckets indexed by count can replace a full sort.",
                "You only need the top few results, not a complete ordering of everything.",
            ),
            takeaway = "When you need the top (or bottom) k items by frequency and the range of possible frequencies is bounded, bucket by frequency instead of sorting - it turns an O(n log n) sort into an O(n) pass.",
        ),
        nextSlug = "encode-and-decode-strings",
    )

    private val encodeAndDecodeStrings = RoadmapLesson(
        slug = "encode-and-decode-strings",
        title = "Encode and Decode Strings",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Design a way to encode a list of strings into a single string, and decode that single string back into the exact original list of strings. The strings can contain any characters, including whatever delimiter you choose to use.",
        constraints = listOf(
            "Strings in the list may contain any character, including digits, punctuation, or the delimiter you pick.",
            "The list may be empty, and individual strings within it may be empty.",
            "Decoding the encoded string must reproduce the exact original list, in the same order.",
            "Aim for encoding and decoding that each take time proportional to the total length of the strings.",
        ),
        examples = listOf(
            LessonExample("[\"hello\",\"world\"]", "\"5#hello5#world\"", "Each string is prefixed with its length and a delimiter, so the decoder knows exactly how many characters to read next."),
            LessonExample("[\"\"]", "\"0#\"", "An empty string is still encoded with its length (0) and the delimiter."),
            LessonExample("[]", "\"\"", "An empty list encodes to an empty string, with nothing to decode back."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "encode-decode-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach reliably decodes back to the original list, even when strings contain unusual characters?",
                choices = listOf(
                    LessonChoice(
                        "Join the strings with a comma between them, then split on commas to decode.",
                        false,
                        "If any string itself contains a comma, splitting on commas would incorrectly break that string into two pieces during decoding.",
                    ),
                    LessonChoice(
                        "Join the strings with a rare character like '#' between them, then split on '#' to decode.",
                        false,
                        "This fails the same way a comma would if any string happens to contain that same rare character - no single delimiter character is guaranteed safe.",
                    ),
                    LessonChoice(
                        "Prefix each string with its length and a delimiter, so the decoder always knows exactly how many characters belong to the next string.",
                        true,
                        "Because the decoder reads a known number of characters rather than searching for a delimiter, the content of the string itself - even if it contains that same delimiter character - can never break the decoding.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "encode-decode-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three prefix each string with a length. Which one decodes back to the correct list?",
                choices = listOf(
                    LessonChoice(
                        text = "Reads the length, then reads that many characters starting from the current position, without first moving past the delimiter.",
                        correct = false,
                        feedback = "If the read position isn't advanced past the length prefix and its delimiter before reading the string, the string read will include leftover digits or the delimiter itself.",
                        code = "fun decode(s: String): List<String> {\n    val result = mutableListOf<String>()\n    var i = 0\n    while (i < s.length) {\n        val delim = s.indexOf('#', i)\n        val len = s.substring(i, delim).toInt()\n        result.add(s.substring(i, i + len))\n        i = delim + 1 + len\n    }\n    return result\n}",
                    ),
                    LessonChoice(
                        text = "Reads the length, moves past the delimiter, then reads exactly that many characters.",
                        correct = true,
                        feedback = "Reading the length, skipping the delimiter, and then reading exactly len characters is exactly how the encoder built the string, so decoding reverses it correctly.",
                        code = "fun decode(s: String): List<String> {\n    val result = mutableListOf<String>()\n    var i = 0\n    while (i < s.length) {\n        val delim = s.indexOf('#', i)\n        val len = s.substring(i, delim).toInt()\n        val start = delim + 1\n        result.add(s.substring(start, start + len))\n        i = start + len\n    }\n    return result\n}",
                    ),
                    LessonChoice(
                        text = "Reads exactly 4 characters as the length every time, regardless of how many digits the length actually has.",
                        correct = false,
                        feedback = "A length like 5 is a single digit, and a length like 12 is two digits - always reading a fixed 4 characters would misread lengths that don't happen to be exactly 4 digits long.",
                        code = "fun decode(s: String): List<String> {\n    val result = mutableListOf<String>()\n    var i = 0\n    while (i < s.length) {\n        val len = s.substring(i, i + 4).toInt()\n        val start = i + 4 + 1\n        result.add(s.substring(start, start + len))\n        i = start + len\n    }\n    return result\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "encode-decode-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This decoder throws an exception when a string in the list happens to contain a '#' character. What's the bug?",
                code = "fun decode(s: String): List<String> {\n    val result = mutableListOf<String>()\n    val parts = s.split(\"#\")\n    var i = 0\n    while (i < parts.size) {\n        val len = parts[i].toInt()\n        result.add(parts[i + 1])\n        i += 2\n    }\n    return result\n}",
                choices = listOf(
                    LessonChoice(
                        "Stop splitting the whole string on '#' up front, and instead locate each length's delimiter one at a time, reading exactly len characters after it.",
                        true,
                        "Splitting the entire string on '#' breaks apart any string content that itself contains a '#' - the delimiter needs to be located one at a time, immediately after each length, not searched for everywhere at once.",
                    ),
                    LessonChoice(
                        "Change parts[i].toInt() to parts[i].toIntOrNull() ?: 0.",
                        false,
                        "Silently treating an unparseable length as 0 hides the real bug instead of fixing it - the string is being split incorrectly in the first place.",
                    ),
                    LessonChoice(
                        "Change i += 2 to i += 1.",
                        false,
                        "That would read the same length or string twice on the next loop iteration - it doesn't address why split() produces the wrong pieces when a string contains '#'.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "encode-decode-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the total number of characters across all strings, what is the time complexity of encoding followed by decoding?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because each string's length prefix must be searched for from the very beginning of the encoded string.",
                        false,
                        "The decoder tracks its read position as it goes and never restarts the search from the beginning - each character of the encoded string is visited only a small, constant number of times overall.",
                    ),
                    LessonChoice(
                        "O(n), because both encoding and decoding read and write each character a constant number of times.",
                        true,
                        "Encoding writes each string's characters once (plus a short length prefix), and decoding reads through the encoded string once, so the total work scales directly with the total number of characters.",
                    ),
                    LessonChoice(
                        "O(n log n), because the strings must be sorted before they can be joined.",
                        false,
                        "Nothing about this encoding scheme requires sorting the strings - they're joined and later split apart in whatever order they originally appeared.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "encode-decode-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the encoded string and the decoded list use, relative to the total input size n?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because encoding always produces a single string.",
                        false,
                        "Producing a single string doesn't mean that string is small - its length still grows with the total size of the strings being encoded, plus a short length prefix for each.",
                    ),
                    LessonChoice(
                        "O(log n), because only the length prefixes need extra space.",
                        false,
                        "The length prefixes are small, but the encoded string also has to hold every character of every original string, which is the dominant cost, not the prefixes.",
                    ),
                    LessonChoice(
                        "O(n), because the encoded string holds every original character plus a small length prefix per string, and decoding rebuilds a list of the same total size.",
                        true,
                        "Both the encoded string and the decoded list scale directly with the total number of characters across all the original strings, plus a small constant amount per string for the length prefix.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture packing several letters into one big envelope to mail them together.\n\nIf you just tape the letters end to end, the recipient can't tell where one letter ends and the next begins - especially if a letter's own text happens to contain your tape marks.\n\nInstead, write a sticky note on each letter saying exactly how many pages it has. Now the recipient can always find the next letter's start, no matter what's written inside.",
            walkthrough = listOf(
                "Encoding [\"hello\", \"world\"]: 'hello' has 5 characters, so write \"5#hello\".",
                "'world' has 5 characters, so append \"5#world\".",
                "The final encoded string is \"5#hello5#world\".",
                "Decoding: starting at position 0, read digits up to the next '#' -> \"5\", so len = 5.",
                "Skip past the '#', then read exactly 5 characters: \"hello\". Move the position forward by 5.",
                "Repeat from the new position: read \"5\", skip '#', read \"world\". Return [\"hello\", \"world\"].",
            ),
            pseudocode = "encode(strings):\n    result = \"\"\n    for s in strings: result += length(s) + \"#\" + s\n    return result\n\ndecode(s):\n    result = []\n    i = 0\n    while i < length(s):\n        find delimiter '#' starting at i\n        len = number before the delimiter\n        start = position right after the delimiter\n        result.add(s[start until start + len])\n        i = start + len\n    return result",
            referenceCode = "fun encode(strs: List<String>): String {\n    val sb = StringBuilder()\n    for (s in strs) sb.append(s.length).append('#').append(s)\n    return sb.toString()\n}\n\nfun decode(s: String): List<String> {\n    val result = mutableListOf<String>()\n    var i = 0\n    while (i < s.length) {\n        val delim = s.indexOf('#', i)\n        val len = s.substring(i, delim).toInt()\n        val start = delim + 1\n        result.add(s.substring(start, start + len))\n        i = start + len\n    }\n    return result\n}",
            timeComplexity = "O(n), where n is the total number of characters across every string in the list.\n\nEncoding writes each character once, plus a short length prefix per string.\n\nDecoding reads through the encoded string once from start to finish, always moving forward, never re-scanning earlier parts.",
            spaceComplexity = "O(n), where n is the total number of characters across every string in the list.\n\nThe encoded string has to hold every original character, plus a small length prefix for each string.\n\nThe decoded list rebuilds those same strings, so it takes roughly the same amount of space again.",
            alternatives = listOf(
                "Pick a delimiter character that's unlikely to appear in real input, like a newline or a null character, and join strings with it directly.\nThis is simpler to write than a length-prefix scheme.\nBut it's fundamentally unsafe: if any string in the list happens to contain that exact character, decoding will split it apart incorrectly.",
                "Escape the delimiter character wherever it appears inside a string, similar to how quotes are escaped inside quoted text, then split on the unescaped delimiter.\nThis avoids needing to track lengths.\nBut writing a correct escaping and unescaping scheme is more error-prone than simply prefixing each string with its length.",
            ),
            commonMistakes = listOf(
                "Splitting the entire encoded string on the delimiter character up front, rather than locating each length's delimiter one at a time.\nIf any original string contains that same delimiter character, a blanket split() would break it into extra pieces.\nThe delimiter should only ever be searched for right after where a length prefix is expected to start.",
                "Reading a fixed number of digits for the length, instead of reading up to the delimiter.\nString lengths can have different numbers of digits - 5 is one digit, 50 is two.\nThe length must be read as 'everything up to the next delimiter', not a fixed-width field.",
                "Forgetting to move the read position forward correctly after reading a string.\nIf the position isn't advanced past both the length prefix and the string's own characters, the next read starts in the wrong place and the rest of the decoding falls apart.\nEach step needs to track exactly how far it has read.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: how do I pack multiple pieces of data into one, and unpack them again without ambiguity?",
                "A simple separator character isn't safe because the data itself could contain that character.",
                "Knowing the length of each piece up front removes the need to search for where it ends.",
            ),
            takeaway = "When a delimiter-based join/split isn't safe because the content could contain the delimiter, prefix each piece with its length instead - the decoder then knows exactly how far to read, regardless of what's inside.",
        ),
        nextSlug = "product-of-array-except-self",
    )

    private val productOfArrayExceptSelf = RoadmapLesson(
        slug = "product-of-array-except-self",
        title = "Product of Array Except Self",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an array of integers, return a new array where each position holds the product of every other value in the original array, excluding the value at that position. Solve it without using the division operator.",
        constraints = listOf(
            "The array has at least two elements.",
            "The array may contain zero, positive, or negative values.",
            "Division is not allowed, even to work around a zero in the array.",
            "Aim for linear time and, ideally, constant extra space beyond the output array.",
        ),
        examples = listOf(
            LessonExample("[1, 2, 3, 4]", "[24, 12, 8, 6]", "Position 0 excludes the 1: 2*3*4=24. Position 1 excludes the 2: 1*3*4=12, and so on."),
            LessonExample("[-1, 1, 0, -3, 3]", "[0, 0, 9, 0, 0]", "Any position that isn't the zero's own position includes that zero as a factor, making the product 0."),
            LessonExample("[2, 3]", "[3, 2]", "Position 0 excludes the 2, leaving just 3. Position 1 excludes the 3, leaving just 2."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "product-except-self-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach avoids division while still finishing in a single pass in each direction?",
                choices = listOf(
                    LessonChoice(
                        "Multiply all the values together, then divide by the value at each position to exclude it.",
                        false,
                        "This would work arithmetically, but division is explicitly disallowed - and it also breaks down whenever the array contains a zero.",
                    ),
                    LessonChoice(
                        "For each position, multiply together the running product of everything to its left with the running product of everything to its right.",
                        true,
                        "The product of everything except position i is exactly the product of everything before i times the product of everything after i - no division needed.",
                    ),
                    LessonChoice(
                        "For each position, loop through the entire array again and multiply every value except the one at that position.",
                        false,
                        "This avoids division, but recomputing the product from scratch for every position costs O(n squared) instead of the linear time a running left and right product achieves.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "product-except-self-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three build left and right running products. Which one combines them correctly?",
                choices = listOf(
                    LessonChoice(
                        text = "result[i] starts as the product of everything left of i, then gets multiplied by the product of everything right of i.",
                        correct = true,
                        feedback = "The left product excludes i and everything after it; multiplying in the right product (which excludes i and everything before it) leaves exactly the product of everything except i.",
                        code = "fun productExceptSelf(nums: IntArray): IntArray {\n    val n = nums.size\n    val result = IntArray(n) { 1 }\n    var left = 1\n    for (i in 0 until n) {\n        result[i] = left\n        left *= nums[i]\n    }\n    var right = 1\n    for (i in n - 1 downTo 0) {\n        result[i] *= right\n        right *= nums[i]\n    }\n    return result\n}",
                    ),
                    LessonChoice(
                        text = "result[i] starts as the product of everything left of i, but multiplies nums[i] itself into the running left product before assigning result[i].",
                        correct = false,
                        feedback = "Multiplying in nums[i] itself before assigning it to result[i] would include the excluded value in its own result - the point is to leave nums[i] out entirely.",
                        code = "fun productExceptSelf(nums: IntArray): IntArray {\n    val n = nums.size\n    val result = IntArray(n) { 1 }\n    var left = 1\n    for (i in 0 until n) {\n        left *= nums[i]\n        result[i] = left\n    }\n    var right = 1\n    for (i in n - 1 downTo 0) {\n        result[i] *= right\n        right *= nums[i]\n    }\n    return result\n}",
                    ),
                    LessonChoice(
                        text = "Computes only the left running product and returns that directly, without a right pass at all.",
                        correct = false,
                        feedback = "The left product alone only accounts for values before i - it completely ignores every value after i, so the result would be missing half of what should be multiplied in.",
                        code = "fun productExceptSelf(nums: IntArray): IntArray {\n    val n = nums.size\n    val result = IntArray(n) { 1 }\n    var left = 1\n    for (i in 0 until n) {\n        result[i] = left\n        left *= nums[i]\n    }\n    return result\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "product-except-self-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version returns the correct values after the left pass but produces wrong answers once the right pass runs. What's the bug?",
                code = "var right = 1\nfor (i in 0 until n) {\n    result[i] *= right\n    right *= nums[i]\n}",
                choices = listOf(
                    LessonChoice(
                        "Change result[i] *= right to result[i] = right.",
                        false,
                        "Overwriting result[i] instead of multiplying into it would throw away the left product computed in the first pass entirely.",
                    ),
                    LessonChoice(
                        "Change for (i in 0 until n) to for (i in n - 1 downTo 0).",
                        true,
                        "The right-side pass has to move from the last index toward the first so that right always reflects everything strictly to the right of i - looping forward would multiply in values from the wrong side.",
                    ),
                    LessonChoice(
                        "Change right *= nums[i] to right += nums[i].",
                        false,
                        "Switching to addition would compute a running sum instead of a running product, which has nothing to do with the products this problem asks for.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "product-except-self-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of values in the array, what is the time complexity of the two-pass left-and-right approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because for each position the rest of the array must be re-multiplied.",
                        false,
                        "The two running-product passes never recompute a product from scratch for each position - each pass only does one multiplication per position as it moves through.",
                    ),
                    LessonChoice(
                        "O(n log n), because the left and right products must be sorted before combining.",
                        false,
                        "There's no sorting involved anywhere in this approach - both passes just walk through the array once in a fixed direction.",
                    ),
                    LessonChoice(
                        "O(n), because the algorithm makes exactly two passes over the array, each doing a constant amount of work per position.",
                        true,
                        "One pass left to right builds the left products, one pass right to left builds and combines the right products - two linear passes together are still O(n).",
                    ),
                ),
            ),
            LessonQuestion(
                id = "product-except-self-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "Not counting the output array itself, how much extra space does this approach use?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because a separate array is needed to store the left products and another for the right products.",
                        false,
                        "The left products can be written directly into the output array as the first pass runs, and the right pass only needs a single running variable, not a whole separate array.",
                    ),
                    LessonChoice(
                        "O(1), because only a single running variable is needed for the left pass and another single running variable for the right pass.",
                        true,
                        "Both passes only ever need to remember one running product at a time - the output array itself isn't counted as 'extra' space since the problem requires returning it anyway.",
                    ),
                    LessonChoice(
                        "O(log n), because the running products grow and need more digits to store as the array gets larger.",
                        false,
                        "How many digits a number needs to store isn't what space complexity measures here - it counts the number of extra variables or structures used, which stays fixed at two running products regardless of array size.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Imagine standing at each position in a line of numbers and wanting the product of everyone except yourself.\n\nOne way to get that: multiply together everyone standing to your left, and separately multiply together everyone standing to your right, then combine those two totals.\n\nThat's the whole trick - compute a running product from the left, then a running product from the right, and multiply the two together at each position.",
            walkthrough = listOf(
                "Left pass over [1, 2, 3, 4]: result[0] = 1 (nothing to the left yet), then left becomes 1. result[1] = 1 (product of just the 1), then left becomes 2. result[2] = 2 (product of 1*2), then left becomes 6. result[3] = 6 (product of 1*2*3).",
                "After the left pass, result = [1, 1, 2, 6].",
                "Right pass, moving from the end backward: at i=3, result[3] *= right(1) stays 6, then right becomes 4.",
                "At i=2, result[2] *= right(4) becomes 8, then right becomes 12.",
                "At i=1, result[1] *= right(12) becomes 12, then right becomes 24.",
                "At i=0, result[0] *= right(24) becomes 24. Final result: [24, 12, 8, 6].",
            ),
            pseudocode = "n = length of nums\nresult = array of n ones\nleft = 1\nfor i from 0 to n-1:\n    result[i] = left\n    left = left * nums[i]\nright = 1\nfor i from n-1 down to 0:\n    result[i] = result[i] * right\n    right = right * nums[i]\nreturn result",
            referenceCode = "fun productExceptSelf(nums: IntArray): IntArray {\n    val n = nums.size\n    val result = IntArray(n) { 1 }\n    var left = 1\n    for (i in 0 until n) {\n        result[i] = left\n        left *= nums[i]\n    }\n    var right = 1\n    for (i in n - 1 downTo 0) {\n        result[i] *= right\n        right *= nums[i]\n    }\n    return result\n}",
            timeComplexity = "O(n), where n is the number of values in the array.\n\nThe algorithm makes exactly two passes over the array - one left to right, one right to left.\n\nEach pass does one multiplication per position, so the total work is proportional to n, not n squared.",
            spaceComplexity = "O(1) extra space, not counting the output array itself.\n\nThe left pass only needs to remember a single running product as it moves forward, and the right pass only needs a single running product moving backward.\n\nNeither pass needs a separate array to store intermediate left or right products - they're written straight into the output array as they're computed.",
            alternatives = listOf(
                "Multiply every value together to get the total product, then divide by each value to exclude it.\nThis is the most direct-sounding approach.\nBut division is explicitly disallowed here, and it also completely breaks if any value in the array is zero, since dividing by zero is undefined.",
                "For each position, loop through the whole array again and multiply every value except the one at that position.\nThis needs no extra arrays or running products at all.\nBut recomputing the product from scratch n times, once per position, costs O(n squared) - much slower than the two-pass approach for large arrays.",
            ),
            commonMistakes = listOf(
                "Including nums[i] itself in the left running product before assigning it to result[i].\nThat would multiply the excluded value back into its own result.\nThe assignment to result[i] must happen before the running product is updated with nums[i], not after.",
                "Running the right-side pass forward through the array instead of backward.\nThe running right product needs to represent 'everything to the right of i', which only makes sense when walking from the last index toward the first.\nLooping forward for the right pass mixes up which values are actually to the right of each position.",
                "Assuming a zero in the array means the whole result must be zero everywhere.\nOnly positions other than the zero's own position include that zero as a factor and become zero.\nThe position where the zero itself sits ends up with the product of everything else, which usually is not zero.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: for each item, what's some aggregate of everyone else, without recomputing from scratch each time?",
                "You need information from 'before' and 'after' each position and can build both with a running value in a single pass each.",
                "A brute-force answer would recompute the same overlapping work repeatedly for every position.",
            ),
            takeaway = "When you need 'everything except this position' for every position, split the problem into a left-to-right running value and a right-to-left running value, then combine them - it turns an O(n squared) recomputation into two linear passes.",
        ),
        nextSlug = "valid-sudoku",
    )

    private val validSudoku = RoadmapLesson(
        slug = "valid-sudoku",
        title = "Valid Sudoku",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given a 9x9 Sudoku board, partially filled in, decide whether the digits already placed satisfy the standard Sudoku rules: each row, each column, and each of the nine 3x3 boxes must not contain the same digit twice. The board does not need to be complete or solvable - only that nothing already placed breaks a rule.",
        constraints = listOf(
            "The board is always exactly 9 rows by 9 columns.",
            "Filled cells contain digits 1 through 9; empty cells use a placeholder such as '.'.",
            "The board does not need to be a solvable or complete puzzle - only currently valid.",
            "Aim to check every rule in a single pass over the board rather than repeatedly rescanning it.",
        ),
        examples = listOf(
            LessonExample("A board with two 5s in the same row", "false", "The same digit appearing twice in one row breaks the row rule, regardless of anything else."),
            LessonExample("A board with a 3 repeated in the same 3x3 box", "false", "Even if the row and column rules are fine, a repeated digit within one of the nine boxes is still invalid."),
            LessonExample("A mostly empty board with a few scattered, non-conflicting digits", "true", "With no digit repeated in any shared row, column, or box, the partial board is valid."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "valid-sudoku-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach checks all three Sudoku rules while only scanning the board once?",
                choices = listOf(
                    LessonChoice(
                        "For each filled cell, track the digit seen in a set for its row, a set for its column, and a set for its box; if any digit is already in the relevant set, the board is invalid.",
                        true,
                        "One pass over all 81 cells is enough, because each cell only ever needs to check and update the three sets - row, column, box - that it belongs to.",
                    ),
                    LessonChoice(
                        "First scan every row for duplicates, then rescan the whole board again for every column, then rescan it again for every box.",
                        false,
                        "This is correct, but it scans the board three separate times instead of combining all three checks into one pass over the cells.",
                    ),
                    LessonChoice(
                        "Sort the digits within each row, column, and box, and check for adjacent duplicates.",
                        false,
                        "Sorting adds unnecessary extra work for each of the 27 groups (9 rows, 9 columns, 9 boxes) when a simple 'have I seen this digit before' check accomplishes the same thing in a single pass.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-sudoku-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use per-row, per-column, and per-box sets. Which one computes the box index correctly?",
                choices = listOf(
                    LessonChoice(
                        text = "Uses the cell's row number alone as the box index.",
                        correct = false,
                        feedback = "The row number alone ranges from 0 to 8, but there are only 9 boxes arranged in a 3x3 grid of boxes - using just the row ignores which of the 3 column-groups the cell falls into.",
                        code = "fun isValidSudoku(board: Array<CharArray>): Boolean {\n    val rows = Array(9) { mutableSetOf<Char>() }\n    val cols = Array(9) { mutableSetOf<Char>() }\n    val boxes = Array(9) { mutableSetOf<Char>() }\n    for (r in 0 until 9) for (c in 0 until 9) {\n        val digit = board[r][c]\n        if (digit == '.') continue\n        val boxIndex = r\n        if (!rows[r].add(digit) || !cols[c].add(digit) || !boxes[boxIndex].add(digit)) return false\n    }\n    return true\n}",
                    ),
                    LessonChoice(
                        text = "Uses (r + c) / 3 as the box index.",
                        correct = false,
                        feedback = "Adding the row and column before dividing mixes cells from different boxes into the same group - row 0/col 6 and row 6/col 0 both give (r+c)/3 = 2, even though they're in completely different boxes.",
                        code = "fun isValidSudoku(board: Array<CharArray>): Boolean {\n    val rows = Array(9) { mutableSetOf<Char>() }\n    val cols = Array(9) { mutableSetOf<Char>() }\n    val boxes = Array(9) { mutableSetOf<Char>() }\n    for (r in 0 until 9) for (c in 0 until 9) {\n        val digit = board[r][c]\n        if (digit == '.') continue\n        val boxIndex = (r + c) / 3\n        if (!rows[r].add(digit) || !cols[c].add(digit) || !boxes[boxIndex].add(digit)) return false\n    }\n    return true\n}",
                    ),
                    LessonChoice(
                        text = "Uses (r / 3) * 3 + (c / 3) as the box index.",
                        correct = true,
                        feedback = "Dividing the row and column by 3 separately identifies which of the 3 box-rows and 3 box-columns a cell belongs to, and combining them with * 3 + gives each of the 9 boxes its own unique index from 0 to 8.",
                        code = "fun isValidSudoku(board: Array<CharArray>): Boolean {\n    val rows = Array(9) { mutableSetOf<Char>() }\n    val cols = Array(9) { mutableSetOf<Char>() }\n    val boxes = Array(9) { mutableSetOf<Char>() }\n    for (r in 0 until 9) for (c in 0 until 9) {\n        val digit = board[r][c]\n        if (digit == '.') continue\n        val boxIndex = (r / 3) * 3 + (c / 3)\n        if (!rows[r].add(digit) || !cols[c].add(digit) || !boxes[boxIndex].add(digit)) return false\n    }\n    return true\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-sudoku-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version incorrectly reports boards with empty cells as invalid. What's the bug?",
                code = "for (r in 0 until 9) for (c in 0 until 9) {\n    val digit = board[r][c]\n    val boxIndex = (r / 3) * 3 + (c / 3)\n    if (!rows[r].add(digit) || !cols[c].add(digit) || !boxes[boxIndex].add(digit)) return false\n}",
                choices = listOf(
                    LessonChoice(
                        "Add if (digit == '.') continue right after reading the digit, before checking the sets.",
                        true,
                        "Without skipping the placeholder character, every empty cell gets added to its row, column, and box sets just like a real digit - and since '.' would then already be in those sets, the second empty cell anywhere triggers a false duplicate.",
                    ),
                    LessonChoice(
                        "Change mutableSetOf<Char>() to mutableListOf<Char>().",
                        false,
                        "Switching from a set to a list wouldn't fix anything - the real issue is that the placeholder character is being treated as a digit that needs to be unique, when it should be skipped entirely.",
                    ),
                    LessonChoice(
                        "Change the loop to only visit filled cells by checking board[r][c] != '.' in the for-loop condition itself.",
                        false,
                        "For-loop headers over a fixed range like 0 until 9 can't easily embed a per-cell content check - the skip needs to happen inside the loop body, right after reading each cell's digit.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-sudoku-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "The board always has exactly 81 cells. What is the time complexity of the single-pass, three-set approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because each cell must be compared against every other cell in its row, column, and box.",
                        false,
                        "Using sets means each cell only checks membership and inserts once per set - it's never directly compared to every other individual cell.",
                    ),
                    LessonChoice(
                        "O(1), because the board size is always fixed at 81 cells, so the total work never grows.",
                        true,
                        "Since a Sudoku board is always 9x9, the number of cells visited and the number of set operations performed never changes no matter what digits are on the board - that fixed amount of work is constant time.",
                    ),
                    LessonChoice(
                        "O(n), because the algorithm makes one pass over the cells.",
                        false,
                        "One pass is correct, but the size of that pass (81 cells) never changes for any Sudoku board - describing it with a variable n that could grow doesn't apply here, since the work is a fixed constant amount.",
                    ),
                ),
                glossary = listOf(
                    GlossaryTerm("O(1)", "Constant time - the total work never changes because the input size itself never changes."),
                ),
            ),
            LessonQuestion(
                id = "valid-sudoku-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space do the 9 row sets, 9 column sets, and 9 box sets use together?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because the number of sets used grows with the size of the board.",
                        false,
                        "The board size (9x9) never changes for a Sudoku puzzle, so the number of sets - and how large each one can get - never grows either.",
                    ),
                    LessonChoice(
                        "O(log n), because sets are typically implemented with a tree-like structure internally.",
                        false,
                        "The internal implementation detail of a particular set type doesn't change what's being measured here - it's the total number of digits that could ever be stored across all the sets, which is fixed.",
                    ),
                    LessonChoice(
                        "O(1), because there are always exactly 27 sets (9 rows, 9 columns, 9 boxes), each holding at most 9 possible digits.",
                        true,
                        "Both the number of sets and the maximum number of digits (1 through 9) each set could ever hold are fixed constants for any Sudoku board, so the total space never grows.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Imagine three referees watching a Sudoku board at once: one referee per row watching for repeated digits, one referee per column doing the same, and one referee per 3x3 box doing the same.\n\nAs each filled cell is revealed, all three referees relevant to that cell check whether they've already seen that digit.\n\nIf any referee has seen it before, the board breaks a rule - it's invalid.",
            walkthrough = listOf(
                "Start at cell (0, 0). If it holds a digit, say '5', check rows[0], cols[0], and boxes[0] for '5'. None have seen it, so add '5' to all three.",
                "Move to cell (0, 1). Suppose it's also '5'. rows[0] already contains '5' from the previous cell - since they're in the same row, this is an immediate conflict.",
                "Return false right away, without needing to check any further cells.",
                "If no conflict is ever found across all 81 cells, every row, column, and box has unique digits wherever they're filled in, so the board is valid - return true.",
            ),
            pseudocode = "rows = 9 empty sets, cols = 9 empty sets, boxes = 9 empty sets\nfor r from 0 to 8:\n    for c from 0 to 8:\n        digit = board[r][c]\n        if digit is the empty placeholder: skip this cell\n        boxIndex = (r / 3) * 3 + (c / 3)\n        if digit already in rows[r], cols[c], or boxes[boxIndex]: return false\n        add digit to rows[r], cols[c], and boxes[boxIndex]\nreturn true",
            referenceCode = "fun isValidSudoku(board: Array<CharArray>): Boolean {\n    val rows = Array(9) { mutableSetOf<Char>() }\n    val cols = Array(9) { mutableSetOf<Char>() }\n    val boxes = Array(9) { mutableSetOf<Char>() }\n    for (r in 0 until 9) {\n        for (c in 0 until 9) {\n            val digit = board[r][c]\n            if (digit == '.') continue\n            val boxIndex = (r / 3) * 3 + (c / 3)\n            if (!rows[r].add(digit) || !cols[c].add(digit) || !boxes[boxIndex].add(digit)) return false\n        }\n    }\n    return true\n}",
            timeComplexity = "O(1), because a Sudoku board is always exactly 9x9, so the number of cells visited never changes.\n\nEvery board, no matter what digits it contains, requires checking the same fixed 81 cells against the same fixed 27 sets.\n\nSince the input size itself can never grow, the work done is a constant, not a variable that scales with anything.",
            spaceComplexity = "O(1), because there are always exactly 9 row sets, 9 column sets, and 9 box sets, and each one can hold at most the 9 possible digits.\n\nNone of these structures grow with anything about the specific board - their maximum size is fixed by the rules of Sudoku itself.\n\nSo the total extra memory used never changes.",
            alternatives = listOf(
                "Check all 9 rows for duplicates, then separately check all 9 columns, then separately check all 9 boxes - three full passes over the board.\nThis is easier to reason about one rule at a time.\nBut it revisits every cell three times instead of once, doing three times the work for no real benefit given the board's fixed, small size.",
                "For each filled cell, directly compare it against every other cell in its row, column, and box without using sets.\nThis avoids any extra data structures.\nBut it turns each cell's check into up to 24 direct comparisons instead of 3 quick set lookups.",
            ),
            commonMistakes = listOf(
                "Forgetting to skip empty placeholder cells before checking them against the sets.\nTreating every empty cell as if it were a real digit to be tracked causes a false conflict the moment a second empty cell shows up anywhere in the same row, column, or box.\nThe empty placeholder must be explicitly skipped before any set logic runs.",
                "Computing the box index incorrectly, such as using the row number alone or adding the row and column together.\nEither mistake groups cells from genuinely different 3x3 boxes into the same tracked set, or splits cells from the same box into different sets.\nThe correct formula divides the row and column by 3 separately, then combines them, to uniquely identify each of the 9 boxes.",
                "Assuming the board must be a complete, solvable puzzle to be 'valid'.\nThis problem only asks whether the digits already placed break any rule - a mostly empty board with no conflicts is still valid, even though it isn't solved.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: does anything appear twice within any of several overlapping groupings?",
                "Each item belongs to more than one group at once - here, its row, its column, and its box - that all need independent duplicate checks.",
                "The overall size of the problem is fixed and small, which is a strong hint that time and space complexity are constant, not variable.",
            ),
            takeaway = "When validating uniqueness across several overlapping groups at once, track one set per group and check membership as you scan - it turns what could be several separate passes into a single pass that updates every relevant group together.",
        ),
        nextSlug = "longest-consecutive-sequence",
    )

    private val longestConsecutiveSequence = RoadmapLesson(
        slug = "longest-consecutive-sequence",
        title = "Longest Consecutive Sequence",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an unsorted array of integers, find the length of the longest run of consecutive integers that all appear somewhere in the array, in any order. If 4, 5, 6, and 7 all appear in the array, that's a run of length 4, whether or not they're next to each other in the array itself.",
        constraints = listOf(
            "The array may be empty, in which case the answer is 0.",
            "Values may repeat; repeats don't extend the run any further.",
            "The array is not sorted, and the run does not need to appear in order within the array.",
            "Aim for linear time - sorting first would work but costs more than necessary.",
        ),
        examples = listOf(
            LessonExample("[100, 4, 200, 1, 3, 2]", "4", "The consecutive run 1, 2, 3, 4 has length 4. 100 and 200 are isolated."),
            LessonExample("[0, 3, 7, 2, 5, 8, 4, 6, 0, 1]", "9", "0 through 8 are all present, forming a run of length 9; the repeated 0 doesn't extend it further."),
            LessonExample("[]", "0", "An empty array has no run at all."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "longest-consecutive-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the longest run without sorting the array first?",
                choices = listOf(
                    LessonChoice(
                        "Sort the array, then walk through counting how long each consecutive run lasts.",
                        false,
                        "This correctly finds the answer, but sorting costs O(n log n) - more work than a hash-set approach needs to achieve the same result.",
                    ),
                    LessonChoice(
                        "Put every value into a set, then for each value that has no value one less than it in the set, count upward from there to see how long the run extends.",
                        true,
                        "Only starting a count from values with no predecessor in the set means each number is only ever counted as part of exactly one run, keeping the total work linear.",
                    ),
                    LessonChoice(
                        "For each value in the array, repeatedly check whether the next integer up is present, without first filtering out values that already have a predecessor present.",
                        false,
                        "Without skipping values that have a predecessor, this recounts the same run starting from every one of its members, which can add up to O(n squared) work in the worst case.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-consecutive-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use a set and only start counting from run-starts. Which one identifies run-starts correctly?",
                choices = listOf(
                    LessonChoice(
                        text = "Only starts counting from a value v when v - 1 is not in the set.",
                        correct = true,
                        feedback = "A value with no predecessor in the set must be the smallest number in its run, so starting the count there - and nowhere else in that run - visits every number in the run exactly once overall.",
                        code = "fun longestConsecutive(nums: IntArray): Int {\n    val set = nums.toHashSet()\n    var longest = 0\n    for (n in set) {\n        if (n - 1 !in set) {\n            var length = 1\n            var current = n\n            while (current + 1 in set) {\n                current++\n                length++\n            }\n            longest = maxOf(longest, length)\n        }\n    }\n    return longest\n}",
                    ),
                    LessonChoice(
                        text = "Only starts counting from a value v when v + 1 is not in the set.",
                        correct = false,
                        feedback = "A value with no successor is actually the largest number in its run, not the smallest - starting there and counting upward would immediately stop after checking just one number.",
                        code = "fun longestConsecutive(nums: IntArray): Int {\n    val set = nums.toHashSet()\n    var longest = 0\n    for (n in set) {\n        if (n + 1 !in set) {\n            var length = 1\n            var current = n\n            while (current + 1 in set) {\n                current++\n                length++\n            }\n            longest = maxOf(longest, length)\n        }\n    }\n    return longest\n}",
                    ),
                    LessonChoice(
                        text = "Starts counting from every value in the set, with no run-start check at all.",
                        correct = false,
                        feedback = "Without skipping values that already have a predecessor in the set, every number in a run of length k gets counted as its own starting point, redoing the same counting work up to k times over.",
                        code = "fun longestConsecutive(nums: IntArray): Int {\n    val set = nums.toHashSet()\n    var longest = 0\n    for (n in set) {\n        var length = 1\n        var current = n\n        while (current + 1 in set) {\n            current++\n            length++\n        }\n        longest = maxOf(longest, length)\n    }\n    return longest\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-consecutive-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version either returns 0 for every input or never finishes running. What's the bug?",
                code = "val set = nums.toHashSet()\nvar longest = 0\nfor (n in set) {\n    if (n - 1 !in set) {\n        var length = 1\n        var current = n\n        while (current + 1 in set) {\n            length++\n        }\n        longest = maxOf(longest, length)\n    }\n}",
                choices = listOf(
                    LessonChoice(
                        "Change n - 1 !in set to n - 1 in set.",
                        false,
                        "Flipping this condition would start counting from values that already have a predecessor, which recounts runs from the middle instead of correctly identifying where each run starts.",
                    ),
                    LessonChoice(
                        "Change nums.toHashSet() to nums.toSortedSet().",
                        false,
                        "A sorted set doesn't fix the counting logic at all - the bug is in how the while loop advances, not in what kind of set is used.",
                    ),
                    LessonChoice(
                        "Add current++ inside the while loop, so it becomes current++; length++.",
                        true,
                        "Without advancing current, the condition current + 1 in set checks the exact same value forever - if that condition is ever true, the loop never terminates instead of walking forward through the run.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-consecutive-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of values in the array, what is the time complexity of the set-based, run-start approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because every value is only ever visited by the inner while loop once, when it's reached as part of its run's actual starting walk.",
                        true,
                        "Even though there's a while loop nested inside a for loop, the total number of inner-loop steps across the whole algorithm never exceeds n, because only true run-starts trigger a walk, and each walk only ever visits numbers in its own run.",
                    ),
                    LessonChoice(
                        "O(n squared), because for every value, the inner while loop can walk through up to n more values.",
                        false,
                        "That would be true if every value triggered a full walk, but the run-start check ensures only one value per run ever starts a walk - across the whole algorithm, the combined walking never exceeds n steps total.",
                    ),
                    LessonChoice(
                        "O(n log n), because the values must be sorted before counting can begin.",
                        false,
                        "This approach uses a hash set, not a sorted structure - values are placed in and checked from the set directly, with no sorting step anywhere.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-consecutive-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the hash set of values use?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because a set is a single data structure.",
                        false,
                        "Being a single data structure doesn't limit how many elements it holds - this set can grow to hold every distinct value in the array.",
                    ),
                    LessonChoice(
                        "O(n), because in the worst case, every value in the array is distinct and gets stored in the set.",
                        true,
                        "If none of the values repeat, the set ends up holding all n of them, so its size scales directly with the size of the input.",
                    ),
                    LessonChoice(
                        "O(log n), because sets typically use a balanced structure internally that grows slowly.",
                        false,
                        "A hash set's size is determined by how many distinct elements are stored in it, not by how the set organizes them internally - it can hold up to n elements.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture a pile of numbered cards scattered on a table, and you want to find the longest unbroken staircase you could build by lining up consecutive numbers.\n\nFirst, spread all the cards out where you can instantly check 'do I have this number?' - that's what the set is for.\n\nThen, for each card, only start building a staircase from it if there's no card one step below it - that guarantees you're starting from the true bottom of a staircase, not partway up one you'd end up re-walking.",
            walkthrough = listOf(
                "Put every value into a set: {100, 4, 200, 1, 3, 2}.",
                "Check 100: is 99 in the set? No. Start counting: 100 is in the set, 101 is not, so this run has length 1.",
                "Check 4: is 3 in the set? Yes - so 4 is not the start of its run, skip it.",
                "Check 1: is 0 in the set? No. Start counting: 1 is in, 2 is in, 3 is in, 4 is in, 5 is not. This run has length 4.",
                "Check 200: is 199 in the set? No. Start counting: only 200 itself, length 1.",
                "The longest run found is 4 (from 1, 2, 3, 4). Return 4.",
            ),
            pseudocode = "set = all values from nums, with duplicates removed\nlongest = 0\nfor each value n in set:\n    if (n - 1) is not in set:\n        length = 1\n        current = n\n        while (current + 1) is in set:\n            current = current + 1\n            length = length + 1\n        longest = max(longest, length)\nreturn longest",
            referenceCode = "fun longestConsecutive(nums: IntArray): Int {\n    val set = nums.toHashSet()\n    var longest = 0\n    for (n in set) {\n        if (n - 1 !in set) {\n            var length = 1\n            var current = n\n            while (current + 1 in set) {\n                current++\n                length++\n            }\n            longest = maxOf(longest, length)\n        }\n    }\n    return longest\n}",
            timeComplexity = "O(n), where n is the number of values in the array.\n\nBuilding the set takes one pass over the array. Then, even though there's a while loop inside a for loop, each number is only ever walked past once in total, because only true run-starts trigger a walk, and each walk stops the moment it reaches the end of its own run.\n\nAdd up every walk across the whole algorithm, and the total number of steps never exceeds n.",
            spaceComplexity = "O(n) in the worst case, where n is the number of values in the array.\n\nIf every value in the array is distinct, the set ends up holding all n of them.\n\nSo the extra memory used grows directly with how many distinct values are in the input.",
            alternatives = listOf(
                "Sort the array first, then walk through it once counting how long each consecutive streak lasts.\nThis is simple to reason about and handles duplicates naturally once sorted.\nBut sorting costs O(n log n), which is more work than the hash-set approach needs to reach the same answer.",
                "Build a union-find (disjoint set) structure that merges consecutive numbers into the same group as they're discovered, then find the largest group.\nThis generalizes well to problems that also need to merge groups later.\nBut it's meaningfully more code and overhead for a problem that a plain hash set already solves in linear time.",
            ),
            commonMistakes = listOf(
                "Starting a count from every value in the set, instead of only values with no predecessor.\nThat re-walks the same run once for every one of its members, turning a linear algorithm into a quadratic one in the worst case.\nOnly values where n - 1 is missing from the set should ever start a count.",
                "Forgetting to advance the current value inside the while loop that walks forward through a run.\nWithout that, the loop either never runs, or - if the condition it's checking never changes - runs forever checking the exact same number.\nEach step of the walk needs to move current forward by one before rechecking the condition.",
                "Sorting the array when the problem doesn't require it and a set-based approach would be faster.\nA sorted approach isn't wrong, but it costs O(n log n) instead of O(n), missing the performance the hash-set trick was designed to achieve.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what's the longest unbroken chain hiding among unordered values?",
                "Order in the input array doesn't matter, only which values are present - a strong hint that a set can replace sorting.",
                "Each item's role, such as whether it starts a chain, can be determined instantly by checking whether a specific related value exists.",
            ),
            takeaway = "When hunting for the longest run of related values scattered through unordered data, use a set for instant existence checks, and only start counting from true starting points - that keeps every value from being re-walked more than once.",
        ),
        nextSlug = "valid-palindrome",
    )

    private val validPalindrome = RoadmapLesson(
        slug = "valid-palindrome",
        title = "Valid Palindrome",
        difficulty = CurriculumDifficulty.EASY,
        description = "Given a string, decide whether it reads the same forwards and backwards after converting all letters to lowercase and removing everything that isn't a letter or digit.",
        constraints = listOf(
            "The string may contain letters, digits, spaces, and punctuation.",
            "Case does not matter: 'A' and 'a' are treated as the same character.",
            "After removing non-alphanumeric characters, an empty result counts as a palindrome.",
            "Aim for constant extra space rather than building a new cleaned-up string.",
        ),
        examples = listOf(
            LessonExample("\"A man, a plan, a canal: Panama\"", "true", "Ignoring case, spaces, and punctuation, this reads \"amanaplanacanalpanama\" both ways."),
            LessonExample("\"race a car\"", "false", "Ignoring spaces, \"raceacar\" does not read the same backwards."),
            LessonExample("\" \"", "true", "After removing the only character (a space, which isn't alphanumeric), nothing is left to compare - trivially a palindrome."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "valid-palindrome-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach checks the palindrome property using constant extra space, without building a new cleaned string first?",
                choices = listOf(
                    LessonChoice(
                        "Build a new string containing only the lowercase letters and digits, then check whether that new string equals its own reverse.",
                        false,
                        "This is correct, but building an entirely new string and its reverse uses O(n) extra space - avoidable with two pointers scanning the original string directly.",
                    ),
                    LessonChoice(
                        "Convert the string to a character array, sort it, and check if it reads the same both ways.",
                        false,
                        "Sorting doesn't preserve the order needed to check for a palindrome at all - it scrambles the very sequence that needs to be compared.",
                    ),
                    LessonChoice(
                        "Use two pointers starting at both ends of the string, skipping non-alphanumeric characters and moving inward, comparing lowercase letters as they go.",
                        true,
                        "This never builds a second string - it compares characters directly from the two ends of the original string, using only two index variables as extra space.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-palindrome-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use two pointers skipping non-alphanumeric characters. Which one compares correctly?",
                choices = listOf(
                    LessonChoice(
                        text = "Compares characters without converting either side to lowercase first.",
                        correct = false,
                        feedback = "Comparing 'A' directly to 'a' would report them as different characters, even though the problem says case shouldn't matter.",
                        code = "fun isPalindrome(s: String): Boolean {\n    var left = 0\n    var right = s.length - 1\n    while (left < right) {\n        while (left < right && !s[left].isLetterOrDigit()) left++\n        while (left < right && !s[right].isLetterOrDigit()) right--\n        if (s[left] != s[right]) return false\n        left++\n        right--\n    }\n    return true\n}",
                    ),
                    LessonChoice(
                        text = "Lowercases both characters before comparing them, after skipping non-alphanumeric characters from both ends.",
                        correct = true,
                        feedback = "Lowercasing both sides before the comparison makes 'A' and 'a' match, exactly as the problem requires, while the skip loops ensure only letters and digits are ever compared.",
                        code = "fun isPalindrome(s: String): Boolean {\n    var left = 0\n    var right = s.length - 1\n    while (left < right) {\n        while (left < right && !s[left].isLetterOrDigit()) left++\n        while (left < right && !s[right].isLetterOrDigit()) right--\n        if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false\n        left++\n        right--\n    }\n    return true\n}",
                    ),
                    LessonChoice(
                        text = "Only skips non-alphanumeric characters from the left pointer, never from the right.",
                        correct = false,
                        feedback = "If the right side has punctuation or spaces, the right pointer would compare a letter against a space or punctuation mark, incorrectly reporting a mismatch.",
                        code = "fun isPalindrome(s: String): Boolean {\n    var left = 0\n    var right = s.length - 1\n    while (left < right) {\n        while (left < right && !s[left].isLetterOrDigit()) left++\n        if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false\n        left++\n        right--\n    }\n    return true\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-palindrome-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version throws an index-out-of-bounds error on strings with lots of punctuation. What's the bug?",
                code = "var left = 0\nvar right = s.length - 1\nwhile (left < right) {\n    while (!s[left].isLetterOrDigit()) left++\n    while (!s[right].isLetterOrDigit()) right--\n    if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false\n    left++\n    right--\n}",
                choices = listOf(
                    LessonChoice(
                        "Add left < right && to both inner skip loops' conditions.",
                        true,
                        "Without also checking left < right inside the skip loops, a string with no alphanumeric characters left to find would let a pointer run right off the end of the string.",
                    ),
                    LessonChoice(
                        "Change .isLetterOrDigit() to .isLetter().",
                        false,
                        "That would incorrectly skip over digits too, which the problem explicitly says should be treated as valid characters to compare.",
                    ),
                    LessonChoice(
                        "Change left++ and right-- at the end to left += 2 and right -= 2.",
                        false,
                        "Jumping by two would skip over characters that still need to be compared, breaking the comparison instead of fixing the crash.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-palindrome-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the length of the string, what is the time complexity of the two-pointer approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because each character comparison might require rescanning the string.",
                        false,
                        "Both pointers only ever move inward and never backward - there's no rescanning, so the total number of character visits is bounded by the string's length.",
                    ),
                    LessonChoice(
                        "O(n), because the two pointers together visit each character at most once as they move toward the middle.",
                        true,
                        "Between them, left and right cover the whole string exactly once, whether they're skipping a non-alphanumeric character or comparing a pair.",
                    ),
                    LessonChoice(
                        "O(n log n), because the characters must be sorted before comparing them for a palindrome.",
                        false,
                        "No sorting happens here - the two pointers compare characters directly from their current positions in the original string order.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-palindrome-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the two-pointer approach use, beyond the input string itself?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because a cleaned-up copy of the string is built first.",
                        false,
                        "This approach never builds a second string - it reads directly from the original string using two index variables.",
                    ),
                    LessonChoice(
                        "O(log n), because the two pointers divide the string in half repeatedly.",
                        false,
                        "The pointers don't divide anything recursively - they're just two simple counters that move one step at a time toward each other.",
                    ),
                    LessonChoice(
                        "O(1), because only two index variables (left and right) are used, regardless of how long the string is.",
                        true,
                        "Two integers is a fixed, constant amount of extra memory, no matter whether the string is 10 characters or 10 million.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture reading a phrase from both ends at once with two fingers, one starting at the very first character and one at the very last.\n\nSkip past anything that isn't a letter or number - punctuation and spaces don't count.\n\nCompare what's under each finger, ignoring uppercase versus lowercase, then move both fingers one step closer to the middle. If the fingers ever meet without a mismatch, it's a palindrome.",
            walkthrough = listOf(
                "String: \"A man, a plan, a canal: Panama\". left starts at index 0 ('A'), right starts at the last index (the final 'a').",
                "Both s[left] and s[right] are letters, so compare 'A'.lowercase() to 'a'.lowercase(): both are 'a' - match. Move left forward, right backward.",
                "left now points at a space - skip it forward until it lands on 'm'. right points at 'm' from \"Panama\" - already a letter.",
                "Compare 'm' to 'm': match. Continue moving inward, skipping commas, colons, and spaces along the way.",
                "This continues all the way to the middle with every pair matching, so the loop finishes without ever returning false.",
                "Return true.",
            ),
            pseudocode = "left = 0, right = length of s - 1\nwhile left < right:\n    while left < right and s[left] is not a letter or digit: left += 1\n    while left < right and s[right] is not a letter or digit: right -= 1\n    if lowercase(s[left]) != lowercase(s[right]): return false\n    left += 1\n    right -= 1\nreturn true",
            referenceCode = "fun isPalindrome(s: String): Boolean {\n    var left = 0\n    var right = s.length - 1\n    while (left < right) {\n        while (left < right && !s[left].isLetterOrDigit()) left++\n        while (left < right && !s[right].isLetterOrDigit()) right--\n        if (s[left].lowercaseChar() != s[right].lowercaseChar()) return false\n        left++\n        right--\n    }\n    return true\n}",
            timeComplexity = "O(n), where n is the length of the string.\n\nThe left pointer only ever moves forward and the right pointer only ever moves backward - neither one ever revisits a position it has already passed.\n\nBetween the two of them, every character in the string is looked at a small, constant number of times.",
            spaceComplexity = "O(1) extra space, meaning it doesn't grow with the length of the string.\n\nThe entire approach only needs two integer variables, left and right, to track positions in the original string.\n\nNo second string, array, or other structure that scales with input size is ever created.",
            alternatives = listOf(
                "Build a new string containing only the lowercase letters and digits, then compare it to its own reverse.\nThis is very easy to read and reason about.\nBut constructing that cleaned-up string (and its reverse) uses O(n) extra space, which the two-pointer approach avoids entirely.",
                "Use recursion, comparing the outermost valid characters and recursively checking the substring between them.\nThis mirrors the two-pointer logic conceptually.\nBut each recursive call adds a frame to the call stack, so it uses O(n) space in the worst case instead of the O(1) an iterative loop achieves.",
            ),
            commonMistakes = listOf(
                "Comparing characters without converting them to the same case first.\nThe problem treats uppercase and lowercase letters as equivalent, so 'A' and 'a' must be compared as a match, not a mismatch.\nBoth characters need to be lowercased before comparing them.",
                "Only skipping non-alphanumeric characters from one side, not both.\nIf punctuation or spaces on the other side aren't also skipped, a letter can end up compared directly against a space or punctuation mark.\nBoth the left and right skip loops are needed, not just one.",
                "Forgetting the left < right bound inside the inner skip loops.\nWithout it, a string that's entirely punctuation can let a pointer run straight off the end of the string.\nEvery skip loop needs its own left < right guard, not just the outer loop.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: does this sequence mirror itself around its center?",
                "You're comparing from both ends inward rather than needing to look at the whole thing at once.",
                "Some characters in the input should be ignored entirely rather than compared.",
            ),
            takeaway = "When checking whether a sequence mirrors itself, move two pointers inward from both ends instead of building a reversed copy - it turns O(n) extra space into O(1).",
        ),
        nextSlug = "two-sum-ii-input-array-is-sorted",
    )

    private val twoSumII = RoadmapLesson(
        slug = "two-sum-ii-input-array-is-sorted",
        title = "Two Sum II",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given a sorted array of integers and a target value, return the 1-indexed positions of the two numbers that add up to the target. Assume exactly one solution exists, and the same element cannot be used twice. Solve it using constant extra space.",
        constraints = listOf(
            "The array is sorted in non-decreasing order.",
            "Exactly one pair of positions adds up to the target.",
            "Return 1-indexed positions, not 0-indexed.",
            "Aim for constant extra space, taking advantage of the array already being sorted.",
        ),
        examples = listOf(
            LessonExample("nums = [2, 7, 11, 15], target = 9", "[1, 2]", "nums[0] + nums[1] (1-indexed: positions 1 and 2) = 2 + 7 = 9."),
            LessonExample("nums = [2, 3, 4], target = 6", "[1, 3]", "nums[0] + nums[2] = 2 + 4 = 6."),
            LessonExample("nums = [-1, 0], target = -1", "[1, 2]", "nums[0] + nums[1] = -1 + 0 = -1."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "two-sum-ii-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach takes advantage of the array already being sorted to use constant extra space?",
                choices = listOf(
                    LessonChoice(
                        "Use a hash map from value to index, just like the unsorted version of this problem.",
                        false,
                        "This works, but a hash map uses O(n) extra space - the array being sorted means a two-pointer approach can find the pair with no extra space at all.",
                    ),
                    LessonChoice(
                        "Start one pointer at the beginning and one at the end; if their sum is too small move the left pointer right, if too large move the right pointer left, if exact return both positions.",
                        true,
                        "Because the array is sorted, moving the left pointer right only ever increases the sum, and moving the right pointer left only ever decreases it - that lets the pointers narrow in on the target using no extra memory.",
                    ),
                    LessonChoice(
                        "Check every pair of positions directly, since the array being sorted doesn't change how many pairs there are.",
                        false,
                        "The array being sorted is exactly what makes the two-pointer approach possible - ignoring that and checking all pairs wastes the sorted order's advantage.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-ii-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use two pointers from both ends. Which one moves them in the right direction?",
                choices = listOf(
                    LessonChoice(
                        text = "Moves left forward when the sum is too small, and right backward when the sum is too large.",
                        correct = true,
                        feedback = "Since the array is sorted, increasing the smaller pointer raises the sum, and decreasing the larger pointer lowers it - moving toward the target from whichever side is 'wrong'.",
                        code = "fun twoSum(numbers: IntArray, target: Int): IntArray {\n    var left = 0\n    var right = numbers.size - 1\n    while (left < right) {\n        val sum = numbers[left] + numbers[right]\n        when {\n            sum == target -> return intArrayOf(left + 1, right + 1)\n            sum < target -> left++\n            else -> right--\n        }\n    }\n    return intArrayOf()\n}",
                    ),
                    LessonChoice(
                        text = "Moves left forward when the sum is too large, and right backward when the sum is too small.",
                        correct = false,
                        feedback = "This is backwards - moving left forward makes the sum bigger, not smaller, so doing that when the sum is already too large moves further away from the target instead of closer.",
                        code = "fun twoSum(numbers: IntArray, target: Int): IntArray {\n    var left = 0\n    var right = numbers.size - 1\n    while (left < right) {\n        val sum = numbers[left] + numbers[right]\n        when {\n            sum == target -> return intArrayOf(left + 1, right + 1)\n            sum > target -> left++\n            else -> right--\n        }\n    }\n    return intArrayOf()\n}",
                    ),
                    LessonChoice(
                        text = "Moves both left forward and right backward together on every step, regardless of the sum.",
                        correct = false,
                        feedback = "Moving both pointers on every step, even when only one side needs adjusting, can skip right past the correct pair entirely.",
                        code = "fun twoSum(numbers: IntArray, target: Int): IntArray {\n    var left = 0\n    var right = numbers.size - 1\n    while (left < right) {\n        val sum = numbers[left] + numbers[right]\n        if (sum == target) return intArrayOf(left + 1, right + 1)\n        left++\n        right--\n    }\n    return intArrayOf()\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-ii-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version returns the correct values but as 0-indexed positions instead of the 1-indexed positions the problem requires. What's the bug?",
                code = "while (left < right) {\n    val sum = numbers[left] + numbers[right]\n    when {\n        sum == target -> return intArrayOf(left, right)\n        sum < target -> left++\n        else -> right--\n    }\n}",
                choices = listOf(
                    LessonChoice(
                        "Change numbers[left] + numbers[right] to numbers[left + 1] + numbers[right + 1].",
                        false,
                        "That would read past the intended positions when computing the sum, corrupting the comparison itself instead of just fixing the returned indices.",
                    ),
                    LessonChoice(
                        "Change left++ to left += 2 and right-- to right -= 2.",
                        false,
                        "Jumping by two would skip over pairs that still need to be checked - it has nothing to do with how the final answer is indexed.",
                    ),
                    LessonChoice(
                        "Change return intArrayOf(left, right) to return intArrayOf(left + 1, right + 1).",
                        true,
                        "The array itself is still read using ordinary 0-indexed positions, but the problem asks for the returned answer to be reported as 1-indexed, so 1 needs to be added only at the point of returning.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-ii-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of values in the array, what is the time complexity of the two-pointer approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because for each left position, every right position must be checked.",
                        false,
                        "The pointers never restart from the beginning - left only ever increases and right only ever decreases, so together they take at most n steps total.",
                    ),
                    LessonChoice(
                        "O(n), because left and right together move at most n steps before meeting in the middle.",
                        true,
                        "Each step moves exactly one of the two pointers closer to the other, and they start n apart at most, so the loop runs at most n times.",
                    ),
                    LessonChoice(
                        "O(log n), because the sorted array allows a binary search at each step.",
                        false,
                        "This approach doesn't binary search from scratch at each step - it just nudges one of the two pointers by one position at a time based on the current sum.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "two-sum-ii-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the two-pointer approach use, not counting the input array?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only two index variables, left and right, are needed no matter how large the array is.",
                        true,
                        "No matter how many values are in the array, this approach never needs more than the two pointers to track its progress.",
                    ),
                    LessonChoice(
                        "O(n), because a hash map of values to indices must still be built to support the lookup.",
                        false,
                        "This particular approach doesn't use a hash map at all - that's the version used for the unsorted variant of this problem, not this sorted, two-pointer version.",
                    ),
                    LessonChoice(
                        "O(log n), because the array is sorted using a divide-and-conquer strategy.",
                        false,
                        "The array is already given sorted - nothing in this solution sorts it again, so there's no divide-and-conquer step to account for.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture two people standing at opposite ends of a sorted line of numbers, each holding up their number and shouting the sum.\n\nIf the sum is too small, the person on the low end steps toward the middle to a bigger number.\n\nIf the sum is too big, the person on the high end steps toward the middle to a smaller number.\n\nBecause the line is sorted, this always moves the sum in the right direction, and they'll meet at the answer without ever needing to remember anyone else's number.",
            walkthrough = listOf(
                "nums = [2, 7, 11, 15], target = 9. left = 0 (value 2), right = 3 (value 15).",
                "Sum = 2 + 15 = 17, which is too large. Move right backward to index 2 (value 11).",
                "Sum = 2 + 11 = 13, still too large. Move right backward to index 1 (value 7).",
                "Sum = 2 + 7 = 9 - exactly the target!",
                "Return the 1-indexed positions: left + 1 = 1, right + 1 = 2. Answer: [1, 2].",
            ),
            pseudocode = "left = 0, right = length of numbers - 1\nwhile left < right:\n    sum = numbers[left] + numbers[right]\n    if sum == target: return [left + 1, right + 1]\n    else if sum < target: left += 1\n    else: right -= 1",
            referenceCode = "fun twoSum(numbers: IntArray, target: Int): IntArray {\n    var left = 0\n    var right = numbers.size - 1\n    while (left < right) {\n        val sum = numbers[left] + numbers[right]\n        when {\n            sum == target -> return intArrayOf(left + 1, right + 1)\n            sum < target -> left++\n            else -> right--\n        }\n    }\n    return intArrayOf()\n}",
            timeComplexity = "O(n), where n is the number of values in the array.\n\nleft only ever moves forward and right only ever moves backward, so together they take at most n total steps before they meet.\n\nEach step does a fixed, constant amount of work - one addition and one comparison.",
            spaceComplexity = "O(1), because the entire approach only needs two integer variables, left and right, regardless of how large the input array is.\n\nUnlike the unsorted version of this problem, no hash map or other structure that grows with n is needed here.",
            alternatives = listOf(
                "Use a hash map from value to index, exactly like the unsorted two-sum problem.\nThis works correctly and doesn't require the array to be sorted.\nBut it uses O(n) extra space for the map, when the fact that this array is already sorted makes a zero-extra-space, two-pointer approach possible instead.",
                "Binary search for the complement of each value as you scan through the array.\nThis also takes advantage of the sorted order.\nBut it costs O(n log n) overall - one binary search per value - which is slower than the two-pointer approach's O(n).",
            ),
            commonMistakes = listOf(
                "Moving the pointers in the wrong direction when the sum doesn't match the target.\nIf the sum is too small, only moving left forward makes it bigger; moving right backward would make it smaller and move further from the target.\nEach pointer only ever moves in the one direction that corrects the sum.",
                "Returning 0-indexed positions instead of the 1-indexed positions the problem specifically asks for.\nThe array itself is still read using ordinary 0-indexed positions internally.\nOnly the final returned answer needs 1 added to each index.",
                "Forgetting that the array is already sorted and reaching for a hash map out of habit.\nA hash map isn't wrong, but it spends O(n) extra space solving a version of the problem that a sorted array doesn't need.\nWhen input is already sorted, that's a strong hint that two pointers can replace extra memory.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: which two items from a sorted collection combine to hit an exact target?",
                "The input being sorted is explicitly guaranteed, which is a strong hint that two pointers can replace a hash map.",
                "Moving toward or away from a target value in a predictable direction, based on sorted order, can guide where to search next.",
            ),
            takeaway = "When an array is already sorted and you need a pair that hits a target, two pointers starting from both ends can find it in linear time using no extra memory - let the sorted order tell you which pointer to move.",
        ),
        nextSlug = "3sum",
    )

    private val threeSum = RoadmapLesson(
        slug = "3sum",
        title = "3Sum",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an array of integers, find all unique triplets of values that sum to zero. Each triplet should be returned with its values in any order, and the overall result should not contain duplicate triplets.",
        constraints = listOf(
            "The array may contain up to 3,000 values.",
            "Values may repeat, but duplicate triplets in the output must be avoided.",
            "The order of triplets in the result, and the order of values within each triplet, does not matter.",
            "Aim for better than the O(n cubed) that checking every triplet directly would cost.",
        ),
        examples = listOf(
            LessonExample("[-1, 0, 1, 2, -1, -4]", "[[-1, -1, 2], [-1, 0, 1]]", "Both triplets sum to zero; no other combination does, and duplicates of these two triplets are excluded."),
            LessonExample("[0, 1, 1]", "[]", "No three values here sum to zero."),
            LessonExample("[0, 0, 0]", "[[0, 0, 0]]", "The only possible triplet, using all three zeros, sums to zero."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "3sum-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds all triplets without checking every possible triplet directly?",
                choices = listOf(
                    LessonChoice(
                        "Check every combination of three values directly, with three nested loops.",
                        false,
                        "This finds every triplet correctly, but three nested loops over n values costs O(n cubed) - very slow once the array has even a few thousand values.",
                    ),
                    LessonChoice(
                        "Sort the array, then for each value, use two pointers on the remaining sorted portion to find pairs that sum to the negative of that value.",
                        true,
                        "Sorting first turns 'find two values summing to a target' into the same two-pointer trick used in Two Sum II, reducing the total work from three nested loops to one loop around a two-pointer scan.",
                    ),
                    LessonChoice(
                        "For each value, build a hash set of the rest of the array and check every other value against it independently.",
                        false,
                        "This still effectively checks every pair for every fixed first value without using the sorted order to skip past invalid ranges, and it makes avoiding duplicate triplets much harder to manage correctly.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "3sum-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three sort first and use two pointers for the remaining sum. Which one correctly avoids duplicate triplets?",
                choices = listOf(
                    LessonChoice(
                        text = "Skips duplicate values for the fixed first number, but never skips duplicates for the two-pointer values after a match.",
                        correct = false,
                        feedback = "Without also skipping duplicate values after finding a match with the two pointers, the same triplet can be added to the result more than once from adjacent equal values.",
                        code = "fun threeSum(nums: IntArray): List<List<Int>> {\n    val sorted = nums.sorted()\n    val result = mutableListOf<List<Int>>()\n    for (i in sorted.indices) {\n        if (i > 0 && sorted[i] == sorted[i - 1]) continue\n        var left = i + 1\n        var right = sorted.size - 1\n        while (left < right) {\n            val sum = sorted[i] + sorted[left] + sorted[right]\n            when {\n                sum < 0 -> left++\n                sum > 0 -> right--\n                else -> {\n                    result.add(listOf(sorted[i], sorted[left], sorted[right]))\n                    left++\n                    right--\n                }\n            }\n        }\n    }\n    return result\n}",
                    ),
                    LessonChoice(
                        text = "Never skips duplicate values for the fixed first number at all.",
                        correct = false,
                        feedback = "Without skipping a repeated first value, the exact same set of triplets gets rediscovered and re-added every time that value repeats in the sorted array.",
                        code = "fun threeSum(nums: IntArray): List<List<Int>> {\n    val sorted = nums.sorted()\n    val result = mutableListOf<List<Int>>()\n    for (i in sorted.indices) {\n        var left = i + 1\n        var right = sorted.size - 1\n        while (left < right) {\n            val sum = sorted[i] + sorted[left] + sorted[right]\n            when {\n                sum < 0 -> left++\n                sum > 0 -> right--\n                else -> {\n                    result.add(listOf(sorted[i], sorted[left], sorted[right]))\n                    while (left < right && sorted[left] == sorted[left + 1]) left++\n                    while (left < right && sorted[right] == sorted[right - 1]) right--\n                    left++\n                    right--\n                }\n            }\n        }\n    }\n    return result\n}",
                    ),
                    LessonChoice(
                        text = "Skips duplicate values for the fixed first number, and also skips duplicate values on both sides after recording a match.",
                        correct = true,
                        feedback = "Skipping repeats at the outer loop avoids re-processing the same starting value, and skipping repeats after a match avoids re-adding the same triplet found from adjacent equal values.",
                        code = "fun threeSum(nums: IntArray): List<List<Int>> {\n    val sorted = nums.sorted()\n    val result = mutableListOf<List<Int>>()\n    for (i in sorted.indices) {\n        if (i > 0 && sorted[i] == sorted[i - 1]) continue\n        var left = i + 1\n        var right = sorted.size - 1\n        while (left < right) {\n            val sum = sorted[i] + sorted[left] + sorted[right]\n            when {\n                sum < 0 -> left++\n                sum > 0 -> right--\n                else -> {\n                    result.add(listOf(sorted[i], sorted[left], sorted[right]))\n                    while (left < right && sorted[left] == sorted[left + 1]) left++\n                    while (left < right && sorted[right] == sorted[right - 1]) right--\n                    left++\n                    right--\n                }\n            }\n        }\n    }\n    return result\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "3sum-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version reports duplicate copies of the same triplet in its output. What's the bug?",
                code = "for (i in sorted.indices) {\n    var left = i + 1\n    var right = sorted.size - 1\n    while (left < right) {\n        val sum = sorted[i] + sorted[left] + sorted[right]\n        when {\n            sum < 0 -> left++\n            sum > 0 -> right--\n            else -> {\n                result.add(listOf(sorted[i], sorted[left], sorted[right]))\n                left++\n                right--\n            }\n        }\n    }\n}",
                choices = listOf(
                    LessonChoice(
                        "Change sorted.size - 1 to sorted.size - 2.",
                        false,
                        "That would exclude the very last value from ever being used as the right pointer's starting position, which cuts off valid triplets rather than fixing duplicates.",
                    ),
                    LessonChoice(
                        "After recording a match, skip past any further values equal to sorted[left] and sorted[right] before moving the pointers inward.",
                        true,
                        "Without skipping equal neighbors after a match, the same triplet can be rediscovered from adjacent, identical values on either side and added to the result more than once.",
                    ),
                    LessonChoice(
                        "Add a skip for a repeated sorted[i] value at the top of the outer loop, without touching left or right at all.",
                        false,
                        "Skipping a repeated first value alone doesn't prevent a single fixed first value from finding the same left/right pair twice when there are duplicate values within its own two-pointer range.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "3sum-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of values in the array, what is the time complexity of the sort-plus-two-pointer approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because sorting costs O(n log n), and then for each of the n starting values, the two-pointer scan does up to O(n) work.",
                        true,
                        "The dominant cost is the loop over n starting values, each paired with an O(n) two-pointer scan, giving O(n squared) overall - the O(n log n) sort is smaller than that and doesn't change the total.",
                    ),
                    LessonChoice(
                        "O(n log n), because that's the cost of sorting the array.",
                        false,
                        "Sorting is only the first step - the loop over every starting value, each doing its own linear two-pointer scan, adds more work on top of the sort.",
                    ),
                    LessonChoice(
                        "O(n cubed), because three values need to be chosen from the array.",
                        false,
                        "That's the cost of checking every triplet directly with three nested loops - fixing one value and then using two pointers for the other two avoids the third nested loop entirely.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "3sum-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "Not counting the space needed to store the output triplets, how much extra space does this approach use?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because a triplet is checked for every pair of remaining positions.",
                        false,
                        "The two-pointer scan for each starting value doesn't store anything extra proportional to n squared - it just uses a couple of index variables while it scans.",
                    ),
                    LessonChoice(
                        "It depends on the sorting algorithm's own extra memory, since the two-pointer scan itself only uses a couple of index variables.",
                        true,
                        "The two pointers used to scan for pairs need only constant extra space; the real extra-space cost comes from whatever the sort implementation itself needs internally, which varies by algorithm.",
                    ),
                    LessonChoice(
                        "O(1), because sorting is always done directly on the array with no extra memory at all.",
                        false,
                        "Not every sorting algorithm sorts with zero extra memory - many need some additional space internally, even if it's much less than a full second copy of the array.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Imagine fixing one number as an 'anchor' and then asking: what pair of the remaining numbers cancels it out to zero?\n\nThat's just the Two Sum II problem in disguise - find two numbers in a sorted list that sum to a specific target, the negative of the anchor.\n\nSort the whole array once, then walk through it, treating each value in turn as the anchor and using two pointers to search the rest for a matching pair, skipping repeated values along the way to avoid reporting the same triplet twice.",
            walkthrough = listOf(
                "Sort [-1, 0, 1, 2, -1, -4] to get [-4, -1, -1, 0, 1, 2].",
                "Anchor at index 0 (-4). Two pointers left=1 (-1), right=5 (2). Sum = -4 + -1 + 2 = -3, too small; move left forward. No triplet found for this anchor.",
                "Anchor at index 1 (-1). left=2 (-1), right=5 (2). Sum = -1 + -1 + 2 = 0 - a match! Record [-1, -1, 2].",
                "Move left and right inward; left=3 (0), right=4 (1). Sum = -1 + 0 + 1 = 0 - another match! Record [-1, 0, 1].",
                "Anchor at index 2 is also -1, the same as the previous anchor - skip it to avoid a duplicate.",
                "Remaining anchors find no further matches. Final result: [[-1, -1, 2], [-1, 0, 1]].",
            ),
            pseudocode = "sorted = nums sorted in increasing order\nresult = empty list\nfor i from 0 to length - 1:\n    if i > 0 and sorted[i] == sorted[i-1]: continue (skip duplicate anchor)\n    left = i + 1, right = length - 1\n    while left < right:\n        sum = sorted[i] + sorted[left] + sorted[right]\n        if sum < 0: left += 1\n        else if sum > 0: right -= 1\n        else:\n            add [sorted[i], sorted[left], sorted[right]] to result\n            skip past duplicate values at left and right\n            left += 1, right -= 1\nreturn result",
            referenceCode = "fun threeSum(nums: IntArray): List<List<Int>> {\n    val sorted = nums.sorted()\n    val result = mutableListOf<List<Int>>()\n    for (i in sorted.indices) {\n        if (i > 0 && sorted[i] == sorted[i - 1]) continue\n        var left = i + 1\n        var right = sorted.size - 1\n        while (left < right) {\n            val sum = sorted[i] + sorted[left] + sorted[right]\n            when {\n                sum < 0 -> left++\n                sum > 0 -> right--\n                else -> {\n                    result.add(listOf(sorted[i], sorted[left], sorted[right]))\n                    while (left < right && sorted[left] == sorted[left + 1]) left++\n                    while (left < right && sorted[right] == sorted[right - 1]) right--\n                    left++\n                    right--\n                }\n            }\n        }\n    }\n    return result\n}",
            timeComplexity = "O(n squared), where n is the number of values in the array.\n\nSorting the array first costs O(n log n).\n\nThen, for each of the n values treated as an anchor, the two-pointer scan across the rest of the array costs up to O(n) - n anchors times an O(n) scan each gives O(n squared), which dominates the smaller O(n log n) sorting cost.",
            spaceComplexity = "Not counting the output, roughly O(log n) to O(n) extra space, depending on how the sort itself is implemented.\n\nThe two-pointer scan only needs a couple of index variables - constant space.\n\nThe real extra-space cost comes from whatever the sorting step needs internally.",
            alternatives = listOf(
                "Check every possible triplet directly with three nested loops.\nThis needs no sorting and is the most straightforward to write.\nBut it costs O(n cubed) time, which becomes far too slow once the array has even a few thousand values.",
                "For each pair of values, use a hash set to check whether their negative sum has already been seen as a third value.\nThis avoids sorting.\nBut carefully deduplicating triplets without the natural ordering that sorting provides is significantly trickier to get right.",
            ),
            commonMistakes = listOf(
                "Forgetting to skip duplicate values for the anchor value in the outer loop.\nWithout that skip, the exact same set of triplets gets rediscovered every time the anchor value repeats.\nA simple check comparing the current anchor to the previous one, skipping if they match, prevents this.",
                "Forgetting to skip duplicate values for left and right after finding a match.\nEven with the anchor deduplicated, adjacent equal values at the left or right pointer can produce the same triplet more than once.\nAfter recording a match, both pointers need their own skip-forward and skip-backward loops past equal neighbors.",
                "Trying to solve this with three independent nested loops out of habit, without noticing that fixing one value turns the rest into a Two Sum II problem.\nThat approach isn't wrong, but it's needlessly slower than the sort-plus-two-pointer technique.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: which combinations of a few values from a collection hit an exact target together?",
                "Fixing one value at a time can reduce a harder combination problem into a simpler two-value search.",
                "The result must avoid duplicates, which sorting makes much easier to manage.",
            ),
            takeaway = "When looking for combinations of three or more values hitting a target, sort first, fix one value at a time, and reduce the remaining search to a two-pointer scan - and always skip duplicate values, both for the fixed value and for the pointers, to avoid repeating the same answer.",
        ),
        nextSlug = "container-with-most-water",
    )

    private val containerWithMostWater = RoadmapLesson(
        slug = "container-with-most-water",
        title = "Container With Most Water",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an array of heights representing vertical lines drawn at each index, find two lines that, together with the x-axis, form a container holding the most water. Return the maximum amount of water the container can hold.",
        constraints = listOf(
            "The array has at least two heights.",
            "Heights are non-negative integers.",
            "The water held is limited by the shorter of the two chosen lines, since water would spill over it.",
            "Aim for linear time rather than checking every pair of lines.",
        ),
        examples = listOf(
            LessonExample("[1, 8, 6, 2, 5, 4, 8, 3, 7]", "49", "Lines at indices 1 (height 8) and 8 (height 7) are 7 apart, holding min(8,7) * 7 = 49 units of water - the maximum possible."),
            LessonExample("[1, 1]", "1", "The only two lines are 1 apart with height 1 each, holding min(1,1) * 1 = 1 unit."),
            LessonExample("[4, 3, 2, 1, 4]", "16", "The two lines at the very ends are both height 4, 4 apart, holding min(4,4) * 4 = 16 units."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "container-water-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the maximum water without checking every pair of lines directly?",
                choices = listOf(
                    LessonChoice(
                        "Start two pointers at the outermost lines, compute the water between them, then always move inward from whichever line is shorter.",
                        true,
                        "The width between the pointers only ever shrinks, so keeping the taller line and moving the shorter one inward is the only way a later pair could possibly hold more water than the current one.",
                    ),
                    LessonChoice(
                        "Check every pair of lines directly, computing the water each pair could hold.",
                        false,
                        "This finds the correct answer, but comparing every pair of lines costs O(n squared), which is far more work than necessary.",
                    ),
                    LessonChoice(
                        "Sort the heights from tallest to shortest, then pair up the two tallest.",
                        false,
                        "Sorting the heights destroys their original positions, and the width between two lines - which the answer directly depends on - is determined by their original indices, not by how tall they are.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "container-water-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use two pointers moving inward. Which one decides correctly which pointer to move?",
                choices = listOf(
                    LessonChoice(
                        text = "Always moves the left pointer inward, regardless of which line is taller.",
                        correct = false,
                        feedback = "If the right line happens to be the shorter one, moving left instead keeps the limiting, shorter line in place and only shrinks the width, which can never increase the water held.",
                        code = "fun maxArea(height: IntArray): Int {\n    var left = 0\n    var right = height.size - 1\n    var best = 0\n    while (left < right) {\n        val area = minOf(height[left], height[right]) * (right - left)\n        best = maxOf(best, area)\n        left++\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "Moves whichever pointer points to the shorter line inward, keeping the taller line in place.",
                        correct = true,
                        feedback = "The shorter line is what's limiting the current container's height, so only moving it inward gives any chance of finding a taller limiting line at a smaller width - moving the taller line could only ever make things worse.",
                        code = "fun maxArea(height: IntArray): Int {\n    var left = 0\n    var right = height.size - 1\n    var best = 0\n    while (left < right) {\n        val area = minOf(height[left], height[right]) * (right - left)\n        best = maxOf(best, area)\n        if (height[left] < height[right]) left++ else right--\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "Moves the pointer pointing to the taller line inward, keeping the shorter line in place.",
                        correct = false,
                        feedback = "Keeping the shorter, limiting line in place while shrinking the width can only ever hold the same or less water than before - it can never discover a better answer than what's already been checked.",
                        code = "fun maxArea(height: IntArray): Int {\n    var left = 0\n    var right = height.size - 1\n    var best = 0\n    while (left < right) {\n        val area = minOf(height[left], height[right]) * (right - left)\n        best = maxOf(best, area)\n        if (height[left] < height[right]) right-- else left++\n    }\n    return best\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "container-water-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version returns 0 for every input, even ones with a clear best container. What's the bug?",
                code = "var left = 0\nvar right = height.size - 1\nvar best = 0\nwhile (left < right) {\n    val area = minOf(height[left], height[right]) * (right - left)\n    if (height[left] < height[right]) left++ else right--\n}",
                choices = listOf(
                    LessonChoice(
                        "Change minOf(height[left], height[right]) to maxOf(height[left], height[right]).",
                        false,
                        "Using the taller line's height instead of the shorter one would overstate how much water the container could actually hold, since water spills over the shorter side.",
                    ),
                    LessonChoice(
                        "Change left++ to left += 1 and right-- to right -= 1.",
                        false,
                        "left += 1 and left++ do exactly the same thing - this change has no effect on the bug at all.",
                    ),
                    LessonChoice(
                        "Add best = maxOf(best, area) right after computing area, before deciding which pointer to move.",
                        true,
                        "Without ever updating best, every computed area is thrown away and the function always returns its initial value of 0 - best needs to track the largest area seen across every pair checked.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "container-water-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of heights, what is the time complexity of the two-pointer approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because every pair of lines is eventually compared.",
                        false,
                        "The two pointers move toward each other and never revisit a position, so not every pair actually gets compared - only n - 1 pairs total are ever checked.",
                    ),
                    LessonChoice(
                        "O(n), because each step moves exactly one pointer inward, and the pointers can move at most n - 1 times total before meeting.",
                        true,
                        "Every iteration of the loop moves the left pointer forward or the right pointer backward by one, and they start at most n - 1 apart, so the loop runs a number of times proportional to n.",
                    ),
                    LessonChoice(
                        "O(n log n), because the heights are effectively sorted as the pointers move.",
                        false,
                        "The pointers never sort anything - they simply move inward based on a single comparison each step, without reordering the array at all.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "container-water-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the two-pointer approach use?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only two index variables and a running best value are needed, regardless of array size.",
                        true,
                        "left, right, and best are three simple variables whose count never changes no matter how many heights are in the input.",
                    ),
                    LessonChoice(
                        "O(n), because the area for every pair must be stored to find the maximum.",
                        false,
                        "Only the single best area found so far needs to be remembered - there's no need to store every computed area at once.",
                    ),
                    LessonChoice(
                        "O(log n), because the two pointers divide the search space in half each step.",
                        false,
                        "The pointers move one step at a time toward each other, not by halving the remaining range the way a binary search would.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture standing between two fences of different heights and asking how much water could pool between them - the water level can never rise above the shorter fence, or it just spills over.\n\nStart by considering the widest possible container, using the two outermost fences.\n\nSince the shorter of those two fences is what's limiting how much water fits, moving the taller fence inward could only ever make things worse - so always move the shorter one instead, hoping to find a taller replacement even though the width shrinks.",
            walkthrough = listOf(
                "Heights [1, 8, 6, 2, 5, 4, 8, 3, 7]. left=0 (height 1), right=8 (height 7). Area = min(1,7) * 8 = 8. Best so far: 8.",
                "Left (height 1) is shorter, so move left inward to index 1 (height 8).",
                "Area = min(8,7) * 7 = 49. Best so far: 49.",
                "Right (height 7) is shorter now, so move right inward to index 7 (height 3).",
                "Area = min(8,3) * 6 = 18 - smaller than the current best, so best stays 49.",
                "Continue narrowing inward; no later pair beats 49. Return 49.",
            ),
            pseudocode = "left = 0, right = length - 1\nbest = 0\nwhile left < right:\n    area = min(height[left], height[right]) * (right - left)\n    best = max(best, area)\n    if height[left] < height[right]: left += 1\n    else: right -= 1\nreturn best",
            referenceCode = "fun maxArea(height: IntArray): Int {\n    var left = 0\n    var right = height.size - 1\n    var best = 0\n    while (left < right) {\n        val area = minOf(height[left], height[right]) * (right - left)\n        best = maxOf(best, area)\n        if (height[left] < height[right]) left++ else right--\n    }\n    return best\n}",
            timeComplexity = "O(n), where n is the number of heights.\n\nEach iteration of the loop moves exactly one of the two pointers one step closer to the other, and they start at most n - 1 positions apart.\n\nSo the loop can run at most n - 1 times before the pointers meet.",
            spaceComplexity = "O(1), because the algorithm only ever needs three simple variables: the left pointer, the right pointer, and the best area found so far.\n\nNone of these grow with how many heights are in the input array.",
            alternatives = listOf(
                "Check every pair of lines directly and compute the area each pair could hold, keeping track of the maximum.\nThis is simple and guaranteed correct with no clever reasoning needed.\nBut comparing every pair costs O(n squared), far slower than the two-pointer approach once there are many heights.",
                "For each line, search only to its right for a taller or equal line before giving up on that starting line.\nThis feels intuitive but doesn't actually reduce the total comparisons in the worst case.\nIt still risks O(n squared) behavior without the guarantee that moving the shorter pointer inward provides.",
            ),
            commonMistakes = listOf(
                "Moving the taller pointer inward instead of the shorter one.\nThe shorter line is always what limits the container's height, so keeping it in place while shrinking the width can only ever hold the same amount of water or less.\nOnly moving the shorter line gives any chance of finding a better answer.",
                "Using the taller line's height instead of the shorter one when computing the area.\nWater spills over the shorter side, so the container's actual height is always limited by whichever line is shorter, never the taller one.",
                "Forgetting to update the running best value after computing each pair's area.\nWithout tracking the best area seen so far across every step, the function has nothing meaningful to return at the end.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: which pair, chosen from opposite ends of a range, maximizes some combination of their values and their distance apart?",
                "A brute-force answer would check every pair, but one side of each pair is always limited by whichever value is smaller.",
                "Moving inward from both ends, guided by which side is currently worse, narrows the search without missing the true best answer.",
            ),
            takeaway = "When maximizing an area or value that's limited by the smaller of two boundary values, start from the widest possible boundaries and always move the limiting, shorter side inward - the wider, better-limited pair is never worth abandoning by moving the taller side instead.",
        ),
        nextSlug = "trapping-rain-water",
    )

    private val trappingRainWater = RoadmapLesson(
        slug = "trapping-rain-water",
        title = "Trapping Rain Water",
        difficulty = CurriculumDifficulty.HARD,
        description = "Given an array of heights representing an elevation map, where the width of each bar is 1, compute how much water it can trap after raining. Water rests in the dips between taller bars on either side.",
        constraints = listOf(
            "The array may be empty, in which case the answer is 0.",
            "Heights are non-negative integers.",
            "Water above any bar is limited by the shorter of the tallest bar to its left and the tallest bar to its right.",
            "Aim for linear time rather than recomputing the tallest bar on each side for every position.",
        ),
        examples = listOf(
            LessonExample("[0,1,0,2,1,0,1,3,2,1,2,1]", "6", "Water pools in several dips along the elevation map, totaling 6 units when added together."),
            LessonExample("[4,2,0,3,2,5]", "9", "The tall bars at both ends trap water over the lower ground between them."),
            LessonExample("[1,1,1]", "0", "A flat elevation map has nowhere for water to collect."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "trapping-water-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach computes the trapped water without recomputing the tallest bar on each side from scratch for every position?",
                choices = listOf(
                    LessonChoice(
                        "For each position, scan left to find the tallest bar so far and scan right to find the tallest bar so far, then use the smaller of the two.",
                        false,
                        "This is correct, but rescanning the entire array to the left and right for every single position costs O(n squared) in total.",
                    ),
                    LessonChoice(
                        "Precompute, in one pass, the tallest bar to the left of each position, and in another pass, the tallest bar to the right of each position; then combine them.",
                        true,
                        "Building both arrays takes two linear passes, and combining them for every position is a third linear pass - three passes total, each proportional to n, is still O(n) overall.",
                    ),
                    LessonChoice(
                        "Sort the bars by height and fill water starting from the tallest bar down to the shortest.",
                        false,
                        "Sorting the bars discards their original positions, which is exactly what determines which bars are 'to the left' or 'to the right' of a given dip - that positional relationship is essential to this problem.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "trapping-water-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three precompute left-max and right-max arrays. Which one combines them into the correct answer per position?",
                choices = listOf(
                    LessonChoice(
                        text = "Water above position i is min(leftMax[i], rightMax[i]) minus height[i].",
                        correct = true,
                        feedback = "The water level at any position is capped by the shorter of the two boundary walls - the tallest bar to the left and to the right - and subtracting the bar's own height gives exactly how much water sits above it.",
                        code = "fun trap(height: IntArray): Int {\n    val n = height.size\n    if (n == 0) return 0\n    val leftMax = IntArray(n)\n    val rightMax = IntArray(n)\n    leftMax[0] = height[0]\n    for (i in 1 until n) leftMax[i] = maxOf(leftMax[i - 1], height[i])\n    rightMax[n - 1] = height[n - 1]\n    for (i in n - 2 downTo 0) rightMax[i] = maxOf(rightMax[i + 1], height[i])\n    var total = 0\n    for (i in 0 until n) {\n        total += minOf(leftMax[i], rightMax[i]) - height[i]\n    }\n    return total\n}",
                    ),
                    LessonChoice(
                        text = "Water above position i is max(leftMax[i], rightMax[i]) minus height[i].",
                        correct = false,
                        feedback = "Using the taller of the two walls instead of the shorter one overstates the trapped water - water can only be held up to the height of the shorter surrounding wall, since it would spill over that side otherwise.",
                        code = "fun trap(height: IntArray): Int {\n    val n = height.size\n    if (n == 0) return 0\n    val leftMax = IntArray(n)\n    val rightMax = IntArray(n)\n    leftMax[0] = height[0]\n    for (i in 1 until n) leftMax[i] = maxOf(leftMax[i - 1], height[i])\n    rightMax[n - 1] = height[n - 1]\n    for (i in n - 2 downTo 0) rightMax[i] = maxOf(rightMax[i + 1], height[i])\n    var total = 0\n    for (i in 0 until n) {\n        total += maxOf(leftMax[i], rightMax[i]) - height[i]\n    }\n    return total\n}",
                    ),
                    LessonChoice(
                        text = "Sets leftMax[i] and rightMax[i] to just height[i] at each position, without carrying forward the running maximum from the previous position.",
                        correct = false,
                        feedback = "Without carrying the maximum forward, leftMax[i] and rightMax[i] only reflect the bar at position i itself, not the tallest bar seen so far in that direction - which loses exactly the information needed to know how high a wall exists on each side.",
                        code = "fun trap(height: IntArray): Int {\n    val n = height.size\n    if (n == 0) return 0\n    val leftMax = IntArray(n)\n    val rightMax = IntArray(n)\n    for (i in 0 until n) leftMax[i] = height[i]\n    for (i in 0 until n) rightMax[i] = height[i]\n    var total = 0\n    for (i in 0 until n) {\n        total += minOf(leftMax[i], rightMax[i]) - height[i]\n    }\n    return total\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "trapping-water-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version undercounts the trapped water for elevation maps with a tall peak in the middle. What's the bug?",
                code = "val leftMax = IntArray(n)\nval rightMax = IntArray(n)\nleftMax[0] = height[0]\nfor (i in 1 until n) leftMax[i] = height[i]\nrightMax[n - 1] = height[n - 1]\nfor (i in n - 2 downTo 0) rightMax[i] = maxOf(rightMax[i + 1], height[i])",
                choices = listOf(
                    LessonChoice(
                        "Change leftMax[0] = height[0] to leftMax[0] = 0.",
                        false,
                        "The very first position has no bar to its left at all, so its own height genuinely is the tallest, and only, 'wall' on that side - starting it at 0 would incorrectly ignore the first bar's own height.",
                    ),
                    LessonChoice(
                        "Change leftMax[i] = height[i] to leftMax[i] = maxOf(leftMax[i - 1], height[i]).",
                        true,
                        "Without carrying forward the running maximum from the previous position, leftMax[i] only ever reflects the current bar's own height, completely forgetting any taller bar that appeared earlier - the running max is what makes it represent 'the tallest bar so far'.",
                    ),
                    LessonChoice(
                        "Change the rightMax loop to also start from index 0 instead of n - 1.",
                        false,
                        "The rightMax array correctly needs to be built from the rightmost position backward, since that's the direction 'tallest so far, looking rightward' has to accumulate from - the bug described is entirely in the leftMax loop.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "trapping-water-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of bars, what is the time complexity of the two-pass, left-max/right-max approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because for each position, both directions must be rescanned.",
                        false,
                        "The left-max and right-max arrays are built once each in a single pass, and then read directly - no position triggers a fresh rescan of the array.",
                    ),
                    LessonChoice(
                        "O(n), because building leftMax is one pass, building rightMax is one pass, and combining them into the answer is a third pass.",
                        true,
                        "Three passes, each visiting every position exactly once and doing a constant amount of work per position, add up to O(n) overall.",
                    ),
                    LessonChoice(
                        "O(n log n), because the left-max and right-max values must be sorted before combining.",
                        false,
                        "Nothing about leftMax or rightMax needs sorting - each is built directly by carrying a running maximum forward or backward through the array in its original order.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "trapping-water-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the leftMax and rightMax arrays approach use?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only the trapped water total needs to be tracked.",
                        false,
                        "The running total is just one variable, but the leftMax and rightMax arrays themselves each need one slot per position in the input, so they scale with the array's size.",
                    ),
                    LessonChoice(
                        "O(log n), because the maximum values only need to be updated occasionally as new taller bars appear.",
                        false,
                        "How often a value actually changes doesn't determine the array's size - leftMax and rightMax both have exactly n slots regardless of how many times each slot's value differs from its neighbor.",
                    ),
                    LessonChoice(
                        "O(n), because the leftMax and rightMax arrays each need one entry per position in the input.",
                        true,
                        "Both arrays are the same length as the input array, so the extra memory used grows directly with how many bars there are.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture an elevation map after rain, and ask how deep the water sits above any one particular bar.\n\nThat depends on two walls: the tallest bar somewhere to its left, and the tallest bar somewhere to its right. Water can only rise as high as the shorter of those two walls, since it would spill over the lower side otherwise.\n\nSo the plan is: for every position, find the tallest wall to its left, find the tallest wall to its right, take the shorter of the two, and subtract the bar's own height to see how much water sits on top of it.",
            walkthrough = listOf(
                "Heights [0,1,0,2,1,0,1,3,2,1,2,1]. Build leftMax by carrying the running max forward: [0,1,1,2,2,2,2,3,3,3,3,3].",
                "Build rightMax by carrying the running max backward: [3,3,3,3,3,3,3,3,2,2,2,1].",
                "At position 2 (height 0): min(leftMax[2]=1, rightMax[2]=3) - 0 = 1 unit of water.",
                "At position 5 (height 0): min(leftMax[5]=2, rightMax[5]=3) - 0 = 2 units of water.",
                "At position 6 (height 1): min(leftMax[6]=2, rightMax[6]=3) - 1 = 1 unit of water.",
                "Adding up water at every position where it's positive gives a total of 6.",
            ),
            pseudocode = "n = length of height\nif n == 0: return 0\nleftMax = array of n zeros; leftMax[0] = height[0]\nfor i from 1 to n-1: leftMax[i] = max(leftMax[i-1], height[i])\nrightMax = array of n zeros; rightMax[n-1] = height[n-1]\nfor i from n-2 down to 0: rightMax[i] = max(rightMax[i+1], height[i])\ntotal = 0\nfor i from 0 to n-1: total += min(leftMax[i], rightMax[i]) - height[i]\nreturn total",
            referenceCode = "fun trap(height: IntArray): Int {\n    val n = height.size\n    if (n == 0) return 0\n    val leftMax = IntArray(n)\n    val rightMax = IntArray(n)\n    leftMax[0] = height[0]\n    for (i in 1 until n) leftMax[i] = maxOf(leftMax[i - 1], height[i])\n    rightMax[n - 1] = height[n - 1]\n    for (i in n - 2 downTo 0) rightMax[i] = maxOf(rightMax[i + 1], height[i])\n    var total = 0\n    for (i in 0 until n) {\n        total += minOf(leftMax[i], rightMax[i]) - height[i]\n    }\n    return total\n}",
            timeComplexity = "O(n), where n is the number of bars in the elevation map.\n\nBuilding leftMax is one linear pass, building rightMax is another linear pass, and adding up the trapped water at each position is a third linear pass.\n\nThree passes, each doing a constant amount of work per position, add up to work proportional to n, not n squared.",
            spaceComplexity = "O(n), because the leftMax and rightMax arrays each need exactly one slot per position in the input.\n\nBoth arrays grow in size directly alongside the elevation map itself.\n\nA more advanced two-pointer version of this solution can bring this down to O(1), but the two-array version is the clearest place to start.",
            alternatives = listOf(
                "For each position, scan left and scan right from scratch to find the tallest bar on each side.\nThis needs no extra arrays at all.\nBut rescanning the whole array in both directions for every single position costs O(n squared), far slower than precomputing leftMax and rightMax once.",
                "Use two pointers moving inward from both ends, tracking a running leftMax and rightMax as you go, and always processing whichever side currently has the smaller max.\nThis achieves the same O(n) time while using only O(1) extra space, since it avoids storing the full leftMax and rightMax arrays.\nBut the reasoning for why it's correct is subtler and easier to get wrong than the more explicit two-array version.",
            ),
            commonMistakes = listOf(
                "Forgetting to carry the running maximum forward, or backward, when building leftMax and rightMax.\nWithout that, each array entry only reflects that single bar's own height, not the tallest bar seen so far in that direction, which defeats the entire purpose of the arrays.",
                "Using the taller of leftMax and rightMax instead of the shorter one when computing water at a position.\nWater can only rise as high as the shorter surrounding wall, since it spills over the lower side - using the taller wall overstates how much water is actually trapped.",
                "Forgetting the empty-array edge case and trying to access height[0] or height[n-1] on an array with no elements.\nAn elevation map with no bars at all traps no water and should return 0 immediately.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what's the boundary or ceiling that limits some value at each position, based on what's around it in both directions?",
                "A brute-force answer would rescan the entire array from every position - precomputing 'best so far from the left' and 'best so far from the right' can replace that rescanning.",
                "The answer at each position depends on information from both directions at once, which two separate single-direction passes can capture independently before combining them.",
            ),
            takeaway = "When a value at each position depends on the best (or worst) seen so far in both directions, precompute a running value from the left and a running value from the right in two separate linear passes, then combine them - it replaces repeated rescanning with a fixed, small number of passes.",
        ),
        nextSlug = "best-time-to-buy-and-sell-stock",
    )

    private val bestTimeToBuyAndSellStock = RoadmapLesson(
        slug = "best-time-to-buy-and-sell-stock",
        title = "Best Time to Buy and Sell Stock",
        difficulty = CurriculumDifficulty.EASY,
        description = "Given an array of daily stock prices, choose a single day to buy and a later day to sell to maximize profit. Return the maximum profit achievable, or 0 if no profit is possible.",
        constraints = listOf(
            "Prices are given in order, one per day.",
            "The sell day must come strictly after the buy day.",
            "If no profitable pair of days exists, the answer is 0, not a negative number.",
            "Aim for a single pass through the prices rather than checking every pair of days.",
        ),
        examples = listOf(
            LessonExample("[7,1,5,3,6,4]", "5", "Buy on day 2 (price 1) and sell on day 5 (price 6) for a profit of 5."),
            LessonExample("[7,6,4,3,1]", "0", "Prices only fall, so no profitable buy-then-sell pair exists - the answer is 0."),
            LessonExample("[2,4,1]", "2", "Buy at 2, sell at 4, for a profit of 2 - buying at 1 has no later day to sell at a profit."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "best-time-stock-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the maximum profit in a single pass through the prices?",
                choices = listOf(
                    LessonChoice(
                        "Check every pair of a buy day and a later sell day directly, computing the profit for each pair.",
                        false,
                        "This finds the correct answer, but checking every pair of days costs O(n squared) - far more work than a single pass needs.",
                    ),
                    LessonChoice(
                        "Track the lowest price seen so far while scanning forward, and at each day compute the profit from selling today versus that lowest price, keeping the best profit found.",
                        true,
                        "Because the buy day must come before the sell day, remembering only the lowest price seen so far - not every past price - is enough to compute the best possible profit ending on each day.",
                    ),
                    LessonChoice(
                        "Sort the prices, then subtract the smallest from the largest.",
                        false,
                        "Sorting scrambles the order the prices actually occurred in, but the sell day must come after the buy day - the largest price might have occurred before the smallest one, which wouldn't be a valid trade.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "best-time-stock-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three track a running minimum price. Which one computes the maximum profit correctly?",
                choices = listOf(
                    LessonChoice(
                        text = "Updates the running minimum price after computing today's potential profit against it.",
                        correct = true,
                        feedback = "Computing profit against the minimum seen strictly before today, before updating it with today's own price, correctly treats today as a possible sell day using only earlier buy days.",
                        code = "fun maxProfit(prices: IntArray): Int {\n    var minPrice = Int.MAX_VALUE\n    var best = 0\n    for (price in prices) {\n        best = maxOf(best, price - minPrice)\n        minPrice = minOf(minPrice, price)\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "Updates the running minimum price before computing today's potential profit against it.",
                        correct = false,
                        feedback = "Updating the minimum first can make today's own price the 'buy' price used against itself, always producing a profit of 0 for that day - the minimum used for today's profit calculation must come from a strictly earlier day.",
                        code = "fun maxProfit(prices: IntArray): Int {\n    var minPrice = Int.MAX_VALUE\n    var best = 0\n    for (price in prices) {\n        minPrice = minOf(minPrice, price)\n        best = maxOf(best, price - minPrice)\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "Tracks a running maximum price instead of a running minimum, and computes maxPrice minus today's price at each day.",
                        correct = false,
                        feedback = "That computes the profit from selling today after buying at some later, higher price, which is backwards - the buy day must come before the sell day, not after it.",
                        code = "fun maxProfit(prices: IntArray): Int {\n    var maxPrice = Int.MIN_VALUE\n    var best = 0\n    for (price in prices) {\n        best = maxOf(best, maxPrice - price)\n        maxPrice = maxOf(maxPrice, price)\n    }\n    return best\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "best-time-stock-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version returns a negative number for prices that only ever fall. What's the bug?",
                code = "var minPrice = prices[0]\nvar best = Int.MIN_VALUE\nfor (price in prices) {\n    best = maxOf(best, price - minPrice)\n    minPrice = minOf(minPrice, price)\n}",
                choices = listOf(
                    LessonChoice(
                        "Change prices[0] to Int.MAX_VALUE for the initial minPrice.",
                        false,
                        "Starting minPrice at the first price is actually fine here, since the first day is a valid possible buy day - the bug is in how best is initialized, not minPrice.",
                    ),
                    LessonChoice(
                        "Change minOf(minPrice, price) to maxOf(minPrice, price).",
                        false,
                        "That would track the running maximum instead of the running minimum, which computes something entirely different from 'the lowest price seen so far to buy at'.",
                    ),
                    LessonChoice(
                        "Change the initial value of best from Int.MIN_VALUE to 0.",
                        true,
                        "The problem defines 'no profitable trade found' as a result of 0, not a negative number - starting best at 0, representing 'do nothing', ensures the answer never goes negative.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "best-time-stock-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of days, what is the time complexity of the running-minimum approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because every day must be compared against every other day.",
                        false,
                        "Only the running minimum from earlier days is used, not a comparison against every other day individually - each day only needs constant work against that single running value.",
                    ),
                    LessonChoice(
                        "O(n), because each day is visited once and does a constant amount of work: one comparison for profit, one comparison for the minimum.",
                        true,
                        "One pass through the prices, with a fixed, small amount of work at each day, gives time proportional to n.",
                    ),
                    LessonChoice(
                        "O(n log n), because the prices must be sorted first to find the minimum and maximum.",
                        false,
                        "Sorting isn't needed here - the running minimum is tracked directly as the array is scanned in its original order.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "best-time-stock-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the running-minimum approach use?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only two variables - the running minimum price and the best profit so far - are needed regardless of how many days there are.",
                        true,
                        "minPrice and best are two fixed variables whose count never changes no matter how large the prices array is.",
                    ),
                    LessonChoice(
                        "O(n), because the profit for every possible pair of days must be stored to find the best one.",
                        false,
                        "Only the single best profit found so far needs to be remembered - there's no need to store the profit for every pair.",
                    ),
                    LessonChoice(
                        "O(log n), because the running minimum requires a sorted structure to update efficiently.",
                        false,
                        "The running minimum is just a single variable updated with a simple comparison each day - no sorted structure of any kind is involved.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Imagine watching stock prices day by day, and every day asking two questions: 'If I had bought at the cheapest price I've seen so far and sold today, how much would I make?' and 'Is today's price the new cheapest I've seen?'\n\nKeep track of the answer to both questions as you go, always remembering the best profit found and the lowest price seen so far.\n\nBy the time you reach the end, you've effectively considered selling on every day against the best possible earlier buy day, without ever needing to look backward.",
            walkthrough = listOf(
                "Prices [7,1,5,3,6,4]. minPrice starts at a very large number, best starts at 0.",
                "Day 0, price 7: profit = 7 minus a very large number is negative, so best stays 0. Update minPrice to 7.",
                "Day 1, price 1: profit = 1 - 7 = -6, best stays 0. Update minPrice to 1.",
                "Day 2, price 5: profit = 5 - 1 = 4, best becomes 4. minPrice stays 1.",
                "Day 3, price 3: profit = 3 - 1 = 2, best stays 4. Day 4, price 6: profit = 6 - 1 = 5, best becomes 5.",
                "Day 5, price 4: profit = 4 - 1 = 3, best stays 5. Return 5.",
            ),
            pseudocode = "minPrice = a very large number\nbest = 0\nfor each price in prices:\n    best = max(best, price - minPrice)\n    minPrice = min(minPrice, price)\nreturn best",
            referenceCode = "fun maxProfit(prices: IntArray): Int {\n    var minPrice = Int.MAX_VALUE\n    var best = 0\n    for (price in prices) {\n        best = maxOf(best, price - minPrice)\n        minPrice = minOf(minPrice, price)\n    }\n    return best\n}",
            timeComplexity = "O(n), where n is the number of days.\n\nEach day is visited exactly once, and at each day only a constant amount of work happens: one comparison to update the best profit, and one comparison to update the running minimum price.\n\nOne pass, constant work per step - that scales in a straight line with n.",
            spaceComplexity = "O(1), because the algorithm only needs two variables: the lowest price seen so far, and the best profit found so far.\n\nNeither of these grows with how many days are in the input - they're simply overwritten as better values are found.",
            alternatives = listOf(
                "Check every pair of a buy day and a later sell day directly, computing the profit for each pair and keeping the best one.\nThis is the most straightforward to reason about.\nBut comparing every pair of days costs O(n squared), far slower than tracking a single running minimum.",
                "Use a variation of Kadane's maximum subarray algorithm on the day-to-day price changes.\nThis reframes the problem as finding the best contiguous run of positive change.\nIt reaches the same O(n) time, but is a less direct way to think about the problem than tracking the lowest price seen so far.",
            ),
            commonMistakes = listOf(
                "Updating the running minimum price before computing that day's profit against it.\nThat lets today's own price be used as its own buy price, which always produces a profit of exactly 0 for that day and can hide a better answer using an actually earlier day.\nThe profit must be computed first, using the minimum from strictly before today, and only then should the minimum be updated with today's price.",
                "Starting the best profit at a very negative number instead of 0.\nThe problem defines 'no profitable trade exists' as an answer of 0, representing simply not trading at all.\nStarting best at 0 ensures the function never reports a negative profit.",
                "Trying to buy at the highest price and sell at the lowest, or ignoring that the sell day must come strictly after the buy day.\nA higher price appearing before a lower one doesn't represent a valid trade, since you can't sell before you buy.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what is the best 'gap' between an earlier low point and a later point, scanned in order?",
                "A brute-force answer would check every pair of positions, but only the best earlier value actually matters at each later position.",
                "The order of the input matters - an earlier value can only pair with a later one, never the reverse.",
            ),
            takeaway = "When the best answer only ever depends on the best value seen so far earlier in the sequence, track that running best in a single variable as you scan forward - it replaces checking every pair with one linear pass.",
        ),
        nextSlug = "longest-substring-without-repeating-characters",
    )

    private val longestSubstringWithoutRepeating = RoadmapLesson(
        slug = "longest-substring-without-repeating-characters",
        title = "Longest Substring Without Repeating Characters",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given a string, find the length of the longest substring that contains no repeated characters. A substring must be made of consecutive characters from the original string.",
        constraints = listOf(
            "The string may be empty, in which case the answer is 0.",
            "The string may contain any mix of letters, digits, and symbols.",
            "A substring's characters must be consecutive in the original string - reordering is not allowed.",
            "Aim for linear time using a sliding window rather than checking every possible substring.",
        ),
        examples = listOf(
            LessonExample("\"abcabcbb\"", "3", "\"abc\" is the longest run with no repeats, giving length 3."),
            LessonExample("\"bbbbb\"", "1", "Every character is the same, so the longest repeat-free run is just a single character."),
            LessonExample("\"pwwkew\"", "3", "\"wke\" has length 3; note that \"pwke\" is not a substring since its letters aren't consecutive in the original string."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "longest-substring-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the answer without checking every possible substring directly?",
                choices = listOf(
                    LessonChoice(
                        "Check every possible substring, and for each one scan it to check whether it has any repeated characters.",
                        false,
                        "This finds the correct answer, but there are O(n squared) substrings, and checking each one for repeats can add even more work on top - far slower than necessary.",
                    ),
                    LessonChoice(
                        "Slide a window across the string, expanding the right edge and tracking characters seen inside it; when a repeat is found, shrink the window from the left until the repeat is gone.",
                        true,
                        "This 'sliding window' technique keeps the window always free of repeats by growing and shrinking it as needed, visiting each character only a small, bounded number of times overall.",
                    ),
                    LessonChoice(
                        "Sort the characters of the string, then look for the longest run of distinct adjacent characters.",
                        false,
                        "Sorting the string destroys the original order, but the problem specifically requires a substring - a run of consecutive characters in the original order, not a rearrangement.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-substring-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use a sliding window with a set of characters currently inside it. Which one shrinks the window correctly on a repeat?",
                choices = listOf(
                    LessonChoice(
                        text = "When a repeat is found, clears the entire set and restarts the window from the very beginning of the string.",
                        correct = false,
                        feedback = "Restarting from the beginning of the string every time a repeat is found throws away useful progress and can make the total work quadratic instead of linear.",
                        code = "fun lengthOfLongestSubstring(s: String): Int {\n    var best = 0\n    var left = 0\n    val seen = mutableSetOf<Char>()\n    for (right in s.indices) {\n        while (s[right] in seen) {\n            seen.clear()\n            left = 0\n        }\n        seen.add(s[right])\n        best = maxOf(best, right - left + 1)\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "When a repeat is found, removes only the newly added right-edge character from the set instead of shrinking from the left.",
                        correct = false,
                        feedback = "Removing the right character doesn't shrink the window from the left at all - it leaves the earlier occurrence of the repeated character still inside the window, so the repeat is never actually resolved.",
                        code = "fun lengthOfLongestSubstring(s: String): Int {\n    var best = 0\n    var left = 0\n    val seen = mutableSetOf<Char>()\n    for (right in s.indices) {\n        if (s[right] in seen) {\n            seen.remove(s[right])\n        } else {\n            seen.add(s[right])\n            best = maxOf(best, right - left + 1)\n        }\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "When a repeat is found, removes characters from the left edge of the window one at a time, advancing left, until the repeated character is no longer in the set.",
                        correct = true,
                        feedback = "Shrinking strictly from the left, one character at a time, until the specific repeated character is removed, correctly restores the 'no repeats' property while keeping as much of the window as possible.",
                        code = "fun lengthOfLongestSubstring(s: String): Int {\n    var best = 0\n    var left = 0\n    val seen = mutableSetOf<Char>()\n    for (right in s.indices) {\n        while (s[right] in seen) {\n            seen.remove(s[left])\n            left++\n        }\n        seen.add(s[right])\n        best = maxOf(best, right - left + 1)\n    }\n    return best\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-substring-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version returns a length that's sometimes too small. What's the bug?",
                code = "var best = 0\nvar left = 0\nval seen = mutableSetOf<Char>()\nfor (right in s.indices) {\n    while (s[right] in seen) {\n        seen.remove(s[left])\n        left++\n    }\n    seen.add(s[right])\n}",
                choices = listOf(
                    LessonChoice(
                        "Add best = maxOf(best, right - left + 1) right after adding s[right] to the set.",
                        true,
                        "Without updating best inside the loop, the window's length is never actually recorded anywhere - the function always returns its initial value of 0 unless something else changes it, which nothing here does.",
                    ),
                    LessonChoice(
                        "Change seen.remove(s[left]) to seen.remove(s[right]).",
                        false,
                        "The character that needs removing from the left edge of the window is s[left], not s[right] - removing s[right] would remove the very character causing the conflict from the wrong side entirely.",
                    ),
                    LessonChoice(
                        "Change left++ to left += 2.",
                        false,
                        "Jumping the left pointer by two could skip past a character that's still legitimately part of the window, shrinking it more than necessary rather than fixing the missing length tracking.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-substring-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the length of the string, what is the time complexity of the sliding window approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because for every right position, the left pointer might scan all the way back to the start.",
                        false,
                        "The left pointer only ever moves forward, never backward - across the whole algorithm it can move at most n times total, not n times for every right position.",
                    ),
                    LessonChoice(
                        "O(n), because both left and right pointers only ever move forward, together visiting each character a small, constant number of times overall.",
                        true,
                        "Since left never moves backward and right never moves backward, and both are bounded by the string's length, the combined total movement across the whole run is proportional to n.",
                    ),
                    LessonChoice(
                        "O(n log n), because the characters in the window must be kept in sorted order.",
                        false,
                        "The set used here only needs to answer 'have I seen this character in the current window?' - it doesn't need to maintain any particular order among the characters.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-substring-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the sliding window's character set use?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because the set can hold as many characters as the length of the string.",
                        false,
                        "The set only ever holds characters currently inside the window, and it also can't hold more distinct characters than exist in the alphabet being used - for typical text that's a small, fixed bound, not something that grows with n.",
                    ),
                    LessonChoice(
                        "O(log n), because sets are typically implemented with a balanced structure that grows slowly.",
                        false,
                        "How the set organizes its contents internally isn't what matters here - what matters is the maximum number of distinct characters it could ever hold at once, which is bounded by the character set being used, not by n.",
                    ),
                    LessonChoice(
                        "O(min(n, alphabet size)), because the set can never hold more characters than either the string's length or the number of distinct characters possible.",
                        true,
                        "The window can never contain more characters than the string has, and it also can never contain more than one of each possible character - whichever of those two limits is smaller caps how large the set can get.",
                    ),
                ),
                glossary = listOf(
                    GlossaryTerm("O(min(n, alphabet size))", "Bounded by whichever is smaller: how long the input is, or how many distinct characters could ever appear."),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture a window sliding across the string, always trying to grow as wide as possible while never containing a repeated character.\n\nExpand the window by moving its right edge forward, adding the new character.\n\nIf that new character is already inside the window, shrink the window from the left edge - one character at a time - until the repeat is gone, then keep expanding.\n\nThe longest the window ever gets, at any point during this process, is the answer.",
            walkthrough = listOf(
                "String \"abcabcbb\". left=0, right starts moving forward. Window grows: 'a', 'ab', 'abc' - best length so far is 3.",
                "right moves to the second 'a' (index 3). 'a' is already in the window, so shrink from the left: remove 'a' at index 0, left becomes 1.",
                "Now the window is 'bc', and 'a' is no longer in it. Add the new 'a': window is 'bca', length 3. Best stays 3.",
                "Continue similarly: the window keeps sliding, occasionally shrinking when a repeat is found, but it never exceeds length 3 again for this particular string.",
                "By the end of the scan, the longest window ever achieved was length 3 (\"abc\").",
                "Return 3.",
            ),
            pseudocode = "best = 0, left = 0\nseen = empty set of characters\nfor right from 0 to length - 1:\n    while s[right] is in seen:\n        remove s[left] from seen\n        left += 1\n    add s[right] to seen\n    best = max(best, right - left + 1)\nreturn best",
            referenceCode = "fun lengthOfLongestSubstring(s: String): Int {\n    var best = 0\n    var left = 0\n    val seen = mutableSetOf<Char>()\n    for (right in s.indices) {\n        while (s[right] in seen) {\n            seen.remove(s[left])\n            left++\n        }\n        seen.add(s[right])\n        best = maxOf(best, right - left + 1)\n    }\n    return best\n}",
            timeComplexity = "O(n), where n is the length of the string.\n\nThe right pointer moves forward once per character, and the left pointer also only ever moves forward, never backward.\n\nAcross the whole run of the algorithm, both pointers together take at most about 2n steps, which is still O(n).",
            spaceComplexity = "O(min(n, alphabet size)) - bounded by whichever is smaller, the length of the string or the number of distinct characters possible.\n\nThe set only ever holds the characters currently inside the window, and it can never contain more than one of each distinct character.\n\nFor typical text, that alphabet size is small and fixed, so in practice this behaves like constant extra space.",
            alternatives = listOf(
                "Check every possible substring directly, and for each one scan it to see whether it has any repeated characters.\nThis is the most direct way to think about the problem.\nBut with O(n squared) substrings, each needing its own scan to check for repeats, this is far slower than a single sliding-window pass.",
                "Use a map from character to its most recent index instead of a set, and jump the left pointer directly to just past the repeated character's last position instead of removing one character at a time.\nThis can skip several shrink steps at once in some cases.\nIt reaches the same O(n) time complexity, but the plain set-based version is simpler to reason about and implement correctly first.",
            ),
            commonMistakes = listOf(
                "Restarting the window from the very beginning of the string whenever a repeat is found, instead of shrinking it from the left.\nThat throws away progress that's still valid and can make the total amount of work grow much faster than a linear pass should.\nOnly the characters up to and including the earlier occurrence of the repeat need to be removed, not the whole window.",
                "Removing the wrong character from the set when shrinking the window, such as removing the newly added right-edge character instead of the left-edge one.\nThat leaves the actual repeated character still inside the window, so the conflict is never truly resolved.\nShrinking must always happen from the left edge, removing s[left] and advancing left.",
                "Forgetting to update the best answer inside the loop after growing the window.\nWithout that update, the function has no way to record how long the window ever got, no matter how well the rest of the logic works.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what's the longest, or shortest, contiguous run of a sequence that satisfies some condition?",
                "A brute-force answer would check every possible contiguous range, but the condition can be maintained incrementally as a window grows and shrinks.",
                "Characters or elements need to stay unique, or satisfy some count-based constraint, only within the current window - not across the whole input.",
            ),
            takeaway = "When looking for the longest (or shortest) contiguous run that satisfies a condition, use a sliding window: grow it from the right, and shrink it from the left only when the condition is violated - that turns an O(n squared) substring check into a single linear pass.",
        ),
        nextSlug = "longest-repeating-character-replacement",
    )

    private val longestRepeatingCharacterReplacement = RoadmapLesson(
        slug = "longest-repeating-character-replacement",
        title = "Longest Repeating Character Replacement",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given a string of uppercase letters and an integer k, you may replace up to k characters in the string with any other uppercase letter. Return the length of the longest substring you can make consist of a single repeated letter after those replacements.",
        constraints = listOf(
            "The string contains only uppercase English letters.",
            "0 <= k <= length of the string.",
            "Replacements can be any letters, and you don't need to specify which ones - only the resulting length matters.",
            "Aim for a linear-time sliding window rather than trying every possible substring and replacement combination.",
        ),
        examples = listOf(
            LessonExample("s = \"ABAB\", k = 2", "4", "Replacing the two Bs with As (or vice versa) makes the whole string one repeated letter."),
            LessonExample("s = \"AABABBA\", k = 1", "4", "The window \"BABB\" has three Bs and one A; replacing that one A with B gives \"BBBB\", a repeated run of length 4."),
            LessonExample("s = \"ABCDE\", k = 1", "2", "With every letter different, one replacement can only ever make two matching characters, like \"AA\" or \"BB\"."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "longest-repeating-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the answer without trying every possible substring and replacement combination?",
                choices = listOf(
                    LessonChoice(
                        "For each possible substring, count how many characters differ from its most common letter, and check whether that count is at most k.",
                        false,
                        "This finds the correct answer, but checking every one of the O(n squared) substrings this way is much more work than a sliding window needs.",
                    ),
                    LessonChoice(
                        "Slide a window across the string, tracking the count of each letter inside it; the window is valid as long as its length minus the count of its most frequent letter is at most k, shrinking from the left when it isn't.",
                        true,
                        "The number of characters that would need replacing in a window is exactly its length minus how many of its most common letter it already has - keeping that value at most k, using a sliding window, avoids ever re-scanning from scratch.",
                    ),
                    LessonChoice(
                        "Try replacing every possible combination of up to k characters directly and check the resulting string each time.",
                        false,
                        "The number of ways to choose which characters to replace grows extremely quickly, making this approach impractical even for modestly sized strings.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-repeating-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three track letter counts in the window and the count of the most frequent letter. Which one correctly decides when to shrink?",
                choices = listOf(
                    LessonChoice(
                        text = "Shrinks the window from the left whenever the window length minus maxCount (the highest count of any letter seen in any window so far) exceeds k.",
                        correct = true,
                        feedback = "Window length minus maxCount is exactly how many characters in the current window would need replacing - shrinking only when that exceeds k keeps the window as large as possible while remaining valid.",
                        code = "fun characterReplacement(s: String, k: Int): Int {\n    val counts = IntArray(26)\n    var left = 0\n    var maxCount = 0\n    var best = 0\n    for (right in s.indices) {\n        counts[s[right] - 'A']++\n        maxCount = maxOf(maxCount, counts[s[right] - 'A'])\n        while (right - left + 1 - maxCount > k) {\n            counts[s[left] - 'A']--\n            left++\n        }\n        best = maxOf(best, right - left + 1)\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "Shrinks the window from the left whenever the window length minus the count of the least frequent letter exceeds k.",
                        correct = false,
                        feedback = "Using the least frequent letter's count instead of the most frequent one doesn't represent 'how many characters would need replacing to make the window uniform' - it's the most common letter that determines how few replacements are needed.",
                        code = "fun characterReplacement(s: String, k: Int): Int {\n    val counts = IntArray(26)\n    var left = 0\n    var best = 0\n    for (right in s.indices) {\n        counts[s[right] - 'A']++\n        val minCount = counts.filter { it > 0 }.minOrNull() ?: 0\n        while (right - left + 1 - minCount > k) {\n            counts[s[left] - 'A']--\n            left++\n        }\n        best = maxOf(best, right - left + 1)\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "Recomputes the count of the most frequent letter in the entire current window from scratch on every step, instead of tracking a running maxCount.",
                        correct = false,
                        feedback = "This produces a correct answer, but rescanning all 26 letter counts to find the current maximum on every single step adds significant extra work compared to tracking maxCount as a running value.",
                        code = "fun characterReplacement(s: String, k: Int): Int {\n    val counts = IntArray(26)\n    var left = 0\n    var best = 0\n    for (right in s.indices) {\n        counts[s[right] - 'A']++\n        val maxCount = counts.max()\n        while (right - left + 1 - maxCount > k) {\n            counts[s[left] - 'A']--\n            left++\n        }\n        best = maxOf(best, right - left + 1)\n    }\n    return best\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-repeating-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version sometimes reports a window as valid when it actually needs more than k replacements. What's the bug?",
                code = "val counts = IntArray(26)\nvar left = 0\nvar maxCount = 0\nvar best = 0\nfor (right in s.indices) {\n    counts[s[right] - 'A']++\n    maxCount = maxOf(maxCount, counts[s[right] - 'A'])\n    if (right - left + 1 - maxCount > k) {\n        counts[s[left] - 'A']--\n        left++\n    }\n    best = maxOf(best, right - left + 1)\n}",
                choices = listOf(
                    LessonChoice(
                        "Change counts[s[right] - 'A']++ to counts[s[right] - 'A'] += 2.",
                        false,
                        "Counting each character twice would corrupt every count in the window, making the maxCount tracking meaningless rather than fixing the shrink logic.",
                    ),
                    LessonChoice(
                        "Change the if before the shrink to a while.",
                        true,
                        "A single if only shrinks the window by one character even if it's still invalid afterward - a while loop is needed so the window keeps shrinking from the left until it's valid again, however many steps that takes.",
                    ),
                    LessonChoice(
                        "Change maxOf(maxCount, counts[s[right] - 'A']) to minOf(maxCount, counts[s[right] - 'A']).",
                        false,
                        "Tracking the minimum instead of the maximum count would measure the wrong thing entirely - maxCount needs to track the highest count of any single letter seen, which is what tells us how few replacements a window needs.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-repeating-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the length of the string, what is the time complexity of the sliding window approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because for every right position, the left pointer might shrink all the way back to the start.",
                        false,
                        "The left pointer only ever moves forward across the entire run of the algorithm, never resetting to the start - its total movement is bounded by n, not by n for every right position.",
                    ),
                    LessonChoice(
                        "O(n), because both the left and right pointers only ever move forward, together taking at most a small constant multiple of n total steps.",
                        true,
                        "Right moves forward once per character, and left also only ever moves forward - across the whole algorithm their combined movement is proportional to n.",
                    ),
                    LessonChoice(
                        "O(26n), which does not simplify to O(n) because of the extra factor of 26.",
                        false,
                        "26 is a fixed constant - the number of uppercase letters - and constants are dropped when describing time complexity, so O(26n) is the same growth rate as O(n).",
                    ),
                ),
            ),
            LessonQuestion(
                id = "longest-repeating-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the counts array use?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because a count must be stored for every character in the string.",
                        false,
                        "Only 26 possible uppercase letters exist, so only 26 slots are ever needed - not one slot per input character.",
                    ),
                    LessonChoice(
                        "O(log n), because the counts grow as the window gets larger.",
                        false,
                        "The individual count values can grow, but the number of slots in the array - which is what's measured here - stays fixed at 26 regardless of how large the window or string gets.",
                    ),
                    LessonChoice(
                        "O(1), because the counts array always has exactly 26 slots, one per uppercase letter, no matter how long the string is.",
                        true,
                        "26 is a fixed constant that never grows with the size of the input, which is exactly what constant extra space means.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture a window sliding across the string, and inside that window, imagine you'd replace every letter except whichever one appears most often - that's the fewest replacements needed to make the window a single repeated letter.\n\nAs the window grows, keep track of the highest count any single letter has reached inside it so far.\n\nIf the window's length minus that highest count ever exceeds k - meaning more than k characters would need replacing - shrink the window from the left until it's affordable again.",
            walkthrough = listOf(
                "s = \"AABABBA\", k = 1. Window grows: 'A' (counts: A=1), 'AA' (A=2), 'AAB' (A=2, B=1).",
                "maxCount is 2 (for A) so far. Window length 3 minus maxCount 2 = 1 replacement needed - within budget (k=1).",
                "Continue growing: 'AABA' (A=3, B=1). maxCount becomes 3. Length 4 minus 3 = 1 - still within budget.",
                "Grow to 'AABAB' (A=3, B=2). maxCount stays 3. Length 5 minus 3 = 2 - exceeds k=1! Shrink from the left: remove the first 'A', window becomes 'ABAB'.",
                "Length 4 minus maxCount (still 3, since maxCount only ever increases) = 1, valid again. Best length recorded so far: 4.",
                "The window continues sliding without ever exceeding length 4 again for this string. Return 4.",
            ),
            pseudocode = "counts = 26 zeros\nleft = 0, maxCount = 0, best = 0\nfor right from 0 to length - 1:\n    counts[s[right]] += 1\n    maxCount = max(maxCount, counts[s[right]])\n    while (right - left + 1) - maxCount > k:\n        counts[s[left]] -= 1\n        left += 1\n    best = max(best, right - left + 1)\nreturn best",
            referenceCode = "fun characterReplacement(s: String, k: Int): Int {\n    val counts = IntArray(26)\n    var left = 0\n    var maxCount = 0\n    var best = 0\n    for (right in s.indices) {\n        counts[s[right] - 'A']++\n        maxCount = maxOf(maxCount, counts[s[right] - 'A'])\n        while (right - left + 1 - maxCount > k) {\n            counts[s[left] - 'A']--\n            left++\n        }\n        best = maxOf(best, right - left + 1)\n    }\n    return best\n}",
            timeComplexity = "O(n), where n is the length of the string.\n\nThe right pointer moves forward once per character, and the left pointer also only ever moves forward, never resetting.\n\nAcross the entire run of the algorithm, both pointers together take a number of steps proportional to n.",
            spaceComplexity = "O(1), because the counts array always has exactly 26 slots - one for each uppercase English letter.\n\nThat size never changes no matter how long the input string is.",
            alternatives = listOf(
                "For every possible substring, count how many characters differ from its most common letter and check that count against k.\nThis is a direct translation of the problem statement.\nBut checking all O(n squared) substrings this way is far slower than maintaining a single sliding window.",
                "Recompute the most frequent letter's count from scratch by scanning all 26 letter counts every time the window changes, instead of tracking a running maxCount.\nThis still reaches a correct answer.\nBut it adds a constant-factor 26 of extra work at every step that tracking maxCount directly avoids.",
            ),
            commonMistakes = listOf(
                "Using an if statement instead of a while loop to shrink the window.\nA single shrink step might not be enough to bring the window back within budget - the window needs to keep shrinking from the left until it satisfies the k-replacement limit again, however many steps that takes.",
                "Tracking the count of the least frequent letter instead of the most frequent one.\nThe number of replacements needed is the window's length minus how many of its most common letter it already has, not its least common one.",
                "Decreasing maxCount when the window shrinks.\nmaxCount only needs to track the highest count ever seen for any letter within some window of the current size or larger - it never needs to decrease, since doing so unnecessarily can cause the window to shrink more than it needs to.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what's the longest contiguous run that could be made uniform with a limited budget of changes?",
                "A sliding window's 'validity' can be checked using just a running count of the window's most frequent element, rather than fully rescanning it.",
                "The condition for a valid window depends only on aggregate information, like counts, that can be updated incrementally as the window grows or shrinks.",
            ),
            takeaway = "When a sliding window's validity depends on 'how many characters would need to change', track the count of the window's most frequent element and compare window length minus that count against your budget - shrink only when the budget is exceeded.",
        ),
        nextSlug = "permutation-in-string",
    )

    private val permutationInString = RoadmapLesson(
        slug = "permutation-in-string",
        title = "Permutation in String",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given two strings, decide whether the second string contains a permutation of the first as a substring - meaning some contiguous stretch of the second string uses exactly the same letters, the same number of times, as the first string, in any order.",
        constraints = listOf(
            "Both strings contain only lowercase English letters.",
            "The first string is generally much shorter than the second.",
            "A permutation means the same letters with the same frequency, not necessarily the same order.",
            "Aim for a fixed-size sliding window rather than checking every possible substring and sorting it.",
        ),
        examples = listOf(
            LessonExample("s1 = \"ab\", s2 = \"eidbaooo\"", "true", "\"ba\", a substring of s2, is a permutation of \"ab\"."),
            LessonExample("s1 = \"ab\", s2 = \"eidboaoo\"", "false", "No contiguous substring of s2 uses exactly one a and one b together."),
            LessonExample("s1 = \"a\", s2 = \"ab\"", "true", "The single character \"a\" appears as a substring, trivially a permutation of itself."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "permutation-string-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach checks for a permutation without sorting every possible substring of s2?",
                choices = listOf(
                    LessonChoice(
                        "For every substring of s2 with the same length as s1, sort both it and s1, then compare the sorted results.",
                        false,
                        "This works, but there are many such substrings, and sorting each one costs extra work per substring - much more than a fixed-size window with letter counts needs.",
                    ),
                    LessonChoice(
                        "Slide a fixed-size window (the length of s1) across s2, tracking letter counts inside the window, and check whether those counts exactly match s1's letter counts at each position.",
                        true,
                        "Because the window size never changes, sliding it one step at a time only requires removing the count of the character that just left and adding the count of the character that just entered - no resorting needed.",
                    ),
                    LessonChoice(
                        "Search s2 for the exact substring s1, character for character, ignoring that a permutation can reorder the letters.",
                        false,
                        "Searching for an exact match would miss valid permutations where the letters appear in a different order, like finding 'ba' when searching literally for 'ab'.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "permutation-string-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three slide a fixed-size window comparing letter counts. Which one correctly maintains the window's size?",
                choices = listOf(
                    LessonChoice(
                        text = "Grows the window one character at a time without ever removing characters that fall outside the window's fixed length.",
                        correct = false,
                        feedback = "Without removing characters that have aged out of the window, the window keeps growing indefinitely instead of staying at a fixed size equal to s1's length.",
                        code = "fun checkInclusion(s1: String, s2: String): Boolean {\n    val need = IntArray(26)\n    for (c in s1) need[c - 'a']++\n    val window = IntArray(26)\n    for (i in s2.indices) {\n        window[s2[i] - 'a']++\n        if (window.contentEquals(need)) return true\n    }\n    return false\n}",
                    ),
                    LessonChoice(
                        text = "Uses a window size equal to s2's length instead of s1's length.",
                        correct = false,
                        feedback = "The window needs to match the length of s1, the pattern being searched for - using s2's own length would make the window cover the entire string on every check, never isolating a candidate substring of the right size.",
                        code = "fun checkInclusion(s1: String, s2: String): Boolean {\n    val need = IntArray(26)\n    for (c in s1) need[c - 'a']++\n    val window = IntArray(26)\n    val windowSize = s2.length\n    for (i in s2.indices) {\n        window[s2[i] - 'a']++\n        if (i >= windowSize) window[s2[i - windowSize] - 'a']--\n        if (window.contentEquals(need)) return true\n    }\n    return false\n}",
                    ),
                    LessonChoice(
                        text = "Adds the new character entering the window and, once the window has grown past s1's length, removes the character that just fell out of it.",
                        correct = true,
                        feedback = "Keeping the window at exactly s1's length by adding one character and removing the one that's now too far behind is what lets it 'slide' - checking the counts at each valid position for a match.",
                        code = "fun checkInclusion(s1: String, s2: String): Boolean {\n    val need = IntArray(26)\n    for (c in s1) need[c - 'a']++\n    val window = IntArray(26)\n    for (i in s2.indices) {\n        window[s2[i] - 'a']++\n        if (i >= s1.length) window[s2[i - s1.length] - 'a']--\n        if (i >= s1.length - 1 && window.contentEquals(need)) return true\n    }\n    return false\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "permutation-string-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version reports a match too early, before the window has actually reached s1's full length. What's the bug?",
                code = "val window = IntArray(26)\nfor (i in s2.indices) {\n    window[s2[i] - 'a']++\n    if (i >= s1.length) window[s2[i - s1.length] - 'a']--\n    if (window.contentEquals(need)) return true\n}",
                choices = listOf(
                    LessonChoice(
                        "Add a check that i >= s1.length - 1 before comparing window to need.",
                        true,
                        "Without that check, the comparison could trigger while the window is still smaller than s1's length, early in the scan, before it has had a chance to actually represent a same-length substring of s2.",
                    ),
                    LessonChoice(
                        "Change i >= s1.length to i > s1.length.",
                        false,
                        "That shifts exactly which index the window starts removing characters at by one position, but doesn't address why a too-small window is being compared against need in the first place.",
                    ),
                    LessonChoice(
                        "Change window[s2[i] - 'a']++ to window[s2[i] - 'a'] += 1.",
                        false,
                        "++ and += 1 do the exact same thing in Kotlin - this change has no effect on the bug.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "permutation-string-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the length of s2 and m as the length of s1, what is the time complexity of the fixed-size sliding window approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n * m), because comparing the window to need at every position takes O(m) time.",
                        false,
                        "Comparing two fixed-size, 26-slot count arrays takes constant time - it doesn't scale with m, since the arrays are always exactly 26 entries long regardless of how long s1 is.",
                    ),
                    LessonChoice(
                        "O(n), because the window slides one position at a time across s2, and each slide does a constant amount of work.",
                        true,
                        "Building s1's initial letter counts takes O(m), and then sliding the window across the rest of s2 takes O(n), with constant work per step - the total is O(n + m), described simply as O(n) when n is the larger of the two.",
                    ),
                    LessonChoice(
                        "O(n log n), because the window's counts must be sorted before comparing them to need.",
                        false,
                        "Comparing two fixed-size count arrays for equality doesn't require sorting either one - it's a direct, position-by-position comparison of 26 fixed slots.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "permutation-string-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space do the need and window count arrays use?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because the window array must track every character of s2 as it slides.",
                        false,
                        "The window array only ever has 26 slots, one per lowercase letter - it doesn't grow as the window slides across more of s2.",
                    ),
                    LessonChoice(
                        "O(m), because the need array is built from the characters of s1.",
                        false,
                        "The need array also only has 26 slots, regardless of how long s1 is - a longer s1 just means larger counts within those same 26 slots, not more slots.",
                    ),
                    LessonChoice(
                        "O(1), because both the need and window arrays always have exactly 26 slots, one per lowercase letter.",
                        true,
                        "26 is a fixed constant that doesn't grow with the length of either string, which is exactly what constant extra space means.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture cutting out a window exactly as wide as s1 and sliding it across s2, one character at a time.\n\nAt every position, ask: does this window contain exactly the same letters, in the same quantities, as s1 - just possibly in a different order?\n\nInstead of re-counting the whole window from scratch each time it slides, just remove the count of the character that fell out the back and add the count of the character that entered the front - the window's letter counts update in constant time at every step.",
            walkthrough = listOf(
                "s1 = \"ab\", s2 = \"eidbaooo\". need = {a: 1, b: 1}. Window size is 2.",
                "Window over \"ei\": counts {e:1, i:1} - doesn't match need.",
                "Slide to \"id\": remove e, add d. counts {i:1, d:1} - doesn't match.",
                "Slide to \"db\": remove i, add b. counts {d:1, b:1} - doesn't match.",
                "Slide to \"ba\": remove d, add a. counts {b:1, a:1} - matches need exactly!",
                "Return true - \"ba\" is a permutation of \"ab\".",
            ),
            pseudocode = "need = 26 counts from s1\nwindow = 26 zeros\nfor i from 0 to length(s2) - 1:\n    window[s2[i]] += 1\n    if i >= length(s1): window[s2[i - length(s1)]] -= 1\n    if i >= length(s1) - 1 and window == need: return true\nreturn false",
            referenceCode = "fun checkInclusion(s1: String, s2: String): Boolean {\n    val need = IntArray(26)\n    for (c in s1) need[c - 'a']++\n    val window = IntArray(26)\n    for (i in s2.indices) {\n        window[s2[i] - 'a']++\n        if (i >= s1.length) window[s2[i - s1.length] - 'a']--\n        if (i >= s1.length - 1 && window.contentEquals(need)) return true\n    }\n    return false\n}",
            timeComplexity = "O(n + m), usually written simply as O(n), where n is the length of s2 and m is the length of s1.\n\nBuilding s1's initial letter counts takes O(m). Sliding the window across s2 takes O(n), with each slide doing a constant amount of work: one addition, one removal, and one fixed-size, 26-slot comparison.\n\nNeither step rescans or resorts anything as the window moves.",
            spaceComplexity = "O(1), because both the need and window arrays always have exactly 26 slots - one per lowercase letter - no matter how long s1 or s2 are.",
            alternatives = listOf(
                "For every substring of s2 with the same length as s1, sort both it and s1 and compare the sorted results.\nThis is easy to reason about directly from the problem statement.\nBut sorting each candidate substring costs more work than just comparing fixed-size letter counts.",
                "Use a single running count of 'how many letters currently differ between the window and need' instead of comparing all 26 slots at every step.\nThis can make each slide step even cheaper in practice.\nBut it adds bookkeeping complexity that isn't necessary once the count arrays are already small and fixed in size.",
            ),
            commonMistakes = listOf(
                "Comparing the window to need before the window has actually grown to s1's full length.\nEarly in the scan, the window only contains a few characters, and comparing it to need at that point can produce a false positive or is simply meaningless.\nThe comparison should only happen once the window has reached the correct size.",
                "Letting the window grow indefinitely instead of removing the character that falls out the back once it exceeds s1's length.\nWithout that removal, the window no longer represents a fixed-length substring of s2, and the counts stop being comparable to need.",
                "Using a window size based on s2's length instead of s1's length.\nThe window needs to match the length of the pattern being searched for, not the length of the string being searched within.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: does some fixed-length window of one sequence match a target composition, regardless of order?",
                "The window size is fixed and known in advance, here the length of s1, which is a strong hint for a fixed-size sliding window rather than one that grows and shrinks.",
                "Order within the window doesn't matter, only the count of each kind of item - a strong hint that counts, not direct comparison, are the right tool.",
            ),
            takeaway = "When checking whether a fixed-length window anywhere in a sequence matches a target composition, slide a same-sized window and update its counts incrementally - add what enters, remove what exits - rather than recomputing or resorting the window from scratch at every position.",
        ),
        nextSlug = "minimum-window-substring",
    )

    private val minimumWindowSubstring = RoadmapLesson(
        slug = "minimum-window-substring",
        title = "Minimum Window Substring",
        difficulty = CurriculumDifficulty.HARD,
        description = "Given two strings, s and t, find the shortest contiguous substring of s that contains every character of t, including matching how many times each character appears in t. Return an empty string if no such substring exists.",
        constraints = listOf(
            "Both strings may contain any characters, and case matters.",
            "The answer must contain every character of t with at least its required frequency.",
            "If multiple substrings of the same minimum length qualify, any one of them may be returned.",
            "Aim for a linear-time sliding window rather than checking every possible substring of s.",
        ),
        examples = listOf(
            LessonExample("s = \"ADOBECODEBANC\", t = \"ABC\"", "\"BANC\"", "\"BANC\" is the shortest substring of s containing at least one A, one B, and one C."),
            LessonExample("s = \"a\", t = \"a\"", "\"a\"", "The whole string already satisfies the requirement with no shorter option available."),
            LessonExample("s = \"a\", t = \"aa\"", "\"\"", "s has only one 'a', but t requires two - no valid substring exists."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "min-window-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the minimum window without checking every possible substring of s directly?",
                choices = listOf(
                    LessonChoice(
                        "Check every possible substring of s, and for each one verify whether it contains all the required characters of t.",
                        false,
                        "This finds the correct answer, but there are O(n squared) substrings to check, and verifying each one can add even more work - far slower than a sliding window needs.",
                    ),
                    LessonChoice(
                        "Slide a window across s, expanding the right edge until it contains everything t needs, then shrink the left edge as much as possible while it still satisfies that requirement, recording the shortest valid window found.",
                        true,
                        "Growing until the window is valid, then shrinking until it's just barely still valid, finds every 'locally shortest' window without ever re-scanning characters that have already been accounted for.",
                    ),
                    LessonChoice(
                        "Sort both s and t, then look for the shortest matching run in the sorted version of s.",
                        false,
                        "Sorting destroys the original positions in s, but the answer must be an actual contiguous substring of s in its original order - a sorted rearrangement isn't a valid substring.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "min-window-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three grow and shrink a window while tracking how many required characters are currently satisfied. Which one shrinks at the right time?",
                choices = listOf(
                    LessonChoice(
                        text = "Shrinks the window from the left as long as it remains valid, updating the best answer each time, and stops shrinking exactly when removing the next character would make it invalid.",
                        correct = true,
                        feedback = "Continuing to shrink for as long as the window stays valid, checking the best length at every step along the way, finds the truly shortest valid window starting near each right-edge position, not just the first valid one found.",
                        code = "fun minWindow(s: String, t: String): String {\n    if (t.isEmpty()) return \"\"\n    val need = HashMap<Char, Int>()\n    for (c in t) need[c] = (need[c] ?: 0) + 1\n    val required = need.size\n    var formed = 0\n    val windowCounts = HashMap<Char, Int>()\n    var left = 0\n    var bestLen = Int.MAX_VALUE\n    var bestLeft = 0\n    for (right in s.indices) {\n        val c = s[right]\n        windowCounts[c] = (windowCounts[c] ?: 0) + 1\n        if (need.containsKey(c) && windowCounts[c] == need[c]) formed++\n        while (formed == required) {\n            if (right - left + 1 < bestLen) {\n                bestLen = right - left + 1\n                bestLeft = left\n            }\n            val leftChar = s[left]\n            windowCounts[leftChar] = windowCounts[leftChar]!! - 1\n            if (need.containsKey(leftChar) && windowCounts[leftChar]!! < need[leftChar]!!) formed--\n            left++\n        }\n    }\n    return if (bestLen == Int.MAX_VALUE) \"\" else s.substring(bestLeft, bestLeft + bestLen)\n}",
                    ),
                    LessonChoice(
                        text = "Shrinks the window from the left exactly once whenever it becomes valid, then immediately resumes growing without checking if it can shrink further.",
                        correct = false,
                        feedback = "Shrinking only once can leave the window larger than necessary - after removing one character it might still be valid and could shrink further, but this stops checking after just a single removal.",
                        code = "fun minWindow(s: String, t: String): String {\n    if (t.isEmpty()) return \"\"\n    val need = HashMap<Char, Int>()\n    for (c in t) need[c] = (need[c] ?: 0) + 1\n    val required = need.size\n    var formed = 0\n    val windowCounts = HashMap<Char, Int>()\n    var left = 0\n    var bestLen = Int.MAX_VALUE\n    var bestLeft = 0\n    for (right in s.indices) {\n        val c = s[right]\n        windowCounts[c] = (windowCounts[c] ?: 0) + 1\n        if (need.containsKey(c) && windowCounts[c] == need[c]) formed++\n        if (formed == required) {\n            if (right - left + 1 < bestLen) {\n                bestLen = right - left + 1\n                bestLeft = left\n            }\n            val leftChar = s[left]\n            windowCounts[leftChar] = windowCounts[leftChar]!! - 1\n            if (need.containsKey(leftChar) && windowCounts[leftChar]!! < need[leftChar]!!) formed--\n            left++\n        }\n    }\n    return if (bestLen == Int.MAX_VALUE) \"\" else s.substring(bestLeft, bestLeft + bestLen)\n}",
                    ),
                    LessonChoice(
                        text = "Records the best answer only right after growing the window on the right, never again during the shrink phase on the left.",
                        correct = false,
                        feedback = "The window is actually smallest right before it becomes invalid again - checking the best length only once, right when the window first becomes valid, misses every smaller valid window found while continuing to shrink from there.",
                        code = "fun minWindow(s: String, t: String): String {\n    if (t.isEmpty()) return \"\"\n    val need = HashMap<Char, Int>()\n    for (c in t) need[c] = (need[c] ?: 0) + 1\n    val required = need.size\n    var formed = 0\n    val windowCounts = HashMap<Char, Int>()\n    var left = 0\n    var bestLen = Int.MAX_VALUE\n    var bestLeft = 0\n    for (right in s.indices) {\n        val c = s[right]\n        windowCounts[c] = (windowCounts[c] ?: 0) + 1\n        if (need.containsKey(c) && windowCounts[c] == need[c]) formed++\n        if (formed == required && right - left + 1 < bestLen) {\n            bestLen = right - left + 1\n            bestLeft = left\n        }\n        while (formed == required) {\n            val leftChar = s[left]\n            windowCounts[leftChar] = windowCounts[leftChar]!! - 1\n            if (need.containsKey(leftChar) && windowCounts[leftChar]!! < need[leftChar]!!) formed--\n            left++\n        }\n    }\n    return if (bestLen == Int.MAX_VALUE) \"\" else s.substring(bestLeft, bestLeft + bestLen)\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "min-window-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version never finds a valid window, always returning an empty string, even when a valid one clearly exists. What's the bug?",
                code = "val need = HashMap<Char, Int>()\nfor (c in t) need[c] = (need[c] ?: 0) + 1\nvar required = need.size + 1\nvar formed = 0",
                choices = listOf(
                    LessonChoice(
                        "Change windowCounts[c] = (windowCounts[c] ?: 0) + 1 to windowCounts[c] = 1.",
                        false,
                        "That would forget any earlier occurrences of the same character within the window, breaking the count tracking - it's not related to why the window is never considered valid.",
                    ),
                    LessonChoice(
                        "Change need.containsKey(c) && windowCounts[c] == need[c] to just need.containsKey(c).",
                        false,
                        "Removing the count comparison would mark a requirement as 'formed' the moment a character appears even once, regardless of how many times t actually needs it - that's a different, looser bug than the one described here.",
                    ),
                    LessonChoice(
                        "Change required = need.size + 1 to required = need.size.",
                        true,
                        "required represents how many distinct characters from t need their full count satisfied - formed can only ever reach need.size, so setting the target one higher than that makes formed == required impossible to ever satisfy.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "min-window-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the length of s and m as the length of t, what is the time complexity of the sliding window approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because for every right position, the left pointer might shrink all the way back to the start.",
                        false,
                        "The left pointer only ever moves forward across the entire run of the algorithm - its total movement over the whole scan is bounded by n, not by n for every right position.",
                    ),
                    LessonChoice(
                        "O(n + m), usually described simply as O(n), because building the need map from t is O(m), and both pointers each move forward at most n times total while scanning s.",
                        true,
                        "One pass to build need, then a single combined pass where left and right pointers each move forward a bounded number of times, together give linear time relative to the lengths involved.",
                    ),
                    LessonChoice(
                        "O(n * m), because every character of s must be checked against every character of t.",
                        false,
                        "Characters of s are checked against the need map using fast, average-constant-time lookups, not by comparing directly against every character of t one by one.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "min-window-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space do the need and windowCounts maps use in the worst case?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because windowCounts can grow to hold every character of s.",
                        false,
                        "windowCounts can only ever hold as many distinct characters as actually appear, which is bounded by the size of the character set being used, not by how long s is.",
                    ),
                    LessonChoice(
                        "O(distinct characters in s and t combined), bounded by the alphabet size rather than by n or m directly.",
                        true,
                        "Both maps only ever store one entry per distinct character encountered, so their size is capped by how many different characters could possibly appear, not by the length of either string.",
                    ),
                    LessonChoice(
                        "O(1), because there are always exactly two maps used.",
                        false,
                        "Having a fixed number of maps doesn't limit how many entries each one can hold - both maps can grow to hold one entry per distinct character seen.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture growing a window across s until it happens to contain everything t asks for - every required character, with at least the right count of each.\n\nOnce that's true, try shrinking the window from the left as much as possible while it still satisfies every requirement, checking its length at each step.\n\nThe moment shrinking further would break one of the requirements, stop, remember the shortest valid length found along the way, and go back to growing the window further to the right.",
            walkthrough = listOf(
                "s = \"ADOBECODEBANC\", t = \"ABC\". need = {A:1, B:1, C:1}, required = 3.",
                "Grow the window right until it contains at least one A, one B, and one C: this first happens at \"ADOBEC\".",
                "Shrink from the left: removing 'A' would break the requirement (no more A left), so the window can't shrink past keeping that A - record length 6 as a candidate.",
                "Continue growing right, then shrinking again from the left each time a new right character keeps the window valid, recording lengths along the way.",
                "Eventually the window \"BANC\" is found, with length 4 - shorter than any window found so far.",
                "No shorter valid window exists elsewhere in the string. Return \"BANC\".",
            ),
            pseudocode = "need = counts of each character in t\nrequired = number of distinct characters in need\nformed = 0\nwindowCounts = empty map\nleft = 0, bestLen = infinity, bestLeft = 0\nfor right from 0 to length(s) - 1:\n    add s[right] to windowCounts\n    if s[right] is in need and windowCounts[s[right]] == need[s[right]]: formed += 1\n    while formed == required:\n        if (right - left + 1) < bestLen: record bestLen and bestLeft\n        remove s[left] from windowCounts\n        if s[left] is in need and windowCounts[s[left]] < need[s[left]]: formed -= 1\n        left += 1\nreturn substring at bestLeft with length bestLen, or empty string if none found",
            referenceCode = "fun minWindow(s: String, t: String): String {\n    if (t.isEmpty()) return \"\"\n    val need = HashMap<Char, Int>()\n    for (c in t) need[c] = (need[c] ?: 0) + 1\n    val required = need.size\n    var formed = 0\n    val windowCounts = HashMap<Char, Int>()\n    var left = 0\n    var bestLen = Int.MAX_VALUE\n    var bestLeft = 0\n    for (right in s.indices) {\n        val c = s[right]\n        windowCounts[c] = (windowCounts[c] ?: 0) + 1\n        if (need.containsKey(c) && windowCounts[c] == need[c]) formed++\n        while (formed == required) {\n            if (right - left + 1 < bestLen) {\n                bestLen = right - left + 1\n                bestLeft = left\n            }\n            val leftChar = s[left]\n            windowCounts[leftChar] = windowCounts[leftChar]!! - 1\n            if (need.containsKey(leftChar) && windowCounts[leftChar]!! < need[leftChar]!!) formed--\n            left++\n        }\n    }\n    return if (bestLen == Int.MAX_VALUE) \"\" else s.substring(bestLeft, bestLeft + bestLen)\n}",
            timeComplexity = "O(n + m), usually written as O(n), where n is the length of s and m is the length of t.\n\nBuilding the need map from t takes O(m).\n\nThen, across the whole scan of s, the right pointer moves forward n times and the left pointer also only ever moves forward, for a combined total proportional to n.",
            spaceComplexity = "O(distinct characters in s and t combined), bounded by the size of the character set being used rather than by the length of either string.\n\nBoth the need map and the windowCounts map only ever store one entry per distinct character actually encountered.",
            alternatives = listOf(
                "Check every possible substring of s directly, verifying for each one whether it contains everything t requires.\nThis follows the problem statement very literally.\nBut with O(n squared) substrings, each needing its own check, this is far slower than a single sliding-window pass.",
                "Restrict the search to only the positions in s where a character from t actually appears, skipping over stretches of s that contain none of t's characters at all.\nThis can meaningfully speed things up in practice when t's characters are sparse within s.\nBut it adds extra bookkeeping and doesn't change the algorithm's worst-case time complexity.",
            ),
            commonMistakes = listOf(
                "Only checking the best-length answer once, right when the window first becomes valid, instead of continuing to check while shrinking.\nThe window keeps getting smaller as it shrinks while staying valid, so the shortest valid window at a given right-edge position is only found by checking at every step of the shrink, not just the first one.",
                "Comparing formed against the wrong target value, such as need.size plus or minus one.\nformed should track how many distinct required characters currently have their full needed count satisfied, and required should be exactly how many distinct characters need must contain.",
                "Forgetting to decrement formed when a required character's count drops below what's needed while shrinking the window.\nWithout that, the algorithm can keep believing the window is still valid even after removing a character it actually needed, leading to a window being reported as shorter than it validly is.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what's the shortest contiguous stretch that satisfies a set of requirements all at once?",
                "A window's validity depends on aggregate counts, whether it has enough of each needed thing, rather than a simple yes/no per character.",
                "Growing until valid, then shrinking until just barely still valid, is a pattern for finding the tightest window that satisfies a condition.",
            ),
            takeaway = "When searching for the shortest window that satisfies a multi-part requirement, grow the window until every requirement is met, then shrink it from the left for as long as it stays valid, recording the length at every step along the way - that finds the true minimum, not just the first valid window found.",
        ),
        nextSlug = "sliding-window-maximum",
    )

    private val slidingWindowMaximum = RoadmapLesson(
        slug = "sliding-window-maximum",
        title = "Sliding Window Maximum",
        difficulty = CurriculumDifficulty.HARD,
        description = "Given an array of integers and a window size k, return the maximum value within each window of k consecutive elements as the window slides from the start of the array to the end.",
        constraints = listOf(
            "1 <= k <= length of the array.",
            "The array may contain positive, negative, or repeated values.",
            "The result has exactly (length of array - k + 1) values, one maximum per window position.",
            "Aim for linear time rather than scanning all k elements fresh for every window position.",
        ),
        examples = listOf(
            LessonExample("nums = [1,3,-1,-3,5,3,6,7], k = 3", "[3,3,5,5,6,7]", "The window [1,3,-1] has max 3, [3,-1,-3] has max 3, [-1,-3,5] has max 5, and so on as the window slides right."),
            LessonExample("nums = [1], k = 1", "[1]", "With a window the same size as the array, there's only one window and its maximum is the single value."),
            LessonExample("nums = [9,11], k = 2", "[11]", "The only window covers both values, and 11 is the larger of the two."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "sliding-max-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds every window's maximum without rescanning all k elements for every window position?",
                choices = listOf(
                    LessonChoice(
                        "For each window position, scan all k elements inside it directly to find the maximum.",
                        false,
                        "This finds the correct answer, but scanning k elements for each of roughly n window positions costs O(n * k) - slow when k is large.",
                    ),
                    LessonChoice(
                        "Maintain a deque of indices, keeping it in decreasing order of value, removing indices that fall outside the current window and removing smaller values from the back before adding a new one.",
                        true,
                        "The deque always keeps its front index pointing at the current window's maximum, and because each index is added and removed from the deque at most once overall, the total work stays linear.",
                    ),
                    LessonChoice(
                        "Sort each window's elements before reading off the maximum as the last one.",
                        false,
                        "Sorting a window of k elements costs extra work for every window position, adding unnecessary overhead when a well-maintained deque can track the maximum directly.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "sliding-max-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three maintain a deque of indices. Which one keeps it correctly ordered so the front is always the window's maximum?",
                choices = listOf(
                    LessonChoice(
                        text = "Before adding a new index, removes indices from the back of the deque whose values are less than the new value; also removes the front index if it has fallen outside the window.",
                        correct = true,
                        feedback = "Removing smaller values from the back before adding a new one keeps the deque in decreasing order of value, and removing an outdated front index keeps the maximum candidate relevant to the current window.",
                        code = "fun maxSlidingWindow(nums: IntArray, k: Int): IntArray {\n    val deque = ArrayDeque<Int>()\n    val result = mutableListOf<Int>()\n    for (i in nums.indices) {\n        while (deque.isNotEmpty() && nums[deque.last()] < nums[i]) deque.removeLast()\n        deque.addLast(i)\n        if (deque.first() <= i - k) deque.removeFirst()\n        if (i >= k - 1) result.add(nums[deque.first()])\n    }\n    return result.toIntArray()\n}",
                    ),
                    LessonChoice(
                        text = "Adds every new index to the deque without ever removing smaller values from the back first.",
                        correct = false,
                        feedback = "Without removing smaller values from the back, the deque can accumulate values in no particular order, so the front index no longer reliably points to the current window's maximum.",
                        code = "fun maxSlidingWindow(nums: IntArray, k: Int): IntArray {\n    val deque = ArrayDeque<Int>()\n    val result = mutableListOf<Int>()\n    for (i in nums.indices) {\n        deque.addLast(i)\n        if (deque.first() <= i - k) deque.removeFirst()\n        if (i >= k - 1) result.add(nums[deque.first()])\n    }\n    return result.toIntArray()\n}",
                    ),
                    LessonChoice(
                        text = "Removes smaller values from the back before adding a new index, but never removes the front index even after it has fallen outside the current window.",
                        correct = false,
                        feedback = "Keeping the deque in decreasing order isn't enough on its own - without removing a front index that has aged out of the window, the reported maximum can come from a value that's no longer actually inside the current window at all.",
                        code = "fun maxSlidingWindow(nums: IntArray, k: Int): IntArray {\n    val deque = ArrayDeque<Int>()\n    val result = mutableListOf<Int>()\n    for (i in nums.indices) {\n        while (deque.isNotEmpty() && nums[deque.last()] < nums[i]) deque.removeLast()\n        deque.addLast(i)\n        if (i >= k - 1) result.add(nums[deque.first()])\n    }\n    return result.toIntArray()\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "sliding-max-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version's results are shifted, sometimes including the maximum from a value that's already outside the window. What's the bug?",
                code = "val deque = ArrayDeque<Int>()\nval result = mutableListOf<Int>()\nfor (i in nums.indices) {\n    while (deque.isNotEmpty() && nums[deque.last()] < nums[i]) deque.removeLast()\n    deque.addLast(i)\n    if (i >= k - 1) result.add(nums[deque.first()])\n}",
                choices = listOf(
                    LessonChoice(
                        "Change nums[deque.last()] < nums[i] to nums[deque.last()] <= nums[i].",
                        false,
                        "Using <= instead of < only changes how equal values are handled at the back of the deque - it doesn't address indices at the front falling outside the window, which is the actual bug here.",
                    ),
                    LessonChoice(
                        "Add if (deque.first() <= i - k) deque.removeFirst() before checking whether to record the window's maximum.",
                        true,
                        "Without removing a front index once it's fallen outside the current window, meaning its position is k or more behind i, the deque can keep reporting a maximum from a value that isn't actually part of the current window anymore.",
                    ),
                    LessonChoice(
                        "Change deque.addLast(i) to deque.addFirst(i).",
                        false,
                        "Adding new indices to the front instead of the back would break the deque's decreasing order entirely, since indices need to be added at the back after larger values ahead of them have already been trimmed.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "sliding-max-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of values in the array, what is the time complexity of the deque-based approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n * k), because each new value might need to remove up to k values from the back of the deque.",
                        false,
                        "Even though a single step could remove several values from the back, each index is only ever added to the deque once and removed from it once across the entire run - the total number of additions and removals combined is bounded by n, not by n times k.",
                    ),
                    LessonChoice(
                        "O(n), because each index is added to the deque exactly once and removed from the deque at most once, across the whole run of the algorithm.",
                        true,
                        "Even though some steps look like they do more work, removing several back values at once, the total number of deque operations across the entire algorithm can never exceed roughly 2n, since every index enters and leaves the deque only once each.",
                    ),
                    LessonChoice(
                        "O(n log n), because the deque must stay sorted, which normally requires log-time insertion.",
                        false,
                        "The deque is kept in decreasing order using simple removals from the back, not a sorted-insertion structure - each operation is a plain constant-time deque operation, not a log-time one.",
                    ),
                ),
                glossary = listOf(
                    GlossaryTerm("O(n)", "Linear time, even though it might look like nested work is happening - each index is only ever added to and removed from the deque once."),
                ),
            ),
            LessonQuestion(
                id = "sliding-max-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the deque use in the worst case?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because the deque could theoretically hold every index in the array at once.",
                        false,
                        "The deque is actively trimmed both at the front, for indices outside the window, and at the back, for smaller values that can never become the maximum again, so it never grows past the current window's size, k.",
                    ),
                    LessonChoice(
                        "O(1), because a deque is a single data structure regardless of how many elements it holds.",
                        false,
                        "Being a single data structure doesn't cap how many elements it can hold - this deque's size is bounded by k, not by a fixed constant like 1.",
                    ),
                    LessonChoice(
                        "O(k), because the deque never holds more indices than fit within the current window's size.",
                        true,
                        "Since every index in the deque must be within the current window (any index that falls outside gets removed from the front), and a window only ever spans k positions, the deque can never hold more than k indices at once.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture a line of people waiting to be considered for 'tallest person currently in view,' where people leave the line once they're out of sight or once someone taller cuts in front of them.\n\nKeep a special line, the deque, of candidate indices, always in decreasing order of value, from front to back.\n\nWhenever a new value arrives, kick out anyone shorter standing at the back of the line first - they'll never be the tallest again while this new, taller person is around. Then check whether the person at the very front of the line has wandered out of the current window; if so, remove them too. Whoever's left at the front is always the current window's maximum.",
            walkthrough = listOf(
                "nums = [1,3,-1,-3,5,3,6,7], k=3. i=0 (value 1): deque = [0].",
                "i=1 (value 3): 3 > nums[0]=1, so remove index 0 from the back. deque = [1].",
                "i=2 (value -1): -1 is not greater than nums[1]=3, so just add. deque = [1, 2]. Window [0..2] is complete: max is nums[1]=3.",
                "i=3 (value -3): add without removing. deque = [1, 2, 3]. Front index 1 is still within window [1..3]. Max is nums[1]=3.",
                "i=4 (value 5): 5 is greater than nums[3]=-3, nums[2]=-1, and nums[1]=3, so remove all three from the back. deque = [4]. Max for window [2..4] is nums[4]=5.",
                "Continue similarly for the rest of the array, giving the full result [3,3,5,5,6,7].",
            ),
            pseudocode = "deque = empty (holds indices)\nresult = empty list\nfor i from 0 to length - 1:\n    while deque is not empty and nums[deque's last index] < nums[i]: remove from back\n    add i to the back of deque\n    if deque's front index <= i - k: remove from front\n    if i >= k - 1: add nums[deque's front index] to result\nreturn result",
            referenceCode = "fun maxSlidingWindow(nums: IntArray, k: Int): IntArray {\n    val deque = ArrayDeque<Int>()\n    val result = mutableListOf<Int>()\n    for (i in nums.indices) {\n        while (deque.isNotEmpty() && nums[deque.last()] < nums[i]) deque.removeLast()\n        deque.addLast(i)\n        if (deque.first() <= i - k) deque.removeFirst()\n        if (i >= k - 1) result.add(nums[deque.first()])\n    }\n    return result.toIntArray()\n}",
            timeComplexity = "O(n), where n is the number of values in the array.\n\nEven though a single step can remove several values from the back of the deque at once, every index is only ever added to the deque one time and removed from it at most one time across the entire run.\n\nAdd up every addition and removal over the whole algorithm, and the total is bounded by a small constant multiple of n.",
            spaceComplexity = "O(k), because the deque is actively trimmed at both ends - indices outside the current window are removed from the front, and values that can never become a future maximum are removed from the back.\n\nSince the deque only ever holds indices that are both inside the current window and still 'in contention' to be a maximum, its size never exceeds the window size, k.",
            alternatives = listOf(
                "For each window position, scan all k elements inside it directly to find the maximum.\nThis is the most direct translation of the problem statement.\nBut scanning k elements for each of roughly n window positions costs O(n * k), which is slow when k is large.",
                "Use a max-heap (priority queue) of values paired with their indices, lazily removing entries once they've aged out of the window when they reach the top.\nThis also finds every window's maximum correctly.\nBut heap operations cost more per step than deque operations, making the total slower than the deque approach.",
            ),
            commonMistakes = listOf(
                "Forgetting to remove smaller values from the back of the deque before adding a new, larger value.\nWithout that step, the deque doesn't stay in decreasing order, so its front index no longer reliably points to the maximum of the current window.",
                "Forgetting to remove the front index once it has fallen outside the current window.\nWithout that check, the reported maximum can come from a value that isn't actually part of the current window anymore, shifting every answer.",
                "Storing values in the deque instead of indices.\nStoring only values makes it impossible to tell whether a candidate has fallen outside the current window, since that check depends on comparing a stored position to the window's current boundaries, not the value itself.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what's the best, maximum or minimum, value within every window as it slides across a sequence?",
                "A brute-force answer would rescan the whole window at every position, but many of those elements are shared between consecutive windows.",
                "Some candidates can be permanently ruled out the moment something better appears alongside them, which hints at a monotonic deque.",
            ),
            takeaway = "When you need the maximum (or minimum) of every sliding window efficiently, maintain a deque of indices in decreasing order of value, trimming smaller values from the back and outdated indices from the front - each index is added and removed at most once, keeping the whole scan linear.",
        ),
        nextSlug = "valid-parentheses",
    )

    private val validParentheses = RoadmapLesson(
        slug = "valid-parentheses",
        title = "Valid Parentheses",
        difficulty = CurriculumDifficulty.EASY,
        description = "Given a string containing just the characters '(', ')', '{', '}', '[' and ']', determine if the brackets are balanced and correctly nested - every opening bracket is closed by the same type of bracket, in the correct order.",
        constraints = listOf(
            "The string contains only bracket characters, no other symbols.",
            "The string may be empty, which counts as balanced.",
            "Brackets must close in the reverse order they were opened.",
            "Aim for a single pass through the string rather than repeatedly scanning for matching pairs.",
        ),
        examples = listOf(
            LessonExample("\"()\"", "true", "A single pair of matching, correctly nested parentheses."),
            LessonExample("\"()[]{}\"", "true", "Three separate, non-overlapping pairs, each properly closed."),
            LessonExample("\"(]\"", "false", "An opening parenthesis is closed by a closing square bracket instead of a closing parenthesis."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "valid-parens-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach checks bracket validity in a single pass through the string?",
                choices = listOf(
                    LessonChoice(
                        "Repeatedly search the string for any adjacent matching pair like '()' and remove it, checking if the string becomes empty.",
                        false,
                        "This can work, but repeatedly searching and removing pairs can cost O(n squared) in the worst case, since each removal may require rescanning the string.",
                    ),
                    LessonChoice(
                        "Push every opening bracket onto a stack; when a closing bracket appears, check whether it matches the bracket on top of the stack, popping it if so.",
                        true,
                        "A stack naturally remembers brackets in the order they need to be closed - the most recently opened bracket must be the next one closed, which is exactly what a stack's last-in-first-out order provides.",
                    ),
                    LessonChoice(
                        "Count the number of opening and closing brackets of each type, and check whether the counts match.",
                        false,
                        "Matching counts alone doesn't guarantee correct nesting - '([)]' has equal counts of each bracket type but is not validly nested.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-parens-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use a stack for opening brackets. Which one correctly validates a closing bracket?",
                choices = listOf(
                    LessonChoice(
                        text = "Pushes closing brackets onto the stack and pops when an opening bracket is seen.",
                        correct = false,
                        feedback = "This has the roles reversed - opening brackets are what need to be remembered for later matching, not closing brackets.",
                        code = "fun isValid(s: String): Boolean {\n    val stack = ArrayDeque<Char>()\n    val pairs = mapOf(')' to '(', ']' to '[', '}' to '{')\n    for (c in s) {\n        if (c in pairs) {\n            stack.addLast(c)\n        } else {\n            if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false\n        }\n    }\n    return stack.isEmpty()\n}",
                    ),
                    LessonChoice(
                        text = "Pops from the stack for a closing bracket without checking whether the popped bracket actually matches, or whether the stack was empty first.",
                        correct = false,
                        feedback = "Popping unconditionally can crash on an empty stack, and even if it doesn't, popping without checking the bracket type lets mismatched pairs like '(]' slip through as valid.",
                        code = "fun isValid(s: String): Boolean {\n    val stack = ArrayDeque<Char>()\n    val opens = setOf('(', '[', '{')\n    for (c in s) {\n        if (c in opens) {\n            stack.addLast(c)\n        } else {\n            stack.removeLast()\n        }\n    }\n    return stack.isEmpty()\n}",
                    ),
                    LessonChoice(
                        text = "Pushes opening brackets onto the stack; for a closing bracket, returns false immediately if the stack is empty or its top doesn't match, otherwise pops it.",
                        correct = true,
                        feedback = "Checking both that the stack isn't empty and that the top actually matches the current closing bracket, before popping, correctly rejects both unmatched and mismatched brackets.",
                        code = "fun isValid(s: String): Boolean {\n    val stack = ArrayDeque<Char>()\n    val pairs = mapOf(')' to '(', ']' to '[', '}' to '{')\n    for (c in s) {\n        if (c in pairs) {\n            if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false\n        } else {\n            stack.addLast(c)\n        }\n    }\n    return stack.isEmpty()\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-parens-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version reports \"(\" (a single, unclosed opening bracket) as valid. What's the bug?",
                code = "val stack = ArrayDeque<Char>()\nval pairs = mapOf(')' to '(', ']' to '[', '}' to '{')\nfor (c in s) {\n    if (c in pairs) {\n        if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false\n    } else {\n        stack.addLast(c)\n    }\n}\nreturn true",
                choices = listOf(
                    LessonChoice(
                        "Change return true at the end to return stack.isEmpty().",
                        true,
                        "An opening bracket that's never closed leaves something on the stack when the string ends - checking that the stack is empty at the very end is what catches unclosed brackets.",
                    ),
                    LessonChoice(
                        "Change stack.removeLast() != pairs[c] to stack.removeLast() == pairs[c].",
                        false,
                        "Flipping this comparison would return false for correctly matched brackets and only continue for mismatched ones - the exact opposite of the intended check.",
                    ),
                    LessonChoice(
                        "Change the pairs map to include the reverse mappings as well.",
                        false,
                        "The map only ever needs to look up a closing bracket's matching opening bracket, so adding the reverse direction wouldn't be used anywhere and doesn't address the missing final stack check.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-parens-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the length of the string, what is the time complexity of the stack-based approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because the stack must be searched for a match at every closing bracket.",
                        false,
                        "The stack is only ever checked at its very top, using a constant-time peek or pop - it's never searched through as a whole.",
                    ),
                    LessonChoice(
                        "O(n), because each character causes at most one push or one pop, both of which are constant-time operations.",
                        true,
                        "One pass through the string, with a fixed, small amount of work per character, gives time proportional to n.",
                    ),
                    LessonChoice(
                        "O(n log n), because the brackets must be sorted before matching.",
                        false,
                        "Nothing here gets sorted - the stack simply tracks brackets in the order they were opened, using direct comparisons.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "valid-parens-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space can the stack use in the worst case?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because a string made entirely of opening brackets would push every character onto the stack.",
                        true,
                        "If none of the brackets are ever closed, like in a string of all opening brackets, the stack grows to hold every single character.",
                    ),
                    LessonChoice(
                        "O(1), because the stack only ever needs to remember the single most recent bracket.",
                        false,
                        "The stack can hold many unmatched opening brackets at once, not just the most recent one - all of them are needed since any of them might need to be matched later.",
                    ),
                    LessonChoice(
                        "O(log n), because the stack only grows when brackets are deeply nested, which happens slowly.",
                        false,
                        "How deeply nested the brackets are isn't bounded logarithmically - a string can be nested as deeply as its length allows, letting the stack grow linearly with the input.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture a stack of plates: the only plate you can take off is the one on top, and it must be the same plate you're 'expecting' based on the order they were stacked.\n\nEvery opening bracket is like placing a plate on the stack.\n\nEvery closing bracket asks: does the type of bracket on top of the stack match what I'm supposed to close? If it does, remove that plate. If it doesn't, or there's no plate left to check against, the brackets aren't properly nested.",
            walkthrough = listOf(
                "String \"()[]{}\". Read '(' - push it. Stack: ['('].",
                "Read ')' - top of stack is '(' matching ')'. Pop it. Stack: [].",
                "Read '[' - push it. Stack: ['[']. Read ']' - matches, pop. Stack: [].",
                "Read '{' - push it. Stack: ['{']. Read '}' - matches, pop. Stack: [].",
                "The string ends and the stack is empty - every bracket was matched and properly nested.",
                "Return true.",
            ),
            pseudocode = "stack = empty\npairs = map from closing bracket to its matching opening bracket\nfor each character c in s:\n    if c is a closing bracket:\n        if stack is empty or top of stack != pairs[c]: return false\n        pop the stack\n    else:\n        push c onto the stack\nreturn stack is empty",
            referenceCode = "fun isValid(s: String): Boolean {\n    val stack = ArrayDeque<Char>()\n    val pairs = mapOf(')' to '(', ']' to '[', '}' to '{')\n    for (c in s) {\n        if (c in pairs) {\n            if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false\n        } else {\n            stack.addLast(c)\n        }\n    }\n    return stack.isEmpty()\n}",
            timeComplexity = "O(n), where n is the length of the string.\n\nEach character triggers exactly one push or one pop, both of which take constant time regardless of how many brackets are already on the stack.\n\nOne pass, constant work per character - that scales in a straight line with n.",
            spaceComplexity = "O(n) in the worst case, where n is the length of the string.\n\nIf the string consists entirely of opening brackets that are never closed, every single one ends up pushed onto the stack.\n\nSo the stack's size can grow to match the length of the input.",
            alternatives = listOf(
                "Repeatedly search the string for an adjacent matching pair like '()' and remove it, checking whether the string eventually becomes empty.\nThis needs no separate stack data structure.\nBut repeatedly scanning and removing pairs can cost O(n squared) in the worst case, since each removal may shift the rest of the string and require a fresh scan.",
                "Count the total number of opening and closing brackets of each type, and check that the counts are equal.\nThis is very simple to compute.\nBut matching counts alone doesn't guarantee correct nesting order - a string like '([)]' has equal counts of each bracket type without being validly nested.",
            ),
            commonMistakes = listOf(
                "Forgetting to check that the stack is empty at the very end.\nAn opening bracket that's never closed leaves something behind on the stack when the string finishes, but without a final check, that unclosed bracket goes unnoticed.\nThe function must confirm the stack is completely empty after processing every character.",
                "Popping from the stack without first checking whether it's empty.\nIf a closing bracket appears with nothing left to match it against, trying to pop crashes instead of correctly reporting an invalid string.\nThe emptiness check must happen before every pop.",
                "Mixing up which brackets get pushed and which get compared.\nOpening brackets need to be remembered for later, while closing brackets need to be checked against what's already been remembered - swapping these roles breaks the whole algorithm.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: does this sequence of paired, nested markers close in the correct, reversed order?",
                "The most recently opened item must be the next one closed - a strong hint for last-in-first-out behavior, which is exactly what a stack provides.",
                "A brute-force answer would repeatedly search and remove matched pairs, which a single stack-based pass can replace.",
            ),
            takeaway = "When validating nested, nesting-order-sensitive structures like brackets, use a stack: push on the way in, and check-then-pop on the way out - the stack naturally enforces that things close in the reverse order they opened.",
        ),
        nextSlug = "min-stack",
    )

    private val minStackLesson = RoadmapLesson(
        slug = "min-stack",
        title = "Min Stack",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Design a stack that supports push, pop, top, and retrieving the minimum element, all in constant time.",
        constraints = listOf(
            "All operations (push, pop, top, getMin) must run in O(1) time.",
            "pop and top are only ever called on a non-empty stack.",
            "The stack may contain duplicate values, including duplicate minimums.",
            "Aim for a solution that doesn't need to rescan the stack's contents to find the minimum.",
        ),
        examples = listOf(
            LessonExample("push(-2), push(0), push(-3), then getMin()", "-3", "The smallest of -2, 0, and -3 is -3."),
            LessonExample("After that, pop(), then top()", "0", "Popping removes -3 (the last pushed value), leaving 0 on top of the stack."),
            LessonExample("After that, getMin()", "-2", "With -3 popped off, the remaining values are -2 and 0, so the minimum is now -2."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "min-stack-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach retrieves the minimum in constant time without rescanning the stack?",
                choices = listOf(
                    LessonChoice(
                        "Scan the entire stack every time getMin() is called to find the smallest value.",
                        false,
                        "This correctly finds the minimum, but scanning the whole stack costs O(n) per call, not the constant time the problem requires.",
                    ),
                    LessonChoice(
                        "Maintain a second stack that tracks the minimum value at each point, pushing a new minimum alongside every push and popping it alongside every pop.",
                        true,
                        "Because the second stack's top always reflects 'the minimum among everything currently on the main stack', both stacks can be pushed and popped together in constant time, keeping getMin() instant.",
                    ),
                    LessonChoice(
                        "Keep a single variable holding the overall minimum ever pushed, updating it only when a smaller value is pushed.",
                        false,
                        "A single variable can't be un-learned when the minimum value is popped off - once that minimum is removed, there's no way to recover what the minimum was before it, without rescanning.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "min-stack-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use a second stack to track minimums. Which one keeps it correctly synchronized with the main stack?",
                choices = listOf(
                    LessonChoice(
                        text = "Pushes the smaller of the new value and the current minimum onto the min-stack every time a value is pushed onto the main stack, and pops both stacks together.",
                        correct = true,
                        feedback = "Keeping the min-stack exactly as tall as the main stack, always holding 'the minimum so far at this depth', means popping either stack always leaves both perfectly in sync.",
                        code = "class MinStack {\n    private val stack = ArrayDeque<Int>()\n    private val minStack = ArrayDeque<Int>()\n\n    fun push(value: Int) {\n        stack.addLast(value)\n        val currentMin = if (minStack.isEmpty()) value else minOf(value, minStack.last())\n        minStack.addLast(currentMin)\n    }\n\n    fun pop() {\n        stack.removeLast()\n        minStack.removeLast()\n    }\n\n    fun top(): Int = stack.last()\n    fun getMin(): Int = minStack.last()\n}",
                    ),
                    LessonChoice(
                        text = "Only pushes onto the min-stack when the new value is smaller than the current minimum, leaving the min-stack shorter than the main stack.",
                        correct = false,
                        feedback = "If the min-stack only grows on new minimums, popping the main stack can remove a value without a matching pop on the min-stack, leaving the two stacks out of sync and getMin() returning a stale answer.",
                        code = "class MinStack {\n    private val stack = ArrayDeque<Int>()\n    private val minStack = ArrayDeque<Int>()\n\n    fun push(value: Int) {\n        stack.addLast(value)\n        if (minStack.isEmpty() || value < minStack.last()) minStack.addLast(value)\n    }\n\n    fun pop() {\n        stack.removeLast()\n        minStack.removeLast()\n    }\n\n    fun top(): Int = stack.last()\n    fun getMin(): Int = minStack.last()\n}",
                    ),
                    LessonChoice(
                        text = "Pushes onto the min-stack on every push, but never pops from the min-stack when the main stack is popped.",
                        correct = false,
                        feedback = "Without popping the min-stack alongside the main stack, the min-stack keeps growing forever and its top no longer reflects the minimum of only what's currently on the main stack.",
                        code = "class MinStack {\n    private val stack = ArrayDeque<Int>()\n    private val minStack = ArrayDeque<Int>()\n\n    fun push(value: Int) {\n        stack.addLast(value)\n        val currentMin = if (minStack.isEmpty()) value else minOf(value, minStack.last())\n        minStack.addLast(currentMin)\n    }\n\n    fun pop() {\n        stack.removeLast()\n    }\n\n    fun top(): Int = stack.last()\n    fun getMin(): Int = minStack.last()\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "min-stack-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version's getMin() sometimes returns a value that's no longer on the stack. What's the bug?",
                code = "fun push(value: Int) {\n    stack.addLast(value)\n    val currentMin = if (minStack.isEmpty()) value else minOf(value, minStack.last())\n    minStack.addLast(currentMin)\n}\n\nfun pop() {\n    stack.removeLast()\n}\n\nfun getMin(): Int = minStack.last()",
                choices = listOf(
                    LessonChoice(
                        "Change minOf(value, minStack.last()) to maxOf(value, minStack.last()).",
                        false,
                        "That would track the running maximum instead of the running minimum, which is a completely different, and incorrect, value for getMin() to return.",
                    ),
                    LessonChoice(
                        "Add minStack.removeLast() inside pop(), alongside stack.removeLast().",
                        true,
                        "Without popping the min-stack every time the main stack is popped, the two stacks drift out of sync - the min-stack keeps every historical minimum around even after the corresponding values have been removed from the main stack.",
                    ),
                    LessonChoice(
                        "Change stack.addLast(value) to stack.addFirst(value).",
                        false,
                        "Changing where values are added on the main stack would break its own top() and pop() behavior entirely, and doesn't address why the min-stack and main stack fall out of sync.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "min-stack-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "What is the time complexity of push, pop, top, and getMin() with the two-stack approach?",
                choices = listOf(
                    LessonChoice(
                        "All four operations run in O(1) time, because each only ever reads or modifies the top of one or both stacks.",
                        true,
                        "Pushing and popping the tops of two stacks, or reading their tops, never depends on how many elements are stored below - each operation does a fixed, constant amount of work.",
                    ),
                    LessonChoice(
                        "push and pop are O(1), but getMin() is O(n) because it must search the min-stack.",
                        false,
                        "getMin() doesn't search anything - it just reads the top of the min-stack directly, which is exactly as fast as reading the top of the main stack.",
                    ),
                    LessonChoice(
                        "All four operations are O(log n), because maintaining the minimum requires a balanced structure.",
                        false,
                        "No balanced or sorted structure is used here - both the main stack and the min-stack are simple, ordinary stacks with constant-time top, push, and pop operations.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "min-stack-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the min-stack use compared to the main stack?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only the current minimum needs to be remembered.",
                        false,
                        "A single remembered minimum can't be recovered once it's popped off - the min-stack needs one entry per value on the main stack to correctly 'remember' the minimum at every depth.",
                    ),
                    LessonChoice(
                        "O(log n), because the minimum only changes occasionally as values are pushed.",
                        false,
                        "How often the minimum actually changes doesn't limit the min-stack's size - it holds one entry for every push, regardless of whether that entry differs from the one below it.",
                    ),
                    LessonChoice(
                        "O(n), because the min-stack holds exactly one entry for every entry on the main stack.",
                        true,
                        "Every push adds one entry to both stacks together, so the min-stack always has exactly as many entries as the main stack, giving it the same O(n) size.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture keeping two stacks of cards side by side, always the same height.\n\nThe first stack holds the actual values, in the order they were pushed, just like a normal stack.\n\nThe second stack holds, at every position, 'the smallest value seen so far, from the bottom up to this point' - so its very top always tells you the minimum of everything currently on the main stack, without having to look anywhere else.",
            walkthrough = listOf(
                "push(-2): main = [-2]. min-stack has no entries yet, so currentMin = -2. min-stack = [-2].",
                "push(0): main = [-2, 0]. currentMin = min(0, -2) = -2. min-stack = [-2, -2].",
                "push(-3): main = [-2, 0, -3]. currentMin = min(-3, -2) = -3. min-stack = [-2, -2, -3]. getMin() returns -3.",
                "pop(): both stacks lose their top entry. main = [-2, 0]. min-stack = [-2, -2]. top() now returns 0.",
                "getMin(): reads min-stack's top, which is -2.",
                "The min-stack always mirrors exactly what the minimum would be if you looked at the main stack up to that same depth.",
            ),
            pseudocode = "stack = empty, minStack = empty\npush(value):\n    stack.push(value)\n    currentMin = value if minStack empty, else min(value, minStack.top())\n    minStack.push(currentMin)\npop():\n    stack.pop()\n    minStack.pop()\ntop(): return stack.top()\ngetMin(): return minStack.top()",
            referenceCode = "class MinStack {\n    private val stack = ArrayDeque<Int>()\n    private val minStack = ArrayDeque<Int>()\n\n    fun push(value: Int) {\n        stack.addLast(value)\n        val currentMin = if (minStack.isEmpty()) value else minOf(value, minStack.last())\n        minStack.addLast(currentMin)\n    }\n\n    fun pop() {\n        stack.removeLast()\n        minStack.removeLast()\n    }\n\n    fun top(): Int = stack.last()\n    fun getMin(): Int = minStack.last()\n}",
            timeComplexity = "O(1) for every operation - push, pop, top, and getMin().\n\nEach operation only ever reads or changes the very top of one or both stacks.\n\nNone of them need to look any deeper into the stack, no matter how many values have been pushed.",
            spaceComplexity = "O(n), where n is the number of values pushed, because the min-stack holds exactly one entry for every entry on the main stack.\n\nThis doubles the memory compared to a plain stack, but that extra entry per push is what makes getMin() instant rather than requiring a rescan.",
            alternatives = listOf(
                "Scan the entire main stack every time getMin() is called, without maintaining any second structure.\nThis uses no extra memory at all.\nBut it makes getMin() cost O(n) per call, which fails the constant-time requirement the moment the stack has more than a handful of values.",
                "Store, alongside each value pushed, just the difference between it and the previous minimum, rather than the minimum itself, to save some memory in certain cases.\nThis can reduce memory usage in specific scenarios.\nBut it's meaningfully trickier to implement correctly than simply mirroring the minimum in a second stack.",
            ),
            commonMistakes = listOf(
                "Only pushing onto the min-stack when a new value happens to be smaller than the current minimum.\nThat leaves the min-stack shorter than the main stack, so popping the main stack can remove a value without a matching pop on the min-stack, leaving the two out of sync.\nThe min-stack needs one entry pushed for every single push on the main stack, not just new minimums.",
                "Forgetting to pop the min-stack whenever the main stack is popped.\nWithout that matching pop, the min-stack keeps growing and its top no longer reflects the minimum of only what's currently on the main stack.\nEvery pop() call needs to remove from both stacks together.",
                "Using a single variable to track the overall minimum instead of a full second stack.\nA single variable has no way to 'remember' what the minimum was before the current one gets popped off - only a stack of historical minimums, one per depth, can recover the right value after a pop.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: how can I answer 'what's the best value currently available?' instantly, even as items get added and removed?",
                "A value that's popped off needs its associated 'best so far' information to also disappear correctly - a hint that auxiliary information should be pushed and popped in lockstep with the main data.",
                "Rescanning to recompute an aggregate value (like a minimum) every time it's asked for is a sign that maintaining it incrementally, alongside the main structure, would be faster.",
            ),
            takeaway = "When a stack needs to answer 'what's the minimum (or maximum) right now?' instantly, maintain a second, parallel stack that tracks that aggregate at every depth, pushed and popped in lockstep with the main stack - it turns an O(n) rescan into an O(1) lookup.",
        ),
        nextSlug = "evaluate-reverse-polish-notation",
    )

    private val evaluateReversePolishNotation = RoadmapLesson(
        slug = "evaluate-reverse-polish-notation",
        title = "Evaluate Reverse Polish Notation",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given a list of tokens representing an arithmetic expression in Reverse Polish Notation (postfix notation, where operators come after their operands), evaluate the expression and return the result.",
        constraints = listOf(
            "Valid operators are +, -, *, and / (integer division that truncates toward zero).",
            "Each operand may be an integer, including negative numbers.",
            "The input is always a valid RPN expression - division by zero does not occur.",
            "Aim for a single pass through the tokens rather than repeatedly rescanning the expression.",
        ),
        examples = listOf(
            LessonExample("[\"2\",\"1\",\"+\",\"3\",\"*\"]", "9", "((2 + 1) * 3) = 9."),
            LessonExample("[\"4\",\"13\",\"5\",\"/\",\"+\"]", "6", "(4 + (13 / 5)) = 4 + 2 = 6, since integer division truncates."),
            LessonExample("[\"10\",\"6\",\"9\",\"3\",\"+\",\"-11\",\"*\",\"/\",\"*\",\"17\",\"+\",\"5\",\"+\"]", "22", "A longer expression that evaluates step by step to 22."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "rpn-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach evaluates an RPN expression in a single pass through the tokens?",
                choices = listOf(
                    LessonChoice(
                        "Convert the expression back to standard infix notation with parentheses, then evaluate that using normal order-of-operations rules.",
                        false,
                        "Converting to infix and re-parsing it adds significant extra work when RPN can actually be evaluated directly, without ever needing parentheses or operator precedence rules.",
                    ),
                    LessonChoice(
                        "Push numbers onto a stack; when an operator is seen, pop the two most recent numbers, apply the operator, and push the result back onto the stack.",
                        true,
                        "Because RPN places an operator immediately after the two operands it applies to, the two most recently pushed numbers are always exactly the ones that operator needs - a stack naturally provides that 'most recent first' access.",
                    ),
                    LessonChoice(
                        "Scan the tokens from right to left instead of left to right, applying each operator to the next two numbers found in that direction.",
                        false,
                        "RPN is specifically designed to be evaluated left to right using a stack - reading it backward doesn't correctly reconstruct the intended order of operations.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rpn-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use a stack for numbers. Which one applies operators in the correct order?",
                choices = listOf(
                    LessonChoice(
                        text = "Pops the second-to-top value as the left operand and the top value as the right operand, so that for subtraction and division, order is preserved correctly.",
                        correct = true,
                        feedback = "Since the top of the stack was pushed most recently, it's the second number in the original expression, meaning it must be the right operand - getting this order right matters especially for subtraction and division.",
                        code = "fun evalRPN(tokens: Array<String>): Int {\n    val stack = ArrayDeque<Int>()\n    val ops = setOf(\"+\", \"-\", \"*\", \"/\")\n    for (token in tokens) {\n        if (token in ops) {\n            val right = stack.removeLast()\n            val left = stack.removeLast()\n            val result = when (token) {\n                \"+\" -> left + right\n                \"-\" -> left - right\n                \"*\" -> left * right\n                else -> left / right\n            }\n            stack.addLast(result)\n        } else {\n            stack.addLast(token.toInt())\n        }\n    }\n    return stack.last()\n}",
                    ),
                    LessonChoice(
                        text = "Pops the top value as the left operand and the second-to-top value as the right operand.",
                        correct = false,
                        feedback = "This swaps which operand is 'left' and which is 'right' - for commutative operators like + and * the result happens to be the same, but for - and / it produces the wrong answer.",
                        code = "fun evalRPN(tokens: Array<String>): Int {\n    val stack = ArrayDeque<Int>()\n    val ops = setOf(\"+\", \"-\", \"*\", \"/\")\n    for (token in tokens) {\n        if (token in ops) {\n            val left = stack.removeLast()\n            val right = stack.removeLast()\n            val result = when (token) {\n                \"+\" -> left + right\n                \"-\" -> left - right\n                \"*\" -> left * right\n                else -> left / right\n            }\n            stack.addLast(result)\n        } else {\n            stack.addLast(token.toInt())\n        }\n    }\n    return stack.last()\n}",
                    ),
                    LessonChoice(
                        text = "Peeks at the top two values without removing them from the stack, then pushes the result on top of them as well.",
                        correct = false,
                        feedback = "Leaving the original two operands on the stack alongside the new result means the stack keeps growing with stale values instead of replacing the two operands with their combined result.",
                        code = "fun evalRPN(tokens: Array<String>): Int {\n    val stack = ArrayDeque<Int>()\n    val ops = setOf(\"+\", \"-\", \"*\", \"/\")\n    for (token in tokens) {\n        if (token in ops) {\n            val right = stack.last()\n            val left = stack[stack.size - 2]\n            val result = when (token) {\n                \"+\" -> left + right\n                \"-\" -> left - right\n                \"*\" -> left * right\n                else -> left / right\n            }\n            stack.addLast(result)\n        } else {\n            stack.addLast(token.toInt())\n        }\n    }\n    return stack.last()\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rpn-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version returns the wrong result for expressions using subtraction or division, but works fine for addition and multiplication. What's the bug?",
                code = "if (token in ops) {\n    val a = stack.removeLast()\n    val b = stack.removeLast()\n    val result = when (token) {\n        \"+\" -> a + b\n        \"-\" -> a - b\n        \"*\" -> a * b\n        else -> a / b\n    }\n    stack.addLast(result)\n}",
                choices = listOf(
                    LessonChoice(
                        "Change stack.removeLast() to stack.removeFirst() for both a and b.",
                        false,
                        "Changing which end of the stack is used for removal would break the stack's fundamental last-in-first-out behavior entirely, not just fix the operand order for subtraction and division.",
                    ),
                    LessonChoice(
                        "Swap which popped value is used as the left operand: use b (popped second) as the left operand and a (popped first) as the right operand.",
                        true,
                        "The value popped first, a, was pushed most recently and is actually the second number in the original expression - it belongs on the right side of subtraction and division, not the left.",
                    ),
                    LessonChoice(
                        "Change else -> a / b to else -> b / a.",
                        false,
                        "Fixing only the division case while leaving subtraction with the swapped operands still produces wrong answers for subtraction expressions.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rpn-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of tokens, what is the time complexity of the stack-based evaluation?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because every operator must search the stack for its operands.",
                        false,
                        "The stack is only ever accessed at its very top, using constant-time pops and pushes - it's never searched through.",
                    ),
                    LessonChoice(
                        "O(n log n), because the numbers must be sorted before applying operators.",
                        false,
                        "Nothing here gets sorted - operators are applied directly to whichever two numbers are currently on top of the stack, in the order the tokens appear.",
                    ),
                    LessonChoice(
                        "O(n), because each token causes a constant number of stack operations: either one push, or two pops and one push.",
                        true,
                        "One pass through the tokens, with a fixed, small amount of work per token, gives time proportional to n.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rpn-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the stack use in the worst case?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because an expression made mostly of numbers with few operators can leave many values on the stack at once.",
                        true,
                        "In the worst case, most of the tokens are numbers pushed before any operator reduces them, so the stack can grow to hold close to n values.",
                    ),
                    LessonChoice(
                        "O(1), because operators immediately reduce two numbers into one, keeping the stack small.",
                        false,
                        "Operators do reduce pairs of numbers, but a long run of numbers before any operators appear can still push many values onto the stack before any reduction happens.",
                    ),
                    LessonChoice(
                        "O(log n), because the stack only grows when operators are deeply nested.",
                        false,
                        "There's no notion of nesting depth limiting the stack here - the stack's size is determined directly by how many numbers get pushed before being consumed by operators, which can be close to n.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture reading the expression left to right and keeping a stack of numbers you haven't used yet.\n\nWhenever you see a number, set it down on the stack.\n\nWhenever you see an operator, it always applies to the two numbers you set down most recently - pick them up, combine them with the operator, and set the result back down as if it were a single number, ready to be used by a later operator.",
            walkthrough = listOf(
                "Tokens [\"2\",\"1\",\"+\",\"3\",\"*\"]. Push 2: stack = [2]. Push 1: stack = [2, 1].",
                "See '+': pop 1 (right) and 2 (left). Compute 2 + 1 = 3. Push 3: stack = [3].",
                "Push 3: stack = [3, 3].",
                "See '*': pop 3 (right) and 3 (left). Compute 3 * 3 = 9. Push 9: stack = [9].",
                "No more tokens. The single value left on the stack is the answer.",
                "Return 9.",
            ),
            pseudocode = "stack = empty\nfor each token in tokens:\n    if token is an operator:\n        right = pop stack\n        left = pop stack\n        result = apply operator to left and right\n        push result\n    else:\n        push token as a number\nreturn the single value left on the stack",
            referenceCode = "fun evalRPN(tokens: Array<String>): Int {\n    val stack = ArrayDeque<Int>()\n    val ops = setOf(\"+\", \"-\", \"*\", \"/\")\n    for (token in tokens) {\n        if (token in ops) {\n            val right = stack.removeLast()\n            val left = stack.removeLast()\n            val result = when (token) {\n                \"+\" -> left + right\n                \"-\" -> left - right\n                \"*\" -> left * right\n                else -> left / right\n            }\n            stack.addLast(result)\n        } else {\n            stack.addLast(token.toInt())\n        }\n    }\n    return stack.last()\n}",
            timeComplexity = "O(n), where n is the number of tokens.\n\nEach token triggers a constant amount of stack activity: a number causes one push, and an operator causes two pops followed by one push.\n\nOne pass, constant work per token - that's linear in n.",
            spaceComplexity = "O(n) in the worst case, where n is the number of tokens.\n\nAn expression made of a long run of numbers before any operators reduce them can leave nearly all of those numbers sitting on the stack at once.\n\nSo the stack's size can grow to be close to the total number of tokens.",
            alternatives = listOf(
                "Convert the RPN expression back into standard infix notation with parentheses, then evaluate it using ordinary order-of-operations parsing.\nThis might feel more familiar if you're used to reading infix expressions.\nBut it adds real extra work, reconstructing structure that RPN was specifically designed to avoid needing in the first place.",
                "Use recursion instead of an explicit stack, processing tokens from the end backward and recursively evaluating operands as they're discovered.\nThis avoids managing a stack data structure directly.\nBut it's a less natural fit for RPN's left-to-right evaluation order and uses call-stack space instead of an explicit one.",
            ),
            commonMistakes = listOf(
                "Swapping which popped value is treated as the left operand and which is the right.\nThe value popped first was pushed most recently, making it the second number in the original expression - it belongs on the right side of the operator, not the left.\nGetting this backwards silently produces wrong answers specifically for subtraction and division, since addition and multiplication don't care about operand order.",
                "Forgetting that RPN needs integer division that truncates toward zero, not standard mathematical division or floor division.\nDifferent languages handle negative-number division differently by default, so it's worth confirming the division behavior matches what the problem expects.",
                "Peeking at the stack's top values without actually removing them before pushing the result.\nLeaving the original operands on the stack alongside the new combined result corrupts the rest of the evaluation, since later operators would then pick up stale values.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: how do I evaluate an expression where every operator immediately follows the values it applies to?",
                "The most recent unused values are always exactly what's needed next - a strong hint for last-in-first-out, stack-based processing.",
                "A brute-force approach involving reparsing structure, like parentheses or precedence, can often be replaced by directly processing tokens in the order given.",
            ),
            takeaway = "When evaluating postfix (RPN) expressions, push operands onto a stack and, on each operator, pop the two most recent operands, apply the operator, and push the result back - the stack always holds exactly the values still waiting to be combined.",
        ),
        nextSlug = "generate-parentheses",
    )

    private val generateParenthesesLesson = RoadmapLesson(
        slug = "generate-parentheses",
        title = "Generate Parentheses",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an integer n, generate all combinations of n pairs of parentheses that are well-formed - meaning every opening parenthesis has a matching closing one, correctly nested.",
        constraints = listOf(
            "n is at least 1.",
            "Every returned string has exactly n opening and n closing parentheses.",
            "The order of the returned combinations does not matter, but no combination should be repeated.",
            "Aim to only ever build strings that could still become valid, rather than generating every possible arrangement and filtering afterward.",
        ),
        examples = listOf(
            LessonExample("n = 3", "[\"((()))\",\"(()())\",\"(())()\",\"()(())\",\"()()()\"]", "All 5 well-formed combinations of 3 pairs of parentheses."),
            LessonExample("n = 1", "[\"()\"]", "With just one pair, only one well-formed combination exists."),
            LessonExample("n = 2", "[\"(())\",\"()()\"]", "The two well-formed combinations of 2 pairs."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "generate-parens-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach avoids generating every possible arrangement of 2n parentheses and filtering out the invalid ones afterward?",
                choices = listOf(
                    LessonChoice(
                        "Generate every possible string of n opening and n closing parentheses in any order, then check each one for validity.",
                        false,
                        "This works, but the number of possible arrangements grows extremely quickly, and most of them are invalid - checking every one wastes a lot of work on strings that could never have been valid.",
                    ),
                    LessonChoice(
                        "Build the string one character at a time, only adding an opening parenthesis if fewer than n have been used so far, and only adding a closing parenthesis if fewer closing than opening parentheses have been used so far.",
                        true,
                        "Tracking how many opening and closing parentheses have been placed so far lets the algorithm only ever build strings that could still become valid, never wasting effort on a doomed-from-the-start combination.",
                    ),
                    LessonChoice(
                        "Generate all combinations for n - 1 pairs first, then try inserting one more pair into every possible position of every existing combination.",
                        false,
                        "This can work in principle, but carefully avoiding duplicate combinations produced by inserting a pair in different but equivalent positions is significantly trickier to get right than building directly with a count-based rule.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "generate-parens-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three build strings recursively while tracking counts of opening and closing parentheses used. Which one applies the correct rules for when each can be added?",
                choices = listOf(
                    LessonChoice(
                        text = "Adds an opening parenthesis only if fewer than n have been used, but allows a closing parenthesis to be added at any time, regardless of how many opens have happened.",
                        correct = false,
                        feedback = "Without requiring fewer closes than opens so far, a closing parenthesis could be added before any matching opening one exists, producing invalid strings like ')('.",
                        code = "fun generateParenthesis(n: Int): List<String> {\n    val result = mutableListOf<String>()\n    fun backtrack(current: StringBuilder, opens: Int, closes: Int) {\n        if (current.length == 2 * n) {\n            result.add(current.toString())\n            return\n        }\n        if (opens < n) {\n            current.append('(')\n            backtrack(current, opens + 1, closes)\n            current.deleteCharAt(current.length - 1)\n        }\n        current.append(')')\n        backtrack(current, opens, closes + 1)\n        current.deleteCharAt(current.length - 1)\n    }\n    backtrack(StringBuilder(), 0, 0)\n    return result\n}",
                    ),
                    LessonChoice(
                        text = "Adds a closing parenthesis only if fewer than n have been used, using the same limit as opening parentheses, without comparing closes to opens.",
                        correct = false,
                        feedback = "Using the same simple 'fewer than n' rule for both doesn't prevent a closing parenthesis from being added before its matching opening one - the closing count specifically needs to stay behind the opening count, not just behind n.",
                        code = "fun generateParenthesis(n: Int): List<String> {\n    val result = mutableListOf<String>()\n    fun backtrack(current: StringBuilder, opens: Int, closes: Int) {\n        if (current.length == 2 * n) {\n            result.add(current.toString())\n            return\n        }\n        if (opens < n) {\n            current.append('(')\n            backtrack(current, opens + 1, closes)\n            current.deleteCharAt(current.length - 1)\n        }\n        if (closes < n) {\n            current.append(')')\n            backtrack(current, opens, closes + 1)\n            current.deleteCharAt(current.length - 1)\n        }\n    }\n    backtrack(StringBuilder(), 0, 0)\n    return result\n}",
                    ),
                    LessonChoice(
                        text = "Adds an opening parenthesis only if fewer than n have been used, and adds a closing parenthesis only if fewer closes than opens have been used so far.",
                        correct = true,
                        feedback = "Requiring the count of closes to stay behind the count of opens guarantees a closing parenthesis is never added without an unmatched opening one already placed earlier in the string.",
                        code = "fun generateParenthesis(n: Int): List<String> {\n    val result = mutableListOf<String>()\n    fun backtrack(current: StringBuilder, opens: Int, closes: Int) {\n        if (current.length == 2 * n) {\n            result.add(current.toString())\n            return\n        }\n        if (opens < n) {\n            current.append('(')\n            backtrack(current, opens + 1, closes)\n            current.deleteCharAt(current.length - 1)\n        }\n        if (closes < opens) {\n            current.append(')')\n            backtrack(current, opens, closes + 1)\n            current.deleteCharAt(current.length - 1)\n        }\n    }\n    backtrack(StringBuilder(), 0, 0)\n    return result\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "generate-parens-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version produces some strings with unbalanced parentheses, like \"(((\" for n = 3. What's the bug?",
                code = "fun backtrack(current: StringBuilder, opens: Int, closes: Int) {\n    if (current.length == 2 * n) {\n        result.add(current.toString())\n        return\n    }\n    if (opens < n) {\n        current.append('(')\n        backtrack(current, opens + 1, closes)\n        current.deleteCharAt(current.length - 1)\n    }\n    if (closes < n) {\n        current.append(')')\n        backtrack(current, opens, closes + 1)\n        current.deleteCharAt(current.length - 1)\n    }\n}",
                choices = listOf(
                    LessonChoice(
                        "Change closes < n to closes < opens.",
                        true,
                        "Comparing closes only against n allows a closing parenthesis to be added even when no unmatched opening parenthesis exists yet - it needs to be compared against how many opens have actually happened so far, not the overall target n.",
                    ),
                    LessonChoice(
                        "Change current.length == 2 * n to current.length == n.",
                        false,
                        "A complete valid combination always has 2n characters total, n openings plus n closings - stopping at length n would cut every string off halfway through, before it could even become balanced.",
                    ),
                    LessonChoice(
                        "Change opens < n to opens < n - 1.",
                        false,
                        "That would prevent the very last opening parenthesis from ever being added, making it impossible to reach n total opens - it doesn't address why closing parentheses are being added too early.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "generate-parens-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "This backtracking approach explores only valid partial combinations. Roughly how does the total work grow as n increases?",
                choices = listOf(
                    LessonChoice(
                        "It grows linearly with n, since only one combination is built at a time.",
                        false,
                        "Even though combinations are built one character at a time, there are many different valid combinations to discover for larger n - the total work reflects exploring all of them, not just following a single path.",
                    ),
                    LessonChoice(
                        "It grows as n squared, since two counts, opens and closes, are being tracked.",
                        false,
                        "Tracking two counts doesn't by itself determine the growth rate - what matters is how many valid combinations and partial paths actually get explored, which grows much faster than n squared as n increases.",
                    ),
                    LessonChoice(
                        "It grows roughly with the number of valid combinations themselves, related to the Catalan numbers, which increases faster than any fixed power of n.",
                        true,
                        "Because the algorithm only explores partial strings that could still become valid, its total work is tied directly to how many valid, and near-valid, combinations exist - a count that grows quickly, following the Catalan number pattern, as n increases.",
                    ),
                ),
                glossary = listOf(
                    GlossaryTerm("Catalan numbers", "A fast-growing sequence of counts that describes exactly how many valid parenthesis combinations exist for a given n."),
                ),
            ),
            LessonQuestion(
                id = "generate-parens-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "Not counting the space used to store the final result list, how much extra space does the recursion use?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because the same StringBuilder is reused throughout.",
                        false,
                        "Reusing one StringBuilder does avoid allocating a new one per call, but the recursive calls themselves still stack up - each level of recursion adds a frame that isn't free.",
                    ),
                    LessonChoice(
                        "O(n), because the recursion goes as deep as the length of a complete combination, which is 2n, and each level adds one call frame.",
                        true,
                        "Building one complete combination requires a chain of 2n recursive calls, one per character added, so the call stack's depth, and the extra space it uses, scales directly with n.",
                    ),
                    LessonChoice(
                        "O(2^n), because that's how many total strings of length 2n exist.",
                        false,
                        "The number of strings that could exist isn't what determines the recursion's extra space - only how deep the recursive call chain goes at any one time matters, and that depth is bounded by 2n, not by how many total combinations exist.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture building a string of parentheses one character at a time, and before adding each character, asking two simple questions: 'Have I already used all n opening parentheses?' and 'Do I have more closing parentheses placed than opening ones so far?'\n\nAn opening parenthesis can always be added as long as there's still room for one, fewer than n used.\n\nA closing parenthesis can only be added if there's an unmatched opening one already waiting for it, fewer closes than opens so far.\n\nFollowing just those two rules, and backtracking, undoing a choice and trying the other, whenever a path is explored, naturally produces only valid combinations.",
            walkthrough = listOf(
                "n = 2. Start with an empty string, opens = 0, closes = 0.",
                "Add '(': string = \"(\", opens = 1. Add '(' again: string = \"((\", opens = 2, the limit, so no more opens allowed.",
                "Add ')': string = \"(()\", closes = 1. Add ')': string = \"(())\", closes = 2. Length is 4 = 2n, so record \"(())\".",
                "Backtrack through the choices already explored until reaching back to \"(\" with only one open used.",
                "From \"(\", try ')' instead of '(': string = \"()\", closes = 1. Add '(': string = \"()(\", opens = 2. Add ')': string = \"()()\", closes = 2. Record \"()()\".",
                "All paths explored. Return [\"(())\", \"()()\"].",
            ),
            pseudocode = "result = empty list\nbacktrack(current, opens, closes):\n    if length of current == 2 * n:\n        add current to result\n        return\n    if opens < n:\n        append '(' to current; backtrack(current, opens + 1, closes); remove last character\n    if closes < opens:\n        append ')' to current; backtrack(current, opens, closes + 1); remove last character\nbacktrack(empty string, 0, 0)\nreturn result",
            referenceCode = "fun generateParenthesis(n: Int): List<String> {\n    val result = mutableListOf<String>()\n    fun backtrack(current: StringBuilder, opens: Int, closes: Int) {\n        if (current.length == 2 * n) {\n            result.add(current.toString())\n            return\n        }\n        if (opens < n) {\n            current.append('(')\n            backtrack(current, opens + 1, closes)\n            current.deleteCharAt(current.length - 1)\n        }\n        if (closes < opens) {\n            current.append(')')\n            backtrack(current, opens, closes + 1)\n            current.deleteCharAt(current.length - 1)\n        }\n    }\n    backtrack(StringBuilder(), 0, 0)\n    return result\n}",
            timeComplexity = "O(4^n / sqrt(n)), the nth Catalan number, which is roughly how many valid combinations exist for n pairs.\n\nBecause the algorithm only ever explores partial strings that could still become valid, never wasting time on paths doomed from the start, its total work stays tied directly to how many valid, and near-valid, combinations actually exist, rather than exploring every possible arrangement of 2n characters.",
            spaceComplexity = "O(n) for the recursion itself, not counting the space needed to store the final result list.\n\nBuilding one complete combination requires a chain of 2n recursive calls, one per character added, so the depth of the call stack, and the extra memory it uses, scales directly with n.",
            alternatives = listOf(
                "Generate every possible string of n opening and n closing parentheses in any order, then filter out the ones that aren't validly nested.\nThis is the most direct translation of the problem statement.\nBut the number of possible arrangements grows extremely quickly, and the vast majority of them are invalid, wasting a lot of work checking strings that could never have worked.",
                "Build up combinations for smaller values of n first, then combine and wrap them to build combinations for larger n, a dynamic programming approach based on how valid combinations are structured.\nThis can reuse previously computed smaller results.\nBut it requires more careful bookkeeping to avoid generating duplicate combinations than the direct backtracking approach.",
            ),
            commonMistakes = listOf(
                "Allowing a closing parenthesis to be added whenever fewer than n have been used, without comparing to how many opening parentheses have actually been placed.\nThat can let a closing parenthesis be added before any unmatched opening one exists, producing invalid strings.\nThe closing count specifically needs to stay behind the opening count at every step, not just behind the overall target n.",
                "Forgetting to backtrack, removing the last character added, after exploring a choice.\nWithout undoing that choice before trying the alternative, the string being built accumulates characters from both branches instead of correctly exploring each possibility independently.",
                "Stopping too early or too late by checking the wrong length condition.\nA complete valid combination always has exactly 2n characters, n openings and n closings together - checking against just n, instead of 2n, would cut every string off before it could ever be balanced.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what are all the valid ways to build something step by step, where some choices are only allowed under certain conditions?",
                "A brute-force answer would generate everything possible and filter afterward, but many invalid paths could be avoided entirely by checking a simple rule before making each choice.",
                "Building up a solution one piece at a time, undoing a choice to try an alternative, is a hallmark of backtracking.",
            ),
            takeaway = "When generating all valid combinations built step by step under a constraint, only make a choice at each step if it could still lead to a valid result, and backtrack, undo the choice, to try alternatives - that avoids wasting time on paths that could never succeed.",
        ),
        nextSlug = "daily-temperatures",
    )

    private val dailyTemperaturesLesson = RoadmapLesson(
        slug = "daily-temperatures",
        title = "Daily Temperatures",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an array of daily temperatures, return an array where each position holds the number of days you'd have to wait after that day to reach a warmer temperature. If there is no future day with a warmer temperature, put 0 at that position instead.",
        constraints = listOf(
            "The array has at least one temperature.",
            "Temperatures are given as whole numbers.",
            "A position with no future warmer day gets 0, not -1 or any other placeholder.",
            "Aim for linear time rather than scanning forward from every single day.",
        ),
        examples = listOf(
            LessonExample("[73,74,75,71,69,72,76,73]", "[1,1,4,2,1,1,0,0]", "Day 0 (73) waits 1 day for 74; day 2 (75) waits 4 days for 76; the last two days have no warmer day ahead, so 0."),
            LessonExample("[30,40,50,60]", "[1,1,1,0]", "Each day is immediately followed by a warmer one, except the last."),
            LessonExample("[30,60,90]", "[1,1,0]", "Similarly, each day but the last has an immediately warmer next day."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "daily-temps-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds every day's answer without scanning forward from each day individually?",
                choices = listOf(
                    LessonChoice(
                        "For each day, scan forward through the rest of the array until a warmer temperature is found, counting how many days that takes.",
                        false,
                        "This finds the correct answer, but scanning forward from every single day can cost O(n squared) in the worst case, such as when temperatures are strictly decreasing.",
                    ),
                    LessonChoice(
                        "Walk through the temperatures once, maintaining a stack of days that are still waiting for a warmer day; whenever the current temperature is warmer than the temperature on top of the stack, pop it and record the day difference.",
                        true,
                        "The stack holds exactly the days that haven't yet found their answer - each day is pushed once and popped once, when its answer is finally found, keeping the total work linear.",
                    ),
                    LessonChoice(
                        "Sort the temperatures and match each one to the nearest larger value in the sorted order.",
                        false,
                        "Sorting destroys the original day-to-day order, but the answer specifically depends on how many days must pass in the original sequence - a sorted rearrangement loses that information entirely.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "daily-temps-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use a stack of day indices waiting for a warmer temperature. Which one correctly resolves them?",
                choices = listOf(
                    LessonChoice(
                        text = "While the stack isn't empty and today's temperature is warmer than the temperature at the index on top of the stack, pops that index and records today's index minus the popped index as its answer.",
                        correct = true,
                        feedback = "Popping every index whose day has now found a warmer temperature, and recording the exact number of days that passed for each one, correctly resolves every waiting day as soon as its answer becomes known.",
                        code = "fun dailyTemperatures(temperatures: IntArray): IntArray {\n    val answer = IntArray(temperatures.size)\n    val stack = ArrayDeque<Int>()\n    for (i in temperatures.indices) {\n        while (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n            val prevDay = stack.removeLast()\n            answer[prevDay] = i - prevDay\n        }\n        stack.addLast(i)\n    }\n    return answer\n}",
                    ),
                    LessonChoice(
                        text = "Pops only a single index from the stack, using if instead of while, even when multiple waiting days could all be resolved by today's temperature.",
                        correct = false,
                        feedback = "If several earlier days were all cooler than today, all of them should be resolved today - stopping after popping just one leaves the others waiting even though today's temperature already answers their question too.",
                        code = "fun dailyTemperatures(temperatures: IntArray): IntArray {\n    val answer = IntArray(temperatures.size)\n    val stack = ArrayDeque<Int>()\n    for (i in temperatures.indices) {\n        if (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n            val prevDay = stack.removeLast()\n            answer[prevDay] = i - prevDay\n        }\n        stack.addLast(i)\n    }\n    return answer\n}",
                    ),
                    LessonChoice(
                        text = "Pushes the temperature values themselves onto the stack instead of the day indices.",
                        correct = false,
                        feedback = "Without the day index, there's no way to compute how many days passed between the two days - the answer specifically needs the difference in positions, not just a comparison of temperature values.",
                        code = "fun dailyTemperatures(temperatures: IntArray): IntArray {\n    val answer = IntArray(temperatures.size)\n    val stack = ArrayDeque<Int>()\n    for (i in temperatures.indices) {\n        while (stack.isNotEmpty() && temperatures[i] > stack.last()) {\n            stack.removeLast()\n        }\n        stack.addLast(temperatures[i])\n    }\n    return answer\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "daily-temps-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version leaves every position in the answer array as 0, even where a warmer day clearly follows. What's the bug?",
                code = "val answer = IntArray(temperatures.size)\nval stack = ArrayDeque<Int>()\nfor (i in temperatures.indices) {\n    while (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n        stack.removeLast()\n    }\n    stack.addLast(i)\n}",
                choices = listOf(
                    LessonChoice(
                        "Change temperatures[i] > temperatures[stack.last()] to temperatures[i] >= temperatures[stack.last()].",
                        false,
                        "Using >= instead of > only changes how exactly-equal temperatures are handled - it doesn't address why the answer array is never actually being written to at all.",
                    ),
                    LessonChoice(
                        "Inside the while loop, save the popped index before removing it, and set answer[poppedIndex] = i - poppedIndex.",
                        true,
                        "The while loop currently pops indices off the stack but never records anything into the answer array - without capturing the popped index and computing the day difference, every position stays at its initial value of 0.",
                    ),
                    LessonChoice(
                        "Change stack.addLast(i) to stack.addFirst(i).",
                        false,
                        "Adding to the front instead of the back would break the stack's last-in-first-out order entirely, and doesn't address the missing answer-array update.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "daily-temps-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of days, what is the time complexity of the stack-based approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because for each day, the stack might need to pop many earlier days.",
                        false,
                        "Even though a single day can pop several earlier days at once, each day is only ever pushed onto the stack once and popped from it at most once across the entire run - the total number of stack operations is bounded by n.",
                    ),
                    LessonChoice(
                        "O(n log n), because the stack must stay sorted by temperature.",
                        false,
                        "The stack isn't kept in any particular sorted order through insertion - it's popped based on direct comparisons to today's temperature, using plain constant-time stack operations.",
                    ),
                    LessonChoice(
                        "O(n), because each day is pushed onto the stack exactly once and popped at most once, across the whole run of the algorithm.",
                        true,
                        "Even though some days trigger multiple pops at once, the combined total of every push and pop across the entire algorithm never exceeds roughly 2n.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "daily-temps-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space do the stack and the answer array use together?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because both the answer array and the stack, in the worst case, can each hold up to n entries.",
                        true,
                        "The answer array always has exactly n entries, one per day, and the stack can hold up to n day indices at once, such as when temperatures are strictly decreasing and nothing gets resolved until the very end.",
                    ),
                    LessonChoice(
                        "O(1), because the stack only ever holds a small, fixed number of days waiting for resolution.",
                        false,
                        "The stack can grow much larger than a small fixed number - if temperatures keep dropping day after day, every one of those days stays on the stack, unresolved, until a warmer day finally appears.",
                    ),
                    LessonChoice(
                        "O(log n), because the stack only grows when temperatures increase, which happens gradually.",
                        false,
                        "How often temperatures happen to increase doesn't bound the stack logarithmically - in the worst case, strictly decreasing temperatures, the stack grows linearly, holding every single day at once.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture a line of people waiting for someone taller to walk by.\n\nAs each new day's temperature comes in, check whether it's warmer than the temperature of whoever's been waiting longest at the back of the line, the most recently added, still-unresolved day.\n\nIf it is, that waiting day finally gets its answer: today minus that day's own index. Keep checking and resolving waiting days from the back of the line until the current temperature isn't warmer than whoever's left waiting, then add today to the back of the line to wait for its own turn.",
            walkthrough = listOf(
                "Temperatures [73,74,75,71,69,72,76,73]. Day 0 (73): stack is empty, push. stack = [0].",
                "Day 1 (74): 74 > temperatures[0]=73, so pop day 0 and set answer[0] = 1 - 0 = 1. Push day 1. stack = [1].",
                "Day 2 (75): 75 > temperatures[1]=74, pop day 1, answer[1] = 2 - 1 = 1. Push day 2. stack = [2].",
                "Day 3 (71): not warmer than temperatures[2]=75, so just push. stack = [2, 3].",
                "Day 4 (69): not warmer than temperatures[3]=71, push. stack = [2, 3, 4].",
                "Day 5 (72): warmer than temperatures[4]=69 (pop, answer[4]=1) and temperatures[3]=71 (pop, answer[3]=2), but not warmer than temperatures[2]=75, so stop and push day 5. Continue similarly for the rest, giving the full answer [1,1,4,2,1,1,0,0].",
            ),
            pseudocode = "answer = array of n zeros\nstack = empty (holds day indices)\nfor i from 0 to n - 1:\n    while stack is not empty and temperatures[i] > temperatures[stack's top]:\n        prevDay = pop stack\n        answer[prevDay] = i - prevDay\n    push i onto stack\nreturn answer",
            referenceCode = "fun dailyTemperatures(temperatures: IntArray): IntArray {\n    val answer = IntArray(temperatures.size)\n    val stack = ArrayDeque<Int>()\n    for (i in temperatures.indices) {\n        while (stack.isNotEmpty() && temperatures[i] > temperatures[stack.last()]) {\n            val prevDay = stack.removeLast()\n            answer[prevDay] = i - prevDay\n        }\n        stack.addLast(i)\n    }\n    return answer\n}",
            timeComplexity = "O(n), where n is the number of days.\n\nEach day is pushed onto the stack exactly once, and it can only ever be popped once, when its answer is finally resolved.\n\nAcross the whole run of the algorithm, the total number of pushes and pops combined never exceeds roughly 2n.",
            spaceComplexity = "O(n), because both the answer array and the stack can each grow to hold up to n entries.\n\nIn the worst case, temperatures that only ever decrease, every day gets pushed onto the stack and none of them get resolved until the array ends, leaving all n days on the stack at once.",
            alternatives = listOf(
                "For each day, scan forward through the rest of the array until a warmer temperature is found.\nThis is the most direct translation of the problem statement.\nBut scanning forward from every day can cost O(n squared) in the worst case, such as when temperatures only ever decrease.",
                "Scan the array from right to left, keeping track of the closest warmer temperature seen so far at each step using auxiliary structures.\nThis can also reach O(n) time.\nBut it requires more careful bookkeeping to correctly find 'the nearest warmer day' walking backward than the natural forward stack-based approach.",
            ),
            commonMistakes = listOf(
                "Using an if statement instead of a while loop when popping the stack.\nA single day's temperature might resolve several earlier waiting days at once, not just the most recent one - the loop needs to keep popping and resolving for as long as today's temperature is warmer than whatever's on top of the stack.",
                "Storing temperature values in the stack instead of day indices.\nWithout the index, there's no way to compute how many days passed between the waiting day and the day that resolves it - the answer requires that day-count difference, not just a temperature comparison.",
                "Forgetting to actually write into the answer array when a day is resolved.\nPopping a day off the stack without recording its answer leaves that position at its default value of 0, even when a warmer day genuinely follows.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: for each item, how far away is the next item that satisfies some 'better than me' condition?",
                "A brute-force answer would scan forward from every position, but a stack of 'still waiting' positions can resolve many of them at once as better values are found.",
                "Once a later value resolves an earlier one, that earlier value never needs to be reconsidered again - a hint for a monotonic stack.",
            ),
            takeaway = "When you need, for each position, the distance to the next element that's 'better' in some way, maintain a stack of positions still waiting for that better element - resolve, and pop, every waiting position that a new element beats, keeping every push and pop bounded to happen at most once per element.",
        ),
        nextSlug = "car-fleet",
    )

    private val carFleetLesson = RoadmapLesson(
        slug = "car-fleet",
        title = "Car Fleet",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "A number of cars are driving toward the same destination along a single-lane road. Given each car's starting position and speed, and the position of the destination, determine how many car fleets will arrive - cars traveling together, close enough that they never pass each other, count as a single fleet.",
        constraints = listOf(
            "All cars start at distinct positions, all less than the destination's position.",
            "Cars cannot pass each other; a faster car catches up to a slower one ahead and then travels at the slower car's pace from then on.",
            "A fleet is any group of cars that ends up arriving at the destination at the same time because of this catching-up behavior.",
            "Aim for a solution based on sorting by starting position rather than simulating every moment of the drive.",
        ),
        examples = listOf(
            LessonExample("target = 12, position = [10,8,0,5,3], speed = [2,4,1,1,3]", "3", "Sorting by position and computing arrival times reveals that some cars merge into shared fleets, leaving 3 distinct fleets."),
            LessonExample("target = 10, position = [3], speed = [3]", "1", "A single car is trivially its own fleet."),
            LessonExample("target = 100, position = [0,2,4], speed = [4,2,1]", "1", "Every car ends up merged into a single fleet, since each car behind eventually catches up to the one ahead before reaching the destination."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "car-fleet-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach determines the number of fleets without simulating the drive moment by moment?",
                choices = listOf(
                    LessonChoice(
                        "Simulate the positions of every car at every unit of time until all cars reach the destination, checking which ones are adjacent when they arrive.",
                        false,
                        "This can work in principle, but simulating every moment in time is far more work than necessary, and choosing a fine enough time step to be accurate is tricky.",
                    ),
                    LessonChoice(
                        "Sort the cars by their starting position, closest to the destination first, compute how long each car alone would take to reach the destination, then scan from the car closest to the destination backward, merging a car into the fleet ahead if its own arrival time is less than or equal to that fleet's time.",
                        true,
                        "A car can never arrive later than the fleet directly ahead of it if its own solo arrival time is already less than or equal to that fleet's time - it will simply catch up and join, so no moment-by-moment simulation is needed.",
                    ),
                    LessonChoice(
                        "Sort the cars by speed instead of by position, and group cars with similar speeds together.",
                        false,
                        "Similar speeds alone don't determine whether cars merge into a fleet - a fast car starting far behind a slow car directly ahead of it will still be blocked and forced to merge, regardless of how their speeds compare to other, unrelated cars.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "car-fleet-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three sort by position and compute solo arrival times. Which one correctly counts the fleets while scanning?",
                choices = listOf(
                    LessonChoice(
                        text = "Scans from the car closest to the destination backward, keeping a running 'current fleet's arrival time'; whenever a car's own solo arrival time is greater than that running time, it starts a new fleet and updates the running time.",
                        correct = true,
                        feedback = "Processing from the front of the line backward means each car is checked against the fleet directly ahead of it - if it would arrive later on its own, it can't catch up and forms a new fleet, but if it would arrive sooner, it merges into the fleet ahead instead.",
                        code = "fun carFleet(target: Int, position: IntArray, speed: IntArray): Int {\n    val cars = position.indices.sortedByDescending { position[it] }\n    var fleets = 0\n    var currentArrival = 0.0\n    for (i in cars) {\n        val arrival = (target - position[i]).toDouble() / speed[i]\n        if (arrival > currentArrival) {\n            fleets++\n            currentArrival = arrival\n        }\n    }\n    return fleets\n}",
                    ),
                    LessonChoice(
                        text = "Scans from the car furthest from the destination forward instead of from the closest car backward.",
                        correct = false,
                        feedback = "A car can only be blocked by a car ahead of it, closer to the destination, so the comparison needs to happen against whatever fleet is directly ahead - scanning from the back forward compares each car against a fleet that hasn't been determined yet.",
                        code = "fun carFleet(target: Int, position: IntArray, speed: IntArray): Int {\n    val cars = position.indices.sortedBy { position[it] }\n    var fleets = 0\n    var currentArrival = 0.0\n    for (i in cars) {\n        val arrival = (target - position[i]).toDouble() / speed[i]\n        if (arrival > currentArrival) {\n            fleets++\n            currentArrival = arrival\n        }\n    }\n    return fleets\n}",
                    ),
                    LessonChoice(
                        text = "Starts a new fleet whenever a car's solo arrival time is less than the running current arrival time, instead of greater.",
                        correct = false,
                        feedback = "A car that would arrive sooner than the fleet ahead is exactly the case where it catches up and merges - flipping the comparison would count merging cars as new fleets and vice versa.",
                        code = "fun carFleet(target: Int, position: IntArray, speed: IntArray): Int {\n    val cars = position.indices.sortedByDescending { position[it] }\n    var fleets = 0\n    var currentArrival = 0.0\n    for (i in cars) {\n        val arrival = (target - position[i]).toDouble() / speed[i]\n        if (arrival < currentArrival) {\n            fleets++\n            currentArrival = arrival\n        }\n    }\n    return fleets\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "car-fleet-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version reports too many fleets, treating cars that should merge as separate. What's the bug?",
                code = "val cars = position.indices.sortedByDescending { position[it] }\nvar fleets = 0\nvar currentArrival = 0.0\nfor (i in cars) {\n    val arrival = (target - position[i]).toDouble() / speed[i]\n    fleets++\n    if (arrival <= currentArrival) fleets--\n    currentArrival = arrival\n}",
                choices = listOf(
                    LessonChoice(
                        "Change sortedByDescending { position[it] } to sortedBy { position[it] }.",
                        false,
                        "Sorting in the wrong direction would compare each car against the wrong neighbor entirely, which is a different bug than the one causing every car to be treated as its own fleet.",
                    ),
                    LessonChoice(
                        "Change (target - position[i]).toDouble() / speed[i] to (target - position[i]) / speed[i].toDouble().",
                        false,
                        "Both expressions compute the exact same value - converting either the numerator or the denominator to a Double before dividing produces the same floating-point division result.",
                    ),
                    LessonChoice(
                        "Change currentArrival = arrival so it only happens inside the case where a new fleet actually starts, not unconditionally after every car.",
                        true,
                        "Updating currentArrival after every single car, even ones that just merged into an existing fleet, overwrites the fleet's true, slower arrival time with the merging car's own faster time - the running time should only change when a genuinely new, slower fleet begins.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "car-fleet-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of cars, what is the time complexity of the sort-then-scan approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because only a single scan through the cars is needed.",
                        false,
                        "The scan itself is linear, but the cars must first be sorted by position, and that sorting step costs more than a single linear pass.",
                    ),
                    LessonChoice(
                        "O(n log n), because sorting the cars by position costs O(n log n), and the single backward scan afterward is only O(n).",
                        true,
                        "Sorting dominates the total cost here - the scan that follows, computing arrival times and merging fleets, only takes linear time on top of that.",
                    ),
                    LessonChoice(
                        "O(n squared), because each car must be compared against every other car to determine merging.",
                        false,
                        "Each car is only ever compared against the single running 'current fleet arrival time', not against every other individual car - that comparison is constant-time per car.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "car-fleet-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the sort-then-scan approach use, not counting the space needed for sorting itself?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only a running fleet count and a running current arrival time are needed while scanning.",
                        true,
                        "Once the cars are sorted, the scan itself only needs two simple variables, regardless of how many cars there are.",
                    ),
                    LessonChoice(
                        "O(n), because the arrival time for every car must be stored in a separate array before comparing them.",
                        false,
                        "Arrival times are computed one at a time during the scan and only need to be compared to the single running current arrival time - there's no need to store every car's arrival time in a separate array.",
                    ),
                    LessonChoice(
                        "O(log n), because the sorted order requires that much extra space to maintain.",
                        false,
                        "Whether extra space is needed at all depends on which sorting algorithm is used, but the scan-and-merge logic that happens after sorting only ever needs a small, fixed number of variables.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture cars lined up on a single-lane road, all heading to the same finish line, where nobody's allowed to pass.\n\nWork out how long each car would take to reach the finish line if it were driving completely alone, at its own speed.\n\nThen, starting from whichever car is closest to the finish line and working backward, check each car in turn: if it would arrive at the finish line later than the car (or fleet) directly ahead of it, it forms its own new fleet. But if it would arrive at the same time or sooner, it must be catching up - it merges into the fleet ahead and effectively finishes at that fleet's slower pace instead.",
            walkthrough = listOf(
                "target = 12, position = [10,8,0,5,3], speed = [2,4,1,1,3]. Sort by position, closest to target first: car at 10 (speed 2), car at 8 (speed 4), car at 5 (speed 1), car at 3 (speed 3), car at 0 (speed 1).",
                "Car at 10: solo arrival = (12-10)/2 = 1.0. This is the first car, so it starts fleet 1. currentArrival = 1.0.",
                "Car at 8: solo arrival = (12-8)/4 = 1.0. Not greater than currentArrival (1.0), so it merges into fleet 1.",
                "Car at 5: solo arrival = (12-5)/1 = 7.0. Greater than currentArrival (1.0), so it starts fleet 2. currentArrival = 7.0.",
                "Car at 3: solo arrival = (12-3)/3 = 3.0. Not greater than currentArrival (7.0), so it merges into fleet 2.",
                "Car at 0: solo arrival = (12-0)/1 = 12.0. Greater than currentArrival (7.0), so it starts fleet 3. Total: 3 fleets.",
            ),
            pseudocode = "sort car indices by position, closest to target first\nfleets = 0, currentArrival = 0\nfor each car in that sorted order:\n    arrival = (target - car's position) / car's speed\n    if arrival > currentArrival:\n        fleets += 1\n        currentArrival = arrival\n    (otherwise, this car merges into the current fleet - no change)\nreturn fleets",
            referenceCode = "fun carFleet(target: Int, position: IntArray, speed: IntArray): Int {\n    val cars = position.indices.sortedByDescending { position[it] }\n    var fleets = 0\n    var currentArrival = 0.0\n    for (i in cars) {\n        val arrival = (target - position[i]).toDouble() / speed[i]\n        if (arrival > currentArrival) {\n            fleets++\n            currentArrival = arrival\n        }\n    }\n    return fleets\n}",
            timeComplexity = "O(n log n), where n is the number of cars.\n\nSorting the cars by their starting position costs O(n log n), which dominates the total.\n\nThe single backward scan that follows, computing each car's solo arrival time and deciding whether it merges, only adds O(n) on top of that.",
            spaceComplexity = "O(1) extra space for the scan itself, not counting whatever space the sorting step requires internally.\n\nWhile scanning, only two variables are needed: a running count of fleets found so far, and the current fleet's arrival time to compare against.",
            alternatives = listOf(
                "Simulate the position of every car at every small unit of time until all cars reach the destination, checking which ones end up adjacent.\nThis can work if the time step is fine enough.\nBut it's far more computation than necessary, and choosing an appropriately small time step to avoid inaccuracy adds its own complications.",
                "Instead of comparing solo arrival times, directly compute the time each car would take to catch up to the car immediately ahead of it and compare speeds pairwise.\nThis focuses on relative rather than absolute arrival times.\nBut it requires carefully chaining these pairwise comparisons together correctly, which is more error-prone than the single running 'current fleet arrival time' approach.",
            ),
            commonMistakes = listOf(
                "Sorting cars by their position from the destination outward, closest first, but then scanning in the wrong direction, or sorting in the wrong order to begin with.\nA car can only ever be blocked by a car ahead of it, closer to the destination, so cars must be processed starting from the one closest to the destination and moving backward.",
                "Updating the running current arrival time after every car, even ones that just merged into an existing fleet.\nThat overwrites the fleet's true, slower arrival time with the faster, merging car's own time, corrupting later comparisons.\nThe running time should only change when a car actually starts a brand-new fleet.",
                "Comparing arrival times with the wrong direction, treating a car that arrives sooner as starting a new fleet instead of merging.\nA car that would arrive at the same time or sooner than the fleet ahead is exactly the one that catches up and merges - it's a car arriving later on its own that's stuck behind and forms a new fleet.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: which items, arranged in a line and moving at different rates, end up grouped together because faster ones catch up to slower ones ahead?",
                "Sorting by position and then scanning from one end reveals which items merge, without needing to simulate every moment of movement.",
                "An item's fate depends only on the item, or group, immediately ahead of it, not on every other item - a hint that a single running comparison value, updated as you scan, is enough.",
            ),
            takeaway = "When faster-moving items catch up to slower ones ahead and merge, sort by position and scan from the front, closest to the destination, backward, comparing each item's solo completion time to a running value for the group ahead - it identifies every merge in a single pass after sorting.",
        ),
        nextSlug = "largest-rectangle-in-histogram",
    )

    private val largestRectangleInHistogramLesson = RoadmapLesson(
        slug = "largest-rectangle-in-histogram",
        title = "Largest Rectangle in Histogram",
        difficulty = CurriculumDifficulty.HARD,
        description = "Given an array of bar heights forming a histogram, where each bar has width 1, find the area of the largest rectangle that can be formed using one or more contiguous bars.",
        constraints = listOf(
            "The array has at least one height.",
            "Heights are non-negative integers.",
            "The rectangle's height is limited by the shortest bar it spans, and its width is how many contiguous bars it covers.",
            "Aim for linear time rather than checking every possible contiguous range of bars.",
        ),
        examples = listOf(
            LessonExample("[2,1,5,6,2,3]", "10", "The rectangle spanning bars of height 5 and 6 (width 2, height min(5,6)=5) gives area 10 - the maximum possible."),
            LessonExample("[2,4]", "4", "A single bar of height 4 gives area 4, tying with using both bars together (min(2,4)*2=4), and no larger option exists."),
            LessonExample("[1,1,1,1]", "4", "Using all four bars of height 1 gives area min(1,1,1,1) * 4 = 4."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "histogram-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the largest rectangle without checking every possible contiguous range of bars?",
                choices = listOf(
                    LessonChoice(
                        "For every pair of a starting and ending bar, compute the area of the rectangle spanning them and keep the largest.",
                        false,
                        "This finds the correct answer, but there are O(n squared) such pairs, and computing the minimum height for each one can add even more work on top.",
                    ),
                    LessonChoice(
                        "Use a stack of bar indices with increasing heights; whenever a shorter bar is encountered, pop taller bars off the stack and compute the largest rectangle each one could have formed, using the current position and the new stack top as its boundaries.",
                        true,
                        "Each bar, once popped, has its largest possible rectangle computed using exactly the boundaries where it stopped being the shortest - the stack lets every bar be resolved this way with each index pushed and popped only once.",
                    ),
                    LessonChoice(
                        "Sort the bars by height, then greedily combine the tallest bars first.",
                        false,
                        "Sorting destroys the original left-to-right order of the bars, but a rectangle must span contiguous bars in their original positions - a sorted rearrangement can't represent that.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "histogram-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three use a stack of indices with increasing heights. Which one computes each popped bar's rectangle correctly?",
                choices = listOf(
                    LessonChoice(
                        text = "When popping a bar, computes its width as the current index minus the new stack top's index minus one, or the current index alone if the stack becomes empty, multiplied by the popped bar's own height.",
                        correct = true,
                        feedback = "The popped bar's rectangle extends from just after the new stack top, its nearest shorter bar to the left, up to but not including the current index, its nearest shorter bar to the right - that span, times its own height, is its largest possible rectangle.",
                        code = "fun largestRectangleArea(heights: IntArray): Int {\n    val stack = ArrayDeque<Int>()\n    var best = 0\n    for (i in heights.indices) {\n        while (stack.isNotEmpty() && heights[i] < heights[stack.last()]) {\n            val height = heights[stack.removeLast()]\n            val width = if (stack.isEmpty()) i else i - stack.last() - 1\n            best = maxOf(best, height * width)\n        }\n        stack.addLast(i)\n    }\n    while (stack.isNotEmpty()) {\n        val height = heights[stack.removeLast()]\n        val width = if (stack.isEmpty()) heights.size else heights.size - stack.last() - 1\n        best = maxOf(best, height * width)\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "When popping a bar, always uses the current index alone as the width, regardless of what's left on the stack.",
                        correct = false,
                        feedback = "Ignoring the new stack top entirely overstates the width whenever there's still a taller bar remaining on the stack to the left - the popped bar's rectangle can't extend past that remaining, shorter boundary.",
                        code = "fun largestRectangleArea(heights: IntArray): Int {\n    val stack = ArrayDeque<Int>()\n    var best = 0\n    for (i in heights.indices) {\n        while (stack.isNotEmpty() && heights[i] < heights[stack.last()]) {\n            val height = heights[stack.removeLast()]\n            val width = i\n            best = maxOf(best, height * width)\n        }\n        stack.addLast(i)\n    }\n    while (stack.isNotEmpty()) {\n        val height = heights[stack.removeLast()]\n        val width = heights.size\n        best = maxOf(best, height * width)\n    }\n    return best\n}",
                    ),
                    LessonChoice(
                        text = "Uses the popped bar's index, rather than the height stored at that index, when computing the rectangle's height.",
                        correct = false,
                        feedback = "The rectangle's height must be the bar's actual height value, not its position in the array - using the index as if it were the height computes a completely unrelated number.",
                        code = "fun largestRectangleArea(heights: IntArray): Int {\n    val stack = ArrayDeque<Int>()\n    var best = 0\n    for (i in heights.indices) {\n        while (stack.isNotEmpty() && heights[i] < heights[stack.last()]) {\n            val poppedIndex = stack.removeLast()\n            val width = if (stack.isEmpty()) i else i - stack.last() - 1\n            best = maxOf(best, poppedIndex * width)\n        }\n        stack.addLast(i)\n    }\n    while (stack.isNotEmpty()) {\n        val poppedIndex = stack.removeLast()\n        val width = if (stack.isEmpty()) heights.size else heights.size - stack.last() - 1\n        best = maxOf(best, poppedIndex * width)\n    }\n    return best\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "histogram-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version misses the largest rectangle for histograms that are increasing all the way to the end, like [1,2,3,4,5]. What's the bug?",
                code = "val stack = ArrayDeque<Int>()\nvar best = 0\nfor (i in heights.indices) {\n    while (stack.isNotEmpty() && heights[i] < heights[stack.last()]) {\n        val height = heights[stack.removeLast()]\n        val width = if (stack.isEmpty()) i else i - stack.last() - 1\n        best = maxOf(best, height * width)\n    }\n    stack.addLast(i)\n}",
                choices = listOf(
                    LessonChoice(
                        "Change heights[i] < heights[stack.last()] to heights[i] <= heights[stack.last()].",
                        false,
                        "This changes how equal-height bars are handled during the main scan, but doesn't address bars that are still sitting unresolved on the stack once the scan finishes.",
                    ),
                    LessonChoice(
                        "After the main loop finishes, add a second loop that keeps popping and resolving whatever's left on the stack, using heights.size in place of i as the right boundary.",
                        true,
                        "A histogram that's increasing all the way to the end never triggers a pop during the main scan, leaving every bar still on the stack - a final cleanup pass, treating the end of the array as the right boundary for each remaining bar, is needed to resolve them.",
                    ),
                    LessonChoice(
                        "Change stack.addLast(i) to only happen when the stack is empty.",
                        false,
                        "The stack needs to track every bar that hasn't yet found a shorter bar to its right, not just the very first one - restricting pushes to only when the stack is empty would prevent the algorithm from tracking increasing sequences of bars at all.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "histogram-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of bars, what is the time complexity of the stack-based approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n squared), because each bar might need to pop many other bars off the stack.",
                        false,
                        "Even though a single bar can trigger multiple pops at once, each bar is only ever pushed onto the stack once and popped from it at most once across the entire algorithm - the total number of stack operations is bounded by n.",
                    ),
                    LessonChoice(
                        "O(n log n), because the stack must remain sorted by height at all times.",
                        false,
                        "The stack is maintained using simple pushes and pops based on direct height comparisons, not a sorted-insertion structure - each operation is a plain constant-time stack operation.",
                    ),
                    LessonChoice(
                        "O(n), because each bar is pushed onto the stack exactly once and popped at most once, including during the final cleanup pass.",
                        true,
                        "Combining the main scan and the cleanup pass, every bar index enters and leaves the stack exactly once each, so the total work across both stays proportional to n.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "histogram-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does the stack use in the worst case?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because a histogram with strictly increasing heights would push every bar onto the stack before any of them get popped.",
                        true,
                        "If heights only ever increase, no bar is ever shorter than the one before it, so nothing triggers a pop during the main scan - every single bar ends up on the stack at once.",
                    ),
                    LessonChoice(
                        "O(1), because bars are popped as soon as a shorter one appears, keeping the stack small.",
                        false,
                        "Bars are only popped when a strictly shorter bar appears later - a histogram that keeps increasing never triggers this, so the stack can grow to hold every bar.",
                    ),
                    LessonChoice(
                        "O(log n), because the stack only grows for a small portion of typical histograms.",
                        false,
                        "There's no logarithmic bound here - in the worst case, strictly increasing heights, the stack's size grows linearly with the number of bars, all the way up to n.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture each bar in the histogram asking: 'How wide could a rectangle at my height stretch, using me as its limiting, shortest, bar?'\n\nA bar's rectangle can extend left until it hits a bar shorter than itself, and right until it hits a bar shorter than itself - anywhere in between, this bar is tall enough to support a rectangle of its own height.\n\nA stack of increasing heights tracks exactly this: when a shorter bar finally appears, every taller bar sitting on the stack has just found its right boundary, and whatever's left below it on the stack is its left boundary, so its rectangle's width and area can be computed right then.",
            walkthrough = listOf(
                "Heights [2,1,5,6,2,3]. i=0 (height 2): stack empty, push. stack=[0].",
                "i=1 (height 1): 1 < heights[0]=2, so pop index 0 (height 2). Stack is now empty, so width = i = 1. Area = 2*1 = 2. Push index 1. stack=[1].",
                "i=2 (height 5): not less than heights[1]=1, so just push. stack=[1,2]. i=3 (height 6): not less than heights[2]=5, push. stack=[1,2,3].",
                "i=4 (height 2): 2 < heights[3]=6, pop index 3 (height 6), width = 4-2-1 = 1, area = 6. Still 2 < heights[2]=5, pop index 2 (height 5), width = 4-1-1 = 2, area = 10 - new best! Push index 4. stack=[1,4].",
                "Continue to the end and run the final cleanup pass on whatever remains on the stack.",
                "The largest area found throughout is 10.",
            ),
            pseudocode = "stack = empty (holds indices, heights increasing bottom to top)\nbest = 0\nfor i from 0 to n - 1:\n    while stack not empty and heights[i] < heights[stack's top]:\n        h = heights[pop stack]\n        w = i if stack is empty else i - stack's top - 1\n        best = max(best, h * w)\n    push i\nwhile stack not empty:  // cleanup pass for whatever remains\n    h = heights[pop stack]\n    w = n if stack is empty else n - stack's top - 1\n    best = max(best, h * w)\nreturn best",
            referenceCode = "fun largestRectangleArea(heights: IntArray): Int {\n    val stack = ArrayDeque<Int>()\n    var best = 0\n    for (i in heights.indices) {\n        while (stack.isNotEmpty() && heights[i] < heights[stack.last()]) {\n            val height = heights[stack.removeLast()]\n            val width = if (stack.isEmpty()) i else i - stack.last() - 1\n            best = maxOf(best, height * width)\n        }\n        stack.addLast(i)\n    }\n    while (stack.isNotEmpty()) {\n        val height = heights[stack.removeLast()]\n        val width = if (stack.isEmpty()) heights.size else heights.size - stack.last() - 1\n        best = maxOf(best, height * width)\n    }\n    return best\n}",
            timeComplexity = "O(n), where n is the number of bars.\n\nEvery bar is pushed onto the stack exactly once, whether during the main scan or, for bars that never get popped along the way, resolved during the final cleanup pass.\n\nCombined, every bar enters and leaves the stack exactly once each, keeping the total work proportional to n.",
            spaceComplexity = "O(n), because a histogram with strictly increasing heights never triggers a pop during the main scan, leaving every single bar sitting on the stack at once, right up until the final cleanup pass resolves them.",
            alternatives = listOf(
                "For every pair of a starting and ending bar, compute the minimum height across that range and use it to find the rectangle's area, keeping the largest.\nThis is the most direct translation of the problem statement.\nBut with O(n squared) pairs, and needing to find the minimum height across each range, this is far slower than the stack-based approach.",
                "Use a divide-and-conquer approach, finding the shortest bar in a range, computing the rectangle that spans the entire range at that height, and recursively checking the ranges to its left and right.\nThis also finds the correct answer.\nBut it costs O(n log n) on average and O(n squared) in the worst case, without the guaranteed linear time the stack approach provides.",
            ),
            commonMistakes = listOf(
                "Forgetting the final cleanup pass after the main scan finishes.\nA histogram with heights that keep increasing all the way to the end never triggers a pop during the scan, leaving every bar still sitting unresolved on the stack - without a cleanup pass, their rectangles are never computed.",
                "Computing the popped bar's width using only the current index, without accounting for whatever's left on the stack as the left boundary.\nThat overstates the width whenever a taller bar remains on the stack, since the popped bar's rectangle can't actually extend past that remaining shorter boundary.",
                "Using a bar's index instead of its actual height value when computing area.\nThe rectangle's height must come from heights at the popped index, not from the index number itself - mixing these up produces numbers that have nothing to do with the actual histogram.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: for each item, how far can it extend in both directions before hitting something that limits it?",
                "A brute-force answer would check every possible range, but each item's own limiting boundaries, nearest smaller neighbor on each side, can be found more directly with a stack.",
                "Once a shorter item appears, every taller item waiting before it has just had its right boundary determined - a hallmark of a monotonic stack problem.",
            ),
            takeaway = "When each item's answer depends on the nearest smaller, or larger, neighbor on both sides, maintain a stack of increasing, or decreasing, values - popping an item when its boundary is finally found lets you compute its answer using exactly where it stopped being the limiting value, in a single linear pass plus a cleanup pass for whatever remains.",
        ),
        nextSlug = "binary-search",
    )

    private val binarySearchLesson = RoadmapLesson(
        slug = "binary-search",
        title = "Binary Search",
        difficulty = CurriculumDifficulty.EASY,
        description = "Given a sorted array of distinct integers and a target value, return the index of the target if it exists in the array, or -1 if it does not.",
        constraints = listOf(
            "The array is sorted in ascending order and contains no duplicates.",
            "The array may contain up to 10,000 values.",
            "Aim for logarithmic time rather than scanning every element.",
        ),
        examples = listOf(
            LessonExample("nums = [-1,0,3,5,9,12], target = 9", "4", "9 appears at index 4."),
            LessonExample("nums = [-1,0,3,5,9,12], target = 2", "-1", "2 is not present in the array."),
            LessonExample("nums = [5], target = 5", "0", "A single-element array where the target is the only value."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "binary-search-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the target in logarithmic time by taking advantage of the array being sorted?",
                choices = listOf(
                    LessonChoice(
                        "Scan the array from left to right, comparing each value to the target until a match is found or the array ends.",
                        false,
                        "This works on any array, sorted or not, but it doesn't take advantage of the sorted order at all, so it still costs linear time in the worst case.",
                    ),
                    LessonChoice(
                        "Repeatedly check the middle element of the remaining range; if it's too small, discard the left half, and if it's too large, discard the right half.",
                        true,
                        "Because the array is sorted, comparing the middle value to the target tells you which entire half can be safely thrown away, cutting the remaining range roughly in half every step.",
                    ),
                    LessonChoice(
                        "Split the array into fixed-size blocks and scan each block's first element to decide which block might contain the target.",
                        false,
                        "Scanning block boundaries still leaves a linear number of blocks to check in the worst case, rather than repeatedly halving the search space the way a true binary search does.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "binary-search-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three maintain low and high pointers and compute a middle index. Which one correctly narrows the range on every comparison?",
                choices = listOf(
                    LessonChoice(
                        text = "Computes mid, and when nums[mid] is too small, sets low = mid + 1; when too large, sets high = mid - 1; otherwise returns mid.",
                        correct = true,
                        feedback = "Moving low or high strictly past the just-checked middle index guarantees mid itself is excluded from the next iteration, so the range keeps shrinking and the loop is guaranteed to terminate.",
                        code = "fun search(nums: IntArray, target: Int): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        when {\n            nums[mid] < target -> low = mid + 1\n            nums[mid] > target -> high = mid - 1\n            else -> return mid\n        }\n    }\n    return -1\n}",
                    ),
                    LessonChoice(
                        text = "Computes mid, and when nums[mid] is too small, sets low = mid; when too large, sets high = mid; otherwise returns mid.",
                        correct = false,
                        feedback = "Leaving mid itself inside the next range means low or high may never actually move when the range has shrunk to two elements, causing the loop to spin forever without terminating.",
                        code = "fun search(nums: IntArray, target: Int): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        when {\n            nums[mid] < target -> low = mid\n            nums[mid] > target -> high = mid\n            else -> return mid\n        }\n    }\n    return -1\n}",
                    ),
                    LessonChoice(
                        text = "Computes mid, and when nums[mid] is too small, sets high = mid - 1; when too large, sets low = mid + 1; otherwise returns mid.",
                        correct = false,
                        feedback = "This swaps which direction each comparison moves the pointers, so the search space shrinks toward the wrong end of the array and never actually converges on the target.",
                        code = "fun search(nums: IntArray, target: Int): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        when {\n            nums[mid] < target -> high = mid - 1\n            nums[mid] > target -> low = mid + 1\n            else -> return mid\n        }\n    }\n    return -1\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "binary-search-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version can throw an overflow error or infinite loop on very large arrays. What's the bug?",
                code = "var low = 0\nvar high = nums.size - 1\nwhile (low <= high) {\n    val mid = (low + high) / 2\n    when {\n        nums[mid] < target -> low = mid + 1\n        nums[mid] > target -> high = mid - 1\n        else -> return mid\n    }\n}\nreturn -1",
                choices = listOf(
                    LessonChoice(
                        "Change while (low <= high) to while (low < high).",
                        false,
                        "Using a strict less-than comparison would stop the loop one iteration too early, skipping the case where low and high point to the same final candidate index.",
                    ),
                    LessonChoice(
                        "Change val mid = (low + high) / 2 to val mid = low + (high - low) / 2.",
                        true,
                        "Adding low and high directly can overflow an integer when both are large, even though their true midpoint fits fine - computing the offset from low first avoids that overflow entirely.",
                    ),
                    LessonChoice(
                        "Change nums[mid] < target to nums[mid] <= target.",
                        false,
                        "That changes which half gets discarded on a match, potentially skipping past the target entirely instead of returning its index, and doesn't relate to the overflow risk described.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "binary-search-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of elements, what is the time complexity of binary search?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because in the worst case every element might need to be checked.",
                        false,
                        "Binary search never checks every element - each comparison discards roughly half of the remaining range, so far fewer than n comparisons are ever needed.",
                    ),
                    LessonChoice(
                        "O(n log n), because the array must be sorted before searching begins.",
                        false,
                        "The array is already given sorted, so no sorting work happens inside the search itself - only the search's own halving behavior counts toward its complexity.",
                    ),
                    LessonChoice(
                        "O(log n), because each comparison discards half of the remaining search range.",
                        true,
                        "Cutting the remaining range in half on every comparison means the range shrinks to size 1 after roughly log base 2 of n steps.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "binary-search-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does this iterative binary search use?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only a fixed handful of variables, low, high, and mid, are tracked regardless of array size.",
                        true,
                        "The iterative version updates the same few integer variables on every loop iteration rather than allocating new space proportional to the array or the range being searched.",
                    ),
                    LessonChoice(
                        "O(log n), because the search range is halved on every step.",
                        false,
                        "Halving the range affects how many iterations the loop runs, not how much memory each iteration uses - the loop itself allocates nothing extra as it progresses.",
                    ),
                    LessonChoice(
                        "O(n), because the array itself must be considered part of the space used.",
                        false,
                        "The input array is typically not counted as extra space since it already exists before the function runs - what matters is space the algorithm itself allocates beyond the input.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture looking up a word in a paper dictionary.\n\nYou don't start at the first page and read every entry - you open to the middle, see whether your word comes before or after that page, and throw away the half you now know it can't be in.\n\nRepeating that on the remaining half, and the half after that, narrows things down astonishingly fast, because the amount left to search keeps cutting in half.",
            walkthrough = listOf(
                "nums = [-1,0,3,5,9,12], target = 9. low = 0, high = 5.",
                "mid = 2, nums[2] = 3. 3 < 9, so the target must be to the right: low = 3.",
                "mid = 4, nums[4] = 9. Match found.",
                "Return 4.",
            ),
            pseudocode = "low = 0, high = length - 1\nwhile low <= high:\n    mid = low + (high - low) / 2\n    if nums[mid] < target: low = mid + 1\n    else if nums[mid] > target: high = mid - 1\n    else: return mid\nreturn -1",
            referenceCode = "fun search(nums: IntArray, target: Int): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        when {\n            nums[mid] < target -> low = mid + 1\n            nums[mid] > target -> high = mid - 1\n            else -> return mid\n        }\n    }\n    return -1\n}",
            timeComplexity = "O(log n), where n is the number of elements.\n\nEach comparison discards roughly half of whatever range remains, so the range shrinks to a single element after about log base 2 of n steps.",
            spaceComplexity = "O(1), because only a small, fixed number of variables, low, high, and mid, are tracked, regardless of how large the array is.",
            alternatives = listOf(
                "Scan the array from the start, checking every element in order until the target is found or the array ends.\nThis works without requiring the array to be sorted at all.\nBut it costs O(n) time in the worst case, far slower than binary search's O(log n) once the array is sorted.",
                "Use recursion instead of a loop, calling the function again on whichever half remains after each comparison.\nThis can read a little more naturally as 'search this half.'\nBut it uses O(log n) call-stack space, where the iterative version uses none beyond a few variables.",
            ),
            commonMistakes = listOf(
                "Computing mid as (low + high) / 2 instead of low + (high - low) / 2.\nAdding low and high directly can overflow in languages with fixed-width integers when both are large, even though the true midpoint would fit comfortably.",
                "Updating low or high to mid instead of mid + 1 or mid - 1.\nLeaving the just-checked index inside the next range means the loop can stop making progress and never terminate once the range narrows to two elements.",
                "Using while (low < high) instead of while (low <= high).\nA strict less-than comparison can exit the loop one iteration early, before the single remaining candidate index has actually been checked.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: how do I find something quickly in data that's already sorted?",
                "A brute-force scan checks every element, but sorted order means comparing against just the middle element rules out an entire half at once.",
                "Whenever 'sorted' appears alongside 'find' or 'search,' binary search is usually worth considering before a linear scan.",
            ),
            takeaway = "When searching sorted data, compare against the middle of the remaining range and discard the half that can't contain the answer - repeating this halves the search space every step, giving logarithmic time instead of linear.",
        ),
        nextSlug = "search-a-2d-matrix",
    )

    private val searchA2DMatrixLesson = RoadmapLesson(
        slug = "search-a-2d-matrix",
        title = "Search a 2D Matrix",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Given an m x n matrix where each row is sorted left to right and the first value of each row is greater than the last value of the previous row, determine whether a given target value exists anywhere in the matrix.",
        constraints = listOf(
            "Every row is individually sorted in ascending order.",
            "The first element of each row is greater than the last element of the row before it, so the whole matrix reads like one long sorted list.",
            "Aim for logarithmic time rather than scanning every row or every cell.",
        ),
        examples = listOf(
            LessonExample("matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 3", "true", "3 sits in the first row."),
            LessonExample("matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 13", "false", "13 falls in a gap that no cell actually holds."),
            LessonExample("matrix = [[1]], target = 1", "true", "A single-cell matrix where the only value is the target."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "matrix-search-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach searches the matrix in logarithmic time by treating it as one long sorted sequence?",
                choices = listOf(
                    LessonChoice(
                        "First binary search the first column to find which row could contain the target, then binary search across that row separately.",
                        false,
                        "This can work, but it performs two separate binary searches rather than treating the whole matrix as one sorted sequence, which is a bit more bookkeeping than necessary.",
                    ),
                    LessonChoice(
                        "Treat the matrix as a single sorted array of length rows times columns, and binary search it directly by converting each middle index into a row and column.",
                        true,
                        "Because each row continues exactly where the previous one left off, the entire matrix behaves like one flat sorted array - a single binary search over that virtual array finds the target directly.",
                    ),
                    LessonChoice(
                        "Scan every row from top to bottom, and within each row, scan left to right until the target is found or ruled out.",
                        false,
                        "This checks every cell in the worst case, which is linear in the total number of cells and ignores the sorted structure that lets far fewer cells be examined.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "matrix-search-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three binary search over a flattened index space of size rows * cols. Which one correctly converts the flat index back into a row and column?",
                choices = listOf(
                    LessonChoice(
                        text = "Computes row = mid / cols and col = mid % cols.",
                        correct = true,
                        feedback = "Dividing by the number of columns gives how many full rows are skipped to reach this flat index, and the remainder gives the position within that row - exactly matching how the rows were laid end to end.",
                        code = "fun searchMatrix(matrix: Array<IntArray>, target: Int): Boolean {\n    val rows = matrix.size\n    val cols = matrix[0].size\n    var low = 0\n    var high = rows * cols - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        val value = matrix[mid / cols][mid % cols]\n        when {\n            value < target -> low = mid + 1\n            value > target -> high = mid - 1\n            else -> return true\n        }\n    }\n    return false\n}",
                    ),
                    LessonChoice(
                        text = "Computes row = mid % rows and col = mid / rows.",
                        correct = false,
                        feedback = "Dividing and taking the remainder by the number of rows, instead of the number of columns, scrambles which cell a flat index actually maps to, since rows can hold a different count of cells than the row count itself.",
                        code = "fun searchMatrix(matrix: Array<IntArray>, target: Int): Boolean {\n    val rows = matrix.size\n    val cols = matrix[0].size\n    var low = 0\n    var high = rows * cols - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        val value = matrix[mid % rows][mid / rows]\n        when {\n            value < target -> low = mid + 1\n            value > target -> high = mid - 1\n            else -> return true\n        }\n    }\n    return false\n}",
                    ),
                    LessonChoice(
                        text = "Computes row = mid / cols and col = mid / cols as well, reusing the same value for both.",
                        correct = false,
                        feedback = "Using the same computed value for both the row and column ignores the position within the row entirely, so most cells are never actually reachable by any mid value.",
                        code = "fun searchMatrix(matrix: Array<IntArray>, target: Int): Boolean {\n    val rows = matrix.size\n    val cols = matrix[0].size\n    var low = 0\n    var high = rows * cols - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        val r = mid / cols\n        val value = matrix[r][r]\n        when {\n            value < target -> low = mid + 1\n            value > target -> high = mid - 1\n            else -> return true\n        }\n    }\n    return false\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "matrix-search-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version throws an index-out-of-bounds error on some matrices. What's the bug?",
                code = "val rows = matrix.size\nval cols = matrix[0].size\nvar low = 0\nvar high = rows * cols\nwhile (low <= high) {\n    val mid = low + (high - low) / 2\n    val value = matrix[mid / cols][mid % cols]\n    when {\n        value < target -> low = mid + 1\n        value > target -> high = mid - 1\n        else -> return true\n    }\n}\nreturn false",
                choices = listOf(
                    LessonChoice(
                        "Change var high = rows * cols to var high = rows * cols - 1.",
                        true,
                        "Valid flat indices only run from 0 up to rows * cols - 1, so starting high at rows * cols allows mid to land one past the last real cell, which is out of bounds.",
                    ),
                    LessonChoice(
                        "Change while (low <= high) to while (low < high).",
                        false,
                        "That would stop the loop one comparison too early rather than fixing the out-of-bounds index, since the boundary problem is with the starting value of high, not the loop condition.",
                    ),
                    LessonChoice(
                        "Change val mid = low + (high - low) / 2 to val mid = (low + high) / 2.",
                        false,
                        "Both expressions compute the same midpoint for reasonably sized matrices - this doesn't address why mid can point past the last valid cell.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "matrix-search-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With m rows and n columns, what is the time complexity of the flattened binary search?",
                choices = listOf(
                    LessonChoice(
                        "O(m + n), because the search touches at most one row and one column separately.",
                        false,
                        "The flattened approach doesn't search rows and columns as separate steps - it performs a single binary search over all m * n cells treated as one sequence.",
                    ),
                    LessonChoice(
                        "O(m * n), because in the worst case every cell might need to be visited.",
                        false,
                        "Binary search never visits every cell - each comparison eliminates roughly half of the remaining flattened range, the same way it would for any sorted array.",
                    ),
                    LessonChoice(
                        "O(log(m * n)), because binary search over the flattened space of m * n cells halves the range on every comparison.",
                        true,
                        "Treating the matrix as one sorted array of m * n elements and binary searching it takes a number of steps proportional to the log of its total size.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "matrix-search-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does this approach use beyond the input matrix?",
                choices = listOf(
                    LessonChoice(
                        "O(m + n), because a temporary row and column must be built to search through.",
                        false,
                        "No temporary row or column is ever built - the row and column for a given flat index are computed directly with division and remainder, using no extra storage.",
                    ),
                    LessonChoice(
                        "O(1), because only a fixed handful of variables, low, high, and mid, are tracked regardless of the matrix's size.",
                        true,
                        "The matrix itself is read in place, and converting a flat index to a row and column is pure arithmetic - nothing proportional to the matrix's size is ever allocated.",
                    ),
                    LessonChoice(
                        "O(log(m * n)), matching the number of comparisons the search performs.",
                        false,
                        "The number of comparisons the loop performs affects how long it runs, not how much memory it uses at any one time - each comparison reuses the same few variables.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture unrolling the matrix row by row into one long line of numbers, left to right, top to bottom.\n\nBecause each row picks up exactly where the previous one left off, that unrolled line is fully sorted from start to finish.\n\nRather than physically building that line, a middle index into the imagined line can be converted back into a row and column with simple division and remainder - so an ordinary binary search still works, just with one extra translation step per comparison.",
            walkthrough = listOf(
                "matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 16. Flattened length = 3 * 4 = 12. low = 0, high = 11.",
                "mid = 5. row = 5 / 4 = 1, col = 5 % 4 = 1. matrix[1][1] = 11. 11 < 16, so low = 6.",
                "mid = 8. row = 8 / 4 = 2, col = 8 % 4 = 0. matrix[2][0] = 23. 23 > 16, so high = 7.",
                "mid = 6. row = 6 / 4 = 1, col = 6 % 4 = 2. matrix[1][2] = 16. Match found.",
                "Return true.",
            ),
            pseudocode = "rows = matrix.size, cols = matrix[0].size\nlow = 0, high = rows * cols - 1\nwhile low <= high:\n    mid = low + (high - low) / 2\n    value = matrix[mid / cols][mid % cols]\n    if value < target: low = mid + 1\n    else if value > target: high = mid - 1\n    else: return true\nreturn false",
            referenceCode = "fun searchMatrix(matrix: Array<IntArray>, target: Int): Boolean {\n    val rows = matrix.size\n    val cols = matrix[0].size\n    var low = 0\n    var high = rows * cols - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        val value = matrix[mid / cols][mid % cols]\n        when {\n            value < target -> low = mid + 1\n            value > target -> high = mid - 1\n            else -> return true\n        }\n    }\n    return false\n}",
            timeComplexity = "O(log(m * n)), where m is the number of rows and n is the number of columns.\n\nTreating the matrix as one sorted sequence of m * n cells and binary searching it takes a number of comparisons proportional to the log of its total size.",
            spaceComplexity = "O(1), because only a fixed handful of variables, low, high, and mid, are tracked - converting a flat index into a row and column uses plain arithmetic, not extra storage.",
            alternatives = listOf(
                "Binary search the first column to find the one row that could contain the target, then binary search across that row.\nThis also reaches O(log m + log n) time.\nBut it requires two separate binary searches and a bit more bookkeeping than treating the matrix as a single flattened sequence.",
                "Start at the top-right corner and step left when the current value is too large, or down when it's too small.\nThis reaches O(m + n) time without any index math.\nBut it's slower than binary search's O(log(m * n)) for large matrices, since it can visit an entire row or column's worth of steps in the worst case.",
            ),
            commonMistakes = listOf(
                "Setting high to rows * cols instead of rows * cols - 1.\nValid flat indices only go up to rows * cols - 1, so starting one too high lets mid compute a row and column that fall outside the matrix.",
                "Swapping the division and remainder, computing row = mid % cols and col = mid / cols instead of the other way around.\nThat scrambles which cell each flat index actually points to, since the row and column play very different roles in the layout.",
                "Assuming every row and every column individually is sorted the same way a normal grid would be, and reaching for a two-dimensional search when the whole matrix is actually laid out as one continuous sorted sequence.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: how do I search a two-dimensional structure that's secretly just a one-dimensional sorted sequence in disguise?",
                "Rows that continue exactly where the previous row left off is a strong signal the matrix can be treated as one flattened sorted array.",
                "Whenever 'sorted' shows up for a structure with more than one dimension, it's worth checking whether it collapses into a single sorted sequence before reaching for a more complex search.",
            ),
            takeaway = "When a two-dimensional structure is sorted such that each row continues exactly where the last one ended, treat it as one flattened sorted array and binary search it directly, converting the middle flat index into a row and column with division and remainder.",
        ),
        nextSlug = "koko-eating-bananas",
    )

    private val kokoEatingBananasLesson = RoadmapLesson(
        slug = "koko-eating-bananas",
        title = "Koko Eating Bananas",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Koko has piles of bananas and h hours before the guards return. Each hour she picks one pile and eats up to k bananas from it (or the whole pile if it has fewer than k); if she finishes a pile early that hour, she doesn't start another. Find the minimum integer eating speed k that lets her finish every pile within h hours.",
        constraints = listOf(
            "There is at least one pile, and h is at least as large as the number of piles.",
            "Each pile has at least one banana.",
            "The eating speed k must be a positive integer.",
            "Aim to avoid trying every possible speed one at a time from 1 upward.",
        ),
        examples = listOf(
            LessonExample("piles = [3,6,7,11], h = 8", "4", "At speed 4, the piles take 1 + 2 + 2 + 3 = 8 hours, exactly fitting; speed 3 would take longer than 8 hours."),
            LessonExample("piles = [30,11,23,4,20], h = 5", "30", "With only enough hours for one pile each, Koko must eat fast enough to clear the largest pile, 30, in a single hour."),
            LessonExample("piles = [30,11,23,4,20], h = 6", "23", "One extra hour lets the speed drop to 23, still just barely finishing every pile in time."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "koko-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the minimum working speed without individually testing every speed starting from 1?",
                choices = listOf(
                    LessonChoice(
                        "Start at speed 1 and increase it by 1 each time, checking after every increase whether that speed finishes within h hours, stopping at the first speed that works.",
                        false,
                        "This eventually finds the right answer, but checking every single speed one at a time from 1 upward can take as many steps as the largest pile size, which is far more work than necessary.",
                    ),
                    LessonChoice(
                        "Binary search over possible speeds from 1 to the largest pile size, checking at each candidate speed whether all piles finish within h hours, and narrowing toward the smallest speed that works.",
                        true,
                        "As speed increases, the hours needed to finish never increases - that consistent, one-directional relationship between speed and total hours is exactly what makes binary searching over candidate speeds valid.",
                    ),
                    LessonChoice(
                        "Sort the piles from largest to smallest and eat the largest pile first every hour, adjusting speed only when a pile is fully finished.",
                        false,
                        "The order in which piles are eaten doesn't change how many hours a given fixed speed takes overall, since each pile's hours depend only on its own size and the chosen speed, not on eating order.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "koko-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three binary search over candidate speeds and check feasibility with a helper. Which one correctly computes the hours needed for a given speed?",
                choices = listOf(
                    LessonChoice(
                        text = "Sums, for every pile, the ceiling of pile size divided by speed, computed as (pile + speed - 1) / speed using integer division.",
                        correct = true,
                        feedback = "A pile that doesn't divide evenly by the speed still needs one more hour to finish the remainder, and adding speed - 1 before integer division correctly rounds that division up instead of down.",
                        code = "fun minEatingSpeed(piles: IntArray, h: Int): Int {\n    fun hoursNeeded(speed: Int): Long {\n        var hours = 0L\n        for (pile in piles) hours += (pile + speed - 1) / speed\n        return hours\n    }\n    var low = 1\n    var high = piles.max()\n    while (low < high) {\n        val mid = low + (high - low) / 2\n        if (hoursNeeded(mid) <= h) high = mid else low = mid + 1\n    }\n    return low\n}",
                    ),
                    LessonChoice(
                        text = "Sums, for every pile, pile size divided by speed using plain integer division, without any rounding adjustment.",
                        correct = false,
                        feedback = "Plain integer division rounds down, silently ignoring the extra hour needed to finish a partial pile - that undercounts the true number of hours a speed actually requires.",
                        code = "fun minEatingSpeed(piles: IntArray, h: Int): Int {\n    fun hoursNeeded(speed: Int): Long {\n        var hours = 0L\n        for (pile in piles) hours += pile / speed\n        return hours\n    }\n    var low = 1\n    var high = piles.max()\n    while (low < high) {\n        val mid = low + (high - low) / 2\n        if (hoursNeeded(mid) <= h) high = mid else low = mid + 1\n    }\n    return low\n}",
                    ),
                    LessonChoice(
                        text = "Sums, for every pile, the ceiling of speed divided by pile size, computed as (speed + pile - 1) / pile.",
                        correct = false,
                        feedback = "This divides speed by pile size instead of pile size by speed, inverting the relationship entirely - the hours a pile takes should shrink as speed grows, not the other way around.",
                        code = "fun minEatingSpeed(piles: IntArray, h: Int): Int {\n    fun hoursNeeded(speed: Int): Long {\n        var hours = 0L\n        for (pile in piles) hours += (speed + pile - 1) / pile\n        return hours\n    }\n    var low = 1\n    var high = piles.max()\n    while (low < high) {\n        val mid = low + (high - low) / 2\n        if (hoursNeeded(mid) <= h) high = mid else low = mid + 1\n    }\n    return low\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "koko-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version converges on a speed that's sometimes too fast, using more speed than necessary. What's the bug?",
                code = "var low = 1\nvar high = piles.max()\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    if (hoursNeeded(mid) <= h) low = mid + 1 else high = mid\n}\nreturn low",
                choices = listOf(
                    LessonChoice(
                        "Change if (hoursNeeded(mid) <= h) low = mid + 1 else high = mid to if (hoursNeeded(mid) <= h) high = mid else low = mid + 1.",
                        true,
                        "When a speed already finishes in time, the search should keep it as a candidate and try slower speeds, not move past it - moving low forward on success instead throws away working speeds and keeps only faster ones.",
                    ),
                    LessonChoice(
                        "Change var high = piles.max() to var high = piles.max() + 1.",
                        false,
                        "The largest pile size is already always a feasible speed, since it finishes that pile in exactly one hour - adding one more to the starting bound doesn't address which direction the search moves after a successful check.",
                    ),
                    LessonChoice(
                        "Change while (low < high) to while (low <= high).",
                        false,
                        "That would change when the loop stops, but the search would still move in the wrong direction after a feasible speed is found, so it wouldn't fix converging on a needlessly fast speed.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "koko-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of piles and m as the largest pile size, what is the time complexity of this approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n * m), because every possible speed from 1 to m is checked against every pile.",
                        false,
                        "Binary searching over the range of speeds means only about log m candidate speeds are ever checked, not all m of them individually.",
                    ),
                    LessonChoice(
                        "O(n * log m), because binary search over speeds takes O(log m) iterations, and each iteration checks feasibility across all n piles.",
                        true,
                        "The speed range from 1 to m shrinks logarithmically through binary search, and computing the hours needed for a candidate speed requires looking at every one of the n piles once.",
                    ),
                    LessonChoice(
                        "O(log n * log m), because both the piles and the speed range are searched using binary search.",
                        false,
                        "Only the speed range is binary searched - computing the hours needed for a given speed still requires a plain linear pass over all n piles, not a search through them.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "koko-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does this approach use beyond the input piles array?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because the hours needed for each pile at the current speed must be stored to sum them.",
                        false,
                        "Each pile's hours are added directly into a running total as the loop goes, so nothing proportional to the number of piles needs to be stored at once.",
                    ),
                    LessonChoice(
                        "O(log m), matching the number of binary search iterations performed over the speed range.",
                        false,
                        "The number of iterations the binary search performs affects how long it runs, not how much memory is used at any single point in time - each iteration reuses the same few variables.",
                    ),
                    LessonChoice(
                        "O(1), because only a fixed handful of variables, the speed bounds and a running hour total, are tracked at any point.",
                        true,
                        "Checking feasibility for a candidate speed only needs a running total updated pile by pile, and the binary search over speeds only needs its own low and high bounds - nothing scales with input size.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture guessing a speed and asking a simple yes-or-no question: 'At this speed, does Koko finish every pile within h hours?'\n\nA faster speed can only ever finish in the same amount of time or less, never more - so as speed increases, the answer to that question can only ever flip from no to yes, never back again.\n\nThat one-directional flip is exactly what makes it safe to binary search over candidate speeds instead of trying each one individually, homing in on the smallest speed where the answer first becomes yes.",
            walkthrough = listOf(
                "piles = [3,6,7,11], h = 8. low = 1, high = 11 (the largest pile).",
                "mid = 6. Hours needed: ceil(3/6)+ceil(6/6)+ceil(7/6)+ceil(11/6) = 1+1+2+2 = 6. 6 <= 8, so this speed works: high = 6.",
                "mid = 3. Hours needed: 1+2+3+4 = 10. 10 > 8, too slow: low = 4.",
                "mid = 5. Hours needed: 1+2+2+3 = 8. 8 <= 8, works: high = 5.",
                "mid = 4. Hours needed: 1+2+2+3 = 8. 8 <= 8, works: high = 4. Now low == high == 4.",
                "Return 4.",
            ),
            pseudocode = "hoursNeeded(speed): sum over piles of ceil(pile / speed)\nlow = 1, high = max(piles)\nwhile low < high:\n    mid = low + (high - low) / 2\n    if hoursNeeded(mid) <= h: high = mid\n    else: low = mid + 1\nreturn low",
            referenceCode = "fun minEatingSpeed(piles: IntArray, h: Int): Int {\n    fun hoursNeeded(speed: Int): Long {\n        var hours = 0L\n        for (pile in piles) hours += (pile + speed - 1) / speed\n        return hours\n    }\n    var low = 1\n    var high = piles.max()\n    while (low < high) {\n        val mid = low + (high - low) / 2\n        if (hoursNeeded(mid) <= h) high = mid else low = mid + 1\n    }\n    return low\n}",
            timeComplexity = "O(n log m), where n is the number of piles and m is the largest pile size.\n\nBinary searching the speed range from 1 to m takes O(log m) iterations, and each iteration computes the hours needed by scanning all n piles once.",
            spaceComplexity = "O(1), because checking a candidate speed only needs a running hour total, and the binary search itself only needs its own low and high bounds - nothing scales with the number of piles.",
            alternatives = listOf(
                "Try every speed one at a time starting from 1, checking each for feasibility, and stop at the first one that works.\nThis is the most direct translation of the problem statement.\nBut it can take as many checks as the largest pile size in the worst case, far more than binary search's logarithmic number of checks.",
                "Compute the exact minimum speed algebraically from the pile sizes and hour budget without any searching.\nIf such a closed form existed it would be instant.\nBut the relationship between speed and total hours involves per-pile rounding, so no simple direct formula captures it - checking candidate speeds is the practical approach.",
            ),
            commonMistakes = listOf(
                "Using plain integer division, pile / speed, instead of rounding up.\nA pile that doesn't divide evenly by the speed still needs one extra hour to finish its remainder, and plain division silently drops that hour, undercounting the total.",
                "Moving low forward after a candidate speed already succeeds, instead of moving high down to it.\nA successful speed should stay in play while the search looks for something even slower that also works - discarding it converges on a needlessly fast speed instead of the minimum one.",
                "Starting the binary search range too low or too high, such as starting high below the largest pile size.\nThe largest pile alone determines the slowest speed that could ever be necessary, since it must finish within a single hour at that speed.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: what is the smallest value in a range for which some yes-or-no condition first becomes true?",
                "A condition that only flips from false to true, and never back, as a candidate value increases is a strong signal that binary searching over the answer itself, not the input array, will work.",
                "Phrases like 'minimum speed,' 'minimum capacity,' or 'minimum days' to satisfy a constraint often hint at binary searching the space of possible answers.",
            ),
            takeaway = "When a problem asks for the minimum value that satisfies a condition which only gets easier to satisfy as the value increases, binary search directly over the range of possible answers, checking feasibility at each candidate, rather than testing every value one at a time.",
        ),
        nextSlug = "find-minimum-in-rotated-sorted-array",
    )

    private val findMinimumInRotatedSortedArrayLesson = RoadmapLesson(
        slug = "find-minimum-in-rotated-sorted-array",
        title = "Find Minimum in Rotated Sorted Array",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "An array that was originally sorted in ascending order has been rotated between 1 and its full length positions to the right. Given the rotated array with all distinct values, find the minimum element.",
        constraints = listOf(
            "All values in the array are unique.",
            "The array has at least one element.",
            "Aim for logarithmic time rather than scanning the whole array.",
        ),
        examples = listOf(
            LessonExample("nums = [3,4,5,1,2]", "1", "Originally [1,2,3,4,5], rotated so the minimum sits after the rotation point."),
            LessonExample("nums = [4,5,6,7,0,1,2]", "0", "The minimum, 0, is the single point where the array drops instead of rising."),
            LessonExample("nums = [11,13,15,17]", "11", "A rotation by the full length leaves the array unrotated, so the first element is already the minimum."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "rotated-min-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the minimum in logarithmic time by using the structure left over from the rotation?",
                choices = listOf(
                    LessonChoice(
                        "Scan the array once, tracking the smallest value seen so far, and return it after checking every element.",
                        false,
                        "This correctly finds the minimum, but scanning every element takes linear time and completely ignores the fact that most of the array is still sorted in two pieces.",
                    ),
                    LessonChoice(
                        "Binary search by comparing the middle element to the rightmost element; if the middle is larger, the minimum lies to its right, and if smaller, the minimum lies at or to its left.",
                        true,
                        "Comparing the middle to the right edge reveals which half is the untouched, still-sorted portion and which half contains the rotation point where the minimum hides, letting half the array be discarded each step.",
                    ),
                    LessonChoice(
                        "Binary search for the value that is smaller than the value immediately before it in the original array's index order.",
                        false,
                        "This describes what makes the minimum special once found, but it isn't itself a comparison that can be evaluated at a single middle index during the search - it doesn't give a rule for which half to discard.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rotated-min-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three binary search with low and high pointers, comparing the middle to an edge. Which one correctly narrows toward the minimum?",
                choices = listOf(
                    LessonChoice(
                        text = "Compares nums[mid] to nums[high]; if nums[mid] > nums[high], sets low = mid + 1, otherwise sets high = mid.",
                        correct = true,
                        feedback = "If the middle value is greater than the rightmost value, the rotation point, and therefore the minimum, must be strictly to the right of mid; otherwise mid itself could still be the minimum, so high should keep it in range.",
                        code = "fun findMin(nums: IntArray): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low < high) {\n        val mid = low + (high - low) / 2\n        if (nums[mid] > nums[high]) low = mid + 1 else high = mid\n    }\n    return nums[low]\n}",
                    ),
                    LessonChoice(
                        text = "Compares nums[mid] to nums[low]; if nums[mid] > nums[low], sets low = mid + 1, otherwise sets high = mid.",
                        correct = false,
                        feedback = "Comparing against the left edge instead of the right edge doesn't reliably tell you which side the rotation point is on, since the left portion can look sorted relative to itself either way.",
                        code = "fun findMin(nums: IntArray): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low < high) {\n        val mid = low + (high - low) / 2\n        if (nums[mid] > nums[low]) low = mid + 1 else high = mid\n    }\n    return nums[low]\n}",
                    ),
                    LessonChoice(
                        text = "Compares nums[mid] to nums[high]; if nums[mid] > nums[high], sets high = mid, otherwise sets low = mid + 1.",
                        correct = false,
                        feedback = "This applies the correct comparison but moves the pointers in the opposite direction from what it should, discarding the half that actually contains the minimum instead of the half that can't.",
                        code = "fun findMin(nums: IntArray): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low < high) {\n        val mid = low + (high - low) / 2\n        if (nums[mid] > nums[high]) high = mid else low = mid + 1\n    }\n    return nums[low]\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rotated-min-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version sometimes skips past the actual minimum and returns the wrong value. What's the bug?",
                code = "var low = 0\nvar high = nums.size - 1\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    if (nums[mid] > nums[high]) low = mid else high = mid - 1\n}\nreturn nums[low]",
                choices = listOf(
                    LessonChoice(
                        "Change if (nums[mid] > nums[high]) low = mid else high = mid - 1 to if (nums[mid] > nums[high]) low = mid + 1 else high = mid.",
                        true,
                        "Leaving low at mid instead of moving it past can stall the search without progress, and moving high to mid - 1 can eliminate mid even when mid itself might still be the minimum - both directions need to keep mid in range only when it could still be the answer.",
                    ),
                    LessonChoice(
                        "Change while (low < high) to while (low <= high).",
                        false,
                        "Allowing low and high to become equal and continue looping doesn't address which direction the pointers move after each comparison, so the underlying skip-past-the-minimum bug would remain.",
                    ),
                    LessonChoice(
                        "Change val mid = low + (high - low) / 2 to val mid = (low + high + 1) / 2.",
                        false,
                        "Rounding the midpoint up or down by one doesn't fix which half gets kept versus discarded after the comparison, which is where the actual bug lies.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rotated-min-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of elements, what is the time complexity of this binary search?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because in the worst case the rotation point could be anywhere in the array.",
                        false,
                        "Where the rotation point happens to sit doesn't matter - every comparison still discards roughly half of the remaining range, regardless of which half turns out to hold the minimum.",
                    ),
                    LessonChoice(
                        "O(n log n), because the array must first be checked for whether it's actually rotated at all.",
                        false,
                        "No separate check for rotation happens before the search begins - the same comparison logic naturally handles an unrotated array as a special case where the minimum sits at index 0.",
                    ),
                    LessonChoice(
                        "O(log n), because each comparison between the middle and an edge value discards half of the remaining search range.",
                        true,
                        "Comparing the middle to the rightmost value reliably identifies which half can be safely thrown away on every step, so the range shrinks logarithmically just like standard binary search.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rotated-min-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does this binary search use?",
                choices = listOf(
                    LessonChoice(
                        "O(log n), matching the number of comparisons the search performs before converging.",
                        false,
                        "The number of comparisons performed affects the running time, not the memory used at any single moment - each comparison reuses the same few tracked variables.",
                    ),
                    LessonChoice(
                        "O(1), because only a fixed handful of variables, low, high, and mid, are tracked regardless of array size.",
                        true,
                        "The array is read in place using index comparisons, and the search only ever updates the same few integer variables as it narrows the range.",
                    ),
                    LessonChoice(
                        "O(n), because the original unrotated array must be reconstructed to compare against.",
                        false,
                        "Nothing about this approach reconstructs the original unrotated array - the minimum is located directly within the rotated array using comparisons alone.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture the array as two sorted runs stitched together at the rotation point, where a big value is immediately followed by a much smaller one.\n\nComparing the middle element to the rightmost element reveals which side of the rotation point mid sits on: if mid is bigger than the rightmost value, mid is still in the first, larger run, and the rotation point, along with the minimum, must be further right.\n\nOtherwise, mid is already at or past the rotation point, so the minimum is at mid or somewhere to its left - either way, half the array can always be safely dropped.",
            walkthrough = listOf(
                "nums = [4,5,6,7,0,1,2]. low = 0, high = 6.",
                "mid = 3, nums[3] = 7. nums[6] = 2. 7 > 2, so the minimum is to the right: low = 4.",
                "mid = 5, nums[5] = 1. nums[6] = 2. 1 <= 2, so the minimum is at mid or to its left: high = 5.",
                "mid = 4, nums[4] = 0. nums[5] = 1. 0 <= 1, so high = 4. Now low == high == 4.",
                "Return nums[4] = 0.",
            ),
            pseudocode = "low = 0, high = length - 1\nwhile low < high:\n    mid = low + (high - low) / 2\n    if nums[mid] > nums[high]: low = mid + 1\n    else: high = mid\nreturn nums[low]",
            referenceCode = "fun findMin(nums: IntArray): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low < high) {\n        val mid = low + (high - low) / 2\n        if (nums[mid] > nums[high]) low = mid + 1 else high = mid\n    }\n    return nums[low]\n}",
            timeComplexity = "O(log n), where n is the number of elements.\n\nComparing the middle value to the rightmost value on every step reliably identifies which half can be discarded, so the search range keeps halving just like in a standard binary search.",
            spaceComplexity = "O(1), because only a fixed handful of variables, low, high, and mid, are tracked as the search narrows, regardless of how large the array is.",
            alternatives = listOf(
                "Scan the array once, tracking the smallest value seen so far.\nThis is the simplest possible approach and needs no rotation-specific logic at all.\nBut it costs O(n) time, ignoring the fact that most of the array is still sorted and could rule out large chunks at once.",
                "Find the rotation point by locating the single index where an element is smaller than the one before it, using a linear scan for that specific pattern.\nThis also directly identifies the minimum.\nBut scanning for that pattern is still O(n) in the worst case, rather than taking advantage of the sorted runs to search in logarithmic time.",
            ),
            commonMistakes = listOf(
                "Comparing the middle value to the leftmost value instead of the rightmost value.\nThe left portion can appear consistent with either an unrotated or rotated array, but comparing against the right edge reliably reveals which half currently contains the rotation point.",
                "Moving high to mid - 1 instead of mid when the middle value could still be the minimum.\nSince mid itself might already be the smallest element, excluding it entirely from the next range can skip right past the correct answer.",
                "Assuming the array must be checked for whether it's rotated at all before searching.\nThe same comparison-based logic already handles an unrotated array correctly, converging on index 0 without any special-casing.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: where does a sorted sequence stop rising and drop back down, inside data that's otherwise sorted in two pieces?",
                "An array described as 'sorted, then rotated' is a strong signal that it's really two sorted runs joined together, and binary search can still exploit that structure.",
                "Comparing the middle of a range against one of its edges, rather than against a fixed target, is a common pattern whenever the search is really hunting for a structural boundary rather than a specific value.",
            ),
            takeaway = "When an array is sorted and then rotated, compare the middle element to an edge of the current range to determine which half still holds the rotation point, and keep narrowing toward it - the array's underlying sorted structure still supports a logarithmic search even after rotation.",
        ),
        nextSlug = "search-in-rotated-sorted-array",
    )

    private val searchInRotatedSortedArrayLesson = RoadmapLesson(
        slug = "search-in-rotated-sorted-array",
        title = "Search in Rotated Sorted Array",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "An array originally sorted in ascending order with distinct values has been rotated at an unknown pivot. Given the rotated array and a target value, return the target's index if it exists, or -1 if it does not.",
        constraints = listOf(
            "All values in the array are unique.",
            "The array has at least one element and may or may not actually be rotated.",
            "Aim for logarithmic time rather than scanning the whole array.",
        ),
        examples = listOf(
            LessonExample("nums = [4,5,6,7,0,1,2], target = 0", "4", "0 sits at index 4, just after the rotation point."),
            LessonExample("nums = [4,5,6,7,0,1,2], target = 3", "-1", "3 does not appear anywhere in the array."),
            LessonExample("nums = [1], target = 0", "-1", "A single-element array where the target isn't the only value present."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "rotated-search-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach searches the rotated array in logarithmic time despite it not being fully sorted?",
                choices = listOf(
                    LessonChoice(
                        "First binary search to find the rotation point, then run a second, ordinary binary search on whichever of the two resulting sorted halves could contain the target.",
                        false,
                        "This can work and still runs in logarithmic time, but it requires two separate binary searches, one to locate the pivot and one to search - more steps than are actually necessary.",
                    ),
                    LessonChoice(
                        "At each step, determine which half of the current range, left of mid or right of mid, is currently sorted, then check whether the target falls within that sorted half's value range to decide which half to keep.",
                        true,
                        "Even though the whole array isn't sorted, at least one half of any given range always is - checking whether the target's value falls inside that sorted half's range gives a reliable rule for which half to keep on every single step.",
                    ),
                    LessonChoice(
                        "Un-rotate the array back into fully sorted order first, then binary search it normally.",
                        false,
                        "Physically rebuilding the original sorted order takes linear time and linear extra space, which defeats the purpose of trying to search in logarithmic time in the first place.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rotated-search-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three check which half is sorted, then decide whether to search there. Which one applies the correct rule?",
                choices = listOf(
                    LessonChoice(
                        text = "If nums[low] <= nums[mid], the left half is sorted; searches it only when nums[low] <= target and target < nums[mid], otherwise searches the right half, and mirrors this logic when the right half is sorted instead.",
                        correct = true,
                        feedback = "Checking that the target's value actually falls between the sorted half's own endpoints, not just comparing it to mid alone, correctly decides whether the target could be hiding in that half or must be in the other one.",
                        code = "fun search(nums: IntArray, target: Int): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        if (nums[mid] == target) return mid\n        if (nums[low] <= nums[mid]) {\n            if (nums[low] <= target && target < nums[mid]) high = mid - 1 else low = mid + 1\n        } else {\n            if (nums[mid] < target && target <= nums[high]) low = mid + 1 else high = mid - 1\n        }\n    }\n    return -1\n}",
                    ),
                    LessonChoice(
                        text = "If nums[low] <= nums[mid], the left half is sorted; searches it whenever target < nums[mid], without checking that target is also at least nums[low].",
                        correct = false,
                        feedback = "Only comparing the target to mid, without also confirming it's at least as large as the sorted half's own starting value, can wrongly search that half for a target that's actually smaller than everything in it.",
                        code = "fun search(nums: IntArray, target: Int): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        if (nums[mid] == target) return mid\n        if (nums[low] <= nums[mid]) {\n            if (target < nums[mid]) high = mid - 1 else low = mid + 1\n        } else {\n            if (nums[mid] < target) low = mid + 1 else high = mid - 1\n        }\n    }\n    return -1\n}",
                    ),
                    LessonChoice(
                        text = "Always assumes the left half is sorted and applies the left-half rule, regardless of whether nums[low] <= nums[mid] actually holds.",
                        correct = false,
                        feedback = "Without first checking which half is actually sorted, applying the left-half rule to a range where the right half is the sorted one uses comparisons that no longer mean what they're assumed to mean.",
                        code = "fun search(nums: IntArray, target: Int): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        if (nums[mid] == target) return mid\n        if (nums[low] <= target && target < nums[mid]) high = mid - 1 else low = mid + 1\n    }\n    return -1\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rotated-search-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version returns -1 for some targets that actually exist in the array. What's the bug?",
                code = "if (nums[low] <= nums[mid]) {\n    if (nums[low] <= target && target < nums[mid]) high = mid - 1 else low = mid + 1\n} else {\n    if (nums[mid] < target && target <= nums[low]) low = mid + 1 else high = mid - 1\n}",
                choices = listOf(
                    LessonChoice(
                        "Change target <= nums[low] to target <= nums[high] in the else branch.",
                        true,
                        "When the right half is the sorted one, the target must be compared against nums[high], the sorted half's own upper end, not against nums[low], which belongs to the unsorted left half in that case.",
                    ),
                    LessonChoice(
                        "Change nums[low] <= nums[mid] to nums[low] < nums[mid].",
                        false,
                        "Whether that comparison is strict or not barely matters here since equal values would mean low and mid are the same index - it doesn't address the wrong boundary used in the right-half branch.",
                    ),
                    LessonChoice(
                        "Change if (nums[mid] == target) return mid to if (nums[mid] >= target) return mid.",
                        false,
                        "Returning as soon as nums[mid] is greater than or equal to the target would return a wrong, non-matching index whenever mid overshoots the target instead of landing exactly on it.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rotated-search-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With n as the number of elements, what is the time complexity of this approach?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because the rotation point must first be located with a separate linear scan.",
                        false,
                        "No separate scan for the rotation point happens - which half is sorted is determined with a single comparison at each step, using the same pass that also narrows toward the target.",
                    ),
                    LessonChoice(
                        "O(log n), because each step discards one full half of the remaining range using a single comparison plus a range check.",
                        true,
                        "Even though the array isn't fully sorted, exactly one comparison determines which half is sorted, and one more determines whether the target could be in it - together they still discard half the range every step.",
                    ),
                    LessonChoice(
                        "O(n log n), because both halves must be checked for whether they're sorted before deciding which one to search.",
                        false,
                        "Only a single comparison, nums[low] versus nums[mid], is needed to determine which half is sorted - there's no need to separately scan both halves to check.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "rotated-search-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does this approach use?",
                choices = listOf(
                    LessonChoice(
                        "O(n), because the array must be conceptually split into its two sorted halves and stored separately.",
                        false,
                        "Neither half is ever actually copied out or stored separately - which half is sorted is determined purely through index comparisons on the original array.",
                    ),
                    LessonChoice(
                        "O(log n), matching the number of comparisons made before the search converges.",
                        false,
                        "The number of comparisons performed determines the running time, not how much memory is in use at any given moment - every comparison reuses the same handful of tracked variables.",
                    ),
                    LessonChoice(
                        "O(1), because only a fixed handful of variables, low, high, and mid, are tracked regardless of array size.",
                        true,
                        "Determining which half is sorted and whether the target falls in it only requires comparing existing array values at a few indices - nothing is copied or allocated proportional to the array's size.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture a normal binary search, but with one extra question added at each step: 'which half of my current range is actually sorted right now?'\n\nBecause the rotation only creates a single break point, at least one of the two halves around any mid is always fully sorted, even if the other one isn't.\n\nOnce it's clear which half is sorted, a simple range check, is the target between that sorted half's own smallest and largest values, decides whether to search there or move to the other half instead.",
            walkthrough = listOf(
                "nums = [4,5,6,7,0,1,2], target = 0. low = 0, high = 6.",
                "mid = 3, nums[3] = 7. nums[low] = 4 <= 7, so the left half [4,5,6,7] is sorted. Is 0 between 4 and 7? No.",
                "So the target must be in the right half: low = 4.",
                "mid = 5, nums[5] = 1. nums[low] = 0 <= 1, so the left half [0,1] is sorted. Is 0 between 0 and 1? Yes, and 0 < nums[mid]: high = 4.",
                "mid = 4, nums[4] = 0. Match found.",
                "Return 4.",
            ),
            pseudocode = "low = 0, high = length - 1\nwhile low <= high:\n    mid = low + (high - low) / 2\n    if nums[mid] == target: return mid\n    if nums[low] <= nums[mid]:\n        if nums[low] <= target < nums[mid]: high = mid - 1\n        else: low = mid + 1\n    else:\n        if nums[mid] < target <= nums[high]: low = mid + 1\n        else: high = mid - 1\nreturn -1",
            referenceCode = "fun search(nums: IntArray, target: Int): Int {\n    var low = 0\n    var high = nums.size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        if (nums[mid] == target) return mid\n        if (nums[low] <= nums[mid]) {\n            if (nums[low] <= target && target < nums[mid]) high = mid - 1 else low = mid + 1\n        } else {\n            if (nums[mid] < target && target <= nums[high]) low = mid + 1 else high = mid - 1\n        }\n    }\n    return -1\n}",
            timeComplexity = "O(log n), where n is the number of elements.\n\nDetermining which half is sorted, and whether the target falls within it, takes only a couple of comparisons per step, and either way one full half of the range is discarded, so the total work still halves at every step.",
            spaceComplexity = "O(1), because only a fixed handful of variables, low, high, and mid, are tracked - no half of the array is ever copied out or stored separately.",
            alternatives = listOf(
                "First binary search to find the exact rotation index, then run a second ordinary binary search restricted to whichever half could contain the target.\nThis also achieves O(log n) time.\nBut it needs two separate binary search passes instead of folding the sorted-half check directly into a single pass.",
                "Scan the array once, comparing each element to the target directly.\nThis needs no reasoning about rotation or sortedness at all.\nBut it costs O(n) time, ignoring that most of the array remains sorted in two pieces that a smarter search could exploit.",
            ),
            commonMistakes = listOf(
                "Comparing the target only to nums[mid] when deciding which half to search, without also checking it against the sorted half's own starting or ending value.\nA target smaller than everything in the sorted half can still be less than nums[mid], leading the search into the wrong half.",
                "Using the wrong boundary value, such as nums[low] when the right half is the sorted one instead of nums[high].\nEach half has its own relevant boundary, and mixing them up produces a range check that doesn't actually correspond to that half's real value range.",
                "Assuming the whole array must be sorted before applying any binary search logic at all, and falling back to a linear scan.\nAt least one half around any mid is always sorted, which is enough structure for binary search to still work.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: how do I binary search when only part of the array is sorted at any given moment?",
                "A single rotation point guarantees that at least one half of any range is fully sorted - a strong signal that a modified binary search, not a linear scan, is the way to exploit that.",
                "Whenever 'rotated' and 'sorted' appear together, checking which half of the current range is sorted before deciding where to search is usually the key insight.",
            ),
            takeaway = "When searching a rotated sorted array, determine which half of the current range is still sorted, then check whether the target's value falls within that sorted half's own range to decide which half to keep - one half is always sorted, which is enough to preserve logarithmic search time.",
        ),
        nextSlug = "time-based-key-value-store",
    )

    private val timeBasedKeyValueStoreLesson = RoadmapLesson(
        slug = "time-based-key-value-store",
        title = "Time Based Key-Value Store",
        difficulty = CurriculumDifficulty.MEDIUM,
        description = "Design a key-value store that supports storing multiple values for the same key at different timestamps, and retrieving the value for a key that was set at the largest timestamp less than or equal to a given query timestamp.",
        constraints = listOf(
            "Timestamps passed to set are strictly increasing for any given key.",
            "A get call may use a timestamp before any value for that key was ever set, in which case it should return an empty string.",
            "Aim for logarithmic time per get call rather than scanning every stored value for a key.",
        ),
        examples = listOf(
            LessonExample("set(\"foo\",\"bar\",1); get(\"foo\",1)", "\"bar\"", "Retrieving at the exact timestamp it was set returns that value."),
            LessonExample("get(\"foo\",3) after only set(\"foo\",\"bar\",1)", "\"bar\"", "3 has no exact match, but 1 is the largest stored timestamp not exceeding 3."),
            LessonExample("set(\"foo\",\"bar\",1); set(\"foo\",\"bar2\",4); get(\"foo\",4)", "\"bar2\"", "The most recent value at or before the query timestamp is returned."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "timemap-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach answers a get query in logarithmic time rather than scanning every stored value for a key?",
                choices = listOf(
                    LessonChoice(
                        "For each key, store its (timestamp, value) pairs in a list in the order they were set, and for get, scan the list from the end backward until a timestamp at or before the query is found.",
                        false,
                        "Because timestamps are set in strictly increasing order, this scan does find the right answer, but in the worst case it still checks every stored pair for that key, costing linear time per query.",
                    ),
                    LessonChoice(
                        "For each key, store its (timestamp, value) pairs in a list in increasing timestamp order, and for get, binary search that list for the largest timestamp not exceeding the query.",
                        true,
                        "Since set calls for a key always use strictly increasing timestamps, the list for that key is automatically sorted by timestamp, so binary search can directly find the right boundary without scanning.",
                    ),
                    LessonChoice(
                        "Store every (key, timestamp, value) triple in one single sorted list across all keys, and for get, binary search the whole combined list.",
                        false,
                        "Mixing every key's entries into one shared list means a query for one key would need extra work to skip past entries belonging to other keys, undermining a clean binary search over just that key's own history.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "timemap-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three binary search a key's timestamp list for the largest timestamp not exceeding the query. Which one correctly narrows toward it?",
                choices = listOf(
                    LessonChoice(
                        text = "Binary searches with low = 0, high = list.size - 1; when list[mid].timestamp <= query, records mid as a candidate answer and moves low = mid + 1; otherwise moves high = mid - 1.",
                        correct = true,
                        feedback = "Recording every timestamp that qualifies as a possible answer while still searching further right for an even larger qualifying timestamp correctly converges on the largest one that doesn't exceed the query.",
                        code = "class TimeMap {\n    private val store = HashMap<String, MutableList<Pair<Int, String>>>()\n    fun set(key: String, value: String, timestamp: Int) {\n        store.getOrPut(key) { mutableListOf() }.add(timestamp to value)\n    }\n    fun get(key: String, timestamp: Int): String {\n        val entries = store[key] ?: return \"\"\n        var low = 0\n        var high = entries.size - 1\n        var result = \"\"\n        while (low <= high) {\n            val mid = low + (high - low) / 2\n            if (entries[mid].first <= timestamp) {\n                result = entries[mid].second\n                low = mid + 1\n            } else {\n                high = mid - 1\n            }\n        }\n        return result\n    }\n}",
                    ),
                    LessonChoice(
                        text = "Binary searches the same way, but records a candidate and moves low = mid + 1 only when list[mid].timestamp == query exactly, otherwise always moves high = mid - 1.",
                        correct = false,
                        feedback = "Requiring an exact timestamp match means any query timestamp that falls between two stored timestamps, which is the common case, never finds a candidate at all, even though a valid earlier entry exists.",
                        code = "class TimeMap {\n    private val store = HashMap<String, MutableList<Pair<Int, String>>>()\n    fun set(key: String, value: String, timestamp: Int) {\n        store.getOrPut(key) { mutableListOf() }.add(timestamp to value)\n    }\n    fun get(key: String, timestamp: Int): String {\n        val entries = store[key] ?: return \"\"\n        var low = 0\n        var high = entries.size - 1\n        var result = \"\"\n        while (low <= high) {\n            val mid = low + (high - low) / 2\n            if (entries[mid].first == timestamp) {\n                result = entries[mid].second\n                low = mid + 1\n            } else {\n                high = mid - 1\n            }\n        }\n        return result\n    }\n}",
                    ),
                    LessonChoice(
                        text = "Binary searches with the comparison reversed: when list[mid].timestamp <= query, moves high = mid - 1 and records mid as a candidate; otherwise moves low = mid + 1.",
                        correct = false,
                        feedback = "Moving high down after a qualifying timestamp is found searches further left for a smaller qualifying timestamp instead of a larger one, converging on the earliest match instead of the most recent one at or before the query.",
                        code = "class TimeMap {\n    private val store = HashMap<String, MutableList<Pair<Int, String>>>()\n    fun set(key: String, value: String, timestamp: Int) {\n        store.getOrPut(key) { mutableListOf() }.add(timestamp to value)\n    }\n    fun get(key: String, timestamp: Int): String {\n        val entries = store[key] ?: return \"\"\n        var low = 0\n        var high = entries.size - 1\n        var result = \"\"\n        while (low <= high) {\n            val mid = low + (high - low) / 2\n            if (entries[mid].first <= timestamp) {\n                result = entries[mid].second\n                high = mid - 1\n            } else {\n                low = mid + 1\n            }\n        }\n        return result\n    }\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "timemap-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version returns an empty string for some valid queries where an earlier timestamp does exist. What's the bug?",
                code = "var low = 0\nvar high = entries.size - 1\nvar result = \"\"\nwhile (low <= high) {\n    val mid = low + (high - low) / 2\n    if (entries[mid].first < timestamp) {\n        result = entries[mid].second\n        low = mid + 1\n    } else {\n        high = mid - 1\n    }\n}\nreturn result",
                choices = listOf(
                    LessonChoice(
                        "Change entries[mid].first < timestamp to entries[mid].first <= timestamp.",
                        true,
                        "Using a strict less-than comparison skips over an entry whose timestamp exactly equals the query, treating it as if it didn't qualify - the query timestamp itself should always count as a valid match, not just timestamps strictly before it.",
                    ),
                    LessonChoice(
                        "Change low = mid + 1 to low = mid in the qualifying branch.",
                        false,
                        "Leaving low at mid instead of moving past it can stall the search and prevent it from making progress, but it doesn't address why an exact-timestamp match is being skipped.",
                    ),
                    LessonChoice(
                        "Change var result = \"\" to var result: String? = null.",
                        false,
                        "Changing the type used to represent 'no match yet' doesn't change which entries the comparison treats as qualifying - the strict less-than is still excluding valid exact matches.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "timemap-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With m as the number of stored timestamps for a given key, what is the time complexity of a single get call?",
                choices = listOf(
                    LessonChoice(
                        "O(m), because in the worst case every stored timestamp for the key must be checked.",
                        false,
                        "Binary search never checks every stored timestamp - each comparison discards roughly half of the remaining candidates within that key's list.",
                    ),
                    LessonChoice(
                        "O(log m), because binary searching the key's sorted timestamp list halves the remaining range on every comparison.",
                        true,
                        "Since each key's timestamps are already in increasing order from how they were set, binary searching that list takes a number of steps proportional to the log of how many timestamps are stored.",
                    ),
                    LessonChoice(
                        "O(m log m), because the timestamps must be sorted before they can be searched.",
                        false,
                        "The timestamps for a key are already stored in increasing order as they're set, since set is only ever called with strictly increasing timestamps - no sorting step happens at query time.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "timemap-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much space does this key-value store use overall, across all set calls?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because get only ever tracks a fixed handful of variables during its binary search.",
                        false,
                        "While a single get call itself only uses a fixed amount of extra space, the store as a whole must retain every value ever set, so its total space grows with how many set calls have been made.",
                    ),
                    LessonChoice(
                        "O(log n), where n is the total number of set calls, since binary search only touches a logarithmic number of entries.",
                        false,
                        "How many entries a single search touches doesn't bound how much total space is stored - every (timestamp, value) pair from every set call must still be kept somewhere for future queries.",
                    ),
                    LessonChoice(
                        "O(n), where n is the total number of set calls, because every stored (timestamp, value) pair must be retained for future queries.",
                        true,
                        "Nothing set is ever discarded, since any past timestamp could still be the right answer for some future query, so the total space used grows directly with the number of set calls made.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture keeping a separate notebook page for each key, and on that page, writing down every value along with the timestamp it was set at, always at the bottom, since timestamps only increase.\n\nBecause the page is filled in strictly increasing timestamp order, it's automatically sorted by the time it's needed - answering 'what was the value at or before time t' becomes the same kind of question binary search already knows how to answer on any sorted list.\n\nThe search hunts for the rightmost entry whose timestamp doesn't exceed the query, remembering the best candidate found so far while still checking further right for an even better one.",
            walkthrough = listOf(
                "set(\"foo\",\"bar\",1); set(\"foo\",\"bar2\",4); set(\"foo\",\"bar3\",7). Key \"foo\" now holds [(1,\"bar\"), (4,\"bar2\"), (7,\"bar3\")].",
                "get(\"foo\", 5). low = 0, high = 2.",
                "mid = 1, entries[1] = (4,\"bar2\"). 4 <= 5, so this is a candidate: result = \"bar2\", low = 2.",
                "mid = 2, entries[2] = (7,\"bar3\"). 7 > 5, not a candidate: high = 1. Now low > high, loop ends.",
                "Return \"bar2\", the value set at the largest timestamp, 4, that doesn't exceed 5.",
            ),
            pseudocode = "set(key, value, timestamp): append (timestamp, value) to store[key]\nget(key, timestamp):\n    entries = store[key], or return \"\" if missing\n    low = 0, high = entries.size - 1, result = \"\"\n    while low <= high:\n        mid = low + (high - low) / 2\n        if entries[mid].timestamp <= timestamp:\n            result = entries[mid].value\n            low = mid + 1\n        else:\n            high = mid - 1\n    return result",
            referenceCode = "class TimeMap {\n    private val store = HashMap<String, MutableList<Pair<Int, String>>>()\n    fun set(key: String, value: String, timestamp: Int) {\n        store.getOrPut(key) { mutableListOf() }.add(timestamp to value)\n    }\n    fun get(key: String, timestamp: Int): String {\n        val entries = store[key] ?: return \"\"\n        var low = 0\n        var high = entries.size - 1\n        var result = \"\"\n        while (low <= high) {\n            val mid = low + (high - low) / 2\n            if (entries[mid].first <= timestamp) {\n                result = entries[mid].second\n                low = mid + 1\n            } else {\n                high = mid - 1\n            }\n        }\n        return result\n    }\n}",
            timeComplexity = "O(log m) per get call, where m is the number of timestamps stored for that key, and O(1) per set call.\n\nEach key's timestamps are already sorted by the time they're queried, since set always receives strictly increasing timestamps, so a get call can binary search directly.",
            spaceComplexity = "O(n), where n is the total number of set calls made across all keys, since every (timestamp, value) pair must be retained for possible future queries.",
            alternatives = listOf(
                "For each key, keep the (timestamp, value) pairs and scan backward from the most recent on every get call.\nThis needs no binary search logic at all.\nBut it costs O(m) time per query in the worst case, rather than the O(log m) that binary search achieves on the same already-sorted data.",
                "Use a sorted map, such as a balanced tree keyed by timestamp, for each key, and query it for the largest key not exceeding the timestamp.\nMany standard libraries provide this as a single built-in operation.\nBut it typically carries more overhead per entry than a plain list with manual binary search, for the same asymptotic time.",
            ),
            commonMistakes = listOf(
                "Requiring an exact timestamp match instead of finding the largest timestamp not exceeding the query.\nMost queries land between two stored timestamps rather than exactly on one, so an exact-match search misses the valid earlier entry that should be returned.",
                "Using a strict less-than comparison, entries[mid].timestamp < timestamp, instead of less-than-or-equal.\nThe query timestamp itself should always count as a valid, matching entry when it was actually set at that exact time, not be treated as one step too late.",
                "Forgetting that a query timestamp before any value was ever set for that key should return an empty string rather than throwing an error or returning a default placeholder.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: how do I find the most recent entry at or before a given point in time, quickly?",
                "Values that arrive in a naturally increasing order, like timestamps, are a strong signal that no explicit sorting step is needed before binary searching them.",
                "'Largest value not exceeding a query' or 'most recent entry at or before' is a common phrasing for a binary search that looks for a boundary rather than an exact match.",
            ),
            takeaway = "When values for a key arrive with strictly increasing timestamps, store them in that same order and binary search for the rightmost timestamp not exceeding the query, keeping the best candidate seen so far while still searching further right for a better one.",
        ),
        nextSlug = "median-of-two-sorted-arrays",
    )

    private val medianOfTwoSortedArraysLesson = RoadmapLesson(
        slug = "median-of-two-sorted-arrays",
        title = "Median of Two Sorted Arrays",
        difficulty = CurriculumDifficulty.HARD,
        description = "Given two sorted arrays, find the median of the combined set of all their values, without actually merging the two arrays into one.",
        constraints = listOf(
            "The two arrays are each individually sorted in ascending order.",
            "The arrays may have different lengths, and either one may be empty.",
            "Aim for logarithmic time relative to the size of the smaller array, rather than merging both arrays.",
        ),
        examples = listOf(
            LessonExample("nums1 = [1,3], nums2 = [2]", "2.0", "Merged and sorted: [1,2,3]. The middle value of the odd-length combination is 2."),
            LessonExample("nums1 = [1,2], nums2 = [3,4]", "2.5", "Merged and sorted: [1,2,3,4]. With an even total count, the median is the average of the two middle values, 2 and 3."),
            LessonExample("nums1 = [], nums2 = [1]", "1.0", "One array is empty, so the median is simply the only value present."),
        ),
        questions = listOf(
            LessonQuestion(
                id = "median-approach",
                kind = LessonQuestionKind.APPROACH,
                prompt = "Which approach finds the median without merging the two arrays into one combined sorted array?",
                choices = listOf(
                    LessonChoice(
                        "Merge both arrays into one sorted array using a standard merge step, then read off the middle value or values directly.",
                        false,
                        "This correctly finds the median, but merging both arrays takes time proportional to their combined size, which is more work than the problem's logarithmic-time goal requires.",
                    ),
                    LessonChoice(
                        "Binary search over how many elements to take from the smaller array's left portion, choosing the matching count from the larger array so the two partitions together split all values evenly, with everything on the left no greater than everything on the right.",
                        true,
                        "Once a partition point in the smaller array is fixed, the required partition point in the larger array follows directly from the total counts needed on each side - binary searching that one partition point is enough to find where left and right meet correctly.",
                    ),
                    LessonChoice(
                        "Binary search each array independently for its own individual median, then average those two medians together.",
                        false,
                        "The overall median of the combined set isn't simply the average of each array's own median - that ignores how the two arrays' values interleave with each other once combined.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "median-implementation",
                kind = LessonQuestionKind.CODE_BLOCK,
                prompt = "All three binary search a partition point in the smaller array. Which one correctly checks whether a partition is valid?",
                choices = listOf(
                    LessonChoice(
                        text = "A partition is valid when the largest value in the smaller array's left part is no greater than the smallest value in the larger array's right part, and the largest value in the larger array's left part is no greater than the smallest value in the smaller array's right part.",
                        correct = true,
                        feedback = "Checking both cross comparisons together guarantees that every value on either left partition is truly no greater than every value on either right partition, which is exactly what makes the combined partition a valid split at the median.",
                        code = "fun findMedianSortedArrays(nums1: IntArray, nums2: IntArray): Double {\n    val (a, b) = if (nums1.size <= nums2.size) nums1 to nums2 else nums2 to nums1\n    var low = 0\n    var high = a.size\n    val half = (a.size + b.size + 1) / 2\n    while (low <= high) {\n        val cutA = (low + high) / 2\n        val cutB = half - cutA\n        val leftA = if (cutA == 0) Int.MIN_VALUE else a[cutA - 1]\n        val rightA = if (cutA == a.size) Int.MAX_VALUE else a[cutA]\n        val leftB = if (cutB == 0) Int.MIN_VALUE else b[cutB - 1]\n        val rightB = if (cutB == b.size) Int.MAX_VALUE else b[cutB]\n        if (leftA <= rightB && leftB <= rightA) {\n            return if ((a.size + b.size) % 2 == 0) (maxOf(leftA, leftB) + minOf(rightA, rightB)) / 2.0\n            else maxOf(leftA, leftB).toDouble()\n        } else if (leftA > rightB) high = cutA - 1 else low = cutA + 1\n    }\n    return 0.0\n}",
                    ),
                    LessonChoice(
                        text = "A partition is valid whenever the largest value in the smaller array's left part is no greater than the smallest value in the larger array's right part, checking only that one comparison.",
                        correct = false,
                        feedback = "Checking only one of the two cross comparisons can accept a partition where the larger array's own left part actually exceeds the smaller array's right part, which isn't a valid split for the median.",
                        code = "fun findMedianSortedArrays(nums1: IntArray, nums2: IntArray): Double {\n    val (a, b) = if (nums1.size <= nums2.size) nums1 to nums2 else nums2 to nums1\n    var low = 0\n    var high = a.size\n    val half = (a.size + b.size + 1) / 2\n    while (low <= high) {\n        val cutA = (low + high) / 2\n        val cutB = half - cutA\n        val leftA = if (cutA == 0) Int.MIN_VALUE else a[cutA - 1]\n        val rightA = if (cutA == a.size) Int.MAX_VALUE else a[cutA]\n        val leftB = if (cutB == 0) Int.MIN_VALUE else b[cutB - 1]\n        val rightB = if (cutB == b.size) Int.MAX_VALUE else b[cutB]\n        if (leftA <= rightB) {\n            return if ((a.size + b.size) % 2 == 0) (maxOf(leftA, leftB) + minOf(rightA, rightB)) / 2.0\n            else maxOf(leftA, leftB).toDouble()\n        } else high = cutA - 1\n    }\n    return 0.0\n}",
                    ),
                    LessonChoice(
                        text = "A partition is valid when the two partitions simply contain an equal number of elements on the left and right overall, without comparing any of the actual boundary values.",
                        correct = false,
                        feedback = "Matching partition sizes alone doesn't guarantee every left value is no greater than every right value - the actual boundary values must be compared, or the partition could split the arrays at the wrong point entirely.",
                        code = "fun findMedianSortedArrays(nums1: IntArray, nums2: IntArray): Double {\n    val (a, b) = if (nums1.size <= nums2.size) nums1 to nums2 else nums2 to nums1\n    var low = 0\n    var high = a.size\n    val half = (a.size + b.size + 1) / 2\n    while (low <= high) {\n        val cutA = (low + high) / 2\n        val cutB = half - cutA\n        val leftA = if (cutA == 0) Int.MIN_VALUE else a[cutA - 1]\n        val rightA = if (cutA == a.size) Int.MAX_VALUE else a[cutA]\n        val leftB = if (cutB == 0) Int.MIN_VALUE else b[cutB - 1]\n        val rightB = if (cutB == b.size) Int.MAX_VALUE else b[cutB]\n        if (cutA + cutB == half) {\n            return if ((a.size + b.size) % 2 == 0) (maxOf(leftA, leftB) + minOf(rightA, rightB)) / 2.0\n            else maxOf(leftA, leftB).toDouble()\n        } else if (cutA < half - cutB) low = cutA + 1 else high = cutA - 1\n    }\n    return 0.0\n}",
                    ),
                ),
            ),
            LessonQuestion(
                id = "median-debug",
                kind = LessonQuestionKind.DEBUG,
                prompt = "This version crashes with an out-of-bounds error whenever the smaller array is empty. What's the bug?",
                code = "val leftA = a[cutA - 1]\nval rightA = if (cutA == a.size) Int.MAX_VALUE else a[cutA]\nval leftB = if (cutB == 0) Int.MIN_VALUE else b[cutB - 1]\nval rightB = if (cutB == b.size) Int.MAX_VALUE else b[cutB]",
                choices = listOf(
                    LessonChoice(
                        "Change val leftA = a[cutA - 1] to val leftA = if (cutA == 0) Int.MIN_VALUE else a[cutA - 1].",
                        true,
                        "When cutA is 0, meaning no elements from array a are taken on the left, indexing a[cutA - 1] reaches for index -1, which doesn't exist - that edge needs the same Int.MIN_VALUE placeholder already used for leftB.",
                    ),
                    LessonChoice(
                        "Change val rightA = if (cutA == a.size) Int.MAX_VALUE else a[cutA] to val rightA = a[cutA].",
                        false,
                        "Removing that boundary check would instead introduce a crash when cutA equals a.size, since a[cutA] would then be reaching one past the end of the array - it doesn't fix the empty-array case.",
                    ),
                    LessonChoice(
                        "Change val leftB = if (cutB == 0) Int.MIN_VALUE else b[cutB - 1] to val leftB = b[cutB - 1].",
                        false,
                        "leftB already has the correct guard in place - removing it would introduce a new crash for cutB equal to 0 rather than fixing the actual missing guard on leftA.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "median-time",
                kind = LessonQuestionKind.TIME_COMPLEXITY,
                prompt = "With m and n as the sizes of the two arrays, what is the time complexity of this partition-based approach?",
                choices = listOf(
                    LessonChoice(
                        "O(m + n), because the merged combination of both arrays must still be considered.",
                        false,
                        "No merged combination is ever built or fully considered - only a partition point within the smaller array is searched for, without touching most elements of either array.",
                    ),
                    LessonChoice(
                        "O(log(m + n)), because binary search is performed over the combined length of both arrays together.",
                        false,
                        "The search only ranges over possible partition points within the smaller array specifically, not over an index space sized by both arrays combined.",
                    ),
                    LessonChoice(
                        "O(log(min(m, n))), because binary search is performed only over the partition point in the smaller of the two arrays.",
                        true,
                        "Since the larger array's partition point is always derived directly from the smaller array's, only the smaller array's partition range needs to be binary searched, taking a number of steps proportional to the log of its size.",
                    ),
                ),
            ),
            LessonQuestion(
                id = "median-space",
                kind = LessonQuestionKind.SPACE_COMPLEXITY,
                prompt = "How much extra space does this partition-based approach use?",
                choices = listOf(
                    LessonChoice(
                        "O(1), because only a fixed handful of variables, the partition boundaries and their four surrounding values, are tracked at any point.",
                        true,
                        "Each step of the search only needs the four boundary values around the current partition and the partition bounds themselves - nothing is copied or merged, so the extra space stays constant.",
                    ),
                    LessonChoice(
                        "O(m + n), because a merged array combining both inputs must be held in memory to locate the median.",
                        false,
                        "This approach specifically avoids ever building a merged array - the median is computed directly from a handful of boundary values around the current partition.",
                    ),
                    LessonChoice(
                        "O(log(min(m, n))), matching the number of binary search iterations performed.",
                        false,
                        "How many iterations the binary search performs determines its running time, not how much memory it uses at any single point - each iteration reuses the same few tracked variables.",
                    ),
                ),
            ),
        ),
        explanation = CompleteExplanation(
            intuition = "Picture drawing a single vertical line through both arrays at once, splitting the combined set of all values into a left group and a right group of equal, or nearly equal, size.\n\nIf that line is drawn in exactly the right place, every value in the left group is no greater than every value in the right group - and once that's true, the median is either the largest value on the left, or the average of the largest on the left and smallest on the right, depending on whether the total count is odd or even.\n\nRather than trying every possible line position, binary searching for where to cut the smaller array is enough, since the cut point in the larger array is then completely determined by how many elements are needed on the left overall.",
            walkthrough = listOf(
                "nums1 = [1,3], nums2 = [2]. Smaller array a = [2], larger array b = [1,3]. half = (1 + 2 + 1) / 2 = 2.",
                "low = 0, high = 1. cutA = 0, cutB = half - cutA = 2.",
                "leftA = MIN_VALUE (cutA is 0), rightA = a[0] = 2. leftB = b[1] = 3, rightB = MAX_VALUE (cutB is b.size).",
                "Check leftA <= rightB (MIN_VALUE <= MAX_VALUE, true) and leftB <= rightA (3 <= 2, false). Since leftB > rightA, more of a's elements are needed on the left: low = cutA + 1 = 1.",
                "cutA = 1, cutB = half - cutA = 1. leftA = a[0] = 2, rightA = MAX_VALUE. leftB = b[0] = 1, rightB = b[1] = 3.",
                "Check leftA <= rightB (2 <= 3, true) and leftB <= rightA (1 <= 2, true). Valid partition. Total count 3 is odd, so return maxOf(leftA, leftB) = maxOf(2, 1) = 2.0.",
            ),
            pseudocode = "a, b = smaller array, larger array\nlow = 0, high = a.size\nhalf = (a.size + b.size + 1) / 2\nwhile low <= high:\n    cutA = (low + high) / 2\n    cutB = half - cutA\n    leftA, rightA = boundary values around cutA in a (or MIN/MAX at the edges)\n    leftB, rightB = boundary values around cutB in b (or MIN/MAX at the edges)\n    if leftA <= rightB and leftB <= rightA:\n        if total count is even: return (max(leftA, leftB) + min(rightA, rightB)) / 2\n        else: return max(leftA, leftB)\n    else if leftA > rightB: high = cutA - 1\n    else: low = cutA + 1",
            referenceCode = "fun findMedianSortedArrays(nums1: IntArray, nums2: IntArray): Double {\n    val (a, b) = if (nums1.size <= nums2.size) nums1 to nums2 else nums2 to nums1\n    var low = 0\n    var high = a.size\n    val half = (a.size + b.size + 1) / 2\n    while (low <= high) {\n        val cutA = (low + high) / 2\n        val cutB = half - cutA\n        val leftA = if (cutA == 0) Int.MIN_VALUE else a[cutA - 1]\n        val rightA = if (cutA == a.size) Int.MAX_VALUE else a[cutA]\n        val leftB = if (cutB == 0) Int.MIN_VALUE else b[cutB - 1]\n        val rightB = if (cutB == b.size) Int.MAX_VALUE else b[cutB]\n        if (leftA <= rightB && leftB <= rightA) {\n            return if ((a.size + b.size) % 2 == 0) (maxOf(leftA, leftB) + minOf(rightA, rightB)) / 2.0\n            else maxOf(leftA, leftB).toDouble()\n        } else if (leftA > rightB) high = cutA - 1 else low = cutA + 1\n    }\n    return 0.0\n}",
            timeComplexity = "O(log(min(m, n))), where m and n are the sizes of the two arrays.\n\nOnly the smaller array's partition point is binary searched, since the larger array's partition point is always fully determined by it, so the number of steps depends on the log of the smaller array's size alone.",
            spaceComplexity = "O(1), because only the current partition boundaries and the four values surrounding them are tracked at any point - no merged array or extra storage proportional to input size is ever built.",
            alternatives = listOf(
                "Merge both sorted arrays into one combined sorted array, then read off the middle value or values directly.\nThis is the most direct translation of what a median actually means.\nBut merging costs O(m + n) time, far more than the O(log(min(m, n))) that the partition-based binary search achieves.",
                "Repeatedly discard k/2 elements from the front of whichever array's k/2th element is smaller, similar to finding the kth smallest element, applied twice to find both middle positions.\nThis also reaches logarithmic time.\nBut it requires more careful handling of the odd-versus-even total count case compared to the single-partition approach.",
            ),
            commonMistakes = listOf(
                "Forgetting to guard the edge cases where a partition point is exactly 0 or exactly the array's own length.\nAt those edges there's no real neighboring value on one side, and using MIN_VALUE or MAX_VALUE as a placeholder is what lets the same comparison logic work correctly there too.",
                "Binary searching over the larger array instead of the smaller one.\nSearching the larger array can leave the derived partition point in the smaller array falling outside its valid range, since the smaller array might not have enough elements to match.",
                "Checking only one of the two required cross comparisons, leftA <= rightB or leftB <= rightA, instead of both.\nA valid partition needs every value on either left side to be no greater than every value on either right side, which requires both comparisons to hold together.",
            ),
            recognitionSignals = listOf(
                "The question is really asking: where can two sorted sequences be cut so that everything on the left of both cuts is no greater than everything on the right of both cuts?",
                "A request for the median, without wanting the arrays actually merged, is a strong signal that a partition-based binary search, not a merge, is the intended approach.",
                "Whenever a target position (like the middle) needs to be found across two separate sorted sequences without combining them, searching over a cut point in the smaller one is a common technique.",
            ),
            takeaway = "When finding the median of two sorted arrays without merging them, binary search for a partition point in the smaller array, deriving the matching partition point in the larger array from the total counts needed, and check that every value on both left partitions is no greater than every value on both right partitions.",
        ),
        nextSlug = null,
    )

    private val lessons = listOf(
        containsDuplicate,
        validAnagram,
        twoSum,
        groupAnagrams,
        topKFrequentElements,
        encodeAndDecodeStrings,
        productOfArrayExceptSelf,
        validSudoku,
        longestConsecutiveSequence,
        validPalindrome,
        twoSumII,
        threeSum,
        containerWithMostWater,
        trappingRainWater,
        bestTimeToBuyAndSellStock,
        longestSubstringWithoutRepeating,
        longestRepeatingCharacterReplacement,
        permutationInString,
        minimumWindowSubstring,
        slidingWindowMaximum,
        validParentheses,
        minStackLesson,
        evaluateReversePolishNotation,
        generateParenthesesLesson,
        dailyTemperaturesLesson,
        carFleetLesson,
        largestRectangleInHistogramLesson,
        binarySearchLesson,
        searchA2DMatrixLesson,
        kokoEatingBananasLesson,
        findMinimumInRotatedSortedArrayLesson,
        searchInRotatedSortedArrayLesson,
        timeBasedKeyValueStoreLesson,
        medianOfTwoSortedArraysLesson,
    ).associateBy { it.slug }

    fun bySlug(slug: String): RoadmapLesson? = lessons[slug]
    fun hasLesson(slug: String): Boolean = slug in lessons
    val all: List<RoadmapLesson> get() = lessons.values.toList()
}
