package com.terminplaner.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.terminplaner.domain.model.Appointment

object ExternalCalendarHelper {

    fun addToExternalCalendar(context: Context, appointment: Appointment) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, appointment.title)
            putExtra(CalendarContract.Events.DESCRIPTION, appointment.description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, appointment.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, appointment.dateTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, appointment.endDateTime)
            // Optional: If you want to invite persons, you might need to handle them as attendees
            // but ACTION_INSERT usually just populates the basic fields.
            if (!appointment.persons.isNullOrBlank()) {
                val currentDescription = appointment.description ?: ""
                val personsText = "Teilnehmer: ${appointment.persons}"
                putExtra(CalendarContract.Events.DESCRIPTION, if (currentDescription.isBlank()) personsText else "$currentDescription\n\n$personsText")
            }
        }
        context.startActivity(intent)
    }
}
