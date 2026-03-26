package com.trackgod.app.feature.stats.chart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.core.database.dao.ExerciseProgressPoint
import com.trackgod.app.feature.stats.ExerciseProgressionData
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

@Composable
fun ExerciseProgressSection(
    progressions: List<ExerciseProgressionData>,
    weightUnit: String,
    modifier: Modifier = Modifier,
) {
    if (progressions.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Blood,
                modifier = Modifier.height(18.dp),
            )
            Text(
                text = "EXERCISE PROGRESSION",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        progressions.forEach { progression ->
            ExerciseProgressCard(
                data = progression,
                weightUnit = weightUnit,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExerciseProgressCard(
    data: ExerciseProgressionData,
    weightUnit: String,
) {
    var expanded by remember { mutableStateOf(false) }
    val rateText = if (data.progressionRate >= 0) "+%.0f%%".format(data.progressionRate)
    else "%.0f%%".format(data.progressionRate)
    val rateColor = if (data.progressionRate >= 0) BloodBright else TextTertiary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { expanded = !expanded },
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.exerciseName.uppercase(),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                )
                Text(
                    text = "${data.category.uppercase()} · ${data.history.size} SESSIONS",
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "%.0f ${weightUnit.uppercase()}".format(data.current1rm),
                    color = BloodBright,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = rateText,
                    color = rateColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.height(16.dp),
                )
            }
        }

        // Expandable chart
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                MiniProgressChart(
                    history = data.history,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "EST. 1RM OVER TIME",
                    color = TextTertiary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}

@Composable
private fun MiniProgressChart(
    history: List<ExerciseProgressPoint>,
    modifier: Modifier = Modifier,
) {
    if (history.size < 2) return

    val values = history.map { it.estimated1rm }
    val minVal = values.min() * 0.95f
    val maxVal = values.max() * 1.05f
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (values.size - 1).coerceAtLeast(1)

        // Area fill
        val areaPath = Path().apply {
            moveTo(0f, h)
            values.forEachIndexed { i, v ->
                val x = i * stepX
                val y = h - ((v - minVal) / range * h)
                lineTo(x, y)
            }
            lineTo(w, h)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(Blood.copy(alpha = 0.3f), Blood.copy(alpha = 0.0f)),
            ),
        )

        // Line
        val linePath = Path().apply {
            values.forEachIndexed { i, v ->
                val x = i * stepX
                val y = h - ((v - minVal) / range * h)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = linePath,
            color = Blood,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // End dot
        val lastX = (values.size - 1) * stepX
        val lastY = h - ((values.last() - minVal) / range * h)
        drawCircle(color = BloodBright, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
    }
}
