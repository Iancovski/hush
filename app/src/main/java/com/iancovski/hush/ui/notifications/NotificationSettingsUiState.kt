package com.iancovski.hush.ui.notifications

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.Settings

data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val notificationMode: NotificationMode = Settings().notificationMode,
)
