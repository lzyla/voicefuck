package com.aienglishcoach.core.network.dto

import kotlinx.serialization.Serializable

/** Request body for `POST /v1/embeddings`. */
@Serializable
data class EmbeddingsRequest(
    val model: String,
    val input: List<String>,
) {
    companion object {
        const val DEFAULT_MODEL = "text-embedding-3-small"
    }
}

@Serializable
data class EmbeddingsResponse(
    val data: List<EmbeddingDto> = emptyList(),
    val usage: UsageDto? = null,
)

@Serializable
data class EmbeddingDto(
    val index: Int = 0,
    val embedding: List<Float> = emptyList(),
)
