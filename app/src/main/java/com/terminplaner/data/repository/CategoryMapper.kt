package com.terminplaner.data.repository

import com.terminplaner.data.local.entity.CategoryEntity
import com.terminplaner.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)