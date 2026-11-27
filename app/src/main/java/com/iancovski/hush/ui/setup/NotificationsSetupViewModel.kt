package com.iancovski.hush.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.notifications.NotificationModeController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class NotificationsSetupViewModel(private val notificationModeController: NotificationModeController) : ViewModel() {

    private val state = MutableStateFlow(NotificationsSetupUiState())

    val uiState: StateFlow<NotificationsSetupUiState> = state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = state.value,
    )

    fun onModeSelected(mode: NotificationMode) {
        state.update { it.copy(selectedMode = mode) }
    }

    /**
     * Saves the choice and completes the setup. The app then leaves the wizard, because it observes
     * the "setup completed" setting.
     */
    fun finish() {
        if (state.value.isFinishing) return
        state.update { it.copy(isFinishing = true) }
        viewModelScope.launch {
            try {
                notificationModeController.completeSetup(state.value.selectedMode)
            } catch (_: IOException) {
                // Let the user try again instead of leaving the button disabled.
                state.update { it.copy(isFinishing = false) }
            }
        }
    }

    companion object {
        fun factory(notificationModeController: NotificationModeController) = viewModelFactory {
            initializer { NotificationsSetupViewModel(notificationModeController) }
        }
    }
}
