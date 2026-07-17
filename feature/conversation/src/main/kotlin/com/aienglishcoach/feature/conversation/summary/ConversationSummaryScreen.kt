package com.aienglishcoach.feature.conversation.summary

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.CoachPrimaryButton
import com.aienglishcoach.core.designsystem.component.ErrorBanner
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.ErrorCategory
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.feature.conversation.R
import com.aienglishcoach.feature.conversation.errorMessage

/** Analysis results: summary, detected errors with corrections, next steps. */
@Composable
fun ConversationSummaryScreen(
    onBack: () -> Unit,
    onGoToExercises: () -> Unit,
    viewModel: ConversationSummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.summary_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.conversation_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (uiState.isAnalyzing) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.summary_analyzing),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Spacing.md),
                        )
                    }
                }
            }

            uiState.analysisError?.let { error ->
                item {
                    ErrorBanner(
                        message = errorMessage(error),
                        actionLabel = stringResource(R.string.summary_retry),
                        onAction = viewModel::retryAnalysis,
                    )
                }
            }

            uiState.summary?.let { summary ->
                item {
                    CoachCard {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                text = stringResource(R.string.summary_feedback_header),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = Spacing.xs),
                            )
                        }
                    }
                }
            }

            if (uiState.errors.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.summary_errors_header, uiState.errors.size),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(uiState.errors, key = { it.id }) { error ->
                    ErrorCard(error)
                }
                item {
                    CoachPrimaryButton(
                        text = stringResource(R.string.summary_go_practice),
                        onClick = onGoToExercises,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else if (uiState.isAnalyzed) {
                item {
                    Text(
                        text = stringResource(R.string.summary_no_errors),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(error: UserError) {
    CoachCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = categoryLabel(error.category),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "„${error.original}”",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Spacing.xxs),
            )
            Text(
                text = "→ ${error.corrected}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = error.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xxs),
            )
        }
    }
}

@Composable
private fun categoryLabel(category: ErrorCategory): String = stringResource(
    when (category) {
        ErrorCategory.GRAMMAR -> R.string.category_grammar
        ErrorCategory.VOCABULARY -> R.string.category_vocabulary
        ErrorCategory.PRONUNCIATION -> R.string.category_pronunciation
        ErrorCategory.FLUENCY -> R.string.category_fluency
    },
)
