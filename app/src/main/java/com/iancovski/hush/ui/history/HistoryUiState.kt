package com.iancovski.hush.ui.history

data class HistoryUiState(
    val isLoading: Boolean = true,
    val calls: List<HistoryItem> = emptyList(),
)

data class HistoryItem(
    val id: Long,
    /** Null when the caller hid the number. */
    val number: String?,
    /** Epoch milliseconds (UTC). The screen converts it to local time. */
    val receivedAt: Long,
)
