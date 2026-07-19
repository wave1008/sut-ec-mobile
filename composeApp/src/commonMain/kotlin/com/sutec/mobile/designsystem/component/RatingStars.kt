package com.sutec.mobile.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier,
    reviewCount: Int? = null,
    starSize: Dp = 14.dp,
    showValue: Boolean = false,
) {
    val extraColors = MaterialTheme.extraColors
    // 0.25刻みの半端は最寄りの 0.5 に丸めて「満/半/空」の3種類だけで表現する。
    val roundedHalf = (rating * 2).roundToInt() / 2.0
    val fullStars = floor(roundedHalf).toInt()
    val hasHalfStar = roundedHalf - fullStars >= 0.5

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        for (i in 0 until 5) {
            val icon = when {
                i < fullStars -> Icons.Filled.Star
                i == fullStars && hasHalfStar -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = extraColors.star,
            )
        }
        if (showValue) {
            Spacer(Modifier.width(MaterialTheme.spacing.xxs))
            // String.format が common に無いため小数第1位までを手動整形。
            val tenths = (rating * 10).roundToInt()
            Text(
                text = "${tenths / 10}.${tenths % 10}",
                style = MaterialTheme.typography.labelMedium,
                color = extraColors.onSurfaceFaint,
            )
        }
        if (reviewCount != null) {
            Spacer(Modifier.width(MaterialTheme.spacing.xxs))
            Text(
                text = "($reviewCount)",
                style = MaterialTheme.typography.labelMedium,
                color = extraColors.onSurfaceFaint,
            )
        }
    }
}
