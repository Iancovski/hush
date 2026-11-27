package com.iancovski.hush.notifications

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.testing.FakeSummaryScheduler
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NotificationModeControllerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scheduler = FakeSummaryScheduler()
    private lateinit var settings: SettingsRepository

    private fun TestScope.controller(): NotificationModeController {
        settings = SettingsRepository(testDataStore(folder), currentTimeMillis = { 1_234L })
        return NotificationModeController(settings, scheduler)
    }

    @Test
    fun `picking the daily summary stores now and replaces the schedule`() = runTest {
        val controller = controller()

        controller.setMode(NotificationMode.DAILY_SUMMARY)

        assertEquals(NotificationMode.DAILY_SUMMARY, settings.settings.first().notificationMode)
        assertEquals(1_234L, settings.lastSummaryAt())
        assertEquals(listOf(SchedulePolicy.REPLACE), scheduler.scheduled)
    }

    @Test
    fun `picking another mode cancels the daily summary`() = runTest {
        val controller = controller()

        controller.setMode(NotificationMode.NEVER)

        assertEquals(NotificationMode.NEVER, settings.settings.first().notificationMode)
        assertEquals(1, scheduler.cancellations)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `finishing the setup with the daily summary schedules it`() = runTest {
        val controller = controller()

        controller.completeSetup(NotificationMode.DAILY_SUMMARY)

        assertTrue(settings.settings.first().setupCompleted)
        assertEquals(listOf(SchedulePolicy.REPLACE), scheduler.scheduled)
    }

    @Test
    fun `app start keeps an existing daily summary schedule`() = runTest {
        val controller = controller()
        controller.setMode(NotificationMode.DAILY_SUMMARY)
        scheduler.scheduled.clear()

        controller.restoreSchedule()

        assertEquals(listOf(SchedulePolicy.KEEP), scheduler.scheduled)
    }

    @Test
    fun `app start schedules nothing for other modes`() = runTest {
        val controller = controller()
        controller.setMode(NotificationMode.EVERY_CALL)

        controller.restoreSchedule()

        assertTrue(scheduler.scheduled.isEmpty())
    }
}
