package com.terminplaner.data.repository

import com.terminplaner.data.local.dao.TaskDao
import com.terminplaner.domain.model.Task
import com.terminplaner.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTasksByAppointmentId(appointmentId: Long): Flow<List<Task>> {
        return taskDao.getTasksByAppointmentId(appointmentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    override suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteTask(id: Long) {
        taskDao.deleteTask(id)
    }

    override suspend fun toggleTaskCompletion(id: Long, isCompleted: Boolean) {
        taskDao.toggleTaskCompletion(id, isCompleted)
    }
}
