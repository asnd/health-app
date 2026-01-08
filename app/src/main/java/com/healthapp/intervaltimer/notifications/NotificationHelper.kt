package com.healthapp.intervaltimer.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.healthapp.intervaltimer.MainActivity
import com.healthapp.intervaltimer.R
import com.healthapp.intervaltimer.data.TimerPhase

object NotificationHelper {
    const val CHANNEL_ID = "interval_timer_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setSound(null, null) // We'll use our own sound
            enableVibration(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createTimerNotification(
        context: Context,
        phase: TimerPhase,
        remainingMinutes: Int
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val (title, content) = when (phase) {
            TimerPhase.ACTIVITY -> {
                context.getString(R.string.activity_phase) to
                    "$remainingMinutes ${context.getString(R.string.minutes)} remaining"
            }
            TimerPhase.REST -> {
                context.getString(R.string.rest_phase) to
                    "$remainingMinutes ${context.getString(R.string.minutes)} remaining"
            }
            TimerPhase.IDLE -> {
                context.getString(R.string.timer_notification_title) to "Timer idle"
            }
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    fun showPhaseChangeNotification(context: Context, phase: TimerPhase) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = when (phase) {
            TimerPhase.ACTIVITY -> context.getString(R.string.activity_phase)
            TimerPhase.REST -> context.getString(R.string.rest_phase)
            TimerPhase.IDLE -> return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Phase changed")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
}
