package com.sutec.mobile.server.seed

import com.sutec.mobile.server.db.Categories
import com.sutec.mobile.server.db.ProductImages
import com.sutec.mobile.server.db.ProductTags
import com.sutec.mobile.server.db.Products
import com.sutec.mobile.server.db.Reviews
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

// 冪等: products が空のときだけ CatalogSeed を投入。Flyway migrate 後・Exposed connect 後に呼ぶ。
object SeedRunner {
    fun seedIfEmpty() = transaction {
        if (Products.selectAll().count() > 0L) return@transaction

        CatalogSeed.categories.forEachIndexed { idx, c ->
            Categories.insert {
                it[id] = c.id
                it[nameJa] = c.nameJa
                it[nameEn] = c.nameEn
                it[emoji] = c.emoji
                it[sortOrder] = idx
            }
        }
        // seq は宣言順を保持(検索/featured の安定ソートの tie-break が client mock と一致)。
        CatalogSeed.products.forEachIndexed { idx, p ->
            Products.insert {
                it[id] = p.id
                it[seq] = idx
                it[nameJa] = p.nameJa
                it[nameEn] = p.nameEn
                it[brandJa] = p.brandJa
                it[brandEn] = p.brandEn
                it[descriptionJa] = p.descriptionJa
                it[descriptionEn] = p.descriptionEn
                it[priceYen] = p.priceYen
                it[listPriceYen] = p.listPriceYen
                it[categoryId] = p.categoryId
                it[rating] = p.rating
                it[reviewCount] = p.reviewCount
                it[inStock] = p.inStock
            }
            p.imageUrls.forEachIndexed { pos, url ->
                ProductImages.insert {
                    it[productId] = p.id
                    it[urlPath] = url
                    it[position] = pos
                }
            }
            p.tags.forEach { t ->
                ProductTags.insert {
                    it[productId] = p.id
                    it[tag] = t.name
                }
            }
        }
        CatalogSeed.reviews.forEach { r ->
            Reviews.insert {
                it[id] = r.id
                it[productId] = r.productId
                it[authorName] = r.authorName
                it[rating] = r.rating
                it[titleJa] = r.titleJa
                it[titleEn] = r.titleEn
                it[bodyJa] = r.bodyJa
                it[bodyEn] = r.bodyEn
                it[date] = r.date
            }
        }
    }
}
