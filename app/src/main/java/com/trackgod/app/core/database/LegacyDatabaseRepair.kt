package com.trackgod.app.core.database

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File

object LegacyDatabaseRepair {
    private const val TAG = "LegacyDatabaseRepair"
    private const val ROOM_IDENTITY_HASH = "4c9f86cc9ffbfb9981c0bd351d44abcd"

    fun repairIfNeeded(dbFile: File) {
        if (!dbFile.exists() || dbFile.length() == 0L) return

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE,
            )

            val needsRepair = needsV1Repair(db)
            val needsNormalization = needsLegacyCategoryNormalization(db)
            if (!needsRepair && !needsNormalization) return

            db.beginTransaction()
            try {
                if (needsRepair) {
                    repairV1Database(db)
                    db.setVersion(TrackGodDatabase.VERSION)
                    Log.i(TAG, "Repaired legacy TrackGod v1 database before Room open")
                }
                val updated = normalizeLegacyCategories(db)
                db.setTransactionSuccessful()
                if (updated > 0) {
                    Log.i(TAG, "Normalized $updated legacy exercise categories")
                }
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Legacy database repair failed", e)
        } finally {
            db?.close()
        }
    }

    private fun needsV1Repair(db: SQLiteDatabase): Boolean {
        if (!tableExists(db, "entries")) return false
        if (!tableExists(db, "exercises") || !tableExists(db, "workouts")) return false
        return !columnExists(db, "exercises", "category") ||
            !columnExists(db, "workouts", "name") ||
            !tableExists(db, "sets") ||
            !tableExists(db, "routines")
    }

    private fun repairV1Database(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=OFF")

        renameIfExists(db, "user_profile", "legacy_user_profile")
        renameIfExists(db, "exercises", "legacy_exercises")
        renameIfExists(db, "workouts", "legacy_workouts")
        renameIfExists(db, "entries", "legacy_entries")
        renameIfExists(db, "body_metrics", "legacy_body_metrics")
        renameIfExists(db, "weight_loss_goals", "legacy_weight_loss_goals")
        renameIfExists(db, "weight_loss_milestones", "legacy_weight_loss_milestones")

        createCurrentTables(db)
        importLegacyData(db)
        createCurrentIndexes(db)
        writeRoomIdentity(db)
    }

    private fun createCurrentTables(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `avatar_uri` TEXT, `gender` TEXT, `birthday` TEXT, `height` REAL, `weight` REAL, `primary_objective` TEXT, `experience_level` TEXT NOT NULL, `weekly_target` INTEGER NOT NULL, `weight_unit` TEXT NOT NULL, `height_unit` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `equipment_type` TEXT NOT NULL, `brand` TEXT, `series` TEXT, `alternative_names` TEXT, `is_custom` INTEGER NOT NULL, `is_active` INTEGER NOT NULL, `usage_count` INTEGER NOT NULL, `last_used_at` INTEGER, `created_at` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `workouts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `date` TEXT NOT NULL, `start_time` INTEGER NOT NULL, `end_time` INTEGER, `duration_seconds` INTEGER, `total_volume` REAL, `notes` TEXT, `is_completed` INTEGER NOT NULL, `created_at` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `sets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workout_id` INTEGER NOT NULL, `exercise_id` INTEGER NOT NULL, `set_number` INTEGER NOT NULL, `weight` REAL NOT NULL, `reps` INTEGER NOT NULL, `rpe` INTEGER, `rir` INTEGER, `note` TEXT, `set_type` TEXT NOT NULL DEFAULT 'working', `created_at` INTEGER NOT NULL, FOREIGN KEY(`workout_id`) REFERENCES `workouts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`exercise_id`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
        db.execSQL("CREATE TABLE IF NOT EXISTS `body_metrics` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `weight` REAL, `photo_uri` TEXT, `note` TEXT, `created_at` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `weight_loss_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `starting_weight` REAL NOT NULL, `target_weight` REAL NOT NULL, `target_date` TEXT NOT NULL, `weekly_goal` REAL, `motivation_text` TEXT, `reminder_day` INTEGER, `reminder_time` TEXT, `is_active` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `weight_loss_milestones` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goal_id` INTEGER NOT NULL, `target_weight` REAL NOT NULL, `description` TEXT, `is_achieved` INTEGER NOT NULL, `achieved_date` TEXT, `created_at` INTEGER NOT NULL, FOREIGN KEY(`goal_id`) REFERENCES `weight_loss_goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE TABLE IF NOT EXISTS `backup_metadata` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `file_path` TEXT NOT NULL, `file_size` INTEGER NOT NULL, `backup_type` TEXT NOT NULL, `created_at` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `routines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `last_used_at` INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `routine_exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `routine_id` INTEGER NOT NULL, `exercise_id` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, FOREIGN KEY(`routine_id`) REFERENCES `routines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`exercise_id`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
    }

    private fun importLegacyData(db: SQLiteDatabase) {
        val nowExpr = "CAST(strftime('%s','now') AS INTEGER) * 1000"

        if (tableExists(db, "machines")) {
            val brandExpr = if (columnExists(db, "machines", "brand")) "brand" else "NULL"
            val seriesExpr = if (columnExists(db, "machines", "series")) "series" else "NULL"
            val altExpr = if (columnExists(db, "machines", "alternative_names")) "alternative_names" else "NULL"
            val categoryExpr = if (columnExists(db, "machines", "category")) categoryCaseExpr("category") else "'Other'"
            val createdExpr = if (columnExists(db, "machines", "created_at")) {
                "COALESCE(${millisExpr("created_at")}, $nowExpr)"
            } else {
                nowExpr
            }
            db.execSQL(
                """
                INSERT INTO exercises (name, category, equipment_type, brand, series, alternative_names, is_custom, is_active, usage_count, last_used_at, created_at)
                SELECT name, $categoryExpr, 'machine', $brandExpr, $seriesExpr, $altExpr, 0, 1, 0, NULL, $createdExpr
                FROM machines m
                WHERE name IS NOT NULL AND TRIM(name) != ''
                  AND NOT EXISTS (SELECT 1 FROM exercises e WHERE lower(TRIM(e.name)) = lower(TRIM(m.name)))
                """.trimIndent(),
            )
        }

        if (tableExists(db, "legacy_exercises")) {
            val categoryExpr = if (columnExists(db, "legacy_exercises", "muscle_group")) categoryCaseExpr("muscle_group") else "'Other'"
            val equipmentExpr = if (columnExists(db, "legacy_exercises", "equipment_type")) "COALESCE(NULLIF(equipment_type, ''), 'machine')" else "'machine'"
            val createdExpr = if (columnExists(db, "legacy_exercises", "created_at")) {
                "COALESCE(${millisExpr("created_at")}, $nowExpr)"
            } else {
                nowExpr
            }
            db.execSQL(
                """
                INSERT INTO exercises (name, category, equipment_type, brand, series, alternative_names, is_custom, is_active, usage_count, last_used_at, created_at)
                SELECT name, $categoryExpr, $equipmentExpr, NULL, NULL, NULL, 0, 1, 0, NULL, $createdExpr
                FROM legacy_exercises le
                WHERE name IS NOT NULL AND TRIM(name) != ''
                  AND NOT EXISTS (SELECT 1 FROM exercises e WHERE lower(TRIM(e.name)) = lower(TRIM(le.name)))
                """.trimIndent(),
            )
        }

        if (tableExists(db, "legacy_entries")) {
            val sourceCategoryExpr = entryCategorySourceExpr(db)
            db.execSQL(
                """
                INSERT INTO exercises (name, category, equipment_type, brand, series, alternative_names, is_custom, is_active, usage_count, last_used_at, created_at)
                SELECT DISTINCT machine_name, $sourceCategoryExpr,
                       'machine', NULL, NULL, NULL, 1, 1, 0, NULL, $nowExpr
                FROM legacy_entries
                WHERE machine_name IS NOT NULL AND TRIM(machine_name) != ''
                  AND NOT EXISTS (SELECT 1 FROM exercises e WHERE lower(TRIM(e.name)) = lower(TRIM(legacy_entries.machine_name)))
                """.trimIndent(),
            )
        }

        if (tableExists(db, "legacy_workouts")) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO workouts (id, name, date, start_time, end_time, duration_seconds, total_volume, notes, is_completed, created_at)
                SELECT id,
                       COALESCE(NULLIF(comment, ''), 'V1 Workout'),
                       CAST(date AS TEXT),
                       COALESCE(${millisExpr("start_time")}, ${millisExpr("created_at")}, $nowExpr),
                       ${millisExpr("end_time")},
                       duration_calculated,
                       NULL,
                       comment,
                       1,
                       COALESCE(${millisExpr("created_at")}, ${millisExpr("start_time")}, $nowExpr)
                FROM legacy_workouts
                """.trimIndent(),
            )
        }

        if (tableExists(db, "legacy_entries")) {
            db.execSQL(
                """
                WITH RECURSIVE expanded(entry_id, set_number) AS (
                    SELECT id, 1 FROM legacy_entries
                    UNION ALL
                    SELECT expanded.entry_id, expanded.set_number + 1
                    FROM expanded
                    JOIN legacy_entries e ON e.id = expanded.entry_id
                    WHERE expanded.set_number < CASE
                        WHEN e.sets IS NULL OR e.sets < 1 THEN 1
                        WHEN e.sets > 50 THEN 50
                        ELSE e.sets
                    END
                )
                INSERT INTO sets (workout_id, exercise_id, set_number, weight, reps, rpe, rir, note, set_type, created_at)
                SELECT e.workout_id,
                       ex.id,
                       expanded.set_number,
                       COALESCE(e.weight, 0),
                       COALESCE(e.reps, 0),
                       NULL,
                       NULL,
                       CASE WHEN expanded.set_number = 1 THEN e.note ELSE NULL END,
                       'working',
                       COALESCE(${millisExpr("e.created_at")}, $nowExpr)
                FROM expanded
                JOIN legacy_entries e ON e.id = expanded.entry_id
                JOIN exercises ex ON lower(ex.name) = lower(e.machine_name)
                WHERE e.workout_id IN (SELECT id FROM workouts)
                """.trimIndent(),
            )

            db.execSQL(
                """
                UPDATE workouts
                SET total_volume = COALESCE((
                    SELECT SUM(weight * reps)
                    FROM sets
                    WHERE sets.workout_id = workouts.id
                ), 0)
                """.trimIndent(),
            )
        }

        if (tableExists(db, "legacy_body_metrics")) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO body_metrics (id, date, weight, photo_uri, note, created_at)
                SELECT id, CAST(date AS TEXT), weight, photo_uri, note, COALESCE(${millisExpr("created_at")}, $nowExpr)
                FROM legacy_body_metrics
                """.trimIndent(),
            )
        }

        if (tableExists(db, "legacy_user_profile")) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO user_profile (id, name, avatar_uri, gender, birthday, height, weight, primary_objective, experience_level, weekly_target, weight_unit, height_unit, created_at, updated_at)
                SELECT id, name, avatar_uri, gender, CAST(birthday AS TEXT), height, weight, goals, COALESCE(experience_level, 'intermediate'), COALESCE(weekly_target, 4), 'kg', 'cm', COALESCE(${millisExpr("created_at")}, $nowExpr), $nowExpr
                FROM legacy_user_profile
                """.trimIndent(),
            )
        }

        if (tableExists(db, "legacy_weight_loss_goals")) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO weight_loss_goals (id, starting_weight, target_weight, target_date, weekly_goal, motivation_text, reminder_day, reminder_time, is_active, created_at, updated_at)
                SELECT id, starting_weight, target_weight, CAST(target_date AS TEXT), weekly_goal, motivation_text, reminder_day, reminder_time, COALESCE(is_active, 1), COALESCE(${millisExpr("created_at")}, $nowExpr), COALESCE(${millisExpr("updated_at")}, $nowExpr)
                FROM legacy_weight_loss_goals
                """.trimIndent(),
            )
        }

        if (tableExists(db, "legacy_weight_loss_milestones")) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO weight_loss_milestones (id, goal_id, target_weight, description, is_achieved, achieved_date, created_at)
                SELECT id, goal_id, target_weight, description, COALESCE(is_achieved, 0), CAST(achieved_date AS TEXT), COALESCE(${millisExpr("created_at")}, $nowExpr)
                FROM legacy_weight_loss_milestones
                WHERE goal_id IN (SELECT id FROM weight_loss_goals)
                """.trimIndent(),
            )
        }
    }

    private fun entryCategorySourceExpr(db: SQLiteDatabase): String {
        val sources = mutableListOf<String>()
        if (tableExists(db, "machines") && columnExists(db, "machines", "name") && columnExists(db, "machines", "category")) {
            sources += "NULLIF((SELECT ${categoryCaseExpr("category")} FROM machines m WHERE lower(TRIM(m.name)) = lower(TRIM(legacy_entries.machine_name)) LIMIT 1), 'Other')"
        }
        if (tableExists(db, "legacy_exercises") && columnExists(db, "legacy_exercises", "name") && columnExists(db, "legacy_exercises", "muscle_group")) {
            sources += "NULLIF((SELECT ${categoryCaseExpr("muscle_group")} FROM legacy_exercises le WHERE lower(TRIM(le.name)) = lower(TRIM(legacy_entries.machine_name)) LIMIT 1), 'Other')"
        }
        return if (sources.isEmpty()) {
            "'Other'"
        } else {
            "COALESCE(${sources.joinToString(", ")}, 'Other')"
        }
    }

    private fun needsLegacyCategoryNormalization(db: SQLiteDatabase): Boolean {
        if (!tableExists(db, "exercises") || !columnExists(db, "exercises", "category")) return false
        return tableExists(db, "machines") || tableExists(db, "legacy_exercises")
    }

    private fun normalizeLegacyCategories(db: SQLiteDatabase): Int {
        var updated = 0
        if (tableExists(db, "machines") && columnExists(db, "machines", "name") && columnExists(db, "machines", "category")) {
            updated += normalizeCategoriesFromSource(db, "machines", "category")
        }
        if (tableExists(db, "legacy_exercises") && columnExists(db, "legacy_exercises", "name") && columnExists(db, "legacy_exercises", "muscle_group")) {
            updated += normalizeCategoriesFromSource(db, "legacy_exercises", "muscle_group")
        }
        return updated
    }

    private fun normalizeCategoriesFromSource(
        db: SQLiteDatabase,
        sourceTable: String,
        sourceCategoryColumn: String,
    ): Int {
        var updated = 0
        db.rawQuery(
            """
            SELECT e.id AS exercise_id, e.category AS exercise_category, s.`$sourceCategoryColumn` AS source_category
            FROM exercises e
            JOIN `$sourceTable` s ON lower(TRIM(e.name)) = lower(TRIM(s.name))
            """.trimIndent(),
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow("exercise_id"))
                val current = cursor.getStringOrNull("exercise_category")
                val source = cursor.getStringOrNull("source_category")
                val better = LegacyCategoryMapper.betterCategory(source, current) ?: continue
                db.execSQL("UPDATE exercises SET category = ? WHERE id = ?", arrayOf<Any>(better, exerciseId))
                updated++
            }
        }
        return updated
    }

    private fun createCurrentIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_category` ON `exercises` (`category`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_usage_count` ON `exercises` (`usage_count`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_is_active` ON `exercises` (`is_active`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workouts_date` ON `workouts` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workouts_is_completed` ON `workouts` (`is_completed`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sets_workout_id` ON `sets` (`workout_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sets_exercise_id` ON `sets` (`exercise_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sets_workout_id_exercise_id` ON `sets` (`workout_id`, `exercise_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_body_metrics_date` ON `body_metrics` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_loss_goals_is_active` ON `weight_loss_goals` (`is_active`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_loss_milestones_goal_id` ON `weight_loss_milestones` (`goal_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_routine_exercises_routine_id` ON `routine_exercises` (`routine_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_routine_exercises_exercise_id` ON `routine_exercises` (`exercise_id`)")
    }

    private fun writeRoomIdentity(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, '$ROOM_IDENTITY_HASH')")
    }

    private fun millisExpr(column: String): String {
        return """
            CASE
                WHEN $column IS NULL THEN NULL
                WHEN CAST($column AS TEXT) GLOB '[0-9]*' AND CAST($column AS INTEGER) > 4102444800 THEN CAST($column AS INTEGER)
                WHEN CAST($column AS TEXT) GLOB '[0-9]*' THEN CAST($column AS INTEGER) * 1000
                ELSE CAST(strftime('%s', $column) AS INTEGER) * 1000
            END
        """.trimIndent()
    }

    private fun categoryCaseExpr(column: String): String {
        val cases = LegacyCategoryMapper.mappings.entries.joinToString("\n") { (legacy, mapped) ->
            "WHEN lower(TRIM($column)) = '${legacy.sqlLiteral()}' THEN '${mapped.sqlLiteral()}'"
        }
        return """
            CASE
                WHEN $column IS NULL OR TRIM($column) = '' THEN 'Other'
                $cases
                ELSE TRIM($column)
            END
        """.trimIndent()
    }

    private fun String.sqlLiteral(): String = replace("'", "''")

    private fun renameIfExists(db: SQLiteDatabase, table: String, legacyTable: String) {
        if (tableExists(db, table) && !tableExists(db, legacyTable)) {
            db.execSQL("ALTER TABLE `$table` RENAME TO `$legacyTable`")
        }
    }

    private fun tableExists(db: SQLiteDatabase, table: String): Boolean {
        db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=? LIMIT 1", arrayOf(table)).use {
            return it.moveToFirst()
        }
    }

    private fun columnExists(db: SQLiteDatabase, table: String, column: String): Boolean {
        db.rawQuery("PRAGMA table_info(`$table`)", null).use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == column) return true
            }
        }
        return false
    }

    private fun android.database.Cursor.getStringOrNull(column: String): String? {
        val index = getColumnIndex(column)
        if (index == -1 || isNull(index)) return null
        return getString(index)
    }
}
