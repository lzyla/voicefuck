package com.aienglishcoach.feature.learningpath

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
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
import com.aienglishcoach.core.designsystem.glass.GlassPanel
import com.aienglishcoach.core.designsystem.glass.GlassStatPill
import com.aienglishcoach.core.designsystem.glass.GlassTokens
import com.aienglishcoach.core.designsystem.glass.ScreenHeading

@Composable
fun LearningPathScreen(
    onOpenLesson: (String) -> Unit,
    viewModel: LearningPathViewModel = hiltViewModel(),
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
                ScreenHeading(title = stringResource(R.string.path_title))
                GlassStatPill(label = stringResource(R.string.path_xp_label), value = uiState.totalXp.toString())
            }

            uiState.units.forEach { unit ->
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(unit.title, color = GlassTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            unit.subtitle,
                            color = GlassTokens.TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                        )
                        unit.lessons.forEach { lesson ->
                            val state = uiState.lessonStates[lesson.id] ?: LessonState.LOCKED
                            LessonRow(
                                title = lesson.title,
                                xpReward = lesson.xpReward,
                                state = state,
                                onClick = { if (state != LessonState.LOCKED) onOpenLesson(lesson.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(
    title: String,
    xpReward: Int,
    state: LessonState,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = state != LessonState.LOCKED, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = when (state) {
                LessonState.LOCKED -> Icons.Rounded.Lock
                LessonState.UNLOCKED -> Icons.Rounded.PlayCircle
                LessonState.COMPLETED -> Icons.Rounded.CheckCircle
            },
            contentDescription = null,
            tint = when (state) {
                LessonState.LOCKED -> GlassTokens.Locked
                LessonState.UNLOCKED -> GlassTokens.Accent
                LessonState.COMPLETED -> GlassTokens.Success
            },
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (state == LessonState.LOCKED) GlassTokens.TextTertiary else GlassTokens.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Text(
                stringResource(R.string.path_xp_reward, xpReward),
                color = GlassTokens.TextTertiary,
                fontSize = 12.sp,
            )
        }
    }
}
