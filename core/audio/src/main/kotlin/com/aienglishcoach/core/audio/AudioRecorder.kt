package com.aienglishcoach.core.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.aienglishcoach.core.domain.service.AudioRecorderService
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [AudioRecorderService] backed by [MediaRecorder], writing AAC/M4A files
 * into the app-private `files/audio` directory (no storage permission needed).
 */
@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
) : AudioRecorderService {

    private var recorder: MediaRecorder? = null
    private var startedAtMillis: Long = 0

    override fun start(): String {
        check(recorder == null) { "Recording already in progress" }

        val directory = File(context.filesDir, AUDIO_DIRECTORY).apply { mkdirs() }
        val outputFile = File(directory, "note_${System.currentTimeMillis()}.m4a")

        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(BIT_RATE)
            setAudioSamplingRate(SAMPLE_RATE)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }

        recorder = mediaRecorder
        startedAtMillis = System.currentTimeMillis()
        return outputFile.absolutePath
    }

    override fun stop(): Int {
        val active = recorder ?: return 0
        recorder = null
        val durationSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1000).toInt()
        runCatching {
            active.stop()
        }.onFailure { Timber.w(it, "MediaRecorder.stop failed (recording too short?)") }
        active.release()
        return durationSeconds
    }

    override fun isRecording(): Boolean = recorder != null

    companion object {
        const val AUDIO_DIRECTORY = "audio"
        private const val BIT_RATE = 128_000
        private const val SAMPLE_RATE = 44_100
    }
}
