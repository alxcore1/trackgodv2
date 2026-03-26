package com.trackgod.app.core.repository

import com.trackgod.app.core.database.dao.WeightLossDao
import com.trackgod.app.core.database.entity.WeightLossGoalEntity
import com.trackgod.app.core.database.entity.WeightLossMilestoneEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightLossRepository @Inject constructor(
    private val weightLossDao: WeightLossDao,
) {

    fun getActiveGoal(): Flow<WeightLossGoalEntity?> =
        weightLossDao.getActiveGoal()

    suspend fun createGoal(
        startWeight: Float,
        targetWeight: Float,
        targetDate: String,
        weeklyGoal: Float?,
        motivation: String?,
    ): Long {
        val now = System.currentTimeMillis()
        val goal = WeightLossGoalEntity(
            startingWeight = startWeight,
            targetWeight = targetWeight,
            targetDate = targetDate,
            weeklyGoal = weeklyGoal,
            motivationText = motivation,
            isActive = true,
            createdAt = now,
            updatedAt = now,
        )
        // Deactivate any existing active goals first
        weightLossDao.deactivateAllGoals()
        return weightLossDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: WeightLossGoalEntity) =
        weightLossDao.updateGoal(goal)

    suspend fun deactivateGoal(goalId: Long) {
        val goal = weightLossDao.getActiveGoal().firstOrNull() ?: return
        if (goal.id == goalId) {
            weightLossDao.updateGoal(
                goal.copy(
                    isActive = false,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    fun getMilestones(goalId: Long): Flow<List<WeightLossMilestoneEntity>> =
        weightLossDao.getMilestones(goalId)

    suspend fun createMilestone(
        goalId: Long,
        targetWeight: Float,
        description: String?,
    ): Long {
        val milestone = WeightLossMilestoneEntity(
            goalId = goalId,
            targetWeight = targetWeight,
            description = description,
            createdAt = System.currentTimeMillis(),
        )
        return weightLossDao.insertMilestone(milestone)
    }

    suspend fun updateMilestone(milestone: WeightLossMilestoneEntity) =
        weightLossDao.updateMilestone(milestone)

    suspend fun deleteMilestone(milestone: WeightLossMilestoneEntity) =
        weightLossDao.deleteMilestone(milestone)
}
