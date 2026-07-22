package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.VolumeDataPoint
import com.trackgod.app.feature.stats.VolumeInsight
import com.trackgod.app.ui.theme.TextTertiary

@Composable
fun VolumeChart(
    data: List<VolumeDataPoint>,
    insight: VolumeInsight = VolumeInsight(),
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val displayData = if (data.size > 12) data.takeLast(12) else data
    val average = insight.average.takeIf { it > 0f } ?: displayData.map { it.volume }.average().toFloat()
    val bestVolume = insight.bestVolume.takeIf { it > 0f } ?: displayData.maxOf { it.volume }
    val bestLabel = insight.bestLabel.ifBlank { displayData.maxBy { it.volume }.label }
    var selected by remember(data) { mutableStateOf<VolumeDataPoint?>(null) }

    ChartShell(
        title = "VOLUME PROGRESSION",
        summary = "BEST WEEK $bestLabel · AVG ${formatCompact(average)} · ${deltaLabel(insight.deltaPercent)}",
        modifier = modifier,
        legend = {
            LegendRow(
                listOf(
                    "VOLUME" to normalChartColor(),
                    "BEST" to bestChartColor(),
                    "AVG" to baselineColor(),
                ),
            )
        },
        detail = selected?.let { point ->
            {
                val index = displayData.indexOf(point)
                val previous = displayData.getOrNull(index - 1)
                val delta = if (previous != null && previous.volume > 0f) {
                    ((point.volume - previous.volume) / previous.volume) * 100f
                } else {
                    null
                }
                DetailPanel(
                    title = "${point.label} DETAIL",
                    lines = listOf(
                        "VOLUME ${formatCompact(point.volume)}",
                        "PREVIOUS ${deltaLabel(delta)}",
                        if (point.volume >= average) "ABOVE AVERAGE" else "BELOW AVERAGE",
                    ),
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(baselineColor(), RectangleShape),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                val maxVolume = displayData.maxOf { it.volume }.coerceAtLeast(1f)
                displayData.forEach { point ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .semantics {
                                    contentDescription = "${point.label} volume ${formatCompact(point.volume)}"
                                }
                                .statsTag("stats-volume-bar-${point.label}")
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { selected = point },
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            val fraction = (point.volume / maxVolume).coerceIn(0f, 1f)
                            val barColor = if (point.volume == bestVolume) bestChartColor() else normalChartColor()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                                    .background(if (point.volume > 0f) barColor else baselineColor(), RectangleShape),
                            )
                            val avgFraction = (average / maxVolume).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(avgFraction)
                                    .padding(top = 139.dp)
                                    .background(baselineColor(), RectangleShape),
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private fun deltaLabel(delta: Float?): String {
    return if (delta == null) "VOLUME BASELINE" else {
        val direction = if (delta >= 0f) "UP" else "DOWN"
        "$direction %.0f%%".format(kotlin.math.abs(delta))
    }
}
