package com.sutec.mobile.feature.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.ProductRepository
import com.sutec.mobile.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val wishlistRepository: WishlistRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    val wishlistedIds: StateFlow<Set<String>> = wishlistRepository.productIds

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // productIds の変化(toggle/remove)に追従して商品リストを再取得する。
    init {
        viewModelScope.launch {
            wishlistRepository.productIds.collect { ids ->
                _loading.value = true
                _products.value = productRepository.getProductsByIds(ids.toList())
                _loading.value = false
            }
        }
    }

    fun toggleWishlist(productId: String) = wishlistRepository.toggle(productId)

    fun remove(productId: String) = wishlistRepository.remove(productId)

    fun addToCart(product: Product) = cartRepository.add(product)
}
