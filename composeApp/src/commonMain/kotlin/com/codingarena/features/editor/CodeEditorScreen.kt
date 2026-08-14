package com.codingarena.features.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.ArenaCourse
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.SectionHeader
import com.codingarena.domain.engine.CourseProgressEngine
import com.codingarena.domain.model.ChapterProgress
import com.codingarena.domain.model.CodeDraft
import com.codingarena.domain.model.CodeSubmission
import com.codingarena.domain.model.CreateSubmissionRequest
import com.codingarena.domain.model.ProgrammingLanguage
import com.codingarena.domain.model.SubmissionPurpose
import com.codingarena.domain.model.SubmissionStatus
import com.codingarena.domain.repository.CodeDraftRepository
import com.codingarena.domain.repository.CourseProgressRepository
import com.codingarena.domain.submission.SubmissionGateway
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class CodeEditorUiState(
    val loading: Boolean = true,
    val language: ProgrammingLanguage = ProgrammingLanguage.PYTHON,
    val sourceCode: String = "",
    val submission: CodeSubmission? = null,
    val submitting: Boolean = false,
    val message: String? = null,
)

class CodeEditorViewModel(
    private val drafts: CodeDraftRepository,
    private val submissions: SubmissionGateway,
    private val progressRepository: CourseProgressRepository,
    private val progressEngine: CourseProgressEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
    private val ids: IdGenerator,
) : ViewModel() {
    private val _state = MutableStateFlow(CodeEditorUiState())
    val state: StateFlow<CodeEditorUiState> = _state.asStateFlow()
    private var exerciseId: String? = null

    fun load(id: String) {
        if (exerciseId == id && !_state.value.loading) return
        exerciseId = id
        selectLanguage(ProgrammingLanguage.PYTHON)
    }

    fun selectLanguage(language: ProgrammingLanguage) {
        viewModelScope.launch {
            val exercise = ArenaCourse.course.exercise(exerciseId ?: return@launch) ?: return@launch
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val saved = drafts.draft(userId, exercise.id, language)
            val template = exercise.templates.firstOrNull { it.language == language }?.starterCode.orEmpty()
            _state.value = _state.value.copy(
                loading = false,
                language = language,
                sourceCode = saved?.sourceCode ?: template,
                submission = null,
                message = if (saved != null) "Offline draft restored" else null,
            )
        }
    }

    fun edit(code: String) { _state.value = _state.value.copy(sourceCode = code, message = null) }

    fun saveDraft() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val id = exerciseId ?: return@launch
            drafts.save(CodeDraft(userId, id, _state.value.language, _state.value.sourceCode, time.nowMillis()))
            _state.value = _state.value.copy(message = "Draft saved on this device")
        }
    }

    fun run() = send(SubmissionPurpose.RUN)
    fun submit() = send(SubmissionPurpose.SUBMIT)

    private fun send(purpose: SubmissionPurpose) {
        if (_state.value.submitting) return
        viewModelScope.launch {
            val exercise = ArenaCourse.course.exercise(exerciseId ?: return@launch) ?: return@launch
            saveDraft()
            _state.value = _state.value.copy(submitting = true, message = "Queued…", submission = null)
            runCatching {
                val accepted = submissions.create(
                    CreateSubmissionRequest(exercise.id, _state.value.language, _state.value.sourceCode, purpose)
                )
                var result = submissions.get(accepted.id)
                repeat(MAX_POLLS) {
                    if (result.status !in activeStatuses) return@repeat
                    delay(POLL_DELAY_MS)
                    result = submissions.get(accepted.id)
                }
                result
            }.onSuccess { result ->
                _state.value = _state.value.copy(
                    submitting = false,
                    submission = result,
                    message = statusMessage(result),
                )
                if (purpose == SubmissionPurpose.SUBMIT && result.status == SubmissionStatus.PASSED) {
                    recordPass(exercise.chapterId, exercise.id)
                }
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    submitting = false,
                    message = error.message ?: "Code execution is unavailable.",
                )
            }
        }
    }

    private suspend fun recordPass(chapterId: String, exerciseId: String) {
        val userId = currentUser.ensureLoaded()?.id ?: return
        val current = progressRepository.chapter(userId, chapterId) ?: ChapterProgress(userId, chapterId)
        progressRepository.save(
            progressEngine.recordIndependentPass(current, exerciseId, ids.newId(), time.nowMillis())
        )
    }

    private fun statusMessage(submission: CodeSubmission): String = when (submission.status) {
        SubmissionStatus.PASSED -> "Best move — every test passed."
        SubmissionStatus.FAILED -> "Inaccuracy — inspect the failed public tests and edge cases."
        SubmissionStatus.SYSTEM_ERROR -> "The execution service could not grade this submission."
        SubmissionStatus.CANCELLED -> "Submission cancelled."
        else -> submission.status.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    private companion object {
        const val MAX_POLLS = 40
        const val POLL_DELAY_MS = 500L
        val activeStatuses = setOf(SubmissionStatus.QUEUED, SubmissionStatus.COMPILING, SubmissionStatus.RUNNING)
    }
}

@Composable
fun CodeEditorScreen(
    exerciseId: String,
    onBack: () -> Unit,
    viewModel: CodeEditorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val exercise = ArenaCourse.course.exercise(exerciseId)
    LaunchedEffect(exerciseId) { viewModel.load(exerciseId) }
    if (exercise == null) { Text("Exercise not found"); return }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 6.dp)) { Text("Back") }
        Text("INDEPENDENT POSITION", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Text(exercise.title, style = MaterialTheme.typography.headlineSmall)
        Text(exercise.prompt, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            exercise.supportedLanguages.forEach { language ->
                FilterChip(
                    selected = state.language == language,
                    onClick = { viewModel.selectLanguage(language) },
                    label = { Text(language.displayName) },
                )
            }
        }
        OutlinedTextField(
            value = state.sourceCode,
            onValueChange = viewModel::edit,
            modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            label = { Text("Your solution") },
        )
        state.submission?.let { submission ->
            SectionHeader("Test results", trailing = "${submission.hiddenPassed}/${submission.hiddenTotal} hidden")
            submission.publicTests.forEach { test ->
                Text("${if (test.passed) "✓" else "✕"} ${test.name}: ${test.actualOutput.orEmpty()}")
            }
            submission.compilerOutput?.takeIf { it.isNotBlank() }?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        state.message?.let { Text(it, Modifier.padding(vertical = 6.dp)) }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = viewModel::saveDraft, Modifier.weight(1f)) { Text("Save") }
            OutlinedButton(onClick = viewModel::run, enabled = !state.submitting, modifier = Modifier.weight(1f)) {
                Text("Run")
            }
            Button(onClick = viewModel::submit, enabled = !state.submitting, modifier = Modifier.weight(1f)) {
                if (state.submitting) CircularProgressIndicator() else Text("Submit")
            }
        }
    }
}
