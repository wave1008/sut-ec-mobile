package com.sutec.mobile.util

// 円表示。common に String.format(グルーピング)が無いため手動で3桁区切り。
// 例: 1980 -> "¥1,980" / -1980 -> "-¥1,980"
fun formatYen(amount: Int): String {
    val negative = amount < 0
    val digits = amount.toString().removePrefix("-")
    val grouped = buildString {
        val n = digits.length
        for (i in 0 until n) {
            if (i > 0 && (n - i) % 3 == 0) append(',')
            append(digits[i])
        }
    }
    return (if (negative) "-¥" else "¥") + grouped
}
