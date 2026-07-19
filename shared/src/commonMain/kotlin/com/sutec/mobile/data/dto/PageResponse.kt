package com.sutec.mobile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
)
