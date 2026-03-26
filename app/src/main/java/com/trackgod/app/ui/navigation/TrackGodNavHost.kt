package com.trackgod.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.trackgod.app.feature.history.EditWorkoutScreen
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
import com.trackgod.app.ui.component.NavTabs
import kotlinx.coroutines.launch

/** Map tab route strings to pager indices. */
private val tabRouteToIndex = mapOf(
    Screen.Altar.route   to 0,
    Screen.History.route  to 1,
    Screen.Stats.route    to 2,
    Screen.Profile.route  to 3,
)

/** Map pager indices back to tab route strings. */
private val tabIndexToRoute = tabRouteToIndex.entries.associate { (k, v) -> v to k }

/**
 * Root navigation host that combines a [NavHost] with the [BottomNavBar].
 *
 * The four main tabs (Altar, History, Stats, Profile) are hosted inside a
 * [HorizontalPager] so the user can swipe between them. The bottom bar is
 * only shown on the pager destination; it hides automatically for flows
 * like splash, onboarding, or workout session.
 */
@Composable
fun TrackGodNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showBottomNav = currentRoute == Screen.Altar.route

    // Pager state -- persisted so it survives recomposition but lives
    // at the TrackGodNavHost level so BottomNavBar can read it.
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    // Track which page index the bottom nav should highlight.
    // Derived from pager swipes via snapshotFlow below.
    var currentTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // Sync: pager swipe -> update tab index (for BottomNavBar highlight)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentTabIndex = page
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentRoute = tabIndexToRoute[currentTabIndex] ?: Screen.Altar.route,
                    onNavigate = { route ->
                        val targetIndex = tabRouteToIndex[route] ?: 0
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetIndex)
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
                    viewModel = viewModel,
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
                    onImportComplete = {
                        navController.navigate(Screen.Altar.route) {
                            popUpTo(Screen.SeedingChoice.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Main tabs (HorizontalPager) ─────────────────────────────────
            composable(Screen.Altar.route) {
                HorizontalPager(
                    state = pagerState,
                ) { page ->
                    when (page) {
                        0 -> AltarScreen(
                            onStartWorkout = { workoutId ->
                                navController.navigate(Screen.WorkoutSession.create(workoutId))
                            },
                            onStartFromTemplate = { workoutId, routineId ->
                                navController.navigate(Screen.WorkoutSession.create(workoutId, routineId))
                            },
                            onResumeWorkout = { workoutId ->
                                navController.navigate(Screen.WorkoutSession.create(workoutId))
                            },
                            onWorkoutTap = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                        )
                        1 -> HistoryScreen(
                            onEditWorkout = { workoutId ->
                                navController.navigate(Screen.EditWorkout.create(workoutId))
                            },
                        )
                        2 -> StatsScreen()
                        3 -> ProfileScreen(
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
                }
            }

            // ── Workout session ─────────────────────────────────────────────
            composable(
                route = Screen.WorkoutSession.route,
                arguments = listOf(
                    navArgument("workoutId") { type = NavType.LongType },
                    navArgument("routineId") { type = NavType.LongType; defaultValue = -1L },
                ),
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: return@composable

                // Observe exercise picker / OCR result from backstack savedStateHandle
                val selectedExerciseId = backStackEntry.savedStateHandle
                    .getStateFlow<Long?>("selectedExerciseId", null)
                    .collectAsStateWithLifecycle()

                val sessionViewModel: com.trackgod.app.feature.workout.session.WorkoutSessionViewModel =
                    hiltViewModel()
                LaunchedEffect(selectedExerciseId.value) {
                    selectedExerciseId.value?.let { exerciseId ->
                        sessionViewModel.selectExerciseById(exerciseId)
                        backStackEntry.savedStateHandle["selectedExerciseId"] = null
                    }
                }

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
                    onNavigateToOcr = { navController.navigate(Screen.OcrScanner.route) },
                )
            }

            // ── OCR Scanner ───────────────────────────────────────────────
            composable(Screen.OcrScanner.route) {
                OcrScannerScreen(
                    onExerciseSelected = { exerciseId ->
                        // Set result directly on WorkoutSession's savedStateHandle
                        try {
                            navController.getBackStackEntry(Screen.WorkoutSession.route)
                                .savedStateHandle["selectedExerciseId"] = exerciseId
                        } catch (_: IllegalArgumentException) {
                            // Fallback: set on previous entry (ExercisePicker)
                            navController.previousBackStackEntry
                                ?.savedStateHandle?.set("selectedExerciseId", exerciseId)
                        }
                        // Pop both OcrScanner and ExercisePicker in one go
                        navController.popBackStack(
                            route = Screen.ExercisePicker.route,
                            inclusive = true,
                        )
                    },
                    onDismiss = { navController.popBackStack() },
                )
            }

            // ── Edit workout ─────────────────────────────────────────────────
            composable(
                route = Screen.EditWorkout.route,
                arguments = listOf(
                    navArgument("workoutId") { type = NavType.LongType },
                ),
            ) {
                EditWorkoutScreen(
                    onNavigateBack = { navController.popBackStack() },
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
                    try {
                        navController.getBackStackEntry(Screen.WeightLoss.route)
                    } catch (_: IllegalArgumentException) {
                        it // fallback to own entry
                    }
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
