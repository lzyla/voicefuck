package com.aienglishcoach.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aienglishcoach.core.database.entity.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {

    @Query("SELECT * FROM voice_notes ORDER BY created_at DESC")
    fun observeAll(): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE id = :id")
    suspend fun getById(id: Long): VoiceNoteEntity?

    @Insert
    suspend fun insert(note: VoiceNoteEntity): Long

    @Query("UPDATE voice_notes SET transcription = :transcription WHERE id = :id")
    suspend fun updateTranscription(id: Long, transcription: String)

    @Query("DELETE FROM voice_notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
