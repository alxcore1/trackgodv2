package com.trackgod.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trackgod.app.core.database.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Delete
    suspend fun delete(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts ORDER BY start_time DESC LIMIT :limit OFFSET :offset")
    fun getPaginated(limit: Int, offset: Int): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY start_time DESC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE is_completed = 0 LIMIT 1")
    suspend fun getIncompleteWorkout(): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE date = :todayDate ORDER BY start_time DESC")
    fun getTodayWorkouts(todayDate: String): Flow<List<WorkoutEntity>>

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun getWorkoutCount(): Int

    @Query("SELECT COUNT(*) FROM workouts WHERE is_completed = 1")
    suspend fun getCompletedWorkoutCount(): Int

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteById(workoutId: Long)

    @Query("SELECT * FROM workouts WHERE is_completed = 1 ORDER BY start_time DESC LIMIT :limit")
    suspend fun getRecentCompleted(limit: Int = 5): List<WorkoutEntity>

    @Query("SELECT DISTINCT date FROM workouts WHERE is_completed = 1 ORDER BY date DESC")
    suspend fun getCompletedWorkoutDates(): List<String>

    @Query("SELECT * FROM workouts WHERE is_completed = 1 ORDER BY start_time DESC")
    fun getAllCompleted(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE is_completed = 1 AND date = :date ORDER BY start_time DESC")
    suspend fun getCompletedByDate(date: String): List<WorkoutEntity>

    @Query(
        """
        SELECT * FROM workouts
        WHERE is_completed = 1 AND name LIKE '%' || :query || '%'
        ORDER BY start_time DESC
        """
    )
    suspend fun searchByName(query: String): List<WorkoutEntity>

    @Query("UPDATE workouts SET name = :name WHERE id = :workoutId")
    suspend fun updateName(workoutId: Long, name: String)

    @Query("UPDATE workouts SET total_volume = :volume WHERE id = :id")
    suspend fun updateVolume(id: Long, volume: Float)

    @Query("SELECT * FROM workouts WHERE name = :name AND is_completed = 1")
    suspend fun getByName(name: String): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE name IN (:names) AND is_completed = 1")
    suspend fun getByNames(names: List<String>): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE is_completed = 1 ORDER BY date DESC")
    suspend fun getAllCompletedOnce(): List<WorkoutEntity>

    @Query(
        """
        SELECT date, total_volume AS totalVolume
        FROM workouts
        WHERE is_completed = 1 AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
        """
    )
    suspend fun getVolumeByDate(startDate: String, endDate: String): List<DateVolume>
}

data class DateVolume(
    val date: String,
    val totalVolume: Float?
)
