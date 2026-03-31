package com.trackgod.app.core.util

import java.time.LocalDate

/**
 * Calculate current streak: consecutive days backward from today
 * that have at least one completed workout.
 *
 * If there is no workout today, the streak starts from yesterday
 * (the streak isn't considered broken until a full day is missed).
 */
fun calculateStreak(workoutDates: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
    if (workoutDates.isEmpty()) return 0

    var day = if (today in workoutDates) today else today.minusDays(1)
    var streak = 0
    while (day in workoutDates) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}
