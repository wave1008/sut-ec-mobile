package com.sutec.mobile.data.repository

import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.OrderTotals

// 金額計算の唯一の実装。cart / checkout / order が必ずこれを使う(表示の齟齬を防ぐ)。
// 規則: 価格は税込のため tax=0。送料は小計>=FREE_SHIPPING_THRESHOLD で無料、それ未満は SHIPPING_FEE。空カートは送料0。
const val FREE_SHIPPING_THRESHOLD_YEN = 3000
const val SHIPPING_FEE_YEN = 500

fun computeOrderTotals(items: List<CartItem>): OrderTotals {
    val subtotal = items.sumOf { it.lineTotalYen }
    val shipping = when {
        subtotal <= 0 -> 0
        subtotal >= FREE_SHIPPING_THRESHOLD_YEN -> 0
        else -> SHIPPING_FEE_YEN
    }
    return OrderTotals(subtotalYen = subtotal, shippingYen = shipping, taxYen = 0)
}
