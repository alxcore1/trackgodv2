package com.trackgod.app.feature.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.BloodBright
import com.trackgod.app.ui.theme.BloodDeep
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.SurfaceLow
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextSecondary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.VoidDeep
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
            .padding(top = 4.dp),
    ) {
        // -- Header -----------------------------------------------------------
        TrackGodHeader()

        Spacer(modifier = Modifier.height(12.dp))

        // -- Search bar -------------------------------------------------------
        SearchBar(
            query = state.searchQuery,
            onQueryChanged = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -- Date picker ------------------------------------------------------
        DatePickerRow(
            weekDates = state.weekDates,
            selectedDate = state.selectedDate,
            workoutDates = state.workoutDatesThisWeek,
            onDateSelected = onDateSelected,
            onWeekNavigate = onWeekNavigate,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -- Section divider --------------------------------------------------
        SectionDivider(
            text = "VERIFIED HISTORY",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                contentPadding = PaddingValues(bottom = 24.dp),
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

// -- Search bar ---------------------------------------------------------------

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .background(VoidDeep, RectangleShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Red left accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(Blood),
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() },
            ),
            cursorBrush = SolidColor(Blood),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "FIND PAST TRANSMISSIONS...",
                            color = TextTertiary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChanged("") }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(18.dp),
            )
        }
    }
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
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
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
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dayOfWeek,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = dayNum,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
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
    val workout = item.workout
    var showContextMenu by remember { mutableStateOf(false) }
    val accentColor = if (index % 2 == 0) Blood else BloodDeep

    val rootModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(IntrinsicSize.Min)
        .background(SurfaceLow, RectangleShape)
        .combinedClickable(
            onClick = onToggleExpand,
            onLongClick = { showContextMenu = true },
        )

    Box {
        // All cards get accent bar, alternating Blood/BloodDeep
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                )
            }
        }

        // Context menu (long-press)
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            offset = DpOffset(16.dp, 0.dp),
            modifier = Modifier.background(SurfaceHighest),
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RENAME",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
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
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EDIT SETS",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
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
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DELETE",
                            color = BloodBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
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
) {
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
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormatted,
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Volume display
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = volumeFormatted,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "${weightUnit.uppercase()} VOL",
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // -- Duration & sets row ---
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = "$durationMin MIN",
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
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
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )

        // Volume delta comparison
        if (item.volumeDelta != null && item.volumeDelta != 0f) {
            Spacer(modifier = Modifier.weight(1f))
            val arrow = if (item.volumeDelta > 0) "↑" else "↓"
            val deltaColor = if (item.volumeDelta > 0) BloodBright else TextTertiary
            Text(
                text = "$arrow ${formatVolume(kotlin.math.abs(item.volumeDelta))}",
                color = deltaColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }

    // Volume intensity bar
    val currentVolume = workout.totalVolume ?: 0f
    if (maxVolumeInList > 0f && currentVolume > 0f) {
        Spacer(modifier = Modifier.height(6.dp))
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.categories.joinToString(" · ") { it.uppercase() },
            color = TextTertiary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
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
    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier = Modifier
                .fillMaxWidth()
                .background(VoidDeep, RectangleShape)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSave() }),
            cursorBrush = SolidColor(Blood),
        )

        Spacer(modifier = Modifier.height(8.dp))

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
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLow,
        shape = RectangleShape,
        title = {
            Text(
                text = "DELETE THIS WORKOUT?",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
        },
        text = {
            Text(
                text = "This cannot be undone. All sets and data for this workout will be permanently removed.",
                color = TextSecondary,
                fontSize = 14.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "DELETE",
                    color = BloodBright,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCEL",
                    color = TextTertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        },
    )
}

// -- Formatting helpers -------------------------------------------------------

private fun formatVolume(volume: Float): String {
    return when {
        volume >= 1_000_000 -> "%.1fM".format(volume / 1_000_000f)
        else -> {
            val whole = volume.toLong()
            val str = whole.toString()
            val result = StringBuilder()
            var count = 0
            for (i in str.length - 1 downTo 0) {
                if (count > 0 && count % 3 == 0) result.insert(0, ' ')
                result.insert(0, str[i])
                count++
            }
            result.toString()
        }
    }
}

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
