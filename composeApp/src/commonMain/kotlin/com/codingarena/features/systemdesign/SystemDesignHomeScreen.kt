package com.codingarena.features.systemdesign

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.SystemDesignContent
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.design.ArenaIcons
import com.codingarena.domain.model.SystemDesignCategory
import com.codingarena.domain.model.SystemDesignCategory.APIS
import com.codingarena.domain.model.SystemDesignCategory.DATABASES
import com.codingarena.domain.model.SystemDesignCategory.INFRASTRUCTURE
import com.codingarena.domain.model.SystemDesignCategory.SCALABILITY
import com.codingarena.domain.model.SystemDesignCategory.THEORY
import com.codingarena.domain.model.SystemDesignConcept
import com.codingarena.domain.model.SystemDesignQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class SystemDesignHomeUiState(
    val concepts: List<SystemDesignConcept> = SystemDesignContent.concepts,
    val todaysQuestion: SystemDesignQuestion? = null,
)

class SystemDesignViewModel(private val time: TimeProvider) : ViewModel() {
    private val _state = MutableStateFlow(SystemDesignHomeUiState())
    val state: StateFlow<SystemDesignHomeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                todaysQuestion = SystemDesignContent.questionForDay(time.epochDay()),
            )
        }
    }
}

/** Browsable system design fundamentals, reached from the More tab. */
@Composable
fun SystemDesignHomeScreen(
    onBack: () -> Unit,
    onOpenConcept: (String) -> Unit,
    viewModel: SystemDesignViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("‹ Profile") }
            }
        }
        item {
            Text(
                "System Design",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        item {
            Text(
                "Fundamentals in plain terms, with a couple of questions to check they landed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
        state.todaysQuestion?.let { question ->
            item {
                DailyQuestionCard(
                    question = question,
                    onClick = { onOpenConcept(SystemDesignContent.conceptFor(question).id) },
                )
            }
        }
        SystemDesignCategory.entries.forEach { category ->
            val inCategory = state.concepts.filter { it.category == category }
            if (inCategory.isNotEmpty()) {
                item { SectionLabel(category.displayName) }
                items(inCategory) { concept ->
                    ConceptRow(concept = concept, onClick = { onOpenConcept(concept.id) })
                }
            }
        }
        item { Box(Modifier.padding(bottom = 24.dp)) }
    }
}

@Composable
private fun DailyQuestionCard(question: SystemDesignQuestion, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp)),
    ) {
        Column(Modifier.padding(22.dp)) {
            Text(
                "TODAY · 2 MIN",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
            )
            Text(
                question.prompt,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 28.dp, bottom = 7.dp),
    )
}

@Composable
private fun ConceptRow(concept: SystemDesignConcept, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(12.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    iconFor(concept.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    concept.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    if (concept.questions.size == 1) "1 question" else "${concept.questions.size} questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Text(
                "›",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

internal fun iconFor(category: SystemDesignCategory): ImageVector = when (category) {
    INFRASTRUCTURE -> ArenaIcons.Server
    DATABASES -> ArenaIcons.Database
    APIS -> ArenaIcons.Bubble
    SCALABILITY -> ArenaIcons.Flag
    THEORY -> ArenaIcons.Bulb
}
