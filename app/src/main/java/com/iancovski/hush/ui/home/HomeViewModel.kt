package com.iancovski.hush.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.permissions.PermissionsChecker
import com.iancovski.hush.ui.permissions.PermissionsTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.ZonedDateTime

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    blockedCallRepository: BlockedCallRepository,
    permissionsChecker: PermissionsChecker,
    now: () -> ZonedDateTime = ZonedDateTime::now,
) : ViewModel() {

    private val permissions = PermissionsTracker(permissionsChecker)
    private val showPermissionsDialog = MutableStateFlow(false)

    // The days are computed when the screen starts observing, so coming back to Home after
    // midnight moves the week forward.
    private val blockedPerDay = flow { emitAll(blockedCallRepository.countsPerDay(lastSevenDayStarts(now()))) }

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settings,
        blockedPerDay,
        permissions.state,
        showPermissionsDialog,
    ) { settings, perDay, permissionsState, dialog ->
        HomeUiState(
            isLoading = false,
            blockingEnabled = settings.blockingEnabled,
            blockedPerDay = perDay,
            permissions = permissionsState,
            showPermissionsDialog = dialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    /** The user can revoke permissions in the system settings at any time, so this runs on every resume. */
    fun refreshPermissions() {
        permissions.refresh()
        // Granted from the system settings while the dialog was open: finish what the user asked for.
        if (showPermissionsDialog.value && permissions.state.value.allGranted) onPermissionsGranted()
    }

    fun onBlockingChange(enabled: Boolean) {
        if (enabled && !permissions.state.value.allGranted) {
            showPermissionsDialog.value = true
            return
        }
        saveBlockingEnabled(enabled)
    }

    fun onPermissionsDialogDismiss() {
        showPermissionsDialog.value = false
    }

    /** @param dialogShown false when Android answered without showing the role dialog to the user. */
    fun onRoleResult(dialogShown: Boolean) = permissions.onRoleResult(dialogShown)

    /** @param canAskAgain false when Android will not show the contacts dialog again. */
    fun onContactsResult(granted: Boolean, canAskAgain: Boolean) = permissions.onContactsResult(granted, canAskAgain)

    /** The missing permissions were granted from the dialog: turn protection on, as the user asked. */
    fun onPermissionsGranted() {
        showPermissionsDialog.value = false
        permissions.refresh()
        saveBlockingEnabled(true)
    }

    private fun saveBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setBlockingEnabled(enabled)
            } catch (_: IOException) {
                // Nothing was saved, so the switch keeps showing the stored value.
            }
        }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            blockedCallRepository: BlockedCallRepository,
            permissionsChecker: PermissionsChecker,
        ) = viewModelFactory {
            initializer { HomeViewModel(settingsRepository, blockedCallRepository, permissionsChecker) }
        }
    }
}
