package com.codingarena.server

import com.codingarena.domain.model.Assignment
import com.codingarena.domain.model.ClassMembership
import com.codingarena.domain.model.Classroom
import com.codingarena.domain.model.CodeSubmission
import com.codingarena.domain.model.UserRole
import com.codingarena.domain.model.ProgressSyncPayload
import com.codingarena.domain.model.RegisterRequest
import kotlinx.serialization.json.Json
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

interface ArenaStore {
    fun createUser(request: RegisterRequest): UserAccount
    fun userByEmail(email: String): UserAccount?

    /**
     * Resolves a Google-authenticated user: returns the account already linked
     * to [googleId], otherwise links an existing account with the same email,
     * otherwise creates a new student account. The email is trusted - the caller
     * has verified it against Google.
     */
    fun linkOrCreateGoogleUser(googleId: String, email: String, displayName: String): UserAccount
    fun saveSubmission(submission: CodeSubmission)
    fun submission(id: String): CodeSubmission?
    fun createClassroom(teacherId: String, name: String, now: Long): Classroom
    fun classrooms(userId: String): List<Classroom>
    fun joinClassroom(userId: String, displayName: String, inviteCode: String, now: Long): Classroom?
    fun membership(classroomId: String, userId: String): ClassMembership?
    fun createAssignment(teacherId: String, assignment: Assignment): Assignment
    fun assignments(classroomId: String, userId: String): List<Assignment>
    fun members(classroomId: String, teacherId: String): List<ClassMembership>
    fun saveProgress(userId: String, payload: ProgressSyncPayload)
    fun progress(userIds: List<String>): Map<String, ProgressSyncPayload>
    fun auditSubmission(submissionId: String, userId: String, event: String, occurredAt: Long)
}

class PostgresArenaStore(private val config: ServerConfig) : ArenaStore {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    init { connection().use { createSchema(it) } }

    override fun createUser(request: RegisterRequest): UserAccount {
        require(request.displayName.trim().length in 2..80) { "Display name must be 2-80 characters" }
        require(request.password.length >= 10) { "Password must be at least 10 characters" }
        if (request.role == UserRole.TEACHER) require(!request.email.isNullOrBlank()) { "Teachers need an email" }
        val email = request.email?.trim()?.lowercase()
        if (email != null) require(userByEmail(email) == null) { "That email is already registered" }
        val account = UserAccount(
            id = UUID.randomUUID().toString(),
            displayName = request.displayName.trim(),
            email = email,
            role = request.role,
            passwordHash = Passwords.hash(request.password),
        )
        connection().use { db ->
            db.prepareStatement(
                "INSERT INTO arena_user(id, display_name, email, role, password_hash, created_at) VALUES (?, ?, ?, ?, ?, ?)"
            ).use { statement ->
                statement.setString(1, account.id)
                statement.setString(2, account.displayName)
                statement.setString(3, account.email)
                statement.setString(4, account.role.name)
                statement.setString(5, account.passwordHash)
                statement.setLong(6, System.currentTimeMillis())
                statement.executeUpdate()
            }
        }
        return account
    }

    override fun userByEmail(email: String): UserAccount? =
        userWhere("email = ?", email.trim().lowercase())

    private fun userByGoogleId(googleId: String): UserAccount? =
        userWhere("google_id = ?", googleId)

    private fun userWhere(clause: String, value: String): UserAccount? = connection().use { db ->
        db.prepareStatement("SELECT * FROM arena_user WHERE $clause").use { statement ->
            statement.setString(1, value)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toUserAccount() else null }
        }
    }

    private fun java.sql.ResultSet.toUserAccount() = UserAccount(
        id = getString("id"),
        displayName = getString("display_name"),
        email = getString("email"),
        role = UserRole.valueOf(getString("role")),
        passwordHash = getString("password_hash"),
    )

    override fun linkOrCreateGoogleUser(googleId: String, email: String, displayName: String): UserAccount {
        userByGoogleId(googleId)?.let { return it }
        val normalisedEmail = email.trim().lowercase()

        userByEmail(normalisedEmail)?.let { existing ->
            connection().use { db ->
                db.prepareStatement("UPDATE arena_user SET google_id = ? WHERE id = ?").use { statement ->
                    statement.setString(1, googleId)
                    statement.setString(2, existing.id)
                    statement.executeUpdate()
                }
            }
            return existing
        }

        val account = UserAccount(
            id = UUID.randomUUID().toString(),
            displayName = displayName.trim().take(80).ifBlank { normalisedEmail.substringBefore('@') },
            email = normalisedEmail,
            role = UserRole.STUDENT,
            passwordHash = null,
        )
        connection().use { db ->
            db.prepareStatement(
                "INSERT INTO arena_user(id, display_name, email, role, password_hash, google_id, created_at) " +
                    "VALUES (?, ?, ?, ?, NULL, ?, ?)"
            ).use { statement ->
                statement.setString(1, account.id)
                statement.setString(2, account.displayName)
                statement.setString(3, account.email)
                statement.setString(4, account.role.name)
                statement.setString(5, googleId)
                statement.setLong(6, System.currentTimeMillis())
                statement.executeUpdate()
            }
        }
        return account
    }

    override fun saveSubmission(submission: CodeSubmission) {
        connection().use { db ->
        db.prepareStatement(
            """INSERT INTO code_submission(id, user_id, payload_json, created_at, updated_at)
               VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE
               SET payload_json = EXCLUDED.payload_json, updated_at = EXCLUDED.updated_at"""
        ).use { statement ->
            statement.setString(1, submission.id)
            statement.setString(2, submission.userId)
            statement.setString(3, json.encodeToString(submission))
            statement.setLong(4, submission.createdAt)
            statement.setLong(5, submission.updatedAt)
            statement.executeUpdate()
        }
        }
    }

    override fun submission(id: String): CodeSubmission? = connection().use { db ->
        db.prepareStatement("SELECT payload_json FROM code_submission WHERE id = ?").use { statement ->
            statement.setString(1, id)
            statement.executeQuery().use { rows ->
                if (rows.next()) json.decodeFromString<CodeSubmission>(rows.getString(1)) else null
            }
        }
    }

    override fun createClassroom(teacherId: String, name: String, now: Long): Classroom {
        require(name.trim().length in 2..80) { "Class name must be 2-80 characters" }
        val classroom = Classroom(
            UUID.randomUUID().toString(), teacherId, name.trim(), uniqueInviteCode(), now,
        )
        connection().use { db ->
            db.autoCommit = false
            db.prepareStatement("INSERT INTO classroom(id, teacher_id, name, invite_code, created_at) VALUES (?, ?, ?, ?, ?)")
                .use { statement ->
                    statement.setString(1, classroom.id); statement.setString(2, teacherId)
                    statement.setString(3, classroom.name); statement.setString(4, classroom.inviteCode)
                    statement.setLong(5, now); statement.executeUpdate()
                }
            insertMembership(db, ClassMembership(classroom.id, teacherId, UserRole.TEACHER, "Teacher", now))
            db.commit()
        }
        return classroom
    }

    override fun classrooms(userId: String): List<Classroom> = connection().use { db ->
        db.prepareStatement(
            """SELECT c.* FROM classroom c JOIN class_membership m ON m.classroom_id = c.id
               WHERE m.user_id = ? ORDER BY c.created_at DESC"""
        ).use { statement ->
            statement.setString(1, userId)
            statement.executeQuery().use { rows ->
                buildList { while (rows.next()) add(rows.classroom()) }
            }
        }
    }

    override fun joinClassroom(
        userId: String,
        displayName: String,
        inviteCode: String,
        now: Long,
    ): Classroom? = connection().use { db ->
        val classroom = db.prepareStatement("SELECT * FROM classroom WHERE invite_code = ?").use { statement ->
            statement.setString(1, inviteCode.trim().uppercase())
            statement.executeQuery().use { rows -> if (rows.next()) rows.classroom() else null }
        } ?: return@use null
        insertMembership(db, ClassMembership(classroom.id, userId, UserRole.STUDENT, displayName.take(80), now))
        classroom
    }

    override fun membership(classroomId: String, userId: String): ClassMembership? = connection().use { db ->
        db.prepareStatement("SELECT * FROM class_membership WHERE classroom_id = ? AND user_id = ?")
            .use { statement ->
                statement.setString(1, classroomId); statement.setString(2, userId)
                statement.executeQuery().use { rows ->
                    if (!rows.next()) null else ClassMembership(
                        classroomId, userId, UserRole.valueOf(rows.getString("role")),
                        rows.getString("display_name"), rows.getLong("joined_at"),
                    )
                }
            }
    }

    override fun createAssignment(teacherId: String, assignment: Assignment): Assignment {
        require(membership(assignment.classroomId, teacherId)?.role == UserRole.TEACHER) { "Teacher access required" }
        connection().use { db ->
            db.prepareStatement("INSERT INTO assignment(id, classroom_id, payload_json, created_at) VALUES (?, ?, ?, ?)")
                .use { statement ->
                    statement.setString(1, assignment.id); statement.setString(2, assignment.classroomId)
                    statement.setString(3, json.encodeToString(assignment)); statement.setLong(4, assignment.createdAt)
                    statement.executeUpdate()
                }
        }
        return assignment
    }

    override fun assignments(classroomId: String, userId: String): List<Assignment> {
        require(membership(classroomId, userId) != null) { "Classroom access required" }
        return connection().use { db ->
            db.prepareStatement("SELECT payload_json FROM assignment WHERE classroom_id = ? ORDER BY created_at DESC")
                .use { statement ->
                    statement.setString(1, classroomId)
                    statement.executeQuery().use { rows ->
                        buildList { while (rows.next()) add(json.decodeFromString<Assignment>(rows.getString(1))) }
                    }
                }
        }
    }

    override fun members(classroomId: String, teacherId: String): List<ClassMembership> {
        require(membership(classroomId, teacherId)?.role == UserRole.TEACHER) { "Teacher access required" }
        return connection().use { db ->
            db.prepareStatement("SELECT * FROM class_membership WHERE classroom_id = ? ORDER BY joined_at")
                .use { statement ->
                    statement.setString(1, classroomId)
                    statement.executeQuery().use { rows -> buildList {
                        while (rows.next()) add(
                            ClassMembership(
                                classroomId, rows.getString("user_id"), UserRole.valueOf(rows.getString("role")),
                                rows.getString("display_name"), rows.getLong("joined_at"),
                            )
                        )
                    } }
                }
        }
    }

    override fun saveProgress(userId: String, payload: ProgressSyncPayload) {
        connection().use { db ->
            db.prepareStatement(
                """INSERT INTO student_progress(user_id, payload_json, updated_at) VALUES (?, ?, ?)
                   ON CONFLICT (user_id) DO UPDATE SET payload_json = EXCLUDED.payload_json,
                   updated_at = EXCLUDED.updated_at WHERE student_progress.updated_at <= EXCLUDED.updated_at"""
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, json.encodeToString(payload))
                statement.setLong(3, payload.updatedAt)
                statement.executeUpdate()
            }
        }
    }

    override fun progress(userIds: List<String>): Map<String, ProgressSyncPayload> {
        if (userIds.isEmpty()) return emptyMap()
        val placeholders = userIds.joinToString(",") { "?" }
        return connection().use { db ->
            db.prepareStatement("SELECT user_id, payload_json FROM student_progress WHERE user_id IN ($placeholders)")
                .use { statement ->
                    userIds.forEachIndexed { index, id -> statement.setString(index + 1, id) }
                    statement.executeQuery().use { rows -> buildMap {
                        while (rows.next()) put(
                            rows.getString("user_id"),
                            json.decodeFromString<ProgressSyncPayload>(rows.getString("payload_json")),
                        )
                    } }
                }
        }
    }

    override fun auditSubmission(submissionId: String, userId: String, event: String, occurredAt: Long) {
        connection().use { db ->
            db.prepareStatement(
                "INSERT INTO submission_audit(submission_id, user_id, event, occurred_at) VALUES (?, ?, ?, ?)"
            ).use { statement ->
                statement.setString(1, submissionId); statement.setString(2, userId)
                statement.setString(3, event.take(80)); statement.setLong(4, occurredAt)
                statement.executeUpdate()
            }
        }
    }

    private fun connection(): Connection = DriverManager.getConnection(
        config.databaseUrl, config.databaseUser, config.databasePassword,
    )

    private fun uniqueInviteCode(): String = UUID.randomUUID().toString().replace("-", "")
        .take(8).uppercase()

    private fun insertMembership(db: Connection, membership: ClassMembership) {
        db.prepareStatement(
            """INSERT INTO class_membership(classroom_id, user_id, role, display_name, joined_at)
               VALUES (?, ?, ?, ?, ?) ON CONFLICT (classroom_id, user_id) DO NOTHING"""
        ).use { statement ->
            statement.setString(1, membership.classroomId); statement.setString(2, membership.userId)
            statement.setString(3, membership.role.name); statement.setString(4, membership.displayName)
            statement.setLong(5, membership.joinedAt); statement.executeUpdate()
        }
    }

    private fun java.sql.ResultSet.classroom() = Classroom(
        getString("id"), getString("teacher_id"), getString("name"),
        getString("invite_code"), getLong("created_at"),
    )

    private fun createSchema(db: Connection) {
        listOf(
            """CREATE TABLE IF NOT EXISTS arena_user (
                id TEXT PRIMARY KEY, display_name TEXT NOT NULL, email TEXT UNIQUE, role TEXT NOT NULL,
                password_hash TEXT NOT NULL, created_at BIGINT NOT NULL)""",
            """CREATE TABLE IF NOT EXISTS code_submission (
                id TEXT PRIMARY KEY, user_id TEXT NOT NULL, payload_json TEXT NOT NULL,
                created_at BIGINT NOT NULL, updated_at BIGINT NOT NULL)""",
            """CREATE INDEX IF NOT EXISTS submission_user_idx ON code_submission(user_id, created_at)""",
            """CREATE TABLE IF NOT EXISTS classroom (
                id TEXT PRIMARY KEY, teacher_id TEXT NOT NULL, name TEXT NOT NULL,
                invite_code TEXT NOT NULL UNIQUE, created_at BIGINT NOT NULL)""",
            """CREATE TABLE IF NOT EXISTS class_membership (
                classroom_id TEXT NOT NULL, user_id TEXT NOT NULL, role TEXT NOT NULL,
                display_name TEXT NOT NULL, joined_at BIGINT NOT NULL,
                PRIMARY KEY(classroom_id, user_id))""",
            """CREATE TABLE IF NOT EXISTS assignment (
                id TEXT PRIMARY KEY, classroom_id TEXT NOT NULL, payload_json TEXT NOT NULL,
                created_at BIGINT NOT NULL)""",
            """CREATE TABLE IF NOT EXISTS student_progress (
                user_id TEXT PRIMARY KEY, payload_json TEXT NOT NULL, updated_at BIGINT NOT NULL)""",
            """CREATE TABLE IF NOT EXISTS submission_audit (
                sequence_id BIGSERIAL PRIMARY KEY, submission_id TEXT NOT NULL, user_id TEXT NOT NULL,
                event TEXT NOT NULL, occurred_at BIGINT NOT NULL)""",
            """CREATE INDEX IF NOT EXISTS submission_audit_idx
                ON submission_audit(user_id, occurred_at)""",
            // Identity-provider accounts: nullable password, unique provider id.
            """ALTER TABLE arena_user ADD COLUMN IF NOT EXISTS google_id TEXT""",
            """ALTER TABLE arena_user ALTER COLUMN password_hash DROP NOT NULL""",
            """CREATE UNIQUE INDEX IF NOT EXISTS arena_user_google_idx
                ON arena_user(google_id) WHERE google_id IS NOT NULL""",
        ).forEach { sql -> db.createStatement().use { it.execute(sql) } }
    }
}
