package com.sutec.mobile.data.repository.impl

import com.sutec.mobile.data.dto.LoginRequest
import com.sutec.mobile.data.dto.SignupRequest
import com.sutec.mobile.data.dto.TokenResponse
import com.sutec.mobile.data.model.User
import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.remote.TokenStore
import com.sutec.mobile.data.repository.AuthRepository
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 実認証。成功で TokenStore に JWT を格納 → 他 Remote 実装が購読して自分のデータを取得する。
class RemoteAuthRepository(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
) : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

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
