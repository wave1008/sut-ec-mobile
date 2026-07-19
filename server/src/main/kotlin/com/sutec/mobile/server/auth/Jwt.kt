package com.sutec.mobile.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.Date
import java.util.UUID

// JWT_SECRET は本番で必ず環境変数指定(既定は開発用)。uid クレームに userId(uuid) を格納。
object Jwt {
    private val secret = System.getenv("JWT_SECRET") ?: "dev-secret-change-me"
    val issuer = System.getenv("JWT_ISSUER") ?: "sut-ec-mobile"
    val audience = System.getenv("JWT_AUDIENCE") ?: "sut-ec-mobile-app"
    const val realm = "sut-ec-mobile"
    private val ttlMs = (System.getenv("JWT_TTL_DAYS")?.toLongOrNull() ?: 30L) * 24 * 60 * 60 * 1000
    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun sign(userId: String): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("uid", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + ttlMs))
        .sign(algorithm)
}

// authenticate("auth-jwt") ブロック内でのみ有効。principal が無い/不正なら例外(=通常到達しない)。
fun ApplicationCall.userId(): UUID {
    val principal = principal<JWTPrincipal>() ?: error("missing JWT principal")
    return UUID.fromString(principal.payload.getClaim("uid").asString())
}
