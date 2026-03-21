package com.trackgod.app.feature.workout.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── State ────────────────────────────────────────────────────────────────────

data class ExercisePickerState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null, // null = All
    val selectedEquipmentFilter: String? = null, // null = ALL, "machine" = machines, "free_weight" = free weights
    val categories: List<String> = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core"),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
)

// ── Events ───────────────────────────────────────────────────────────────────

sealed interface ExercisePickerEvent {
    data class SearchQueryChanged(val query: String) : ExercisePickerEvent
    data class CategorySelected(val category: String?) : ExercisePickerEvent
    data class EquipmentFilterSelected(val filter: String?) : ExercisePickerEvent
    data object ToggleAddDialog : ExercisePickerEvent
    data class CreateExercise(
        val name: String,
        val category: String,
        val equipmentType: String,
    ) : ExercisePickerEvent
}

// ── ViewModel ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExercisePickerViewModel @javax.inject.Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val selectedEquipmentFilter = MutableStateFlow<String?>(null)
    private val showAddDialog = MutableStateFlow(false)

    companion object {
        private val FREE_WEIGHT_TYPES = setOf("barbell", "dumbbell", "bodyweight")
    }

    /**
     * Reactive exercise list.
     *
     * Strategy:
     *  - query blank + no category  -> getAllActive()
     *  - query blank + category set -> getByCategory()
     *  - query set   + no category  -> search()
     *  - query set   + category set -> search() then filter client-side
     *  - equipment filter applied client-side on top
     */
    private val exercises = combine(searchQuery, selectedCategory, selectedEquipmentFilter) { query, category, equipFilter ->
        Triple(query, category, equipFilter)
    }.flatMapLatest { (query, category, equipFilter) ->
        val baseFlow = when {
            query.isBlank() && category == null -> exerciseRepository.getAllActive()
            query.isBlank() -> exerciseRepository.getByCategory(category!!)
            category == null -> exerciseRepository.search(query)
            else -> exerciseRepository.search(query).map { list ->
                list.filter { it.category.equals(category, ignoreCase = true) }
            }
        }
        if (equipFilter == null) {
            baseFlow
        } else {
            baseFlow.map { list ->
                list.filter { exercise ->
                    when (equipFilter) {
                        "machine" -> exercise.equipmentType.equals("machine", ignoreCase = true)
                        "free_weight" -> exercise.equipmentType.lowercase() in FREE_WEIGHT_TYPES
                        else -> true
                    }
                }
            }
        }
    }

    val state: StateFlow<ExercisePickerState> = combine(
        exercises,
        searchQuery,
        selectedCategory,
        selectedEquipmentFilter,
        showAddDialog,
    ) { exerciseList, query, category, equipFilter, addDialogVisible ->
        ExercisePickerState(
            exercises = exerciseList,
            searchQuery = query,
            selectedCategory = category,
            selectedEquipmentFilter = equipFilter,
            isLoading = false,
            showAddDialog = addDialogVisible,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExercisePickerState(),
    )

    // ── Event handling ───────────────────────────────────────────────────────

    fun onEvent(event: ExercisePickerEvent) {
        when (event) {
            is ExercisePickerEvent.SearchQueryChanged -> {
                searchQuery.update { event.query }
            }

            is ExercisePickerEvent.CategorySelected -> {
                selectedCategory.update { event.category }
            }

            is ExercisePickerEvent.EquipmentFilterSelected -> {
                selectedEquipmentFilter.update { event.filter }
            }

            is ExercisePickerEvent.ToggleAddDialog -> {
                showAddDialog.update { !it }
            }

            is ExercisePickerEvent.CreateExercise -> {
                viewModelScope.launch {
                    exerciseRepository.create(
                        name = event.name,
                        category = event.category,
                        equipmentType = event.equipmentType,
                    )
                    showAddDialog.update { false }
                }
            }
        }
    }
}
