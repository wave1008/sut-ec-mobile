package com.sutec.mobile.feature.cart

import androidx.lifecycle.ViewModel
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.OrderTotals
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.WishlistRepository
import kotlinx.coroutines.flow.StateFlow

class CartViewModel(
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository,
) : ViewModel() {
    val items: StateFlow<List<CartItem>> = cartRepository.items
    val totals: StateFlow<OrderTotals> = cartRepository.totals
    val wishlistedIds: StateFlow<Set<String>> = wishlistRepository.productIds

    fun setQuantity(productId: String, quantity: Int) {
        cartRepository.setQuantity(productId, quantity)
    }

    fun remove(productId: String) {
        cartRepository.remove(productId)
    }

    fun toggleWishlist(id: String) {
        wishlistRepository.toggle(id)
    }
}
