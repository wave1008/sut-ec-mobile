package com.sutec.mobile.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.sutec.mobile.data.model.OrderStatus
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr

// OrdersScreen / OrderDetailScreen で共有するステータス表示。
@Composable
fun OrderStatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val extraColors = MaterialTheme.extraColors
    val label = when (status) {
        OrderStatus.PROCESSING -> tr(ja = "準備中", en = "Processing")
        OrderStatus.SHIPPED -> tr(ja = "発送済み", en = "Shipped")
        OrderStatus.DELIVERED -> tr(ja = "配達済み", en = "Delivered")
        OrderStatus.CANCELLED -> tr(ja = "キャンセル", en = "Cancelled")
    }
    val color = when (status) {
        OrderStatus.PROCESSING -> MaterialTheme.colorScheme.primary
        OrderStatus.SHIPPED -> extraColors.star
        OrderStatus.DELIVERED -> extraColors.success
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xxs),
    )
}
