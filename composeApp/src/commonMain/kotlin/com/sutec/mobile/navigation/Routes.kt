package com.sutec.mobile.navigation

import kotlinx.serialization.Serializable

// 型安全ルート(navigation-compose の @Serializable ルート)。
// 下タブの根: Home / Search / Cart / Wishlist / Account。
@Serializable object HomeRoute
@Serializable data class CatalogRoute(val categoryId: String? = null)
@Serializable data class SearchRoute(val initialQuery: String? = null)
@Serializable data class ProductDetailRoute(val productId: String)
@Serializable object CartRoute
@Serializable object CheckoutRoute
@Serializable data class OrderConfirmationRoute(val orderId: String)
@Serializable object OrdersRoute
@Serializable data class OrderDetailRoute(val orderId: String)
@Serializable object WishlistRoute
@Serializable object LoginRoute
@Serializable object SignupRoute
@Serializable object AccountRoute
@Serializable object AddressesRoute
@Serializable data class AddressEditRoute(val addressId: String? = null)
@Serializable object PaymentMethodsRoute
@Serializable data class PaymentEditRoute(val paymentId: String? = null)
