package com.aienglishcoach.feature.practice.notes

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.EmptyState
import com.aienglishcoach.core.designsystem.component.ErrorBanner
import com.aienglishcoach.core.designsystem.component.LoadingIndicator
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.VoiceNote
import com.aienglishcoach.feature.practice.R
import com.aienglishcoach.feature.practice.errorMessage

/**
 * Voice memos: record spoken practice with a live timer, save it under a
 * title, then play back or delete recordings from the list.
 */
@Composable
fun VoiceNotesScreen(
    viewModel: VoiceNotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.startRecording()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.notes_title)) })
        },
        floatingActionButton = {
            RecordFab(
                isRecording = uiState.isRecording,
                recordingSeconds = uiState.recordingSeconds,
                onStop = viewModel::stopRecording,
                onRecord = {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        viewModel.startRecording()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            )
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

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.notes.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.KeyboardVoice,
                    title = stringResource(R.string.notes_empty_title),
                    description = stringResource(R.string.notes_empty_description),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        VoiceNoteRow(
                            note = note,
                            isPlaying = uiState.playingNoteId == note.id,
                            onTogglePlayback = { viewModel.togglePlayback(note) },
                            onDelete = { viewModel.requestDelete(note) },
                        )
                    }
                }
            }
        }
    }

    uiState.pendingRecording?.let {
        SaveNoteDialog(
            onSave = viewModel::savePendingRecording,
            onDiscard = viewModel::discardPendingRecording,
        )
    }

    uiState.noteToDelete?.let { note ->
        DeleteNoteDialog(
            note = note,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete,
        )
    }
}

@Composable
private fun RecordFab(
    isRecording: Boolean,
    recordingSeconds: Int,
    onRecord: () -> Unit,
    onStop: () -> Unit,
) {
    if (isRecording) {
        ExtendedFloatingActionButton(
            onClick = onStop,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Stop,
                    contentDescription = stringResource(R.string.notes_stop),
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.notes_recording_time,
                        formatDuration(recordingSeconds),
                    ),
                )
            },
        )
    } else {
        ExtendedFloatingActionButton(
            onClick = onRecord,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(R.string.notes_record)) },
        )
    }
}

@Composable
private fun VoiceNoteRow(
    note: VoiceNote,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    onDelete: () -> Unit,
) {
    CoachCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onTogglePlayback) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.notes_stop_playback else R.string.notes_play,
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = Spacing.xs)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = formatDuration(note.durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.notes_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SaveNoteDialog(
    onSave: (String) -> Unit,
    onDiscard: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDiscard,
        title = { Text(stringResource(R.string.notes_save_title)) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.notes_save_label)) },
                placeholder = { Text(stringResource(R.string.notes_save_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(title) }) {
                Text(stringResource(R.string.notes_save_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text(stringResource(R.string.notes_save_discard))
            }
        },
    )
}

@Composable
private fun DeleteNoteDialog(
    note: VoiceNote,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notes_delete_title)) },
        text = { Text(stringResource(R.string.notes_delete_message, note.title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.notes_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.notes_delete_cancel))
            }
        },
    )
}

@Composable
private fun formatDuration(totalSeconds: Int): String =
    stringResource(R.string.notes_duration, totalSeconds / 60, totalSeconds % 60)
