package com.aienglishcoach.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.errorOrNull
import com.aienglishcoach.core.common.result.isSuccess
import com.aienglishcoach.core.domain.usecase.memory.ConsolidateMemoryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Consolidates the AI tutor's long-term memory after a conversation:
 * extraction, dedup, embeddings and pruning. Network failures retry with
 * WorkManager backoff; permanent failures (e.g. missing API key) do not.
 */
@HiltWorker
class MemoryConsolidationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val consolidateMemory: ConsolidateMemoryUseCase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val conversationId = inputData.getLong(KEY_CONVERSATION_ID, -1L)
        if (conversationId <= 0) return Result.failure()

        val result = consolidateMemory(conversationId)
        return when {
            result.isSuccess -> Result.success()
            result.errorOrNull() is AppError.Network && runAttemptCount < MAX_RETRIES ->
                Result.retry()
            else -> {
                Timber.w("Memory consolidation failed: ${result.errorOrNull()}")
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_CONVERSATION_ID = "conversation_id"
        const val WORK_NAME_PREFIX = "memory_consolidation_"
        private const val MAX_RETRIES = 3
    }
}
