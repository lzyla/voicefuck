package com.aienglishcoach.core.domain.usecase.exercise

import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseAttempt
import com.aienglishcoach.core.domain.model.ReviewGrade
import com.aienglishcoach.core.domain.repository.ExerciseRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.repository.UserErrorRepository
import com.aienglishcoach.core.domain.revision.Sm2Scheduler
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Grades the learner's answer, records the attempt, reschedules the exercise
 * with SM-2 and, once an exercise is answered correctly enough times, marks
 * the originating error as resolved.
 */
class SubmitExerciseAnswerUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val userErrorRepository: UserErrorRepository,
    private val statisticsRepository: StatisticsRepository,
    private val sm2Scheduler: Sm2Scheduler,
) {

    suspend operator fun invoke(exercise: Exercise, userAnswer: String): ExerciseOutcome {
        val isCorrect = isAnswerCorrect(exercise, userAnswer)
        val now = Clock.System.now()

        exerciseRepository.recordAttempt(
            ExerciseAttempt(
                exerciseId = exercise.id,
                userAnswer = userAnswer,
                isCorrect = isCorrect,
                attemptedAt = now,
            ),
        )

        val grade = if (isCorrect) ReviewGrade.GOOD else ReviewGrade.AGAIN
        val newSrs = sm2Scheduler.next(exercise.srs, grade, now)
        exerciseRepository.update(exercise.copy(srs = newSrs))

        if (isCorrect &&
            newSrs.repetitionCount >= RESOLVE_AFTER_REPETITIONS &&
            exercise.sourceErrorId != null
        ) {
            userErrorRepository.markResolved(exercise.sourceErrorId)
        }

        statisticsRepository.addToDay(
            date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            exercisesDone = 1,
            exercisesCorrect = if (isCorrect) 1 else 0,
        )

        return ExerciseOutcome(isCorrect = isCorrect, explanation = exercise.explanation)
    }

    private fun isAnswerCorrect(exercise: Exercise, userAnswer: String): Boolean =
        normalize(userAnswer) == normalize(exercise.correctAnswer)

    private fun normalize(text: String): String =
        text.trim().lowercase().replace(Regex("[.!?,;:]+$"), "").replace(Regex("\\s+"), " ")

    companion object {
        /** Correct repetitions needed before the source error counts as fixed. */
        const val RESOLVE_AFTER_REPETITIONS = 2
    }
}

data class ExerciseOutcome(val isCorrect: Boolean, val explanation: String)
