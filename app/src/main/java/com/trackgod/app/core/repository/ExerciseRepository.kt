package com.trackgod.app.core.repository

import com.trackgod.app.core.database.dao.ExerciseDao
import com.trackgod.app.core.database.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao
) {

    fun getAllActive(): Flow<List<ExerciseEntity>> =
        exerciseDao.getAllActive()

    suspend fun getAllActiveSnapshot(): List<ExerciseEntity> =
        exerciseDao.getAllActiveSnapshot()

    fun getByCategory(category: String): Flow<List<ExerciseEntity>> =
        exerciseDao.getByCategory(category)

    fun search(query: String): Flow<List<ExerciseEntity>> =
        exerciseDao.search(query)

    suspend fun getDistinctBrands(): List<String> =
        exerciseDao.getDistinctBrands()

    suspend fun getById(id: Long): ExerciseEntity? =
        exerciseDao.getById(id)

    suspend fun create(
        name: String,
        category: String,
        equipmentType: String,
        brand: String? = null
    ): Long {
        val entity = ExerciseEntity(
            name = name,
            category = category,
            equipmentType = equipmentType,
            brand = brand,
            isCustom = true,
            isActive = true,
            usageCount = 0,
            createdAt = System.currentTimeMillis()
        )
        return exerciseDao.insert(entity)
    }

    suspend fun incrementUsage(id: Long) {
        exerciseDao.incrementUsageCount(id, System.currentTimeMillis())
    }

    suspend fun seedExercises(exercises: List<ExerciseEntity>) {
        exerciseDao.insertAll(exercises)
    }

    suspend fun getCount(): Int =
        exerciseDao.getCount()
}
