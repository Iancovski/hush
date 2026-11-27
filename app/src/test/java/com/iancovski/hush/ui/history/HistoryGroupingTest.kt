package com.iancovski.hush.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class HistoryGroupingTest {

    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    private fun call(id: Long, at: LocalDateTime, zone: ZoneId = saoPaulo) =
        HistoryItem(id = id, number = "$id", receivedAt = at.atZone(zone).toInstant().toEpochMilli())

    @Test
    fun `calls are grouped by local day keeping newest first`() {
        val calls = listOf(
            call(4, LocalDateTime.of(2026, 11, 27, 13, 32)),
            call(3, LocalDateTime.of(2026, 11, 27, 9, 5)),
            call(2, LocalDateTime.of(2026, 11, 25, 22, 40)),
            call(1, LocalDateTime.of(2026, 11, 25, 8, 0)),
        )

        val days = groupByDay(calls, saoPaulo)

        assertEquals(listOf(LocalDate.of(2026, 11, 27), LocalDate.of(2026, 11, 25)), days.map { it.date })
        assertEquals(listOf(listOf(4L, 3L), listOf(2L, 1L)), days.map { day -> day.calls.map { it.id } })
    }

    @Test
    fun `the day comes from the local time, not UTC`() {
        // 23:30 in São Paulo is already the next day in UTC.
        val lateCall = call(1, LocalDateTime.of(2026, 11, 26, 23, 30))

        val days = groupByDay(listOf(lateCall), saoPaulo)

        assertEquals(LocalDate.of(2026, 11, 26), days.single().date)
    }

    @Test
    fun `an empty history has no days`() {
        assertEquals(emptyList<HistoryDay>(), groupByDay(emptyList(), saoPaulo))
    }
}
