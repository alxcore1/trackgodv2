package com.trackgod.app.service

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.trackgod.app.core.repository.BackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// -- Result types -------------------------------------------------------------

sealed class IntegrityResult {
    data object Healthy : IntegrityResult()
    data class Recovered(val fromBackup: String) : IntegrityResult()
    data object FreshStart : IntegrityResult()
    data class Error(val message: String) : IntegrityResult()
}

sealed class RecoveryResult {
    data class Restored(val backupDate: Long) : RecoveryResult()
    data object Failed : RecoveryResult()
}

// -- Manager ------------------------------------------------------------------

@Singleton
class DatabaseIntegrityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupRepository: BackupRepository,
) {
    companion object {
        private const val DB_NAME = "trackgod.db"
    }

    /**
     * Perform a startup integrity check on the database.
     *
     * - If the database is healthy, returns [IntegrityResult.Healthy].
     * - If corrupted, attempts auto-recovery from backups.
     * - If the file is missing, attempts to restore from the latest backup.
     */
    suspend fun performStartupCheck(): IntegrityResult = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(DB_NAME)

        // Check if database file exists
        if (!dbFile.exists()) {
            return@withContext attemptRecoveryAndReport()
        }

        // Check file size
        if (dbFile.length() == 0L) {
            return@withContext attemptRecoveryAndReport()
        }

        // Run PRAGMA integrity_check
        if (!runIntegrityCheck(dbFile)) {
            return@withContext attemptRecoveryAndReport()
        }

        IntegrityResult.Healthy
    }

    /**
     * Attempt to recover the database from available backups.
     *
     * Tries each backup from newest to oldest until one succeeds.
     */
    suspend fun attemptRecovery(): RecoveryResult = withContext(Dispatchers.IO) {
        val stats = backupRepository.getBackupStats()
        if (stats.count == 0) return@withContext RecoveryResult.Failed

        // Get all backups sorted newest-first via the DAO
        val backups = backupRepository.getAllBackupsOnce()

        for (backup in backups) {
            val file = File(backup.filePath)
            if (!file.exists() || file.length() == 0L) continue

            val restored = backupRepository.restoreFromBackup(backup.filePath)
            if (restored) {
                return@withContext RecoveryResult.Restored(backup.createdAt)
            }
        }

        RecoveryResult.Failed
    }

    /**
     * Create a safety backup before risky operations (updates, imports).
     *
     * @return The backup file path, or null on failure.
     */
    suspend fun createSafetyBackup(): String? {
        val result = backupRepository.createBackup(type = "safety")
        return if (result.success) result.path else null
    }

    // -- Helpers --------------------------------------------------------------

    private suspend fun attemptRecoveryAndReport(): IntegrityResult {
        return when (val result = attemptRecovery()) {
            is RecoveryResult.Restored -> IntegrityResult.Recovered(
                fromBackup = result.backupDate.toString()
            )
            is RecoveryResult.Failed -> IntegrityResult.FreshStart
        }
    }

    private fun runIntegrityCheck(dbFile: File): Boolean {
        return try {
            val db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
            )
            val cursor = db.rawQuery("PRAGMA integrity_check", null)
            val healthy = if (cursor.moveToFirst()) {
                cursor.getString(0).equals("ok", ignoreCase = true)
            } else {
                false
            }
            cursor.close()
            db.close()
            healthy
        } catch (_: Exception) {
            false
        }
    }
}
