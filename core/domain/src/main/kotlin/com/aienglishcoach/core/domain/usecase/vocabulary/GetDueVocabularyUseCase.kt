package com.aienglishcoach.core.domain.usecase.vocabulary

import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

/** Returns vocabulary items due for flashcard review, capped per session. */
class GetDueVocabularyUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
) {

    suspend operator fun invoke(limit: Int = SESSION_SIZE): List<VocabularyItem> =
        vocabularyRepository.getDueItems(now = Clock.System.now(), limit = limit)

    companion object {
        const val SESSION_SIZE = 20
    }
}
