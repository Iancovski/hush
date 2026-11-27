package com.iancovski.hush.ui.allowlist

data class AllowlistUiState(
    val isLoading: Boolean = true,
    val numbers: List<AllowlistItem> = emptyList(),
    /** The add or edit dialog, or null when it is closed. */
    val numberDialog: NumberDialogState? = null,
    /** The number waiting for the user to confirm its removal. */
    val pendingRemoval: AllowlistItem? = null,
)

data class AllowlistItem(
    val id: Long,
    val number: String,
    val name: String?,
)

data class NumberDialogState(
    /** The entry being edited, or null when adding a new number. */
    val editing: AllowlistItem? = null,
    /** Number to start with when adding, for example one picked from the history. */
    val prefilledNumber: String? = null,
    val error: NumberError? = null,
    val isSaving: Boolean = false,
)

enum class NumberError {
    INVALID_NUMBER,
    ALREADY_ON_LIST,
}
