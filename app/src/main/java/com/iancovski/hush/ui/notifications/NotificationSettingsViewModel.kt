package com.iancovski.hush.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.notifications.NotificationModeController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class NotificationSettingsViewModel(
    settingsRepository: SettingsRepository,
    private val notificationModeController: NotificationModeController,
) : ViewModel() {

    val uiState: StateFlow<NotificationSettingsUiState> = settingsRepository.settings
        .map { settings -> NotificationSettingsUiState(isLoading = false, notificationMode = settings.notificationMode) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationSettingsUiState(),
        )

    fun onNotificationModeSelect(mode: NotificationMode) {
        viewModelScope.launch {
            try {
                notificationModeController.setMode(mode)
            } catch (_: IOException) {
                // Nothing was saved, so the selection keeps showing the stored mode.
            }
        }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            notificationModeController: NotificationModeController,
        ) = viewModelFactory {
            initializer { NotificationSettingsViewModel(settingsRepository, notificationModeController) }
        }
    }
}
