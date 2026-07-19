package com.sutec.mobile.data.repository.impl

import com.sutec.mobile.data.dto.PageResponse
import com.sutec.mobile.data.model.Category
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.model.Review
import com.sutec.mobile.data.remote.ApiClient
import com.sutec.mobile.data.repository.ProductRepository
import com.sutec.mobile.data.repository.SearchQuery
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

// カタログは公開エンドポイント(認証不要)。imageUrls はサーバー相対 → resolve で絶対URL化。
class RemoteProductRepository(private val api: ApiClient) : ProductRepository {

    override suspend fun getCategories(): List<Category> =
        api.http.get("categories").body()

    override suspend fun getFeatured(): List<Product> =
        api.http.get("products/featured").body<List<Product>>().map(api::resolve)

    override suspend fun getProducts(query: SearchQuery): List<Product> {
        val resp = api.http.get("products") {
            query.text?.let { parameter("text", it) }
            query.categoryId?.let { parameter("categoryId", it) }
            query.minPriceYen?.let { parameter("minPriceYen", it) }
            query.maxPriceYen?.let { parameter("maxPriceYen", it) }
            query.tag?.let { parameter("tag", it.name) }
            parameter("sort", query.sort.name)
            parameter("pageSize", 100)
        }.body<PageResponse<Product>>()
        return resp.items.map(api::resolve)
    }

    override suspend fun getProductsByCategory(categoryId: String): List<Product> =
        getProducts(SearchQuery(categoryId = categoryId))

    // 404 は null(商品なし)。ネットワーク/その他エラーは例外を伝播し、画面側で再試行できるようにする。
    override suspend fun getProduct(id: String): Product? {
        val resp = api.http.get("products/$id")
        if (resp.status == HttpStatusCode.NotFound) return null
        check(resp.status.isSuccess()) { "getProduct failed: ${resp.status}" }
        return api.resolve(resp.body<Product>())
    }

    override suspend fun getProductsByIds(ids: List<String>): List<Product> {
        if (ids.isEmpty()) return emptyList()
        return api.http.get("products/by-ids") {
            parameter("ids", ids.joinToString(","))
        }.body<List<Product>>().map(api::resolve)
    }

    override suspend fun getReviews(productId: String): List<Review> =
        api.http.get("products/$productId/reviews").body()

    override suspend fun getRelated(productId: String): List<Product> =
        api.http.get("products/$productId/related").body<List<Product>>().map(api::resolve)
}
