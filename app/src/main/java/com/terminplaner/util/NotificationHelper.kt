package com.terminplaner.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.terminplaner.MainActivity
import com.terminplaner.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "appointment_channel"
        const val CHANNEL_NAME = "Termin-Benachrichtigungen"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kanal für Termin-Erinnerungen"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String, appointmentId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("appointmentId", appointmentId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Add "Erledigt" action
        val doneIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_DONE_APPOINTMENT"
            putExtra("appointmentId", appointmentId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (appointmentId + 4000000).toInt(),
            doneIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.addAction(
            android.R.drawable.ic_menu_view,
            "Erledigt",
            donePendingIntent
        )

        // Add "15 Min" snooze action
        val snoozeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_SNOOZE_APPOINTMENT"
            putExtra("appointmentId", appointmentId)
            putExtra("snooze_minutes", 15)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (appointmentId + 5000000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.addAction(
            android.R.drawable.ic_menu_recent_history,
            "15 Min",
            snoozePendingIntent
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(appointmentId.toInt(), builder.build())
    }

    fun showTaskNotification(title: String, message: String, taskId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (taskId + 1000000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Add "Erledigt" action for tasks
        val doneIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_DONE_TASK"
            putExtra("taskId", taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 6000000).toInt(),
            doneIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.addAction(
            android.R.drawable.ic_menu_view,
            "Erledigt",
            donePendingIntent
        )

        // Add snooze actions
        listOf(15, 30, 60).forEach { minutes ->
            val snoozeIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = "ACTION_SNOOZE_TASK"
                putExtra("taskId", taskId)
                putExtra("snooze_minutes", minutes)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (taskId + 2000000 + minutes).toInt(),
                snoozeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(
                android.R.drawable.ic_menu_recent_history,
                "$minutes Min",
                snoozePendingIntent
            )
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((taskId + 1000000).toInt(), builder.build())
    }
}
