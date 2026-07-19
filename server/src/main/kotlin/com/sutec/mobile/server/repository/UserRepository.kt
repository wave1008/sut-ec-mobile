package com.sutec.mobile.server.repository

import com.sutec.mobile.data.model.User
import com.sutec.mobile.server.db.Carts
import com.sutec.mobile.server.db.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

// password_hash を含む行。API へは漏らさない(User へマップして返す)。
data class UserRow(val id: UUID, val name: String, val email: String, val passwordHash: String)

object UserRepository {
    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }

    private fun ResultRow.toRow() = UserRow(
        id = this[Users.id],
        name = this[Users.name],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
    )

    suspend fun findByEmail(email: String): UserRow? = dbQuery {
        Users.selectAll().where { Users.email eq email }.map { it.toRow() }.firstOrNull()
    }

    suspend fun findById(id: UUID): User? = dbQuery {
        Users.selectAll().where { Users.id eq id }
            .map { User(it[Users.id].toString(), it[Users.name], it[Users.email]) }
            .firstOrNull()
    }

    // ユーザー作成と同時に空カート行を用意(S3 の cart 操作が user_id 前提のため)。
    suspend fun create(name: String, email: String, passwordHash: String): User = dbQuery {
        val newId = UUID.randomUUID()
        Users.insert {
            it[id] = newId
            it[Users.name] = name
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[createdAt] = Instant.now()
        }
        Carts.insert { it[userId] = newId }
        User(newId.toString(), name, email)
    }
}
