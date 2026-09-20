package com.aienglishcoach.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.CoachPrimaryButton
import com.aienglishcoach.core.designsystem.component.LevelChip
import com.aienglishcoach.core.designsystem.component.LoadingIndicator
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.EnglishLevel
import com.aienglishcoach.core.domain.model.LearningGoal
import com.aienglishcoach.core.domain.model.ThemeMode
import com.aienglishcoach.core.domain.model.TtsVoice
import com.aienglishcoach.core.domain.model.UserPreferences

/**
 * Settings screen: learning profile, tutor voice, AI configuration,
 * appearance and data management. Every change persists immediately.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showWipeDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                LearningProfileSection(
                    preferences = uiState.preferences,
                    onLevelSelected = viewModel::onLevelSelected,
                    onGoalToggled = viewModel::onGoalToggled,
                    onDailyGoalSelected = viewModel::onDailyGoalSelected,
                )
                TutorVoiceSection(
                    preferences = uiState.preferences,
                    onVoiceSelected = viewModel::onVoiceSelected,
                    onSpeechRateChanged = viewModel::onSpeechRateChanged,
                )
                AiSection(
                    uiState = uiState,
                    onApiKeyInputChanged = viewModel::onApiKeyInputChanged,
                    onSaveApiKey = viewModel::saveApiKey,
                    onModelSelected = viewModel::onModelSelected,
                )
                AppearanceSection(
                    preferences = uiState.preferences,
                    onThemeSelected = viewModel::onThemeSelected,
                    onDynamicColorChanged = viewModel::onDynamicColorChanged,
                    onRemindersChanged = viewModel::onRemindersChanged,
                )
                DataSection(onWipeRequested = { showWipeDialog = true })
            }
        }
    }

    if (showWipeDialog) {
        WipeConfirmationDialog(
            onConfirm = {
                showWipeDialog = false
                viewModel.wipeAllData()
            },
            onDismiss = { showWipeDialog = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LearningProfileSection(
    preferences: UserPreferences,
    onLevelSelected: (EnglishLevel) -> Unit,
    onGoalToggled: (LearningGoal) -> Unit,
    onDailyGoalSelected: (Int) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_profile)) {
        Text(
            text = stringResource(R.string.settings_english_level),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            EnglishLevel.entries.forEach { level ->
                LevelChip(
                    label = level.name,
                    selected = preferences.englishLevel == level,
                    onClick = { onLevelSelected(level) },
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_learning_goals),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            LearningGoal.entries.forEach { goal ->
                LevelChip(
                    label = goal.label(),
                    selected = goal in preferences.learningGoals,
                    onClick = { onGoalToggled(goal) },
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_daily_goal),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            DailyGoalOptions.forEach { minutes ->
                LevelChip(
                    label = stringResource(R.string.settings_minutes_chip, minutes),
                    selected = preferences.dailyGoalMinutes == minutes,
                    onClick = { onDailyGoalSelected(minutes) },
                )
            }
        }
    }
}

@Composable
private fun TutorVoiceSection(
    preferences: UserPreferences,
    onVoiceSelected: (TtsVoice) -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_voice)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            TtsVoice.entries.forEach { voice ->
                LevelChip(
                    label = voice.label(),
                    selected = preferences.ttsVoice == voice,
                    onClick = { onVoiceSelected(voice) },
                )
            }
        }
        var speechRate by remember(preferences.ttsSpeechRate) {
            mutableFloatStateOf(preferences.ttsSpeechRate)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.settings_speech_rate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.settings_speech_rate_value, speechRate),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Slider(
            value = speechRate,
            onValueChange = { speechRate = it },
            onValueChangeFinished = { onSpeechRateChanged(speechRate) },
            valueRange = SpeechRateRange,
        )
    }
}

@Composable
private fun AiSection(
    uiState: SettingsUiState,
    onApiKeyInputChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onModelSelected: (String) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_ai)) {
        OutlinedTextField(
            value = uiState.apiKeyInput,
            onValueChange = onApiKeyInputChanged,
            label = { Text(stringResource(R.string.settings_api_key_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = uiState.apiKeyError,
            supportingText = when {
                uiState.apiKeyError -> {
                    { Text(stringResource(R.string.settings_api_key_error)) }
                }
                uiState.apiKeySaved -> {
                    {
                        Text(
                            text = stringResource(R.string.settings_api_key_saved),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                else -> null
            },
            modifier = Modifier.fillMaxWidth(),
        )
        CoachPrimaryButton(
            text = stringResource(R.string.settings_api_key_save),
            onClick = onSaveApiKey,
            enabled = uiState.apiKeyInput.isNotBlank(),
        )
        Text(
            text = stringResource(R.string.settings_model_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            LevelChip(
                label = stringResource(R.string.settings_model_fast),
                selected = uiState.preferences.aiModel == ModelFast,
                onClick = { onModelSelected(ModelFast) },
            )
            LevelChip(
                label = stringResource(R.string.settings_model_accurate),
                selected = uiState.preferences.aiModel == ModelAccurate,
                onClick = { onModelSelected(ModelAccurate) },
            )
        }
    }
}

@Composable
private fun AppearanceSection(
    preferences: UserPreferences,
    onThemeSelected: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onRemindersChanged: (Boolean) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            ThemeMode.entries.forEach { theme ->
                LevelChip(
                    label = theme.label(),
                    selected = preferences.theme == theme,
                    onClick = { onThemeSelected(theme) },
                )
            }
        }
        SwitchRow(
            label = stringResource(R.string.settings_dynamic_color),
            checked = preferences.useDynamicColor,
            onCheckedChange = onDynamicColorChanged,
        )
        SwitchRow(
            label = stringResource(R.string.settings_reminders),
            checked = preferences.revisionRemindersEnabled,
            onCheckedChange = onRemindersChanged,
        )
    }
}

@Composable
private fun DataSection(onWipeRequested: () -> Unit) {
    SettingsSection(title = stringResource(R.string.settings_section_data)) {
        Button(
            onClick = onWipeRequested,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.settings_wipe_button))
        }
    }
}

@Composable
private fun WipeConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_wipe_dialog_title)) },
        text = { Text(stringResource(R.string.settings_wipe_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.settings_wipe_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_wipe_cancel))
            }
        },
    )
}

/** Card with a section title followed by its settings controls. */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    CoachCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LearningGoal.label(): String = stringResource(
    when (this) {
        LearningGoal.WORK -> R.string.settings_goal_work
        LearningGoal.TRAVEL -> R.string.settings_goal_travel
        LearningGoal.EMIGRATION -> R.string.settings_goal_emigration
        LearningGoal.EXAMS -> R.string.settings_goal_exams
        LearningGoal.FLUENCY -> R.string.settings_goal_fluency
        LearningGoal.SOCIAL -> R.string.settings_goal_social
    },
)

@Composable
private fun TtsVoice.label(): String = stringResource(
    when (this) {
        TtsVoice.US -> R.string.settings_voice_us
        TtsVoice.UK -> R.string.settings_voice_uk
    },
)

@Composable
private fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    },
)

private val DailyGoalOptions = listOf(5, 10, 15, 30)
private val SpeechRateRange = 0.5f..1.5f
private const val ModelFast = "gpt-4o-mini"
private const val ModelAccurate = "gpt-4o"
