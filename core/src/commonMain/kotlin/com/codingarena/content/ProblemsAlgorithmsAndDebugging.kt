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
 * Brace-language line numbers shift relative to Python for `debug-index-01`:
 * closing the loop before the final `return` pushes it from line 5 to line 7.
 * Every C-family variant of that problem uses the same numbering, so the
 * shifted choice list is shared rather than repeated six times.
 */
private val debugIndexBraceChoices = listOf(
    AnswerChoice(
        "a", "Line 2", tag = "2",
        rationale = "Correct: because the body reads nums[i + 1], the loop must stop at " +
            "len(nums) - 1.",
    ),
    AnswerChoice(
        "b", "Line 3", tag = "3",
        rationale = "Comparing neighbours is exactly the right check - it is the range " +
            "that permits an out-of-bounds i.",
        insight = "This is where the crash surfaces, so suspecting it is natural. The " +
            "fix belongs at the loop bound.",
    ),
    AnswerChoice("c", "Line 4", tag = "4", rationale = "Returning false on the first inversion is correct."),
    AnswerChoice("d", "Line 7", tag = "7", rationale = "Returning true after finding no inversion is correct."),
)

/**
 * Starter content, part three: searching, sorting, recursion, complexity and
 * the debugging categories.
 */
internal val algorithmAndDebuggingProblems: List<CodingProblem> = listOf(

    CodingProblem(
        id = "binary-search-01",
        title = "Binary search that never terminates",
        description = "This binary search hangs on some inputs. Which line is the culprit?",
        difficultyRating = 1200,
        primaryTopic = CodingTopic.BINARY_SEARCH,
        secondaryTopics = listOf(CodingTopic.DEBUGGING),
        challengeType = ChallengeType.FIND_THE_BUG,
        codeSnippet = """
            1  def search(nums, target):
            2      lo, hi = 0, len(nums) - 1
            3      while lo <= hi:
            4          mid = (lo + hi) // 2
            5          if nums[mid] == target:
            6              return mid
            7          elif nums[mid] < target:
            8              lo = mid
            9          else:
            10             hi = mid - 1
            11     return -1
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "Line 8", tag = "8",
                rationale = "Correct: lo = mid leaves mid inside the range, so when hi = lo + 1 " +
                    "the window stops shrinking and the loop spins forever. It must be mid + 1.",
            ),
            AnswerChoice(
                "b", "Line 4", tag = "4",
                rationale = "Integer division is fine here. In languages with fixed-width ints " +
                    "this line can overflow, but that is not what causes the hang.",
                insight = "Midpoint calculation is a genuine source of binary search bugs - worth " +
                    "checking, just not the fault here.",
            ),
            AnswerChoice(
                "c", "Line 3", tag = "3",
                rationale = "lo <= hi is the correct condition for an inclusive range.",
                insight = "The loop condition and the update rule have to agree, so checking it " +
                    "is sound - here the update is the half that is wrong.",
            ),
            AnswerChoice(
                "d", "Line 10", tag = "10",
                rationale = "hi = mid - 1 correctly excludes mid, which has already been tested.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Every branch must exclude mid, because mid has already been compared. With " +
            "lo = mid, a two-element window where the answer lies right recomputes the same mid " +
            "forever.",
        bestApproach = "Use lo = mid + 1 and hi = mid - 1 so that each iteration strictly shrinks " +
            "the search window.",
        timeComplexity = "O(log n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Leaving mid inside the range in one of the branches",
            "Mismatching the loop condition (< versus <=) with the update rule",
        ),
        hints = listOf(
            "Trace lo = 0, hi = 1 with the target to the right.",
            "After comparing nums[mid], should mid still be in the search range?",
        ),
        patternId = "binary-search",
        estimatedSeconds = 60,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                1  int search(int[] nums, int target) {
                2      int lo = 0, hi = nums.length - 1;
                3      while (lo <= hi) {
                4          int mid = (lo + hi) / 2;
                5          if (nums[mid] == target) {
                6              return mid;
                7          } else if (nums[mid] < target) {
                8              lo = mid;
                9          } else {
                10             hi = mid - 1;
                11         }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                1  function search(nums, target) {
                2      let lo = 0, hi = nums.length - 1;
                3      while (lo <= hi) {
                4          const mid = Math.floor((lo + hi) / 2);
                5          if (nums[mid] === target) {
                6              return mid;
                7          } else if (nums[mid] < target) {
                8              lo = mid;
                9          } else {
                10             hi = mid - 1;
                11         }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                1  fun search(nums: IntArray, target: Int): Int {
                2      var lo = 0; var hi = nums.size - 1
                3      while (lo <= hi) {
                4          val mid = (lo + hi) / 2
                5          if (nums[mid] == target) {
                6              return mid
                7          } else if (nums[mid] < target) {
                8              lo = mid
                9          } else {
                10             hi = mid - 1
                11         }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                1  int search(vector<int>& nums, int target) {
                2      int lo = 0, hi = nums.size() - 1;
                3      while (lo <= hi) {
                4          int mid = (lo + hi) / 2;
                5          if (nums[mid] == target) {
                6              return mid;
                7          } else if (nums[mid] < target) {
                8              lo = mid;
                9          } else {
                10             hi = mid - 1;
                11         }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                1  func search(nums []int, target int) int {
                2      lo, hi := 0, len(nums)-1
                3      for lo <= hi {
                4          mid := (lo + hi) / 2
                5          if nums[mid] == target {
                6              return mid
                7          } else if nums[mid] < target {
                8              lo = mid
                9          } else {
                10             hi = mid - 1
                11         }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                1  func search(_ nums: [Int], _ target: Int) -> Int {
                2      var lo = 0, hi = nums.count - 1
                3      while lo <= hi {
                4          let mid = (lo + hi) / 2
                5          if nums[mid] == target {
                6              return mid
                7          } else if nums[mid] < target {
                8              lo = mid
                9          } else {
                10             hi = mid - 1
                11         }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "binary-search-complexity-01",
        title = "Cost of halving",
        description = "What is the time complexity of binary search on a sorted array of n " +
            "elements?",
        difficultyRating = 1000,
        primaryTopic = CodingTopic.COMPLEXITY,
        secondaryTopics = listOf(CodingTopic.BINARY_SEARCH),
        challengeType = ChallengeType.TIME_COMPLEXITY,
        choices = listOf(
            AnswerChoice("a", "O(log n)", rationale = "Correct: each comparison discards half the range."),
            AnswerChoice(
                "b", "O(n)",
                rationale = "That is a linear scan. Binary search never looks at most elements.",
                insight = "O(n) is the right answer for searching an *unsorted* array.",
            ),
            AnswerChoice(
                "c", "O(n log n)",
                rationale = "That is the cost of sorting the array, not of searching one that is " +
                    "already sorted.",
                insight = "Worth remembering if the array arrives unsorted - then this is the true " +
                    "total cost.",
            ),
            AnswerChoice("d", "O(1)", rationale = "Constant time would mean the position is known without searching."),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "The range goes n, n/2, n/4, ... and reaches 1 after about log2(n) halvings, " +
            "so the number of comparisons grows logarithmically.",
        bestApproach = "Ask how many times the input can be halved before nothing is left - that " +
            "count is the complexity.",
        timeComplexity = "O(log n)",
        spaceComplexity = "O(1) iteratively",
        commonMistakes = listOf(
            "Quoting O(log n) while forgetting the O(n log n) sort needed to get there",
            "Assuming binary search works on unsorted input",
        ),
        hints = listOf("How many times can n be halved before you reach 1?"),
        patternId = "binary-search",
        estimatedSeconds = 30,
    ),

    CodingProblem(
        id = "recursion-01",
        title = "Trace the recursion",
        description = "What is printed by the call f(3)?",
        difficultyRating = 1000,
        primaryTopic = CodingTopic.RECURSION,
        challengeType = ChallengeType.VARIABLE_TRACE,
        codeSnippet = """
            def f(n):
                if n == 0:
                    return
                f(n - 1)
                print(n, end=" ")
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "1 2 3",
                rationale = "Correct: the recursive call runs before the print, so the deepest " +
                    "frame prints first.",
            ),
            AnswerChoice(
                "b", "3 2 1",
                rationale = "That is what you get when the print comes *before* the recursive call.",
                insight = "You traced the frames correctly - the ordering hinges on which side of " +
                    "the recursion the print sits.",
            ),
            AnswerChoice(
                "c", "1 2 3 4",
                rationale = "The base case returns at n == 0 without printing, so 4 never appears.",
            ),
            AnswerChoice(
                "d", "3 2 1 0",
                rationale = "0 is not printed - the base case returns before reaching the print.",
                insight = "Right that the base case matters; it exits before printing anything.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "f(3) calls f(2) calls f(1) calls f(0), which returns immediately. The " +
            "prints then happen on the way back up: 1, then 2, then 3.",
        bestApproach = "Draw the call stack downward, then read the prints off as the frames " +
            "unwind.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(n) for the call stack",
        commonMistakes = listOf(
            "Reading work after the recursive call as if it ran on the way down",
            "Including the base case in the output",
        ),
        hints = listOf(
            "Does the print run before or after the recursive call?",
            "Which frame reaches its print statement first?",
        ),
        patternId = "dfs",
        estimatedSeconds = 45,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                void f(int n) {
                    if (n == 0) return;
                    f(n - 1);
                    System.out.print(n + " ");
                }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function f(n) {
                    if (n === 0) return;
                    f(n - 1);
                    process.stdout.write(n + " ");
                }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun f(n: Int) {
                    if (n == 0) return
                    f(n - 1)
                    print("${'$'}n ")
                }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                void f(int n) {
                    if (n == 0) return;
                    f(n - 1);
                    cout << n << " ";
                }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                func f(n int) {
                    if n == 0 {
                        return
                    }
                    f(n - 1)
                    fmt.Print(n, " ")
                }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                func f(_ n: Int) {
                    if n == 0 { return }
                    f(n - 1)
                    print(n, terminator: " ")
                }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "sorting-01",
        title = "Sorting with a tie-break",
        description = "You must sort employees by salary descending, and by name ascending where " +
            "salaries are equal. Which approach is best?",
        difficultyRating = 1150,
        primaryTopic = CodingTopic.SORTING,
        challengeType = ChallengeType.MULTIPLE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "Sort once with a compound key: salary descending, then name ascending",
                rationale = "Correct: one sort, one unambiguous ordering.",
            ),
            AnswerChoice(
                "b", "Sort by name, then sort by salary, relying on a stable sort",
                rationale = "This does work with a stable sort, but it depends on a property many " +
                    "readers will not notice and costs two passes.",
                insight = "Genuinely correct if the sort is stable, and knowing that is worth " +
                    "points in an interview - just say the assumption out loud.",
            ),
            AnswerChoice(
                "c", "Sort by salary, then sort by name",
                rationale = "Wrong order: the final sort by name becomes the primary key, so " +
                    "salary ordering is lost.",
                insight = "The two-pass idea is on the right track - the keys are applied in the " +
                    "reverse of the order needed.",
            ),
            AnswerChoice(
                "d", "Sort by salary, then bubble equal-salary groups by name",
                rationale = "Correct results, but it hand-rolls what the comparator already does " +
                    "and adds an O(n^2) worst case.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Multi-key ordering belongs in the comparator. With repeated sorts you must " +
            "apply keys least-significant first *and* rely on stability - correct, but fragile " +
            "and easy to misread.",
        bestApproach = "Sort once with a comparator that compares salary descending and falls " +
            "back to name ascending.",
        timeComplexity = "O(n log n)",
        spaceComplexity = "O(1) to O(n) depending on the sort",
        commonMistakes = listOf(
            "Applying repeated sorts in the wrong order",
            "Assuming a stable sort in a language that does not guarantee one",
        ),
        hints = listOf("Where should the tie-break rule live?"),
        patternId = "sorting",
        estimatedSeconds = 50,
    ),

    CodingProblem(
        id = "time-complexity-01",
        title = "Nested loop with a halving inner bound",
        description = "What is the time complexity of this function?",
        difficultyRating = 950,
        primaryTopic = CodingTopic.COMPLEXITY,
        challengeType = ChallengeType.TIME_COMPLEXITY,
        codeSnippet = """
            def f(n):
                total = 0
                for i in range(n):
                    j = 1
                    while j < n:
                        total += 1
                        j *= 2
                return total
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "O(n log n)",
                rationale = "Correct: the outer loop runs n times and the inner loop doubles j, " +
                    "so it runs about log2(n) times.",
            ),
            AnswerChoice(
                "b", "O(n^2)",
                rationale = "That would be right if the inner loop incremented j. It doubles, so " +
                    "it finishes far sooner.",
                insight = "Recognising a nested loop as the driver of the cost is right - the " +
                    "inner loop's growth rate is the detail.",
            ),
            AnswerChoice(
                "c", "O(n)",
                rationale = "That ignores the inner loop entirely.",
            ),
            AnswerChoice(
                "d", "O(log n)",
                rationale = "That is only the inner loop; the outer loop multiplies it by n.",
                insight = "You read the inner loop correctly - it just has to be multiplied by the " +
                    "outer one.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Doubling j takes it from 1 to n in log2(n) steps. Multiplied by the n " +
            "iterations of the outer loop, the total is O(n log n).",
        bestApproach = "Work out each loop's iteration count separately, then multiply for nested " +
            "loops and add for sequential ones.",
        timeComplexity = "O(n log n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Assuming any nested loop is automatically O(n^2)",
            "Missing that multiplying the counter gives logarithmic rather than linear growth",
        ),
        hints = listOf(
            "How does j change each iteration - does it grow by addition or by multiplication?",
            "How many doublings does it take to get from 1 to n?",
        ),
        patternId = "complexity",
        estimatedSeconds = 45,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                int f(int n) {
                    int total = 0;
                    for (int i = 0; i < n; i++) {
                        int j = 1;
                        while (j < n) {
                            total += 1;
                            j *= 2;
                        }
                    }
                    return total;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function f(n) {
                    let total = 0;
                    for (let i = 0; i < n; i++) {
                        let j = 1;
                        while (j < n) {
                            total += 1;
                            j *= 2;
                        }
                    }
                    return total;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun f(n: Int): Int {
                    var total = 0
                    for (i in 0 until n) {
                        var j = 1
                        while (j < n) {
                            total += 1
                            j *= 2
                        }
                    }
                    return total
                }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                int f(int n) {
                    int total = 0;
                    for (int i = 0; i < n; i++) {
                        int j = 1;
                        while (j < n) {
                            total += 1;
                            j *= 2;
                        }
                    }
                    return total;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                func f(n int) int {
                    total := 0
                    for i := 0; i < n; i++ {
                        j := 1
                        for j < n {
                            total++
                            j *= 2
                        }
                    }
                    return total
                }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                func f(_ n: Int) -> Int {
                    var total = 0
                    for _ in 0..<n {
                        var j = 1
                        while j < n {
                            total += 1
                            j *= 2
                        }
                    }
                    return total
                }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "space-complexity-01",
        title = "Space cost of a recursive sum",
        description = "What is the space complexity of this function?",
        difficultyRating = 1050,
        primaryTopic = CodingTopic.COMPLEXITY,
        secondaryTopics = listOf(CodingTopic.RECURSION),
        challengeType = ChallengeType.SPACE_COMPLEXITY,
        codeSnippet = """
            def total(nums, i=0):
                if i == len(nums):
                    return 0
                return nums[i] + total(nums, i + 1)
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "O(n)",
                rationale = "Correct: n nested calls sit on the stack before any of them return.",
            ),
            AnswerChoice(
                "b", "O(1)",
                rationale = "That would be true of a loop, or of a language that eliminates tail " +
                    "calls - but this call is not in tail position, and Python does not do that " +
                    "anyway.",
                insight = "The function allocates no data structures, which is the part that " +
                    "makes O(1) tempting. The call stack is the hidden cost.",
            ),
            AnswerChoice(
                "c", "O(log n)",
                rationale = "Logarithmic stack depth comes from halving recursions like binary " +
                    "search; this one descends one element at a time.",
            ),
            AnswerChoice(
                "d", "O(n^2)",
                rationale = "Each frame holds a constant amount of data, so n frames cost O(n).",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Recursion depth is space. The function builds nothing on the heap, but n " +
            "stack frames must exist at once before the base case unwinds them.",
        bestApproach = "Count the maximum recursion depth and multiply by the space each frame " +
            "holds; add any explicit data structures on top.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Ignoring the call stack when a function allocates nothing explicitly",
            "Assuming a recursive solution is automatically as cheap in space as a loop",
        ),
        hints = listOf(
            "How many calls are open at the deepest point?",
            "Does the call stack count toward space complexity?",
        ),
        patternId = "complexity",
        estimatedSeconds = 45,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                int total(int[] nums, int i) {
                    if (i == nums.length) return 0;
                    return nums[i] + total(nums, i + 1);
                }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function total(nums, i = 0) {
                    if (i === nums.length) return 0;
                    return nums[i] + total(nums, i + 1);
                }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun total(nums: IntArray, i: Int = 0): Int {
                    if (i == nums.size) return 0
                    return nums[i] + total(nums, i + 1)
                }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                int total(vector<int>& nums, int i = 0) {
                    if (i == (int)nums.size()) return 0;
                    return nums[i] + total(nums, i + 1);
                }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                func total(nums []int, i int) int {
                    if i == len(nums) {
                        return 0
                    }
                    return nums[i] + total(nums, i+1)
                }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                func total(_ nums: [Int], _ i: Int = 0) -> Int {
                    if i == nums.count { return 0 }
                    return nums[i] + total(nums, i + 1)
                }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "debug-loops-01",
        title = "The loop that misses the last element",
        description = "This should return the sum of every element, but it always misses one. " +
            "Which line is wrong?",
        difficultyRating = 950,
        primaryTopic = CodingTopic.DEBUGGING,
        secondaryTopics = listOf(CodingTopic.ARRAYS),
        challengeType = ChallengeType.FIND_THE_BUG,
        codeSnippet = """
            1  def total(nums):
            2      result = 0
            3      i = 0
            4      while i < len(nums) - 1:
            5          result += nums[i]
            6          i += 1
            7      return result
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "Line 4", tag = "4",
                rationale = "Correct: the condition stops one element early. It should be " +
                    "i < len(nums).",
            ),
            AnswerChoice(
                "b", "Line 3", tag = "3",
                rationale = "Starting at index 0 is correct.",
            ),
            AnswerChoice(
                "c", "Line 6", tag = "6",
                rationale = "Incrementing by one is correct; the loop just exits too soon.",
                insight = "The bound and the step do have to agree, so checking here is reasonable.",
            ),
            AnswerChoice(
                "d", "Line 2", tag = "2",
                rationale = "Seeding a sum at 0 is correct - unlike seeding a maximum at 0.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "The last valid index is len(nums) - 1, so a loop that must include it needs " +
            "i < len(nums). Writing len(nums) - 1 as the bound skips the final element.",
        bestApproach = "Check the bound against a two-element input: it should visit both indices " +
            "0 and 1.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Confusing the last valid *index* with the bound the loop should stop at",
            "Testing only with long inputs, where a single missing element is easy to overlook",
        ),
        hints = listOf(
            "Run it mentally on a two-element list.",
            "What is the largest index the loop actually reaches?",
        ),
        patternId = "array-traversal",
        estimatedSeconds = 40,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                1  int total(int[] nums) {
                2      int result = 0;
                3      int i = 0;
                4      while (i < nums.length - 1) {
                5          result += nums[i];
                6          i += 1;
                7      }
                8      return result;
                9  }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                1  function total(nums) {
                2      let result = 0;
                3      let i = 0;
                4      while (i < nums.length - 1) {
                5          result += nums[i];
                6          i += 1;
                7      }
                8      return result;
                9  }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                1  fun total(nums: IntArray): Int {
                2      var result = 0
                3      var i = 0
                4      while (i < nums.size - 1) {
                5          result += nums[i]
                6          i += 1
                7      }
                8      return result
                9  }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                1  int total(vector<int>& nums) {
                2      int result = 0;
                3      int i = 0;
                4      while (i < (int)nums.size() - 1) {
                5          result += nums[i];
                6          i += 1;
                7      }
                8      return result;
                9  }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                1  func total(nums []int) int {
                2      result := 0
                3      i := 0
                4      for i < len(nums)-1 {
                5          result += nums[i]
                6          i++
                7      }
                8      return result
                9  }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                1  func total(_ nums: [Int]) -> Int {
                2      var result = 0
                3      var i = 0
                4      while i < nums.count - 1 {
                5          result += nums[i]
                6          i += 1
                7      }
                8      return result
                9  }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "debug-index-01",
        title = "Index out of range",
        description = "This compares each element to the next one, but crashes. Which line is at " +
            "fault?",
        difficultyRating = 1000,
        primaryTopic = CodingTopic.DEBUGGING,
        secondaryTopics = listOf(CodingTopic.ARRAYS),
        challengeType = ChallengeType.FIND_THE_BUG,
        codeSnippet = """
            1  def is_sorted(nums):
            2      for i in range(len(nums)):
            3          if nums[i] > nums[i + 1]:
            4              return False
            5      return True
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "Line 2", tag = "2",
                rationale = "Correct: because the body reads nums[i + 1], the loop must stop at " +
                    "len(nums) - 1.",
            ),
            AnswerChoice(
                "b", "Line 3", tag = "3",
                rationale = "Comparing neighbours is exactly the right check - it is the range " +
                    "that permits an out-of-bounds i.",
                insight = "This is where the crash surfaces, so suspecting it is natural. The " +
                    "fix belongs at the loop bound.",
            ),
            AnswerChoice(
                "c", "Line 4", tag = "4",
                rationale = "Returning False on the first inversion is correct.",
            ),
            AnswerChoice(
                "d", "Line 5", tag = "5",
                rationale = "Returning True after finding no inversion is correct.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Any loop whose body reads ahead by one must shorten its range by one. The " +
            "crash appears on line 3, but the defect is the bound on line 2 - a distinction worth " +
            "making explicitly when debugging.",
        bestApproach = "Match the loop bound to the furthest index the body actually touches: " +
            "range(len(nums) - 1) here.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Fixing the line that crashes rather than the bound that allowed it",
            "Forgetting that an empty or single-element list should still be considered sorted",
        ),
        hints = listOf(
            "What is the largest value of i the loop produces?",
            "What does the body read at that value?",
        ),
        patternId = "array-traversal",
        estimatedSeconds = 45,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                1  boolean isSorted(int[] nums) {
                2      for (int i = 0; i < nums.length; i++) {
                3          if (nums[i] > nums[i + 1]) {
                4              return false;
                5          }
                6      }
                7      return true;
                8  }
                """.trimIndent(),
                choices = debugIndexBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                1  function isSorted(nums) {
                2      for (let i = 0; i < nums.length; i++) {
                3          if (nums[i] > nums[i + 1]) {
                4              return false;
                5          }
                6      }
                7      return true;
                8  }
                """.trimIndent(),
                choices = debugIndexBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                KOTLIN,
                """
                1  fun isSorted(nums: IntArray): Boolean {
                2      for (i in nums.indices) {
                3          if (nums[i] > nums[i + 1]) {
                4              return false
                5          }
                6      }
                7      return true
                8  }
                """.trimIndent(),
                choices = debugIndexBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                CPP,
                """
                1  bool isSorted(vector<int>& nums) {
                2      for (int i = 0; i < (int)nums.size(); i++) {
                3          if (nums[i] > nums[i + 1]) {
                4              return false;
                5          }
                6      }
                7      return true;
                8  }
                """.trimIndent(),
                choices = debugIndexBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                GO,
                """
                1  func isSorted(nums []int) bool {
                2      for i := 0; i < len(nums); i++ {
                3          if nums[i] > nums[i+1] {
                4              return false
                5          }
                6      }
                7      return true
                8  }
                """.trimIndent(),
                choices = debugIndexBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                SWIFT,
                """
                1  func isSorted(_ nums: [Int]) -> Bool {
                2      for i in 0..<nums.count {
                3          if nums[i] > nums[i + 1] {
                4              return false
                5          }
                6      }
                7      return true
                8  }
                """.trimIndent(),
                choices = debugIndexBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
        ),
    ),

    CodingProblem(
        id = "dp-knapsack-01",
        title = "Choosing items under a limit",
        description = "\"Given item weights and values and a capacity, maximise the total value " +
            "you can carry.\" Which pattern does this call for?",
        difficultyRating = 1400,
        primaryTopic = CodingTopic.DYNAMIC_PROGRAMMING,
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "0/1 knapsack dynamic programming",
                rationale = "Correct: each item is taken or skipped, and subproblems over " +
                    "(items considered, remaining capacity) repeat heavily.",
            ),
            AnswerChoice(
                "b", "Greedy by value-to-weight ratio",
                rationale = "That is optimal for *fractional* knapsack only. When items cannot be " +
                    "split, greedy can miss the best combination.",
                insight = "A strong instinct, and exactly right for the fractional variant - the " +
                    "indivisibility is what breaks it.",
            ),
            AnswerChoice(
                "c", "Sliding window over the items",
                rationale = "The chosen items need not be contiguous, so no window applies.",
            ),
            AnswerChoice(
                "d", "Binary search on the answer",
                rationale = "There is no cheap feasibility check for a candidate value, which is " +
                    "what this technique needs.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "The two markers of DP are both present: an optimal choice built from " +
            "optimal sub-choices, and overlapping subproblems. \"Take it or leave it\" under a " +
            "budget is the classic 0/1 knapsack shape.",
        bestApproach = "Define dp[c] as the best value achievable with capacity c, and for each " +
            "item update capacities downward so no item is used twice.",
        timeComplexity = "O(items * capacity)",
        spaceComplexity = "O(capacity) with the rolling one-dimensional table",
        commonMistakes = listOf(
            "Applying the fractional greedy solution to the 0/1 variant",
            "Iterating capacity upward in the 1-D version, which silently allows reusing an item",
        ),
        hints = listOf(
            "Can an item be taken partially?",
            "What two properties tell you a problem is dynamic programming?",
        ),
        patternId = "dynamic-programming",
        estimatedSeconds = 60,
    ),
)
