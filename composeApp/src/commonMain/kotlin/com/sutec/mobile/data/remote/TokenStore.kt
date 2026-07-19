package com.sutec.mobile.data.remote

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// JWT の唯一の保持先。Settings(Android=SharedPreferences / iOS=NSUserDefaults)で永続化し、
// 起動時に復元する。初期値に保存済みトークンを載せるため、購読する Remote 実装は起動時に
// そのトークンで自分のデータを復元できる。AuthRepository が set/clear、ApiClient が付与。
class TokenStore(private val settings: Settings) {
    private val _token = MutableStateFlow(settings.getStringOrNull(KEY))
    val token: StateFlow<String?> = _token

    fun set(value: String) {
        settings.putString(KEY, value)
        _token.value = value
    }

    fun clear() {
        settings.remove(KEY)
        _token.value = null
    }

    fun current(): String? = _token.value

    private companion object {
        const val KEY = "auth_token"
    }
}
