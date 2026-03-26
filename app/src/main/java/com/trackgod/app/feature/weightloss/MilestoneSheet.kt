package com.trackgod.app.feature.weightloss

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

/**
 * Bottom sheet for adding a new milestone to the active goal.
 *
 * @param startingWeight The starting weight of the active goal (upper bound).
 * @param targetWeight The target weight of the active goal (lower bound).
 * @param weightUnit Display unit (kg or lbs).
 * @param onSave Callback with (targetWeight, description).
 * @param onDismiss Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneSheet(
    startingWeight: Float,
    targetWeight: Float,
    weightUnit: String,
    onSave: (Float, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var milestoneWeight by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val parsedWeight = milestoneWeight.replace(",", ".").toFloatOrNull()
    val isValid = parsedWeight != null
            && parsedWeight < startingWeight
            && parsedWeight >= targetWeight

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
                text = "ADD MILESTONE",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = String.format(java.util.Locale.US, "BETWEEN %.1f AND %.1f %s",
                    targetWeight, startingWeight, weightUnit.uppercase()
                ),
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Milestone Weight
            TrackGodTextField(
                value = milestoneWeight,
                onValueChange = { milestoneWeight = it },
                label = "MILESTONE WEIGHT ($weightUnit)",
                placeholder = "0.0",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            TrackGodTextField(
                value = description,
                onValueChange = { description = it },
                label = "DESCRIPTION (OPTIONAL)",
                placeholder = "e.g. Halfway there!",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save
            TrackGodButton(
                text = "SAVE MILESTONE",
                onClick = {
                    val w = milestoneWeight.replace(",", ".").toFloatOrNull() ?: return@TrackGodButton
                    val desc = description.ifBlank { null }
                    onSave(w, desc)
                },
                enabled = isValid,
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
