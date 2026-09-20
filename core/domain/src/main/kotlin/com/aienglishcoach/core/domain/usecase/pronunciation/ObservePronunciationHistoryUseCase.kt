package com.aienglishcoach.core.domain.usecase.pronunciation

import com.aienglishcoach.core.domain.model.PronunciationResult
import com.aienglishcoach.core.domain.repository.PronunciationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams recent pronunciation practice results, newest first. */
class ObservePronunciationHistoryUseCase @Inject constructor(
    private val pronunciationRepository: PronunciationRepository,
) {
    operator fun invoke(limit: Int = 50): Flow<List<PronunciationResult>> =
        pronunciationRepository.observeRecent(limit)
}
