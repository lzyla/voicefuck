package com.aienglishcoach.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches `Authorization: Bearer <key>` to every OpenAI request.
 *
 * The key is fetched lazily through [ApiKeyProvider] so the interceptor never
 * caches credentials, and requests without a configured key are rejected
 * locally with a 401-like error instead of leaking an empty header.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = apiKeyProvider.getBlocking()
            ?: throw MissingApiKeyException()

        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $apiKey")
            .build()
        return chain.proceed(request)
    }
}

/** Thrown before a request is sent when no API key is configured. */
class MissingApiKeyException : java.io.IOException("AI API key is not configured")

/** Bridges the encrypted key storage (`:core:datastore`) into OkHttp. */
fun interface ApiKeyProvider {
    /** Called on an OkHttp worker thread — blocking here is acceptable. */
    fun getBlocking(): String?
}
