package com.trackgod.app.feature.workout.session

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import com.trackgod.app.core.database.entity.ExerciseEntity
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.NumberInput
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodCard
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.Void

// ── Screen (ViewModel-wired entry point) ────────────────────────────────────

@Composable
fun WorkoutSessionScreen(
    workoutId: Long,
    onNavigateToExercisePicker: () -> Unit,
    onNavigateBack: () -> Unit = {},
    onWorkoutComplete: () -> Unit,
    onWorkoutDiscarded: () -> Unit,
    viewModel: WorkoutSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Vibrate when rest timer completes
    LaunchedEffect(state.restTimerCompleted) {
        if (state.restTimerCompleted) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                mgr?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
            viewModel.consumeRestTimerCompleted()
        }
    }

    // Navigate away when workout is confirmed or discarded
    LaunchedEffect(state.showCompleteDialog) {
        // Handled via dialog callbacks below
    }

    WorkoutSessionContent(
        state = state,
        onWeightChanged = viewModel::updateWeight,
        onRepsChanged = viewModel::updateReps,
        onRpeChanged = viewModel::updateRpe,
        onRirChanged = viewModel::updateRir,
        onNoteChanged = viewModel::updateNote,
        onLogSet = viewModel::logSet,
        onEditSet = viewModel::editSet,
        onSaveEdit = viewModel::saveEdit,
        onCancelEdit = viewModel::cancelEdit,
        onDeleteSet = viewModel::deleteSet,
        onSkipRest = viewModel::skipRestTimer,
        onFinishWorkout = viewModel::finishWorkout,
        onDismissCompleteDialog = viewModel::dismissCompleteDialog,
        onConfirmFinish = { name ->
            viewModel.confirmFinish(name) { onWorkoutComplete() }
        },
        onDiscardWorkout = {
            viewModel.discardWorkout()
            onWorkoutDiscarded()
        },
        onNavigateBack = onNavigateBack,
        onNavigateToExercisePicker = onNavigateToExercisePicker,
        onCloseExercise = viewModel::closeExercise,
        onResumeExercise = { exercise -> viewModel.selectExercise(exercise) },
        generateWorkoutName = viewModel::generateWorkoutName,
    )
}

// ── Content (stateless, previewable) ────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutSessionContent(
    state: WorkoutSessionState,
    onWeightChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onRpeChanged: (String) -> Unit,
    onRirChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onLogSet: () -> Unit,
    onEditSet: (Long) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteSet: (Long) -> Unit,
    onSkipRest: () -> Unit,
    onFinishWorkout: () -> Unit,
    onDismissCompleteDialog: () -> Unit,
    onConfirmFinish: (String) -> Unit,
    onDiscardWorkout: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToExercisePicker: () -> Unit,
    onCloseExercise: () -> Unit,
    onResumeExercise: (ExerciseEntity) -> Unit,
    generateWorkoutName: () -> String,
) {
    var showBackConfirm by remember { mutableStateOf(false) }
    var deleteSetId by remember { mutableStateOf<Long?>(null) }

    // Intercept system back button during active workout
    BackHandler {
        showBackConfirm = true
    }

    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Header
        SessionHeader()

        // Stats panel
        StatsPanel(
            exerciseCount = state.exerciseCount,
            setsCount = state.totalSetsCount,
            totalVolume = state.totalVolume,
            durationSeconds = state.sessionDurationSeconds,
            weightUnit = state.weightUnit,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Control buttons
        ControlButtons(
            onPause = { /* Pause not implemented yet */ },
            onEnd = onFinishWorkout,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main content area (scrollable)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (state.currentExercise == null) {
                // No exercise selected -- show session log + choose tile

                // Previously logged exercises in this session
                if (state.allExercisesInSession.isNotEmpty()) {
                    item {
                        SectionDivider(
                            text = "SESSION LOG",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(
                        items = state.allExercisesInSession,
                        key = { it.exercise.id },
                    ) { exerciseWithSets ->
                        SessionExerciseCard(
                            name = exerciseWithSets.exercise.name,
                            setCount = exerciseWithSets.setCount,
                            category = exerciseWithSets.exercise.category,
                            onClick = { onResumeExercise(exerciseWithSets.exercise) },
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    SectionDivider(
                        text = if (state.allExercisesInSession.isEmpty()) "SELECT AN EXERCISE" else "ADD ANOTHER",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ChooseExerciseTile(onClick = onNavigateToExercisePicker)
                }
            } else {
                // Exercise selected -- input area
                item {
                    ExerciseInputSection(
                        state = state,
                        onWeightChanged = onWeightChanged,
                        onRepsChanged = onRepsChanged,
                        onRpeChanged = onRpeChanged,
                        onRirChanged = onRirChanged,
                        onNoteChanged = onNoteChanged,
                        onLogSet = onLogSet,
                        onSaveEdit = onSaveEdit,
                        onCancelEdit = onCancelEdit,
                    )
                }

                // Rest timer
                if (state.isRestTimerRunning) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        RestTimerSection(
                            restTimeRemaining = state.restTimeRemaining,
                            onSkip = onSkipRest,
                        )
                    }
                }

                // Completed sets
                if (state.completedSets.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionDivider(
                            text = "COMPLETED SETS",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(
                        items = state.completedSets,
                        key = { _, set -> set.id },
                    ) { index, set ->
                        CompletedSetRow(
                            setNumber = index + 1,
                            weight = set.weight,
                            reps = set.reps,
                            note = set.note,
                            weightUnit = state.weightUnit,
                            weightIncrement = state.weightIncrement,
                            isEditing = state.editingSetId == set.id,
                            onClick = { onEditSet(set.id) },
                            onLongClick = { deleteSetId = set.id },
                        )
                    }
                }

                // Close exercise button (return to session hub)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        TrackGodButton(
                            text = "CLOSE EXERCISE",
                            onClick = onCloseExercise,
                            variant = ButtonVariant.Ghost,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    } // MetalTextureBackground

    // Completion dialog
    if (state.showCompleteDialog) {
        WorkoutCompleteDialog(
            exerciseCount = state.exerciseCount,
            totalSets = state.totalSetsCount,
            totalVolume = state.totalVolume,
            durationSeconds = state.sessionDurationSeconds,
            defaultName = generateWorkoutName(),
            onSave = onConfirmFinish,
            onDiscard = onDiscardWorkout,
            onDismiss = onDismissCompleteDialog,
        )
    }

    // Back button confirmation dialog
    if (showBackConfirm) {
        ConfirmationDialog(
            title = "LEAVE WORKOUT?",
            message = "Your workout is still in progress. You can resume it later from the altar.",
            confirmText = "LEAVE",
            dismissText = "STAY",
            onConfirm = {
                showBackConfirm = false
                onNavigateBack()
            },
            onDismiss = { showBackConfirm = false },
        )
    }

    // Delete set confirmation dialog
    val pendingDeleteId = deleteSetId
    if (pendingDeleteId != null) {
        ConfirmationDialog(
            title = "DELETE THIS SET?",
            message = "This action cannot be undone.",
            confirmText = "DELETE",
            dismissText = "CANCEL",
            onConfirm = {
                onDeleteSet(pendingDeleteId)
                deleteSetId = null
            },
            onDismiss = { deleteSetId = null },
        )
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun SessionHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liveDotAlpha",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Void)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "WORKOUT",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.weight(1f),
        )

        // LIVE indicator with pulsing red dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(liveDotAlpha)
                    .background(Blood, shape = RectangleShape),
            )
            Text(
                text = "LIVE",
                color = BloodBright,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
        }
    }
}

// ── Stats Panel ─────────────────────────────────────────────────────────────

@Composable
private fun StatsPanel(
    exerciseCount: Int,
    setsCount: Int,
    totalVolume: Float,
    durationSeconds: Long,
    weightUnit: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLow)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatColumn(label = "EXERCISES", value = exerciseCount.toString())
        StatColumn(label = "SETS", value = setsCount.toString())
        StatColumn(label = "VOLUME", value = formatVolumeShort(totalVolume))
        StatColumn(label = "TIME", value = formatTimeMMSS(durationSeconds), monospace = true)
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    monospace: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = if (monospace) FontFamily.Monospace else null,
        )
    }
}

// ── Control Buttons ─────────────────────────────────────────────────────────

@Composable
private fun ControlButtons(
    onPause: () -> Unit,
    onEnd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TrackGodButton(
            text = "PAUSE",
            onClick = onPause,
            icon = Icons.Default.Pause,
            modifier = Modifier.weight(1f),
        )
        TrackGodButton(
            text = "END",
            onClick = onEnd,
            variant = ButtonVariant.Secondary,
            icon = Icons.Default.Stop,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Choose Exercise Tile ────────────────────────────────────────────────────

@Composable
private fun SessionExerciseCard(
    name: String,
    setCount: Int,
    category: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(SurfaceLow, RectangleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Text(
                text = "${category.uppercase()} · $setCount SETS",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                letterSpacing = 2.sp,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Resume",
            tint = BloodBright,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ChooseExerciseTile(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(SurfaceLow, shape = RectangleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = BloodBright,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "+ CHOOSE EXERCISE",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "BROWSE MANUALLY",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
        }
    }
}

// ── Exercise Input Section ──────────────────────────────────────────────────

@Composable
private fun ExerciseInputSection(
    state: WorkoutSessionState,
    onWeightChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onRpeChanged: (String) -> Unit,
    onRirChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onLogSet: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    val exercise = state.currentExercise ?: return
    val isEditing = state.editingSetId != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        // Exercise name
        Text(
            text = exercise.name.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Category + equipment
        Text(
            text = "${exercise.category} / ${exercise.equipmentType}".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            letterSpacing = 2.sp,
        )

        // Smart default hint
        if (state.lastSessionHint != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state.lastSessionHint,
                style = MaterialTheme.typography.labelMedium,
                color = BloodBright,
                letterSpacing = 1.sp,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weight + Reps inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NumberInput(
                value = state.weightInput,
                onValueChange = onWeightChanged,
                label = "WEIGHT",
                unit = state.weightUnit.uppercase(),
                step = state.weightIncrement,
                modifier = Modifier.weight(1f),
            )
            NumberInput(
                value = state.repsInput,
                onValueChange = onRepsChanged,
                label = "REPS",
                unit = "REPS",
                step = 1f,
                modifier = Modifier.weight(1f),
            )
        }

        // RPE / RIR inputs (conditional)
        if (state.showRpe || state.showRir) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                if (state.showRpe) {
                    NumberInput(
                        value = state.rpeInput?.toString() ?: "",
                        onValueChange = onRpeChanged,
                        label = "RPE (1-10)",
                        unit = "RPE",
                        step = 1f,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (state.showRir) {
                    NumberInput(
                        value = state.rirInput?.toString() ?: "",
                        onValueChange = onRirChanged,
                        label = "RIR (0-5)",
                        unit = "RIR",
                        step = 1f,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Optional note
        TrackGodTextField(
            value = state.noteInput,
            onValueChange = onNoteChanged,
            label = "NOTE",
            placeholder = "OPTIONAL NOTE...",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Log / Save / Cancel buttons
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TrackGodButton(
                    text = "CANCEL",
                    onClick = onCancelEdit,
                    variant = ButtonVariant.Ghost,
                    modifier = Modifier.weight(1f),
                )
                TrackGodButton(
                    text = "SAVE EDIT",
                    onClick = onSaveEdit,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            // Input validation error
            state.inputError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelMedium,
                    color = BloodBright,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            TrackGodButton(
                text = "LOG SET",
                onClick = onLogSet,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Rest Timer Section ──────────────────────────────────────────────────────

@Composable
private fun RestTimerSection(
    restTimeRemaining: Int,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionDivider(
            text = "REST TIMER",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Large countdown
        Text(
            text = formatRestTime(restTimeRemaining),
            style = MaterialTheme.typography.displaySmall,
            color = BloodBright,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        TrackGodButton(
            text = "SKIP",
            onClick = onSkip,
            variant = ButtonVariant.Ghost,
        )
    }
}

// ── Completed Set Row ───────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompletedSetRow(
    setNumber: Int,
    weight: Float,
    reps: Int,
    note: String?,
    weightUnit: String,
    weightIncrement: Float,
    isEditing: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val weightStr = if (weightIncrement % 1f == 0f) {
        weight.toInt().toString()
    } else {
        "%.1f".format(weight)
    }

    TrackGodCard(
        accentBorder = isEditing,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SET $setNumber",
                color = TextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            Text(
                text = "$weightStr${weightUnit.uppercase()} x $reps",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
        }
        if (!note.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.uppercase(),
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

// ── Formatting Helpers ──────────────────────────────────────────────────────

private fun formatVolumeShort(volume: Float): String {
    return when {
        volume >= 1_000_000 -> "%.1fM".format(volume / 1_000_000f)
        volume >= 1_000 -> "%.1fK".format(volume / 1_000f)
        else -> "%.0f".format(volume)
    }
}

private fun formatTimeMMSS(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

private fun formatRestTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
