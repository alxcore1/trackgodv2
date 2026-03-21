package com.trackgod.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trackgod.app.core.database.entity.BodyMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metric: BodyMetricEntity): Long

    @Update
    suspend fun update(metric: BodyMetricEntity)

    @Delete
    suspend fun delete(metric: BodyMetricEntity)

    @Query("SELECT * FROM body_metrics ORDER BY date DESC")
    fun getAll(): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): BodyMetricEntity?

    @Query("SELECT * FROM body_metrics WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics WHERE photo_uri IS NOT NULL ORDER BY date DESC LIMIT :limit")
    fun getProgressPhotos(limit: Int): Flow<List<BodyMetricEntity>>
}
