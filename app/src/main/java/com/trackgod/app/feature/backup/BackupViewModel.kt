package com.trackgod.app.feature.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackgod.app.core.database.entity.BackupMetadataEntity
import com.trackgod.app.core.repository.BackupRepository
import com.trackgod.app.core.repository.BackupStats
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// -- UI State -----------------------------------------------------------------

data class BackupUiState(
    val stats: BackupStats = BackupStats(0, 0L, null),
    val isLoading: Boolean = false,
    val message: String? = null,
    val showRestartDialog: Boolean = false,
    val exportUri: Uri? = null,
)

// -- ViewModel ----------------------------------------------------------------

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val backups: StateFlow<List<BackupMetadataEntity>> =
        backupRepository.getAllBackups()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val stats = backupRepository.getBackupStats()
            _uiState.value = _uiState.value.copy(stats = stats)
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = backupRepository.createBackup(type = "manual")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = if (result.success) "BACKUP CREATED" else "BACKUP FAILED: ${result.errorMessage}",
            )
            if (result.success) loadStats()
        }
    }

    fun deleteBackup(backup: BackupMetadataEntity) {
        viewModelScope.launch {
            backupRepository.deleteBackup(backup)
            loadStats()
            _uiState.value = _uiState.value.copy(message = "BACKUP DELETED")
        }
    }

    fun restoreFromBackup(backup: BackupMetadataEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)

            // Safety backup first
            backupRepository.createBackup(type = "safety")

            val success = backupRepository.restoreFromBackup(backup.filePath)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                showRestartDialog = success,
                message = if (!success) "RESTORE FAILED" else null,
            )
        }
    }

    fun exportDatabase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val uri = backupRepository.exportDatabase()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                exportUri = uri,
                message = if (uri == null) "EXPORT FAILED" else null,
            )
        }
    }

    fun importDatabase(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val success = backupRepository.importDatabase(sourceUri)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                showRestartDialog = success,
                message = if (!success) "IMPORT FAILED: INVALID DATABASE FILE" else null,
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearExportUri() {
        _uiState.value = _uiState.value.copy(exportUri = null)
    }

    fun dismissRestartDialog() {
        _uiState.value = _uiState.value.copy(showRestartDialog = false)
    }
}
