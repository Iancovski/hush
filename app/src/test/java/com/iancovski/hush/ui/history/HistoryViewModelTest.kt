package com.iancovski.hush.ui.history

import com.iancovski.hush.data.AllowlistRepository
import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.testing.FakeAllowedNumberDao
import com.iancovski.hush.testing.FakeBlockedCallDao
import com.iancovski.hush.testing.MainDispatcherRule
import com.iancovski.hush.testing.fakeNormalizeNumber
import com.iancovski.hush.ui.allowlist.NumberDialogState
import com.iancovski.hush.ui.allowlist.NumberError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = BlockedCallRepository(FakeBlockedCallDao(), normalizeNumber = { null })
    private val allowlist = AllowlistRepository(FakeAllowedNumberDao(), ::fakeNormalizeNumber)

    private fun TestScope.createViewModel(): HistoryViewModel {
        val viewModel = HistoryViewModel(repository, allowlist)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    private val blockedCall = HistoryItem(id = 1, number = "(11) 3000-1234", receivedAt = 1_000L)

    @Test
    fun `starts loading and then shows an empty history`() = runTest {
        val viewModel = HistoryViewModel(repository, allowlist)
        assertTrue(viewModel.uiState.value.isLoading)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(HistoryUiState(isLoading = false, calls = emptyList()), viewModel.uiState.value)
    }

    @Test
    fun `lists blocked calls newest first`() = runTest {
        repository.record(number = "+5511900001111", receivedAt = 1_000L)
        repository.record(number = null, receivedAt = 3_000L)
        repository.record(number = "+5511900002222", receivedAt = 2_000L)

        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                HistoryItem(id = 2, number = null, receivedAt = 3_000L),
                HistoryItem(id = 3, number = "+5511900002222", receivedAt = 2_000L),
                HistoryItem(id = 1, number = "+5511900001111", receivedAt = 1_000L),
            ),
            viewModel.uiState.value.calls,
        )
    }

    @Test
    fun `clearing the history empties the list`() = runTest {
        repository.record(number = "+5511900001111", receivedAt = 1_000L)
        val viewModel = createViewModel()

        viewModel.onClearHistory()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.calls.isEmpty())
    }

    @Test
    fun `adding a blocked number to the allowlist starts with its number`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddToAllowlistClick(blockedCall)
        testScheduler.advanceUntilIdle()

        assertEquals(NumberDialogState(prefilledNumber = "(11) 3000-1234"), viewModel.uiState.value.allowlistDialog)
    }

    @Test
    fun `a hidden number cannot be added to the allowlist`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddToAllowlistClick(HistoryItem(id = 1, number = null, receivedAt = 1_000L))
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.allowlistDialog)
    }

    @Test
    fun `confirming saves the number and confirms it once`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddToAllowlistClick(blockedCall)

        viewModel.onAllowlistDialogConfirm(number = "(11) 3000-1234", name = "Clínica")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.allowlistDialog)
        assertTrue(viewModel.uiState.value.showAddedToAllowlist)
        assertEquals("Clínica", allowlist.allowedNumbers.first().single().name)

        viewModel.onAddedToAllowlistShown()
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddedToAllowlist)
    }

    @Test
    fun `a number already on the allowlist shows an error`() = runTest {
        allowlist.add(number = "+55 11 3000-1234", name = null)
        val viewModel = createViewModel()
        viewModel.onAddToAllowlistClick(blockedCall)

        viewModel.onAllowlistDialogConfirm(number = "(11) 3000-1234", name = "")
        testScheduler.advanceUntilIdle()

        assertEquals(NumberError.ALREADY_ON_LIST, viewModel.uiState.value.allowlistDialog?.error)
        assertFalse(viewModel.uiState.value.showAddedToAllowlist)
    }
}
