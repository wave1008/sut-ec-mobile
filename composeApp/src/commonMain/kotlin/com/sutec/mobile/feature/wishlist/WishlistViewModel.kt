package com.sutec.mobile.feature.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.ProductRepository
import com.sutec.mobile.data.repository.WishlistRepository
import com.sutec.mobile.util.safeLaunch
import kotlinx.coroutines.CancellationException
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

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    // productIds の変化(toggle/remove)に追従して商品リストを再取得する。
    // try/catch は collect の内側に置く(外側だと1度の失敗で collect が終了し、以後の変化に追従しなくなる)。
    init {
        viewModelScope.launch {
            wishlistRepository.productIds.collect { ids -> fetch(ids.toList()) }
        }
    }

    private suspend fun fetch(ids: List<String>) {
        _loading.value = true
        _error.value = false
        try {
            _products.value = productRepository.getProductsByIds(ids)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _error.value = true
        } finally {
            _loading.value = false
        }
    }

    fun retry() = safeLaunch { fetch(wishlistedIds.value.toList()) }

    fun toggleWishlist(productId: String) = wishlistRepository.toggle(productId)

    fun remove(productId: String) = wishlistRepository.remove(productId)

    fun addToCart(product: Product) = cartRepository.add(product)
}
