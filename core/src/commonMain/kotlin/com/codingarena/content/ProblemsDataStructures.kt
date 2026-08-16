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

/** Starter content, part two: stacks, queues, lists, trees, graphs and heaps. */
internal val dataStructureProblems: List<CodingProblem> = listOf(

    CodingProblem(
        id = "stack-behaviour-01",
        title = "What the stack leaves behind",
        description = "What does this print?",
        difficultyRating = 900,
        primaryTopic = CodingTopic.STACKS,
        challengeType = ChallengeType.OUTPUT_PREDICTION,
        codeSnippet = """
            stack = []
            for c in "abcd":
                if c == "c":
                    stack.pop()
                else:
                    stack.append(c)
            print("".join(stack))
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "ad",
                rationale = "Correct: a and b are pushed, c pops b, then d is pushed.",
            ),
            AnswerChoice(
                "b", "abd",
                rationale = "That skips the pop. When c is seen, an element is removed rather " +
                    "than appended.",
                insight = "The push order is right - the branch on \"c\" is what changes things.",
            ),
            AnswerChoice(
                "c", "abcd",
                rationale = "Every character would only be appended if the pop branch never ran.",
            ),
            AnswerChoice(
                "d", "bd",
                rationale = "pop() removes the most recent element, which is b, not the oldest.",
                insight = "You correctly tracked that one element is removed - a stack removes from " +
                    "the top, not the bottom.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Push a, push b, then c triggers pop() which removes b (the top), then push " +
            "d. The stack ends as [a, d].",
        bestApproach = "Trace the stack contents after every iteration rather than trying to " +
            "reason about the whole loop at once.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Assuming pop() removes from the front like a queue",
            "Missing that the pop branch also skips the append",
        ),
        hints = listOf("Write the stack contents after each character."),
        patternId = "monotonic-stack",
        estimatedSeconds = 40,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                List<Character> stack = new ArrayList<>();
                for (char c : "abcd".toCharArray()) {
                    if (c == 'c') {
                        stack.remove(stack.size() - 1);
                    } else {
                        stack.add(c);
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (char c : stack) sb.append(c);
                System.out.println(sb);
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                let stack = [];
                for (const c of "abcd") {
                    if (c === "c") {
                        stack.pop();
                    } else {
                        stack.push(c);
                    }
                }
                console.log(stack.join(""));
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                val stack = mutableListOf<Char>()
                for (c in "abcd") {
                    if (c == 'c') {
                        stack.removeAt(stack.size - 1)
                    } else {
                        stack.add(c)
                    }
                }
                println(stack.joinToString(""))
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                vector<char> stack;
                for (char c : string("abcd")) {
                    if (c == 'c') {
                        stack.pop_back();
                    } else {
                        stack.push_back(c);
                    }
                }
                for (char c : stack) cout << c;
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                stack := []byte{}
                for _, c := range "abcd" {
                    if c == 'c' {
                        stack = stack[:len(stack)-1]
                    } else {
                        stack = append(stack, byte(c))
                    }
                }
                fmt.Println(string(stack))
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                var stack: [Character] = []
                for c in "abcd" {
                    if c == "c" {
                        stack.removeLast()
                    } else {
                        stack.append(c)
                    }
                }
                print(String(stack))
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "queue-behaviour-01",
        title = "Queue ordering",
        description = "Elements 1, 2, 3 are enqueued, then one is dequeued, then 4 is enqueued. " +
            "What is the dequeue order from here?",
        difficultyRating = 900,
        primaryTopic = CodingTopic.QUEUES,
        challengeType = ChallengeType.OUTPUT_PREDICTION,
        choices = listOf(
            AnswerChoice("a", "2, 3, 4", rationale = "Correct: 1 has already left, and 4 joined the back."),
            AnswerChoice(
                "b", "4, 3, 2",
                rationale = "That is stack order. A queue is first in, first out.",
                insight = "You tracked which elements remain correctly - only the direction is off.",
            ),
            AnswerChoice(
                "c", "3, 2, 4",
                rationale = "A queue never reorders the elements already waiting.",
            ),
            AnswerChoice(
                "d", "1, 2, 3",
                rationale = "1 was already removed by the dequeue.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "A queue removes from the front and adds to the back. After enqueueing " +
            "1, 2, 3 the dequeue removes 1, leaving [2, 3]; enqueueing 4 gives [2, 3, 4].",
        bestApproach = "Track both ends explicitly: removals happen at the front, insertions at " +
            "the back.",
        timeComplexity = "O(1) per operation",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Confusing queue (FIFO) with stack (LIFO) ordering",
            "Using a list and removing from the front in O(n) instead of a deque",
        ),
        hints = listOf("Which end does a queue remove from?"),
        patternId = "bfs",
        estimatedSeconds = 30,
    ),

    CodingProblem(
        id = "linked-list-01",
        title = "The traversal that never ends",
        description = "This should print every value in a linked list, but it loops forever. " +
            "Which line is at fault?",
        difficultyRating = 1000,
        primaryTopic = CodingTopic.LINKED_LISTS,
        secondaryTopics = listOf(CodingTopic.DEBUGGING),
        challengeType = ChallengeType.FIND_THE_BUG,
        codeSnippet = """
            1  def print_all(head):
            2      node = head
            3      while node is not None:
            4          print(node.value)
            5          node = head.next
            6      return
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "Line 5", tag = "5",
                rationale = "Correct: it advances from head every time instead of from node, so " +
                    "the traversal never moves past the second element.",
            ),
            AnswerChoice(
                "b", "Line 3", tag = "3",
                rationale = "Checking for None is the right loop condition; the problem is that " +
                    "node never progresses toward it.",
                insight = "Suspecting the loop condition is sound reasoning for an infinite loop - " +
                    "here the condition is fine and the update is not.",
            ),
            AnswerChoice(
                "c", "Line 2", tag = "2",
                rationale = "Seeding node from head is correct.",
            ),
            AnswerChoice(
                "d", "Line 4", tag = "4",
                rationale = "Printing has no effect on termination.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "node = head.next re-reads the same second node on every iteration, so node " +
            "is never None and the loop cannot end. It must advance relative to the current node.",
        bestApproach = "Advance with node = node.next so each iteration moves one step further " +
            "along the list.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)",
        commonMistakes = listOf(
            "Advancing from the head pointer rather than the cursor",
            "Losing the head reference by advancing head itself when it is still needed",
        ),
        hints = listOf(
            "For the loop to end, what has to change each iteration?",
            "Look closely at which variable line 5 reads from.",
        ),
        patternId = "array-traversal",
        estimatedSeconds = 45,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                1  void printAll(Node head) {
                2      Node node = head;
                3      while (node != null) {
                4          System.out.println(node.value);
                5          node = head.next;
                6      }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                1  function printAll(head) {
                2      let node = head;
                3      while (node !== null) {
                4          console.log(node.value);
                5          node = head.next;
                6      }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                1  fun printAll(head: Node?) {
                2      var node = head
                3      while (node != null) {
                4          println(node.value)
                5          node = head?.next
                6      }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                1  void printAll(Node* head) {
                2      Node* node = head;
                3      while (node != nullptr) {
                4          cout << node->value << endl;
                5          node = head->next;
                6      }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                1  func printAll(head *Node) {
                2      node := head
                3      for node != nil {
                4          fmt.Println(node.Value)
                5          node = head.Next
                6      }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                1  func printAll(_ head: Node?) {
                2      var node = head
                3      while node != nil {
                4          print(node!.value)
                5          node = head?.next
                6      }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "tree-dfs-01",
        title = "In-order traversal output",
        description = "For this tree, what does an in-order traversal print?\n\n" +
            "        4\n" +
            "       / \\\n" +
            "      2   6\n" +
            "     / \\\n" +
            "    1   3",
        difficultyRating = 1100,
        primaryTopic = CodingTopic.TREES,
        secondaryTopics = listOf(CodingTopic.RECURSION),
        challengeType = ChallengeType.OUTPUT_PREDICTION,
        codeSnippet = """
            def inorder(node):
                if node is None:
                    return
                inorder(node.left)
                print(node.value)
                inorder(node.right)
        """.trimIndent(),
        choices = listOf(
            AnswerChoice(
                "a", "1 2 3 4 6",
                rationale = "Correct: in-order on a binary search tree yields sorted output.",
            ),
            AnswerChoice(
                "b", "4 2 1 3 6",
                rationale = "That is pre-order - the node is printed before descending left.",
                insight = "You traced the recursion correctly; the print position is what defines " +
                    "the traversal.",
            ),
            AnswerChoice(
                "c", "1 3 2 6 4",
                rationale = "That is post-order - the node is printed after both children.",
                insight = "Correct recursive structure, different print placement.",
            ),
            AnswerChoice(
                "d", "4 2 6 1 3",
                rationale = "That is a level-order (BFS) traversal, which needs a queue rather " +
                    "than recursion.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "In-order visits the entire left subtree, then the node, then the right " +
            "subtree. On a binary search tree that produces the values in sorted order - which is " +
            "the fact most in-order interview questions are really testing.",
        bestApproach = "Locate the print statement relative to the two recursive calls: before " +
            "both is pre-order, between them is in-order, after both is post-order.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(h) for the call stack",
        commonMistakes = listOf(
            "Mixing up pre-, in- and post-order by misplacing the visit",
            "Forgetting that in-order on a BST is sorted, and re-sorting the result",
        ),
        hints = listOf(
            "Where does the print sit relative to the two recursive calls?",
            "What is special about in-order on a binary search tree?",
        ),
        patternId = "dfs",
        estimatedSeconds = 50,
        languageVariants = listOf(
            CodeVariant(
                JAVA,
                """
                void inorder(Node node) {
                    if (node == null) return;
                    inorder(node.left);
                    System.out.println(node.value);
                    inorder(node.right);
                }
                """.trimIndent(),
            ),
            CodeVariant(
                JAVASCRIPT,
                """
                function inorder(node) {
                    if (node === null) return;
                    inorder(node.left);
                    console.log(node.value);
                    inorder(node.right);
                }
                """.trimIndent(),
            ),
            CodeVariant(
                KOTLIN,
                """
                fun inorder(node: Node?) {
                    if (node == null) return
                    inorder(node.left)
                    println(node.value)
                    inorder(node.right)
                }
                """.trimIndent(),
            ),
            CodeVariant(
                CPP,
                """
                void inorder(Node* node) {
                    if (node == nullptr) return;
                    inorder(node->left);
                    cout << node->value << " ";
                    inorder(node->right);
                }
                """.trimIndent(),
            ),
            CodeVariant(
                GO,
                """
                func inorder(node *Node) {
                    if node == nil {
                        return
                    }
                    inorder(node.Left)
                    fmt.Println(node.Value)
                    inorder(node.Right)
                }
                """.trimIndent(),
            ),
            CodeVariant(
                SWIFT,
                """
                func inorder(_ node: Node?) {
                    guard let node = node else { return }
                    inorder(node.left)
                    print(node.value)
                    inorder(node.right)
                }
                """.trimIndent(),
            ),
        ),
    ),

    CodingProblem(
        id = "tree-bfs-01",
        title = "Level-order needs which structure?",
        description = "You must print a binary tree one level at a time, left to right. Which " +
            "data structure does the traversal need?",
        difficultyRating = 1050,
        primaryTopic = CodingTopic.TREES,
        secondaryTopics = listOf(CodingTopic.QUEUES),
        challengeType = ChallengeType.DATA_STRUCTURE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "A queue",
                rationale = "Correct: FIFO ordering is exactly what keeps nodes in level order.",
            ),
            AnswerChoice(
                "b", "A stack",
                rationale = "LIFO ordering gives depth-first traversal, diving down one branch " +
                    "before the rest of the level.",
                insight = "Reaching for an explicit structure instead of recursion is the right " +
                    "move - the ordering is what has to change.",
            ),
            AnswerChoice(
                "c", "A min-heap keyed by depth",
                rationale = "This would work but adds an unnecessary O(log n) per operation; a " +
                    "queue already preserves the order for free.",
                insight = "You are right that depth is the ordering key - a queue just maintains " +
                    "it without any comparisons.",
            ),
            AnswerChoice(
                "d", "A hash set",
                rationale = "A set has no ordering at all.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Enqueue the root, then repeatedly dequeue a node and enqueue its children. " +
            "Because children are always added behind everything at the current level, the queue " +
            "hands nodes back in exactly level order.",
        bestApproach = "Use a queue, and capture its size at the start of each round if you need " +
            "the levels separated.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(w) for the widest level",
        commonMistakes = listOf(
            "Using a stack and getting depth-first order",
            "Failing to snapshot the queue length when levels must be grouped",
        ),
        hints = listOf("Which ordering discipline keeps siblings together?"),
        patternId = "bfs",
        estimatedSeconds = 40,
    ),

    CodingProblem(
        id = "graph-rearrange-01",
        title = "Assemble a BFS",
        description = "Put these lines in the correct order to form a breadth-first search over " +
            "a graph.",
        difficultyRating = 1250,
        primaryTopic = CodingTopic.GRAPHS,
        secondaryTopics = listOf(CodingTopic.QUEUES),
        challengeType = ChallengeType.REARRANGE_CODE,
        choices = listOf(
            AnswerChoice("l1", "queue = deque([start]); seen = {start}"),
            AnswerChoice("l2", "while queue:"),
            AnswerChoice("l3", "    node = queue.popleft()"),
            AnswerChoice("l4", "    for neighbour in graph[node]:"),
            AnswerChoice("l5", "        if neighbour not in seen:"),
            AnswerChoice("l6", "            seen.add(neighbour); queue.append(neighbour)"),
        ),
        correctAnswerIds = listOf("l1", "l2", "l3", "l4", "l5", "l6"),
        explanation = "Seed the queue and the seen set together, then loop while work remains: " +
            "take from the front, expand the neighbours, and admit each unseen one exactly once. " +
            "Marking a node as seen at enqueue time - not at dequeue time - is what stops the " +
            "same node being queued twice.",
        bestApproach = "Initialise the frontier and the visited set together, then dequeue, " +
            "expand and admit unseen neighbours.",
        timeComplexity = "O(V + E)",
        spaceComplexity = "O(V)",
        commonMistakes = listOf(
            "Marking nodes as seen when dequeuing, which lets duplicates pile up in the queue",
            "Forgetting to seed the seen set with the start node",
        ),
        hints = listOf(
            "What has to exist before the loop can run?",
            "When should a node be marked as seen - when it is added, or when it is taken out?",
        ),
        patternId = "bfs",
        estimatedSeconds = 75,
    ),

    CodingProblem(
        id = "graph-traversal-01",
        title = "Counting islands",
        description = "\"Given a grid of land and water cells, count the connected regions of " +
            "land.\" Which pattern does this call for?",
        difficultyRating = 1200,
        primaryTopic = CodingTopic.GRAPHS,
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "Graph traversal (DFS or BFS) from each unvisited land cell",
                rationale = "Correct: the grid is a graph, and each traversal consumes exactly one " +
                    "connected region.",
            ),
            AnswerChoice(
                "b", "Dynamic programming over the grid",
                rationale = "There is no optimal substructure here - the question is connectivity, " +
                    "not an optimum.",
            ),
            AnswerChoice(
                "c", "Sliding window over each row",
                rationale = "A window cannot see that land in one row connects to land in the next.",
                insight = "Scanning row by row is the right starting instinct - it just has to " +
                    "follow connections vertically too.",
            ),
            AnswerChoice(
                "d", "Sort the cells and scan for groups",
                rationale = "Sorting destroys adjacency, which is the only thing that matters here.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "\"Connected regions\" is the giveaway. Scan the grid; every time you meet " +
            "unvisited land, run one traversal that marks the whole region, and add one to the " +
            "count. Union-Find is the other valid answer, and is preferred when regions merge " +
            "dynamically.",
        bestApproach = "Iterate the grid and, on each unvisited land cell, flood the region with " +
            "DFS or BFS while incrementing the count.",
        timeComplexity = "O(rows * cols)",
        spaceComplexity = "O(rows * cols) worst case",
        commonMistakes = listOf(
            "Not marking cells visited, which revisits the same region forever",
            "Treating diagonals as connected when the problem says four-directional",
        ),
        hints = listOf(
            "What does \"connected\" suggest about the shape of the data?",
            "How many traversals does one region need?",
        ),
        patternId = "graph-traversal",
        estimatedSeconds = 50,
    ),

    CodingProblem(
        id = "heap-topk-01",
        title = "The k largest of a million",
        description = "Return the 10 largest values from a stream of one million integers, using " +
            "as little memory as possible. Which structure fits?",
        difficultyRating = 1300,
        primaryTopic = CodingTopic.HEAPS,
        challengeType = ChallengeType.DATA_STRUCTURE_CHOICE,
        choices = listOf(
            AnswerChoice(
                "a", "A min-heap of size k",
                rationale = "Correct: hold only the best k so far and evict the smallest whenever " +
                    "a larger value arrives - O(n log k) time and O(k) space.",
            ),
            AnswerChoice(
                "b", "Sort everything and take the last k",
                rationale = "O(n log n) time and, for a stream, O(n) memory - it needs the entire " +
                    "input in hand.",
                insight = "It is correct, and worth stating as a baseline before optimising.",
            ),
            AnswerChoice(
                "c", "A max-heap of all n elements",
                rationale = "Correct results, but it holds all million values when only ten " +
                    "matter.",
                insight = "The right structure family - the trick is inverting it so the heap " +
                    "stays small.",
            ),
            AnswerChoice(
                "d", "A hash map of value to count",
                rationale = "A hash map gives no ordering, so extracting the top k still means " +
                    "scanning or sorting everything.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "The counter-intuitive part is using a *min*-heap to track maxima: its root " +
            "is the weakest of your current top k, which is precisely the element to evict when " +
            "something better arrives.",
        bestApproach = "Keep a min-heap capped at k. Push each value, and pop whenever the heap " +
            "grows past k.",
        timeComplexity = "O(n log k)",
        spaceComplexity = "O(k)",
        commonMistakes = listOf(
            "Using a max-heap of size k, whose root is the largest rather than the one to evict",
            "Sorting the whole stream when only k elements are needed",
        ),
        hints = listOf(
            "You only ever need to know the weakest member of your current best set.",
            "Which heap puts that weakest member at the root?",
        ),
        patternId = "heap",
        estimatedSeconds = 55,
    ),

    CodingProblem(
        id = "monotonic-stack-01",
        title = "Next greater element",
        description = "\"For each element, find the next element to its right that is larger.\" " +
            "Which pattern solves this in linear time?",
        difficultyRating = 1350,
        primaryTopic = CodingTopic.STACKS,
        challengeType = ChallengeType.PATTERN_RECOGNITION,
        choices = listOf(
            AnswerChoice(
                "a", "Monotonic stack",
                rationale = "Correct: keep a decreasing stack of indices still waiting for their " +
                    "answer, and resolve them as larger values arrive.",
            ),
            AnswerChoice(
                "b", "Nested loops scanning right from each element",
                rationale = "Correct but O(n^2). The repeated rescanning is the clue that a stack " +
                    "can carry the pending work instead.",
                insight = "It is the honest brute force, and the right thing to describe before " +
                    "optimising.",
            ),
            AnswerChoice(
                "c", "Two pointers",
                rationale = "Two pointers need a sorted array or a monotonic movement rule; " +
                    "neither holds here.",
            ),
            AnswerChoice(
                "d", "Binary search",
                rationale = "The array is unsorted, and \"next to the right\" is positional rather " +
                    "than ordered.",
            ),
        ),
        correctAnswerIds = listOf("a"),
        explanation = "Elements waiting for a larger neighbour form a decreasing sequence. Hold " +
            "them on a stack; when a new value arrives, it is the answer for everything smaller " +
            "on top. Each index is pushed and popped at most once, so the total work is linear.",
        bestApproach = "Scan left to right holding a stack of indices with decreasing values, " +
            "popping and resolving each time a larger value arrives.",
        timeComplexity = "O(n)",
        spaceComplexity = "O(n)",
        commonMistakes = listOf(
            "Storing values instead of indices when the position is needed for the answer",
            "Forgetting that anything left on the stack at the end has no next greater element",
        ),
        hints = listOf(
            "What do all the elements still waiting for an answer have in common?",
            "How many times does any single index get pushed or popped?",
        ),
        patternId = "monotonic-stack",
        estimatedSeconds = 55,
    ),
)
