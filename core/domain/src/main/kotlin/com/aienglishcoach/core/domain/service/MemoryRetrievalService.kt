package com.aienglishcoach.core.domain.service

import com.aienglishcoach.core.domain.model.AiMemory

/**
 * Retrieval side of the long-term memory (RAG): given a query text, returns
 * the most relevant memories using embedding similarity, falling back to
 * recency/importance when embeddings are unavailable (offline).
 */
interface MemoryRetrievalService {

    suspend fun retrieveRelevant(query: String, topK: Int = DEFAULT_TOP_K): List<AiMemory>

    /** Computes and stores embeddings for memories that do not have one. */
    suspend fun backfillEmbeddings()

    companion object {
        const val DEFAULT_TOP_K = 8
    }
}
