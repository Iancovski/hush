package com.iancovski.hush.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface SaveAllowedNumberResult {
    data object Saved : SaveAllowedNumberResult
    data object InvalidNumber : SaveAllowedNumberResult
    data object AlreadyOnList : SaveAllowedNumberResult
}

/** Single source of truth for the allowlist. */
class AllowlistRepository(
    private val dao: AllowedNumberDao,
    private val normalizeNumber: (String) -> String?,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {

    /** Every allowed number, newest first. */
    val allowedNumbers: Flow<List<AllowedNumber>> = dao.observeAll()

    /** The E.164 forms on the list, which the call screening matches against. */
    val normalizedNumbers: Flow<Set<String>> =
        allowedNumbers.map { numbers -> numbers.mapTo(HashSet()) { it.normalizedNumber } }

    /** Adds a number. Only numbers that can be normalized are accepted, since matching needs it. */
    suspend fun add(number: String, name: String?): SaveAllowedNumberResult {
        val entry = Entry.of(number, name, normalizeNumber) ?: return SaveAllowedNumberResult.InvalidNumber
        val rowId = dao.insert(
            AllowedNumber(
                number = entry.number,
                normalizedNumber = entry.normalizedNumber,
                name = entry.name,
                createdAt = currentTimeMillis(),
            ),
        )
        return if (rowId == -1L) SaveAllowedNumberResult.AlreadyOnList else SaveAllowedNumberResult.Saved
    }

    /** Changes the number and name of an entry, with the same rules as [add]. */
    suspend fun update(id: Long, number: String, name: String?): SaveAllowedNumberResult {
        val entry = Entry.of(number, name, normalizeNumber) ?: return SaveAllowedNumberResult.InvalidNumber
        val updatedRows = dao.update(id, entry.number, entry.normalizedNumber, entry.name)
        return if (updatedRows == 0) SaveAllowedNumberResult.AlreadyOnList else SaveAllowedNumberResult.Saved
    }

    suspend fun remove(id: Long) {
        dao.delete(id)
    }

    /** What the user typed, cleaned up and normalized. */
    private data class Entry(val number: String, val normalizedNumber: String, val name: String?) {
        companion object {
            fun of(number: String, name: String?, normalizeNumber: (String) -> String?): Entry? {
                val typedNumber = number.trim()
                val normalized = normalizeNumber(typedNumber) ?: return null
                return Entry(typedNumber, normalized, name?.trim()?.takeIf { it.isNotEmpty() })
            }
        }
    }
}
