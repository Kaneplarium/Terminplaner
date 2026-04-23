package com.terminplaner.domain.model

data class ExportData(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val appointments: List<Appointment> = emptyList(),
    val categories: List<Category> = emptyList()
)
