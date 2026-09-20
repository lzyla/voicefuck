package com.aienglishcoach.core.designsystem.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Full-bleed aurora/northern-lights gradient, generated procedurally rather
 * than from the reference bundle's stock image (that asset carries an
 * unlicensed Freepik preview watermark — see the handoff README, which
 * explicitly allows "generate an equivalent gradient" as the production
 * path). Painted once as the screen's base layer; glass panels sit on top.
 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Base: near-black navy.
            drawRect(color = Color(0xFF05070E))

            // Deep blue wash filling the middle of the screen.
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0F18),
                        Color(0xFF13236B),
                        Color(0xFF0D1140),
                    ),
                ),
            )

            // Cyan-blue glow, lower-left (matches the reference's left blob).
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3FA9F5).copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(w * 0.12f, h * 0.55f),
                    radius = w * 0.75f,
                ),
                radius = w * 0.75f,
                center = Offset(w * 0.12f, h * 0.55f),
            )

            // Violet glow, upper-right.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFB79AE0).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w * 0.92f, h * 0.28f),
                    radius = w * 0.65f,
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.92f, h * 0.28f),
            )

            // Cobalt blue glow, upper-middle (the reference's dominant band).
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3355E8).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.18f),
                    radius = w * 0.9f,
                ),
                radius = w * 0.9f,
                center = Offset(w * 0.5f, h * 0.18f),
            )

            // Deep purple pool, bottom.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3A1F6B).copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(w * 0.55f, h * 1.05f),
                    radius = w * 0.85f,
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.55f, h * 1.05f),
            )

            // Vignette to keep edges dark, matching the reference's corners.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFF05070E).copy(alpha = 0.55f)),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 1.05f,
                ),
            )
        }
    }
}
