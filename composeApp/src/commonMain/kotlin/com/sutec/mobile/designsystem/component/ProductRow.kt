package com.sutec.mobile.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.sutec.mobile.i18n.tr

@Composable
fun ProductRow(
    product: Product,
    isWishlisted: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extraColors = MaterialTheme.extraColors
    val spacing = MaterialTheme.spacing
    val lang = LocalAppLanguage.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncProductImage(
            url = product.imageUrls.firstOrNull(),
            contentDescription = product.name(lang),
            modifier = Modifier.width(104.dp),
            aspectRatio = 1f,
        )

        Spacer(Modifier.width(spacing.md))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = product.name(lang),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isWishlisted) {
                            tr("お気に入りから削除", "Remove from wishlist")
                        } else {
                            tr("お気に入りに追加", "Add to wishlist")
                        },
                        tint = if (isWishlisted) extraColors.sale else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = product.brand(lang),
                style = MaterialTheme.typography.bodySmall,
                color = extraColors.onSurfaceFaint,
                maxLines = 1,
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
            )
        }
    }
}
