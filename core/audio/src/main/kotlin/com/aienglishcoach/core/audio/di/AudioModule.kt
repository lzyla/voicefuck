package com.aienglishcoach.core.audio.di

import com.aienglishcoach.core.audio.AudioRecorder
import com.aienglishcoach.core.audio.SpeechRecognizerManager
import com.aienglishcoach.core.audio.TextToSpeechManager
import com.aienglishcoach.core.domain.service.AudioRecorderService
import com.aienglishcoach.core.domain.service.SpeechToTextService
import com.aienglishcoach.core.domain.service.TextToSpeechService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    abstract fun bindSpeechToTextService(impl: SpeechRecognizerManager): SpeechToTextService

    @Binds
    abstract fun bindTextToSpeechService(impl: TextToSpeechManager): TextToSpeechService

    @Binds
    abstract fun bindAudioRecorderService(impl: AudioRecorder): AudioRecorderService
}
