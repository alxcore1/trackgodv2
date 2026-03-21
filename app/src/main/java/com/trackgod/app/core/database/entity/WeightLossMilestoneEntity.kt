package com.trackgod.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_loss_milestones",
    foreignKeys = [
        ForeignKey(
            entity = WeightLossGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["goal_id"])
    ]
)
data class WeightLossMilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "goal_id")
    val goalId: Long,

    @ColumnInfo(name = "target_weight")
    val targetWeight: Float,

    val description: String? = null,

    @ColumnInfo(name = "is_achieved")
    val isAchieved: Boolean = false,

    @ColumnInfo(name = "achieved_date")
    val achievedDate: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
