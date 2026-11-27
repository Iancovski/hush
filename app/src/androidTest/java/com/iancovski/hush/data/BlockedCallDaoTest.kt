package com.iancovski.hush.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented test names use camelCase: spaces in method names need DEX 040 (API 30), and minSdk is 29.
@RunWith(AndroidJUnit4::class)
class BlockedCallDaoTest {

    private lateinit var database: HushDatabase
    private lateinit var dao: BlockedCallDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, HushDatabase::class.java).build()
        dao = database.blockedCallDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertedCallsAreObservedNewestFirst() = runTest {
        dao.insert(BlockedCall(number = "1", normalizedNumber = "+551", receivedAt = 1_000L))
        dao.insert(BlockedCall(number = "3", normalizedNumber = "+553", receivedAt = 3_000L))
        dao.insert(BlockedCall(number = null, normalizedNumber = null, receivedAt = 2_000L))

        val calls = dao.observeAll().first()

        assertEquals(listOf(3_000L, 2_000L, 1_000L), calls.map { it.receivedAt })
        assertEquals(listOf("3", null, "1"), calls.map { it.number })
    }

    @Test
    fun deleteAllRemovesEveryCall() = runTest {
        dao.insert(BlockedCall(number = "1", normalizedNumber = "+551", receivedAt = 1_000L))
        dao.insert(BlockedCall(number = "2", normalizedNumber = "+552", receivedAt = 2_000L))

        dao.deleteAll()

        assertEquals(emptyList<BlockedCall>(), dao.observeAll().first())
    }

    @Test
    fun countSinceCountsOnlyLaterCalls() = runTest {
        dao.insert(BlockedCall(number = "1", normalizedNumber = null, receivedAt = 1_000L))
        dao.insert(BlockedCall(number = "2", normalizedNumber = null, receivedAt = 2_000L))
        dao.insert(BlockedCall(number = "3", normalizedNumber = null, receivedAt = 3_000L))

        assertEquals(2, dao.countSince(1_000L))
        assertEquals(0, dao.countSince(3_000L))
    }
}
