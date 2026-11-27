package com.iancovski.hush.testing

import com.iancovski.hush.data.AllowedNumber
import com.iancovski.hush.data.AllowedNumberDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [AllowedNumberDao] that keeps Room's unique normalized number and newest-first order. */
class FakeAllowedNumberDao : AllowedNumberDao {

    private var nextId = 1L
    val numbers = MutableStateFlow<List<AllowedNumber>>(emptyList())

    override suspend fun insert(number: AllowedNumber): Long {
        if (numbers.value.any { it.normalizedNumber == number.normalizedNumber }) return -1L
        val id = nextId++
        numbers.value = numbers.value + number.copy(id = id)
        return id
    }

    override suspend fun update(id: Long, number: String, normalizedNumber: String, name: String?): Int {
        val takenByAnother = numbers.value.any { it.normalizedNumber == normalizedNumber && it.id != id }
        if (takenByAnother || numbers.value.none { it.id == id }) return 0
        numbers.value = numbers.value.map {
            if (it.id == id) it.copy(number = number, normalizedNumber = normalizedNumber, name = name) else it
        }
        return 1
    }

    override fun observeAll(): Flow<List<AllowedNumber>> =
        numbers.map { list -> list.sortedByDescending { it.createdAt } }

    override suspend fun delete(id: Long) {
        numbers.value = numbers.value.filterNot { it.id == id }
    }
}
