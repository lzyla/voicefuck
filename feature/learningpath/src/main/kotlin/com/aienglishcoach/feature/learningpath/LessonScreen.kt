package com.aienglishcoach.feature.learningpath

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aienglishcoach.core.designsystem.glass.AuroraBackground
import com.aienglishcoach.core.designsystem.glass.GlassPanel
import com.aienglishcoach.core.designsystem.glass.GlassPrimaryButton
import com.aienglishcoach.core.designsystem.glass.GlassProgressBar
import com.aienglishcoach.core.designsystem.glass.GlassSecondaryButton
import com.aienglishcoach.core.designsystem.glass.GlassSelectable
import com.aienglishcoach.core.designsystem.glass.GlassTokens
import com.aienglishcoach.core.designsystem.glass.ScreenHeading

@Composable
fun LessonScreen(
    onBack: () -> Unit,
    onLessonFinished: () -> Unit,
    viewModel: LessonViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lesson = viewModel.lesson

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = GlassTokens.ScreenSidePadding,
                    vertical = GlassTokens.ScreenTopPadding,
                ),
        ) {
            ScreenHeading(title = lesson.title, modifier = Modifier.padding(bottom = 20.dp))

            when (uiState.phase) {
                LessonPhase.FLASHCARDS -> FlashcardsPhase(
                    lesson = lesson,
                    index = uiState.flashcardIndex,
                    onNext = viewModel::nextFlashcard,
                )
                LessonPhase.EXERCISE -> ExercisePhase(
                    lesson = lesson,
                    answers = uiState.exerciseAnswers,
                    onAnswer = viewModel::answerExercise,
                    onFinish = viewModel::finishExercise,
                )
                LessonPhase.COMPLETE -> CompletePhase(
                    xpReward = lesson.xpReward,
                    correctCount = uiState.exerciseCorrectCount,
                    totalQuestions = lesson.exercise.size,
                    onContinue = onLessonFinished,
                )
            }
        }
    }
}

@Composable
private fun FlashcardsPhase(lesson: Lesson, index: Int, onNext: () -> Unit) {
    var revealed by remember(index) { mutableStateOf(false) }
    val card = lesson.flashcards[index]

    Column(modifier = Modifier.fillMaxSize()) {
        GlassProgressBar(progress = (index + 1f) / lesson.flashcards.size)
        Spacer(modifier = Modifier.height(20.dp))
        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(card.front, color = GlassTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                if (revealed) {
                    Text(
                        card.back,
                        color = GlassTokens.TextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (revealed) {
            GlassPrimaryButton(
                text = stringResource(R.string.lesson_flashcard_next),
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            GlassSecondaryButton(
                text = stringResource(R.string.lesson_flashcard_reveal),
                onClick = { revealed = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ExercisePhase(
    lesson: Lesson,
    answers: Map<Int, Int>,
    onAnswer: (Int, Int) -> Unit,
    onFinish: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        lesson.exercise.forEachIndexed { questionIndex, question ->
            GlassPanel(modifier = Modifier.fillMaxWidth().padding(bottom = GlassTokens.CardGap)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        question.prompt,
                        color = GlassTokens.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    question.options.forEachIndexed { optionIndex, option ->
                        GlassSelectable(
                            selected = answers[questionIndex] == optionIndex,
                            onClick = { onAnswer(questionIndex, optionIndex) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        ) {
                            Text(
                                option,
                                color = GlassTokens.TextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            )
                        }
                    }
                }
            }
        }
        GlassPrimaryButton(
            text = stringResource(R.string.lesson_exercise_finish),
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CompletePhase(
    xpReward: Int,
    correctCount: Int,
    totalQuestions: Int,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.EmojiEvents,
            contentDescription = null,
            tint = GlassTokens.Success,
            modifier = Modifier.height(72.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.lesson_complete_title),
            color = GlassTokens.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )
        Text(
            stringResource(R.string.lesson_complete_summary, correctCount, totalQuestions, xpReward),
            color = GlassTokens.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(32.dp))
        GlassPrimaryButton(
            text = stringResource(R.string.lesson_complete_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
