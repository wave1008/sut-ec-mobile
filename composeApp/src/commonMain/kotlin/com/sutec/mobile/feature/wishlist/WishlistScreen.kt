package com.sutec.mobile.feature.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.EmptyState
import com.sutec.mobile.designsystem.component.LoadingState
import com.sutec.mobile.designsystem.component.ProductCard
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onProductClick: (String) -> Unit,
    onBrowse: () -> Unit,
    viewModel: WishlistViewModel = koinViewModel(),
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val wishlistedIds by viewModel.wishlistedIds.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar(title = tr(ja = "お気に入り", en = "Wishlist")) },
    ) { padding ->
        when {
            loading && products.isEmpty() -> LoadingState(modifier = Modifier.padding(padding))

            products.isEmpty() -> EmptyState(
                icon = Icons.Filled.FavoriteBorder,
                title = tr(ja = "お気に入りは空です", en = "Your wishlist is empty"),
                actionLabel = tr(ja = "商品を探す", en = "Browse products"),
                onAction = onBrowse,
                modifier = Modifier.padding(padding),
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.screenH),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = wishlistedIds.contains(product.id),
                        onClick = { onProductClick(product.id) },
                        onToggleWishlist = { viewModel.remove(product.id) },
                    )
                }
            }
        }
    }
}
