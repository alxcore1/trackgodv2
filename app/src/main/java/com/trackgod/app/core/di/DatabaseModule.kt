package com.trackgod.app.core.di

import android.content.Context
import androidx.room.Room
import com.trackgod.app.core.database.TrackGodDatabase
import com.trackgod.app.core.database.dao.BackupDao
import com.trackgod.app.core.database.dao.BodyMetricDao
import com.trackgod.app.core.database.dao.ExerciseDao
import com.trackgod.app.core.database.dao.SetDao
import com.trackgod.app.core.database.dao.UserProfileDao
import com.trackgod.app.core.database.dao.WeightLossDao
import com.trackgod.app.core.database.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TrackGodDatabase {
        return Room.databaseBuilder(
            context,
            TrackGodDatabase::class.java,
            "trackgod.db"
        ).addMigrations(TrackGodDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideUserProfileDao(database: TrackGodDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideExerciseDao(database: TrackGodDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    fun provideWorkoutDao(database: TrackGodDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideSetDao(database: TrackGodDatabase): SetDao {
        return database.setDao()
    }

    @Provides
    fun provideBodyMetricDao(database: TrackGodDatabase): BodyMetricDao {
        return database.bodyMetricDao()
    }

    @Provides
    fun provideWeightLossDao(database: TrackGodDatabase): WeightLossDao {
        return database.weightLossDao()
    }

    @Provides
    fun provideBackupDao(database: TrackGodDatabase): BackupDao {
        return database.backupDao()
    }
}
