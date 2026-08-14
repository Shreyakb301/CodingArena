package com.codingarena.features.interview

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.codingarena.content.InterviewContent
import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.engine.InterviewEngine
import com.codingarena.domain.engine.RecommendedInterviewToday
import com.codingarena.domain.model.BehavioralCategory
import com.codingarena.domain.model.InterviewProgress
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.repository.InterviewProgressRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class InterviewHomeUiState(
    val loading: Boolean = true,
    val recommended: RecommendedInterviewToday? = null,
    val progress: InterviewProgress? = null,
    val techCommAvailable: Boolean = false,
)

class InterviewHomeViewModel(
    private val engine: InterviewEngine,
    private val progressRepo: InterviewProgressRepository,
    private val curriculumRepo: CurriculumRepository,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(InterviewHomeUiState())
    val state: StateFlow<InterviewHomeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val progress = progressRepo.load(userId)
            val records = curriculumRepo.records(userId)
            val techCommAvailable = InterviewContent.techCommItems.any { (records[it.problemSlug]?.totalSeen ?: 0) > 0 }
            _state.value = InterviewHomeUiState(
                loading = false,
                recommended = engine.recommendedToday(progress, time.epochDay()),
                progress = progress,
                techCommAvailable = techCommAvailable,
            )
        }
    }
}

@Composable
fun InterviewHomeScreen(
    onStartCategoryWorkout: (BehavioralCategory) -> Unit,
    onOpenBehavioralWorkouts: () -> Unit,
    onOpenTechComm: () -> Unit,
    onOpenMockInterview: () -> Unit,
    viewModel: InterviewHomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    if (state.loading) return

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            Text(
                "Interview",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
        }
        state.recommended?.let { recommended ->
            item {
                RecommendedTodayCard(
                    recommended = recommended,
                    onStart = { onStartCategoryWorkout(recommended.category) },
                )
            }
        }
        item { SectionLabel("PRACTICE") }
        item {
            InterviewModeRow(
                mark = "★",
                title = "Behavioral workouts",
                subtitle = "Build clear STAR stories",
                onClick = onOpenBehavioralWorkouts,
            )
        }
        item {
            InterviewModeRow(
                mark = "▤",
                title = "Technical communication",
                subtitle = "Explain decisions and tradeoffs",
                enabled = state.techCommAvailable,
                onClick = onOpenTechComm,
            )
        }
        item {
            InterviewModeRow(
                mark = "◐",
                title = "Mock interview",
                subtitle = "Practice a complete interview",
                onClick = onOpenMockInterview,
            )
        }
        item { Box(Modifier.padding(bottom = 24.dp)) }
    }
}

@Composable
private fun RecommendedTodayCard(recommended: RecommendedInterviewToday, onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "RECOMMENDED TODAY",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                recommended.category.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                "A guided question-by-question workout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp),
            )
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) { Text("Start workout") }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
    )
}

@Composable
internal fun InterviewModeRow(
    mark: String,
    title: String,
    subtitle: String = "",
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val contentAlpha = if (enabled) 1f else 0.5f
            Box(
                modifier = Modifier.size(52.dp).background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = contentAlpha),
                    RoundedCornerShape(16.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    mark,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = contentAlpha),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha))
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (enabled) Text("›", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
