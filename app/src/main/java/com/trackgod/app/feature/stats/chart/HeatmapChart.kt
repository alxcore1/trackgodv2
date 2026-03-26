package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.HeatmapDay
import com.trackgod.app.ui.theme.Blood
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * GitHub-style heatmap: weeks as columns (horizontal), days Mon-Sun as rows (vertical).
 * Day labels on the left, month labels on top. Scrolls horizontally for long ranges.
 */
@Composable
fun HeatmapChart(
    data: List<HeatmapDay>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    // Build a date → intensity map
    val intensityMap = data.associate { it.date to it.intensity }

    // Determine date range: align to full weeks (Mon start)
    val startDate = data.minOf { it.date }.with(DayOfWeek.MONDAY)
    val endDate = data.maxOf { it.date }.with(DayOfWeek.SUNDAY)

    // Calculate weeks
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
    val totalWeeks = (totalDays + 6) / 7

    val cellSize = 14.dp
    val cellSpacing = 2.dp
    val dayLabelWidth = 20.dp
    val monthLabelHeight = 14.dp
    val rows = 7

    val gridWidth = dayLabelWidth + (cellSize + cellSpacing) * totalWeeks + 4.dp
    val gridHeight = monthLabelHeight + (cellSize + cellSpacing) * rows + 4.dp

    Column(modifier = modifier) {
        Text(
            text = "CONSISTENCY HEATMAP",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        val scrollState = rememberScrollState(Int.MAX_VALUE) // scroll to end (most recent)
        val textMeasurer = rememberTextMeasurer()
        val labelStyle = MaterialTheme.typography.labelSmall.copy(
            color = TextTertiary,
            fontSize = 8.sp,
        )

        Canvas(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .width(gridWidth)
                .height(gridHeight),
        ) {
            val cellSizePx = cellSize.toPx()
            val cellSpacingPx = cellSpacing.toPx()
            val dayLabelWidthPx = dayLabelWidth.toPx()
            val monthLabelHeightPx = monthLabelHeight.toPx()

            // Day labels (Mon, Wed, Fri) on the left
            val dayLabels = listOf("M", "", "W", "", "F", "", "S")
            for (row in 0 until rows) {
                val label = dayLabels[row]
                if (label.isNotEmpty()) {
                    val y = monthLabelHeightPx + row * (cellSizePx + cellSpacingPx) + cellSizePx / 2
                    val layoutResult = textMeasurer.measure(
                        text = label,
                        style = labelStyle,
                        maxLines = 1,
                        constraints = Constraints(maxWidth = dayLabelWidthPx.toInt()),
                    )
                    drawText(
                        textLayoutResult = layoutResult,
                        topLeft = Offset(0f, y - layoutResult.size.height / 2f),
                    )
                }
            }

            // Month labels on top + grid cells
            var lastMonth = -1
            for (week in 0 until totalWeeks) {
                val weekStartDate = startDate.plusWeeks(week.toLong())

                // Month label at start of each new month
                if (weekStartDate.monthValue != lastMonth) {
                    lastMonth = weekStartDate.monthValue
                    val monthLabel = weekStartDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
                    val x = dayLabelWidthPx + week * (cellSizePx + cellSpacingPx)
                    val layoutResult = textMeasurer.measure(
                        text = monthLabel,
                        style = labelStyle,
                        maxLines = 1,
                        constraints = Constraints(maxWidth = (cellSizePx * 4).toInt()),
                    )
                    drawText(
                        textLayoutResult = layoutResult,
                        topLeft = Offset(x, 0f),
                    )
                }

                // Cells for each day in the week
                for (dayOfWeek in 0 until 7) {
                    val date = weekStartDate.plusDays(dayOfWeek.toLong())
                    if (date.isBefore(data.minOf { it.date }) || date.isAfter(data.maxOf { it.date })) continue

                    val intensity = intensityMap[date] ?: 0
                    val x = dayLabelWidthPx + week * (cellSizePx + cellSpacingPx)
                    val y = monthLabelHeightPx + dayOfWeek * (cellSizePx + cellSpacingPx)

                    drawRect(
                        color = intensityColor(intensity),
                        topLeft = Offset(x, y),
                        size = Size(cellSizePx, cellSizePx),
                    )
                }
            }
        }
    }
}

private fun intensityColor(intensity: Int): Color = when (intensity) {
    0 -> Color(0xFF2A2A2A)
    1 -> Color(0xFF4A1A1A)
    2 -> Color(0xFF7A1010)
    3 -> Blood
    4 -> Color(0xFFCC2200)
    else -> Color(0xFF2A2A2A)
}
