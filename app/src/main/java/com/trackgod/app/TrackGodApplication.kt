package com.trackgod.app

import android.app.Application
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
    }
}
