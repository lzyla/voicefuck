package com.aienglishcoach.feature.practice.pronunciation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.ErrorBanner
import com.aienglishcoach.core.designsystem.component.MicButton
import com.aienglishcoach.core.designsystem.component.MicState
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.PronunciationResult
import com.aienglishcoach.feature.practice.R
import com.aienglishcoach.feature.practice.errorMessage

/** Score at or above which an attempt is shown in the "good" color. */
private const val GOOD_SCORE_THRESHOLD = 85

/**
 * Pronunciation trainer: read the target phrase into the mic, get a 0-100
 * score with feedback, listen to the model pronunciation and browse recent
 * attempts.
 */
@Composable
fun PronunciationScreen(
    viewModel: PronunciationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onMicTapped()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.pronunciation_title)) })
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            uiState.error?.let { error ->
                ErrorBanner(
                    message = errorMessage(error),
                    actionLabel = stringResource(R.string.practice_error_dismiss),
                    onAction = viewModel::dismissError,
                    modifier = Modifier.padding(Spacing.md),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item(key = "phrase") {
                    PhraseCard(
                        phrase = uiState.phrase,
                        onListen = viewModel::onListenTapped,
                        onNextPhrase = viewModel::nextPhrase,
                    )
                }
                item(key = "mic") {
                    MicSection(
                        isListening = uiState.isListening,
                        isEvaluating = uiState.isEvaluating,
                        voiceLevel = uiState.voiceLevel,
                        onTap = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO,
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                viewModel.onMicTapped()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                    )
                }
                uiState.lastResult?.let { result ->
                    item(key = "result") {
                        ResultCard(result = result)
                    }
                }
                item(key = "history-header") {
                    Text(
                        text = stringResource(R.string.pronunciation_history_header),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (uiState.history.isEmpty()) {
                    item(key = "history-empty") {
                        Text(
                            text = stringResource(R.string.pronunciation_history_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(uiState.history, key = { it.id }) { result ->
                        HistoryRow(result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhraseCard(
    phrase: String,
    onListen: () -> Unit,
    onNextPhrase: () -> Unit,
) {
    CoachCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.pronunciation_instruction),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = phrase,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                OutlinedButton(onClick = onListen) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeUp,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.pronunciation_listen),
                        modifier = Modifier.padding(start = Spacing.xxs),
                    )
                }
                TextButton(onClick = onNextPhrase) {
                    Text(stringResource(R.string.pronunciation_next_phrase))
                }
            }
        }
    }
}

@Composable
private fun MicSection(
    isListening: Boolean,
    isEvaluating: Boolean,
    voiceLevel: Float,
    onTap: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MicButton(
            state = when {
                isEvaluating -> MicState.Processing
                isListening -> MicState.Listening
                else -> MicState.Idle
            },
            level = voiceLevel,
            onClick = onTap,
        )
        Text(
            text = stringResource(
                when {
                    isEvaluating -> R.string.pronunciation_hint_evaluating
                    isListening -> R.string.pronunciation_hint_listening
                    else -> R.string.pronunciation_hint_idle
                },
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun ResultCard(result: PronunciationResult) {
    val scoreColor = if (result.score >= GOOD_SCORE_THRESHOLD) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.error
    }
    CoachCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = stringResource(R.string.pronunciation_score, result.score),
                style = MaterialTheme.typography.displaySmall,
                color = scoreColor,
            )
            Text(
                text = stringResource(R.string.pronunciation_heard, result.recognizedText),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = result.feedback ?: stringResource(
                    if (result.score >= GOOD_SCORE_THRESHOLD) {
                        R.string.pronunciation_great
                    } else {
                        R.string.pronunciation_keep_trying
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HistoryRow(result: PronunciationResult) {
    CoachCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = result.word,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.pronunciation_score, result.score),
                style = MaterialTheme.typography.titleMedium,
                color = if (result.score >= GOOD_SCORE_THRESHOLD) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}
