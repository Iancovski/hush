package com.iancovski.hush.ui.home

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iancovski.hush.R
import com.iancovski.hush.ui.permissions.PermissionsUiState
import com.iancovski.hush.ui.theme.HushTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented test names use camelCase: spaces in method names need DEX 040 (API 30), and minSdk is 29.
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test
    fun activeProtectionShowsItsTextsAndTurnsOffOnClick() {
        var changedTo: Boolean? = null
        composeRule.setContent {
            HushTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = false, blockingEnabled = true),
                    onBlockingChange = { changedTo = it },
                    onRequestPermissions = {},
                    onPermissionsDialogDismiss = {},
                    onOpenHistory = {},
                    onOpenAllowlist = {},
                    onOpenNotificationSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.home_protection_on_description), substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.home_protection_on_title), substring = true)
            .assertIsOn()
            .performClick()

        assertEquals(false, changedTo)
    }

    @Test
    fun inactiveProtectionShowsItsTextsAndTurnsOnOnClick() {
        var changedTo: Boolean? = null
        composeRule.setContent {
            HushTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = false, blockingEnabled = false),
                    onBlockingChange = { changedTo = it },
                    onRequestPermissions = {},
                    onPermissionsDialogDismiss = {},
                    onOpenHistory = {},
                    onOpenAllowlist = {},
                    onOpenNotificationSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.home_protection_off_description), substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.home_protection_off_title), substring = true)
            .assertIsOff()
            .performClick()

        assertEquals(true, changedTo)
    }

    @Test
    fun historyButtonOpensTheHistory() {
        var opened = false
        composeRule.setContent {
            HushTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = false, blockingEnabled = true),
                    onBlockingChange = {},
                    onRequestPermissions = {},
                    onPermissionsDialogDismiss = {},
                    onOpenHistory = { opened = true },
                    onOpenAllowlist = {},
                    onOpenNotificationSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.history_title)).performClick()

        assertTrue(opened)
    }

    @Test
    fun allowlistButtonOpensTheAllowlist() {
        var opened = false
        composeRule.setContent {
            HushTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = false, blockingEnabled = true),
                    onBlockingChange = {},
                    onRequestPermissions = {},
                    onPermissionsDialogDismiss = {},
                    onOpenHistory = {},
                    onOpenAllowlist = { opened = true },
                    onOpenNotificationSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.allowlist_title)).performClick()

        assertTrue(opened)
    }

    @Test
    fun notificationsButtonOpensTheNotificationSettings() {
        var opened = false
        composeRule.setContent {
            HushTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = false, blockingEnabled = true),
                    onBlockingChange = {},
                    onRequestPermissions = {},
                    onPermissionsDialogDismiss = {},
                    onOpenHistory = {},
                    onOpenAllowlist = {},
                    onOpenNotificationSettings = { opened = true },
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.notification_settings_title)).performClick()

        assertTrue(opened)
    }

    @Test
    fun blockedCallsCounterShowsTheTotalAndOpensTheHistory() {
        var opened = false
        composeRule.setContent {
            HushTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = false, blockingEnabled = true, blockedPerDay = listOf(2, 5, 1, 4, 0, 9, 3)),
                    onBlockingChange = {},
                    onRequestPermissions = {},
                    onPermissionsDialogDismiss = {},
                    onOpenHistory = { opened = true },
                    onOpenAllowlist = {},
                    onOpenNotificationSettings = {},
                )
            }
        }

        composeRule.onNodeWithText("24", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.home_blocked_calls_title), substring = true).performClick()

        assertTrue(opened)
    }

    @Test
    fun aMissingPermissionShowsProtectionInactive() {
        composeRule.setContent {
            HushTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        isLoading = false,
                        blockingEnabled = true,
                        blockedPerDay = List(7) { 0 },
                        permissions = PermissionsUiState(callScreeningRoleHeld = true, contactsGranted = false),
                    ),
                    onBlockingChange = {},
                    onRequestPermissions = {},
                    onPermissionsDialogDismiss = {},
                    onOpenHistory = {},
                    onOpenAllowlist = {},
                    onOpenNotificationSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.home_protection_missing_permissions), substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.home_protection_off_title), substring = true).assertIsOff()
    }

    @Test
    fun theDialogListsTheMissingPermissionsAndAsksForThem() {
        var requests = 0
        composeRule.setContent {
            HushTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        isLoading = false,
                        blockingEnabled = false,
                        blockedPerDay = List(7) { 0 },
                        permissions = PermissionsUiState(callScreeningRoleHeld = false, contactsGranted = true),
                        showPermissionsDialog = true,
                    ),
                    onBlockingChange = {},
                    onRequestPermissions = { requests++ },
                    onPermissionsDialogDismiss = {},
                    onOpenHistory = {},
                    onOpenAllowlist = {},
                    onOpenNotificationSettings = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.home_permissions_dialog_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.permission_call_screening_title), substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.permission_contacts_title), substring = true).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.setup_permissions_grant)).performClick()

        assertEquals(1, requests)
    }
}
