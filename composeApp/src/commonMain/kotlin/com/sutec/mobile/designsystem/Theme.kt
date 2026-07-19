package com.sutec.mobile.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// アプリ共通テーマ。全画面はこの配下で描画する(App がルートで適用)。
// 既定はシステムのダークモードに追従。Spacing は MaterialTheme.spacing、追加色は MaterialTheme.extraColors。
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AppDarkColorScheme else AppColorScheme
    val extraColors = if (darkTheme) AppDarkExtraColors else AppExtraColors()
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalAppExtraColors provides extraColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
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
