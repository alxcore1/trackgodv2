package com.trackgod.app.core.database

import android.content.Context
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.repository.ExerciseRepository
import com.trackgod.app.core.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend fun seedIfNeeded() {
        if (settingsRepository.isDatabaseSeeded()) return

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
                    isCustom = false,
                    isActive = true,
                    usageCount = 0,
                    createdAt = now
                )
            )
        }

        exerciseRepository.seedExercises(exercises)
        settingsRepository.setDatabaseSeeded()
    }
}
