package com.iancovski.hush.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.brandGradient

/** The launcher glyph on the brand gradient. Decorative: it always sits next to the app name. */
@Composable
internal fun HushLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(MaterialTheme.colorScheme.brandGradient),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Common layout of the wizard steps: scrollable content on top and the main action pinned to the
 * bottom, both kept clear of the system bars.
 */
@Composable
internal fun SetupStepScaffold(
    step: SetupStep,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    contentAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 32.dp, bottom = 16.dp),
                horizontalAlignment = contentAlignment,
                content = content,
            )
            SetupStepIndicator(
                step = step,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp),
            )
            Button(
                onClick = onActionClick,
                enabled = actionEnabled,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Steps of the wizard, in order. */
internal enum class SetupStep {
    WELCOME,
    PERMISSIONS,
    NOTIFICATIONS,
}

/**
 * One dot per step, all the same size. The current dot is the bright primary and the others a dark
 * outline, so they differ in lightness and not only in hue. Screen readers hear "Step 2 of 3" instead
 * of the dots, and each screen's title also tells the step.
 */
@Composable
internal fun SetupStepIndicator(
    step: SetupStep,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.setup_step_indicator, step.ordinal + 1, SetupStep.entries.size)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SetupStep.entries.forEach { entry ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (entry == step) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
            )
        }
    }
}

@Composable
internal fun SetupStepTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}
