package com.codingarena.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
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
    onOpenPractice: () -> Unit,
    onOpenLearningPath: () -> Unit,
    onOpenRatings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
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
        else -> state.snapshot?.let {
            HomeContent(it, onOpenPractice, onOpenLearningPath, onOpenRatings)
        }
    }
}

@Composable
private fun HomeContent(
    snapshot: HomeSnapshot,
    onOpenPractice: () -> Unit,
    onOpenLearningPath: () -> Unit,
    onOpenRatings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "{}",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(" CodingArena", style = MaterialTheme.typography.titleMedium)
                }
                Row(
                    modifier = Modifier.clickable(onClick = onOpenRatings),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("🔥 ${snapshot.streak.currentStreak}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("◆ ${snapshot.ratings.overall}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Column(Modifier.padding(top = 46.dp, bottom = 26.dp)) {
                Text(
                    "Ready for one round?",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        item {
            DailyPracticeCard(onOpenPractice)
        }

        item {
            Column(Modifier.padding(top = 26.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                HomeActionRow(
                    mark = "↗",
                    title = "Continue roadmap",
                    subtitle = snapshot.learningPath?.let { path ->
                        "${path.title} · ${(path.fraction * 100).toInt()}%"
                    } ?: "Choose your next pattern",
                    onClick = onOpenLearningPath,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                HomeActionRow(
                    mark = "↻",
                    title = "Review",
                    subtitle = if (snapshot.dueReviewCount == 1) "1 question due" else "${snapshot.dueReviewCount} questions due",
                    onClick = onOpenPractice,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }

        item { Box(Modifier.padding(bottom = 24.dp)) }
    }
}

@Composable
private fun DailyPracticeCard(onOpen: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp)),
    ) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 24.dp)) {
            Text(
                "TODAY · 3 MIN",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
            )
            Text(
                "Daily practice",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                "Three questions picked from what you need to remember.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Start practice  →", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun HomeActionRow(mark: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(11.dp)).padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(mark, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
