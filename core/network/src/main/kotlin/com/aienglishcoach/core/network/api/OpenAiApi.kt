package com.aienglishcoach.core.network.api

import com.aienglishcoach.core.network.dto.ChatCompletionRequest
import com.aienglishcoach.core.network.dto.ChatCompletionResponse
import com.aienglishcoach.core.network.dto.EmbeddingsRequest
import com.aienglishcoach.core.network.dto.EmbeddingsResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * OpenAI-compatible REST API. The Authorization header is attached by
 * [com.aienglishcoach.core.network.interceptor.AuthInterceptor].
 */
interface OpenAiApi {

    @POST("v1/chat/completions")
    suspend fun chatCompletions(@Body request: ChatCompletionRequest): ChatCompletionResponse

    @POST("v1/embeddings")
    suspend fun embeddings(@Body request: EmbeddingsRequest): EmbeddingsResponse
}
