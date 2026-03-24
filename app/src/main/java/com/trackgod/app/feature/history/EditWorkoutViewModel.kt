package com.trackgod.app.feature.history

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditableSet(
    val id: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val note: String?,
    val isDeleted: Boolean = false,
    val isModified: Boolean = false,
    val isNew: Boolean = false,
)

data class EditableExercise(
    val exercise: ExerciseEntity,
    val sets: List<EditableSet>,
)

data class EditWorkoutState(
    val workout: WorkoutEntity? = null,
    val exercises: List<EditableExercise> = emptyList(),
    val workoutName: String = "",
    val editingSetId: Long? = null,
    val editWeight: String = "",
    val editReps: String = "",
    val weightUnit: String = "kg",
    val weightIncrement: Float = 2.5f,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EditWorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val workoutId: Long = savedStateHandle.get<Long>("workoutId")
        ?: throw IllegalArgumentException("EditWorkoutViewModel requires workoutId")

    private val _state = MutableStateFlow(EditWorkoutState())
    val state: StateFlow<EditWorkoutState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val weightUnit = settingsRepository.getWeightUnit()
            val weightIncrement = settingsRepository.getDefaultWeightIncrement()

            val workout = workoutRepository.getWorkout(workoutId)
            val sets = workoutRepository.getSetsForWorkoutOnce(workoutId)
            val exerciseIds = sets.map { it.exerciseId }.distinct()
            val exercises = exerciseIds.mapNotNull { exerciseRepository.getById(it) }

            val editableExercises = exercises.map { exercise ->
                EditableExercise(
                    exercise = exercise,
                    sets = sets.filter { it.exerciseId == exercise.id }
                        .mapIndexed { index, set ->
                            EditableSet(
                                id = set.id,
                                exerciseId = set.exerciseId,
                                setNumber = index + 1,
                                weight = set.weight,
                                reps = set.reps,
                                note = set.note,
                            )
                        },
                )
            }

            _state.update {
                it.copy(
                    workout = workout,
                    exercises = editableExercises,
                    workoutName = workout?.name ?: "",
                    weightUnit = weightUnit,
                    weightIncrement = weightIncrement,
                    isLoading = false,
                )
            }
        }
    }

    fun updateWorkoutName(name: String) {
        _state.update { it.copy(workoutName = name) }
    }

    fun startEditingSet(set: EditableSet) {
        _state.update {
            it.copy(
                editingSetId = set.id,
                editWeight = formatWeight(set.weight),
                editReps = set.reps.toString(),
            )
        }
    }

    fun cancelEditingSet() {
        _state.update { it.copy(editingSetId = null) }
    }

    fun updateEditWeight(value: String) {
        _state.update { it.copy(editWeight = value) }
    }

    fun updateEditReps(value: String) {
        _state.update { it.copy(editReps = value) }
    }

    fun saveSetEdit() {
        val s = _state.value
        val setId = s.editingSetId ?: return
        val weight = s.editWeight.toFloatOrNull() ?: return
        val reps = s.editReps.toIntOrNull() ?: return

        _state.update { state ->
            state.copy(
                exercises = state.exercises.map { ex ->
                    ex.copy(
                        sets = ex.sets.map { set ->
                            if (set.id == setId) set.copy(
                                weight = weight,
                                reps = reps,
                                isModified = true,
                            ) else set
                        }
                    )
                },
                editingSetId = null,
            )
        }
    }

    fun deleteSet(setId: Long) {
        _state.update { state ->
            state.copy(
                exercises = state.exercises.map { ex ->
                    ex.copy(
                        sets = ex.sets.map { set ->
                            if (set.id == setId) set.copy(isDeleted = true) else set
                        }
                    )
                }.filter { ex -> ex.sets.any { !it.isDeleted } },
                editingSetId = if (state.editingSetId == setId) null else state.editingSetId,
            )
        }
    }

    fun deleteExercise(exerciseId: Long) {
        _state.update { state ->
            state.copy(
                exercises = state.exercises.map { ex ->
                    if (ex.exercise.id == exerciseId) {
                        ex.copy(sets = ex.sets.map { it.copy(isDeleted = true) })
                    } else ex
                }.filter { ex -> ex.sets.any { !it.isDeleted } },
            )
        }
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val s = _state.value

                // Update workout name
                workoutRepository.updateWorkoutName(workoutId, s.workoutName)

                // Process set changes
                for (ex in s.exercises) {
                    for (set in ex.sets) {
                        when {
                            set.isDeleted -> workoutRepository.deleteSetById(set.id)
                            set.isModified -> workoutRepository.updateSetWeightAndReps(
                                set.id, set.weight, set.reps
                            )
                        }
                    }
                }

                // Recalculate total volume
                workoutRepository.recalculateVolume(workoutId)

                _state.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = "Failed to save changes") }
            }
        }
    }

    private fun formatWeight(value: Float): String {
        return if (value % 1f == 0f) value.toInt().toString()
        else "%.1f".format(value)
    }
}
