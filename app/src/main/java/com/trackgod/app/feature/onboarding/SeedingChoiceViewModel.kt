package com.trackgod.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.SeedDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeedingChoiceViewModel @Inject constructor(
    val seedDatabase: SeedDatabase,
) : ViewModel() {

    private val _isSeeding = MutableStateFlow(false)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    fun seedFull(onComplete: () -> Unit) {
        if (_isSeeding.value) return
        _isSeeding.value = true
        viewModelScope.launch {
            try {
                seedDatabase.seedIfNeeded()
                onComplete()
            } catch (_: Exception) {
                _isSeeding.value = false
            }
        }
    }

    fun seedBasics(onComplete: () -> Unit) {
        if (_isSeeding.value) return
        _isSeeding.value = true
        viewModelScope.launch {
            try {
                seedDatabase.seedBasicsOnly()
                onComplete()
            } catch (_: Exception) {
                _isSeeding.value = false
            }
        }
    }

    fun seedEmpty(onComplete: () -> Unit) {
        if (_isSeeding.value) return
        _isSeeding.value = true
        viewModelScope.launch {
            try {
                seedDatabase.markAsSeeded()
                onComplete()
            } catch (_: Exception) {
                _isSeeding.value = false
            }
        }
    }
}
