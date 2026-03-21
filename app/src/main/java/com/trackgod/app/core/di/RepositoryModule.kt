package com.trackgod.app.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for repository layer.
 *
 * ExerciseRepository, WorkoutRepository, SettingsRepository, and SeedDatabase
 * all use @Inject constructor, so Hilt can provide them automatically without
 * explicit @Provides methods. This module exists as a placeholder for any
 * future repository bindings that may require @Binds or @Provides.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule
