package com.codingarena.features.classroom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.codingarena.content.ArenaCourse
import com.codingarena.core.design.ArenaListItem
import com.codingarena.core.design.SectionHeader
import com.codingarena.data.remote.KtorClassroomGateway
import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.model.Assignment
import com.codingarena.domain.model.ClassMembership
import com.codingarena.domain.model.Classroom
import com.codingarena.domain.model.CreateAssignmentRequest
import com.codingarena.domain.model.LoginRequest
import com.codingarena.domain.model.RegisterRequest
import com.codingarena.domain.model.UserRole
import com.codingarena.domain.model.StudentProgressView
import com.codingarena.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel

data class ClassroomUiState(
    val role: UserRole? = null,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val className: String = "",
    val inviteCode: String = "",
    val classrooms: List<Classroom> = emptyList(),
    val selected: Classroom? = null,
    val members: List<ClassMembership> = emptyList(),
    val assignments: List<Assignment> = emptyList(),
    val studentProgress: List<StudentProgressView> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null,
)

class ClassroomViewModel(
    private val gateway: ClassroomGateway,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ClassroomUiState())
    val state: StateFlow<ClassroomUiState> = _state.asStateFlow()

    fun load() = launch {
        val role = settings.get(KtorClassroomGateway.AUTH_ROLE)
            ?.let { value -> UserRole.entries.firstOrNull { it.name == value } }
        _state.value = _state.value.copy(
            role = role,
            displayName = settings.get(KtorClassroomGateway.AUTH_NAME).orEmpty(),
        )
        if (role != null) refreshClasses()
    }

    fun setName(value: String) { _state.value = _state.value.copy(displayName = value) }
    fun setEmail(value: String) { _state.value = _state.value.copy(email = value) }
    fun setPassword(value: String) { _state.value = _state.value.copy(password = value) }
    fun setClassName(value: String) { _state.value = _state.value.copy(className = value) }
    fun setInvite(value: String) { _state.value = _state.value.copy(inviteCode = value.uppercase()) }

    fun register(role: UserRole) = launch {
        val response = gateway.register(
            RegisterRequest(_state.value.displayName, _state.value.email, _state.value.password, role)
        )
        _state.value = _state.value.copy(role = response.role, message = "Account ready")
        refreshClasses()
    }

    fun login() = launch {
        val response = gateway.login(LoginRequest(_state.value.email, _state.value.password))
        _state.value = _state.value.copy(role = response.role, displayName = response.displayName)
        refreshClasses()
    }

    fun createClassroom() = launch {
        val classroom = gateway.createClassroom(_state.value.className)
        _state.value = _state.value.copy(className = "", message = "Share code ${classroom.inviteCode}")
        refreshClasses()
    }

    fun joinClassroom() = launch {
        gateway.join(_state.value.inviteCode, _state.value.displayName)
        _state.value = _state.value.copy(inviteCode = "", message = "Class joined")
        refreshClasses()
    }

    fun select(classroom: Classroom) = launch {
        if (_state.value.role == UserRole.TEACHER) {
            val dashboard = gateway.dashboard(classroom.id)
            val members = gateway.members(classroom.id)
            _state.value = _state.value.copy(
                selected = classroom,
                assignments = dashboard.assignments,
                members = members,
                studentProgress = dashboard.students,
            )
        } else {
            _state.value = _state.value.copy(
                selected = classroom,
                assignments = gateway.assignments(classroom.id),
            )
        }
    }

    fun assignCurrentChapter() = launch {
        val classroom = _state.value.selected ?: return@launch
        val chapter = ArenaCourse.availableChapters.first()
        gateway.createAssignment(
            classroom.id,
            CreateAssignmentRequest("Complete ${chapter.title}", chapterIds = listOf(chapter.id)),
        )
        select(classroom)
    }

    private suspend fun refreshClasses() {
        _state.value = _state.value.copy(classrooms = gateway.classrooms())
    }

    private fun launch(block: suspend () -> Unit) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            runCatching { block() }
                .onFailure { _state.value = _state.value.copy(message = it.message ?: "Request failed") }
            _state.value = _state.value.copy(busy = false)
        }
    }
}

@Composable
fun ClassroomScreen(onBack: () -> Unit, viewModel: ClassroomViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 6.dp)) { Text("Back") }
            Text("CLASSROOM", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Text("Learn together", style = MaterialTheme.typography.headlineMedium)
        }
        if (state.role == null) {
            item {
                OutlinedTextField(state.displayName, viewModel::setName, label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.email, viewModel::setEmail, label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(state.password, viewModel::setPassword, label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ viewModel.register(UserRole.STUDENT) }, Modifier.weight(1f)) { Text("Student") }
                    Button({ viewModel.register(UserRole.TEACHER) }, Modifier.weight(1f)) { Text("Teacher") }
                }
                OutlinedButton(viewModel::login, Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Sign in") }
            }
        } else {
            item { Text("Signed in as ${state.displayName} · ${state.role?.name?.lowercase().orEmpty()}") }
            if (state.role == UserRole.TEACHER) {
                item {
                    OutlinedTextField(state.className, viewModel::setClassName, label = { Text("New class name") },
                        modifier = Modifier.fillMaxWidth())
                    Button(viewModel::createClassroom, Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("Create class")
                    }
                }
            } else {
                item {
                    OutlinedTextField(state.inviteCode, viewModel::setInvite, label = { Text("Class invite code") },
                        modifier = Modifier.fillMaxWidth())
                    Button(viewModel::joinClassroom, Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Join class") }
                }
            }
            item { SectionHeader("My classes") }
            items(state.classrooms) { classroom ->
                ArenaListItem(
                    title = classroom.name,
                    subtitle = if (state.role == UserRole.TEACHER) "Invite ${classroom.inviteCode}" else "Assignments",
                    trailing = "›",
                    onClick = { viewModel.select(classroom) },
                )
            }
            state.selected?.let { selected ->
                item { SectionHeader(selected.name) }
                if (state.role == UserRole.TEACHER) {
                    item {
                        Text("${state.members.count { it.role == UserRole.STUDENT }} students")
                        Button(viewModel::assignCurrentChapter, Modifier.fillMaxWidth().padding(top = 6.dp)) {
                            Text("Assign first Roadmap chapter")
                        }
                    }
                    items(state.studentProgress) { student ->
                        val due = student.chapters.count { progress ->
                            progress.reviewDueAt?.let { it <= Clock.System.now().toEpochMilliseconds() } == true
                        }
                        val mastered = student.chapters.count { progress ->
                            progress.evidence.count { it.kind.name == "REVIEW_PASS" } >= 3
                        }
                        val confusion = student.confusions.maxByOrNull { it.count }
                        ArenaListItem(
                            student.displayName,
                            "$mastered mastered · $due due" +
                                (confusion?.let { " · ${it.actual} → ${it.mistakenFor}" } ?: ""),
                            leading = "♟",
                        )
                    }
                }
                items(state.assignments) { assignment ->
                    ArenaListItem(assignment.title, assignment.chapterIds.joinToString(), leading = "A")
                }
            }
        }
        state.message?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary) } }
        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}
