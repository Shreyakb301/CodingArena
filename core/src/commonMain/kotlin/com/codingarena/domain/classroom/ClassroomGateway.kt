package com.codingarena.domain.classroom

import com.codingarena.domain.model.Assignment
import com.codingarena.domain.model.AuthResponse
import com.codingarena.domain.model.ClassMembership
import com.codingarena.domain.model.Classroom
import com.codingarena.domain.model.CreateAssignmentRequest
import com.codingarena.domain.model.LoginRequest
import com.codingarena.domain.model.RegisterRequest
import com.codingarena.domain.model.ProgressSyncPayload
import com.codingarena.domain.model.ClassroomDashboard

interface ClassroomGateway {
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun classrooms(): List<Classroom>
    suspend fun createClassroom(name: String): Classroom
    suspend fun join(inviteCode: String, displayName: String): Classroom
    suspend fun createAssignment(classroomId: String, request: CreateAssignmentRequest): Assignment
    suspend fun assignments(classroomId: String): List<Assignment>
    suspend fun members(classroomId: String): List<ClassMembership>
    suspend fun pushProgress(payload: ProgressSyncPayload)

    /** The signed-in user's own saved progress, or null if nothing has been synced yet. */
    suspend fun fetchProgress(): ProgressSyncPayload?
    suspend fun dashboard(classroomId: String): ClassroomDashboard
    suspend fun signOut()
}
