package com.codingarena.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.core.design.SectionHeader
import com.codingarena.data.remote.KtorClassroomGateway
import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.model.ProgrammingLanguage
import com.codingarena.domain.model.LoginRequest
import com.codingarena.domain.model.ProgressSyncPayload
import com.codingarena.domain.model.RegisterRequest
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.repository.CourseProgressRepository
import com.codingarena.domain.repository.ProfileRepository
import com.codingarena.domain.repository.SettingsRepository
import com.codingarena.domain.repository.StreakRepository
import com.codingarena.domain.sync.SyncUseCase
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.core.common.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

object SettingKeys {
    const val THEME = "theme"
    const val NOTIFICATIONS = "notifications_enabled"
    const val SYNC_ON_CELLULAR = "sync_on_cellular"
}

data class SettingsUiState(
    val theme: String = "system",
    val notificationsEnabled: Boolean = true,
    val syncOnCellular: Boolean = true,
    val language: ProgrammingLanguage = ProgrammingLanguage.PYTHON,
    val weeklyGoalDays: Int = 5,
    val pendingUploads: Int = 0,
    val lastSyncedAt: Long? = null,
    val syncing: Boolean = false,
    val syncMessage: String? = null,
    val signedIn: Boolean = false,
    val accountDisplayName: String = "",
    val authName: String = "",
    val authEmail: String = "",
    val authPassword: String = "",
    val authBusy: Boolean = false,
    val authMessage: String? = null,
)

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val profiles: ProfileRepository,
    private val streaks: StreakRepository,
    private val attempts: AttemptRepository,
    private val syncUseCase: SyncUseCase,
    private val currentUser: CurrentUser,
    private val classroomGateway: ClassroomGateway,
    private val courseProgress: CourseProgressRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val profile = currentUser.ensureLoaded()
            val authName = settings.get(KtorClassroomGateway.AUTH_NAME)
            _state.value = _state.value.copy(
                theme = settings.get(SettingKeys.THEME) ?: "system",
                notificationsEnabled = settings.get(SettingKeys.NOTIFICATIONS) != "false",
                syncOnCellular = settings.get(SettingKeys.SYNC_ON_CELLULAR) != "false",
                language = profile?.onboarding?.preferredLanguage ?: ProgrammingLanguage.PYTHON,
                weeklyGoalDays = profile?.id?.let { streaks.load(it).weeklyGoalDays } ?: 5,
                pendingUploads = attempts.unsynced().count { it.userId == profile?.id },
                lastSyncedAt = syncUseCase.lastSyncedAt(),
                signedIn = !authName.isNullOrBlank(),
                accountDisplayName = authName.orEmpty(),
            )
        }
    }

    fun setAuthName(value: String) { _state.value = _state.value.copy(authName = value) }
    fun setAuthEmail(value: String) { _state.value = _state.value.copy(authEmail = value) }
    fun setAuthPassword(value: String) { _state.value = _state.value.copy(authPassword = value) }

    fun createAccount() = authAction {
        classroomGateway.register(
            RegisterRequest(_state.value.authName, _state.value.authEmail, _state.value.authPassword)
        )
        val syncFailed = runCatching { pushLocalProgress() }.isFailure
        if (syncFailed) "Account created - progress backup will retry later."
        else "Account created - your roadmap progress is now backed up."
    }

    fun signIn() = authAction {
        classroomGateway.login(LoginRequest(_state.value.authEmail, _state.value.authPassword))
        val syncFailed = runCatching { restoreProgress() }.isFailure
        if (syncFailed) "Signed in - progress sync will retry later."
        else "Signed in - your roadmap progress has been restored."
    }

    fun signOut() {
        viewModelScope.launch {
            classroomGateway.signOut()
            _state.value = _state.value.copy(
                signedIn = false,
                accountDisplayName = "",
                authName = "",
                authEmail = "",
                authPassword = "",
                authMessage = null,
            )
        }
    }

    /** Pulls the account's saved chapters onto this device, then re-uploads the merged set. */
    fun syncAccountProgress() = authAction {
        restoreProgress()
        "Progress synced."
    }

    private suspend fun restoreProgress() {
        val userId = currentUser.ensureLoaded()?.id ?: return
        val remote = classroomGateway.fetchProgress()
        remote?.chapters?.forEach { chapter ->
            courseProgress.save(chapter.copy(userId = userId), synced = true)
        }
        pushLocalProgress()
    }

    private suspend fun pushLocalProgress() {
        val userId = currentUser.ensureLoaded()?.id ?: return
        val chapters = courseProgress.all(userId).values.toList()
        if (chapters.isEmpty()) return
        classroomGateway.pushProgress(ProgressSyncPayload(chapters, updatedAt = time.nowMillis()))
    }

    private fun authAction(block: suspend () -> String) {
        if (_state.value.authBusy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(authBusy = true, authMessage = null)
            runCatching { block() }
                .onSuccess { message ->
                    val authName = settings.get(KtorClassroomGateway.AUTH_NAME)
                    _state.value = _state.value.copy(
                        signedIn = !authName.isNullOrBlank(),
                        accountDisplayName = authName.orEmpty(),
                        authMessage = message,
                    )
                }
                .onFailure { _state.value = _state.value.copy(authMessage = it.message ?: "Request failed") }
            _state.value = _state.value.copy(authBusy = false)
        }
    }

    /**
     * Runs a sync pass on demand.
     *
     * Until the backend exists this reports "not enabled yet" rather than
     * pretending to succeed - see OfflineOnlyRemoteDataSource.
     */
    fun syncNow() {
        if (_state.value.syncing) return
        _state.value = _state.value.copy(syncing = true, syncMessage = null)
        viewModelScope.launch {
            val userId = currentUser.userId
            if (userId == null) {
                _state.value = _state.value.copy(syncing = false, syncMessage = "No profile yet.")
                return@launch
            }
            val report = syncUseCase(userId)
            _state.value = _state.value.copy(
                syncing = false,
                syncMessage = report.failure?.message
                    ?: "Uploaded ${report.attemptsUploaded}, downloaded ${report.problemsDownloaded}.",
            )
            refresh()
        }
    }

    fun setTheme(theme: String) {
        _state.value = _state.value.copy(theme = theme)
        viewModelScope.launch { settings.put(SettingKeys.THEME, theme) }
    }

    fun setNotifications(enabled: Boolean) {
        _state.value = _state.value.copy(notificationsEnabled = enabled)
        viewModelScope.launch { settings.put(SettingKeys.NOTIFICATIONS, enabled.toString()) }
    }

    fun setSyncOnCellular(enabled: Boolean) {
        _state.value = _state.value.copy(syncOnCellular = enabled)
        viewModelScope.launch { settings.put(SettingKeys.SYNC_ON_CELLULAR, enabled.toString()) }
    }

    fun setLanguage(language: ProgrammingLanguage) {
        _state.value = _state.value.copy(language = language)
        viewModelScope.launch {
            val profile = currentUser.profile.value ?: return@launch
            profiles.save(profile.copy(onboarding = profile.onboarding.copy(preferredLanguage = language)))
            currentUser.refresh()
        }
    }

    fun setWeeklyGoal(days: Int) {
        _state.value = _state.value.copy(weeklyGoalDays = days)
        viewModelScope.launch {
            val userId = currentUser.userId ?: return@launch
            streaks.save(userId, streaks.load(userId).copy(weeklyGoalDays = days))
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(Modifier.padding(top = 16.dp)) {
                TextButton(onClick = onBack) { Text("Back") }
                Text("Settings", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item { SectionHeader("Theme") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system", "light", "dark").forEach { option ->
                    FilterChip(
                        selected = state.theme == option,
                        onClick = { viewModel.setTheme(option) },
                        label = { Text(option.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
        }

        item { SectionHeader("Preferred language") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ProgrammingLanguage.entries.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { language ->
                            FilterChip(
                                selected = state.language == language,
                                onClick = { viewModel.setLanguage(language) },
                                label = { Text(language.displayName) },
                            )
                        }
                    }
                }
            }
        }

        item { SectionHeader("Weekly goal") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (2..7).forEach { days ->
                    FilterChip(
                        selected = state.weeklyGoalDays == days,
                        onClick = { viewModel.setWeeklyGoal(days) },
                        label = { Text("$days") },
                    )
                }
            }
        }

        item { SectionHeader("Account") }
        item {
            if (state.signedIn) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text("Signed in as ${state.accountDisplayName}", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Your Roadmap progress is backed up to your account.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = viewModel::syncAccountProgress, enabled = !state.authBusy) {
                            Text(if (state.authBusy) "Syncing..." else "Sync now")
                        }
                        OutlinedButton(onClick = viewModel::signOut, enabled = !state.authBusy) { Text("Sign out") }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Create an account to keep your Roadmap progress if you switch phones or reinstall.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        state.authName, viewModel::setAuthName,
                        label = { Text("Display name") },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    )
                    OutlinedTextField(
                        state.authEmail, viewModel::setAuthEmail,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        state.authPassword, viewModel::setAuthPassword,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = viewModel::createAccount,
                            enabled = !state.authBusy && state.authEmail.isNotBlank() && state.authPassword.isNotBlank(),
                            modifier = Modifier.weight(1f),
                        ) { Text("Create account") }
                        OutlinedButton(
                            onClick = viewModel::signIn,
                            enabled = !state.authBusy && state.authEmail.isNotBlank() && state.authPassword.isNotBlank(),
                            modifier = Modifier.weight(1f),
                        ) { Text("Sign in") }
                    }
                }
            }
            state.authMessage?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        item { SectionHeader("Notifications") }
        item {
            ToggleRow(
                title = "Daily practice reminder",
                subtitle = "A nudge when your streak is at risk",
                checked = state.notificationsEnabled,
                onChange = viewModel::setNotifications,
            )
        }

        item { SectionHeader("Data") }
        item {
            ToggleRow(
                title = "Sync on cellular",
                subtitle = "Otherwise progress syncs on Wi-Fi only",
                checked = state.syncOnCellular,
                onChange = viewModel::setSyncOnCellular,
            )
        }
        item {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text(
                    if (state.pendingUploads == 0) {
                        "Everything on this device is saved locally."
                    } else {
                        "${state.pendingUploads} answer(s) waiting to upload."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    state.lastSyncedAt?.let { "Last synced at $it." } ?: "Never synced.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = viewModel::syncNow,
                    enabled = !state.syncing,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(if (state.syncing) "Syncing..." else "Sync now")
                }
                state.syncMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
        item {
            Text(
                "Ratings, streaks and quiz history stay on this device and work fully offline. " +
                    "Roadmap course progress syncs through the account above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }

        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
