package com.codingarena.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole { STUDENT, TEACHER }

@Serializable
data class Classroom(
    val id: String,
    val teacherId: String,
    val name: String,
    val inviteCode: String,
    val createdAt: Long,
)

@Serializable
data class ClassMembership(
    val classroomId: String,
    val userId: String,
    val role: UserRole,
    val displayName: String,
    val joinedAt: Long,
)

@Serializable
data class Assignment(
    val id: String,
    val classroomId: String,
    val title: String,
    val chapterIds: List<String> = emptyList(),
    val exerciseIds: List<String> = emptyList(),
    val dueAt: Long? = null,
    val createdAt: Long,
)

