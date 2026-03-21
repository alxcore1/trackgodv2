package com.trackgod.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String? = null,

    val gender: String? = null,

    val birthday: String? = null,

    val height: Float? = null,

    val weight: Float? = null,

    @ColumnInfo(name = "primary_objective")
    val primaryObjective: String? = null,

    @ColumnInfo(name = "experience_level")
    val experienceLevel: String = "intermediate",

    @ColumnInfo(name = "weekly_target")
    val weeklyTarget: Int = 4,

    @ColumnInfo(name = "weight_unit")
    val weightUnit: String = "kg",

    @ColumnInfo(name = "height_unit")
    val heightUnit: String = "cm",

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
