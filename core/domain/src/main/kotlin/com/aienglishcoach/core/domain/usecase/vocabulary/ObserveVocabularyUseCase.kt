package com.aienglishcoach.core.domain.usecase.vocabulary

import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the full vocabulary list, newest first. */
class ObserveVocabularyUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
) {
    operator fun invoke(): Flow<List<VocabularyItem>> = vocabularyRepository.observeAll()
}
