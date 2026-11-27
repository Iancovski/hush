package com.iancovski.hush.ui.notifications

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.notifications.NotificationModeController
import com.iancovski.hush.notifications.SchedulePolicy
import com.iancovski.hush.testing.FakeSummaryScheduler
import com.iancovski.hush.testing.MainDispatcherRule
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

// DataStore reads and writes its file on a real thread, so these tests wait for the expected state
// instead of advancing virtual time. If that state never comes, runTest fails with a timeout.
class NotificationSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    private val scheduler = FakeSummaryScheduler()

    @Test
    fun `starts loading and then shows the stored mode`() = runTest {
        val settings = SettingsRepository(testDataStore(folder))
        val viewModel = NotificationSettingsViewModel(settings, NotificationModeController(settings, scheduler))

        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.uiState.first {
            it == NotificationSettingsUiState(isLoading = false, notificationMode = NotificationMode.EVERY_CALL)
        }
    }

    @Test
    fun `picking a mode saves it and schedules the summary`() = runTest {
        val settings = SettingsRepository(testDataStore(folder))
        val viewModel = NotificationSettingsViewModel(settings, NotificationModeController(settings, scheduler))
        viewModel.uiState.first { !it.isLoading }

        viewModel.onNotificationModeSelect(NotificationMode.DAILY_SUMMARY)

        viewModel.uiState.first { it.notificationMode == NotificationMode.DAILY_SUMMARY }
        settings.settings.first { it.notificationMode == NotificationMode.DAILY_SUMMARY }
        assertEquals(listOf(SchedulePolicy.REPLACE), scheduler.scheduled)
    }
}
