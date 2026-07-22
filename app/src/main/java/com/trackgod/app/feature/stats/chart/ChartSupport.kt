package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHigh
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

@Composable
internal fun ChartShell(
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    legend: @Composable (() -> Unit)? = null,
    detail: @Composable (() -> Unit)? = null,
    body: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$title chart" },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )
        if (summary.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.labelMedium,
                color = BloodBright,
            )
        }
        if (legend != null) {
            Spacer(modifier = Modifier.height(10.dp))
            legend()
        }
        Spacer(modifier = Modifier.height(12.dp))
        body()
        if (detail != null) {
            Spacer(modifier = Modifier.height(10.dp))
            detail()
        }
    }
}

@Composable
internal fun DetailPanel(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceHigh, RectangleShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )
        lines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
        }
    }
}

@Composable
internal fun LegendRow(items: List<Pair<String, androidx.compose.ui.graphics.Color>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { (label, color) ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, RectangleShape),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                )
            }
        }
    }
}

internal fun statsSlug(value: String): String {
    return value.lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}

internal fun formatCompact(value: Float): String {
    return when {
        value >= 1_000_000f -> "%.1fM".format(value / 1_000_000f)
        value >= 1_000f -> "%.1fK".format(value / 1_000f)
        value == value.toLong().toFloat() -> value.toLong().toString()
        else -> "%.1f".format(value)
    }
}

internal fun baselineColor() = SurfaceHighest
internal fun normalChartColor() = Blood
internal fun bestChartColor() = StatsBestColor

private val StatsBestColor = Color(0xFFB3271F)

internal fun Modifier.statsTag(tag: String): Modifier = testTag(tag)
