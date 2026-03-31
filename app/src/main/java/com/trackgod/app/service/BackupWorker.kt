package com.trackgod.app.service

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Daily WorkManager worker that creates an automatic backup and enforces
 * a file-based retention policy.
 *
 * This worker performs a raw file copy of the database instead of opening a
 * second Room instance, which would risk WAL lock contention with the
 * Hilt-managed singleton.
 */
class BackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        private const val DB_NAME = "trackgod.db"
        private const val BACKUP_DIR = "backups"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val context = applicationContext
            val dbFile = context.getDatabasePath(DB_NAME)

            if (!dbFile.exists() || dbFile.length() == 0L) {
                return@withContext Result.failure()
            }

            // Checkpoint WAL so the main db file is up to date
            checkpointDatabase(dbFile)

            val backupDir = File(context.filesDir, BACKUP_DIR).apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "trackgod_backup_${timestamp}.db")

            dbFile.copyTo(backupFile, overwrite = true)

            if (!backupFile.exists() || backupFile.length() == 0L) {
                return@withContext Result.retry()
            }

            // Enforce retention by deleting oldest files on disk
            val prefs: SharedPreferences =
                context.getSharedPreferences("trackgod_prefs", Context.MODE_PRIVATE)
            val maxBackups = prefs.getInt("max_backups", 10)
            enforceFileRetention(backupDir, maxBackups)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    /**
     * Checkpoint WAL using the raw Android SQLite API (no Room).
     */
    private fun checkpointDatabase(dbFile: File) {
        try {
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

    /**
     * Keep only the [maxBackups] most recent backup files, deleting the rest.
     */
    private fun enforceFileRetention(backupDir: File, maxBackups: Int) {
        val backups = backupDir.listFiles { file ->
            file.name.startsWith("trackgod_backup_") && file.name.endsWith(".db")
        } ?: return

        if (backups.size <= maxBackups) return

        // Sort oldest-first by last modified time, delete the excess
        backups.sortedBy { it.lastModified() }
            .dropLast(maxBackups)
            .forEach { it.delete() }
    }
}

/**
 * Scheduler helper for the daily auto-backup.
 */
object BackupScheduler {

    private const val WORK_NAME = "daily_backup"

    fun scheduleDaily(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
