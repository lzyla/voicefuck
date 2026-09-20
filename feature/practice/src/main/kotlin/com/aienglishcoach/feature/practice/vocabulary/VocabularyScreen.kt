package com.aienglishcoach.feature.practice.vocabulary

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.CoachPrimaryButton
import com.aienglishcoach.core.designsystem.component.EmptyState
import com.aienglishcoach.core.designsystem.component.ErrorBanner
import com.aienglishcoach.core.designsystem.component.LoadingIndicator
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.model.VocabularyStatus
import com.aienglishcoach.feature.practice.R
import com.aienglishcoach.feature.practice.errorMessage

/**
 * Vocabulary list: due-count header with a review call-to-action, all saved
 * words with SRS status chips and a FAB to add a word manually.
 */
@Composable
fun VocabularyScreen(
    onStartReview: () -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.vocabulary_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddDialog) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.vocabulary_add),
                )
            }
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
                uiState.items.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.Style,
                    title = stringResource(R.string.vocabulary_empty_title),
                    description = stringResource(R.string.vocabulary_empty_description),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    if (uiState.dueCount > 0) {
                        item(key = "due-header") {
                            DueHeader(dueCount = uiState.dueCount, onStartReview = onStartReview)
                        }
                    }
                    items(uiState.items, key = { it.id }) { item ->
                        VocabularyRow(item = item)
                    }
                }
            }
        }
    }

    if (uiState.isAddDialogVisible) {
        AddWordDialog(
            onSave = viewModel::addWord,
            onDismiss = viewModel::dismissAddDialog,
        )
    }
}

@Composable
private fun DueHeader(
    dueCount: Int,
    onStartReview: () -> Unit,
) {
    CoachCard(modifier = Modifier.padding(bottom = Spacing.xs)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.vocabulary_due_header, dueCount),
                style = MaterialTheme.typography.titleMedium,
            )
            CoachPrimaryButton(
                text = stringResource(R.string.vocabulary_review_now),
                onClick = onStartReview,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun VocabularyRow(item: VocabularyItem) {
    CoachCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.word,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = item.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(statusLabel(item.status)) },
            )
        }
    }
}

@Composable
private fun statusLabel(status: VocabularyStatus): String = stringResource(
    when (status) {
        VocabularyStatus.NEW -> R.string.vocabulary_status_new
        VocabularyStatus.LEARNING -> R.string.vocabulary_status_learning
        VocabularyStatus.MASTERED -> R.string.vocabulary_status_mastered
    },
)

@Composable
private fun AddWordDialog(
    onSave: (word: String, translation: String, example: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var word by rememberSaveable { mutableStateOf("") }
    var translation by rememberSaveable { mutableStateOf("") }
    var example by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.vocabulary_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text(stringResource(R.string.vocabulary_add_word)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    label = { Text(stringResource(R.string.vocabulary_add_translation)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = example,
                    onValueChange = { example = it },
                    label = { Text(stringResource(R.string.vocabulary_add_example)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(word, translation, example) },
                enabled = word.isNotBlank() && translation.isNotBlank(),
            ) {
                Text(stringResource(R.string.vocabulary_add_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.vocabulary_add_cancel))
            }
        },
    )
}
