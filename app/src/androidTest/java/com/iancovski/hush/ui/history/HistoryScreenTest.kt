package com.iancovski.hush.ui.history

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
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
                HistoryScreen(uiState = HistoryUiState(isLoading = false), onBackClick = {}, onClearHistory = {})
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
                )
            }
        }

        composeRule.onNodeWithContentDescription(string(R.string.navigate_back)).performClick()

        assertTrue(wentBack)
    }
}
