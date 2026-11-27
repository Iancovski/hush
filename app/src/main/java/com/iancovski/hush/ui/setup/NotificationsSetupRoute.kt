package com.iancovski.hush.ui.setup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.iancovski.hush.data.NotificationMode

/** Connects [NotificationsSetupScreen] to the Android notification permission dialog. */
@Composable
fun NotificationsSetupRoute(
    uiState: NotificationsSetupUiState,
    onModeSelect: (NotificationMode) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // Denying the permission does not block the setup: Hush then behaves as if NEVER was picked.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onFinish() }

    NotificationsSetupScreen(
        uiState = uiState,
        onModeSelect = onModeSelect,
        onFinishClick = {
            val needsPermission = uiState.selectedMode != NotificationMode.NEVER &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            if (needsPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onFinish()
            }
        },
        modifier = modifier,
    )
}
