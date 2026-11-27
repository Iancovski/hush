package com.iancovski.hush.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

// Brand palette. Screens read colors from MaterialTheme.colorScheme, never from these values.
private val Purple = Color(0xFF7B5CFF)
private val Lilac = Color(0xFFA886FF)
private val DarkBackground = Color(0xFF1C1B24)
private val SoftWhite = Color(0xFFF5F5FA)

// Pure white gives the best contrast available on Purple (4.4:1, versus 4.0:1 for SoftWhite).
private val OnPurple = Color(0xFFFFFFFF)

// Status colors, light enough to stand out on the dark surfaces.
private val Green = Color(0xFF4ADE80)
private val Red = Color(0xFFF87171)

internal val HushColorScheme = darkColorScheme(
    primary = Purple,
    onPrimary = OnPurple,
    primaryContainer = Color(0xFF3D2C8F),
    onPrimaryContainer = Color(0xFFE8DEFF),
    inversePrimary = Color(0xFF5B3FD6),
    secondary = Lilac,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF3B3357),
    onSecondaryContainer = Color(0xFFEADFFF),
    tertiary = Lilac,
    onTertiary = DarkBackground,
    tertiaryContainer = Color(0xFF3B3357),
    onTertiaryContainer = Color(0xFFEADFFF),
    background = DarkBackground,
    onBackground = SoftWhite,
    surface = DarkBackground,
    onSurface = SoftWhite,
    surfaceVariant = Color(0xFF2E2D38),
    onSurfaceVariant = Color(0xFFC9C6D6),
    surfaceTint = Purple,
    inverseSurface = SoftWhite,
    inverseOnSurface = Color(0xFF2E2D38),
    outline = Color(0xFF8F8C9E),
    outlineVariant = Color(0xFF45434F),
    surfaceBright = Color(0xFF3A3945),
    surfaceDim = DarkBackground,
    surfaceContainerLowest = Color(0xFF16151D),
    surfaceContainerLow = Color(0xFF22212B),
    surfaceContainer = Color(0xFF26252F),
    surfaceContainerHigh = Color(0xFF2E2D38),
    surfaceContainerHighest = Color(0xFF383743),
    error = Red,
    onError = DarkBackground,
)

/** Brand gradient, from the main purple to lilac. */
val ColorScheme.brandGradient: Brush
    get() = Brush.linearGradient(listOf(primary, secondary))

/**
 * Background of highlighted cards: a faint lilac tint at the top, fading down into the card color.
 * The tint stays subtle so text and controls on the card keep their contrast.
 */
val ColorScheme.cardGradient: Brush
    get() = Brush.verticalGradient(
        listOf(secondary.copy(alpha = 0.15f).compositeOver(surfaceContainer), surfaceContainer),
    )

/**
 * Border of highlighted cards: a lavender line, brightest at the top edge and fading to a faint
 * outline on the sides and bottom, as if light hit the card from above. It gives the card a sense
 * of elevation without a shadow.
 */
val ColorScheme.cardHighlight: Brush
    get() = Brush.verticalGradient(
        0f to secondary.copy(alpha = 0.2f),
        0.4f to secondary.copy(alpha = 0.06f),
        1f to secondary.copy(alpha = 0.04f),
    )

/**
 * Positive status, such as active protection. Material 3 has no such role, and Hush has a single
 * fixed color scheme, so it is a constant.
 */
@Suppress("UnusedReceiverParameter")
val ColorScheme.success: Color
    get() = Green
