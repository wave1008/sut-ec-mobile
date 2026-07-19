package com.sutec.mobile.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.repository.SortOption
import com.sutec.mobile.designsystem.component.AppFilterChip
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.EmptyState
import com.sutec.mobile.designsystem.component.ErrorState
import com.sutec.mobile.designsystem.component.LoadingState
import com.sutec.mobile.designsystem.component.ProductCard
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

private val SORT_OPTIONS = listOf(
    SortOption.RELEVANCE,
    SortOption.PRICE_ASC,
    SortOption.PRICE_DESC,
    SortOption.RATING,
    SortOption.NEWEST,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    categoryId: String?,
    onProductClick: (String) -> Unit,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: CatalogViewModel = koinViewModel(),
) {
    LaunchedEffect(categoryId) { viewModel.load(categoryId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wishlistedIds by viewModel.wishlistedIds.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    Scaffold(
        topBar = {
            AppTopBar(
                title = uiState.category?.name(LocalAppLanguage.current)
                    ?: tr("すべての商品", "All products"),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = tr("検索", "Search"))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.loading -> LoadingState(modifier = Modifier.fillMaxSize())
                uiState.error -> ErrorState(onRetry = viewModel::retry, modifier = Modifier.fillMaxSize())
                uiState.products.isEmpty() -> Column {
                    SortChipsRow(sort = uiState.sort, onSelect = viewModel::setSort)
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = tr("商品が見つかりません", "No products found"),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> Column {
                    SortChipsRow(sort = uiState.sort, onSelect = viewModel::setSort)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(spacing.screenH),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        items(uiState.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                isWishlisted = product.id in wishlistedIds,
                                onClick = { onProductClick(product.id) },
                                onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortChipsRow(sort: SortOption, onSelect: (SortOption) -> Unit) {
    val spacing = MaterialTheme.spacing
    LazyRow(
        contentPadding = PaddingValues(horizontal = spacing.screenH, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        items(SORT_OPTIONS, key = { it }) { option ->
            AppFilterChip(
                selected = sort == option,
                label = sortLabel(option),
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
private fun sortLabel(option: SortOption): String = when (option) {
    SortOption.RELEVANCE -> tr("おすすめ", "Recommended")
    SortOption.PRICE_ASC -> tr("価格が安い順", "Price: low to high")
    SortOption.PRICE_DESC -> tr("価格が高い順", "Price: high to low")
    SortOption.RATING -> tr("評価順", "Top rated")
    SortOption.NEWEST -> tr("新着順", "Newest")
}
