package com.iancovski.hush.notifications

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import kotlinx.coroutines.flow.first

/** Saves the notification mode and keeps the daily summary scheduled only while it is picked. */
class NotificationModeController(
    private val settingsRepository: SettingsRepository,
    private val scheduler: SummaryScheduler,
) {

    /** Picked in the setup wizard. */
    suspend fun completeSetup(mode: NotificationMode) {
        settingsRepository.completeSetup(mode)
        applySchedule(mode)
    }

    /** Picked in the settings. */
    suspend fun setMode(mode: NotificationMode) {
        settingsRepository.setNotificationMode(mode)
        applySchedule(mode)
    }

    /** On app start, schedules the summary again in case the work was lost. */
    suspend fun restoreSchedule() {
        if (settingsRepository.settings.first().notificationMode == NotificationMode.DAILY_SUMMARY) {
            scheduler.schedule(SchedulePolicy.KEEP)
        }
    }

    private fun applySchedule(mode: NotificationMode) {
        if (mode == NotificationMode.DAILY_SUMMARY) {
            scheduler.schedule(SchedulePolicy.REPLACE)
        } else {
            scheduler.cancel()
        }
    }
}
