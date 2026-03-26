package com.trackgod.app.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.service.BackupScheduler
import com.trackgod.app.service.WeighInReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsState(
    // Workout
    val restTimerEnabled: Boolean = true,
    val restTimerDuration: Int = 90,
    val restTimerAutoStart: Boolean = true,
    val showRpe: Boolean = false,
    val showRir: Boolean = false,
    val defaultWeightIncrement: Float = 2.5f,
    // Display
    val weightUnit: String = "kg",
    val heightUnit: String = "cm",
    // Notifications
    val restTimerSound: Boolean = true,
    val weighInReminder: Boolean = false,
    val reminderDay: String = "Sunday",
    val reminderTime: String = "08:00",
    // Data
    val autoBackup: Boolean = true,
    val maxBackups: Int = 10,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: com.trackgod.app.core.repository.UserRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _state.value = SettingsState(
            restTimerEnabled = settingsRepository.isRestTimerEnabled(),
            restTimerDuration = settingsRepository.getRestTimerDuration(),
            restTimerAutoStart = settingsRepository.isRestTimerAutoStart(),
            showRpe = settingsRepository.showRpe(),
            showRir = settingsRepository.showRir(),
            defaultWeightIncrement = settingsRepository.getDefaultWeightIncrement(),
            weightUnit = settingsRepository.getWeightUnit(),
            heightUnit = settingsRepository.getHeightUnit(),
            restTimerSound = settingsRepository.isRestTimerSoundEnabled(),
            weighInReminder = settingsRepository.isWeighInReminderEnabled(),
            reminderDay = settingsRepository.getReminderDay(),
            reminderTime = settingsRepository.getReminderTime(),
            autoBackup = settingsRepository.isAutoBackupEnabled(),
            maxBackups = settingsRepository.getMaxBackups(),
        )
    }

    // -- Workout --

    fun setRestTimerEnabled(enabled: Boolean) {
        settingsRepository.setRestTimerEnabled(enabled)
        _state.update { it.copy(restTimerEnabled = enabled) }
    }

    fun setRestTimerDuration(seconds: Int) {
        settingsRepository.setRestTimerDuration(seconds)
        _state.update { it.copy(restTimerDuration = seconds) }
    }

    fun setRestTimerAutoStart(enabled: Boolean) {
        settingsRepository.setRestTimerAutoStart(enabled)
        _state.update { it.copy(restTimerAutoStart = enabled) }
    }

    fun setShowRpe(enabled: Boolean) {
        settingsRepository.setShowRpe(enabled)
        _state.update { it.copy(showRpe = enabled) }
    }

    fun setShowRir(enabled: Boolean) {
        settingsRepository.setShowRir(enabled)
        _state.update { it.copy(showRir = enabled) }
    }

    fun setDefaultWeightIncrement(value: Float) {
        settingsRepository.setDefaultWeightIncrement(value)
        _state.update { it.copy(defaultWeightIncrement = value) }
    }

    // -- Display --

    fun setWeightUnit(unit: String) {
        settingsRepository.setWeightUnit(unit)
        _state.update { it.copy(weightUnit = unit) }
        // Sync to profile entity so both sources agree
        viewModelScope.launch { userRepository.updateWeightUnit(unit) }
    }

    fun setHeightUnit(unit: String) {
        settingsRepository.setHeightUnit(unit)
        _state.update { it.copy(heightUnit = unit) }
        viewModelScope.launch { userRepository.updateHeightUnit(unit) }
    }

    // -- Notifications --

    fun setRestTimerSound(enabled: Boolean) {
        settingsRepository.setRestTimerSoundEnabled(enabled)
        _state.update { it.copy(restTimerSound = enabled) }
    }

    fun setWeighInReminder(enabled: Boolean) {
        settingsRepository.setWeighInReminderEnabled(enabled)
        _state.update { it.copy(weighInReminder = enabled) }
        if (enabled) {
            scheduleWeighInReminder()
        } else {
            WeighInReminderScheduler.cancel(appContext)
        }
    }

    fun setReminderDay(day: String) {
        settingsRepository.setReminderDay(day)
        _state.update { it.copy(reminderDay = day) }
        if (_state.value.weighInReminder) {
            scheduleWeighInReminder()
        }
    }

    fun setReminderTime(time: String) {
        settingsRepository.setReminderTime(time)
        _state.update { it.copy(reminderTime = time) }
        if (_state.value.weighInReminder) {
            scheduleWeighInReminder()
        }
    }

    private fun scheduleWeighInReminder() {
        val day = _state.value.reminderDay
        val timeParts = _state.value.reminderTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        WeighInReminderScheduler.schedule(appContext, day, hour, minute)
    }

    // -- Data --

    fun setAutoBackup(enabled: Boolean) {
        settingsRepository.setAutoBackupEnabled(enabled)
        _state.update { it.copy(autoBackup = enabled) }
        if (enabled) {
            BackupScheduler.scheduleDaily(appContext)
        } else {
            BackupScheduler.cancel(appContext)
        }
    }

    fun setMaxBackups(count: Int) {
        settingsRepository.setMaxBackups(count)
        _state.update { it.copy(maxBackups = count) }
    }
}
