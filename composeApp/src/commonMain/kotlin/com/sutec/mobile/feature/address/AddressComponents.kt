package com.sutec.mobile.feature.address

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr

// Addresses/PaymentMethods 両画面のカードで使う既定バッジ。file-private ではなく internal にして共有。
@Composable
internal fun DefaultBadge() {
    val extraColors = MaterialTheme.extraColors
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(extraColors.success.copy(alpha = 0.12f))
            .padding(horizontal = spacing.sm, vertical = spacing.xxs),
    ) {
        Text(
            text = tr("既定", "Default"),
            style = MaterialTheme.typography.labelSmall,
            color = extraColors.success,
        )
    }
}
