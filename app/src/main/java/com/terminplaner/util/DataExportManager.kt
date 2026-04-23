package com.terminplaner.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.terminplaner.domain.model.ExportData
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun autoExport() = withContext(Dispatchers.IO) {
        try {
            val appointments = appointmentRepository.getAllAppointmentsForExport()
            val categories = categoryRepository.getAllCategoriesForExport()

            val exportData = ExportData(
                appointments = appointments,
                categories = categories
            )

            val json = gson.toJson(exportData)
            val file = File(context.filesDir, "auto_export_terminplaner.json")
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
