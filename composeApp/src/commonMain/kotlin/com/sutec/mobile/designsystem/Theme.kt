package com.sutec.mobile.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// アプリ共通テーマ。全画面はこの配下で描画する(App がルートで適用)。
// Spacing は MaterialTheme.spacing、追加色は MaterialTheme.extraColors で参照(下の拡張)。
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalAppExtraColors provides AppExtraColors(),
    ) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

// 参照ショートカット。`MaterialTheme.spacing.md` / `MaterialTheme.extraColors.sale` で使う。
val MaterialTheme.spacing: Spacing
    @Composable get() = LocalSpacing.current

val MaterialTheme.extraColors: AppExtraColors
    @Composable get() = LocalAppExtraColors.current
