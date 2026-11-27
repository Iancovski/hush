package com.iancovski.hush.ui.home

import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.testing.FakeBlockedCallDao
import com.iancovski.hush.testing.MainDispatcherRule
import com.iancovski.hush.testing.testDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

// DataStore reads and writes its file on a real thread, so these tests wait for the expected state
// instead of advancing virtual time. If that state never comes, runTest fails with a timeout.
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    private val zone = ZoneId.of("America/Sao_Paulo")
    private val now = ZonedDateTime.of(LocalDateTime.of(2026, 11, 27, 17, 0), zone)
    private val blockedCalls = BlockedCallRepository(FakeBlockedCallDao(), normalizeNumber = { null })
    private lateinit var settings: SettingsRepository

    private fun TestScope.createViewModel(): HomeViewModel {
        settings = SettingsRepository(testDataStore(folder))
        return HomeViewModel(settings, blockedCalls, now = { now })
    }

    private fun at(day: Int, hour: Int) =
        ZonedDateTime.of(LocalDateTime.of(2026, 11, day, hour, 0), zone).toInstant().toEpochMilli()

    @Test
    fun `starts loading and then shows blocking on with an empty week`() = runTest {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.uiState.first {
            it == HomeUiState(isLoading = false, blockingEnabled = true, blockedPerDay = List(7) { 0 })
        }
    }

    @Test
    fun `counts the calls blocked on each of the last seven days`() = runTest {
        blockedCalls.record(number = "old", receivedAt = at(day = 20, hour = 23)) // Eight days ago: left out.
        blockedCalls.record(number = "1", receivedAt = at(day = 21, hour = 9))
        blockedCalls.record(number = "2", receivedAt = at(day = 25, hour = 12))
        blockedCalls.record(number = "3", receivedAt = at(day = 27, hour = 8))
        blockedCalls.record(number = null, receivedAt = at(day = 27, hour = 16))
        val viewModel = createViewModel()

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(listOf(1, 0, 0, 0, 1, 0, 2), state.blockedPerDay)
        assertEquals(4, state.blockedLastSevenDays)
    }

    @Test
    fun `turning the switch off saves it and updates the state`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.onBlockingChange(false)

        settings.settings.first { !it.blockingEnabled }
        viewModel.uiState.first { !it.isLoading && !it.blockingEnabled }
    }
}
