package com.trackgod.app.feature.workout.session

import com.trackgod.app.util.formatVolume
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary

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
    isSaving: Boolean = false,
    finishError: String? = null,
    onSave: (name: String, saveAsTemplate: Boolean) -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    var workoutName by remember { mutableStateOf(defaultName) }
    var saveAsTemplate by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = spacing.xl)
                .fillMaxWidth()
                .background(color = SurfaceLow, shape = RectangleShape)
                .padding(spacing.xl),
        ) {
            // Title
            Text(
                text = "RITUAL COMPLETE",
                style = MaterialTheme.typography.headlineLarge,
                color = BloodBright,
                letterSpacing = 4.sp,
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // Stats rows
            StatRow(label = "EXERCISES", value = exerciseCount.toString())
            Spacer(modifier = Modifier.height(spacing.sm))
            StatRow(label = "TOTAL SETS", value = totalSets.toString())
            Spacer(modifier = Modifier.height(spacing.sm))
            StatRow(label = "VOLUME", value = formatVolume(totalVolume))
            Spacer(modifier = Modifier.height(spacing.sm))
            StatRow(label = "DURATION", value = formatDuration(durationSeconds))

            Spacer(modifier = Modifier.height(spacing.xl))

            // Editable name field
            TrackGodTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = "WORKOUT NAME",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            if (showDiscardConfirm) {
                // Discard confirmation
                Text(
                    text = "DISCARD THIS WORKOUT?",
                    style = MaterialTheme.typography.labelLarge,
                    color = BloodBright,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(spacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
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
                // Zero-sets warning + back option
                if (totalSets <= 0) {
                    Text(
                        text = "LOG AT LEAST ONE SET",
                        style = MaterialTheme.typography.labelMedium,
                        color = BloodBright,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing.sm),
                    )

                    // Back to workout — primary action when no sets
                    TrackGodButton(
                        text = "BACK TO WORKOUT",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(spacing.sm))

                    // Discard as secondary option
                    TrackGodButton(
                        text = "DISCARD",
                        onClick = { showDiscardConfirm = true },
                        variant = ButtonVariant.Ghost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    // Normal action buttons when sets exist
                    // Back to workout option
                    TrackGodButton(
                        text = "BACK TO WORKOUT",
                        onClick = onDismiss,
                        variant = ButtonVariant.Ghost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing.sm),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        TrackGodButton(
                            text = "DISCARD",
                            onClick = { showDiscardConfirm = true },
                            variant = ButtonVariant.Ghost,
                            modifier = Modifier.weight(1f),
                        )
                        TrackGodButton(
                            text = "SAVE",
                            onClick = { onSave(workoutName, saveAsTemplate) },
                            enabled = workoutName.isNotBlank() && !isSaving,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Save as template toggle
                Spacer(modifier = Modifier.height(spacing.sm))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { saveAsTemplate = !saveAsTemplate }
                        .padding(vertical = spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (saveAsTemplate) com.trackgod.app.ui.theme.Blood
                                else com.trackgod.app.ui.theme.SurfaceLow,
                                RectangleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (saveAsTemplate) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = com.trackgod.app.ui.theme.TextPrimary,
                            )
                        }
                    }
                    Text(
                        text = "SAVE AS RITUAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = com.trackgod.app.ui.theme.TextTertiary,
                    )
                }

                if (finishError != null) {
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Text(
                        text = finishError,
                        style = MaterialTheme.typography.labelLarge,
                        color = com.trackgod.app.ui.theme.BloodBright,
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
            style = MaterialTheme.typography.labelLarge,
            color = TextTertiary,
        )
        Text(
            text = value,
            color = TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

// ── Formatting helpers ───────────────────────────────────────────────────────

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
