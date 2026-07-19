package com.sutec.mobile.server.db

import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.Product
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb

// Flyway(V2__schema.sql)がスキーマの正本。ここは Exposed マッピングで SQL と列名・型を一致させる。
internal val dbJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

object Categories : Table("categories") {
    val id = text("id")
    val nameJa = text("name_ja")
    val nameEn = text("name_en")
    val emoji = text("emoji")
    val sortOrder = integer("sort_order")
    override val primaryKey = PrimaryKey(id)
}

object Products : Table("products") {
    val id = text("id")
    val seq = integer("seq")
    val nameJa = text("name_ja")
    val nameEn = text("name_en")
    val brandJa = text("brand_ja")
    val brandEn = text("brand_en")
    val descriptionJa = text("description_ja")
    val descriptionEn = text("description_en")
    val priceYen = integer("price_yen")
    val listPriceYen = integer("list_price_yen").nullable()
    val categoryId = text("category_id")
    val rating = double("rating")
    val reviewCount = integer("review_count")
    val inStock = bool("in_stock")
    override val primaryKey = PrimaryKey(id)
}

object ProductImages : Table("product_images") {
    val id = long("id").autoIncrement()
    val productId = text("product_id")
    val urlPath = text("url_path")
    val position = integer("position")
    override val primaryKey = PrimaryKey(id)
}

object ProductTags : Table("product_tags") {
    val productId = text("product_id")
    val tag = text("tag")
    override val primaryKey = PrimaryKey(productId, tag)
}

object Reviews : Table("reviews") {
    val id = text("id")
    val productId = text("product_id")
    val authorName = text("author_name")
    val rating = integer("rating")
    val titleJa = text("title_ja")
    val titleEn = text("title_en")
    val bodyJa = text("body_ja")
    val bodyEn = text("body_en")
    val date = text("date")
    override val primaryKey = PrimaryKey(id)
}

object Users : Table("users") {
    val id = uuid("id")
    val name = text("name")
    val email = text("email")
    val passwordHash = text("password_hash")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Addresses : Table("addresses") {
    val id = text("id")
    val userId = uuid("user_id")
    val fullName = text("full_name")
    val postalCode = text("postal_code")
    val prefecture = text("prefecture")
    val city = text("city")
    val line1 = text("line1")
    val line2 = text("line2")
    val phone = text("phone")
    val isDefault = bool("is_default")
    override val primaryKey = PrimaryKey(id)
}

object PaymentMethods : Table("payment_methods") {
    val id = text("id")
    val userId = uuid("user_id")
    val type = text("type")
    val brand = text("brand")
    val last4 = text("last4")
    val holderName = text("holder_name")
    val expMonth = integer("exp_month")
    val expYear = integer("exp_year")
    val isDefault = bool("is_default")
    override val primaryKey = PrimaryKey(id)
}

object Carts : Table("carts") {
    val userId = uuid("user_id")
    override val primaryKey = PrimaryKey(userId)
}

object CartItems : Table("cart_items") {
    val cartUserId = uuid("cart_user_id")
    val productId = text("product_id")
    val quantity = integer("quantity")
    override val primaryKey = PrimaryKey(cartUserId, productId)
}

object WishlistItems : Table("wishlist_items") {
    val userId = uuid("user_id")
    val productId = text("product_id")
    override val primaryKey = PrimaryKey(userId, productId)
}

object Orders : Table("orders") {
    val id = text("id")
    val userId = uuid("user_id")
    val status = text("status")
    val placedAt = text("placed_at")
    val createdAt = timestamp("created_at")
    val subtotalYen = integer("subtotal_yen")
    val shippingYen = integer("shipping_yen")
    val taxYen = integer("tax_yen")
    val shippingAddress = jsonb<Address>("shipping_address_json", dbJson)
    val paymentLabel = text("payment_label")
    override val primaryKey = PrimaryKey(id)
}

object OrderItems : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = text("order_id")
    val position = integer("position")
    val product = jsonb<Product>("product_json", dbJson)
    val quantity = integer("quantity")
    override val primaryKey = PrimaryKey(id)
}
