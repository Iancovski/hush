package com.iancovski.hush.ui.allowlist

import androidx.annotation.StringRes
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.HushTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented test names use camelCase: spaces in method names need DEX 040 (API 30), and minSdk is 29.
@RunWith(AndroidJUnit4::class)
class AllowlistScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(@StringRes id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    private fun setScreen(
        uiState: AllowlistUiState,
        onAddClick: () -> Unit = {},
        onDialogConfirm: (String, String) -> Unit = { _, _ -> },
        onEditClick: (AllowlistItem) -> Unit = {},
        onRemoveClick: (AllowlistItem) -> Unit = {},
    ) {
        composeRule.setContent {
            HushTheme {
                AllowlistScreen(
                    uiState = uiState,
                    onBackClick = {},
                    onAddClick = onAddClick,
                    onEditClick = onEditClick,
                    onDialogInputChange = {},
                    onDialogConfirm = onDialogConfirm,
                    onDialogDismiss = {},
                    onRemoveClick = onRemoveClick,
                    onRemoveConfirm = {},
                    onRemoveDismiss = {},
                )
            }
        }
    }

    @Test
    fun emptyListShowsTheEmptyStateAndTheAddButton() {
        var addClicks = 0
        setScreen(AllowlistUiState(isLoading = false), onAddClick = { addClicks++ })

        composeRule.onNodeWithText(string(R.string.allowlist_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.allowlist_add)).performClick()

        assertEquals(1, addClicks)
    }

    @Test
    fun numbersShowTheirNameAndNumber() {
        setScreen(
            AllowlistUiState(
                isLoading = false,
                numbers = listOf(
                    AllowlistItem(id = 2, number = "+55 11 3000-1234", name = "Clínica"),
                    AllowlistItem(id = 1, number = "+55 21 99999-0000", name = null),
                ),
            ),
        )

        composeRule.onNodeWithText("Clínica").assertIsDisplayed()
        composeRule.onNodeWithText("+55 11 3000-1234").assertIsDisplayed()
        composeRule.onNodeWithText("+55 21 99999-0000").assertIsDisplayed()
    }

    @Test
    fun removeButtonReportsTheNumber() {
        val item = AllowlistItem(id = 1, number = "+55 11 3000-1234", name = "Clínica")
        var removed: AllowlistItem? = null
        setScreen(AllowlistUiState(isLoading = false, numbers = listOf(item)), onRemoveClick = { removed = it })

        composeRule.onNodeWithContentDescription(string(R.string.allowlist_remove, "Clínica")).performClick()

        assertEquals(item, removed)
    }

    @Test
    fun addDialogSendsWhatWasTyped() {
        var added: Pair<String, String>? = null
        setScreen(
            AllowlistUiState(isLoading = false, numberDialog = NumberDialogState()),
            onDialogConfirm = { number, name -> added = number to name },
        )
        val confirm = composeRule.onNodeWithText(string(R.string.allowlist_add_confirm))
        confirm.assertIsNotEnabled()

        composeRule.onNodeWithText(string(R.string.allowlist_number_label)).performTextInput("11 3000-1234")
        composeRule.onNodeWithText(string(R.string.allowlist_name_label)).performTextInput("Clínica")
        confirm.performClick()

        assertEquals("11 3000-1234" to "Clínica", added)
    }

    @Test
    fun editButtonReportsTheNumber() {
        val item = AllowlistItem(id = 1, number = "+55 11 3000-1234", name = "Clínica")
        var edited: AllowlistItem? = null
        setScreen(AllowlistUiState(isLoading = false, numbers = listOf(item)), onEditClick = { edited = it })

        composeRule.onNodeWithContentDescription(string(R.string.allowlist_edit, "Clínica")).performClick()

        assertEquals(item, edited)
    }

    @Test
    fun editDialogStartsWithTheEntryAndSendsTheChanges() {
        val item = AllowlistItem(id = 1, number = "11 3000-1243", name = null)
        var saved: Pair<String, String>? = null
        setScreen(
            AllowlistUiState(isLoading = false, numbers = listOf(item), numberDialog = NumberDialogState(editing = item)),
            onDialogConfirm = { number, name -> saved = number to name },
        )
        composeRule.onNodeWithText(string(R.string.allowlist_edit_dialog_title)).assertIsDisplayed()
        // The number also shows in the list behind the dialog, so check the field itself.
        composeRule.onNodeWithText(string(R.string.allowlist_number_label)).assert(hasText("11 3000-1243"))

        composeRule.onNodeWithText(string(R.string.allowlist_number_label)).performTextClearance()
        composeRule.onNodeWithText(string(R.string.allowlist_number_label)).performTextInput("11 3000-1234")
        composeRule.onNodeWithText(string(R.string.allowlist_name_label)).performTextInput("Clínica")
        composeRule.onNodeWithText(string(R.string.allowlist_save)).performClick()

        assertEquals("11 3000-1234" to "Clínica", saved)
    }

    @Test
    fun addDialogShowsTheError() {
        setScreen(
            AllowlistUiState(isLoading = false, numberDialog = NumberDialogState(error = NumberError.ALREADY_ON_LIST)),
        )

        composeRule.onNodeWithText(string(R.string.allowlist_error_duplicate)).assertIsDisplayed()
    }
}
