package com.sutec.mobile.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.util.formatYen

@Composable
fun PriceText(
    priceYen: Int,
    listPriceYen: Int? = null,
    modifier: Modifier = Modifier,
    priceStyle: TextStyle = MaterialTheme.typography.titleMedium,
    compact: Boolean = false,
) {
    val extraColors = MaterialTheme.extraColors
    val listPrice = listPriceYen?.takeIf { it > priceYen }

    Row(verticalAlignment = Alignment.Bottom, modifier = modifier) {
        Text(
            text = formatYen(priceYen),
            style = priceStyle,
            fontWeight = FontWeight.Bold,
            color = extraColors.price,
        )
        if (listPrice != null) {
            val discountPercent = ((listPrice - priceYen) * 100) / listPrice
            Spacer(Modifier.width(MaterialTheme.spacing.xs))
            if (!compact) {
                Text(
                    text = formatYen(listPrice),
                    style = MaterialTheme.typography.bodySmall,
                    color = extraColors.onSurfaceFaint,
                    textDecoration = TextDecoration.LineThrough,
                )
                Spacer(Modifier.width(MaterialTheme.spacing.xs))
            }
            Text(
                text = "-$discountPercent%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = extraColors.sale,
            )
        }
    }
}
