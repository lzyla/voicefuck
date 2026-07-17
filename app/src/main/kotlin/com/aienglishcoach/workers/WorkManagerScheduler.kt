package com.aienglishcoach.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.aienglishcoach.core.domain.service.BackgroundScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** [BackgroundScheduler] implemented with WorkManager. */
@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : BackgroundScheduler {

    override fun scheduleMemoryConsolidation(conversationId: Long) {
        val request = OneTimeWorkRequestBuilder<MemoryConsolidationWorker>()
            .setInputData(
                workDataOf(MemoryConsolidationWorker.KEY_CONVERSATION_ID to conversationId),
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MemoryConsolidationWorker.WORK_NAME_PREFIX + conversationId,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    override fun scheduleDailyRevisionReminder() {
        val request = PeriodicWorkRequestBuilder<RevisionReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(4, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RevisionReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
