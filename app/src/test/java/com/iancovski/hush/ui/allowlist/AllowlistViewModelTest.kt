package com.iancovski.hush.ui.allowlist

import com.iancovski.hush.data.AllowlistRepository
import com.iancovski.hush.testing.FakeAllowedNumberDao
import com.iancovski.hush.testing.MainDispatcherRule
import com.iancovski.hush.testing.fakeNormalizeNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AllowlistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = AllowlistRepository(FakeAllowedNumberDao(), ::fakeNormalizeNumber)

    private fun TestScope.createViewModel(): AllowlistViewModel {
        val viewModel = AllowlistViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `starts loading and then shows an empty list`() = runTest {
        val viewModel = AllowlistViewModel(repository)
        assertTrue(viewModel.uiState.value.isLoading)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(AllowlistUiState(isLoading = false), viewModel.uiState.value)
    }

    @Test
    fun `adding a valid number closes the dialog and lists it`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddClick()
        viewModel.onDialogConfirm(number = "(11) 3000-1234", name = "Clínica")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.numberDialog)
        assertEquals(listOf(AllowlistItem(id = 1, number = "(11) 3000-1234", name = "Clínica")), state.numbers)
    }

    @Test
    fun `an invalid number keeps the dialog open with an error`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddClick()
        viewModel.onDialogConfirm(number = "123", name = "")
        testScheduler.advanceUntilIdle()

        assertEquals(NumberDialogState(error = NumberError.INVALID_NUMBER), viewModel.uiState.value.numberDialog)
        assertTrue(viewModel.uiState.value.numbers.isEmpty())
    }

    @Test
    fun `a number already on the list shows an error`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "")
        val viewModel = createViewModel()

        viewModel.onAddClick()
        viewModel.onDialogConfirm(number = "+55 11 3000-1234", name = "")
        testScheduler.advanceUntilIdle()

        assertEquals(NumberError.ALREADY_ON_LIST, viewModel.uiState.value.numberDialog?.error)
    }

    @Test
    fun `editing the number clears the error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddClick()
        viewModel.onDialogConfirm(number = "123", name = "")
        testScheduler.advanceUntilIdle()

        viewModel.onDialogInputChange()
        testScheduler.advanceUntilIdle()

        assertEquals(NumberDialogState(), viewModel.uiState.value.numberDialog)
    }

    @Test
    fun `editing opens the dialog with the entry and saves the changes`() = runTest {
        repository.add(number = "(11) 3000-1243", name = "")
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()
        val item = viewModel.uiState.value.numbers.single()

        viewModel.onEditClick(item)
        testScheduler.advanceUntilIdle()
        assertEquals(NumberDialogState(editing = item), viewModel.uiState.value.numberDialog)

        viewModel.onDialogConfirm(number = "(11) 3000-1234", name = "Clínica")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.numberDialog)
        assertEquals(
            listOf(AllowlistItem(id = item.id, number = "(11) 3000-1234", name = "Clínica")),
            viewModel.uiState.value.numbers,
        )
    }

    @Test
    fun `editing into a number of another entry shows an error`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "Clínica")
        repository.add(number = "(21) 3000-5678", name = "Escola")
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()
        val school = viewModel.uiState.value.numbers.single { it.name == "Escola" }

        viewModel.onEditClick(school)
        viewModel.onDialogConfirm(number = "11 3000-1234", name = "Escola")
        testScheduler.advanceUntilIdle()

        assertEquals(NumberError.ALREADY_ON_LIST, viewModel.uiState.value.numberDialog?.error)
    }

    @Test
    fun `removing asks for confirmation and then removes the number`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "")
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()
        val item = viewModel.uiState.value.numbers.single()

        viewModel.onRemoveClick(item)
        testScheduler.advanceUntilIdle()
        assertEquals(item, viewModel.uiState.value.pendingRemoval)

        viewModel.onRemoveConfirm()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingRemoval)
        assertTrue(viewModel.uiState.value.numbers.isEmpty())
    }

    @Test
    fun `dismissing the removal keeps the number`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "")
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onRemoveClick(viewModel.uiState.value.numbers.single())
        viewModel.onRemoveDismiss()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingRemoval)
        assertEquals(1, viewModel.uiState.value.numbers.size)
    }
}
