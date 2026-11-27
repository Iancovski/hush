package com.iancovski.hush.notifications

import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.testing.FakeNotifier
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BlockedCallAlertsTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val notifier = FakeNotifier()

    @Test
    fun `every call mode notifies each blocked call`() = runTest {
        val settings = SettingsRepository(testDataStore(folder))
        settings.setNotificationMode(NotificationMode.EVERY_CALL)

        BlockedCallAlerts(settings, notifier).onCallBlocked(number = "+5511900001111", receivedAt = 1_000L)

        assertEquals(listOf<Pair<String?, Long>>("+5511900001111" to 1_000L), notifier.blockedCalls)
    }

    @Test
    fun `other modes do not notify each call`() = runTest {
        val settings = SettingsRepository(testDataStore(folder))
        val alerts = BlockedCallAlerts(settings, notifier)

        settings.setNotificationMode(NotificationMode.DAILY_SUMMARY)
        alerts.onCallBlocked(number = "1", receivedAt = 1_000L)
        settings.setNotificationMode(NotificationMode.NEVER)
        alerts.onCallBlocked(number = "2", receivedAt = 2_000L)

        assertTrue(notifier.blockedCalls.isEmpty())
    }
}
