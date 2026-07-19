package com.sutec.mobile.server

import com.sutec.mobile.server.auth.Jwt
import com.sutec.mobile.server.db.Database
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8090
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    requireSecureConfig()
    Database.init()
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureValidation()
    configureCORS()
    configureRateLimit()
    configureSecurity()
    configureRouting()
}

// 本番(APP_ENV=production)で開発既定の JWT_SECRET のままだと起動を止める。
fun Application.requireSecureConfig() {
    if ((System.getenv("APP_ENV") ?: "dev") == "production") {
        val secret = System.getenv("JWT_SECRET")
        check(!secret.isNullOrBlank() && secret != Jwt.DEV_SECRET) {
            "JWT_SECRET must be set to a strong non-default value when APP_ENV=production"
        }
    }
}
