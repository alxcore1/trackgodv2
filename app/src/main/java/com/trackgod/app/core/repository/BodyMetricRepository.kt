package com.trackgod.app.core.repository

import com.trackgod.app.core.database.dao.BodyMetricDao
import com.trackgod.app.core.database.entity.BodyMetricEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyMetricRepository @Inject constructor(
    private val bodyMetricDao: BodyMetricDao,
) {

    fun getAll(): Flow<List<BodyMetricEntity>> =
        bodyMetricDao.getAll()

    suspend fun getLatest(): BodyMetricEntity? =
        bodyMetricDao.getLatest()

    suspend fun logWeighIn(
        weight: Float,
        note: String?,
        photoUri: String?,
    ): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val entity = BodyMetricEntity(
            date = dateFormat.format(Date()),
            weight = weight,
            note = note,
            photoUri = photoUri,
            createdAt = System.currentTimeMillis(),
        )
        return bodyMetricDao.insert(entity)
    }

    fun getWeightHistory(limit: Int = 30): Flow<List<BodyMetricEntity>> =
        bodyMetricDao.getAll()

    fun getProgressPhotos(limit: Int = 20): Flow<List<BodyMetricEntity>> =
        bodyMetricDao.getProgressPhotos(limit)

    /**
     * Save a progress photo (no weight) as a body-metric entry.
     */
    suspend fun addProgressPhoto(photoUri: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val entity = BodyMetricEntity(
            date = dateFormat.format(Date()),
            weight = null,
            note = null,
            photoUri = photoUri,
            createdAt = System.currentTimeMillis(),
        )
        return bodyMetricDao.insert(entity)
    }

    suspend fun deleteMetric(metric: BodyMetricEntity) =
        bodyMetricDao.delete(metric)
}
