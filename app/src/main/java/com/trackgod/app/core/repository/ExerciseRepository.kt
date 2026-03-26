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

    suspend fun getRecentlyUsed(limit: Int = 6): List<ExerciseEntity> =
        exerciseDao.getRecentlyUsed(limit)

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

    suspend fun rename(id: Long, newName: String) {
        val exercise = exerciseDao.getById(id) ?: return
        exerciseDao.update(exercise.copy(name = newName))
    }

    suspend fun hide(id: Long) {
        exerciseDao.deactivate(id)
    }

    suspend fun incrementUsage(id: Long) {
        exerciseDao.incrementUsageCount(id, System.currentTimeMillis())
    }

    suspend fun seedExercises(exercises: List<ExerciseEntity>) {
        exerciseDao.insertAll(exercises)
    }

    suspend fun removeDuplicates(): Int =
        exerciseDao.removeDuplicates()

    suspend fun getCount(): Int =
        exerciseDao.getCount()

    /**
     * One-time migration: parse machine exercise names to extract series.
     * E.g., "Hammer Strength MTS Iso-Lateral Front Lat Pulldown"
     * → brand="Hammer Strength", series="MTS", name stays the same (for compatibility)
     */
    suspend fun populateSeriesFromNames() {
        val machines = exerciseDao.getAllMachinesOnce()
        val knownSeries = mapOf(
            "Hammer Strength" to listOf("MTS", "Select", "Plate-Loaded", "HD Elite"),
            "Life Fitness" to listOf("Insignia", "Signature", "Optima", "Circuit", "Axiom"),
            "Gym80" to listOf("Sygnum", "Pure Kraft", "Plate Loaded"),
        )

        for (exercise in machines) {
            val brand = exercise.brand ?: continue
            val seriesList = knownSeries[brand] ?: continue
            val nameLower = exercise.name.lowercase()

            // Find which series matches in the name
            val matchedSeries = seriesList.firstOrNull { series ->
                nameLower.contains(series.lowercase())
            }

            if (matchedSeries != null && exercise.series == null) {
                exerciseDao.updateSeries(exercise.id, matchedSeries)
            }
        }

        // Deduplicate: when a machine exists both with and without series extracted,
        // keep the one with series and deactivate the duplicate.
        deduplicateMachines(machines)
    }

    /**
     * Deactivate duplicate machine exercises where one has series extracted
     * and the other still has the series baked into the name.
     * E.g., "Insignia Back Extension" (no series) vs "Back Extension" (series=Insignia)
     */
    private suspend fun deduplicateMachines(machines: List<ExerciseEntity>) {
        // Reload after series population
        val allMachines = exerciseDao.getAllMachinesOnce()
        val byBrand = allMachines.groupBy { it.brand ?: "" }

        for ((brand, exercises) in byBrand) {
            if (brand.isBlank()) continue
            val withSeries = exercises.filter { it.series != null }
            val withoutSeries = exercises.filter { it.series == null }

            for (clean in withSeries) {
                // The duplicate has the series prefix still in the name
                val seriesPrefix = clean.series ?: continue
                for (dirty in withoutSeries) {
                    val dirtyNameClean = dirty.name.lowercase()
                        .removePrefix(brand.lowercase()).trim()
                    val cleanNameClean = clean.name.lowercase()
                        .removePrefix(brand.lowercase()).trim()
                        .removePrefix(seriesPrefix.lowercase()).trim()

                    // "insignia back extension" vs "back extension"
                    if (dirtyNameClean.removePrefix(seriesPrefix.lowercase()).trim() == cleanNameClean) {
                        // Transfer usage count to the clean version, deactivate dirty
                        if (dirty.usageCount > 0 && clean.usageCount == 0) {
                            exerciseDao.transferUsage(clean.id, dirty.usageCount, dirty.lastUsedAt)
                        }
                        exerciseDao.deactivate(dirty.id)
                    }
                }
            }
        }
    }
}
