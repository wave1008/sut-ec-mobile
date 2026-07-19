package com.sutec.mobile.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.sutec.mobile.data.model.ProductTag
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr

@Composable
fun TagBadge(tag: ProductTag, modifier: Modifier = Modifier) {
    val extraColors = MaterialTheme.extraColors
    val label = when (tag) {
        ProductTag.PRIME -> tr(ja = "Prime", en = "Prime")
        ProductTag.BESTSELLER -> tr(ja = "ベストセラー", en = "Best Seller")
        ProductTag.NEW -> tr(ja = "新着", en = "New")
        ProductTag.SALE -> tr(ja = "セール", en = "Sale")
        ProductTag.LOW_STOCK -> tr(ja = "残りわずか", en = "Low stock")
    }
    val (background, foreground) = when (tag) {
        ProductTag.PRIME -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        ProductTag.BESTSELLER -> extraColors.star.copy(alpha = 0.15f) to extraColors.star
        ProductTag.NEW -> extraColors.success.copy(alpha = 0.15f) to extraColors.success
        ProductTag.SALE -> extraColors.sale.copy(alpha = 0.15f) to extraColors.sale
        ProductTag.LOW_STOCK -> extraColors.sale.copy(alpha = 0.15f) to extraColors.sale
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = foreground,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xxs),
    )
}
