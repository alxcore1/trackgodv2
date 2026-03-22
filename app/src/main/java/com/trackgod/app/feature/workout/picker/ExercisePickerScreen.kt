package com.trackgod.app.feature.workout.picker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.ui.component.ButtonVariant
import com.trackgod.app.ui.component.EmptyState
import com.trackgod.app.ui.component.MetalTextureBackground
import com.trackgod.app.ui.component.TrackGodButton
import com.trackgod.app.ui.theme.Blood
import com.trackgod.app.ui.theme.SurfaceHighest
import com.trackgod.app.ui.theme.TextPrimary
import com.trackgod.app.ui.theme.TextTertiary
import com.trackgod.app.ui.theme.TrackGodTheme
import com.trackgod.app.ui.theme.Void
import com.trackgod.app.ui.theme.VoidDeep

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ExercisePickerScreen(
    onExerciseSelected: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit,
    viewModel: ExercisePickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ExercisePickerContent(
        state = state,
        onSearchQueryChanged = { viewModel.onEvent(ExercisePickerEvent.SearchQueryChanged(it)) },
        onCategorySelected = { viewModel.onEvent(ExercisePickerEvent.CategorySelected(it)) },
        onEquipmentFilterSelected = { viewModel.onEvent(ExercisePickerEvent.EquipmentFilterSelected(it)) },
        onBrandToggled = { viewModel.onEvent(ExercisePickerEvent.BrandToggled(it)) },
        onClearBrands = { viewModel.onEvent(ExercisePickerEvent.ClearBrands) },
        onExerciseSelected = onExerciseSelected,
        onDismiss = onDismiss,
        onToggleAddDialog = { viewModel.onEvent(ExercisePickerEvent.ToggleAddDialog) },
        onCreateExercise = { name, category, equipment ->
            viewModel.onEvent(ExercisePickerEvent.CreateExercise(name, category, equipment))
        },
    )
}

// ── Content (stateless, previewable) ─────────────────────────────────────────

@Composable
private fun ExercisePickerContent(
    state: ExercisePickerState,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onEquipmentFilterSelected: (String?) -> Unit,
    onBrandToggled: (String) -> Unit,
    onClearBrands: () -> Unit,
    onExerciseSelected: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit,
    onToggleAddDialog: () -> Unit,
    onCreateExercise: (name: String, category: String, equipmentType: String) -> Unit,
) {
    MetalTextureBackground {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
    ) {
        // 1. Header bar
        HeaderBar(onDismiss = onDismiss)

        // 2. Search bar
        SearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChanged,
        )

        // 3. Equipment type filter chips
        EquipmentFilterChips(
            selected = state.selectedEquipmentFilter,
            onSelected = onEquipmentFilterSelected,
        )

        // 3b. Brand filter (shown only when MACHINE is selected)
        if (state.selectedEquipmentFilter == "machine" && state.availableBrands.isNotEmpty()) {
            BrandFilterChips(
                brands = state.availableBrands,
                selectedBrands = state.selectedBrands,
                onBrandToggled = onBrandToggled,
                onClearAll = onClearBrands,
            )
        }

        // 4. Category filter chips
        CategoryChips(
            categories = state.categories,
            selected = state.selectedCategory,
            onSelected = onCategorySelected,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 5. Exercise list (fills remaining space)
        if (state.exercises.isEmpty() && !state.isLoading) {
            val isEmptyDb = state.searchQuery.isBlank() && state.selectedCategory == null && state.selectedEquipmentFilter == null
            if (isEmptyDb) {
                EmptyState(
                    icon = Icons.Default.FitnessCenter,
                    title = "NO EXERCISES LOADED",
                    subtitle = "Add your first exercise to begin",
                    modifier = Modifier.weight(1f),
                )
            } else {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "No Matches Found",
                    subtitle = "Try a different search or category.",
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(
                    items = state.exercises,
                    key = { it.id },
                ) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        onClick = { onExerciseSelected(exercise) },
                    )
                }
            }
        }

        // 6. Bottom action
        val isEmptyDb = state.exercises.isEmpty() && !state.isLoading
                && state.searchQuery.isBlank() && state.selectedCategory == null && state.selectedEquipmentFilter == null
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Void)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            TrackGodButton(
                text = "+ ADD CUSTOM",
                onClick = onToggleAddDialog,
                variant = if (isEmptyDb) ButtonVariant.Primary else ButtonVariant.Secondary,
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    } // MetalTextureBackground

    // Add-exercise dialog
    if (state.showAddDialog) {
        AddExerciseDialog(
            onSave = onCreateExercise,
            onDismiss = onToggleAddDialog,
        )
    }
}

// ── Header Bar ───────────────────────────────────────────────────────────────

@Composable
private fun HeaderBar(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Void)
            .padding(horizontal = 12.dp),
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
            text = "SELECT WEAPON",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.weight(1f),
        )

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextTertiary,
            )
        }
    }
}

// ── Search Bar ───────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VoidDeep)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Red left accent bar
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(Blood),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            ),
            singleLine = true,
            cursorBrush = SolidColor(Blood),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "SEARCH EXERCISES...",
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
    }
}

// ── Category Chips ───────────────────────────────────────────────────────────

@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // "ALL" chip
        item(key = "all") {
            CategoryChip(
                label = "All",
                isActive = selected == null,
                onClick = { onSelected(null) },
            )
        }
        items(
            items = categories,
            key = { it },
        ) { category ->
            CategoryChip(
                label = category,
                isActive = selected == category,
                onClick = { onSelected(category) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label.uppercase(),
        color = if (isActive) TextPrimary else TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier
            .background(
                color = if (isActive) Blood else SurfaceHighest,
                shape = RectangleShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

// ── Equipment Filter Chips ───────────────────────────────────────────────────

@Composable
private fun EquipmentFilterChips(
    selected: String?,
    onSelected: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryChip(
            label = "All",
            isActive = selected == null,
            onClick = { onSelected(null) },
        )
        CategoryChip(
            label = "Machine",
            isActive = selected == "machine",
            onClick = { onSelected("machine") },
        )
        CategoryChip(
            label = "Free Weight",
            isActive = selected == "free_weight",
            onClick = { onSelected("free_weight") },
        )
    }
}

@Composable
private fun BrandFilterChips(
    brands: List<String>,
    selectedBrands: Set<String>,
    onBrandToggled: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            CategoryChip(
                label = if (selectedBrands.isEmpty()) "All Brands" else "Clear",
                isActive = selectedBrands.isEmpty(),
                onClick = onClearAll,
            )
        }
        items(brands.size) { index ->
            val brand = brands[index]
            CategoryChip(
                label = brand,
                isActive = brand in selectedBrands,
                onClick = { onBrandToggled(brand) },
            )
        }
    }
}

// ── Exercise Row ─────────────────────────────────────────────────────────────

@Composable
private fun ExerciseRow(
    exercise: ExerciseEntity,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val rowBackground by animateColorAsState(
        targetValue = if (isPressed) Blood.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 80),
        label = "rowBg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(rowBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(if (isPressed) Blood else Color.Transparent),
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Exercise info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 14.dp),
        ) {
            Text(
                text = exercise.name.uppercase(),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = buildString {
                    append(exercise.category.uppercase())
                    append(" \u00b7 ")
                    append(exercise.equipmentType.uppercase())
                    if (!exercise.brand.isNullOrBlank()) {
                        append(" \u00b7 ")
                        append(exercise.brand.uppercase())
                    }
                },
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }

        // Usage count badge
        if (exercise.usageCount > 0) {
            Text(
                text = "${exercise.usageCount}",
                color = TextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .background(SurfaceHighest, RectangleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val PREVIEW_EXERCISES = listOf(
    ExerciseEntity(
        id = 1, name = "Bench Press", category = "Chest",
        equipmentType = "Barbell", usageCount = 12, createdAt = 0,
    ),
    ExerciseEntity(
        id = 2, name = "Squat", category = "Legs",
        equipmentType = "Barbell", usageCount = 8, createdAt = 0,
    ),
    ExerciseEntity(
        id = 3, name = "Deadlift", category = "Back",
        equipmentType = "Barbell", usageCount = 5, createdAt = 0,
    ),
    ExerciseEntity(
        id = 4, name = "Overhead Press", category = "Shoulders",
        equipmentType = "Barbell", usageCount = 0, createdAt = 0,
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF131313, widthDp = 400, heightDp = 800)
@Composable
private fun ExercisePickerScreenPreview() {
    TrackGodTheme {
        ExercisePickerContent(
            state = ExercisePickerState(
                exercises = PREVIEW_EXERCISES,
                isLoading = false,
            ),
            onSearchQueryChanged = {},
            onCategorySelected = {},
            onEquipmentFilterSelected = {},
            onExerciseSelected = {},
            onDismiss = {},
            onToggleAddDialog = {},
            onBrandToggled = {},
            onClearBrands = {},
            onCreateExercise = { _, _, _ -> },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313, widthDp = 400, heightDp = 800)
@Composable
private fun ExercisePickerEmptyPreview() {
    TrackGodTheme {
        ExercisePickerContent(
            state = ExercisePickerState(
                exercises = emptyList(),
                searchQuery = "zzzz",
                isLoading = false,
            ),
            onSearchQueryChanged = {},
            onCategorySelected = {},
            onEquipmentFilterSelected = {},
            onBrandToggled = {},
            onClearBrands = {},
            onExerciseSelected = {},
            onDismiss = {},
            onToggleAddDialog = {},
            onCreateExercise = { _, _, _ -> },
        )
    }
}
