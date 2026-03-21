package com.trackgod.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trackgod.app.core.database.entity.SetEntity
import kotlinx.coroutines.flow.Flow

data class CategoryVolume(
    val category: String,
    val totalVolume: Float
)

data class PersonalRecordResult(
    val exerciseId: Long,
    val name: String,
    val estimated1rm: Float
)

@Dao
interface SetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: SetEntity): Long

    @Update
    suspend fun update(set: SetEntity)

    @Delete
    suspend fun delete(set: SetEntity)

    @Query("SELECT * FROM sets WHERE workout_id = :workoutId ORDER BY set_number ASC")
    fun getByWorkout(workoutId: Long): Flow<List<SetEntity>>

    @Query(
        """
        SELECT s.* FROM sets s
        INNER JOIN workouts w ON s.workout_id = w.id
        WHERE s.exercise_id = :exerciseId AND w.is_completed = 1
        ORDER BY w.start_time DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentForExercise(exerciseId: Long, limit: Int = 10): List<SetEntity>

    @Query("DELETE FROM sets WHERE workout_id = :workoutId")
    suspend fun deleteByWorkout(workoutId: Long)

    @Query(
        """
        SELECT e.category AS category, SUM(s.weight * s.reps) AS totalVolume
        FROM sets s
        INNER JOIN exercises e ON s.exercise_id = e.id
        INNER JOIN workouts w ON s.workout_id = w.id
        WHERE w.date BETWEEN :startDate AND :endDate AND w.is_completed = 1
        GROUP BY e.category
        """
    )
    suspend fun getVolumeByCategory(startDate: String, endDate: String): List<CategoryVolume>

    @Query(
        """
        SELECT s.exercise_id AS exerciseId, e.name AS name,
               MAX(s.weight * (1 + 0.0333 * s.reps)) AS estimated1rm
        FROM sets s
        INNER JOIN exercises e ON s.exercise_id = e.id
        INNER JOIN workouts w ON s.workout_id = w.id
        WHERE w.is_completed = 1
        GROUP BY s.exercise_id
        """
    )
    suspend fun getPersonalRecords(): List<PersonalRecordResult>

    @Query("SELECT * FROM sets WHERE workout_id = :workoutId ORDER BY set_number ASC")
    suspend fun getByWorkoutOnce(workoutId: Long): List<SetEntity>

    @Query("SELECT COUNT(*) FROM sets WHERE workout_id = :workoutId AND exercise_id = :exerciseId")
    suspend fun countSetsForExercise(workoutId: Long, exerciseId: Long): Int

    @Query("SELECT DISTINCT exercise_id FROM sets WHERE workout_id = :workoutId")
    suspend fun getDistinctExerciseIds(workoutId: Long): List<Long>

    @Query("SELECT * FROM sets WHERE workout_id IN (:workoutIds)")
    suspend fun getByWorkoutIds(workoutIds: List<Long>): List<SetEntity>
}
