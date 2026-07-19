package com.sutec.mobile.data.remote

import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.data.model.Product
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// 共有 HttpClient。エンジンはクラスパスから解決(Android=OkHttp / iOS=Darwin)。
// defaultRequest で base(/api/v1) と Bearer(TokenStore) を毎リクエスト付与する。
// サーバーは画像 imageUrls を相対(images/<f>.jpg)で返すため、resolve で絶対URL化して Coil に渡す。
class ApiClient(private val tokenStore: TokenStore) {
    val base: String = serverBaseUrl()

    val http: HttpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        }
        defaultRequest {
            url("$base/api/v1/")
            contentType(ContentType.Application.Json)
            tokenStore.current()?.let { bearerAuth(it) }
        }
    }

    fun imageUrl(path: String): String = if (path.startsWith("http")) path else "$base/$path"

    fun resolve(product: Product): Product =
        product.copy(imageUrls = product.imageUrls.map(::imageUrl))

    fun resolveItems(items: List<CartItem>): List<CartItem> =
        items.map { it.copy(product = resolve(it.product)) }

    fun resolveOrder(order: Order): Order =
        order.copy(items = resolveItems(order.items))
}
