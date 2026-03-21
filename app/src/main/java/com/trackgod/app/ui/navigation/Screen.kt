package com.trackgod.app.ui.navigation

/**
 * All navigation destinations in TrackGod v2.
 *
 * Routes are grouped by context:
 * - Top-level (no bottom nav): splash, onboarding, seeding choice, v1 import
 * - Main tabs: altar, workout (history), stats, profile
 * - Workout flow: session, exercise picker, OCR scanner
 * - Profile sub-screens: edit profile, settings, weight loss, etc.
 */
sealed class Screen(val route: String) {

    // ── Top-level (no bottom nav) ────────────────────────────────────────────

    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object SeedingChoice : Screen("seeding_choice")
    data object V1Import : Screen("v1_import")

    // ── Main tabs ────────────────────────────────────────────────────────────

    data object Altar : Screen("altar")
    data object History : Screen("workout")  // matches BottomNavBar route
    data object Stats : Screen("stats")
    data object Profile : Screen("profile")

    // ── Workout flow ─────────────────────────────────────────────────────────

    data object WorkoutSession : Screen("workout_session/{workoutId}") {
        fun create(workoutId: Long) = "workout_session/$workoutId"
    }

    data object ExercisePicker : Screen("exercise_picker")
    data object OcrScanner : Screen("ocr_scanner")

    // ── Profile sub-screens ──────────────────────────────────────────────────

    data object EditProfile : Screen("edit_profile")
    data object Settings : Screen("settings")
    data object WeightLoss : Screen("weight_loss")
    data object PhotoComparison : Screen("photo_comparison")
    data object Backup : Screen("backup")
    data object PrivacyPolicy : Screen("privacy_policy")
}
