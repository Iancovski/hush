package com.iancovski.hush.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.permissions.PermissionStatus
import com.iancovski.hush.permissions.PermissionsChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class PermissionsViewModel(private val permissionsChecker: PermissionsChecker) : ViewModel() {

    private data class BlockedDialogs(
        val callScreening: Boolean = false,
        val contacts: Boolean = false,
    )

    private val status = MutableStateFlow(permissionsChecker.check())
    private val blockedDialogs = MutableStateFlow(BlockedDialogs())

    val uiState: StateFlow<PermissionsUiState> = combine(status, blockedDialogs, ::toUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = toUiState(status.value, blockedDialogs.value),
        )

    /** Reads the permissions again, for example when the user comes back from the system settings. */
    fun refresh() {
        status.value = permissionsChecker.check()
    }

    /** @param dialogShown false when Android answered without showing the role dialog to the user. */
    fun onRoleResult(dialogShown: Boolean) {
        blockedDialogs.update { it.copy(callScreening = !dialogShown) }
        refresh()
    }

    /** @param canAskAgain false when Android will not show the contacts dialog again. */
    fun onContactsResult(granted: Boolean, canAskAgain: Boolean) {
        blockedDialogs.update { it.copy(contacts = !granted && !canAskAgain) }
        refresh()
    }

    private fun toUiState(status: PermissionStatus, blocked: BlockedDialogs) = PermissionsUiState(
        callScreeningRoleHeld = status.callScreeningRoleHeld,
        contactsGranted = status.contactsGranted,
        callScreeningDialogBlocked = blocked.callScreening,
        contactsDialogBlocked = blocked.contacts,
    )

    companion object {
        fun factory(permissionsChecker: PermissionsChecker) = viewModelFactory {
            initializer { PermissionsViewModel(permissionsChecker) }
        }
    }
}
