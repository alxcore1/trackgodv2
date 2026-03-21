package com.trackgod.app.feature.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.SeedDatabase
import com.trackgod.app.core.repository.UserRepository
import com.trackgod.app.service.DatabaseIntegrityManager
import com.trackgod.app.service.IntegrityResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val seedDatabase: SeedDatabase,
    private val integrityManager: DatabaseIntegrityManager,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _hasProfile = MutableStateFlow(false)
    val hasProfile: StateFlow<Boolean> = _hasProfile.asStateFlow()

    init {
        viewModelScope.launch {
            // Integrity check before seeding
            when (val result = integrityManager.performStartupCheck()) {
                is IntegrityResult.Healthy -> {
                    Log.d("SplashViewModel", "Database integrity check: HEALTHY")
                }
                is IntegrityResult.Recovered -> {
                    Log.w("SplashViewModel", "Database recovered from backup: ${result.fromBackup}")
                }
                is IntegrityResult.FreshStart -> {
                    Log.w("SplashViewModel", "Database fresh start -- no backup available")
                }
                is IntegrityResult.Error -> {
                    Log.e("SplashViewModel", "Database integrity error: ${result.message}")
                }
            }

            // Check if user has completed onboarding
            val profileExists = userRepository.hasProfile()
            _hasProfile.value = profileExists

            // Only auto-seed if profile already exists (returning user)
            // New users will pick their seed option in onboarding
            if (profileExists) {
                seedDatabase.seedIfNeeded()
            }

            _isReady.value = true
        }
    }
}
