package com.codingarena.features.patternlibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.PatternLibrary
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ArenaListItem
import com.codingarena.core.design.CodeBlock
import com.codingarena.core.design.ProgressBar
import com.codingarena.core.design.SectionHeader
import com.codingarena.domain.model.MasteryLevel
import com.codingarena.domain.model.PatternProgress
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.usecase.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class PatternLibraryViewModel(
    private val attempts: AttemptRepository,
    private val currentUser: CurrentUser,
) : ViewModel() {

    private val _progress = MutableStateFlow<Map<String, PatternProgress>>(emptyMap())
    val progress: StateFlow<Map<String, PatternProgress>> = _progress.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val userId = currentUser.ensureLoaded()?.id ?: return@launch
            val solved = attempts.recent(userId, limit = 500)
                .filter { it.wasCorrect }
                .map { it.problemId }
                .toSet()

            _progress.value = PatternLibrary.patterns.associate { pattern ->
                val required = pattern.allProblemIds
                val done = required.filter { it in solved }.toSet()
                pattern.id to PatternProgress(
                    patternId = pattern.id,
                    solvedProblemIds = done,
                    totalProblems = required.size,
                    level = when {
                        required.isEmpty() || done.isEmpty() -> MasteryLevel.NOT_STARTED
                        done.size == required.size -> MasteryLevel.MASTERED
                        done.size >= required.size / 2 -> MasteryLevel.PRACTISED
                        else -> MasteryLevel.LEARNING
                    },
                )
            }
        }
    }
}

@Composable
fun PatternLibraryScreen(
    onOpenPattern: (String) -> Unit,
    viewModel: PatternLibraryViewModel = koinViewModel(),
) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(Modifier.padding(top = 16.dp)) {
                Text("Pattern Library", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Recognising which pattern a problem wants is most of the interview.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        PatternLibrary.categories.forEach { category ->
            item { SectionHeader(category.displayName) }
            items(PatternLibrary.byCategory(category)) { pattern ->
                val patternProgress = progress[pattern.id]
                Column {
                    ArenaListItem(
                        title = pattern.name,
                        subtitle = pattern.summary,
                        trailing = patternProgress?.level?.displayName,
                        onClick = { onOpenPattern(pattern.id) },
                    )
                    patternProgress?.takeIf { it.totalProblems > 0 }?.let {
                        ProgressBar(it.fraction, Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}

@Composable
fun PatternDetailScreen(
    patternId: String,
    onOpenProblem: (String) -> Unit,
    onBack: () -> Unit,
) {
    val pattern = PatternLibrary.byId(patternId)

    if (pattern == null) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Pattern not found.")
            TextButton(onClick = onBack) { Text("Go back") }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(Modifier.padding(top = 16.dp)) {
                TextButton(onClick = onBack) { Text("Back") }
                Text(pattern.name, style = MaterialTheme.typography.headlineSmall)
                Row(
                    Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ArenaChip(pattern.timeComplexity)
                    ArenaChip(pattern.spaceComplexity)
                }
            }
        }

        item { LessonBlock("What it is", pattern.summary) }
        item { LessonBlock("When to use it", pattern.whenToUse) }

        item { SectionHeader("Recognition signals") }
        items(pattern.recognitionSignals) { signal ->
            Text("- $signal", style = MaterialTheme.typography.bodyLarge)
        }

        item { SectionHeader("Worked example") }
        item { CodeBlock(pattern.visualExample) }

        item { SectionHeader("Template") }
        item { CodeBlock(pattern.codeTemplate) }

        item { SectionHeader("Common mistakes") }
        items(pattern.commonMistakes) { mistake ->
            Text("- $mistake", style = MaterialTheme.typography.bodyLarge)
        }

        practiceSection("Beginner", pattern.beginnerProblemIds, onOpenProblem)
        practiceSection("Intermediate", pattern.intermediateProblemIds, onOpenProblem)
        practiceSection("Mastery", pattern.masteryProblemIds, onOpenProblem)

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.practiceSection(
    title: String,
    problemIds: List<String>,
    onOpenProblem: (String) -> Unit,
) {
    if (problemIds.isEmpty()) return
    item { SectionHeader("$title practice") }
    items(problemIds) { id ->
        val problem = com.codingarena.content.StarterContent.byId(id)
        ArenaListItem(
            title = problem?.title ?: id,
            subtitle = problem?.challengeType?.displayName,
            trailing = problem?.difficultyRating?.toString(),
            onClick = { onOpenProblem(id) },
        )
    }
}

@Composable
private fun LessonBlock(title: String, body: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}
