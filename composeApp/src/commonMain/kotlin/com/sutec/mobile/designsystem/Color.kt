package com.sutec.mobile.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ライト&ミニマル: 白基調 + インディゴ単色アクセント + スレート系ニュートラル。
private val Indigo = Color(0xFF4F46E5)
private val IndigoDark = Color(0xFF4338CA)
private val IndigoContainer = Color(0xFFE0E7FF)
private val Slate900 = Color(0xFF0F172A)
private val Slate700 = Color(0xFF334155)
private val Slate500 = Color(0xFF64748B)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate50 = Color(0xFFF8FAFC)

// ダーク用の追加トーン。
private val Indigo400 = Color(0xFF818CF8)
private val Indigo300 = Color(0xFFA5B4FC)
private val Indigo800 = Color(0xFF3730A3)
private val Slate950 = Color(0xFF020617)
private val Slate850 = Color(0xFF162032)
private val Slate800 = Color(0xFF1E293B)
private val Slate400 = Color(0xFF94A3B8)
private val Slate300 = Color(0xFFCBD5E1)

val AppColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Slate700,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    tertiary = IndigoDark,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Slate50,
    surfaceContainer = Slate100,
    outline = Slate200,
    outlineVariant = Slate100,
    error = Color(0xFFDC2626),
    onError = Color.White,
    scrim = Color(0x66000000),
)

val AppDarkColorScheme = darkColorScheme(
    primary = Indigo400,
    onPrimary = Slate950,
    primaryContainer = Indigo800,
    onPrimaryContainer = IndigoContainer,
    secondary = Slate300,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate100,
    tertiary = Indigo300,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate850,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    surfaceContainerLowest = Slate950,
    surfaceContainerLow = Slate850,
    surfaceContainer = Slate800,
    outline = Slate700,
    outlineVariant = Slate800,
    error = Color(0xFFF87171),
    onError = Slate950,
    scrim = Color(0x99000000),
)

// Material3 の色枠に無いアクセント(価格/セール/星/在庫)を CompositionLocal で供給。既定=ライト。
data class AppExtraColors(
    val price: Color = Slate900,
    val sale: Color = Color(0xFFE11D48),
    val star: Color = Color(0xFFF59E0B),
    val success: Color = Color(0xFF16A34A),
    val onSurfaceFaint: Color = Slate500,
    val divider: Color = Slate200,
    val cardBorder: Color = Slate200,
)

val AppDarkExtraColors = AppExtraColors(
    price = Slate100,
    sale = Color(0xFFFB7185),
    star = Color(0xFFFBBF24),
    success = Color(0xFF4ADE80),
    onSurfaceFaint = Slate400,
    divider = Slate700,
    cardBorder = Slate700,
)

val LocalAppExtraColors = staticCompositionLocalOf { AppExtraColors() }
