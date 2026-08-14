package com.codingarena.features.blitz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.ArenaCourse
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.engine.CourseProgressEngine
import com.codingarena.domain.model.ChapterProgress
import com.codingarena.domain.model.ConfusionSummary
import com.codingarena.domain.model.Exercise
import com.codingarena.domain.model.MasteryState
import com.codingarena.domain.model.ProgressSyncPayload
import com.codingarena.domain.repository.CourseProgressRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class StrategyBlitzUiState(
    val queue: List<Exercise> = emptyList(),
    val index: Int = 0,
    val picked: Int? = null,
    val correct: Int = 0,
    val confusions: List<ConfusionSummary> = emptyList(),
    val finished: Boolean = false,
) {
    val current: Exercise? get() = queue.getOrNull(index)
}

class StrategyBlitzViewModel(
    private val repository: CourseProgressRepository,
    private val currentUser: CurrentUser,
    private val engine: CourseProgressEngine,
    private val time: TimeProvider,
    private val ids: IdGenerator,
    private val classroom: ClassroomGateway,
) : ViewModel() {
    private val _state = MutableStateFlow(StrategyBlitzUiState())
    val state: StateFlow<StrategyBlitzUiState> = _state.asStateFlow()
    private var userId: String? = null

    fun start() {
        if (_state.value.queue.isNotEmpty()) return
        viewModelScope.launch {
            val id = currentUser.ensureLoaded()?.id ?: return@launch
            userId = id
            val progress = repository.all(id)
            val states = engine.states(ArenaCourse.course, progress, time.nowMillis())
            val queue = ArenaCourse.availableChapters
                .filter { states[it.id] in setOf(MasteryState.PRACTICE, MasteryState.MASTERED, MasteryState.REVIEW_DUE) }
                .sortedBy { statePriority(states[it.id]) }
                .mapNotNull { chapter -> chapter.exercises.firstOrNull { it.mode.name == "BLITZ" } }
                .take(10)
            _state.value = StrategyBlitzUiState(queue = queue, finished = queue.isEmpty())
        }
    }

    fun answer(index: Int) {
        val current = _state.value.current ?: return
        if (_state.value.picked != null) return
        val correct = index == current.correctChoiceIndex
        val confusion = if (!correct) {
            ConfusionSummary(
                actual = current.choices[current.correctChoiceIndex ?: 0],
                mistakenFor = current.choices[index],
                count = 1,
            )
        } else null
        _state.value = _state.value.copy(
            picked = index,
            correct = _state.value.correct + if (correct) 1 else 0,
            confusions = _state.value.confusions + listOfNotNull(confusion),
        )
        viewModelScope.launch {
            val id = userId ?: return@launch
            val progress = repository.chapter(id, current.chapterId) ?: ChapterProgress(id, current.chapterId)
            repository.save(
                engine.recordReview(progress, current.id, correct, ids.newId(), time.nowMillis())
            )
        }
    }

    fun next() {
        val next = _state.value.index + 1
        if (next >= _state.value.queue.size) {
            _state.value = _state.value.copy(finished = true)
            sync()
        } else {
            _state.value = _state.value.copy(index = next, picked = null)
        }
    }

    private fun sync() {
        viewModelScope.launch {
            val id = userId ?: return@launch
            runCatching {
                classroom.pushProgress(
                    ProgressSyncPayload(repository.all(id).values.toList(), _state.value.confusions, time.nowMillis())
                )
            }
        }
    }

    private fun statePriority(state: MasteryState?): Int = when (state) {
        MasteryState.REVIEW_DUE -> 0
        MasteryState.PRACTICE -> 1
        MasteryState.MASTERED -> 2
        else -> 3
    }
}

@Composable
fun StrategyBlitzScreen(onDone: () -> Unit, viewModel: StrategyBlitzViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.start() }

    if (state.finished) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(if (state.queue.isEmpty()) "No tactics unlocked yet" else "Round complete",
                style = MaterialTheme.typography.headlineMedium)
            Text(
                if (state.queue.isEmpty()) "Pass a Roadmap checkpoint to unlock its strategy cards."
                else "${state.correct} of ${state.queue.size} best moves found.",
                style = MaterialTheme.typography.bodyLarge,
            )
            state.confusions.groupingBy { it.actual to it.mistakenFor }.eachCount().forEach { (pair, count) ->
                Text("Review: ${pair.first} was called ${pair.second} $count time(s)")
            }
            Button(onClick = onDone, Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Done") }
        }
        return
    }

    val exercise = state.current ?: return
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("POSITION ${state.index + 1}/${state.queue.size}")
            ArenaChip("${state.correct} best moves")
        }
        Text(exercise.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 28.dp))
        Text(exercise.prompt, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 10.dp, bottom = 20.dp))
        exercise.choices.forEachIndexed { index, option ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(enabled = state.picked == null) {
                    viewModel.answer(index)
                },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        state.picked == null -> MaterialTheme.colorScheme.surface
                        index == exercise.correctChoiceIndex -> MaterialTheme.colorScheme.primaryContainer
                        index == state.picked -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
            ) { Text(option, Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) }
        }
        state.picked?.let { picked ->
            Text(
                if (picked == exercise.correctChoiceIndex) "Best move — ${exercise.explanation}"
                else "Inaccuracy — ${exercise.explanation}",
                modifier = Modifier.padding(top = 20.dp),
            )
            Button(onClick = viewModel::next, Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Next position") }
        }
        TextButton(onClick = onDone, modifier = Modifier.padding(top = 8.dp)) { Text("End round") }
    }
}
