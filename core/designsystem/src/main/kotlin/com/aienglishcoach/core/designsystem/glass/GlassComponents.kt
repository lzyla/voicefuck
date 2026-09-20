package com.aienglishcoach.core.designsystem.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Translucent frosted-look panel — the base building block of every screen. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.ShapeLargeCard,
    fill: Color = GlassTokens.PanelFill,
    border: Color = GlassTokens.PanelBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(fill, shape)
            .border(borderWidth, border, shape),
    ) {
        content()
    }
}

/** Selectable row/chip: option lists, goal rows, word-bank chips. */
@Composable
fun GlassSelectable(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.ShapeButton,
    content: @Composable () -> Unit,
) {
    val fill = if (selected) GlassTokens.AccentGlassSoft else GlassTokens.PanelFill
    val border = if (selected) GlassTokens.PanelBorderStrong else GlassTokens.PanelBorder
    val borderWidth = if (selected) 2.dp else 1.dp
    Box(
        modifier = modifier
            .background(fill, shape)
            .border(borderWidth, border, shape)
            .clickable(onClick = onClick),
    ) {
        content()
    }
}

@Composable
fun GlassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                if (enabled) GlassTokens.AccentGlassFill else GlassTokens.AccentGlassFill.copy(alpha = 0.3f),
                GlassTokens.ShapeButton,
            )
            .border(1.dp, GlassTokens.PanelBorderStrong.copy(alpha = 0.6f), GlassTokens.ShapeButton)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = GlassTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun GlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(GlassTokens.PanelFill, GlassTokens.ShapeButton)
            .border(1.dp, GlassTokens.PanelBorder, GlassTokens.ShapeButton)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = GlassTokens.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun GlassDarkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(Color.Black.copy(alpha = 0.25f), GlassTokens.ShapeButton)
            .border(1.dp, GlassTokens.PanelBorder, GlassTokens.ShapeButton)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = GlassTokens.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

/** Small pill used for stats (e.g. "Level B1", "5 day streak"). */
@Composable
fun GlassStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    GlassPanel(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, color = GlassTokens.TextSecondary, fontSize = 11.sp)
            Text(value, color = GlassTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}

/** A rounded translucent toggle switch (iOS/Android-agnostic look). */
@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackColor = if (checked) GlassTokens.Accent else Color.White.copy(alpha = 0.25f)
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 26.dp)
            .background(trackColor, CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(20.dp)
                .background(Color.White, CircleShape),
        )
    }
}

/** Text/option chip — used for word-bank tiles and settings disclosure hints. */
@Composable
fun GlassChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val fill = if (selected) GlassTokens.AccentGlassFill else GlassTokens.PanelFill
    val border = if (selected) GlassTokens.PanelBorderStrong else GlassTokens.PanelBorder
    Box(
        modifier = modifier
            .background(fill, GlassTokens.ShapeChip)
            .border(1.dp, border, GlassTokens.ShapeChip)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text,
            color = GlassTokens.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}

/** Circular icon control (mute / mic / end-call). */
@Composable
fun GlassIconCircle(
    size: Dp,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background, CircleShape)
            .border(1.dp, GlassTokens.PanelBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Section heading used at the top of most screens. */
@Composable
fun ScreenHeading(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(modifier = modifier) {
        Text(
            title,
            color = GlassTokens.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
        )
        if (subtitle != null) {
            Text(
                subtitle,
                color = GlassTokens.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Linear progress track matching the glass aesthetic. */
@Composable
fun GlassProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.White.copy(alpha = 0.25f),
    fillColor: Color = Color.White,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(trackColor, RoundedCornerShape(4.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .background(fillColor, RoundedCornerShape(4.dp)),
        )
    }
}
