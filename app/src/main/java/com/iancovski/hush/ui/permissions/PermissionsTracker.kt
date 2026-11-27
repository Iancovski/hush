package com.iancovski.hush.ui.permissions

import com.iancovski.hush.permissions.PermissionsChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Tracks the required permissions for a screen that asks for them: what is granted, and which
 * dialogs Android no longer shows. Shared by the setup wizard and the home screen.
 */
class PermissionsTracker(private val permissionsChecker: PermissionsChecker) {

    private val mutableState = MutableStateFlow(
        permissionsChecker.check().let {
            PermissionsUiState(callScreeningRoleHeld = it.callScreeningRoleHeld, contactsGranted = it.contactsGranted)
        },
    )

    val state: StateFlow<PermissionsUiState> = mutableState.asStateFlow()

    /** Reads the permissions again, for example when the user comes back from the system settings. */
    fun refresh() {
        val status = permissionsChecker.check()
        mutableState.update {
            it.copy(callScreeningRoleHeld = status.callScreeningRoleHeld, contactsGranted = status.contactsGranted)
        }
    }

    /** @param dialogShown false when Android answered without showing the role dialog to the user. */
    fun onRoleResult(dialogShown: Boolean) {
        mutableState.update { it.copy(callScreeningDialogBlocked = !dialogShown) }
        refresh()
    }

    /** @param canAskAgain false when Android will not show the contacts dialog again. */
    fun onContactsResult(granted: Boolean, canAskAgain: Boolean) {
        mutableState.update { it.copy(contactsDialogBlocked = !granted && !canAskAgain) }
        refresh()
    }
}
