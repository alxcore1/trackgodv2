package com.trackgod.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trackgod.app.feature.altar.AltarScreen
import com.trackgod.app.feature.history.HistoryScreen
import com.trackgod.app.feature.profile.ProfileScreen
import com.trackgod.app.feature.splash.SplashScreen
import com.trackgod.app.feature.stats.StatsScreen
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
        ) {
            // Splash / entry screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onEnter = {
                        navController.navigate(Screen.Altar.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            }

            // Main tabs
            composable(Screen.Altar.route) { AltarScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}
