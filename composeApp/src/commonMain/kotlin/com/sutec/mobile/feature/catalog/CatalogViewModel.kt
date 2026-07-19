package com.sutec.mobile.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.Category
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.ProductRepository
import com.sutec.mobile.data.repository.SearchQuery
import com.sutec.mobile.data.repository.SortOption
import com.sutec.mobile.data.repository.WishlistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val category: Category? = null,
    val products: List<Product> = emptyList(),
    val sort: SortOption = SortOption.RELEVANCE,
)

class CatalogViewModel(
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    val wishlistedIds: StateFlow<Set<String>> = wishlistRepository.productIds

    // categoryId は load() が最後に呼ばれた値を保持し、setSort() の再クエリに使う。
    private var currentCategoryId: String? = null

    fun load(categoryId: String?) {
        currentCategoryId = categoryId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = false)
            try {
                val categories = productRepository.getCategories()
                val category = categoryId?.let { id -> categories.firstOrNull { it.id == id } }
                val products = productRepository.getProducts(
                    SearchQuery(categoryId = categoryId, sort = _uiState.value.sort),
                )
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    category = category,
                    products = products,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = true)
            }
        }
    }

    fun retry() = load(currentCategoryId)

    fun setSort(sort: SortOption) {
        _uiState.value = _uiState.value.copy(sort = sort)
        viewModelScope.launch {
            val products = productRepository.getProducts(
                SearchQuery(categoryId = currentCategoryId, sort = sort),
            )
            _uiState.value = _uiState.value.copy(products = products)
        }
    }

    fun toggleWishlist(productId: String) = wishlistRepository.toggle(productId)

    fun addToCart(product: Product) = cartRepository.add(product)
}
