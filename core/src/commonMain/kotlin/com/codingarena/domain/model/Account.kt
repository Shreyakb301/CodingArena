package com.codingarena.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val displayName: String,
    val email: String? = null,
    val password: String,
    val role: UserRole = UserRole.STUDENT,
)

@Serializable data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val role: UserRole,
    val displayName: String,
)

@Serializable data class CreateClassroomRequest(val name: String)
@Serializable data class JoinClassroomRequest(val inviteCode: String, val displayName: String)
@Serializable data class CreateAssignmentRequest(
    val title: String,
    val chapterIds: List<String> = emptyList(),
    val exerciseIds: List<String> = emptyList(),
    val dueAt: Long? = null,
)

@Serializable
data class ConfusionSummary(val actual: String, val mistakenFor: String, val count: Int)

@Serializable
data class ProgressSyncPayload(
    val chapters: List<ChapterProgress>,
    val confusions: List<ConfusionSummary> = emptyList(),
    val updatedAt: Long,
)

@Serializable
data class StudentProgressView(
    val userId: String,
    val displayName: String,
    val chapters: List<ChapterProgress>,
    val confusions: List<ConfusionSummary>,
)

@Serializable
data class ClassroomDashboard(
    val classroom: Classroom,
    val students: List<StudentProgressView>,
    val assignments: List<Assignment>,
)
