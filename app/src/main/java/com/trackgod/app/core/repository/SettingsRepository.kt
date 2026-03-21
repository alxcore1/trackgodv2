package com.trackgod.app.core.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("trackgod_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_ACTIVE_WORKOUT_ID = "active_workout_id"
        private const val KEY_REST_TIMER_DURATION = "rest_timer_duration"
        private const val KEY_REST_TIMER_AUTO_START = "rest_timer_auto_start"
        private const val KEY_REST_TIMER_ENABLED = "rest_timer_enabled"
        private const val KEY_SHOW_RPE = "show_rpe"
        private const val KEY_SHOW_RIR = "show_rir"
        private const val KEY_DEFAULT_WEIGHT_INCREMENT = "default_weight_increment"
        private const val KEY_WEIGHT_UNIT = "weight_unit"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_DATABASE_SEEDED = "database_seeded"
        private const val NO_ACTIVE_WORKOUT = -1L
    }

    // --- Active Workout ---

    fun getActiveWorkoutId(): Long? {
        val id = prefs.getLong(KEY_ACTIVE_WORKOUT_ID, NO_ACTIVE_WORKOUT)
        return if (id == NO_ACTIVE_WORKOUT) null else id
    }

    fun setActiveWorkoutId(id: Long) {
        prefs.edit().putLong(KEY_ACTIVE_WORKOUT_ID, id).apply()
    }

    fun clearActiveWorkoutId() {
        prefs.edit().remove(KEY_ACTIVE_WORKOUT_ID).apply()
    }

    // --- Rest Timer ---

    fun getRestTimerDuration(): Int =
        prefs.getInt(KEY_REST_TIMER_DURATION, 90)

    fun setRestTimerDuration(seconds: Int) {
        prefs.edit().putInt(KEY_REST_TIMER_DURATION, seconds).apply()
    }

    fun isRestTimerAutoStart(): Boolean =
        prefs.getBoolean(KEY_REST_TIMER_AUTO_START, true)

    fun setRestTimerAutoStart(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REST_TIMER_AUTO_START, enabled).apply()
    }

    fun isRestTimerEnabled(): Boolean =
        prefs.getBoolean(KEY_REST_TIMER_ENABLED, true)

    fun setRestTimerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REST_TIMER_ENABLED, enabled).apply()
    }

    // --- Display Preferences ---

    fun showRpe(): Boolean =
        prefs.getBoolean(KEY_SHOW_RPE, false)

    fun setShowRpe(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_RPE, enabled).apply()
    }

    fun showRir(): Boolean =
        prefs.getBoolean(KEY_SHOW_RIR, false)

    fun setShowRir(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_RIR, enabled).apply()
    }

    // --- Weight Settings ---

    fun getDefaultWeightIncrement(): Float =
        prefs.getFloat(KEY_DEFAULT_WEIGHT_INCREMENT, 2.5f)

    fun setDefaultWeightIncrement(value: Float) {
        prefs.edit().putFloat(KEY_DEFAULT_WEIGHT_INCREMENT, value).apply()
    }

    fun getWeightUnit(): String =
        prefs.getString(KEY_WEIGHT_UNIT, "kg") ?: "kg"

    fun setWeightUnit(unit: String) {
        prefs.edit().putString(KEY_WEIGHT_UNIT, unit).apply()
    }

    // --- First Launch / Seeding ---

    fun isFirstLaunch(): Boolean =
        prefs.getBoolean(KEY_FIRST_LAUNCH, true)

    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun isDatabaseSeeded(): Boolean =
        prefs.getBoolean(KEY_DATABASE_SEEDED, false)

    fun setDatabaseSeeded() {
        prefs.edit().putBoolean(KEY_DATABASE_SEEDED, true).apply()
    }
}
