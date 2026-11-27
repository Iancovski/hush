package com.iancovski.hush.screening

import com.iancovski.hush.data.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsSnapshotTest {

    @Test
    fun `returns the latest settings`() = runTest {
        val settings = MutableStateFlow(Settings())
        val snapshot = SettingsSnapshot(settings, backgroundScope)
        assertEquals(Settings(), snapshot.current())

        settings.value = Settings(blockingEnabled = false)
        // Lets the snapshot's collector pick up the change.
        runCurrent()

        assertEquals(Settings(blockingEnabled = false), snapshot.current())
    }

    @Test
    fun `waits for the first read when it is still running`() = runTest {
        val slowSettings = flow {
            delay(500)
            emit(Settings(blockingEnabled = false))
        }
        val snapshot = SettingsSnapshot(slowSettings, backgroundScope, firstLoadTimeoutMillis = 1_000)

        val current = async { snapshot.current() }

        assertEquals(Settings(blockingEnabled = false), current.await())
    }

    @Test
    fun `gives up when the first read takes too long`() = runTest {
        val snapshot = SettingsSnapshot(emptyFlow(), backgroundScope, firstLoadTimeoutMillis = 1_000)

        assertNull(snapshot.current())
    }
}
