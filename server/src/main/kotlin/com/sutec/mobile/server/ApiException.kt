package com.sutec.mobile.server

import io.ktor.http.*

// ドメインエラーの統一表現。StatusPages が status + ErrorBody(code,message) にマップする。
class ApiException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
) : RuntimeException(message)
