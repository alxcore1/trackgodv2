package com.trackgod.app.core.database

import android.content.Context
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.repository.ExerciseRepository
import com.trackgod.app.core.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository
) {
    private val seedMutex = Mutex()

    suspend fun seedIfNeeded() {
        seedMutex.withLock {
            if (settingsRepository.isDatabaseSeeded()) return
            val exercises = loadExercisesFromAssets()
            exerciseRepository.seedExercises(exercises)
            settingsRepository.setDatabaseSeeded()
        }
    }

    /**
     * Seed only non-machine exercises (barbell, dumbbell, bodyweight, cable, other).
     */
    suspend fun seedBasicsOnly() {
        seedMutex.withLock {
            if (settingsRepository.isDatabaseSeeded()) return
            val exercises = loadExercisesFromAssets()
                .filter { it.equipmentType != "machine" }
            exerciseRepository.seedExercises(exercises)
            settingsRepository.setDatabaseSeeded()
        }
    }

    /**
     * Mark the database as seeded without inserting any exercises.
     * Used for the "Empty Slate" option during onboarding.
     */
    suspend fun markAsSeeded() {
        settingsRepository.setDatabaseSeeded()
    }

    /**
     * Seed non-machine exercises (basics) plus machines from the selected brands only.
     * Machines from non-selected brands are NOT inserted.
     */
    suspend fun seedWithBrands(brands: Set<String>) {
        seedMutex.withLock {
            if (settingsRepository.isDatabaseSeeded()) return@withLock
            val allExercises = loadExercisesFromAssets()
            val toSeed = allExercises.filter { exercise ->
                exercise.equipmentType != "machine" ||
                    brands.contains(exercise.brand)
            }
            exerciseRepository.seedExercises(toSeed)
            settingsRepository.setSelectedBrands(brands)
            settingsRepository.setDatabaseSeeded()
        }
    }

    /**
     * Add a brand after initial seeding: inserts any missing machine exercises
     * for this brand and activates all exercises of this brand.
     */
    suspend fun addBrand(brand: String) {
        seedMutex.withLock {
            val allExercises = loadExercisesFromAssets()
            val brandMachines = allExercises.filter {
                it.equipmentType == "machine" && it.brand == brand
            }

            // Find which exercises are already in the DB (by name) to avoid duplicates
            val existingNames = exerciseRepository.getNamesByBrand(brand).toSet()
            val newExercises = brandMachines.filter { it.name !in existingNames }

            if (newExercises.isNotEmpty()) {
                exerciseRepository.seedExercises(newExercises)
            }

            // Re-activate any previously deactivated exercises for this brand
            exerciseRepository.activateByBrand(brand)

            settingsRepository.addSelectedBrand(brand)
        }
    }

    /**
     * Remove a brand: deactivates (does NOT delete) all exercises of this brand
     * and removes it from the persisted selection.
     */
    suspend fun removeBrand(brand: String) {
        seedMutex.withLock {
            exerciseRepository.deactivateByBrand(brand)
            settingsRepository.removeSelectedBrand(brand)
        }
    }

    /**
     * Returns all unique brands from the seed JSON with their exercise counts,
     * sorted alphabetically. Used by the brand-picker UI.
     */
    suspend fun getAvailableBrands(): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val allExercises = loadExercisesFromAssets()
        allExercises
            .filter { it.equipmentType == "machine" && !it.brand.isNullOrBlank() }
            .groupBy { it.brand!! }
            .map { (brand, exercises) -> brand to exercises.size }
            .sortedByDescending { it.second }
    }

    /** Remove duplicate exercises that snuck in from double-seeding. */
    suspend fun removeDuplicates() {
        exerciseRepository.removeDuplicates()
    }

    private fun loadExercisesFromAssets(): List<ExerciseEntity> {
        val json = context.assets.open("exercises_seed.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(json)
        val now = System.currentTimeMillis()
        val exercises = mutableListOf<ExerciseEntity>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            exercises.add(
                ExerciseEntity(
                    name = obj.getString("name"),
                    category = obj.getString("category"),
                    equipmentType = obj.getString("equipmentType"),
                    brand = if (obj.has("brand")) obj.getString("brand") else null,
                    series = if (obj.has("series")) obj.getString("series") else null,
                    isCustom = false,
                    isActive = true,
                    usageCount = 0,
                    createdAt = now
                )
            )
        }

        return exercises
    }
}
