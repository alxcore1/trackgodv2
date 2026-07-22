package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.MuscleGroupData
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

@Composable
fun MuscleGroupChart(
    data: List<MuscleGroupData>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val top = data.maxBy { it.percentage }
    val low = data.minBy { it.percentage }
    var selected by remember(data) { mutableStateOf<MuscleGroupData?>(null) }

    ChartShell(
        title = "MUSCLE LOAD DISTRIBUTION",
        summary = "TOP CATEGORY ${top.category.uppercase()} · UNDERREPRESENTED ${low.category.uppercase()}",
        modifier = modifier,
        detail = selected?.let { item ->
            {
                DetailPanel(
                    title = "${item.category.uppercase()} DETAIL",
                    lines = listOf(
                        "LOAD SHARE ${item.percentage.toInt()}%",
                        "VOLUME ${formatCompact(item.volume)}",
                        if (item == low) "UNDERREPRESENTED CATEGORY" else "TRAINED CATEGORY",
                    ),
                )
            }
        },
    ) {
        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statsTag("stats-muscle-row-${statsSlug(item.category)}")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { selected = item },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.category.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    modifier = Modifier.width(100.dp),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .background(SurfaceHighest, RectangleShape),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((item.percentage / 100f).coerceIn(0f, 1f))
                            .height(12.dp)
                            .background(if (item == top) bestChartColor() else Blood, RectangleShape),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.percentage.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                    modifier = Modifier.width(38.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
