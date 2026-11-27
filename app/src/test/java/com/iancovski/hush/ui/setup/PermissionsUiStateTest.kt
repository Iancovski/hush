package com.iancovski.hush.ui.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionsUiStateTest {

    @Test
    fun `the role is requested first when nothing is granted`() {
        val state = PermissionsUiState()

        assertEquals(GrantAction.REQUEST_CALL_SCREENING_ROLE, state.grantAction)
        assertFalse(state.needsSystemSettings)
    }

    @Test
    fun `contacts are requested once the role is held`() {
        val state = PermissionsUiState(callScreeningRoleHeld = true)

        assertEquals(GrantAction.REQUEST_CONTACTS, state.grantAction)
    }

    @Test
    fun `contacts dialog still comes before the settings when the role dialog is blocked`() {
        val state = PermissionsUiState(callScreeningDialogBlocked = true)

        assertEquals(GrantAction.REQUEST_CONTACTS, state.grantAction)
    }

    @Test
    fun `default apps settings open when the role dialog is blocked and contacts are granted`() {
        val state = PermissionsUiState(contactsGranted = true, callScreeningDialogBlocked = true)

        assertEquals(GrantAction.OPEN_DEFAULT_APPS_SETTINGS, state.grantAction)
        assertTrue(state.needsSystemSettings)
    }

    @Test
    fun `app settings open when the contacts dialog is blocked and the role is held`() {
        val state = PermissionsUiState(callScreeningRoleHeld = true, contactsDialogBlocked = true)

        assertEquals(GrantAction.OPEN_APP_SETTINGS, state.grantAction)
        assertTrue(state.needsSystemSettings)
    }

    @Test
    fun `there is nothing to grant when both permissions are granted`() {
        val state = PermissionsUiState(callScreeningRoleHeld = true, contactsGranted = true)

        assertTrue(state.allGranted)
        assertNull(state.grantAction)
        assertFalse(state.needsSystemSettings)
    }
}
