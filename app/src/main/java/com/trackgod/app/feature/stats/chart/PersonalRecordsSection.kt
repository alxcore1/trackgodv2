package com.trackgod.app.feature.stats.chart

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
import androidx.compose.ui.unit.dp
import com.trackgod.app.feature.stats.PersonalRecordData
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

/**
 * Row of up to 3 Personal Record cards showing top estimated 1RMs.
 */
@Composable
fun PersonalRecordsSection(
    records: List<PersonalRecordData>,
    weightUnit: String,
    modifier: Modifier = Modifier,
) {
    if (records.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "PERSONAL RECORDS",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Show top 3
            records.take(3).forEach { pr ->
                TrackGodCard(
                    modifier = Modifier.weight(1f),
                ) {
                    // Exercise name
                    Text(
                        text = pr.exerciseName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                        maxLines = 1,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Estimated 1RM value
                    Text(
                        text = formatWeight(pr.estimated1rm),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                    )

                    // Unit
                    Text(
                        text = weightUnit.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Actual weight x reps
                    Text(
                        text = "${formatWeight(pr.weight)} x ${pr.reps}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                    )
                }
            }

            // If fewer than 3 records, fill remaining space
            val remaining = 3 - records.take(3).size
            repeat(remaining) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun formatWeight(value: Float): String {
    return if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        String.format("%.1f", value)
    }
}
