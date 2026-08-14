package com.codingarena.features.course

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.ArenaCourse
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ProgressBar
import com.codingarena.domain.engine.CourseProgressEngine
import com.codingarena.domain.model.ChapterProgress
import com.codingarena.domain.model.MasteryState
import com.codingarena.domain.repository.CourseProgressRepository
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.model.ProgressSyncPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class CourseRoadmapUiState(
    val loading: Boolean = true,
    val progress: Map<String, ChapterProgress> = emptyMap(),
    val states: Map<String, MasteryState> = emptyMap(),
)

class CourseRoadmapViewModel(
    private val repository: CourseProgressRepository,
    private val currentUser: CurrentUser,
    private val engine: CourseProgressEngine,
    private val time: TimeProvider,
    private val classroom: ClassroomGateway,
) : ViewModel() {
    private val _state = MutableStateFlow(CourseRoadmapUiState())
    val state: StateFlow<CourseRoadmapUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            repository.observe(userId).collect { progress ->
                _state.value = CourseRoadmapUiState(
                    loading = false,
                    progress = progress,
                    states = engine.states(ArenaCourse.course, progress, time.nowMillis()),
                )
                runCatching {
                    classroom.pushProgress(
                        ProgressSyncPayload(progress.values.toList(), updatedAt = time.nowMillis())
                    )
                }
            }
        }
    }
}

@Composable
fun CourseRoadmapScreen(
    onOpenChapter: (String) -> Unit,
    viewModel: CourseRoadmapViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val available = ArenaCourse.availableChapters
    val mastered = state.states.values.count { it == MasteryState.MASTERED }
    val due = state.states.values.count { it == MasteryState.REVIEW_DUE }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(Modifier.padding(top = 22.dp, bottom = 12.dp)) {
                Text("YOUR TRAINING BOARD", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Text("Roadmap", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Learn a rank, prove it independently, then keep it sharp.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("$mastered mastered · $due due", style = MaterialTheme.typography.labelLarge)
                    Text("${available.size} available", style = MaterialTheme.typography.labelLarge)
                }
                ProgressBar(mastered.toFloat() / available.size.coerceAtLeast(1), Modifier.padding(top = 8.dp))
            }
        }

        items(ArenaCourse.course.chapters) { chapter ->
            val mastery = state.states[chapter.id] ?: MasteryState.LOCKED
            val locked = mastery == MasteryState.LOCKED
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = !locked) { onOpenChapter(chapter.id) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    Modifier.size(54.dp).clip(CircleShape).background(
                        when (mastery) {
                            MasteryState.MASTERED -> MaterialTheme.colorScheme.primary
                            MasteryState.REVIEW_DUE -> MaterialTheme.colorScheme.errorContainer
                            MasteryState.LOCKED -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (mastery == MasteryState.MASTERED) "♛" else chapter.rank.toString(),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Rank ${chapter.rank} · ${chapter.title}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (chapter.available) chapter.summary else "Coming soon",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ArenaChip(mastery.displayName, filled = mastery == MasteryState.REVIEW_DUE)
            }
        }
        item { Box(Modifier.padding(bottom = 24.dp)) }
    }
}
