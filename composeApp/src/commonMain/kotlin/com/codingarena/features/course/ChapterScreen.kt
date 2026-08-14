package com.codingarena.features.course

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.ArenaCourse
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.CodeBlock
import com.codingarena.core.design.SectionHeader
import com.codingarena.domain.engine.CourseProgressEngine
import com.codingarena.domain.model.ChapterProgress
import com.codingarena.domain.model.ExerciseMode
import com.codingarena.domain.model.MasteryState
import com.codingarena.domain.repository.CourseProgressRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class ChapterUiState(
    val loading: Boolean = true,
    val progress: ChapterProgress? = null,
    val mastery: MasteryState = MasteryState.LEARNING,
    val checkpointChoice: Int? = null,
    val checkpointFeedback: String? = null,
)

class ChapterViewModel(
    private val repository: CourseProgressRepository,
    private val currentUser: CurrentUser,
    private val engine: CourseProgressEngine,
    private val time: TimeProvider,
    private val ids: IdGenerator,
) : ViewModel() {
    private val _state = MutableStateFlow(ChapterUiState())
    val state: StateFlow<ChapterUiState> = _state.asStateFlow()
    private var chapterId: String? = null

    fun load(id: String) {
        if (chapterId == id && !_state.value.loading) return
        chapterId = id
        refresh()
    }

    fun completeLesson() = update { progress, chapter ->
        chapter.lessonBlockIds.fold(progress) { value, blockId ->
            if (blockId in value.completedBlockIds) value
            else engine.recordLessonBlock(value, blockId, ids.newId(), time.nowMillis())
        }
    }

    fun answerCheckpoint(index: Int) {
        val chapter = ArenaCourse.course.chapter(chapterId ?: return) ?: return
        val checkpoint = chapter.checkpointExercises.firstOrNull() ?: return
        val correct = index == checkpoint.correctChoiceIndex
        _state.value = _state.value.copy(
            checkpointChoice = index,
            checkpointFeedback = if (correct) "Best move — practice is unlocked."
            else "Inaccuracy — choose a structural signal, then try again.",
        )
        if (correct) update { progress, _ ->
            engine.recordCheckpoint(progress, checkpoint.id, 1.0, ids.newId(), time.nowMillis())
        }
    }

    private fun update(block: (ChapterProgress, com.codingarena.domain.model.Chapter) -> ChapterProgress) {
        viewModelScope.launch {
            val id = chapterId ?: return@launch
            val chapter = ArenaCourse.course.chapter(id) ?: return@launch
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val current = repository.chapter(userId, id) ?: ChapterProgress(userId, id)
            repository.save(block(current, chapter))
            refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val id = chapterId ?: return@launch
            val chapter = ArenaCourse.course.chapter(id) ?: return@launch
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val all = repository.all(userId)
            _state.value = _state.value.copy(
                loading = false,
                progress = all[id] ?: ChapterProgress(userId, id),
                mastery = engine.state(chapter, all[id], all, time.nowMillis()),
            )
        }
    }
}

@Composable
fun ChapterScreen(
    chapterId: String,
    onOpenEditor: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ChapterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val chapter = ArenaCourse.course.chapter(chapterId)
    LaunchedEffect(chapterId) { viewModel.load(chapterId) }
    if (chapter == null) { Text("Chapter not found"); return }
    val checkpoint = chapter.checkpointExercises.first()

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("Back") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("RANK ${chapter.rank}", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Text(chapter.title, style = MaterialTheme.typography.headlineMedium)
                }
                ArenaChip(state.mastery.displayName)
            }
            Text(chapter.summary, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        chapter.lessons.forEach { lesson ->
            item { SectionHeader(lesson.title) }
            items(lesson.blocks) { block ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(block.title, style = MaterialTheme.typography.titleMedium)
                        Text(block.explanation, Modifier.padding(top = 6.dp))
                        CodeBlock(block.visualExample, Modifier.padding(top = 12.dp))
                        Text("Recognition signals", style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 12.dp))
                        block.recognitionSignals.forEach { Text("• $it") }
                    }
                }
            }
        }
        item { SectionHeader("Guided puzzles") }
        items(chapter.exercises.filter {
            it.mode in setOf(ExerciseMode.TRACE, ExerciseMode.REARRANGE, ExerciseMode.DEBUG, ExerciseMode.FILL_CODE)
        }) { exercise ->
            var picked by remember(exercise.id) { mutableStateOf<Int?>(null) }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp)) {
                    ArenaChip(exercise.mode.displayName)
                    Text(exercise.prompt, style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp))
                    exercise.choices.forEachIndexed { index, choice ->
                        OutlinedButton(
                            onClick = { picked = index },
                            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                        ) { Text(choice) }
                    }
                    picked?.let { index ->
                        Text(
                            if (index == exercise.correctChoiceIndex) "Best move — ${exercise.explanation}"
                            else "Inaccuracy — trace the invariant and try again.",
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
        item {
            Button(onClick = viewModel::completeLesson, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.progress?.completedBlockIds?.containsAll(chapter.lessonBlockIds) == true)
                    "Lesson complete ✓" else "Complete lesson")
            }
        }
        if (state.progress?.completedBlockIds?.containsAll(chapter.lessonBlockIds) == true) {
            item { SectionHeader("Chapter checkpoint") }
            item { Text(checkpoint.prompt, style = MaterialTheme.typography.titleMedium) }
            items(checkpoint.choices) { choice ->
                val index = checkpoint.choices.indexOf(choice)
                OutlinedButton(
                    onClick = { viewModel.answerCheckpoint(index) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(choice) }
            }
            state.checkpointFeedback?.let { feedback -> item { Text(feedback) } }
        }
        if ((state.progress?.checkpointScore ?: 0.0) >= CourseProgressEngine.CHECKPOINT_PASS) {
            item { SectionHeader("Independent practice") }
            items(chapter.independentExercises) { exercise ->
                Button(onClick = { onOpenEditor(exercise.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Open ${exercise.title}")
                }
            }
        }
        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}
