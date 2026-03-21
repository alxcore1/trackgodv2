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

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteById(workoutId: Long)
}
