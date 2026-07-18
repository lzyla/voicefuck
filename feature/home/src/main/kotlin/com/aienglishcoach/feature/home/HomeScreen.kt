package com.aienglishcoach.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.glass.AuroraBackground
import com.aienglishcoach.core.designsystem.glass.GlassIconCircle
import com.aienglishcoach.core.designsystem.glass.GlassPanel
import com.aienglishcoach.core.designsystem.glass.GlassPrimaryButton
import com.aienglishcoach.core.designsystem.glass.GlassProgressBar
import com.aienglishcoach.core.designsystem.glass.GlassStatPill
import com.aienglishcoach.core.designsystem.glass.GlassTokens
import com.aienglishcoach.core.designsystem.glass.ScreenHeading

/**
 * Dashboard: streak, daily-goal progress, quick conversation start and
 * due-revision shortcuts — reskinned to the liquid-glass visual language.
 */
@Composable
fun HomeScreen(
    onStartConversation: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenVocabularyReview: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenConversations: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Scaffold(containerColor = Color.Transparent) { padding ->
            when (val state = uiState) {
                HomeUiState.Loading -> Unit
                is HomeUiState.Ready -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
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
                        ScreenHeading(title = stringResource(R.string.home_title))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassIconCircle(
                                size = 44.dp,
                                background = GlassTokens.PanelFill,
                                onClick = onOpenConversations,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Chat,
                                    contentDescription = stringResource(R.string.home_conversation_history),
                                    tint = GlassTokens.TextPrimary,
                                )
                            }
                            GlassIconCircle(
                                size = 44.dp,
                                background = GlassTokens.PanelFill,
                                onClick = onOpenSettings,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = stringResource(R.string.home_settings),
                                    tint = GlassTokens.TextPrimary,
                                )
                            }
                        }
                    }

                    DailyGoalCard(
                        todayMinutes = state.summary.todayMinutes,
                        goalMinutes = state.summary.dailyGoalMinutes,
                        onStartConversation = onStartConversation,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(GlassTokens.CardGap)) {
                        GlassStatPill(
                            label = stringResource(R.string.home_streak),
                            value = state.summary.streakDays.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        GlassStatPill(
                            label = stringResource(R.string.home_due_exercises),
                            value = state.summary.dueExercises.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        GlassStatPill(
                            label = stringResource(R.string.home_due_words),
                            value = state.summary.dueVocabulary.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (state.summary.dueExercises > 0) {
                        PromptCard(
                            text = stringResource(
                                R.string.home_exercises_prompt,
                                state.summary.dueExercises,
                            ),
                            onClick = onOpenExercises,
                        )
                    }
                    if (state.summary.dueVocabulary > 0) {
                        PromptCard(
                            text = stringResource(
                                R.string.home_vocabulary_prompt,
                                state.summary.dueVocabulary,
                            ),
                            onClick = onOpenVocabularyReview,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyGoalCard(
    todayMinutes: Int,
    goalMinutes: Int,
    onStartConversation: () -> Unit,
) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val progress = if (goalMinutes > 0) todayMinutes.toFloat() / goalMinutes else 0f
            Text(
                text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                color = GlassTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
            )
            Text(
                text = stringResource(R.string.home_progress, todayMinutes, goalMinutes),
                color = GlassTokens.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            com.aienglishcoach.core.designsystem.glass.GlassProgressBar(
                progress = progress,
                fillColor = GlassTokens.Accent,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (todayMinutes >= goalMinutes) {
                    stringResource(R.string.home_goal_reached)
                } else {
                    stringResource(R.string.home_goal_encouragement)
                },
                color = GlassTokens.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            GlassPrimaryButton(
                text = stringResource(R.string.home_start_conversation),
                onClick = onStartConversation,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PromptCard(text: String, onClick: () -> Unit) {
    GlassPanel(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Quiz,
                contentDescription = null,
                tint = GlassTokens.Accent,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, color = GlassTokens.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
