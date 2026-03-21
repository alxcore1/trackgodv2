package com.trackgod.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "body_metrics",
    indices = [
        Index(value = ["date"])
    ]
)
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: String,

    val weight: Float? = null,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,

    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
