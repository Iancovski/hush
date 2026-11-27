package com.iancovski.hush.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iancovski.hush.AppContainer
import com.iancovski.hush.ui.history.HistoryScreen
import com.iancovski.hush.ui.history.HistoryViewModel
import com.iancovski.hush.ui.home.HomeScreen
import com.iancovski.hush.ui.home.HomeViewModel
import com.iancovski.hush.ui.setup.NotificationsSetupRoute
import com.iancovski.hush.ui.setup.NotificationsSetupViewModel
import com.iancovski.hush.ui.setup.PermissionsRoute
import com.iancovski.hush.ui.setup.PermissionsViewModel
import com.iancovski.hush.ui.setup.WelcomeScreen

@Composable
fun HushNavDisplay(
    setupCompleted: Boolean,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(if (setupCompleted) Home else SetupWelcome)

    // Finishing the wizard is saved in the settings, which flips setupCompleted. Replacing the whole
    // back stack keeps Back from returning to the wizard.
    LaunchedEffect(setupCompleted) {
        if (setupCompleted) backStack.replaceSetupWithHome()
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SetupWelcome> {
                WelcomeScreen(onStartClick = { backStack.add(SetupPermissions) })
            }
            entry<SetupPermissions> {
                val viewModel: PermissionsViewModel = viewModel(
                    factory = PermissionsViewModel.factory(container.permissionsChecker),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                PermissionsRoute(
                    uiState = uiState,
                    onRefresh = viewModel::refresh,
                    onRoleResult = viewModel::onRoleResult,
                    onContactsResult = viewModel::onContactsResult,
                    onContinue = { backStack.add(SetupNotifications) },
                )
            }
            entry<SetupNotifications> {
                val viewModel: NotificationsSetupViewModel = viewModel(
                    factory = NotificationsSetupViewModel.factory(container.settingsRepository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NotificationsSetupRoute(
                    uiState = uiState,
                    onModeSelect = viewModel::onModeSelected,
                    onFinish = viewModel::finish,
                )
            }
            entry<Home> {
                val viewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.factory(container.settingsRepository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    uiState = uiState,
                    onBlockingChange = viewModel::onBlockingChange,
                    onOpenHistory = { backStack.add(History) },
                )
            }
            entry<History> {
                val viewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.factory(container.blockedCallRepository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HistoryScreen(
                    uiState = uiState,
                    onBackClick = { backStack.removeLastOrNull() },
                    onClearHistory = viewModel::onClearHistory,
                )
            }
        },
    )
}

private fun NavBackStack<NavKey>.replaceSetupWithHome() {
    if (none { it is SetupKey }) return
    // Add before removing so the back stack is never empty, which NavDisplay does not allow.
    add(Home)
    removeAll { it != Home }
}
