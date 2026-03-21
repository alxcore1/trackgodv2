package com.trackgod.app.core.repository

import com.trackgod.app.core.database.dao.CategoryVolume
import com.trackgod.app.core.database.dao.DateVolume
import com.trackgod.app.core.database.dao.ExerciseDao
import com.trackgod.app.core.database.dao.ExerciseFrequencyResult
import com.trackgod.app.core.database.dao.PersonalRecordResult
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

    suspend fun getRecentCompletedWorkouts(limit: Int = 5): List<WorkoutEntity> =
        workoutDao.getRecentCompleted(limit)

    suspend fun getSetsForWorkoutIds(workoutIds: List<Long>): List<SetEntity> =
        if (workoutIds.isEmpty()) emptyList() else setDao.getByWorkoutIds(workoutIds)

    suspend fun getCompletedWorkoutDates(): List<String> =
        workoutDao.getCompletedWorkoutDates()

    fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>> =
        workoutDao.getAllCompleted()

    suspend fun getCompletedByDate(date: String): List<WorkoutEntity> =
        workoutDao.getCompletedByDate(date)

    suspend fun searchWorkoutsByName(query: String): List<WorkoutEntity> =
        workoutDao.searchByName(query)

    suspend fun updateWorkoutName(workoutId: Long, name: String) {
        workoutDao.updateName(workoutId, name)
    }

    suspend fun getSetsForWorkoutOnce(workoutId: Long): List<SetEntity> =
        setDao.getByWorkoutOnce(workoutId)

    suspend fun getExerciseById(exerciseId: Long): ExerciseEntity? =
        exerciseDao.getById(exerciseId)

    suspend fun getCompletedWorkoutCount(): Int =
        workoutDao.getCompletedWorkoutCount()

    // -- Stats / Analytics queries ------------------------------------------------

    suspend fun getAllCompletedWorkoutsOnce(): List<WorkoutEntity> =
        workoutDao.getAllCompletedOnce()

    suspend fun getVolumeByDate(startDate: String, endDate: String): List<DateVolume> =
        workoutDao.getVolumeByDate(startDate, endDate)

    suspend fun getVolumeByCategory(startDate: String, endDate: String): List<CategoryVolume> =
        setDao.getVolumeByCategory(startDate, endDate)

    suspend fun getPersonalRecords(): List<PersonalRecordResult> =
        setDao.getPersonalRecords()

    suspend fun getExerciseFrequency(startDate: String, endDate: String, limit: Int = 8): List<ExerciseFrequencyResult> =
        setDao.getExerciseFrequency(startDate, endDate, limit)
}
