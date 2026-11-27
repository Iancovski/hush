package com.iancovski.hush.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.ZonedDateTime

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    blockedCallRepository: BlockedCallRepository,
    now: () -> ZonedDateTime = ZonedDateTime::now,
) : ViewModel() {

    // The days are computed when the screen starts observing, so coming back to Home after
    // midnight moves the week forward.
    private val blockedPerDay = flow { emitAll(blockedCallRepository.countsPerDay(lastSevenDayStarts(now()))) }

    val uiState: StateFlow<HomeUiState> = combine(settingsRepository.settings, blockedPerDay) { settings, perDay ->
        HomeUiState(isLoading = false, blockingEnabled = settings.blockingEnabled, blockedPerDay = perDay)
    }.stateIn(
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
        fun factory(settingsRepository: SettingsRepository, blockedCallRepository: BlockedCallRepository) =
            viewModelFactory {
                initializer { HomeViewModel(settingsRepository, blockedCallRepository) }
            }
    }
}
