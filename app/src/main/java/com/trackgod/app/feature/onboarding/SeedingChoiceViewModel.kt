package com.trackgod.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.SeedDatabase
import com.trackgod.app.ui.component.BrandItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeedingChoiceViewModel @Inject constructor(
    private val seedDatabase: SeedDatabase,
) : ViewModel() {

    private val _isSeeding = MutableStateFlow(false)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    private val _showBrandPicker = MutableStateFlow(false)
    val showBrandPicker: StateFlow<Boolean> = _showBrandPicker.asStateFlow()

    private val _availableBrands = MutableStateFlow<List<BrandItem>>(emptyList())
    val availableBrands: StateFlow<List<BrandItem>> = _availableBrands.asStateFlow()

    private val _selectedBrands = MutableStateFlow<Set<String>>(emptySet())
    val selectedBrands: StateFlow<Set<String>> = _selectedBrands.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private fun showErrorAndAutoDismiss(message: String) {
        _errorMessage.value = message
        viewModelScope.launch {
            delay(4_000L)
            _errorMessage.value = null
        }
    }

    fun showBrandSelection() {
        _showBrandPicker.value = true
        viewModelScope.launch {
            try {
                val brands = seedDatabase.getAvailableBrands()
                _availableBrands.value = brands.map { (name, count) ->
                    BrandItem(name = name, exerciseCount = count, isSelected = false)
                }
                _selectedBrands.value = emptySet()
            } catch (_: Exception) {
                _showBrandPicker.value = false
                showErrorAndAutoDismiss("SETUP FAILED. PLEASE TRY AGAIN.")
            }
        }
    }

    fun goBackToStep1() {
        _showBrandPicker.value = false
    }

    fun selectAllBrands() {
        val allNames = _availableBrands.value.map { it.name }.toSet()
        _selectedBrands.value = allNames
        _availableBrands.value = _availableBrands.value.map { it.copy(isSelected = true) }
    }

    fun deselectAllBrands() {
        _selectedBrands.value = emptySet()
        _availableBrands.value = _availableBrands.value.map { it.copy(isSelected = false) }
    }

    fun toggleBrand(name: String) {
        val current = _selectedBrands.value
        val updated = if (name in current) current - name else current + name
        _selectedBrands.value = updated
        _availableBrands.value = _availableBrands.value.map { brand ->
            brand.copy(isSelected = brand.name in updated)
        }
    }

    fun seedWithSelectedBrands(onComplete: () -> Unit) {
        if (_isSeeding.value) return
        _isSeeding.value = true
        viewModelScope.launch {
            try {
                seedDatabase.seedWithBrands(_selectedBrands.value)
                onComplete()
            } catch (_: Exception) {
                _isSeeding.value = false
                showErrorAndAutoDismiss("SETUP FAILED. PLEASE TRY AGAIN.")
            }
        }
    }

    fun skipBrandSelection(onComplete: () -> Unit) {
        if (_isSeeding.value) return
        _isSeeding.value = true
        viewModelScope.launch {
            try {
                seedDatabase.seedIfNeeded()
                onComplete()
            } catch (_: Exception) {
                _isSeeding.value = false
                showErrorAndAutoDismiss("SETUP FAILED. PLEASE TRY AGAIN.")
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
                showErrorAndAutoDismiss("SETUP FAILED. PLEASE TRY AGAIN.")
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
                showErrorAndAutoDismiss("SETUP FAILED. PLEASE TRY AGAIN.")
            }
        }
    }
}
