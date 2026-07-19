package com.sutec.mobile.data.repository.impl

import com.sutec.mobile.data.dto.PlaceOrderRequest
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.remote.TokenStore
import com.sutec.mobile.data.repository.OrderRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// orders は token 変化で再取得。placeOrder はサーバーが金額を権威計算し、確定注文を返す。
class RemoteOrderRepository(
    private val api: ApiClient,
    tokenStore: TokenStore,
) : OrderRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    init {
        scope.launch {
            tokenStore.token.collect { token ->
                if (token != null) refresh() else _orders.value = emptyList()
            }
        }
    }

    private suspend fun refresh() {
        runCatching { api.http.get("orders").body<List<Order>>() }
            .onSuccess { list -> _orders.value = list.map(api::resolveOrder) }
    }

    override suspend fun placeOrder(addressId: String, paymentMethodId: String): Order {
        val order = api.http.post("orders") {
            setBody(PlaceOrderRequest(addressId, paymentMethodId))
        }.body<Order>().let(api::resolveOrder)
        _orders.value = listOf(order) + _orders.value
        return order
    }

    override fun getOrder(id: String): Order? = _orders.value.firstOrNull { it.id == id }
}
