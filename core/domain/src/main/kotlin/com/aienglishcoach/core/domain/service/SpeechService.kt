package com.aienglishcoach.core.domain.service

import com.aienglishcoach.core.common.result.AppError
import kotlinx.coroutines.flow.Flow

/**
 * Port to on-device speech recognition. Implemented in `:core:audio` with
 * Android's `SpeechRecognizer`; kept abstract so the domain and ViewModels
 * stay testable and a cloud STT engine can be swapped in later.
 */
interface SpeechToTextService {

    /** True when a recognition service is available on this device. */
    fun isAvailable(): Boolean

    /**
     * Starts a single recognition session for English speech and emits
     * [SpeechEvent]s until a final result or an error is delivered.
     */
    fun listen(): Flow<SpeechEvent>

    /** Cancels an ongoing session, if any. */
    fun stop()
}

sealed interface SpeechEvent {
    /** The recognizer is ready and the user may start speaking. */
    data object ReadyForSpeech : SpeechEvent

    /** Live partial hypothesis while the user is speaking. */
    data class Partial(val text: String) : SpeechEvent

    /** Microphone input level 0..1 for waveform animation. */
    data class RmsChanged(val level: Float) : SpeechEvent

    /** Final transcription with recognizer confidence 0..1 (or null). */
    data class Result(val text: String, val confidence: Float?) : SpeechEvent

    data class Error(val error: AppError) : SpeechEvent
}

/**
 * Port to text-to-speech. Implemented in `:core:audio` with Android's
 * `TextToSpeech` engine.
 */
interface TextToSpeechService {

    /** Speaks [text] aloud; completes when playback finishes or fails. */
    suspend fun speak(text: String): Boolean

    /** Immediately stops any ongoing speech. */
    fun stop()

    fun isSpeaking(): Boolean

    /** Applies voice (US/UK) and speech rate from user preferences. */
    fun configure(voice: com.aienglishcoach.core.domain.model.TtsVoice, speechRate: Float)

    fun shutdown()
}

/**
 * Port to raw audio recording for voice notes.
 */
interface AudioRecorderService {

    /** Starts recording into a new file and returns its absolute path. */
    fun start(): String

    /** Stops recording and returns the duration in seconds. */
    fun stop(): Int

    fun isRecording(): Boolean
}
