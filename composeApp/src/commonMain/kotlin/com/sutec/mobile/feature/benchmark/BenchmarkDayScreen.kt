package com.sutec.mobile.feature.benchmark

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.sutec.mobile.designsystem.component.AppTopBar
import com.sutec.mobile.designsystem.spacing
import com.sutec.mobile.i18n.tr

private val MONTH_LABELS_EN = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

// month は 1..12。現在時刻は使わない(決定的スナップショットのため)。
@Composable
fun BenchmarkDayScreen(year: Int, month: Int, day: Int, onBack: () -> Unit) {
    val spacing = MaterialTheme.spacing
    val title = tr("${year}年${month}月${day}日", "${MONTH_LABELS_EN[month - 1]} $day, $year")

    Scaffold(
        modifier = Modifier.testTag("screen_benchmark_day"),
        topBar = { AppTopBar(title = title, onBack = onBack) },
    ) { padding ->
        // LazyColumn は使わない: BenchmarkCalendarScreen と同じくベンチ計測のため
        // 24スロット全てを常時コンポーズツリーへ載せる(仮想化すると測定対象が減る)。
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            for (h in 0..23) {
                val hh = h.toString().padStart(2, '0')
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { }
                        .testTag("slot_$hh")
                        .padding(horizontal = spacing.screenH),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$hh:00",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(48.dp),
                    )
                    Spacer(Modifier.width(spacing.sm))
                }
                HorizontalDivider()
            }
        }
    }
}
