package com.trackgod.app.feature.history

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.NumberInput
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

@Composable
fun EditWorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditWorkoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MetalTextureBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Void)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextTertiary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateBack() },
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "EDIT WORKOUT",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f),
                )
            }

            // Workout name
            TrackGodTextField(
                value = state.workoutName,
                onValueChange = viewModel::updateWorkoutName,
                label = "WORKOUT NAME",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Exercise list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                state.exercises.forEach { editableExercise ->
                    // Exercise header
                    item(key = "header_${editableExercise.exercise.id}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = editableExercise.exercise.name.uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                )
                                Text(
                                    text = "${editableExercise.exercise.category} · ${editableExercise.exercise.equipmentType}".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    letterSpacing = 2.sp,
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete exercise",
                                tint = TextTertiary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { viewModel.deleteExercise(editableExercise.exercise.id) },
                            )
                        }
                    }

                    // Sets for this exercise
                    val activeSets = editableExercise.sets.filter { !it.isDeleted }
                    items(
                        items = activeSets,
                        key = { it.id },
                    ) { set ->
                        if (state.editingSetId == set.id) {
                            // Inline edit mode
                            EditSetRow(
                                weightValue = state.editWeight,
                                repsValue = state.editReps,
                                weightUnit = state.weightUnit,
                                weightIncrement = state.weightIncrement,
                                onWeightChanged = viewModel::updateEditWeight,
                                onRepsChanged = viewModel::updateEditReps,
                                onSave = viewModel::saveSetEdit,
                                onCancel = viewModel::cancelEditingSet,
                            )
                        } else {
                            // Display mode
                            SetDisplayRow(
                                setNumber = set.setNumber,
                                weight = set.weight,
                                reps = set.reps,
                                weightUnit = state.weightUnit,
                                isModified = set.isModified,
                                onClick = { viewModel.startEditingSet(set) },
                                onDelete = { viewModel.deleteSet(set.id) },
                            )
                        }
                    }
                }
            }

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Void)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TrackGodButton(
                    text = "CANCEL",
                    onClick = onNavigateBack,
                    variant = ButtonVariant.Ghost,
                    modifier = Modifier.weight(1f),
                )
                TrackGodButton(
                    text = if (state.isSaving) "SAVING..." else "SAVE",
                    onClick = { viewModel.saveChanges { onNavigateBack() } },
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SetDisplayRow(
    setNumber: Int,
    weight: Float,
    reps: Int,
    weightUnit: String,
    isModified: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "SET $setNumber",
            color = TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.width(60.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        val weightStr = if (weight % 1f == 0f) "${weight.toInt()}" else String.format(Locale.US, "%.1f", weight)
        Text(
            text = "$weightStr${weightUnit.uppercase()} x $reps",
            color = if (isModified) BloodBright else TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete set",
            tint = TextTertiary,
            modifier = Modifier
                .size(18.dp)
                .clickable { onDelete() },
        )
    }
}

@Composable
private fun EditSetRow(
    weightValue: String,
    repsValue: String,
    weightUnit: String,
    weightIncrement: Float,
    onWeightChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLow)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NumberInput(
                value = weightValue,
                onValueChange = onWeightChanged,
                label = "WEIGHT",
                unit = weightUnit.uppercase(),
                step = weightIncrement,
                modifier = Modifier.weight(1f),
            )
            NumberInput(
                value = repsValue,
                onValueChange = onRepsChanged,
                label = "REPS",
                unit = "REPS",
                step = 1f,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TrackGodButton(
                text = "CANCEL",
                onClick = onCancel,
                variant = ButtonVariant.Ghost,
                modifier = Modifier.weight(1f),
            )
            TrackGodButton(
                text = "SAVE",
                onClick = onSave,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
