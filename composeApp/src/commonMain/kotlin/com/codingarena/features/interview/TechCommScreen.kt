package com.codingarena.features.interview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.InterviewContent
import com.codingarena.content.NeetCode150
import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.engine.InterviewEngine
import com.codingarena.domain.model.AdaptivePracticePhase
import com.codingarena.domain.model.InterviewRoundState
import com.codingarena.domain.model.InterviewRoundSurface
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.repository.InterviewProgressRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ---------------------------------------------------------------- problem list

data class TechCommProblemSummary(val slug: String, val title: String)

class TechCommProblemsViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val currentUser: CurrentUser,
) : ViewModel() {

    private val _problems = MutableStateFlow<List<TechCommProblemSummary>>(emptyList())
    val problems: StateFlow<List<TechCommProblemSummary>> = _problems.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val records = curriculumRepo.records(userId)
            _problems.value = InterviewContent.techCommItems
                .groupBy { it.problemSlug }
                .filter { (slug, _) -> (records[slug]?.totalSeen ?: 0) > 0 }
                .mapNotNull { (slug, _) ->
                    val title = NeetCode150.curriculum.bySlug(slug)?.title ?: return@mapNotNull null
                    TechCommProblemSummary(slug, title)
                }
                .sortedBy { it.title }
        }
    }
}

@Composable
fun TechCommProblemsScreen(
    onBack: () -> Unit,
    onSelectProblem: (String) -> Unit,
    viewModel: TechCommProblemsViewModel = koinViewModel(),
) {
    val problems by viewModel.problems.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← Interview") }
        Text(
            "Technical Communication",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        if (problems.isEmpty()) {
            Text(
                "Learn a problem on the Roadmap first to unlock its communication drills here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(problems) { problem ->
                    InterviewModeRow(
                        mark = problem.title.take(1),
                        title = problem.title,
                        onClick = { onSelectProblem(problem.slug) },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- adaptive tech comm round

data class TechCommRoundUiState(
    val loading: Boolean = true,
    val notAvailable: Boolean = false,
    val roundState: InterviewRoundState? = null,
    val selectedId: String? = null,
    val checked: Boolean = false,
)

class TechCommRoundViewModel(
    private val engine: InterviewEngine,
    private val progressRepo: InterviewProgressRepository,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(TechCommRoundUiState())
    val state: StateFlow<TechCommRoundUiState> = _state.asStateFlow()

    private var userId: String? = null

    fun start(problemSlug: String) {
        viewModelScope.launch {
            _state.value = TechCommRoundUiState(loading = true)
            val uid = currentUser.ensureLoaded()?.id ?: return@launch
            userId = uid
            val progress = progressRepo.load(uid)
            val fresh = InterviewRoundState(
                id = "techcomm-${time.nowMillis()}",
                userId = uid,
                surface = InterviewRoundSurface.TECH_COMM,
                problemSlug = problemSlug,
                difficulty = progress?.techCommDifficulty ?: PracticeDifficulty.BEGINNER,
                consecutiveBelowEight = progress?.techCommConsecutiveBelowTarget ?: 0,
                startedAt = time.nowMillis(),
            )
            advanceRound(fresh, problemSlug)
        }
    }

    private suspend fun advanceRound(current: InterviewRoundState, problemSlug: String) {
        val updated = engine.startTechCommRound(current, problemSlug, InterviewContent.techCommItems, time.nowMillis())
        _state.value = TechCommRoundUiState(
            loading = false,
            roundState = updated,
            notAvailable = updated.phase != AdaptivePracticePhase.ROUND_ACTIVE,
        )
    }

    fun select(choiceId: String) {
        _state.value = _state.value.copy(selectedId = choiceId, checked = false)
    }

    fun check() {
        _state.value = _state.value.copy(checked = true)
    }

    fun next() {
        val state = _state.value
        val roundState = state.roundState ?: return
        val question = roundState.currentRound?.currentQuestion ?: return
        val choiceId = state.selectedId ?: return
        viewModelScope.launch {
            val now = time.nowMillis()
            var updated = engine.submitTechCommRoundAnswer(roundState, question.instanceId, choiceId, now)
            if (updated.currentRound?.isFullyAnswered == true) {
                updated = engine.finishTechCommRound(updated, now)
                persistIfFinished(updated, now)
            }
            _state.value = _state.value.copy(roundState = updated, selectedId = null, checked = false)
        }
    }

    fun continueRound() {
        val roundState = _state.value.roundState ?: return
        val problemSlug = roundState.problemSlug ?: return
        viewModelScope.launch { advanceRound(roundState, problemSlug) }
    }

    private suspend fun persistIfFinished(updated: InterviewRoundState, now: Long) {
        if (updated.phase != AdaptivePracticePhase.ROUND_RESULTS) return
        val uid = userId ?: return
        val progress = progressRepo.load(uid)
        val newProgress = engine.applyTechCommRoundToProgress(progress, uid, updated, now)
        progressRepo.save(newProgress, now)
    }
}

@Composable
fun TechCommRoundScreen(
    problemSlug: String,
    onDone: () -> Unit,
    viewModel: TechCommRoundViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(problemSlug) { viewModel.start(problemSlug) }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.notAvailable -> NotAvailableState(onDone)
        state.roundState?.phase == AdaptivePracticePhase.ROUND_RESULTS -> TechCommRoundResultsPage(
            roundState = state.roundState!!,
            onContinue = viewModel::continueRound,
            onFinish = onDone,
        )
        state.roundState?.currentRound?.currentQuestion != null -> {
            val round = state.roundState!!.currentRound!!
            val question = round.currentQuestion!!
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDone) { Text("Exit") }
                    Text(
                        "${round.currentIndex + 1} OF ${round.questions.size}" +
                            (question.techCommKind?.let { " · ${it.displayName.uppercase()}" } ?: ""),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                RatedDecisionPoint(
                    scenario = null,
                    prompt = question.prompt,
                    choices = question.choices,
                    selectedId = state.selectedId,
                    checked = state.checked,
                    showFeedback = true,
                    checkLabel = "Check",
                    onSelect = viewModel::select,
                    onCheck = viewModel::check,
                )
                if (state.checked) {
                    Button(
                        onClick = viewModel::next,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    ) { Text(if (round.currentIndex == round.questions.lastIndex) "Finish" else "Next") }
                }
            }
        }
    }
}

@Composable
private fun TechCommRoundResultsPage(
    roundState: InterviewRoundState,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
) {
    val round = roundState.currentRound ?: return
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Column(Modifier.weight(1f).padding(top = 16.dp)) {
            Text(
                "COMPLETED ROUND",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "${round.score} / ${round.questions.size}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Next round: ${roundState.difficulty.label()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Continue Practicing") }
        TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) { Text("Finish Practice") }
    }
}
