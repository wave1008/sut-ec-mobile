package com.sutec.mobile.feature.benchmark

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.sutec.mobile.designsystem.component.AppFilterChip
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr

// 決定的スナップショットのため現在時刻は使わない。年の起点は固定。
private const val BASE_YEAR = 2020
private val YEAR_PRESETS = listOf(1, 3, 6, 12)

@Composable
fun BenchmarkCalendarScreen(onBack: () -> Unit, onDayClick: (year: Int, month: Int, day: Int) -> Unit) {
    val spacing = MaterialTheme.spacing
    // rememberSaveable: 詳細画面へ遷移して戻っても年数選択を保持する(remember だと破棄され既定に戻る)。
    var years by rememberSaveable { mutableStateOf(3) }
    val cellCount = remember(years) {
        (0 until years).sumOf { y -> (0 until 12).sumOf { m -> daysInMonth(BASE_YEAR + y, m) } }
    }

    Scaffold(
        modifier = Modifier.testTag("screen_benchmark"),
        topBar = { AppTopBar(title = tr("UIベンチマーク", "UI Benchmark"), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.screenH),
        ) {
            Spacer(Modifier.height(spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                YEAR_PRESETS.forEach { n ->
                    AppFilterChip(
                        selected = years == n,
                        label = "$n",
                        onClick = { years = n },
                        modifier = Modifier.testTag("chip_years_$n"),
                    )
                }
            }
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = tr("セル数: $cellCount", "Cells: $cellCount"),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("benchmark_cell_count"),
            )
            Spacer(Modifier.height(spacing.md))

            // LazyColumn/LazyVerticalGrid は使わない: 画面外要素も含め全セルをセマンティクスツリーへ
            // 同時に載せることがこのベンチの目的(スクロール仮想化があると測定対象が減ってしまう)。
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                for (y in 0 until years) {
                    val year = BASE_YEAR + y
                    Text(
                        text = "$year",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("year_header_$year"),
                    )
                    Spacer(Modifier.height(spacing.sm))
                    (0 until 12).chunked(3).forEach { rowMonths ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            rowMonths.forEach { month ->
                                Box(modifier = Modifier.weight(1f)) {
                                    MiniMonth(
                                        year = year,
                                        month = month,
                                        onDayClick = onDayClick,
                                    )
                                }
                            }
                            repeat(3 - rowMonths.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(spacing.sm))
                    }
                    Spacer(Modifier.height(spacing.lg))
                }
            }
        }
    }
}

private val WEEKDAY_LABELS_JA = listOf("月", "火", "水", "木", "金", "土", "日")
private val WEEKDAY_LABELS_EN = listOf("M", "T", "W", "T", "F", "S", "S")
private val MONTH_LABELS_JA = (1..12).map { "${it}月" }
private val MONTH_LABELS_EN = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

@Composable
private fun MiniMonth(
    year: Int,
    month: Int,
    onDayClick: (year: Int, month: Int, day: Int) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val weekdayLabels = WEEKDAY_LABELS_JA.zip(WEEKDAY_LABELS_EN).map { (ja, en) -> tr(ja, en) }
    val monthLabel = tr(MONTH_LABELS_JA[month], MONTH_LABELS_EN[month])
    val mm = (month + 1).toString().padStart(2, '0')

    Column {
        Text(
            text = monthLabel,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.testTag("month_${year}_${month + 1}"),
        )
        Spacer(Modifier.height(spacing.xxs))
        // セルは固定サイズにせず weight で 7 等分する: 月を 3 列に並べても端末幅内へ収め、
        // 幅超過による水平クリップ(セルがタップ不能になる)を防ぐ。
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            weekdayLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f).height(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        val offset = mondayStartOffset(year, month)
        val days = daysInMonth(year, month)
        val totalCells = offset + days
        val rows = (totalCells + 6) / 7
        var day = 1
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    if (cellIndex < offset || day > days) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val dd = day.toString().padStart(2, '0')
                        val key = "day_${year}_${mm}_$dd"
                        val currentDay = day
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { onDayClick(year, month + 1, currentDay) }
                                .testTag(key),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        day++
                    }
                }
            }
        }
    }
}

private fun isLeapYear(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

// month は 0..11。現在時刻には依存しない純関数。
private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1 -> if (isLeapYear(year)) 29 else 28
    3, 5, 8, 10 -> 30
    else -> 31
}

// Zeller の公式で year-month-1日の曜日を求め、月曜始まりのオフセットへ変換する。
private fun mondayStartOffset(year: Int, month: Int): Int {
    var y = year
    var m = month + 1
    if (m < 3) {
        m += 12
        y -= 1
    }
    val k = y % 100
    val j = y / 100
    // h: 0=土,1=日,2=月,...,6=金
    val h = (1 + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
    return (h + 5) % 7
}
