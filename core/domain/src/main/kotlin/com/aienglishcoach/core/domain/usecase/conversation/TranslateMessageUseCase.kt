package com.aienglishcoach.core.domain.usecase.conversation

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.common.result.map
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import javax.inject.Inject

/**
 * On-demand Polish translation of a tutor message (long-press gesture).
 * The translation is cached on the message row so it is fetched only once.
 */
class TranslateMessageUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val aiTutorService: AiTutorService,
) {

    suspend operator fun invoke(messageId: Long, text: String): AppResult<String> =
        aiTutorService.translateToPolish(text).map { translation ->
            conversationRepository.updateMessageTranslation(messageId, translation)
            translation
        }
}
