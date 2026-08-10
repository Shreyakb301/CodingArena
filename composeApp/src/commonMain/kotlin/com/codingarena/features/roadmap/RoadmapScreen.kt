package com.codingarena.features.roadmap

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import com.codingarena.content.NeetCode150
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ProgressBar
import com.codingarena.core.design.SectionHeader
import com.codingarena.core.design.StatTile
import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.engine.BlitzMode
import com.codingarena.domain.engine.Confusion
import com.codingarena.domain.engine.storageKey
import com.codingarena.domain.model.Curriculum
import com.codingarena.domain.model.CurriculumProblem
import com.codingarena.domain.model.CurriculumProgress
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.RecallRecord
import com.codingarena.domain.model.RecallStrength
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class RoadmapUiState(
    val loading: Boolean = true,
    val showBlind75: Boolean = false,
    val progress: CurriculumProgress? = null,
    val records: Map<String, RecallRecord> = emptyMap(),
    val bestScore: Int = 0,
    val confusions: List<Confusion> = emptyList(),
    val expanded: PatternGroup? = null,
) {
    val curriculum: Curriculum
        get() = if (showBlind75) NeetCode150.blind75 else NeetCode150.curriculum
}

class RoadmapViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val engine: BlitzEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(RoadmapUiState())
    val state: StateFlow<RoadmapUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val records = curriculumRepo.records(userId)
            _state.value = _state.value.copy(
                loading = false,
                records = records,
                progress = engine.progress(_state.value.curriculum, records, time.nowMillis()),
                bestScore = curriculumRepo.bestScore(userId),
            )
        }
    }

    fun toggleList(blind75: Boolean) {
        _state.value = _state.value.copy(showBlind75 = blind75)
        refresh()
    }

    /**
     * Records that the problem was actually solved on LeetCode.
     *
     * Separate from recall: knowing it is a Sliding Window problem and having
     * written the solution are different milestones.
     */
    fun toggleSolved(slug: String) {
        viewModelScope.launch {
            val userId = currentUser.userId ?: return@launch
            val existing = curriculumRepo.record(userId, slug)
            curriculumRepo.save(
                userId,
                engine.markSolved(existing, slug, solved = existing?.solved != true, now = time.nowMillis()),
            )
            refresh()
        }
    }

    fun toggleSection(group: PatternGroup) {
        _state.value = _state.value.copy(
            expanded = if (_state.value.expanded == group) null else group,
        )
    }
}

/**
 * The NeetCode 150 / Blind 75 roadmap.
 *
 * Progress here is deliberately separate from your rating: rating measures
 * skill, this measures coverage of a specific list. Drilling a card you already
 * know barely moves your rating, but it should still visibly advance the map.
 */
@Composable
fun RoadmapScreen(
    onStartBlitz: (String) -> Unit,
    onOpenProblem: (String) -> Unit,
    viewModel: RoadmapViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val progress = state.progress
    val curriculum = state.curriculum

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(Modifier.padding(top = 16.dp)) {
                Text(curriculum.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    curriculum.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !state.showBlind75,
                    onClick = { viewModel.toggleList(false) },
                    label = { Text("NeetCode 150") },
                )
                FilterChip(
                    selected = state.showBlind75,
                    onClick = { viewModel.toggleList(true) },
                    label = { Text("Blind 75") },
                )
            }
        }

        if (progress != null) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "${progress.mastered} of ${progress.total} locked in",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        ProgressBar(progress.fraction, Modifier.padding(vertical = 8.dp))
                        Text(
                            "${progress.seen} seen at least once. A card locks in after three " +
                                "correct recalls in a row.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Locked in", progress.mastered.toString(), Modifier.weight(1f))
                    StatTile("Solved", progress.solved.toString(), Modifier.weight(1f))
                    StatTile("Due", progress.due.toString(), Modifier.weight(1f))
                    StatTile("Best run", state.bestScore.toString(), Modifier.weight(1f))
                }
            }

            item {
                if (progress.due > 0) {
                    Button(
                        onClick = { onStartBlitz(BlitzMode.DueToday.storageKey) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Review ${progress.due} due card(s)") }
                } else {
                    Button(
                        onClick = { onStartBlitz(BlitzMode.WeakestFirst.storageKey) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Blitz my weak spots") }
                }
            }

            if (progress.weakestSections(3).isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        progress.weakestSections(3).forEach {
                            ArenaChip("${it.group.displayName} ${it.mastered}/${it.total}")
                        }
                    }
                }
            }
        }

        item { SectionHeader("Pattern groups", trailing = "tap to expand") }

        items(curriculum.sections) { section ->
            val sectionProgress = progress?.sectionProgress?.get(section.group)
            val isExpanded = state.expanded == section.group

            Card(
                Modifier.fillMaxWidth().clickable { viewModel.toggleSection(section.group) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            section.group.displayName,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "${sectionProgress?.mastered ?: 0}/${section.problems.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    ProgressBar(sectionProgress?.fraction ?: 0f, Modifier.padding(top = 8.dp))

                    if (isExpanded) {
                        section.problems.forEach { problem ->
                            ProblemRow(
                                problem = problem,
                                record = state.records[problem.slug],
                                onClick = { onOpenProblem(problem.slug) },
                                onToggleSolved = { viewModel.toggleSolved(problem.slug) },
                            )
                        }
                        Button(
                            onClick = {
                                onStartBlitz(BlitzMode.Section(section.group).storageKey)
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        ) { Text("Blitz ${section.group.displayName}") }
                    }
                }
            }
        }

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}

@Composable
private fun ProblemRow(
    problem: CurriculumProblem,
    record: RecallRecord?,
    onClick: () -> Unit,
    onToggleSolved: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tapping the checkbox marks it solved; tapping the row opens the
        // pattern lesson - two different intents, so two hit targets.
        Checkbox(
            checked = record?.solved == true,
            onCheckedChange = { onToggleSolved() },
        )
        Column(Modifier.weight(1f).clickable(onClick = onClick)) {
            Text(problem.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                problem.ask,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (problem.inBlind75) ArenaChip("B75")
            ArenaChip(
                (record?.strength ?: RecallStrength.UNSEEN).displayName,
                filled = record?.isMastered == true,
            )
        }
    }
}
