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
class ScreeningSnapshotTest {

    @Test
    fun `returns the latest settings and allowlist`() = runTest {
        val settings = MutableStateFlow(Settings())
        val allowed = MutableStateFlow(emptySet<String>())
        val snapshot = ScreeningSnapshot(settings, allowed, backgroundScope)
        assertEquals(ScreeningState(Settings(), emptySet()), snapshot.current())

        settings.value = Settings(blockingEnabled = false)
        allowed.value = setOf("+551130001234")
        // Lets the snapshot's collector pick up the change.
        runCurrent()

        assertEquals(ScreeningState(Settings(blockingEnabled = false), setOf("+551130001234")), snapshot.current())
    }

    @Test
    fun `waits for the first read when it is still running`() = runTest {
        val slowAllowlist = flow {
            delay(500)
            emit(setOf("+551130001234"))
        }
        val snapshot = ScreeningSnapshot(
            settings = MutableStateFlow(Settings()),
            allowedNumbers = slowAllowlist,
            scope = backgroundScope,
            firstLoadTimeoutMillis = 1_000,
        )

        val current = async { snapshot.current() }

        assertEquals(ScreeningState(Settings(), setOf("+551130001234")), current.await())
    }

    @Test
    fun `gives up when the first read takes too long`() = runTest {
        val snapshot = ScreeningSnapshot(
            settings = MutableStateFlow(Settings()),
            allowedNumbers = emptyFlow(),
            scope = backgroundScope,
            firstLoadTimeoutMillis = 1_000,
        )

        assertNull(snapshot.current())
    }
}
