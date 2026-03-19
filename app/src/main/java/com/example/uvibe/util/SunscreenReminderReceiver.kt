package com.example.uvibe.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.uvibe.MainActivity
import com.example.uvibe.R

class SunscreenReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_EXPIRE -> {
                clearReminderState(context)
                return
            }
            ACTION_PRE_ALERT -> Unit
            else -> return
        }

        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val launchIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            3001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Sunscreen reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Check your sunscreen timer."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "sunscreen_reminders"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val ACTION_PRE_ALERT = "com.example.uvibe.action.PRE_ALERT"
        const val ACTION_EXPIRE = "com.example.uvibe.action.EXPIRE"
        private const val PREF_NAME = "uvibe_sunscreen_reminder"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sunscreen reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Reminders for sunscreen reapplication and protection expiry"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        private fun clearReminderState(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("enabled", false)
                .remove("last_applied_at")
                .remove("next_reminder_at")
                .apply()
        }
    }
}
