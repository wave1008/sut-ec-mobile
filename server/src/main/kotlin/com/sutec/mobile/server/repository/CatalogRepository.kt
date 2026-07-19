package com.sutec.mobile.server.repository

import com.sutec.mobile.data.model.Category
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.model.ProductTag
import com.sutec.mobile.data.model.Review
import com.sutec.mobile.data.repository.SearchQuery
import com.sutec.mobile.data.repository.SortOption
import com.sutec.mobile.server.db.Categories
import com.sutec.mobile.server.db.ProductImages
import com.sutec.mobile.server.db.ProductTags
import com.sutec.mobile.server.db.Products
import com.sutec.mobile.server.db.Reviews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

// フィルタ/並替ロジックは composeApp の InMemoryProductRepository に厳密一致させる(結果の齟齬防止)。
// データ小規模のため全件ロード→Kotlin でフィルタ/ソート。
object CatalogRepository {

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }

    suspend fun getCategories(): List<Category> = dbQuery {
        Categories.selectAll().orderBy(Categories.sortOrder to SortOrder.ASC).map {
            Category(it[Categories.id], it[Categories.nameJa], it[Categories.nameEn], it[Categories.emoji])
        }
    }

    // seq 昇順で宣言順を再現(検索/featured の安定ソートの tie-break を mock と一致させる)。
    // internal: cart/order の store が自分の transaction 内から再利用する(dbQuery を重ねない)。
    internal fun productsInTx(): List<Product> = loadAllProducts()

    private fun loadAllProducts(): List<Product> {
        val imagesByProduct = ProductImages.selectAll()
            .orderBy(ProductImages.position to SortOrder.ASC)
            .groupBy({ it[ProductImages.productId] }, { it[ProductImages.urlPath] })
        val tagsByProduct = ProductTags.selectAll()
            .groupBy({ it[ProductTags.productId] }, { ProductTag.valueOf(it[ProductTags.tag]) })
        return Products.selectAll().orderBy(Products.seq to SortOrder.ASC).map { row ->
            val pid = row[Products.id]
            Product(
                id = pid,
                nameJa = row[Products.nameJa], nameEn = row[Products.nameEn],
                brandJa = row[Products.brandJa], brandEn = row[Products.brandEn],
                descriptionJa = row[Products.descriptionJa], descriptionEn = row[Products.descriptionEn],
                priceYen = row[Products.priceYen], listPriceYen = row[Products.listPriceYen],
                categoryId = row[Products.categoryId],
                imageUrls = imagesByProduct[pid] ?: emptyList(),
                rating = row[Products.rating], reviewCount = row[Products.reviewCount],
                inStock = row[Products.inStock],
                tags = tagsByProduct[pid] ?: emptyList(),
            )
        }
    }

    suspend fun getFeatured(): List<Product> = dbQuery {
        val all = loadAllProducts()
        val tagged = all.filter { it.tags.isNotEmpty() }.sortedByDescending { it.rating }
        (tagged + all.sortedByDescending { it.rating }).distinctBy { it.id }.take(12)
    }

    suspend fun search(query: SearchQuery): List<Product> = dbQuery {
        var result = loadAllProducts().asSequence()
        query.text?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()?.let { needle ->
            result = result.filter { p ->
                p.nameJa.lowercase().contains(needle) ||
                    p.nameEn.lowercase().contains(needle) ||
                    p.brandJa.lowercase().contains(needle) ||
                    p.brandEn.lowercase().contains(needle)
            }
        }
        query.categoryId?.let { c -> result = result.filter { it.categoryId == c } }
        query.minPriceYen?.let { min -> result = result.filter { it.priceYen >= min } }
        query.maxPriceYen?.let { max -> result = result.filter { it.priceYen <= max } }
        query.tag?.let { tag -> result = result.filter { tag in it.tags } }
        val list = result.toList()
        when (query.sort) {
            SortOption.RELEVANCE -> list
            SortOption.PRICE_ASC -> list.sortedBy { it.priceYen }
            SortOption.PRICE_DESC -> list.sortedByDescending { it.priceYen }
            SortOption.RATING -> list.sortedByDescending { it.rating }
            SortOption.NEWEST -> list.sortedByDescending { ProductTag.NEW in it.tags }
        }
    }

    suspend fun getProduct(id: String): Product? = dbQuery {
        loadAllProducts().firstOrNull { it.id == id }
    }

    suspend fun getProductsByIds(ids: List<String>): List<Product> = dbQuery {
        val byId = loadAllProducts().associateBy { it.id }
        ids.mapNotNull { byId[it] }
    }

    suspend fun getReviews(productId: String): List<Review> = dbQuery {
        Reviews.selectAll().where { Reviews.productId eq productId }.map {
            Review(
                it[Reviews.id], it[Reviews.productId], it[Reviews.authorName], it[Reviews.rating],
                it[Reviews.titleJa], it[Reviews.titleEn], it[Reviews.bodyJa], it[Reviews.bodyEn], it[Reviews.date],
            )
        }
    }

    suspend fun getRelated(productId: String): List<Product> = dbQuery {
        val all = loadAllProducts()
        val target = all.firstOrNull { it.id == productId } ?: return@dbQuery emptyList()
        all.filter { it.categoryId == target.categoryId && it.id != productId }.take(6)
    }
}
