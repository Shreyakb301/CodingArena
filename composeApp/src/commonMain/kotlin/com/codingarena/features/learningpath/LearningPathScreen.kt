package com.codingarena.features.learningpath

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.StarterContent
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ArenaListItem
import com.codingarena.core.design.EmptyState
import com.codingarena.core.design.ProgressBar
import com.codingarena.core.design.SectionHeader
import com.codingarena.domain.engine.LearningPathEngine
import com.codingarena.domain.engine.SpacedRepetitionEngine
import com.codingarena.domain.model.LearningPath
import com.codingarena.domain.model.LearningPathStep
import com.codingarena.domain.model.ScheduledReview
import com.codingarena.domain.model.StepKind
import com.codingarena.domain.repository.LearningPathRepository
import com.codingarena.domain.repository.ReviewRepository
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.domain.usecase.RefreshLearningPathUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class LearningPathUiState(
    val loading: Boolean = true,
    val path: LearningPath? = null,
    val dueReviews: List<ScheduledReview> = emptyList(),
)

class LearningPathViewModel(
    private val paths: LearningPathRepository,
    private val reviews: ReviewRepository,
    private val refreshPath: RefreshLearningPathUseCase,
    private val pathEngine: LearningPathEngine,
    private val srsEngine: SpacedRepetitionEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(LearningPathUiState())
    val state: StateFlow<LearningPathUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            _state.value = LearningPathUiState(
                loading = false,
                path = refreshPath(userId),
                dueReviews = srsEngine.dueReviews(reviews.all(userId), time.nowMillis()),
            )
        }
    }

    fun buildNewPath() {
        viewModelScope.launch {
            val userId = currentUser.userId ?: return@launch
            _state.value = _state.value.copy(path = refreshPath(userId, force = true))
        }
    }

    fun markLessonRead(stepId: String) {
        val path = _state.value.path ?: return
        viewModelScope.launch {
            val updated = pathEngine.completeLesson(path, stepId, time.nowMillis())
            paths.save(updated)
            _state.value = _state.value.copy(path = updated)
        }
    }
}

@Composable
fun LearningPathScreen(
    onOpenProblem: (String) -> Unit,
    onOpenPattern: (String) -> Unit,
    viewModel: LearningPathViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val path = state.path

        if (path == null) {
            item {
                if (state.loading) {
                    EmptyState("Working out what you should practise next...")
                } else {
                    EmptyState("Solve a few problems and a learning path will be built for you.")
                }
            }
        } else {
            item {
                Card(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(path.title, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            path.rationale,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        ProgressBar(path.fraction, Modifier.padding(top = 12.dp))
                        Text(
                            "${path.completedStepCount} of ${path.steps.size} steps complete",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }

            item { SectionHeader("Steps") }
            items(path.steps) { step ->
                StepCard(
                    step = step,
                    isCurrent = path.currentStep?.id == step.id,
                    onOpenProblem = onOpenProblem,
                    onOpenPattern = onOpenPattern,
                    onMarkRead = { viewModel.markLessonRead(step.id) },
                )
            }

            if (path.isComplete) {
                item {
                    Button(
                        onClick = viewModel::buildNewPath,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Build my next path") }
                }
            }
        }

        if (state.dueReviews.isNotEmpty()) {
            item { SectionHeader("Due for review", trailing = "${state.dueReviews.size}") }
            items(state.dueReviews) { review ->
                val problem = StarterContent.byId(review.problemId)
                ArenaListItem(
                    title = problem?.title ?: review.problemId,
                    subtitle = "${review.topic.displayName} - " +
                        if (review.lapses > 0) "missed ${review.lapses} time(s)" else "scheduled review",
                    trailing = review.stage.displayName,
                    onClick = { onOpenProblem(review.problemId) },
                )
            }
        }

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}

@Composable
private fun StepCard(
    step: LearningPathStep,
    isCurrent: Boolean,
    onOpenProblem: (String) -> Unit,
    onOpenPattern: (String) -> Unit,
    onMarkRead: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(step.title, style = MaterialTheme.typography.titleMedium)
                ArenaChip(
                    if (step.isComplete) "Done" else step.kind.displayName,
                    filled = step.isComplete,
                )
            }

            // Hoisted into a local: `patternId` lives in :core, so the compiler
            // will not smart-cast it across the module boundary.
            val patternId = step.patternId
            if (step.kind == StepKind.LESSON && patternId != null) {
                Row(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = { onOpenPattern(patternId) }) { Text("Read") }
                    if (!step.isComplete) {
                        OutlinedButton(onClick = onMarkRead) { Text("Mark read") }
                    }
                }
            }

            step.problemIds.forEach { problemId ->
                val problem = StarterContent.byId(problemId)
                val done = problemId in step.completedProblemIds
                ArenaListItem(
                    title = problem?.title ?: problemId,
                    subtitle = if (done) "Solved" else problem?.challengeType?.displayName,
                    trailing = problem?.difficultyRating?.toString(),
                    onClick = { onOpenProblem(problemId) },
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
