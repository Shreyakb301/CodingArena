package com.codingarena.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.codingarena.domain.repository.InterviewProgressRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.ReviewRepository
import com.codingarena.domain.repository.StreakRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

data class ProfileUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val ratings: PlayerRatings = PlayerRatings(),
    val stats: PlayerStats = PlayerStats(),
    val streak: StreakState = StreakState(),
    val rushStats: CodeRushStats = CodeRushStats(),
    val readiness: InterviewReadiness? = null,
    val interviewQuestionsSolved: Int = 0,
    val unlockedAchievements: Int = 0,
)

class ProfileViewModel(
    private val ratings: RatingRepository,
    private val attempts: AttemptRepository,
    private val streaks: StreakRepository,
    private val reviews: ReviewRepository,
    private val codeRush: CodeRushRepository,
    private val interviewProgress: InterviewProgressRepository,
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
                interviewQuestionsSolved = interviewProgress.load(userId)?.totalCompleted ?: 0,
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
    onOpenPatterns: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val profile = state.profile
    val name = profile?.displayName?.takeIf { it.isNotBlank() } ?: "Guest"
    val initials = name.split(' ').filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "G" }
    val joined = profile?.createdAt?.takeIf { it > 0L }?.let {
        val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
        "Joined ${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Profile", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth().padding(top = 38.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(72.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Medium)
                }
                Text(name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 14.dp))
                joined?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Row(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
                ProfileNumber(state.ratings.overall.toString(), "Rating", Modifier.weight(1f))
                ProfileNumber("${state.streak.currentStreak} days", "Streak", Modifier.weight(1f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
        item { ProfileCountRow("Questions solved", state.stats.totalCompleted) }
        item { ProfileCountRow("Interview questions solved", state.interviewQuestionsSolved) }
        item { Box(Modifier.padding(bottom = 24.dp)) }
    }
}

@Composable
private fun ProfileNumber(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileCountRow(label: String, value: Int) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}
