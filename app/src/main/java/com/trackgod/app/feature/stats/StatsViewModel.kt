package com.trackgod.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.core.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// -- State & data models ------------------------------------------------------

data class StatsState(
    // Time range
    val selectedTimeRange: TimeRange = TimeRange.MONTH,

    // Volume Progression (bar chart data)
    val volumeByPeriod: List<VolumeDataPoint> = emptyList(),
    val totalVolume: Float = 0f,

    // Muscle Group Distribution (donut chart)
    val muscleGroupVolumes: List<MuscleGroupData> = emptyList(),

    // Personal Records
    val personalRecords: List<PersonalRecordData> = emptyList(),

    // Training Heatmap (90 days calendar grid)
    val heatmapData: List<HeatmapDay> = emptyList(),

    // Strength Balance
    val strengthBalance: List<StrengthBalanceData> = emptyList(),

    // Exercise Frequency (top exercises by usage count)
    val exerciseFrequency: List<ExerciseFrequencyData> = emptyList(),

    // Workout Consistency
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val workoutsPerWeek: List<WeeklyConsistencyData> = emptyList(),
    val totalWorkouts: Int = 0,

    val weightUnit: String = "kg",
    val isLoading: Boolean = true,
    val hasData: Boolean = false,
)

enum class TimeRange(val label: String, val days: Int) {
    WEEK("WEEK", 7),
    MONTH("MONTH", 30),
    QUARTER("QUARTER", 90),
    YEAR("YEAR", 365),
    ALL("ALL", Int.MAX_VALUE),
}

data class VolumeDataPoint(val label: String, val volume: Float, val date: String)
data class MuscleGroupData(val category: String, val volume: Float, val percentage: Float)
data class PersonalRecordData(val exerciseName: String, val estimated1rm: Float, val weight: Float, val reps: Int)
data class HeatmapDay(val date: LocalDate, val volume: Float, val intensity: Int)
data class StrengthBalanceData(val category: String, val volume: Float, val percentage: Float)
data class ExerciseFrequencyData(val exerciseName: String, val count: Int, val maxCount: Int)
data class WeeklyConsistencyData(val weekLabel: String, val workoutCount: Int)

// -- ViewModel ----------------------------------------------------------------

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        _state.update { it.copy(weightUnit = settingsRepository.getWeightUnit()) }
        loadAnalytics()
    }

    // -- Public actions -------------------------------------------------------

    fun onTimeRangeChanged(range: TimeRange) {
        _state.update { it.copy(selectedTimeRange = range, isLoading = true) }
        loadAnalytics()
    }

    // -- Data loading ---------------------------------------------------------

    private fun loadAnalytics() {
        viewModelScope.launch {
            try {
                val range = _state.value.selectedTimeRange
                val today = LocalDate.now()
                val startDate = if (range.days == Int.MAX_VALUE) {
                    LocalDate.of(2000, 1, 1)
                } else {
                    today.minusDays(range.days.toLong())
                }
                val startStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val endStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

                // Load all data in parallel via structured concurrency
                val volumeByDate = workoutRepository.getVolumeByDate(startStr, endStr)
                val categoryVolumes = workoutRepository.getVolumeByCategory(startStr, endStr)
                val personalRecords = workoutRepository.getPersonalRecords()
                val allCompletedWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
                val exerciseFrequency = workoutRepository.getExerciseFrequency(startStr, endStr)

                // 1. Volume Progression
                val volumeDataPoints = buildVolumeProgression(volumeByDate, range, today, startDate)
                val totalVolume = volumeByDate.sumOf { (it.totalVolume ?: 0f).toDouble() }.toFloat()

                // 2. Muscle Group Distribution
                val totalCategoryVolume = categoryVolumes.sumOf { it.totalVolume.toDouble() }.toFloat()
                val muscleGroupData = categoryVolumes
                    .sortedByDescending { it.totalVolume }
                    .map { cv ->
                        val pct = if (totalCategoryVolume > 0) cv.totalVolume / totalCategoryVolume * 100f else 0f
                        MuscleGroupData(cv.category, cv.totalVolume, pct)
                    }

                // 3. Personal Records (top 6 by estimated 1RM)
                val prData = personalRecords
                    .sortedByDescending { it.estimated1rm }
                    .take(6)
                    .map { pr ->
                        PersonalRecordData(pr.name, pr.estimated1rm, pr.weight, pr.reps)
                    }

                // 4. Training Heatmap (use selected range, but cap at 90 days minimum)
                val heatmapStart = if (range.days <= 90) startDate else today.minusDays(89)
                val heatmap = buildHeatmap(allCompletedWorkouts, heatmapStart, today)

                // 5. Strength Balance (grouped categories)
                val strengthBalance = buildStrengthBalance(categoryVolumes)

                // 6. Exercise Frequency
                val maxCount = exerciseFrequency.maxOfOrNull { it.count } ?: 1
                val freqData = exerciseFrequency.map { ef ->
                    ExerciseFrequencyData(ef.name, ef.count, maxCount)
                }

                // 7. Workout Consistency
                val workoutDates = allCompletedWorkouts.mapNotNull { w ->
                    runCatching { LocalDate.parse(w.date, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
                }.toSortedSet()
                val currentStreak = calculateCurrentStreak(workoutDates, today)
                val longestStreak = calculateLongestStreak(workoutDates)
                val weeklyConsistency = buildWeeklyConsistency(allCompletedWorkouts, today)
                val totalWorkoutsInRange = allCompletedWorkouts.count { w ->
                    val d = runCatching { LocalDate.parse(w.date, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
                    d != null && !d.isBefore(startDate) && !d.isAfter(today)
                }

                _state.update {
                    it.copy(
                        volumeByPeriod = volumeDataPoints,
                        totalVolume = totalVolume,
                        muscleGroupVolumes = muscleGroupData,
                        personalRecords = prData,
                        heatmapData = heatmap,
                        strengthBalance = strengthBalance,
                        exerciseFrequency = freqData,
                        currentStreak = currentStreak,
                        longestStreak = longestStreak,
                        workoutsPerWeek = weeklyConsistency,
                        totalWorkouts = totalWorkoutsInRange,
                        isLoading = false,
                        hasData = allCompletedWorkouts.isNotEmpty(),
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // -- Volume Progression ---------------------------------------------------

    private fun buildVolumeProgression(
        volumeByDate: List<com.trackgod.app.core.database.dao.DateVolume>,
        range: TimeRange,
        today: LocalDate,
        startDate: LocalDate,
    ): List<VolumeDataPoint> {
        if (volumeByDate.isEmpty()) return emptyList()

        // Parse all date-volume pairs
        val dateVolumeMap = mutableMapOf<LocalDate, Float>()
        for (dv in volumeByDate) {
            val date = runCatching { LocalDate.parse(dv.date, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
                ?: continue
            dateVolumeMap[date] = (dateVolumeMap[date] ?: 0f) + (dv.totalVolume ?: 0f)
        }

        return when (range) {
            TimeRange.WEEK -> {
                // Group by day
                val dayLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                (0..6).map { offset ->
                    val day = weekStart.plusDays(offset.toLong())
                    val vol = dateVolumeMap[day] ?: 0f
                    VolumeDataPoint(dayLabels[offset], vol, day.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            }

            TimeRange.MONTH -> {
                // Group by week (W1, W2, W3, W4, W5)
                val weeks = mutableListOf<VolumeDataPoint>()
                var weekStart = startDate
                var weekIndex = 1
                while (!weekStart.isAfter(today)) {
                    val weekEnd = minOf(weekStart.plusDays(6), today)
                    val vol = dateVolumeMap.entries
                        .filter { (d, _) -> !d.isBefore(weekStart) && !d.isAfter(weekEnd) }
                        .sumOf { it.value.toDouble() }.toFloat()
                    weeks.add(VolumeDataPoint("W$weekIndex", vol, weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                    weekStart = weekEnd.plusDays(1)
                    weekIndex++
                }
                weeks
            }

            TimeRange.QUARTER -> {
                // Group by week
                val weeks = mutableListOf<VolumeDataPoint>()
                var weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                var weekIndex = 1
                while (!weekStart.isAfter(today)) {
                    val weekEnd = minOf(weekStart.plusDays(6), today)
                    val vol = dateVolumeMap.entries
                        .filter { (d, _) -> !d.isBefore(weekStart) && !d.isAfter(weekEnd) }
                        .sumOf { it.value.toDouble() }.toFloat()
                    weeks.add(VolumeDataPoint("W$weekIndex", vol, weekStart.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                    weekStart = weekEnd.plusDays(1)
                    weekIndex++
                }
                weeks
            }

            TimeRange.YEAR, TimeRange.ALL -> {
                // Group by month
                val monthLabels = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
                val months = mutableListOf<VolumeDataPoint>()
                var current = startDate.withDayOfMonth(1)
                while (!current.isAfter(today)) {
                    val monthEnd = current.plusMonths(1).minusDays(1)
                    val vol = dateVolumeMap.entries
                        .filter { (d, _) -> !d.isBefore(current) && !d.isAfter(monthEnd) }
                        .sumOf { it.value.toDouble() }.toFloat()
                    val label = monthLabels[current.monthValue - 1]
                    months.add(VolumeDataPoint(label, vol, current.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                    current = current.plusMonths(1)
                }
                months
            }
        }
    }

    // -- Heatmap --------------------------------------------------------------

    private fun buildHeatmap(
        workouts: List<com.trackgod.app.core.database.entity.WorkoutEntity>,
        start: LocalDate,
        end: LocalDate,
    ): List<HeatmapDay> {
        // Build date -> total volume map
        val volumeByDate = mutableMapOf<LocalDate, Float>()
        for (w in workouts) {
            val date = runCatching { LocalDate.parse(w.date, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
                ?: continue
            if (!date.isBefore(start) && !date.isAfter(end)) {
                volumeByDate[date] = (volumeByDate[date] ?: 0f) + (w.totalVolume ?: 0f)
            }
        }

        val days = ChronoUnit.DAYS.between(start, end).toInt() + 1
        return (0 until days).map { offset ->
            val day = start.plusDays(offset.toLong())
            val volume = volumeByDate[day] ?: 0f
            val intensity = when {
                volume <= 0f -> 0
                volume < 2000f -> 1
                volume < 5000f -> 2
                volume < 10000f -> 3
                else -> 4
            }
            HeatmapDay(day, volume, intensity)
        }
    }

    // -- Strength Balance -----------------------------------------------------

    private fun buildStrengthBalance(
        categoryVolumes: List<com.trackgod.app.core.database.dao.CategoryVolume>,
    ): List<StrengthBalanceData> {
        // Map exercise categories to body regions
        val upperCategories = setOf("Chest", "Shoulders", "Arms", "Biceps", "Triceps")
        val lowerCategories = setOf("Legs", "Quadriceps", "Hamstrings", "Glutes", "Calves")
        val backCategories = setOf("Back", "Lats", "Traps")
        val coreCategories = setOf("Core", "Abs", "Abdominals")

        var upperVol = 0f
        var lowerVol = 0f
        var backVol = 0f
        var coreVol = 0f

        for (cv in categoryVolumes) {
            val cat = cv.category
            when {
                upperCategories.any { cat.equals(it, ignoreCase = true) } -> upperVol += cv.totalVolume
                lowerCategories.any { cat.equals(it, ignoreCase = true) } -> lowerVol += cv.totalVolume
                backCategories.any { cat.equals(it, ignoreCase = true) } -> backVol += cv.totalVolume
                coreCategories.any { cat.equals(it, ignoreCase = true) } -> coreVol += cv.totalVolume
                else -> upperVol += cv.totalVolume // Default unknown categories to upper
            }
        }

        val total = upperVol + lowerVol + backVol + coreVol
        if (total <= 0f) return emptyList()

        return listOf(
            StrengthBalanceData("Upper", upperVol, upperVol / total * 100f),
            StrengthBalanceData("Lower", lowerVol, lowerVol / total * 100f),
            StrengthBalanceData("Back", backVol, backVol / total * 100f),
            StrengthBalanceData("Core", coreVol, coreVol / total * 100f),
        ).filter { it.volume > 0f }
    }

    // -- Streak calculations --------------------------------------------------

    private fun calculateCurrentStreak(workoutDates: Set<LocalDate>, today: LocalDate): Int {
        if (workoutDates.isEmpty()) return 0
        var streak = 0
        var day = today
        while (day in workoutDates) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    private fun calculateLongestStreak(workoutDates: Set<LocalDate>): Int {
        if (workoutDates.isEmpty()) return 0
        val sorted = workoutDates.sorted()
        var longest = 1
        var current = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].plusDays(1)) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    // -- Weekly Consistency ---------------------------------------------------

    private fun buildWeeklyConsistency(
        workouts: List<com.trackgod.app.core.database.entity.WorkoutEntity>,
        today: LocalDate,
    ): List<WeeklyConsistencyData> {
        // Last 8 weeks, each counted by number of workout days
        val result = mutableListOf<WeeklyConsistencyData>()
        for (weeksAgo in 7 downTo 0) {
            val weekStart = today.minusWeeks(weeksAgo.toLong())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekEnd = weekStart.plusDays(6)
            val count = workouts.count { w ->
                val date = runCatching { LocalDate.parse(w.date, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
                date != null && !date.isBefore(weekStart) && !date.isAfter(weekEnd) && w.isCompleted
            }
            val label = "${weekStart.dayOfMonth}/${weekStart.monthValue}"
            result.add(WeeklyConsistencyData(label, count))
        }
        return result
    }
}
