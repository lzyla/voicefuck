package com.aienglishcoach.feature.practice.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Style
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.CoachPrimaryButton
import com.aienglishcoach.core.designsystem.component.CoachSecondaryButton
import com.aienglishcoach.core.designsystem.component.EmptyState
import com.aienglishcoach.core.designsystem.component.ErrorBanner
import com.aienglishcoach.core.designsystem.component.LoadingIndicator
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.ReviewGrade
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.feature.practice.R
import com.aienglishcoach.feature.practice.errorMessage

/**
 * Flashcard review session: word (+example) on the front, tap to flip to the
 * translation, then grade the recall with one of four SM-2 buttons.
 */
@Composable
fun VocabularyReviewScreen(
    onBack: () -> Unit,
    viewModel: VocabularyReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.review_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            uiState.error?.let { error ->
                ErrorBanner(
                    message = errorMessage(error),
                    actionLabel = stringResource(R.string.practice_error_dismiss),
                    onAction = viewModel::dismissError,
                )
            }

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.isFinished -> ReviewSummary(
                    reviewedCount = uiState.reviewedCount,
                    onFinish = onBack,
                )
                uiState.isEmpty -> EmptyState(
                    icon = Icons.Rounded.Style,
                    title = stringResource(R.string.review_empty_title),
                    description = stringResource(R.string.review_empty_description),
                )
                else -> uiState.currentItem?.let { item ->
                    Flashcard(
                        item = item,
                        index = uiState.currentIndex,
                        total = uiState.items.size,
                        isRevealed = uiState.isRevealed,
                        isGrading = uiState.isGrading,
                        onFlip = viewModel::revealCard,
                        onGrade = viewModel::grade,
                    )
                }
            }
        }
    }
}

@Composable
private fun Flashcard(
    item: VocabularyItem,
    index: Int,
    total: Int,
    isRevealed: Boolean,
    isGrading: Boolean,
    onFlip: () -> Unit,
    onGrade: (ReviewGrade) -> Unit,
) {
    Text(
        text = stringResource(R.string.review_progress, index + 1, total),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    CoachCard(onClick = if (isRevealed) null else onFlip) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = item.word,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            item.example?.let { example ->
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = example,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (isRevealed) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = item.translation,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            } else {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = stringResource(R.string.review_tap_to_flip),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (isRevealed) {
        GradeButtons(isGrading = isGrading, onGrade = onGrade)
    }
}

@Composable
private fun GradeButtons(
    isGrading: Boolean,
    onGrade: (ReviewGrade) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoachSecondaryButton(
            text = stringResource(R.string.review_grade_again),
            onClick = { onGrade(ReviewGrade.AGAIN) },
            enabled = !isGrading,
            modifier = Modifier.weight(1f),
        )
        CoachSecondaryButton(
            text = stringResource(R.string.review_grade_hard),
            onClick = { onGrade(ReviewGrade.HARD) },
            enabled = !isGrading,
            modifier = Modifier.weight(1f),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoachSecondaryButton(
            text = stringResource(R.string.review_grade_good),
            onClick = { onGrade(ReviewGrade.GOOD) },
            enabled = !isGrading,
            modifier = Modifier.weight(1f),
        )
        CoachSecondaryButton(
            text = stringResource(R.string.review_grade_easy),
            onClick = { onGrade(ReviewGrade.EASY) },
            enabled = !isGrading,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReviewSummary(
    reviewedCount: Int,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = stringResource(R.string.review_summary_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.review_summary_count, reviewedCount),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        CoachPrimaryButton(
            text = stringResource(R.string.review_finish),
            onClick = onFinish,
        )
    }
}
