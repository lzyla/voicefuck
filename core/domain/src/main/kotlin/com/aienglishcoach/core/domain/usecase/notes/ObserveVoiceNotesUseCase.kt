package com.aienglishcoach.core.domain.usecase.notes

import com.aienglishcoach.core.domain.model.VoiceNote
import com.aienglishcoach.core.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams all voice memos, newest first. */
class ObserveVoiceNotesUseCase @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
) {
    operator fun invoke(): Flow<List<VoiceNote>> = voiceNoteRepository.observeAll()
}
