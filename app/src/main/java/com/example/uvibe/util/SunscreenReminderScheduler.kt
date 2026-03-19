package com.example.uvibe.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import com.example.uvibe.ui.model.SunscreenReminderUiModel

object SunscreenReminderScheduler {
    private const val REQUEST_CODE_PRE_ALERT = 2001
    private const val REQUEST_CODE_EXPIRY = 2002
    private const val ACTION_PRE_ALERT = "com.example.uvibe.action.PRE_ALERT"
    private const val ACTION_EXPIRE = "com.example.uvibe.action.EXPIRE"

    fun schedule(context: Context, state: SunscreenReminderUiModel) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        cancel(context)

        if (!state.isEnabled) return

        val nextReminderAtMillis = state.nextReminderAtMillis ?: return
        val preAlertAtMillis = nextReminderAtMillis - state.preAlertMinutes * 60_000L

        if (preAlertAtMillis > System.currentTimeMillis()) {
            scheduleAlarm(
                alarmManager = alarmManager,
                triggerAtMillis = preAlertAtMillis,
                operation = pendingIntent(
                    context = context,
                    requestCode = REQUEST_CODE_PRE_ALERT,
                    action = ACTION_PRE_ALERT,
                    title = "Protection ending soon",
                    message = "Your sunscreen reminder is due in 10 minutes.",
                )
            )
        }

        scheduleAlarm(
            alarmManager = alarmManager,
            triggerAtMillis = nextReminderAtMillis,
            operation = pendingIntent(
                context = context,
                requestCode = REQUEST_CODE_EXPIRY,
                action = ACTION_EXPIRE,
                title = "",
                message = "",
            )
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        alarmManager.cancel(
            pendingIntent(
                context = context,
                requestCode = REQUEST_CODE_PRE_ALERT,
                action = ACTION_PRE_ALERT,
                title = "",
                message = "",
            )
        )
        alarmManager.cancel(
            pendingIntent(
                context = context,
                requestCode = REQUEST_CODE_EXPIRY,
                action = ACTION_EXPIRE,
                title = "",
                message = "",
            )
        )
    }

    private fun pendingIntent(
        context: Context,
        requestCode: Int,
        action: String,
        title: String,
        message: String,
    ): PendingIntent {
        val intent = Intent(context, SunscreenReminderReceiver::class.java).apply {
            this.action = action
            putExtra(SunscreenReminderReceiver.EXTRA_TITLE, title)
            putExtra(SunscreenReminderReceiver.EXTRA_MESSAGE, message)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        operation: PendingIntent,
    ) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            }
        }.getOrElse {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }
}
