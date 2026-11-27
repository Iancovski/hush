package com.iancovski.hush.ui.history

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.HushTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented test names use camelCase: spaces in method names need DEX 040 (API 30), and minSdk is 29.
@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private val calls = listOf(
        HistoryItem(id = 2, number = "+5511900001111", receivedAt = 2_000L),
        HistoryItem(id = 1, number = null, receivedAt = 1_000L),
    )

    @Test
    fun emptyHistoryShowsTheEmptyStateAndNoMenu() {
        composeRule.setContent {
            HushTheme {
                HistoryScreen(
                    uiState = HistoryUiState(isLoading = false),
                    onBackClick = {},
                    onClearHistory = {},
                    onCallNumberClick = {},
                    onAddToAllowlistClick = {},
                    onAddToContactsClick = {},
                    onAllowlistDialogInputChange = {},
                    onAllowlistDialogConfirm = { _, _ -> },
                    onAllowlistDialogDismiss = {},
                    onAddedToAllowlistShown = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.history_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.history_more_options)).assertDoesNotExist()
    }

    @Test
    fun callsShowTheirNumberOrPrivateLabel() {
        composeRule.setContent {
            HushTheme {
                HistoryScreen(
                    uiState = HistoryUiState(isLoading = false, calls = calls),
                    onBackClick = {},
                    onClearHistory = {},
                    onCallNumberClick = {},
                    onAddToAllowlistClick = {},
                    onAddToContactsClick = {},
                    onAllowlistDialogInputChange = {},
                    onAllowlistDialogConfirm = { _, _ -> },
                    onAllowlistDialogDismiss = {},
                    onAddedToAllowlistShown = {},
                )
            }
        }

        composeRule.onNodeWithText("+5511900001111").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.history_private_number)).assertIsDisplayed()
    }

    @Test
    fun callsOfTodayAreUnderTheTodayHeader() {
        val now = System.currentTimeMillis()
        composeRule.setContent {
            HushTheme {
                HistoryScreen(
                    uiState = HistoryUiState(
                        isLoading = false,
                        calls = listOf(HistoryItem(id = 1, number = "+5511900001111", receivedAt = now)),
                    ),
                    onBackClick = {},
                    onClearHistory = {},
                    onCallNumberClick = {},
                    onAddToAllowlistClick = {},
                    onAddToContactsClick = {},
                    onAllowlistDialogInputChange = {},
                    onAllowlistDialogConfirm = { _, _ -> },
                    onAllowlistDialogDismiss = {},
                    onAddedToAllowlistShown = {},
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.history_today)).assertIsDisplayed()
    }

    @Test
    fun clearingAsksForConfirmationFirst() {
        var cleared = 0
        composeRule.setContent {
            HushTheme {
                HistoryScreen(
                    uiState = HistoryUiState(isLoading = false, calls = calls),
                    onBackClick = {},
                    onClearHistory = { cleared++ },
                    onCallNumberClick = {},
                    onAddToAllowlistClick = {},
                    onAddToContactsClick = {},
                    onAllowlistDialogInputChange = {},
                    onAllowlistDialogConfirm = { _, _ -> },
                    onAllowlistDialogDismiss = {},
                    onAddedToAllowlistShown = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription(string(R.string.history_more_options)).performClick()
        composeRule.onNodeWithText(string(R.string.history_clear)).performClick()
        composeRule.onNodeWithText(string(R.string.history_clear_dialog_title)).assertIsDisplayed()
        assertEquals(0, cleared)

        composeRule.onNodeWithText(string(R.string.history_clear_confirm)).performClick()

        assertEquals(1, cleared)
        composeRule.onNodeWithText(string(R.string.history_clear_dialog_title)).assertDoesNotExist()
    }

    @Test
    fun backButtonNavigatesBack() {
        var wentBack = false
        composeRule.setContent {
            HushTheme {
                HistoryScreen(
                    uiState = HistoryUiState(isLoading = false),
                    onBackClick = { wentBack = true },
                    onClearHistory = {},
                    onCallNumberClick = {},
                    onAddToAllowlistClick = {},
                    onAddToContactsClick = {},
                    onAllowlistDialogInputChange = {},
                    onAllowlistDialogConfirm = { _, _ -> },
                    onAllowlistDialogDismiss = {},
                    onAddedToAllowlistShown = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription(string(R.string.navigate_back)).performClick()

        assertTrue(wentBack)
    }

    private fun setHistory(
        onCallNumberClick: (HistoryItem) -> Unit = {},
        onAddToAllowlistClick: (HistoryItem) -> Unit = {},
        onAddToContactsClick: (HistoryItem) -> Unit = {},
    ) {
        composeRule.setContent {
            HushTheme {
                HistoryScreen(
                    uiState = HistoryUiState(isLoading = false, calls = calls),
                    onBackClick = {},
                    onClearHistory = {},
                    onCallNumberClick = onCallNumberClick,
                    onAddToAllowlistClick = onAddToAllowlistClick,
                    onAddToContactsClick = onAddToContactsClick,
                    onAllowlistDialogInputChange = {},
                    onAllowlistDialogConfirm = { _, _ -> },
                    onAllowlistDialogDismiss = {},
                    onAddedToAllowlistShown = {},
                )
            }
        }
    }

    @Test
    fun tappingACallOffersToAddItToTheAllowlist() {
        var added: HistoryItem? = null
        setHistory(onAddToAllowlistClick = { added = it })

        composeRule.onNodeWithText("+5511900001111").performClick()
        composeRule.onNodeWithText(string(R.string.history_add_to_contacts)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.history_add_to_allowlist)).performClick()
        composeRule.waitForIdle()

        assertEquals(calls.first(), added)
    }

    @Test
    fun tappingACallOffersToAddItToTheContacts() {
        var added: HistoryItem? = null
        setHistory(onAddToContactsClick = { added = it })

        composeRule.onNodeWithText("+5511900001111").performClick()
        composeRule.onNodeWithText(string(R.string.history_add_to_contacts)).performClick()
        composeRule.waitForIdle()

        assertEquals(calls.first(), added)
    }

    @Test
    fun aHiddenNumberShowsItsActionsDisabled() {
        var actions = 0
        setHistory(
            onCallNumberClick = { actions++ },
            onAddToAllowlistClick = { actions++ },
            onAddToContactsClick = { actions++ },
        )

        composeRule.onNodeWithText(string(R.string.history_private_number)).performClick()

        listOf(R.string.history_call_number, R.string.history_add_to_allowlist, R.string.history_add_to_contacts)
            .forEach { action -> composeRule.onNodeWithText(string(action)).assertIsNotEnabled() }
        assertEquals(0, actions)
    }

    @Test
    fun tappingACallOffersToCallItBack() {
        var called: HistoryItem? = null
        setHistory(onCallNumberClick = { called = it })

        composeRule.onNodeWithText("+5511900001111").performClick()
        composeRule.onNodeWithText(string(R.string.history_call_number)).performClick()
        composeRule.waitForIdle()

        assertEquals(calls.first(), called)
    }
}
