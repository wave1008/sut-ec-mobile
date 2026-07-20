package com.sutec.mobile.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.component.AsyncProductImage
import com.sutec.mobile.designsystem.component.EmptyState
import com.sutec.mobile.designsystem.component.ErrorState
import com.sutec.mobile.designsystem.component.LoadingState
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr
import com.sutec.mobile.util.formatYen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit,
    onBrowse: () -> Unit,
    onBack: () -> Unit,
    viewModel: OrdersViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.testTag("screen_orders"),
        topBar = { AppTopBar(title = tr(ja = "注文履歴", en = "Orders"), onBack = onBack) },
    ) { padding ->
        val content = Modifier.padding(padding)
        when {
            uiState.loading && uiState.orders.isEmpty() -> LoadingState(content.fillMaxSize())
            uiState.error && uiState.orders.isEmpty() -> ErrorState(onRetry = viewModel::retry, modifier = content.fillMaxSize())
            uiState.orders.isEmpty() -> EmptyState(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = tr(ja = "注文履歴がありません", en = "No orders yet"),
                actionLabel = tr(ja = "買い物を始める", en = "Start shopping"),
                onAction = onBrowse,
                modifier = content,
            )
            else -> LazyColumn(
                modifier = content,
                contentPadding = PaddingValues(MaterialTheme.spacing.screenH),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                items(uiState.orders, key = { it.id }) { order ->
                    OrderCard(order = order, onClick = { onOrderClick(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    val spacing = MaterialTheme.spacing
    val extraColors = MaterialTheme.extraColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, extraColors.cardBorder, MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(spacing.md)
            .testTag("order_row_${order.id}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = order.id, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = order.placedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = extraColors.onSurfaceFaint,
                )
            }
            OrderStatusChip(status = order.status)
        }

        Spacer(Modifier.height(spacing.sm))

        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            order.items.take(4).forEach { item ->
                AsyncProductImage(
                    url = item.product.imageUrls.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier.width(48.dp),
                    aspectRatio = 1f,
                    cornerRadius = 8.dp,
                )
            }
        }

        Spacer(Modifier.height(spacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tr(ja = "${order.itemCount}点", en = "${order.itemCount} items"),
                style = MaterialTheme.typography.bodyMedium,
                color = extraColors.onSurfaceFaint,
            )
            Text(
                text = formatYen(order.totals.totalYen),
                style = MaterialTheme.typography.titleMedium,
                color = extraColors.price,
            )
        }
    }
}
