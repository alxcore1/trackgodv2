package com.trackgod.app.feature.altar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.dao.UserProfileDao
import com.trackgod.app.core.database.entity.WorkoutEntity
import com.trackgod.app.core.repository.ExerciseRepository
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.core.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val weeklyGoal: Int = 4,
    val workoutDaysThisWeek: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
)

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class AltarViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    private val userProfileDao: UserProfileDao,
) : ViewModel() {

    private val _state = MutableStateFlow(AltarState())
    val state: StateFlow<AltarState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        // One-time V1 workout rename
        viewModelScope.launch {
            if (!settingsRepository.getBooleanFlag("v1_workouts_renamed_v2")) {
                workoutRepository.renameV1Workouts()
                settingsRepository.setBooleanFlag("v1_workouts_renamed_v2", true)
            }

            // One-time: populate series field for machine exercises
            if (!settingsRepository.getBooleanFlag("exercise_series_populated_v2")) {
                exerciseRepository.populateSeriesFromNames()
                settingsRepository.setBooleanFlag("exercise_series_populated_v2", true)
            }
        }

        // Observe today's workouts reactively (updates when workout completes)
        viewModelScope.launch {
            workoutRepository.getTodayWorkouts(todayDate).collectLatest { todayWorkouts ->
                val completedToday = todayWorkouts.filter { it.isCompleted }
                val totalVolume = completedToday.mapNotNull { it.totalVolume }.sum()
                val totalDurationSeconds = completedToday.mapNotNull { it.durationSeconds }.sum()

                // Load today's sets for set count
                val todayWorkoutIds = completedToday.map { it.id }
                val todaySets = workoutRepository.getSetsForWorkoutIds(todayWorkoutIds)

                // Refresh non-reactive data when today's workouts change
                val recent = workoutRepository.getRecentCompletedWorkouts(5)
                val streak = calculateStreak()
                val weekDays = calculateWorkoutDaysThisWeek()

                _state.update {
                    it.copy(
                        todayWorkoutCount = completedToday.size,
                        todaySets = todaySets.size,
                        todayVolume = totalVolume,
                        todayDurationMinutes = totalDurationSeconds / 60,
                        recentWorkouts = recent,
                        currentStreak = streak,
                        workoutDaysThisWeek = weekDays,
                        isLoading = false,
                    )
                }
            }
        }

        // Load non-reactive data: streak, incomplete workout, recent workouts, weekly goal
        viewModelScope.launch {
            // Load weekly target from user profile
            val profile = userProfileDao.getProfileOnce()
            val weeklyGoal = profile?.weeklyTarget ?: 4

            // Check SharedPreferences first for active workout ID (survives app kill)
            val activeId = settingsRepository.getActiveWorkoutId()
            var incompleteWorkout: WorkoutEntity? = null
            if (activeId != null) {
                val workout = workoutRepository.getWorkout(activeId)
                if (workout != null && !workout.isCompleted) {
                    incompleteWorkout = workout
                } else {
                    // Stale ID -- workout was deleted or already completed
                    settingsRepository.clearActiveWorkoutId()
                }
            }
            // Fallback: also check DB in case prefs were cleared but DB has an incomplete workout
            if (incompleteWorkout == null) {
                incompleteWorkout = workoutRepository.getIncompleteWorkout()
                if (incompleteWorkout != null) {
                    // Sync prefs with DB state
                    settingsRepository.setActiveWorkoutId(incompleteWorkout.id)
                }
            }

            val recent = workoutRepository.getRecentCompletedWorkouts(5)
            val streak = calculateStreak()
            val weekDays = calculateWorkoutDaysThisWeek()

            _state.update {
                it.copy(
                    hasIncompleteWorkout = incompleteWorkout != null,
                    incompleteWorkoutId = incompleteWorkout?.id,
                    recentWorkouts = recent,
                    currentStreak = streak,
                    weeklyGoal = weeklyGoal,
                    workoutDaysThisWeek = weekDays,
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

    /**
     * Calculate which days this week (Mon=1 .. Sun=7) have completed workouts.
     */
    private suspend fun calculateWorkoutDaysThisWeek(): Set<Int> {
        val dates = workoutRepository.getCompletedWorkoutDates()
        if (dates.isEmpty()) return emptySet()

        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)

        return dates.mapNotNull { dateStr ->
            runCatching { LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
        }.filter { date ->
            !date.isBefore(weekStart) && !date.isAfter(weekEnd)
        }.map { date ->
            date.dayOfWeek.value // Monday=1, Sunday=7
        }.toSet()
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
