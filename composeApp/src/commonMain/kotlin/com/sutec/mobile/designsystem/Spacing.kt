package com.sutec.mobile.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 8pt グリッド。余白は Spacing.md を基準に。screenH は画面左右パディングの標準値。
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val screenH: Dp = 16.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
