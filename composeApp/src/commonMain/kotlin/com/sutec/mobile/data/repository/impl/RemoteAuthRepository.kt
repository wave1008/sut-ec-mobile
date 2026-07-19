package com.sutec.mobile.data.repository.impl

import com.sutec.mobile.data.dto.LoginRequest
import com.sutec.mobile.data.dto.SignupRequest
import com.sutec.mobile.data.dto.TokenResponse
import com.sutec.mobile.data.model.User
import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.remote.TokenStore
import com.sutec.mobile.data.repository.AuthRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 実認証。成功で TokenStore に JWT を格納 → 他 Remote 実装が購読して自分のデータを取得する。
// 永続化トークンがあれば起動時に /me でセッションを復元する(ログインしっぱなし)。
class RemoteAuthRepository(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
) : AuthRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // 保存済みトークンで /me を叩きセッション復元。401(無効/期限切れ)ならログアウト扱い。
        // ネットワーク不通など不確定失敗ではトークンを消さない(サーバー復帰後に使える)。
        if (tokenStore.current() != null) {
            scope.launch {
                val resp = runCatching { api.http.get("me") }.getOrNull()
                when {
                    resp == null -> Unit
                    resp.status == HttpStatusCode.Unauthorized -> tokenStore.clear()
                    resp.status.isSuccess() -> _currentUser.value = runCatching { resp.body<User>() }.getOrNull()
                    else -> Unit
                }
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val resp = api.http.post("auth/login") { setBody(LoginRequest(email, password)) }
        if (!resp.status.isSuccess()) error("login failed: ${resp.status}")
        val body = resp.body<TokenResponse>()
        tokenStore.set(body.token)
        _currentUser.value = body.user
        body.user
    }

    override suspend fun signup(name: String, email: String, password: String): Result<User> = runCatching {
        val resp = api.http.post("auth/signup") { setBody(SignupRequest(name, email, password)) }
        if (!resp.status.isSuccess()) error("signup failed: ${resp.status}")
        val body = resp.body<TokenResponse>()
        tokenStore.set(body.token)
        _currentUser.value = body.user
        body.user
    }

    override fun logout() {
        tokenStore.clear()
        _currentUser.value = null
    }
}
