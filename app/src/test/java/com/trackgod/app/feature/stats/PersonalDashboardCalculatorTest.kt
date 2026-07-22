package com.trackgod.app.feature.stats

import com.trackgod.app.core.database.entity.BodyMetricEntity
import com.trackgod.app.core.database.entity.UserProfileEntity
import com.trackgod.app.core.database.entity.WorkoutEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class PersonalDashboardCalculatorTest {

    private val today = LocalDate.of(2026, 4, 27)

    @Test
    fun normalizesObjectivesFromLegacyAndOnboardingFormats() {
        assertEquals(PersonalObjective.LoseWeight, PersonalDashboardCalculator.normalizeObjective("lose_weight"))
        assertEquals(PersonalObjective.LoseWeight, PersonalDashboardCalculator.normalizeObjective("Lose Weight"))
        assertEquals(PersonalObjective.GainMuscle, PersonalDashboardCalculator.normalizeObjective("gain_muscle"))
        assertEquals(PersonalObjective.GainMuscle, PersonalDashboardCalculator.normalizeObjective("Gain Muscle"))
        assertEquals(PersonalObjective.GetFit, PersonalDashboardCalculator.normalizeObjective("get_fit"))
        assertEquals(PersonalObjective.GetFit, PersonalDashboardCalculator.normalizeObjective("Get Fit"))
        assertNull(PersonalDashboardCalculator.normalizeObjective("maintain"))
    }

    @Test
    fun calculatesAgeAndAppTenureFromProfileDates() {
        val data = PersonalDashboardCalculator.build(
            profile = profile(
                birthday = "1990-04-28",
                createdAt = millis(LocalDate.of(2026, 4, 1)),
            ),
            latestBodyMetric = null,
            completedWorkouts = emptyList(),
            today = today,
        )

        assertEquals("26 DAYS", data.appTenure.value)
        assertEquals("35", data.age.value)
        assertFalse(data.age.isLocked)
    }

    @Test
    fun convertsWeightAndHeightUnitsForKcalEstimates() {
        val data = PersonalDashboardCalculator.build(
            profile = profile(
                gender = "male",
                birthday = "1996-04-27",
                height = 6f,
                heightUnit = "ft",
                weight = 180f,
                weightUnit = "lbs",
                primaryObjective = "gain_muscle",
            ),
            latestBodyMetric = null,
            completedWorkouts = listOf(workout(today.minusDays(1)), workout(today.minusDays(3)), workout(today.minusDays(5))),
            today = today,
        )

        assertEquals(30, data.kcal.ageYears)
        assertEquals(182.9f, data.kcal.heightCm!!, 0.1f)
        assertEquals(81.6f, data.kcal.weightKg!!, 0.1f)
        assertEquals("GAIN MUSCLE", data.objective.value)
        assertFalse(data.recommendedKcal.isLocked)
        assertEquals("2340", data.recommendedKcal.value)
        assertEquals("2090", data.maintenanceKcal.value)
    }

    @Test
    fun usesNeutralBmrEstimateWhenGenderIsOtherOrMissing() {
        val data = PersonalDashboardCalculator.build(
            profile = profile(
                gender = "other",
                birthday = "1996-04-27",
                height = 180f,
                weight = 80f,
                primaryObjective = "get_fit",
            ),
            latestBodyMetric = null,
            completedWorkouts = emptyList(),
            today = today,
        )

        assertTrue(data.kcal.isGenderNeutralEstimate)
        assertTrue(data.recommendedKcal.detail.contains("ESTIMATE"))
        assertEquals(data.maintenanceKcal.value, data.recommendedKcal.value)
    }

    @Test
    fun calculatesLastNinetyDayHabitMetrics() {
        val workouts = listOf(
            workout(LocalDate.of(2026, 4, 7), hour = 6, durationSeconds = 3600),
            workout(LocalDate.of(2026, 4, 14), hour = 7, durationSeconds = 5400),
            workout(LocalDate.of(2026, 4, 21), hour = 18, durationSeconds = 1800),
            workout(LocalDate.of(2026, 4, 27), hour = 12, durationSeconds = null),
            workout(LocalDate.of(2025, 12, 31), hour = 20, durationSeconds = 7200),
        )

        val data = PersonalDashboardCalculator.build(
            profile = profile(weeklyTarget = 3),
            latestBodyMetric = null,
            completedWorkouts = workouts,
            today = today,
        )

        assertEquals("0.3", data.averageWorkoutsPerWeek.value)
        assertEquals("10%", data.targetAdherence.value)
        assertEquals("TUESDAY", data.favoriteWeekday.value)
        assertEquals("MORNING", data.favoriteTimeWindow.value)
        assertEquals("60 MIN", data.averageSessionDuration.value)
        assertEquals("7 DAYS", data.longestRestGap.value)
    }

    @Test
    fun showsUnlockStatesWhenProfileDataIsMissing() {
        val data = PersonalDashboardCalculator.build(
            profile = profile(
                gender = null,
                birthday = null,
                height = null,
                weight = null,
                primaryObjective = null,
            ),
            latestBodyMetric = null,
            completedWorkouts = emptyList(),
            today = today,
        )

        assertTrue(data.age.isLocked)
        assertEquals("ADD BIRTHDAY", data.age.value)
        assertTrue(data.currentWeight.isLocked)
        assertEquals("ADD WEIGHT", data.currentWeight.value)
        assertTrue(data.recommendedKcal.isLocked)
        assertEquals("ADD BIRTHDAY + HEIGHT + WEIGHT TO UNLOCK KCAL", data.recommendedKcal.value)
        assertTrue(data.objective.isLocked)
    }

    @Test
    fun prefersLatestBodyMetricForWeightAndLastWeighIn() {
        val metric = BodyMetricEntity(
            id = 1,
            date = "2026-04-20",
            weight = 82.5f,
            createdAt = millis(LocalDate.of(2026, 4, 20)),
        )

        val data = PersonalDashboardCalculator.build(
            profile = profile(weight = 80f, weightUnit = "kg"),
            latestBodyMetric = metric,
            completedWorkouts = emptyList(),
            today = today,
        )

        assertEquals("82.5 KG", data.currentWeight.value)
        assertEquals("2026-04-20", data.lastWeighIn.value)
        assertEquals("7 DAYS AGO", data.lastWeighIn.detail)
    }

    private fun profile(
        gender: String? = "male",
        birthday: String? = "1990-01-01",
        height: Float? = 180f,
        heightUnit: String = "cm",
        weight: Float? = 80f,
        weightUnit: String = "kg",
        primaryObjective: String? = "get_fit",
        weeklyTarget: Int = 4,
        createdAt: Long = millis(LocalDate.of(2026, 1, 1)),
    ) = UserProfileEntity(
        id = 1,
        name = "Tester",
        gender = gender,
        birthday = birthday,
        height = height,
        weight = weight,
        primaryObjective = primaryObjective,
        weeklyTarget = weeklyTarget,
        weightUnit = weightUnit,
        heightUnit = heightUnit,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

    private fun workout(
        date: LocalDate,
        hour: Int = 18,
        durationSeconds: Int? = 3600,
    ) = WorkoutEntity(
        id = date.toEpochDay(),
        name = "Workout",
        date = date.toString(),
        startTime = date.atTime(hour, 0).toInstant(ZoneOffset.UTC).toEpochMilli(),
        durationSeconds = durationSeconds,
        totalVolume = 1000f,
        isCompleted = true,
        createdAt = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
    )

    private fun millis(date: LocalDate): Long =
        date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
}
