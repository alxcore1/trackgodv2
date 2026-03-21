package com.trackgod.app.core.repository

import com.trackgod.app.core.database.dao.ExerciseDao
import com.trackgod.app.core.database.dao.SetDao
import com.trackgod.app.core.database.dao.WorkoutDao
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.database.entity.SetEntity
import com.trackgod.app.core.database.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao,
    private val exerciseDao: ExerciseDao
) {

    suspend fun createWorkout(): Long {
        val now = System.currentTimeMillis()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val workout = WorkoutEntity(
            name = "",
            date = today,
            startTime = now,
            isCompleted = false,
            createdAt = now
        )
        return workoutDao.insert(workout)
    }

    suspend fun getWorkout(id: Long): WorkoutEntity? =
        workoutDao.getById(id)

    suspend fun addSet(
        workoutId: Long,
        exerciseId: Long,
        weight: Float,
        reps: Int,
        note: String? = null,
        rpe: Int? = null,
        rir: Int? = null
    ): Long {
        val setNumber = setDao.countSetsForExercise(workoutId, exerciseId) + 1
        val now = System.currentTimeMillis()
        val set = SetEntity(
            workoutId = workoutId,
            exerciseId = exerciseId,
            setNumber = setNumber,
            weight = weight,
            reps = reps,
            note = note,
            rpe = rpe,
            rir = rir,
            createdAt = now
        )
        val setId = setDao.insert(set)
        exerciseDao.incrementUsageCount(exerciseId, now)
        return setId
    }

    suspend fun updateSet(set: SetEntity) {
        setDao.update(set)
    }

    suspend fun deleteSet(set: SetEntity) {
        setDao.delete(set)
    }

    fun getSetsForWorkout(workoutId: Long): Flow<List<SetEntity>> =
        setDao.getByWorkout(workoutId)

    suspend fun getRecentSetsForExercise(exerciseId: Long): List<SetEntity> =
        setDao.getRecentForExercise(exerciseId)

    suspend fun completeWorkout(workoutId: Long, name: String) {
        val workout = workoutDao.getById(workoutId) ?: return
        val now = System.currentTimeMillis()
        val durationSeconds = ((now - workout.startTime) / 1000).toInt()
        val sets = setDao.getByWorkoutOnce(workoutId)
        val totalVolume = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()

        workoutDao.update(
            workout.copy(
                name = name,
                endTime = now,
                durationSeconds = durationSeconds,
                totalVolume = totalVolume,
                isCompleted = true
            )
        )
    }

    suspend fun getIncompleteWorkout(): WorkoutEntity? =
        workoutDao.getIncompleteWorkout()

    suspend fun deleteWorkout(workoutId: Long) {
        workoutDao.deleteById(workoutId)
    }

    fun getTodayWorkouts(date: String): Flow<List<WorkoutEntity>> =
        workoutDao.getTodayWorkouts(date)

    suspend fun getExercisesInWorkout(workoutId: Long): List<ExerciseEntity> {
        val exerciseIds = setDao.getDistinctExerciseIds(workoutId)
        return exerciseIds.mapNotNull { exerciseDao.getById(it) }
    }
}
