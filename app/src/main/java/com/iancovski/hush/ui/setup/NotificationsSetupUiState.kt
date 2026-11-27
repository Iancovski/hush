package com.iancovski.hush.ui.setup

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.Settings

data class NotificationsSetupUiState(
    val selectedMode: NotificationMode = Settings().notificationMode,
    val isFinishing: Boolean = false,
)
