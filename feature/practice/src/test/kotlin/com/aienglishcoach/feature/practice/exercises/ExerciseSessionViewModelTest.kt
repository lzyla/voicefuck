package com.aienglishcoach.feature.practice.exercises

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseType
import com.aienglishcoach.core.domain.model.SrsState
import com.aienglishcoach.core.domain.usecase.exercise.ExerciseOutcome
import com.aienglishcoach.core.domain.usecase.exercise.GenerateExercisesUseCase
import com.aienglishcoach.core.domain.usecase.exercise.GetDueExercisesUseCase
import com.aienglishcoach.core.domain.usecase.exercise.SubmitExerciseAnswerUseCase
import com.aienglishcoach.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseSessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDueExercises: GetDueExercisesUseCase = mockk()
    private val generateExercises: GenerateExercisesUseCase = mockk()
    private val submitExerciseAnswer: SubmitExerciseAnswerUseCase = mockk()

    private fun createViewModel() = ExerciseSessionViewModel(
        getDueExercises = getDueExercises,
        generateExercises = generateExercises,
        submitExerciseAnswer = submitExerciseAnswer,
    )

    @Test
    fun `session loads due exercises without generating`() = runTest {
        val due = listOf(exercise(id = 1), exercise(id = 2))
        coEvery { getDueExercises(any()) } returns due

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(due, state.exercises)
        assertEquals(due.first(), state.currentExercise)
        coVerify(exactly = 0) { generateExercises(any()) }
    }

    @Test
    fun `correct answer reports outcome and advancing moves to next exercise`() = runTest {
        val first = exercise(id = 1, correctAnswer = "went")
        val second = exercise(id = 2)
        coEvery { getDueExercises(any()) } returns listOf(first, second)
        coEvery { submitExerciseAnswer(first, "went") } returns
            ExerciseOutcome(isCorrect = true, explanation = "Past simple of go.")

        val viewModel = createViewModel()
        viewModel.onAnswerChanged("went")
        viewModel.submitAnswer()

        with(viewModel.uiState.value) {
            assertTrue(outcome?.isCorrect == true)
            assertEquals(1, correctCount)
            assertEquals(0, currentIndex)
        }
        coVerify(exactly = 1) { submitExerciseAnswer(first, "went") }

        viewModel.nextExercise()

        with(viewModel.uiState.value) {
            assertEquals(1, currentIndex)
            assertEquals(second, currentExercise)
            assertNull(outcome)
            assertEquals("", answer)
            assertFalse(isFinished)
        }
    }

    @Test
    fun `advancing past the last exercise finishes the session`() = runTest {
        val only = exercise(id = 1, correctAnswer = "went")
        coEvery { getDueExercises(any()) } returns listOf(only)
        coEvery { submitExerciseAnswer(only, "went") } returns
            ExerciseOutcome(isCorrect = true, explanation = "OK")

        val viewModel = createViewModel()
        viewModel.onAnswerChanged("went")
        viewModel.submitAnswer()
        viewModel.nextExercise()

        assertTrue(viewModel.uiState.value.isFinished)
        assertEquals(1, viewModel.uiState.value.correctCount)
    }

    @Test
    fun `empty due list triggers generation and reloads`() = runTest {
        val generated = listOf(exercise(id = 10))
        coEvery { getDueExercises(any()) } returnsMany listOf(emptyList(), generated)
        coEvery { generateExercises(any()) } returns AppResult.success(generated)

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        coVerify(exactly = 1) { generateExercises(any()) }
        coVerify(exactly = 2) { getDueExercises(any()) }
        assertEquals(generated, state.exercises)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `empty due list and empty generation ends in empty state`() = runTest {
        coEvery { getDueExercises(any()) } returns emptyList()
        coEvery { generateExercises(any()) } returns AppResult.success(emptyList())

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    private fun exercise(
        id: Long,
        correctAnswer: String = "answer",
    ) = Exercise(
        id = id,
        type = ExerciseType.FILL_GAP,
        question = "Yesterday I ___ to school.",
        correctAnswer = correctAnswer,
        explanation = "Past simple.",
        srs = SrsState(dueAt = NOW),
        createdAt = NOW,
    )

    private companion object {
        val NOW: Instant = Instant.parse("2026-01-01T00:00:00Z")
    }
}
