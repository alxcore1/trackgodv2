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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.VolumeDataPoint
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Custom Canvas bar chart for volume progression.
 *
 * Draws red vertical bars proportional to volume, with x-axis labels below.
 * No grid lines, no decorations -- minimal industrial style.
 */
@Composable
fun VolumeChart(
    data: List<VolumeDataPoint>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    // Limit data to last 12 points to prevent overbleed on ALL/YEAR
    val displayData = if (data.size > 12) data.takeLast(12) else data

    Column(modifier = modifier) {
        // Section title
        Text(
            text = "VOLUME PROGRESSION",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        val textMeasurer = rememberTextMeasurer()
        val labelStyle = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            val maxVolume = displayData.maxOf { it.volume }.coerceAtLeast(1f)
            val barCount = displayData.size
            val totalWidth = size.width
            val chartHeight = size.height - 24.dp.toPx()
            val barSpacing = if (barCount > 20) 1.dp.toPx() else 4.dp.toPx()
            val barWidth = ((totalWidth - barSpacing * (barCount + 1)) / barCount)
                .coerceAtLeast(2.dp.toPx())

            // Show labels only every Nth bar to prevent overlap
            val labelEvery = when {
                barCount <= 8 -> 1
                barCount <= 16 -> 2
                barCount <= 30 -> 4
                else -> (barCount / 8).coerceAtLeast(3)
            }

            displayData.forEachIndexed { index, point ->
                val barHeight = if (maxVolume > 0f) {
                    (point.volume / maxVolume) * chartHeight
                } else {
                    0f
                }

                val x = barSpacing + index * (barWidth + barSpacing)
                val y = chartHeight - barHeight

                // Bar
                if (barHeight > 0f) {
                    drawRect(
                        color = Blood,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                    )
                } else {
                    drawRect(
                        color = SurfaceHighest,
                        topLeft = Offset(x, chartHeight - 2.dp.toPx()),
                        size = Size(barWidth, 2.dp.toPx()),
                    )
                }

                // X-axis label (skip some to prevent overlap)
                if (index % labelEvery == 0 || index == barCount - 1) {
                    drawLabel(
                        textMeasurer = textMeasurer,
                        text = point.label,
                        style = labelStyle,
                        x = x + barWidth / 2,
                        y = chartHeight + 6.dp.toPx(),
                        maxWidth = (barWidth + barSpacing) * labelEvery,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawLabel(
    textMeasurer: TextMeasurer,
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    x: Float,
    y: Float,
    maxWidth: Float,
) {
    val layoutResult = textMeasurer.measure(
        text = text,
        style = style,
        overflow = TextOverflow.Clip,
        maxLines = 1,
        constraints = Constraints(maxWidth = maxWidth.toInt().coerceAtLeast(1)),
    )
    val labelX = (x - layoutResult.size.width / 2f).coerceIn(0f, size.width - layoutResult.size.width)
    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(
            x = labelX,
            y = y,
        ),
    )
}
