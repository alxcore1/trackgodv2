package com.trackgod.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.UserProfileEntity
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.core.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 0,
    val totalSteps: Int = 6,
    // Form data
    val name: String = "",
    val avatarUri: String? = null,
    val gender: String? = null,
    val birthday: String? = null,
    val height: String = "175",
    val weight: String = "80",
    val weightUnit: String = "kg",
    val heightUnit: String = "cm",
    val primaryObjective: String? = null,
    val experienceLevel: String = "intermediate",
    val weeklyTarget: Int = 4,
    // UI
    val canProceed: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        updateCanProceed()
    }

    // ── Field updaters ──────────────────────────────────────────────────────

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
        updateCanProceed()
    }

    fun updateAvatarUri(uri: String?) {
        _state.update { it.copy(avatarUri = uri) }
    }

    fun updateGender(gender: String) {
        _state.update { it.copy(gender = gender) }
        updateCanProceed()
    }

    fun updateHeight(height: String) {
        _state.update { it.copy(height = height) }
        updateCanProceed()
    }

    fun updateWeight(weight: String) {
        _state.update { it.copy(weight = weight) }
        updateCanProceed()
    }

    fun updateWeightUnit(unit: String) {
        _state.update { it.copy(weightUnit = unit) }
    }

    fun updateHeightUnit(unit: String) {
        _state.update { it.copy(heightUnit = unit) }
    }

    fun updatePrimaryObjective(objective: String) {
        _state.update { it.copy(primaryObjective = objective) }
        updateCanProceed()
    }

    fun updateExperienceLevel(level: String) {
        _state.update { it.copy(experienceLevel = level) }
        updateCanProceed()
    }

    fun updateWeeklyTarget(target: Int) {
        _state.update { it.copy(weeklyTarget = target.coerceIn(1, 7)) }
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    fun nextStep() {
        val s = _state.value
        if (!s.canProceed || s.currentStep >= s.totalSteps - 1) return
        _state.update { it.copy(currentStep = it.currentStep + 1) }
        updateCanProceed()
    }

    fun previousStep() {
        if (_state.value.currentStep <= 0) return
        _state.update { it.copy(currentStep = it.currentStep - 1) }
        updateCanProceed()
    }

    // ── Save profile ────────────────────────────────────────────────────────

    fun saveProfile(onComplete: () -> Unit) {
        val s = _state.value
        if (s.isSaving) return

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val entity = UserProfileEntity(
                    name = s.name.trim(),
                    avatarUri = s.avatarUri,
                    gender = s.gender,
                    birthday = s.birthday,
                    height = s.height.toFloatOrNull(),
                    weight = s.weight.toFloatOrNull(),
                    primaryObjective = s.primaryObjective,
                    experienceLevel = s.experienceLevel,
                    weeklyTarget = s.weeklyTarget,
                    weightUnit = s.weightUnit,
                    heightUnit = s.heightUnit,
                    createdAt = now,
                    updatedAt = now,
                )
                userRepository.createProfile(entity)

                // Sync unit preferences to settings
                settingsRepository.setWeightUnit(s.weightUnit)
                settingsRepository.setHeightUnit(s.heightUnit)

                _state.update { it.copy(isSaving = false) }
                onComplete()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save profile",
                    )
                }
            }
        }
    }

    // ── Validation ──────────────────────────────────────────────────────────

    private fun updateCanProceed() {
        val s = _state.value
        val canProceed = when (s.currentStep) {
            0 -> s.name.isNotBlank()
            1 -> s.gender != null
            2 -> {
                val h = s.height.toFloatOrNull()
                val w = s.weight.toFloatOrNull()
                h != null && h > 0 && w != null && w > 0
            }
            3 -> true // Units step -- always valid (has defaults)
            4 -> s.primaryObjective != null
            5 -> s.experienceLevel.isNotBlank() && s.weeklyTarget in 1..7
            else -> false
        }
        _state.update { it.copy(canProceed = canProceed) }
    }
}
