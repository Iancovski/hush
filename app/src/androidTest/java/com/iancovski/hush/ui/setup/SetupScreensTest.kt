package com.iancovski.hush.ui.setup

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iancovski.hush.R
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.ui.theme.HushTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented test names use camelCase: spaces in method names need DEX 040 (API 30), and minSdk is 29.
@RunWith(AndroidJUnit4::class)
class SetupScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(@StringRes id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    @Test
    fun welcomeShowsTheFirstStepAndStartsTheWizard() {
        var started = false
        composeRule.setContent {
            HushTheme { WelcomeScreen(onStartClick = { started = true }) }
        }

        composeRule.onNodeWithContentDescription(string(R.string.setup_step_indicator, 1, 3)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.setup_welcome_start)).performClick()

        assertTrue(started)
    }

    @Test
    fun missingPermissionsShowTheGrantButton() {
        var grantClicks = 0
        composeRule.setContent {
            HushTheme {
                PermissionsScreen(
                    uiState = PermissionsUiState(),
                    onGrantClick = { grantClicks++ },
                    onContinueClick = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription(string(R.string.setup_step_indicator, 2, 3)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.setup_permissions_grant)).performClick()

        assertEquals(1, grantClicks)
    }

    @Test
    fun blockedPermissionDialogShowsTheSettingsButton() {
        composeRule.setContent {
            HushTheme {
                PermissionsScreen(
                    uiState = PermissionsUiState(callScreeningRoleHeld = true, contactsDialogBlocked = true),
                    onGrantClick = {},
                    onContinueClick = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.setup_permissions_open_settings)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.setup_permissions_settings_hint)).assertIsDisplayed()
    }

    @Test
    fun grantedPermissionsLetTheUserContinue() {
        var continued = false
        composeRule.setContent {
            HushTheme {
                PermissionsScreen(
                    uiState = PermissionsUiState(callScreeningRoleHeld = true, contactsGranted = true),
                    onGrantClick = {},
                    onContinueClick = { continued = true },
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.setup_continue)).performClick()

        assertTrue(continued)
    }

    @Test
    fun notificationOptionsReportThePickedMode() {
        var picked: NotificationMode? = null
        composeRule.setContent {
            HushTheme {
                NotificationsSetupScreen(
                    uiState = NotificationsSetupUiState(),
                    onModeSelect = { picked = it },
                    onFinishClick = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription(string(R.string.setup_step_indicator, 3, 3)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.notification_mode_every_call), substring = true)
            .assertIsSelected()
        composeRule.onNodeWithText(string(R.string.notification_mode_never), substring = true).performClick()

        assertEquals(NotificationMode.NEVER, picked)
    }
}
