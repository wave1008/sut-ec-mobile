package com.sutec.mobile.data.repository.impl

import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.remote.TokenStore
import com.sutec.mobile.data.repository.AccountRepository
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 住所/支払いは token 変化で再取得。ミューテーションは背景でサーバー実行し、レスポンス(更新済み一覧)で
// StateFlow を確定する(id 採番はサーバーのため楽観 insert はしない)。getAddress/getPayment はキャッシュ参照。
class RemoteAccountRepository(
    private val api: ApiClient,
    tokenStore: TokenStore,
) : AccountRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    override val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    private val _payments = MutableStateFlow<List<PaymentMethod>>(emptyList())
    override val paymentMethods: StateFlow<List<PaymentMethod>> = _payments.asStateFlow()

    init {
        scope.launch {
            tokenStore.token.collect { token ->
                if (token != null) {
                    refreshAddresses()
                    refreshPayments()
                } else {
                    _addresses.value = emptyList()
                    _payments.value = emptyList()
                }
            }
        }
    }

    private suspend fun refreshAddresses() {
        runCatching { api.http.get("addresses").body<List<Address>>() }.onSuccess { _addresses.value = it }
    }

    private suspend fun refreshPayments() {
        runCatching { api.http.get("payment-methods").body<List<PaymentMethod>>() }.onSuccess { _payments.value = it }
    }

    override fun upsertAddress(address: Address) {
        scope.launch {
            runCatching {
                val resp: HttpResponse = if (address.id.isBlank()) {
                    api.http.post("addresses") { setBody(address) }
                } else {
                    api.http.put("addresses/${address.id}") { setBody(address) }
                }
                resp.body<List<Address>>()
            }.onSuccess { _addresses.value = it }
        }
    }

    override fun deleteAddress(id: String) {
        scope.launch {
            runCatching { api.http.delete("addresses/$id").body<List<Address>>() }.onSuccess { _addresses.value = it }
        }
    }

    override fun setDefaultAddress(id: String) {
        scope.launch {
            runCatching { api.http.post("addresses/$id/default").body<List<Address>>() }.onSuccess { _addresses.value = it }
        }
    }

    override fun getAddress(id: String): Address? = _addresses.value.firstOrNull { it.id == id }

    override fun upsertPayment(method: PaymentMethod) {
        scope.launch {
            runCatching {
                val resp: HttpResponse = if (method.id.isBlank()) {
                    api.http.post("payment-methods") { setBody(method) }
                } else {
                    api.http.put("payment-methods/${method.id}") { setBody(method) }
                }
                resp.body<List<PaymentMethod>>()
            }.onSuccess { _payments.value = it }
        }
    }

    override fun deletePayment(id: String) {
        scope.launch {
            runCatching { api.http.delete("payment-methods/$id").body<List<PaymentMethod>>() }.onSuccess { _payments.value = it }
        }
    }

    override fun setDefaultPayment(id: String) {
        scope.launch {
            runCatching { api.http.post("payment-methods/$id/default").body<List<PaymentMethod>>() }.onSuccess { _payments.value = it }
        }
    }

    override fun getPayment(id: String): PaymentMethod? = _payments.value.firstOrNull { it.id == id }
}
