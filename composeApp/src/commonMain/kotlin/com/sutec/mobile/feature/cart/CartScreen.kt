package com.sutec.mobile.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.OrderTotals
import com.sutec.mobile.data.repository.FREE_SHIPPING_THRESHOLD_YEN
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.AsyncProductImage
import com.sutec.mobile.designsystem.component.EmptyState
import com.sutec.mobile.designsystem.component.KeyValueRow
import com.sutec.mobile.designsystem.component.PriceText
import com.sutec.mobile.designsystem.component.PrimaryButton
import com.sutec.mobile.designsystem.component.QuantityStepper
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.AppLanguage
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.tr
import com.sutec.mobile.util.formatYen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onProductClick: (String) -> Unit,
    onCheckout: () -> Unit,
    onContinueShopping: () -> Unit,
    viewModel: CartViewModel = koinViewModel(),
) {
    val cartItems by viewModel.items.collectAsStateWithLifecycle()
    val totals by viewModel.totals.collectAsStateWithLifecycle()
    val lang = LocalAppLanguage.current

    Scaffold(
        topBar = { AppTopBar(title = tr("カート", "Cart")) },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CartSummaryBar(totals = totals, onCheckout = onCheckout)
            }
        },
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Outlined.ShoppingCart,
                    title = tr("カートは空です", "Your cart is empty"),
                    actionLabel = tr("買い物を続ける", "Continue shopping"),
                    onAction = onContinueShopping,
                )
            }
        } else {
            val remaining = FREE_SHIPPING_THRESHOLD_YEN - totals.subtotalYen
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacing.screenH,
                    vertical = MaterialTheme.spacing.md,
                ),
            ) {
                if (remaining > 0) {
                    item {
                        Text(
                            text = tr(
                                ja = "あと${formatYen(remaining)}で送料無料",
                                en = "${formatYen(remaining)} away from free shipping",
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = MaterialTheme.spacing.md),
                        )
                    }
                }
                items(cartItems, key = { it.product.id }) { cartItem ->
                    CartItemRow(
                        item = cartItem,
                        lang = lang,
                        onProductClick = onProductClick,
                        onQuantityChange = { quantity -> viewModel.setQuantity(cartItem.product.id, quantity) },
                        onRemove = { viewModel.remove(cartItem.product.id) },
                    )
                    HorizontalDivider(color = MaterialTheme.extraColors.divider)
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    lang: AppLanguage,
    onProductClick: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncProductImage(
            url = item.product.imageUrls.firstOrNull(),
            contentDescription = item.product.name(lang),
            modifier = Modifier.width(88.dp).clickable { onProductClick(item.product.id) },
            aspectRatio = 1f,
        )
        Spacer(Modifier.width(MaterialTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.product.name(lang),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp).testTag("btn_remove_item")) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = tr("削除", "Remove"),
                        tint = MaterialTheme.extraColors.onSurfaceFaint,
                    )
                }
            }
            Spacer(Modifier.height(MaterialTheme.spacing.xxs))
            PriceText(
                priceYen = item.product.priceYen,
                listPriceYen = item.product.listPriceYen,
                priceStyle = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuantityStepper(
                    quantity = item.quantity,
                    onDecrement = { onQuantityChange(item.quantity - 1) },
                    onIncrement = { onQuantityChange(item.quantity + 1) },
                )
                Text(
                    text = formatYen(item.lineTotalYen),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CartSummaryBar(totals: OrderTotals, onCheckout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(MaterialTheme.spacing.md),
    ) {
        KeyValueRow(label = tr("小計", "Subtotal"), value = formatYen(totals.subtotalYen))
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        KeyValueRow(
            label = tr("送料", "Shipping"),
            value = if (totals.shippingYen == 0) tr("無料", "Free") else formatYen(totals.shippingYen),
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        KeyValueRow(label = tr("合計", "Total"), value = formatYen(totals.totalYen), emphasize = true)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        PrimaryButton(text = tr("レジに進む", "Proceed to checkout"), onClick = onCheckout)
    }
}
