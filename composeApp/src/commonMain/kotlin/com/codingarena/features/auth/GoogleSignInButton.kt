package com.codingarena.features.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.codingarena.data.remote.ArenaServerConfig
import org.koin.compose.koinInject

/**
 * Starts the server-side Google OAuth flow (`/v1/auth/google/start`), which
 * redirects back to the app with a session token. Shared by [LoginScreen] and
 * the onboarding welcome step.
 */
@Composable
fun GoogleSignInButton(modifier: Modifier = Modifier) {
    val config = koinInject<ArenaServerConfig>()
    val uriHandler = LocalUriHandler.current
    OutlinedButton(
        onClick = { uriHandler.openUri("${config.baseUrl}/v1/auth/google/start") },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text("Continue with Google")
    }
}
