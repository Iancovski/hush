package com.iancovski.hush

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iancovski.hush.ui.navigation.HushNavDisplay
import com.iancovski.hush.ui.theme.HushTheme

class MainActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as HushApplication).container

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory(container.settingsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is always dark, so the system bars need light icons even when the system is in light mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            HushTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                // While loading, nothing is drawn: the window background already has the app color.
                val state = uiState
                if (state is MainUiState.Ready) {
                    HushNavDisplay(setupCompleted = state.setupCompleted, container = container)
                }
            }
        }
    }
}
