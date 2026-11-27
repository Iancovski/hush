package com.iancovski.hush.data

import com.iancovski.hush.testing.FakeAllowedNumberDao
import com.iancovski.hush.testing.fakeNormalizeNumber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AllowlistRepositoryTest {

    private val dao = FakeAllowedNumberDao()
    private val repository = AllowlistRepository(dao, ::fakeNormalizeNumber, currentTimeMillis = { 1_000L })

    @Test
    fun `a valid number is added as typed and normalized`() = runTest {
        val result = repository.add(number = " (11) 3000-1234 ", name = " Clínica ")

        assertEquals(SaveAllowedNumberResult.Saved, result)
        assertEquals(
            listOf(
                AllowedNumber(
                    id = 1,
                    number = "(11) 3000-1234",
                    normalizedNumber = "+551130001234",
                    name = "Clínica",
                    createdAt = 1_000L,
                ),
            ),
            dao.numbers.value,
        )
    }

    @Test
    fun `a blank name is stored as no name`() = runTest {
        repository.add(number = "11 3000-1234", name = "   ")

        assertEquals(null, dao.numbers.value.single().name)
    }

    @Test
    fun `a number that cannot be normalized is rejected`() = runTest {
        val result = repository.add(number = "1234", name = "")

        assertEquals(SaveAllowedNumberResult.InvalidNumber, result)
        assertTrue(dao.numbers.value.isEmpty())
    }

    @Test
    fun `the same number in another format is already added`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "")

        val result = repository.add(number = "+55 11 3000-1234", name = "Clínica")

        assertEquals(SaveAllowedNumberResult.AlreadyOnList, result)
        assertEquals(1, dao.numbers.value.size)
    }

    @Test
    fun `normalized numbers feed the screening`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "")
        repository.add(number = "+1 415 555 0100", name = "")

        assertEquals(setOf("+551130001234", "+14155550100"), repository.normalizedNumbers.first())
    }

    @Test
    fun `editing fixes the number and the name`() = runTest {
        repository.add(number = "(11) 3000-1243", name = "")
        val id = dao.numbers.value.single().id

        val result = repository.update(id, number = " (11) 3000-1234 ", name = " Clínica ")

        assertEquals(SaveAllowedNumberResult.Saved, result)
        val edited = dao.numbers.value.single()
        assertEquals("(11) 3000-1234", edited.number)
        assertEquals("+551130001234", edited.normalizedNumber)
        assertEquals("Clínica", edited.name)
    }

    @Test
    fun `editing only the name keeps the same number`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "")
        val id = dao.numbers.value.single().id

        val result = repository.update(id, number = "+55 11 3000-1234", name = "Clínica")

        assertEquals(SaveAllowedNumberResult.Saved, result)
        assertEquals("Clínica", dao.numbers.value.single().name)
    }

    @Test
    fun `editing into a number of another entry is rejected`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "Clínica")
        repository.add(number = "(21) 3000-5678", name = "Escola")
        val school = dao.numbers.value.single { it.name == "Escola" }

        val result = repository.update(school.id, number = "11 3000-1234", name = "Escola")

        assertEquals(SaveAllowedNumberResult.AlreadyOnList, result)
        assertEquals("(21) 3000-5678", dao.numbers.value.single { it.id == school.id }.number)
    }

    @Test
    fun `editing into an invalid number is rejected`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "")
        val id = dao.numbers.value.single().id

        val result = repository.update(id, number = "123", name = "")

        assertEquals(SaveAllowedNumberResult.InvalidNumber, result)
        assertEquals("(11) 3000-1234", dao.numbers.value.single().number)
    }

    @Test
    fun `a removed number leaves the list`() = runTest {
        repository.add(number = "(11) 3000-1234", name = "")

        repository.remove(dao.numbers.value.single().id)

        assertTrue(repository.allowedNumbers.first().isEmpty())
    }
}
