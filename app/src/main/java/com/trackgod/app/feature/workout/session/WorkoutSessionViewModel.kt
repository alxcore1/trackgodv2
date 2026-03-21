package com.trackgod.app.feature.workout.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.database.entity.SetEntity
import com.trackgod.app.core.database.entity.WorkoutEntity
import com.trackgod.app.core.repository.ExerciseRepository
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.core.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── State ────────────────────────────────────────────────────────────────────

data class ExerciseWithSets(
    val exercise: ExerciseEntity,
    val setCount: Int,
)

data class WorkoutSessionState(
    val workout: WorkoutEntity? = null,
    val currentExercise: ExerciseEntity? = null,
    val completedSets: List<SetEntity> = emptyList(),
    val allExercisesInSession: List<ExerciseWithSets> = emptyList(),
    val weightInput: String = "",
    val repsInput: String = "",
    val noteInput: String = "",
    val rpeInput: Int? = null,
    val rirInput: Int? = null,
    val lastSessionHint: String? = null,
    val restTimeRemaining: Int = 0,
    val isRestTimerRunning: Boolean = false,
    val sessionDurationSeconds: Long = 0,
    val totalSetsCount: Int = 0,
    val totalVolume: Float = 0f,
    val exerciseCount: Int = 0,
    val editingSetId: Long? = null,
    val showCompleteDialog: Boolean = false,
    val showRpe: Boolean = false,
    val showRir: Boolean = false,
    val restTimerEnabled: Boolean = true,
    val restTimerAutoStart: Boolean = true,
    val restTimerDuration: Int = 90,
    val weightUnit: String = "kg",
    val weightIncrement: Float = 2.5f,
    val isLoading: Boolean = true,
)

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutSessionState())
    val state: StateFlow<WorkoutSessionState> = _state.asStateFlow()

    private val restTimerManager = RestTimerManager()

    private var sessionTimerJob: Job? = null
    private var setsCollectionJob: Job? = null
    private var workoutId: Long = -1L

    init {
        val passedId = savedStateHandle.get<Long>("workoutId") ?: -1L

        viewModelScope.launch {
            // Load settings
            loadSettings()

            // Create or resume workout
            workoutId = if (passedId <= 0L) {
                val newId = workoutRepository.createWorkout()
                settingsRepository.setActiveWorkoutId(newId)
                newId
            } else {
                passedId
            }

            // Load workout entity
            val workout = workoutRepository.getWorkout(workoutId)
            _state.update { it.copy(workout = workout, isLoading = false) }

            // Start session timer
            startSessionTimer(workout?.startTime ?: System.currentTimeMillis())

            // Observe all sets for this workout (for stats)
            observeSets()
        }

        // Observe rest timer flows
        viewModelScope.launch {
            restTimerManager.timeRemaining.collectLatest { remaining ->
                _state.update { it.copy(restTimeRemaining = remaining) }
            }
        }
        viewModelScope.launch {
            restTimerManager.isRunning.collectLatest { running ->
                _state.update { it.copy(isRestTimerRunning = running) }
            }
        }
    }

    // ── Settings ─────────────────────────────────────────────────────────────

    private fun loadSettings() {
        _state.update {
            it.copy(
                showRpe = settingsRepository.showRpe(),
                showRir = settingsRepository.showRir(),
                restTimerEnabled = settingsRepository.isRestTimerEnabled(),
                restTimerAutoStart = settingsRepository.isRestTimerAutoStart(),
                restTimerDuration = settingsRepository.getRestTimerDuration(),
                weightUnit = settingsRepository.getWeightUnit(),
                weightIncrement = settingsRepository.getDefaultWeightIncrement(),
            )
        }
    }

    // ── Session Timer ────────────────────────────────────────────────────────

    private fun startSessionTimer(startTime: Long) {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _state.update { it.copy(sessionDurationSeconds = elapsed) }
                delay(1_000L)
            }
        }
    }

    // ── Sets Observation ─────────────────────────────────────────────────────

    private fun observeSets() {
        setsCollectionJob?.cancel()
        setsCollectionJob = viewModelScope.launch {
            workoutRepository.getSetsForWorkout(workoutId).collectLatest { allSets ->
                val currentExerciseId = _state.value.currentExercise?.id
                val completedForExercise = if (currentExerciseId != null) {
                    allSets.filter { it.exerciseId == currentExerciseId }
                } else {
                    emptyList()
                }

                val totalVolume = allSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                val exerciseIds = allSets.map { it.exerciseId }.distinct()

                // Build exercise-with-sets summary
                val exerciseSummaries = exerciseIds.mapNotNull { exId ->
                    val exercise = exerciseRepository.getById(exId) ?: return@mapNotNull null
                    ExerciseWithSets(
                        exercise = exercise,
                        setCount = allSets.count { it.exerciseId == exId },
                    )
                }

                _state.update {
                    it.copy(
                        completedSets = completedForExercise,
                        allExercisesInSession = exerciseSummaries,
                        totalSetsCount = allSets.size,
                        totalVolume = totalVolume,
                        exerciseCount = exerciseIds.size,
                    )
                }
            }
        }
    }

    // ── Exercise Selection ───────────────────────────────────────────────────

    fun selectExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    currentExercise = exercise,
                    editingSetId = null,
                    noteInput = "",
                    lastSessionHint = null,
                )
            }

            // Load smart defaults from previous sessions
            val recentSets = workoutRepository.getRecentSetsForExercise(exercise.id)
            val unit = _state.value.weightUnit
            if (recentSets.isNotEmpty()) {
                val lastSet = recentSets.first()
                val weightStr = formatWeight(lastSet.weight, _state.value.weightIncrement)
                _state.update {
                    it.copy(
                        weightInput = weightStr,
                        repsInput = lastSet.reps.toString(),
                        lastSessionHint = "LAST: $weightStr$unit x ${lastSet.reps}",
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        weightInput = "",
                        repsInput = "",
                    )
                }
            }

            // Trigger set re-filter for new exercise
            observeSets()
        }
    }

    // ── Input Updates ────────────────────────────────────────────────────────

    fun updateWeight(value: String) {
        if (value.isEmpty() || value.matches(Regex("""^\d*\.?\d*$"""))) {
            _state.update { it.copy(weightInput = value) }
        }
    }

    fun updateReps(value: String) {
        if (value.isEmpty() || value.matches(Regex("""^\d*$"""))) {
            _state.update { it.copy(repsInput = value) }
        }
    }

    fun updateNote(value: String) {
        _state.update { it.copy(noteInput = value) }
    }

    fun incrementWeight(delta: Float) {
        val current = _state.value.weightInput.toFloatOrNull() ?: 0f
        val next = (current + delta).coerceAtLeast(0f)
        _state.update { it.copy(weightInput = formatWeight(next, _state.value.weightIncrement)) }
    }

    fun incrementReps(delta: Int) {
        val current = _state.value.repsInput.toIntOrNull() ?: 0
        val next = (current + delta).coerceAtLeast(0)
        _state.update { it.copy(repsInput = next.toString()) }
    }

    // ── Set Logging ──────────────────────────────────────────────────────────

    fun logSet() {
        val s = _state.value
        val exercise = s.currentExercise ?: return
        val weight = s.weightInput.toFloatOrNull() ?: return
        val reps = s.repsInput.toIntOrNull() ?: return
        if (weight <= 0f || reps <= 0) return

        viewModelScope.launch {
            workoutRepository.addSet(
                workoutId = workoutId,
                exerciseId = exercise.id,
                weight = weight,
                reps = reps,
                note = s.noteInput.ifBlank { null },
                rpe = s.rpeInput,
                rir = s.rirInput,
            )

            // Clear note after logging (weight/reps stay for easy repeat)
            _state.update { it.copy(noteInput = "", rpeInput = null, rirInput = null) }

            // Auto-start rest timer
            if (s.restTimerEnabled && s.restTimerAutoStart) {
                restTimerManager.start(
                    durationSeconds = s.restTimerDuration,
                    scope = viewModelScope,
                    onComplete = { /* vibration trigger handled at UI layer */ },
                )
            }
        }
    }

    // ── Set Editing ──────────────────────────────────────────────────────────

    fun editSet(setId: Long) {
        val set = _state.value.completedSets.find { it.id == setId } ?: return
        _state.update {
            it.copy(
                editingSetId = setId,
                weightInput = formatWeight(set.weight, it.weightIncrement),
                repsInput = set.reps.toString(),
                noteInput = set.note ?: "",
                rpeInput = set.rpe,
                rirInput = set.rir,
            )
        }
    }

    fun saveEdit() {
        val s = _state.value
        val setId = s.editingSetId ?: return
        val set = s.completedSets.find { it.id == setId } ?: return
        val weight = s.weightInput.toFloatOrNull() ?: return
        val reps = s.repsInput.toIntOrNull() ?: return
        if (weight <= 0f || reps <= 0) return

        viewModelScope.launch {
            workoutRepository.updateSet(
                set.copy(
                    weight = weight,
                    reps = reps,
                    note = s.noteInput.ifBlank { null },
                    rpe = s.rpeInput,
                    rir = s.rirInput,
                )
            )
            cancelEdit()
        }
    }

    fun cancelEdit() {
        // Restore smart defaults or clear
        val s = _state.value
        val lastHint = s.lastSessionHint
        if (lastHint != null) {
            // Re-extract from hint
            val recentWeight = s.completedSets.lastOrNull()?.weight
            val recentReps = s.completedSets.lastOrNull()?.reps
            _state.update {
                it.copy(
                    editingSetId = null,
                    weightInput = recentWeight?.let { w -> formatWeight(w, it.weightIncrement) } ?: "",
                    repsInput = recentReps?.toString() ?: "",
                    noteInput = "",
                    rpeInput = null,
                    rirInput = null,
                )
            }
        } else {
            _state.update {
                it.copy(
                    editingSetId = null,
                    noteInput = "",
                    rpeInput = null,
                    rirInput = null,
                )
            }
        }
    }

    fun deleteSet(setId: Long) {
        val set = _state.value.completedSets.find { it.id == setId } ?: return
        viewModelScope.launch {
            workoutRepository.deleteSet(set)
            if (_state.value.editingSetId == setId) {
                cancelEdit()
            }
        }
    }

    // ── Rest Timer ───────────────────────────────────────────────────────────

    fun skipRestTimer() {
        restTimerManager.skip()
    }

    // ── Finish / Discard ─────────────────────────────────────────────────────

    fun finishWorkout() {
        _state.update { it.copy(showCompleteDialog = true) }
    }

    fun dismissCompleteDialog() {
        _state.update { it.copy(showCompleteDialog = false) }
    }

    /**
     * Generate a workout name from the categories of exercises used.
     * E.g., "Chest & Back" or "Legs".
     */
    fun generateWorkoutName(): String {
        val categories = _state.value.allExercisesInSession
            .map { it.exercise.category }
            .distinct()
        return when {
            categories.isEmpty() -> "Workout"
            categories.size == 1 -> categories.first()
            categories.size == 2 -> "${categories[0]} & ${categories[1]}"
            else -> categories.take(2).joinToString(" & ") + " +"
        }
    }

    fun confirmFinish(name: String) {
        viewModelScope.launch {
            workoutRepository.completeWorkout(workoutId, name)
            settingsRepository.clearActiveWorkoutId()
            sessionTimerJob?.cancel()
            restTimerManager.stop()
        }
    }

    fun discardWorkout() {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            settingsRepository.clearActiveWorkoutId()
            sessionTimerJob?.cancel()
            restTimerManager.stop()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun formatWeight(value: Float, increment: Float): String {
        return if (increment % 1f == 0f) {
            value.toInt().toString()
        } else {
            "%.1f".format(value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionTimerJob?.cancel()
        restTimerManager.stop()
    }
}
