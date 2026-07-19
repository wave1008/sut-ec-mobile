package com.sutec.mobile.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillMaxWidth: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier,
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(contentAlignment = Alignment.Center) {
            val textColor = if (loading) Color.Transparent else MaterialTheme.colorScheme.onPrimary
            Text(text, color = textColor)
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(text)
    }
}

@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Text(text)
    }
}
