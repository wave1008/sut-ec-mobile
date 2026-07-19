package com.sutec.mobile.server.repository

import com.sutec.mobile.data.dto.PlaceOrderRequest
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.data.model.OrderStatus
import com.sutec.mobile.data.model.OrderTotals
import com.sutec.mobile.data.model.PaymentType
import com.sutec.mobile.data.repository.computeOrderTotals
import com.sutec.mobile.server.ApiException
import com.sutec.mobile.server.db.Addresses
import com.sutec.mobile.server.db.CartItems
import com.sutec.mobile.server.db.OrderItems
import com.sutec.mobile.server.db.Orders
import com.sutec.mobile.server.db.PaymentMethods
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

// 注文はユーザー単位。確定時に金額をサーバーが権威計算し、items は Product スナップショット。
object OrderStore {
    private val TOKYO = ZoneId.of("Asia/Tokyo")

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }

    private fun itemsOf(orderId: String): List<CartItem> =
        OrderItems.selectAll().where { OrderItems.orderId eq orderId }
            .orderBy(OrderItems.position to SortOrder.ASC)
            .map { CartItem(it[OrderItems.product], it[OrderItems.quantity]) }

    private fun rowToOrder(row: ResultRow): Order = Order(
        id = row[Orders.id],
        items = itemsOf(row[Orders.id]),
        totals = OrderTotals(row[Orders.subtotalYen], row[Orders.shippingYen], row[Orders.taxYen]),
        status = OrderStatus.valueOf(row[Orders.status]),
        placedAt = row[Orders.placedAt],
        shippingAddress = row[Orders.shippingAddress],
        paymentLabel = row[Orders.paymentLabel],
    )

    suspend fun list(userId: UUID): List<Order> = dbQuery {
        Orders.selectAll().where { Orders.userId eq userId }
            .orderBy(Orders.createdAt to SortOrder.DESC)
            .map { rowToOrder(it) }
    }

    suspend fun get(userId: UUID, id: String): Order? = dbQuery {
        Orders.selectAll().where { (Orders.id eq id) and (Orders.userId eq userId) }
            .map { rowToOrder(it) }.firstOrNull()
    }

    // カート内容 + 住所/支払い(所有確認) から Order を構築。金額は computeOrderTotals が権威。
    suspend fun place(userId: UUID, req: PlaceOrderRequest): Order = dbQuery {
        val products = CatalogRepository.productsInTx().associateBy { it.id }
        val items = CartItems.selectAll().where { CartItems.cartUserId eq userId }
            .orderBy(CartItems.productId to SortOrder.ASC)
            .mapNotNull { row -> products[row[CartItems.productId]]?.let { CartItem(it, row[CartItems.quantity]) } }
        if (items.isEmpty()) {
            throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "cart is empty")
        }

        val address: Address = Addresses.selectAll()
            .where { (Addresses.id eq req.addressId) and (Addresses.userId eq userId) }
            .map {
                Address(
                    it[Addresses.id], it[Addresses.fullName], it[Addresses.postalCode], it[Addresses.prefecture],
                    it[Addresses.city], it[Addresses.line1], it[Addresses.line2], it[Addresses.phone], it[Addresses.isDefault],
                )
            }.firstOrNull()
            ?: throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "address not found: ${req.addressId}")

        val payRow = PaymentMethods.selectAll()
            .where { (PaymentMethods.id eq req.paymentMethodId) and (PaymentMethods.userId eq userId) }
            .firstOrNull()
            ?: throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "payment method not found: ${req.paymentMethodId}")
        val paymentLabel = when (PaymentType.valueOf(payRow[PaymentMethods.type])) {
            PaymentType.CARD -> "${payRow[PaymentMethods.brand]} •••• ${payRow[PaymentMethods.last4]}"
            PaymentType.CASH_ON_DELIVERY -> "代金引換"
        }

        val totals = computeOrderTotals(items)
        val orderId = "ord_" + UUID.randomUUID().toString().take(8)
        val placedAt = LocalDate.now(TOKYO).toString()

        Orders.insert {
            it[id] = orderId
            it[Orders.userId] = userId
            it[status] = OrderStatus.PROCESSING.name
            it[Orders.placedAt] = placedAt
            it[subtotalYen] = totals.subtotalYen
            it[shippingYen] = totals.shippingYen
            it[taxYen] = totals.taxYen
            it[shippingAddress] = address
            it[Orders.paymentLabel] = paymentLabel
        }
        items.forEachIndexed { idx, ci ->
            OrderItems.insert {
                it[OrderItems.orderId] = orderId
                it[position] = idx
                it[product] = ci.product
                it[quantity] = ci.quantity
            }
        }
        CartItems.deleteWhere { cartUserId eq userId }

        Order(
            id = orderId, items = items, totals = totals, status = OrderStatus.PROCESSING,
            placedAt = placedAt, shippingAddress = address, paymentLabel = paymentLabel,
        )
    }
}
