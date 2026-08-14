package com.codingarena.features.roadmap

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codingarena.content.GlossaryTerm
import com.codingarena.content.LessonChoice
import com.codingarena.content.LessonQuestion
import com.codingarena.content.RoadmapLesson
import com.codingarena.content.RoadmapLessons
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.CodeBlock
import com.codingarena.core.design.ProgressBar
import com.codingarena.domain.engine.StreakEngine
import com.codingarena.domain.repository.StreakRepository
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.core.design.SectionHeader
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val STAGE_PROBLEM = 0
private const val STAGE_QUESTIONS = 1
private const val STAGE_COMPLETE = 2

@Composable
fun RoadmapLessonScreen(
    slug: String,
    onBack: () -> Unit,
    onReadExplanation: (String) -> Unit,
    onNextProblem: (String?) -> Unit,
) {
    val lesson = RoadmapLessons.bySlug(slug)
    if (lesson == null) {
        MissingLesson(slug, onBack)
        return
    }

    var stage by rememberSaveable(slug) { mutableIntStateOf(STAGE_PROBLEM) }
    var questionIndex by rememberSaveable(slug) { mutableIntStateOf(0) }
    var reviewingProblem by rememberSaveable(slug) { mutableStateOf(false) }

    if (reviewingProblem) {
        ProblemPage(
            lesson = lesson,
            backLabel = "Back to question",
            startLabel = "Back to question",
            onBack = { reviewingProblem = false },
            onStart = { reviewingProblem = false },
        )
        return
    }

    when (stage) {
        STAGE_PROBLEM -> ProblemPage(
            lesson = lesson,
            onBack = onBack,
            onStart = { stage = STAGE_QUESTIONS },
        )
        STAGE_QUESTIONS -> QuestionPage(
            lesson = lesson,
            questionIndex = questionIndex,
            onBack = onBack,
            onViewProblem = { reviewingProblem = true },
            onNext = {
                if (questionIndex == lesson.questions.lastIndex) stage = STAGE_COMPLETE
                else questionIndex += 1
            },
        )
        else -> CompletionPage(
            lesson = lesson,
            onBack = onBack,
            onReadExplanation = { onReadExplanation(lesson.slug) },
            onNextProblem = { onNextProblem(lesson.nextSlug) },
        )
    }
}

@Composable
private fun ProblemPage(
    lesson: RoadmapLesson,
    onBack: () -> Unit,
    onStart: () -> Unit,
    backLabel: String = "Back",
    startLabel: String = "Start questions",
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text(backLabel) }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(lesson.title, style = MaterialTheme.typography.headlineMedium)
                }
                ArenaChip(lesson.difficulty.displayName)
            }
        }
        item {
            Text(lesson.description, style = MaterialTheme.typography.bodyLarge)
        }
        item { SectionHeader("Examples") }
        items(lesson.examples) { example ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CodeBlock("Input:  ${example.input}\nOutput: ${example.output}")
                    Text(example.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item { SectionHeader("Constraints") }
        items(lesson.constraints) { constraint -> Text("• $constraint") }
        item {
            Text(
                "The pattern and solution are intentionally hidden. Use the examples and constraints to reason about the choices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 28.dp),
            ) { Text(startLabel) }
        }
    }
}

@Composable
private fun QuestionPage(
    lesson: RoadmapLesson,
    questionIndex: Int,
    onBack: () -> Unit,
    onViewProblem: () -> Unit,
    onNext: () -> Unit,
) {
    val question = lesson.questions[questionIndex]
    var selectedIndex by remember(question.id) { mutableStateOf<Int?>(null) }
    var checked by remember(question.id) { mutableStateOf(false) }
    val selected = selectedIndex?.let(question.choices::get)
    val showResult = checked && selected?.correct == true

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.Close, contentDescription = "Exit lesson")
            }
            ProgressBar(
                (questionIndex + 1).toFloat() / lesson.questions.size,
                Modifier.weight(1f),
            )
            IconButton(onClick = onViewProblem) {
                Icon(Icons.Filled.Visibility, contentDescription = "View problem")
            }
        }

        AnimatedContent(
            targetState = showResult,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (slideInHorizontally(tween(600)) { width -> width } + fadeIn(tween(600))) togetherWith
                    (slideOutHorizontally(tween(600)) { width -> -width } + fadeOut(tween(600)))
            },
            label = "question-result",
        ) { isResult ->
            if (!isResult) {
                QuestionChoices(
                    question = question,
                    selectedIndex = selectedIndex,
                    checked = checked,
                    selected = selected,
                    onSelect = { index ->
                        selectedIndex = index
                        checked = false
                    },
                    onCheck = { checked = true },
                    onRetry = {
                        checked = false
                        selectedIndex = null
                    },
                )
            } else if (selected != null) {
                QuestionResult(
                    choice = selected,
                    isLastQuestion = questionIndex == lesson.questions.lastIndex,
                    onNext = onNext,
                )
            }
        }
    }
}

@Composable
private fun QuestionChoices(
    question: LessonQuestion,
    selectedIndex: Int?,
    checked: Boolean,
    selected: LessonChoice?,
    onSelect: (Int) -> Unit,
    onCheck: () -> Unit,
    onRetry: () -> Unit,
) {
    val wrongChoice = selected?.takeIf { checked && !it.correct }
    val listState = remember(question.id) { LazyListState() }
    val scope = rememberCoroutineScope()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(Modifier.padding(bottom = 12.dp)) {
                Text(question.prompt, style = MaterialTheme.typography.titleMedium)
                question.code?.let { CodeBlock(it, Modifier.padding(top = 12.dp)) }
            }
        }
        items(question.choices.indices.toList()) { index ->
            val choice = question.choices[index]
            val isSelected = index == selectedIndex
            val isWrongSelection = isSelected && choice === wrongChoice
            Card(
                onClick = { onSelect(index) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    when {
                        isWrongSelection -> MaterialTheme.colorScheme.error
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                ),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(choice.text, style = MaterialTheme.typography.bodyLarge)
                    choice.code?.let { CodeBlock(it) }
                }
            }
        }
        if (wrongChoice != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Consider the tradeoff", fontWeight = FontWeight.Bold)
                        Text(wrongChoice.feedback)
                    }
                }
            }
        }
        item {
            if (wrongChoice != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Choose again after reconsidering the examples and constraint.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = {
                            onRetry()
                            scope.launch { listState.animateScrollToItem(0) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                Button(
                    onClick = onCheck,
                    enabled = selectedIndex != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Check")
                }
            }
        }
        if (question.glossary.isNotEmpty()) {
            item {
                GlossaryCard(question.glossary, Modifier.padding(bottom = 24.dp))
            }
        }
    }
}

@Composable
private fun GlossaryCard(glossary: List<GlossaryTerm>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Terms used", fontWeight = FontWeight.Bold)
            glossary.forEach { entry ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(entry.term, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        entry.definition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionResult(
    choice: LessonChoice,
    isLastQuestion: Boolean,
    onNext: () -> Unit,
) {
    var iconVisible by remember(choice) { mutableStateOf(false) }
    LaunchedEffect(choice) { iconVisible = true }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = iconVisible,
            enter = scaleIn(tween(700)) + fadeIn(tween(700)),
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp).size(64.dp),
            )
        }
        Text(
            "Correct",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Strong reasoning", fontWeight = FontWeight.Bold)
                Text(choice.feedback)
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
        ) { Text(if (isLastQuestion) "Finish lesson" else "Next question") }
    }
}

@Composable
private fun CompletionPage(
    lesson: RoadmapLesson,
    onBack: () -> Unit,
    onReadExplanation: () -> Unit,
    onNextProblem: () -> Unit,
) {
    val hasNext = lesson.nextSlug != null
    val streaks = koinInject<StreakRepository>()
    val currentUser = koinInject<CurrentUser>()
    val time = koinInject<TimeProvider>()
    var currentStreak by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) {
        val userId = currentUser.ensureLoaded()?.id ?: return@LaunchedEffect
        currentStreak = StreakEngine().refresh(streaks.load(userId), time.epochDay()).currentStreak
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onBack) { Text("← Roadmap") }
            currentStreak?.let { streak ->
                if (streak > 0) ArenaChip("🔥 ${streak}d")
            }
        }

        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        Column(
            Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                ) + fadeIn(tween(300)),
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp),
                )
            }
            Text(
                "Done!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                "You solved \"${lesson.title}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Button(
            onClick = onNextProblem,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (hasNext) "Continue to next problem" else "Return to roadmap")
        }
        OutlinedButton(
            onClick = onReadExplanation,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp),
        ) {
            Text("Read full explanation")
        }
    }
}

@Composable
fun RoadmapExplanationScreen(
    slug: String,
    onBack: () -> Unit,
    onNextProblem: (String?) -> Unit,
) {
    val lesson = RoadmapLessons.bySlug(slug)
    if (lesson == null) {
        MissingLesson(slug, onBack)
        return
    }
    val explanation = lesson.explanation
    val hasNext = lesson.nextSlug != null
    val readable = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp, letterSpacing = 0.3.sp)
    val readableListItem = MaterialTheme.typography.bodyMedium.copy(lineHeight = 26.sp, letterSpacing = 0.3.sp)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("Back") }
            Text("COMPLETE EXPLANATION", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Text(lesson.title, style = MaterialTheme.typography.headlineMedium)
        }
        item { SectionHeader("Intuition") }
        item { Text(explanation.intuition, style = readable) }
        item { SectionHeader("Step-by-step walkthrough") }
        items(explanation.walkthrough.mapIndexed { index, step -> "${index + 1}. $step" }) {
            Text(it, style = readableListItem)
        }
        item { SectionHeader("Pseudocode") }
        item { CodeBlock(explanation.pseudocode) }
        item { SectionHeader("Reference implementation") }
        item { CodeBlock(explanation.referenceCode) }
        item { SectionHeader("Complexity") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Time", fontWeight = FontWeight.Bold)
                Text(explanation.timeComplexity, style = readable)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Space", fontWeight = FontWeight.Bold)
                Text(explanation.spaceComplexity, style = readable)
            }
        }
        item { SectionHeader("Alternative approaches") }
        items(explanation.alternatives) { Text("• $it", style = readableListItem) }
        item { SectionHeader("Common mistakes") }
        items(explanation.commonMistakes) { Text("• $it", style = readableListItem) }
        item { SectionHeader("Recognition signals") }
        items(explanation.recognitionSignals) { Text("• $it", style = readableListItem) }
        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Takeaway", style = MaterialTheme.typography.titleMedium)
            Text(explanation.takeaway, style = readable,
                modifier = Modifier.padding(top = 8.dp))
            Button(
                onClick = { onNextProblem(lesson.nextSlug) },
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 28.dp),
            ) { Text(if (hasNext) "Continue to next problem" else "Return to roadmap") }
        }
    }
}

@Composable
private fun MissingLesson(slug: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Guided lesson coming soon", style = MaterialTheme.typography.headlineMedium)
        Text("The authored lesson for $slug is not available yet.")
        Button(onClick = onBack) { Text("Back to roadmap") }
    }
}
