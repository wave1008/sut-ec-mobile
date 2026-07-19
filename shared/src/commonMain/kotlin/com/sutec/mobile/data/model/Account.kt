package com.sutec.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
)

@Serializable
data class Address(
    val id: String,
    val fullName: String,
    val postalCode: String,
    val prefecture: String,
    val city: String,
    val line1: String,
    val line2: String = "",
    val phone: String,
    val isDefault: Boolean = false,
)

@Serializable
enum class PaymentType { CARD, CASH_ON_DELIVERY }

@Serializable
data class PaymentMethod(
    val id: String,
    val type: PaymentType,
    // CARD のときのみ意味を持つ。
    val brand: String = "",
    val last4: String = "",
    val holderName: String = "",
    val expMonth: Int = 0,
    val expYear: Int = 0,
    val isDefault: Boolean = false,
)
