package com.sutec.mobile.server

import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.repository.FREE_SHIPPING_THRESHOLD_YEN
import com.sutec.mobile.data.repository.SHIPPING_FEE_YEN
import com.sutec.mobile.data.repository.computeOrderTotals
import kotlin.test.Test
import kotlin.test.assertEquals

// 注文確定の権威計算(computeOrderTotals)の規則を固定。API とアプリで同一実装(:shared)。
class OrderTotalsTest {
    private fun product(id: String, price: Int) = Product(
        id = id, nameJa = id, nameEn = id, brandJa = "", brandEn = "",
        descriptionJa = "", descriptionEn = "", priceYen = price, categoryId = "c",
        imageUrls = emptyList(), rating = 0.0, reviewCount = 0,
    )

    @Test fun emptyCartHasZeroShipping() {
        val t = computeOrderTotals(emptyList())
        assertEquals(0, t.subtotalYen); assertEquals(0, t.shippingYen); assertEquals(0, t.taxYen); assertEquals(0, t.totalYen)
    }

    @Test fun belowThresholdChargesFlatShipping() {
        val t = computeOrderTotals(listOf(CartItem(product("a", FREE_SHIPPING_THRESHOLD_YEN - 1), 1)))
        assertEquals(SHIPPING_FEE_YEN, t.shippingYen)
        assertEquals(FREE_SHIPPING_THRESHOLD_YEN - 1 + SHIPPING_FEE_YEN, t.totalYen)
    }

    @Test fun atOrAboveThresholdIsFreeShipping() {
        val t = computeOrderTotals(listOf(CartItem(product("a", FREE_SHIPPING_THRESHOLD_YEN), 1)))
        assertEquals(0, t.shippingYen)
        assertEquals(FREE_SHIPPING_THRESHOLD_YEN, t.totalYen)
    }

    @Test fun taxIsAlwaysZeroTaxIncludedPricing() {
        val t = computeOrderTotals(listOf(CartItem(product("a", 1000), 2)))
        assertEquals(0, t.taxYen)
        assertEquals(2000, t.subtotalYen)
    }
}
