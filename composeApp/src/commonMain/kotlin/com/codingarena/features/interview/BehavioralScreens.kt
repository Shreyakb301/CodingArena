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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.engine.InterviewEngine
import com.codingarena.domain.model.AdaptivePracticePhase
import com.codingarena.domain.model.BehavioralCategory
import com.codingarena.domain.model.InterviewRoundState
import com.codingarena.domain.model.InterviewRoundSurface
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.repository.InterviewProgressRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ---------------------------------------------------------------- categories list

@Composable
fun BehavioralCategoriesScreen(
    onBack: () -> Unit,
    onSelectCategory: (BehavioralCategory) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← Interview") }
        Text(
            "Behavioral Workouts",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(BehavioralCategory.entries) { category ->
                InterviewModeRow(
                    mark = category.displayName.take(1),
                    title = category.displayName,
                    onClick = { onSelectCategory(category) },
                )
            }
        }
    }
}

// ---------------------------------------------------------------- adaptive behavioral round

data class BehavioralRoundUiState(
    val loading: Boolean = true,
    val notAvailable: Boolean = false,
    val roundState: InterviewRoundState? = null,
    val selectedId: String? = null,
    val checked: Boolean = false,
)

class BehavioralRoundViewModel(
    private val engine: InterviewEngine,
    private val progressRepo: InterviewProgressRepository,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(BehavioralRoundUiState())
    val state: StateFlow<BehavioralRoundUiState> = _state.asStateFlow()

    private var userId: String? = null

    fun start(category: BehavioralCategory?) {
        viewModelScope.launch {
            _state.value = BehavioralRoundUiState(loading = true)
            val uid = currentUser.ensureLoaded()?.id ?: return@launch
            userId = uid
            val progress = progressRepo.load(uid)
            val fresh = InterviewRoundState(
                id = "behavioral-${time.nowMillis()}",
                userId = uid,
                surface = InterviewRoundSurface.BEHAVIORAL,
                category = category,
                difficulty = progress?.behavioralDifficulty ?: PracticeDifficulty.BEGINNER,
                consecutiveBelowEight = progress?.behavioralConsecutiveBelowTarget ?: 0,
                startedAt = time.nowMillis(),
            )
            advanceRound(fresh)
        }
    }

    private suspend fun advanceRound(current: InterviewRoundState) {
        val updated = engine.startBehavioralRound(
            current, InterviewContent.starWorkouts, InterviewContent.behavioralExercises, time.nowMillis(),
        )
        _state.value = BehavioralRoundUiState(
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
            var updated = engine.submitBehavioralRoundAnswer(roundState, question.instanceId, choiceId, now)
            if (updated.currentRound?.isFullyAnswered == true) {
                updated = engine.finishBehavioralRound(updated, now)
                persistIfFinished(updated, now)
            }
            _state.value = _state.value.copy(roundState = updated, selectedId = null, checked = false)
        }
    }

    fun continueRound() {
        val roundState = _state.value.roundState ?: return
        viewModelScope.launch { advanceRound(roundState) }
    }

    private suspend fun persistIfFinished(updated: InterviewRoundState, now: Long) {
        if (updated.phase != AdaptivePracticePhase.ROUND_RESULTS) return
        val uid = userId ?: return
        val progress = progressRepo.load(uid)
        val newProgress = engine.applyBehavioralRoundToProgress(progress, uid, updated, now)
        progressRepo.save(newProgress, now)
    }
}

@Composable
fun BehavioralRoundScreen(
    category: BehavioralCategory?,
    onExit: () -> Unit,
    viewModel: BehavioralRoundViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(category) { viewModel.start(category) }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.notAvailable -> NotAvailableState(onExit)
        state.roundState?.phase == AdaptivePracticePhase.ROUND_RESULTS -> BehavioralRoundResultsPage(
            roundState = state.roundState!!,
            onContinue = viewModel::continueRound,
            onFinish = onExit,
        )
        state.roundState?.currentRound?.currentQuestion != null -> BehavioralQuestionPage(
            state = state,
            onExit = onExit,
            onSelect = viewModel::select,
            onCheck = viewModel::check,
            onNext = viewModel::next,
        )
    }
}

@Composable
private fun BehavioralQuestionPage(
    state: BehavioralRoundUiState,
    onExit: () -> Unit,
    onSelect: (String) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
) {
    val round = state.roundState?.currentRound ?: return
    val question = round.currentQuestion ?: return

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onExit) { Text("Exit") }
            Text(
                "QUESTION ${round.currentIndex + 1} OF ${round.questions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        question.category?.let {
            Text(it.displayName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
        }
        if (question.isReplay) {
            Text(
                "You already solved this one - a quick reminder before moving on.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        question.commonQuestion?.let {
            Text(
                "Common interview question: \"$it\"",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        question.scenario?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
        }
        question.referenceAnswer?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
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
            onSelect = onSelect,
            onCheck = onCheck,
        )
        if (state.checked) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            ) { Text(if (round.currentIndex == round.questions.lastIndex) "Finish" else "Next") }
        }
    }
}

@Composable
private fun BehavioralRoundResultsPage(
    roundState: InterviewRoundState,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
) {
    val round = roundState.currentRound ?: return
    val dimensionBreakdown = round.dimensionBreakdown
    val categories = round.questions.mapNotNull { it.category }.distinct()
    val title = if (categories.size == 1) categories.first().displayName.uppercase() else "BEHAVIORAL WORKOUT"

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Column(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    Text(
                        "COMPLETED ROUND: $title",
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
                    if (dimensionBreakdown.isNotEmpty()) {
                        Text(
                            "Dimension breakdown",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
                        )
                    }
                }
            }
            items(dimensionBreakdown) { score ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ratingContainerColor(score.rating)),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(score.dimension.displayName, fontWeight = FontWeight.Bold)
                        Text(score.rating.label, color = ratingColor(score.rating))
                    }
                }
            }
        }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Continue Practicing") }
        TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) { Text("Finish Practice") }
    }
}

internal fun PracticeDifficulty.label(): String = when (this) {
    PracticeDifficulty.BEGINNER -> "Beginner"
    PracticeDifficulty.DEVELOPING -> "Developing"
    PracticeDifficulty.INTERMEDIATE -> "Intermediate"
    PracticeDifficulty.ADVANCED -> "Advanced"
}

@Composable
internal fun NotAvailableState(onExit: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Not available yet", style = MaterialTheme.typography.headlineMedium)
        Text("This content isn't authored yet - more is on the way.")
        Button(onClick = onExit) { Text("Back") }
    }
}
