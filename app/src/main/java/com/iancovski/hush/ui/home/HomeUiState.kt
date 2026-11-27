package com.iancovski.hush.ui.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val blockingEnabled: Boolean = false,
    /** Blocked calls on each of the last seven days, oldest first, today last. */
    val blockedPerDay: List<Int> = emptyList(),
) {
    val blockedLastSevenDays: Int
        get() = blockedPerDay.sum()
}
