package com.sutec.mobile.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// iOS(Kotlin/Native)は未捕捉コルーチン例外で即 abort する(App.kt 参照)。API 呼び出しを含む launch を
// 素で書くと、サーバー停止時の ConnectException 等が未捕捉になりクラッシュする。書き忘れ=クラッシュを
// 構造的に無くすため、ViewModel の API 呼び出しは safeLaunch に寄せる。
// CancellationException は必ず再送出する(協調的キャンセルを壊さない)。それ以外は onError に落とす。
fun ViewModel.safeLaunch(
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit,
): Job = viewModelScope.launch {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        onError(e)
    }
}

// Remote Repository が自前生成する CoroutineScope の安全網。個別の失敗処理は各呼び出しの runCatching が担うが、
// 保護漏れがあっても未捕捉例外でプロセスを落とさないために全スコープへ付与する(iOS abort 対策)。
val remoteScopeExceptionHandler = CoroutineExceptionHandler { _, _ -> }
