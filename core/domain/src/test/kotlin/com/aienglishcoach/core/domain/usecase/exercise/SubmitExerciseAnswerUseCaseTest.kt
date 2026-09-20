package com.aienglishcoach.core.domain.usecase.exercise

import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseType
import com.aienglishcoach.core.domain.model.SrsState
import com.aienglishcoach.core.domain.repository.ExerciseRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.repository.UserErrorRepository
import com.aienglishcoach.core.domain.revision.Sm2Scheduler
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubmitExerciseAnswerUseCaseTest {

    private val exerciseRepository: ExerciseRepository = mockk(relaxed = true)
    private val userErrorRepository: UserErrorRepository = mockk(relaxed = true)
    private val statisticsRepository: StatisticsRepository = mockk(relaxed = true)

    private lateinit var useCase: SubmitExerciseAnswerUseCase

    private val exercise = Exercise(
        id = 7,
        type = ExerciseType.FILL_GAP,
        question = "I ___ to work yesterday.",
        correctAnswer = "went",
        explanation = "Past simple of go.",
        sourceErrorId = 3,
        srs = SrsState(dueAt = Instant.parse("2026-07-17T08:00:00Z")),
        createdAt = Instant.parse("2026-07-16T08:00:00Z"),
    )

    @Before
    fun setUp() {
        useCase = SubmitExerciseAnswerUseCase(
            exerciseRepository = exerciseRepository,
            userErrorRepository = userErrorRepository,
            statisticsRepository = statisticsRepository,
            sm2Scheduler = Sm2Scheduler(),
        )
    }

    @Test
    fun `correct answer is accepted despite case whitespace and punctuation`() = runTest {
        val outcome = useCase(exercise, "  WENT. ")

        assertTrue(outcome.isCorrect)
        coVerify { exerciseRepository.recordAttempt(match { it.isCorrect }) }
    }

    @Test
    fun `wrong answer is rejected and rescheduled without resolving the error`() = runTest {
        val outcome = useCase(exercise, "goed")

        assertFalse(outcome.isCorrect)
        coVerify(exactly = 0) { userErrorRepository.markResolved(any()) }
    }

    @Test
    fun `enough correct repetitions resolve the source error`() = runTest {
        val updated = slot<Exercise>()
        coEvery { exerciseRepository.update(capture(updated)) } just Runs

        // First correct answer: repetitionCount becomes 1 — not resolved yet.
        useCase(exercise, "went")
        coVerify(exactly = 0) { userErrorRepository.markResolved(any()) }

        // Second correct answer on the updated exercise: resolves the error.
        useCase(updated.captured, "went")
        coVerify { userErrorRepository.markResolved(3) }
    }

    @Test
    fun `every attempt books statistics`() = runTest {
        useCase(exercise, "went")

        coVerify {
            statisticsRepository.addToDay(
                date = any(),
                conversationSeconds = 0,
                messagesSent = 0,
                wordsLearned = 0,
                exercisesDone = 1,
                exercisesCorrect = 1,
            )
        }
    }
}
