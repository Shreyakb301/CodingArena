package com.codingarena.features.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.CodeBlock
import com.codingarena.domain.model.AnswerChoice
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.ProgrammingLanguage
import com.codingarena.domain.model.forLanguage
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.session.ChallengeSession
import com.codingarena.domain.session.ChallengeState
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Screen state.
 *
 * All the challenge *rules* live in [ChallengeState] / [ChallengeSession] in
 * `:core`, where they are unit tested. This wrapper adds only the things that
 * are genuinely presentation concerns: loading, the ticking clock, in-flight
 * submission and errors.
 */
data class ChallengeUiState(
    val loading: Boolean = true,
    val challenge: ChallengeState? = null,
    val elapsedSeconds: Int = 0,
    val submitting: Boolean = false,
    val submittedAttemptId: String? = null,
    val error: String? = null,
) {
    val problem: CodingProblem? get() = challenge?.problem
    val selected: List<String> get() = challenge?.selected.orEmpty()
    val hintsRemaining: Int get() = challenge?.hintsRemaining ?: 0
    val canSubmit: Boolean get() = challenge?.canSubmit == true && !submitting
}

class ChallengeViewModel(
    private val problems: ProblemRepository,
    private val submitAnswer: SubmitAnswerUseCase,
    private val currentUser: CurrentUser,
    private val resultStore: PracticeResultStore,
    private val time: TimeProvider,
    private val session: ChallengeSession = ChallengeSession(),
) : ViewModel() {

    private val _state = MutableStateFlow(ChallengeUiState())
    val state: StateFlow<ChallengeUiState> = _state.asStateFlow()

    private var source: AttemptSource = AttemptSource.PRACTICE

    fun load(problemId: String, attemptSource: AttemptSource) {
        if (_state.value.problem?.id == problemId) return
        source = attemptSource
        viewModelScope.launch {
            val problem = problems.byId(problemId)
            val language = currentUser.ensureLoaded()?.onboarding?.preferredLanguage ?: ProgrammingLanguage.PYTHON
            _state.value = if (problem == null) {
                ChallengeUiState(loading = false, error = "That problem is no longer available.")
            } else {
                ChallengeUiState(
                    loading = false,
                    challenge = session.start(problem.forLanguage(language), time.nowMillis()),
                )
            }
        }
    }

    fun select(choiceId: String) = mutate { session.select(it, choiceId) }

    /** Moves a line up or down in a rearrange-the-code problem. */
    fun move(choiceId: String, delta: Int) = mutate { session.move(it, choiceId, delta) }

    fun revealHint() = mutate { session.revealHint(it) }

    fun tick() {
        val challenge = _state.value.challenge ?: return
        _state.value = _state.value.copy(
            elapsedSeconds = challenge.elapsedSeconds(time.nowMillis()),
        )
    }

    fun submit() {
        val current = _state.value
        val challenge = current.challenge ?: return
        if (!current.canSubmit) return

        _state.value = current.copy(submitting = true)
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id
            if (userId == null) {
                _state.value = _state.value.copy(submitting = false, error = "No profile found.")
                return@launch
            }
            val submission = session.toSubmission(challenge, userId, source)
            if (submission == null) {
                _state.value = _state.value.copy(submitting = false)
                return@launch
            }
            runCatching { submitAnswer(submission) }
                .onSuccess { result ->
                    resultStore.put(result, challenge.problem)
                    _state.value = _state.value.copy(
                        submitting = false,
                        challenge = session.markSubmitted(challenge),
                        submittedAttemptId = result.attempt.id,
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        submitting = false,
                        error = it.message ?: "Could not save your answer.",
                    )
                }
        }
    }

    private fun mutate(block: (ChallengeState) -> ChallengeState) {
        val challenge = _state.value.challenge ?: return
        _state.value = _state.value.copy(challenge = block(challenge))
    }
}

@Composable
fun ChallengeScreen(
    problemId: String,
    source: AttemptSource,
    onSubmitted: (String) -> Unit,
    onExit: () -> Unit,
    viewModel: ChallengeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(problemId) { viewModel.load(problemId, source) }

    LaunchedEffect(state.submittedAttemptId) {
        state.submittedAttemptId?.let(onSubmitted)
    }

    // A visible timer, because solve speed feeds both the review label and the
    // spaced-repetition interval.
    LaunchedEffect(state.challenge?.startedAt) {
        while (state.challenge != null && state.submittedAttemptId == null) {
            viewModel.tick()
            kotlinx.coroutines.delay(1000)
        }
    }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

        state.problem == null -> Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.error ?: "Problem not found.", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onExit) { Text("Go back") }
        }

        else -> ChallengeContent(state, viewModel, onExit)
    }
}

@Composable
private fun ChallengeContent(
    state: ChallengeUiState,
    viewModel: ChallengeViewModel,
    onExit: () -> Unit,
) {
    val problem = state.problem!!

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onExit) { Text("Exit") }
            Text(
                "${state.elapsedSeconds}s",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ArenaChip(problem.primaryTopic.displayName)
                    ArenaChip(problem.difficulty.displayName)
                }
            }
            item {
                Text(problem.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    problem.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            problem.codeSnippet?.let { snippet ->
                item { CodeBlock(snippet) }
            }
            item {
                Text(
                    problem.challengeType.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            if (problem.challengeType.isOrdering) {
                itemsIndexed(state.selected) { index, choiceId ->
                    val choice = problem.choice(choiceId) ?: return@itemsIndexed
                    OrderableRow(
                        position = index + 1,
                        choice = choice,
                        canMoveUp = index > 0,
                        canMoveDown = index < state.selected.lastIndex,
                        onMoveUp = { viewModel.move(choiceId, -1) },
                        onMoveDown = { viewModel.move(choiceId, 1) },
                    )
                }
            } else {
                itemsIndexed(problem.choices) { _, choice ->
                    ChoiceRow(
                        choice = choice,
                        selected = choice.id in state.selected,
                        onClick = { viewModel.select(choice.id) },
                    )
                }
            }

            val visibleHints = state.challenge?.visibleHints.orEmpty()
            if (visibleHints.isNotEmpty()) {
                itemsIndexed(visibleHints) { index, hint ->
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Hint ${index + 1}", style = MaterialTheme.typography.labelSmall)
                            Text(hint, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item { Column(Modifier.padding(bottom = 12.dp)) {} }
        }

        Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            if (state.hintsRemaining > 0) {
                OutlinedButton(
                    onClick = viewModel::revealHint,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Show a hint (${state.hintsRemaining} left) - costs rating")
                }
            }
            Button(
                onClick = viewModel::submit,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) {
                Text(if (state.submitting) "Checking..." else "Submit")
            }
            state.error?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(choice: AnswerChoice, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            choice.tag?.let {
                Box(
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(6.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) { Text(it, style = MaterialTheme.typography.labelSmall) }
            }
            Text(
                choice.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = if (choice.tag != null) 10.dp else 0.dp),
            )
        }
    }
}

@Composable
private fun OrderableRow(
    position: Int,
    choice: AnswerChoice,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "$position",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                choice.text,
                style = com.codingarena.core.design.CodeTextStyle,
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
            )
            TextButton(onClick = onMoveUp, enabled = canMoveUp) { Text("Up") }
            TextButton(onClick = onMoveDown, enabled = canMoveDown) { Text("Down") }
        }
    }
}
