package com.sutec.mobile.server

import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

// 認証エンドポイント(signup/login/me/logout)の総当り対策。IP 単位で毎分制限。
// 上限は RATE_LIMIT_PER_MIN で調整(既定60)。Routing で rateLimit(AuthRateLimit){ authRoutes() } と囲む。
val AuthRateLimit = RateLimitName("auth")

fun Application.configureRateLimit() {
    val perMinute = System.getenv("RATE_LIMIT_PER_MIN")?.toIntOrNull() ?: 60
    install(RateLimit) {
        register(AuthRateLimit) {
            rateLimiter(limit = perMinute, refillPeriod = 60.seconds)
            requestKey { call -> call.request.origin.remoteHost }
        }
    }
}
