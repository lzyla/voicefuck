package com.aienglishcoach.core.domain.usecase.exercise

import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.repository.ExerciseRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

/** Returns exercises due for revision right now, capped for one session. */
class GetDueExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) {

    suspend operator fun invoke(limit: Int = SESSION_SIZE): List<Exercise> =
        exerciseRepository.getDueExercises(now = Clock.System.now(), limit = limit)

    companion object {
        const val SESSION_SIZE = 10
    }
}
