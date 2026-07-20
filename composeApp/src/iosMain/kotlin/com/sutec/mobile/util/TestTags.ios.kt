package com.sutec.mobile.util

import androidx.compose.ui.Modifier

// iOS では testTag が自動的に accessibilityIdentifier として露出するため追加設定は不要。
actual fun Modifier.exposeTestTagsAsResourceId(): Modifier = this
