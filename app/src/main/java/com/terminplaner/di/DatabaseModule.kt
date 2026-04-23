package com.terminplaner.di

import android.content.Context
import androidx.room.Room
import com.terminplaner.data.local.TerminPlanerDatabase
import com.terminplaner.data.local.dao.AppointmentDao
import com.terminplaner.data.local.dao.CategoryDao
import com.terminplaner.data.local.dao.TaskDao
import com.terminplaner.data.repository.AppointmentRepositoryImpl
import com.terminplaner.data.repository.CategoryRepositoryImpl
import com.terminplaner.data.repository.TaskRepositoryImpl
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import com.terminplaner.domain.repository.TaskRepository
import com.terminplaner.util.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TerminPlanerDatabase {
        return Room.databaseBuilder(
            context,
            TerminPlanerDatabase::class.java,
            "terminplaner.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAppointmentDao(database: TerminPlanerDatabase): AppointmentDao {
        return database.appointmentDao()
    }

    @Provides
    fun provideCategoryDao(database: TerminPlanerDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideTaskDao(database: TerminPlanerDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context): AlarmScheduler {
        return AlarmScheduler(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
