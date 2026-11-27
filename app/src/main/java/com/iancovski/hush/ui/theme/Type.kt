package com.iancovski.hush.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.iancovski.hush.R

// Inter is bundled as a single variable font, so every weight comes from one file and nothing is downloaded.
@OptIn(ExperimentalTextApi::class)
private fun interFont(weight: FontWeight) = Font(
    resId = R.font.inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Inter = FontFamily(
    interFont(FontWeight.Normal),
    interFont(FontWeight.Medium),
    interFont(FontWeight.SemiBold),
    interFont(FontWeight.Bold),
)

private val Baseline = Typography()

private fun TextStyle.withInter() = copy(fontFamily = Inter)

/** Material 3 type scale with Inter in every style. */
internal val HushTypography = Typography(
    displayLarge = Baseline.displayLarge.withInter(),
    displayMedium = Baseline.displayMedium.withInter(),
    displaySmall = Baseline.displaySmall.withInter(),
    headlineLarge = Baseline.headlineLarge.withInter(),
    headlineMedium = Baseline.headlineMedium.withInter(),
    headlineSmall = Baseline.headlineSmall.withInter(),
    titleLarge = Baseline.titleLarge.withInter(),
    titleMedium = Baseline.titleMedium.withInter(),
    titleSmall = Baseline.titleSmall.withInter(),
    bodyLarge = Baseline.bodyLarge.withInter(),
    bodyMedium = Baseline.bodyMedium.withInter(),
    bodySmall = Baseline.bodySmall.withInter(),
    labelLarge = Baseline.labelLarge.withInter(),
    labelMedium = Baseline.labelMedium.withInter(),
    labelSmall = Baseline.labelSmall.withInter(),
)
