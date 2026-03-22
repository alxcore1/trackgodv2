package com.trackgod.app.core.repository

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.trackgod.app.core.database.TrackGodDatabase
import com.trackgod.app.core.database.entity.BodyMetricEntity
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.database.entity.SetEntity
import com.trackgod.app.core.database.entity.UserProfileEntity
import com.trackgod.app.core.database.entity.WorkoutEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class ImportResult(
    val success: Boolean,
    val workoutsImported: Int = 0,
    val exercisesImported: Int = 0,
    val setsImported: Int = 0,
    val bodyMetricsImported: Int = 0,
    val profileImported: Boolean = false,
    val error: String? = null,
)

@Singleton
class V1ImportRepository @Inject constructor(
    private val db: TrackGodDatabase,
    @ApplicationContext private val context: Context,
) {

    companion object {
        private val ISO_FORMATS = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
        )

        private val CATEGORY_MAP = mapOf(
            "upper body" to "Chest",
            "lower body" to "Legs",
            "core" to "Core",
            "back" to "Back",
            "chest" to "Chest",
            "shoulders" to "Shoulders",
            "arms" to "Arms",
            "legs" to "Legs",
            "cardio" to "Cardio",
            "full body" to "Full Body",
        )

        private val OBJECTIVE_MAP = mapOf(
            "lose weight" to "Lose Weight",
            "get fit" to "Get Fit",
            "gain muscle" to "Gain Muscle",
            "build muscle" to "Gain Muscle",
            "strength" to "Gain Muscle",
            "weight loss" to "Lose Weight",
            "fitness" to "Get Fit",
        )
    }

    suspend fun importV1Database(sourceUri: Uri): ImportResult = withContext(Dispatchers.IO) {
        var v1Db: SQLiteDatabase? = null
        val tempFile = File(context.cacheDir, "v1_import_temp.db")

        try {
            // 1. Copy to temp location
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext ImportResult(success = false, error = "Cannot read selected file")

            // Validate SQLite header
            if (tempFile.length() < 16) {
                tempFile.delete()
                return@withContext ImportResult(success = false, error = "File is too small to be a database")
            }

            // 2. Open as raw SQLite
            v1Db = SQLiteDatabase.openDatabase(
                tempFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
            )

            // Validate that required v1 tables exist
            if (!tableExists(v1Db, "workouts") || !tableExists(v1Db, "entries")) {
                return@withContext ImportResult(
                    success = false,
                    error = "Not a valid TrackGod v1 database (missing workouts or entries table)",
                )
            }

            val now = System.currentTimeMillis()

            // 3. Import exercises (machines)
            val exerciseNameToId = mutableMapOf<String, Long>()
            var exercisesImported = 0

            if (tableExists(v1Db, "machines")) {
                v1Db.rawQuery("SELECT * FROM machines", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val name = cursor.getStringOrNull("name") ?: continue
                        val category = cursor.getStringOrNull("category") ?: "Other"
                        val brand = cursor.getStringOrNull("brand")
                        val alternativeNames = cursor.getStringOrNull("alternative_names")

                        val mappedCategory = CATEGORY_MAP[category.lowercase()] ?: category

                        val exerciseId = db.exerciseDao().insert(
                            ExerciseEntity(
                                name = name,
                                category = mappedCategory,
                                equipmentType = "machine",
                                brand = brand,
                                alternativeNames = alternativeNames,
                                isCustom = false,
                                isActive = true,
                                createdAt = now,
                            )
                        )
                        exerciseNameToId[name.lowercase()] = exerciseId
                        exercisesImported++
                    }
                }
            }

            // 4. Import workouts and entries
            var workoutsImported = 0
            var setsImported = 0

            // Map v1 workout IDs to v2 workout IDs
            val v1WorkoutIdToV2Id = mutableMapOf<Long, Long>()

            v1Db.rawQuery("SELECT * FROM workouts ORDER BY id ASC", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val v1Id = cursor.getLongOrNull("id") ?: continue
                    val date = cursor.getStringOrNull("date") ?: continue
                    val comment = cursor.getStringOrNull("comment")
                    val startTimeRaw = cursor.getStringOrNull("start_time")
                    val endTimeRaw = cursor.getStringOrNull("end_time")
                    val createdAtRaw = cursor.getStringOrNull("created_at")

                    val startTime = parseTimestamp(startTimeRaw) ?: parseTimestamp(createdAtRaw) ?: now
                    val endTime = parseTimestamp(endTimeRaw)

                    val durationSeconds = if (endTime != null && endTime > startTime) {
                        ((endTime - startTime) / 1000).toInt()
                    } else {
                        null
                    }

                    val v2WorkoutId = db.workoutDao().insert(
                        WorkoutEntity(
                            name = comment?.takeIf { it.isNotBlank() } ?: "V1 Workout",
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            durationSeconds = durationSeconds,
                            isCompleted = true,
                            notes = null,
                            createdAt = startTime,
                        )
                    )

                    v1WorkoutIdToV2Id[v1Id] = v2WorkoutId
                    workoutsImported++
                }
            }

            // 5. Import entries -> expand to individual sets
            v1Db.rawQuery("SELECT * FROM entries ORDER BY id ASC", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val v1WorkoutId = cursor.getLongOrNull("workout_id") ?: continue
                    val machineName = cursor.getStringOrNull("machine_name") ?: continue
                    val weight = cursor.getFloatOrNull("weight") ?: 0f
                    val reps = cursor.getIntOrNull("reps") ?: 0
                    val setsCount = cursor.getIntOrNull("sets") ?: 1
                    val note = cursor.getStringOrNull("note")

                    val v2WorkoutId = v1WorkoutIdToV2Id[v1WorkoutId] ?: continue

                    // Find or create exercise
                    val exerciseId = exerciseNameToId[machineName.lowercase()]
                        ?: run {
                            val newId = db.exerciseDao().insert(
                                ExerciseEntity(
                                    name = machineName,
                                    category = "Other",
                                    equipmentType = "machine",
                                    isCustom = true,
                                    isActive = true,
                                    createdAt = now,
                                )
                            )
                            exerciseNameToId[machineName.lowercase()] = newId
                            exercisesImported++
                            newId
                        }

                    // Expand sets count to individual set rows
                    val expandCount = setsCount.coerceIn(1, 50)
                    for (setNum in 1..expandCount) {
                        db.setDao().insert(
                            SetEntity(
                                workoutId = v2WorkoutId,
                                exerciseId = exerciseId,
                                setNumber = setNum,
                                weight = weight,
                                reps = reps,
                                note = if (setNum == 1) note else null,
                                createdAt = now,
                            )
                        )
                        setsImported++
                    }
                }
            }

            // 6. Update workout totalVolume
            for ((_, v2WorkoutId) in v1WorkoutIdToV2Id) {
                val sets = db.setDao().getByWorkoutOnce(v2WorkoutId)
                val totalVolume = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                val workout = db.workoutDao().getById(v2WorkoutId)
                if (workout != null) {
                    db.workoutDao().update(workout.copy(totalVolume = totalVolume))
                }
            }

            // 7. Import body metrics
            var bodyMetricsImported = 0
            if (tableExists(v1Db, "body_metrics")) {
                v1Db.rawQuery("SELECT * FROM body_metrics", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val date = cursor.getStringOrNull("date") ?: continue
                        val weightVal = cursor.getFloatOrNull("weight")
                        val noteVal = cursor.getStringOrNull("note")
                        val createdAtRaw = cursor.getStringOrNull("created_at")

                        db.bodyMetricDao().insert(
                            BodyMetricEntity(
                                date = date,
                                weight = weightVal,
                                photoUri = null, // v1 URIs won't work on v2 install
                                note = noteVal,
                                createdAt = parseTimestamp(createdAtRaw) ?: now,
                            )
                        )
                        bodyMetricsImported++
                    }
                }
            }

            // 8. Import user profile
            var profileImported = false
            if (tableExists(v1Db, "user_profile")) {
                v1Db.rawQuery("SELECT * FROM user_profile LIMIT 1", null).use { cursor ->
                    if (cursor.moveToFirst()) {
                        val name = cursor.getStringOrNull("name") ?: "Athlete"
                        val gender = cursor.getStringOrNull("gender")
                        val birthday = cursor.getStringOrNull("birthday")
                        val height = cursor.getFloatOrNull("height")
                        val weightVal = cursor.getFloatOrNull("weight")
                        val goals = cursor.getStringOrNull("goals")
                        val weeklyTarget = cursor.getIntOrNull("weekly_target") ?: 4
                        val experienceLevel = cursor.getStringOrNull("experience_level") ?: "intermediate"

                        val primaryObjective = goals?.let { goalsText ->
                            OBJECTIVE_MAP.entries.firstOrNull { (key, _) ->
                                goalsText.lowercase().contains(key)
                            }?.value ?: goalsText
                        }

                        db.userProfileDao().insert(
                            UserProfileEntity(
                                name = name,
                                gender = gender,
                                birthday = birthday,
                                height = height,
                                weight = weightVal,
                                primaryObjective = primaryObjective,
                                experienceLevel = experienceLevel,
                                weeklyTarget = weeklyTarget,
                                weightUnit = "kg",
                                heightUnit = "cm",
                                createdAt = now,
                                updatedAt = now,
                            )
                        )
                        profileImported = true
                    }
                }
            }

            ImportResult(
                success = true,
                workoutsImported = workoutsImported,
                exercisesImported = exercisesImported,
                setsImported = setsImported,
                bodyMetricsImported = bodyMetricsImported,
                profileImported = profileImported,
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                error = e.message ?: "Unknown error during import",
            )
        } finally {
            v1Db?.close()
            tempFile.delete()
        }
    }

    // -- Helpers ------------------------------------------------------------------

    private fun tableExists(db: SQLiteDatabase, tableName: String): Boolean {
        db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName),
        ).use { cursor ->
            return cursor.count > 0
        }
    }

    private fun parseTimestamp(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null

        // Try as epoch milliseconds
        raw.toLongOrNull()?.let { epoch ->
            // If it looks like seconds (< year 2100 in seconds), convert to millis
            return if (epoch < 4_102_444_800L) epoch * 1000 else epoch
        }

        // Try ISO / datetime formats
        for (format in ISO_FORMATS) {
            try {
                return format.parse(raw)?.time
            } catch (_: Exception) {
                // try next format
            }
        }

        return null
    }

    // -- Cursor extension helpers -------------------------------------------------

    private fun Cursor.getStringOrNull(column: String): String? {
        val index = getColumnIndex(column)
        if (index == -1) return null
        return if (isNull(index)) null else getString(index)
    }

    private fun Cursor.getLongOrNull(column: String): Long? {
        val index = getColumnIndex(column)
        if (index == -1) return null
        return if (isNull(index)) null else getLong(index)
    }

    private fun Cursor.getIntOrNull(column: String): Int? {
        val index = getColumnIndex(column)
        if (index == -1) return null
        return if (isNull(index)) null else getInt(index)
    }

    private fun Cursor.getFloatOrNull(column: String): Float? {
        val index = getColumnIndex(column)
        if (index == -1) return null
        return if (isNull(index)) null else getFloat(index)
    }
}
