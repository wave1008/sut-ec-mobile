package com.sutec.mobile.data.model

import com.sutec.mobile.i18n.AppLanguage
import kotlinx.serialization.Serializable

// 商品。文言は日英を両持ちし name(lang)/description(lang) で解決(i18n の tr と同思想)。
// 価格は円の整数(minor unit なし)。listPriceYen があれば割引の取消線に使う。
@Serializable
data class Product(
    val id: String,
    val nameJa: String,
    val nameEn: String,
    val brandJa: String,
    val brandEn: String,
    val descriptionJa: String,
    val descriptionEn: String,
    val priceYen: Int,
    val listPriceYen: Int? = null,
    val categoryId: String,
    val imageUrls: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val inStock: Boolean = true,
    val tags: List<ProductTag> = emptyList(),
) {
    fun name(lang: AppLanguage) = if (lang == AppLanguage.JA) nameJa else nameEn
    fun brand(lang: AppLanguage) = if (lang == AppLanguage.JA) brandJa else brandEn
    fun description(lang: AppLanguage) = if (lang == AppLanguage.JA) descriptionJa else descriptionEn
    val discountPercent: Int?
        get() = listPriceYen?.takeIf { it > priceYen }?.let { ((it - priceYen) * 100) / it }
}

// バッジ表示用のタグ。UI 側で色/ラベルを解決。
@Serializable
enum class ProductTag { PRIME, BESTSELLER, NEW, SALE, LOW_STOCK }

@Serializable
data class Category(
    val id: String,
    val nameJa: String,
    val nameEn: String,
    // Material Icons 名ではなく絵文字。resource 同梱を避けつつ一覧を華やかにする。
    val emoji: String,
) {
    fun name(lang: AppLanguage) = if (lang == AppLanguage.JA) nameJa else nameEn
}

@Serializable
data class Review(
    val id: String,
    val productId: String,
    val authorName: String,
    val rating: Int,
    val titleJa: String,
    val titleEn: String,
    val bodyJa: String,
    val bodyEn: String,
    val date: String,
) {
    fun title(lang: AppLanguage) = if (lang == AppLanguage.JA) titleJa else titleEn
    fun body(lang: AppLanguage) = if (lang == AppLanguage.JA) bodyJa else bodyEn
}
