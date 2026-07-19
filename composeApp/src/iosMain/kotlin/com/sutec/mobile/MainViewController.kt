package com.sutec.mobile

import androidx.compose.ui.window.ComposeUIViewController
import com.sutec.mobile.di.initKoin
import platform.UIKit.UIViewController

private val doInitKoin = run {
    initKoin()
}

fun MainViewController(): UIViewController {
    doInitKoin
    return ComposeUIViewController { App() }
}
