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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aienglishcoach.core.designsystem.glass.AuroraBackground
import com.aienglishcoach.core.designsystem.glass.GlassIconCircle
import com.aienglishcoach.core.designsystem.glass.GlassPanel
import com.aienglishcoach.core.designsystem.glass.GlassPrimaryButton
import com.aienglishcoach.core.designsystem.glass.GlassSecondaryButton
import com.aienglishcoach.core.designsystem.glass.GlassTokens

/**
 * "Go Pro" paywall — static screen matching the design handoff. There is no
 * real Play Billing integration behind it; [onClose] is the only working
 * action (both the primary CTA and the close button just dismiss it).
 */
@Composable
fun PaywallScreen(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = GlassTokens.ScreenSidePadding,
                    vertical = GlassTokens.ScreenTopPadding,
                ),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                GlassIconCircle(size = 40.dp, background = GlassTokens.PanelFill, onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.paywall_close), tint = GlassTokens.TextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = GlassTokens.Accent,
                modifier = Modifier.height(56.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.paywall_title),
                color = GlassTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.paywall_subtitle),
                color = GlassTokens.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    PAYWALL_FEATURE_RES_IDS.forEach { res ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = GlassTokens.Success)
                            Text(
                                text = stringResource(res),
                                color = GlassTokens.TextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            GlassPrimaryButton(
                text = stringResource(R.string.paywall_cta),
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassSecondaryButton(
                text = stringResource(R.string.paywall_dismiss),
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val PAYWALL_FEATURE_RES_IDS = listOf(
    R.string.paywall_feature_unlimited_conversations,
    R.string.paywall_feature_advanced_scenarios,
    R.string.paywall_feature_detailed_feedback,
    R.string.paywall_feature_offline_lessons,
)
