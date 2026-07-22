package com.trackgod.app.feature.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingRestoreUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val showRestartRequired: Boolean = false,
)

@HiltViewModel
class OnboardingRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingRestoreUiState())
    val uiState: StateFlow<OnboardingRestoreUiState> = _uiState.asStateFlow()

    fun importBackup(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            val success = backupRepository.importDatabase(sourceUri)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    showRestartRequired = success,
                    message = if (success) null else "RESTORE FAILED. SELECT A VALID TRACKGOD V2 .DB BACKUP.",
                )
            }
        }
    }
}
