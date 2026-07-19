package com.sutec.mobile.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// i18n 方針: 文字列レジストリを持たず、呼び出し側でインラインに `tr(ja, en)` を書く。
// これにより機能ごとの文字列追加が共有ファイルへの書き込み競合を生まない(並列実装のため)。
// 言語切替は LocaleController(StateFlow) -> App が LocalAppLanguage に流し込む -> 全画面が即時再構成。

// App 直下で LocaleController.language を collect し、この Local に必ず供給する。
val LocalAppLanguage = compositionLocalOf { AppLanguage.JA }

// Composable 内での翻訳取得。読者言語は LocalAppLanguage。
@Composable
@ReadOnlyComposable
fun tr(ja: String, en: String): String =
    when (LocalAppLanguage.current) {
        AppLanguage.JA -> ja
        AppLanguage.EN -> en
    }

// 非 Composable(データ整形など)向け。言語を明示的に渡す。
fun tr(lang: AppLanguage, ja: String, en: String): String =
    when (lang) {
        AppLanguage.JA -> ja
        AppLanguage.EN -> en
    }

// アプリ全体の言語状態。Koin シングルトンで供給し、プロフィール画面から toggle。
class LocaleController {
    private val _language = MutableStateFlow(AppLanguage.JA)
    val language: StateFlow<AppLanguage> = _language

    fun set(lang: AppLanguage) {
        _language.value = lang
    }

    fun toggle() {
        _language.value = if (_language.value == AppLanguage.JA) AppLanguage.EN else AppLanguage.JA
    }
}
