package com.iancovski.hush.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.HushTheme
import com.iancovski.hush.ui.theme.cardGradient
import com.iancovski.hush.ui.theme.cardHighlight
import com.iancovski.hush.ui.theme.success

/** Main screen: the switch that turns call blocking on or off. */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onBlockingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(24.dp))
            // The stored value arrives almost at once. Waiting for it avoids showing the wrong state.
            if (!uiState.isLoading) {
                ProtectionCard(
                    enabled = uiState.blockingEnabled,
                    onEnabledChange = onBlockingChange,
                )
            }
        }
    }
}

@Composable
private fun ProtectionCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    // Clipping before toggleable keeps the touch ripple inside the rounded corners.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.cardGradient)
            .border(width = 1.dp, brush = MaterialTheme.colorScheme.cardHighlight, shape = shape)
            .toggleable(value = enabled, role = Role.Switch, onValueChange = onEnabledChange)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Decorative: the title already says whether protection is on, so the color is not the only cue.
        Icon(
            painter = painterResource(if (enabled) R.drawable.ic_verified_user else R.drawable.ic_gpp_bad),
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    if (enabled) R.string.home_protection_on_title else R.string.home_protection_off_title,
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(
                    if (enabled) {
                        R.string.home_protection_on_description
                    } else {
                        R.string.home_protection_off_description
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(16.dp))
        // The whole card handles the click, so the switch only shows the state.
        Switch(checked = enabled, onCheckedChange = null)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeScreenEnabledPreview() {
    HushTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = false, blockingEnabled = true),
            onBlockingChange = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeScreenDisabledPreview() {
    HushTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = false, blockingEnabled = false),
            onBlockingChange = {},
        )
    }
}
