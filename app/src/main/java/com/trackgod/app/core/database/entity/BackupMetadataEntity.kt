package com.trackgod.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "backup_type")
    val backupType: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
