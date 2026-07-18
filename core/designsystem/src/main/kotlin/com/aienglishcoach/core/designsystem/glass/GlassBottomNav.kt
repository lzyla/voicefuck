package com.aienglishcoach.core.designsystem.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class GlassNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

/**
 * Floating 5-tab bar (Home · Path · Practice · Chat · Profile) matching the
 * handoff spec: 60dp tall, inset, blurred glass fill (approximated per
 * [GlassTokens] doc comment), active = full-opacity icon/label.
 */
@Composable
fun GlassBottomNav(
    items: List<GlassNavItem>,
    currentRoute: String?,
    onNavigate: (GlassNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(GlassTokens.PanelFillStrong, GlassTokens.ShapeTabBar)
            .border(1.dp, GlassTokens.PanelBorder, GlassTokens.ShapeTabBar),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        items.forEach { item ->
            val active = item.route == currentRoute
            Column(
                modifier = Modifier
                    .clickable { onNavigate(item) }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (active) Color.White else Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = item.label,
                    color = if (active) Color.White else Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}
