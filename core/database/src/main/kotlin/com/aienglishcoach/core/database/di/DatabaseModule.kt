package com.aienglishcoach.core.database.di

import android.content.Context
import androidx.room.Room
import com.aienglishcoach.core.database.CoachDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoachDatabase =
        Room.databaseBuilder(context, CoachDatabase::class.java, CoachDatabase.NAME)
            .build()

    @Provides fun provideConversationDao(db: CoachDatabase) = db.conversationDao()
    @Provides fun provideUserErrorDao(db: CoachDatabase) = db.userErrorDao()
    @Provides fun provideVocabularyDao(db: CoachDatabase) = db.vocabularyDao()
    @Provides fun provideExerciseDao(db: CoachDatabase) = db.exerciseDao()
    @Provides fun providePronunciationDao(db: CoachDatabase) = db.pronunciationDao()
    @Provides fun provideVoiceNoteDao(db: CoachDatabase) = db.voiceNoteDao()
    @Provides fun provideDailyStatsDao(db: CoachDatabase) = db.dailyStatsDao()
    @Provides fun provideAiMemoryDao(db: CoachDatabase) = db.aiMemoryDao()
}
