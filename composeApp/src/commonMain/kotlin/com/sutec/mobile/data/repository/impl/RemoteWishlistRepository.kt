package com.sutec.mobile.data.repository.impl

import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.remote.TokenStore
import com.sutec.mobile.data.repository.WishlistRepository
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// toggle/remove は楽観更新 + 背景同期。token が入るとサーバーから再取得。
class RemoteWishlistRepository(
    private val api: ApiClient,
    tokenStore: TokenStore,
) : WishlistRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _ids = MutableStateFlow<Set<String>>(emptySet())
    override val productIds: StateFlow<Set<String>> = _ids.asStateFlow()

    init {
        scope.launch {
            tokenStore.token.collect { token ->
                if (token != null) refresh() else _ids.value = emptySet()
            }
        }
    }

    private suspend fun refresh() {
        runCatching { api.http.get("wishlist").body<List<String>>() }.onSuccess { _ids.value = it.toSet() }
    }

    override fun toggle(productId: String) {
        val current = _ids.value
        if (productId in current) {
            _ids.value = current - productId
            scope.launch {
                runCatching { api.http.delete("wishlist/$productId").body<List<String>>() }.onSuccess { _ids.value = it.toSet() }
            }
        } else {
            _ids.value = current + productId
            scope.launch {
                runCatching { api.http.put("wishlist/$productId").body<List<String>>() }.onSuccess { _ids.value = it.toSet() }
            }
        }
    }

    override fun remove(productId: String) {
        _ids.value = _ids.value - productId
        scope.launch {
            runCatching { api.http.delete("wishlist/$productId").body<List<String>>() }.onSuccess { _ids.value = it.toSet() }
        }
    }
}
