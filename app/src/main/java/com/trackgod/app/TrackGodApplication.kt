package com.trackgod.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.trackgod.app.service.BackupScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrackGodApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Schedule daily auto-backup if enabled
        val prefs = getSharedPreferences("trackgod_prefs", MODE_PRIVATE)
        val autoBackupEnabled = prefs.getBoolean("auto_backup", true)
        if (autoBackupEnabled) {
            BackupScheduler.scheduleDaily(this)
        }

        // Create notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            // Rest timer channel (high priority for alerts)
            nm.createNotificationChannel(
                NotificationChannel(
                    "rest_timer",
                    "Rest Timer",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Alerts when your rest period is over"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                }
            )

            // Active workout channel (low priority, persistent)
            nm.createNotificationChannel(
                NotificationChannel(
                    "workout_active",
                    "Active Workout",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Shows while a workout is in progress"
                    setShowBadge(false)
                }
            )
        }
    }
}
