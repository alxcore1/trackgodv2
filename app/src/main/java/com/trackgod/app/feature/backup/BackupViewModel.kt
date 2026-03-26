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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// -- UI State -----------------------------------------------------------------

data class BackupUiState(
    val stats: BackupStats = BackupStats(0, 0L, null),
    val isLoading: Boolean = false,
    val message: String? = null,
    val showRestartDialog: Boolean = false,
    val exportUri: Uri? = null,
    val csvExportUri: Uri? = null,
)

// -- ViewModel ----------------------------------------------------------------

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val workoutRepository: com.trackgod.app.core.repository.WorkoutRepository,
    private val settingsRepository: com.trackgod.app.core.repository.SettingsRepository,
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
            _uiState.update { it.copy(stats = stats) }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = backupRepository.createBackup(type = "manual")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = if (result.success) "BACKUP CREATED" else "BACKUP FAILED: ${result.errorMessage}",
                )
            }
            if (result.success) loadStats()
        }
    }

    fun deleteBackup(backup: BackupMetadataEntity) {
        viewModelScope.launch {
            backupRepository.deleteBackup(backup)
            loadStats()
            _uiState.update { it.copy(message = "BACKUP DELETED") }
        }
    }

    fun restoreFromBackup(backup: BackupMetadataEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            // Safety backup first
            backupRepository.createBackup(type = "safety")

            val success = backupRepository.restoreFromBackup(backup.filePath)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showRestartDialog = success,
                    message = if (!success) "RESTORE FAILED" else null,
                )
            }
        }
    }

    fun exportDatabase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val uri = backupRepository.exportDatabase()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    exportUri = uri,
                    message = if (uri == null) "EXPORT FAILED" else null,
                )
            }
        }
    }

    fun importDatabase(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val success = backupRepository.importDatabase(sourceUri)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showRestartDialog = success,
                    message = if (!success) "IMPORT FAILED: INVALID DATABASE FILE" else null,
                )
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            try {
                val rows = workoutRepository.getAllSetsForCsvExport()
                val unit = settingsRepository.getWeightUnit()
                fun csvEscape(s: String): String = "\"${s.replace("\"", "\"\"")}\""

                val sb = StringBuilder()
                sb.appendLine("Date,Workout,Exercise,Set,Weight ($unit),Reps,RPE,RIR,Note")
                for (r in rows) {
                    val note = r.note?.replace("\n", " ") ?: ""
                    sb.appendLine("${r.date},${csvEscape(r.workoutName)},${csvEscape(r.exerciseName)},${r.setNumber},${String.format(java.util.Locale.US, "%.1f", r.weight)},${r.reps},${r.rpe ?: ""},${r.rir ?: ""},${csvEscape(note)}")
                }

                val exportDir = java.io.File(appContext.cacheDir, "exports").apply { mkdirs() }
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                val file = java.io.File(exportDir, "trackgod_export_$timestamp.csv")
                file.writeText(sb.toString())

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    file,
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        csvExportUri = uri,
                        message = "CSV EXPORTED (${rows.size} SETS)",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "CSV EXPORT FAILED",
                    )
                }
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val success = backupRepository.deleteAllData()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showRestartDialog = success,
                    message = if (!success) "DELETE FAILED" else null,
                )
            }
        }
    }

    fun clearCsvExportUri() {
        _uiState.update { it.copy(csvExportUri = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearExportUri() {
        _uiState.update { it.copy(exportUri = null) }
    }

    fun dismissRestartDialog() {
        _uiState.update { it.copy(showRestartDialog = false) }
    }
}
