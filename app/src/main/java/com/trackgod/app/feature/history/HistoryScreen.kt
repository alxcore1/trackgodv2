package com.trackgod.app.feature.history

import com.trackgod.app.util.formatVolume
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import com.trackgod.app.feature.workout.session.ConfirmationDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackgod.app.core.database.entity.SetEntity
import com.trackgod.app.core.database.entity.WorkoutEntity
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.EmptyState
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.SectionDivider
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.component.TrackGodHeader
import com.trackgod.app.ui.component.TrackGodSearchField
import com.trackgod.app.ui.component.TrackGodTextField
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.screenPadding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@Composable
fun HistoryScreen(
    onEditWorkout: (Long) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HistoryContent(
        state = state,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDateSelected = viewModel::onDateSelected,
        onWeekNavigate = viewModel::onWeekNavigate,
        onToggleExpand = viewModel::onToggleExpand,
        onStartEditing = viewModel::onStartEditing,
        onEditingNameChanged = viewModel::onEditingNameChanged,
        onSaveEditingName = viewModel::onSaveEditingName,
        onCancelEditing = viewModel::onCancelEditing,
        onRequestDelete = viewModel::onRequestDelete,
        onCancelDelete = viewModel::onCancelDelete,
        onConfirmDelete = viewModel::onConfirmDelete,
        onEditWorkout = onEditWorkout,
    )
}

// -- Content (stateless, previewable) -----------------------------------------

@Composable
private fun HistoryContent(
    state: HistoryState,
    onSearchQueryChanged: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onWeekNavigate: (Int) -> Unit,
    onToggleExpand: (Long) -> Unit,
    onStartEditing: (Long, String) -> Unit,
    onEditingNameChanged: (String) -> Unit,
    onSaveEditingName: () -> Unit,
    onCancelEditing: () -> Unit,
    onRequestDelete: (Long) -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onEditWorkout: (Long) -> Unit = {},
) {
    val spacing = TrackGodTheme.spacing

    MetalTextureBackground {
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Blood)
        }
        return@MetalTextureBackground
    }

    // Delete confirmation dialog
    if (state.showDeleteConfirm != null) {
        DeleteConfirmDialog(
            onConfirm = onConfirmDelete,
            onDismiss = onCancelDelete,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = spacing.xs),
    ) {
        // -- Header -----------------------------------------------------------
        TrackGodHeader()

        Spacer(modifier = Modifier.height(spacing.md))

        // -- Search bar -------------------------------------------------------
        TrackGodSearchField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = "FIND PAST TRANSMISSIONS...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
        )

        Spacer(modifier = Modifier.height(18.dp))

        // -- Date picker ------------------------------------------------------
        DatePickerRow(
            weekDates = state.weekDates,
            selectedDate = state.selectedDate,
            workoutDates = state.workoutDatesThisWeek,
            onDateSelected = onDateSelected,
            onWeekNavigate = onWeekNavigate,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(18.dp))

        // -- Section divider --------------------------------------------------
        SectionDivider(
            text = "VERIFIED HISTORY",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
        )

        Spacer(modifier = Modifier.height(spacing.md))

        // -- Workout list or empty state --------------------------------------
        if (state.workouts.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "NO TRANSMISSIONS LOGGED",
                subtitle = if (state.searchQuery.isNotBlank() || state.selectedDate != null) {
                    "No workouts match your filters."
                } else {
                    "The altar awaits your first offering."
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(
                    items = state.workouts,
                    key = { _, item -> item.workout.id },
                ) { index, item ->
                    WorkoutCard(
                        item = item,
                        index = index,
                        isExpanded = state.expandedWorkoutId == item.workout.id,
                        isEditing = state.editingWorkoutId == item.workout.id,
                        editingName = state.editingName,
                        weightUnit = state.weightUnit,
                        onToggleExpand = { onToggleExpand(item.workout.id) },
                        onStartEditing = { onStartEditing(item.workout.id, item.workout.name) },
                        onEditingNameChanged = onEditingNameChanged,
                        onSaveEditingName = onSaveEditingName,
                        onCancelEditing = onCancelEditing,
                        onRequestDelete = { onRequestDelete(item.workout.id) },
                        onEditWorkout = { onEditWorkout(item.workout.id) },
                        maxVolumeInList = state.maxVolumeInList,
                    )
                }
            }
        }
    }
    } // MetalTextureBackground
}

// -- Horizontal date picker ---------------------------------------------------

@Composable
private fun DatePickerRow(
    weekDates: List<LocalDate>,
    selectedDate: LocalDate?,
    workoutDates: Set<LocalDate> = emptySet(),
    onDateSelected: (LocalDate) -> Unit,
    onWeekNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TrackGodTheme.spacing
    Row(
        modifier = modifier.padding(horizontal = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left arrow
        IconButton(onClick = { onWeekNavigate(-1) }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous week",
                tint = TextTertiary,
                modifier = Modifier.size(18.dp),
            )
        }

        // Fixed Row (not LazyRow) — guarantees all 7 days fit on any screen
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            weekDates.forEach { date ->
                DateChip(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == LocalDate.now(),
                    hasWorkout = date in workoutDates,
                    onClick = { onDateSelected(date) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Right arrow
        IconButton(onClick = { onWeekNavigate(1) }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next week",
                tint = TextTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasWorkout: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TrackGodTheme.spacing
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> Blood
            else -> SurfaceLow
        },
        animationSpec = tween(150),
        label = "dateChipBg",
    )

    val textColor = when {
        isSelected -> TextPrimary
        isToday -> BloodBright
        else -> TextSecondary
    }

    val dayOfWeek = date.dayOfWeek
        .getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH)
        .uppercase()
        .take(3)
    val dayNum = date.dayOfMonth.toString()

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .background(bgColor, RectangleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = spacing.xs, vertical = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 9.sp,
                letterSpacing = 1.sp,
            ),
            color = textColor,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = dayNum,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
            color = textColor,
        )
        // Workout indicator dot
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    if (hasWorkout && !isSelected) Blood else Color.Transparent,
                    CircleShape,
                ),
        )
    }
}

// -- Workout card -------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutCard(
    item: WorkoutWithDetails,
    index: Int,
    isExpanded: Boolean,
    isEditing: Boolean,
    editingName: String,
    weightUnit: String,
    onToggleExpand: () -> Unit,
    onStartEditing: () -> Unit,
    onEditingNameChanged: (String) -> Unit,
    onSaveEditingName: () -> Unit,
    onCancelEditing: () -> Unit,
    onRequestDelete: () -> Unit,
    onEditWorkout: () -> Unit = {},
    maxVolumeInList: Float = 0f,
) {
    val spacing = TrackGodTheme.spacing
    val workout = item.workout
    var showContextMenu by remember { mutableStateOf(false) }
    val accentColor = Blood

    val rootModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .height(IntrinsicSize.Min)
        .background(SurfaceLow, RectangleShape)
        .combinedClickable(
            onClick = onToggleExpand,
            onLongClick = { showContextMenu = true },
        )

    Box {
        // All cards get accent bar
        Row(modifier = rootModifier) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp, vertical = spacing.md),
            ) {
                WorkoutCardContent(
                    item = item,
                    isExpanded = isExpanded,
                    isEditing = isEditing,
                    editingName = editingName,
                    weightUnit = weightUnit,
                    maxVolumeInList = maxVolumeInList,
                    onEditingNameChanged = onEditingNameChanged,
                    onSaveEditingName = onSaveEditingName,
                    onCancelEditing = onCancelEditing,
                    onEditWorkout = onEditWorkout,
                )
            }
        }

        // Context menu (long-press)
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            offset = DpOffset(18.dp, 0.dp),
            modifier = Modifier.background(SurfaceHighest),
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(spacing.sm))
                        Text(
                            text = "RENAME",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )
                    }
                },
                onClick = {
                    showContextMenu = false
                    onStartEditing()
                },
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(spacing.sm))
                        Text(
                            text = "EDIT SETS",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )
                    }
                },
                onClick = {
                    showContextMenu = false
                    onEditWorkout()
                },
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = BloodBright,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(spacing.sm))
                        Text(
                            text = "DELETE",
                            style = MaterialTheme.typography.labelLarge,
                            color = BloodBright,
                        )
                    }
                },
                onClick = {
                    showContextMenu = false
                    onRequestDelete()
                },
            )
        }
    }
}

@Composable
private fun WorkoutCardContent(
    item: WorkoutWithDetails,
    isExpanded: Boolean,
    isEditing: Boolean,
    editingName: String,
    weightUnit: String,
    maxVolumeInList: Float = 0f,
    onEditingNameChanged: (String) -> Unit,
    onSaveEditingName: () -> Unit,
    onCancelEditing: () -> Unit,
    onEditWorkout: () -> Unit = {},
) {
    val spacing = TrackGodTheme.spacing
    val workout = item.workout
    val displayName = workout.name.ifBlank { "UNTITLED WORKOUT" }.uppercase()
    val volumeFormatted = formatVolume(workout.totalVolume ?: 0f)
    val durationMin = (workout.durationSeconds ?: 0) / 60
    val dateFormatted = formatDate(workout.date)

    // -- Name row (or edit row) ---
    if (isEditing) {
        EditNameRow(
            name = editingName,
            onNameChanged = onEditingNameChanged,
            onSave = onSaveEditingName,
            onCancel = onCancelEditing,
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    ),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                )
            }

            Spacer(modifier = Modifier.width(spacing.md))

            // Volume display
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = volumeFormatted,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Text(
                    text = "${weightUnit.uppercase()} VOL",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                    color = TextTertiary,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(spacing.sm))

    // -- Duration & sets row ---
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = "$durationMin MIN",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )

        Box(
            modifier = Modifier
                .size(3.dp)
                .background(Blood),
        )

        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = "${item.totalSets} SETS",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )

        // Volume delta comparison
        if (item.volumeDelta != null && item.volumeDelta != 0f) {
            Spacer(modifier = Modifier.weight(1f))
            val arrow = if (item.volumeDelta > 0) "↑" else "↓"
            val deltaColor = if (item.volumeDelta > 0) BloodBright else TextTertiary
            Text(
                text = "$arrow ${formatVolume(kotlin.math.abs(item.volumeDelta))}",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = deltaColor,
            )
        }
    }

    // Volume intensity bar
    val currentVolume = workout.totalVolume ?: 0f
    if (maxVolumeInList > 0f && currentVolume > 0f) {
        Spacer(modifier = Modifier.height(spacing.sm))
        val fraction = (currentVolume / maxVolumeInList).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(2.dp)
                .background(Blood.copy(alpha = 0.5f)),
        )
    }

    // Category tags
    if (item.categories.isNotEmpty()) {
        Spacer(modifier = Modifier.height(spacing.sm))
        Text(
            text = item.categories.joinToString(" · ") { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = TextTertiary,
        )
    }

    // -- Edit button (visible when expanded) ---
    if (isExpanded) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = onEditWorkout,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit workout",
                    tint = BloodBright,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    // -- Expanded exercise detail ---
    WorkoutDetailInline(
        exercises = item.exercises,
        isVisible = isExpanded && item.exercises.isNotEmpty(),
        weightUnit = weightUnit,
    )
}

// -- Edit name row ------------------------------------------------------------

@Composable
private fun EditNameRow(
    name: String,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val spacing = TrackGodTheme.spacing
    Column(modifier = Modifier.fillMaxWidth()) {
        TrackGodTextField(
            value = name,
            onValueChange = onNameChanged,
            label = "WORKOUT NAME",
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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

// -- Delete confirmation dialog -----------------------------------------------

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = "DELETE THIS WORKOUT?",
        message = "This cannot be undone. All sets and data for this workout will be permanently removed.",
        confirmText = "DELETE",
        dismissText = "CANCEL",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

// -- Formatting helpers -------------------------------------------------------

private fun formatDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        val month = date.month.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH).uppercase()
        val day = date.dayOfMonth
        val currentYear = LocalDate.now().year
        if (date.year != currentYear) {
            "$month $day, ${date.year}"
        } else {
            "$month $day"
        }
    } catch (_: Exception) {
        dateStr.uppercase()
    }
}

// -- Previews -----------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun HistoryScreenPreview() {
    TrackGodTheme {
        HistoryContent(
            state = HistoryState(
                workouts = listOf(
                    WorkoutWithDetails(
                        workout = WorkoutEntity(
                            id = 1,
                            name = "Chest & Triceps",
                            date = "2026-03-21",
                            startTime = 0,
                            totalVolume = 12_450f,
                            durationSeconds = 4440,
                            isCompleted = true,
                            createdAt = 0,
                        ),
                        totalSets = 24,
                        exercises = listOf(
                            ExerciseWithSetsInWorkout(
                                exerciseName = "Barbell Bench Press",
                                category = "chest",
                                sets = listOf(
                                    SetEntity(id = 1, workoutId = 1, exerciseId = 1, setNumber = 1, weight = 80f, reps = 10, createdAt = 0),
                                    SetEntity(id = 2, workoutId = 1, exerciseId = 1, setNumber = 2, weight = 80f, reps = 10, createdAt = 0),
                                    SetEntity(id = 3, workoutId = 1, exerciseId = 1, setNumber = 3, weight = 82.5f, reps = 8, createdAt = 0),
                                ),
                            ),
                            ExerciseWithSetsInWorkout(
                                exerciseName = "Cable Fly",
                                category = "chest",
                                sets = listOf(
                                    SetEntity(id = 4, workoutId = 1, exerciseId = 2, setNumber = 1, weight = 15f, reps = 12, createdAt = 0),
                                    SetEntity(id = 5, workoutId = 1, exerciseId = 2, setNumber = 2, weight = 15f, reps = 12, createdAt = 0),
                                ),
                            ),
                        ),
                    ),
                    WorkoutWithDetails(
                        workout = WorkoutEntity(
                            id = 2,
                            name = "Back & Biceps",
                            date = "2026-03-19",
                            startTime = 0,
                            totalVolume = 15_800f,
                            durationSeconds = 4920,
                            isCompleted = true,
                            createdAt = 0,
                        ),
                        totalSets = 28,
                    ),
                ),
                expandedWorkoutId = 1,
                weekDates = (0L..6L).map { LocalDate.of(2026, 3, 16).plusDays(it) },
                selectedDate = LocalDate.of(2026, 3, 21),
                isLoading = false,
                weightUnit = "kg",
            ),
            onSearchQueryChanged = {},
            onDateSelected = {},
            onWeekNavigate = {},
            onToggleExpand = {},
            onStartEditing = { _, _ -> },
            onEditingNameChanged = {},
            onSaveEditingName = {},
            onCancelEditing = {},
            onRequestDelete = {},
            onCancelDelete = {},
            onConfirmDelete = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun HistoryScreenEmptyPreview() {
    TrackGodTheme {
        HistoryContent(
            state = HistoryState(
                workouts = emptyList(),
                weekDates = (0L..6L).map { LocalDate.of(2026, 3, 16).plusDays(it) },
                isLoading = false,
            ),
            onSearchQueryChanged = {},
            onDateSelected = {},
            onWeekNavigate = {},
            onToggleExpand = {},
            onStartEditing = { _, _ -> },
            onEditingNameChanged = {},
            onSaveEditingName = {},
            onCancelEditing = {},
            onRequestDelete = {},
            onCancelDelete = {},
            onConfirmDelete = {},
        )
    }
}
