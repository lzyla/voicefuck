package com.aienglishcoach.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body for `POST /v1/chat/completions` (OpenAI-compatible). */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val temperature: Double? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("response_format")
    val responseFormat: ResponseFormatDto? = null,
)

@Serializable
data class ChatMessageDto(
    /** "system", "user" or "assistant". */
    val role: String,
    val content: String,
) {
    companion object {
        const val ROLE_SYSTEM = "system"
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
    }
}

/** `{"type": "json_object"}` enforces a JSON reply for structured output. */
@Serializable
data class ResponseFormatDto(val type: String) {
    companion object {
        val JSON = ResponseFormatDto(type = "json_object")
    }
}

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val choices: List<ChatChoiceDto> = emptyList(),
    val usage: UsageDto? = null,
) {
    /** Content of the first choice or null when the reply is unusable. */
    fun firstContentOrNull(): String? =
        choices.firstOrNull()?.message?.content?.takeIf { it.isNotBlank() }
}

@Serializable
data class ChatChoiceDto(
    val index: Int = 0,
    val message: ChatMessageDto,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
data class UsageDto(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
)
