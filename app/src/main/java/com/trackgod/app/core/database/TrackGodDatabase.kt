package com.trackgod.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trackgod.app.core.database.converter.Converters
import com.trackgod.app.core.database.dao.BackupDao
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
        BackupMetadataEntity::class
    ],
    version = 2,
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
    }

    abstract fun userProfileDao(): UserProfileDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun setDao(): SetDao
    abstract fun bodyMetricDao(): BodyMetricDao
    abstract fun weightLossDao(): WeightLossDao
    abstract fun backupDao(): BackupDao
}
