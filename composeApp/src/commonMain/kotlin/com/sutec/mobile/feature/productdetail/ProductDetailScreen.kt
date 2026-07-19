package com.sutec.mobile.feature.productdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.data.model.Review
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.AsyncProductImage
import com.sutec.mobile.designsystem.component.EmptyState
import com.sutec.mobile.designsystem.component.ErrorState
import com.sutec.mobile.designsystem.component.LoadingState
import com.sutec.mobile.designsystem.component.PriceText
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.component.ProductCard
import com.sutec.mobile.designsystem.component.QuantityStepper
import com.sutec.mobile.designsystem.component.RatingStars
import com.sutec.mobile.designsystem.component.SectionHeader
import com.sutec.mobile.designsystem.component.TagBadge
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.tr
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onOpenCart: () -> Unit,
    viewModel: ProductDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wishlistedIds by viewModel.wishlistedIds.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addedMessage = tr("カートに追加しました", "Added to cart")
    val viewCartLabel = tr("見る", "View")

    LaunchedEffect(productId) { viewModel.load(productId) }

    val product = uiState.product
    val isWishlisted = product != null && product.id in wishlistedIds

    Scaffold(
        topBar = {
            AppTopBar(
                title = "",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleWishlist() }, enabled = product != null) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isWishlisted) MaterialTheme.extraColors.sale else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onOpenCart) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            if (product != null) {
                Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 2.dp, shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.screenH),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                    ) {
                        QuantityStepper(
                            quantity = uiState.quantity,
                            onDecrement = { viewModel.setQuantity(uiState.quantity - 1) },
                            onIncrement = { viewModel.setQuantity(uiState.quantity + 1) },
                        )
                        PrimaryButton(
                            text = tr("カートに追加", "Add to cart"),
                            enabled = product.inStock,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.addToCart()
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = addedMessage,
                                        actionLabel = viewCartLabel,
                                        duration = SnackbarDuration.Short,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) onOpenCart()
                                }
                            },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.loading -> LoadingState(Modifier.fillMaxSize())

                uiState.error -> ErrorState(onRetry = viewModel::retry, modifier = Modifier.fillMaxSize())

                uiState.notFound -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = tr("商品が見つかりません", "Product not found"),
                    modifier = Modifier.fillMaxSize(),
                )

                product != null -> ProductDetailBody(
                    product = product,
                    reviews = uiState.reviews,
                    related = uiState.related,
                    wishlistedIds = wishlistedIds,
                    onToggleRelatedWishlist = { id -> viewModel.toggleWishlist(id) },
                    onProductClick = onProductClick,
                )
            }
        }
    }
}

@Composable
private fun ProductDetailBody(
    product: Product,
    reviews: List<Review>,
    related: List<Product>,
    wishlistedIds: Set<String>,
    onToggleRelatedWishlist: (String) -> Unit,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = LocalAppLanguage.current
    val spacing = MaterialTheme.spacing

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        val pageCount = product.imageUrls.size.coerceAtLeast(1)
        val pagerState = rememberPagerState(pageCount = { pageCount })
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            AsyncProductImage(
                url = product.imageUrls.getOrNull(page),
                contentDescription = product.name(lang),
                modifier = Modifier.fillMaxWidth(),
                aspectRatio = 1f,
                cornerRadius = 0.dp,
            )
        }
        if (pageCount > 1) {
            Spacer(Modifier.height(spacing.xs))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(pageCount) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.extraColors.cardBorder
                                },
                            ),
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(spacing.screenH)) {
            Text(
                text = product.brand(lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.extraColors.onSurfaceFaint,
            )
            Spacer(Modifier.height(spacing.xxs))
            Text(text = product.name(lang), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(spacing.xs))
            RatingStars(rating = product.rating, reviewCount = product.reviewCount, showValue = true)
            Spacer(Modifier.height(spacing.sm))
            PriceText(
                priceYen = product.priceYen,
                listPriceYen = product.listPriceYen,
                priceStyle = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = if (product.inStock) tr("在庫あり", "In stock") else tr("在庫切れ", "Out of stock"),
                style = MaterialTheme.typography.bodyMedium,
                color = if (product.inStock) MaterialTheme.extraColors.success else MaterialTheme.extraColors.sale,
            )

            if (product.tags.isNotEmpty()) {
                Spacer(Modifier.height(spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    product.tags.forEach { tag -> TagBadge(tag = tag) }
                }
            }

            Spacer(Modifier.height(spacing.xl))
            Text(text = product.description(lang), style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(spacing.xl))
            SectionHeader(title = tr("レビュー", "Reviews"))
            Spacer(Modifier.height(spacing.sm))
            if (reviews.isEmpty()) {
                Text(
                    text = tr("レビューはまだありません", "No reviews yet"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.extraColors.onSurfaceFaint,
                )
            } else {
                reviews.forEachIndexed { index, review ->
                    ReviewItem(review = review)
                    if (index != reviews.lastIndex) Spacer(Modifier.height(spacing.md))
                }
            }
        }

        if (related.isNotEmpty()) {
            Spacer(Modifier.height(spacing.lg))
            SectionHeader(
                title = tr("関連商品", "Related"),
                modifier = Modifier.padding(horizontal = spacing.screenH),
            )
            Spacer(Modifier.height(spacing.sm))
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.screenH),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(related, key = { it.id }) { item ->
                    ProductCard(
                        product = item,
                        isWishlisted = item.id in wishlistedIds,
                        onClick = { onProductClick(item.id) },
                        onToggleWishlist = { onToggleRelatedWishlist(item.id) },
                        modifier = Modifier.width(160.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
private fun ReviewItem(review: Review, modifier: Modifier = Modifier) {
    val lang = LocalAppLanguage.current
    val spacing = MaterialTheme.spacing
    Column(modifier = modifier.fillMaxWidth()) {
        RatingStars(rating = review.rating.toDouble(), starSize = 12.dp)
        Spacer(Modifier.height(spacing.xxs))
        Text(
            text = review.authorName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.extraColors.onSurfaceFaint,
        )
        Spacer(Modifier.height(spacing.xxs))
        Text(text = review.title(lang), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(spacing.xxs))
        Text(text = review.body(lang), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(spacing.xxs))
        Text(
            text = review.date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.extraColors.onSurfaceFaint,
        )
    }
}
