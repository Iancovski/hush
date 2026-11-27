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
import com.iancovski.hush.ui.allowlist.AllowlistScreen
import com.iancovski.hush.ui.allowlist.AllowlistViewModel
import com.iancovski.hush.ui.history.HistoryRoute
import com.iancovski.hush.ui.history.HistoryViewModel
import com.iancovski.hush.ui.home.HomeRoute
import com.iancovski.hush.ui.home.HomeViewModel
import com.iancovski.hush.ui.notifications.NotificationSettingsRoute
import com.iancovski.hush.ui.notifications.NotificationSettingsViewModel
import com.iancovski.hush.ui.setup.NotificationsSetupRoute
import com.iancovski.hush.ui.setup.NotificationsSetupViewModel
import com.iancovski.hush.ui.setup.PermissionsRoute
import com.iancovski.hush.ui.setup.PermissionsViewModel
import com.iancovski.hush.ui.setup.WelcomeScreen

@Composable
fun HushNavDisplay(
    setupCompleted: Boolean,
    container: AppContainer,
    openHistoryRequested: Boolean,
    onHistoryOpened: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(if (setupCompleted) Home else SetupWelcome)

    // Finishing the wizard is saved in the settings, which flips setupCompleted. Replacing the whole
    // back stack keeps Back from returning to the wizard.
    LaunchedEffect(setupCompleted) {
        if (setupCompleted) backStack.replaceSetupWithHome()
    }

    // A notification was tapped: show the history, with Home under it so Back leads there.
    LaunchedEffect(openHistoryRequested, setupCompleted) {
        if (openHistoryRequested && setupCompleted) {
            backStack.showHistoryOverHome()
            onHistoryOpened()
        }
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
                    factory = NotificationsSetupViewModel.factory(container.notificationModeController),
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
                    factory = HomeViewModel.factory(
                        container.settingsRepository,
                        container.blockedCallRepository,
                        container.permissionsChecker,
                    ),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HomeRoute(
                    uiState = uiState,
                    onRefreshPermissions = viewModel::refreshPermissions,
                    onBlockingChange = viewModel::onBlockingChange,
                    onRoleResult = viewModel::onRoleResult,
                    onContactsResult = viewModel::onContactsResult,
                    onPermissionsGranted = viewModel::onPermissionsGranted,
                    onPermissionsDialogDismiss = viewModel::onPermissionsDialogDismiss,
                    onOpenHistory = { backStack.add(History) },
                    onOpenAllowlist = { backStack.add(Allowlist) },
                    onOpenNotificationSettings = { backStack.add(NotificationSettings) },
                )
            }
            entry<History> {
                val viewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.factory(container.blockedCallRepository, container.allowlistRepository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HistoryRoute(
                    uiState = uiState,
                    onBackClick = { backStack.removeLastOrNull() },
                    onClearHistory = viewModel::onClearHistory,
                    onAddToAllowlistClick = viewModel::onAddToAllowlistClick,
                    onAllowlistDialogInputChange = viewModel::onAllowlistDialogInputChange,
                    onAllowlistDialogConfirm = viewModel::onAllowlistDialogConfirm,
                    onAllowlistDialogDismiss = viewModel::onAllowlistDialogDismiss,
                    onAddedToAllowlistShown = viewModel::onAddedToAllowlistShown,
                )
            }
            entry<NotificationSettings> {
                val viewModel: NotificationSettingsViewModel = viewModel(
                    factory = NotificationSettingsViewModel.factory(
                        container.settingsRepository,
                        container.notificationModeController,
                    ),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NotificationSettingsRoute(
                    uiState = uiState,
                    onBackClick = { backStack.removeLastOrNull() },
                    onNotificationModeSelect = viewModel::onNotificationModeSelect,
                )
            }
            entry<Allowlist> {
                val viewModel: AllowlistViewModel = viewModel(
                    factory = AllowlistViewModel.factory(container.allowlistRepository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                AllowlistScreen(
                    uiState = uiState,
                    onBackClick = { backStack.removeLastOrNull() },
                    onAddClick = viewModel::onAddClick,
                    onEditClick = viewModel::onEditClick,
                    onDialogInputChange = viewModel::onDialogInputChange,
                    onDialogConfirm = viewModel::onDialogConfirm,
                    onDialogDismiss = viewModel::onDialogDismiss,
                    onRemoveClick = viewModel::onRemoveClick,
                    onRemoveConfirm = viewModel::onRemoveConfirm,
                    onRemoveDismiss = viewModel::onRemoveDismiss,
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

private fun NavBackStack<NavKey>.showHistoryOverHome() {
    if (lastOrNull() == History) return
    // Add before removing so the back stack is never empty, which NavDisplay does not allow.
    add(History)
    removeAll { it != History }
    add(0, Home)
}
