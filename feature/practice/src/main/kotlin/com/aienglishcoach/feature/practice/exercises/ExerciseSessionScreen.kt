package com.aienglishcoach.feature.practice.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.CoachPrimaryButton
import com.aienglishcoach.core.designsystem.component.CoachSecondaryButton
import com.aienglishcoach.core.designsystem.component.EmptyState
import com.aienglishcoach.core.designsystem.component.ErrorBanner
import com.aienglishcoach.core.designsystem.component.LoadingIndicator
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseType
import com.aienglishcoach.core.domain.usecase.exercise.ExerciseOutcome
import com.aienglishcoach.feature.practice.R
import com.aienglishcoach.feature.practice.errorMessage

/**
 * One-at-a-time exercise revision session with per-answer feedback and an
 * end-of-session score summary.
 */
@Composable
fun ExerciseSessionScreen(
    onBack: () -> Unit,
    viewModel: ExerciseSessionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exercises_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.exercises_back),
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
                .verticalScroll(rememberScrollState())
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
                uiState.isFinished -> SessionSummary(
                    correctCount = uiState.correctCount,
                    totalCount = uiState.exercises.size,
                    onFinish = onBack,
                )
                uiState.isEmpty -> EmptyState(
                    icon = Icons.Rounded.Quiz,
                    title = stringResource(R.string.exercises_empty_title),
                    description = stringResource(R.string.exercises_empty_description),
                )
                else -> uiState.currentExercise?.let { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        index = uiState.currentIndex,
                        total = uiState.exercises.size,
                        answer = uiState.answer,
                        outcome = uiState.outcome,
                        isSubmitting = uiState.isSubmitting,
                        onAnswerChanged = viewModel::onAnswerChanged,
                        onSubmit = viewModel::submitAnswer,
                        onSelectOption = viewModel::selectOption,
                        onNext = viewModel::nextExercise,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    index: Int,
    total: Int,
    answer: String,
    outcome: ExerciseOutcome?,
    isSubmitting: Boolean,
    onAnswerChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSelectOption: (String) -> Unit,
    onNext: () -> Unit,
) {
    Text(
        text = stringResource(R.string.exercises_progress, index + 1, total),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    CoachCard {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
            Text(
                text = exercise.question,
                style = MaterialTheme.typography.titleMedium,
            )
            if (exercise.type == ExerciseType.SPEAKING) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = stringResource(R.string.exercises_speaking_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    when (exercise.type) {
        ExerciseType.MULTIPLE_CHOICE -> exercise.options.forEach { option ->
            CoachSecondaryButton(
                text = option,
                onClick = { onSelectOption(option) },
                enabled = outcome == null && !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        // SPEAKING reuses the text input in the MVP; a voice check via
        // SpeechToTextService will replace it in a later iteration.
        ExerciseType.FILL_GAP,
        ExerciseType.TRANSLATION,
        ExerciseType.SPEAKING,
        -> {
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChanged,
                label = { Text(stringResource(R.string.exercises_answer_label)) },
                placeholder = { Text(stringResource(R.string.exercises_answer_placeholder)) },
                enabled = outcome == null && !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
            if (outcome == null) {
                CoachPrimaryButton(
                    text = stringResource(R.string.exercises_submit),
                    onClick = onSubmit,
                    enabled = answer.isNotBlank() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (outcome != null) {
        OutcomeCard(
            outcome = outcome,
            correctAnswer = exercise.correctAnswer,
            onNext = onNext,
        )
    }
}

@Composable
private fun OutcomeCard(
    outcome: ExerciseOutcome,
    correctAnswer: String,
    onNext: () -> Unit,
) {
    CoachCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = stringResource(
                    if (outcome.isCorrect) {
                        R.string.exercises_correct
                    } else {
                        R.string.exercises_incorrect
                    },
                ),
                style = MaterialTheme.typography.titleMedium,
                color = if (outcome.isCorrect) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            if (!outcome.isCorrect) {
                Text(
                    text = stringResource(R.string.exercises_correct_answer, correctAnswer),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = outcome.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CoachPrimaryButton(
                text = stringResource(R.string.exercises_next),
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SessionSummary(
    correctCount: Int,
    totalCount: Int,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = stringResource(R.string.exercises_summary_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.exercises_summary_score, correctCount, totalCount),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        CoachPrimaryButton(
            text = stringResource(R.string.exercises_finish),
            onClick = onFinish,
        )
    }
}
