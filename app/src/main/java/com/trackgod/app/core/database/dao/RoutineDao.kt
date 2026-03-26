package com.trackgod.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trackgod.app.core.database.entity.RoutineEntity
import com.trackgod.app.core.database.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

data class RoutineWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val lastUsedAt: Long?,
    val exerciseCount: Int,
)

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<RoutineExerciseEntity>)

    @Query("""
        SELECT r.id, r.name, r.created_at AS createdAt, r.last_used_at AS lastUsedAt,
               COUNT(re.id) AS exerciseCount
        FROM routines r
        LEFT JOIN routine_exercises re ON re.routine_id = r.id
        GROUP BY r.id
        ORDER BY r.last_used_at DESC, r.created_at DESC
    """)
    fun getAllWithCount(): Flow<List<RoutineWithCount>>

    @Query("SELECT * FROM routine_exercises WHERE routine_id = :routineId ORDER BY sort_order ASC")
    suspend fun getExercisesForRoutine(routineId: Long): List<RoutineExerciseEntity>

    @Query("UPDATE routines SET last_used_at = :timestamp WHERE id = :routineId")
    suspend fun updateLastUsed(routineId: Long, timestamp: Long)

    @Query("UPDATE routines SET name = :name WHERE id = :routineId")
    suspend fun updateName(routineId: Long, name: String)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)
}
