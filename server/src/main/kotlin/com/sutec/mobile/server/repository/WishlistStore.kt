package com.sutec.mobile.server.repository

import com.sutec.mobile.server.db.WishlistItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

// お気に入りはユーザー単位の product_id 集合。toggle は client が add(PUT)/remove(DELETE) で表現。
object WishlistStore {
    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }

    suspend fun list(userId: UUID): List<String> = dbQuery {
        WishlistItems.selectAll().where { WishlistItems.userId eq userId }
            .map { it[WishlistItems.productId] }
    }

    suspend fun add(userId: UUID, productId: String): List<String> = dbQuery {
        WishlistItems.insertIgnore {
            it[WishlistItems.userId] = userId
            it[WishlistItems.productId] = productId
        }
        WishlistItems.selectAll().where { WishlistItems.userId eq userId }.map { it[WishlistItems.productId] }
    }

    suspend fun remove(userId: UUID, productId: String): List<String> = dbQuery {
        WishlistItems.deleteWhere { (WishlistItems.userId eq userId) and (WishlistItems.productId eq productId) }
        WishlistItems.selectAll().where { WishlistItems.userId eq userId }.map { it[WishlistItems.productId] }
    }
}
