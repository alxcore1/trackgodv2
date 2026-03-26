package com.trackgod.app.feature.workout.session

import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.window.Dialog
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.trackgod.app.ui.theme.VoidDeep

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

    // Request notification permission on Android 13+ (needed for foreground service + rest timer)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notifPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { /* granted or not, we proceed either way */ }

        LaunchedEffect(Unit) {
            val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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

    // PR celebration vibration
    LaunchedEffect(state.prMessage) {
        if (state.prMessage != null) {
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
                        VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 100, 80, 200), -1)
                    )
                }
            }
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
        onPauseRest = viewModel::pauseRestTimer,
        onResumeRest = viewModel::resumeRestTimer,
        onAdjustRest = viewModel::adjustRestTimer,
        onToggleRestTimerForSession = viewModel::toggleRestTimerForSession,
        onFinishWorkout = viewModel::finishWorkout,
        onDismissCompleteDialog = viewModel::dismissCompleteDialog,
        onConfirmFinish = { name, saveAsTemplate ->
            viewModel.confirmFinish(name, saveAsTemplate) { onWorkoutComplete() }
        },
        onDiscardWorkout = {
            viewModel.discardWorkout { onWorkoutDiscarded() }
        },
        onNavigateBack = onNavigateBack,
        onNavigateToExercisePicker = onNavigateToExercisePicker,
        onCloseExercise = viewModel::closeExercise,
        onResumeExercise = { exercise -> viewModel.selectExercise(exercise) },
        onCycleSetType = viewModel::cycleSetType,
        onAcceptSuperset = viewModel::acceptSuperset,
        onDeclineSuperset = viewModel::declineSuperset,
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
    onPauseRest: () -> Unit,
    onResumeRest: () -> Unit,
    onAdjustRest: (Int) -> Unit,
    onToggleRestTimerForSession: () -> Unit,
    onFinishWorkout: () -> Unit,
    onDismissCompleteDialog: () -> Unit,
    onConfirmFinish: (String, Boolean) -> Unit,
    onDiscardWorkout: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToExercisePicker: () -> Unit,
    onCloseExercise: () -> Unit,
    onResumeExercise: (ExerciseEntity) -> Unit,
    onCycleSetType: () -> Unit = {},
    onAcceptSuperset: () -> Unit = {},
    onDeclineSuperset: () -> Unit = {},
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
            .padding(top = 4.dp),
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
                        onCycleSetType = onCycleSetType,
                        onResumeExercise = onResumeExercise,
                    )
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
                            isPr = set.id in state.prSetIds,
                            setType = set.setType,
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
            finishError = state.finishError,
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

    // Superset offer dialog
    if (state.supersetOffer != null && state.supersetOfferNewId != null) {
        val offerExName = state.allExercisesInSession
            .firstOrNull { it.exercise.id == state.supersetOffer }?.exercise?.name?.uppercase() ?: "PREVIOUS"
        ConfirmationDialog(
            title = "SUPERSET?",
            message = "Group with $offerExName?",
            confirmText = "SUPERSET",
            dismissText = "SEPARATE",
            onConfirm = onAcceptSuperset,
            onDismiss = onDeclineSuperset,
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

    // Rest timer popup dialog
    if (state.isRestTimerRunning || state.isRestTimerPaused) {
        RestTimerDialog(
            timeRemaining = state.restTimeRemaining,
            isPaused = state.isRestTimerPaused,
            onSkip = onSkipRest,
            onPause = onPauseRest,
            onResume = onResumeRest,
            onAdjust = onAdjustRest,
            onDisableForSession = onToggleRestTimerForSession,
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
    onEnd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        TrackGodButton(
            text = "END",
            onClick = onEnd,
            variant = ButtonVariant.Secondary,
            icon = Icons.Default.Stop,
            modifier = Modifier.fillMaxWidth(),
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
    onCycleSetType: () -> Unit = {},
    onResumeExercise: (ExerciseEntity) -> Unit = {},
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

        // Superset quick-switch
        val ssGroup = state.supersetGroups.values.firstOrNull { exercise.id in it }
        if (ssGroup != null) {
            val partnerId = ssGroup.firstOrNull { it != exercise.id }
            val partnerName = state.allExercisesInSession
                .firstOrNull { it.exercise.id == partnerId }?.exercise?.name?.uppercase()
            if (partnerName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SS → $partnerName",
                    color = Blood,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (partnerId != null) onResumeExercise(
                            state.allExercisesInSession.first { it.exercise.id == partnerId }.exercise
                        )
                    },
                )
            }
        }

        // Progressive overload suggestion
        if (state.overloadHint != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SUGGESTED",
                color = TextTertiary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = state.overloadHint,
                color = BloodBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }

        // Previous session sets (expandable)
        if (state.lastSessionSets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LastSessionSets(
                sets = state.lastSessionSets,
                weightUnit = state.weightUnit,
                weightIncrement = state.weightIncrement,
                onSetTapped = { set ->
                    onWeightChanged(formatWeightForInput(set.weight, state.weightIncrement))
                    onRepsChanged(set.reps.toString())
                },
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
            Spacer(modifier = Modifier.width(16.dp))
            NumberInput(
                value = state.repsInput,
                onValueChange = onRepsChanged,
                label = "REPS",
                unit = "REPS",
                step = 1f,
                modifier = Modifier.weight(1f),
            )
        }

        // Plate calculator link
        var showPlateCalc by remember { mutableStateOf(false) }
        if (state.weightInput.isNotEmpty() && !isEditing) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "PLATE CALCULATOR",
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { showPlateCalc = true },
            )
        }
        if (showPlateCalc) {
            PlateCalculatorSheet(
                initialWeight = state.weightInput,
                weightUnit = state.weightUnit,
                onDismiss = { showPlateCalc = false },
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

            // Set type + LOG SET in one row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Set type cycle button
                val typeLabel = when (state.setTypeInput) {
                    "warmup" -> "WU"
                    "drop" -> "D"
                    "failure" -> "F"
                    else -> "W"
                }
                val typeBg = if (state.setTypeInput == "working") SurfaceLow else Blood.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(typeBg, RectangleShape)
                        .clickable { onCycleSetType() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = typeLabel,
                        color = if (state.setTypeInput == "working") TextTertiary else BloodBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                }
                TrackGodButton(
                    text = "LOG SET",
                    onClick = onLogSet,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Rest Timer Dialog ───────────────────────────────────────────────────────

@Composable
private fun RestTimerDialog(
    timeRemaining: Int,
    isPaused: Boolean,
    onSkip: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAdjust: (Int) -> Unit,
    onDisableForSession: () -> Unit,
) {
    Dialog(onDismissRequest = { /* user must skip or wait */ }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = VoidDeep, shape = RectangleShape)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title
            Text(
                text = "REST TIMER",
                color = TextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Large countdown
            Text(
                text = formatRestTime(timeRemaining),
                style = MaterialTheme.typography.displayLarge,
                color = if (isPaused) TextTertiary else BloodBright,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
            )

            if (isPaused) {
                Text(
                    text = "PAUSED",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // +/- 15s adjust buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TrackGodButton(
                    text = "-15s",
                    onClick = { onAdjust(-15) },
                    variant = ButtonVariant.Ghost,
                    modifier = Modifier.weight(1f),
                )
                TrackGodButton(
                    text = "+15s",
                    onClick = { onAdjust(15) },
                    variant = ButtonVariant.Ghost,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pause / Resume button
            TrackGodButton(
                text = if (isPaused) "RESUME" else "PAUSE",
                onClick = if (isPaused) onResume else onPause,
                variant = ButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Skip button
            TrackGodButton(
                text = "SKIP",
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Disable for session toggle
            Text(
                text = "DISABLE REST TIMER",
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onDisableForSession() },
            )
        }
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
    isPr: Boolean = false,
    setType: String = "working",
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val weightStr = if (weightIncrement % 1f == 0f) {
        weight.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", weight)
    }

    TrackGodCard(
        accentBorder = isEditing || isPr,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "SET $setNumber",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                // Set type badge (only for non-working sets)
                if (setType != "working") {
                    Text(
                        text = when (setType) {
                            "warmup" -> "WU"
                            "drop" -> "D"
                            "failure" -> "F"
                            else -> ""
                        },
                        color = if (setType == "warmup") TextTertiary else Blood,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                }
                if (isPr) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(com.trackgod.app.R.drawable.ic_pentagram),
                        contentDescription = "PR",
                        tint = Blood,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "PR",
                        color = Blood,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                    )
                }
            }
            val isWarmupSet = setType == "warmup"
            Text(
                text = "$weightStr${weightUnit.uppercase()} x $reps",
                color = when {
                    isPr -> BloodBright
                    isWarmupSet -> TextTertiary
                    else -> TextPrimary
                },
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

// ── Last Session Sets ──────────────────────────────────────────────────────

@Composable
private fun LastSessionSets(
    sets: List<com.trackgod.app.core.database.entity.SetEntity>,
    weightUnit: String,
    weightIncrement: Float,
    onSetTapped: (com.trackgod.app.core.database.entity.SetEntity) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val unit = weightUnit.uppercase()

    Column {
        // Header row — tap to expand/collapse
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "LAST SESSION (${sets.size} SETS)",
                style = MaterialTheme.typography.labelMedium,
                color = BloodBright,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = BloodBright,
                modifier = Modifier.size(18.dp),
            )
        }

        // Expandable set list
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "TAP TO AUTO-FILL",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 2.sp,
                    fontSize = 8.sp,
                )
                sets.forEachIndexed { index, set ->
                    val weightStr = formatWeightForInput(set.weight, weightIncrement)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceLow, RectangleShape)
                            .clickable { onSetTapped(set) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "SET ${index + 1}",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                        )
                        Text(
                            text = "$weightStr$unit  ×  ${set.reps}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

// ── Formatting Helpers ──────────────────────────────────────────────────────

private fun formatWeightForInput(value: Float, increment: Float): String {
    return if (increment % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", value)
    }
}

private fun formatVolumeShort(volume: Float): String {
    return when {
        volume >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", volume / 1_000_000f)
        volume >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", volume / 1_000f)
        else -> String.format(java.util.Locale.US, "%.0f", volume)
    }
}

private fun formatTimeMMSS(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun formatRestTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
