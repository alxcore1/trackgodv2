package com.trackgod.app.core.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.trackgod.app.core.database.dao.BackupDao
import com.trackgod.app.core.database.entity.BackupMetadataEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// -- Result types -------------------------------------------------------------

data class BackupResult(
    val success: Boolean,
    val path: String? = null,
    val sizeBytes: Long = 0,
    val errorMessage: String? = null,
)

data class BackupStats(
    val count: Int,
    val totalSizeBytes: Long,
    val lastBackupTime: Long?,
)

// -- Repository ---------------------------------------------------------------

@Singleton
class BackupRepository @Inject constructor(
    private val backupDao: BackupDao,
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val BACKUP_DIR = "backups"
        private const val EXPORT_DIR = "exports"
        private const val DB_NAME = "trackgod.db"
        private val SQLITE_HEADER = "SQLite format 3".toByteArray(Charsets.US_ASCII)
    }

    // -- Create ---------------------------------------------------------------

    /**
     * Create a backup of the current database.
     *
     * @param type One of "auto", "manual", or "safety".
     */
    suspend fun createBackup(type: String = "manual"): BackupResult = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists() || dbFile.length() == 0L) {
                return@withContext BackupResult(
                    success = false,
                    errorMessage = "Database file not found or empty",
                )
            }

            // Checkpoint WAL to ensure all data is in the main file
            checkpointDatabase()

            val backupDir = File(context.filesDir, BACKUP_DIR).apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "trackgod_backup_${timestamp}.db")

            dbFile.copyTo(backupFile, overwrite = true)

            // Validate copy
            if (!backupFile.exists() || backupFile.length() == 0L) {
                return@withContext BackupResult(
                    success = false,
                    errorMessage = "Backup file validation failed",
                )
            }

            val metadata = BackupMetadataEntity(
                filePath = backupFile.absolutePath,
                fileSize = backupFile.length(),
                backupType = type,
                createdAt = System.currentTimeMillis(),
            )
            backupDao.insert(metadata)

            BackupResult(
                success = true,
                path = backupFile.absolutePath,
                sizeBytes = backupFile.length(),
            )
        } catch (e: Exception) {
            BackupResult(success = false, errorMessage = e.message)
        }
    }

    // -- Restore --------------------------------------------------------------

    /**
     * Restore the database from a backup file.
     *
     * After calling this the app MUST be restarted so Room re-initializes.
     */
    suspend fun restoreFromBackup(backupPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupPath)
            if (!backupFile.exists() || !isValidSqlite(backupFile)) return@withContext false

            val dbFile = context.getDatabasePath(DB_NAME)

            // Copy backup over the main database
            backupFile.copyTo(dbFile, overwrite = true)

            // Delete WAL / SHM sidecar files
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()

            true
        } catch (_: Exception) {
            false
        }
    }

    // -- Export ----------------------------------------------------------------

    /**
     * Export the database to a shareable content URI.
     *
     * Creates a copy in the cache/exports directory and generates a FileProvider URI.
     */
    suspend fun exportDatabase(): Uri? = withContext(Dispatchers.IO) {
        try {
            checkpointDatabase()

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) return@withContext null

            val exportDir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val exportFile = File(exportDir, "trackgod_export_${timestamp}.db")

            dbFile.copyTo(exportFile, overwrite = true)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile,
            )
        } catch (_: Exception) {
            null
        }
    }

    // -- Import ---------------------------------------------------------------

    /**
     * Import a database from a content URI.
     *
     * Creates a safety backup first. After success the app MUST be restarted.
     */
    suspend fun importDatabase(sourceUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // Validate the source file
            val tempFile = File(context.cacheDir, "import_temp.db")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext false

            if (!isValidSqlite(tempFile)) {
                tempFile.delete()
                return@withContext false
            }

            // Create safety backup before replacing
            createBackup(type = "safety")

            val dbFile = context.getDatabasePath(DB_NAME)

            tempFile.copyTo(dbFile, overwrite = true)

            // Delete WAL / SHM sidecar files
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()

            tempFile.delete()
            true
        } catch (_: Exception) {
            false
        }
    }

    // -- Query ----------------------------------------------------------------

    fun getAllBackups(): Flow<List<BackupMetadataEntity>> =
        backupDao.getAll()

    suspend fun getAllBackupsOnce(): List<BackupMetadataEntity> =
        backupDao.getAllOnce()

    suspend fun deleteBackup(backup: BackupMetadataEntity) = withContext(Dispatchers.IO) {
        // Delete file from filesystem
        File(backup.filePath).delete()
        // Delete metadata from database
        backupDao.delete(backup)
    }

    suspend fun enforceRetentionPolicy(maxBackups: Int = 10) = withContext(Dispatchers.IO) {
        val count = backupDao.getCount()
        if (count <= maxBackups) return@withContext

        val excess = count - maxBackups
        val oldBackups = backupDao.getOldestBackups(excess)
        for (backup in oldBackups) {
            File(backup.filePath).delete()
            backupDao.delete(backup)
        }
    }

    suspend fun getBackupStats(): BackupStats = withContext(Dispatchers.IO) {
        BackupStats(
            count = backupDao.getCount(),
            totalSizeBytes = backupDao.getTotalSize() ?: 0L,
            lastBackupTime = backupDao.getLastBackupTime(),
        )
    }

    // -- Helpers --------------------------------------------------------------

    private fun checkpointDatabase() {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE,
            )
            db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).use { it.moveToFirst() }
            db.close()
        } catch (_: Exception) {
            // Non-critical: backup will still work, just might miss latest WAL data
        }
    }

    private fun isValidSqlite(file: File): Boolean {
        if (!file.exists() || file.length() < SQLITE_HEADER.size) return false
        return try {
            FileInputStream(file).use { stream ->
                val header = ByteArray(SQLITE_HEADER.size)
                stream.read(header)
                header.contentEquals(SQLITE_HEADER)
            }
        } catch (_: Exception) {
            false
        }
    }
}
