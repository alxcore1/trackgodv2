package com.trackgod.app.feature.profile

import androidx.lifecycle.ViewModel
import com.trackgod.app.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        _state.value = _state.value.copy(restTimerEnabled = enabled)
    }

    fun setRestTimerDuration(seconds: Int) {
        settingsRepository.setRestTimerDuration(seconds)
        _state.value = _state.value.copy(restTimerDuration = seconds)
    }

    fun setRestTimerAutoStart(enabled: Boolean) {
        settingsRepository.setRestTimerAutoStart(enabled)
        _state.value = _state.value.copy(restTimerAutoStart = enabled)
    }

    fun setShowRpe(enabled: Boolean) {
        settingsRepository.setShowRpe(enabled)
        _state.value = _state.value.copy(showRpe = enabled)
    }

    fun setShowRir(enabled: Boolean) {
        settingsRepository.setShowRir(enabled)
        _state.value = _state.value.copy(showRir = enabled)
    }

    fun setDefaultWeightIncrement(value: Float) {
        settingsRepository.setDefaultWeightIncrement(value)
        _state.value = _state.value.copy(defaultWeightIncrement = value)
    }

    // -- Display --

    fun setWeightUnit(unit: String) {
        settingsRepository.setWeightUnit(unit)
        _state.value = _state.value.copy(weightUnit = unit)
    }

    fun setHeightUnit(unit: String) {
        settingsRepository.setHeightUnit(unit)
        _state.value = _state.value.copy(heightUnit = unit)
    }

    // -- Notifications --

    fun setRestTimerSound(enabled: Boolean) {
        settingsRepository.setRestTimerSoundEnabled(enabled)
        _state.value = _state.value.copy(restTimerSound = enabled)
    }

    fun setWeighInReminder(enabled: Boolean) {
        settingsRepository.setWeighInReminderEnabled(enabled)
        _state.value = _state.value.copy(weighInReminder = enabled)
    }

    fun setReminderDay(day: String) {
        settingsRepository.setReminderDay(day)
        _state.value = _state.value.copy(reminderDay = day)
    }

    fun setReminderTime(time: String) {
        settingsRepository.setReminderTime(time)
        _state.value = _state.value.copy(reminderTime = time)
    }

    // -- Data --

    fun setAutoBackup(enabled: Boolean) {
        settingsRepository.setAutoBackupEnabled(enabled)
        _state.value = _state.value.copy(autoBackup = enabled)
    }

    fun setMaxBackups(count: Int) {
        settingsRepository.setMaxBackups(count)
        _state.value = _state.value.copy(maxBackups = count)
    }
}
