package com.trackgod.app.feature.weightloss

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.core.database.entity.BodyMetricEntity
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Industrial line chart for weight progress.
 *
 * Draws a blood-red line with area fill showing weight over time.
 * A dashed horizontal line marks the target weight.
 *
 * @param weightHistory List of body metrics, newest first (will be reversed for display).
 * @param targetWeight Optional target weight for the dashed reference line.
 * @param weightUnit Display unit label (KG or LBS).
 * @param modifier Modifier for the root container.
 */
@Composable
fun WeightProgressChart(
    weightHistory: List<BodyMetricEntity>,
    targetWeight: Float?,
    weightUnit: String,
    modifier: Modifier = Modifier,
) {
    val entries = weightHistory
        .filter { it.weight != null }
        .reversed() // oldest first for left-to-right drawing
        .takeLast(30)

    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(SurfaceLow),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "NO WEIGH-IN DATA YET",
                color = TextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }
        return
    }

    val weights = entries.mapNotNull { it.weight }
    val allValues = if (targetWeight != null) weights + targetWeight else weights
    val minWeight = (allValues.min() - 2f).coerceAtLeast(0f)
    val maxWeight = allValues.max() + 2f
    val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Y-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        ) {
            Text(
                text = "%.1f".format(maxWeight),
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = weightUnit.uppercase(),
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }

        // Chart canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(SurfaceLow),
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val paddingH = 8f
            val paddingV = 12f
            val chartWidth = canvasWidth - paddingH * 2
            val chartHeight = canvasHeight - paddingV * 2

            if (weights.size < 2) {
                // Single point -- draw a dot
                val y = paddingV + chartHeight * (1f - (weights[0] - minWeight) / weightRange)
                drawCircle(
                    color = Blood,
                    radius = 6f,
                    center = Offset(canvasWidth / 2f, y),
                )
                return@Canvas
            }

            // Build the line path
            val linePath = Path()
            val areaPath = Path()

            val stepX = chartWidth / (weights.size - 1).coerceAtLeast(1)

            weights.forEachIndexed { index, weight ->
                val x = paddingH + index * stepX
                val y = paddingV + chartHeight * (1f - (weight - minWeight) / weightRange)

                if (index == 0) {
                    linePath.moveTo(x, y)
                    areaPath.moveTo(x, paddingV + chartHeight) // bottom
                    areaPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    areaPath.lineTo(x, y)
                }
            }

            // Close the area path
            val lastX = paddingH + (weights.size - 1) * stepX
            areaPath.lineTo(lastX, paddingV + chartHeight)
            areaPath.close()

            // Draw area fill
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Blood.copy(alpha = 0.25f),
                        Blood.copy(alpha = 0.02f),
                    ),
                    startY = paddingV,
                    endY = paddingV + chartHeight,
                ),
            )

            // Draw the line
            drawPath(
                path = linePath,
                color = Blood,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                ),
            )

            // Draw target weight dashed line
            if (targetWeight != null && targetWeight in minWeight..maxWeight) {
                val targetY = paddingV + chartHeight * (1f - (targetWeight - minWeight) / weightRange)
                drawLine(
                    color = TextTertiary,
                    start = Offset(paddingH, targetY),
                    end = Offset(paddingH + chartWidth, targetY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                )
            }

            // Highlight last point (current weight)
            val lastWeight = weights.last()
            val lastY = paddingV + chartHeight * (1f - (lastWeight - minWeight) / weightRange)
            drawCircle(
                color = Blood,
                radius = 6f,
                center = Offset(lastX, lastY),
            )
            drawCircle(
                color = Color(0xFF1C1B1B),
                radius = 3f,
                center = Offset(lastX, lastY),
            )
        }

        // Bottom labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            Text(
                text = "%.1f".format(minWeight),
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (targetWeight != null) {
                Text(
                    text = "TARGET: %.1f".format(targetWeight),
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}
