package com.trackgod.app.core.util

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Process
import android.os.SystemClock
import kotlin.system.exitProcess

fun restartAppProcess(context: Context) {
    val appContext = context.applicationContext
    val launchIntent = appContext.packageManager
        .getLaunchIntentForPackage(appContext.packageName)
        ?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ?: return

    val pendingIntent = PendingIntent.getActivity(
        appContext,
        0,
        launchIntent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + 150L,
        pendingIntent,
    )

    (context as? Activity)?.finishAffinity()
    Process.killProcess(Process.myPid())
    exitProcess(0)
}
