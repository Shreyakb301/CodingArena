package com.codingarena.content

import com.codingarena.domain.model.PatternGroup.ARRAYS_HASHING
import com.codingarena.domain.model.PatternGroup.BINARY_SEARCH
import com.codingarena.domain.model.PatternGroup.SLIDING_WINDOW
import com.codingarena.domain.model.PatternGroup.STACK
import com.codingarena.domain.model.PatternGroup.TWO_POINTERS
import com.codingarena.domain.model.PracticeDifficulty.ADVANCED
import com.codingarena.domain.model.ProgrammingLanguage.CPP
import com.codingarena.domain.model.ProgrammingLanguage.GO
import com.codingarena.domain.model.ProgrammingLanguage.JAVA
import com.codingarena.domain.model.ProgrammingLanguage.JAVASCRIPT
import com.codingarena.domain.model.ProgrammingLanguage.PYTHON
import com.codingarena.domain.model.ProgrammingLanguage.SWIFT
import com.codingarena.domain.model.WorkoutCodeVariant
import com.codingarena.domain.model.WorkoutStep
import com.codingarena.domain.model.WorkoutStepKind.APPROACH
import com.codingarena.domain.model.WorkoutStepKind.BOUNDARY_UPDATE
import com.codingarena.domain.model.WorkoutStepKind.CODE_BLOCK
import com.codingarena.domain.model.WorkoutStepKind.PATTERN_RECOGNITION
import com.codingarena.domain.model.WorkoutStepKind.SPACE_COMPLEXITY
import com.codingarena.domain.model.WorkoutStepKind.STATE_SELECTION
import com.codingarena.domain.model.WorkoutStepKind.TIME_COMPLEXITY

/**
 * Advanced tier: ambiguous pattern selection, real tradeoffs between
 * approaches, algorithm limitations, difficult edge cases. Same 15 problems,
 * ten steps per topic matching the round quota, same doubled-kind conceptKey
 * sharing pattern as [developingWorkoutSteps] / [intermediateWorkoutSteps].
 */
internal val advancedWorkoutSteps: List<WorkoutStep> = listOf(
    // ------------------------------------------------------------ Arrays & Hashing
    step(
        "two-sum", ARRAYS_HASHING, PATTERN_RECOGNITION,
        "You're told the array is already sorted and asked to solve Two Sum again. Does the hash-map approach still make sense, or does something else now dominate?",
        conceptKey = "arrays-hashing-when-sortedness-changes-the-answer", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Two pointers from both ends now becomes the stronger choice - it reaches the same O(n) time as the hash map but with O(1) space instead of O(n), an advantage only available once sortedness can be assumed.",
                true,
                "The hash-map approach doesn't stop working, but sortedness unlocks a strictly better tradeoff - the same time complexity with no extra space, which is exactly the kind of context-dependent choice that separates 'a correct approach' from 'the best approach given what's known.'",
            ),
            choice(
                "Nothing changes - the hash map remains strictly the best choice regardless of sortedness.",
                false,
                "Sortedness specifically enables a pointer-based approach that matches the hash map's time complexity while using no extra space - ignoring that context misses a real, available improvement.",
            ),
            choice(
                "Sortedness makes binary search over each element the best choice instead.",
                false,
                "Binary searching for each element's complement would cost O(n log n) total, which is worse than either the hash map or the two-pointer approach - sortedness enables something better than that.",
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, PATTERN_RECOGNITION,
        "You're told the values are guaranteed to be integers within a small, known range (say, 0 to 1000), and the array can be huge. Does a hash set remain the best choice?",
        conceptKey = "arrays-hashing-when-sortedness-changes-the-answer", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Not necessarily - a fixed-size boolean array indexed directly by value can replace the hash set, avoiding hashing overhead entirely while still achieving O(n) time and bounded O(range) space.",
                true,
                "When the value range is small and known in advance, direct indexing sidesteps hashing altogether - it's a genuine, context-dependent improvement over a general-purpose hash set, not just a stylistic preference.",
            ),
            choice(
                "No - a hash set is always optimal regardless of what's known about the value range.",
                false,
                "Knowing the values fall in a small, fixed range specifically enables an even simpler and often faster direct-indexing approach - dismissing that context ignores a real available improvement.",
            ),
            choice(
                "Yes, but only if the array is also guaranteed to be sorted.",
                false,
                "Sortedness isn't the relevant property here - it's the small, known value range that enables direct indexing, independent of whether the array itself is sorted.",
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, APPROACH,
        "Now the strings can contain any Unicode character, not just lowercase English letters, and can be extremely long. Does the fixed IntArray(26) approach still apply, and if not, what does?",
        conceptKey = "arrays-hashing-fixed-vs-general-alphabet-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "No - a fixed 26-slot array assumes a specific alphabet; a HashMap<Char, Int> generalizes to any character set at the cost of some hashing overhead, trading a small constant-factor speed loss for correctness across all Unicode input.",
                true,
                "The fixed-size array's speed comes directly from assuming a known, small alphabet - once that assumption breaks, a hash map becomes not just an alternative but a genuine correctness requirement, not merely a style choice.",
            ),
            choice(
                "Yes - IntArray(26) can be resized dynamically to accommodate any character set encountered.",
                false,
                "A fixed-size array's size is set at creation and indexed by a specific computed offset (like c - 'a') - it isn't something that adapts to arbitrary characters without changing the indexing scheme entirely, which is effectively switching to a different data structure.",
            ),
            choice(
                "No - anagram checking becomes fundamentally impossible once Unicode is involved.",
                false,
                "Anagram checking remains entirely solvable for Unicode strings - it just requires a more general counting structure (like a hash map) instead of one built around a small, fixed alphabet.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, APPROACH,
        "The interviewer now asks for *all* pairs summing to target, not just one, and the array may contain many duplicate values. What's the real complication the original approach doesn't handle?",
        conceptKey = "arrays-hashing-single-vs-all-pairs-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "The original approach returns on the first match found, but finding *all* pairs (including from repeated values) requires tracking every index per value and avoiding both duplicate pairs and reusing the same index twice - a meaningfully different bookkeeping problem.",
                true,
                "Returning early was a simplification specific to needing just one answer - once every valid pair matters, the algorithm has to change shape to track all occurrences per value and carefully avoid double-counting, which the original design doesn't address at all.",
            ),
            choice(
                "The original approach already correctly finds all pairs, since the loop visits every index.",
                false,
                "The original approach returns immediately upon the first match, specifically because it was designed to answer 'does at least one pair exist' - it does not continue searching for or collecting every other valid pair.",
            ),
            choice(
                "The complication only exists if the array isn't sorted first.",
                false,
                "Sortedness isn't what creates the complication - the core issue is that finding *all* pairs (as opposed to just one) requires fundamentally different bookkeeping regardless of whether the array happens to be sorted.",
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, STATE_SELECTION,
        "The array is now a streaming, unbounded sequence of values, too large to fit in memory as a full set. What does this limitation force you to reconsider about the approach entirely?",
        conceptKey = "arrays-hashing-unbounded-stream-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "An exact hash set of every value seen becomes infeasible at true scale; a probabilistic structure like a Bloom filter trades a small, bounded false-positive rate for dramatically reduced memory, which the original exact approach cannot offer as an option.",
                true,
                "Once the input can't fit in memory, exactness itself becomes the tradeoff to reconsider - accepting a small, known chance of error in exchange for bounded memory is a fundamentally different kind of solution than the exact hash-set approach.",
            ),
            choice(
                "Nothing changes - a hash set still works identically regardless of how large the stream gets.",
                false,
                "A hash set's memory usage grows with the number of distinct values seen, without bound - on a truly unbounded stream, this eventually exceeds available memory, which the original approach has no way to handle.",
            ),
            choice(
                "The problem becomes unsolvable once the stream is unbounded.",
                false,
                "The problem remains solvable, just not with an exact, unbounded-memory structure - approximate structures like Bloom filters exist precisely to handle this class of situation with bounded resources.",
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, BOUNDARY_UPDATE,
        "s and t are both extremely long (hundreds of millions of characters) and this check runs millions of times per second in a hot path. The count-map approach is \"correct but too slow.\" What's the actual limitation being hit?",
        conceptKey = "arrays-hashing-throughput-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "The algorithm's O(n) time complexity is already optimal for comparing two strings of length n - the limitation isn't the algorithm's shape, it's the sheer constant amount of work per call multiplied by an extremely high call frequency, which no single-string algorithmic change alone can fix.",
                true,
                "When an already-optimal-complexity algorithm is 'too slow' at scale, the ceiling being hit is usually about total throughput (calls per second times work per call), not about finding a faster complexity class - that reframes the problem toward caching, precomputation, or reducing call frequency instead.",
            ),
            choice(
                "A different algorithm with better than O(n) complexity for this exact comparison exists and should be used instead.",
                false,
                "Determining whether two strings of length n are anagrams provably requires looking at every character at least once in the worst case - O(n) is the best possible complexity class here, not a shortcoming to engineer around.",
            ),
            choice(
                "The limitation is entirely due to using a HashMap instead of an IntArray for the counts.",
                false,
                "Switching to a fixed array would reduce constant-factor overhead somewhat, but at the scale described, that alone is unlikely to be the dominant bottleneck - the deeper limitation is the sheer volume of comparisons being requested.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, CODE_BLOCK,
        "Which snippet correctly handles the case where the array could contain integer values at the very edge of Int range, where target - nums[i] might itself overflow?",
        conceptKey = "arrays-hashing-overflow-edge-case", difficulty = ADVANCED,
        code = "val seen = HashMap<Int, Int>()\nfor (i in nums.indices) {\n    // ???\n}",
        choices = listOf(
            choice(
                "val complement = target.toLong() - nums[i].toLong()\nif (complement in Int.MIN_VALUE..Int.MAX_VALUE) {\n    val c = complement.toInt()\n    if (seen.containsKey(c)) return intArrayOf(seen[c]!!, i)\n}\nseen[nums[i]] = i",
                true,
                "Computing the subtraction in Long avoids the overflow that target - nums[i] could produce in plain Int arithmetic when both values sit near the extremes of the Int range, then safely checks whether the result even fits back into Int before using it as a key.",
                code = "val complement = target.toLong() - nums[i].toLong()\nif (complement in Int.MIN_VALUE..Int.MAX_VALUE) {\n    val c = complement.toInt()\n    if (seen.containsKey(c)) return intArrayOf(seen[c]!!, i)\n}\nseen[nums[i]] = i",
            ),
            choice(
                "val complement = target - nums[i]\nif (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
                false,
                "Computing target - nums[i] directly in Int arithmetic can silently overflow and wrap around to an incorrect value when both are near Int's extremes, producing a wrong complement without any visible error.",
                code = "val complement = target - nums[i]\nif (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
            ),
            choice(
                "val complement = Math.abs(target - nums[i])\nif (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
                false,
                "Taking the absolute value doesn't address overflow at all, and it also changes the actual value being searched for - a negative complement and its positive counterpart are different values that shouldn't be conflated.",
                code = "val complement = Math.abs(target - nums[i])\nif (seen.containsKey(complement)) return intArrayOf(seen[complement]!!, i)\nseen[nums[i]] = i",
            ),
        ),
        // Only Java and C++ get a variant: this question is specifically about
        // 32-bit int overflow. Kotlin's Int and Java's int share that width, and
        // C++'s int conventionally does too, but Python ints are arbitrary
        // precision, JS numbers safely cover this range, and Go/Swift's Int is
        // 64-bit on modern platforms - the scenario doesn't translate for them,
        // so those languages fall back to the Kotlin version rather than a
        // fabricated equivalent.
        languageVariants = listOf(
            WorkoutCodeVariant(
                JAVA,
                "Map<Integer, Integer> seen = new HashMap<>();\nfor (int i = 0; i < nums.length; i++) {\n    // ???\n}",
                choices = listOf(
                    choice("long complement = (long) target - nums[i];\nif (complement >= Integer.MIN_VALUE && complement <= Integer.MAX_VALUE) {\n    int c = (int) complement;\n    if (seen.containsKey(c)) return new int[]{seen.get(c), i};\n}\nseen.put(nums[i], i);", true, "Computing the subtraction in long avoids the overflow that target - nums[i] could produce in plain int arithmetic when both values sit near the extremes of the int range, then safely checks whether the result even fits back into int before using it as a key.", code = "long complement = (long) target - nums[i];\nif (complement >= Integer.MIN_VALUE && complement <= Integer.MAX_VALUE) {\n    int c = (int) complement;\n    if (seen.containsKey(c)) return new int[]{seen.get(c), i};\n}\nseen.put(nums[i], i);"),
                    choice("int complement = target - nums[i];\nif (seen.containsKey(complement)) return new int[]{seen.get(complement), i};\nseen.put(nums[i], i);", false, "Computing target - nums[i] directly in int arithmetic can silently overflow and wrap around to an incorrect value when both are near int's extremes, producing a wrong complement without any visible error.", code = "int complement = target - nums[i];\nif (seen.containsKey(complement)) return new int[]{seen.get(complement), i};\nseen.put(nums[i], i);"),
                    choice("int complement = Math.abs(target - nums[i]);\nif (seen.containsKey(complement)) return new int[]{seen.get(complement), i};\nseen.put(nums[i], i);", false, "Taking the absolute value doesn't address overflow at all, and it also changes the actual value being searched for - a negative complement and its positive counterpart are different values that shouldn't be conflated.", code = "int complement = Math.abs(target - nums[i]);\nif (seen.containsKey(complement)) return new int[]{seen.get(complement), i};\nseen.put(nums[i], i);"),
                ),
            ),
            WorkoutCodeVariant(
                CPP,
                "unordered_map<int, int> seen;\nfor (int i = 0; i < (int)nums.size(); i++) {\n    // ???\n}",
                choices = listOf(
                    choice("long long complement = (long long)target - nums[i];\nif (complement >= INT_MIN && complement <= INT_MAX) {\n    int c = (int)complement;\n    if (seen.count(c)) return {seen[c], i};\n}\nseen[nums[i]] = i;", true, "Computing the subtraction as a long long avoids the overflow that target - nums[i] could produce in plain int arithmetic when both values sit near the extremes of int's range, then safely checks whether the result even fits back into int before using it as a key.", code = "long long complement = (long long)target - nums[i];\nif (complement >= INT_MIN && complement <= INT_MAX) {\n    int c = (int)complement;\n    if (seen.count(c)) return {seen[c], i};\n}\nseen[nums[i]] = i;"),
                    choice("int complement = target - nums[i];\nif (seen.count(complement)) return {seen[complement], i};\nseen[nums[i]] = i;", false, "Computing target - nums[i] directly in int arithmetic is undefined behavior on overflow and can wrap around to an incorrect value when both are near int's extremes, producing a wrong complement without any visible error.", code = "int complement = target - nums[i];\nif (seen.count(complement)) return {seen[complement], i};\nseen[nums[i]] = i;"),
                    choice("int complement = abs(target - nums[i]);\nif (seen.count(complement)) return {seen[complement], i};\nseen[nums[i]] = i;", false, "Taking the absolute value doesn't address overflow at all, and it also changes the actual value being searched for - a negative complement and its positive counterpart are different values that shouldn't be conflated.", code = "int complement = abs(target - nums[i]);\nif (seen.count(complement)) return {seen[complement], i};\nseen[nums[i]] = i;"),
                ),
            ),
        ),
    ),
    step(
        "contains-duplicate", ARRAYS_HASHING, CODE_BLOCK,
        "A teammate wants a version that works identically whether nums is an IntArray or an arbitrarily-typed List<T> (as long as T has proper equals/hashCode). Which snippet generalizes cleanly?",
        conceptKey = "arrays-hashing-generic-type-generalization", difficulty = ADVANCED,
        code = "fun <T> hasDuplicate(items: List<T>): Boolean {\n    // ???\n}",
        choices = listOf(
            choice(
                "val seen = HashSet<T>()\nfor (item in items) if (!seen.add(item)) return true\nreturn false",
                true,
                "A HashSet<T> relies only on T's equals/hashCode contract, exactly like the original IntArray version relied on Int's built-in equality - the generic version is structurally identical, just parameterized over the element type.",
                code = "val seen = HashSet<T>()\nfor (item in items) if (!seen.add(item)) return true\nreturn false",
            ),
            choice(
                "val seen = HashSet<Int>()\nfor (item in items) if (!seen.add(item.hashCode())) return true\nreturn false",
                false,
                "Using only the hash code as the set's element risks false positives from hash collisions between genuinely different objects that happen to share a hash code - equals() must also be honored, which a plain HashSet<T> does automatically and this doesn't.",
                code = "val seen = HashSet<Int>()\nfor (item in items) if (!seen.add(item.hashCode())) return true\nreturn false",
            ),
            choice(
                "return items.toSet().size == items.size",
                false,
                "This is actually also correct and equivalent in behavior - it's a reasonable alternative, but it builds the entire set before comparing sizes rather than being able to return as soon as a duplicate is found, unlike the early-exit version.",
                code = "return items.toSet().size == items.size",
            ),
        ),
        languageVariants = listOf(
            WorkoutCodeVariant(
                PYTHON,
                "def has_duplicate(items):\n    # ???",
                choices = listOf(
                    choice("seen = set()\nfor item in items:\n    if item in seen:\n        return True\n    seen.add(item)\nreturn False", true, "A plain set relies only on the item's own equality and hashing, exactly like the original int-array version relied on int's built-in equality - this version is structurally identical, just untyped, since Python generalizes over element type for free.", code = "seen = set()\nfor item in items:\n    if item in seen:\n        return True\n    seen.add(item)\nreturn False"),
                    choice("seen = set()\nfor item in items:\n    key = str(item)\n    if key in seen:\n        return True\n    seen.add(key)\nreturn False", false, "Deduplicating by str(item) instead of the item itself risks false positives from different values that stringify the same way, like the integer 1 and the string \"1\" - the item's own equality must be honored, which a plain set does automatically and this doesn't.", code = "seen = set()\nfor item in items:\n    key = str(item)\n    if key in seen:\n        return True\n    seen.add(key)\nreturn False"),
                    choice("return len(set(items)) != len(items)", false, "This is actually also correct and equivalent in behavior - it's a reasonable alternative, but it builds the entire set before comparing sizes rather than being able to return as soon as a duplicate is found, unlike the early-exit version.", code = "return len(set(items)) != len(items)"),
                ),
            ),
            WorkoutCodeVariant(
                JAVA,
                "static <T> boolean hasDuplicate(List<T> items) {\n    // ???\n}",
                choices = listOf(
                    choice("Set<T> seen = new HashSet<>();\nfor (T item : items) if (!seen.add(item)) return true;\nreturn false;", true, "A HashSet<T> relies only on T's equals/hashCode contract, exactly like the original int-array version relied on Integer's built-in equality - the generic version is structurally identical, just parameterized over the element type.", code = "Set<T> seen = new HashSet<>();\nfor (T item : items) if (!seen.add(item)) return true;\nreturn false;"),
                    choice("Set<Integer> seen = new HashSet<>();\nfor (T item : items) if (!seen.add(item.hashCode())) return true;\nreturn false;", false, "Using only the hash code as the set's element risks false positives from hash collisions between genuinely different objects that happen to share a hash code - equals() must also be honored, which a plain HashSet<T> does automatically and this doesn't.", code = "Set<Integer> seen = new HashSet<>();\nfor (T item : items) if (!seen.add(item.hashCode())) return true;\nreturn false;"),
                    choice("return new HashSet<>(items).size() != items.size();", false, "This is actually also correct and equivalent in behavior - it's a reasonable alternative, but it builds the entire set before comparing sizes rather than being able to return as soon as a duplicate is found, unlike the early-exit version.", code = "return new HashSet<>(items).size() != items.size();"),
                ),
            ),
            WorkoutCodeVariant(
                JAVASCRIPT,
                "function hasDuplicate(items) {\n    // ???\n}",
                choices = listOf(
                    choice("const seen = new Set();\nfor (const item of items) {\n    if (seen.has(item)) return true;\n    seen.add(item);\n}\nreturn false;", true, "A Set relies only on the item's own equality, exactly like the original number-array version relied on that equality for numbers - this version is structurally identical, just untyped, since JavaScript generalizes over element type for free.", code = "const seen = new Set();\nfor (const item of items) {\n    if (seen.has(item)) return true;\n    seen.add(item);\n}\nreturn false;"),
                    choice("const seen = new Set();\nfor (const item of items) {\n    const key = String(item);\n    if (seen.has(key)) return true;\n    seen.add(key);\n}\nreturn false;", false, "Deduplicating by String(item) instead of the item itself risks false positives from different values that stringify the same way, like the number 1 and the string \"1\" - the item's own equality must be honored, which a plain Set does automatically and this doesn't.", code = "const seen = new Set();\nfor (const item of items) {\n    const key = String(item);\n    if (seen.has(key)) return true;\n    seen.add(key);\n}\nreturn false;"),
                    choice("return new Set(items).size !== items.length;", false, "This is actually also correct and equivalent in behavior - it's a reasonable alternative, but it builds the entire set before comparing sizes rather than being able to return as soon as a duplicate is found, unlike the early-exit version.", code = "return new Set(items).size !== items.length;"),
                ),
            ),
            WorkoutCodeVariant(
                CPP,
                "template <typename T>\nbool hasDuplicate(vector<T>& items) {\n    // ???\n}",
                choices = listOf(
                    choice("unordered_set<T> seen;\nfor (auto& item : items) {\n    if (!seen.insert(item).second) return true;\n}\nreturn false;", true, "An unordered_set<T> relies only on T's own equality and hash, exactly like the original int-vector version relied on int's built-in equality - the templated version is structurally identical, just parameterized over the element type.", code = "unordered_set<T> seen;\nfor (auto& item : items) {\n    if (!seen.insert(item).second) return true;\n}\nreturn false;"),
                    choice("unordered_set<size_t> seen;\nfor (auto& item : items) {\n    size_t h = std::hash<T>{}(item);\n    if (!seen.insert(h).second) return true;\n}\nreturn false;", false, "Using only the hash value as the set's element risks false positives from hash collisions between genuinely different objects that happen to share a hash - equality must also be honored, which a plain unordered_set<T> does automatically and this doesn't.", code = "unordered_set<size_t> seen;\nfor (auto& item : items) {\n    size_t h = std::hash<T>{}(item);\n    if (!seen.insert(h).second) return true;\n}\nreturn false;"),
                    choice("return unordered_set<T>(items.begin(), items.end()).size() != items.size();", false, "This is actually also correct and equivalent in behavior - it's a reasonable alternative, but it builds the entire set before comparing sizes rather than being able to return as soon as a duplicate is found, unlike the early-exit version.", code = "return unordered_set<T>(items.begin(), items.end()).size() != items.size();"),
                ),
            ),
            WorkoutCodeVariant(
                GO,
                "func hasDuplicate[T comparable](items []T) bool {\n    // ???\n}",
                choices = listOf(
                    choice("seen := map[T]bool{}\nfor _, item := range items {\n    if seen[item] {\n        return true\n    }\n    seen[item] = true\n}\nreturn false", true, "A map keyed by T relies only on T's own equality, exactly like the original int-slice version relied on int's built-in equality - the generic version is structurally identical, just parameterized over the element type via the comparable constraint.", code = "seen := map[T]bool{}\nfor _, item := range items {\n    if seen[item] {\n        return true\n    }\n    seen[item] = true\n}\nreturn false"),
                    choice("seen := map[string]bool{}\nfor _, item := range items {\n    key := fmt.Sprintf(\"%v\", item)\n    if seen[key] {\n        return true\n    }\n    seen[key] = true\n}\nreturn false", false, "Deduplicating by a formatted string instead of the item itself risks false positives from different values that format the same way - the item's own equality must be honored, which a map keyed by T does automatically and this doesn't.", code = "seen := map[string]bool{}\nfor _, item := range items {\n    key := fmt.Sprintf(\"%v\", item)\n    if seen[key] {\n        return true\n    }\n    seen[key] = true\n}\nreturn false"),
                    choice("set := map[T]bool{}\nfor _, item := range items {\n    set[item] = true\n}\nreturn len(set) != len(items)", false, "This is actually also correct and equivalent in behavior - it's a reasonable alternative, but it builds the entire set before comparing sizes rather than being able to return as soon as a duplicate is found, unlike the early-exit version.", code = "set := map[T]bool{}\nfor _, item := range items {\n    set[item] = true\n}\nreturn len(set) != len(items)"),
                ),
            ),
            WorkoutCodeVariant(
                SWIFT,
                "func hasDuplicate<T: Hashable>(_ items: [T]) -> Bool {\n    // ???\n}",
                choices = listOf(
                    choice("var seen = Set<T>()\nfor item in items {\n    if seen.contains(item) { return true }\n    seen.insert(item)\n}\nreturn false", true, "A Set<T> relies only on T's own Hashable conformance, exactly like the original int-array version relied on Int's built-in equality - the generic version is structurally identical, just parameterized over the element type.", code = "var seen = Set<T>()\nfor item in items {\n    if seen.contains(item) { return true }\n    seen.insert(item)\n}\nreturn false"),
                    choice("var seen = Set<Int>()\nfor item in items {\n    let h = item.hashValue\n    if seen.contains(h) { return true }\n    seen.insert(h)\n}\nreturn false", false, "Using only the hash value as the set's element risks false positives from hash collisions between genuinely different values that happen to share a hash - equality must also be honored, which a plain Set<T> does automatically and this doesn't.", code = "var seen = Set<Int>()\nfor item in items {\n    let h = item.hashValue\n    if seen.contains(h) { return true }\n    seen.insert(h)\n}\nreturn false"),
                    choice("return Set(items).count != items.count", false, "This is actually also correct and equivalent in behavior - it's a reasonable alternative, but it builds the entire set before comparing sizes rather than being able to return as soon as a duplicate is found, unlike the early-exit version.", code = "return Set(items).count != items.count"),
                ),
            ),
        ),
    ),
    step(
        "valid-anagram", ARRAYS_HASHING, TIME_COMPLEXITY,
        "A teammate claims the count-map approach is \"basically O(1)\" in practice because the alphabet is fixed at 26 letters, so the loop bound is effectively constant. Is this a defensible way to describe it?",
        conceptKey = "arrays-hashing-alphabet-bound-vs-input-length", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "No - the 26-slot count array is O(1) space, but the *time* still scales with the string's length n, since every character has to be visited to update the counts; the fixed alphabet bounds space, not the number of characters processed.",
                true,
                "It's an easy conflation to make, but the alphabet size limits how large the auxiliary structure can grow, not how many characters need to be read - reading each of n characters is unavoidable regardless of how few distinct symbols they can be.",
            ),
            choice(
                "Yes - since there are only 26 possible counts to update, the total work is bounded regardless of string length.",
                false,
                "The number of distinct counts being tracked doesn't bound how many times those counts get incremented - a length-n string still requires n individual increment operations, one per character.",
            ),
            choice(
                "Yes, but only for strings shorter than 26 characters.",
                false,
                "String length relative to the alphabet size doesn't change the fundamental relationship - the algorithm's time complexity is O(n) regardless of whether n is smaller or much larger than 26.",
            ),
        ),
    ),
    step(
        "two-sum", ARRAYS_HASHING, SPACE_COMPLEXITY,
        "A teammate argues that in a memory-constrained environment, sorting the array in place and using two pointers is strictly better than the hash-map approach, full stop. Is \"strictly better\" defensible here?",
        conceptKey = "arrays-hashing-space-vs-index-preservation-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Not quite - sorting in place saves the O(n) map space, but it also destroys the original indices the problem requires as output, so it only becomes viable if extra bookkeeping (like a parallel original-index array) is added back in, which reintroduces some of the very space being saved.",
                true,
                "Whether sort-then-two-pointers is actually better depends on what has to be preserved - dropping the hash map's space is real, but recovering the original indices afterward isn't free, so 'strictly better' overstates a genuine tradeoff as an unconditional win.",
            ),
            choice(
                "Yes - sorting in place with two pointers always dominates the hash-map approach whenever memory is limited.",
                false,
                "This ignores that the problem specifically asks for original index positions, which sorting destroys - recovering them requires extra space or structure that eats into the memory savings being claimed.",
            ),
            choice(
                "No - sorting in place is never viable for this problem regardless of memory constraints.",
                false,
                "Sort-then-two-pointers is a legitimate approach when index preservation is handled (e.g. by sorting pairs of value-and-original-index together) - it's a real tradeoff worth considering, not something to dismiss outright.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Two Pointers
    step(
        "valid-palindrome", TWO_POINTERS, PATTERN_RECOGNITION,
        "Now the check must also work correctly for strings containing astral-plane Unicode characters (which are represented as surrogate pairs, two Char values per visible character, in Kotlin's String). What does this limitation expose about the simple two-pointer approach?",
        conceptKey = "two-pointers-surrogate-pair-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Indexing by individual Char (as `s[left]`/`s[right]` does) can split a surrogate pair in half, comparing only one half of a character - correctly handling this requires iterating by code point, not by raw Char index, which changes how the two pointers need to move.",
                true,
                "The two-pointer *idea* still applies, but the assumption that one index equals one visible character breaks down for characters outside the Basic Multilingual Plane - the implementation needs code-point-aware indexing, not just the same Char-based loop.",
            ),
            choice(
                "Nothing changes - Char-based indexing already correctly represents every Unicode character in Kotlin.",
                false,
                "Kotlin's Char is a UTF-16 code unit, not a full Unicode code point - some characters require two Chars (a surrogate pair), which plain index-based access can split incorrectly.",
            ),
            choice(
                "The problem becomes unsolvable with two pointers once surrogate pairs are involved.",
                false,
                "It remains solvable with two pointers - the fix is to advance by code point rather than by raw Char index, not to abandon the two-pointer idea altogether.",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, PATTERN_RECOGNITION,
        "You're now asked for 4Sum (four numbers summing to target) instead of three. Does the sorted two-pointer approach still apply, and what does it cost to extend it?",
        conceptKey = "two-pointers-surrogate-pair-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Yes, it extends by adding one more fixed outer loop (now two nested fixed indices before the two-pointer sweep), which raises the time complexity from O(n squared) to O(n cubed) - the pattern generalizes, but each additional fixed value costs another full loop.",
                true,
                "The core two-pointer idea (fix everything except the last two values, then converge inward) still works for any fixed target count, it just costs one more nested loop per additional fixed value, which is a real, compounding complexity tradeoff worth naming explicitly.",
            ),
            choice(
                "No - two pointers only ever works for exactly three target values.",
                false,
                "The technique isn't inherently limited to three values - it generalizes to k values by fixing k-2 of them and running two pointers on the remainder, at the cost of additional nested loops.",
            ),
            choice(
                "Yes, and it keeps the exact same O(n squared) time complexity as 3Sum.",
                false,
                "Adding another value to fix before the two-pointer sweep adds another full loop over the array, which pushes the total complexity to O(n cubed), not the same O(n squared) as before.",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, APPROACH,
        "Consider a case where all heights are distinct and monotonically increasing, like [1, 2, 3, ..., 1000000]. Does the shorter-side-moves greedy rule still provably find the true maximum, or does this expose a limitation?",
        conceptKey = "two-pointers-greedy-correctness-proof-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It still provably works: at every step, moving the shorter side is the only move that could possibly increase the limiting height, and this greedy exchange argument holds regardless of the specific height pattern, monotonic or not - the proof doesn't depend on any particular shape of the input.",
                true,
                "The correctness of the shorter-side-moves rule comes from a general argument (moving the taller side can never help) that applies to any array of heights - a monotonic pattern doesn't weaken or strengthen that argument, it's just one specific case it still covers.",
            ),
            choice(
                "It fails on monotonic sequences since the maximum area is always at the very ends, which the algorithm might move away from too early.",
                false,
                "For a strictly increasing sequence, the maximum area actually does end up being correctly found at the two ends, since the shorter (left) side is the one that keeps moving inward while the taller (right) end stays put, correctly preserving the best possible width against the tallest bar.",
            ),
            choice(
                "It only works on monotonic sequences and fails on more \"random\" height patterns.",
                false,
                "The greedy correctness argument doesn't rely on any specific pattern like monotonicity - it holds for arbitrary height arrangements, which is exactly why the algorithm works generally, not just on convenient special cases.",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, APPROACH,
        "The interviewer asks for a version that also works when the input is a doubly-linked list instead of an array, where random access isn't O(1). What does this limitation force you to reconsider?",
        conceptKey = "two-pointers-random-access-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "The two-pointer sweep itself is fine (it only ever moves forward/backward one step at a time, which a linked list supports), but sorting a linked list efficiently requires a different algorithm (like merge sort adapted for linked lists) since array-style in-place sorting relies on random access this structure doesn't offer.",
                true,
                "The two-pointer *movement* pattern (step-by-step, never jumping) is actually linked-list-friendly - the real limitation shows up specifically in the sorting step, which needs to be rethought for a structure without O(1) indexing.",
            ),
            choice(
                "Nothing needs to change - two pointers work identically on arrays and linked lists.",
                false,
                "While the two-pointer movement itself translates reasonably well, the prerequisite sorting step does not - array-based sorting techniques that rely on random access don't directly apply to a linked list.",
            ),
            choice(
                "The whole approach becomes infeasible on a linked list, since two pointers require array indexing.",
                false,
                "Two pointers fundamentally just need 'move to next' and 'move to previous' operations, which a doubly-linked list supports directly - the approach remains feasible, just needs a linked-list-appropriate sort first.",
            ),
        ),
    ),
    step(
        "valid-palindrome", TWO_POINTERS, STATE_SELECTION,
        "You're asked to support \"almost palindromes\" - strings valid after removing at most k characters (not just 0 or 1). Does the two-pointer state (just left and right indices) still suffice?",
        conceptKey = "two-pointers-state-insufficient-for-generalization", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "No - allowing up to k removals turns this into a problem needing to track how many removals have been used along each candidate path, which naturally leads toward dynamic programming or recursion with memoization rather than a simple two-pointer scan.",
                true,
                "The single-mismatch (\"Palindrome II\") case stays manageable with a small branch at the first mismatch, but generalizing to k removals means the number of possible decision paths grows combinatorially, which plain left/right pointer state can't represent - a genuine algorithmic limitation, not just an implementation detail.",
            ),
            choice(
                "Yes - just add a counter for how many mismatches have been allowed so far, and the two-pointer approach handles it unchanged otherwise.",
                false,
                "A simple counter doesn't capture *which* removals were made, and different removal choices along the way can lead to very different downstream comparisons - the problem's branching structure outgrows a simple two-pointer-plus-counter design.",
            ),
            choice(
                "No - allowing any removals at all makes the problem theoretically unsolvable.",
                false,
                "The generalized problem is very much solvable, just not with the same simple two-pointer technique - it calls for a different algorithmic tool (like dynamic programming) suited to its larger decision space.",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, BOUNDARY_UPDATE,
        "Consider heights = [] (empty array) or heights = [5] (a single value). Trace what the standard shorter-side-moves algorithm actually does, and whether it needs special-casing.",
        conceptKey = "two-pointers-degenerate-input-boundary", difficulty = ADVANCED,
        code = "var left = 0\nvar right = height.size - 1\nvar maxArea = 0\nwhile (left < right) {\n    val area = minOf(height[left], height[right]) * (right - left)\n    maxArea = maxOf(maxArea, area)\n    if (height[left] < height[right]) left++ else right--\n}\nreturn maxArea",
        choices = listOf(
            choice(
                "For both cases, left < right is false from the very start (0 < -1 for empty, 0 < 0 for one element), so the loop body never runs and maxArea correctly returns its initial value of 0 - no special-casing is actually needed.",
                true,
                "The loop's own condition already handles these degenerate inputs gracefully, returning the sensible answer of 0 (no container possible) without ever touching height[left] or height[right] in a way that would be out of bounds.",
            ),
            choice(
                "The empty case throws an index-out-of-bounds error trying to access height[right] when right = -1.",
                false,
                "right = height.size - 1 = -1 for an empty array, but the while loop's condition (left < right, i.e. 0 < -1) is checked *before* the body runs and is false, so height[right] is never actually accessed.",
            ),
            choice(
                "Both cases require an explicit early return to avoid incorrect behavior.",
                false,
                "The loop's natural condition already prevents its body from executing on either degenerate input - no explicit early-return special case is needed for correctness here.",
            ),
        ),
    ),
    step(
        "valid-palindrome", TWO_POINTERS, CODE_BLOCK,
        "Which snippet correctly and efficiently supports checking palindromes case-sensitively as an optional mode, without duplicating the entire function?",
        conceptKey = "two-pointers-configurable-case-sensitivity-code", difficulty = ADVANCED,
        code = "fun isPalindrome(s: String, caseSensitive: Boolean): Boolean {\n    var left = 0\n    var right = s.length - 1\n    while (left < right) {\n        while (left < right && !s[left].isLetterOrDigit()) left++\n        while (left < right && !s[right].isLetterOrDigit()) right--\n        // ???\n        left++\n        right--\n    }\n    return true\n}",
        choices = listOf(
            choice(
                "val a = if (caseSensitive) s[left] else s[left].lowercaseChar()\nval b = if (caseSensitive) s[right] else s[right].lowercaseChar()\nif (a != b) return false",
                true,
                "Branching only at the comparison step, based on the caseSensitive flag, reuses the entire skip-and-scan structure unchanged and adds the configurability exactly where it's needed, with no duplicated logic.",
                code = "val a = if (caseSensitive) s[left] else s[left].lowercaseChar()\nval b = if (caseSensitive) s[right] else s[right].lowercaseChar()\nif (a != b) return false",
            ),
            choice(
                "if (caseSensitive) { if (s[left] != s[right]) return false } else { return isPalindrome(s.lowercase(), false) }",
                false,
                "Recursing into a fresh call with a fully lowercased copy of the string abandons the current left/right progress and restarts the whole scan from the beginning, which is both wasteful and structurally awkward.",
                code = "if (caseSensitive) { if (s[left] != s[right]) return false } else { return isPalindrome(s.lowercase(), false) }",
            ),
            choice(
                "if (s.lowercaseChar() != s.lowercaseChar()) return false",
                false,
                "Calling lowercaseChar() on the whole string s rather than on the individual characters at left and right doesn't even compile as intended - the comparison needs to happen on the two specific characters currently being examined.",
                code = "if (s.lowercaseChar() != s.lowercaseChar()) return false",
            ),
        ),
        languageVariants = listOf(
            WorkoutCodeVariant(
                PYTHON,
                "def is_palindrome(s, case_sensitive):\n    left = 0\n    right = len(s) - 1\n    while left < right:\n        while left < right and not s[left].isalnum():\n            left += 1\n        while left < right and not s[right].isalnum():\n            right -= 1\n        # ???\n        left += 1\n        right -= 1\n    return True",
                choices = listOf(
                    choice("a = s[left] if case_sensitive else s[left].lower()\nb = s[right] if case_sensitive else s[right].lower()\nif a != b:\n    return False", true, "Branching only at the comparison step, based on the case_sensitive flag, reuses the entire skip-and-scan structure unchanged and adds the configurability exactly where it's needed, with no duplicated logic.", code = "a = s[left] if case_sensitive else s[left].lower()\nb = s[right] if case_sensitive else s[right].lower()\nif a != b:\n    return False"),
                    choice("if case_sensitive:\n    if s[left] != s[right]:\n        return False\nelse:\n    return is_palindrome(s.lower(), False)", false, "Recursing into a fresh call with a fully lowercased copy of the string abandons the current left/right progress and restarts the whole scan from the beginning, which is both wasteful and structurally awkward.", code = "if case_sensitive:\n    if s[left] != s[right]:\n        return False\nelse:\n    return is_palindrome(s.lower(), False)"),
                    choice("if s.lower() != s.lower():\n    return False", false, "Comparing the whole string s to itself rather than the individual characters at left and right is a vacuous, always-false condition - the comparison needs to happen on the two specific characters currently being examined.", code = "if s.lower() != s.lower():\n    return False"),
                ),
            ),
            WorkoutCodeVariant(
                JAVA,
                "boolean isPalindrome(String s, boolean caseSensitive) {\n    int left = 0;\n    int right = s.length() - 1;\n    while (left < right) {\n        while (left < right && !Character.isLetterOrDigit(s.charAt(left))) left++;\n        while (left < right && !Character.isLetterOrDigit(s.charAt(right))) right--;\n        // ???\n        left++;\n        right--;\n    }\n    return true;\n}",
                choices = listOf(
                    choice("char a = caseSensitive ? s.charAt(left) : Character.toLowerCase(s.charAt(left));\nchar b = caseSensitive ? s.charAt(right) : Character.toLowerCase(s.charAt(right));\nif (a != b) return false;", true, "Branching only at the comparison step, based on the caseSensitive flag, reuses the entire skip-and-scan structure unchanged and adds the configurability exactly where it's needed, with no duplicated logic.", code = "char a = caseSensitive ? s.charAt(left) : Character.toLowerCase(s.charAt(left));\nchar b = caseSensitive ? s.charAt(right) : Character.toLowerCase(s.charAt(right));\nif (a != b) return false;"),
                    choice("if (caseSensitive) { if (s.charAt(left) != s.charAt(right)) return false; } else { return isPalindrome(s.toLowerCase(), false); }", false, "Recursing into a fresh call with a fully lowercased copy of the string abandons the current left/right progress and restarts the whole scan from the beginning, which is both wasteful and structurally awkward.", code = "if (caseSensitive) { if (s.charAt(left) != s.charAt(right)) return false; } else { return isPalindrome(s.toLowerCase(), false); }"),
                    choice("if (s.toLowerCase() != s.toLowerCase()) return false;", false, "Comparing String references with != rather than the individual characters at left and right is both a reference-equality bug and a vacuous comparison - the comparison needs to happen on the two specific characters currently being examined.", code = "if (s.toLowerCase() != s.toLowerCase()) return false;"),
                ),
            ),
            WorkoutCodeVariant(
                JAVASCRIPT,
                "function isPalindrome(s, caseSensitive) {\n    let left = 0;\n    let right = s.length - 1;\n    while (left < right) {\n        while (left < right && !/[a-z0-9]/i.test(s[left])) left++;\n        while (left < right && !/[a-z0-9]/i.test(s[right])) right--;\n        // ???\n        left++;\n        right--;\n    }\n    return true;\n}",
                choices = listOf(
                    choice("const a = caseSensitive ? s[left] : s[left].toLowerCase();\nconst b = caseSensitive ? s[right] : s[right].toLowerCase();\nif (a !== b) return false;", true, "Branching only at the comparison step, based on the caseSensitive flag, reuses the entire skip-and-scan structure unchanged and adds the configurability exactly where it's needed, with no duplicated logic.", code = "const a = caseSensitive ? s[left] : s[left].toLowerCase();\nconst b = caseSensitive ? s[right] : s[right].toLowerCase();\nif (a !== b) return false;"),
                    choice("if (caseSensitive) { if (s[left] !== s[right]) return false; } else { return isPalindrome(s.toLowerCase(), false); }", false, "Recursing into a fresh call with a fully lowercased copy of the string abandons the current left/right progress and restarts the whole scan from the beginning, which is both wasteful and structurally awkward.", code = "if (caseSensitive) { if (s[left] !== s[right]) return false; } else { return isPalindrome(s.toLowerCase(), false); }"),
                    choice("if (s.toLowerCase() !== s.toLowerCase()) return false;", false, "Comparing the whole string s to itself rather than the individual characters at left and right is a vacuous, always-false condition - the comparison needs to happen on the two specific characters currently being examined.", code = "if (s.toLowerCase() !== s.toLowerCase()) return false;"),
                ),
            ),
            WorkoutCodeVariant(
                CPP,
                "bool isPalindrome(string s, bool caseSensitive) {\n    int left = 0;\n    int right = s.size() - 1;\n    while (left < right) {\n        while (left < right && !isalnum(s[left])) left++;\n        while (left < right && !isalnum(s[right])) right--;\n        // ???\n        left++;\n        right--;\n    }\n    return true;\n}",
                choices = listOf(
                    choice("char a = caseSensitive ? s[left] : tolower(s[left]);\nchar b = caseSensitive ? s[right] : tolower(s[right]);\nif (a != b) return false;", true, "Branching only at the comparison step, based on the caseSensitive flag, reuses the entire skip-and-scan structure unchanged and adds the configurability exactly where it's needed, with no duplicated logic.", code = "char a = caseSensitive ? s[left] : tolower(s[left]);\nchar b = caseSensitive ? s[right] : tolower(s[right]);\nif (a != b) return false;"),
                    choice("if (caseSensitive) { if (s[left] != s[right]) return false; } else { string lower = s; transform(lower.begin(), lower.end(), lower.begin(), ::tolower); return isPalindrome(lower, false); }", false, "Recursing into a fresh call with a fully lowercased copy of the string abandons the current left/right progress and restarts the whole scan from the beginning, which is both wasteful and structurally awkward.", code = "if (caseSensitive) { if (s[left] != s[right]) return false; } else { string lower = s; transform(lower.begin(), lower.end(), lower.begin(), ::tolower); return isPalindrome(lower, false); }"),
                    choice("if (tolower(s) != tolower(s)) return false;", false, "Calling tolower() on the whole string s rather than on the individual characters at left and right doesn't even compile - tolower expects a single character, not a std::string.", code = "if (tolower(s) != tolower(s)) return false;"),
                ),
            ),
            WorkoutCodeVariant(
                GO,
                "func isPalindrome(s string, caseSensitive bool) bool {\n    left := 0\n    right := len(s) - 1\n    for left < right {\n        for left < right && !isAlnum(s[left]) {\n            left++\n        }\n        for left < right && !isAlnum(s[right]) {\n            right--\n        }\n        // ???\n        left++\n        right--\n    }\n    return true\n}",
                choices = listOf(
                    choice("a := s[left]\nb := s[right]\nif !caseSensitive {\n    a = byte(unicode.ToLower(rune(a)))\n    b = byte(unicode.ToLower(rune(b)))\n}\nif a != b {\n    return false\n}", true, "Branching only at the comparison step, based on the caseSensitive flag, reuses the entire skip-and-scan structure unchanged and adds the configurability exactly where it's needed, with no duplicated logic.", code = "a := s[left]\nb := s[right]\nif !caseSensitive {\n    a = byte(unicode.ToLower(rune(a)))\n    b = byte(unicode.ToLower(rune(b)))\n}\nif a != b {\n    return false\n}"),
                    choice("if caseSensitive {\n    if s[left] != s[right] {\n        return false\n    }\n} else {\n    return isPalindrome(strings.ToLower(s), false)\n}", false, "Recursing into a fresh call with a fully lowercased copy of the string abandons the current left/right progress and restarts the whole scan from the beginning, which is both wasteful and structurally awkward.", code = "if caseSensitive {\n    if s[left] != s[right] {\n        return false\n    }\n} else {\n    return isPalindrome(strings.ToLower(s), false)\n}"),
                    choice("if strings.ToLower(s) != strings.ToLower(s) {\n    return false\n}", false, "Comparing the whole string s to itself rather than the individual characters at left and right is a vacuous, always-false condition - the comparison needs to happen on the two specific characters currently being examined.", code = "if strings.ToLower(s) != strings.ToLower(s) {\n    return false\n}"),
                ),
            ),
            WorkoutCodeVariant(
                SWIFT,
                "func isPalindrome(_ s: [Character], _ caseSensitive: Bool) -> Bool {\n    var left = 0\n    var right = s.count - 1\n    while left < right {\n        while left < right && !(s[left].isLetter || s[left].isNumber) { left += 1 }\n        while left < right && !(s[right].isLetter || s[right].isNumber) { right -= 1 }\n        // ???\n        left += 1\n        right -= 1\n    }\n    return true\n}",
                choices = listOf(
                    choice("let a = caseSensitive ? s[left] : Character(s[left].lowercased())\nlet b = caseSensitive ? s[right] : Character(s[right].lowercased())\nif a != b { return false }", true, "Branching only at the comparison step, based on the caseSensitive flag, reuses the entire skip-and-scan structure unchanged and adds the configurability exactly where it's needed, with no duplicated logic.", code = "let a = caseSensitive ? s[left] : Character(s[left].lowercased())\nlet b = caseSensitive ? s[right] : Character(s[right].lowercased())\nif a != b { return false }"),
                    choice("if caseSensitive {\n    if s[left] != s[right] { return false }\n} else {\n    return isPalindrome(s.map { Character($0.lowercased()) }, false)\n}", false, "Recursing into a fresh call with a fully lowercased copy of the array abandons the current left/right progress and restarts the whole scan from the beginning, which is both wasteful and structurally awkward.", code = "if caseSensitive {\n    if s[left] != s[right] { return false }\n} else {\n    return isPalindrome(s.map { Character($0.lowercased()) }, false)\n}"),
                    choice("if s.map({ $0.lowercased() }) != s.map({ $0.lowercased() }) { return false }", false, "Comparing the whole array to itself rather than the individual characters at left and right is a vacuous, always-false condition - the comparison needs to happen on the two specific characters currently being examined.", code = "if s.map({ $0.lowercased() }) != s.map({ $0.lowercased() }) { return false }"),
                ),
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, CODE_BLOCK,
        "Which snippet correctly and efficiently extends the algorithm to also return the pair of indices achieving the maximum area, not just the area itself, without changing its time complexity?",
        conceptKey = "two-pointers-return-indices-not-just-value", difficulty = ADVANCED,
        code = "var left = 0\nvar right = height.size - 1\nvar maxArea = 0\nvar bestLeft = 0\nvar bestRight = height.size - 1\nwhile (left < right) {\n    val area = minOf(height[left], height[right]) * (right - left)\n    // ???\n    if (height[left] < height[right]) left++ else right--\n}",
        choices = listOf(
            choice(
                "if (area > maxArea) { maxArea = area; bestLeft = left; bestRight = right }",
                true,
                "Recording the current left and right alongside maxArea, updated together in the same comparison, adds only O(1) work per step and requires no second pass over the array to recover which indices produced the best area.",
                code = "if (area > maxArea) { maxArea = area; bestLeft = left; bestRight = right }",
            ),
            choice(
                "maxArea = maxOf(maxArea, area)\nif (maxArea == area) { bestLeft = height.indexOf(height[left]); bestRight = height.indexOf(height[right]) }",
                false,
                "Using indexOf to relocate the indices afterward is both redundant (left and right are already known at this point) and unreliable if the same height value appears elsewhere in the array, since indexOf would find the wrong occurrence.",
                code = "maxArea = maxOf(maxArea, area)\nif (maxArea == area) { bestLeft = height.indexOf(height[left]); bestRight = height.indexOf(height[right]) }",
            ),
            choice(
                "maxArea = maxOf(maxArea, area)\nbestLeft = left\nbestRight = right",
                false,
                "Updating bestLeft and bestRight unconditionally on every step, not just when a new maximum is actually found, would leave them pointing at whichever pair was checked last, not the pair that produced the true maximum area.",
                code = "maxArea = maxOf(maxArea, area)\nbestLeft = left\nbestRight = right",
            ),
        ),
    ),
    step(
        "container-with-most-water", TWO_POINTERS, TIME_COMPLEXITY,
        "A teammate argues that since the greedy shorter-side-moves rule requires a correctness proof (it's not \"obviously\" right the way brute force is), its true time complexity should account for that proof's complexity too. Address this framing.",
        conceptKey = "two-pointers-proof-complexity-vs-runtime-complexity", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "This conflates two unrelated things - the difficulty of *proving* an algorithm correct (a one-time, offline reasoning effort) has no bearing on the *runtime* complexity of executing it, which is purely about how the algorithm's own operations scale with input size.",
                true,
                "How hard an algorithm is to convince yourself is correct is a property of the algorithm's design and the reasoning needed to trust it - it's a completely separate axis from how its execution time scales with n, which is what time complexity actually measures.",
            ),
            choice(
                "The teammate is right - algorithms requiring non-obvious correctness proofs are inherently slower than those that don't.",
                false,
                "There's no mechanical relationship between how subtle an algorithm's correctness argument is and how fast it runs - a greedy algorithm with a clever proof can easily be faster than a naive one with an obvious proof.",
            ),
            choice(
                "The teammate is right, but only for algorithms proven correct by exchange arguments specifically.",
                false,
                "The type of correctness argument used (exchange argument, induction, or otherwise) doesn't factor into the algorithm's runtime complexity - these remain entirely separate concerns regardless of proof technique.",
            ),
        ),
    ),
    step(
        "3sum", TWO_POINTERS, SPACE_COMPLEXITY,
        "A teammate wants to avoid mutating (sorting) the input array, since the caller might rely on the original order elsewhere. What's the real cost of preserving the original order?",
        conceptKey = "two-pointers-immutability-cost-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Sorting a copy instead of the original array adds O(n) extra space specifically to preserve the caller's ordering guarantee - a real, deliberate tradeoff between the algorithm's own space usage and not producing a side effect on shared, external state.",
                true,
                "Whether mutating the caller's array is acceptable is a design decision with a genuine cost either way - avoiding the side effect isn't free, it specifically costs the extra O(n) space needed to hold a sorted copy instead of sorting in place.",
            ),
            choice(
                "There's no real cost - copying the array before sorting has no effect on space complexity.",
                false,
                "A copy of the array is itself O(n) additional space beyond what in-place sorting would need - this is a genuine increase in space usage, not a cost-free choice.",
            ),
            choice(
                "The cost is only in time, not space, since copying an array is a linear-time operation.",
                false,
                "Copying does take linear time, but it also allocates a whole new array's worth of memory - the cost shows up in both dimensions, not just time.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Sliding Window
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, PATTERN_RECOGNITION,
        "Now the array can contain negative numbers as well as positive ones. Does the sliding window approach still correctly apply, or does this expose a real limitation of the technique?",
        conceptKey = "sliding-window-negative-values-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It breaks down - the window's correctness relies on the running sum growing monotonically as the window expands and shrinking monotonically as it contracts, which negative values violate, since adding an element could now *decrease* the sum instead of increasing it.",
                true,
                "The entire shrink-while-qualifying logic depends on knowing that removing an element always decreases the sum and adding one always increases it - with negative numbers, that guarantee disappears, and a different technique (like prefix sums with more careful handling) is needed instead.",
            ),
            choice(
                "It still works identically, since the window mechanics don't depend on the sign of the values.",
                false,
                "The window's shrink decision specifically relies on the sum only ever growing as it expands and only ever shrinking as it contracts - negative values break that monotonic relationship, undermining the core assumption.",
            ),
            choice(
                "It still works, but only if the array has more positive values than negative ones.",
                false,
                "The relative count of positive versus negative values doesn't restore the monotonic sum property that the window relies on - even a single negative value can break the assumption in a way that produces incorrect results.",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, PATTERN_RECOGNITION,
        "Now you're asked for the longest substring with *at most two* distinct characters, not zero repeats. Does the same window-and-set structure generalize, and what has to change?",
        conceptKey = "sliding-window-negative-values-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It generalizes by swapping the plain set for a count map (character to frequency) and changing the violation condition from \"character already present\" to \"more than two distinct characters currently in the window\" - the grow-and-shrink mechanics stay the same.",
                true,
                "The core sliding-window shape (grow from the right, shrink from the left on violation) is reusable for a whole family of related problems - what changes is specifically what counts as a violation and what state is needed to detect it.",
            ),
            choice(
                "It doesn't generalize - allowing repeated characters requires abandoning the sliding window entirely.",
                false,
                "Allowing a bounded number of distinct characters is still an incrementally checkable condition as the window grows, which is exactly the kind of condition a sliding window can track - the technique still applies.",
            ),
            choice(
                "It generalizes, but only by tracking window length instead of character identity.",
                false,
                "Window length alone can't determine whether a distinct-character-count constraint is satisfied - the window specifically needs to track which characters (and how many of each) are currently inside it.",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, APPROACH,
        "Now up to two transactions are allowed (buy-sell-buy-sell, non-overlapping), not just one. Does the single running-minimum approach extend, or does this reveal a real limitation?",
        conceptKey = "sliding-window-single-vs-multi-transaction-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It reveals a real limitation - one running minimum and one running profit only capture a single buy-sell cycle; two transactions require tracking multiple interacting states (e.g. profit after one transaction, then a new minimum cost basis incorporating that profit for a second transaction), which is a genuinely different, more stateful algorithm.",
                true,
                "The one-transaction version's simplicity comes specifically from only needing to remember one thing at a time - allowing a second, non-overlapping transaction means the state has to capture 'best result so far, from which a second cycle could still begin,' which a single running minimum can't represent.",
            ),
            choice(
                "It extends trivially - just run the same single-transaction algorithm twice on the same array and add the two profits.",
                false,
                "Running the algorithm twice independently could double-count overlapping days or fail to find the true optimal split point between two non-overlapping transactions - the two transactions need to be reasoned about jointly, not as two separate, independent problems.",
            ),
            choice(
                "It extends trivially - just track two running minimums instead of one.",
                false,
                "Two independent running minimums don't capture the necessary relationship between the first transaction's profit and the second transaction's effective starting cost - the state needed is more interconnected than that.",
            ),
        ),
    ),
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, APPROACH,
        "The array can now be up to 10^9 elements, streamed rather than held in memory, and target can change between queries on the same stream. What does this limitation expose about the standard sliding-window approach?",
        conceptKey = "sliding-window-streaming-multi-query-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "The window mechanics themselves are naturally streaming-friendly (they never look backward beyond the current window), but re-running the whole scan from scratch for every new target value defeats the point of streaming - a genuinely different design would need to answer multiple targets without full re-scans.",
                true,
                "A sliding window processes each element once and never revisits earlier ones, which is compatible with a true stream - the real tension is that each *new target* currently forces a brand-new pass, which isn't a limitation of the window shape itself but of running it repeatedly from scratch.",
            ),
            choice(
                "The sliding window approach is fundamentally incompatible with streaming data and would need to be entirely replaced.",
                false,
                "The window's core behavior, only ever looking at a contiguous, forward-moving range, is actually well-suited to a single streaming pass - the real complication is handling multiple different target queries efficiently, not the streaming itself.",
            ),
            choice(
                "This limitation only matters if target is allowed to be negative.",
                false,
                "The sign of target isn't the relevant constraint here - the actual complication is about re-scanning a massive stream for each new query, which is a scale-and-repetition problem, not a sign problem.",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, STATE_SELECTION,
        "Now a transaction fee is charged on every sale, and unlimited transactions are allowed. What state does profit tracking now need that the original single-transaction version didn't?",
        conceptKey = "sliding-window-fee-adjusted-state-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "The algorithm needs to track two running quantities simultaneously - the best profit if currently holding a share, and the best profit if currently not holding one - since the fee changes the calculus of when re-buying is worthwhile, which a single running minimum can no longer capture alone.",
                true,
                "Once fees and unlimited transactions are both in play, the decision of whether to sell-then-rebuy or hold through a dip depends on more than just 'is this the lowest price so far' - it needs its own held/not-held state machine, a genuinely richer piece of state than one running minimum.",
            ),
            choice(
                "Nothing new is needed - the running minimum and running profit variables from the single-transaction version still suffice.",
                false,
                "With unlimited transactions and a per-sale fee, a single running minimum can't account for the tradeoff between holding through a temporary dip versus selling and re-buying (and paying another fee) - richer state is genuinely required.",
            ),
            choice(
                "Only the fee amount itself needs to be tracked as one additional running variable.",
                false,
                "The fee's value is a fixed input, not something that needs its own running state - what's actually missing is state representing whether a share is currently being held, which interacts with the fee on every potential sale.",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, BOUNDARY_UPDATE,
        "The generalized \"at most k distinct characters\" version shrinks by removing s[left] and decrementing its count, but forgets to remove the character from the map entirely when its count hits zero. Does this actually cause incorrect results?",
        conceptKey = "sliding-window-zero-count-cleanup-edge-case", difficulty = ADVANCED,
        code = "val counts = HashMap<Char, Int>()\n// on shrink: counts[s[left]] = counts[s[left]]!! - 1\n// distinct count check uses: counts.size",
        choices = listOf(
            choice(
                "Yes - leaving a zero-count entry in the map means counts.size overcounts the number of *currently present* distinct characters, since a character that's been fully removed still occupies a slot with value 0.",
                true,
                "The distinct-character check relies on counts.size accurately reflecting what's truly still in the window - a lingering zero-count entry inflates that number, making the window seem to violate the constraint (or stay valid) incorrectly.",
            ),
            choice(
                "No - a count of zero is harmless since it doesn't affect any character comparisons.",
                false,
                "The bug isn't about comparing characters directly, it's that counts.size (used to check the distinct-character limit) counts every key in the map regardless of its value, including now-irrelevant zero entries.",
            ),
            choice(
                "No - this only matters if the same character is removed and re-added many times in a row.",
                false,
                "The problem shows up the very first time any character's count reaches zero, not specifically after repeated add/remove cycles - a single lingering zero entry is enough to throw off the size-based check.",
            ),
        ),
    ),
    step(
        "minimum-size-subarray-sum", SLIDING_WINDOW, CODE_BLOCK,
        "Which snippet correctly and efficiently extends the window approach to also return the actual subarray (not just its length), without changing the algorithm's time complexity?",
        conceptKey = "sliding-window-return-subarray-not-length", difficulty = ADVANCED,
        code = "var left = 0\nvar sum = 0\nvar minLen = Int.MAX_VALUE\nvar bestStart = 0\nfor (right in nums.indices) {\n    sum += nums[right]\n    while (sum >= target) {\n        // ???\n        sum -= nums[left]\n        left++\n    }\n}\nreturn if (minLen == Int.MAX_VALUE) intArrayOf() else nums.copyOfRange(bestStart, bestStart + minLen)",
        choices = listOf(
            choice(
                "if (right - left + 1 < minLen) { minLen = right - left + 1; bestStart = left }",
                true,
                "Recording bestStart alongside minLen, updated together whenever a shorter window is found, adds only O(1) work per check - the final copyOfRange then extracts the actual subarray without needing any additional passes over the array.",
                code = "if (right - left + 1 < minLen) { minLen = right - left + 1; bestStart = left }",
            ),
            choice(
                "minLen = minOf(minLen, right - left + 1)\nbestStart = nums.indexOf(nums[left])",
                false,
                "Using indexOf to relocate left's value afterward is both redundant (left is already known) and incorrect if that value repeats earlier in the array, since indexOf would find the wrong occurrence.",
                code = "minLen = minOf(minLen, right - left + 1)\nbestStart = nums.indexOf(nums[left])",
            ),
            choice(
                "minLen = minOf(minLen, right - left + 1)\nbestStart = right - minLen + 1",
                false,
                "Deriving bestStart from the *overall* minLen instead of the *current* window's own left position can produce a start index belonging to a different window than the one whose length was just measured, especially once a shorter window is later found elsewhere.",
                code = "minLen = minOf(minLen, right - left + 1)\nbestStart = right - minLen + 1",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, CODE_BLOCK,
        "Which snippet correctly and efficiently extends the algorithm to also return which day to buy and which day to sell, not just the profit, without changing its time complexity?",
        conceptKey = "sliding-window-return-days-not-just-profit", difficulty = ADVANCED,
        code = "var minPrice = Int.MAX_VALUE\nvar minPriceDay = 0\nvar maxProfit = 0\nvar buyDay = 0\nvar sellDay = 0\nfor (day in prices.indices) {\n    val price = prices[day]\n    // ???\n}",
        choices = listOf(
            choice(
                "if (price - minPrice > maxProfit) { maxProfit = price - minPrice; buyDay = minPriceDay; sellDay = day }\nif (price < minPrice) { minPrice = price; minPriceDay = day }",
                true,
                "Recording buyDay and sellDay only when a strictly better profit is found, using the minimum's *own* recorded day rather than the current day, correctly tracks which specific days produced the best profit in a single pass.",
                code = "if (price - minPrice > maxProfit) { maxProfit = price - minPrice; buyDay = minPriceDay; sellDay = day }\nif (price < minPrice) { minPrice = price; minPriceDay = day }",
            ),
            choice(
                "if (price < minPrice) { minPrice = price; minPriceDay = day }\nif (price - minPrice > maxProfit) { maxProfit = price - minPrice; buyDay = minPriceDay; sellDay = day }",
                false,
                "Updating the minimum before checking profit means a new minimum day can be immediately compared against itself as both buy and sell day, producing a zero-profit 'best' pair on the very day the minimum updates.",
                code = "if (price < minPrice) { minPrice = price; minPriceDay = day }\nif (price - minPrice > maxProfit) { maxProfit = price - minPrice; buyDay = minPriceDay; sellDay = day }",
            ),
            choice(
                "if (price - minPrice > maxProfit) { maxProfit = price - minPrice; buyDay = day; sellDay = day }\nif (price < minPrice) { minPrice = price; minPriceDay = day }",
                false,
                "Setting buyDay to the current day instead of minPriceDay records the wrong purchase day entirely - the buy day should be wherever the running minimum price actually occurred, not wherever the profit was just measured.",
                code = "if (price - minPrice > maxProfit) { maxProfit = price - minPrice; buyDay = day; sellDay = day }\nif (price < minPrice) { minPrice = price; minPriceDay = day }",
            ),
        ),
    ),
    step(
        "best-time-to-buy-and-sell-stock", SLIDING_WINDOW, TIME_COMPLEXITY,
        "A teammate claims that since the two-transaction and fee-based variants require more state, they must also have worse time complexity than the original O(n). Is more state the same thing as more time?",
        conceptKey = "sliding-window-state-count-vs-time-complexity", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Not necessarily - as long as the extra state variables are each updated in O(1) per element and the array is still scanned once, the algorithm remains O(n) overall; more state changes what's tracked per step, not how many steps or passes are needed.",
                true,
                "Tracking two or three running values instead of one doesn't inherently add more passes over the input - if each variable still updates in constant time per element, the total time complexity can stay exactly the same class, just with a larger constant factor.",
            ),
            choice(
                "Yes - any increase in the number of tracked state variables necessarily increases the time complexity class.",
                false,
                "The number of variables tracked and the number of times the input is scanned are independent concerns - more state can absolutely coexist with the same O(n) complexity, as long as updates stay constant-time per element.",
            ),
            choice(
                "Yes, but only because the fee-based variant specifically requires re-scanning the array twice.",
                false,
                "A well-designed fee-based, multi-transaction version can still be solved in a single forward pass, tracking the extra held/not-held state as it goes, without needing a second scan.",
            ),
        ),
    ),
    step(
        "longest-substring-without-repeating-characters", SLIDING_WINDOW, SPACE_COMPLEXITY,
        "A teammate argues that since the window's space bound is O(min(n, charset size)), for a huge charset (like full Unicode) it's effectively O(n) anyway, so the distinction \"doesn't matter in practice.\" Evaluate this.",
        conceptKey = "sliding-window-charset-bound-practical-significance", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It depends entirely on the actual input - for a genuinely huge, diverse charset and a string at least as long, the bound does collapse toward O(n) in practice, but for the much more common case of a small working alphabet (like ASCII text), the tighter min(n, charset) bound is a real, meaningful guarantee, not a technicality.",
                true,
                "The tighter bound isn't just theoretical pedantry - whether it 'matters in practice' depends on the actual character set of realistic inputs, which for most text processing is far smaller than n, making the distinction genuinely load-bearing rather than academic.",
            ),
            choice(
                "The teammate is entirely correct - the min(n, charset) bound is purely a theoretical nicety with no practical significance in any case.",
                false,
                "For inputs drawn from a small alphabet (which describes a large share of real text-processing use cases), the charset-size bound is meaningfully smaller than n and does have real practical significance, not just theoretical interest.",
            ),
            choice(
                "The teammate is entirely wrong - the space bound is always exactly the charset size, never n, regardless of input.",
                false,
                "For a short string with a huge possible charset, the window can never hold more than the string's own length worth of distinct characters - the bound is a minimum of the two, not always pinned to the charset size alone.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Stack
    step(
        "valid-parentheses", STACK, PATTERN_RECOGNITION,
        "Now the string can also contain other paired delimiters that aren't strictly nested, like XML-style tags that can legitimately overlap in some contexts. Does the plain stack-matching approach still directly apply?",
        conceptKey = "stack-non-strict-nesting-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Not without modification - the stack approach specifically assumes strict, well-nested structure (last-opened, first-closed); a format that allows legitimate overlap needs a fundamentally different validity rule, since a single stack can't represent two independently-tracked, interleaved regions.",
                true,
                "The whole reason a stack works for standard bracket matching is the guarantee that nesting is strict - once that guarantee is relaxed, the very data structure that made the problem tractable no longer models the allowed structure correctly.",
            ),
            choice(
                "Yes - a stack handles any bracket-like structure, nested or overlapping, without modification.",
                false,
                "A stack's last-in-first-out behavior specifically encodes strict nesting - genuinely overlapping, non-nested structures violate that assumption and aren't correctly validated by an unmodified single-stack approach.",
            ),
            choice(
                "Yes, as long as a second stack is added for the overlapping delimiters, with no other changes needed.",
                false,
                "Simply adding a second stack doesn't by itself define what makes overlapping delimiters valid or invalid - the actual validity rules for allowed overlap would need to be worked out first, which changes the algorithm's logic, not just its data structures.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, PATTERN_RECOGNITION,
        "Now you're asked for the *k-th* next warmer day for each day, not just the first. Does the single monotonic stack still directly solve this, or does the limitation of tracking only \"the next one\" show up?",
        conceptKey = "stack-non-strict-nesting-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "A single stack tracking only the immediately next warmer day doesn't directly generalize - finding the k-th such day for every position needs either k separate passes (each finding the \"next warmer\" relative to the previous result) or a different structure entirely that can answer multi-step queries efficiently.",
                true,
                "The monotonic stack's elegance comes from resolving exactly one relationship (the very next warmer day) per element - extending that to a k-th relationship loses the simple one-pass structure and needs a genuinely different approach to stay efficient.",
            ),
            choice(
                "Yes - the same single stack directly returns the k-th warmer day with no modification, since it processes days in order anyway.",
                false,
                "The stack as designed only ever resolves a day against the *first* subsequent day taller than it - it discards the information needed to continue past that point, so it can't directly report a k-th relationship.",
            ),
            choice(
                "Yes, as long as k is replaced with 1 in the comparison logic.",
                false,
                "Changing a comparison's threshold doesn't restore information the stack has already discarded (like which earlier waiting days were resolved and in what order) - a structural change is needed, not just a parameter tweak.",
            ),
        ),
    ),
    step(
        "min-stack", STACK, APPROACH,
        "The interviewer now asks for getMax() as well as getMin(), both in O(1), on the same stack. Does the existing parallel-min-stack design extend cleanly, and at what cost?",
        conceptKey = "stack-dual-extremum-tracking-tradeoff", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It extends cleanly by adding a second parallel max-stack alongside the existing min-stack, tracking the running maximum the same way - correct and straightforward, but it triples the total memory used (main stack plus two auxiliary stacks) compared to tracking just the minimum alone.",
                true,
                "The min-stack trick generalizes symmetrically to a max-stack with no new algorithmic ideas needed - the real cost worth naming explicitly is the extra O(n) space for the second auxiliary stack, not any added conceptual complexity.",
            ),
            choice(
                "It doesn't extend - a single stack design can only ever support one tracked extremum (min or max), never both simultaneously.",
                false,
                "There's nothing preventing two independent parallel stacks, one for the running minimum and one for the running maximum, from coexisting alongside the same main stack - both can be maintained together.",
            ),
            choice(
                "It extends cleanly with no additional space cost, since the same min-stack values can be reinterpreted as max values.",
                false,
                "A value that's the running minimum at some point in the stack's history isn't generally also the running maximum at that same point - a genuinely separate structure is needed to track the maximum, which does cost additional space.",
            ),
        ),
    ),
    step(
        "valid-parentheses", STACK, APPROACH,
        "You're told the input can be adversarially crafted to maximize stack depth (e.g. a million nested opening brackets with no closes). What real limitation does this expose about a naive recursive implementation, versus the iterative stack version?",
        conceptKey = "stack-recursion-depth-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "A recursive implementation that processes one character per recursive call would build up a call stack proportional to the nesting depth, risking a stack overflow on adversarial input, while the iterative version's explicit ArrayDeque-based stack lives on the heap and isn't bound by the language runtime's call-stack size limit.",
                true,
                "This is exactly the kind of limitation that separates 'works on typical input' from 'is robust to adversarial input' - a call-stack-based recursive design has a hard limit that an explicit, heap-allocated stack structure doesn't share.",
            ),
            choice(
                "There's no meaningful difference - both approaches have identical depth limitations regardless of implementation.",
                false,
                "The language runtime's call stack has a comparatively small, fixed size limit, while an explicit data structure like ArrayDeque can grow much larger, bounded mainly by available heap memory - these are genuinely different limits.",
            ),
            choice(
                "The iterative version has the same limitation, since ArrayDeque internally uses recursion to resize.",
                false,
                "ArrayDeque's internal resizing is an iterative array-copy operation, not a recursive one - it doesn't add call-stack depth as more elements are pushed, unlike a character-per-recursive-call design would.",
            ),
        ),
    ),
    step(
        "min-stack", STACK, STATE_SELECTION,
        "You're now asked to support removing an *arbitrary* element (not just the top) while still maintaining O(1) getMin. Does the parallel min-stack design accommodate this, or does it reveal a genuine limitation?",
        conceptKey = "stack-arbitrary-removal-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It reveals a genuine limitation - a stack's whole contract is last-in-first-out access, so removing from the middle isn't a stack operation at all; supporting arbitrary removal while keeping O(1) min queries needs a fundamentally different structure, like a balanced tree or a doubly-linked list paired with a multiset of values.",
                true,
                "The min-stack trick specifically relies on push and pop happening in a strict LIFO order to keep the two stacks synchronized - arbitrary removal breaks that synchronization assumption entirely, which isn't a bug to patch but a sign the underlying data structure choice itself needs to change.",
            ),
            choice(
                "It accommodates this cleanly - just call remove on the ArrayDeque at the appropriate position in both stacks.",
                false,
                "Removing from the middle of the main stack desynchronizes it from the min-stack, whose entries were only ever valid under the assumption that pushes and pops happen strictly at the top - the min-stack's values wouldn't correctly reflect the remaining elements.",
            ),
            choice(
                "It accommodates this as long as getMin is changed to rescan the whole stack instead of reading the min-stack's top.",
                false,
                "Rescanning avoids the desync problem but abandons O(1) getMin entirely, turning it into O(n) - the question specifically asks whether O(1) getMin can be preserved, which this doesn't achieve.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, BOUNDARY_UPDATE,
        "Now temperatures can be equal, and \"warmer\" should include \"the next day at least as warm, but only if it's not immediately followed by an even warmer day within the same run.\" How does this genuinely ambiguous rule interact with the existing strict `>` comparison?",
        conceptKey = "stack-ambiguous-tie-rule-interpretation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "This rule is underspecified as stated - before touching the code, it needs a precise, unambiguous definition (e.g. does \"immediately followed\" mean the very next day, or any later day in an unbroken equal-temperature run?) since different reasonable interpretations would require different comparison operators and possibly different state entirely.",
                true,
                "Recognizing when a requirement is genuinely ambiguous, rather than just assuming one interpretation and coding it, is itself part of engineering judgment - this rule as written doesn't map cleanly onto a single unambiguous comparison change.",
            ),
            choice(
                "The fix is straightforward: just change `>` to `>=` everywhere in the comparison.",
                false,
                "Blindly switching to >= doesn't capture the added \"but only if not immediately followed by an even warmer day\" qualifier at all - the rule has more nuance than a single operator change can express.",
            ),
            choice(
                "The rule has no actual effect on the algorithm, since equal temperatures were already being handled correctly.",
                false,
                "The original algorithm's strict > comparison specifically does *not* resolve equal-temperature days against each other - this new rule is asking for meaningfully different behavior, not a restatement of existing behavior.",
            ),
        ),
    ),
    step(
        "valid-parentheses", STACK, CODE_BLOCK,
        "Which snippet correctly and efficiently supports checking validity for a string that may be gigabytes in size, without holding the entire string in memory at once?",
        conceptKey = "stack-streaming-input-code", difficulty = ADVANCED,
        code = "// input arrives as a sequence of Char via a streaming reader, not a full String\nfun isValidStreaming(charSource: Iterator<Char>): Boolean {\n    // ???\n}",
        choices = listOf(
            choice(
                "val stack = ArrayDeque<Char>()\nval pairs = mapOf(')' to '(', ']' to '[', '}' to '{')\nwhile (charSource.hasNext()) {\n    val c = charSource.next()\n    if (c in \"([{\") stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }\n}\nreturn stack.isEmpty()",
                true,
                "The stack-based algorithm never actually needed random access to the whole string at once - it only ever looks at one character at a time and the current stack top, which makes it naturally streaming-compatible with no restructuring beyond swapping the iteration source.",
                code = "val stack = ArrayDeque<Char>()\nval pairs = mapOf(')' to '(', ']' to '[', '}' to '{')\nwhile (charSource.hasNext()) {\n    val c = charSource.next()\n    if (c in \"([{\") stack.addLast(c) else { if (stack.isEmpty() || stack.removeLast() != pairs[c]) return false }\n}\nreturn stack.isEmpty()",
            ),
            choice(
                "val allChars = charSource.asSequence().toList()\nreturn isValid(allChars.joinToString(\"\"))",
                false,
                "Collecting the entire stream into a list before processing defeats the purpose of streaming entirely, since it still requires holding the whole input in memory at once, exactly what needed to be avoided.",
                code = "val allChars = charSource.asSequence().toList()\nreturn isValid(allChars.joinToString(\"\"))",
            ),
            choice(
                "val stack = ArrayDeque<Char>()\nwhile (charSource.hasNext()) stack.addLast(charSource.next())\nreturn stack.size % 2 == 0",
                false,
                "Just checking whether the total character count is even doesn't verify anything about matching types or correct nesting order - it would accept clearly invalid strings like \"))((\" as long as the length happens to be even.",
                code = "val stack = ArrayDeque<Char>()\nwhile (charSource.hasNext()) stack.addLast(charSource.next())\nreturn stack.size % 2 == 0",
            ),
        ),
    ),
    step(
        "min-stack", STACK, CODE_BLOCK,
        "Which snippet correctly reduces the min-stack's memory from O(n) to O(1) extra by storing only the *difference* from the previous minimum instead of a full running minimum per entry, while still supporting O(1) getMin?",
        conceptKey = "stack-delta-encoding-space-optimization-code", difficulty = ADVANCED,
        code = "val stack = ArrayDeque<Long>()  // stores either the raw value, or an encoded delta\nvar min = Long.MAX_VALUE\nfun push(v: Long) {\n    // ???\n}",
        choices = listOf(
            choice(
                "if (stack.isEmpty()) { stack.addLast(v); min = v } else { stack.addLast(v - min); if (v < min) min = v }",
                true,
                "Storing v - min (which is negative exactly when v becomes the new minimum) lets a single running min variable, updated alongside each push, recover both the original value and the minimum at every level during pop - the min-stack's whole array is eliminated in exchange for this bookkeeping.",
                code = "if (stack.isEmpty()) { stack.addLast(v); min = v } else { stack.addLast(v - min); if (v < min) min = v }",
            ),
            choice(
                "stack.addLast(v)\nmin = minOf(min, v)",
                false,
                "This still only tracks a single overwritten running minimum with no history - it's the earlier flawed single-variable design that can't recover a prior minimum once popped, not the delta-encoding trick being asked for.",
                code = "stack.addLast(v)\nmin = minOf(min, v)",
            ),
            choice(
                "stack.addLast(v - min)",
                false,
                "Storing the delta without ever updating min when v becomes a new minimum means every subsequent delta would be computed against a stale minimum, corrupting the encoding for the rest of the stack's lifetime.",
                code = "stack.addLast(v - min)",
            ),
        ),
    ),
    step(
        "min-stack", STACK, TIME_COMPLEXITY,
        "A teammate argues that once getMax is added alongside getMin (each with their own parallel stack), the *time* complexity of push and pop must increase, since more bookkeeping happens per call. Evaluate this claim precisely.",
        conceptKey = "stack-dual-extremum-time-complexity", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "The claim conflates a larger constant factor with a change in complexity class - pushing to two auxiliary stacks instead of one still means each operation does a fixed, input-size-independent amount of work, so push and pop both remain O(1), just with a bigger constant.",
                true,
                "Adding more O(1) work per call (a second stack push alongside the first) doesn't change the complexity class, since neither stack's per-operation cost grows with n - it's still a fixed amount of work per call, exactly the definition of O(1).",
            ),
            choice(
                "The claim is correct - doubling the auxiliary bookkeeping doubles the complexity class from O(1) to O(2), which behaves differently at scale.",
                false,
                "O(1) and O(2) describe the exact same complexity class under Big-O notation, which discards constant multipliers - doing twice as much *constant* work per call doesn't produce different scaling behavior as n grows.",
            ),
            choice(
                "The claim is correct, but only for pop, since it has to check and potentially update both auxiliary stacks.",
                false,
                "Both push and pop touch a small, fixed number of stacks (the main one plus however many auxiliary ones) per call, regardless of overall stack size - neither operation's complexity class changes, pop included.",
            ),
        ),
    ),
    step(
        "daily-temperatures", STACK, SPACE_COMPLEXITY,
        "A teammate proposes processing the temperatures in reverse order specifically to reduce the stack's worst-case space usage. Does reversing the traversal direction actually change the worst-case space bound?",
        conceptKey = "stack-traversal-direction-space-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "No - a symmetric worst case still exists in reverse (a strictly *increasing* sequence, rather than decreasing, would now leave every day unresolved on the stack), so the worst-case space bound remains O(n) regardless of which direction the scan runs.",
                true,
                "Reversing the direction just relabels which specific input pattern becomes the worst case - it doesn't eliminate the existence of *some* pattern that leaves every element unresolved on the stack, so the O(n) worst-case bound is unavoidable either way.",
            ),
            choice(
                "Yes - processing in reverse always reduces the worst-case space to O(log n).",
                false,
                "There's no mechanism by which simply reversing traversal direction would produce a logarithmic bound - the stack can still grow to hold every element in some adversarial input, regardless of scan direction.",
            ),
            choice(
                "Yes - processing in reverse eliminates the need for a stack entirely.",
                false,
                "The underlying problem (resolving each day against the nearest day satisfying a condition) still needs some way to track unresolved days regardless of direction - a stack or equivalent structure remains necessary.",
            ),
        ),
    ),

    // ------------------------------------------------------------ Binary Search
    step(
        "binary-search", BINARY_SEARCH, PATTERN_RECOGNITION,
        "Now the array is sorted but so large it can't fit in memory - only random-access reads at a given index are available, each with real latency (like a network call). Does binary search's O(log n) guarantee still translate to \"fast\" here?",
        conceptKey = "binary-search-io-latency-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "The number of comparisons is still O(log n), which is genuinely few, but if each comparison now costs real, possibly significant latency (a network round trip) rather than a cheap memory access, the *wall-clock* time can still be substantial - the algorithmic complexity and the real-world cost model have diverged.",
                true,
                "O(log n) describes how many *operations* are needed, not how expensive each operation is - when the cost per operation changes dramatically (from a memory access to a network round trip), the same complexity class can translate to very different real-world performance.",
            ),
            choice(
                "Yes - O(log n) guarantees fast performance in absolute terms regardless of what each comparison costs.",
                false,
                "Complexity analysis describes scaling behavior, not absolute wall-clock time - if each individual operation becomes far more expensive, the total real-world time changes even though the number of operations (and thus the complexity class) stays the same.",
            ),
            choice(
                "No - binary search becomes entirely unusable once random access has any latency at all.",
                false,
                "Binary search remains usable and still minimizes the *number* of expensive accesses needed compared to a linear scan - it's still the right algorithmic choice, the nuance is just that 'fast' now has a different real-world meaning.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, PATTERN_RECOGNITION,
        "Now duplicate values are allowed in the rotated array (e.g. [2, 2, 2, 0, 1]). Does the standard mid-vs-high comparison still reliably identify which half holds the rotation point?",
        conceptKey = "binary-search-io-latency-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "No - when nums[mid] equals nums[high], the comparison can no longer tell which side the rotation point is on, since both configurations (rotation point to the left or to the right of mid) are consistent with that tie; the only safe fallback is to shrink high by one and continue, which degrades the worst-case time to O(n).",
                true,
                "The comparison's power comes from strict inequality reliably revealing structure - a tie removes that signal entirely, forcing a conservative, one-step-at-a-time fallback that sacrifices the logarithmic guarantee in the worst case (e.g. an array of all-equal values with one rotation).",
            ),
            choice(
                "Yes - the comparison works identically whether or not duplicates are present.",
                false,
                "A tie between nums[mid] and nums[high] specifically removes the information the comparison relies on to determine which half is sorted - this is a genuine complication that duplicate-free arrays never present.",
            ),
            choice(
                "No - duplicates make the array's minimum undefined, since multiple positions could hold the same minimal value.",
                false,
                "The minimum value is still well-defined even with duplicates (the smallest value present, regardless of how many times it repeats) - the complication is specifically in *how to search* for it efficiently, not in whether it's defined.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, APPROACH,
        "Now the feasibility check itself (hoursNeeded) is not monotonic in speed due to some real-world complication (say, a mandatory cooldown between piles that varies non-linearly with speed). Does binary search on the answer still apply?",
        conceptKey = "binary-search-monotonicity-precondition-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "No, not directly - binary search on the answer fundamentally relies on feasibility flipping in only one direction as the candidate value increases; if that monotonic relationship breaks, the standard narrowing logic can incorrectly discard a feasible region, and a different search strategy would be needed.",
                true,
                "Binary search on the answer isn't a generic tool for 'find the minimum value satisfying some condition' - it specifically requires that condition to be monotonic in the candidate value, and recognizing when that precondition breaks is exactly the kind of judgment that separates using a tool correctly from misapplying it.",
            ),
            choice(
                "Yes - binary search on the answer works for any feasibility function regardless of whether it's monotonic.",
                false,
                "Binary search's narrowing logic (discard half the range based on one comparison) is only sound when feasibility doesn't flip back and forth as the candidate changes - without monotonicity, that discard step can eliminate the true answer.",
            ),
            choice(
                "Yes, as long as the search checks both directions (low to high and high to low) simultaneously.",
                false,
                "Checking both directions doesn't restore the core guarantee binary search needs - if feasibility can flip multiple times, no simple bidirectional narrowing strategy reliably finds the true minimum without potentially missing it.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, APPROACH,
        "You're asked to find the minimum in an array that's been rotated an unknown number of times *and* may have been rotated in either direction (left or right). Does this genuinely change the problem, or is it equivalent to the standard version?",
        conceptKey = "binary-search-direction-ambiguity-analysis", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It's actually equivalent - a right rotation by k on an array of length n produces the same result as a left rotation by (n - k), so \"rotated in either direction by some unknown amount\" describes exactly the same space of possible arrays as \"rotated by some unknown amount\" in one fixed direction.",
                true,
                "This is a case where a seemingly harder-sounding generalization turns out not to add any real new cases - recognizing that the two framings describe an identical set of possible inputs is itself a valuable piece of reasoning before writing any new code.",
            ),
            choice(
                "It's a genuinely harder problem requiring the algorithm to first detect which direction the rotation occurred in.",
                false,
                "There's no way to detect \"which direction\" a rotation occurred in from the array alone, and there doesn't need to be - a rotation by k in one direction is indistinguishable from, and equivalent to, a specific rotation amount in the other direction on the same array.",
            ),
            choice(
                "It's unsolvable in general, since left and right rotations produce fundamentally different array structures.",
                false,
                "Left and right rotations of a sorted array by complementary amounts produce the exact same resulting array - there's no structural difference to distinguish, which is precisely why the two framings are equivalent.",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, STATE_SELECTION,
        "You're asked to adapt binary search to find the target's position in a sorted array that also supports fast insertions (like a balanced BST-backed sorted collection) rather than a plain array. What state assumption does standard binary search make that no longer holds?",
        conceptKey = "binary-search-index-based-state-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "It assumes O(1) access to \"the element at position mid\" via direct indexing - a tree-backed sorted collection doesn't offer that same constant-time positional access, so low/high/mid as plain integer indices no longer directly map to an O(1) lookup the way they do for an array.",
                true,
                "Binary search's efficiency isn't just about the comparison logic - it specifically leans on arrays' O(1) random access by index, which a different underlying structure with the same sorted-order guarantee might not provide, changing what 'efficient' even means for that structure.",
            ),
            choice(
                "It assumes the array is sorted in ascending order, which a BST-backed collection can't guarantee.",
                false,
                "A balanced BST-backed sorted collection maintains sorted order just as reliably as a sorted array - sortedness itself isn't the assumption that breaks here, direct positional indexing is.",
            ),
            choice(
                "It assumes the target value is unique, which no longer holds once insertions are supported.",
                false,
                "Whether values are allowed to repeat is unrelated to whether the collection supports insertions - uniqueness isn't the assumption that changes when moving from a plain array to a tree-backed structure.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, BOUNDARY_UPDATE,
        "Now piles can be empty (size 0), and the eating time formula changes so that speed must be a positive real number, not just a positive integer, with the answer required to be precise to two decimal places. What does this expose about the original integer-bounds binary search?",
        conceptKey = "binary-search-integer-vs-real-domain-limitation", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "The original low < high integer-narrowing loop assumes a discrete domain where 'no more candidates left' is well-defined by low meeting high; over the reals, there's no such natural stopping point, so the loop needs to instead run for a fixed number of iterations or until the range shrinks below a chosen precision threshold like 0.005.",
                true,
                "Binary search over integers and binary search over reals share the halving idea, but the termination condition genuinely differs - integers have a natural 'nothing left to check' point that real numbers don't, which has to be replaced with a precision-based stopping rule instead.",
            ),
            choice(
                "Nothing changes - the same low < high loop with integer division works correctly for real-valued speeds too.",
                false,
                "Integer division and integer-based loop termination don't translate directly to a continuous domain - the loop's stopping condition and the mid calculation both need to be adapted for real-valued bounds and a precision target.",
            ),
            choice(
                "The problem becomes unsolvable once real-valued speeds are allowed, since infinite precision can't be achieved.",
                false,
                "The problem remains solvable to any *finite*, specified precision (like two decimal places) by iterating until the search range shrinks below that precision threshold - infinite precision was never actually required.",
            ),
        ),
    ),
    step(
        "find-minimum-in-rotated-sorted-array", BINARY_SEARCH, CODE_BLOCK,
        "Which snippet correctly handles the case with duplicates (falling back to a linear step only when genuinely necessary), rather than assuming no duplicates exist?",
        conceptKey = "binary-search-duplicate-aware-code", difficulty = ADVANCED,
        code = "var low = 0\nvar high = nums.size - 1\nwhile (low < high) {\n    val mid = low + (high - low) / 2\n    // ???\n}\nreturn nums[low]",
        choices = listOf(
            choice(
                "if (nums[mid] > nums[high]) low = mid + 1 else if (nums[mid] < nums[high]) high = mid else high--",
                true,
                "This keeps the standard fast comparison for the two informative cases (strictly greater or strictly less) and only falls back to the slow, safe high-- step specifically when a tie makes the direction genuinely ambiguous - the worst case degrades to O(n) only when it truly must.",
                code = "if (nums[mid] > nums[high]) low = mid + 1 else if (nums[mid] < nums[high]) high = mid else high--",
            ),
            choice(
                "if (nums[mid] >= nums[high]) low = mid + 1 else high = mid",
                false,
                "Treating a tie the same as \"mid is greater\" can incorrectly discard the side that actually contains the minimum - a tie specifically needs its own cautious handling, not folding into one of the two directional branches.",
                code = "if (nums[mid] >= nums[high]) low = mid + 1 else high = mid",
            ),
            choice(
                "high--",
                false,
                "Always shrinking high by one, ignoring the comparison entirely, abandons the fast comparison-based narrowing for every single step, degrading every case to O(n) instead of only the specific tied cases that actually require it.",
                code = "high--",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, CODE_BLOCK,
        "Which snippet correctly and efficiently supports a variant where each pile also has a per-pile minimum eating speed (Koko can never eat a given pile slower than its own minimum), without changing the overall O(n log m) complexity?",
        conceptKey = "binary-search-per-item-constraint-code", difficulty = ADVANCED,
        code = "data class Pile(val bananas: Int, val minSpeed: Int)\nfun hoursNeeded(piles: List<Pile>, speed: Int): Long {\n    // ???\n}",
        choices = listOf(
            choice(
                "return piles.sumOf { val effective = maxOf(speed, it.minSpeed); (it.bananas + effective - 1) / effective }",
                true,
                "Clamping the speed used for each pile up to at least that pile's own minimum, computed inline per pile in the same single pass, adds no extra loop or search - the overall binary search still calls this once per candidate speed, unchanged in complexity.",
                code = "return piles.sumOf { val effective = maxOf(speed, it.minSpeed); (it.bananas + effective - 1) / effective }",
            ),
            choice(
                "val validPiles = piles.filter { speed >= it.minSpeed }\nreturn validPiles.sumOf { (it.bananas + speed - 1) / speed }",
                false,
                "Filtering out piles whose minimum exceeds the candidate speed silently ignores them entirely instead of still requiring them to be eaten (at their own minimum speed) - every pile must still be accounted for in the total hours.",
                code = "val validPiles = piles.filter { speed >= it.minSpeed }\nreturn validPiles.sumOf { (it.bananas + speed - 1) / speed }",
            ),
            choice(
                "return piles.sumOf { (it.bananas + speed - 1) / speed } + piles.sumOf { it.minSpeed }",
                false,
                "Adding each pile's minSpeed directly onto the hour total conflates a speed value with an hours value - these aren't the same unit, and this doesn't correctly enforce that a pile is eaten at its own minimum speed floor.",
                code = "return piles.sumOf { (it.bananas + speed - 1) / speed } + piles.sumOf { it.minSpeed }",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, CODE_BLOCK,
        "Which snippet correctly and safely adapts binary search to work over an abstract, indexable, sorted collection (not necessarily an IntArray), using a generic comparator instead of assuming Int and `<`?",
        conceptKey = "binary-search-generic-comparator-code", difficulty = ADVANCED,
        code = "fun <T> search(size: Int, target: T, get: (Int) -> T, compare: (T, T) -> Int): Int {\n    var low = 0\n    var high = size - 1\n    while (low <= high) {\n        val mid = low + (high - low) / 2\n        // ???\n    }\n    return -1\n}",
        choices = listOf(
            choice(
                "val cmp = compare(get(mid), target)\nwhen { cmp < 0 -> low = mid + 1; cmp > 0 -> high = mid - 1; else -> return mid }",
                true,
                "Routing every comparison through the caller-supplied compare function, rather than assuming < and > work directly on T, is exactly what makes this version reusable for any sorted, indexable collection of any comparable type.",
                code = "val cmp = compare(get(mid), target)\nwhen { cmp < 0 -> low = mid + 1; cmp > 0 -> high = mid - 1; else -> return mid }",
            ),
            choice(
                "when { get(mid) < target -> low = mid + 1; get(mid) > target -> high = mid - 1; else -> return mid }",
                false,
                "Using < and > directly on a generic type T doesn't compile in general - T isn't guaranteed to support those operators, which is exactly why a comparator function was passed in in the first place.",
                code = "when { get(mid) < target -> low = mid + 1; get(mid) > target -> high = mid - 1; else -> return mid }",
            ),
            choice(
                "val cmp = compare(target, get(mid))\nwhen { cmp < 0 -> low = mid + 1; cmp > 0 -> high = mid - 1; else -> return mid }",
                false,
                "Swapping the argument order to compare(target, get(mid)) inverts the sign of every comparison relative to what the branches below expect, moving the search in the wrong direction whenever the two aren't equal.",
                code = "val cmp = compare(target, get(mid))\nwhen { cmp < 0 -> low = mid + 1; cmp > 0 -> high = mid - 1; else -> return mid }",
            ),
        ),
    ),
    step(
        "binary-search", BINARY_SEARCH, TIME_COMPLEXITY,
        "A teammate argues that since real-world CPUs have cache hierarchies, binary search's \"random access\" jumps around memory in a way that could actually be *slower* in practice than a well-optimized linear scan for small-to-medium arrays, despite the better Big-O. Is this a legitimate concern?",
        conceptKey = "binary-search-cache-locality-vs-asymptotic", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "Yes, legitimately - binary search's jumps have poor cache locality compared to a linear scan's sequential access pattern, so for small enough arrays where O(log n) versus O(n) barely differs in raw comparison count, cache-friendly linear scanning can genuinely win in wall-clock time despite the worse asymptotic complexity.",
                true,
                "Big-O complexity intentionally ignores hardware-level effects like cache behavior, but those effects are very real in practice - for small arrays, the actual measured performance can favor the asymptotically 'worse' algorithm, which is exactly why real-world libraries often switch to linear or insertion-based approaches below some threshold size.",
            ),
            choice(
                "No - O(log n) always outperforms O(n) in wall-clock time regardless of any hardware-level effects.",
                false,
                "Asymptotic complexity describes behavior as n grows large - it deliberately doesn't capture constant-factor, hardware-specific effects like cache locality, which can genuinely dominate for small-to-medium input sizes in practice.",
            ),
            choice(
                "No - cache behavior only affects write-heavy workloads, not read-only searches like this one.",
                false,
                "Cache locality affects reads just as much as writes - a scattered access pattern from binary search's jumps can incur more cache misses than a sequential linear scan, regardless of whether the data is being read or written.",
            ),
        ),
    ),
    step(
        "koko-eating-bananas", BINARY_SEARCH, SPACE_COMPLEXITY,
        "A teammate proposes precomputing hoursNeeded for every possible speed from 1 to max(piles) up front, storing the results in an array, to make each binary search step an O(1) lookup instead of an O(n) recomputation. Evaluate this tradeoff precisely.",
        conceptKey = "binary-search-precompute-vs-lazy-evaluation-tradeoff", difficulty = ADVANCED,
        choices = listOf(
            choice(
                "This adds O(m) space (m = max pile size) and O(n * m) time just to build the precomputed table - which is worse overall than the O(n log m) time and O(1) space of computing hoursNeeded lazily only for the O(log m) speeds binary search actually visits.",
                true,
                "Precomputing every possible answer trades away exactly the efficiency binary search was providing in the first place - since only a small, logarithmic number of candidate speeds ever get checked, computing the other, unused ones ahead of time is pure waste, not an optimization.",
            ),
            choice(
                "This strictly improves performance, since every hoursNeeded lookup becomes O(1) instead of O(n).",
                false,
                "Individual lookups do become faster, but the upfront cost of building the table (checking every possible speed, most of which binary search would never have visited) is far more expensive overall than just computing the O(log m) speeds actually needed.",
            ),
            choice(
                "This has no meaningful tradeoff, since both approaches use the same total amount of work either way.",
                false,
                "The two approaches do very different amounts of total work - precomputing touches every possible speed value, while binary search's lazy evaluation only ever touches the O(log m) speeds it actually needs to check.",
            ),
        ),
    ),
)
