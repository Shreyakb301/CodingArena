package com.codingarena.features.practice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.NeetCode150
import com.codingarena.content.ProblemWorkouts
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.CodeBlock
import com.codingarena.core.design.ProgressBar
import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.engine.PracticeWorkoutEngine
import com.codingarena.domain.engine.StreakEngine
import com.codingarena.domain.model.AdaptivePracticePhase
import com.codingarena.domain.model.AdaptivePracticeRound
import com.codingarena.domain.model.AdaptivePracticeState
import com.codingarena.domain.model.MissedConcept
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.PracticeSessionKind
import com.codingarena.domain.model.ProgrammingLanguage
import com.codingarena.domain.model.WorkoutChoice
import com.codingarena.domain.model.WorkoutStep
import com.codingarena.domain.model.WorkoutStepKind
import com.codingarena.domain.model.forLanguage
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.repository.PracticeStateRepository
import com.codingarena.domain.repository.StreakRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** Where a [WorkoutRunnerScreen] session comes from. */
sealed interface WorkoutSource {
    data object Recommended : WorkoutSource
    data class TopicFocused(val group: PatternGroup) : WorkoutSource
    data object Mixed : WorkoutSource
}

/** Shown when a saved [AdaptivePracticeState] belongs to a different mode/topic than [requestedSource]. */
data class ResumeConflict(
    val savedMode: PracticeSessionKind,
    val savedTopic: PatternGroup?,
    val requestedSource: WorkoutSource,
)

data class WorkoutRunnerUiState(
    val loading: Boolean = true,
    val currentStreak: Int = 0,
    /** True once loading finishes but no round could be built - nothing authored yet for this source. */
    val notAvailable: Boolean = false,
    val practiceState: AdaptivePracticeState? = null,
    val resumeConflict: ResumeConflict? = null,
)

class WorkoutRunnerViewModel(
    private val engine: PracticeWorkoutEngine,
    private val blitzEngine: BlitzEngine,
    private val curriculumRepo: CurriculumRepository,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
    private val streaks: StreakRepository,
    private val ids: IdGenerator,
    private val practiceStateRepo: PracticeStateRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutRunnerUiState())
    val state: StateFlow<WorkoutRunnerUiState> = _state.asStateFlow()

    private var userId: String? = null

    fun start(source: WorkoutSource) {
        viewModelScope.launch {
            _state.value = WorkoutRunnerUiState(loading = true)
            val uid = currentUser.ensureLoaded()?.id ?: return@launch
            userId = uid
            val streak = StreakEngine().refresh(streaks.load(uid), time.epochDay()).currentStreak

            val requestedMode = source.toMode()
            val requestedTopic = (source as? WorkoutSource.TopicFocused)?.group

            when (val saved = practiceStateRepo.load(uid)) {
                null -> beginFresh(uid, requestedMode, requestedTopic, streak)
                else -> if (saved.mode == requestedMode && saved.topic == requestedTopic) {
                    resumeSaved(uid, saved, streak)
                } else {
                    _state.value = WorkoutRunnerUiState(
                        loading = false,
                        currentStreak = streak,
                        resumeConflict = ResumeConflict(saved.mode, saved.topic, source),
                    )
                }
            }
        }
    }

    fun resumeConflictingSession() {
        val uid = userId ?: return
        val streak = _state.value.currentStreak
        viewModelScope.launch {
            val saved = practiceStateRepo.load(uid) ?: return@launch
            resumeSaved(uid, saved, streak)
        }
    }

    fun startNewSession() {
        val uid = userId ?: return
        val source = _state.value.resumeConflict?.requestedSource ?: return
        val mode = source.toMode()
        val topic = (source as? WorkoutSource.TopicFocused)?.group
        val streak = _state.value.currentStreak
        viewModelScope.launch {
            practiceStateRepo.clear(uid)
            beginFresh(uid, mode, topic, streak)
        }
    }

    private suspend fun beginFresh(uid: String, mode: PracticeSessionKind, topic: PatternGroup?, streak: Int) {
        val fresh = AdaptivePracticeState(
            id = ids.newId(),
            userId = uid,
            mode = mode,
            topic = topic,
            difficulty = PracticeDifficulty.BEGINNER,
            startedAt = time.nowMillis(),
        )
        advanceRound(uid, fresh, streak)
    }

    private suspend fun resumeSaved(uid: String, saved: AdaptivePracticeState, streak: Int) {
        if (saved.phase == AdaptivePracticePhase.READY) {
            advanceRound(uid, saved, streak)
        } else {
            _state.value = WorkoutRunnerUiState(loading = false, practiceState = saved, currentStreak = streak)
        }
    }

    /** Builds a new round onto [current] (valid from READY or ROUND_RESULTS) and persists it. */
    private suspend fun advanceRound(uid: String, current: AdaptivePracticeState, streak: Int) {
        val curriculum = NeetCode150.curriculum
        val records = curriculumRepo.records(uid)
        val progress = blitzEngine.progress(curriculum, records, time.nowMillis())
        val language = currentUser.profile.value?.onboarding?.preferredLanguage ?: ProgrammingLanguage.PYTHON
        val workouts = ProblemWorkouts.workouts.map { workout ->
            workout.copy(steps = workout.steps.map { it.forLanguage(language) })
        }
        val updated = engine.startRound(
            current, curriculum, progress, records, workouts, time.nowMillis(),
        )
        practiceStateRepo.save(updated, time.nowMillis())
        _state.value = WorkoutRunnerUiState(
            loading = false,
            practiceState = updated,
            currentStreak = streak,
            notAvailable = updated.phase != AdaptivePracticePhase.ROUND_ACTIVE,
        )
    }

    fun continuePracticing() {
        val uid = userId ?: return
        val current = _state.value.practiceState ?: return
        val streak = _state.value.currentStreak
        viewModelScope.launch { advanceRound(uid, current, streak) }
    }

    fun finishPractice(onDone: () -> Unit) {
        val uid = userId ?: return
        viewModelScope.launch {
            practiceStateRepo.clear(uid)
            onDone()
        }
    }

    fun submit(chosenIndex: Int, elapsedMs: Long) {
        val uid = userId ?: return
        val practiceState = _state.value.practiceState ?: return
        val question = practiceState.currentRound?.currentQuestion ?: return
        viewModelScope.launch {
            var updated = engine.submitRoundAnswer(
                practiceState, question.instanceId, chosenIndex, elapsedMs, time.nowMillis(),
            )
            if (updated.currentRound?.isFullyAnswered == true) {
                updated = engine.finishRound(updated, time.nowMillis())
            }
            practiceStateRepo.save(updated, time.nowMillis())
            _state.value = _state.value.copy(practiceState = updated)
        }
    }

    private fun WorkoutSource.toMode(): PracticeSessionKind = when (this) {
        WorkoutSource.Recommended -> PracticeSessionKind.RECOMMENDED
        is WorkoutSource.TopicFocused -> PracticeSessionKind.TOPIC_FOCUSED
        WorkoutSource.Mixed -> PracticeSessionKind.MIXED
    }
}

@Composable
fun WorkoutRunnerScreen(
    source: WorkoutSource,
    onExit: () -> Unit,
    viewModel: WorkoutRunnerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(source) { viewModel.start(source) }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.resumeConflict != null -> ResumeConflictPrompt(
            conflict = state.resumeConflict!!,
            onResume = viewModel::resumeConflictingSession,
            onStartNew = viewModel::startNewSession,
            onExit = onExit,
        )
        state.notAvailable -> NotAvailableState(onExit)
        state.practiceState?.phase == AdaptivePracticePhase.ROUND_RESULTS -> RoundResultsPage(
            practiceState = state.practiceState!!,
            onContinue = viewModel::continuePracticing,
            onFinish = { viewModel.finishPractice(onExit) },
        )
        state.practiceState?.currentRound != null -> RoundQuestionPage(
            round = state.practiceState!!.currentRound!!,
            onExit = onExit,
            onSubmit = viewModel::submit,
        )
    }
}

@Composable
private fun ResumeConflictPrompt(
    conflict: ResumeConflict,
    onResume: () -> Unit,
    onStartNew: () -> Unit,
    onExit: () -> Unit,
) {
    val label = conflict.savedTopic?.displayName ?: when (conflict.savedMode) {
        PracticeSessionKind.MIXED -> "Mixed Practice"
        else -> "Recommended Practice"
    }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Unfinished session", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 24.dp))
        Text(
            "You have an unfinished $label session. Resume where you left off, or start a new session instead.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onResume, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Resume Session") }
        TextButton(onClick = onStartNew, modifier = Modifier.fillMaxWidth()) { Text("Start New Session") }
        TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}

@Composable
private fun NotAvailableState(onExit: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Not available yet", style = MaterialTheme.typography.headlineMedium)
        Text("This topic doesn't have authored practice content yet - more is on the way.")
        Button(onClick = onExit) { Text("Back") }
    }
}

@Composable
private fun RoundQuestionPage(
    round: AdaptivePracticeRound,
    onExit: () -> Unit,
    onSubmit: (chosenIndex: Int, elapsedMs: Long) -> Unit,
) {
    val question = round.currentQuestion ?: return
    val step = question.step
    val time = koinInject<TimeProvider>()
    var selectedIndex by remember(question.instanceId) { mutableStateOf<Int?>(null) }
    var checked by remember(question.instanceId) { mutableStateOf(false) }
    val stepStartedAt = remember(question.instanceId) { time.nowMillis() }
    val selected = selectedIndex?.let(step.choices::get)
    val showResult = checked && selected?.correct == true

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Exit practice")
            }
            ProgressBar(
                (round.currentIndex + 1).toFloat() / round.questions.size,
                Modifier.weight(1f),
            )
            if (question.isReview) ArenaChip("Review")
        }

        AnimatedContent(
            targetState = showResult,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                (slideInHorizontally(tween(600)) { width -> width } + fadeIn(tween(600))) togetherWith
                    (slideOutHorizontally(tween(600)) { width -> -width } + fadeOut(tween(600)))
            },
            label = "round-question-result",
        ) { isResult ->
            if (!isResult) {
                WorkoutStepChoices(
                    step = step,
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
                WorkoutStepResult(
                    choice = selected,
                    isLastStep = round.currentIndex == round.questions.lastIndex,
                    onNext = { onSubmit(selectedIndex ?: 0, time.nowMillis() - stepStartedAt) },
                )
            }
        }
    }
}

@Composable
private fun WorkoutStepChoices(
    step: WorkoutStep,
    selectedIndex: Int?,
    checked: Boolean,
    selected: WorkoutChoice?,
    onSelect: (Int) -> Unit,
    onCheck: () -> Unit,
    onRetry: () -> Unit,
) {
    val wrongChoice = selected?.takeIf { checked && !it.correct }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(Modifier.padding(bottom = 12.dp)) {
                Text(stepKindLabel(step.kind), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(step.prompt, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
                step.code?.let { CodeBlock(it, Modifier.padding(top = 12.dp)) }
            }
        }
        items(step.choices.indices.toList()) { index ->
            val choice = step.choices[index]
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
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Consider the tradeoff", fontWeight = FontWeight.Bold)
                        Text(wrongChoice.feedback)
                    }
                }
            }
        }
        item {
            if (wrongChoice != null) {
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Retry") }
            } else {
                Button(
                    onClick = onCheck,
                    enabled = selectedIndex != null,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                ) { Text("Check") }
            }
        }
    }
}

@Composable
private fun WorkoutStepResult(choice: WorkoutChoice, isLastStep: Boolean, onNext: () -> Unit) {
    var iconVisible by remember(choice) { mutableStateOf(false) }
    LaunchedEffect(choice) { iconVisible = true }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible = iconVisible, enter = scaleIn(tween(700)) + fadeIn(tween(700))) {
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
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) { Text(if (isLastStep) "Finish" else "Next") }
    }
}

@Composable
private fun RoundResultsPage(
    practiceState: AdaptivePracticeState,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
) {
    val round = practiceState.currentRound ?: return
    val score = round.score
    val incorrect = round.questions.size - score
    val needsImprovement = practiceState.missedConcepts
        .filter { it.pendingReview }
        .sortedByDescending { it.missCount }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Column(Modifier.padding(top = 24.dp)) {
            Text(
                "ROUND COMPLETE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "$score / ${round.questions.size}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    "$score correct",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "$incorrect incorrect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (needsImprovement.isEmpty()) Arrangement.Center else Arrangement.Top,
        ) {
            RoundEncouragement(round = round, nextDifficulty = practiceState.difficulty)

            if (needsImprovement.isNotEmpty()) {
                Text(
                    "CONCEPTS THAT NEED IMPROVEMENT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                )
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    needsImprovement.forEach { concept -> MissedConceptRow(concept) }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Next round",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                difficultyLabel(practiceState.difficulty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continue Practicing")
        }
        TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text("Finish Practice")
        }
    }
}

/**
 * Tied to the actual difficulty transition, not a guessed score cutoff - an
 * advance/regression is unambiguous, matching
 * [PracticeWorkoutEngine.finishRound][com.codingarena.domain.engine.PracticeWorkoutEngine.finishRound]'s
 * own state machine exactly rather than re-deriving it from the score alone.
 */
@Composable
private fun RoundEncouragement(round: AdaptivePracticeRound, nextDifficulty: PracticeDifficulty) {
    val target = round.targetDifficulty
    val advanced = nextDifficulty.ordinal > target.ordinal
    val regressed = nextDifficulty.ordinal < target.ordinal

    val (headline, subtext) = when {
        round.score == round.questions.size -> "Perfect round" to
            "You got every question right. Keep the momentum going."
        advanced -> "Well done" to
            "You are ready for the next round."
        regressed -> "Keep going!" to
            "A few focused reps will help rebuild the fundamentals."
        round.score >= 6 -> "Nice work!" to
            "Solid round — a few more reps and you'll advance."
        else -> "Keep going!" to
            "Tough round, but you're building the reps that make it stick."
    }

    var visible by remember(round) { mutableStateOf(false) }
    LaunchedEffect(round) { visible = true }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 12.dp)) {
        AnimatedVisibility(visible = visible, enter = scaleIn(tween(700)) + fadeIn(tween(700))) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        }
        Text(
            headline,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            subtext,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun MissedConceptRow(concept: MissedConcept) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${concept.group.displayName} · ${stepKindLabel(concept.kind)}", fontWeight = FontWeight.Bold)
            Text(
                "Missed ${concept.missCount} time${if (concept.missCount == 1) "" else "s"} at ${difficultyLabel(concept.missedAtDifficulty)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun difficultyLabel(difficulty: PracticeDifficulty): String = when (difficulty) {
    PracticeDifficulty.BEGINNER -> "Beginner"
    PracticeDifficulty.DEVELOPING -> "Developing"
    PracticeDifficulty.INTERMEDIATE -> "Intermediate"
    PracticeDifficulty.ADVANCED -> "Advanced"
}

private fun stepKindLabel(kind: WorkoutStepKind): String = when (kind) {
    WorkoutStepKind.PATTERN_RECOGNITION -> "Identify the pattern"
    WorkoutStepKind.APPROACH -> "Choose the best approach"
    WorkoutStepKind.STATE_SELECTION -> "Select the required state"
    WorkoutStepKind.BOUNDARY_UPDATE -> "Boundary and update logic"
    WorkoutStepKind.CODE_BLOCK -> "Identify the correct code block"
    WorkoutStepKind.TIME_COMPLEXITY -> "Time complexity"
    WorkoutStepKind.SPACE_COMPLEXITY -> "Space complexity"
    WorkoutStepKind.TRANSFER -> "Transfer the pattern"
    WorkoutStepKind.EDGE_CASE -> "Test an edge case"
}
