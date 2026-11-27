package com.iancovski.hush.ui.home

import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.permissions.PermissionStatus
import com.iancovski.hush.testing.FakeBlockedCallDao
import com.iancovski.hush.testing.FakePermissionsChecker
import com.iancovski.hush.testing.MainDispatcherRule
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

// DataStore reads and writes its file on a real thread, so these tests wait for the expected state
// instead of advancing virtual time. If that state never comes, runTest fails with a timeout.
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    private val zone = ZoneId.of("America/Sao_Paulo")
    private val now = ZonedDateTime.of(LocalDateTime.of(2026, 11, 27, 17, 0), zone)
    private val blockedCalls = BlockedCallRepository(FakeBlockedCallDao(), normalizeNumber = { null })
    private val permissions = FakePermissionsChecker(PermissionStatus(callScreeningRoleHeld = true, contactsGranted = true))
    private lateinit var settings: SettingsRepository

    private fun TestScope.createViewModel(): HomeViewModel {
        settings = SettingsRepository(testDataStore(folder))
        return HomeViewModel(settings, blockedCalls, permissions, now = { now })
    }

    private fun at(day: Int, hour: Int) =
        ZonedDateTime.of(LocalDateTime.of(2026, 11, day, hour, 0), zone).toInstant().toEpochMilli()

    @Test
    fun `starts loading and then shows active protection with an empty week`() = runTest {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        val state = viewModel.uiState.first { !it.isLoading }
        assertTrue(state.protectionActive)
        assertEquals(List(7) { 0 }, state.blockedPerDay)
    }

    @Test
    fun `counts the calls blocked on each of the last seven days`() = runTest {
        blockedCalls.record(number = "old", receivedAt = at(day = 20, hour = 23)) // Eight days ago: left out.
        blockedCalls.record(number = "1", receivedAt = at(day = 21, hour = 9))
        blockedCalls.record(number = "2", receivedAt = at(day = 25, hour = 12))
        blockedCalls.record(number = "3", receivedAt = at(day = 27, hour = 8))
        blockedCalls.record(number = null, receivedAt = at(day = 27, hour = 16))
        val viewModel = createViewModel()

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(listOf(1, 0, 0, 0, 1, 0, 2), state.blockedPerDay)
        assertEquals(4, state.blockedLastSevenDays)
    }

    @Test
    fun `turning the switch off saves it and updates the state`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.onBlockingChange(false)

        settings.settings.first { !it.blockingEnabled }
        viewModel.uiState.first { !it.isLoading && !it.protectionActive }
    }

    @Test
    fun `a removed permission shows protection inactive without changing the stored choice`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.first { !it.isLoading }

        permissions.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = false)
        viewModel.refreshPermissions()

        val state = viewModel.uiState.first { !it.permissions.contactsGranted }
        assertTrue(state.blockingEnabled)
        assertFalse(state.protectionActive)
        assertTrue(settings.settings.first().blockingEnabled)
    }

    @Test
    fun `turning protection on without permissions explains them instead of saving`() = runTest {
        permissions.status = PermissionStatus(callScreeningRoleHeld = false, contactsGranted = true)
        val viewModel = createViewModel()
        settings.setBlockingEnabled(false)
        viewModel.uiState.first { !it.isLoading && !it.blockingEnabled }

        viewModel.onBlockingChange(true)

        viewModel.uiState.first { it.showPermissionsDialog }
        assertFalse(settings.settings.first().blockingEnabled)
    }

    @Test
    fun `granting the permissions from the dialog turns protection on`() = runTest {
        permissions.status = PermissionStatus(callScreeningRoleHeld = false, contactsGranted = true)
        val viewModel = createViewModel()
        settings.setBlockingEnabled(false)
        viewModel.uiState.first { !it.isLoading && !it.blockingEnabled }
        viewModel.onBlockingChange(true)

        permissions.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = true)
        viewModel.onPermissionsGranted()

        val state = viewModel.uiState.first { it.protectionActive }
        assertFalse(state.showPermissionsDialog)
    }

    @Test
    fun `coming back from the system settings with the permissions turns protection on`() = runTest {
        permissions.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = false)
        val viewModel = createViewModel()
        settings.setBlockingEnabled(false)
        viewModel.uiState.first { !it.isLoading && !it.blockingEnabled }
        viewModel.onBlockingChange(true)

        permissions.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = true)
        viewModel.refreshPermissions()

        val state = viewModel.uiState.first { it.protectionActive }
        assertFalse(state.showPermissionsDialog)
    }

    @Test
    fun `dismissing the dialog keeps protection off`() = runTest {
        permissions.status = PermissionStatus(callScreeningRoleHeld = false, contactsGranted = false)
        val viewModel = createViewModel()
        viewModel.uiState.first { !it.isLoading }
        viewModel.onBlockingChange(true)
        viewModel.uiState.first { it.showPermissionsDialog }

        viewModel.onPermissionsDialogDismiss()

        val state = viewModel.uiState.first { !it.showPermissionsDialog }
        assertFalse(state.protectionActive)
    }
}
