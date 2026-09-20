package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.VoiceNote
import kotlinx.coroutines.flow.Flow

/** Storage for voice memos. */
interface VoiceNoteRepository {

    fun observeAll(): Flow<List<VoiceNote>>

    suspend fun insert(note: VoiceNote): Long

    suspend fun updateTranscription(id: Long, transcription: String)

    /** Deletes the note and its audio file. */
    suspend fun delete(id: Long)
}
