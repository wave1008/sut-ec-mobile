package com.sutec.mobile.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sutec.mobile.designsystem.extraColors

@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
    max: Int = 99,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.extraColors.cardBorder, CircleShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDecrement,
            enabled = enabled && quantity > min,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(Icons.Filled.Remove, contentDescription = null)
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center,
        )
        IconButton(
            onClick = onIncrement,
            enabled = enabled && quantity < max,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}
