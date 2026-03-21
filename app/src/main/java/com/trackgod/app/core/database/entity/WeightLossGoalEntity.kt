package com.trackgod.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_loss_goals",
    indices = [
        Index(value = ["is_active"])
    ]
)
data class WeightLossGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "starting_weight")
    val startingWeight: Float,

    @ColumnInfo(name = "target_weight")
    val targetWeight: Float,

    @ColumnInfo(name = "target_date")
    val targetDate: String,

    @ColumnInfo(name = "weekly_goal")
    val weeklyGoal: Float? = null,

    @ColumnInfo(name = "motivation_text")
    val motivationText: String? = null,

    @ColumnInfo(name = "reminder_day")
    val reminderDay: Int? = null,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: String? = null,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
