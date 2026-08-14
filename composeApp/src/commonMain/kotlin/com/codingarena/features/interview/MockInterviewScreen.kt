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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.codingarena.domain.model.MockInterview
import com.codingarena.domain.model.MockInterviewAnswer
import com.codingarena.domain.model.MockInterviewResult
import com.codingarena.domain.model.MockInterviewSession
import com.codingarena.domain.repository.InterviewProgressRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class MockInterviewUiState(
    val loading: Boolean = true,
    val notAvailable: Boolean = false,
    val session: MockInterviewSession? = null,
    val result: MockInterviewResult? = null,
)

class MockInterviewViewModel(
    private val engine: InterviewEngine,
    private val progressRepo: InterviewProgressRepository,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(MockInterviewUiState())
    val state: StateFlow<MockInterviewUiState> = _state.asStateFlow()

    private var userId: String? = null

    fun start() {
        viewModelScope.launch {
            _state.value = MockInterviewUiState(loading = true)
            val uid = currentUser.ensureLoaded()?.id ?: return@launch
            userId = uid
            val progress = progressRepo.load(uid)
            val mock = engine.pickMockInterview(progress, InterviewContent.mockInterviews)
            _state.value = if (mock == null) {
                MockInterviewUiState(loading = false, notAvailable = true)
            } else {
                MockInterviewUiState(loading = false, session = engine.startMockInterview(mock))
            }
        }
    }

    fun submit(choiceId: String) {
        val session = _state.value.session ?: return
        val updatedSession = engine.submitMockAnswer(session, choiceId)
        if (!updatedSession.isOver) {
            _state.value = _state.value.copy(session = updatedSession)
            return
        }
        val now = time.nowMillis()
        val result = engine.finishMockInterview(updatedSession, now) ?: return
        _state.value = _state.value.copy(session = updatedSession, result = result)

        val uid = userId ?: return
        viewModelScope.launch {
            val progress = progressRepo.load(uid)
            val updatedProgress = engine.recordMockResult(progress, uid, result, now)
            progressRepo.save(updatedProgress, now)
        }
    }
}

@Composable
fun MockInterviewScreen(
    onExit: () -> Unit,
    viewModel: MockInterviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.start() }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.notAvailable -> NotAvailableState(onExit)
        state.result != null -> MockResultsPage(state.result!!, onDone = onExit)
        state.session != null -> MockDecisionPage(state.session!!, onExit = onExit, onSubmit = viewModel::submit)
    }
}

@Composable
private fun MockDecisionPage(
    session: MockInterviewSession,
    onExit: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val decision = session.currentDecision ?: return
    var selectedId by remember(decision.id) { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onExit) { Text("Exit") }
            Text(
                "DECISION ${session.currentDecisionIndex + 1} OF ${session.mock.decisions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (session.currentDecisionIndex == 0) {
            Text(session.mock.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                session.mock.setup,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
        }
        RatedDecisionPoint(
            scenario = null,
            prompt = decision.prompt,
            choices = decision.choices,
            selectedId = selectedId,
            checked = false,
            showFeedback = false,
            checkLabel = if (session.currentDecisionIndex == session.mock.decisions.lastIndex) "Finish" else "Continue",
            onSelect = { selectedId = it },
            onCheck = { selectedId?.let(onSubmit) },
        )
    }
}

@Composable
private fun MockResultsPage(result: MockInterviewResult, onDone: () -> Unit) {
    val mock = InterviewContent.mockInterviews.firstOrNull { it.id == result.mockId } ?: return
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Column(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    Text(
                        mock.title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        result.overallRating.label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = ratingColor(result.overallRating),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        "Here's how each decision graded, now that the interview is over.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    )
                }
            }
            items(result.answers) { answer -> MockAnswerCard(mock, answer, mock.decisions.firstOrNull { it.id == answer.decisionId }?.prompt) }
        }
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) { Text("Done") }
    }
}

@Composable
private fun MockAnswerCard(mock: MockInterview, answer: MockInterviewAnswer, prompt: String?) {
    val choice = mock.decisions
        .firstOrNull { it.id == answer.decisionId }
        ?.choices?.firstOrNull { it.id == answer.choiceId }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ratingContainerColor(answer.rating)),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            prompt?.let { Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) }
            Text(answer.rating.label, color = ratingColor(answer.rating), fontWeight = FontWeight.Bold)
            choice?.let { Text(it.feedback) }
        }
    }
}
