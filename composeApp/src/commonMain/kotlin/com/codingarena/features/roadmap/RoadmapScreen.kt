package com.codingarena.features.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.NeetCode150
import com.codingarena.content.RoadmapLessons
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ProgressBar
import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.engine.BlitzMode
import com.codingarena.domain.engine.Confusion
import com.codingarena.domain.engine.StreakEngine
import com.codingarena.domain.engine.storageKey
import com.codingarena.domain.model.Curriculum
import com.codingarena.domain.model.CurriculumProblem
import com.codingarena.domain.model.CurriculumProgress
import com.codingarena.domain.model.CurriculumSection
import com.codingarena.domain.model.PatternGroup
import com.codingarena.domain.model.RecallRecord
import com.codingarena.domain.model.RecallStrength
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.repository.StreakRepository
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
    val currentStreak: Int = 0,
) {
    val curriculum: Curriculum
        get() = if (showBlind75) NeetCode150.blind75 else NeetCode150.curriculum
}

class RoadmapViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val engine: BlitzEngine,
    private val currentUser: CurrentUser,
    private val time: TimeProvider,
    private val streaks: StreakRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RoadmapUiState())
    val state: StateFlow<RoadmapUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val records = curriculumRepo.records(userId)
            val streak = StreakEngine().refresh(streaks.load(userId), time.epochDay())
            _state.value = _state.value.copy(
                loading = false,
                records = records,
                progress = engine.progress(_state.value.curriculum, records, time.nowMillis()),
                bestScore = curriculumRepo.bestScore(userId),
                currentStreak = streak.currentStreak,
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
 * The CodingArena 150 / Essential Shortlist roadmap.
 *
 * Progress here is deliberately separate from your rating: rating measures
 * skill, this measures coverage of a specific list. Drilling a card you already
 * know barely moves your rating, but it should still visibly advance the map.
 */
@Composable
fun RoadmapScreen(
    onStartBlitz: (String) -> Unit,
    onOpenProblem: (String) -> Unit,
    onBrowsePatterns: () -> Unit,
    viewModel: RoadmapViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    val progress = state.progress
    val curriculum = state.curriculum
    val currentGroup = curriculum.sections.firstOrNull { section ->
        (progress?.sectionProgress?.get(section.group)?.fraction ?: 0f) < 1f
    }?.group

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Roadmap", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBrowsePatterns) { Text("Browse Patterns") }
                if (state.currentStreak > 0) ArenaChip("🔥 ${state.currentStreak}d")
            }
        }

        if (progress != null) {
            if (progress.due > 0) {
                Button(
                    onClick = { onStartBlitz(BlitzMode.DueToday.storageKey) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text("Review ${progress.due} due card(s)") }
            } else {
                Button(
                    onClick = { onStartBlitz(BlitzMode.WeakestFirst.storageKey) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text("Blitz my weak spots") }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(curriculum.sections) { index, section ->
                val sectionProgress = progress?.sectionProgress?.get(section.group)
                val isExpanded = state.expanded == section.group

                PathSectionNode(
                    number = index + 1,
                    section = section,
                    mastered = sectionProgress?.mastered ?: 0,
                    fraction = sectionProgress?.fraction ?: 0f,
                    isCurrent = section.group == currentGroup,
                    isExpanded = isExpanded,
                    records = state.records,
                    onToggle = { viewModel.toggleSection(section.group) },
                    onOpenProblem = onOpenProblem,
                    onToggleSolved = viewModel::toggleSolved,
                    onStartBlitz = {
                        onStartBlitz(BlitzMode.Section(section.group).storageKey)
                    },
                    showConnector = index < curriculum.sections.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun PathSectionNode(
    number: Int,
    section: CurriculumSection,
    mastered: Int,
    fraction: Float,
    isCurrent: Boolean,
    isExpanded: Boolean,
    records: Map<String, RecallRecord>,
    onToggle: () -> Unit,
    onOpenProblem: (String) -> Unit,
    onToggleSolved: (String) -> Unit,
    onStartBlitz: () -> Unit,
    showConnector: Boolean,
) {
    val complete = fraction >= 1f
    val nodeColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        complete -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val nodeContentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimary
        complete -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val nodeSize = if (isCurrent) 76.dp else 64.dp
    val placeLabelLeft = number % 2 == 0

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (placeLabelLeft) {
                PathNodeLabel(
                    section = section,
                    mastered = mastered,
                    fraction = fraction,
                    isCurrent = isCurrent,
                    isExpanded = isExpanded,
                    alignEnd = true,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.size(nodeSize)
                    .clip(CircleShape)
                    .background(nodeColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when {
                        isCurrent -> "GO"
                        complete -> "✓"
                        else -> number.toString()
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = nodeContentColor,
                )
            }
            Spacer(Modifier.width(12.dp))
            if (!placeLabelLeft) {
                PathNodeLabel(
                    section = section,
                    mastered = mastered,
                    fraction = fraction,
                    isCurrent = isCurrent,
                    isExpanded = isExpanded,
                    alignEnd = false,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
        }

        if (isExpanded) {
            Column(
                Modifier.fillMaxWidth().padding(start = 76.dp, bottom = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                section.problems.forEach { problem ->
                    ProblemRow(
                        problem = problem,
                        record = records[problem.slug],
                        onClick = { onOpenProblem(problem.slug) },
                        onToggleSolved = { onToggleSolved(problem.slug) },
                    )
                }
                Button(onClick = onStartBlitz, modifier = Modifier.fillMaxWidth()) {
                    Text("Practice this pattern")
                }
            }
        }

        if (showConnector) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Box(
                    Modifier.width(3.dp).height(20.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }
    }
}

@Composable
private fun PathNodeLabel(
    section: CurriculumSection,
    mastered: Int,
    fraction: Float,
    isCurrent: Boolean,
    isExpanded: Boolean,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        if (isCurrent) {
            Text(
                "NEXT UP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            section.group.displayName,
            style = MaterialTheme.typography.titleMedium,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        )
        Text(
            "$mastered/${section.problems.size}  ${if (isExpanded) "−" else "+"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ProgressBar(fraction, Modifier.padding(top = 6.dp))
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
            if (RoadmapLessons.hasLesson(problem.slug)) {
                Text(
                    "Guided lesson",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ArenaChip(
                (record?.strength ?: RecallStrength.UNSEEN).displayName,
                filled = record?.isMastered == true,
            )
        }
    }
}
