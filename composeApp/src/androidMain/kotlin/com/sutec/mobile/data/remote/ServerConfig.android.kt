package com.sutec.mobile.data.remote

// 接続先はビルド時に生成される ServerConfigDefaults(gradle.properties 等で設定)から解決。
// Android エミュレータからホストの localhost は 10.0.2.2(既定)。
actual fun serverBaseUrl(): String =
    "http://${ServerConfigDefaults.ANDROID_HOST}:${ServerConfigDefaults.PORT}"
