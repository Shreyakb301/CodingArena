package com.codingarena.features.interview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codingarena.domain.model.ChoiceRating
import com.codingarena.domain.model.RatedChoice

/** Rating-appropriate accent color, reused across every Interview runner. */
@Composable
internal fun ratingColor(rating: ChoiceRating) = when (rating) {
    ChoiceRating.STRONG -> MaterialTheme.colorScheme.primary
    ChoiceRating.REASONABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    ChoiceRating.WEAK -> MaterialTheme.colorScheme.error
}

@Composable
internal fun ratingContainerColor(rating: ChoiceRating) = when (rating) {
    ChoiceRating.STRONG -> MaterialTheme.colorScheme.primaryContainer
    ChoiceRating.REASONABLE -> MaterialTheme.colorScheme.surfaceVariant
    ChoiceRating.WEAK -> MaterialTheme.colorScheme.errorContainer
}

/**
 * One decision point: an optional scenario, a prompt, and a list of choices.
 * Shared by every Interview exercise type (behavioral, STAR stage, technical
 * communication, mock interview) since they all share the same [RatedChoice]
 * shape.
 *
 * [showFeedback] controls whether picking a choice reveals its rating and
 * feedback immediately (every mode except the mock interview, which withholds
 * feedback until the run is over) or just advances via [onCheck] with no
 * reveal at all (the mock interview's own screen drives that by passing false
 * and its own advance button).
 */
@Composable
internal fun RatedDecisionPoint(
    scenario: String?,
    prompt: String,
    choices: List<RatedChoice>,
    selectedId: String?,
    checked: Boolean,
    showFeedback: Boolean,
    checkLabel: String,
    onSelect: (String) -> Unit,
    onCheck: () -> Unit,
) {
    val selected = choices.firstOrNull { it.id == selectedId }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(Modifier.padding(bottom = 10.dp)) {
                scenario?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))
                }
                Text(prompt, style = MaterialTheme.typography.titleMedium)
            }
        }
        items(choices) { choice ->
            val isSelected = choice.id == selectedId
            Card(
                onClick = { if (!checked) onSelect(choice.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Text(
                    choice.text,
                    Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        if (!checked) {
            item {
                Button(
                    onClick = onCheck,
                    enabled = selectedId != null,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                ) { Text(checkLabel) }
            }
        } else if (showFeedback && selected != null) {
            item { RatingFeedbackCard(selected) }
        }
    }
}

@Composable
internal fun RatingFeedbackCard(choice: RatedChoice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ratingContainerColor(choice.rating)),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(choice.rating.label, fontWeight = FontWeight.Bold, color = ratingColor(choice.rating))
            Text(choice.feedback)
        }
    }
}
