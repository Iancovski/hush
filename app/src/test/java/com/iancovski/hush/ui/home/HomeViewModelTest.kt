package com.iancovski.hush.ui.home

import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.testing.MainDispatcherRule
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

// DataStore reads and writes its file on a real thread, so these tests wait for the expected state
// instead of advancing virtual time. If that state never comes, runTest fails with a timeout.
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `starts loading and then shows blocking on by default`() = runTest {
        val viewModel = HomeViewModel(SettingsRepository(testDataStore(folder)))

        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.uiState.first { it == HomeUiState(isLoading = false, blockingEnabled = true) }
    }

    @Test
    fun `turning the switch off saves it and updates the state`() = runTest {
        val repository = SettingsRepository(testDataStore(folder))
        val viewModel = HomeViewModel(repository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onBlockingChange(false)

        repository.settings.first { !it.blockingEnabled }
        viewModel.uiState.first { it == HomeUiState(isLoading = false, blockingEnabled = false) }
    }
}
