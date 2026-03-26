package com.trackgod.app.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.SetEntity
import com.trackgod.app.core.database.entity.WorkoutEntity
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.core.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// -- Data models --------------------------------------------------------------

data class ExerciseWithSetsInWorkout(
    val exerciseName: String,
    val category: String,
    val sets: List<SetEntity>,
)

data class WorkoutWithDetails(
    val workout: WorkoutEntity,
    val exercises: List<ExerciseWithSetsInWorkout> = emptyList(),
    val totalSets: Int = 0,
    val volumeDelta: Float? = null,
    val categories: List<String> = emptyList(),
)

data class HistoryState(
    val workouts: List<WorkoutWithDetails> = emptyList(),
    val selectedDate: LocalDate? = null,
    val searchQuery: String = "",
    val expandedWorkoutId: Long? = null,
    val editingWorkoutId: Long? = null,
    val editingName: String = "",
    val weekDates: List<LocalDate> = emptyList(),
    val weekOffset: Int = 0,
    val isLoading: Boolean = true,
    val weightUnit: String = "kg",
    val showDeleteConfirm: Long? = null,
    val workoutDatesThisWeek: Set<LocalDate> = emptySet(),
    val maxVolumeInList: Float = 0f,
)

// -- ViewModel ----------------------------------------------------------------

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    /** Active Flow collection job -- cancelled and relaunched on filter changes. */
    private var observeJob: Job? = null

    init {
        loadWeekDates(0)
        loadWeightUnit()
        startObserving()
    }

    // -- Data loading ---------------------------------------------------------

    /**
     * Starts (or restarts) the reactive Flow collection from the database.
     * Cancels any previous collector to avoid duplicate processing.
     */
    private fun startObserving() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            workoutRepository.getAllCompletedWorkouts().collectLatest { allWorkouts ->
                try {
                    val filtered = applyFilters(allWorkouts)

                    // Build a map of workout name → previous volume for delta comparison
                    val volumeByName = mutableMapOf<String, Float>()

                    val withDetails = filtered.map { workout ->
                        val sets = workoutRepository.getSetsForWorkoutOnce(workout.id)
                        val categories = sets.map { it.exerciseId }
                            .distinct()
                            .mapNotNull { workoutRepository.getExerciseById(it)?.category }
                            .distinct()
                        val currentVolume = workout.totalVolume ?: 0f
                        val name = workout.name.lowercase()
                        val previousVolume = volumeByName[name]
                        val delta = if (previousVolume != null) currentVolume - previousVolume else null
                        volumeByName[name] = currentVolume
                        WorkoutWithDetails(
                            workout = workout,
                            totalSets = sets.size,
                            volumeDelta = delta,
                            categories = categories,
                        )
                    }

                    // Workout dates for week indicator
                    val weekDates = _state.value.weekDates.toSet()
                    val datesWithWorkouts = allWorkouts
                        .mapNotNull { w ->
                            runCatching { LocalDate.parse(w.date, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
                        }
                        .filter { it in weekDates }
                        .toSet()

                    val maxVol = withDetails.maxOfOrNull { it.workout.totalVolume ?: 0f } ?: 0f

                    _state.update {
                        it.copy(
                            workouts = enrichExpandedWorkout(withDetails, it.expandedWorkoutId),
                            isLoading = false,
                            workoutDatesThisWeek = datesWithWorkouts,
                            maxVolumeInList = maxVol,
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HistoryViewModel", "Error processing workouts", e)
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun applyFilters(workouts: List<WorkoutEntity>): List<WorkoutEntity> {
        val currentState = _state.value
        var result = workouts

        // Filter by selected date
        if (currentState.selectedDate != null) {
            val dateStr = currentState.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            result = result.filter { it.date == dateStr }
        }

        // Filter by search query
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            result = result.filter { it.name.lowercase().contains(query) }
        }

        return result
    }

    private suspend fun enrichExpandedWorkout(
        workouts: List<WorkoutWithDetails>,
        expandedId: Long?,
    ): List<WorkoutWithDetails> {
        if (expandedId == null) return workouts
        return workouts.map { item ->
            if (item.workout.id == expandedId && item.exercises.isEmpty()) {
                val exercises = loadExerciseDetails(expandedId)
                item.copy(exercises = exercises)
            } else {
                item
            }
        }
    }

    private suspend fun loadExerciseDetails(workoutId: Long): List<ExerciseWithSetsInWorkout> {
        val sets = workoutRepository.getSetsForWorkoutOnce(workoutId)
        val exerciseIds = sets.map { it.exerciseId }.distinct()

        return exerciseIds.mapNotNull { exerciseId ->
            val exercise = workoutRepository.getExerciseById(exerciseId) ?: return@mapNotNull null
            val exerciseSets = sets.filter { it.exerciseId == exerciseId }
                .sortedBy { it.setNumber }
            ExerciseWithSetsInWorkout(
                exerciseName = exercise.name,
                category = exercise.category,
                sets = exerciseSets,
            )
        }
    }

    private fun loadWeekDates(offset: Int) {
        val today = LocalDate.now()
        val weekStart = today.plusWeeks(offset.toLong())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val dates = (0L..6L).map { weekStart.plusDays(it) }
        _state.update { it.copy(weekDates = dates, weekOffset = offset) }
    }

    private fun loadWeightUnit() {
        val unit = settingsRepository.getWeightUnit()
        _state.update { it.copy(weightUnit = unit) }
    }

    // -- User actions ---------------------------------------------------------

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        startObserving()
    }

    fun onDateSelected(date: LocalDate) {
        val current = _state.value.selectedDate
        if (current == date) {
            // Deselect
            _state.update { it.copy(selectedDate = null) }
        } else {
            _state.update { it.copy(selectedDate = date) }
        }
        startObserving()
    }

    fun onWeekNavigate(direction: Int) {
        val newOffset = _state.value.weekOffset + direction
        if (newOffset > 0) return // Don't go into the future
        loadWeekDates(newOffset)
    }

    fun onToggleExpand(workoutId: Long) {
        val currentExpanded = _state.value.expandedWorkoutId
        if (currentExpanded == workoutId) {
            // Collapse
            _state.update { it.copy(expandedWorkoutId = null) }
        } else {
            // Expand new one
            _state.update { it.copy(expandedWorkoutId = workoutId) }
            viewModelScope.launch {
                val exercises = loadExerciseDetails(workoutId)
                _state.update { state ->
                    state.copy(
                        workouts = state.workouts.map { item ->
                            if (item.workout.id == workoutId) {
                                item.copy(exercises = exercises)
                            } else {
                                item
                            }
                        }
                    )
                }
            }
        }
    }

    fun onStartEditing(workoutId: Long, currentName: String) {
        _state.update {
            it.copy(
                editingWorkoutId = workoutId,
                editingName = currentName,
            )
        }
    }

    fun onEditingNameChanged(name: String) {
        _state.update { it.copy(editingName = name) }
    }

    fun onSaveEditingName() {
        val editingId = _state.value.editingWorkoutId ?: return
        val newName = _state.value.editingName.trim()
        if (newName.isBlank()) return

        viewModelScope.launch {
            workoutRepository.updateWorkoutName(editingId, newName)
            _state.update {
                it.copy(
                    editingWorkoutId = null,
                    editingName = "",
                )
            }
            // Flow auto-updates the list from the DB change
        }
    }

    fun onCancelEditing() {
        _state.update {
            it.copy(
                editingWorkoutId = null,
                editingName = "",
            )
        }
    }

    fun onRequestDelete(workoutId: Long) {
        _state.update { it.copy(showDeleteConfirm = workoutId) }
    }

    fun onCancelDelete() {
        _state.update { it.copy(showDeleteConfirm = null) }
    }

    fun onConfirmDelete() {
        val workoutId = _state.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            _state.update {
                it.copy(
                    showDeleteConfirm = null,
                    expandedWorkoutId = if (it.expandedWorkoutId == workoutId) null else it.expandedWorkoutId,
                )
            }
            // Flow auto-updates the list from the DB change
        }
    }
}
