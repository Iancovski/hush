package com.iancovski.hush

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iancovski.hush.ui.navigation.HushNavDisplay
import com.iancovski.hush.ui.theme.HushTheme

class MainActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as HushApplication).container

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory(container.settingsRepository)
    }

    /** Set when a notification opened the app; the navigation shows the history and clears it. */
    private var openHistoryRequested by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is always dark, so the system bars need light icons even when the system is in light mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        // After a configuration change the restored back stack already shows where the user was.
        if (savedInstanceState == null) handleIntent(intent)
        setContent {
            HushTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                // While loading, nothing is drawn: the window background already has the app color.
                val state = uiState
                if (state is MainUiState.Ready) {
                    HushNavDisplay(
                        setupCompleted = state.setupCompleted,
                        container = container,
                        openHistoryRequested = openHistoryRequested,
                        onHistoryOpened = { openHistoryRequested = false },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_HISTORY, false) == true) openHistoryRequested = true
    }

    companion object {
        private const val EXTRA_OPEN_HISTORY = "com.iancovski.hush.OPEN_HISTORY"

        /** Opens the history, reusing the running app when there is one. */
        fun openHistoryIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_OPEN_HISTORY, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
