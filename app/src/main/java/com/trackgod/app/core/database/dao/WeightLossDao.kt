package com.trackgod.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trackgod.app.core.database.entity.WeightLossGoalEntity
import com.trackgod.app.core.database.entity.WeightLossMilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLossDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: WeightLossGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: WeightLossGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: WeightLossGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: WeightLossMilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: WeightLossMilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: WeightLossMilestoneEntity)

    @Query("UPDATE weight_loss_goals SET is_active = 0 WHERE is_active = 1")
    suspend fun deactivateAllGoals()

    @Query("SELECT * FROM weight_loss_goals WHERE is_active = 1 LIMIT 1")
    fun getActiveGoal(): Flow<WeightLossGoalEntity?>

    @Query("SELECT * FROM weight_loss_milestones WHERE goal_id = :goalId ORDER BY target_weight DESC")
    fun getMilestones(goalId: Long): Flow<List<WeightLossMilestoneEntity>>

    @Query("SELECT * FROM weight_loss_goals ORDER BY created_at DESC")
    fun getAllGoals(): Flow<List<WeightLossGoalEntity>>
}
