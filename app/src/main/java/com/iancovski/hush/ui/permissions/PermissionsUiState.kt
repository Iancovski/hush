package com.iancovski.hush.ui.permissions

/** What the grant button does next. */
enum class GrantAction {
    REQUEST_CALL_SCREENING_ROLE,
    REQUEST_CONTACTS,

    /** Android no longer shows the role dialog, so the user picks Hush in the default apps settings. */
    OPEN_DEFAULT_APPS_SETTINGS,

    /** Android no longer shows the contacts dialog, so the user grants it in the app's settings. */
    OPEN_APP_SETTINGS,
}

data class PermissionsUiState(
    val callScreeningRoleHeld: Boolean = false,
    val contactsGranted: Boolean = false,
    val callScreeningDialogBlocked: Boolean = false,
    val contactsDialogBlocked: Boolean = false,
) {
    val allGranted: Boolean
        get() = callScreeningRoleHeld && contactsGranted

    val canRequestContacts: Boolean
        get() = !contactsGranted && !contactsDialogBlocked

    /** Dialogs come first, so one tap asks for everything Android still lets the app ask for. */
    val grantAction: GrantAction?
        get() = when {
            !callScreeningRoleHeld && !callScreeningDialogBlocked -> GrantAction.REQUEST_CALL_SCREENING_ROLE
            canRequestContacts -> GrantAction.REQUEST_CONTACTS
            !callScreeningRoleHeld -> GrantAction.OPEN_DEFAULT_APPS_SETTINGS
            !contactsGranted -> GrantAction.OPEN_APP_SETTINGS
            else -> null
        }

    val needsSystemSettings: Boolean
        get() = grantAction == GrantAction.OPEN_DEFAULT_APPS_SETTINGS ||
            grantAction == GrantAction.OPEN_APP_SETTINGS
}
