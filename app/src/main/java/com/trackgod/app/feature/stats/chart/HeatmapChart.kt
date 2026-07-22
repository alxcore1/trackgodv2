package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.HeatmapDay
import com.trackgod.app.feature.stats.HeatmapInsight
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HeatmapChart(
    data: List<HeatmapDay>,
    insight: HeatmapInsight = HeatmapInsight(),
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val today = LocalDate.now()
    var selected by remember(data) { mutableStateOf<HeatmapDay?>(null) }

    ChartShell(
        title = "CONSISTENCY HEATMAP",
        summary = "ACTIVE DAYS ${insight.activeDays.takeIf { it > 0 } ?: data.count { it.volume > 0f }} · WEEK ${insight.currentWeekActivity}",
        modifier = modifier,
        legend = {
            LegendRow(
                listOf(
                    "REST" to intensityColor(0),
                    "LOW" to intensityColor(1),
                    "MED" to intensityColor(2),
                    "HIGH" to intensityColor(4),
                ),
            )
        },
        detail = selected?.let { day ->
            {
                DetailPanel(
                    title = if (day.date == today) "TODAY DETAIL" else "${day.date.format(DateTimeFormatter.ISO_LOCAL_DATE)} DETAIL",
                    lines = listOf(
                        "VOLUME ${formatCompact(day.volume)}",
                        "INTENSITY ${intensityLabel(day.intensity)}",
                        if (day.volume > 0f) "WORKOUT DAY" else "REST DAY",
                    ),
                )
            }
        },
    ) {
        val weeks = data.chunked(7)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { day ->
                        val isToday = day.date == today
                        Box(
                            modifier = Modifier
                                .size(17.dp)
                                .background(intensityColor(day.intensity), RectangleShape)
                                .then(
                                    if (isToday) {
                                        Modifier.border(1.dp, BloodBright, RectangleShape)
                                    } else {
                                        Modifier
                                    },
                                )
                                .semantics {
                                    contentDescription = "${day.date} ${intensityLabel(day.intensity)} ${formatCompact(day.volume)}"
                                }
                                .statsTag("stats-heatmap-day-${day.date}")
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { selected = day },
                        )
                    }
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "TODAY OUTLINED WHEN VISIBLE",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
        )
    }
}

private fun intensityColor(intensity: Int): Color = when (intensity) {
    0 -> SurfaceHighest
    1 -> Color(0xFF4A1A1A)
    2 -> Color(0xFF7A1010)
    3 -> Blood
    4 -> Color(0xFFB3271F)
    else -> SurfaceHighest
}

private fun intensityLabel(intensity: Int): String = when (intensity) {
    0 -> "REST"
    1 -> "LOW"
    2 -> "MED"
    3, 4 -> "HIGH"
    else -> "REST"
}
