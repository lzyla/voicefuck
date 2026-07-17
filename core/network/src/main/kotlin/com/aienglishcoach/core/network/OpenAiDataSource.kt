package com.aienglishcoach.core.network

import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.network.api.OpenAiApi
import com.aienglishcoach.core.network.dto.ChatCompletionRequest
import com.aienglishcoach.core.network.dto.ChatMessageDto
import com.aienglishcoach.core.network.dto.EmbeddingsRequest
import com.aienglishcoach.core.network.dto.ResponseFormatDto
import com.aienglishcoach.core.network.interceptor.MissingApiKeyException
import kotlinx.coroutines.delay
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin, error-mapping facade over [OpenAiApi]. All AI callers go through this
 * class so HTTP failures are consistently translated into [AppError] and
 * transient failures (429/5xx/timeouts) are retried with exponential backoff.
 */
@Singleton
class OpenAiDataSource @Inject constructor(
    private val api: OpenAiApi,
) {

    /**
     * Runs a chat completion and returns the first choice's content.
     *
     * @param jsonResponse when true, forces `response_format=json_object`
     *   (used for analysis/generation calls parsed as JSON).
     */
    suspend fun complete(
        model: String,
        messages: List<ChatMessageDto>,
        temperature: Double = DEFAULT_TEMPERATURE,
        maxTokens: Int? = null,
        jsonResponse: Boolean = false,
    ): AppResult<String> = runMapped {
        val response = api.chatCompletions(
            ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = temperature,
                maxTokens = maxTokens,
                responseFormat = if (jsonResponse) ResponseFormatDto.JSON else null,
            ),
        )
        response.firstContentOrNull()
            ?: throw EmptyCompletionException()
    }

    /** Returns one embedding vector per input string, in input order. */
    suspend fun embed(
        texts: List<String>,
        model: String = EmbeddingsRequest.DEFAULT_MODEL,
    ): AppResult<List<FloatArray>> = runMapped {
        val response = api.embeddings(EmbeddingsRequest(model = model, input = texts))
        response.data
            .sortedBy { it.index }
            .map { it.embedding.toFloatArray() }
    }

    private suspend fun <T> runMapped(block: suspend () -> T): AppResult<T> {
        var attempt = 0
        while (true) {
            try {
                return AppResult.success(block())
            } catch (exception: Exception) {
                val error = exception.toAppError()
                val retryable = error is AppError.RateLimited ||
                    (error is AppError.AiService && exception is HttpException)
                if (retryable && attempt < MAX_RETRIES) {
                    attempt++
                    val backoffMillis = INITIAL_BACKOFF_MILLIS shl (attempt - 1)
                    Timber.w("OpenAI call failed (attempt $attempt), retrying in ${backoffMillis}ms")
                    delay(backoffMillis)
                    continue
                }
                Timber.w(exception, "OpenAI call failed")
                return AppResult.failure(error)
            }
        }
    }

    private fun Exception.toAppError(): AppError = when (this) {
        is MissingApiKeyException -> AppError.MissingApiKey
        is HttpException -> when (code()) {
            401, 403 -> AppError.InvalidApiKey
            429 -> AppError.RateLimited
            in 500..599 -> AppError.AiService("HTTP ${code()}")
            else -> AppError.AiService("HTTP ${code()}")
        }
        is EmptyCompletionException -> AppError.AiService("Empty completion")
        is IOException -> AppError.Network
        else -> AppError.Unknown(message)
    }

    private class EmptyCompletionException : RuntimeException("Completion had no content")

    companion object {
        const val DEFAULT_TEMPERATURE = 0.7
        private const val MAX_RETRIES = 2
        private const val INITIAL_BACKOFF_MILLIS = 1000L
    }
}
