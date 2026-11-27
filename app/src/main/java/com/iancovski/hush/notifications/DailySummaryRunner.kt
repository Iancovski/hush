package com.iancovski.hush.notifications

import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * One run of the daily summary. It counts the calls blocked since the previous summary, so a late
 * run never skips or double-counts calls.
 */
class DailySummaryRunner(
    private val settingsRepository: SettingsRepository,
    private val blockedCallRepository: BlockedCallRepository,
    private val notifier: Notifier,
    private val scheduler: SummaryScheduler,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {

    suspend fun run() {
        // The work is cancelled when the mode changes, but a run may already have started.
        if (settingsRepository.settings.first().notificationMode != NotificationMode.DAILY_SUMMARY) return
        val now = currentTimeMillis()
        val since = settingsRepository.lastSummaryAt() ?: (now - TimeUnit.DAYS.toMillis(1))
        val blockedCount = blockedCallRepository.countSince(since)
        if (blockedCount > 0) notifier.showDailySummary(blockedCount)
        settingsRepository.setLastSummaryAt(now)
        scheduler.schedule(SchedulePolicy.AFTER_CURRENT)
    }
}
