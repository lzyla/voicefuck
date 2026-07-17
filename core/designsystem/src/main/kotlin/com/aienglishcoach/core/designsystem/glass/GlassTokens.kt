package com.aienglishcoach.core.designsystem.glass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design tokens for the "liquid glass" visual language used by the
 * curriculum/community-facing screens (onboarding, home, path, chat,
 * profile, paywall) — a distinct look from the Material 3 [CoachTheme][
 * com.aienglishcoach.core.designsystem.theme.CoachTheme] used by the core
 * conversation/practice flows. See `design_handoff_ai_english_coach/README.md`
 * (2026-07-17 handoff) for the source spec these values are transcribed from.
 *
 * Deliberate simplification: the reference spec uses CSS `backdrop-filter:
 * blur(...)` to blur whatever sits *behind* each glass panel. True backdrop
 * blur isn't available in stable Jetpack Compose without a third-party
 * dependency (e.g. Haze, which currently requires Kotlin 2.2.20 — newer than
 * this project's 2.0.21, so pulling it in risks a broken build). Since the
 * background behind these panels is always a smooth gradient (no fine
 * detail to blur away), a translucent fill + border + soft shadow reads as
 * near-identical to a blurred panel and is used instead everywhere below.
 */
object GlassTokens {

    // Accent
    val Accent = Color(0xFF8AA2F0)
    val AccentGlassFill = Color(0xFF8AA2F0).copy(alpha = 0.55f)
    val AccentGlassSoft = Color(0xFF8AA2F0).copy(alpha = 0.40f)

    // Glass panels
    val PanelFill = Color.White.copy(alpha = 0.15f)
    val PanelFillStrong = Color.White.copy(alpha = 0.20f)
    val PanelFillSubtle = Color.White.copy(alpha = 0.09f)
    val PanelBorder = Color.White.copy(alpha = 0.30f)
    val PanelBorderStrong = Color.White.copy(alpha = 0.7f)

    // Text
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.80f)
    val TextTertiary = Color.White.copy(alpha = 0.6f)

    // Semantic
    val Success = Color(0xFF7DFFB0)
    val SuccessHighlight = Color(0xFF7DFFB0).copy(alpha = 0.35f)
    val Destructive = Color(0xFFE85A6E).copy(alpha = 0.55f)
    val Locked = Color.White.copy(alpha = 0.5f)

    // Radii (see README "Border radius" table)
    val RadiusLargeCard = 22.dp
    val RadiusButton = 16.dp
    val RadiusChip = 18.dp
    val RadiusTabBar = 24.dp

    val ShapeLargeCard = RoundedCornerShape(RadiusLargeCard)
    val ShapeButton = RoundedCornerShape(RadiusButton)
    val ShapeChip = RoundedCornerShape(RadiusChip)
    val ShapeTabBar = RoundedCornerShape(RadiusTabBar)

    // Spacing
    val ScreenSidePadding = 20.dp
    val ScreenTopPadding = 64.dp
    val ScreenBottomPadding = 24.dp
    val CardGap = 12.dp
}
