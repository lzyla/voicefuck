package com.aienglishcoach.core.domain.usecase.exercise

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.common.result.map
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.repository.ExerciseRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.repository.UserErrorRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Turns the learner's unresolved errors into personalized exercises.
 * Called after conversation analysis and from the practice hub on demand.
 */
class GenerateExercisesUseCase @Inject constructor(
    private val userErrorRepository: UserErrorRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    private val aiTutorService: AiTutorService,
) {

    suspend operator fun invoke(maxErrors: Int = DEFAULT_MAX_ERRORS): AppResult<List<Exercise>> {
        val errors = userErrorRepository.getUnresolved(limit = maxErrors)
        if (errors.isEmpty()) return AppResult.success(emptyList())

        val preferences = settingsRepository.preferences.first()
        return aiTutorService.generateExercises(errors, preferences).map { exercises ->
            val ids = exerciseRepository.insertAll(exercises)
            exercises.zip(ids) { exercise, id -> exercise.copy(id = id) }
        }
    }

    companion object {
        const val DEFAULT_MAX_ERRORS = 10
    }
}
