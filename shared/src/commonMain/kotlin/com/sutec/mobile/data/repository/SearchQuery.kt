package com.sutec.mobile.data.repository

import com.sutec.mobile.data.model.ProductTag

enum class SortOption { RELEVANCE, PRICE_ASC, PRICE_DESC, RATING, NEWEST }

data class SearchQuery(
    val text: String? = null,
    val categoryId: String? = null,
    val minPriceYen: Int? = null,
    val maxPriceYen: Int? = null,
    val tag: ProductTag? = null,
    val sort: SortOption = SortOption.RELEVANCE,
)
