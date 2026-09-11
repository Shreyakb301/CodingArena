package com.codingarena.features.systemdesign

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codingarena.content.SystemDesignContent
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.SectionHeader
import com.codingarena.core.design.reviewColors
import com.codingarena.domain.model.SystemDesignChoice
import com.codingarena.domain.model.SystemDesignConcept
import com.codingarena.domain.model.SystemDesignQuestion

/** One fundamental: a short explanation, then a couple of questions to check it landed. */
@Composable
fun SystemDesignConceptScreen(conceptId: String, onBack: () -> Unit) {
    val concept = remember(conceptId) { SystemDesignContent.byId(conceptId) }
    if (concept == null) {
        onBack()
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                TextButton(onClick = onBack) { Text("‹ System Design") }
            }
        }
        item {
            ArenaChip(concept.category.displayName, modifier = Modifier.padding(bottom = 10.dp))
        }
        item {
            Text(
                concept.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
        item {
            Text(
                concept.summary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 18.dp),
            )
        }
        item { SectionHeader("Key points") }
        items(concept.keyPoints) { point ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("•", color = MaterialTheme.colorScheme.primary)
                Text(point, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item { SectionHeader("Check yourself", modifier = Modifier.padding(top = 20.dp)) }
        items(concept.questions) { question ->
            SystemDesignQuestionCard(question, Modifier.padding(bottom = 16.dp))
        }
        item {}
    }
}

@Composable
private fun SystemDesignQuestionCard(question: SystemDesignQuestion, modifier: Modifier = Modifier) {
    var selectedIndex by remember(question.id) { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(question.prompt, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            question.choices.forEachIndexed { index, choice ->
                SystemDesignChoiceRow(
                    choice = choice,
                    isSelected = index == selectedIndex,
                    revealed = selectedIndex != null,
                    onClick = { if (selectedIndex == null) selectedIndex = index },
                )
            }
        }
    }
}

@Composable
private fun SystemDesignChoiceRow(
    choice: SystemDesignChoice,
    isSelected: Boolean,
    revealed: Boolean,
    onClick: () -> Unit,
) {
    val colors = reviewColors()
    val borderColor = when {
        isSelected && choice.correct -> colors.bestMove
        isSelected -> colors.blunder
        revealed && choice.correct -> colors.bestMove
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.5f else 0.25f))
            .clickable(enabled = !revealed, onClick = onClick)
            .padding(1.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(13.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isSelected || (revealed && choice.correct)) {
                Icon(
                    if (choice.correct) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(choice.text, style = MaterialTheme.typography.bodyMedium)
        }
        if (isSelected) {
            Text(
                choice.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
