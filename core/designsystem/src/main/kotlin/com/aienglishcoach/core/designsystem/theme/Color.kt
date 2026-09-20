package com.aienglishcoach.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Brand palette (see docs/07-ui-design-system.md):
 * primary   #4F46E5 (indigo)  – actions, active states, mic button
 * secondary #14B8A6 (teal)    – progress, success accents
 * tertiary  #F59E0B (amber)   – streaks, highlights
 * error     #DC2626
 *
 * Full stable Material 3 ColorScheme role set (lightColorScheme/darkColorScheme
 * already expose these on the current stable material3 1.4.0 — they are not
 * gated behind the still-alpha "Material 3 Expressive" theme APIs, see
 * docs/architecture.md for the adoption decision). "Fixed" roles are
 * deliberately theme-invariant per the M3 spec, so they are defined once and
 * reused by both LightColorScheme and DarkColorScheme.
 */

// Light
private val PrimaryLight = Color(0xFF4F46E5)
private val OnPrimaryLight = Color(0xFFFFFFFF)
private val PrimaryContainerLight = Color(0xFFE0E7FF)
private val OnPrimaryContainerLight = Color(0xFF1E1B4B)
private val SecondaryLight = Color(0xFF14B8A6)
private val OnSecondaryLight = Color(0xFFFFFFFF)
private val SecondaryContainerLight = Color(0xFFCCFBF1)
private val OnSecondaryContainerLight = Color(0xFF134E4A)
private val TertiaryLight = Color(0xFFF59E0B)
private val OnTertiaryLight = Color(0xFF3A2A00)
private val TertiaryContainerLight = Color(0xFFFEF3C7)
private val OnTertiaryContainerLight = Color(0xFF78350F)
private val ErrorLight = Color(0xFFDC2626)
private val OnErrorLight = Color(0xFFFFFFFF)
private val ErrorContainerLight = Color(0xFFFEE2E2)
private val OnErrorContainerLight = Color(0xFF7F1D1D)
private val BackgroundLight = Color(0xFFFAFAFF)
private val OnBackgroundLight = Color(0xFF1B1B23)
private val SurfaceLight = Color(0xFFFFFFFF)
private val OnSurfaceLight = Color(0xFF1B1B23)
private val SurfaceVariantLight = Color(0xFFE7E7F2)
private val OnSurfaceVariantLight = Color(0xFF474753)
private val OutlineLight = Color(0xFF787885)
private val OutlineVariantLight = Color(0xFFC7C7D6)
private val SurfaceDimLight = Color(0xFFDAD9E0)
private val SurfaceBrightLight = Color(0xFFFFFFFF)
private val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
private val SurfaceContainerLowLight = Color(0xFFF5F3FA)
private val SurfaceContainerLight = Color(0xFFEFEDF6)
private val SurfaceContainerHighLight = Color(0xFFE9E7F0)
private val SurfaceContainerHighestLight = Color(0xFFE3E1EA)
private val InverseSurfaceLight = Color(0xFF2F2F38)
private val InverseOnSurfaceLight = Color(0xFFF1F0F7)
private val InversePrimaryLight = Color(0xFFBAC2FF)

// Dark
private val PrimaryDark = Color(0xFFA5B4FC)
private val OnPrimaryDark = Color(0xFF272263)
private val PrimaryContainerDark = Color(0xFF3B3690)
private val OnPrimaryContainerDark = Color(0xFFE0E7FF)
private val SecondaryDark = Color(0xFF5EEAD4)
private val OnSecondaryDark = Color(0xFF00382F)
private val SecondaryContainerDark = Color(0xFF0F766E)
private val OnSecondaryContainerDark = Color(0xFFCCFBF1)
private val TertiaryDark = Color(0xFFFCD34D)
private val OnTertiaryDark = Color(0xFF422C00)
private val TertiaryContainerDark = Color(0xFFB45309)
private val OnTertiaryContainerDark = Color(0xFFFEF3C7)
private val ErrorDark = Color(0xFFFCA5A5)
private val OnErrorDark = Color(0xFF5C0A0A)
private val ErrorContainerDark = Color(0xFF991B1B)
private val OnErrorContainerDark = Color(0xFFFEE2E2)
private val BackgroundDark = Color(0xFF121218)
private val OnBackgroundDark = Color(0xFFE4E4EC)
private val SurfaceDark = Color(0xFF1C1C24)
private val OnSurfaceDark = Color(0xFFE4E4EC)
private val SurfaceVariantDark = Color(0xFF303040)
private val OnSurfaceVariantDark = Color(0xFFC5C5D2)
private val OutlineDark = Color(0xFF8F8F9E)
private val OutlineVariantDark = Color(0xFF47454F)
private val SurfaceDimDark = Color(0xFF121218)
private val SurfaceBrightDark = Color(0xFF38373F)
private val SurfaceContainerLowestDark = Color(0xFF0D0D12)
private val SurfaceContainerLowDark = Color(0xFF1A1920)
private val SurfaceContainerDark = Color(0xFF1E1D25)
private val SurfaceContainerHighDark = Color(0xFF29282F)
private val SurfaceContainerHighestDark = Color(0xFF34333B)
private val InverseSurfaceDark = Color(0xFFE3E1EA)
private val InverseOnSurfaceDark = Color(0xFF2F2F38)
private val InversePrimaryDark = Color(0xFF4F46E5)

// Fixed roles: theme-invariant by design (M3 spec) — shared by both schemes.
private val PrimaryFixed = Color(0xFFE0E7FF)
private val PrimaryFixedDim = Color(0xFFC7D2FE)
private val OnPrimaryFixed = Color(0xFF1E1B4B)
private val OnPrimaryFixedVariant = Color(0xFF4338CA)
private val SecondaryFixed = Color(0xFFCCFBF1)
private val SecondaryFixedDim = Color(0xFF7DD3C0)
private val OnSecondaryFixed = Color(0xFF134E4A)
private val OnSecondaryFixedVariant = Color(0xFF0F766E)
private val TertiaryFixed = Color(0xFFFEF3C7)
private val TertiaryFixedDim = Color(0xFFFCD34D)
private val OnTertiaryFixed = Color(0xFF78350F)
private val OnTertiaryFixedVariant = Color(0xFFB45309)
private val Scrim = Color(0xFF000000)

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = Scrim,
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = Scrim,
)
