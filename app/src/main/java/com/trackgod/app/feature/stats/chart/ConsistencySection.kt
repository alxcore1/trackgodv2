package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.WeeklyConsistencyData
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Consistency section with current/longest streak stats and a weekly workouts bar chart.
 */
@Composable
fun ConsistencySection(
    currentStreak: Int,
    longestStreak: Int,
    workoutsPerWeek: List<WeeklyConsistencyData>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "CONSISTENCY",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Streak stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StreakStat(label = "STREAK", value = "$currentStreak DAYS")
            StreakStat(label = "LONGEST", value = "$longestStreak DAYS")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Workouts per week label
        Text(
            text = "WORKOUTS / WEEK (${workoutsPerWeek.size} WEEKS)",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Weekly bar chart
        if (workoutsPerWeek.isNotEmpty()) {
            WeeklyBarChart(data = workoutsPerWeek)
        }
    }
}

@Composable
private fun StreakStat(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = BloodBright,
        )
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<WeeklyConsistencyData>,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
    ) {
        val maxCount = data.maxOf { it.workoutCount }.coerceAtLeast(1)
        val barCount = data.size
        val totalWidth = size.width
        val chartHeight = size.height - 20.dp.toPx()
        val barSpacing = 6.dp.toPx()
        val barWidth = ((totalWidth - barSpacing * (barCount + 1)) / barCount)
            .coerceAtLeast(4.dp.toPx())

        data.forEachIndexed { index, week ->
            val barHeight = if (maxCount > 0) {
                (week.workoutCount.toFloat() / maxCount) * chartHeight
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

            // Label
            val layoutResult = textMeasurer.measure(
                text = week.weekLabel,
                style = labelStyle,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                constraints = Constraints(maxWidth = (barWidth + barSpacing).toInt().coerceAtLeast(1)),
            )
            drawText(
                textLayoutResult = layoutResult,
                topLeft = Offset(
                    x = x + barWidth / 2 - layoutResult.size.width / 2f,
                    y = chartHeight + 4.dp.toPx(),
                ),
            )
        }
    }
}
