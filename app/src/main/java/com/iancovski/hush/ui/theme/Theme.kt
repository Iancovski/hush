package com.iancovski.hush.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * App theme. Hush is always dark, with its brand palette and Inter. Dynamic color is not used,
 * because it would replace the brand colors with the wallpaper palette.
 */
@Composable
fun HushTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HushColorScheme,
        typography = HushTypography,
        content = content,
    )
}
