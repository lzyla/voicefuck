package com.aienglishcoach.feature.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.theme.Spacing

/**
 * Practice hub: entry point to exercises, vocabulary, pronunciation and voice
 * notes. Exercise and vocabulary tiles carry due-count badges.
 */
@Composable
fun PracticeHubScreen(
    onOpenExercises: () -> Unit,
    onOpenVocabulary: () -> Unit,
    onOpenPronunciation: () -> Unit,
    onOpenNotes: () -> Unit,
    viewModel: PracticeHubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.practice_hub_title)) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            PracticeTile(
                icon = Icons.Rounded.Quiz,
                title = stringResource(R.string.practice_tile_exercises),
                description = stringResource(R.string.practice_tile_exercises_description),
                dueCount = uiState.dueExercises,
                onClick = onOpenExercises,
            )
            PracticeTile(
                icon = Icons.Rounded.Style,
                title = stringResource(R.string.practice_tile_vocabulary),
                description = stringResource(R.string.practice_tile_vocabulary_description),
                dueCount = uiState.dueVocabulary,
                onClick = onOpenVocabulary,
            )
            PracticeTile(
                icon = Icons.Rounded.RecordVoiceOver,
                title = stringResource(R.string.practice_tile_pronunciation),
                description = stringResource(R.string.practice_tile_pronunciation_description),
                dueCount = 0,
                onClick = onOpenPronunciation,
            )
            PracticeTile(
                icon = Icons.Rounded.KeyboardVoice,
                title = stringResource(R.string.practice_tile_notes),
                description = stringResource(R.string.practice_tile_notes_description),
                dueCount = 0,
                onClick = onOpenNotes,
            )
        }
    }
}

@Composable
private fun PracticeTile(
    icon: ImageVector,
    title: String,
    description: String,
    dueCount: Int,
    onClick: () -> Unit,
) {
    CoachCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Spacing.xl),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (dueCount > 0) {
                Badge {
                    Text(text = stringResource(R.string.practice_due_badge, dueCount))
                }
            }
        }
    }
}
