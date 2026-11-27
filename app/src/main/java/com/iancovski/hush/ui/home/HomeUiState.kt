package com.iancovski.hush.ui.home

import com.iancovski.hush.ui.permissions.PermissionsUiState

data class HomeUiState(
    val isLoading: Boolean = true,
    /** What the user picked with the switch. */
    val blockingEnabled: Boolean = false,
    /** Blocked calls on each of the last seven days, oldest first, today last. */
    val blockedPerDay: List<Int> = emptyList(),
    val permissions: PermissionsUiState = PermissionsUiState(callScreeningRoleHeld = true, contactsGranted = true),
    /** Explains the missing permissions after the user tried to turn protection on. */
    val showPermissionsDialog: Boolean = false,
) {
    val blockedLastSevenDays: Int
        get() = blockedPerDay.sum()

    /**
     * Whether calls are actually blocked. Without a required permission they are not, whatever the
     * switch was set to; the stored choice is kept, so protection resumes once the permission is back.
     */
    val protectionActive: Boolean
        get() = blockingEnabled && permissions.allGranted
}
