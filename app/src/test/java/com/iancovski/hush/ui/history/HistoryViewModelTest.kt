package com.iancovski.hush.ui.history

import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.testing.FakeBlockedCallDao
import com.iancovski.hush.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = BlockedCallRepository(FakeBlockedCallDao(), normalizeNumber = { null })

    private fun TestScope.createViewModel(): HistoryViewModel {
        val viewModel = HistoryViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `starts loading and then shows an empty history`() = runTest {
        val viewModel = HistoryViewModel(repository)
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
}
