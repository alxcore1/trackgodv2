package com.trackgod.app.feature.workout.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void
import com.trackgod.app.ui.theme.VoidDeep

/**
 * Dialog shown when the user finishes a workout.
 *
 * Displays session stats and allows the user to name the workout
 * before saving or discarding it.
 */
@Composable
fun WorkoutCompleteDialog(
    exerciseCount: Int,
    totalSets: Int,
    totalVolume: Float,
    durationSeconds: Long,
    defaultName: String,
    onSave: (name: String) -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    var workoutName by remember { mutableStateOf(defaultName) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .background(color = SurfaceLow, shape = RectangleShape)
                .padding(24.dp),
        ) {
            // Title
            Text(
                text = "RITUAL COMPLETE",
                style = MaterialTheme.typography.headlineLarge,
                color = BloodBright,
                letterSpacing = 4.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats rows
            StatRow(label = "EXERCISES", value = exerciseCount.toString())
            Spacer(modifier = Modifier.height(8.dp))
            StatRow(label = "TOTAL SETS", value = totalSets.toString())
            Spacer(modifier = Modifier.height(8.dp))
            StatRow(label = "VOLUME", value = formatVolume(totalVolume))
            Spacer(modifier = Modifier.height(8.dp))
            StatRow(label = "DURATION", value = formatDuration(durationSeconds))

            Spacer(modifier = Modifier.height(24.dp))

            // Workout name label
            Text(
                text = "WORKOUT NAME",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Editable name field
            BasicTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = VoidDeep, shape = RectangleShape)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary,
                ),
                singleLine = true,
                cursorBrush = SolidColor(Blood),
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (showDiscardConfirm) {
                // Discard confirmation
                Text(
                    text = "DISCARD THIS WORKOUT?",
                    color = BloodBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TrackGodButton(
                        text = "KEEP",
                        onClick = { showDiscardConfirm = false },
                        variant = ButtonVariant.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                    TrackGodButton(
                        text = "DISCARD",
                        onClick = onDiscard,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                // Zero-sets warning
                if (totalSets <= 0) {
                    Text(
                        text = "LOG AT LEAST ONE SET",
                        color = BloodBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                }

                // Normal action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TrackGodButton(
                        text = "DISCARD",
                        onClick = { showDiscardConfirm = true },
                        variant = ButtonVariant.Ghost,
                        modifier = Modifier.weight(1f),
                    )
                    TrackGodButton(
                        text = "SAVE WORKOUT",
                        onClick = { onSave(workoutName) },
                        enabled = workoutName.isNotBlank() && totalSets > 0,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ── Stat row helper ──────────────────────────────────────────────────────────

@Composable
private fun StatRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Text(
            text = value,
            color = TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

// ── Formatting helpers ───────────────────────────────────────────────────────

private fun formatVolume(volume: Float): String {
    return when {
        volume >= 1_000_000 -> "%.1fM".format(volume / 1_000_000f)
        volume >= 1_000 -> "%,.0f".format(volume)
        else -> "%.0f".format(volume)
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        "%d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}
