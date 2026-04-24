package com.terminplaner.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appointmentRepository: AppointmentRepository

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                rescheduleAlarms(context)
            }
            "ACTION_TASK_REMINDER" -> {
                val title = intent.getStringExtra("title") ?: "Aufgabe"
                val message = intent.getStringExtra("message") ?: "Erinnerung an deine Aufgabe."
                val taskId = intent.getLongExtra("taskId", -1L)
                if (taskId != -1L) {
                    NotificationHelper(context).showTaskNotification(title, message, taskId)
                }
            }
            "ACTION_SNOOZE_TASK" -> {
                val taskId = intent.getLongExtra("taskId", -1L)
                val minutes = intent.getIntExtra("snooze_minutes", 15)
                if (taskId != -1L) {
                    snoozeTask(context, taskId, minutes.toLong())
                }
            }
            "ACTION_DONE_TASK" -> {
                val taskId = intent.getLongExtra("taskId", -1L)
                if (taskId != -1L) {
                    markTaskDone(context, taskId)
                }
            }
            "ACTION_SNOOZE_APPOINTMENT" -> {
                val appointmentId = intent.getLongExtra("appointmentId", -1L)
                val minutes = intent.getIntExtra("snooze_minutes", 15)
                if (appointmentId != -1L) {
                    snoozeAppointment(context, appointmentId, minutes.toLong())
                }
            }
            "ACTION_DONE_APPOINTMENT" -> {
                val appointmentId = intent.getLongExtra("appointmentId", -1L)
                if (appointmentId != -1L) {
                    markAppointmentDone(context, appointmentId)
                }
            }
            "ACTION_START_FOCUS_MODE" -> {
                DndManager(context).setDndMode(true)
            }
            "ACTION_END_FOCUS_MODE" -> {
                DndManager(context).setDndMode(false)
            }
            else -> {
                if (intent.action == "ACTION_APPOINTMENT_REMINDER" || intent.action == null) {
                    val title = intent.getStringExtra("title") ?: "Termin"
                    val message = intent.getStringExtra("message") ?: "Du hast einen anstehenden Termin."
                    val appointmentId = intent.getLongExtra("appointmentId", -1L)

                    if (appointmentId != -1L) {
                        NotificationHelper(context).showNotification(title, message, appointmentId)
                    }
                }
            }
        }
    }

    private fun snoozeTask(context: Context, taskId: Long, minutes: Long) {
        // Dismiss the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel((taskId + 1000000).toInt())

        CoroutineScope(Dispatchers.IO).launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            val snoozeTime = System.currentTimeMillis() + (minutes * 60 * 1000)
            val updatedTask = task.copy(reminderTime = snoozeTime)
            
            taskRepository.updateTask(updatedTask)
            AlarmScheduler(context).scheduleTaskReminder(updatedTask)
        }
    }

    private fun markTaskDone(context: Context, taskId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel((taskId + 1000000).toInt())

        CoroutineScope(Dispatchers.IO).launch {
            taskRepository.toggleTaskCompletion(taskId, true)
        }
    }

    private fun snoozeAppointment(context: Context, appointmentId: Long, minutes: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(appointmentId.toInt())

        CoroutineScope(Dispatchers.IO).launch {
            val appointment = appointmentRepository.getAppointmentById(appointmentId) ?: return@launch
            val snoozeTime = System.currentTimeMillis() + (minutes * 60 * 1000)
            val updatedAppointment = appointment.copy(dateTime = snoozeTime)
            
            // We don't update the appointment in DB to keep original time, 
            // but we schedule a new one-time alarm.
            // Or maybe we should update it? Usually snooze just reschedules the notification.
            // Let's just reschedule the notification using a temporary alarm action.
            val scheduler = AlarmScheduler(context)
            scheduler.schedule(updatedAppointment) 
        }
    }

    private fun markAppointmentDone(context: Context, appointmentId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(appointmentId.toInt())

        CoroutineScope(Dispatchers.IO).launch {
            val appointment = appointmentRepository.getAppointmentById(appointmentId) ?: return@launch
            appointmentRepository.updateAppointment(appointment.copy(isCompleted = true))
        }
    }

    private fun rescheduleAlarms(context: Context) {
        val scheduler = AlarmScheduler(context)
        CoroutineScope(Dispatchers.IO).launch {
            // Appointments
            val appointments = appointmentRepository.getAllAppointmentsForExport()
            appointments.filter { !it.isDeleted && it.dateTime > System.currentTimeMillis() }
                .forEach { scheduler.schedule(it) }
            
            // Tasks
            taskRepository.getAllTasks().collect { tasks ->
                tasks.filter { !it.isCompleted && (it.reminderTime ?: 0L) > System.currentTimeMillis() }
                    .forEach { scheduler.scheduleTaskReminder(it) }
            }
        }
    }
}
