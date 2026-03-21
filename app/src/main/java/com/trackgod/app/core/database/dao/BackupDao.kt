package com.trackgod.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trackgod.app.core.database.entity.BackupMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(backup: BackupMetadataEntity): Long

    @Delete
    suspend fun delete(backup: BackupMetadataEntity)

    @Query("SELECT * FROM backup_metadata ORDER BY created_at DESC")
    fun getAll(): Flow<List<BackupMetadataEntity>>

    @Query("SELECT * FROM backup_metadata ORDER BY created_at DESC")
    suspend fun getAllOnce(): List<BackupMetadataEntity>

    @Query("SELECT COUNT(*) FROM backup_metadata")
    suspend fun getCount(): Int

    @Query("SELECT SUM(file_size) FROM backup_metadata")
    suspend fun getTotalSize(): Long?

    @Query("SELECT MAX(created_at) FROM backup_metadata")
    suspend fun getLastBackupTime(): Long?

    @Query("DELETE FROM backup_metadata WHERE id NOT IN (SELECT id FROM backup_metadata ORDER BY created_at DESC LIMIT :keepCount)")
    suspend fun deleteOldest(keepCount: Int)

    @Query("SELECT * FROM backup_metadata ORDER BY created_at ASC LIMIT :count")
    suspend fun getOldestBackups(count: Int): List<BackupMetadataEntity>
}
