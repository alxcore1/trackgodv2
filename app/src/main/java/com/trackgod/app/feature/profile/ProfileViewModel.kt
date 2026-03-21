package com.trackgod.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.UserProfileEntity
import com.trackgod.app.core.repository.UserRepository
import com.trackgod.app.core.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ProfileState(
    val profile: UserProfileEntity? = null,
    val totalWorkouts: Int = 0,
    val memberSince: String = "",
    val isLoading: Boolean = true,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
        loadWorkoutCount()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userRepository.getProfile().collect { profile ->
                _state.value = _state.value.copy(
                    profile = profile,
                    memberSince = profile?.let { formatMemberSince(it.createdAt) } ?: "",
                    isLoading = false,
                )
            }
        }
    }

    private fun loadWorkoutCount() {
        viewModelScope.launch {
            val count = workoutRepository.getCompletedWorkoutCount()
            _state.value = _state.value.copy(totalWorkouts = count)
        }
    }

    private fun formatMemberSince(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}
