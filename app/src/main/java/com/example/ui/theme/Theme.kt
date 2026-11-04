package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Semantic Storybook Palette ensuring WCAG AA/AAA contrast across both
 * Daylight Washi (Light Mode) and Sumi Night Lantern (Dark Mode).
 */
@Immutable
data class StorybookPalette(
    val isDark: Boolean,
    val pageBg: Color,
    val stageBg: Color,
    val cardSurface: Color,
    val subtleSurface: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val sketchBorder: Color,
    val subtleBorder: Color,
    val sageWash: Color,
    val onSageWash: Color,
    val sageAccent: Color,
    val coralWash: Color,
    val onCoralWash: Color,
    val coralAccent: Color,
    val skyWash: Color,
    val onSkyWash: Color,
    val skyAccent: Color,
    val ochreWash: Color,
    val onOchreWash: Color,
    val ochreAccent: Color,
    val lavenderWash: Color,
    val onLavenderWash: Color,
    val ctaBg: Color,
    val onCtaBg: Color
)

val LightStorybookPalette = StorybookPalette(
    isDark = false,
    pageBg = WashiPaperBg,
    stageBg = WashiWarmPeach,
    cardSurface = WashiCardPure,
    subtleSurface = WashiSurfaceSubtle,
    inkPrimary = SumiInk,
    inkSecondary = SumiInkSecondary,
    sketchBorder = EspressoStroke,
    subtleBorder = SumiRuleBorder,
    sageWash = BambooSurface,
    onSageWash = BambooInk,
    sageAccent = BambooGreen,
    coralWash = VermilionSurface,
    onCoralWash = VermilionInk,
    coralAccent = VermilionRed,
    skyWash = SoraBlueSurface,
    onSkyWash = SoraBlueInk,
    skyAccent = SoraBlue,
    ochreWash = OchreSurface,
    onOchreWash = OchreInk,
    ochreAccent = OchreGold,
    lavenderWash = LavenderMistSurface,
    onLavenderWash = LavenderMistInk,
    ctaBg = CoralPersimmon,
    onCtaBg = WashiCardPure
)

val DarkStorybookPalette = StorybookPalette(
    isDark = true,
    pageBg = SumiNightBg,
    stageBg = Color(0xFF211B16),
    cardSurface = SumiNightSurface,
    subtleSurface = SumiNightCard,
    inkPrimary = SumiNightInk,
    inkSecondary = SumiNightSecondary,
    sketchBorder = Color(0xFFE6D5C1),
    subtleBorder = SumiNightBorder,
    sageWash = Color(0xFF243823),
    onSageWash = Color(0xFFE2F0D8),
    sageAccent = Color(0xFF8CC288),
    coralWash = Color(0xFF45221C),
    onCoralWash = Color(0xFFFFDED7),
    coralAccent = Color(0xFFF28470),
    skyWash = Color(0xFF1D3447),
    onSkyWash = Color(0xFFD9EEFC),
    skyAccent = Color(0xFF84BEE3),
    ochreWash = Color(0xFF3E2F17),
    onOchreWash = Color(0xFFFCE9C7),
    ochreAccent = Color(0xFFE8AB5A),
    lavenderWash = Color(0xFF342742),
    onLavenderWash = Color(0xFFEEE3F7),
    ctaBg = Color(0xFFE56B4E),
    onCtaBg = Color(0xFFFFFDF9)
)

val LocalStorybookPalette = staticCompositionLocalOf { LightStorybookPalette }

private val LightStorybookScheme = lightColorScheme(
    primary = VermilionRed,
    onPrimary = Color.White,
    primaryContainer = VermilionSurface,
    onPrimaryContainer = VermilionInk,
    secondary = BambooGreen,
    onSecondary = Color.White,
    secondaryContainer = BambooSurface,
    onSecondaryContainer = BambooInk,
    tertiary = SoraBlue,
    onTertiary = Color.White,
    tertiaryContainer = SoraBlueSurface,
    onTertiaryContainer = SoraBlueInk,
    background = WashiPaperBg,
    onBackground = SumiInk,
    surface = WashiCardPure,
    onSurface = SumiInk,
    surfaceVariant = WashiSurfaceSubtle,
    onSurfaceVariant = SumiInkSecondary,
    outline = SumiRuleBorder,
    outlineVariant = Color(0xFFE8D8C3),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

private val DarkStorybookScheme = darkColorScheme(
    primary = Color(0xFFF28470),
    onPrimary = Color(0xFF2B0B06),
    primaryContainer = Color(0xFF5C1E14),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFF8CC288),
    onSecondary = Color(0xFF122911),
    secondaryContainer = Color(0xFF294527),
    onSecondaryContainer = BambooSurface,
    tertiary = Color(0xFF84BEE3),
    onTertiary = Color(0xFF0E293B),
    tertiaryContainer = Color(0xFF21445C),
    onTertiaryContainer = SoraBlueSurface,
    background = SumiNightBg,
    onBackground = SumiNightInk,
    surface = SumiNightSurface,
    onSurface = SumiNightInk,
    surfaceVariant = SumiNightCard,
    onSurfaceVariant = SumiNightSecondary,
    outline = SumiNightBorder,
    outlineVariant = Color(0xFF332A24)
)

val HojaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object HojaTheme {
    val palette: StorybookPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalStorybookPalette.current
}

@Composable
fun HojaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkStorybookScheme else LightStorybookScheme
    val storybookPalette = if (darkTheme) DarkStorybookPalette else LightStorybookPalette

    androidx.compose.runtime.CompositionLocalProvider(
        LocalStorybookPalette provides storybookPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = HojaShapes,
            content = content
        )
    }
}
