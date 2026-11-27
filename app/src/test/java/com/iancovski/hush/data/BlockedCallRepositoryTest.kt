package com.iancovski.hush.data

import com.iancovski.hush.testing.FakeBlockedCallDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockedCallRepositoryTest {

    private val dao = FakeBlockedCallDao()

    @Test
    fun `recording keeps the number as received and its normalized form`() = runTest {
        val repository = BlockedCallRepository(dao, normalizeNumber = { "+5511987654321" })

        repository.record(number = "(11) 98765-4321", receivedAt = 1_000L)

        assertEquals(
            listOf(BlockedCall(id = 1, number = "(11) 98765-4321", normalizedNumber = "+5511987654321", receivedAt = 1_000L)),
            dao.calls.value,
        )
    }

    @Test
    fun `recording a hidden number stores no number`() = runTest {
        val repository = BlockedCallRepository(dao, normalizeNumber = { error("hidden numbers are not normalized") })

        repository.record(number = null, receivedAt = 1_000L)

        assertEquals(
            listOf(BlockedCall(id = 1, number = null, normalizedNumber = null, receivedAt = 1_000L)),
            dao.calls.value,
        )
    }

    @Test
    fun `clearing removes every blocked call`() = runTest {
        val repository = BlockedCallRepository(dao, normalizeNumber = { null })
        repository.record(number = "1", receivedAt = 1_000L)
        repository.record(number = "2", receivedAt = 2_000L)

        repository.clear()

        assertTrue(repository.blockedCalls.first().isEmpty())
    }
}
