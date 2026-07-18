package com.aienglishcoach.core.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.SpeechErrorReason
import com.aienglishcoach.core.domain.service.SpeechEvent
import com.aienglishcoach.core.domain.service.SpeechToTextService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [SpeechToTextService] backed by Android's on-device/Google
 * [SpeechRecognizer], configured for US English with partial results.
 *
 * The recognizer must be created and driven on the main thread; the
 * [callbackFlow] below is collected with `flowOn(main)` by callers
 * (ViewModels collect on main by default).
 */
@Singleton
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechToTextService {

    private var activeRecognizer: SpeechRecognizer? = null

    override fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    override fun listen(): Flow<SpeechEvent> = callbackFlow {
        if (!isAvailable()) {
            trySend(
                SpeechEvent.Error(
                    AppError.SpeechRecognition(SpeechErrorReason.SERVICE_UNAVAILABLE),
                ),
            )
            close()
            return@callbackFlow
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        activeRecognizer = recognizer

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechEvent.ReadyForSpeech)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize the typical -2..10 dB range into 0..1.
                val level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                trySend(SpeechEvent.RmsChanged(level))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults.firstResult()?.let { trySend(SpeechEvent.Partial(it)) }
            }

            override fun onResults(results: Bundle?) {
                val text = results.firstResult()
                if (text.isNullOrBlank()) {
                    trySend(
                        SpeechEvent.Error(
                            AppError.SpeechRecognition(SpeechErrorReason.NO_MATCH),
                        ),
                    )
                } else {
                    val confidence = results
                        ?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                        ?.firstOrNull()
                        ?.takeIf { it >= 0f }
                    trySend(SpeechEvent.Result(text, confidence))
                }
                close()
            }

            override fun onError(error: Int) {
                val reason = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> SpeechErrorReason.NO_MATCH
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechErrorReason.TIMEOUT
                    SpeechRecognizer.ERROR_AUDIO -> SpeechErrorReason.AUDIO
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechErrorReason.RECOGNIZER_BUSY
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        trySend(SpeechEvent.Error(AppError.MicrophonePermissionDenied))
                        close()
                        return
                    }
                    else -> SpeechErrorReason.OTHER
                }
                Timber.d("SpeechRecognizer error=$error mapped to $reason")
                trySend(SpeechEvent.Error(AppError.SpeechRecognition(reason)))
                close()
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        recognizer.startListening(buildIntent())

        awaitClose {
            runCatching {
                recognizer.cancel()
                recognizer.destroy()
            }
            if (activeRecognizer === recognizer) activeRecognizer = null
        }
    }

    override fun stop() {
        runCatching { activeRecognizer?.stopListening() }
    }

    private fun buildIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, RECOGNITION_LANGUAGE)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

    private fun Bundle?.firstResult(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private companion object {
        const val RECOGNITION_LANGUAGE = "en-US"
    }
}
