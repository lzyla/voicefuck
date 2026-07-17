package com.aienglishcoach.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.aienglishcoach.core.domain.model.TtsVoice
import com.aienglishcoach.core.domain.service.TextToSpeechService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * [TextToSpeechService] backed by the platform [TextToSpeech] engine.
 * Initialization is lazy and awaited by the first [speak] call.
 */
@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : TextToSpeechService {

    private var tts: TextToSpeech? = null
    private val initialized = CompletableDeferred<Boolean>()
    private val initStarted = AtomicBoolean(false)

    private fun ensureEngine(): TextToSpeech? {
        if (initStarted.compareAndSet(false, true)) {
            tts = TextToSpeech(context) { status ->
                val ok = status == TextToSpeech.SUCCESS
                if (ok) tts?.language = Locale.US
                if (!ok) Timber.w("TextToSpeech init failed with status=$status")
                initialized.complete(ok)
            }
        }
        return tts
    }

    override suspend fun speak(text: String): Boolean {
        ensureEngine()
        if (!initialized.await()) return false
        val engine = tts ?: return false

        return suspendCancellableCoroutine { continuation ->
            val utteranceId = UUID.randomUUID().toString()

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) = Unit

                override fun onDone(id: String?) {
                    if (id == utteranceId && continuation.isActive) continuation.resume(true)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) {
                    if (id == utteranceId && continuation.isActive) continuation.resume(false)
                }

                override fun onError(id: String?, errorCode: Int) {
                    if (id == utteranceId && continuation.isActive) continuation.resume(false)
                }
            })

            val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            if (result != TextToSpeech.SUCCESS && continuation.isActive) {
                continuation.resume(false)
            }

            continuation.invokeOnCancellation { engine.stop() }
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun isSpeaking(): Boolean = tts?.isSpeaking == true

    override fun configure(voice: TtsVoice, speechRate: Float) {
        ensureEngine()
        tts?.apply {
            language = when (voice) {
                TtsVoice.US -> Locale.US
                TtsVoice.UK -> Locale.UK
            }
            setSpeechRate(speechRate.coerceIn(MIN_RATE, MAX_RATE))
        }
    }

    override fun shutdown() {
        tts?.shutdown()
        tts = null
    }

    private companion object {
        const val MIN_RATE = 0.5f
        const val MAX_RATE = 1.5f
    }
}
