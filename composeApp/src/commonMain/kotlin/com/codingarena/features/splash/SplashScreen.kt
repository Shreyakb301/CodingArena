package com.codingarena.features.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.domain.usecase.StartAppUseCase
import com.codingarena.domain.usecase.StartDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

sealed interface SplashState {
    data object Loading : SplashState
    data object NeedsOnboarding : SplashState
    data object Ready : SplashState
    data class Failed(val message: String) : SplashState
}

class SplashViewModel(
    private val startApp: StartAppUseCase,
    private val currentUser: CurrentUser,
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = SplashState.Loading
        viewModelScope.launch {
            runCatching { startApp() }
                .onSuccess { destination ->
                    _state.value = when (destination) {
                        StartDestination.Onboarding -> SplashState.NeedsOnboarding
                        is StartDestination.Home -> {
                            currentUser.set(destination.profile)
                            SplashState.Ready
                        }
                    }
                }
                .onFailure {
                    _state.value = SplashState.Failed(
                        it.message ?: "Could not open your local data."
                    )
                }
        }
    }
}

@Composable
fun SplashScreen(
    onOnboardingNeeded: () -> Unit,
    onReady: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            SplashState.NeedsOnboarding -> onOnboardingNeeded()
            SplashState.Ready -> onReady()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("CodingArena", style = MaterialTheme.typography.displaySmall)
        Text(
            "Measure your progress, not your problem count.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(Modifier.height(48.dp)) {}
        when (val current = state) {
            is SplashState.Failed -> Text(
                current.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            else -> CircularProgressIndicator()
        }
    }
}
