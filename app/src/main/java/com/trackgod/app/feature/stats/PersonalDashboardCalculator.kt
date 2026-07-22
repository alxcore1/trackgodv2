package com.trackgod.app.feature.stats

import com.trackgod.app.core.database.entity.BodyMetricEntity
import com.trackgod.app.core.database.entity.UserProfileEntity
import com.trackgod.app.core.database.entity.WorkoutEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

enum class PersonalObjective(val displayName: String) {
    LoseWeight("LOSE WEIGHT"),
    GainMuscle("GAIN MUSCLE"),
    GetFit("GET FIT"),
}

data class PersonalMetricCardData(
    val title: String,
    val value: String,
    val detail: String = "",
    val isLocked: Boolean = false,
)

data class KcalInputs(
    val ageYears: Int? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val isGenderNeutralEstimate: Boolean = false,
)

data class PersonalDashboardData(
    val appTenure: PersonalMetricCardData = PersonalMetricCardData("APP TENURE", "0 DAYS"),
    val age: PersonalMetricCardData = PersonalMetricCardData("AGE", "ADD BIRTHDAY", isLocked = true),
    val currentWeight: PersonalMetricCardData = PersonalMetricCardData("CURRENT WEIGHT", "ADD WEIGHT", isLocked = true),
    val lastWeighIn: PersonalMetricCardData = PersonalMetricCardData("LAST WEIGH-IN", "LOG WEIGHT", isLocked = true),
    val objective: PersonalMetricCardData = PersonalMetricCardData("OBJECTIVE", "SET OBJECTIVE", isLocked = true),
    val recommendedKcal: PersonalMetricCardData = PersonalMetricCardData("RECOMMENDED KCAL", "ADD BIRTHDAY + HEIGHT + WEIGHT TO UNLOCK KCAL", isLocked = true),
    val maintenanceKcal: PersonalMetricCardData = PersonalMetricCardData("MAINTENANCE KCAL", "ADD BIRTHDAY + HEIGHT + WEIGHT TO UNLOCK TDEE", isLocked = true),
    val averageWorkoutsPerWeek: PersonalMetricCardData = PersonalMetricCardData("AVG WORKOUTS / WEEK", "0.0"),
    val targetAdherence: PersonalMetricCardData = PersonalMetricCardData("TARGET ADHERENCE", "0%"),
    val favoriteWeekday: PersonalMetricCardData = PersonalMetricCardData("FAVORITE WEEKDAY", "NO SIGNAL", isLocked = true),
    val favoriteTimeWindow: PersonalMetricCardData = PersonalMetricCardData("FAVORITE TIME", "NO SIGNAL", isLocked = true),
    val averageSessionDuration: PersonalMetricCardData = PersonalMetricCardData("AVG SESSION", "NO DURATION", isLocked = true),
    val longestRestGap: PersonalMetricCardData = PersonalMetricCardData("LONGEST REST GAP", "NO GAP", isLocked = true),
    val kcal: KcalInputs = KcalInputs(),
) {
    val cards: List<PersonalMetricCardData>
        get() = listOf(
            appTenure,
            age,
            currentWeight,
            lastWeighIn,
            objective,
            recommendedKcal,
            maintenanceKcal,
            averageWorkoutsPerWeek,
            targetAdherence,
            favoriteWeekday,
            favoriteTimeWindow,
            averageSessionDuration,
            longestRestGap,
        )
}

object PersonalDashboardCalculator {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun normalizeObjective(value: String?): PersonalObjective? {
        val normalized = value
            ?.trim()
            ?.lowercase()
            ?.replace("-", "_")
            ?.replace(" ", "_")
            ?: return null
        return when (normalized) {
            "lose_weight" -> PersonalObjective.LoseWeight
            "gain_muscle" -> PersonalObjective.GainMuscle
            "get_fit" -> PersonalObjective.GetFit
            else -> null
        }
    }

    fun build(
        profile: UserProfileEntity?,
        latestBodyMetric: BodyMetricEntity?,
        completedWorkouts: List<WorkoutEntity>,
        today: LocalDate = LocalDate.now(),
    ): PersonalDashboardData {
        if (profile == null) return PersonalDashboardData()

        val recentWorkouts = completedWorkouts
            .filter { it.isCompleted }
            .mapNotNull { workout -> parseDate(workout.date)?.let { it to workout } }
            .filter { (date, _) -> !date.isBefore(today.minusDays(89)) && !date.isAfter(today) }

        val ageYears = profile.birthday?.let { calculateAge(it, today) }
        val currentWeight = latestBodyMetric?.weight ?: profile.weight
        val weightKg = currentWeight?.let { convertWeightToKg(it, profile.weightUnit) }
        val heightCm = profile.height?.let { convertHeightToCm(it, profile.heightUnit) }
        val objective = normalizeObjective(profile.primaryObjective)
        val kcal = calculateKcal(profile.gender, ageYears, heightCm, weightKg, objective, recentWorkouts.size)

        val avgWorkoutsPerWeek = recentWorkouts.size / 90f * 7f
        val targetAdherence = if (profile.weeklyTarget > 0) {
            (avgWorkoutsPerWeek / profile.weeklyTarget * 100f).roundToInt()
        } else {
            0
        }
        val favoriteWeekday = recentWorkouts
            .groupingBy { (date, _) -> date.dayOfWeek }
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<java.time.DayOfWeek, Int>> { it.value }.thenByDescending { it.key.value })
            ?.key
            ?.name
        val favoriteTime = recentWorkouts
            .groupingBy { (_, workout) -> timeWindow(workout.startTime) }
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenBy { timeWindowRank(it.key) })
            ?.key

        val durations = recentWorkouts.mapNotNull { (_, workout) -> workout.durationSeconds }.filter { it > 0 }
        val avgDurationMinutes = durations.takeIf { it.isNotEmpty() }?.average()?.div(60.0)?.roundToInt()
        val longestGap = recentWorkouts.map { it.first }.distinct().sorted().zipWithNext()
            .maxOfOrNull { (a, b) -> ChronoUnit.DAYS.between(a, b).toInt() }

        return PersonalDashboardData(
            appTenure = PersonalMetricCardData(
                title = "APP TENURE",
                value = "${daysSince(profile.createdAt, today)} DAYS",
            ),
            age = if (ageYears != null) {
                PersonalMetricCardData("AGE", ageYears.toString())
            } else {
                PersonalMetricCardData("AGE", "ADD BIRTHDAY", isLocked = true)
            },
            currentWeight = if (currentWeight != null) {
                PersonalMetricCardData(
                    title = "CURRENT WEIGHT",
                    value = "${formatDecimal(currentWeight)} ${profile.weightUnit.uppercase()}",
                    detail = if (latestBodyMetric?.weight != null) "LATEST WEIGH-IN" else "PROFILE WEIGHT",
                )
            } else {
                PersonalMetricCardData("CURRENT WEIGHT", "ADD WEIGHT", isLocked = true)
            },
            lastWeighIn = buildLastWeighIn(latestBodyMetric, today),
            objective = if (objective != null) {
                PersonalMetricCardData("OBJECTIVE", objective.displayName)
            } else {
                PersonalMetricCardData("OBJECTIVE", "SET OBJECTIVE", isLocked = true)
            },
            recommendedKcal = kcal?.let {
                val recommended = when (objective) {
                    PersonalObjective.LoseWeight -> it - 400
                    PersonalObjective.GainMuscle -> it + 250
                    PersonalObjective.GetFit, null -> it
                }.coerceAtLeast(1200)
                PersonalMetricCardData(
                    title = "RECOMMENDED KCAL",
                    value = roundKcal(recommended).toString(),
                    detail = if (isNeutralGender(profile.gender)) "DAILY ESTIMATE" else "DAILY TARGET",
                )
            } ?: PersonalMetricCardData(
                "RECOMMENDED KCAL",
                "ADD BIRTHDAY + HEIGHT + WEIGHT TO UNLOCK KCAL",
                isLocked = true,
            ),
            maintenanceKcal = kcal?.let {
                PersonalMetricCardData(
                    title = "MAINTENANCE KCAL",
                    value = roundKcal(it).toString(),
                    detail = if (isNeutralGender(profile.gender)) "TDEE ESTIMATE" else "TDEE",
                )
            } ?: PersonalMetricCardData(
                "MAINTENANCE KCAL",
                "ADD BIRTHDAY + HEIGHT + WEIGHT TO UNLOCK TDEE",
                isLocked = true,
            ),
            averageWorkoutsPerWeek = PersonalMetricCardData(
                "AVG WORKOUTS / WEEK",
                String.format(java.util.Locale.US, "%.1f", avgWorkoutsPerWeek),
                "LAST 90 DAYS",
            ),
            targetAdherence = PersonalMetricCardData(
                "TARGET ADHERENCE",
                "$targetAdherence%",
                "${profile.weeklyTarget} / WEEK TARGET",
            ),
            favoriteWeekday = favoriteWeekday?.let {
                PersonalMetricCardData("FAVORITE WEEKDAY", it, "LAST 90 DAYS")
            } ?: PersonalMetricCardData("FAVORITE WEEKDAY", "NO SIGNAL", isLocked = true),
            favoriteTimeWindow = favoriteTime?.let {
                PersonalMetricCardData("FAVORITE TIME", it, "START TIME")
            } ?: PersonalMetricCardData("FAVORITE TIME", "NO SIGNAL", isLocked = true),
            averageSessionDuration = avgDurationMinutes?.let {
                PersonalMetricCardData("AVG SESSION", "$it MIN", "COMPLETED WORKOUTS")
            } ?: PersonalMetricCardData("AVG SESSION", "NO DURATION", isLocked = true),
            longestRestGap = longestGap?.let {
                PersonalMetricCardData("LONGEST REST GAP", "$it DAYS", "BETWEEN WORKOUTS")
            } ?: PersonalMetricCardData("LONGEST REST GAP", "NO GAP", isLocked = true),
            kcal = KcalInputs(
                ageYears = ageYears,
                heightCm = heightCm,
                weightKg = weightKg,
                isGenderNeutralEstimate = isNeutralGender(profile.gender),
            ),
        )
    }

    private fun calculateKcal(
        gender: String?,
        ageYears: Int?,
        heightCm: Float?,
        weightKg: Float?,
        objective: PersonalObjective?,
        workoutCount90Days: Int,
    ): Int? {
        if (ageYears == null || heightCm == null || weightKg == null || objective == null) return null
        val base = 10f * weightKg + 6.25f * heightCm - 5f * ageYears
        val bmr = when (gender?.trim()?.lowercase()) {
            "male" -> base + 5f
            "female" -> base - 161f
            else -> ((base + 5f) + (base - 161f)) / 2f
        }
        val workoutsPerWeek = workoutCount90Days / 90f * 7f
        val activityFactor = when {
            workoutsPerWeek >= 6f -> 1.725f
            workoutsPerWeek >= 4f -> 1.55f
            workoutsPerWeek >= 2f -> 1.375f
            workoutsPerWeek >= 1f -> 1.2f
            else -> 1.15f
        }
        return (bmr * activityFactor).roundToInt()
    }

    private fun buildLastWeighIn(metric: BodyMetricEntity?, today: LocalDate): PersonalMetricCardData {
        val date = metric?.date?.let(::parseDate)
        return if (date != null) {
            val daysAgo = ChronoUnit.DAYS.between(date, today).coerceAtLeast(0)
            PersonalMetricCardData("LAST WEIGH-IN", metric.date, "$daysAgo DAYS AGO")
        } else {
            PersonalMetricCardData("LAST WEIGH-IN", "LOG WEIGHT", isLocked = true)
        }
    }

    private fun calculateAge(birthday: String, today: LocalDate): Int? {
        val birthDate = parseDate(birthday) ?: return null
        var age = today.year - birthDate.year
        if (today.monthValue < birthDate.monthValue ||
            (today.monthValue == birthDate.monthValue && today.dayOfMonth < birthDate.dayOfMonth)
        ) {
            age--
        }
        return age.coerceAtLeast(0)
    }

    private fun daysSince(epochMillis: Long, today: LocalDate): Long {
        val createdDate = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(createdDate, today).coerceAtLeast(0)
    }

    private fun parseDate(value: String): LocalDate? =
        runCatching { LocalDate.parse(value, dateFormatter) }.getOrNull()

    private fun convertWeightToKg(value: Float, unit: String): Float =
        if (unit.equals("lbs", ignoreCase = true)) value / 2.20462f else value

    private fun convertHeightToCm(value: Float, unit: String): Float =
        if (unit.equals("ft", ignoreCase = true)) value * 30.48f else value

    private fun timeWindow(startTime: Long): String {
        val hour = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).hour
        return when (hour) {
            in 5..10 -> "MORNING"
            in 11..15 -> "MIDDAY"
            in 16..20 -> "EVENING"
            else -> "NIGHT"
        }
    }

    private fun timeWindowRank(window: String): Int =
        when (window) {
            "MORNING" -> 0
            "MIDDAY" -> 1
            "EVENING" -> 2
            else -> 3
        }

    private fun isNeutralGender(gender: String?): Boolean =
        gender.isNullOrBlank() || gender.equals("other", ignoreCase = true)

    private fun roundKcal(value: Int): Int = ((value + 5) / 10) * 10

    private fun formatDecimal(value: Float): String =
        if (value % 1f == 0f) value.roundToInt().toString() else String.format(java.util.Locale.US, "%.1f", value)
}
