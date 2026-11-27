package com.iancovski.hush.ui.history

import com.iancovski.hush.ui.allowlist.NumberDialogState

data class HistoryUiState(
    val isLoading: Boolean = true,
    val calls: List<HistoryItem> = emptyList(),
    /** The dialog that adds a blocked number to the allowlist, or null when it is closed. */
    val allowlistDialog: NumberDialogState? = null,
    /** Confirms that a number was just added to the allowlist, until the message is shown. */
    val showAddedToAllowlist: Boolean = false,
)

data class HistoryItem(
    val id: Long,
    /** Null when the caller hid the number. */
    val number: String?,
    /** Epoch milliseconds (UTC). The screen converts it to local time. */
    val receivedAt: Long,
)
