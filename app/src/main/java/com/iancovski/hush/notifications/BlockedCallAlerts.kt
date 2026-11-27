package com.iancovski.hush.notifications

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import kotlinx.coroutines.flow.first

/** Notifies each blocked call when the user picked EVERY_CALL. */
class BlockedCallAlerts(
    private val settingsRepository: SettingsRepository,
    private val notifier: Notifier,
) {

    suspend fun onCallBlocked(number: String?, receivedAt: Long) {
        if (settingsRepository.settings.first().notificationMode == NotificationMode.EVERY_CALL) {
            notifier.showBlockedCall(number, receivedAt)
        }
    }
}
