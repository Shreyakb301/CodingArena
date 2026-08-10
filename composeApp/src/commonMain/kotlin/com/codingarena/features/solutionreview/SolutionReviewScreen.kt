package com.codingarena.features.solutionreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.codingarena.core.design.ArenaChip
import com.codingarena.core.design.ArenaListItem
import com.codingarena.core.design.RatingDelta
import com.codingarena.core.design.ReviewLabelBadge
import com.codingarena.core.design.SectionHeader
import com.codingarena.domain.model.SolutionReview
import com.codingarena.features.challenge.PracticeResultStore
import org.koin.compose.koinInject

/**
 * The flagship screen (spec 5.5).
 *
 * Ordered the way a chess post-mortem reads: the verdict first, then what you
 * did right, then what went wrong and why it matters, then the best line - and
 * only at the end, what to practise next.
 */
@Composable
fun SolutionReviewScreen(
    attemptId: String,
    onPractiseNext: (String) -> Unit,
    onDone: () -> Unit,
    resultStore: PracticeResultStore = koinInject(),
) {
    val entry = resultStore.get(attemptId)

    if (entry == null) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("That review is no longer available.")
            Text(
                "Your answer was still recorded.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onDone, modifier = Modifier.padding(top = 12.dp)) { Text("Back home") }
        }
        return
    }

    val review = entry.result.review

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(Modifier.padding(top = 16.dp)) {
                    ReviewLabelBadge(review.label)
                    Text(
                        review.headline,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            item {
                review.ratingUpdate?.let { update ->
                    Card(
                        Modifier.fillMaxWidth(),
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
                                Text("Rating", style = MaterialTheme.typography.labelSmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        update.overallAfter.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Box(Modifier.padding(start = 8.dp)) {
                                        RatingDelta(update.overallChange)
                                    }
                                }
                            }
                            if (update.topicChanges.isNotEmpty()) {
                                Row(
                                    Modifier.padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    update.topicChanges.entries.take(3).forEach { (topic, change) ->
                                        ArenaChip(
                                            "${topic.shortName} ${if (change >= 0) "+" else ""}$change"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            review.goodMove?.let {
                item { ReviewBlock("Good move", it, MaterialTheme.colorScheme.primary) }
            }
            review.mistake?.let {
                item { ReviewBlock("What went wrong", it, MaterialTheme.colorScheme.error) }
            }
            item { ReviewBlock("Why it matters", review.whyItMatters, null) }
            item { ReviewBlock("Best move", review.bestMove, MaterialTheme.colorScheme.primary) }

            if (review.timeComplexity != null || review.spaceComplexity != null) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        review.timeComplexity?.let { ArenaChip("Time $it") }
                        review.spaceComplexity?.let { ArenaChip("Space $it") }
                        review.pattern?.let { ArenaChip(it, filled = true) }
                    }
                }
            }

            if (review.commonMistakes.isNotEmpty()) {
                item { SectionHeader("Common interview mistakes") }
                items(review.commonMistakes) { mistake ->
                    Text("- $mistake", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (entry.result.newAchievements.isNotEmpty()) {
                item { SectionHeader("Unlocked") }
                items(entry.result.newAchievements) { achievement ->
                    ArenaListItem(
                        title = "${achievement.emoji}  ${achievement.title}",
                        subtitle = achievement.description,
                    )
                }
            }

            if (review.recommendedPractice.isNotEmpty()) {
                item { SectionHeader("Practise this next") }
                items(review.recommendedPractice) { recommendation ->
                    ArenaListItem(
                        title = recommendation.title,
                        subtitle = recommendation.reason,
                        trailing = recommendation.difficultyRating.toString(),
                        onClick = { onPractiseNext(recommendation.problemId) },
                    )
                }
            }

            item {
                Text(
                    reviewScheduleLine(review),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) { Text("Done") }
    }
}

@Composable
private fun ReviewBlock(title: String, body: String, accent: Color?) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accent ?: MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                body,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private fun reviewScheduleLine(review: SolutionReview): String =
    if (review.nextReviewAt == null) {
        "This problem is not scheduled for review."
    } else {
        "Scheduled to come back around as part of your spaced repetition queue."
    }
