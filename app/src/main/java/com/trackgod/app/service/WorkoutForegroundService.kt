package com.trackgod.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.trackgod.app.MainActivity
import com.trackgod.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WorkoutForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "workout_active"
        const val NOTIFICATION_ID = 9999
        private const val EXTRA_START_TIME = "start_time"

        fun start(context: Context, startTime: Long) {
            val intent = Intent(context, WorkoutForegroundService::class.java).apply {
                putExtra(EXTRA_START_TIME, startTime)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WorkoutForegroundService::class.java))
            // Immediately cancel notification in case onDestroy is delayed
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIFICATION_ID)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var updateJob: Job? = null
    private var workoutStartTime: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        workoutStartTime = intent?.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
            ?: System.currentTimeMillis()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(0),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification(0))
        }

        updateJob?.cancel()
        updateJob = scope.launch {
            while (true) {
                val elapsed = ((System.currentTimeMillis() - workoutStartTime) / 1000).toInt()
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIFICATION_ID, buildNotification(elapsed))
                delay(30_000L) // Update every 30 seconds
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        updateJob?.cancel()
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Workout",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows while a workout is in progress"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(elapsedSeconds: Int): android.app.Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPending = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val minutes = elapsedSeconds / 60
        val hours = minutes / 60
        val durationText = if (hours > 0) {
            "%dh %02dm".format(hours, minutes % 60)
        } else {
            "%d min".format(minutes)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pentagram)
            .setContentTitle("Workout In Progress")
            .setContentText("$durationText - Tap to return to TrackGod")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(tapPending)
            .build()
    }
}
