package com.trackgod.app.feature.weightloss

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void
import java.util.Calendar

/**
 * Bottom sheet for creating a new weight loss goal.
 *
 * @param currentWeight Pre-fill starting weight from profile or latest weigh-in.
 * @param weightUnit Display unit (kg or lbs).
 * @param onSave Callback with (startWeight, targetWeight, targetDate, weeklyGoal, motivation).
 * @param onDismiss Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupSheet(
    currentWeight: Float?,
    weightUnit: String,
    onSave: (Float, Float, String, Float?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var startWeight by remember { mutableStateOf(currentWeight?.let { "%.1f".format(it) } ?: "") }
    var targetWeight by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var weeklyGoal by remember { mutableStateOf("0.5") }
    var motivation by remember { mutableStateOf("") }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            // Title
            Text(
                text = "SET YOUR GOAL",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Starting Weight
            TrackGodTextField(
                value = startWeight,
                onValueChange = { startWeight = it },
                label = "STARTING WEIGHT ($weightUnit)",
                placeholder = "0.0",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Weight
            TrackGodTextField(
                value = targetWeight,
                onValueChange = { targetWeight = it },
                label = "TARGET WEIGHT ($weightUnit)",
                placeholder = "0.0",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Date
            SectionDivider(text = "TARGET DATE", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLow)
                    .clickable {
                        val cal = Calendar.getInstance()
                        // Default to 3 months from now
                        cal.add(Calendar.MONTH, 3)
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                targetDate = "%04d-%02d-%02d".format(year, month + 1, day)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = targetDate.ifBlank { "TAP TO SELECT" },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (targetDate.isBlank()) TextTertiary else TextPrimary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekly Goal
            TrackGodTextField(
                value = weeklyGoal,
                onValueChange = { weeklyGoal = it },
                label = "WEEKLY GOAL ($weightUnit/WEEK)",
                placeholder = "0.5",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Motivation
            TrackGodTextField(
                value = motivation,
                onValueChange = { motivation = it },
                label = "MOTIVATION (OPTIONAL)",
                placeholder = "Why are you doing this?",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Validation error
            val sw = startWeight.toFloatOrNull()
            val tw = targetWeight.toFloatOrNull()
            val isWeightValid = sw != null && tw != null && sw > tw
            val showWeightError = sw != null && tw != null && sw <= tw

            if (showWeightError) {
                Text(
                    text = "Starting weight must be greater than target weight",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save
            TrackGodButton(
                text = "SAVE GOAL",
                onClick = {
                    val startW = startWeight.toFloatOrNull() ?: return@TrackGodButton
                    val targetW = targetWeight.toFloatOrNull() ?: return@TrackGodButton
                    if (targetDate.isBlank()) return@TrackGodButton
                    if (startW <= targetW) return@TrackGodButton
                    val wg = weeklyGoal.toFloatOrNull()
                    val mot = motivation.ifBlank { null }
                    onSave(startW, targetW, targetDate, wg, mot)
                },
                enabled = isWeightValid && targetDate.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel
            TrackGodButton(
                text = "CANCEL",
                onClick = onDismiss,
                variant = ButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
