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
}
