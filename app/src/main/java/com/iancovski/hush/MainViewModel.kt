package com.iancovski.hush

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Decides whether the app opens on the setup wizard or on Home. */
class MainViewModel(settingsRepository: SettingsRepository) : ViewModel() {

    val uiState: StateFlow<MainUiState> = settingsRepository.settings
        .map { settings -> settings.setupCompleted }
        .distinctUntilChanged()
        .map { setupCompleted -> MainUiState.Ready(setupCompleted) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState.Loading,
        )

    companion object {
        fun factory(settingsRepository: SettingsRepository) = viewModelFactory {
            initializer { MainViewModel(settingsRepository) }
        }
    }
}
