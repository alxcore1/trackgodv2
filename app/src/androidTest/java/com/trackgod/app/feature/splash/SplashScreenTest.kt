package com.trackgod.app.feature.splash

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import com.trackgod.app.ui.theme.TrackGodTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun firstRunShowsOnboardingAndRestoreActions() {
        composeRule.setContent {
            TrackGodTheme {
                SplashScreenContent(
                    isReady = true,
                    hasProfile = false,
                    onEnter = {},
                    onEnterOnboarding = {},
                    onRestoreFromBackup = {},
                    animationsEnabled = false,
                )
            }
        }

        composeRule.onNodeWithText("TAP TO ENTER THE ALTAR").assertIsDisplayed()
        composeRule.onNodeWithText("RESTORE FROM BACKUP").assertIsDisplayed()
        composeRule.onNodeWithText("FULL DATABASE RESTORE :: .DB FILE").assertIsDisplayed()
    }

    @Test
    fun restoreActionOnlyShowsForFirstRunAndInvokesCallback() {
        var restoreClicked = false

        composeRule.setContent {
            TrackGodTheme {
                SplashScreenContent(
                    isReady = true,
                    hasProfile = false,
                    onEnter = {},
                    onEnterOnboarding = {},
                    onRestoreFromBackup = { restoreClicked = true },
                    animationsEnabled = false,
                )
            }
        }

        composeRule.onNodeWithText("RESTORE FROM BACKUP").performClick()

        assertTrue(restoreClicked)

        composeRule.setContent {
            TrackGodTheme {
                SplashScreenContent(
                    isReady = true,
                    hasProfile = true,
                    onEnter = {},
                    onEnterOnboarding = {},
                    onRestoreFromBackup = {},
                    animationsEnabled = false,
                )
            }
        }

        composeRule.onAllNodesWithText("RESTORE FROM BACKUP").assertCountEquals(0)
    }
}
