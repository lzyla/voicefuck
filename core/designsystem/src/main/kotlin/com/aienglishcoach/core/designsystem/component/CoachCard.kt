package com.aienglishcoach.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Standard surface card; clickable variant when [onClick] is provided. */
@Composable
fun CoachCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = colors,
            elevation = elevation,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            colors = colors,
            elevation = elevation,
            content = content,
        )
    }
}
