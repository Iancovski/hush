package com.iancovski.hush.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class HomeViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = settingsRepository.settings
        .map { settings -> HomeUiState(isLoading = false, blockingEnabled = settings.blockingEnabled) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun onBlockingChange(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setBlockingEnabled(enabled)
            } catch (_: IOException) {
                // Nothing was saved, so the switch keeps showing the stored value.
            }
        }
    }

    companion object {
        fun factory(settingsRepository: SettingsRepository) = viewModelFactory {
            initializer { HomeViewModel(settingsRepository) }
        }
    }
}
