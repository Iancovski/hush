package com.iancovski.hush.ui.history

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The blocked calls of one local day, newest first. */
data class HistoryDay(
    val date: LocalDate,
    val calls: List<HistoryItem>,
)

/**
 * Groups calls by the day they arrived in [zone]. The calls come newest first, and both the days and
 * the calls inside each day keep that order.
 */
fun groupByDay(calls: List<HistoryItem>, zone: ZoneId): List<HistoryDay> =
    calls
        .groupBy { call -> Instant.ofEpochMilli(call.receivedAt).atZone(zone).toLocalDate() }
        .map { (date, callsOfDay) -> HistoryDay(date, callsOfDay) }
