package com.trackgod.app.service

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.trackgod.app.core.database.dao.BackupDao
import com.trackgod.app.core.repository.BackupRepository
import java.util.concurrent.TimeUnit

/**
 * Daily WorkManager worker that creates an automatic backup and enforces
 * the retention policy.
 */
class BackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Build a lightweight BackupRepository manually since we are not
            // using @HiltWorker to keep setup simple and avoid the extra
            // hilt-work dependency.
            val context = applicationContext
            val db = androidx.room.Room.databaseBuilder(
                context,
                com.trackgod.app.core.database.TrackGodDatabase::class.java,
                "trackgod.db",
            ).addMigrations(com.trackgod.app.core.database.TrackGodDatabase.MIGRATION_1_2, com.trackgod.app.core.database.TrackGodDatabase.MIGRATION_2_3, com.trackgod.app.core.database.TrackGodDatabase.MIGRATION_3_4)
             .build()

            val backupDao: BackupDao = db.backupDao()
            val repo = BackupRepository(backupDao, context)

            val result = repo.createBackup(type = "auto")

            if (result.success) {
                // Read max backups from prefs
                val prefs: SharedPreferences =
                    context.getSharedPreferences("trackgod_prefs", Context.MODE_PRIVATE)
                val maxBackups = prefs.getInt("max_backups", 10)
                repo.enforceRetentionPolicy(maxBackups)
            }

            db.close()

            if (result.success) Result.success() else Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
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
