package com.iancovski.hush.ui.allowlist

import android.database.SQLException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.AllowlistRepository
import com.iancovski.hush.data.SaveAllowedNumberResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllowlistViewModel(private val allowlistRepository: AllowlistRepository) : ViewModel() {

    private val numberDialog = MutableStateFlow<NumberDialogState?>(null)
    private val pendingRemoval = MutableStateFlow<AllowlistItem?>(null)

    val uiState: StateFlow<AllowlistUiState> = combine(
        allowlistRepository.allowedNumbers,
        numberDialog,
        pendingRemoval,
    ) { numbers, dialog, removal ->
        AllowlistUiState(
            isLoading = false,
            numbers = numbers.map { AllowlistItem(id = it.id, number = it.number, name = it.name) },
            numberDialog = dialog,
            pendingRemoval = removal,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AllowlistUiState(),
    )

    fun onAddClick() {
        numberDialog.value = NumberDialogState()
    }

    fun onEditClick(item: AllowlistItem) {
        numberDialog.value = NumberDialogState(editing = item)
    }

    /** Clears the error as soon as the user edits the number again. */
    fun onDialogInputChange() {
        numberDialog.update { it?.copy(error = null) }
    }

    fun onDialogDismiss() {
        numberDialog.value = null
    }

    fun onDialogConfirm(number: String, name: String) {
        val dialog = numberDialog.value ?: return
        if (dialog.isSaving) return
        numberDialog.value = dialog.copy(isSaving = true, error = null)
        viewModelScope.launch {
            val result = try {
                val editing = dialog.editing
                if (editing == null) {
                    allowlistRepository.add(number, name)
                } else {
                    allowlistRepository.update(editing.id, number, name)
                }
            } catch (_: SQLException) {
                null
            }
            when (result) {
                SaveAllowedNumberResult.Saved -> numberDialog.value = null
                SaveAllowedNumberResult.InvalidNumber -> showDialogError(NumberError.INVALID_NUMBER)
                SaveAllowedNumberResult.AlreadyOnList -> showDialogError(NumberError.ALREADY_ON_LIST)
                // Nothing was saved: keep the dialog open so the user can try again.
                null -> numberDialog.update { it?.copy(isSaving = false) }
            }
        }
    }

    fun onRemoveClick(item: AllowlistItem) {
        pendingRemoval.value = item
    }

    fun onRemoveDismiss() {
        pendingRemoval.value = null
    }

    fun onRemoveConfirm() {
        val item = pendingRemoval.value ?: return
        pendingRemoval.value = null
        viewModelScope.launch {
            try {
                allowlistRepository.remove(item.id)
            } catch (_: SQLException) {
                // Nothing was deleted, so the list keeps showing the number.
            }
        }
    }

    private fun showDialogError(error: NumberError) {
        numberDialog.update { it?.copy(isSaving = false, error = error) }
    }

    companion object {
        fun factory(allowlistRepository: AllowlistRepository) = viewModelFactory {
            initializer { AllowlistViewModel(allowlistRepository) }
        }
    }
}
