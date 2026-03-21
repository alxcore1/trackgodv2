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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.NumberInput
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.Void

/**
 * Bottom sheet for logging a new weigh-in.
 *
 * @param lastWeight Pre-fill from the previous weigh-in.
 * @param weightUnit Display unit (kg or lbs).
 * @param onLog Callback with (weight, note, photoUri).
 * @param onDismiss Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeighInSheet(
    lastWeight: Float?,
    weightUnit: String,
    onLog: (Float, String?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var weight by remember {
        mutableStateOf(lastWeight?.let { "%.1f".format(it) } ?: "")
    }
    var note by remember { mutableStateOf("") }

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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title
            Text(
                text = "LOG WEIGH-IN",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Weight input
            NumberInput(
                value = weight,
                onValueChange = { weight = it },
                label = "WEIGHT",
                unit = weightUnit.uppercase(),
                step = 0.1f,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Note
            TrackGodTextField(
                value = note,
                onValueChange = { note = it },
                label = "NOTE (OPTIONAL)",
                placeholder = "How are you feeling?",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo placeholder (Task 2 handles full implementation)
            TrackGodButton(
                text = "ADD PHOTO",
                onClick = { /* Placeholder for Task 2 */ },
                variant = ButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Log
            TrackGodButton(
                text = "LOG WEIGH-IN",
                onClick = {
                    val w = weight.toFloatOrNull() ?: return@TrackGodButton
                    val n = note.ifBlank { null }
                    onLog(w, n, null)
                },
                enabled = weight.toFloatOrNull() != null,
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
