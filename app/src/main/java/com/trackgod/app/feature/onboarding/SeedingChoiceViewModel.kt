package com.trackgod.app.feature.onboarding

import androidx.lifecycle.ViewModel
import com.trackgod.app.core.database.SeedDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Thin ViewModel that exposes [SeedDatabase] to the SeedingChoiceScreen
 * via Hilt injection. All seeding logic lives in SeedDatabase itself.
 */
@HiltViewModel
class SeedingChoiceViewModel @Inject constructor(
    val seedDatabase: SeedDatabase,
) : ViewModel()
