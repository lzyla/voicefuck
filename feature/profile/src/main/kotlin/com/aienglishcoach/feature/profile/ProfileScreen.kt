package com.aienglishcoach.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aienglishcoach.core.designsystem.glass.AuroraBackground
import com.aienglishcoach.core.designsystem.glass.GlassIconCircle
import com.aienglishcoach.core.designsystem.glass.GlassPanel
import com.aienglishcoach.core.designsystem.glass.GlassStatPill
import com.aienglishcoach.core.designsystem.glass.GlassTokens
import com.aienglishcoach.core.designsystem.glass.ScreenHeading

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenStatistics: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = GlassTokens.ScreenSidePadding,
                    vertical = GlassTokens.ScreenTopPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(GlassTokens.CardGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScreenHeading(title = stringResource(R.string.profile_title))
                GlassIconCircle(size = 44.dp, background = GlassTokens.PanelFill, onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.profile_settings), tint = GlassTokens.TextPrimary)
                }
            }

            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(GlassTokens.AccentGlassFill, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = GlassTokens.TextPrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.profile_level_badge, uiState.englishLevel.name),
                        color = GlassTokens.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(GlassTokens.CardGap)) {
                        GlassStatPill(
                            label = stringResource(R.string.profile_streak_label),
                            value = uiState.streakDays.toString(),
                        )
                    }
                }
            }

            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                fill = GlassTokens.AccentGlassSoft,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.profile_go_pro_title),
                            color = GlassTokens.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                        Text(
                            stringResource(R.string.profile_go_pro_subtitle),
                            color = GlassTokens.TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                    GlassIconCircle(size = 40.dp, background = GlassTokens.PanelFillStrong, onClick = onOpenPaywall) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = GlassTokens.TextPrimary)
                    }
                }
            }

            GlassPanel(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenStatistics)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(R.string.profile_progress_row),
                        color = GlassTokens.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }
            }

            Text(
                stringResource(R.string.profile_achievements_title),
                color = GlassTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            uiState.achievements.forEach { achievement ->
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = if (achievement.unlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = if (achievement.unlocked) GlassTokens.Success else GlassTokens.Locked,
                        )
                        Text(
                            text = stringResource(achievement.titleRes),
                            color = if (achievement.unlocked) GlassTokens.TextPrimary else GlassTokens.TextTertiary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}
