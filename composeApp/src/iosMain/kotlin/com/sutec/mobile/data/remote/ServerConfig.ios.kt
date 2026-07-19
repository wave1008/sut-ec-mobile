package com.sutec.mobile.data.remote

// 接続先はビルド時に生成される ServerConfigDefaults(gradle.properties 等で設定)から解決。
// iOS シミュレータはホストの localhost をそのまま参照できる(既定 127.0.0.1)。
actual fun serverBaseUrl(): String =
    "http://${ServerConfigDefaults.IOS_HOST}:${ServerConfigDefaults.PORT}"
