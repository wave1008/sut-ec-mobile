package com.sutec.mobile.util

import androidx.compose.ui.Modifier

// testTag をプラットフォームのアクセシビリティ識別子として露出させるルート用ヘルパー。
// Android: semantics{testTagsAsResourceId=true} で resource-id 化(UIAutomator/foundation-tester 用)。
// iOS: testTag は自動で accessibilityIdentifier になるため no-op。
// testTagsAsResourceId は Android 専用 API のため commonMain から直接参照できず expect/actual で分離。
// 契約: docs/test/ui-test-tags.md
expect fun Modifier.exposeTestTagsAsResourceId(): Modifier
