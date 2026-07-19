package com.sutec.mobile.server

import com.sutec.mobile.server.db.Database
import com.sutec.mobile.server.routes.accountRoutes
import com.sutec.mobile.server.routes.authRoutes
import com.sutec.mobile.server.routes.cartRoutes
import com.sutec.mobile.server.routes.catalogRoutes
import com.sutec.mobile.server.routes.orderRoutes
import com.sutec.mobile.server.routes.wishlistRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import kotlinx.serialization.Serializable

@Serializable data class Health(val status: String, val db: String)

fun Application.configureRouting() {
    // 画像実体: mock-server/images/<id>-<n>.jpg。:server:run の作業ディレクトリはルート。
    // ディレクトリが無い場合は登録しない(テストや誤設定時の起動失敗を避ける)。
    val imagesDir = File(System.getProperty("IMAGES_DIR") ?: System.getenv("IMAGES_DIR") ?: "mock-server/images")
    routing {
        if (imagesDir.isDirectory) staticFiles("/images", imagesDir)

        get("/health") {
            val dbUp = runCatching {
                Database.dataSource.connection.use { c ->
                    c.createStatement().use { st ->
                        st.executeQuery("SELECT 1").use { rs -> rs.next() }
                    }
                }
            }.getOrDefault(false)
            val code = if (dbUp) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
            call.respond(code, Health(status = if (dbUp) "ok" else "degraded", db = if (dbUp) "up" else "down"))
        }

        route("/api/v1") {
            catalogRoutes()
            rateLimit(AuthRateLimit) {
                authRoutes()
            }
            authenticate("auth-jwt") {
                cartRoutes()
                wishlistRoutes()
                orderRoutes()
                accountRoutes()
            }
        }
    }
}
