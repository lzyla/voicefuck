package com.aienglishcoach.core.data.di

import com.aienglishcoach.core.common.dispatcher.DefaultDispatcherProvider
import com.aienglishcoach.core.common.dispatcher.DispatcherProvider
import com.aienglishcoach.core.data.repository.ConversationRepositoryImpl
import com.aienglishcoach.core.data.repository.ExerciseRepositoryImpl
import com.aienglishcoach.core.data.repository.MemoryRepositoryImpl
import com.aienglishcoach.core.data.repository.PronunciationRepositoryImpl
import com.aienglishcoach.core.data.repository.SettingsRepositoryImpl
import com.aienglishcoach.core.data.repository.StatisticsRepositoryImpl
import com.aienglishcoach.core.data.repository.UserErrorRepositoryImpl
import com.aienglishcoach.core.data.repository.VocabularyRepositoryImpl
import com.aienglishcoach.core.data.repository.VoiceNoteRepositoryImpl
import com.aienglishcoach.core.datastore.ApiKeyStore
import com.aienglishcoach.core.domain.repository.ConversationRepository
import com.aienglishcoach.core.domain.repository.ExerciseRepository
import com.aienglishcoach.core.domain.repository.MemoryRepository
import com.aienglishcoach.core.domain.repository.PronunciationRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import com.aienglishcoach.core.domain.repository.UserErrorRepository
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import com.aienglishcoach.core.domain.repository.VoiceNoteRepository
import com.aienglishcoach.core.network.interceptor.ApiKeyProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository

    @Binds
    abstract fun bindVocabularyRepository(impl: VocabularyRepositoryImpl): VocabularyRepository

    @Binds
    abstract fun bindUserErrorRepository(impl: UserErrorRepositoryImpl): UserErrorRepository

    @Binds
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    abstract fun bindPronunciationRepository(impl: PronunciationRepositoryImpl): PronunciationRepository

    @Binds
    abstract fun bindVoiceNoteRepository(impl: VoiceNoteRepositoryImpl): VoiceNoteRepository

    @Binds
    abstract fun bindStatisticsRepository(impl: StatisticsRepositoryImpl): StatisticsRepository

    @Binds
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): MemoryRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {

        /**
         * Bridges the encrypted key store into OkHttp's [ApiKeyProvider].
         * `runBlocking` is safe here: OkHttp interceptors run on worker
         * threads and the underlying read is a fast local file access.
         */
        @Provides
        @Singleton
        fun provideApiKeyProvider(apiKeyStore: ApiKeyStore): ApiKeyProvider =
            ApiKeyProvider { runBlocking { apiKeyStore.get() } }
    }
}
