package com.codingarena.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ArenaListItem
import com.codingarena.core.design.ProgressBar
import com.codingarena.core.design.SectionHeader
import com.codingarena.core.design.StatTile
import com.codingarena.domain.engine.CodeRushEngine
import com.codingarena.domain.engine.ReadinessEngine
import com.codingarena.domain.engine.SpacedRepetitionEngine
import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.model.CodeRushStats
import com.codingarena.domain.model.InterviewReadiness
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.PlayerStats
import com.codingarena.domain.model.StreakState
import com.codingarena.domain.model.UserProfile
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.repository.CodeRushRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.ReviewRepository
import com.codingarena.domain.repository.StreakRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class ProfileUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val ratings: PlayerRatings = PlayerRatings(),
    val stats: PlayerStats = PlayerStats(),
    val streak: StreakState = StreakState(),
    val rushStats: CodeRushStats = CodeRushStats(),
    val readiness: InterviewReadiness? = null,
    val unlockedAchievements: Int = 0,
)

class ProfileViewModel(
    private val ratings: RatingRepository,
    private val attempts: AttemptRepository,
    private val streaks: StreakRepository,
    private val reviews: ReviewRepository,
    private val codeRush: CodeRushRepository,
    private val achievements: com.codingarena.domain.repository.AchievementRepository,
    private val rushEngine: CodeRushEngine,
    private val readinessEngine: ReadinessEngine,
    private val srsEngine: SpacedRepetitionEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val profile = currentUser.ensureLoaded() ?: return@launch
            val userId = profile.id
            val recent = attempts.recent(userId, limit = 500)
            val completed = recent.filter { it.completedAt != null }
            val durations = completed.mapNotNull { it.solveDurationMs }

            val playerRatings = ratings.load(userId)
            val stats = PlayerStats(
                totalCompleted = completed.size,
                totalCorrect = completed.count { it.wasCorrect },
                totalHintsUsed = completed.sumOf { it.hintsUsed },
                averageSolveMs = if (durations.isEmpty()) 0L else durations.sum() / durations.size,
                problemsSeen = completed.map { it.problemId }.distinct().size,
                upcomingReviews = srsEngine.upcomingCount(reviews.all(userId), time.nowMillis()),
            )

            _state.value = ProfileUiState(
                loading = false,
                profile = profile,
                ratings = playerRatings,
                stats = stats,
                streak = streaks.load(userId),
                rushStats = rushEngine.summarise(codeRush.sessions(userId)),
                readiness = readinessEngine.estimate(
                    playerRatings,
                    stats,
                    profile.onboarding.targetJobLevel,
                ),
                unlockedAchievements = achievements.unlocked(userId).size,
            )
        }
    }
}

@Composable
fun ProfileScreen(
    onOpenAchievements: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRatings: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(Modifier.padding(top = 16.dp)) {
                Text(
                    state.profile?.displayName?.takeIf { it.isNotBlank() } ?: "Guest",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    "Preparing for ${state.profile?.onboarding?.targetJobLevel?.displayName ?: "interviews"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        state.readiness?.let { readiness ->
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
                        ProgressBar(readiness.score / 100f, Modifier.padding(vertical = 8.dp))
                        Text(readiness.rationale, style = MaterialTheme.typography.bodyMedium)
                        if (readiness.limitingTopics.isNotEmpty()) {
                            Row(
                                Modifier.padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                readiness.limitingTopics.forEach { ArenaChip(it.displayName) }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Rating", state.ratings.overall.toString(), Modifier.weight(1f))
                StatTile(
                    "Streak",
                    state.streak.currentStreak.toString(),
                    Modifier.weight(1f),
                    caption = "best ${state.streak.longestStreak}",
                )
                StatTile(
                    "Solved",
                    state.stats.totalCompleted.toString(),
                    Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    "Accuracy",
                    "${(state.stats.accuracy * 100).toInt()}%",
                    Modifier.weight(1f),
                )
                StatTile(
                    "Avg solve",
                    "${state.stats.averageSolveMs / 1000}s",
                    Modifier.weight(1f),
                )
                StatTile(
                    "Rush best",
                    state.rushStats.bestScore.toString(),
                    Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    "Hints used",
                    state.stats.totalHintsUsed.toString(),
                    Modifier.weight(1f),
                )
                StatTile(
                    "Upcoming reviews",
                    state.stats.upcomingReviews.toString(),
                    Modifier.weight(1f),
                )
            }
        }

        item { SectionHeader("Weakest topics") }
        item {
            val weakest = state.ratings.weakestTopics(3)
            if (weakest.isEmpty()) {
                Text(
                    "Practise a few problems to see where your gaps are.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    weakest.forEach { ArenaChip("${it.topic.shortName} ${it.rating}") }
                }
            }
        }

        item {
            ArenaListItem(
                title = "Achievements",
                subtitle = "${state.unlockedAchievements} unlocked",
                onClick = onOpenAchievements,
            )
        }
        item {
            ArenaListItem(
                title = "Ratings and history",
                subtitle = "Topic breakdown and rating chart",
                onClick = onOpenRatings,
            )
        }
        item {
            ArenaListItem(
                title = "Settings",
                subtitle = "Theme, goals, data",
                onClick = onOpenSettings,
            )
        }

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}
