package com.sutec.mobile.data.repository.impl

import com.sutec.mobile.data.dto.AddCartItemRequest
import com.sutec.mobile.data.dto.CartDto
import com.sutec.mobile.data.dto.SetQuantityRequest
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.OrderTotals
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.remote.TokenStore
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.computeOrderTotals
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ローカル StateFlow キャッシュ + 楽観更新。ミューテーションは非 suspend(UI 即時反映)で、
// 背景でサーバー同期しレスポンスでキャッシュを確定する。token が入るとサーバーから再取得。
class RemoteCartRepository(
    private val api: ApiClient,
    tokenStore: TokenStore,
) : CartRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    override val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    private val _count = MutableStateFlow(0)
    override val count: StateFlow<Int> = _count.asStateFlow()

    private val _totals = MutableStateFlow(computeOrderTotals(emptyList()))
    override val totals: StateFlow<OrderTotals> = _totals.asStateFlow()

    init {
        scope.launch {
            tokenStore.token.collect { token ->
                if (token != null) refresh() else applyLocal(emptyList())
            }
        }
    }

    private suspend fun refresh() {
        runCatching { api.http.get("cart").body<CartDto>() }.onSuccess { applyDto(it) }
    }

    private fun applyDto(dto: CartDto) = applyLocal(api.resolveItems(dto.items))

    private fun applyLocal(updated: List<CartItem>) {
        _items.value = updated
        _count.value = updated.sumOf { it.quantity }
        _totals.value = computeOrderTotals(updated)
    }

    override fun add(product: Product, quantity: Int) {
        val current = _items.value
        val updated = if (current.any { it.product.id == product.id }) {
            current.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + quantity) else it }
        } else {
            current + CartItem(product, quantity)
        }
        applyLocal(updated)
        scope.launch {
            runCatching { api.http.post("cart/items") { setBody(AddCartItemRequest(product.id, quantity)) }.body<CartDto>() }
                .onSuccess { applyDto(it) }
        }
    }

    override fun setQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            remove(productId)
            return
        }
        applyLocal(_items.value.map { if (it.product.id == productId) it.copy(quantity = quantity) else it })
        scope.launch {
            runCatching { api.http.patch("cart/items/$productId") { setBody(SetQuantityRequest(quantity)) }.body<CartDto>() }
                .onSuccess { applyDto(it) }
        }
    }

    override fun remove(productId: String) {
        applyLocal(_items.value.filterNot { it.product.id == productId })
        scope.launch {
            runCatching { api.http.delete("cart/items/$productId").body<CartDto>() }.onSuccess { applyDto(it) }
        }
    }

    override fun clear() {
        applyLocal(emptyList())
        scope.launch {
            runCatching { api.http.delete("cart").body<CartDto>() }.onSuccess { applyDto(it) }
        }
    }
}
