package com.iancovski.hush.data

/** Blocked calls per day over seven consecutive days, oldest first, as counted by Room. */
data class DailyBlockedCounts(
    val day0: Int,
    val day1: Int,
    val day2: Int,
    val day3: Int,
    val day4: Int,
    val day5: Int,
    val day6: Int,
) {
    fun toList(): List<Int> = listOf(day0, day1, day2, day3, day4, day5, day6)
}
