package com.trackgod.app.feature.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import com.trackgod.app.core.database.dao.ExerciseProgressPoint
import com.trackgod.app.ui.theme.TrackGodTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class StatsScreenChartsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun statsChartsExposeSummariesAndInlineDetails() {
        composeRule.setContent {
            TrackGodTheme {
                StatsContent(
                    state = seededStatsState(),
                    onTimeRangeChanged = {},
                )
            }
        }

        composeRule.onNodeWithText("PROGRESS").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("WORKOUTS / WEEK", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("BALANCE").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("BEST WEEK", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("REST").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("HIGH").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("stats-volume-bar-W2").performScrollTo().performClick()
        composeRule.onNodeWithText("W2 DETAIL").assertIsDisplayed()

        val firstHeatmapDate = LocalDate.now().minusDays(13)
        composeRule.onNodeWithTag("stats-heatmap-day-$firstHeatmapDate").performScrollTo().performTouchInput { click() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("$firstHeatmapDate DETAIL").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("stats-progression-card-bench-press").performScrollTo().performClick()
        composeRule.onNodeWithText("FIRST 1RM", substring = true).performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("stats-balance-row-upper").performScrollTo().performClick()
        composeRule.onNodeWithText("UPPER DETAIL").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("stats-muscle-row-chest").performScrollTo().performClick()
        composeRule.onNodeWithText("CHEST DETAIL").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("stats-frequency-row-bench-press").performScrollTo().performClick()
        composeRule.onNodeWithText("RELATIVE FREQUENCY", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun statsShowsSelectedRangeEmptyMessageWhenHistoricalDataExistsOutsideRange() {
        composeRule.setContent {
            TrackGodTheme {
                StatsContent(
                    state = seededStatsState().copy(
                        selectedTimeRange = TimeRange.MONTH,
                        hasData = true,
                        hasWorkoutsInSelectedRange = false,
                        rangeEmptyMessage = "NO WORKOUTS IN LAST 30 DAYS",
                        totalWorkouts = 0,
                        volumeByPeriod = emptyList(),
                        strengthBalance = emptyList(),
                        muscleGroupVolumes = emptyList(),
                        exerciseFrequency = emptyList(),
                    ),
                    onTimeRangeChanged = {},
                )
            }
        }

        composeRule.onNodeWithText("NO WORKOUTS IN LAST 30 DAYS").assertIsDisplayed()
    }

    @Test
    fun exerciseFrequencyUsesShortDisplayNameAndFullDetailName() {
        composeRule.setContent {
            TrackGodTheme {
                StatsContent(
                    state = seededStatsState().copy(
                        exerciseFrequency = listOf(
                            ExerciseFrequencyData(
                                exerciseName = "Chest Press",
                                count = 8,
                                maxCount = 8,
                                fullExerciseName = "Hammer Strength Chest Press",
                            ),
                        ),
                    ),
                    onTimeRangeChanged = {},
                )
            }
        }

        composeRule.onNodeWithTag("stats-frequency-row-chest-press").performScrollTo().performClick()
        composeRule.onNodeWithText("HAMMER STRENGTH CHEST PRESS DETAIL").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun statsModeSelectorSwitchesBetweenPerformanceAndPersonal() {
        var selectedMode by mutableStateOf(StatsMode.Performance)

        composeRule.setContent {
            TrackGodTheme {
                StatsContent(
                    state = seededStatsState().copy(selectedStatsMode = selectedMode),
                    onStatsModeChanged = { selectedMode = it },
                    onTimeRangeChanged = {},
                )
            }
        }

        composeRule.onNodeWithText("PERFORMANCE").assertIsDisplayed()
        composeRule.onNodeWithText("PERSONAL").assertIsDisplayed()
        composeRule.onNodeWithText("MO").assertIsDisplayed()

        composeRule.onNodeWithText("PERSONAL").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("PERSONAL DASHBOARD").assertIsDisplayed()
        composeRule.onAllNodesWithText("CURRENT WEIGHT").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("TARGET ADHERENCE").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("RECOMMENDED KCAL").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("MO").assertCountEquals(0)
    }

    @Test
    fun personalDashboardShowsExpectedCardsAndUnlockStates() {
        composeRule.setContent {
            TrackGodTheme {
                StatsContent(
                    state = seededStatsState().copy(
                        selectedStatsMode = StatsMode.Personal,
                        personalDashboard = PersonalDashboardData(
                            appTenure = PersonalMetricCardData("APP TENURE", "116 DAYS"),
                            age = PersonalMetricCardData("AGE", "39"),
                            currentWeight = PersonalMetricCardData("CURRENT WEIGHT", "82.5 KG"),
                            lastWeighIn = PersonalMetricCardData("LAST WEIGH-IN", "2026-04-20", "7 DAYS AGO"),
                            objective = PersonalMetricCardData("OBJECTIVE", "GAIN MUSCLE"),
                            recommendedKcal = PersonalMetricCardData(
                                "RECOMMENDED KCAL",
                                "2620",
                                "DAILY TARGET",
                            ),
                            maintenanceKcal = PersonalMetricCardData("MAINTENANCE KCAL", "2820", "TDEE ESTIMATE"),
                            averageWorkoutsPerWeek = PersonalMetricCardData("AVG WORKOUTS / WEEK", "3.4"),
                            targetAdherence = PersonalMetricCardData("TARGET ADHERENCE", "85%"),
                            favoriteWeekday = PersonalMetricCardData("FAVORITE WEEKDAY", "MONDAY"),
                            favoriteTimeWindow = PersonalMetricCardData("FAVORITE TIME", "EVENING"),
                            averageSessionDuration = PersonalMetricCardData("AVG SESSION", "64 MIN"),
                            longestRestGap = PersonalMetricCardData("LONGEST REST GAP", "9 DAYS"),
                        ),
                    ),
                )
            }
        }

        listOf(
            "BODY",
            "NUTRITION",
            "TRAINING RHYTHM",
            "APP TENURE",
            "AGE",
            "CURRENT WEIGHT",
            "LAST WEIGH-IN",
            "OBJECTIVE",
            "RECOMMENDED KCAL",
            "MAINTENANCE KCAL",
            "AVG WORKOUTS / WEEK",
            "TARGET ADHERENCE",
            "FAVORITE WEEKDAY",
            "FAVORITE TIME",
            "SESSION / REST",
        ).forEach { label ->
            composeRule.onAllNodesWithText(label).onFirst().performScrollTo().assertIsDisplayed()
        }
        composeRule.onAllNodesWithText("39 YRS").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("2620 KCAL").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("2820 KCAL").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("7 DAYS AGO").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("APR 20, 2026").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("REST GAP: 9 DAYS").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun personalDashboardShowsInlineUnlockPrompts() {
        composeRule.setContent {
            TrackGodTheme {
                StatsContent(
                    state = seededStatsState().copy(
                        selectedStatsMode = StatsMode.Personal,
                        personalDashboard = PersonalDashboardData(
                            age = PersonalMetricCardData("AGE", "ADD BIRTHDAY", isLocked = true),
                            recommendedKcal = PersonalMetricCardData(
                                "RECOMMENDED KCAL",
                                "ADD BIRTHDAY + HEIGHT + WEIGHT TO UNLOCK KCAL",
                                isLocked = true,
                            ),
                            maintenanceKcal = PersonalMetricCardData(
                                "MAINTENANCE KCAL",
                                "ADD BIRTHDAY + HEIGHT + WEIGHT TO UNLOCK TDEE",
                                isLocked = true,
                            ),
                        ),
                    ),
                )
            }
        }

        composeRule.onAllNodesWithText("ADD BIRTHDAY TO UNLOCK AGE").onFirst().performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("ADD BIRTHDAY + HEIGHT + WEIGHT TO UNLOCK KCAL").onFirst().performScrollTo().assertIsDisplayed()
    }

    private fun seededStatsState(): StatsState {
        val today = LocalDate.now()
        return StatsState(
            isLoading = false,
            hasData = true,
            hasWorkoutsInSelectedRange = true,
            selectedTimeRange = TimeRange.MONTH,
            weightUnit = "kg",
            totalVolume = 18_000f,
            volumeByPeriod = listOf(
                VolumeDataPoint("W1", 6_000f, today.minusDays(14).toString()),
                VolumeDataPoint("W2", 12_000f, today.minusDays(7).toString()),
            ),
            heatmapData = (0..13).map { offset ->
                val date = today.minusDays((13 - offset).toLong())
                HeatmapDay(date, if (offset == 13) 7_500f else 0f, if (offset == 13) 3 else 0)
            },
            personalRecords = listOf(
                PersonalRecordData("Bench Press", 120f, 100f, 6),
            ),
            exerciseProgressions = listOf(
                ExerciseProgressionData(
                    exerciseName = "Bench Press",
                    category = "Chest",
                    history = listOf(
                        ExerciseProgressPoint(today.minusDays(21).toString(), 90f, 100f, 2_000f, 3),
                        ExerciseProgressPoint(today.toString(), 105f, 120f, 3_000f, 4),
                    ),
                    current1rm = 120f,
                    progressionRate = 20f,
                ),
            ),
            currentStreak = 2,
            longestStreak = 5,
            workoutsPerWeek = listOf(
                WeeklyConsistencyData("13/4", 2),
                WeeklyConsistencyData("20/4", 3),
            ),
            strengthBalance = listOf(
                StrengthBalanceData("Upper", 12_000f, 66.6f),
                StrengthBalanceData("Lower", 6_000f, 33.3f),
            ),
            muscleGroupVolumes = listOf(
                MuscleGroupData("Chest", 12_000f, 66.6f),
                MuscleGroupData("Legs", 6_000f, 33.3f),
            ),
            exerciseFrequency = listOf(
                ExerciseFrequencyData("Bench Press", 8, 8),
                ExerciseFrequencyData("Squat", 4, 8),
            ),
            totalWorkouts = 5,
        )
    }
}
