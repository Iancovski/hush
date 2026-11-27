package com.iancovski.hush.testing

import com.iancovski.hush.data.BlockedCall
import com.iancovski.hush.data.BlockedCallDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [BlockedCallDao] that keeps Room's ordering: newest first. */
class FakeBlockedCallDao : BlockedCallDao {

    private var nextId = 1L
    val calls = MutableStateFlow<List<BlockedCall>>(emptyList())

    override suspend fun insert(call: BlockedCall) {
        calls.value = calls.value + call.copy(id = nextId++)
    }

    override fun observeAll(): Flow<List<BlockedCall>> =
        calls.map { list -> list.sortedByDescending { it.receivedAt } }

    override suspend fun deleteAll() {
        calls.value = emptyList()
    }
}
