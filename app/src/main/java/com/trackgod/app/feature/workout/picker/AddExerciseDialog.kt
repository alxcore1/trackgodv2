package com.trackgod.app.feature.workout.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme

// ── Chip helper ──────────────────────────────────────────────────────────────

@Composable
private fun SelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label.uppercase(),
        color = if (selected) TextPrimary else TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = modifier
            .background(
                color = if (selected) Blood else SurfaceHighest,
                shape = RectangleShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

// ── Dialog ───────────────────────────────────────────────────────────────────

private val CATEGORIES = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core")
private val EQUIPMENT_TYPES = listOf("Barbell", "Dumbbell", "Machine", "Cable", "Bodyweight", "Other")

/**
 * Dialog for creating a custom exercise.
 *
 * @param onSave Called with (name, category, equipmentType) when user confirms.
 * @param onDismiss Called when user cancels or taps outside.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExerciseDialog(
    onSave: (name: String, category: String, equipmentType: String, brand: String?) -> Unit,
    onDismiss: () -> Unit,
    availableBrands: List<String> = emptyList(),
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedEquipment by remember { mutableStateOf<String?>(null) }
    var selectedBrand by remember { mutableStateOf<String?>(null) }

    val isMachine = selectedEquipment?.equals("Machine", ignoreCase = true) == true
    val canSave = name.isNotBlank() && selectedCategory != null && selectedEquipment != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(SurfaceLow, RectangleShape)
                .padding(24.dp),
        ) {
            // Title
            Text(
                text = "ADD CUSTOM EXERCISE",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Name field
            TrackGodTextField(
                value = name,
                onValueChange = { name = it },
                label = "Exercise Name",
                placeholder = "Enter name",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category label
            Text(
                text = "CATEGORY",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CATEGORIES.forEach { category ->
                    SelectionChip(
                        label = category,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Equipment type label
            Text(
                text = "EQUIPMENT TYPE",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EQUIPMENT_TYPES.forEach { equipment ->
                    SelectionChip(
                        label = equipment,
                        selected = selectedEquipment == equipment,
                        onClick = { selectedEquipment = equipment },
                    )
                }
            }

            // Brand selection (only visible when Machine is selected)
            if (isMachine && availableBrands.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "BRAND (OPTIONAL)",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableBrands.forEach { brand ->
                        SelectionChip(
                            label = brand,
                            selected = selectedBrand == brand,
                            onClick = {
                                selectedBrand = if (selectedBrand == brand) null else brand
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TrackGodButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = ButtonVariant.Ghost,
                )
                Spacer(modifier = Modifier.width(12.dp))
                TrackGodButton(
                    text = "Save",
                    onClick = {
                        if (canSave) {
                            onSave(name.trim(), selectedCategory!!, selectedEquipment!!, selectedBrand)
                        }
                    },
                    enabled = canSave,
                    variant = ButtonVariant.Primary,
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun AddExerciseDialogPreview() {
    TrackGodTheme {
        AddExerciseDialog(
            onSave = { _, _, _, _ -> },
            onDismiss = {},
        )
    }
}
