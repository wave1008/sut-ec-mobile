package com.sutec.mobile.util

import com.sutec.mobile.BuildConfig

// iOS の Platform.isDebugBinary と対称に、デバッグビルドで自動有効化(env FT_RESET でも opt-in 可)。
// release では無効。
actual fun shouldResetNavOnLaunch(): Boolean =
    BuildConfig.DEBUG || System.getenv("FT_RESET") != null
