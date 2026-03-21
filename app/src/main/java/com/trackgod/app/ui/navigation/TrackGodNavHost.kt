package com.trackgod.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trackgod.app.feature.altar.AltarScreen
import com.trackgod.app.feature.backup.BackupScreen
import com.trackgod.app.feature.history.HistoryScreen
import com.trackgod.app.feature.onboarding.OnboardingScreen
import com.trackgod.app.feature.onboarding.SeedingChoiceScreen
import com.trackgod.app.feature.onboarding.SeedingChoiceViewModel
import com.trackgod.app.feature.onboarding.V1ImportScreen
import com.trackgod.app.feature.profile.EditProfileScreen
import com.trackgod.app.feature.profile.PrivacyPolicyScreen
import com.trackgod.app.feature.profile.ProfileScreen
import com.trackgod.app.feature.profile.SettingsScreen
import com.trackgod.app.feature.splash.SplashScreen
import com.trackgod.app.feature.stats.StatsScreen
import com.trackgod.app.feature.weightloss.PhotoComparisonScreen
import com.trackgod.app.feature.weightloss.WeightLossScreen
import com.trackgod.app.feature.weightloss.WeightLossViewModel
import com.trackgod.app.feature.ocr.OcrScannerScreen
import com.trackgod.app.feature.workout.picker.ExercisePickerScreen
import com.trackgod.app.feature.workout.session.WorkoutSessionScreen
import com.trackgod.app.ui.component.BottomNavBar

/** Routes where the bottom navigation bar should be visible. */
private val mainTabRoutes = setOf(
    Screen.Altar.route,
    Screen.History.route,
    Screen.Stats.route,
    Screen.Profile.route,
)

/**
 * Root navigation host that combines a [NavHost] with the [BottomNavBar].
 *
 * The bottom bar is only shown on main-tab destinations; it hides
 * automatically for flows like splash, onboarding, or workout session.
 */
@Composable
fun TrackGodNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in mainTabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentRoute = currentRoute ?: Screen.Altar.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop back to the altar (graph root) so the back stack
                            // never grows beyond one entry per tab.
                            popUpTo(Screen.Altar.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = { fadeOut(animationSpec = tween(200)) },
        ) {
            // Splash / entry screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onEnter = {
                        navController.navigate(Screen.Altar.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onEnterOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Onboarding flow ──────────────────────────────────────────────
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(Screen.SeedingChoice.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(Screen.SeedingChoice.route) {
                val viewModel: SeedingChoiceViewModel =
                    androidx.hilt.navigation.compose.hiltViewModel()
                SeedingChoiceScreen(
                    seedDatabase = viewModel.seedDatabase,
                    onComplete = {
                        navController.navigate(Screen.Altar.route) {
                            popUpTo(Screen.SeedingChoice.route) { inclusive = true }
                        }
                    },
                    onNavigateToV1Import = {
                        navController.navigate(Screen.V1Import.route)
                    },
                )
            }

            composable(Screen.V1Import.route) {
                V1ImportScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ── Main tabs ───────────────────────────────────────────────────
            composable(Screen.Altar.route) {
                AltarScreen(
                    onStartWorkout = { workoutId ->
                        navController.navigate(Screen.WorkoutSession.create(workoutId))
                    },
                    onResumeWorkout = { workoutId ->
                        navController.navigate(Screen.WorkoutSession.create(workoutId))
                    },
                    onWorkoutTap = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Altar.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToWeightLoss = {
                        navController.navigate(Screen.WeightLoss.route)
                    },
                    onNavigateToBackup = {
                        navController.navigate(Screen.Backup.route)
                    },
                    onNavigateToPrivacyPolicy = {
                        navController.navigate(Screen.PrivacyPolicy.route)
                    },
                )
            }

            // ── Workout session ─────────────────────────────────────────────
            composable(
                route = Screen.WorkoutSession.route,
                arguments = listOf(
                    navArgument("workoutId") { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: return@composable
                WorkoutSessionScreen(
                    workoutId = workoutId,
                    onNavigateToExercisePicker = {
                        navController.navigate(Screen.ExercisePicker.route)
                    },
                    onNavigateBack = {
                        navController.navigate(Screen.Altar.route) {
                            popUpTo(Screen.Altar.route) { inclusive = true }
                        }
                    },
                    onWorkoutComplete = {
                        navController.navigate(Screen.Altar.route) {
                            popUpTo(Screen.Altar.route) { inclusive = true }
                        }
                    },
                    onWorkoutDiscarded = {
                        navController.navigate(Screen.Altar.route) {
                            popUpTo(Screen.Altar.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Exercise picker ─────────────────────────────────────────────
            composable(Screen.ExercisePicker.route) {
                ExercisePickerScreen(
                    onExerciseSelected = { exercise ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedExerciseId", exercise.id)
                        navController.popBackStack()
                    },
                    onDismiss = { navController.popBackStack() },
                )
            }

            // ── OCR Scanner ───────────────────────────────────────────────
            composable(Screen.OcrScanner.route) {
                OcrScannerScreen(
                    onExerciseSelected = { exerciseId ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedExerciseId", exerciseId)
                        navController.popBackStack()
                    },
                    onDismiss = { navController.popBackStack() },
                )
            }

            // ── Profile sub-screens ─────────────────────────────────────────
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.WeightLoss.route) {
                WeightLossScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPhotoComparison = {
                        navController.navigate(Screen.PhotoComparison.route)
                    },
                )
            }
            composable(Screen.PhotoComparison.route) {
                // Share the WeightLossViewModel with the parent route so
                // the comparison screen sees the same progress photos.
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Screen.WeightLoss.route)
                }
                val viewModel: WeightLossViewModel =
                    androidx.hilt.navigation.compose.hiltViewModel(parentEntry)
                val state by viewModel.state.collectAsStateWithLifecycle()

                PhotoComparisonScreen(
                    photos = state.progressPhotos,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Backup.route) {
                BackupScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
