package com.codingarena.features.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ArenaListItem
import com.codingarena.core.design.ProgressBar
import com.codingarena.core.design.RatingDelta
import com.codingarena.core.design.SectionHeader
import com.codingarena.core.design.StatTile
import com.codingarena.domain.model.AttemptSource
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.domain.usecase.GetHomeSnapshotUseCase
import com.codingarena.domain.usecase.HomeSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class HomeUiState(
    val loading: Boolean = true,
    val snapshot: HomeSnapshot? = null,
    val error: String? = null,
)

class HomeViewModel(
    private val getSnapshot: GetHomeSnapshotUseCase,
    private val currentUser: CurrentUser,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id
            if (userId == null) {
                _state.value = HomeUiState(loading = false, error = "No profile found.")
                return@launch
            }
            runCatching { getSnapshot(userId) }
                .onSuccess { _state.value = HomeUiState(loading = false, snapshot = it) }
                .onFailure {
                    _state.value = HomeUiState(loading = false, error = it.message ?: "Failed to load.")
                }
        }
    }
}

@Composable
fun HomeScreen(
    onOpenProblem: (String, AttemptSource) -> Unit,
    onOpenCodeRush: () -> Unit,
    onOpenLearningPath: () -> Unit,
    onOpenRatings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Re-read on every entry: ratings, streaks and review counts all move as a
    // result of work done on other screens.
    LaunchedEffect(Unit) { viewModel.refresh() }

    when {
        state.loading -> Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }

        state.error != null -> Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 12.dp)) {
                Text("Try again")
            }
        }

        else -> state.snapshot?.let { snapshot ->
            HomeContent(snapshot, onOpenProblem, onOpenCodeRush, onOpenLearningPath, onOpenRatings)
        }
    }
}

@Composable
private fun HomeContent(
    snapshot: HomeSnapshot,
    onOpenProblem: (String, AttemptSource) -> Unit,
    onOpenCodeRush: () -> Unit,
    onOpenLearningPath: () -> Unit,
    onOpenRatings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        snapshot.profile?.displayName?.takeIf { it.isNotBlank() }?.let { "Hi, $it" }
                            ?: "Welcome back",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        if (snapshot.streakAtRisk) {
                            "Your ${snapshot.streak.currentStreak}-day streak ends today"
                        } else {
                            "${snapshot.streak.currentStreak}-day streak"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (snapshot.streakAtRisk) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.clickable(onClick = onOpenRatings)) {
                    Text(
                        snapshot.ratings.overall.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    RatingDelta(snapshot.recentRatingChange)
                }
            }
        }

        item {
            DailyPuzzleCard(snapshot) { id -> onOpenProblem(id, AttemptSource.DAILY_PUZZLE) }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = "Due reviews",
                    value = snapshot.dueReviewCount.toString(),
                    caption = "${snapshot.upcomingReviewCount} this week",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Accuracy",
                    value = "${(snapshot.stats.accuracy * 100).toInt()}%",
                    caption = "${snapshot.stats.totalCompleted} solved",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        snapshot.readiness?.let { readiness ->
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Interview readiness", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${readiness.score}/100 - ${readiness.band.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        ProgressBar(
                            readiness.score / 100f,
                            Modifier.padding(vertical = 8.dp),
                        )
                        Text(readiness.rationale, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        snapshot.learningPath?.let { path ->
            item {
                SectionHeader("Your learning path", trailing = "See all")
                Card(
                    Modifier.fillMaxWidth().clickable(onClick = onOpenLearningPath),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(path.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            path.rationale,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ProgressBar(path.fraction, Modifier.padding(top = 10.dp))
                        Text(
                            "${path.completedStepCount} of ${path.steps.size} steps",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = onOpenCodeRush, modifier = Modifier.fillMaxWidth()) {
                Text("Start Code Rush")
            }
        }

        if (snapshot.recommendations.isNotEmpty()) {
            item { SectionHeader("Practise next") }
            items(snapshot.recommendations) { recommendation ->
                ArenaListItem(
                    title = recommendation.title,
                    subtitle = recommendation.reason,
                    trailing = recommendation.difficultyRating.toString(),
                    onClick = { onOpenProblem(recommendation.problemId, AttemptSource.PRACTICE) },
                )
            }
        }

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}

@Composable
private fun DailyPuzzleCard(snapshot: HomeSnapshot, onOpen: (String) -> Unit) {
    val puzzle = snapshot.dailyPuzzle ?: return
    val done = snapshot.dailyPuzzleResult != null

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Daily Puzzle", style = MaterialTheme.typography.labelSmall)
                ArenaChip(puzzle.difficulty.displayName)
            }
            Text(
                puzzle.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                "${puzzle.primaryTopic.displayName} - ${puzzle.challengeType.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (done) {
                val result = snapshot.dailyPuzzleResult!!
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Scored ${result.score(puzzle.difficultyRating, puzzle.estimatedSeconds)}/100",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    RatingDelta(result.ratingChange)
                }
            } else {
                Button(
                    onClick = { onOpen(puzzle.id) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) { Text("Solve today's puzzle") }
            }
        }
    }
}
