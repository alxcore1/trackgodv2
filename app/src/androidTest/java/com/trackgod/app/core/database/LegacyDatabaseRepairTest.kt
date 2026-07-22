package com.trackgod.app.core.database

import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class LegacyDatabaseRepairTest {

    @Test
    fun repairV1DatabaseImportsMachineCategoriesBeforeEntryCreatedExercises() {
        val file = tempDbFile("v1-repair.db")
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE machines (id INTEGER PRIMARY KEY, name TEXT, brand TEXT, series TEXT, category TEXT, alternative_names TEXT, created_at INTEGER)")
            db.execSQL("CREATE TABLE exercises (id INTEGER PRIMARY KEY, name TEXT, muscle_group TEXT, equipment_type TEXT, created_at INTEGER)")
            db.execSQL("CREATE TABLE workouts (id INTEGER PRIMARY KEY, date TEXT, comment TEXT, start_time INTEGER, end_time INTEGER, duration_calculated INTEGER, created_at INTEGER)")
            db.execSQL("CREATE TABLE entries (id INTEGER PRIMARY KEY, workout_id INTEGER, machine_name TEXT, weight REAL, reps INTEGER, sets INTEGER, note TEXT, created_at INTEGER)")
            db.execSQL("INSERT INTO machines (name, brand, category, created_at) VALUES ('Chest Press', 'Hammer Strength', 'Chest', 1000)")
            db.execSQL("INSERT INTO machines (name, brand, category, created_at) VALUES ('Standing Calf Raise', 'Life Fitness', 'Calves', 1000)")
            db.execSQL("INSERT INTO exercises (name, muscle_group, equipment_type, created_at) VALUES ('Chest Press', 'Upper Body', 'machine', 1000)")
            db.execSQL("INSERT INTO workouts (id, date, comment, start_time, created_at) VALUES (1, '2026-04-20', 'Push', 1000, 1000)")
            db.execSQL("INSERT INTO entries (workout_id, machine_name, weight, reps, sets, created_at) VALUES (1, 'Chest Press', 100.0, 8, 2, 1000)")
            db.execSQL("INSERT INTO entries (workout_id, machine_name, weight, reps, sets, created_at) VALUES (1, 'Standing Calf Raise', 80.0, 12, 1, 1000)")
        }

        LegacyDatabaseRepair.repairIfNeeded(file)

        SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            assertEquals("Chest", categoryFor(db, "Chest Press"))
            assertEquals("Legs", categoryFor(db, "Standing Calf Raise"))
            assertEquals(3, scalarInt(db, "SELECT COUNT(*) FROM sets"))
        }
    }

    @Test
    fun repairCurrentDatabaseNormalizesOtherExercisesFromLeftoverMachines() {
        val file = tempDbFile("current-normalize.db")
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, equipment_type TEXT NOT NULL, brand TEXT, series TEXT, alternative_names TEXT, is_custom INTEGER NOT NULL, is_active INTEGER NOT NULL, usage_count INTEGER NOT NULL, last_used_at INTEGER, created_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, date TEXT NOT NULL, start_time INTEGER NOT NULL, end_time INTEGER, duration_seconds INTEGER, total_volume REAL, notes TEXT, is_completed INTEGER NOT NULL, created_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE sets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, workout_id INTEGER NOT NULL, exercise_id INTEGER NOT NULL, set_number INTEGER NOT NULL, weight REAL NOT NULL, reps INTEGER NOT NULL, rpe INTEGER, rir INTEGER, note TEXT, set_type TEXT NOT NULL DEFAULT 'working', created_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE routines (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, created_at INTEGER NOT NULL, last_used_at INTEGER)")
            db.execSQL("CREATE TABLE machines (id INTEGER PRIMARY KEY, name TEXT, category TEXT)")
            db.execSQL("INSERT INTO exercises (name, category, equipment_type, is_custom, is_active, usage_count, created_at) VALUES ('Chest Press', 'Other', 'machine', 1, 1, 0, 1000)")
            db.execSQL("INSERT INTO exercises (name, category, equipment_type, is_custom, is_active, usage_count, created_at) VALUES ('Standing Calf Raise', 'Calves', 'machine', 1, 1, 0, 1000)")
            db.execSQL("INSERT INTO machines (name, category) VALUES ('Chest Press', 'Chest')")
            db.execSQL("INSERT INTO machines (name, category) VALUES ('Standing Calf Raise', 'Calves')")
        }

        LegacyDatabaseRepair.repairIfNeeded(file)

        SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            assertEquals("Chest", categoryFor(db, "Chest Press"))
            assertEquals("Legs", categoryFor(db, "Standing Calf Raise"))
        }
    }

    private fun tempDbFile(name: String): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return File(context.cacheDir, name).also {
            if (it.exists()) it.delete()
        }
    }

    private fun categoryFor(db: SQLiteDatabase, name: String): String {
        db.rawQuery("SELECT category FROM exercises WHERE name = ?", arrayOf(name)).use { cursor ->
            cursor.moveToFirst()
            return cursor.getString(0)
        }
    }

    private fun scalarInt(db: SQLiteDatabase, sql: String): Int {
        db.rawQuery(sql, null).use { cursor ->
            cursor.moveToFirst()
            return cursor.getInt(0)
        }
    }
}
