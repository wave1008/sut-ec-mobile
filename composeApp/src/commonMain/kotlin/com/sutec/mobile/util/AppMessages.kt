package com.sutec.mobile.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

// 画面横断のトースト(Snackbar)通知バス。非 Composable(ApiClient/ViewModel)から show() で発火し、
// App のルートが collect して現在言語で表示する。i18n のため ja/en を両持ち。
// Buffered Channel を使うのは、起動直後(セッション失効など)の発火を購読開始前でも取りこぼさないため。
data class UiMessage(val ja: String, val en: String)

class AppMessages {
    private val channel = Channel<UiMessage>(Channel.BUFFERED)
    val messages: Flow<UiMessage> = channel.receiveAsFlow()

    fun show(ja: String, en: String) {
        channel.trySend(UiMessage(ja, en))
    }
}
