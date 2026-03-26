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
import android.content.Context
import com.trackgod.app.service.RestTimerAlarmScheduler
import com.trackgod.app.service.WorkoutForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.trackgod.app.core.util.WorkoutNaming
import java.util.Locale
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
    val lastSessionSets: List<SetEntity> = emptyList(),
    val restTimeRemaining: Int = 0,
    val isRestTimerRunning: Boolean = false,
    val isRestTimerPaused: Boolean = false,
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
    val restTimerCompleted: Boolean = false,
    val finishError: String? = null,
    val inputError: String? = null,
    val prMessage: String? = null,
    val prSetIds: Set<Long> = emptySet(),
    val overloadHint: String? = null,
    val setTypeInput: String = "working",
    // Superset state (session-only, not persisted)
    val supersetGroups: Map<Int, List<Long>> = emptyMap(),  // groupId → exerciseIds
    val supersetOffer: Long? = null,  // exerciseId being offered for superset
    val supersetOfferNewId: Long? = null,  // new exercise being offered
)

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    private val routineRepository: com.trackgod.app.core.repository.RoutineRepository,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutSessionState())
    val state: StateFlow<WorkoutSessionState> = _state.asStateFlow()

    private val restTimerManager = RestTimerManager()

    private var sessionTimerJob: Job? = null
    private var setsCollectionJob: Job? = null
    private var workoutId: Long = -1L

    init {
        val passedId: Long = savedStateHandle.get<Long>("workoutId") ?: -1L

        if (passedId > 0) viewModelScope.launch {
            // Load settings
            loadSettings()

            // Use the workout ID provided via navigation route argument.
            // The workout is already created by AltarViewModel before navigation.
            workoutId = passedId

            // Load workout entity
            val workout = workoutRepository.getWorkout(workoutId)
            _state.update { it.copy(workout = workout, isLoading = false) }

            // Start foreground service to keep app alive
            val startTime = workout?.startTime ?: System.currentTimeMillis()
            WorkoutForegroundService.start(appContext, startTime)

            // Start session timer
            startSessionTimer(startTime)

            // Observe all sets for this workout (for stats)
            observeSets()

            // Pre-load exercises from template if routineId is provided
            val routineId = savedStateHandle.get<Long>("routineId") ?: -1L
            if (routineId > 0) {
                val routineExercises = routineRepository.getExercisesForRoutine(routineId)
                if (routineExercises.isNotEmpty()) {
                    val firstExId = routineExercises.first().exerciseId
                    val exercise = exerciseRepository.getById(firstExId)
                    if (exercise != null) selectExercise(exercise)
                }
                routineRepository.updateLastUsed(routineId)
            }
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
        viewModelScope.launch {
            restTimerManager.isPaused.collectLatest { paused ->
                _state.update { it.copy(isRestTimerPaused = paused) }
            }
        }

        // Exercise picker result is observed by NavHost LaunchedEffect
        // and forwarded via selectExerciseById()
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

                val totalVolume = allSets.filter { it.setType != "warmup" }.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
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

    fun selectExerciseById(exerciseId: Long) {
        viewModelScope.launch {
            val exercise = exerciseRepository.getById(exerciseId) ?: return@launch
            val currentEx = _state.value.currentExercise
            val isNewToSession = _state.value.allExercisesInSession.none { it.exercise.id == exerciseId }

            // Offer superset if switching from another exercise to a brand-new one
            // Don't switch yet — wait for dialog response
            if (currentEx != null && isNewToSession && currentEx.id != exerciseId) {
                offerSuperset(currentEx.id, exerciseId)
                // Store pending exercise, will be selected after dialog
                return@launch
            }

            selectExercise(exercise)
        }
    }

    fun closeExercise() {
        _state.update {
            it.copy(
                currentExercise = null,
                editingSetId = null,
                weightInput = "",
                repsInput = "",
                noteInput = "",
                rpeInput = null,
                rirInput = null,
                lastSessionHint = null,
                inputError = null,
            )
        }
    }

    fun selectExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    currentExercise = exercise,
                    editingSetId = null,
                    noteInput = "",
                    lastSessionHint = null,
                    lastSessionSets = emptyList(),
                )
            }

            // Load smart defaults from previous session
            val recentSets = workoutRepository.getRecentSetsForExercise(exercise.id)
            // Only show sets from the most recent workout (not mixed from multiple)
            val lastWorkoutId = recentSets.firstOrNull()?.workoutId
            val lastWorkoutSets = if (lastWorkoutId != null) {
                recentSets.filter { it.workoutId == lastWorkoutId }
            } else emptyList()

            val unit = _state.value.weightUnit
            val increment = _state.value.weightIncrement
            if (lastWorkoutSets.isNotEmpty()) {
                // Compute progressive overload suggestion
                val suggestion = computeOverloadSuggestion(lastWorkoutSets, increment)
                val sugWeight = formatWeight(suggestion.first, increment)
                val sugReps = suggestion.second
                val lastSet = lastWorkoutSets.first()
                val lastWeightStr = formatWeight(lastSet.weight, increment)

                val hint = if (suggestion.first != lastSet.weight || suggestion.second != lastSet.reps) {
                    "LAST: $lastWeightStr$unit x ${lastSet.reps}  →  TRY: $sugWeight$unit x $sugReps"
                } else {
                    "LAST: $lastWeightStr$unit x ${lastSet.reps}  →  REPEAT"
                }

                _state.update {
                    it.copy(
                        weightInput = sugWeight,
                        repsInput = sugReps.toString(),
                        lastSessionHint = hint,
                        lastSessionSets = lastWorkoutSets,
                        overloadHint = hint,
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        weightInput = "",
                        repsInput = "",
                        overloadHint = null,
                    )
                }
            }

            // Trigger set re-filter for new exercise
            observeSets()
        }
    }

    // ── Input Updates ────────────────────────────────────────────────────────

    fun updateWeight(value: String) {
        if (value.isEmpty() || value.matches(Regex("""^\d*[.,]?\d*$"""))) {
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

    fun cycleSetType() {
        val next = when (_state.value.setTypeInput) {
            "working" -> "warmup"
            "warmup" -> "drop"
            "drop" -> "failure"
            else -> "working"
        }
        _state.update { it.copy(setTypeInput = next) }
    }

    // ── Supersets ──────────────────────────────────────────────────────────

    fun offerSuperset(currentExerciseId: Long, newExerciseId: Long) {
        _state.update { it.copy(supersetOffer = currentExerciseId, supersetOfferNewId = newExerciseId) }
    }

    fun acceptSuperset() {
        val s = _state.value
        val current = s.supersetOffer ?: return
        val newEx = s.supersetOfferNewId ?: return

        // Check if either exercise is already in a group
        val existingGroup = s.supersetGroups.entries.firstOrNull { current in it.value || newEx in it.value }
        val groups = s.supersetGroups.toMutableMap()

        if (existingGroup != null) {
            val updated = (existingGroup.value + current + newEx).distinct()
            groups[existingGroup.key] = updated
        } else {
            val nextId = (s.supersetGroups.keys.maxOrNull() ?: 0) + 1
            groups[nextId] = listOf(current, newEx)
        }

        _state.update { it.copy(supersetGroups = groups, supersetOffer = null, supersetOfferNewId = null) }

        // Now switch to the new exercise
        viewModelScope.launch {
            val exercise = exerciseRepository.getById(newEx)
            if (exercise != null) selectExercise(exercise)
        }
    }

    fun declineSuperset() {
        val newExId = _state.value.supersetOfferNewId
        _state.update { it.copy(supersetOffer = null, supersetOfferNewId = null) }

        // Switch to the new exercise even though not supersetted
        if (newExId != null) {
            viewModelScope.launch {
                val exercise = exerciseRepository.getById(newExId)
                if (exercise != null) selectExercise(exercise)
            }
        }
    }

    fun getSupersetPartner(exerciseId: Long): ExerciseEntity? {
        val s = _state.value
        val group = s.supersetGroups.values.firstOrNull { exerciseId in it } ?: return null
        val partnerId = group.firstOrNull { it != exerciseId } ?: return null
        return s.allExercisesInSession.firstOrNull { it.exercise.id == partnerId }?.exercise
    }

    fun updateRpe(value: String) {
        val rpe = value.toIntOrNull()?.coerceIn(1, 10)
        _state.update { it.copy(rpeInput = if (value.isEmpty()) null else rpe) }
    }

    fun updateRir(value: String) {
        val rir = value.toIntOrNull()?.coerceIn(0, 5)
        _state.update { it.copy(rirInput = if (value.isEmpty()) null else rir) }
    }

    fun incrementWeight(delta: Float) {
        val current = _state.value.weightInput.replace(",", ".").toFloatOrNull() ?: 0f
        val next = (current + delta).coerceIn(0f, MAX_WEIGHT)
        _state.update { it.copy(weightInput = formatWeight(next, _state.value.weightIncrement)) }
    }

    fun incrementReps(delta: Int) {
        val current = _state.value.repsInput.toIntOrNull() ?: 0
        val next = (current + delta).coerceIn(0, MAX_REPS)
        _state.update { it.copy(repsInput = next.toString()) }
    }

    // ── Set Logging ──────────────────────────────────────────────────────────

    fun logSet() {
        val s = _state.value
        val exercise = s.currentExercise ?: return
        val weight = s.weightInput.replace(",", ".").toFloatOrNull()?.coerceAtMost(MAX_WEIGHT)
        val reps = s.repsInput.toIntOrNull()?.coerceAtMost(MAX_REPS)
        if (weight == null || reps == null || weight < 0f || reps <= 0) {
            _state.update { it.copy(inputError = "ENTER VALID WEIGHT AND REPS") }
            return
        }

        val isWarmup = s.setTypeInput == "warmup"

        viewModelScope.launch {
            // Check previous best BEFORE logging the new set (skip for warmups)
            val previousBest1RM = if (!isWarmup) {
                workoutRepository.getBest1RMForExercise(exercise.id) ?: 0f
            } else 0f

            val setId = workoutRepository.addSet(
                workoutId = workoutId,
                exerciseId = exercise.id,
                weight = weight,
                reps = reps,
                note = s.noteInput.ifBlank { null },
                rpe = s.rpeInput,
                rir = s.rirInput,
                setType = s.setTypeInput,
            )

            // PR detection (skip for warmups)
            val new1RM = weight * (1 + 0.0333f * reps)
            if (!isWarmup && new1RM > previousBest1RM && weight > 0f) {
                _state.update { it.copy(
                    prMessage = "NEW PR!",
                    prSetIds = it.prSetIds + setId,
                ) }
                // Auto-dismiss after 3 seconds
                launch {
                    delay(3000L)
                    _state.update { it.copy(prMessage = null) }
                }
            }

            // Clear note/error/setType after logging (weight/reps stay for easy repeat)
            _state.update { it.copy(noteInput = "", rpeInput = null, rirInput = null, inputError = null, setTypeInput = "working") }

            // Auto-start rest timer (skip for warmups)
            if (s.restTimerEnabled && s.restTimerAutoStart && !isWarmup) {
                restTimerManager.start(
                    durationSeconds = s.restTimerDuration,
                    scope = viewModelScope,
                    onComplete = {
                        _state.update { it.copy(restTimerCompleted = true) }
                    },
                )
                // Schedule alarm for screen-off notification
                RestTimerAlarmScheduler.schedule(appContext, s.restTimerDuration)
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
        val weight = (s.weightInput.replace(",", ".").toFloatOrNull() ?: return).coerceAtMost(MAX_WEIGHT)
        val reps = (s.repsInput.toIntOrNull() ?: return).coerceAtMost(MAX_REPS)
        if (weight < 0f || reps <= 0) return

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
        RestTimerAlarmScheduler.cancel(appContext)
    }

    fun pauseRestTimer() {
        restTimerManager.pause()
        RestTimerAlarmScheduler.cancel(appContext)
    }

    fun resumeRestTimer() {
        restTimerManager.resume()
        // Reschedule alarm for the remaining time
        val remaining = _state.value.restTimeRemaining
        if (remaining > 0) {
            RestTimerAlarmScheduler.schedule(appContext, remaining)
        }
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        restTimerManager.adjustTime(deltaSeconds)
        // Read actual remaining time from timer manager (not stale state)
        val remaining = restTimerManager.timeRemaining.value
        if (remaining > 0 && _state.value.isRestTimerRunning && !_state.value.isRestTimerPaused) {
            RestTimerAlarmScheduler.schedule(appContext, remaining)
        }
    }

    /** Toggle rest timer on/off for the current session only (not persisted). */
    fun toggleRestTimerForSession() {
        val wasEnabled = _state.value.restTimerEnabled
        if (wasEnabled) {
            restTimerManager.skip()
            RestTimerAlarmScheduler.cancel(appContext)
        }
        _state.update { it.copy(restTimerEnabled = !wasEnabled) }
    }

    /** Called by the UI after handling the rest-timer-completed vibration. */
    fun consumeRestTimerCompleted() {
        _state.update { it.copy(restTimerCompleted = false) }
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
        return WorkoutNaming.generateName(categories)
    }

    fun confirmFinish(name: String, saveAsTemplate: Boolean = false, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                workoutRepository.completeWorkout(workoutId, name)
                if (saveAsTemplate) {
                    routineRepository.createFromWorkout(workoutId, name)
                }
                settingsRepository.clearActiveWorkoutId()
                sessionTimerJob?.cancel()
                restTimerManager.stop()
                RestTimerAlarmScheduler.cancel(appContext)
                WorkoutForegroundService.stop(appContext)
                _state.update { it.copy(finishError = null) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(finishError = "Failed to save workout. Please retry.") }
            }
        }
    }

    fun clearFinishError() {
        _state.update { it.copy(finishError = null) }
    }

    fun discardWorkout(onComplete: () -> Unit) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            settingsRepository.clearActiveWorkoutId()
            sessionTimerJob?.cancel()
            restTimerManager.stop()
            RestTimerAlarmScheduler.cancel(appContext)
            WorkoutForegroundService.stop(appContext)
            onComplete()
        }
    }

    companion object {
        const val MAX_WEIGHT = 9999f
        const val MAX_REPS = 999
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Compute progressive overload suggestion from last session sets.
     * Returns Pair(suggestedWeight, suggestedReps).
     */
    private fun computeOverloadSuggestion(
        lastSets: List<SetEntity>,
        increment: Float,
    ): Pair<Float, Int> {
        if (lastSets.isEmpty()) return Pair(0f, 0)

        // Find the most common weight (mode)
        val targetWeight = lastSets.groupBy { it.weight }
            .maxByOrNull { it.value.size }?.key ?: lastSets.first().weight
        val setsAtTarget = lastSets.filter { it.weight == targetWeight }
        val targetReps = setsAtTarget.first().reps
        val minReps = setsAtTarget.minOf { it.reps }

        return when {
            // All sets hit target reps → increase weight
            minReps >= targetReps -> Pair(targetWeight + increment, targetReps)
            // Slight drop-off (within 2 reps) → try +1 rep at same weight
            minReps >= targetReps - 2 -> Pair(targetWeight, targetReps + 1)
            // Significant drop-off → repeat same
            else -> Pair(targetWeight, targetReps)
        }
    }

    private fun formatWeight(value: Float, increment: Float): String {
        return if (increment % 1f == 0f) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionTimerJob?.cancel()
        restTimerManager.stop()
        RestTimerAlarmScheduler.cancel(appContext)
    }
}
