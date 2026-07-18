package com.aienglishcoach.core.domain.usecase.vocabulary

import com.aienglishcoach.core.domain.model.ReviewGrade
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.model.VocabularyStatus
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import com.aienglishcoach.core.domain.revision.Sm2Scheduler
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * Applies a flashcard review: reschedules the item with SM-2 and promotes its
 * status (NEW -> LEARNING -> MASTERED) based on successful repetitions.
 */
class ReviewVocabularyItemUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val sm2Scheduler: Sm2Scheduler,
) {

    suspend operator fun invoke(item: VocabularyItem, grade: ReviewGrade) {
        val newSrs = sm2Scheduler.next(item.srs, grade, Clock.System.now())
        val newStatus = when {
            newSrs.repetitionCount >= MASTERED_REPETITIONS -> VocabularyStatus.MASTERED
            newSrs.repetitionCount >= 1 -> VocabularyStatus.LEARNING
            else -> VocabularyStatus.NEW
        }
        vocabularyRepository.update(item.copy(srs = newSrs, status = newStatus))
    }

    companion object {
        const val MASTERED_REPETITIONS = 5
    }
}
