package com.trackgod.app.feature.stats.chart

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    val top = records.maxBy { it.estimated1rm }
    var selected by remember(records) { mutableStateOf<PersonalRecordData?>(null) }
    Column(modifier = modifier) {
        Text(
            text = "PERSONAL RECORDS",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "TOP PR ${top.exerciseName.uppercase()} · EST. 1RM ${formatWeight(top.estimated1rm)} ${weightUnit.uppercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Show top 3
            records.take(3).forEach { pr ->
                TrackGodCard(
                    modifier = Modifier
                        .weight(1f)
                        .statsTag("stats-pr-card-${statsSlug(pr.exerciseName)}")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { selected = pr },
                ) {
                    // Exercise name (marquee if too long)
                    Text(
                        text = pr.exerciseName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            velocity = 30.dp,
                        ),
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

        selected?.let { pr ->
            Spacer(modifier = Modifier.height(10.dp))
            DetailPanel(
                title = "${pr.exerciseName.uppercase()} DETAIL",
                lines = listOf(
                    "EST. 1RM ${formatWeight(pr.estimated1rm)} ${weightUnit.uppercase()}",
                    "SOURCE SET ${formatWeight(pr.weight)} x ${pr.reps}",
                    "EXERCISE ${pr.exerciseName.uppercase()}",
                ),
            )
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
