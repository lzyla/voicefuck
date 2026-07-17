package com.aienglishcoach.feature.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aienglishcoach.core.designsystem.component.CoachPrimaryButton
import com.aienglishcoach.core.designsystem.component.CoachSecondaryButton
import com.aienglishcoach.core.designsystem.component.LevelChip
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.EnglishLevel
import com.aienglishcoach.core.domain.model.LearningGoal
import kotlinx.coroutines.launch

/**
 * Onboarding pager: welcome -> level -> goals -> microphone permission ->
 * API key. All steps are skippable-forward except the level selection.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    fun goNext() {
        scope.launch {
            if (pagerState.currentPage < PAGE_COUNT - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            } else {
                viewModel.finish(onFinished)
            }
        }
    }

    Scaffold { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding),
            userScrollEnabled = false,
        ) { page ->
            when (page) {
                0 -> WelcomePage(onNext = ::goNext)
                1 -> LevelPage(
                    selected = uiState.selectedLevel,
                    onSelect = viewModel::selectLevel,
                    onNext = ::goNext,
                )
                2 -> GoalsPage(
                    selectedGoals = uiState.selectedGoals,
                    dailyGoalMinutes = uiState.dailyGoalMinutes,
                    onToggleGoal = viewModel::toggleGoal,
                    onSelectDailyGoal = viewModel::selectDailyGoal,
                    onNext = ::goNext,
                )
                3 -> PermissionsPage(onNext = ::goNext)
                4 -> ApiKeyPage(
                    value = uiState.apiKeyInput,
                    isError = uiState.apiKeyError,
                    onValueChange = viewModel::onApiKeyChanged,
                    onNext = ::goNext,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    title: String,
    description: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.md),
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        content()
        Spacer(modifier = Modifier.height(Spacing.xl))
        CoachPrimaryButton(
            text = primaryLabel,
            onClick = onPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        if (secondaryLabel != null && onSecondary != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            CoachSecondaryButton(
                text = secondaryLabel,
                onClick = onSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_welcome_title),
        description = stringResource(R.string.onboarding_welcome_description),
        primaryLabel = stringResource(R.string.onboarding_start),
        onPrimary = onNext,
    ) {
        Icon(
            imageVector = Icons.Rounded.RecordVoiceOver,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp),
        )
    }
}

@Composable
private fun LevelPage(
    selected: EnglishLevel,
    onSelect: (EnglishLevel) -> Unit,
    onNext: () -> Unit,
) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_level_title),
        description = stringResource(R.string.onboarding_level_description),
        primaryLabel = stringResource(R.string.onboarding_next),
        onPrimary = onNext,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            EnglishLevel.entries.forEach { level ->
                LevelChip(
                    label = level.name,
                    selected = level == selected,
                    onClick = { onSelect(level) },
                )
            }
        }
    }
}

@Composable
private fun GoalsPage(
    selectedGoals: Set<LearningGoal>,
    dailyGoalMinutes: Int,
    onToggleGoal: (LearningGoal) -> Unit,
    onSelectDailyGoal: (Int) -> Unit,
    onNext: () -> Unit,
) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_goals_title),
        description = stringResource(R.string.onboarding_goals_description),
        primaryLabel = stringResource(R.string.onboarding_next),
        onPrimary = onNext,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            LearningGoal.entries.forEach { goal ->
                LevelChip(
                    label = goalLabel(goal),
                    selected = goal in selectedGoals,
                    onClick = { onToggleGoal(goal) },
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = stringResource(R.string.onboarding_daily_goal),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            DAILY_GOAL_OPTIONS.forEach { minutes ->
                LevelChip(
                    label = stringResource(R.string.onboarding_minutes, minutes),
                    selected = minutes == dailyGoalMinutes,
                    onClick = { onSelectDailyGoal(minutes) },
                )
            }
        }
    }
}

@Composable
private fun PermissionsPage(onNext: () -> Unit) {
    var requested by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Denial is allowed: the conversation screen re-asks with a rationale.
        requested = true
        onNext()
    }

    OnboardingPage(
        title = stringResource(R.string.onboarding_permissions_title),
        description = stringResource(R.string.onboarding_permissions_description),
        primaryLabel = stringResource(R.string.onboarding_grant_microphone),
        onPrimary = {
            if (requested) onNext() else launcher.launch(Manifest.permission.RECORD_AUDIO)
        },
        secondaryLabel = stringResource(R.string.onboarding_skip),
        onSecondary = onNext,
    ) {
        Icon(
            imageVector = Icons.Rounded.Mic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp),
        )
    }
}

@Composable
private fun ApiKeyPage(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_api_title),
        description = stringResource(R.string.onboarding_api_description),
        primaryLabel = stringResource(R.string.onboarding_finish),
        onPrimary = onNext,
        secondaryLabel = stringResource(R.string.onboarding_skip),
        onSecondary = onNext,
    ) {
        Icon(
            imageVector = Icons.Rounded.Key,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.onboarding_api_label)) },
            isError = isError,
            supportingText = if (isError) {
                { Text(stringResource(R.string.onboarding_api_error)) }
            } else {
                null
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun goalLabel(goal: LearningGoal): String = stringResource(
    when (goal) {
        LearningGoal.WORK -> R.string.goal_work
        LearningGoal.TRAVEL -> R.string.goal_travel
        LearningGoal.EMIGRATION -> R.string.goal_emigration
        LearningGoal.EXAMS -> R.string.goal_exams
        LearningGoal.FLUENCY -> R.string.goal_fluency
        LearningGoal.SOCIAL -> R.string.goal_social
    },
)

private const val PAGE_COUNT = 5
private val DAILY_GOAL_OPTIONS = listOf(5, 10, 15, 30)
