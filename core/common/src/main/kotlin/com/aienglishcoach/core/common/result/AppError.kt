package com.aienglishcoach.core.common.result

/**
 * Domain-level failure taxonomy for the whole application.
 *
 * Every layer maps its low-level exceptions (IO, HTTP, speech, storage) into
 * one of these values so that ViewModels can render a meaningful, localized
 * message without knowing anything about the underlying transport.
 */
sealed interface AppError {

    /** No network connection or the request could not reach the server. */
    data object Network : AppError

    /** The user has not configured an AI API key yet. */
    data object MissingApiKey : AppError

    /** The configured API key was rejected (HTTP 401/403). */
    data object InvalidApiKey : AppError

    /** Rate limit or quota exhausted (HTTP 429). */
    data object RateLimited : AppError

    /** The AI provider failed (HTTP 5xx) or returned an unusable payload. */
    data class AiService(val message: String? = null) : AppError

    /** Speech recognition failed (no match, busy recognizer, missing service). */
    data class SpeechRecognition(val reason: SpeechErrorReason) : AppError

    /** Text-to-speech engine failed to initialize or speak. */
    data object TextToSpeech : AppError

    /** Microphone permission is not granted. */
    data object MicrophonePermissionDenied : AppError

    /** Local persistence failure (Room/DataStore/file system). */
    data class Storage(val message: String? = null) : AppError

    /** Anything that does not fit the categories above. */
    data class Unknown(val message: String? = null) : AppError
}

/** Fine-grained reasons for speech recognition failures. */
enum class SpeechErrorReason {
    NO_MATCH,
    TIMEOUT,
    AUDIO,
    RECOGNIZER_BUSY,
    SERVICE_UNAVAILABLE,
    OTHER,
}
