package com.iancovski.hush.ui.history

import android.database.SQLException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.AllowlistRepository
import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.data.SaveAllowedNumberResult
import com.iancovski.hush.ui.allowlist.NumberDialogState
import com.iancovski.hush.ui.allowlist.NumberError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val blockedCallRepository: BlockedCallRepository,
    private val allowlistRepository: AllowlistRepository,
) : ViewModel() {

    private val allowlistDialog = MutableStateFlow<NumberDialogState?>(null)
    private val showAddedToAllowlist = MutableStateFlow(false)

    val uiState: StateFlow<HistoryUiState> = combine(
        blockedCallRepository.blockedCalls,
        allowlistDialog,
        showAddedToAllowlist,
    ) { calls, dialog, added ->
        HistoryUiState(
            isLoading = false,
            calls = calls.map { HistoryItem(id = it.id, number = it.number, receivedAt = it.receivedAt) },
            allowlistDialog = dialog,
            showAddedToAllowlist = added,
        )
    }.stateIn(
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

    /** Opens the allowlist dialog with the number filled in, so the user can still name it. */
    fun onAddToAllowlistClick(item: HistoryItem) {
        val number = item.number ?: return
        allowlistDialog.value = NumberDialogState(prefilledNumber = number)
    }

    /** Clears the error as soon as the user edits the number again. */
    fun onAllowlistDialogInputChange() {
        allowlistDialog.update { it?.copy(error = null) }
    }

    fun onAllowlistDialogDismiss() {
        allowlistDialog.value = null
    }

    fun onAllowlistDialogConfirm(number: String, name: String) {
        val dialog = allowlistDialog.value ?: return
        if (dialog.isSaving) return
        allowlistDialog.value = dialog.copy(isSaving = true, error = null)
        viewModelScope.launch {
            val result = try {
                allowlistRepository.add(number, name)
            } catch (_: SQLException) {
                null
            }
            when (result) {
                SaveAllowedNumberResult.Saved -> {
                    allowlistDialog.value = null
                    showAddedToAllowlist.value = true
                }
                SaveAllowedNumberResult.InvalidNumber -> showDialogError(NumberError.INVALID_NUMBER)
                SaveAllowedNumberResult.AlreadyOnList -> showDialogError(NumberError.ALREADY_ON_LIST)
                // Nothing was saved: keep the dialog open so the user can try again.
                null -> allowlistDialog.update { it?.copy(isSaving = false) }
            }
        }
    }

    fun onAddedToAllowlistShown() {
        showAddedToAllowlist.value = false
    }

    private fun showDialogError(error: NumberError) {
        allowlistDialog.update { it?.copy(isSaving = false, error = error) }
    }

    companion object {
        fun factory(blockedCallRepository: BlockedCallRepository, allowlistRepository: AllowlistRepository) =
            viewModelFactory {
                initializer { HistoryViewModel(blockedCallRepository, allowlistRepository) }
            }
    }
}
