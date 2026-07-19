package com.sutec.mobile.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.Category
import com.sutec.mobile.data.repository.SortOption
import com.sutec.mobile.designsystem.component.AppFilterChip
import com.sutec.mobile.designsystem.component.EmptyState
import com.sutec.mobile.designsystem.component.LoadingState
import com.sutec.mobile.designsystem.component.ProductCard
import com.sutec.mobile.designsystem.component.SearchField
import com.sutec.mobile.designsystem.component.SectionHeader
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

// 並び替えは AppFilterChip 1個をタップでこのリスト順に循環させる(専用メニュー部品が無いため)。
private val sortCycle = listOf(
    SortOption.RELEVANCE,
    SortOption.PRICE_ASC,
    SortOption.PRICE_DESC,
    SortOption.RATING,
    SortOption.NEWEST,
)

@Composable
private fun sortLabel(sort: SortOption): String = when (sort) {
    SortOption.RELEVANCE -> tr("おすすめ順", "Relevance")
    SortOption.PRICE_ASC -> tr("価格が安い順", "Price: low to high")
    SortOption.PRICE_DESC -> tr("価格が高い順", "Price: high to low")
    SortOption.RATING -> tr("評価が高い順", "Top rated")
    SortOption.NEWEST -> tr("新着順", "Newest")
}

@Composable
private fun pricePresetLabel(preset: PricePreset): String = when (preset) {
    PricePreset.UNDER_1000 -> tr("〜¥1,000", "Under ¥1,000")
    PricePreset.R1000_5000 -> tr("¥1,000〜¥5,000", "¥1,000-¥5,000")
    PricePreset.R5000_20000 -> tr("¥5,000〜¥20,000", "¥5,000-¥20,000")
    PricePreset.OVER_20000 -> tr("¥20,000〜", "Over ¥20,000")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String?,
    onProductClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wishlistedIds by viewModel.wishlistedIds.collectAsStateWithLifecycle()
    val lang = LocalAppLanguage.current

    LaunchedEffect(initialQuery) { viewModel.load(initialQuery) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                SearchField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = tr("商品を検索", "Search products"),
                    onSearch = { viewModel.search() },
                    onClear = {
                        viewModel.onQueryChange("")
                        viewModel.search()
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.screenH, vertical = MaterialTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            ) {
                uiState.categories.forEach { category ->
                    val selected = uiState.selectedCategoryId == category.id
                    AppFilterChip(
                        selected = selected,
                        label = category.name(lang),
                        onClick = { viewModel.setCategory(if (selected) null else category.id) },
                    )
                }
                PricePreset.entries.forEach { preset ->
                    val selected = uiState.pricePreset == preset
                    AppFilterChip(
                        selected = selected,
                        label = pricePresetLabel(preset),
                        onClick = { viewModel.setPricePreset(if (selected) null else preset) },
                    )
                }
                AppFilterChip(
                    selected = uiState.sort != SortOption.RELEVANCE,
                    label = sortLabel(uiState.sort),
                    onClick = {
                        val nextIndex = (sortCycle.indexOf(uiState.sort) + 1) % sortCycle.size
                        viewModel.setSort(sortCycle[nextIndex])
                    },
                )
            }

            when {
                uiState.loading -> LoadingState(Modifier.fillMaxSize())

                uiState.searched && uiState.results.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = tr("該当する商品がありません", "No products found"),
                    message = tr("キーワードや条件を変えてお試しください", "Try different keywords or filters"),
                    modifier = Modifier.fillMaxSize(),
                )

                uiState.searched -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(MaterialTheme.spacing.screenH),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    items(uiState.results, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isWishlisted = product.id in wishlistedIds,
                            onClick = { onProductClick(product.id) },
                            onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                        )
                    }
                }

                else -> SearchHint(
                    categories = uiState.categories,
                    onCategoryClick = { viewModel.setCategory(it) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// 未検索時のヒント: カテゴリ一覧をタップで即絞り込み検索。
@Composable
private fun SearchHint(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalAppLanguage.current
    Column(modifier = modifier.padding(horizontal = MaterialTheme.spacing.screenH)) {
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        SectionHeader(title = tr("カテゴリから探す", "Browse categories"))
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            items(categories, key = { it.id }) { category ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(category.id) }
                        .padding(vertical = MaterialTheme.spacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = category.emoji, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(MaterialTheme.spacing.xxs))
                        Text(
                            text = category.name(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.extraColors.onSurfaceFaint,
                        )
                    }
                }
            }
        }
    }
}
