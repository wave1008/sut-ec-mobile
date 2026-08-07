package com.sutec.mobile.data.repository.impl

import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.remote.TokenStore
import com.sutec.mobile.data.repository.AccountRepository
import com.sutec.mobile.data.repository.UnauthorizedException
import com.sutec.mobile.util.remoteScopeExceptionHandler
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 住所/支払いは token 変化で再取得。ミューテーションは呼び出し元コンテキストで実行し、
// レスポンス(更新済み一覧)で StateFlow を確定する(id 採番はサーバーのため楽観 insert はしない)。
// getAddress/getPayment はキャッシュ参照。
class RemoteAccountRepository(
    private val api: ApiClient,
    tokenStore: TokenStore,
) : AccountRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + remoteScopeExceptionHandler)

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

    // resp.status を確認して例外化してから body デシリアライズする(expectSuccess=false なので
    // 素通しだと 401 が List のデシリアライズ失敗として握りつぶされる)。
    private suspend fun checkStatus(resp: HttpResponse) {
        if (resp.status == HttpStatusCode.Unauthorized) throw UnauthorizedException()
        if (!resp.status.isSuccess()) error("request failed: ${resp.status}")
    }

    override suspend fun upsertAddress(address: Address): Result<Unit> = runCatching {
        val resp: HttpResponse = if (address.id.isBlank()) {
            api.http.post("addresses") { setBody(address) }
        } else {
            api.http.put("addresses/${address.id}") { setBody(address) }
        }
        checkStatus(resp)
        _addresses.value = resp.body<List<Address>>()
    }

    override suspend fun deleteAddress(id: String): Result<Unit> = runCatching {
        val resp = api.http.delete("addresses/$id")
        checkStatus(resp)
        _addresses.value = resp.body<List<Address>>()
    }

    override suspend fun setDefaultAddress(id: String): Result<Unit> = runCatching {
        val resp = api.http.post("addresses/$id/default")
        checkStatus(resp)
        _addresses.value = resp.body<List<Address>>()
    }

    override fun getAddress(id: String): Address? = _addresses.value.firstOrNull { it.id == id }

    override suspend fun upsertPayment(method: PaymentMethod): Result<Unit> = runCatching {
        val resp: HttpResponse = if (method.id.isBlank()) {
            api.http.post("payment-methods") { setBody(method) }
        } else {
            api.http.put("payment-methods/${method.id}") { setBody(method) }
        }
        checkStatus(resp)
        _payments.value = resp.body<List<PaymentMethod>>()
    }

    override suspend fun deletePayment(id: String): Result<Unit> = runCatching {
        val resp = api.http.delete("payment-methods/$id")
        checkStatus(resp)
        _payments.value = resp.body<List<PaymentMethod>>()
    }

    override suspend fun setDefaultPayment(id: String): Result<Unit> = runCatching {
        val resp = api.http.post("payment-methods/$id/default")
        checkStatus(resp)
        _payments.value = resp.body<List<PaymentMethod>>()
    }

    override fun getPayment(id: String): PaymentMethod? = _payments.value.firstOrNull { it.id == id }
}
