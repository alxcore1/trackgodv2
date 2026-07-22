package com.trackgod.app.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.trackgod.app.ui.theme.TrackGodTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingRestoreScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsRestoreFilePickerContent() {
        composeRule.setContent {
            TrackGodTheme {
                OnboardingRestoreScreenContent(
                    uiState = OnboardingRestoreUiState(),
                    onNavigateBack = {},
                    onPickFile = {},
                    onRestart = {},
                )
            }
        }

        composeRule.onNodeWithText("FROM BACKUP", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("FULL DATABASE RESTORE :: .DB FILE").assertIsDisplayed()
        composeRule.onNodeWithText("SELECT BACKUP FILE").assertIsDisplayed()
    }

    @Test
    fun loadingDisablesFilePicker() {
        composeRule.setContent {
            TrackGodTheme {
                OnboardingRestoreScreenContent(
                    uiState = OnboardingRestoreUiState(isLoading = true),
                    onNavigateBack = {},
                    onPickFile = {},
                    onRestart = {},
                )
            }
        }

        composeRule.onNodeWithText("RESTORING...").assertIsNotEnabled()
    }

    @Test
    fun failedImportShowsInvalidFileMessage() {
        composeRule.setContent {
            TrackGodTheme {
                OnboardingRestoreScreenContent(
                    uiState = OnboardingRestoreUiState(
                        message = "RESTORE FAILED. SELECT A VALID TRACKGOD V2 .DB BACKUP.",
                    ),
                    onNavigateBack = {},
                    onPickFile = {},
                    onRestart = {},
                )
            }
        }

        composeRule.onNodeWithText("RESTORE FAILED. SELECT A VALID TRACKGOD V2 .DB BACKUP.")
            .assertIsDisplayed()
    }

    @Test
    fun successfulImportShowsRestartCta() {
        var restarted = false

        composeRule.setContent {
            TrackGodTheme {
                OnboardingRestoreScreenContent(
                    uiState = OnboardingRestoreUiState(showRestartRequired = true),
                    onNavigateBack = {},
                    onPickFile = {},
                    onRestart = { restarted = true },
                )
            }
        }

        composeRule.onNodeWithText("RESTART REQUIRED").assertIsDisplayed()
        composeRule.onNodeWithText("RESTART").assertIsDisplayed().assertIsEnabled().performClick()

        assertTrue(restarted)
    }
}
