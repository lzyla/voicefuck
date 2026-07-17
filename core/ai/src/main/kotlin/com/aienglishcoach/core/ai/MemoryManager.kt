package com.aienglishcoach.core.ai

import com.aienglishcoach.core.common.result.getOrNull
import com.aienglishcoach.core.domain.model.AiMemory
import com.aienglishcoach.core.domain.repository.MemoryRepository
import com.aienglishcoach.core.domain.service.MemoryRetrievalService
import com.aienglishcoach.core.network.OpenAiDataSource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Retrieval-augmented memory: ranks stored memories against a query using
 * cosine similarity over OpenAI embeddings computed locally in Kotlin.
 *
 * Degrades gracefully offline — when the query embedding cannot be fetched,
 * memories are ranked by importance and recency instead, so conversations
 * still get personalized without the network round-trip.
 */
@Singleton
class MemoryManager @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val openAiDataSource: OpenAiDataSource,
) : MemoryRetrievalService {

    override suspend fun retrieveRelevant(query: String, topK: Int): List<AiMemory> {
        val memories = memoryRepository.getAll()
        if (memories.size <= topK) return memories

        val queryEmbedding = openAiDataSource.embed(listOf(query))
            .getOrNull()
            ?.firstOrNull()

        return if (queryEmbedding == null) {
            // Offline / failure fallback: importance + recency ordering
            // (getAll already returns importance DESC, updated DESC).
            memories.take(topK)
        } else {
            memories
                .sortedByDescending { memory ->
                    memory.embedding
                        ?.let { cosineSimilarity(queryEmbedding, it) }
                        ?.plus(memory.importance * IMPORTANCE_BOOST)
                        ?: Double.NEGATIVE_INFINITY
                }
                .take(topK)
        }
    }

    override suspend fun backfillEmbeddings() {
        val missing = memoryRepository.getAll().filter { it.embedding == null }
        if (missing.isEmpty()) return

        val embeddings = openAiDataSource.embed(missing.map { it.content }).getOrNull()
        if (embeddings == null || embeddings.size != missing.size) {
            Timber.w("Embedding backfill skipped (offline or size mismatch)")
            return
        }
        missing.zip(embeddings).forEach { (memory, embedding) ->
            memoryRepository.updateEmbedding(memory.id, embedding)
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        if (a.isEmpty() || a.size != b.size) return 0.0
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        if (normA == 0.0 || normB == 0.0) return 0.0
        return dot / (sqrt(normA) * sqrt(normB))
    }

    private companion object {
        /** Small tie-breaker so importance still matters among similar hits. */
        const val IMPORTANCE_BOOST = 0.01
    }
}
