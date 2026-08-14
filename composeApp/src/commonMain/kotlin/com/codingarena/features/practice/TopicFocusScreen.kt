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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.NeetCode150
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ProgressBar
import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class TopicFocusUiState(val topics: List<TopicSummary> = emptyList())

data class TopicSummary(val group: PatternGroup, val fraction: Float)

class TopicFocusViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val engine: BlitzEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(TopicFocusUiState())
    val state: StateFlow<TopicFocusUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val curriculum = NeetCode150.curriculum
            val records = curriculumRepo.records(userId)
            val progress = engine.progress(curriculum, records, time.nowMillis())
            _state.value = TopicFocusUiState(
                topics = curriculum.sections
                    .filter { section -> (progress.sectionProgress[section.group]?.seen ?: 0) > 0 }
                    .map { section ->
                        TopicSummary(section.group, progress.sectionProgress[section.group]?.fraction ?: 0f)
                    },
            )
        }
    }
}

/** Only topics the user has already touched - "already learned," per the spec. */
@Composable
fun TopicFocusScreen(
    onBack: () -> Unit,
    onSelectTopic: (PatternGroup) -> Unit,
    viewModel: TopicFocusViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← Practice") }
        Text(
            "Focus on a topic",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        )
        if (state.topics.isEmpty()) {
            Text(
                "Work through a bit of the Roadmap first, and topics you've touched will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
            return@Column
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(state.topics, key = { it.group.name }) { topic ->
                TopicRow(topic = topic, onClick = { onSelectTopic(topic.group) })
            }
        }
    }
}

@Composable
private fun TopicRow(topic: TopicSummary, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
            )
            Column(Modifier.weight(1f)) {
                Text(topic.group.displayName, style = MaterialTheme.typography.titleMedium)
                ProgressBar(topic.fraction, Modifier.padding(top = 6.dp))
            }
            Text(
                "${(topic.fraction * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
