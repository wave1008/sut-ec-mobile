package com.sutec.mobile.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.Category
import com.sutec.mobile.designsystem.component.ErrorState
import com.sutec.mobile.designsystem.component.LoadingState
import com.sutec.mobile.designsystem.component.ProductCard
import com.sutec.mobile.designsystem.component.SearchField
import com.sutec.mobile.designsystem.component.SectionHeader
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSeeAllFeatured: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wishlistedIds by viewModel.wishlistedIds.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    if (uiState.loading) {
        LoadingState(modifier = Modifier.fillMaxSize())
        return
    }
    if (uiState.error) {
        ErrorState(onRetry = viewModel::retry, modifier = Modifier.fillMaxSize())
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_home")
            .verticalScroll(rememberScrollState())
            .padding(bottom = spacing.lg),
    ) {
        Column(modifier = Modifier.padding(horizontal = spacing.screenH)) {
            Spacer(Modifier.height(spacing.md))
            Text(
                text = "SUT Store",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(spacing.sm))
            SearchField(
                value = "",
                onValueChange = {},
                placeholder = tr("商品を検索", "Search products"),
                readOnly = true,
                onClick = onSearchClick,
            )
            Spacer(Modifier.height(spacing.md))
            PromoBanner()
            Spacer(Modifier.height(spacing.lg))
            Text(
                text = tr("カテゴリ", "Categories"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(spacing.sm))
        LazyRow(
            contentPadding = PaddingValues(horizontal = spacing.screenH),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            items(uiState.categories, key = { it.id }) { category ->
                CategoryChip(category = category, onClick = { onCategoryClick(category.id) })
            }
        }

        Spacer(Modifier.height(spacing.lg))
        SectionHeader(
            title = tr("おすすめ", "Featured"),
            actionLabel = tr("すべて見る", "See all"),
            onAction = onSeeAllFeatured,
            modifier = Modifier.padding(horizontal = spacing.screenH),
        )
        Spacer(Modifier.height(spacing.sm))
        LazyRow(
            contentPadding = PaddingValues(horizontal = spacing.screenH),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            items(uiState.featured, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    isWishlisted = product.id in wishlistedIds,
                    onClick = { onProductClick(product.id) },
                    onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                    modifier = Modifier.width(160.dp),
                )
            }
        }

        if (uiState.bestsellers.isNotEmpty()) {
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(
                title = tr("ベストセラー", "Bestsellers"),
                modifier = Modifier.padding(horizontal = spacing.screenH),
            )
            Spacer(Modifier.height(spacing.sm))
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.screenH),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                items(uiState.bestsellers, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = product.id in wishlistedIds,
                        onClick = { onProductClick(product.id) },
                        onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                        modifier = Modifier.width(160.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PromoBanner() {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
                ),
            )
            .padding(spacing.md),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column {
            Text(
                text = tr("今だけ全品送料無料", "Free shipping, today only"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.height(spacing.xxs))
            Text(
                text = tr("新生活応援キャンペーン実施中", "New season campaign is on now"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun CategoryChip(category: Category, onClick: () -> Unit) {
    val spacing = MaterialTheme.spacing
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = category.emoji, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(spacing.xxs))
        Text(
            text = category.name(LocalAppLanguage.current),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
