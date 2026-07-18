package com.aienglishcoach.feature.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aienglishcoach.core.designsystem.glass.AuroraBackground
import com.aienglishcoach.core.designsystem.glass.GlassPanel
import com.aienglishcoach.core.designsystem.glass.GlassPrimaryButton
import com.aienglishcoach.core.designsystem.glass.GlassSecondaryButton
import com.aienglishcoach.core.designsystem.glass.GlassSelectable
import com.aienglishcoach.core.designsystem.glass.GlassTokens
import com.aienglishcoach.core.designsystem.glass.ScreenHeading
import com.aienglishcoach.core.domain.model.EnglishLevel
import com.aienglishcoach.core.domain.model.LearningGoal
import kotlinx.coroutines.launch

/**
 * Onboarding pager, restyled to the "liquid glass" visual language from the
 * 2026-07-17 design handoff: welcome -> sign up -> placement test -> level
 * result -> goals & daily commitment -> microphone permission -> API key.
 * Sign-up is local-only (no real account backend exists yet); the placement
 * test is a static on-device question bank, not an adaptive item bank.
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

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Scaffold(containerColor = Color.Transparent) { padding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().padding(padding),
                userScrollEnabled = false,
            ) { page ->
                when (page) {
                    0 -> WelcomePage(onNext = ::goNext)
                    1 -> SignUpPage(
                        name = uiState.displayName,
                        onNameChange = viewModel::onDisplayNameChanged,
                        onNext = ::goNext,
                    )
                    2 -> PlacementTestPage(
                        answers = uiState.placementAnswers,
                        onAnswer = viewModel::answerPlacementQuestion,
                        onNext = {
                            viewModel.finishPlacementTest()
                            goNext()
                        },
                    )
                    3 -> LevelResultPage(
                        level = uiState.selectedLevel,
                        correctCount = uiState.placementCorrectCount,
                        onSelectLevel = viewModel::selectLevel,
                        onNext = ::goNext,
                    )
                    4 -> GoalsPage(
                        selectedGoals = uiState.selectedGoals,
                        dailyGoalMinutes = uiState.dailyGoalMinutes,
                        onToggleGoal = viewModel::toggleGoal,
                        onSelectDailyGoal = viewModel::selectDailyGoal,
                        onNext = ::goNext,
                    )
                    5 -> PermissionsPage(onNext = ::goNext)
                    6 -> ApiKeyPage(
                        value = uiState.apiKeyInput,
                        isError = uiState.apiKeyError,
                        onValueChange = viewModel::onApiKeyChanged,
                        onNext = ::goNext,
                    )
                }
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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = GlassTokens.ScreenSidePadding,
                vertical = GlassTokens.ScreenTopPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = GlassTokens.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            color = GlassTokens.TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        content()
        Spacer(modifier = Modifier.height(32.dp))
        GlassPrimaryButton(text = primaryLabel, onClick = onPrimary, modifier = Modifier.fillMaxWidth())
        if (secondaryLabel != null && onSecondary != null) {
            Spacer(modifier = Modifier.height(10.dp))
            GlassSecondaryButton(text = secondaryLabel, onClick = onSecondary, modifier = Modifier.fillMaxWidth())
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
            tint = GlassTokens.TextPrimary,
            modifier = Modifier.size(96.dp),
        )
    }
}

@Composable
private fun SignUpPage(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_signup_title),
        description = stringResource(R.string.onboarding_signup_description),
        primaryLabel = stringResource(R.string.onboarding_signup_continue),
        onPrimary = onNext,
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.onboarding_signup_name_label)) },
            singleLine = true,
            colors = glassTextFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PlacementTestPage(
    answers: Map<Int, Int>,
    onAnswer: (Int, Int) -> Unit,
    onNext: () -> Unit,
) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_placement_title),
        description = stringResource(
            R.string.onboarding_placement_description,
            PLACEMENT_QUESTIONS.size,
        ),
        primaryLabel = stringResource(R.string.onboarding_placement_see_result),
        onPrimary = onNext,
    ) {
        PLACEMENT_QUESTIONS.forEachIndexed { questionIndex, question ->
            GlassPanel(modifier = Modifier.fillMaxWidth().padding(bottom = GlassTokens.CardGap)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(
                            R.string.onboarding_placement_question_counter,
                            questionIndex + 1,
                            PLACEMENT_QUESTIONS.size,
                        ),
                        color = GlassTokens.TextTertiary,
                        fontSize = 11.sp,
                    )
                    Text(
                        text = question.prompt,
                        color = GlassTokens.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                    )
                    question.options.forEachIndexed { optionIndex, option ->
                        GlassSelectable(
                            selected = answers[questionIndex] == optionIndex,
                            onClick = { onAnswer(questionIndex, optionIndex) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        ) {
                            Text(
                                text = option,
                                color = GlassTokens.TextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelResultPage(
    level: EnglishLevel,
    correctCount: Int,
    onSelectLevel: (EnglishLevel) -> Unit,
    onNext: () -> Unit,
) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_level_result_title, level.name),
        description = stringResource(
            R.string.onboarding_level_result_description,
            correctCount,
            PLACEMENT_QUESTIONS.size,
        ),
        primaryLabel = stringResource(R.string.onboarding_level_result_continue),
        onPrimary = onNext,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EnglishLevel.entries.forEach { entry ->
                GlassSelectable(
                    selected = entry == level,
                    onClick = { onSelectLevel(entry) },
                ) {
                    Text(
                        text = entry.name,
                        color = GlassTokens.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LearningGoal.entries.forEach { goal ->
                GlassSelectable(
                    selected = goal in selectedGoals,
                    onClick = { onToggleGoal(goal) },
                ) {
                    Text(
                        text = goalLabel(goal),
                        color = GlassTokens.TextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.onboarding_daily_goal),
            color = GlassTokens.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DAILY_GOAL_OPTIONS.forEach { minutes ->
                GlassSelectable(
                    selected = minutes == dailyGoalMinutes,
                    onClick = { onSelectDailyGoal(minutes) },
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_minutes, minutes),
                        color = GlassTokens.TextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
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
            tint = GlassTokens.TextPrimary,
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
            tint = GlassTokens.TextPrimary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = glassTextFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun glassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = GlassTokens.TextPrimary,
    unfocusedTextColor = GlassTokens.TextPrimary,
    focusedBorderColor = GlassTokens.PanelBorderStrong,
    unfocusedBorderColor = GlassTokens.PanelBorder,
    focusedLabelColor = GlassTokens.TextSecondary,
    unfocusedLabelColor = GlassTokens.TextTertiary,
    cursorColor = GlassTokens.Accent,
)

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

private const val PAGE_COUNT = 7
private val DAILY_GOAL_OPTIONS = listOf(5, 10, 15, 30)
