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
 * Starter content, part four: the patterns that had lessons but no practice.
 *
 * Backtracking, Greedy, Tries, Bit Manipulation, Union Find and Topological
 * Sort all shipped as Pattern Library entries with nothing to solve, which
 * meant the recommender could never target them and a user weak in any of them
 * was invisible to the learning path engine.
 */
/**
 * Brace-language line numbers for `backtracking-02`, shared across all six
 * variants: using an explicit brace on every if/for (rather than Python's
 * bare-colon blocks) consistently pushes the "undo" bug from line 11 to
 * line 13 and the skip-check from line 6 to line 7.
 */
private val backtrackingBraceChoices = listOf(
    AnswerChoice(
        "a", "Line 13", tag = "13",
        rationale = "Correct: the used flag is reset but the element is never popped off " +
            "path, so path keeps growing and later branches carry stale elements.",
    ),
    AnswerChoice(
        "b", "Line 3", tag = "3",
        rationale = "Copying the path before appending is exactly right - appending the live " +
            "path itself would be the other classic bug here.",
        insight = "This is the line that is usually wrong in this shape of code, so " +
            "checking it first is good instinct.",
    ),
    AnswerChoice("c", "Line 7", tag = "7", rationale = "Skipping already-used elements is correct for permutations."),
    AnswerChoice(
        "d", "Line 11", tag = "11",
        rationale = "Appending on the way down is correct; the fault is that nothing " +
            "removes it on the way back up.",
        insight = "You are looking at the right pair of operations - the missing half is " +
            "the undo, not the do.",
    ),
)

/**
 * Brace-language line numbers for `unionfind-02`. Java/JavaScript/C++/Go can
 * reassign their `x` parameter directly, matching Python's line count exactly
 * except for the closing braces that push `union`'s body and `connected`'s
 * return down by two lines. Kotlin/Swift parameters are immutable, so those
 * need one extra local-variable line, shifting everything by one more.
 */
private val unionFindBraceChoicesGroupA = listOf(
    AnswerChoice(
        "a", "Line 9", tag = "9",
        rationale = "Correct: it links the elements directly instead of their roots, so " +
            "an earlier merge involving a is silently discarded.",
    ),
    AnswerChoice(
        "b", "Line 13", tag = "13",
        rationale = "Comparing roots is exactly right - that is what find is for.",
        insight = "This is the classic Union Find mistake, so suspecting it is sound. " +
            "Here it is the one line that is correct.",
    ),
    AnswerChoice("c", "Line 2", tag = "2", rationale = "Walking up until a node is its own parent is the correct root test."),
    AnswerChoice(
        "d", "Line 3", tag = "3",
        rationale = "Ascending one level at a time is correct; it is merely slow without " +
            "path compression, which is not what breaks correctness here.",
        insight = "You are right that this line is suboptimal - it costs speed, not " +
            "correctness.",
    ),
)
private val unionFindBraceChoicesGroupB = listOf(
    AnswerChoice(
        "a", "Line 10", tag = "10",
        rationale = "Correct: it links the elements directly instead of their roots, so " +
            "an earlier merge involving a is silently discarded.",
    ),
    AnswerChoice(
        "b", "Line 14", tag = "14",
        rationale = "Comparing roots is exactly right - that is what find is for.",
        insight = "This is the classic Union Find mistake, so suspecting it is sound. " +
            "Here it is the one line that is correct.",
    ),
    AnswerChoice("c", "Line 3", tag = "3", rationale = "Walking up until a node is its own parent is the correct root test."),
    AnswerChoice(
        "d", "Line 4", tag = "4",
        rationale = "Ascending one level at a time is correct; it is merely slow without " +
            "path compression, which is not what breaks correctness here.",
        insight = "You are right that this line is suboptimal - it costs speed, not " +
            "correctness.",
    ),
)

internal val advancedPatternProblems: List<CodingProblem> = listOf(

    // ------------------------------------------------------------ backtracking

    CodingProblem(
        id = "backtracking-01",
        title = "Every possible subset",
        description = "\"Return every subset of a list of distinct integers.\" Which pattern " +
            "does this call for?",
        difficultyRating = 1250,
        primaryTopic = CodingTopic.BACKTRACKING,
        secondaryTopics = listOf(CodingTopic.RECURSION),
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "Backtracking - build each subset, record it, then undo the last choice",
                rationale = "Correct: \"every possible\" means enumerate, and each element is a " +
                    "take-it-or-leave-it decision.",
            ),
            AnswerChoice(
                "b", "Dynamic programming",
                rationale = "DP reuses answers to overlapping subproblems. Here every subset is " +
                    "distinct - there is nothing to reuse, only to enumerate.",
                insight = "Asking whether subproblems repeat is exactly the right question; here " +
                    "the answer happens to be no.",
            ),
            AnswerChoice(
                "c", "Sliding window",
                rationale = "Subsets are not contiguous, so no window can describe them.",
            ),
            AnswerChoice(
                "d", "Greedy",
                rationale = "Greedy picks one answer. This problem wants all of them.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "\"All possible\" is the giveaway for backtracking. With n distinct " +
            "elements there are 2^n subsets, and the recursion tree mirrors that exactly: at each " +
            "element you branch on include or exclude.",
        bestApproach = "Recurse over the index, appending the current element, recording the " +
            "path, then removing it again before trying the next branch.",
        timeComplexity = "O(n * 2^n)",
        spaceComplexity = "O(n) for the recursion, excluding the output",
        commonMistakes = listOf(
            "Recording the live path list instead of a copy, so every result mutates together",
            "Forgetting to undo the choice, which leaks elements into sibling branches",
        ),
        hints = listOf(
            "How many subsets does a list of n distinct elements have?",
            "What decision do you make about each element in turn?",
        ),
        patternId = "backtracking",
        estimatedSeconds = 50,
    ),

    CodingProblem(
        id = "backtracking-02",
        title = "The backtrack that never backtracks",
        description = "This should collect every permutation, but the results come out wrong. " +
            "Which line is at fault?",
        difficultyRating = 1400,
        primaryTopic = CodingTopic.BACKTRACKING,
        secondaryTopics = listOf(CodingTopic.DEBUGGING, CodingTopic.RECURSION),
        challengeType = ChallengeType.FIND_THE_BUG,
        codeSnippet = """
            1  def permute(nums, path, used, out):
            2      if len(path) == len(nums):
            3          out.append(path[:])
            4          return
            5      for i, n in enumerate(nums):
            6          if used[i]:
            7              continue
            8          used[i] = True
            9          path.append(n)
            10         permute(nums, path, used, out)
            11         used[i] = False
            12     return
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "Line 11", tag = "11",
                rationale = "Correct: the used flag is reset but the element is never popped off " +
                    "path, so path keeps growing and later branches carry stale elements.",
            ),
            AnswerChoice(
                "b", "Line 3", tag = "3",
                rationale = "Copying with path[:] is exactly right - appending path itself would " +
                    "be the other classic bug here.",
                insight = "This is the line that is usually wrong in this shape of code, so " +
                    "checking it first is good instinct.",
            ),
            AnswerChoice(
                "c", "Line 6", tag = "6",
                rationale = "Skipping already-used elements is correct for permutations.",
            ),
            AnswerChoice(
                "d", "Line 9", tag = "9",
                rationale = "Appending on the way down is correct; the fault is that nothing " +
                    "removes it on the way back up.",
                insight = "You are looking at the right pair of operations - the missing half is " +
                    "the undo, not the do.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Backtracking is do, recurse, undo. Every mutation before the recursive " +
            "call needs a matching reversal after it. Here used[i] is reversed but path.append is " +
            "not, so the two pieces of state drift apart.",
        bestApproach = "Add path.pop() alongside used[i] = False, so both mutations are undone " +
            "together after the recursive call returns.",
        timeComplexity = "O(n * n!)",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Undoing some state but not all of it",
            "Appending the live path rather than a copy",
        ),
        hints = listOf(
            "Backtracking is do, recurse, undo. Count the dos and the undos.",
            "Two things are mutated before line 10. How many are reversed after it?",
        ),
        patternId = "backtracking",
        estimatedSeconds = 60,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                1  void permute(int[] nums, List<Integer> path, boolean[] used, List<List<Integer>> out) {
                2      if (path.size() == nums.length) {
                3          out.add(new ArrayList<>(path));
                4          return;
                5      }
                6      for (int i = 0; i < nums.length; i++) {
                7          if (used[i]) {
                8              continue;
                9          }
                10         used[i] = true;
                11         path.add(nums[i]);
                12         permute(nums, path, used, out);
                13         used[i] = false;
                14     }
                """.trimIndent(),
                choices = backtrackingBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                1  function permute(nums, path, used, out) {
                2      if (path.length === nums.length) {
                3          out.push([...path]);
                4          return;
                5      }
                6      for (let i = 0; i < nums.length; i++) {
                7          if (used[i]) {
                8              continue;
                9          }
                10         used[i] = true;
                11         path.push(nums[i]);
                12         permute(nums, path, used, out);
                13         used[i] = false;
                14     }
                """.trimIndent(),
                choices = backtrackingBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                KOTLIN,
                """
                1  fun permute(nums: IntArray, path: MutableList<Int>, used: BooleanArray, out: MutableList<List<Int>>) {
                2      if (path.size == nums.size) {
                3          out.add(path.toList())
                4          return
                5      }
                6      for (i in nums.indices) {
                7          if (used[i]) {
                8              continue
                9          }
                10         used[i] = true
                11         path.add(nums[i])
                12         permute(nums, path, used, out)
                13         used[i] = false
                14     }
                """.trimIndent(),
                choices = backtrackingBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                CPP,
                """
                1  void permute(vector<int>& nums, vector<int>& path, vector<bool>& used, vector<vector<int>>& out) {
                2      if (path.size() == nums.size()) {
                3          out.push_back(path);
                4          return;
                5      }
                6      for (int i = 0; i < (int)nums.size(); i++) {
                7          if (used[i]) {
                8              continue;
                9          }
                10         used[i] = true;
                11         path.push_back(nums[i]);
                12         permute(nums, path, used, out);
                13         used[i] = false;
                14     }
                """.trimIndent(),
                choices = backtrackingBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                GO,
                """
                1  func permute(nums []int, path []int, used []bool, out *[][]int) {
                2      if len(path) == len(nums) {
                3          *out = append(*out, append([]int{}, path...))
                4          return
                5      }
                6      for i := 0; i < len(nums); i++ {
                7          if used[i] {
                8              continue
                9          }
                10         used[i] = true
                11         path = append(path, nums[i])
                12         permute(nums, path, used, out)
                13         used[i] = false
                14     }
                """.trimIndent(),
                choices = backtrackingBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                SWIFT,
                """
                1  func permute(_ nums: [Int], _ path: inout [Int], _ used: inout [Bool], _ out: inout [[Int]]) {
                2      if path.count == nums.count {
                3          out.append(path)
                4          return
                5      }
                6      for i in 0..<nums.count {
                7          if used[i] {
                8              continue
                9          }
                10         used[i] = true
                11         path.append(nums[i])
                12         permute(nums, &path, &used, &out)
                13         used[i] = false
                14     }
                """.trimIndent(),
                choices = backtrackingBraceChoices,
                correctAnswerIds = listOf("a"),
            ),
        ),
    ),

    CodingProblem(
        id = "backtracking-03",
        title = "Space cost of enumerating subsets",
        description = "Ignoring the space used by the returned list itself, what is the space " +
            "complexity of a backtracking subset generator over n elements?",
        difficultyRating = 1350,
        primaryTopic = CodingTopic.COMPLEXITY,
        secondaryTopics = listOf(CodingTopic.BACKTRACKING, CodingTopic.RECURSION),
        challengeType = ChallengeType.SPACE_COMPLEXITY,
        choices = listOf(
            AnswerChoice(
                "a", "O(n)",
                rationale = "Correct: the recursion goes at most n deep, and the working path " +
                    "holds at most n elements.",
            ),
            AnswerChoice(
                "b", "O(2^n)",
                rationale = "That is the size of the output, which the question explicitly " +
                    "excludes. Only one branch is live at a time.",
                insight = "2^n is the right count of subsets - the distinction being tested is " +
                    "output size versus working memory.",
            ),
            AnswerChoice(
                "c", "O(1)",
                rationale = "The recursion stack and the working path both grow with n.",
            ),
            AnswerChoice(
                "d", "O(n^2)",
                rationale = "Each of the n frames holds a constant amount of its own state, so " +
                    "the total is linear, not quadratic.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Backtracking explores one root-to-leaf path at a time. Only that path and " +
            "its stack frames are live, so working space is linear even though the output is " +
            "exponential. Interviewers ask this specifically to see whether you separate the two.",
        bestApproach = "Count the maximum recursion depth plus the largest working structure, and " +
            "state separately whether the output is being counted.",
        timeComplexity = "O(n * 2^n)",
        spaceComplexity = "O(n) excluding output",
        commonMistakes = listOf(
            "Reporting the output size as the space complexity",
            "Forgetting the recursion stack when the function allocates nothing explicitly",
        ),
        hints = listOf("How many branches of the recursion tree exist at the same moment?"),
        patternId = "backtracking",
        estimatedSeconds = 45,
    ),

    // ------------------------------------------------------------------ greedy

    CodingProblem(
        id = "greedy-01",
        title = "The most meetings you can attend",
        description = "\"Given meeting start and end times, attend as many as possible without " +
            "overlap.\" Which pattern solves this?",
        difficultyRating = 1200,
        primaryTopic = CodingTopic.GREEDY,
        secondaryTopics = listOf(CodingTopic.SORTING),
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "Greedy - sort by end time and take every meeting that still fits",
                rationale = "Correct: finishing earliest always leaves the most room for what " +
                    "follows, which is what makes the greedy choice provably safe.",
            ),
            AnswerChoice(
                "b", "Dynamic programming over the meetings",
                rationale = "DP gives the right answer but is unnecessary work - the greedy " +
                    "choice here is provably optimal.",
                insight = "Not wrong, just heavier than it needs to be. Worth saying that you " +
                    "checked whether greedy was safe before using it.",
            ),
            AnswerChoice(
                "c", "Backtracking over every subset of meetings",
                rationale = "Correct but exponential, when a sort plus one pass suffices.",
                insight = "It is the honest brute force, and a fine thing to describe first.",
            ),
            AnswerChoice(
                "d", "Sliding window",
                rationale = "The meetings you attend need not be consecutive in the input.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "The exchange argument: if an optimal schedule does not start with the " +
            "earliest-finishing meeting, swapping it in never makes things worse. That proof is " +
            "what separates a greedy solution you can defend from one you are guessing at.",
        bestApproach = "Sort by end time, then walk the list taking any meeting whose start is " +
            "at or after the last end time you accepted.",
        timeComplexity = "O(n log n)",
        spaceComplexity = "O(1) beyond the sort",
        commonMistakes = listOf(
            "Sorting by start time or by duration, both of which give wrong answers",
            "Using greedy without being able to justify why the local choice is safe",
        ),
        hints = listOf(
            "Which meeting leaves the most room for everything after it?",
            "Try sorting by start time on [0,10], [1,2], [3,4] and see what happens.",
        ),
        patternId = "greedy",
        estimatedSeconds = 50,
    ),

    CodingProblem(
        id = "greedy-02",
        title = "Which key do you sort by?",
        description = "For the maximum non-overlapping meetings problem, which sort key makes the " +
            "greedy choice correct?",
        difficultyRating = 1350,
        primaryTopic = CodingTopic.GREEDY,
        secondaryTopics = listOf(CodingTopic.SORTING),
        challengeType = ChallengeType.MULTIPLE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "End time, ascending",
                rationale = "Correct: finishing earliest maximises the remaining time, and the " +
                    "exchange argument proves it optimal.",
            ),
            AnswerChoice(
                "b", "Start time, ascending",
                rationale = "Fails on [0,10], [1,2], [3,4]: it takes the long meeting first and " +
                    "gets one instead of two.",
                insight = "Sorting is definitely the right first move - the key is what decides " +
                    "whether the greedy step is safe.",
            ),
            AnswerChoice(
                "c", "Duration, ascending",
                rationale = "Fails on [0,4], [3,5], [4,8]: the shortest meeting sits in the " +
                    "middle and blocks both others.",
                insight = "Intuitive, and it beats sorting by start time - it is still not " +
                    "provably optimal though.",
            ),
            AnswerChoice(
                "d", "Number of overlaps, ascending",
                rationale = "Computing overlap counts costs more than the problem needs and still " +
                    "is not optimal.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Every wrong key here has a small counterexample, which is the point: the " +
            "difference between a greedy solution that works and one that does not is often a " +
            "single sort key. Reach for a counterexample before trusting one.",
        bestApproach = "Sort by end time ascending and take greedily.",
        timeComplexity = "O(n log n)",
        spaceComplexity = "O(1) beyond the sort",
        commonMistakes = listOf(
            "Assuming any sensible-looking sort key works",
            "Not testing the strategy against a small adversarial input",
        ),
        hints = listOf(
            "Try each key on [0,10], [1,2], [3,4].",
            "Which choice leaves the largest usable interval behind it?",
        ),
        patternId = "greedy",
        estimatedSeconds = 50,
    ),

    CodingProblem(
        id = "greedy-03",
        title = "When greedy quietly fails",
        description = "Making change with the fewest coins by always taking the largest coin that " +
            "fits. Which coin set breaks this?",
        difficultyRating = 1300,
        primaryTopic = CodingTopic.GREEDY,
        secondaryTopics = listOf(CodingTopic.DYNAMIC_PROGRAMMING),
        challengeType = ChallengeType.EDGE_CASE,
        choices = listOf(
            AnswerChoice(
                "a", "Coins [1, 3, 4], target 6",
                rationale = "Correct: greedy takes 4 then 1 then 1 (three coins). The optimum is " +
                    "3 + 3 (two coins).",
            ),
            AnswerChoice(
                "b", "Coins [1, 5, 10, 25], target 30",
                rationale = "Greedy gives 25 + 5, which is optimal. Standard currency systems are " +
                    "designed so greedy works.",
                insight = "Right that this is the familiar case - it is exactly why the failure " +
                    "is so easy to miss.",
            ),
            AnswerChoice(
                "c", "Coins [1, 2, 4, 8], target 15",
                rationale = "Greedy gives 8 + 4 + 2 + 1, which is optimal for powers of two.",
            ),
            AnswerChoice(
                "d", "Coins [1], target 7",
                rationale = "With a single denomination there is only one answer.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Greedy coin change is only optimal for certain coin systems. Real currency " +
            "happens to be one of them, which is what makes this trap so effective - the strategy " +
            "looks proven because it works on every example you have met.",
        bestApproach = "Use dynamic programming: dp[amount] = 1 + the best over every coin that " +
            "fits. That is correct for any coin set.",
        timeComplexity = "O(amount * coins) with DP",
        spaceComplexity = "O(amount)",
        commonMistakes = listOf(
            "Generalising from standard currency to arbitrary coin sets",
            "Using greedy without an exchange argument to back it",
        ),
        hints = listOf(
            "Try to build 6 from coins of 1, 3 and 4.",
            "Greedy takes the largest coin first. Is that always the fewest coins?",
        ),
        patternId = "greedy",
        estimatedSeconds = 55,
    ),

    // ------------------------------------------------------------------- tries

    CodingProblem(
        id = "trie-01",
        title = "Autocomplete over a dictionary",
        description = "You must return every word starting with a given prefix, over a fixed " +
            "dictionary of 200,000 words, many times per second. Which structure fits?",
        difficultyRating = 1250,
        primaryTopic = CodingTopic.TRIES,
        secondaryTopics = listOf(CodingTopic.STRINGS),
        challengeType = ChallengeType.DATA_STRUCTURE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "A trie",
                rationale = "Correct: walking the prefix costs its length, and everything below " +
                    "that node is an answer.",
            ),
            AnswerChoice(
                "b", "A hash set of words",
                rationale = "Hashing answers exact membership in O(1) but knows nothing about " +
                    "prefixes - you would have to scan all 200,000 words per query.",
                insight = "The right structure for \"is this exact word in the list?\", which is " +
                    "the neighbouring problem.",
            ),
            AnswerChoice(
                "c", "A sorted list with binary search",
                rationale = "This does work - prefix matches form a contiguous block - at " +
                    "O(log n) per query. A trie is the more direct fit but this is a defensible " +
                    "answer.",
                insight = "Genuinely good: sorting makes shared prefixes adjacent, and it is " +
                    "worth stating as an alternative.",
            ),
            AnswerChoice(
                "d", "A min-heap",
                rationale = "A heap orders by priority, not by prefix.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "A trie stores words by shared prefix, so the cost of a query depends on " +
            "the prefix length rather than the dictionary size. With a fixed dictionary and heavy " +
            "query load, paying once to build it is clearly worth it.",
        bestApproach = "Build a trie once, walk it to the prefix node, then collect every " +
            "complete word beneath.",
        timeComplexity = "O(prefix length) to locate, plus the number of matches",
        spaceComplexity = "O(total characters)",
        commonMistakes = listOf(
            "Building a trie for a single lookup, where a hash set is simpler",
            "Forgetting to mark word ends, so a prefix reads as a stored word",
        ),
        hints = listOf(
            "The query is about prefixes, not whole words.",
            "The dictionary never changes and is queried constantly - what can you precompute?",
        ),
        patternId = "trie",
        estimatedSeconds = 45,
    ),

    CodingProblem(
        id = "trie-02",
        title = "Completing a trie insert",
        description = "Which line belongs in the blank so that this inserts a word into a trie?",
        difficultyRating = 1350,
        primaryTopic = CodingTopic.TRIES,
        secondaryTopics = listOf(CodingTopic.STRINGS),
        challengeType = ChallengeType.FILL_IN_THE_BLANK,
        codeSnippet = """
            def insert(root, word):
                node = root
                for ch in word:
                    # ___ blank ___
                node["*"] = True     # mark a complete word
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "node = node.setdefault(ch, {})",
                rationale = "Correct: creates the child if missing, then descends into it either way.",
            ),
            AnswerChoice(
                "b", "node = node[ch]",
                rationale = "Descends correctly but raises a KeyError the first time a character " +
                    "has not been seen before - it never creates anything.",
                insight = "The descent is exactly right; what is missing is creating the node when " +
                    "it does not exist yet.",
            ),
            AnswerChoice(
                "c", "node[ch] = {}",
                rationale = "Creates a child but never descends, so every character overwrites " +
                    "the same level and the word is flattened.",
                insight = "You have the creation half; the cursor also has to move down.",
            ),
            AnswerChoice(
                "d", "root = root.setdefault(ch, {})",
                rationale = "Moves the root pointer instead of the cursor, corrupting the trie " +
                    "for every later insert.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Each step must do two things: make sure the child exists, and move the " +
            "cursor into it. setdefault does both in one call - it returns the existing child if " +
            "there is one, and otherwise inserts and returns a fresh dictionary.",
        bestApproach = "Walk the word one character at a time, creating missing nodes as you go, " +
            "then mark the final node as a word end.",
        timeComplexity = "O(length of the word)",
        spaceComplexity = "O(length of the word) in the worst case",
        commonMistakes = listOf(
            "Descending without creating, which fails on the first new character",
            "Creating without descending, which flattens every word into one level",
            "Forgetting the word-end marker entirely",
        ),
        hints = listOf(
            "Each iteration has to do two things, not one.",
            "What should happen when the character has never been inserted before?",
        ),
        patternId = "trie",
        estimatedSeconds = 55,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                void insert(TrieNode root, String word) {
                    TrieNode node = root;
                    for (char ch : word.toCharArray()) {
                        // ___ blank ___
                    }
                    node.isWord = true;
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "node = node.children.computeIfAbsent(ch, c -> new TrieNode());", rationale = "Correct: creates the child if missing, then descends into it either way."),
                    AnswerChoice("b", "node = node.children.get(ch);", rationale = "Descends correctly but returns null the first time a character has not been seen before - it never creates anything.", insight = "The descent is exactly right; what is missing is creating the node when it does not exist yet."),
                    AnswerChoice("c", "node.children.put(ch, new TrieNode());", rationale = "Creates a child but never descends, so every character overwrites the same level and the word is flattened.", insight = "You have the creation half; the cursor also has to move down."),
                    AnswerChoice("d", "root = root.children.computeIfAbsent(ch, c -> new TrieNode());", rationale = "Moves the root pointer instead of the cursor, corrupting the trie for every later insert."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function insert(root, word) {
                    let node = root;
                    for (const ch of word) {
                        // ___ blank ___
                    }
                    node["*"] = true;
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "node = node[ch] ??= {};", rationale = "Correct: creates the child if missing, then descends into it either way."),
                    AnswerChoice("b", "node = node[ch];", rationale = "Descends correctly but node becomes undefined the first time a character has not been seen before - it never creates anything.", insight = "The descent is exactly right; what is missing is creating the node when it does not exist yet."),
                    AnswerChoice("c", "node[ch] = {};", rationale = "Creates a child but never descends, so every character overwrites the same level and the word is flattened.", insight = "You have the creation half; the cursor also has to move down."),
                    AnswerChoice("d", "root = root[ch] ??= {};", rationale = "Moves the root pointer instead of the cursor, corrupting the trie for every later insert."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun insert(root: TrieNode, word: String) {
                    var node = root
                    for (ch in word) {
                        // ___ blank ___
                    }
                    node.isWord = true
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "node = node.children.getOrPut(ch) { TrieNode() }", rationale = "Correct: creates the child if missing, then descends into it either way."),
                    AnswerChoice("b", "node = node.children[ch]!!", rationale = "Descends correctly but throws the first time a character has not been seen before - it never creates anything.", insight = "The descent is exactly right; what is missing is creating the node when it does not exist yet."),
                    AnswerChoice("c", "node.children[ch] = TrieNode()", rationale = "Creates a child but never descends, so every character overwrites the same level and the word is flattened.", insight = "You have the creation half; the cursor also has to move down."),
                    AnswerChoice("d", "root.children[ch] = root.children.getOrPut(ch) { TrieNode() }", rationale = "Rewrites the root's own entry instead of moving the cursor down, corrupting the trie for every later insert."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                CPP,
                """
                void insert(TrieNode* root, string word) {
                    TrieNode* node = root;
                    for (char ch : word) {
                        // ___ blank ___
                    }
                    node->isWord = true;
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "node = node->children.emplace(ch, new TrieNode()).first->second;", rationale = "Correct: creates the child if missing, then descends into it either way."),
                    AnswerChoice("b", "node = node->children[ch];", rationale = "Silently yields a null child the first time a character has not been seen before - it never creates a real node.", insight = "The descent is exactly right; what is missing is creating the node when it does not exist yet."),
                    AnswerChoice("c", "node->children[ch] = new TrieNode();", rationale = "Creates a child but never descends, so every character overwrites the same level and the word is flattened.", insight = "You have the creation half; the cursor also has to move down."),
                    AnswerChoice("d", "root = root->children.emplace(ch, new TrieNode()).first->second;", rationale = "Moves the root pointer instead of the cursor, corrupting the trie for every later insert."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                GO,
                """
                func insert(root *TrieNode, word string) {
                    node := root
                    for _, ch := range word {
                        // ___ blank ___
                    }
                    node.IsWord = true
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "node = getOrCreate(node.Children, ch)", rationale = "Correct: creates the child if missing, then descends into it either way."),
                    AnswerChoice("b", "node = node.Children[ch]", rationale = "Descends correctly but node becomes nil the first time a character has not been seen before - it never creates anything.", insight = "The descent is exactly right; what is missing is creating the node when it does not exist yet."),
                    AnswerChoice("c", "node.Children[ch] = &TrieNode{}", rationale = "Creates a child but never descends, so every character overwrites the same level and the word is flattened.", insight = "You have the creation half; the cursor also has to move down."),
                    AnswerChoice("d", "root = getOrCreate(root.Children, ch)", rationale = "Moves the root pointer instead of the cursor, corrupting the trie for every later insert."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                SWIFT,
                """
                func insert(_ root: TrieNode, _ word: String) {
                    var node = root
                    for ch in word {
                        // ___ blank ___
                    }
                    node.isWord = true
                }
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "if node.children[ch] == nil { node.children[ch] = TrieNode() }\nnode = node.children[ch]!", rationale = "Correct: creates the child if missing, then descends into it either way."),
                    AnswerChoice("b", "node = node.children[ch]!", rationale = "Descends correctly but crashes the first time a character has not been seen before - it never creates anything.", insight = "The descent is exactly right; what is missing is creating the node when it does not exist yet."),
                    AnswerChoice("c", "node.children[ch] = TrieNode()", rationale = "Creates a child but never descends, so every character overwrites the same level and the word is flattened.", insight = "You have the creation half; the cursor also has to move down."),
                    AnswerChoice("d", "if root.children[ch] == nil { root.children[ch] = TrieNode() }\nroot = root.children[ch]!", rationale = "Moves the root pointer instead of the cursor, corrupting the trie for every later insert."),
                ),
                correctAnswerIds = listOf("a"),
            ),
        ),
    ),

    // -------------------------------------------------------- bit manipulation

    CodingProblem(
        id = "bits-01",
        title = "What does x & (x - 1) do?",
        description = "What does this print?",
        difficultyRating = 1150,
        primaryTopic = CodingTopic.BIT_MANIPULATION,
        challengeType = ChallengeType.OUTPUT_PREDICTION,
        codeSnippet = """
            x = 12          # binary 1100
            count = 0
            while x:
                x = x & (x - 1)
                count += 1
            print(count)
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "2",
                rationale = "Correct: 12 is 1100, which has two set bits, and each iteration " +
                    "clears exactly one.",
            ),
            AnswerChoice(
                "b", "4",
                rationale = "That is the number of bit positions in 1100, not the number of ones.",
                insight = "You read the binary correctly - the trick clears set bits, not positions.",
            ),
            AnswerChoice(
                "c", "12",
                rationale = "The loop does not decrement by one; it clears a whole bit each time.",
            ),
            AnswerChoice(
                "d", "3",
                rationale = "12 in binary is 1100, which has two ones, not three.",
                insight = "The right idea - counting set bits - just applied to the wrong binary " +
                    "value.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Subtracting 1 flips the lowest set bit to 0 and everything below it to 1; " +
            "the AND then wipes that whole tail. So each iteration removes exactly one set bit, " +
            "and the loop count is the population count. 1100 -> 1000 -> 0000.",
        bestApproach = "Recognise x & (x - 1) as \"clear the lowest set bit\", which makes this " +
            "a population count in O(number of set bits).",
        timeComplexity = "O(set bits), better than O(word size)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Confusing the number of bits with the number of set bits",
            "Assuming the loop runs once per bit position rather than once per one",
        ),
        hints = listOf(
            "Write 12 in binary, then write 11, then AND them.",
            "What happens to the lowest set bit each time round?",
        ),
        patternId = "bit-manipulation",
        estimatedSeconds = 45,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                int x = 12;
                int count = 0;
                while (x != 0) {
                    x = x & (x - 1);
                    count++;
                }
                System.out.println(count);
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                let x = 12;
                let count = 0;
                while (x) {
                    x = x & (x - 1);
                    count++;
                }
                console.log(count);
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                var x = 12
                var count = 0
                while (x != 0) {
                    x = x and (x - 1)
                    count++
                }
                println(count)
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                int x = 12;
                int count = 0;
                while (x) {
                    x = x & (x - 1);
                    count++;
                }
                cout << count;
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                x := 12
                count := 0
                for x != 0 {
                    x = x & (x - 1)
                    count++
                }
                fmt.Println(count)
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                var x = 12
                var count = 0
                while x != 0 {
                    x = x & (x - 1)
                    count += 1
                }
                print(count)
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "bits-02",
        title = "The number that appears once",
        description = "Every value in an array appears exactly twice except one. Find it in O(1) " +
            "space. Which approach works?",
        difficultyRating = 1250,
        primaryTopic = CodingTopic.BIT_MANIPULATION,
        secondaryTopics = listOf(CodingTopic.ARRAYS),
        challengeType = ChallengeType.MULTIPLE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "XOR every element together",
                rationale = "Correct: XOR cancels equal pairs to zero and leaves the lone value.",
            ),
            AnswerChoice(
                "b", "Count occurrences in a hash map",
                rationale = "Correct results, but it needs O(n) extra space and the question asks " +
                    "for constant.",
                insight = "The obvious right answer, and the one to give first if no space " +
                    "constraint were stated.",
            ),
            AnswerChoice(
                "c", "Sort, then scan for the element with no neighbour equal to it",
                rationale = "Works, but costs O(n log n) time and mutates the input.",
                insight = "Sorting to make duplicates adjacent is a sound instinct; it is just " +
                    "slower than XOR here.",
            ),
            AnswerChoice(
                "d", "Sum every element and compare with twice the set sum",
                rationale = "This does work, but it risks overflow on large inputs and needs a " +
                    "set anyway, so it is not constant space.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "XOR has three properties that combine perfectly here: x ^ x is 0, x ^ 0 is " +
            "x, and it is order-independent. So every pair annihilates regardless of position and " +
            "the survivor is the answer.",
        bestApproach = "Fold the array with XOR, starting from 0. One pass, one variable.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Reaching for a hash map without checking the space constraint",
            "Assuming XOR needs the array sorted or grouped - it does not",
        ),
        hints = listOf(
            "What is x ^ x? And x ^ 0?",
            "Does the order of the XORs matter?",
        ),
        patternId = "bit-manipulation",
        estimatedSeconds = 50,
    ),

    CodingProblem(
        id = "bits-03",
        title = "Trace the shifting",
        description = "What is the value of result when this returns, for n = 5?",
        difficultyRating = 1300,
        primaryTopic = CodingTopic.BIT_MANIPULATION,
        challengeType = ChallengeType.VARIABLE_TRACE,
        codeSnippet = """
            def f(n):
                result = 0
                while n > 0:
                    result = (result << 1) | (n & 1)
                    n = n >> 1
                return result
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "5",
                rationale = "Correct: 5 is 101, and reversing its bits gives 101 again - it is a " +
                    "palindrome in binary.",
            ),
            AnswerChoice(
                "b", "10",
                rationale = "That would be 5 shifted left once, but the loop consumes n rather " +
                    "than doubling it.",
                insight = "You spotted the left shift; it is being applied to the accumulator, " +
                    "not to n.",
            ),
            AnswerChoice(
                "c", "2",
                rationale = "That would be 5 shifted right once - only the first iteration.",
            ),
            AnswerChoice(
                "d", "3",
                rationale = "3 is 11 in binary; there is no step here that drops a bit.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "The function reverses the bits of n. It peels the lowest bit off n and " +
            "pushes it onto the bottom of result, shifting result up each time. 5 is 101, whose " +
            "reversal is 101 - which is why this input hides the behaviour and makes a good trace " +
            "question.",
        bestApproach = "Tabulate n and result in binary after each iteration rather than " +
            "reasoning about the whole loop at once.",
        timeComplexity = "O(bits in n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Reading the shift as applying to n instead of to the accumulator",
            "Testing only with a binary palindrome and concluding the function is the identity",
        ),
        hints = listOf(
            "Write n and result in binary after every iteration.",
            "n & 1 takes the lowest bit. Where does it end up in result?",
        ),
        patternId = "bit-manipulation",
        estimatedSeconds = 60,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                int f(int n) {
                    int result = 0;
                    while (n > 0) {
                        result = (result << 1) | (n & 1);
                        n = n >> 1;
                    }
                    return result;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function f(n) {
                    let result = 0;
                    while (n > 0) {
                        result = (result << 1) | (n & 1);
                        n = n >> 1;
                    }
                    return result;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun f(start: Int): Int {
                    var result = 0
                    var n = start
                    while (n > 0) {
                        result = (result shl 1) or (n and 1)
                        n = n shr 1
                    }
                    return result
                }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                int f(int n) {
                    int result = 0;
                    while (n > 0) {
                        result = (result << 1) | (n & 1);
                        n = n >> 1;
                    }
                    return result;
                }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                func f(n int) int {
                    result := 0
                    for n > 0 {
                        result = (result << 1) | (n & 1)
                        n = n >> 1
                    }
                    return result
                }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                func f(_ start: Int) -> Int {
                    var result = 0
                    var n = start
                    while n > 0 {
                        result = (result << 1) | (n & 1)
                        n = n >> 1
                    }
                    return result
                }
                """.trimIndent(),
            ),
        ),
    ),

    // -------------------------------------------------------------- union find

    CodingProblem(
        id = "unionfind-01",
        title = "Connections arriving one at a time",
        description = "\"Cables are added between servers one by one. After each cable, report " +
            "how many separate networks remain.\" Which pattern fits?",
        difficultyRating = 1400,
        primaryTopic = CodingTopic.GRAPHS,
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "Union Find",
                rationale = "Correct: groups only ever merge, and each merge is near-constant time.",
            ),
            AnswerChoice(
                "b", "Run a fresh BFS after every cable",
                rationale = "Correct answers, but re-traversing the whole graph per edge makes it " +
                    "O(E * (V + E)).",
                insight = "BFS is the right tool for connectivity on a *static* graph - the thing " +
                    "that changes it here is that edges keep arriving.",
            ),
            AnswerChoice(
                "c", "Topological sort",
                rationale = "That orders a directed acyclic graph; these connections are " +
                    "undirected and there is no ordering to produce.",
            ),
            AnswerChoice(
                "d", "Dijkstra's algorithm",
                rationale = "Shortest paths are not being asked for - only whether things are " +
                    "connected.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "The signal is incremental merging: groups combine over time and never " +
            "split. Union Find is built for exactly that, and it answers the count for free - " +
            "start at n components and subtract one every time a union actually joins two " +
            "different roots.",
        bestApproach = "Maintain a parent array with path compression and union by rank, keeping " +
            "a running component count.",
        timeComplexity = "Near O(1) amortised per operation",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Decrementing the component count even when both nodes already shared a root",
            "Skipping path compression and degrading to O(n) per query",
        ),
        hints = listOf(
            "Do the groups ever split, or only merge?",
            "What changes about the answer when a cable joins two servers already connected?",
        ),
        patternId = "union-find",
        estimatedSeconds = 55,
    ),

    CodingProblem(
        id = "unionfind-02",
        title = "Union Find that loses track",
        description = "This should report whether two nodes are connected, but it gets some pairs " +
            "wrong. Which line is at fault?",
        difficultyRating = 1450,
        primaryTopic = CodingTopic.GRAPHS,
        secondaryTopics = listOf(CodingTopic.DEBUGGING),
        challengeType = ChallengeType.FIND_THE_BUG,
        codeSnippet = """
            1  def find(parent, x):
            2      while parent[x] != x:
            3          x = parent[x]
            4      return x
            5
            6  def union(parent, a, b):
            7      parent[a] = b
            8
            9  def connected(parent, a, b):
            10     return find(parent, a) == find(parent, b)
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "Line 7", tag = "7",
                rationale = "Correct: it links the elements directly instead of their roots, so " +
                    "an earlier merge involving a is silently discarded.",
            ),
            AnswerChoice(
                "b", "Line 10", tag = "10",
                rationale = "Comparing roots is exactly right - that is what find is for.",
                insight = "This is the classic Union Find mistake, so suspecting it is sound. " +
                    "Here it is the one line that is correct.",
            ),
            AnswerChoice(
                "c", "Line 2", tag = "2",
                rationale = "Walking up until a node is its own parent is the correct root test.",
            ),
            AnswerChoice(
                "d", "Line 3", tag = "3",
                rationale = "Ascending one level at a time is correct; it is merely slow without " +
                    "path compression, which is not what breaks correctness here.",
                insight = "You are right that this line is suboptimal - it costs speed, not " +
                    "correctness.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Union must operate on roots. Setting parent[a] = b overwrites whatever a " +
            "already pointed at, detaching a's existing tree. The fix is " +
            "parent[find(a)] = find(b), which merges the two trees rather than reassigning a leaf.",
        bestApproach = "Find both roots first, return early if they match, then point one root at " +
            "the other - ideally by rank.",
        timeComplexity = "Near O(1) amortised with compression and rank",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Unioning elements rather than their roots",
            "Comparing elements directly instead of comparing roots",
        ),
        hints = listOf(
            "Trace union(1,2) then union(1,3), then ask whether 2 and 3 are connected.",
            "What does union need to look up before it changes anything?",
        ),
        patternId = "union-find",
        estimatedSeconds = 65,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                1  int find(int[] parent, int x) {
                2      while (parent[x] != x) {
                3          x = parent[x];
                4      }
                5      return x;
                6  }
                7
                8  void union(int[] parent, int a, int b) {
                9      parent[a] = b;
                10 }
                11
                12 boolean connected(int[] parent, int a, int b) {
                13     return find(parent, a) == find(parent, b);
                14 }
                """.trimIndent(),
                choices = unionFindBraceChoicesGroupA,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                1  function find(parent, x) {
                2      while (parent[x] !== x) {
                3          x = parent[x];
                4      }
                5      return x;
                6  }
                7
                8  function union(parent, a, b) {
                9      parent[a] = b;
                10 }
                11
                12 function connected(parent, a, b) {
                13     return find(parent, a) === find(parent, b);
                14 }
                """.trimIndent(),
                choices = unionFindBraceChoicesGroupA,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                KOTLIN,
                """
                1  fun find(parent: IntArray, start: Int): Int {
                2      var x = start
                3      while (parent[x] != x) {
                4          x = parent[x]
                5      }
                6      return x
                7  }
                8
                9  fun union(parent: IntArray, a: Int, b: Int) {
                10     parent[a] = b
                11 }
                12
                13 fun connected(parent: IntArray, a: Int, b: Int): Boolean {
                14     return find(parent, a) == find(parent, b)
                15 }
                """.trimIndent(),
                choices = unionFindBraceChoicesGroupB,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                CPP,
                """
                1  int find(vector<int>& parent, int x) {
                2      while (parent[x] != x) {
                3          x = parent[x];
                4      }
                5      return x;
                6  }
                7
                8  void unionSets(vector<int>& parent, int a, int b) {
                9      parent[a] = b;
                10 }
                11
                12 bool connected(vector<int>& parent, int a, int b) {
                13     return find(parent, a) == find(parent, b);
                14 }
                """.trimIndent(),
                choices = unionFindBraceChoicesGroupA,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                GO,
                """
                1  func find(parent []int, x int) int {
                2      for parent[x] != x {
                3          x = parent[x]
                4      }
                5      return x
                6  }
                7
                8  func union(parent []int, a int, b int) {
                9      parent[a] = b
                10 }
                11
                12 func connected(parent []int, a int, b int) bool {
                13     return find(parent, a) == find(parent, b)
                14 }
                """.trimIndent(),
                choices = unionFindBraceChoicesGroupA,
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                SWIFT,
                """
                1  func find(_ parent: [Int], _ start: Int) -> Int {
                2      var x = start
                3      while parent[x] != x {
                4          x = parent[x]
                5      }
                6      return x
                7  }
                8
                9  func union(_ parent: inout [Int], _ a: Int, _ b: Int) {
                10     parent[a] = b
                11 }
                12
                13 func connected(_ parent: [Int], _ a: Int, _ b: Int) -> Bool {
                14     return find(parent, a) == find(parent, b)
                15 }
                """.trimIndent(),
                choices = unionFindBraceChoicesGroupB,
                correctAnswerIds = listOf("a"),
            ),
        ),
    ),

    // --------------------------------------------------------- topological sort

    CodingProblem(
        id = "toposort-01",
        title = "Courses with prerequisites",
        description = "\"Given courses and their prerequisites, produce an order in which they " +
            "can all be taken, or report that it is impossible.\" Which pattern fits?",
        difficultyRating = 1350,
        primaryTopic = CodingTopic.GRAPHS,
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "Topological sort",
                rationale = "Correct: prerequisites are directed edges, and a valid order is " +
                    "exactly a topological ordering. Impossible means there is a cycle.",
            ),
            AnswerChoice(
                "b", "Breadth-first search from each course",
                rationale = "BFS explores reachability but produces no ordering that respects " +
                    "every prerequisite at once.",
                insight = "Close - Kahn's algorithm for topological sort is BFS with indegrees " +
                    "bolted on, so the instinct is nearly there.",
            ),
            AnswerChoice(
                "c", "Union Find",
                rationale = "Union Find handles undirected connectivity. Prerequisites have " +
                    "direction, and direction is the whole problem.",
                insight = "The right family of tool for grouping - it just cannot represent " +
                    "\"before\" and \"after\".",
            ),
            AnswerChoice(
                "d", "Greedy by number of prerequisites",
                rationale = "Taking the fewest-prerequisite course first is roughly what Kahn's " +
                    "algorithm does, but without maintaining indegrees it does not stay correct.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "\"A before B\" constraints over a set of tasks is the definition of a " +
            "topological ordering. The second half of the question matters just as much: if the " +
            "ordering cannot include every course, the prerequisite graph contains a cycle.",
        bestApproach = "Kahn's algorithm: queue every zero-indegree node, emit it, decrement its " +
            "neighbours, and check at the end that every node was emitted.",
        timeComplexity = "O(V + E)",
        spaceComplexity = "O(V + E)",
        commonMistakes = listOf(
            "Not checking that every node was emitted, so a cycle goes unreported",
            "Building the indegree map from the wrong edge direction",
        ),
        hints = listOf(
            "What kind of constraint is \"A must come before B\"?",
            "What does it mean if no valid order exists?",
        ),
        patternId = "topological-sort",
        estimatedSeconds = 55,
    ),

    CodingProblem(
        id = "toposort-02",
        title = "Assemble Kahn's algorithm",
        description = "Put these lines in order to produce a topological sort that also detects " +
            "cycles.",
        difficultyRating = 1400,
        primaryTopic = CodingTopic.GRAPHS,
        secondaryTopics = listOf(CodingTopic.QUEUES),
        challengeType = ChallengeType.REARRANGE_CODE,
        choices = listOf(
            AnswerChoice("k1", "queue = [n for n in nodes if indegree[n] == 0]"),
            AnswerChoice("k2", "order = []"),
            AnswerChoice("k3", "while queue:"),
            AnswerChoice("k4", "    node = queue.pop()"),
            AnswerChoice("k5", "    order.append(node)"),
            AnswerChoice("k6", "    for nxt in graph[node]:"),
            AnswerChoice("k7", "        indegree[nxt] -= 1"),
            AnswerChoice("k8", "        if indegree[nxt] == 0: queue.append(nxt)"),
            AnswerChoice("k9", "return order if len(order) == len(nodes) else []"),
        ),
        correctAnswerIds = listOf("k1", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9"),
        explanation = "Seed the queue with everything that has no prerequisites, then repeatedly " +
            "emit a node and relieve its dependents. A node joins the queue only at the moment " +
            "its last prerequisite is emitted. The final length check is the cycle detector: a " +
            "short result means some nodes never reached indegree zero.",
        bestApproach = "Kahn's algorithm - queue the zero-indegree nodes, emit and decrement, " +
            "then verify every node was emitted.",
        timeComplexity = "O(V + E)",
        spaceComplexity = "O(V + E)",
        commonMistakes = listOf(
            "Dropping the final length check, which silently returns a partial order on a cycle",
            "Enqueueing a node before its indegree actually reaches zero",
        ),
        hints = listOf(
            "What can safely be taken before anything else has been done?",
            "How would you notice that a cycle made the ordering impossible?",
        ),
        patternId = "topological-sort",
        estimatedSeconds = 80,
    ),

    // ------------------------------------------- reinforcing thin pattern areas

    CodingProblem(
        id = "heap-02",
        title = "Keeping the heap at size k",
        description = "This keeps the k largest values seen so far. Which line belongs in the " +
            "blank?",
        difficultyRating = 1250,
        primaryTopic = CodingTopic.HEAPS,
        challengeType = ChallengeType.FILL_IN_THE_BLANK,
        codeSnippet = """
            import heapq

            def top_k(nums, k):
                heap = []
                for n in nums:
                    heapq.heappush(heap, n)
                    # ___ blank ___
                return heap
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "if len(heap) > k: heapq.heappop(heap)",
                rationale = "Correct: a min-heap's root is its smallest element, so popping when " +
                    "the heap overflows discards the weakest candidate.",
            ),
            AnswerChoice(
                "b", "if len(heap) > k: heap.pop()",
                rationale = "list.pop() removes the last element of the backing array, which is " +
                    "not the heap's smallest - it corrupts the invariant.",
                insight = "The condition is exactly right; the removal has to go through heapq to " +
                    "preserve the heap property.",
            ),
            AnswerChoice(
                "c", "if len(heap) < k: heapq.heappop(heap)",
                rationale = "Backwards: this pops while the heap is still too small, so it never " +
                    "accumulates k elements.",
                insight = "Right operation, inverted comparison.",
            ),
            AnswerChoice(
                "d", "heapq.heappop(heap)",
                rationale = "Popping unconditionally removes one element for every one pushed, so " +
                    "the heap never grows past one.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "The counter-intuitive part of top-k is using a *min*-heap: its root is the " +
            "weakest of your current best k, which is exactly the element to evict when a better " +
            "one arrives. Capping the size is what keeps this O(n log k) rather than O(n log n).",
        bestApproach = "Push every element, then pop whenever the heap grows past k.",
        timeComplexity = "O(n log k)",
        spaceComplexity = "O(k)",
        commonMistakes = listOf(
            "Using list.pop() and breaking the heap invariant",
            "Letting the heap grow to n when only k elements are ever needed",
        ),
        hints = listOf(
            "Which element sits at the root of a min-heap?",
            "When exactly is the heap one element too big?",
        ),
        patternId = "heap",
        estimatedSeconds = 50,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                PriorityQueue<Integer> heap = new PriorityQueue<>();
                for (int n : nums) {
                    heap.offer(n);
                    // ___ blank ___
                }
                return heap;
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "if (heap.size() > k) heap.poll();", rationale = "Correct: a min-heap's head is its smallest element, so polling when the heap overflows discards the weakest candidate."),
                    AnswerChoice("b", "if (heap.size() > k) heap.remove(Collections.max(heap));", rationale = "That removes the largest element instead of the smallest, which is exactly backwards for keeping the top k.", insight = "The condition is exactly right; it is the choice of which element to remove that is inverted."),
                    AnswerChoice("c", "if (heap.size() < k) heap.poll();", rationale = "Backwards: this polls while the heap is still too small, so it never accumulates k elements.", insight = "Right operation, inverted comparison."),
                    AnswerChoice("d", "heap.poll();", rationale = "Polling unconditionally removes one element for every one pushed, so the heap never grows past one."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                const heap = new MinHeap();
                for (const n of nums) {
                    heap.push(n);
                    // ___ blank ___
                }
                return heap;
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "if (heap.size > k) heap.pop();", rationale = "Correct: a min-heap's root is its smallest element, so popping when the heap overflows discards the weakest candidate."),
                    AnswerChoice("b", "if (heap.size > k) heap.removeMax();", rationale = "That removes the largest element instead of the smallest, which is exactly backwards for keeping the top k.", insight = "The condition is exactly right; it is the choice of which element to remove that is inverted."),
                    AnswerChoice("c", "if (heap.size < k) heap.pop();", rationale = "Backwards: this pops while the heap is still too small, so it never accumulates k elements.", insight = "Right operation, inverted comparison."),
                    AnswerChoice("d", "heap.pop();", rationale = "Popping unconditionally removes one element for every one pushed, so the heap never grows past one."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                KOTLIN,
                """
                val heap = PriorityQueue<Int>()
                for (n in nums) {
                    heap.offer(n)
                    // ___ blank ___
                }
                return heap
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "if (heap.size > k) heap.poll()", rationale = "Correct: a min-heap's head is its smallest element, so polling when the heap overflows discards the weakest candidate."),
                    AnswerChoice("b", "if (heap.size > k) heap.remove(heap.max())", rationale = "That removes the largest element instead of the smallest, which is exactly backwards for keeping the top k.", insight = "The condition is exactly right; it is the choice of which element to remove that is inverted."),
                    AnswerChoice("c", "if (heap.size < k) heap.poll()", rationale = "Backwards: this polls while the heap is still too small, so it never accumulates k elements.", insight = "Right operation, inverted comparison."),
                    AnswerChoice("d", "heap.poll()", rationale = "Polling unconditionally removes one element for every one pushed, so the heap never grows past one."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                CPP,
                """
                priority_queue<int, vector<int>, greater<int>> heap;
                for (int n : nums) {
                    heap.push(n);
                    // ___ blank ___
                }
                return heap;
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "if (heap.size() > k) heap.pop();", rationale = "Correct: a min-heap's top is its smallest element, so popping when the heap overflows discards the weakest candidate."),
                    AnswerChoice("b", "if (heap.size() > k) { heap.pop(); heap.push(heap.top()); }", rationale = "That pops the smallest and immediately pushes it back, which is a no-op that leaves the heap one too large.", insight = "The overflow check is right; the removal itself undoes nothing useful."),
                    AnswerChoice("c", "if (heap.size() < k) heap.pop();", rationale = "Backwards: this pops while the heap is still too small, so it never accumulates k elements.", insight = "Right operation, inverted comparison."),
                    AnswerChoice("d", "heap.pop();", rationale = "Popping unconditionally removes one element for every one pushed, so the heap never grows past one."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                GO,
                """
                for _, n := range nums {
                    heap.Push(h, n)
                    // ___ blank ___
                }
                return h
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "if h.Len() > k { heap.Pop(h) }", rationale = "Correct: heap.Pop re-heapifies after removing the root, so the weakest candidate is discarded correctly."),
                    AnswerChoice("b", "if h.Len() > k { h.Pop() }", rationale = "Calling the interface's raw Pop directly skips container/heap's re-heapify step, corrupting the heap invariant.", insight = "The condition is exactly right; the removal has to go through the heap package to preserve the invariant."),
                    AnswerChoice("c", "if h.Len() < k { heap.Pop(h) }", rationale = "Backwards: this pops while the heap is still too small, so it never accumulates k elements.", insight = "Right operation, inverted comparison."),
                    AnswerChoice("d", "heap.Pop(h)", rationale = "Popping unconditionally removes one element for every one pushed, so the heap never grows past one."),
                ),
                correctAnswerIds = listOf("a"),
            ),
            CodeVariant(
                SWIFT,
                """
                var heap = MinHeap()
                for n in nums {
                    heap.push(n)
                    // ___ blank ___
                }
                return heap
                """.trimIndent(),
                choices = listOf(
                    AnswerChoice("a", "if heap.count > k { heap.pop() }", rationale = "Correct: a min-heap's root is its smallest element, so popping when the heap overflows discards the weakest candidate."),
                    AnswerChoice("b", "if heap.count > k { heap.removeMax() }", rationale = "That removes the largest element instead of the smallest, which is exactly backwards for keeping the top k.", insight = "The condition is exactly right; it is the choice of which element to remove that is inverted."),
                    AnswerChoice("c", "if heap.count < k { heap.pop() }", rationale = "Backwards: this pops while the heap is still too small, so it never accumulates k elements.", insight = "Right operation, inverted comparison."),
                    AnswerChoice("d", "heap.pop()", rationale = "Popping unconditionally removes one element for every one pushed, so the heap never grows past one."),
                ),
                correctAnswerIds = listOf("a"),
            ),
        ),
    ),

    CodingProblem(
        id = "dp-02",
        title = "Trace the DP table",
        description = "This counts the ways to climb n stairs taking 1 or 2 steps. What is " +
            "dp[4] when the loop finishes for n = 4?",
        difficultyRating = 1250,
        primaryTopic = CodingTopic.DYNAMIC_PROGRAMMING,
        challengeType = ChallengeType.VARIABLE_TRACE,
        codeSnippet = """
            def climb(n):
                dp = [0] * (n + 1)
                dp[0] = 1
                dp[1] = 1
                for i in range(2, n + 1):
                    dp[i] = dp[i - 1] + dp[i - 2]
                return dp[n]
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "5",
                rationale = "Correct: dp is 1, 1, 2, 3, 5 - the Fibonacci sequence shifted by one.",
            ),
            AnswerChoice(
                "b", "4",
                rationale = "That is dp[3] + 1, or a miscount that treats the two base cases as " +
                    "one.",
                insight = "You are close - one base case off is the usual cause of this answer.",
            ),
            AnswerChoice(
                "c", "8",
                rationale = "That is dp[5]; the loop stops at i = 4.",
                insight = "The recurrence is right, just carried one step too far.",
            ),
            AnswerChoice(
                "d", "3",
                rationale = "That is dp[3]. Two more steps of the recurrence remain.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "To reach stair i you arrived from i-1 or i-2, so the ways add. Filling in: " +
            "dp[2] = 2, dp[3] = 3, dp[4] = 5. Getting the base cases right is most of the " +
            "difficulty - dp[0] = 1 encodes the single way to stand still.",
        bestApproach = "Write the table out left to right; only the last two entries are ever " +
            "needed, so this reduces to two variables and O(1) space.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(n), reducible to O(1)",
        commonMistakes = listOf(
            "Getting the base cases wrong, which shifts every later answer",
            "Recursing without memoisation and paying exponential time",
        ),
        hints = listOf(
            "Fill in dp[2], then dp[3], then dp[4].",
            "Which stairs can you have come from to land on stair i?",
        ),
        patternId = "dynamic-programming",
        estimatedSeconds = 55,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                int climb(int n) {
                    int[] dp = new int[n + 1];
                    dp[0] = 1;
                    dp[1] = 1;
                    for (int i = 2; i <= n; i++) {
                        dp[i] = dp[i - 1] + dp[i - 2];
                    }
                    return dp[n];
                }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function climb(n) {
                    const dp = new Array(n + 1).fill(0);
                    dp[0] = 1;
                    dp[1] = 1;
                    for (let i = 2; i <= n; i++) {
                        dp[i] = dp[i - 1] + dp[i - 2];
                    }
                    return dp[n];
                }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun climb(n: Int): Int {
                    val dp = IntArray(n + 1)
                    dp[0] = 1
                    dp[1] = 1
                    for (i in 2..n) {
                        dp[i] = dp[i - 1] + dp[i - 2]
                    }
                    return dp[n]
                }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                int climb(int n) {
                    vector<int> dp(n + 1, 0);
                    dp[0] = 1;
                    dp[1] = 1;
                    for (int i = 2; i <= n; i++) {
                        dp[i] = dp[i - 1] + dp[i - 2];
                    }
                    return dp[n];
                }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                func climb(n int) int {
                    dp := make([]int, n+1)
                    dp[0] = 1
                    dp[1] = 1
                    for i := 2; i <= n; i++ {
                        dp[i] = dp[i-1] + dp[i-2]
                    }
                    return dp[n]
                }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                func climb(_ n: Int) -> Int {
                    var dp = [Int](repeating: 0, count: n + 1)
                    dp[0] = 1
                    dp[1] = 1
                    for i in 2...n {
                        dp[i] = dp[i - 1] + dp[i - 2]
                    }
                    return dp[n]
                }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "sorting-02",
        title = "Why merge sort is n log n",
        description = "What is the time complexity of merge sort, and where does each factor come " +
            "from?",
        difficultyRating = 1150,
        primaryTopic = CodingTopic.COMPLEXITY,
        secondaryTopics = listOf(CodingTopic.SORTING),
        challengeType = ChallengeType.TIME_COMPLEXITY,
        choices = listOf(
            AnswerChoice(
                "a", "O(n log n) - log n levels of splitting, O(n) merging work per level",
                rationale = "Correct, and the reasoning matters as much as the answer.",
            ),
            AnswerChoice(
                "b", "O(n^2) - every element is compared with every other",
                rationale = "That is selection or bubble sort. Merge sort never compares all pairs.",
                insight = "Right shape of reasoning for the quadratic sorts, wrong algorithm.",
            ),
            AnswerChoice(
                "c", "O(n) - each element is visited a constant number of times",
                rationale = "Each element is touched once per level, and there are log n levels - " +
                    "not a constant.",
                insight = "You correctly spotted that merging is linear; it just happens log n " +
                    "times.",
            ),
            AnswerChoice(
                "d", "O(log n) - the array is halved each time",
                rationale = "That counts the splits but ignores the merging, which touches every " +
                    "element at every level.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Halving until you reach single elements takes log2(n) levels. At each " +
            "level every element is merged exactly once, which is O(n). Multiply for O(n log n) - " +
            "and unlike quicksort, this is the worst case as well as the average.",
        bestApproach = "Separate the recursion depth from the work done per level, then multiply.",
        timeComplexity = "O(n log n)",
        spaceComplexity = "O(n) for the merge buffer",
        commonMistakes = listOf(
            "Quoting O(n log n) without being able to say where each factor comes from",
            "Forgetting merge sort's O(n) auxiliary space when comparing it with quicksort",
        ),
        hints = listOf(
            "How many times can the array be halved?",
            "How much work happens across one whole level of the recursion?",
        ),
        patternId = "sorting",
        estimatedSeconds = 45,
    ),
)
