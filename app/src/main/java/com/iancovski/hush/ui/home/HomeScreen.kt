package com.iancovski.hush.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.HushTheme
import com.iancovski.hush.ui.theme.cardGradient
import com.iancovski.hush.ui.theme.cardHighlight
import com.iancovski.hush.ui.theme.success

/** Main screen: the switch that turns call blocking on or off, and the way into the other screens. */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onBlockingChange: (Boolean) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAllowlist: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
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
                Spacer(Modifier.height(16.dp))
                BlockedCallsCard(
                    blockedPerDay = uiState.blockedPerDay,
                    total = uiState.blockedLastSevenDays,
                    onClick = onOpenHistory,
                )
                Spacer(Modifier.height(16.dp))
                HomeCard(highlighted = false) {
                    HomeMenuItem(
                        iconRes = R.drawable.ic_history,
                        label = stringResource(R.string.history_title),
                        onClick = onOpenHistory,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    HomeMenuItem(
                        iconRes = R.drawable.ic_person,
                        label = stringResource(R.string.allowlist_title),
                        onClick = onOpenAllowlist,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    HomeMenuItem(
                        iconRes = R.drawable.ic_notifications,
                        label = stringResource(R.string.notification_settings_title),
                        onClick = onOpenNotificationSettings,
                    )
                }
            }
        }
    }
}

/**
 * Container of the Home cards. A highlighted card (the protection status) has the lilac gradient and
 * the elevation border; the others are flat, a little lighter than the background. Clickable content
 * goes inside it, so the touch ripple stays within the rounded corners.
 */
@Composable
private fun HomeCard(
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val background = if (highlighted) {
        Modifier
            .background(MaterialTheme.colorScheme.cardGradient)
            .border(width = 1.dp, brush = MaterialTheme.colorScheme.cardHighlight, shape = shape)
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(background),
        content = content,
    )
}

@Composable
private fun ProtectionCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeCard(highlighted = true, modifier = modifier) {
        Row(
            modifier = Modifier
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
}

/** How many calls were blocked in the last seven days, with a decorative chart. Opens the history. */
@Composable
private fun BlockedCallsCard(
    blockedPerDay: List<Int>,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeCard(highlighted = false, modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onClick)
                // Less at the bottom: the total's line already leaves empty space under its digits, so
                // equal padding would make the bottom look wider than the top.
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 13.dp),
        ) {
            // The chart sits on the baseline of the total: digits have nothing below it, so aligning
            // the bottoms of the boxes would leave the number looking higher than the bars.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alignBy(LastBaseline),
            ) {
                Text(
                    text = stringResource(R.string.home_blocked_calls_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_blocked_calls_period),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.width(16.dp))
            WeekChart(
                blockedPerDay = blockedPerDay,
                modifier = Modifier.alignBy { chart -> chart.measuredHeight },
            )
        }
    }
}

/**
 * One bar per day, oldest on the left. The busier the day, the taller and lighter its bar. Purely
 * decorative: the total next to it carries the information, so screen readers skip it.
 */
@Composable
private fun WeekChart(
    blockedPerDay: List<Int>,
    modifier: Modifier = Modifier,
) {
    val heights = remember(blockedPerDay) { barHeights(blockedPerDay) }
    val shades = remember(blockedPerDay) { barShades(blockedPerDay) }
    val quietColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        .compositeOver(MaterialTheme.colorScheme.surfaceContainerLow)
    val busyColor = MaterialTheme.colorScheme.secondary
    Row(
        modifier = modifier
            .height(64.dp)
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        heights.forEachIndexed { day, height ->
            Box(
                modifier = Modifier
                    .width(10.dp)
                    // A day without calls still shows a short stub, so the week keeps its shape.
                    .fillMaxHeight(height.coerceAtLeast(MIN_BAR_HEIGHT))
                    .clip(RoundedCornerShape(3.dp))
                    .background(lerp(quietColor, busyColor, shades[day])),
            )
        }
    }
}

private const val MIN_BAR_HEIGHT = 0.12f

/** A row that opens another screen: icon, label and a chevron. */
@Composable
private fun HomeMenuItem(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Both icons are decorative: the label says where the row leads.
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeScreenEnabledPreview() {
    HushTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = false, blockingEnabled = true, blockedPerDay = listOf(2, 5, 1, 4, 6, 9, 3)),
            onBlockingChange = {},
            onOpenHistory = {},
            onOpenAllowlist = {},
            onOpenNotificationSettings = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeScreenDisabledPreview() {
    HushTheme {
        HomeScreen(
            uiState = HomeUiState(isLoading = false, blockingEnabled = false, blockedPerDay = List(7) { 0 }),
            onBlockingChange = {},
            onOpenHistory = {},
            onOpenAllowlist = {},
            onOpenNotificationSettings = {},
        )
    }
}
