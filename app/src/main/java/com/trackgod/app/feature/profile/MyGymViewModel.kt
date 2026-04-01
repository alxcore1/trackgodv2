package com.trackgod.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.SeedDatabase
import com.trackgod.app.core.repository.ExerciseRepository
import com.trackgod.app.core.repository.SettingsRepository
import com.trackgod.app.ui.component.BrandItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyGymState(
    val brands: List<BrandItem> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
)

@HiltViewModel
class MyGymViewModel @Inject constructor(
    private val seedDatabase: SeedDatabase,
    private val settingsRepository: SettingsRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private var dismissJob: Job? = null
    private val _state = MutableStateFlow(MyGymState())
    val state: StateFlow<MyGymState> = _state.asStateFlow()

    init {
        loadBrands()
    }

    private fun loadBrands() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val availableBrands = seedDatabase.getAvailableBrands()
            val selectedBrands = settingsRepository.getSelectedBrands()

            val brandItems = availableBrands.map { (name, count) ->
                BrandItem(
                    name = name,
                    exerciseCount = count,
                    isSelected = selectedBrands.contains(name),
                )
            }

            _state.update { it.copy(brands = brandItems, isLoading = false) }
        }
    }

    fun toggleBrand(name: String) {
        val current = _state.value.brands.find { it.name == name } ?: return
        val wasSelected = current.isSelected

        // Optimistically update UI
        _state.update { state ->
            state.copy(
                brands = state.brands.map { brand ->
                    if (brand.name == name) brand.copy(isSelected = !wasSelected) else brand
                },
            )
        }

        viewModelScope.launch {
            try {
                if (wasSelected) {
                    seedDatabase.removeBrand(name)
                    val count = current.exerciseCount
                    _state.update { it.copy(message = "$count machines hidden. Logged data is preserved.") }
                    autoDismissMessage()
                } else {
                    seedDatabase.addBrand(name)
                    val count = current.exerciseCount
                    _state.update { it.copy(message = "$count machines added.") }
                    autoDismissMessage()
                }
            } catch (e: Exception) {
                // Roll back optimistic update
                _state.update { state ->
                    state.copy(
                        brands = state.brands.map { brand ->
                            if (brand.name == name) brand.copy(isSelected = wasSelected) else brand
                        },
                        message = "FAILED TO UPDATE. PLEASE TRY AGAIN.",
                    )
                }
                autoDismissMessage()
            }
        }
    }

    private fun autoDismissMessage() {
        dismissJob?.cancel()
        dismissJob = viewModelScope.launch {
            delay(3_000L)
            _state.update { it.copy(message = null) }
        }
    }
}
