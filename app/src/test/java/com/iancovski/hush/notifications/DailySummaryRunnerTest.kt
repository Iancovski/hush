package com.iancovski.hush.notifications

import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.testing.FakeBlockedCallDao
import com.iancovski.hush.testing.FakeNotifier
import com.iancovski.hush.testing.FakeSummaryScheduler
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DailySummaryRunnerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val notifier = FakeNotifier()
    private val scheduler = FakeSummaryScheduler()
    private val blockedCalls = BlockedCallRepository(FakeBlockedCallDao(), normalizeNumber = { null })
    private lateinit var settings: SettingsRepository

    private fun TestScope.runner(now: Long = NOW): DailySummaryRunner {
        settings = SettingsRepository(testDataStore(folder), currentTimeMillis = { LAST_SUMMARY })
        return DailySummaryRunner(settings, blockedCalls, notifier, scheduler, currentTimeMillis = { now })
    }

    @Test
    fun `posts the calls blocked since the last summary`() = runTest {
        val runner = runner()
        settings.setNotificationMode(NotificationMode.DAILY_SUMMARY)
        blockedCalls.record(number = "1", receivedAt = LAST_SUMMARY - 1) // Already in the previous summary.
        blockedCalls.record(number = "2", receivedAt = LAST_SUMMARY + 1)
        blockedCalls.record(number = null, receivedAt = NOW - 1)

        runner.run()

        assertEquals(listOf(2), notifier.dailySummaries)
    }

    @Test
    fun `posts nothing when no call was blocked`() = runTest {
        val runner = runner()
        settings.setNotificationMode(NotificationMode.DAILY_SUMMARY)

        runner.run()

        assertTrue(notifier.dailySummaries.isEmpty())
    }

    @Test
    fun `stores the run time and schedules the next run`() = runTest {
        val runner = runner()
        settings.setNotificationMode(NotificationMode.DAILY_SUMMARY)

        runner.run()

        assertEquals(NOW, settings.lastSummaryAt())
        assertEquals(listOf(SchedulePolicy.AFTER_CURRENT), scheduler.scheduled)
    }

    @Test
    fun `does nothing when the mode is no longer the daily summary`() = runTest {
        val runner = runner()
        settings.setNotificationMode(NotificationMode.EVERY_CALL)
        blockedCalls.record(number = "1", receivedAt = NOW - 1)

        runner.run()

        assertTrue(notifier.dailySummaries.isEmpty())
        assertTrue(scheduler.scheduled.isEmpty())
    }

    private companion object {
        const val LAST_SUMMARY = 1_000_000L
        const val NOW = 2_000_000L
    }
}
