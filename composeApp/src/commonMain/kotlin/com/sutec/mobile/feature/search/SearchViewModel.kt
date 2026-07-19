package com.sutec.mobile.feature.search

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 価格帯の刻み。min/max は SearchQuery.minPriceYen/maxPriceYen にそのまま渡す(null は下限/上限なし)。
enum class PricePreset(val minPriceYen: Int?, val maxPriceYen: Int?) {
    UNDER_1000(null, 1000),
    R1000_5000(1000, 5000),
    R5000_20000(5000, 20000),
    OVER_20000(20000, null),
}

data class SearchUiState(
    val query: String = "",
    val selectedCategoryId: String? = null,
    val pricePreset: PricePreset? = null,
    val sort: SortOption = SortOption.RELEVANCE,
    val results: List<Product> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: Boolean = false,
    // false の間は「未検索」表示(カテゴリ一覧等のヒント)。0件結果と区別するために必要。
    val searched: Boolean = false,
    val categories: List<Category> = emptyList(),
)

class SearchViewModel(
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val wishlistedIds: StateFlow<Set<String>> = wishlistRepository.productIds

    fun load(initialQuery: String?) {
        viewModelScope.launch {
            val categories = productRepository.getCategories()
            _uiState.update { it.copy(categories = categories) }
        }
        if (!initialQuery.isNullOrBlank()) {
            _uiState.update { it.copy(query = initialQuery) }
            search()
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun setCategory(id: String?) {
        _uiState.update { it.copy(selectedCategoryId = id) }
        search()
    }

    fun setSort(sort: SortOption) {
        _uiState.update { it.copy(sort = sort) }
        search()
    }

    fun setPricePreset(preset: PricePreset?) {
        _uiState.update { it.copy(pricePreset = preset) }
        search()
    }

    private var page = 0
    private var total = 0

    private fun queryOf(state: SearchUiState) = SearchQuery(
        text = state.query.trim().takeIf { it.isNotEmpty() },
        categoryId = state.selectedCategoryId,
        minPriceYen = state.pricePreset?.minPriceYen,
        maxPriceYen = state.pricePreset?.maxPriceYen,
        sort = state.sort,
    )

    // 新規検索: 先頭ページから取得し直す。
    fun search() {
        page = 0
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            try {
                val resp = productRepository.getProductsPage(queryOf(state), page = 0, pageSize = PAGE_SIZE)
                total = resp.total
                _uiState.update {
                    it.copy(results = resp.items, loading = false, searched = true, hasMore = resp.items.size < total)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = true, searched = true) }
            }
        }
    }

    // 無限スクロール: 次ページを末尾に追加。
    fun loadMore() {
        val state = _uiState.value
        if (state.loading || state.loadingMore || !state.hasMore) return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingMore = true) }
            try {
                val next = page + 1
                val resp = productRepository.getProductsPage(queryOf(state), page = next, pageSize = PAGE_SIZE)
                page = next
                val merged = _uiState.value.results + resp.items
                _uiState.update { it.copy(results = merged, loadingMore = false, hasMore = merged.size < total) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loadingMore = false) }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 12
    }

    fun toggleWishlist(id: String) = wishlistRepository.toggle(id)

    fun addToCart(product: Product, quantity: Int = 1) = cartRepository.add(product, quantity)
}
