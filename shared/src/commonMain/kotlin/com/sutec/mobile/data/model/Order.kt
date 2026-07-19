package com.sutec.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus { PROCESSING, SHIPPED, DELIVERED, CANCELLED }

// 確定注文。items はカートのスナップショット(以後カート変更の影響を受けない)。
@Serializable
data class Order(
    val id: String,
    val items: List<CartItem>,
    val totals: OrderTotals,
    val status: OrderStatus,
    val placedAt: String,
    val shippingAddress: Address,
    val paymentLabel: String,
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
}
