package com.iancovski.hush.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.permissions.PermissionsChecker
import com.iancovski.hush.ui.permissions.PermissionsTracker
import com.iancovski.hush.ui.permissions.PermissionsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PermissionsViewModel(permissionsChecker: PermissionsChecker) : ViewModel() {

    private val tracker = PermissionsTracker(permissionsChecker)

    val uiState: StateFlow<PermissionsUiState> = tracker.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = tracker.state.value,
    )

    /** Reads the permissions again, for example when the user comes back from the system settings. */
    fun refresh() = tracker.refresh()

    /** @param dialogShown false when Android answered without showing the role dialog to the user. */
    fun onRoleResult(dialogShown: Boolean) = tracker.onRoleResult(dialogShown)

    /** @param canAskAgain false when Android will not show the contacts dialog again. */
    fun onContactsResult(granted: Boolean, canAskAgain: Boolean) = tracker.onContactsResult(granted, canAskAgain)

    companion object {
        fun factory(permissionsChecker: PermissionsChecker) = viewModelFactory {
            initializer { PermissionsViewModel(permissionsChecker) }
        }
    }
}
