package com.trackgod.app.core.repository

import com.trackgod.app.core.database.dao.RoutineDao
import com.trackgod.app.core.database.dao.RoutineWithCount
import com.trackgod.app.core.database.dao.SetDao
import com.trackgod.app.core.database.entity.RoutineEntity
import com.trackgod.app.core.database.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val setDao: SetDao,
) {

    fun getAllWithCount(): Flow<List<RoutineWithCount>> =
        routineDao.getAllWithCount()

    suspend fun getExercisesForRoutine(routineId: Long): List<RoutineExerciseEntity> =
        routineDao.getExercisesForRoutine(routineId)

    /**
     * Create a routine from a completed workout's exercises.
     * Captures the exercise order as used in the workout.
     */
    suspend fun createFromWorkout(workoutId: Long, name: String): Long {
        val now = System.currentTimeMillis()
        val routineId = routineDao.insertRoutine(
            RoutineEntity(name = name, createdAt = now)
        )

        val exerciseIds = setDao.getDistinctExerciseIds(workoutId)
        val routineExercises = exerciseIds.mapIndexed { index, exerciseId ->
            RoutineExerciseEntity(
                routineId = routineId,
                exerciseId = exerciseId,
                sortOrder = index,
            )
        }
        routineDao.insertExercises(routineExercises)

        return routineId
    }

    suspend fun updateLastUsed(routineId: Long) {
        routineDao.updateLastUsed(routineId, System.currentTimeMillis())
    }

    suspend fun rename(routineId: Long, name: String) {
        routineDao.updateName(routineId, name)
    }

    suspend fun delete(routineId: Long) {
        routineDao.deleteRoutine(routineId)
    }
}
