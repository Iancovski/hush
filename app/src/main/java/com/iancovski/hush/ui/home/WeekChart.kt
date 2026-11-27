package com.iancovski.hush.ui.home

import com.iancovski.hush.data.DAYS_PER_WEEK
import java.time.ZonedDateTime

/**
 * The eight boundaries of the last seven local days, today included: the start of each day, oldest
 * first, and the start of tomorrow. Built with java.time, so a daylight saving day keeps its length.
 */
fun lastSevenDayStarts(now: ZonedDateTime): List<Long> {
    val firstDay = now.toLocalDate().minusDays(DAYS_PER_WEEK - 1L)
    return (0..DAYS_PER_WEEK).map { offset ->
        firstDay.plusDays(offset.toLong()).atStartOfDay(now.zone).toInstant().toEpochMilli()
    }
}

/** Height of each bar from 0 to 1, relative to the busiest day. */
fun barHeights(counts: List<Int>): List<Float> {
    val max = counts.maxOrNull() ?: 0
    return counts.map { if (max == 0) 0f else it.toFloat() / max }
}

/**
 * Shade of each bar from 0 (dark) to 1 (light), placing each day between the quietest and the
 * busiest day of the week. When every day has the same count, all bars share one shade.
 */
fun barShades(counts: List<Int>): List<Float> {
    val min = counts.minOrNull() ?: 0
    val max = counts.maxOrNull() ?: 0
    return counts.map { count ->
        when {
            max == min -> if (max == 0) 0f else 1f
            else -> (count - min).toFloat() / (max - min)
        }
    }
}
