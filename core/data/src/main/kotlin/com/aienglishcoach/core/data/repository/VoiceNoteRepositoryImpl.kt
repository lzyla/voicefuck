package com.aienglishcoach.core.data.repository

import com.aienglishcoach.core.common.dispatcher.DispatcherProvider
import com.aienglishcoach.core.data.mapper.toDomain
import com.aienglishcoach.core.data.mapper.toEntity
import com.aienglishcoach.core.database.dao.VoiceNoteDao
import com.aienglishcoach.core.domain.model.VoiceNote
import com.aienglishcoach.core.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceNoteRepositoryImpl @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao,
    private val dispatcherProvider: DispatcherProvider,
) : VoiceNoteRepository {

    override fun observeAll(): Flow<List<VoiceNote>> =
        voiceNoteDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun insert(note: VoiceNote): Long = voiceNoteDao.insert(note.toEntity())

    override suspend fun updateTranscription(id: Long, transcription: String) =
        voiceNoteDao.updateTranscription(id, transcription)

    override suspend fun delete(id: Long) {
        val note = voiceNoteDao.getById(id) ?: return
        voiceNoteDao.deleteById(id)
        withContext(dispatcherProvider.io) {
            val deleted = File(note.audioPath).delete()
            if (!deleted) Timber.w("Audio file for note $id could not be deleted")
        }
    }
}
