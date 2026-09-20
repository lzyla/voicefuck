package com.aienglishcoach.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aienglishcoach.core.database.dao.AiMemoryDao
import com.aienglishcoach.core.database.dao.ConversationDao
import com.aienglishcoach.core.database.dao.DailyStatsDao
import com.aienglishcoach.core.database.dao.ExerciseDao
import com.aienglishcoach.core.database.dao.PronunciationDao
import com.aienglishcoach.core.database.dao.UserErrorDao
import com.aienglishcoach.core.database.dao.VocabularyDao
import com.aienglishcoach.core.database.dao.VoiceNoteDao
import com.aienglishcoach.core.database.entity.AiMemoryEntity
import com.aienglishcoach.core.database.entity.ConversationEntity
import com.aienglishcoach.core.database.entity.DailyStatsEntity
import com.aienglishcoach.core.database.entity.ExerciseAttemptEntity
import com.aienglishcoach.core.database.entity.ExerciseEntity
import com.aienglishcoach.core.database.entity.MessageEntity
import com.aienglishcoach.core.database.entity.PronunciationResultEntity
import com.aienglishcoach.core.database.entity.UserErrorEntity
import com.aienglishcoach.core.database.entity.VocabularyItemEntity
import com.aienglishcoach.core.database.entity.VoiceNoteEntity

/**
 * Single Room database of the app — the local source of truth (offline-first).
 * Schema history is exported to `core/database/schemas` for migration tests.
 */
@Database(
    version = 1,
    exportSchema = true,
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        UserErrorEntity::class,
        VocabularyItemEntity::class,
        ExerciseEntity::class,
        ExerciseAttemptEntity::class,
        PronunciationResultEntity::class,
        VoiceNoteEntity::class,
        DailyStatsEntity::class,
        AiMemoryEntity::class,
    ],
)
abstract class CoachDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun userErrorDao(): UserErrorDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun pronunciationDao(): PronunciationDao
    abstract fun voiceNoteDao(): VoiceNoteDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun aiMemoryDao(): AiMemoryDao

    companion object {
        const val NAME = "aienglishcoach.db"
    }
}
