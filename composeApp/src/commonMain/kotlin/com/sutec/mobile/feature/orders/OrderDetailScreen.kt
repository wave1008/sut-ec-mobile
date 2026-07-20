package com.sutec.mobile.feature.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.OrderTotals
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.AsyncProductImage
import com.sutec.mobile.designsystem.component.KeyValueRow
import com.sutec.mobile.designsystem.component.LoadingState
import com.sutec.mobile.designsystem.component.SectionHeader
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.LocalAppLanguage
import com.sutec.mobile.i18n.tr
import com.sutec.mobile.util.formatYen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: OrderDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(orderId) { viewModel.load(orderId) }

    val order by viewModel.order.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    Scaffold(
        modifier = Modifier.testTag("screen_order_detail"),
        topBar = { AppTopBar(title = tr(ja = "注文詳細", en = "Order details"), onBack = onBack) },
    ) { padding ->
        val current = order
        if (current == null) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.screenH, vertical = spacing.md),
            ) {
                OrderStatusChip(status = current.status, modifier = Modifier.testTag("text_order_status"))

                Spacer(Modifier.height(spacing.sm))

                Text(text = current.id, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = current.placedAt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.extraColors.onSurfaceFaint,
                )

                Spacer(Modifier.height(spacing.lg))
                SectionHeader(title = tr(ja = "お届け先", en = "Shipping address"))
                Spacer(Modifier.height(spacing.sm))
                AddressBlock(address = current.shippingAddress)

                Spacer(Modifier.height(spacing.lg))
                SectionHeader(title = tr(ja = "お支払い", en = "Payment"))
                Spacer(Modifier.height(spacing.sm))
                Text(text = current.paymentLabel, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(spacing.lg))
                SectionHeader(title = tr(ja = "商品", en = "Items"))
                Spacer(Modifier.height(spacing.sm))
                Column {
                    current.items.forEach { item ->
                        OrderLineItem(item = item, onClick = { onProductClick(item.product.id) })
                    }
                }

                Spacer(Modifier.height(spacing.lg))
                HorizontalDivider(color = MaterialTheme.extraColors.divider)
                Spacer(Modifier.height(spacing.md))
                AmountBreakdown(totals = current.totals)
                Spacer(Modifier.height(spacing.lg))
            }
        }
    }
}

@Composable
private fun AddressBlock(address: Address, modifier: Modifier = Modifier) {
    val extraColors = MaterialTheme.extraColors
    Column(modifier = modifier) {
        Text(text = address.fullName, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "〒${address.postalCode} ${address.prefecture}${address.city}${address.line1}" +
                if (address.line2.isNotBlank()) " ${address.line2}" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = extraColors.onSurfaceFaint,
        )
        Text(
            text = address.phone,
            style = MaterialTheme.typography.bodyMedium,
            color = extraColors.onSurfaceFaint,
        )
    }
}

@Composable
private fun OrderLineItem(item: CartItem, onClick: () -> Unit) {
    val spacing = MaterialTheme.spacing
    val lang = LocalAppLanguage.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncProductImage(
            url = item.product.imageUrls.firstOrNull(),
            contentDescription = item.product.name(lang),
            modifier = Modifier.width(64.dp),
            aspectRatio = 1f,
            cornerRadius = 8.dp,
        )
        Spacer(Modifier.width(spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name(lang),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(spacing.xxs))
            Text(
                text = tr(ja = "数量 ${item.quantity}", en = "Qty ${item.quantity}"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.extraColors.onSurfaceFaint,
            )
        }
        Spacer(Modifier.width(spacing.sm))
        Text(text = formatYen(item.lineTotalYen), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun AmountBreakdown(totals: OrderTotals, modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.spacing
    Column(modifier = modifier.fillMaxWidth()) {
        KeyValueRow(label = tr(ja = "小計", en = "Subtotal"), value = formatYen(totals.subtotalYen))
        Spacer(Modifier.height(spacing.xs))
        KeyValueRow(label = tr(ja = "送料", en = "Shipping"), value = formatYen(totals.shippingYen))
        Spacer(Modifier.height(spacing.xs))
        KeyValueRow(
            label = tr(ja = "合計", en = "Total"),
            value = formatYen(totals.totalYen),
            emphasize = true,
            valueColor = MaterialTheme.extraColors.price,
        )
    }
}
