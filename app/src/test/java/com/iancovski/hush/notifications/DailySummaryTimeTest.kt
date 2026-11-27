package com.iancovski.hush.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DailySummaryTimeTest {

    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int, zone: ZoneId = saoPaulo) =
        ZonedDateTime.of(LocalDateTime.of(year, month, day, hour, minute), zone)

    @Test
    fun `before 8 PM the summary is today`() {
        assertEquals(Duration.ofHours(6).plusMinutes(30), delayUntilNextSummary(at(2026, 11, 27, 13, 30)))
    }

    @Test
    fun `after 8 PM the summary is tomorrow`() {
        assertEquals(Duration.ofHours(23), delayUntilNextSummary(at(2026, 11, 27, 21, 0)))
    }

    @Test
    fun `exactly at 8 PM the next summary is tomorrow`() {
        assertEquals(Duration.ofDays(1), delayUntilNextSummary(at(2026, 11, 27, 20, 0)))
    }

    @Test
    fun `a daylight saving change keeps the summary at 8 PM local time`() {
        // New York moves clocks forward at 2 AM on March 8, 2026, so that day has 23 hours.
        val newYork = ZoneId.of("America/New_York")

        val delay = delayUntilNextSummary(at(2026, 3, 7, 21, 0, newYork))

        assertEquals(Duration.ofHours(22), delay)
    }
}
