package com.iancovski.hush.screening

import com.iancovski.hush.data.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CallScreenerTest {

    private class FakeContactLookup(
        private val savedNumbers: Set<String> = emptySet(),
        private val readable: Boolean = true,
    ) : ContactLookup {
        override fun isSavedContact(number: String): Boolean? = if (readable) number in savedNumbers else null
    }

    private fun TestScope.screener(
        settings: Flow<Settings> = MutableStateFlow(Settings()),
        contacts: ContactLookup = FakeContactLookup(savedNumbers = setOf(SAVED_NUMBER)),
    ) = CallScreener(SettingsSnapshot(settings, backgroundScope), contacts)

    private fun incoming(number: String?) = IncomingCall(isIncoming = true, number = number)

    @Test
    fun `unknown number is blocked when blocking is on`() = runTest {
        val decision = screener().screen(incoming(UNKNOWN_NUMBER))

        assertEquals(ScreeningDecision.Block, decision)
    }

    @Test
    fun `saved contact is allowed`() = runTest {
        val decision = screener().screen(incoming(SAVED_NUMBER))

        assertEquals(ScreeningDecision.Allow, decision)
    }

    @Test
    fun `hidden number is blocked when blocking is on`() = runTest {
        val decision = screener().screen(incoming(number = null))

        assertEquals(ScreeningDecision.Block, decision)
    }

    @Test
    fun `every incoming call is allowed when blocking is off`() = runTest {
        val screener = screener(settings = MutableStateFlow(Settings(blockingEnabled = false)))

        assertEquals(ScreeningDecision.Allow, screener.screen(incoming(UNKNOWN_NUMBER)))
        assertEquals(ScreeningDecision.Allow, screener.screen(incoming(number = null)))
    }

    @Test
    fun `outgoing call is allowed`() = runTest {
        val decision = screener().screen(IncomingCall(isIncoming = false, number = UNKNOWN_NUMBER))

        assertEquals(ScreeningDecision.Allow, decision)
    }

    @Test
    fun `call is allowed when contacts cannot be read`() = runTest {
        val screener = screener(contacts = FakeContactLookup(readable = false))

        assertEquals(ScreeningDecision.Allow, screener.screen(incoming(UNKNOWN_NUMBER)))
    }

    @Test
    fun `call is allowed when settings are not loaded in time`() = runTest {
        val screener = screener(settings = emptyFlow())

        assertEquals(ScreeningDecision.Allow, screener.screen(incoming(UNKNOWN_NUMBER)))
    }

    private companion object {
        const val SAVED_NUMBER = "+5511987654321"
        const val UNKNOWN_NUMBER = "+5511900000000"
    }
}
