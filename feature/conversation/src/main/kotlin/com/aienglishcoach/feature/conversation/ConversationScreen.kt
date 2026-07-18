package com.aienglishcoach.feature.conversation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.designsystem.component.ErrorBanner
import com.aienglishcoach.core.designsystem.component.LevelChip
import com.aienglishcoach.core.designsystem.component.MessageBubble
import com.aienglishcoach.core.designsystem.component.MicButton
import com.aienglishcoach.core.designsystem.component.MicState
import com.aienglishcoach.core.designsystem.glass.AuroraBackground
import com.aienglishcoach.core.designsystem.glass.GlassTokens
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.MessageRole

/**
 * Live voice conversation. Bottom half shows the transcript; the mic button
 * drives the voice loop. Long-press a tutor bubble to see a Polish translation.
 */
@Composable
fun ConversationScreen(
    onBack: () -> Unit,
    onConversationEnded: (Long) -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEndDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            uiState.title.ifBlank { stringResource(R.string.conversation_title) },
                            color = GlassTokens.TextPrimary,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.conversation_back),
                                tint = GlassTokens.TextPrimary,
                            )
                        }
                    },
                    actions = {
                        if (uiState.phase == ConversationPhase.Active) {
                            IconButton(onClick = { showEndDialog = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.StopCircle,
                                    contentDescription = stringResource(R.string.conversation_end),
                                    tint = GlassTokens.TextPrimary,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            when (uiState.phase) {
                ConversationPhase.ScenarioSelection -> ScenarioSelection(
                    onSelect = viewModel::selectScenario,
                    modifier = Modifier.padding(padding),
                )
                ConversationPhase.Active -> ActiveConversation(
                    uiState = uiState,
                    onMicTapped = viewModel::onMicTapped,
                    onTranslate = viewModel::onTranslateRequested,
                    onDismissError = viewModel::dismissError,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text(stringResource(R.string.conversation_end_title)) },
            text = { Text(stringResource(R.string.conversation_end_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndDialog = false
                        viewModel.onEndConversation(onConversationEnded)
                    },
                ) {
                    Text(stringResource(R.string.conversation_end_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text(stringResource(R.string.conversation_end_cancel))
                }
            },
        )
    }
}

@Composable
private fun ScenarioSelection(
    onSelect: (ConversationScenario) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.conversation_pick_scenario),
            style = MaterialTheme.typography.headlineSmall,
            color = GlassTokens.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            ConversationScenario.entries.forEach { scenario ->
                LevelChip(
                    label = scenarioLabel(scenario),
                    selected = false,
                    onClick = { onSelect(scenario) },
                )
            }
        }
    }
}

@Composable
private fun ActiveConversation(
    uiState: ConversationUiState,
    onMicTapped: () -> Unit,
    onTranslate: (com.aienglishcoach.core.domain.model.Message) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) onMicTapped()
    }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        uiState.error?.let { error ->
            ErrorBanner(
                message = errorMessage(error),
                actionLabel = stringResource(R.string.conversation_error_dismiss),
                onAction = onDismissError,
                modifier = Modifier.padding(Spacing.md),
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                MessageBubble(
                    text = message.content,
                    translation = message.translation,
                    isUser = message.role == MessageRole.USER,
                    onLongPress = { onTranslate(message) },
                )
            }
        }

        if (uiState.partialText.isNotBlank()) {
            Text(
                text = uiState.partialText,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTokens.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg),
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MicButton(
                    state = when (uiState.micState) {
                        MicUiState.Idle -> MicState.Idle
                        MicUiState.Listening -> MicState.Listening
                        MicUiState.Processing -> MicState.Processing
                        MicUiState.Speaking -> MicState.Speaking
                    },
                    level = uiState.voiceLevel,
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            onMicTapped()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                )
                Text(
                    text = stringResource(
                        when (uiState.micState) {
                            MicUiState.Idle -> R.string.conversation_hint_idle
                            MicUiState.Listening -> R.string.conversation_hint_listening
                            MicUiState.Processing -> R.string.conversation_hint_processing
                            MicUiState.Speaking -> R.string.conversation_hint_speaking
                        },
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = GlassTokens.TextSecondary,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}

@Composable
private fun scenarioLabel(scenario: ConversationScenario): String = stringResource(
    when (scenario) {
        ConversationScenario.FREE_TALK -> R.string.scenario_free_talk
        ConversationScenario.SMALL_TALK -> R.string.scenario_small_talk
        ConversationScenario.JOB_INTERVIEW -> R.string.scenario_job_interview
        ConversationScenario.BUSINESS_MEETING -> R.string.scenario_business_meeting
        ConversationScenario.TRAVEL -> R.string.scenario_travel
        ConversationScenario.RESTAURANT -> R.string.scenario_restaurant
        ConversationScenario.SHOPPING -> R.string.scenario_shopping
        ConversationScenario.HEALTH -> R.string.scenario_health
    },
)

@Composable
internal fun errorMessage(error: AppError): String = stringResource(
    when (error) {
        AppError.Network -> R.string.error_network
        AppError.MissingApiKey -> R.string.error_missing_api_key
        AppError.InvalidApiKey -> R.string.error_invalid_api_key
        AppError.RateLimited -> R.string.error_rate_limited
        is AppError.AiService -> R.string.error_ai_service
        is AppError.SpeechRecognition -> R.string.error_speech
        AppError.TextToSpeech -> R.string.error_tts
        AppError.MicrophonePermissionDenied -> R.string.error_mic_permission
        is AppError.Storage -> R.string.error_storage
        is AppError.Unknown -> R.string.error_unknown
    },
)
