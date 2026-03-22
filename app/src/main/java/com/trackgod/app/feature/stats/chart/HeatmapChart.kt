package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.HeatmapDay
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodDeep
import com.trackgod.app.ui.theme.BloodGlow
import com.trackgod.app.ui.theme.SurfaceBright
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.VoidDeep

/**
 * 90-day consistency heatmap grid.
 *
 * 7 columns (Mon-Sun) x ~13 rows. Each cell colored by workout intensity.
 * Most recent day is at the bottom-right of the grid.
 */
@Composable
fun HeatmapChart(
    data: List<HeatmapDay>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "CONSISTENCY HEATMAP",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Day-of-week labels
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

        val textMeasurer = rememberTextMeasurer()
        val labelStyle = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            val cols = 7
            val labelRowHeight = 16.dp.toPx()
            val cellSpacing = 3.dp.toPx()
            val availableWidth = size.width - (cols + 1) * cellSpacing
            val cellSize = (availableWidth / cols).coerceAtLeast(8.dp.toPx())

            // Draw day-of-week labels at top
            for (col in 0 until cols) {
                val x = cellSpacing + col * (cellSize + cellSpacing) + cellSize / 2
                val layoutResult = textMeasurer.measure(
                    text = dayLabels[col],
                    style = labelStyle,
                    maxLines = 1,
                    constraints = Constraints(maxWidth = cellSize.toInt().coerceAtLeast(1)),
                )
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = Offset(
                        x = x - layoutResult.size.width / 2f,
                        y = 0f,
                    ),
                )
            }

            val gridTop = labelRowHeight + 4.dp.toPx()

            // We want to fill in columns left-to-right with the first day at top-left
            // and the most recent at bottom-right. The first day's DayOfWeek determines
            // its column. Subsequent days fill row-by-row.
            if (data.isEmpty()) return@Canvas

            val firstDayCol = (data.first().date.dayOfWeek.value - 1) // Mon=0, Sun=6

            for ((index, day) in data.withIndex()) {
                val gridIndex = index + firstDayCol
                val col = gridIndex % cols
                val row = gridIndex / cols

                val x = cellSpacing + col * (cellSize + cellSpacing)
                val y = gridTop + row * (cellSize + cellSpacing)

                val color = intensityColor(day.intensity)

                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize),
                )
            }
        }
    }
}

private fun intensityColor(intensity: Int): Color = when (intensity) {
    0 -> Color(0xFF1E1E1E)             // Visible dark cell (not invisible)
    1 -> Color(0xFF3D1515)             // Faint red tint
    2 -> BloodDeep                     // Medium red
    3 -> Blood                         // Strong red
    4 -> BloodGlow                     // Bright red/glow
    else -> Color(0xFF1E1E1E)
}
