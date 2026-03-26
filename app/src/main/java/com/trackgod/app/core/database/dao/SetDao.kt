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
    val estimated1rm: Float,
    val weight: Float,
    val reps: Int
)

data class ExerciseProgressPoint(
    val date: String,
    val maxWeight: Float,
    val estimated1rm: Float,
    val totalVolume: Float,
    val setCount: Int,
)

data class ExerciseFrequencyResult(
    val name: String,
    val count: Int
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
        WHERE w.date BETWEEN :startDate AND :endDate AND w.is_completed = 1 AND s.set_type != 'warmup'
        GROUP BY e.category
        """
    )
    suspend fun getVolumeByCategory(startDate: String, endDate: String): List<CategoryVolume>

    @Query(
        """
        SELECT s.exercise_id AS exerciseId, e.name AS name,
               s.weight * (1 + 0.0333 * s.reps) AS estimated1rm,
               s.weight AS weight, s.reps AS reps
        FROM sets s
        INNER JOIN exercises e ON s.exercise_id = e.id
        INNER JOIN workouts w ON s.workout_id = w.id
        WHERE w.is_completed = 1 AND s.set_type != 'warmup'
          AND s.id = (
              SELECT s2.id FROM sets s2
              INNER JOIN workouts w2 ON s2.workout_id = w2.id
              WHERE s2.exercise_id = s.exercise_id AND w2.is_completed = 1 AND s2.set_type != 'warmup'
              ORDER BY s2.weight * (1 + 0.0333 * s2.reps) DESC
              LIMIT 1
          )
        GROUP BY s.exercise_id
        """
    )
    suspend fun getPersonalRecords(): List<PersonalRecordResult>

    /** Best estimated 1RM for a single exercise across all completed workouts. */
    @Query("""
        SELECT MAX(s.weight * (1 + 0.0333 * s.reps))
        FROM sets s
        INNER JOIN workouts w ON s.workout_id = w.id
        WHERE s.exercise_id = :exerciseId AND w.is_completed = 1 AND s.set_type != 'warmup'
    """)
    suspend fun getBest1RMForExercise(exerciseId: Long): Float?

    /** Per-date progression for a single exercise (for charts). */
    @Query("""
        SELECT w.date AS date,
               MAX(s.weight) AS maxWeight,
               MAX(s.weight * (1 + 0.0333 * s.reps)) AS estimated1rm,
               SUM(s.weight * s.reps) AS totalVolume,
               COUNT(*) AS setCount
        FROM sets s
        INNER JOIN workouts w ON s.workout_id = w.id
        WHERE s.exercise_id = :exerciseId AND w.is_completed = 1 AND s.set_type != 'warmup'
        GROUP BY w.date
        ORDER BY w.date ASC
        LIMIT :limit
    """)
    suspend fun getProgressionForExercise(exerciseId: Long, limit: Int = 30): List<ExerciseProgressPoint>

    @Query("SELECT * FROM sets WHERE workout_id = :workoutId ORDER BY set_number ASC")
    suspend fun getByWorkoutOnce(workoutId: Long): List<SetEntity>

    @Query("DELETE FROM sets WHERE id = :setId")
    suspend fun deleteById(setId: Long)

    @Query("UPDATE sets SET weight = :weight, reps = :reps WHERE id = :setId")
    suspend fun updateWeightAndReps(setId: Long, weight: Float, reps: Int)

    @Query("SELECT COUNT(*) FROM sets WHERE workout_id = :workoutId AND exercise_id = :exerciseId")
    suspend fun countSetsForExercise(workoutId: Long, exerciseId: Long): Int

    @Query("SELECT DISTINCT exercise_id FROM sets WHERE workout_id = :workoutId")
    suspend fun getDistinctExerciseIds(workoutId: Long): List<Long>

    @Query("SELECT * FROM sets WHERE workout_id IN (:workoutIds)")
    suspend fun getByWorkoutIds(workoutIds: List<Long>): List<SetEntity>

    @Query(
        """
        SELECT e.name AS name, COUNT(DISTINCT s.workout_id) AS count
        FROM sets s
        INNER JOIN exercises e ON s.exercise_id = e.id
        INNER JOIN workouts w ON s.workout_id = w.id
        WHERE w.date BETWEEN :startDate AND :endDate AND w.is_completed = 1
        GROUP BY s.exercise_id
        ORDER BY count DESC
        LIMIT :limit
        """
    )
    suspend fun getExerciseFrequency(startDate: String, endDate: String, limit: Int = 8): List<ExerciseFrequencyResult>

    @Query("""
        SELECT w.date AS date, w.name AS workoutName, e.name AS exerciseName,
               s.set_number AS setNumber, s.weight AS weight, s.reps AS reps,
               s.rpe AS rpe, s.rir AS rir, s.note AS note
        FROM sets s
        INNER JOIN workouts w ON s.workout_id = w.id
        INNER JOIN exercises e ON s.exercise_id = e.id
        WHERE w.is_completed = 1
        ORDER BY w.date DESC, w.start_time DESC, e.name ASC, s.set_number ASC
    """)
    suspend fun getAllSetsForCsvExport(): List<CsvExportRow>
}

data class CsvExportRow(
    val date: String,
    val workoutName: String,
    val exerciseName: String,
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val rpe: Int?,
    val rir: Int?,
    val note: String?,
)
