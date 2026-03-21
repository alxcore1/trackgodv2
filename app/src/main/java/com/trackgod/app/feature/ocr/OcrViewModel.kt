package com.trackgod.app.feature.ocr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── State ───────────────────────────────────────────────────────────────────

data class OcrState(
    val isScanning: Boolean = true,
    val result: OcrResult? = null,
    val selectedMatch: OcrMatch? = null,
    val showManualEntry: Boolean = false,
    val manualName: String = "",
    val manualCategory: String = "",
    val manualEquipment: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val confirmedExerciseId: Long? = null,
)

// ── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val ocrProcessor: OcrProcessor,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OcrState())
    val state: StateFlow<OcrState> = _state.asStateFlow()

    /**
     * Run OCR on the captured [bitmap] and update state with results.
     */
    fun processCapture(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, error = null) }

            try {
                val result = ocrProcessor.processImage(bitmap)
                _state.update {
                    it.copy(
                        isScanning = false,
                        result = result,
                        selectedMatch = result.matches.firstOrNull(),
                        isProcessing = false,
                        showManualEntry = false,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message ?: "OCR processing failed",
                    )
                }
            }
        }
    }

    /** Select a match from the results list. */
    fun selectMatch(match: OcrMatch) {
        _state.update { it.copy(selectedMatch = match) }
    }

    /**
     * Confirm the currently selected match.
     * Sets [OcrState.confirmedExerciseId] which the screen observes
     * to pop back with the result.
     */
    fun confirmSelection() {
        val exerciseId = _state.value.selectedMatch?.exercise?.id ?: return
        _state.update { it.copy(confirmedExerciseId = exerciseId) }
    }

    /** Toggle manual entry visibility. */
    fun showManualEntry() {
        _state.update { it.copy(showManualEntry = true) }
    }

    fun updateManualName(name: String) {
        _state.update { it.copy(manualName = name) }
    }

    fun updateManualCategory(category: String) {
        _state.update { it.copy(manualCategory = category) }
    }

    fun updateManualEquipment(equipment: String) {
        _state.update { it.copy(manualEquipment = equipment) }
    }

    /**
     * Create a new exercise from manual entry, then confirm it.
     */
    fun saveManualEntry() {
        val s = _state.value
        if (s.manualName.isBlank() || s.manualCategory.isBlank() || s.manualEquipment.isBlank()) return

        viewModelScope.launch {
            val id = exerciseRepository.create(
                name = s.manualName.trim(),
                category = s.manualCategory,
                equipmentType = s.manualEquipment,
            )
            _state.update { it.copy(confirmedExerciseId = id) }
        }
    }

    /** Clear result and return to camera. */
    fun resetScan() {
        _state.update {
            OcrState() // fresh state, isScanning = true
        }
    }
}
