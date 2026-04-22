package com.terminplaner.domain.repository

import com.terminplaner.domain.model.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAllAppointments(): Flow<List<Appointment>>
    fun getAppointmentsByDate(startOfDay: Long, endOfDay: Long): Flow<List<Appointment>>
    fun getDeletedAppointments(): Flow<List<Appointment>>
    suspend fun getAppointmentById(id: Long): Appointment?
    suspend fun insertAppointment(appointment: Appointment): Long
    suspend fun updateAppointment(appointment: Appointment)
    suspend fun softDeleteAppointment(id: Long)
    suspend fun restoreAppointment(id: Long)
    suspend fun permanentlyDeleteAppointment(id: Long)
    suspend fun emptyTrash()
    suspend fun getAllAppointmentsForExport(): List<Appointment>
    suspend fun importAppointments(appointments: List<Appointment>)
}