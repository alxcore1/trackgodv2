package com.trackgod.app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager worker that shows a weigh-in reminder notification.
 *
 * Scheduled weekly via [WeighInReminderScheduler].
 */
class WeighInReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "weigh_in_reminder"
        private const val NOTIFICATION_ID = 9001
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()
        showNotification()
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WEIGH-IN REMINDERS",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Weekly reminders to log your weight."
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("TRACKGOD")
            .setContentText("Time to step on the scale. Log your weight.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }
}

/**
 * Scheduler helper for the weigh-in reminder.
 *
 * Call [schedule] when the user enables the reminder; [cancel] when disabled.
 */
object WeighInReminderScheduler {

    private const val WORK_NAME = "weigh_in_reminder"

    /**
     * Schedule a weekly reminder at the given day/time.
     *
     * @param dayOfWeek Day name (e.g. "Sunday", "Monday").
     * @param hour Hour in 24-hour format.
     * @param minute Minute.
     */
    fun schedule(context: Context, dayOfWeek: String, hour: Int, minute: Int) {
        val initialDelay = calculateInitialDelay(dayOfWeek, hour, minute)

        val request = PeriodicWorkRequestBuilder<WeighInReminderWorker>(
            7, TimeUnit.DAYS,
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Calculate milliseconds from now until the next occurrence of
     * [dayOfWeek] at [hour]:[minute].
     */
    private fun calculateInitialDelay(dayOfWeek: String, hour: Int, minute: Int): Long {
        val targetDay = dayNameToCalendarDay(dayOfWeek)

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, targetDay)
        }

        // If the target time is in the past this week, move to next week
        if (target.before(now)) {
            target.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }

    private fun dayNameToCalendarDay(name: String): Int = when (name.lowercase()) {
        "sunday" -> Calendar.SUNDAY
        "monday" -> Calendar.MONDAY
        "tuesday" -> Calendar.TUESDAY
        "wednesday" -> Calendar.WEDNESDAY
        "thursday" -> Calendar.THURSDAY
        "friday" -> Calendar.FRIDAY
        "saturday" -> Calendar.SATURDAY
        else -> Calendar.SUNDAY
    }
}
