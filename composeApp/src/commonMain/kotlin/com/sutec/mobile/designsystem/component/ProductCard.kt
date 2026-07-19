package com.sutec.mobile.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sutec.mobile.data.model.Product
import com.sutec.mobile.designsystem.extraColors
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.LocalAppLanguage

@Composable
fun ProductCard(
    product: Product,
    isWishlisted: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extraColors = MaterialTheme.extraColors
    val spacing = MaterialTheme.spacing

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, extraColors.cardBorder, MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(spacing.sm),
    ) {
        Box {
            AsyncProductImage(
                url = product.imageUrls.firstOrNull(),
                contentDescription = product.name(LocalAppLanguage.current),
                modifier = Modifier.fillMaxWidth(),
                aspectRatio = 1f,
            )
            IconButton(
                onClick = onToggleWishlist,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(spacing.xxs)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isWishlisted) extraColors.sale else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(spacing.sm))

        product.tags.firstOrNull()?.let { tag ->
            TagBadge(tag = tag)
            Spacer(Modifier.height(spacing.xs))
        }

        Text(
            text = product.name(LocalAppLanguage.current),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(spacing.xxs))

        RatingStars(
            rating = product.rating,
            reviewCount = product.reviewCount,
            starSize = 12.dp,
        )

        Spacer(Modifier.height(spacing.xxs))

        PriceText(
            priceYen = product.priceYen,
            listPriceYen = product.listPriceYen,
            priceStyle = MaterialTheme.typography.titleSmall,
            compact = true,
        )
    }
}
