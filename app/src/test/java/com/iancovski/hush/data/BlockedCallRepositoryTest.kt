package com.iancovski.hush.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BlockedCallRepositoryTest {

    private class FakeBlockedCallDao : BlockedCallDao {
        val calls = MutableStateFlow<List<BlockedCall>>(emptyList())

        override suspend fun insert(call: BlockedCall) {
            calls.value = calls.value + call
        }

        override fun observeAll(): Flow<List<BlockedCall>> = calls
    }

    private val dao = FakeBlockedCallDao()

    @Test
    fun `recording keeps the number as received and its normalized form`() = runTest {
        val repository = BlockedCallRepository(dao, normalizeNumber = { "+5511987654321" })

        repository.record(number = "(11) 98765-4321", receivedAt = 1_000L)

        assertEquals(
            listOf(BlockedCall(number = "(11) 98765-4321", normalizedNumber = "+5511987654321", receivedAt = 1_000L)),
            dao.calls.value,
        )
    }

    @Test
    fun `recording a hidden number stores no number`() = runTest {
        val repository = BlockedCallRepository(dao, normalizeNumber = { error("hidden numbers are not normalized") })

        repository.record(number = null, receivedAt = 1_000L)

        assertEquals(
            listOf(BlockedCall(number = null, normalizedNumber = null, receivedAt = 1_000L)),
            dao.calls.value,
        )
    }
}
