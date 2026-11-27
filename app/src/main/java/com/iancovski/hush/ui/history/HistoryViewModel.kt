package com.iancovski.hush.ui.history

import android.database.SQLException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.BlockedCallRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val blockedCallRepository: BlockedCallRepository) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = blockedCallRepository.blockedCalls
        .map { calls ->
            HistoryUiState(
                isLoading = false,
                calls = calls.map { HistoryItem(id = it.id, number = it.number, receivedAt = it.receivedAt) },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    fun onClearHistory() {
        viewModelScope.launch {
            try {
                blockedCallRepository.clear()
            } catch (_: SQLException) {
                // Nothing was deleted, so the list keeps showing the stored calls.
            }
        }
    }

    companion object {
        fun factory(blockedCallRepository: BlockedCallRepository) = viewModelFactory {
            initializer { HistoryViewModel(blockedCallRepository) }
        }
    }
}
