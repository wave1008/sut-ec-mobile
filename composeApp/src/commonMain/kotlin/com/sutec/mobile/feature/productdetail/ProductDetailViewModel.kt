package com.sutec.mobile.feature.productdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.model.Review
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.ProductRepository
import com.sutec.mobile.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val loading: Boolean = true,
    val product: Product? = null,
    val reviews: List<Review> = emptyList(),
    val related: List<Product> = emptyList(),
    val quantity: Int = 1,
    val notFound: Boolean = false,
)

class ProductDetailViewModel(
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    val wishlistedIds: StateFlow<Set<String>> = wishlistRepository.productIds

    fun load(productId: String) {
        viewModelScope.launch {
            _uiState.update { ProductDetailUiState(loading = true) }
            val product = productRepository.getProduct(productId)
            if (product == null) {
                _uiState.update { it.copy(loading = false, notFound = true) }
                return@launch
            }
            val reviews = productRepository.getReviews(productId)
            val related = productRepository.getRelated(productId)
            _uiState.update {
                it.copy(
                    loading = false,
                    product = product,
                    reviews = reviews,
                    related = related,
                    quantity = 1,
                    notFound = false,
                )
            }
        }
    }

    fun setQuantity(quantity: Int) {
        _uiState.update { it.copy(quantity = quantity.coerceIn(1, 99)) }
    }

    fun addToCart() {
        val state = _uiState.value
        val product = state.product ?: return
        cartRepository.add(product, state.quantity)
    }

    // id 省略時は現商品(画面本体)。related の ProductCard タップ用に id 指定も許容。
    fun toggleWishlist(id: String? = null) {
        val target = id ?: _uiState.value.product?.id ?: return
        wishlistRepository.toggle(target)
    }
}
