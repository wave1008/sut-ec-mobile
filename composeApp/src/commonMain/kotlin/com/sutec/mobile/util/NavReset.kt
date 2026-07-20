package com.sutec.mobile.util

// テスト/デバッグ実行時に、コールド起動でナビの復元状態を捨ててルート(Home)から開始するか。
// 背景: iOS の Compose 状態復元は NavController のバックスタックをディスク保持し、再起動後に復元する。
// 全シナリオが launchApp()(= simctl の terminate+relaunch)で毎回フル再起動しても、前回押し込んだ
// 画面(例: ログイン)が復元されシナリオ間に漏れる(foundation-tester 事象A)。→ 起動時にルートへ正規化する。
// 有効化: iOS=Platform.isDebugBinary or env FT_RESET / Android=BuildConfig.DEBUG or env FT_RESET。本番(release)では無効。
// 両プラットフォームともデバッグ/テストビルドで自動有効(foundation-tester が回すのは debug ビルドのためツール側の env 注入は不要)。
expect fun shouldResetNavOnLaunch(): Boolean
