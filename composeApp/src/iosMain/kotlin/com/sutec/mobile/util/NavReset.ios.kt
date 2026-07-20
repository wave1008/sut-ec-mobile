package com.sutec.mobile.util

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.getenv

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
actual fun shouldResetNavOnLaunch(): Boolean =
    Platform.isDebugBinary || getenv("FT_RESET") != null
