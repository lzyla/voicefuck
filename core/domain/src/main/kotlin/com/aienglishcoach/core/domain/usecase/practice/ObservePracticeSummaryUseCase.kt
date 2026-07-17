package com.aienglishcoach.core.domain.usecase.practice

import com.aienglishcoach.core.domain.repository.ExerciseRepository
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import javax.inject.Inject

/** Streams due counts for the practice hub badges. */
class ObservePracticeSummaryUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val vocabularyRepository: VocabularyRepository,
) {

    operator fun invoke(): Flow<PracticeSummary> {
        val now = Clock.System.now()
        return combine(
            exerciseRepository.observeDueCount(now),
            vocabularyRepository.observeDueCount(now),
        ) { dueExercises, dueVocabulary ->
            PracticeSummary(dueExercises = dueExercises, dueVocabulary = dueVocabulary)
        }
    }
}

data class PracticeSummary(
    val dueExercises: Int = 0,
    val dueVocabulary: Int = 0,
)
