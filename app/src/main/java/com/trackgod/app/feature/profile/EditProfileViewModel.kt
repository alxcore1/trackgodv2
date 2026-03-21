package com.trackgod.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.UserProfileEntity
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.core.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val name: String = "",
    val avatarUri: String = "",
    val gender: String = "",
    val birthday: String = "",
    val height: String = "",
    val weight: String = "",
    val primaryObjective: String = "",
    val experienceLevel: String = "Intermediate",
    val weeklyTarget: String = "4",
    val weightUnit: String = "kg",
    val heightUnit: String = "cm",
    val nameError: Boolean = false,
    val isLoading: Boolean = true,
    val isNewProfile: Boolean = true,
    val existingId: Long = 0,
    val existingCreatedAt: Long = 0,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    init {
        loadExistingProfile()
    }

    private fun loadExistingProfile() {
        viewModelScope.launch {
            val profile = userRepository.getProfileOnce()
            if (profile != null) {
                _state.value = _state.value.copy(
                    name = profile.name,
                    avatarUri = profile.avatarUri ?: "",
                    gender = profile.gender ?: "",
                    birthday = profile.birthday ?: "",
                    height = profile.height?.toString() ?: "",
                    weight = profile.weight?.toString() ?: "",
                    primaryObjective = profile.primaryObjective ?: "",
                    experienceLevel = profile.experienceLevel,
                    weeklyTarget = profile.weeklyTarget.toString(),
                    weightUnit = profile.weightUnit,
                    heightUnit = profile.heightUnit,
                    isLoading = false,
                    isNewProfile = false,
                    existingId = profile.id,
                    existingCreatedAt = profile.createdAt,
                )
            } else {
                _state.value = _state.value.copy(isLoading = false, isNewProfile = true)
            }
        }
    }

    fun onNameChanged(value: String) {
        _state.value = _state.value.copy(name = value, nameError = false)
    }

    fun onAvatarUriChanged(uri: String) {
        _state.value = _state.value.copy(avatarUri = uri)
    }

    fun onGenderChanged(value: String) {
        _state.value = _state.value.copy(gender = value)
    }

    fun onBirthdayChanged(value: String) {
        _state.value = _state.value.copy(birthday = value)
    }

    fun onHeightChanged(value: String) {
        _state.value = _state.value.copy(height = value)
    }

    fun onWeightChanged(value: String) {
        _state.value = _state.value.copy(weight = value)
    }

    fun onPrimaryObjectiveChanged(value: String) {
        _state.value = _state.value.copy(primaryObjective = value)
    }

    fun onExperienceLevelChanged(value: String) {
        _state.value = _state.value.copy(experienceLevel = value)
    }

    fun onWeeklyTargetChanged(value: String) {
        _state.value = _state.value.copy(weeklyTarget = value)
    }

    fun onWeightUnitChanged(value: String) {
        _state.value = _state.value.copy(weightUnit = value)
    }

    fun onHeightUnitChanged(value: String) {
        _state.value = _state.value.copy(heightUnit = value)
    }

    fun save(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.value = current.copy(nameError = true)
            return
        }

        val now = System.currentTimeMillis()
        val entity = UserProfileEntity(
            id = if (current.isNewProfile) 0 else current.existingId,
            name = current.name.trim(),
            avatarUri = current.avatarUri.ifBlank { null },
            gender = current.gender.ifBlank { null },
            birthday = current.birthday.ifBlank { null },
            height = current.height.toFloatOrNull(),
            weight = current.weight.toFloatOrNull(),
            primaryObjective = current.primaryObjective.ifBlank { null },
            experienceLevel = current.experienceLevel.ifBlank { "intermediate" },
            weeklyTarget = current.weeklyTarget.toIntOrNull()?.coerceIn(1, 7) ?: 4,
            weightUnit = current.weightUnit,
            heightUnit = current.heightUnit,
            createdAt = if (current.isNewProfile) now else current.existingCreatedAt,
            updatedAt = now,
        )

        viewModelScope.launch {
            if (current.isNewProfile) {
                userRepository.createProfile(entity)
            } else {
                userRepository.updateProfile(entity)
            }
            // Sync units to app-wide settings so other screens pick them up
            settingsRepository.setWeightUnit(current.weightUnit)
            settingsRepository.setHeightUnit(current.heightUnit)
            onSuccess()
        }
    }
}
