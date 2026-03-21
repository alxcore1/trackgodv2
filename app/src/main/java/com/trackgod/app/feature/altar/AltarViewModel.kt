package com.trackgod.app.feature.altar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.WorkoutEntity
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.core.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ── State ────────────────────────────────────────────────────────────────────

data class AltarState(
    val todayWorkoutCount: Int = 0,
    val todaySets: Int = 0,
    val todayVolume: Float = 0f,
    val todayDurationMinutes: Int = 0,
    val currentStreak: Int = 0,
    val hasIncompleteWorkout: Boolean = false,
    val incompleteWorkoutId: Long? = null,
    val recentWorkouts: List<WorkoutEntity> = emptyList(),
    val isLoading: Boolean = true,
)

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class AltarViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AltarState())
    val state: StateFlow<AltarState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Observe today's workouts reactively (updates when workout completes)
        viewModelScope.launch {
            workoutRepository.getTodayWorkouts(todayDate).collectLatest { todayWorkouts ->
                val completedToday = todayWorkouts.filter { it.isCompleted }
                val totalVolume = completedToday.mapNotNull { it.totalVolume }.sum()
                val totalDurationSeconds = completedToday.mapNotNull { it.durationSeconds }.sum()

                // Load today's sets for set count
                val todayWorkoutIds = completedToday.map { it.id }
                val todaySets = workoutRepository.getSetsForWorkoutIds(todayWorkoutIds)

                _state.update {
                    it.copy(
                        todayWorkoutCount = completedToday.size,
                        todaySets = todaySets.size,
                        todayVolume = totalVolume,
                        todayDurationMinutes = totalDurationSeconds / 60,
                    )
                }
            }
        }

        // Load non-reactive data: streak, incomplete workout, recent workouts
        viewModelScope.launch {
            val incomplete = workoutRepository.getIncompleteWorkout()
            val recent = workoutRepository.getRecentCompletedWorkouts(5)
            val streak = calculateStreak()

            _state.update {
                it.copy(
                    hasIncompleteWorkout = incomplete != null,
                    incompleteWorkoutId = incomplete?.id,
                    recentWorkouts = recent,
                    currentStreak = streak,
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Calculate current streak: consecutive days backward from today
     * that have at least one completed workout.
     */
    private suspend fun calculateStreak(): Int {
        val dates = workoutRepository.getCompletedWorkoutDates()
        if (dates.isEmpty()) return 0

        val today = LocalDate.now()
        val workoutDates = dates.mapNotNull { dateStr ->
            runCatching { LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
        }.toSet()

        var streak = 0
        var day = today
        while (workoutDates.contains(day)) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    suspend fun startNewWorkout(): Long {
        val workoutId = workoutRepository.createWorkout()
        settingsRepository.setActiveWorkoutId(workoutId)
        return workoutId
    }

    fun resumeWorkout(): Long? {
        return _state.value.incompleteWorkoutId
    }

    fun discardIncompleteWorkout() {
        val workoutId = _state.value.incompleteWorkoutId ?: return
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            settingsRepository.clearActiveWorkoutId()
            _state.update {
                it.copy(
                    hasIncompleteWorkout = false,
                    incompleteWorkoutId = null,
                )
            }
        }
    }
}
