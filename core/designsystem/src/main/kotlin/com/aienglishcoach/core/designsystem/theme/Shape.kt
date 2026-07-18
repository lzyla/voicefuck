package com.aienglishcoach.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii, tuned toward the softer, more expressive proportions Google's
 * current Material 3 guidance favors (larger radii, friendlier feel) while
 * staying on the stable 5-token `Shapes` API — the expanded Expressive shape
 * scale (`extraSmallIncreased`, `MaterialShapes`, …) only exists in the still
 * -alpha material3 1.5.0 line, see docs/architecture.md for the decision to
 * defer adopting it until it stabilizes.
 *
 * Chips/small controls: 8dp. Cards: 16dp. Sheets/dialogs: 20dp.
 * Buttons/pills and the mic button use CircleShape directly, not this scale.
 */
val CoachShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
