package com.trackgod.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trackgod.app.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity): Long

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Delete
    suspend fun delete(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM user_profile LIMIT 1)")
    suspend fun hasProfile(): Boolean

    @Query("UPDATE user_profile SET weight_unit = :weightUnit")
    suspend fun updateWeightUnit(weightUnit: String)

    @Query("UPDATE user_profile SET height_unit = :heightUnit")
    suspend fun updateHeightUnit(heightUnit: String)
}
