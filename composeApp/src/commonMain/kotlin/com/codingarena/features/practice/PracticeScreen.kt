package com.codingarena.features.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.NeetCode150
import com.codingarena.content.ProblemWorkouts
import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.engine.BlitzMode
import com.codingarena.domain.engine.PracticeWorkoutEngine
import com.codingarena.domain.engine.storageKey
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class PracticeUiState(
    val currentTopic: PatternGroup? = null,
    val recommendedAvailable: Boolean = false,
    val hasCompletedLesson: Boolean = false,
)

class PracticeViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val blitzEngine: BlitzEngine,
    private val workoutEngine: PracticeWorkoutEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val curriculum = NeetCode150.curriculum
            val records = curriculumRepo.records(userId)
            val progress = blitzEngine.progress(curriculum, records, time.nowMillis())
            _state.value = PracticeUiState(
                currentTopic = workoutEngine.currentTopic(curriculum, progress),
                recommendedAvailable = ProblemWorkouts.workouts.isNotEmpty(),
                hasCompletedLesson = records.values.any { it.solved },
            )
        }
    }
}

@Composable
fun PracticeScreen(
    onStartRecommended: () -> Unit,
    onOpenTopicFocus: () -> Unit,
    onStartBlitz: (String) -> Unit,
    onStartMixed: () -> Unit,
    onOpenCodeRush: () -> Unit,
    onOpenQuickRecall: () -> Unit,
    viewModel: PracticeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
    ) {
        item {
            Text(
                "Practice",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 20.dp, bottom = 24.dp),
            )
        }
        item {
            RecommendedPracticeCard(
                topicName = state.currentTopic?.displayName,
                available = state.recommendedAvailable,
                onStart = onStartRecommended,
            )
        }
        item { SectionLabel("CHOOSE A MODE") }
        item {
            PracticeModeRow(
                mark = "▦",
                title = "Practice by topic",
                subtitle = "Choose a roadmap pattern",
                onClick = onOpenTopicFocus,
            )
        }
        item {
            PracticeModeRow(
                mark = "{ }",
                title = "Work on weak spots",
                subtitle = if (state.hasCompletedLesson) "Start with what needs attention"
                    else "Complete a lesson to unlock this",
                enabled = state.hasCompletedLesson,
                onClick = { onStartBlitz(BlitzMode.WeakestFirst.storageKey) },
            )
        }
        item {
            PracticeModeRow(
                mark = "◐",
                title = "Mixed practice",
                subtitle = "A shuffled set across learned topics",
                onClick = onStartMixed,
            )
        }
        item {
            PracticeModeRow(
                mark = "⚡",
                title = "Quick recall",
                subtitle = "Short pattern-recognition drills",
                onClick = onOpenQuickRecall,
            )
        }
        item {
            PracticeModeRow(
                mark = "CR",
                title = "Code Rush",
                subtitle = "Timed mixed questions · 5 minutes",
                onClick = onOpenCodeRush,
            )
        }
        item { Box(Modifier.padding(bottom = 24.dp)) }
    }
}

@Composable
private fun RecommendedPracticeCard(
    topicName: String?,
    available: Boolean,
    onStart: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().background(
            MaterialTheme.colorScheme.primary,
            RoundedCornerShape(22.dp),
        ),
    ) {
        Column(Modifier.padding(22.dp)) {
            Text(
                "RECOMMENDED · 3 QUESTIONS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
            )
            Text(
                topicName ?: "Mixed review",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                "A short set based on what you need to remember.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Button(
                onClick = onStart,
                enabled = available,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                ),
                shape = RoundedCornerShape(14.dp),
            ) { Text(if (available) "Start practice  →" else "Coming soon", fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 28.dp, bottom = 7.dp),
    )
}

@Composable
private fun PracticeModeRow(
    mark: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val contentAlpha = if (enabled) 1f else 0.4f
    Column(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = contentAlpha),
                    RoundedCornerShape(12.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    mark,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Text(
                "›",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
