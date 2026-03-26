package com.trackgod.app.feature.workout.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.repository.ExerciseRepository
import com.trackgod.app.core.repository.SettingsRepository
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

data class ExercisePickerState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedEquipmentFilter: String? = null,
    val selectedBrands: Set<String> = emptySet(),
    val availableBrands: List<String> = emptyList(),
    val categories: List<String> = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core"),
    val recentlyUsed: List<ExerciseEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
)

sealed interface ExercisePickerEvent {
    data class SearchQueryChanged(val query: String) : ExercisePickerEvent
    data class CategorySelected(val category: String?) : ExercisePickerEvent
    data class EquipmentFilterSelected(val filter: String?) : ExercisePickerEvent
    data class BrandToggled(val brand: String) : ExercisePickerEvent
    data object ClearBrands : ExercisePickerEvent
    data object ToggleAddDialog : ExercisePickerEvent
    data class CreateExercise(
        val name: String,
        val category: String,
        val equipmentType: String,
        val brand: String? = null,
    ) : ExercisePickerEvent
    data class RenameExercise(val id: Long, val newName: String) : ExercisePickerEvent
    data class HideExercise(val id: Long) : ExercisePickerEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExercisePickerViewModel @javax.inject.Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val selectedEquipmentFilter = MutableStateFlow<String?>(null)
    private val selectedBrands = MutableStateFlow<Set<String>>(emptySet())
    private val availableBrands = MutableStateFlow<List<String>>(emptyList())
    private val showAddDialog = MutableStateFlow(false)
    private val recentlyUsed = MutableStateFlow<List<ExerciseEntity>>(emptyList())

    companion object {
        private val FREE_WEIGHT_TYPES = setOf("barbell", "dumbbell", "bodyweight")
    }

    init {
        // Load persisted filters
        selectedBrands.value = settingsRepository.getSelectedBrands()
        selectedCategory.value = settingsRepository.getSelectedCategory()
        selectedEquipmentFilter.value = settingsRepository.getSelectedEquipmentFilter()

        // Load available brands + recently used from DB
        viewModelScope.launch {
            availableBrands.value = exerciseRepository.getDistinctBrands()
            recentlyUsed.value = exerciseRepository.getRecentlyUsed(6)
        }
    }

    private val exercises = combine(
        searchQuery, selectedCategory, selectedEquipmentFilter, selectedBrands
    ) { query, category, equipFilter, brands ->
        data class Filters(val q: String, val cat: String?, val equip: String?, val brands: Set<String>)
        Filters(query, category, equipFilter, brands)
    }.flatMapLatest { filters ->
        val baseFlow = when {
            filters.q.isBlank() && filters.cat == null -> exerciseRepository.getAllActive()
            filters.q.isBlank() -> exerciseRepository.getByCategory(filters.cat!!)
            filters.cat == null -> exerciseRepository.search(filters.q)
            else -> exerciseRepository.search(filters.q).map { list ->
                list.filter { it.category.equals(filters.cat, ignoreCase = true) }
            }
        }
        baseFlow.map { list ->
            var result = list

            // Equipment filter
            if (filters.equip != null) {
                result = result.filter { exercise ->
                    when (filters.equip) {
                        "machine" -> exercise.equipmentType.equals("machine", ignoreCase = true)
                        "free_weight" -> exercise.equipmentType.lowercase() in FREE_WEIGHT_TYPES
                        else -> true
                    }
                }
            }

            // Brand filter (only when machine filter is active and brands are selected)
            if (filters.brands.isNotEmpty() && filters.equip == "machine") {
                result = result.filter { exercise ->
                    exercise.brand?.let { it in filters.brands } ?: false
                }
            }

            result
        }
    }

    val state: StateFlow<ExercisePickerState> = combine(
        exercises,
        searchQuery,
        selectedCategory,
        selectedEquipmentFilter,
        combine(selectedBrands, availableBrands, showAddDialog, recentlyUsed) { a, b, c, d ->
            arrayOf(a, b, c, d)
        },
    ) { exerciseList, query, category, equipFilter, rest ->
        @Suppress("UNCHECKED_CAST")
        ExercisePickerState(
            exercises = exerciseList,
            searchQuery = query,
            selectedCategory = category,
            selectedEquipmentFilter = equipFilter,
            selectedBrands = rest[0] as Set<String>,
            availableBrands = rest[1] as List<String>,
            isLoading = false,
            showAddDialog = rest[2] as Boolean,
            recentlyUsed = rest[3] as List<ExerciseEntity>,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExercisePickerState(),
    )

    fun onEvent(event: ExercisePickerEvent) {
        when (event) {
            is ExercisePickerEvent.SearchQueryChanged -> searchQuery.update { event.query }
            is ExercisePickerEvent.CategorySelected -> {
                selectedCategory.update { event.category }
                settingsRepository.setSelectedCategory(event.category)
            }
            is ExercisePickerEvent.EquipmentFilterSelected -> {
                selectedEquipmentFilter.update { event.filter }
                settingsRepository.setSelectedEquipmentFilter(event.filter)
            }

            is ExercisePickerEvent.BrandToggled -> {
                selectedBrands.update { current ->
                    val updated = if (event.brand in current) current - event.brand else current + event.brand
                    settingsRepository.setSelectedBrands(updated)
                    updated
                }
            }

            is ExercisePickerEvent.ClearBrands -> {
                selectedBrands.update {
                    settingsRepository.setSelectedBrands(emptySet())
                    emptySet()
                }
            }

            is ExercisePickerEvent.ToggleAddDialog -> showAddDialog.update { !it }

            is ExercisePickerEvent.CreateExercise -> {
                viewModelScope.launch {
                    exerciseRepository.create(
                        name = event.name,
                        category = event.category,
                        equipmentType = event.equipmentType,
                        brand = event.brand,
                    )
                    showAddDialog.update { false }
                }
            }

            is ExercisePickerEvent.RenameExercise -> {
                viewModelScope.launch {
                    exerciseRepository.rename(event.id, event.newName)
                }
            }

            is ExercisePickerEvent.HideExercise -> {
                viewModelScope.launch {
                    exerciseRepository.hide(event.id)
                }
            }
        }
    }
}
