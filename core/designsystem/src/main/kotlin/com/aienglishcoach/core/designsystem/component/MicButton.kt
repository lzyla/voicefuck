package com.aienglishcoach.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/** Voice interaction states of the conversation microphone button. */
enum class MicState { Idle, Listening, Processing, Speaking }

/**
 * The primary voice control of the app. A pulsing halo communicates the
 * active state; [level] (0..1, mic RMS) modulates the halo while listening.
 */
@Composable
fun MicButton(
    state: MicState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    level: Float = 0f,
    enabled: Boolean = true,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic-pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == MicState.Listening || state == MicState.Speaking) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mic-pulse-scale",
    )

    val containerColor by animateColorAsState(
        targetValue = when (state) {
            MicState.Idle -> MaterialTheme.colorScheme.primary
            MicState.Listening -> MaterialTheme.colorScheme.error
            MicState.Processing -> MaterialTheme.colorScheme.surfaceVariant
            MicState.Speaking -> MaterialTheme.colorScheme.secondary
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "mic-color",
    )

    // A brief spring "pop" on every state change (physics-based motion, e.g.
    // idle -> listening): snap slightly oversized, then spring back to rest.
    // The continuous halo pulse above stays tween-based since
    // infiniteRepeatable requires a DurationBasedAnimationSpec and cannot use
    // spring() directly.
    val statePop = remember { Animatable(1f) }
    LaunchedEffect(state) {
        statePop.snapTo(1.12f)
        statePop.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }

    Box(modifier = modifier.size(96.dp), contentAlignment = Alignment.Center) {
        // Halo behind the button; grows with voice level while listening.
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(pulse + level * 0.25f)
                .background(containerColor.copy(alpha = 0.25f), CircleShape),
        )
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(72.dp)
                .scale(statePop.value)
                .background(containerColor, CircleShape),
        ) {
            Icon(
                imageVector = when (state) {
                    MicState.Idle -> Icons.Rounded.Mic
                    MicState.Listening -> Icons.Rounded.GraphicEq
                    MicState.Processing -> Icons.Rounded.HourglassTop
                    MicState.Speaking -> Icons.Rounded.VolumeUp
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
