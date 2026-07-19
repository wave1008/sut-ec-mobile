package com.sutec.mobile.server.repository

import com.sutec.mobile.data.dto.CartDto
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.repository.computeOrderTotals
import com.sutec.mobile.server.ApiException
import com.sutec.mobile.server.db.CartItems
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

// カートはユーザー単位。product はカタログ参照(スナップショットは注文確定時のみ)。
object CartStore {
    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }

    private fun cartOf(userId: UUID): CartDto {
        val products = CatalogRepository.productsInTx().associateBy { it.id }
        // 表示順は product_id 昇順(cart_items に挿入順の列を持たないため。UI 影響は小)。
        val items = CartItems.selectAll()
            .where { CartItems.cartUserId eq userId }
            .orderBy(CartItems.productId to SortOrder.ASC)
            .mapNotNull { row ->
                products[row[CartItems.productId]]?.let { CartItem(it, row[CartItems.quantity]) }
            }
        return CartDto(items, computeOrderTotals(items))
    }

    suspend fun get(userId: UUID): CartDto = dbQuery { cartOf(userId) }

    suspend fun addItem(userId: UUID, productId: String, quantity: Int): CartDto = dbQuery {
        if (CatalogRepository.productsInTx().none { it.id == productId }) {
            throw ApiException(HttpStatusCode.NotFound, "NOT_FOUND", "product not found: $productId")
        }
        val existing = CartItems.selectAll()
            .where { (CartItems.cartUserId eq userId) and (CartItems.productId eq productId) }
            .firstOrNull()
        if (existing != null) {
            CartItems.update({ (CartItems.cartUserId eq userId) and (CartItems.productId eq productId) }) {
                it[CartItems.quantity] = existing[CartItems.quantity] + quantity
            }
        } else {
            CartItems.insert {
                it[cartUserId] = userId
                it[CartItems.productId] = productId
                it[CartItems.quantity] = quantity
            }
        }
        cartOf(userId)
    }

    // client の setQuantity 準拠: 0 以下で削除。存在しない product は無視。
    suspend fun setQuantity(userId: UUID, productId: String, quantity: Int): CartDto = dbQuery {
        if (quantity <= 0) {
            CartItems.deleteWhere { (cartUserId eq userId) and (CartItems.productId eq productId) }
        } else {
            CartItems.update({ (CartItems.cartUserId eq userId) and (CartItems.productId eq productId) }) {
                it[CartItems.quantity] = quantity
            }
        }
        cartOf(userId)
    }

    suspend fun remove(userId: UUID, productId: String): CartDto = dbQuery {
        CartItems.deleteWhere { (cartUserId eq userId) and (CartItems.productId eq productId) }
        cartOf(userId)
    }

    suspend fun clear(userId: UUID): CartDto = dbQuery {
        CartItems.deleteWhere { cartUserId eq userId }
        cartOf(userId)
    }
}
