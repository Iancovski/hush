package com.iancovski.hush.data

import androidx.datastore.preferences.core.longPreferencesKey
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val lastSummaryAtKey = longPreferencesKey("last_summary_at")

    @Test
    fun `settings use the defaults when nothing is stored`() = runTest {
        val repository = SettingsRepository(testDataStore(folder))

        val settings = repository.settings.first()

        assertTrue(settings.blockingEnabled)
        assertEquals(NotificationMode.EVERY_CALL, settings.notificationMode)
        assertFalse(settings.setupCompleted)
    }

    @Test
    fun `completing the setup stores the mode and marks the setup as done`() = runTest {
        val repository = SettingsRepository(testDataStore(folder))

        repository.completeSetup(NotificationMode.NEVER)

        val settings = repository.settings.first()
        assertEquals(NotificationMode.NEVER, settings.notificationMode)
        assertTrue(settings.setupCompleted)
        assertTrue(settings.blockingEnabled)
    }

    @Test
    fun `turning blocking off is stored`() = runTest {
        val repository = SettingsRepository(testDataStore(folder))

        repository.setBlockingEnabled(false)

        assertFalse(repository.settings.first().blockingEnabled)
    }

    @Test
    fun `picking the daily summary stores now as the last summary time`() = runTest {
        val dataStore = testDataStore(folder)
        val repository = SettingsRepository(dataStore, currentTimeMillis = { 1_234L })

        repository.completeSetup(NotificationMode.DAILY_SUMMARY)

        assertEquals(1_234L, dataStore.data.first()[lastSummaryAtKey])
    }

    @Test
    fun `picking another mode does not store a last summary time`() = runTest {
        val dataStore = testDataStore(folder)
        val repository = SettingsRepository(dataStore, currentTimeMillis = { 1_234L })

        repository.completeSetup(NotificationMode.EVERY_CALL)

        assertNull(dataStore.data.first()[lastSummaryAtKey])
    }
}
