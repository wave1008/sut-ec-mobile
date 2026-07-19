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
    val loadingMore: Boolean = false,
    val hasMore: Boolean = false,
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

    private var page = 0
    private var total = 0

    fun load(categoryId: String?) {
        currentCategoryId = categoryId
        page = 0
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = false)
            try {
                val categories = productRepository.getCategories()
                val category = categoryId?.let { id -> categories.firstOrNull { it.id == id } }
                val resp = productRepository.getProductsPage(
                    SearchQuery(categoryId = categoryId, sort = _uiState.value.sort), page = 0, pageSize = PAGE_SIZE,
                )
                total = resp.total
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    category = category,
                    products = resp.items,
                    hasMore = resp.items.size < total,
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
        page = 0
        viewModelScope.launch {
            val resp = productRepository.getProductsPage(
                SearchQuery(categoryId = currentCategoryId, sort = sort), page = 0, pageSize = PAGE_SIZE,
            )
            total = resp.total
            _uiState.value = _uiState.value.copy(products = resp.items, hasMore = resp.items.size < total)
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.loading || state.loadingMore || !state.hasMore) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingMore = true)
            try {
                val next = page + 1
                val resp = productRepository.getProductsPage(
                    SearchQuery(categoryId = currentCategoryId, sort = state.sort), page = next, pageSize = PAGE_SIZE,
                )
                page = next
                val merged = _uiState.value.products + resp.items
                _uiState.value = _uiState.value.copy(products = merged, loadingMore = false, hasMore = merged.size < total)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loadingMore = false)
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 12
    }

    fun toggleWishlist(productId: String) = wishlistRepository.toggle(productId)

    fun addToCart(product: Product) = cartRepository.add(product)
}
