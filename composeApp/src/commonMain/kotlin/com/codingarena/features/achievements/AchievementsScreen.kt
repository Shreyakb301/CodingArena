package com.codingarena.features.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.codingarena.core.design.ProgressBar
import com.codingarena.core.design.SectionHeader
import com.codingarena.domain.engine.AchievementContext
import com.codingarena.domain.engine.AchievementEngine
import com.codingarena.domain.model.AchievementProgress
import com.codingarena.domain.model.PlayerStats
import com.codingarena.domain.repository.AchievementRepository
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.repository.CodeRushRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.StreakRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class AchievementsViewModel(
    private val achievements: AchievementRepository,
    private val ratings: RatingRepository,
    private val streaks: StreakRepository,
    private val attempts: AttemptRepository,
    private val codeRush: CodeRushRepository,
    private val engine: AchievementEngine,
    private val currentUser: CurrentUser,
) : ViewModel() {

    private val _progress = MutableStateFlow<List<AchievementProgress>>(emptyList())
    val progress: StateFlow<List<AchievementProgress>> = _progress.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val profile = currentUser.ensureLoaded() ?: return@launch
            val userId = profile.id
            val recent = attempts.recent(userId, limit = 500)
            val completed = recent.filter { it.completedAt != null }

            _progress.value = engine.evaluate(
                context = AchievementContext(
                    stats = PlayerStats(
                        totalCompleted = completed.size,
                        totalCorrect = completed.count { it.wasCorrect },
                        totalHintsUsed = completed.sumOf { it.hintsUsed },
                    ),
                    ratings = ratings.load(userId),
                    streak = streaks.load(userId),
                    startingRating = profile.startingRating,
                    hintFreeStreak = recent.takeWhile { it.wasCorrect && it.hintsUsed == 0 }.size,
                    bestCodeRushScore = codeRush.bestScore(userId),
                ),
                alreadyUnlocked = achievements.unlocked(userId),
            )
        }
    }
}

@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel = koinViewModel()) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val unlocked = progress.filter { it.isUnlocked }
    val locked = progress.filterNot { it.isUnlocked }.sortedByDescending { it.fraction }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(Modifier.padding(top = 16.dp)) {
                Text("Achievements", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${unlocked.size} of ${progress.size} unlocked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (unlocked.isNotEmpty()) {
            item { SectionHeader("Unlocked") }
            items(unlocked) { AchievementCard(it) }
        }

        item { SectionHeader("In progress") }
        items(locked) { AchievementCard(it) }

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}

@Composable
private fun AchievementCard(progress: AchievementProgress) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (progress.isUnlocked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${progress.achievement.emoji}  ${progress.achievement.title}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    progress.achievement.tier.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                progress.achievement.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!progress.isUnlocked) {
                ProgressBar(progress.fraction, Modifier.padding(top = 10.dp))
                Text(
                    "${progress.current} / ${progress.target}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
