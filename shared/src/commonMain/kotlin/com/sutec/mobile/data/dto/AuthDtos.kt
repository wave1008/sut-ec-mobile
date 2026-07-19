package com.sutec.mobile.data.dto

import com.sutec.mobile.data.model.User
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(val name: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

// 認証成功時に返す。token は JWT(Bearer)。
@Serializable
data class TokenResponse(val token: String, val user: User)
