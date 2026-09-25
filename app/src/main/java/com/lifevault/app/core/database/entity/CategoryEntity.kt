package com.lifevault.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey val id: String, // UUID; built-ins use fixed ids e.g. "cat_identity"
    val name: String, // 1..40 chars, unique (case-insensitive enforced in repo)
    val iconKey: String, // Material Symbol name, e.g. "badge"
    val colorKey: String, // one of the 12 palette keys, e.g. "blue"
    val isSystem: Boolean, // built-in: cannot be deleted
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
