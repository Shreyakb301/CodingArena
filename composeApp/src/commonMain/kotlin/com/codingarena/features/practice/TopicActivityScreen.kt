package com.codingarena.features.practice

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codingarena.content.NeetCode150
import com.codingarena.content.ProblemWorkouts
import com.codingarena.content.RoadmapLessons
import com.codingarena.domain.model.PatternGroup

/** The ways to practice one topic. */
@Composable
fun TopicActivityScreen(
    group: PatternGroup,
    onBack: () -> Unit,
    onCompleteWorkout: (PatternGroup) -> Unit,
    onLearnTheConcept: (slug: String?, patternId: String?) -> Unit,
    onTimedPractice: () -> Unit,
) {
    val firstProblemSlug = NeetCode150.curriculum.problems.firstOrNull { it.group == group }?.slug
    val hasLesson = firstProblemSlug != null && RoadmapLessons.hasLesson(firstProblemSlug)
    val workoutsForTopic = ProblemWorkouts.byGroup(group)
    val hasWorkouts = workoutsForTopic.isNotEmpty()

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← Topics") }
        Text(
            group.displayName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item {
                ActivityRow(
                    mark = "◈",
                    title = "Complete Workout",
                    subtitle = if (hasWorkouts) "Pattern, approach, and code reasoning in one run" else "Content coming soon for this topic",
                    enabled = hasWorkouts,
                    onClick = { onCompleteWorkout(group) },
                )
            }
            item {
                ActivityRow(
                    mark = "◎",
                    title = "Learn the Concept",
                    subtitle = if (hasLesson) "Guided walkthrough of the pattern" else "Pattern overview and recognition signals",
                    enabled = true,
                    onClick = { onLearnTheConcept(firstProblemSlug.takeIf { hasLesson }, group.patternId) },
                )
            }
            item {
                ActivityRow(
                    mark = "CR",
                    title = "Timed Practice",
                    subtitle = "Code Rush, under the clock",
                    enabled = true,
                    onClick = onTimedPractice,
                )
            }
        }
    }
}

@Composable
private fun ActivityRow(
    mark: String,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val contentAlpha = if (enabled) 1f else 0.5f
            Box(
                modifier = Modifier.size(52.dp).background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = contentAlpha),
                    RoundedCornerShape(16.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    mark,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = contentAlpha),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
