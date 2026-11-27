package com.iancovski.hush.ui.setup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.HushTheme
import com.iancovski.hush.ui.theme.brandGradient

/** First wizard step: what Hush does. */
@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SetupStepScaffold(
        step = SetupStep.WELCOME,
        actionLabel = stringResource(R.string.setup_welcome_start),
        onActionClick = onStartClick,
        modifier = modifier,
        contentAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        HushLogo()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_name),
            // The app name is large text, so the gradient keeps enough contrast on the background.
            style = MaterialTheme.typography.displayMedium.copy(
                brush = MaterialTheme.colorScheme.brandGradient,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.setup_welcome_slogan),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(64.dp))
        Text(
            text = stringResource(R.string.setup_welcome_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {
    HushTheme {
        WelcomeScreen(onStartClick = {})
    }
}
