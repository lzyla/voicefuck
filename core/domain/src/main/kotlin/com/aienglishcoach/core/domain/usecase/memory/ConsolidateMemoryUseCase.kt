package com.aienglishcoach.core.domain.usecase.memory

import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.common.result.map
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.MemoryRepository
import com.aienglishcoach.core.domain.service.AiTutorService
import com.aienglishcoach.core.domain.service.MemoryRetrievalService
import javax.inject.Inject

/**
 * Long-term memory consolidation, run in the background after a conversation:
 * the AI extracts new facts/preferences/weaknesses/goals, they are upserted
 * (the service deduplicates against existing memories), embeddings are
 * backfilled for retrieval and the store is pruned to a sane size.
 */
class ConsolidateMemoryUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    private val aiTutorService: AiTutorService,
    private val memoryRetrievalService: MemoryRetrievalService,
) {

    suspend operator fun invoke(conversationId: Long): AppResult<Int> {
        val history = conversationRepository.getMessages(conversationId)
        if (history.isEmpty()) return AppResult.success(0)

        val existing = memoryRepository.getAll()

        return aiTutorService.extractMemories(history, existing).map { newMemories ->
            newMemories.forEach { memoryRepository.upsert(it) }
            memoryRetrievalService.backfillEmbeddings()
            memoryRepository.prune(maxCount = MAX_MEMORIES)
            newMemories.size
        }
    }

    companion object {
        const val MAX_MEMORIES = 200
    }
}
