package com.aienglishcoach.core.domain.usecase.vocabulary

import com.aienglishcoach.core.domain.model.SrsState
import com.aienglishcoach.core.domain.model.VocabularyItem
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/** Adds a word manually (from the vocabulary screen or a long-pressed message). */
class SaveVocabularyItemUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val statisticsRepository: StatisticsRepository,
) {

    suspend operator fun invoke(
        word: String,
        translation: String,
        example: String? = null,
        sourceConversationId: Long? = null,
    ): Long {
        val now = Clock.System.now()
        val id = vocabularyRepository.upsert(
            VocabularyItem(
                word = word.trim(),
                translation = translation.trim(),
                example = example?.trim()?.takeIf { it.isNotEmpty() },
                sourceConversationId = sourceConversationId,
                srs = SrsState(dueAt = now),
                createdAt = now,
            ),
        )
        statisticsRepository.addToDay(
            date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            wordsLearned = 1,
        )
        return id
    }
}
