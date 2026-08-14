package com.codingarena.features.blitz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codingarena.domain.engine.BlitzMode
import com.codingarena.domain.engine.storageKey

@Composable
fun BlitzHubScreen(onStart: (String) -> Unit, onStartStrategy: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("TACTICS TRAINER", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Text("Blitz", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Read the position and choose the best strategy before writing code.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Roadmap tactics", style = MaterialTheme.typography.titleMedium)
                Text("Only strategies from chapters you have unlocked, with due reviews first.")
                Button(onClick = onStartStrategy, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Text("Start strategy round")
                }
            }
        }
        BlitzMode.standard.forEach { mode ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(mode.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (mode) {
                            BlitzMode.DueToday -> "Scheduled patterns that need to be recalled again."
                            BlitzMode.WeakestFirst -> "Confusions, slow answers, and shaky patterns first."
                            BlitzMode.FullList -> "A mixed run across the complete strategy board."
                            BlitzMode.Blind75 -> "A compact set of essential interview patterns."
                            is BlitzMode.Section -> "Train one pattern family."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = { onStart(mode.storageKey) },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    ) { Text("Start round") }
                }
            }
        }
    }
}
