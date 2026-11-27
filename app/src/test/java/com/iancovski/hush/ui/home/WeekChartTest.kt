package com.iancovski.hush.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class WeekChartTest {

    @Test
    fun `the week covers the last seven local days, today included`() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 11, 27, 17, 13), zone)

        val starts = lastSevenDayStarts(now)

        val expected = (21..28).map { day ->
            ZonedDateTime.of(LocalDateTime.of(2026, 11, day, 0, 0), zone).toInstant().toEpochMilli()
        }
        assertEquals(expected, starts)
    }

    @Test
    fun `a daylight saving day keeps its real length`() {
        // New York moves clocks forward on March 8, 2026, so that day lasts 23 hours.
        val zone = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 3, 10, 12, 0), zone)

        val starts = lastSevenDayStarts(now)

        val dayLengths = starts.zipWithNext { start, end -> Duration.ofMillis(end - start).toHours() }
        assertEquals(listOf(24L, 24L, 24L, 24L, 23L, 24L, 24L), dayLengths)
    }

    @Test
    fun `bars are as tall as their share of the busiest day`() {
        assertEquals(listOf(0.5f, 1f, 0f, 0.25f), barHeights(listOf(2, 4, 0, 1)))
    }

    @Test
    fun `a week without calls has flat bars`() {
        assertEquals(List(7) { 0f }, barHeights(List(7) { 0 }))
    }

    @Test
    fun `the quietest day is darkest and the busiest is lightest`() {
        assertEquals(listOf(0.25f, 1f, 0f, 0.5f), barShades(listOf(3, 6, 2, 4)))
    }

    @Test
    fun `days with the same count share a shade`() {
        assertEquals(List(7) { 1f }, barShades(List(7) { 3 }))
        assertEquals(List(7) { 0f }, barShades(List(7) { 0 }))
    }
}
