package com.trackgod.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trackgod.app.core.database.converter.Converters
import com.trackgod.app.core.database.dao.BackupDao
import com.trackgod.app.core.database.dao.RoutineDao
import com.trackgod.app.core.database.dao.BodyMetricDao
import com.trackgod.app.core.database.dao.ExerciseDao
import com.trackgod.app.core.database.dao.SetDao
import com.trackgod.app.core.database.dao.UserProfileDao
import com.trackgod.app.core.database.dao.WeightLossDao
import com.trackgod.app.core.database.dao.WorkoutDao
import com.trackgod.app.core.database.entity.BackupMetadataEntity
import com.trackgod.app.core.database.entity.BodyMetricEntity
import com.trackgod.app.core.database.entity.ExerciseEntity
import com.trackgod.app.core.database.entity.SetEntity
import com.trackgod.app.core.database.entity.UserProfileEntity
import com.trackgod.app.core.database.entity.WeightLossGoalEntity
import com.trackgod.app.core.database.entity.WeightLossMilestoneEntity
import com.trackgod.app.core.database.entity.RoutineEntity
import com.trackgod.app.core.database.entity.RoutineExerciseEntity
import com.trackgod.app.core.database.entity.WorkoutEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ExerciseEntity::class,
        WorkoutEntity::class,
        SetEntity::class,
        BodyMetricEntity::class,
        WeightLossGoalEntity::class,
        WeightLossMilestoneEntity::class,
        BackupMetadataEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TrackGodDatabase : RoomDatabase() {

    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercises ADD COLUMN series TEXT DEFAULT NULL")
            }
        }
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sets ADD COLUMN set_type TEXT NOT NULL DEFAULT 'working'")
            }
        }
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS routines (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        last_used_at INTEGER
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS routine_exercises (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        routine_id INTEGER NOT NULL,
                        exercise_id INTEGER NOT NULL,
                        sort_order INTEGER NOT NULL,
                        FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE,
                        FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_routine_exercises_routine_id ON routine_exercises(routine_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_routine_exercises_exercise_id ON routine_exercises(exercise_id)")
            }
        }
    }

    abstract fun userProfileDao(): UserProfileDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun setDao(): SetDao
    abstract fun bodyMetricDao(): BodyMetricDao
    abstract fun weightLossDao(): WeightLossDao
    abstract fun backupDao(): BackupDao
    abstract fun routineDao(): RoutineDao
}
