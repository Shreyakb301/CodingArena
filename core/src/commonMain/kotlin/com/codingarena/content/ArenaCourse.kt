package com.codingarena.content

import com.codingarena.domain.model.Chapter
import com.codingarena.domain.model.ConceptBlock
import com.codingarena.domain.model.Course
import com.codingarena.domain.model.ExampleTest
import com.codingarena.domain.model.Exercise
import com.codingarena.domain.model.ExerciseMode
import com.codingarena.domain.model.LanguageTemplate
import com.codingarena.domain.model.Lesson
import com.codingarena.domain.model.ProgrammingLanguage

/**
 * The first publishable course. Advanced ranks are visible as coming soon so
 * the path communicates its destination without pretending unfinished content
 * is available.
 */
object ArenaCourse {
    val course = Course(
        id = "coding-foundations",
        title = "Coding Foundations to Pattern Mastery",
        description = "Learn the moves, solve without guidance, then keep the pattern sharp.",
        chapters = buildList {
            add(chapter("variables", "Variables & Values", 1, "Store and update the state a solution needs.",
                signals = listOf("A value changes while the program runs", "You need to remember a result")))
            add(chapter("conditionals", "Conditionals", 2, "Choose a move based on the current position.",
                prerequisite = "variables", signals = listOf("The behavior depends on a condition", "The prompt says if, unless, or otherwise")))
            add(chapter("loops", "Loops", 3, "Repeat a move safely across a sequence.",
                prerequisite = "conditionals", signals = listOf("Visit every item", "Repeat until a condition changes")))
            add(chapter("functions", "Functions", 4, "Package one responsibility behind a clear input and output.",
                prerequisite = "loops", signals = listOf("The same operation is reused", "A large solution has separable steps")))
            add(chapter("debugging", "Debugging", 5, "Trace state and isolate the first incorrect move.",
                prerequisite = "functions", signals = listOf("Observed output differs from expected output", "An edge case changes a boundary")))
            add(chapter("arrays", "Arrays & Strings", 6, "Traverse indexed sequences and reason about boundaries.",
                prerequisite = "debugging", patternId = "array-traversal",
                signals = listOf("Ordered values are accessed by position", "The answer depends on every element")))
            add(chapter("sets-maps", "Sets & Maps", 7, "Trade memory for fast membership and counting.",
                prerequisite = "arrays", patternId = "frequency-map",
                signals = listOf("Have I seen this before?", "Count, duplicate, anagram, or frequency")))
            add(chapter("hashing", "Arrays & Hashing", 8, "Recognize when stored complements beat repeated scanning.",
                prerequisite = "sets-maps", patternId = "frequency-map",
                signals = listOf("A nested scan searches for a match", "Original indices must be preserved")))

            val later = listOf(
                "two-pointers" to "Two Pointers",
                "sliding-window" to "Sliding Window",
                "binary-search" to "Binary Search",
                "stacks-queues" to "Stacks & Queues",
                "linked-lists" to "Linked Lists",
                "recursion" to "Recursion",
                "trees" to "Tree Traversal",
                "graphs" to "Graphs",
                "heaps" to "Heaps",
                "backtracking" to "Backtracking",
                "greedy" to "Greedy",
                "dynamic-programming" to "Dynamic Programming",
                "tries" to "Tries",
                "intervals" to "Intervals",
                "bit-manipulation" to "Bit Manipulation",
            )
            var previous = "hashing"
            later.forEachIndexed { index, (id, title) ->
                add(
                    Chapter(
                        id = id,
                        title = title,
                        rank = index + 9,
                        summary = "An advanced rank that unlocks after the foundations course expands.",
                        prerequisiteIds = listOf(previous),
                        lessons = emptyList(),
                        exercises = emptyList(),
                        patternId = id,
                        available = false,
                    )
                )
                previous = id
            }
        },
    )

    val availableChapters: List<Chapter> get() = course.chapters.filter { it.available }

    private fun chapter(
        id: String,
        title: String,
        rank: Int,
        summary: String,
        prerequisite: String? = null,
        patternId: String? = null,
        signals: List<String>,
    ): Chapter {
        val blockId = "$id-core"
        val lesson = Lesson(
            id = "$id-lesson",
            title = "Learn the position",
            blocks = listOf(
                ConceptBlock(
                    id = blockId,
                    title = title,
                    explanation = summary,
                    visualExample = visualFor(id),
                    recognitionSignals = signals,
                )
            ),
        )
        val checkpoint = Exercise(
            id = "$id-checkpoint",
            chapterId = id,
            title = "$title checkpoint",
            prompt = "Which signal most strongly suggests using $title?",
            mode = ExerciseMode.CHECKPOINT,
            conceptIds = listOf(blockId),
            recognitionSignals = signals,
            difficultyRating = 700 + rank * 55,
            patternId = patternId,
            choices = signals + "The variable names are short",
            correctChoiceIndex = 0,
            explanation = "Choose an approach from the structure of the problem, not cosmetic details.",
        )
        val independent = independentExercise(id, title, rank, blockId, patternId, signals)
        val guidedMode = listOf(
            ExerciseMode.TRACE,
            ExerciseMode.REARRANGE,
            ExerciseMode.DEBUG,
            ExerciseMode.FILL_CODE,
        )[(rank - 1) % 4]
        val guided = Exercise(
            id = "$id-guided",
            chapterId = id,
            title = "Guided $title position",
            prompt = "Use $title to identify the only move that keeps the program correct.",
            mode = guidedMode,
            conceptIds = listOf(blockId),
            recognitionSignals = signals,
            difficultyRating = 680 + rank * 55,
            patternId = patternId,
            choices = listOf("Apply the chapter signal", "Ignore the boundary", "Restart with a random approach"),
            correctChoiceIndex = 0,
            explanation = "The first move follows the invariant taught in this chapter.",
        )
        val blitz = Exercise(
            id = "$id-blitz",
            chapterId = id,
            title = "$title: choose the move",
            prompt = scenarioFor(id),
            mode = ExerciseMode.BLITZ,
            conceptIds = listOf(blockId),
            recognitionSignals = signals,
            difficultyRating = 720 + rank * 55,
            patternId = patternId,
            choices = blitzChoices(id),
            correctChoiceIndex = 0,
            explanation = blitzExplanation(id),
        )
        return Chapter(
            id = id,
            title = title,
            rank = rank,
            summary = summary,
            prerequisiteIds = listOfNotNull(prerequisite),
            lessons = listOf(lesson),
            exercises = listOf(guided, checkpoint, independent, blitz),
            patternId = patternId,
        )
    }

    private fun independentExercise(
        id: String,
        title: String,
        rank: Int,
        conceptId: String,
        patternId: String?,
        signals: List<String>,
    ): Exercise = Exercise(
        id = "$id-independent",
        chapterId = id,
        title = "Independent $title solve",
        prompt = independentPrompt(id),
        mode = ExerciseMode.INDEPENDENT_CODE,
        conceptIds = listOf(conceptId),
        recognitionSignals = signals,
        difficultyRating = 760 + rank * 60,
        patternId = patternId,
        templates = templatesFor(id),
        examples = examplesFor(id),
        hiddenTestCount = 4,
        explanation = "First identify the chapter signal, then implement the smallest correct solution.",
    )

    private fun visualFor(id: String): String = when (id) {
        "variables" -> "score = 0  ->  score = score + 1  ->  1"
        "conditionals" -> "if score >= target:\n    return 'win'\nelse:\n    return 'keep playing'"
        "loops" -> "[3, 1, 4]\n ^  ->  ^  ->  ^"
        "functions" -> "input  ->  solve(input)  ->  output"
        "debugging" -> "expected: 6\nactual:   3\nfirst wrong state: index 2"
        "arrays" -> "values = [4, 7, 2]\nindices =  0  1  2"
        "sets-maps" -> "seen: {} -> {4} -> {4, 7}"
        else -> "[2, 7, 11], target 9\nneed 7 -> store 2\nneed 2 -> found"
    }

    private fun scenarioFor(id: String): String = when (id) {
        "variables" -> "Track the highest score seen while reading results."
        "conditionals" -> "Return a different label for negative, zero, and positive values."
        "loops" -> "Inspect every value in a list exactly once."
        "functions" -> "The same validation is needed in three places."
        "debugging" -> "A loop works for three items but crashes on an empty list."
        "arrays" -> "Find the maximum value in an unsorted array."
        "sets-maps" -> "Report whether any value appears twice."
        else -> "Find two values that sum to a target while preserving their indices."
    }

    private fun blitzChoices(id: String): List<String> = when (id) {
        "variables" -> listOf("Running variable", "Binary search", "Recursion", "Graph traversal")
        "conditionals" -> listOf("Conditional branches", "Hash map", "Two pointers", "Queue")
        "loops" -> listOf("Single traversal", "Binary search", "Backtracking", "Heap")
        "functions" -> listOf("Extract a function", "Duplicate the code", "Global state", "Nested loops")
        "debugging" -> listOf("Trace the first bad state", "Rewrite everything", "Add random delays", "Sort the input")
        "arrays" -> listOf("Array traversal", "Graph search", "Trie", "Backtracking")
        "sets-maps" -> listOf("Set membership", "Two pointers", "Binary search", "Recursion")
        else -> listOf("Hash map complements", "Nested brute force", "Sliding window", "Binary search")
    }

    private fun blitzExplanation(id: String): String = when (id) {
        "hashing" -> "A complement map preserves indices and reduces repeated searching from quadratic to linear time."
        "sets-maps" -> "A set directly represents the question 'have I seen this value before?'."
        else -> "The first option matches the structural signal in the scenario."
    }

    private fun independentPrompt(id: String): String = when (id) {
        "variables" -> "Read integers and return the largest value."
        "conditionals" -> "Return -1, 0, or 1 according to the sign of an integer."
        "loops" -> "Return the sum of all integers in the input array."
        "functions" -> "Implement a function that returns whether an integer is even."
        "debugging" -> "Repair the function so it returns 0 for an empty array and the maximum otherwise."
        "arrays" -> "Return the index of the first occurrence of a target, or -1."
        "sets-maps" -> "Return true when the array contains a duplicate."
        else -> "Return the indices of two values whose sum equals the target."
    }

    private fun examplesFor(id: String): List<ExampleTest> = when (id) {
        "sets-maps" -> listOf(ExampleTest("[1,2,1]", "true"), ExampleTest("[1,2,3]", "false"))
        "hashing" -> listOf(ExampleTest("[2,7,11,15]; 9", "[0,1]"))
        "conditionals" -> listOf(ExampleTest("-8", "-1"), ExampleTest("0", "0"))
        "loops" -> listOf(ExampleTest("[3,1,4]", "8"))
        "functions" -> listOf(ExampleTest("8", "true"), ExampleTest("7", "false"))
        "debugging" -> listOf(ExampleTest("[]", "0"), ExampleTest("[-4,-2]", "-2"))
        "arrays" -> listOf(ExampleTest("[4,7,2]; 7", "1"), ExampleTest("[4,7,2]; 9", "-1"))
        else -> listOf(ExampleTest("[3,1,4]", "4"))
    }

    private fun templatesFor(id: String): List<LanguageTemplate> {
        return listOf(
            LanguageTemplate(ProgrammingLanguage.PYTHON, "import sys\n\ndef solve(raw):\n    # Parse raw input, then make your move\n    pass\n\nprint(solve(sys.stdin.read().strip()))"),
            LanguageTemplate(ProgrammingLanguage.JAVA, "import java.io.*;\nclass Main {\n  static Object solve(String raw) {\n    // Parse raw input, then make your move\n    return null;\n  }\n  public static void main(String[] args) throws Exception {\n    System.out.print(solve(new String(System.in.readAllBytes()).trim()));\n  }\n}"),
            LanguageTemplate(ProgrammingLanguage.JAVASCRIPT, "const fs = require('fs');\nfunction solve(raw) {\n  // Parse raw input, then make your move\n}\nconsole.log(solve(fs.readFileSync(0, 'utf8').trim()));"),
            LanguageTemplate(ProgrammingLanguage.KOTLIN, "fun solve(raw: String): Any? {\n    // Parse raw input, then make your move\n    return null\n}\nfun main() = print(solve(generateSequence(::readLine).joinToString(\"\\n\").trim()))"),
            LanguageTemplate(ProgrammingLanguage.CPP, "#include <bits/stdc++.h>\nusing namespace std;\nstring solve(const string& raw) {\n    // Parse raw input, then make your move\n    return \"\";\n}\nint main() { string raw((istreambuf_iterator<char>(cin)), {}); cout << solve(raw); }"),
            LanguageTemplate(ProgrammingLanguage.GO, "package main\nimport (\"fmt\"; \"io\"; \"os\")\nfunc solve(raw string) interface{} {\n    // Parse raw input, then make your move\n    return nil\n}\nfunc main() { raw, _ := io.ReadAll(os.Stdin); fmt.Print(solve(string(raw))) }"),
            LanguageTemplate(ProgrammingLanguage.SWIFT, "import Foundation\nfunc solve(_ raw: String) -> Any {\n    // Parse raw input, then make your move\n    return \"\"\n}\nlet data = FileHandle.standardInput.readDataToEndOfFile()\nprint(solve(String(data: data, encoding: .utf8)!.trimmingCharacters(in: .whitespacesAndNewlines)), terminator: \"\")"),
        )
    }
}
