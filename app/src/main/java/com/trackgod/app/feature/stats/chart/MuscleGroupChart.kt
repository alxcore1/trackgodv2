package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.MuscleGroupData
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * 2-column grid showing muscle load distribution percentages.
 *
 * Each cell shows category name + percentage. Sorted by percentage descending.
 */
@Composable
fun MuscleGroupChart(
    data: List<MuscleGroupData>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "MUSCLE LOAD DISTRIBUTION",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2-column grid
        val rows = data.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 6.dp),
                    ) {
                        Text(
                            text = item.category.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary,
                        )
                        Text(
                            text = "${item.percentage.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BloodBright,
                        )
                    }
                }
                // If odd number, fill remaining space
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
