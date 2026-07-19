package com.sutec.mobile.data.remote

// API サーバーのベースURL。エミュレータ/シミュレータからホストの localhost を指す
// アドレスはプラットフォームで異なる(Android=10.0.2.2 / iOS=127.0.0.1)。画像も同ホストの /images。
expect fun serverBaseUrl(): String
