package com.codingarena.features.coderush

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.CodeBlock
import com.codingarena.core.design.ProgressBar
import com.codingarena.domain.engine.CodeRushEngine
import com.codingarena.domain.engine.StreakEngine
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.model.CodeRushMode
import com.codingarena.domain.model.CodeRushSession
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.forLanguage
import com.codingarena.domain.model.RushEndReason
import com.codingarena.domain.model.StreakActivity
import com.codingarena.domain.repository.CodeRushRepository
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.repository.StreakRepository
import com.codingarena.domain.usecase.AnswerSubmission
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/** Mode keys used in the navigation route. */
object CodeRushModes {
    const val THREE = "three"
    const val FIVE = "five"
    const val SURVIVAL = "survival"

    fun parse(key: String): CodeRushMode = when (key) {
        THREE -> CodeRushMode.ThreeMinute
        SURVIVAL -> CodeRushMode.Survival
        else -> CodeRushMode.FiveMinute
    }

    fun key(mode: CodeRushMode): String = when (mode) {
        CodeRushMode.ThreeMinute -> THREE
        CodeRushMode.Survival -> SURVIVAL
        else -> FIVE
    }
}

data class RushUiState(
    val session: CodeRushSession? = null,
    val problem: CodingProblem? = null,
    val secondsRemaining: Int? = null,
    val questionStartedAt: Long = 0L,
    val lastAnswerCorrect: Boolean? = null,
    val finished: Boolean = false,
)

class CodeRushSessionViewModel(
    private val problems: ProblemRepository,
    private val codeRush: CodeRushRepository,
    private val streaks: StreakRepository,
    private val submitAnswer: SubmitAnswerUseCase,
    private val engine: CodeRushEngine,
    private val streakEngine: StreakEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
    private val ids: IdGenerator,
) : ViewModel() {

    private val _state = MutableStateFlow(RushUiState())
    val state: StateFlow<RushUiState> = _state.asStateFlow()

    private var candidates: List<CodingProblem> = emptyList()

    fun start(mode: CodeRushMode) {
        if (_state.value.session != null) return
        viewModelScope.launch {
            val profile = currentUser.ensureLoaded() ?: return@launch
            val userId = profile.id
            val language = profile.onboarding.preferredLanguage
            candidates = problems.all().map { it.forLanguage(language) }
            val session = engine.start(ids.newId(), userId, mode, time.nowMillis())
            _state.value = RushUiState(
                session = session,
                problem = engine.nextProblem(session, candidates),
                secondsRemaining = engine.secondsRemaining(session, time.nowMillis()),
                questionStartedAt = time.nowMillis(),
            )
        }
    }

    fun answer(choiceId: String) {
        val state = _state.value
        val session = state.session ?: return
        val problem = state.problem ?: return
        if (session.isOver) return

        val now = time.nowMillis()
        val correct = problem.isCorrect(listOf(choiceId))
        val updated = engine.submit(session, problem, correct, now - state.questionStartedAt, now)

        viewModelScope.launch {
            // Rush answers still move ratings, at reduced weight - the engine
            // applies that discount via AttemptSource.CODE_RUSH.
            currentUser.userId?.let { userId ->
                runCatching {
                    submitAnswer(
                        AnswerSubmission(
                            userId = userId,
                            problem = problem,
                            selectedAnswerIds = listOf(choiceId),
                            startedAt = state.questionStartedAt,
                            source = AttemptSource.CODE_RUSH,
                        )
                    )
                }
            }
        }

        if (updated.isOver) {
            finish(updated, updated.endReason ?: RushEndReason.OUT_OF_LIVES)
            return
        }

        val next = engine.nextProblem(updated, candidates)
        if (next == null) {
            finish(updated, RushEndReason.NO_MORE_PROBLEMS)
            return
        }

        _state.value = state.copy(
            session = updated,
            problem = next,
            questionStartedAt = now,
            lastAnswerCorrect = correct,
        )
    }

    fun tick() {
        val session = _state.value.session ?: return
        if (session.isOver) return
        val now = time.nowMillis()
        if (engine.isTimeUp(session, now)) {
            finish(session, RushEndReason.TIME_UP)
        } else {
            _state.value = _state.value.copy(
                secondsRemaining = engine.secondsRemaining(session, now),
            )
        }
    }

    fun abandon() {
        _state.value.session?.let { finish(it, RushEndReason.ABANDONED) }
    }

    private fun finish(session: CodeRushSession, reason: RushEndReason) {
        val ended = engine.finish(session, reason, time.nowMillis())
        _state.value = _state.value.copy(session = ended, problem = null, finished = true)

        viewModelScope.launch {
            codeRush.save(ended)
            // A rush counts as a practice day only once it clears the
            // five-question bar (spec 5.12).
            if (streakEngine.codeRushQualifies(ended.answers.size)) {
                currentUser.userId?.let { userId ->
                    val today = time.epochDay()
                    val refreshed = streakEngine.refresh(streaks.load(userId), today)
                    streaks.save(
                        userId,
                        streakEngine.recordActivity(refreshed, StreakActivity.CODE_RUSH, today).state,
                    )
                }
            }
        }
    }
}

@Composable
fun CodeRushSessionScreen(
    modeKey: String,
    onFinished: () -> Unit,
    viewModel: CodeRushSessionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(modeKey) { viewModel.start(CodeRushModes.parse(modeKey)) }

    LaunchedEffect(state.session?.id) {
        while (state.session != null && !state.finished) {
            viewModel.tick()
            delay(500)
        }
    }

    val session = state.session
    when {
        session == null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        state.finished -> RushSummary(session, onFinished)
        state.problem != null -> RushQuestion(state, viewModel::answer, viewModel::abandon)
        else -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    }
}

@Composable
private fun RushQuestion(
    state: RushUiState,
    onAnswer: (String) -> Unit,
    onAbandon: () -> Unit,
) {
    val problem = state.problem!!
    val session = state.session!!
    val totalSeconds = session.mode.durationSeconds
    val elapsedProgress = totalSeconds?.let { total ->
        val remaining = state.secondsRemaining ?: total
        ((total - remaining).toFloat() / total).coerceIn(0f, 1f)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onAbandon) {
                Icon(Icons.Filled.Close, contentDescription = "Give up")
            }
            if (elapsedProgress != null) {
                ProgressBar(elapsedProgress, Modifier.weight(1f))
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("♥".repeat(session.livesRemaining), style = MaterialTheme.typography.titleMedium)
            Text("${session.score}", style = MaterialTheme.typography.headlineSmall)
            Text(
                state.secondsRemaining?.let { "${it}s" } ?: "Survival",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(problem.description, style = MaterialTheme.typography.titleMedium)
            }
            problem.codeSnippet?.let {
                item { CodeBlock(it, Modifier.padding(vertical = 4.dp)) }
            }
            item {
                Text(
                    problem.challengeType.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            items(problem.choices) { choice ->
                Card(
                    Modifier.fillMaxWidth().clickable { onAnswer(choice.id) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Text(
                        choice.text,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    )
                }
            }
            item { Box(Modifier.padding(bottom = 12.dp)) }
        }
    }
}

@Composable
private fun RushSummary(session: CodeRushSession, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            when (session.endReason) {
                RushEndReason.TIME_UP -> "Time!"
                RushEndReason.OUT_OF_LIVES -> "Out of lives"
                RushEndReason.NO_MORE_PROBLEMS -> "You cleared the set"
                else -> "Run ended"
            },
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(session.score.toString(), style = MaterialTheme.typography.displaySmall)
        Text(
            "${session.answers.size} answered - ${(session.accuracy * 100).toInt()}% accuracy",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (session.answers.size < StreakActivity.CODE_RUSH_QUESTION_THRESHOLD) {
            Text(
                "Answer ${StreakActivity.CODE_RUSH_QUESTION_THRESHOLD} questions for this to " +
                    "count toward your streak.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Button(onClick = onDone, modifier = Modifier.padding(top = 24.dp)) { Text("Done") }
    }
}
