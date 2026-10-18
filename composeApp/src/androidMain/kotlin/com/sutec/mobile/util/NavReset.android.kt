package com.sutec.mobile.util

// KMP の Android ライブラリは BuildConfig を持たないので、androidApp の SutEcApplication が起動時に BuildConfig.DEBUG を入れる。
var isAndroidDebugBuild: Boolean = false

// iOS の Platform.isDebugBinary と対称に、デバッグビルドで自動有効化(env FT_RESET でも opt-in 可)。
// release では無効。
actual fun shouldResetNavOnLaunch(): Boolean =
    isAndroidDebugBuild || System.getenv("FT_RESET") != null
