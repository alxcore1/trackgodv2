package com.trackgod.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["category"]),
        Index(value = ["usage_count"]),
        Index(value = ["is_active"])
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val category: String,

    @ColumnInfo(name = "equipment_type")
    val equipmentType: String,

    val brand: String? = null,

    @ColumnInfo(name = "alternative_names")
    val alternativeNames: String? = null,

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,

    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
