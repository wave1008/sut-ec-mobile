package com.sutec.mobile.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

// CORS_ORIGINS(カンマ区切り, 例 "https://app.example.com,https://admin.example.com")が
// 指定されればそのオリジンに限定。未指定なら開発用に anyHost。
fun Application.configureCORS() {
    val origins = System.getenv("CORS_ORIGINS")
        ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    install(CORS) {
        if (origins.isEmpty()) {
            anyHost()
        } else {
            origins.forEach { origin ->
                val parts = origin.split("://", limit = 2)
                if (parts.size == 2) allowHost(parts[1], schemes = listOf(parts[0])) else allowHost(origin)
            }
        }
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }
}
