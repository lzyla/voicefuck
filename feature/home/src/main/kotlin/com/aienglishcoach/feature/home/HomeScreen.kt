package com.aienglishcoach.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.CoachPrimaryButton
import com.aienglishcoach.core.designsystem.component.LoadingIndicator
import com.aienglishcoach.core.designsystem.component.ProgressRing
import com.aienglishcoach.core.designsystem.component.StatTile
import com.aienglishcoach.core.designsystem.theme.Spacing

/**
 * Dashboard: streak, daily-goal progress ring, quick conversation start and
 * due-revision shortcuts.
 */
@Composable
fun HomeScreen(
    onStartConversation: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenVocabularyReview: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.home_settings),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            HomeUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
            is HomeUiState.Ready -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                DailyGoalCard(
                    todayMinutes = state.summary.todayMinutes,
                    goalMinutes = state.summary.dailyGoalMinutes,
                    onStartConversation = onStartConversation,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatTile(
                        value = state.summary.streakDays.toString(),
                        label = stringResource(R.string.home_streak),
                        icon = Icons.Rounded.LocalFireDepartment,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        value = state.summary.dueExercises.toString(),
                        label = stringResource(R.string.home_due_exercises),
                        icon = Icons.Rounded.Quiz,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        value = state.summary.dueVocabulary.toString(),
                        label = stringResource(R.string.home_due_words),
                        icon = Icons.Rounded.Style,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (state.summary.dueExercises > 0) {
                    CoachCard(onClick = onOpenExercises) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                text = stringResource(
                                    R.string.home_exercises_prompt,
                                    state.summary.dueExercises,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
                if (state.summary.dueVocabulary > 0) {
                    CoachCard(onClick = onOpenVocabularyReview) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                text = stringResource(
                                    R.string.home_vocabulary_prompt,
                                    state.summary.dueVocabulary,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
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
    CoachCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProgressRing(
                progress = if (goalMinutes > 0) todayMinutes.toFloat() / goalMinutes else 0f,
                label = stringResource(R.string.home_progress, todayMinutes, goalMinutes),
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = if (todayMinutes >= goalMinutes) {
                    stringResource(R.string.home_goal_reached)
                } else {
                    stringResource(R.string.home_goal_encouragement)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            CoachPrimaryButton(
                text = stringResource(R.string.home_start_conversation),
                onClick = onStartConversation,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
