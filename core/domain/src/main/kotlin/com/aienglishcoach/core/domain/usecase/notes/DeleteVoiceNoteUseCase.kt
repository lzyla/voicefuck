package com.aienglishcoach.core.domain.usecase.notes

import com.aienglishcoach.core.domain.repository.VoiceNoteRepository
import javax.inject.Inject

/** Deletes a voice memo together with its audio file. */
class DeleteVoiceNoteUseCase @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
) {
    suspend operator fun invoke(id: Long) = voiceNoteRepository.delete(id)
}
