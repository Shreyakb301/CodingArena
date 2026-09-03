package com.codingarena.features.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.core.design.CodeBlock
import com.codingarena.domain.engine.PlacementAnswer
import com.codingarena.domain.engine.PlacementResult
import com.codingarena.domain.engine.PlacementTestEngine
import com.codingarena.domain.model.CodingProblem
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.ExperienceLevel
import com.codingarena.domain.model.OnboardingAnswers
import com.codingarena.domain.model.forLanguage
import com.codingarena.domain.model.ProgrammingLanguage
import com.codingarena.domain.model.SessionLength
import com.codingarena.domain.model.TargetJobLevel
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.usecase.CompleteOnboardingUseCase
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.content.ArenaCourse
import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.engine.CourseProgressEngine
import com.codingarena.domain.model.ChapterProgress
import com.codingarena.domain.repository.CourseProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/** The onboarding questions, in order. */
enum class OnboardingStep(val title: String, val subtitle: String) {
    WELCOME("Welcome to CodingArena", ""),
    EXPERIENCE("How much have you practised?", "This sets your starting rating - it adjusts fast."),
    LANGUAGE("Which language do you think in?", "Code snippets will use it where they can."),
    GOAL("What are you preparing for?", "Sets the target your readiness score is measured against."),
    TOPICS("Which topics do you already know?", "Anything you skip simply starts unrated."),
    COMMITMENT("How often do you want to practise?", "Used for your weekly goal and streak."),
    PLACEMENT("Quick placement test", "Eight short questions. Optional - you can skip it."),
    DONE("You are set up", "Your first learning path is ready."),
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val displayName: String = "",
    val answers: OnboardingAnswers = OnboardingAnswers(),
    /** Questions for the optional placement test, empty until that step. */
    val placementQuestions: List<CodingProblem> = emptyList(),
    val placementIndex: Int = 0,
    val placementAnswers: List<PlacementAnswer> = emptyList(),
    val placementResult: PlacementResult? = null,
    val placementSkipped: Boolean = false,
    val saving: Boolean = false,
    val finished: Boolean = false,
) {
    val currentPlacementQuestion: CodingProblem?
        get() = placementQuestions.getOrNull(placementIndex)

    val placementInProgress: Boolean
        get() = placementQuestions.isNotEmpty() && placementResult == null && !placementSkipped
}

class OnboardingViewModel(
    private val completeOnboarding: CompleteOnboardingUseCase,
    private val currentUser: CurrentUser,
    private val problems: ProblemRepository,
    private val placementEngine: PlacementTestEngine,
    private val courseProgress: CourseProgressRepository,
    private val courseEngine: CourseProgressEngine,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun next() {
        val current = _state.value.step
        val index = OnboardingStep.entries.indexOf(current)
        if (index < OnboardingStep.entries.size - 1) {
            _state.value = _state.value.copy(step = OnboardingStep.entries[index + 1])
        }
    }

    /** Loads the placement questions the first time that step is shown. */
    fun preparePlacement() {
        if (_state.value.placementQuestions.isNotEmpty()) return
        viewModelScope.launch {
            val language = _state.value.answers.preferredLanguage
            _state.value = _state.value.copy(
                placementQuestions = placementEngine.buildTest(problems.all())
                    .map { it.forLanguage(language) },
            )
        }
    }

    fun answerPlacement(choiceId: String) {
        val state = _state.value
        val question = state.currentPlacementQuestion ?: return

        val answers = state.placementAnswers + PlacementAnswer(question, listOf(choiceId))
        val nextIndex = state.placementIndex + 1
        val finished = nextIndex >= state.placementQuestions.size

        _state.value = state.copy(
            placementAnswers = answers,
            placementIndex = nextIndex,
            placementResult = if (finished) {
                placementEngine.estimate(answers, state.answers.experienceLevel.startingRating)
            } else {
                null
            },
        )
    }

    fun skipPlacement() {
        _state.value = _state.value.copy(placementSkipped = true)
    }

    fun back() {
        val index = OnboardingStep.entries.indexOf(_state.value.step)
        if (index > 0) {
            _state.value = _state.value.copy(step = OnboardingStep.entries[index - 1])
        }
    }

    fun setName(name: String) {
        _state.value = _state.value.copy(displayName = name)
    }

    fun setExperience(level: ExperienceLevel) = update { it.copy(experienceLevel = level) }
    fun setLanguage(language: ProgrammingLanguage) = update { it.copy(preferredLanguage = language) }
    fun setJobLevel(level: TargetJobLevel) = update { it.copy(targetJobLevel = level) }
    fun setSessionLength(length: SessionLength) = update { it.copy(sessionLength = length) }
    fun setWeeklyGoal(days: Int) = update { it.copy(weeklyGoalDays = days) }

    fun toggleTopic(topic: CodingTopic) = update {
        it.copy(
            knownTopics = if (topic in it.knownTopics) it.knownTopics - topic
            else it.knownTopics + topic
        )
    }

    fun finish() {
        if (_state.value.saving) return
        _state.value = _state.value.copy(saving = true)
        viewModelScope.launch {
            val profile = completeOnboarding(
                displayName = _state.value.displayName,
                answers = _state.value.answers,
                placement = _state.value.placementResult,
            )
            currentUser.set(profile)
            val estimate = _state.value.placementResult?.estimatedRating
                ?: _state.value.answers.experienceLevel.startingRating
            val target = courseEngine.placementChapter(ArenaCourse.course, estimate)
            ArenaCourse.availableChapters.filter { it.rank < target.rank }.forEach { chapter ->
                courseProgress.save(
                    ChapterProgress(
                        userId = profile.id,
                        chapterId = chapter.id,
                        completedBlockIds = chapter.lessonBlockIds,
                        checkpointScore = 1.0,
                        passedIndependentExerciseIds = chapter.independentExercises.map { it.id }.toSet(),
                        lastPractisedAt = time.nowMillis(),
                    )
                )
            }
            _state.value = _state.value.copy(saving = false, finished = true)
        }
    }

    private fun update(block: (OnboardingAnswers) -> OnboardingAnswers) {
        _state.value = _state.value.copy(answers = block(_state.value.answers))
    }
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

    LaunchedEffect(state.step) {
        if (state.step == OnboardingStep.PLACEMENT) viewModel.preparePlacement()
    }

    // Short steps sit optically centred; the long ones (topic grid, placement
    // test) top-align and scroll.
    val centred = state.step in CENTRED_STEPS
    val steps = OnboardingStep.entries

    Column(
        Modifier.fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        StepSegments(current = steps.indexOf(state.step), total = steps.size)

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (centred) {
                Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            } else {
                Arrangement.spacedBy(12.dp)
            },
            contentPadding = PaddingValues(vertical = 28.dp),
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        state.step.title,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    if (state.step.subtitle.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            state.step.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            when (state.step) {
                OnboardingStep.WELCOME -> item {
                    OutlinedTextField(
                        value = state.displayName,
                        onValueChange = viewModel::setName,
                        label = { Text("What should we call you?") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OnboardingStep.EXPERIENCE -> items(ExperienceLevel.entries) { level ->
                    ChoiceRow(
                        label = level.displayName,
                        selected = state.answers.experienceLevel == level,
                        onClick = { viewModel.setExperience(level) },
                    )
                }

                OnboardingStep.LANGUAGE -> items(ProgrammingLanguage.entries) { language ->
                    ChoiceRow(
                        label = language.displayName,
                        selected = state.answers.preferredLanguage == language,
                        onClick = { viewModel.setLanguage(language) },
                    )
                }

                OnboardingStep.GOAL -> items(TargetJobLevel.entries) { level ->
                    ChoiceRow(
                        label = level.displayName,
                        selected = state.answers.targetJobLevel == level,
                        onClick = { viewModel.setJobLevel(level) },
                    )
                }

                OnboardingStep.TOPICS -> item {
                    TopicGrid(
                        selected = state.answers.knownTopics,
                        onToggle = viewModel::toggleTopic,
                    )
                }

                OnboardingStep.COMMITMENT -> {
                    item { GroupLabel("Session length") }
                    items(SessionLength.entries) { length ->
                        ChoiceRow(
                            label = length.displayName,
                            selected = state.answers.sessionLength == length,
                            onClick = { viewModel.setSessionLength(length) },
                        )
                    }
                    item { GroupLabel("Days per week") }
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        ) {
                            (2..7).forEach { days ->
                                FilterChip(
                                    selected = state.answers.weeklyGoalDays == days,
                                    onClick = { viewModel.setWeeklyGoal(days) },
                                    label = { Text("$days") },
                                )
                            }
                        }
                    }
                }

                OnboardingStep.PLACEMENT -> {
                    val question = state.currentPlacementQuestion
                    // Pulled into a local so it is readable inside the item
                    // lambdas below without depending on a smart cast.
                    val placement = state.placementResult
                    when {
                        state.placementSkipped -> item {
                            Text(
                                "Skipped. We start you from your experience level and let your " +
                                    "first few problems settle the rating.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                            )
                        }

                        placement != null -> item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    placement.estimatedRating.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    placement.summary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        question != null -> {
                            item {
                                Text(
                                    "Question ${state.placementIndex + 1} of " +
                                        "${state.placementQuestions.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            item {
                                Text(
                                    question.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            question.codeSnippet?.let { item { CodeBlock(it) } }
                            item {
                                Text(
                                    question.challengeType.prompt,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            items(question.choices) { choice ->
                                Card(
                                    Modifier.fillMaxWidth()
                                        .clickable { viewModel.answerPlacement(choice.id) },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                    ),
                                ) {
                                    Text(choice.text, Modifier.padding(16.dp))
                                }
                            }
                            item {
                                TextButton(onClick = viewModel::skipPlacement) {
                                    Text("Skip the placement test")
                                }
                            }
                        }

                        else -> item { Text("Preparing questions...") }
                    }
                }

                OnboardingStep.DONE -> item {
                    Text(
                        "Your rating starts as an estimate. It becomes a measurement after a " +
                            "handful of problems - that is the point.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Button(
            onClick = {
                if (state.step == OnboardingStep.DONE) viewModel.finish() else viewModel.next()
            },
            // Blocked mid-placement so the estimate is never built from a
            // half-finished test.
            enabled = !state.saving && !state.placementInProgress,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) {
            Text(if (state.step == OnboardingStep.DONE) "Start practising" else "Continue")
        }

        if (state.step != OnboardingStep.WELCOME) {
            TextButton(
                onClick = viewModel::back,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp),
            ) { Text("Back") }
        }
    }
}

/** The steps whose content is short enough to sit centred rather than scroll. */
private val CENTRED_STEPS = setOf(
    OnboardingStep.WELCOME,
    OnboardingStep.EXPERIENCE,
    OnboardingStep.LANGUAGE,
    OnboardingStep.GOAL,
    OnboardingStep.DONE,
)

@Composable
private fun StepSegments(current: Int, total: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(total) { i ->
            Box(
                Modifier.weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (i <= current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            )
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            label,
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
private fun TopicGrid(
    selected: Set<CodingTopic>,
    onToggle: (CodingTopic) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CodingTopic.subjectTopics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { topic ->
                    FilterChip(
                        selected = topic in selected,
                        onClick = { onToggle(topic) },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(
                                topic.displayName,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                textAlign = TextAlign.Center,
                            )
                        },
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
