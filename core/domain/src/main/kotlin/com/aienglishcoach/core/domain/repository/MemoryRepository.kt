package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.AiMemory
import com.aienglishcoach.core.domain.model.MemoryKind

/** Storage for the AI tutor's long-term memory about the learner. */
interface MemoryRepository {

    suspend fun getAll(): List<AiMemory>

    suspend fun getByKind(kind: MemoryKind): List<AiMemory>

    suspend fun upsert(memory: AiMemory): Long

    suspend fun updateEmbedding(id: Long, embedding: FloatArray)

    suspend fun delete(id: Long)

    /** Removes the least important, oldest memories above [maxCount]. */
    suspend fun prune(maxCount: Int)
}
