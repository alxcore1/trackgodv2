package com.trackgod.app.core.repository

import com.trackgod.app.core.database.dao.UserProfileDao
import com.trackgod.app.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
) {

    fun getProfile(): Flow<UserProfileEntity?> =
        userProfileDao.getProfile()

    suspend fun getProfileOnce(): UserProfileEntity? =
        userProfileDao.getProfileOnce()

    suspend fun hasProfile(): Boolean =
        userProfileDao.hasProfile()

    suspend fun createProfile(entity: UserProfileEntity): Long {
        val existing = userProfileDao.getProfileOnce()
        return if (existing == null) {
            userProfileDao.insert(entity)
        } else {
            userProfileDao.update(
                entity.copy(
                    id = existing.id,
                    createdAt = existing.createdAt,
                ),
            )
            existing.id
        }
    }

    suspend fun updateProfile(entity: UserProfileEntity) =
        userProfileDao.update(entity)

    suspend fun updateWeightUnit(unit: String) =
        userProfileDao.updateWeightUnit(unit)

    suspend fun updateHeightUnit(unit: String) =
        userProfileDao.updateHeightUnit(unit)
}
