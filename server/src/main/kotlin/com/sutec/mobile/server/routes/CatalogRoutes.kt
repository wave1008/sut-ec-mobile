package com.sutec.mobile.server.routes

import com.sutec.mobile.data.dto.PageResponse
import com.sutec.mobile.data.model.ProductTag
import com.sutec.mobile.data.repository.SearchQuery
import com.sutec.mobile.data.repository.SortOption
import com.sutec.mobile.server.repository.CatalogRepository
import io.ktor.server.application.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.*
import io.ktor.server.routing.*

// /products/featured と /products/by-ids は /products/{id} より前に置く(Ktor は定数優先だが明示)。
fun Route.catalogRoutes() {
    get("/categories") { call.respond(CatalogRepository.getCategories()) }

    get("/products/featured") { call.respond(CatalogRepository.getFeatured()) }

    get("/products/by-ids") {
        val ids = call.request.queryParameters["ids"]
            ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        call.respond(CatalogRepository.getProductsByIds(ids))
    }

    get("/products") {
        val p = call.request.queryParameters
        val query = SearchQuery(
            text = p["text"],
            categoryId = p["categoryId"],
            minPriceYen = p["minPriceYen"]?.toIntOrNull(),
            maxPriceYen = p["maxPriceYen"]?.toIntOrNull(),
            tag = p["tag"]?.let { runCatching { ProductTag.valueOf(it) }.getOrNull() },
            sort = p["sort"]?.let { runCatching { SortOption.valueOf(it) }.getOrNull() } ?: SortOption.RELEVANCE,
        )
        val page = (p["page"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
        val pageSize = (p["pageSize"]?.toIntOrNull() ?: 20).coerceIn(1, 100)
        val all = CatalogRepository.search(query)
        val items = all.drop(page * pageSize).take(pageSize)
        call.respond(PageResponse(items, page, pageSize, all.size))
    }

    get("/products/{id}") {
        val id = call.parameters["id"]!!
        val product = CatalogRepository.getProduct(id) ?: throw NotFoundException("product not found: $id")
        call.respond(product)
    }

    get("/products/{id}/reviews") {
        call.respond(CatalogRepository.getReviews(call.parameters["id"]!!))
    }

    get("/products/{id}/related") {
        call.respond(CatalogRepository.getRelated(call.parameters["id"]!!))
    }
}
