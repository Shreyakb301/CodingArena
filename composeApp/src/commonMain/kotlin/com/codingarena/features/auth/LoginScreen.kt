package com.codingarena.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * A full, dedicated sign-in screen - not a button buried in onboarding.
 * Email/password (login or create-account, toggled) plus "Continue with
 * Google". [onDone] fires once a session exists; it does not touch onboarding
 * or local progress, so it works the same whether it is opened from the
 * welcome step or later from Settings.
 */
@Composable
fun LoginScreen(
    onClose: () -> Unit,
    onDone: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) { if (state.done) onDone() }

    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp),
    ) {
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.Start)) {
            Text("Close")
        }

        Column(
            Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val title = if (state.mode == AuthMode.LOG_IN) "Log in" else "Create your account"
            Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Sync your rating, streak and progress across devices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))

            if (state.mode == AuthMode.SIGN_UP) {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = viewModel::setName,
                    label = { Text("Your name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                )
            }
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::setEmail,
                label = { Text("Email") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::setPassword,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )

            state.error?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }

            Button(
                onClick = viewModel::submit,
                enabled = state.canSubmit && !state.busy,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(top = 20.dp),
            ) {
                Text(
                    when {
                        state.busy -> "Please wait…"
                        state.mode == AuthMode.LOG_IN -> "Log in"
                        else -> "Create account"
                    },
                )
            }
            TextButton(
                onClick = { viewModel.setMode(if (state.mode == AuthMode.LOG_IN) AuthMode.SIGN_UP else AuthMode.LOG_IN) },
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Text(
                    if (state.mode == AuthMode.LOG_IN) "New here? Create an account"
                    else "Already have an account? Log in",
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(Modifier.weight(1f))
                Text("or", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(Modifier.weight(1f))
            }

            GoogleSignInButton()
        }
    }
}
