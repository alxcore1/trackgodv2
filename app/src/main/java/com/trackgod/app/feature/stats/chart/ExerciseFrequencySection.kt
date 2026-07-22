package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.ExerciseFrequencyData
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Horizontal bar chart showing top exercises by execution count.
 *
 * Exercise name on left, proportional bar on right.
 */
@Composable
fun ExerciseFrequencySection(
    data: List<ExerciseFrequencyData>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val top = data.maxBy { it.count }
    var selected by remember(data) { mutableStateOf<ExerciseFrequencyData?>(null) }
    Column(modifier = modifier) {
        Text(
            text = "MOST EXECUTED RITES",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "MOST REPEATED ${top.exerciseName.uppercase()} · ${top.count} SESSIONS",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        data.take(8).forEach { item ->
            ExerciseFrequencyRow(
                item = item,
                onClick = { selected = item },
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        selected?.let { item ->
            DetailPanel(
                title = "${item.fullExerciseName.uppercase()} DETAIL",
                lines = listOf(
                    "DISPLAY ${item.exerciseName.uppercase()}",
                    "COUNT ${item.count}",
                    "RELATIVE FREQUENCY ${((item.count.toFloat() / item.maxCount.coerceAtLeast(1)) * 100).toInt()}%",
                    if (item == top) "MOST REPEATED EXERCISE" else "TRACKED EXERCISE",
                ),
            )
        }
    }
}

@Composable
private fun ExerciseFrequencyRow(
    item: ExerciseFrequencyData,
    onClick: () -> Unit,
) {
    val name = item.exerciseName.uppercase()
    val count = item.count
    val maxCount = item.maxCount
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statsTag("stats-frequency-row-${statsSlug(item.exerciseName)}")
            .semantics { contentDescription = "${item.fullExerciseName} exercise frequency" }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Exercise name (marquee if too long)
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            modifier = Modifier
                .weight(0.44f)
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    velocity = 30.dp,
                ),
            maxLines = 1,
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Bar
        Box(
            modifier = Modifier
                .weight(0.56f)
                .height(10.dp)
                .background(SurfaceHighest, shape = RectangleShape),
        ) {
            val fraction = if (maxCount > 0) {
                (count.toFloat() / maxCount).coerceIn(0f, 1f)
            } else {
                0f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .background(Blood, shape = RectangleShape),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Count
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            modifier = Modifier.width(24.dp),
        )
    }
}
