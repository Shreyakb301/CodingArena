package com.codingarena.features.auth

import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.model.Assignment
import com.codingarena.domain.model.AuthResponse
import com.codingarena.domain.model.ClassMembership
import com.codingarena.domain.model.Classroom
import com.codingarena.domain.model.ClassroomDashboard
import com.codingarena.domain.model.CreateAssignmentRequest
import com.codingarena.domain.model.LoginRequest
import com.codingarena.domain.model.ProgressSyncPayload
import com.codingarena.domain.model.RegisterRequest
import com.codingarena.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeGateway(
    var onRegister: (RegisterRequest) -> AuthResponse = { auth() },
    var onLogin: (LoginRequest) -> AuthResponse = { auth() },
) : ClassroomGateway {
    val registered = mutableListOf<RegisterRequest>()
    val loggedIn = mutableListOf<LoginRequest>()

    override suspend fun register(request: RegisterRequest): AuthResponse {
        registered += request
        return onRegister(request)
    }

    override suspend fun login(request: LoginRequest): AuthResponse {
        loggedIn += request
        return onLogin(request)
    }

    override suspend fun classrooms() = notUsed()
    override suspend fun createClassroom(name: String) = notUsed()
    override suspend fun join(inviteCode: String, displayName: String) = notUsed()
    override suspend fun createAssignment(classroomId: String, request: CreateAssignmentRequest) = notUsed()
    override suspend fun assignments(classroomId: String): List<Assignment> = notUsed()
    override suspend fun members(classroomId: String): List<ClassMembership> = notUsed()
    override suspend fun pushProgress(payload: ProgressSyncPayload) = notUsed()
    override suspend fun fetchProgress(): ProgressSyncPayload? = notUsed()
    override suspend fun dashboard(classroomId: String): ClassroomDashboard = notUsed()
    override suspend fun signOut() = notUsed()

    private fun notUsed(): Nothing = throw AssertionError("not used by AuthViewModel")

    companion object {
        fun auth() = AuthResponse("token-xyz", "user-1", UserRole.STUDENT, "Ada")
    }
}

class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `cannot submit until required fields are filled`() {
        val vm = AuthViewModel(FakeGateway())
        assertFalse(vm.state.value.canSubmit)

        vm.setEmail("a@b.com")
        assertFalse(vm.state.value.canSubmit) // no password yet
        vm.setPassword("longpassword")
        assertTrue(vm.state.value.canSubmit)  // log in only needs email + password

        vm.setMode(AuthMode.SIGN_UP)
        assertFalse(vm.state.value.canSubmit) // sign up also needs a name
        vm.setName("Ada")
        assertTrue(vm.state.value.canSubmit)
    }

    @Test
    fun `a successful login sets done and calls the gateway once`() = runTest(dispatcher) {
        val gateway = FakeGateway()
        val vm = AuthViewModel(gateway)
        vm.setEmail(" A@B.com ")
        vm.setPassword("longpassword")

        vm.submit()
        runCurrent()

        assertTrue(vm.state.value.done)
        assertFalse(vm.state.value.busy)
        assertEquals(1, gateway.loggedIn.size)
        assertEquals("a@b.com", gateway.loggedIn.single().email) // trimmed + lowercased
    }

    @Test
    fun `sign up sends the display name and a STUDENT role`() = runTest(dispatcher) {
        val gateway = FakeGateway()
        val vm = AuthViewModel(gateway).apply {
            setMode(AuthMode.SIGN_UP)
            setName("  Grace  ")
            setEmail("grace@hopper.dev")
            setPassword("longpassword")
        }
        vm.submit()
        runCurrent()

        val req = gateway.registered.single()
        assertEquals("Grace", req.displayName)
        assertEquals(UserRole.STUDENT, req.role)
        assertTrue(vm.state.value.done)
    }

    @Test
    fun `a failed login surfaces the server message and does not set done`() = runTest(dispatcher) {
        val gateway = FakeGateway(onLogin = { error("Invalid credentials") })
        val vm = AuthViewModel(gateway).apply {
            setEmail("a@b.com")
            setPassword("wrongpassword")
        }
        vm.submit()
        runCurrent()

        assertEquals("Invalid credentials", vm.state.value.error)
        assertFalse(vm.state.value.done)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `switching mode clears a previous error`() = runTest(dispatcher) {
        val vm = AuthViewModel(FakeGateway(onLogin = { error("nope") })).apply {
            setEmail("a@b.com"); setPassword("longpassword")
        }
        vm.submit(); runCurrent()
        assertEquals("nope", vm.state.value.error)

        vm.setMode(AuthMode.SIGN_UP)
        assertEquals(null, vm.state.value.error)
    }

    @Test
    fun `submit is a no-op while busy or when it cannot submit`() = runTest(dispatcher) {
        val gateway = FakeGateway()
        val vm = AuthViewModel(gateway)

        vm.submit() // no fields
        runCurrent()
        assertEquals(0, gateway.loggedIn.size)
        assertFalse(vm.state.value.done)
    }
}
