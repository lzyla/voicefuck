package com.aienglishcoach.core.domain.usecase.notes

import com.aienglishcoach.core.domain.model.VoiceNote
import com.aienglishcoach.core.domain.repository.VoiceNoteRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

/** Persists a freshly recorded voice memo. */
class SaveVoiceNoteUseCase @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
) {

    suspend operator fun invoke(title: String, audioPath: String, durationSeconds: Int): Long =
        voiceNoteRepository.insert(
            VoiceNote(
                title = title.ifBlank { DEFAULT_TITLE },
                audioPath = audioPath,
                durationSeconds = durationSeconds,
                createdAt = Clock.System.now(),
            ),
        )

    companion object {
        const val DEFAULT_TITLE = "Voice note"
    }
}
