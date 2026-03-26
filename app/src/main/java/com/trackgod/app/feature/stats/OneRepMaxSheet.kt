package com.trackgod.app.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.component.NumberInput
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneRepMaxSheet(
    weightUnit: String,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }

    val weight = weightInput.replace(",", ".").toFloatOrNull() ?: 0f
    val reps = repsInput.toIntOrNull() ?: 0
    val unit = weightUnit.uppercase()

    // Calculate 1RM using 3 formulas
    val epley = if (reps == 1) weight else if (reps > 0 && weight > 0) weight * (1 + reps / 30f) else 0f
    val brzycki = if (reps == 1) weight else if (reps in 2..36 && weight > 0) weight * 36f / (37f - reps) else 0f
    val lander = if (reps == 1) weight else if (reps > 0 && weight > 0) 100f * weight / (101.3f - 2.67f * reps) else 0f
    val average = if (epley > 0 && brzycki > 0 && lander > 0) (epley + brzycki + lander) / 3f else 0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Void,
        shape = RectangleShape,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            Text(
                text = "1RM CALCULATOR",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ENTER WEIGHT AND REPS TO ESTIMATE YOUR ONE REP MAX",
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Weight + Reps inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NumberInput(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = "WEIGHT",
                    unit = unit,
                    step = 2.5f,
                    modifier = Modifier.weight(1f),
                )
                NumberInput(
                    value = repsInput,
                    onValueChange = { v -> if (v.isEmpty() || v.matches(Regex("^\\d*$"))) repsInput = v },
                    label = "REPS",
                    unit = "REPS",
                    step = 1f,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Results
            if (average > 0f) {
                Text(
                    text = "ESTIMATED 1RM",
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                ResultRow("EPLEY", epley, unit)
                Spacer(modifier = Modifier.height(6.dp))
                ResultRow("BRZYCKI", brzycki, unit)
                Spacer(modifier = Modifier.height(6.dp))
                ResultRow("LANDER", lander, unit)

                Spacer(modifier = Modifier.height(12.dp))

                // Average (highlighted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLow)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "AVERAGE",
                        color = BloodBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", average)} $unit",
                        color = BloodBright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResultRow(label: String, value: Float, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Text(
            text = "${String.format(java.util.Locale.US, "%.1f", value)} $unit",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
        )
    }
}
