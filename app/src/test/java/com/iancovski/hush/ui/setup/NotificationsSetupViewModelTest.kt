package com.iancovski.hush.ui.setup

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.testing.MainDispatcherRule
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.createViewModel(repository: SettingsRepository): NotificationsSetupViewModel {
        val viewModel = NotificationsSetupViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `every blocked call is selected by default`() = runTest {
        val viewModel = createViewModel(SettingsRepository(testDataStore(folder)))

        assertEquals(NotificationMode.EVERY_CALL, viewModel.uiState.value.selectedMode)
        assertFalse(viewModel.uiState.value.isFinishing)
    }

    @Test
    fun `selecting a mode updates the state`() = runTest {
        val viewModel = createViewModel(SettingsRepository(testDataStore(folder)))

        viewModel.onModeSelected(NotificationMode.DAILY_SUMMARY)
        testScheduler.advanceUntilIdle()

        assertEquals(NotificationMode.DAILY_SUMMARY, viewModel.uiState.value.selectedMode)
    }

    @Test
    fun `finishing saves the selected mode and completes the setup`() = runTest {
        val repository = SettingsRepository(testDataStore(folder))
        val viewModel = createViewModel(repository)

        viewModel.onModeSelected(NotificationMode.NEVER)
        viewModel.finish()

        // DataStore writes on a real thread, so wait for the saved value instead of advancing time.
        val settings = repository.settings.first { it.setupCompleted }
        assertEquals(NotificationMode.NEVER, settings.notificationMode)
        assertTrue(viewModel.uiState.value.isFinishing)
    }
}
