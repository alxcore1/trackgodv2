package com.trackgod.app.feature.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.repository.ImportResult
import com.trackgod.app.core.repository.V1ImportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ImportStatus {
    Idle,
    Importing,
    Success,
    Error,
}

data class V1ImportState(
    val status: ImportStatus = ImportStatus.Idle,
    val result: ImportResult? = null,
    val selectedFileName: String? = null,
)

@HiltViewModel
class V1ImportViewModel @Inject constructor(
    private val v1ImportRepository: V1ImportRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(V1ImportState())
    val state: StateFlow<V1ImportState> = _state.asStateFlow()

    fun importDatabase(uri: Uri, fileName: String?) {
        _state.value = V1ImportState(
            status = ImportStatus.Importing,
            selectedFileName = fileName,
        )

        viewModelScope.launch {
            val result = v1ImportRepository.importV1Database(uri)
            _state.value = V1ImportState(
                status = if (result.success) ImportStatus.Success else ImportStatus.Error,
                result = result,
                selectedFileName = fileName,
            )
        }
    }

    fun resetState() {
        _state.value = V1ImportState()
    }
}
