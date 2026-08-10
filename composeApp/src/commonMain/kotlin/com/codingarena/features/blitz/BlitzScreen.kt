package com.codingarena.features.blitz

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.NeetCode150
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ProgressBar
import com.codingarena.core.design.reviewColors
import com.codingarena.domain.engine.BlitzCard
import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.engine.BlitzMode
import com.codingarena.domain.engine.BlitzSession
import com.codingarena.domain.engine.Confusion
import com.codingarena.domain.model.CurriculumProblem
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class BlitzUiState(
    val loading: Boolean = true,
    val queue: List<CurriculumProblem> = emptyList(),
    val index: Int = 0,
    val card: BlitzCard? = null,
    val session: BlitzSession? = null,
    val questionStartedAt: Long = 0L,
    /** Set briefly after answering, so the card can flash right or wrong. */
    val lastChoice: PatternGroup? = null,
    val lastWasCorrect: Boolean? = null,
    /** Why that pattern, written for the specific mistake made. */
    val feedback: String? = null,
    val finished: Boolean = false,
    val confusions: List<Confusion> = emptyList(),
) {
    val remaining: Int get() = (queue.size - index).coerceAtLeast(0)
    val progress: Float
        get() = if (queue.isEmpty()) 0f else (index.toFloat() / queue.size).coerceIn(0f, 1f)
}

class BlitzViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val engine: BlitzEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
    private val ids: IdGenerator,
) : ViewModel() {

    private val _state = MutableStateFlow(BlitzUiState())
    val state: StateFlow<BlitzUiState> = _state.asStateFlow()

    fun start(mode: BlitzMode) {
        if (_state.value.session != null) return
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val records = curriculumRepo.records(userId)
            val curriculum = if (mode is BlitzMode.Blind75) {
                NeetCode150.blind75
            } else {
                NeetCode150.curriculum
            }
            val queue = engine.buildQueue(curriculum, records, mode, now = time.nowMillis())

            _state.value = BlitzUiState(
                loading = false,
                queue = queue,
                card = queue.firstOrNull()?.let { engine.cardFor(it) },
                session = engine.start(curriculum.id, mode, time.nowMillis()),
                questionStartedAt = time.nowMillis(),
                finished = queue.isEmpty(),
            )
        }
    }

    fun answer(choice: PatternGroup) {
        val current = _state.value
        val card = current.card ?: return
        val session = current.session ?: return
        if (current.lastChoice != null) return // already answered, waiting to advance

        val now = time.nowMillis()
        val elapsed = now - current.questionStartedAt
        val correct = card.isCorrect(choice)

        _state.value = current.copy(
            session = engine.submit(session, card, choice, elapsed),
            lastChoice = choice,
            lastWasCorrect = correct,
            feedback = card.feedbackFor(choice),
        )

        viewModelScope.launch {
            currentUser.userId?.let { userId ->
                val existing = curriculumRepo.record(userId, card.problem.slug)
                curriculumRepo.save(
                    userId,
                    engine.recordAnswer(existing, card, choice, elapsed, now),
                )
            }
        }
    }

    /** Moves to the next card after the right/wrong flash. */
    fun advance() {
        val current = _state.value
        if (current.lastChoice == null) return
        val nextIndex = current.index + 1

        if (nextIndex >= current.queue.size) {
            finish()
            return
        }

        _state.value = current.copy(
            index = nextIndex,
            card = engine.cardFor(current.queue[nextIndex]),
            questionStartedAt = time.nowMillis(),
            lastChoice = null,
            lastWasCorrect = null,
            feedback = null,
        )
    }

    fun finish() {
        val session = _state.value.session ?: return
        val ended = engine.finish(session, time.nowMillis())
        _state.value = _state.value.copy(
            session = ended,
            finished = true,
            card = null,
            confusions = engine.confusions(listOf(ended)),
        )
        viewModelScope.launch {
            currentUser.userId?.let { curriculumRepo.saveSession(it, ids.newId(), ended) }
        }
    }
}

@Composable
fun BlitzScreen(
    modeKey: String,
    onDone: () -> Unit,
    viewModel: BlitzViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(modeKey) { viewModel.start(BlitzMode.fromKey(modeKey)) }

    // A correct answer flashes by; a wrong one waits for a tap. Auto-advancing
    // past an explanation the user has not read defeats the point of writing it.
    LaunchedEffect(state.lastChoice) {
        if (state.lastChoice != null && state.lastWasCorrect == true) {
            delay(450)
            viewModel.advance()
        }
    }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        state.finished -> BlitzSummary(state, onDone)
        state.card != null -> BlitzQuestion(
            state = state,
            onAnswer = viewModel::answer,
            onContinue = viewModel::advance,
            onQuit = viewModel::finish,
        )
        else -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    }
}

@Composable
private fun BlitzQuestion(
    state: BlitzUiState,
    onAnswer: (PatternGroup) -> Unit,
    onContinue: () -> Unit,
    onQuit: () -> Unit,
) {
    val card = state.card!!
    val session = state.session!!
    val colors = reviewColors()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${state.index + 1} / ${state.queue.size}", style = MaterialTheme.typography.labelLarge)
            Text(
                "${session.score} correct",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (session.streak >= 3) ArenaChip("${session.streak} streak", filled = true)
        }
        ProgressBar(state.progress, Modifier.padding(top = 8.dp))

        Column(Modifier.weight(1f).padding(top = 24.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ArenaChip(card.problem.difficulty.displayName)
                if (card.problem.inBlind75) ArenaChip("Blind 75")
            }
            Text(
                card.problem.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                card.problem.ask,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                "Which pattern?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            )

            card.options.forEach { option ->
                val answered = state.lastChoice != null
                val isCorrectOption = option == card.correct
                val isPicked = option == state.lastChoice

                Card(
                    Modifier.fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !answered) { onAnswer(option) },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            !answered -> MaterialTheme.colorScheme.surface
                            isCorrectOption -> colors.bestMove
                            isPicked -> colors.blunder
                            else -> MaterialTheme.colorScheme.surface
                        },
                    ),
                ) {
                    Text(
                        option.displayName,
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (answered && isCorrectOption) FontWeight.Bold else null,
                    )
                }
            }
        }

        if (state.lastWasCorrect == false && state.feedback != null) {
            Card(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "WHY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        state.feedback,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    ) { Text("Got it") }
                }
            }
        } else {
            TextButton(onClick = onQuit) { Text("End run") }
        }
    }
}

@Composable
private fun BlitzSummary(state: BlitzUiState, onDone: () -> Unit) {
    val session = state.session

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Run complete", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${session?.score ?: 0} / ${session?.answered?.size ?: 0}",
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    "Best streak ${session?.bestStreak ?: 0} · " +
                        "${((session?.accuracy ?: 0.0) * 100).toInt()}% · " +
                        "${(session?.averageMs ?: 0L) / 1000}s average",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.confusions.isNotEmpty()) {
            item {
                Text(
                    "What tripped you up",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            items(state.confusions) { confusion ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Text(confusion.description, Modifier.padding(14.dp))
                }
            }
        }

        item {
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("Back to roadmap")
            }
        }
    }
}
