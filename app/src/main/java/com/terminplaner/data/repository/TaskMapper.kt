package com.terminplaner.data.repository

import com.terminplaner.data.local.entity.TaskEntity
import com.terminplaner.domain.model.Task

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    appointmentId = appointmentId,
    reminderTime = reminderTime,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    appointmentId = appointmentId,
    reminderTime = reminderTime,
    createdAt = createdAt,
    updatedAt = updatedAt
)
