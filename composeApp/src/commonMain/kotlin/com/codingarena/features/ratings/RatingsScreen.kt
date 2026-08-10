package com.codingarena.features.ratings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.EmptyState
import com.codingarena.core.design.ProgressBar
import com.codingarena.core.design.SectionHeader
import com.codingarena.domain.model.CodingTopic
import com.codingarena.domain.model.PlayerRatings
import com.codingarena.domain.model.RatingHistoryEntry
import com.codingarena.domain.model.UserTopicRating
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class RatingsUiState(
    val loading: Boolean = true,
    val ratings: PlayerRatings = PlayerRatings(),
    val history: List<RatingHistoryEntry> = emptyList(),
)

class RatingsViewModel(
    private val ratings: RatingRepository,
    private val currentUser: CurrentUser,
) : ViewModel() {

    private val _state = MutableStateFlow(RatingsUiState())
    val state: StateFlow<RatingsUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            _state.value = RatingsUiState(
                loading = false,
                ratings = ratings.load(userId),
                history = ratings.overallHistory(userId),
            )
        }
    }
}

@Composable
fun RatingsScreen(
    onOpenTopicPractice: (String) -> Unit,
    viewModel: RatingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val practised = state.ratings.topics.values
        .filter { it.attempts > 0 }
        .sortedByDescending { it.rating }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(Modifier.padding(top = 16.dp)) {
                Text("Overall rating", style = MaterialTheme.typography.labelSmall)
                Text(
                    state.ratings.overall.toString(),
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }

        item {
            if (state.history.size >= 2) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Rating history", style = MaterialTheme.typography.labelSmall)
                        RatingSparkline(
                            values = state.history.map { it.rating },
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().height(90.dp).padding(top = 8.dp),
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                state.history.first().rating.toString(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Text(
                                state.history.last().rating.toString(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }

        if (practised.isEmpty()) {
            item {
                EmptyState("Solve a few problems and your topic ratings will appear here.")
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    practised.lastOrNull()?.let {
                        ArenaChip("Weakest: ${it.topic.displayName}")
                    }
                    practised.firstOrNull()?.let {
                        ArenaChip("Strongest: ${it.topic.displayName}", filled = true)
                    }
                }
            }

            item { SectionHeader("Topic ratings") }
            items(practised) { rating ->
                TopicRatingRow(rating, state.ratings.overall)
            }
        }

        if (state.ratings.modes.isNotEmpty()) {
            item { SectionHeader("Practice modes") }
            items(state.ratings.modes.values.toList()) { mode ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(mode.mode.displayName, style = MaterialTheme.typography.bodyLarge)
                    Text(mode.rating.toString(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}

@Composable
private fun TopicRatingRow(rating: UserTopicRating, overall: Int) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(rating.topic.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${rating.attempts} attempts - ${(rating.accuracy * 100).toInt()}% accuracy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(rating.rating.toString(), style = MaterialTheme.typography.titleMedium)
                    if (rating.isProvisional) {
                        Text(
                            "provisional",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            // Bar is drawn relative to the overall rating, so the picture is
            // "where is this topic against the rest of me", not an absolute scale.
            ProgressBar(
                fraction = (rating.rating.toFloat() / (overall * 1.5f)).coerceIn(0f, 1f),
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

/**
 * A minimal line chart.
 *
 * Hand-drawn rather than pulled from a charting library: the spec asks for
 * simple, readable mobile charts, and this keeps the dependency list (and the
 * iOS binary) smaller.
 */
@Composable
private fun RatingSparkline(
    values: List<Int>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (values.size < 2) return
    val min = values.min()
    val max = values.max()
    val span = (max - min).coerceAtLeast(1)

    Canvas(modifier) {
        val stepX = size.width / (values.size - 1)
        var previous = Offset(
            x = 0f,
            y = size.height - ((values[0] - min).toFloat() / span) * size.height,
        )
        values.forEachIndexed { index, value ->
            if (index == 0) return@forEachIndexed
            val point = Offset(
                x = stepX * index,
                y = size.height - ((value - min).toFloat() / span) * size.height,
            )
            drawLine(color = color, start = previous, end = point, strokeWidth = 3f)
            previous = point
        }
    }
}
