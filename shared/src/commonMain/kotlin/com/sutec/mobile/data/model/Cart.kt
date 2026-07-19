package com.sutec.mobile.data.model

import kotlinx.serialization.Serializable

// カート明細。product はスナップショットではなくカタログ参照(モックのため価格改定は考慮しない)。
@Serializable
data class CartItem(
    val product: Product,
    val quantity: Int,
) {
    val lineTotalYen: Int get() = product.priceYen * quantity
}

// 金額内訳。checkout / order で共有。shipping/tax の算出は CartRepository が担う。
@Serializable
data class OrderTotals(
    val subtotalYen: Int,
    val shippingYen: Int,
    val taxYen: Int,
) {
    val totalYen: Int get() = subtotalYen + shippingYen + taxYen
}
