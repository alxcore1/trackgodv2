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

    suspend fun createProfile(entity: UserProfileEntity): Long =
        userProfileDao.insert(entity)

    suspend fun updateProfile(entity: UserProfileEntity) =
        userProfileDao.update(entity)
}
