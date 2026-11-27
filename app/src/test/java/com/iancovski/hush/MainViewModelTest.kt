package com.iancovski.hush

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.testing.MainDispatcherRule
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    // DataStore reads and writes its file on a real thread, so the test waits for the expected state
    // instead of advancing virtual time. If that state never comes, runTest fails with a timeout.
    @Test
    fun `app starts on the wizard until the setup is completed`() = runTest {
        val repository = SettingsRepository(testDataStore(folder))
        val viewModel = MainViewModel(repository)

        viewModel.uiState.first { it == MainUiState.Ready(setupCompleted = false) }

        repository.completeSetup(NotificationMode.EVERY_CALL)

        viewModel.uiState.first { it == MainUiState.Ready(setupCompleted = true) }
    }
}
