package com.iancovski.hush.ui.notifications

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iancovski.hush.R
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.ui.theme.HushTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented test names use camelCase: spaces in method names need DEX 040 (API 30), and minSdk is 29.
@RunWith(AndroidJUnit4::class)
class NotificationSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun setScreen(
        mode: NotificationMode,
        notificationsAllowed: Boolean,
        onModeSelect: (NotificationMode) -> Unit = {},
        onEnableClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            HushTheme {
                NotificationSettingsScreen(
                    uiState = NotificationSettingsUiState(isLoading = false, notificationMode = mode),
                    notificationsAllowed = notificationsAllowed,
                    onBackClick = {},
                    onNotificationModeSelect = onModeSelect,
                    onEnableNotificationsClick = onEnableClick,
                )
            }
        }
    }

    @Test
    fun theStoredModeIsSelectedAndAnotherCanBePicked() {
        var picked: NotificationMode? = null
        setScreen(NotificationMode.EVERY_CALL, notificationsAllowed = true, onModeSelect = { picked = it })

        composeRule.onNodeWithText(string(R.string.notification_mode_every_call), substring = true).assertIsSelected()
        composeRule.onNodeWithText(string(R.string.notification_mode_daily_summary), substring = true).performClick()

        assertEquals(NotificationMode.DAILY_SUMMARY, picked)
    }

    @Test
    fun aWarningShowsWhenNotificationsAreOffInAndroid() {
        var enableClicks = 0
        setScreen(NotificationMode.DAILY_SUMMARY, notificationsAllowed = false, onEnableClick = { enableClicks++ })

        composeRule.onNodeWithText(string(R.string.notification_settings_disabled)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.notification_settings_enable)).performClick()

        assertEquals(1, enableClicks)
    }

    @Test
    fun noWarningWhenTheModeIsNever() {
        setScreen(NotificationMode.NEVER, notificationsAllowed = false)

        composeRule.onNodeWithText(string(R.string.notification_settings_disabled)).assertDoesNotExist()
    }

    @Test
    fun noWarningWhenNotificationsAreAllowed() {
        setScreen(NotificationMode.EVERY_CALL, notificationsAllowed = true)

        composeRule.onNodeWithText(string(R.string.notification_settings_disabled)).assertDoesNotExist()
    }
}
