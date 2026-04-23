package com.terminplaner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.terminplaner.data.local.dao.AppointmentDao
import com.terminplaner.data.local.dao.CategoryDao
import com.terminplaner.data.local.dao.TaskDao
import com.terminplaner.data.local.entity.AppointmentEntity
import com.terminplaner.data.local.entity.CategoryEntity
import com.terminplaner.data.local.entity.TaskEntity

@Database(
    entities = [AppointmentEntity::class, CategoryEntity::class, TaskEntity::class],
    version = 5,
    exportSchema = false
)
abstract class TerminPlanerDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
}
