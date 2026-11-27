package com.iancovski.hush

sealed interface MainUiState {

    /** Settings are still being read, so the first screen is not known yet. */
    data object Loading : MainUiState

    data class Ready(val setupCompleted: Boolean) : MainUiState
}
