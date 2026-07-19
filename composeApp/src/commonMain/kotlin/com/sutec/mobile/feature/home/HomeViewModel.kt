package com.sutec.mobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.Category
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.model.ProductTag
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.ProductRepository
import com.sutec.mobile.data.repository.WishlistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val categories: List<Category> = emptyList(),
    val featured: List<Product> = emptyList(),
    val bestsellers: List<Product> = emptyList(),
)

class HomeViewModel(
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val wishlistedIds: StateFlow<Set<String>> = wishlistRepository.productIds

    init {
        load()
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val categories = productRepository.getCategories()
                val featured = productRepository.getFeatured()
                // BESTSELLER タグ付きを優先しつつ、featured から件数を補う(専用エンドポイントが無いため)。
                val bestsellers = (featured.filter { ProductTag.BESTSELLER in it.tags } + featured)
                    .distinctBy { it.id }
                    .take(10)
                _uiState.value = HomeUiState(
                    loading = false,
                    categories = categories,
                    featured = featured,
                    bestsellers = bestsellers,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun retry() = load()

    fun toggleWishlist(productId: String) = wishlistRepository.toggle(productId)

    fun addToCart(product: Product) = cartRepository.add(product)
}
