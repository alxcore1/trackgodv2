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
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

@Composable
fun HeatmapChart(
    data: List<HeatmapDay>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val cols = 7
    val firstDayCol = (data.first().date.dayOfWeek.value - 1) // Mon=0
    val totalCells = data.size + firstDayCol
    val rows = (totalCells + cols - 1) / cols

    // Calculate height: label row + grid rows with fixed cell size
    val cellSizeDp = 16.dp
    val cellSpacingDp = 3.dp
    val labelHeight = 18.dp
    val canvasHeight = labelHeight + (cellSizeDp + cellSpacingDp) * rows + 4.dp

    Column(modifier = modifier) {
        Text(
            text = "CONSISTENCY HEATMAP",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        val textMeasurer = rememberTextMeasurer()
        val labelStyle = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight),
        ) {
            val cellSpacing = cellSpacingDp.toPx()
            val cellSize = cellSizeDp.toPx()
            // Center the grid if cells don't fill the full width
            val gridWidth = cols * cellSize + (cols + 1) * cellSpacing
            val offsetX = (size.width - gridWidth) / 2f

            // Day-of-week labels
            for (col in 0 until cols) {
                val x = offsetX + cellSpacing + col * (cellSize + cellSpacing) + cellSize / 2
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

            val gridTop = labelHeight.toPx() + 4.dp.toPx()

            for ((index, day) in data.withIndex()) {
                val gridIndex = index + firstDayCol
                val col = gridIndex % cols
                val row = gridIndex / cols

                val x = offsetX + cellSpacing + col * (cellSize + cellSpacing)
                val y = gridTop + row * (cellSize + cellSpacing)

                drawRect(
                    color = intensityColor(day.intensity),
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize),
                )
            }
        }
    }
}

private fun intensityColor(intensity: Int): Color = when (intensity) {
    0 -> Color(0xFF2A2A2A)             // Clearly visible dark gray cell
    1 -> Color(0xFF4A1A1A)             // Noticeable red tint
    2 -> Color(0xFF7A1010)             // Medium red
    3 -> Blood                         // Strong red (#8B0000)
    4 -> Color(0xFFCC2200)             // Bright red
    else -> Color(0xFF2A2A2A)
}
