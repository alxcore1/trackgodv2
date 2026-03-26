package com.trackgod.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trackgod.app.core.database.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Delete
    suspend fun delete(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises WHERE is_active = 1 ORDER BY usage_count DESC, name ASC")
    fun getAllActive(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE is_active = 1 ORDER BY usage_count DESC, name ASC")
    suspend fun getAllActiveSnapshot(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE category = :category AND is_active = 1 ORDER BY usage_count DESC")
    fun getByCategory(category: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE is_active = 1 AND (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%') ORDER BY usage_count DESC")
    fun search(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("UPDATE exercises SET usage_count = usage_count + 1, last_used_at = :timestamp WHERE id = :id")
    suspend fun incrementUsageCount(id: Long, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getCount(): Int

    @Query("SELECT * FROM exercises WHERE is_active = 1 AND last_used_at IS NOT NULL ORDER BY last_used_at DESC LIMIT :limit")
    suspend fun getRecentlyUsed(limit: Int = 6): List<ExerciseEntity>

    @Query("SELECT DISTINCT brand FROM exercises WHERE brand IS NOT NULL AND brand != '' AND is_active = 1 ORDER BY brand ASC")
    suspend fun getDistinctBrands(): List<String>

    @Query("SELECT * FROM exercises WHERE equipment_type = 'machine' AND is_active = 1")
    suspend fun getAllMachinesOnce(): List<ExerciseEntity>

    @Query("UPDATE exercises SET series = :series WHERE id = :id")
    suspend fun updateSeries(id: Long, series: String)

    @Query("UPDATE exercises SET is_active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE exercises SET usage_count = usage_count + :count, last_used_at = CASE WHEN :lastUsed IS NOT NULL AND (last_used_at IS NULL OR :lastUsed > last_used_at) THEN :lastUsed ELSE last_used_at END WHERE id = :id")
    suspend fun transferUsage(id: Long, count: Int, lastUsed: Long?)

    /**
     * Delete duplicate exercises, keeping the one with the lowest id (or highest usage_count).
     * Duplicates that have sets referencing them are NOT deleted to avoid orphaned data.
     */
    @Query("""
        DELETE FROM exercises WHERE id IN (
            SELECT e.id FROM exercises e
            WHERE e.id NOT IN (
                SELECT MIN(e2.id) FROM exercises e2 GROUP BY e2.name
            )
            AND e.id NOT IN (
                SELECT DISTINCT exercise_id FROM sets
            )
        )
    """)
    suspend fun removeDuplicates(): Int
}
