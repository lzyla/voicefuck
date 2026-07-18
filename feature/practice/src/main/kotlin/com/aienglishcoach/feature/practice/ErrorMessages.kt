package com.aienglishcoach.feature.practice

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aienglishcoach.core.common.result.AppError

/**
 * Maps a domain [AppError] to a localized, user-facing Polish message.
 * Local to this feature — feature modules must not depend on each other.
 */
@Composable
internal fun errorMessage(error: AppError): String = stringResource(
    when (error) {
        AppError.Network -> R.string.error_network
        AppError.MissingApiKey -> R.string.error_missing_api_key
        AppError.InvalidApiKey -> R.string.error_invalid_api_key
        AppError.RateLimited -> R.string.error_rate_limited
        is AppError.AiService -> R.string.error_ai_service
        is AppError.SpeechRecognition -> R.string.error_speech
        AppError.TextToSpeech -> R.string.error_tts
        AppError.MicrophonePermissionDenied -> R.string.error_mic_permission
        is AppError.Storage -> R.string.error_storage
        is AppError.Unknown -> R.string.error_unknown
    },
)
