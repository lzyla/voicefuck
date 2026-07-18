package com.aienglishcoach.core.ai.di

import com.aienglishcoach.core.ai.MemoryManager
import com.aienglishcoach.core.ai.OpenAiTutorService
import com.aienglishcoach.core.domain.service.AiTutorService
import com.aienglishcoach.core.domain.service.MemoryRetrievalService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    abstract fun bindAiTutorService(impl: OpenAiTutorService): AiTutorService

    @Binds
    abstract fun bindMemoryRetrievalService(impl: MemoryManager): MemoryRetrievalService
}
