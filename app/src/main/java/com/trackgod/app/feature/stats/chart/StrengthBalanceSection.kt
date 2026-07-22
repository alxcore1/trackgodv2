package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import com.trackgod.app.feature.stats.StrengthBalanceData
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Horizontal bar chart showing strength balance across body regions.
 *
 * Each row: CATEGORY label | filled bar (#8B0000) + unfilled (#353534) | percentage.
 */
@Composable
fun StrengthBalanceSection(
    data: List<StrengthBalanceData>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val dominant = data.maxBy { it.percentage }
    val lowest = data.minBy { it.percentage }
    var selected by remember(data) { mutableStateOf<StrengthBalanceData?>(null) }
    Column(modifier = modifier) {
        Text(
            text = "STRENGTH BALANCE",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "DOMINANT GROUP ${dominant.category.uppercase()} · LOWEST ${lowest.category.uppercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        data.forEach { item ->
            StrengthBalanceRow(
                item = item,
                onClick = { selected = item },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        selected?.let { item ->
            DetailPanel(
                title = "${item.category.uppercase()} DETAIL",
                lines = listOf(
                    "SHARE ${item.percentage.toInt()}%",
                    "VOLUME ${formatCompact(item.volume)}",
                    if (item == dominant) "DOMINANT GROUP" else "BALANCE GROUP",
                ),
            )
        }
    }
}

@Composable
private fun StrengthBalanceRow(
    item: StrengthBalanceData,
    onClick: () -> Unit,
) {
    val label = item.category.uppercase()
    val percentage = item.percentage
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statsTag("stats-balance-row-${statsSlug(item.category)}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Category label (fixed width)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            modifier = Modifier.width(56.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .background(SurfaceHighest, shape = RectangleShape),
        ) {
            val fraction = (percentage / 100f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(12.dp)
                    .background(Blood, shape = RectangleShape),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Percentage
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            modifier = Modifier.width(36.dp),
        )
    }
}
