package com.aienglishcoach.core.domain.model

import kotlinx.datetime.Instant

/**
 * A single long-term memory item the AI tutor keeps about the learner.
 * Memories are extracted after each conversation and injected into future
 * system prompts via embedding-based retrieval (RAG).
 */
data class AiMemory(
    val id: Long = 0,
    val kind: MemoryKind,
    /** Natural-language memory, e.g. "Works as a nurse, night shifts". */
    val content: String,
    /** Embedding vector for similarity search; null until computed. */
    val embedding: FloatArray? = null,
    /** 1–5; higher importance memories win ties during retrieval. */
    val importance: Int = 3,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AiMemory) return false
        return id == other.id &&
            kind == other.kind &&
            content == other.content &&
            importance == other.importance &&
            createdAt == other.createdAt &&
            updatedAt == other.updatedAt &&
            (embedding contentEquals other.embedding)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + kind.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + importance
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        return result
    }
}

enum class MemoryKind { FACT, PREFERENCE, WEAKNESS, GOAL, PROGRESS }
