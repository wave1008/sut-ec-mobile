package com.sutec.mobile.data.dto

import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.OrderTotals
import kotlinx.serialization.Serializable

// GET /cart。count は items から導出可能なので持たない。
@Serializable
data class CartDto(val items: List<CartItem>, val totals: OrderTotals)

@Serializable
data class AddCartItemRequest(val productId: String, val quantity: Int = 1)

// ゲストカート統合。未ログイン時のローカルカートをログイン時にサーバーへ加算マージする。
@Serializable
data class MergeCartRequest(val items: List<AddCartItemRequest>)

@Serializable
data class SetQuantityRequest(val quantity: Int)

// POST /orders。金額・items はサーバーがカートから権威決定するため送らない。
@Serializable
data class PlaceOrderRequest(val addressId: String, val paymentMethodId: String)
