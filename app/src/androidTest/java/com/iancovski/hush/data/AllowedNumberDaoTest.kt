package com.iancovski.hush.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented test names use camelCase: spaces in method names need DEX 040 (API 30), and minSdk is 29.
@RunWith(AndroidJUnit4::class)
class AllowedNumberDaoTest {

    private lateinit var database: HushDatabase
    private lateinit var dao: AllowedNumberDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, HushDatabase::class.java).build()
        dao = database.allowedNumberDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun allowed(number: String, normalized: String, createdAt: Long) =
        AllowedNumber(number = number, normalizedNumber = normalized, name = null, createdAt = createdAt)

    @Test
    fun numbersAreObservedNewestFirst() = runTest {
        dao.insert(allowed("1", "+551", createdAt = 1_000L))
        dao.insert(allowed("2", "+552", createdAt = 2_000L))

        assertEquals(listOf("2", "1"), dao.observeAll().first().map { it.number })
    }

    @Test
    fun aDuplicateNormalizedNumberIsIgnored() = runTest {
        val firstId = dao.insert(allowed("(11) 3000-1234", "+551130001234", createdAt = 1_000L))

        val secondId = dao.insert(allowed("+55 11 3000-1234", "+551130001234", createdAt = 2_000L))

        assertNotEquals(-1L, firstId)
        assertEquals(-1L, secondId)
        assertEquals(listOf("(11) 3000-1234"), dao.observeAll().first().map { it.number })
    }

    @Test
    fun updateChangesTheEntry() = runTest {
        val id = dao.insert(allowed("(11) 3000-1243", "+551130001243", createdAt = 1_000L))

        val updated = dao.update(id, number = "(11) 3000-1234", normalizedNumber = "+551130001234", name = "Clínica")

        assertEquals(1, updated)
        val entry = dao.observeAll().first().single()
        assertEquals("(11) 3000-1234", entry.number)
        assertEquals("+551130001234", entry.normalizedNumber)
        assertEquals("Clínica", entry.name)
    }

    @Test
    fun updateIntoANumberOfAnotherEntryIsIgnored() = runTest {
        dao.insert(allowed("(11) 3000-1234", "+551130001234", createdAt = 1_000L))
        val otherId = dao.insert(allowed("(21) 3000-5678", "+552130005678", createdAt = 2_000L))

        val updated = dao.update(otherId, number = "11 3000-1234", normalizedNumber = "+551130001234", name = null)

        assertEquals(0, updated)
        assertEquals("(21) 3000-5678", dao.observeAll().first().single { it.id == otherId }.number)
    }

    @Test
    fun deleteRemovesOnlyThatNumber() = runTest {
        val keptId = dao.insert(allowed("1", "+551", createdAt = 1_000L))
        val removedId = dao.insert(allowed("2", "+552", createdAt = 2_000L))

        dao.delete(removedId)

        assertEquals(listOf(keptId), dao.observeAll().first().map { it.id })
    }
}
