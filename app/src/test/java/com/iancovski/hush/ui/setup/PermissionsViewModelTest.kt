package com.iancovski.hush.ui.setup

import com.iancovski.hush.permissions.PermissionStatus
import com.iancovski.hush.permissions.PermissionsChecker
import com.iancovski.hush.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakePermissionsChecker(
        var status: PermissionStatus = PermissionStatus(callScreeningRoleHeld = false, contactsGranted = false),
    ) : PermissionsChecker {
        override fun check() = status
    }

    private val checker = FakePermissionsChecker()

    private fun TestScope.createViewModel(): PermissionsViewModel {
        val viewModel = PermissionsViewModel(checker)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `initial state reflects the current permissions`() = runTest {
        checker.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = false)

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.callScreeningRoleHeld)
        assertFalse(viewModel.uiState.value.contactsGranted)
    }

    @Test
    fun `refresh picks up permissions granted in the system settings`() = runTest {
        val viewModel = createViewModel()

        checker.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = true)
        viewModel.refresh()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.allGranted)
    }

    @Test
    fun `role answered without a dialog switches the button to the settings`() = runTest {
        checker.status = PermissionStatus(callScreeningRoleHeld = false, contactsGranted = true)
        val viewModel = createViewModel()

        viewModel.onRoleResult(dialogShown = false)
        testScheduler.advanceUntilIdle()

        assertEquals(GrantAction.OPEN_DEFAULT_APPS_SETTINGS, viewModel.uiState.value.grantAction)
    }

    @Test
    fun `role denied in the dialog can be requested again`() = runTest {
        val viewModel = createViewModel()

        viewModel.onRoleResult(dialogShown = true)
        testScheduler.advanceUntilIdle()

        assertEquals(GrantAction.REQUEST_CALL_SCREENING_ROLE, viewModel.uiState.value.grantAction)
    }

    @Test
    fun `contacts denied for good switches the button to the app settings`() = runTest {
        checker.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = false)
        val viewModel = createViewModel()

        viewModel.onContactsResult(granted = false, canAskAgain = false)
        testScheduler.advanceUntilIdle()

        assertEquals(GrantAction.OPEN_APP_SETTINGS, viewModel.uiState.value.grantAction)
    }

    @Test
    fun `contacts denied once can be requested again`() = runTest {
        checker.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = false)
        val viewModel = createViewModel()

        viewModel.onContactsResult(granted = false, canAskAgain = true)
        testScheduler.advanceUntilIdle()

        assertEquals(GrantAction.REQUEST_CONTACTS, viewModel.uiState.value.grantAction)
    }

    @Test
    fun `granting contacts updates the state`() = runTest {
        checker.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = false)
        val viewModel = createViewModel()

        checker.status = PermissionStatus(callScreeningRoleHeld = true, contactsGranted = true)
        viewModel.onContactsResult(granted = true, canAskAgain = false)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.allGranted)
    }
}
