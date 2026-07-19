package com.sutec.mobile.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// JWT の唯一の保持先。ApiClient が付与、AuthRepository が set/clear、
// cart/wishlist/orders/account の Remote 実装が変化を購読して再取得/クリアする。
// 永続化なし(プロセス終了で消える。将来 multiplatform-settings で永続化)。
class TokenStore {
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    fun set(value: String) { _token.value = value }
    fun clear() { _token.value = null }
    fun current(): String? = _token.value
}
